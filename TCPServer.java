import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
//server that handles the TCP requests from the clients
public class TCPServer implements Runnable{
	int port;
	ServerSocket soc;
	TCPServer(int port, String uname, String pass, String time){
		this.port = port;
		// authentication and time is stored on the server
		ServerRecords.username = uname;
		ServerRecords.password = pass;
		ServerRecords.server_time = time;
		int unixtime = Integer.parseInt(ServerRecords.server_time);
		ServerRecords.server_time_utc = new SimpleDateFormat("MM/dd/yy HH:mm:ss").format(unixtime*100);
		System.out.println("TCP server started at port " + port);
	}
	
	public void run(){
		
		try {
			soc = new ServerSocket(port);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(true){
			try{
				Socket s = soc.accept();
				//System.out.println("Connection established with server");
				ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
				Packet inpacket = (Packet)ois.readObject();
				//System.out.println("Packet received at server");
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				
				// if request is of the type gettime
				if(inpacket.request_type.equals("gettime")){
					Response outpacket = new Response();
					if(inpacket.format.equals("UTC"))
						outpacket.send_time = ServerRecords.server_time_utc;
					else
						outpacket.send_time = ServerRecords.server_time;
					oos.writeObject(outpacket);
				}
				
				// if request is of the time settime
				else{
					// if username and password match to that of the server
					if(inpacket.username.equals(ServerRecords.username) && inpacket.password.equals(ServerRecords.password)){
						ServerRecords.server_time = inpacket.time;
						int unixtime = Integer.parseInt(ServerRecords.server_time);
						ServerRecords.server_time_utc = new SimpleDateFormat("MM/dd/yy HH:mm:ss").format(unixtime*1000);
						Response outpacket = new Response();
						outpacket.auth = "Time set successfully!";
						oos.writeObject(outpacket);
						System.out.println("Time set by client to " + ServerRecords.server_time);
					}
					// if username and password do not match to that of the server
					else{
						Response out = new Response();
						out.auth = "Invalid username or password!";
						oos.writeObject(out);
					}
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
