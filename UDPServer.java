import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;

// server that handles the UDP requests from the clients
public class UDPServer implements Runnable{
	int port;
	UDPServer(int port, String uname, String pass, String time){
		this.port = port;
		// authentication and time is stored on the server
		ServerRecords.username = uname;
		ServerRecords.password = pass;
		ServerRecords.server_time = time;
		System.out.println("UDP server started at port " + port);
	}
	public void run(){
		try {
			DatagramSocket soc = new DatagramSocket(port);
			// to handle incoming and outgoing packets separately
			DatagramPacket inpacket, outpacket;
			InetAddress client_address;
			int client_port;
			while(true){
				// buffers to handle request and response
				byte[] inbuffer = new byte[1000];
				byte[] outbuffer = new byte[1000];
				inpacket = new DatagramPacket(inbuffer,inbuffer.length);
				soc.receive(inpacket);
				// getting address of the client from the incoming packet
				client_address = inpacket.getAddress();
				// getting the port number of the client where the packets are to be delivered
				client_port = inpacket.getPort();
				// reading a serialized object from the input stream
				// get the array of bytes from the packet which is the input to ByteArrayInputStream
				// deserializes the object serialized by ObjectOutputStream
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(inpacket.getData()));
				// receive object from the client
				Packet in = (Packet)ois.readObject();
				// used to serialize and de serialize objects
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				
				// if request is of the type gettime
				if(in.request_type.equals("gettime")){	
					Response out = new Response();
					// gettime from the server
					if(in.format.equals("UTC"))
						out.send_time = ServerRecords.server_time_utc;
					else
						out.send_time = ServerRecords.server_time;
					oos.writeObject(out);
					outbuffer = bos.toByteArray();
					outpacket = new DatagramPacket(outbuffer, outbuffer.length, client_address, client_port);
					// send packet to the client
					soc.send(outpacket);
					//soc.close();
				}
				// if request is of the type settime
				else{
					// if username and password match to that of the server
					if(in.username.equals(ServerRecords.username) && in.password.equals(ServerRecords.password)){
						ServerRecords.server_time = in.time;
						int unixtime = Integer.parseInt(ServerRecords.server_time);
						ServerRecords.server_time_utc = new SimpleDateFormat("MM/dd/yy HH:mm:ss").format(unixtime*1000);
						Response out = new Response();
						out.auth = "Time set successfully!";
						oos.writeObject(out);
						outbuffer = bos.toByteArray();
						outpacket = new DatagramPacket(outbuffer, outbuffer.length, client_address, client_port);
						soc.send(outpacket);
						//soc.close();
						System.out.println("Time set by client to " + ServerRecords.server_time);
					}
					// if username and password do not match to that of the server
					else{
						// create response packet
						Response out = new Response();
						out.auth = "Invalid username or password!";
						oos.writeObject(out);
						outbuffer = bos.toByteArray();
						outpacket = new DatagramPacket(outbuffer, outbuffer.length, client_address, client_port);
						// send packet to the client
						soc.send(outpacket);
						//soc.close();
					}
				}
				//soc.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
