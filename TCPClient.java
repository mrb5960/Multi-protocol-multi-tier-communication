import java.io.*;
import java.net.*;

public class TCPClient implements Runnable{
	int port, iterations;
	String username, password, format, request_type, time_to_set;
	
	TCPClient(int port, String tts, String uname, String pass, String format, String req, int iterations){
		// get details from Tsapp
		this.port = port;
		this.username = uname;
		this.password = pass;
		this.format = format;
		this.request_type = req;
		this.iterations = iterations;
		this.time_to_set = tts;
	}
	
	public void run(){
		try {
			System.out.println("##############################################################");
			System.out.println("TCP Client started");
			// variables to calculate rtt
			long start, end, duration;
			if(request_type.equals("gettime")){
				System.out.println("TCP client requesting server for time");
				// request sent for n iterations
				for(int i = 0; i < iterations; i++){
				Socket s = new Socket("localhost", port);
				//System.out.println("CLient started...");
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				// add data to the packet that is to be sent to the server
				Packet outpacket = new Packet();
				outpacket.username = username;
				outpacket.password = password;
				outpacket.format = format;
				outpacket.request_type = request_type;
				outpacket.time = time_to_set;
				// serializing the object and sending it to the server
				oos.writeObject(outpacket);
				ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
						start = System.currentTimeMillis();
						oos.writeObject(outpacket);
						//System.out.println("packet sent");
						Response inpacket = new Response();
						// getting the response from the server
						inpacket = (Response)ois.readObject();
						//s.close();
						//displaying time and the number of hops
						System.out.println("The time is: " + inpacket.send_time);
						end = System.currentTimeMillis();
						duration = end - start;
						System.out.println("RTT: " + duration + " milliseconds");
						System.out.println("Number of hops = " + inpacket.hops);
						System.out.println("##############################################################");
				}
			}
			else{
				System.out.println("TCP client modifying time");
				Socket s = new Socket("localhost", port);
				//System.out.println("CLient started...");
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				Packet outpacket = new Packet();
				outpacket.username = username;
				outpacket.password = password;
				outpacket.format = format;
				outpacket.request_type = request_type;
				outpacket.time = time_to_set;
				oos.writeObject(outpacket);
				ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
				start = System.currentTimeMillis();
				oos.writeObject(outpacket);
				//System.out.println("packet sent");
				Response inpacket = (Response)ois.readObject();
				end = System.currentTimeMillis();
				duration = end - start;
				System.out.println("RTT: " + duration + " milliseconds");
				System.out.println("Number of hops = " + inpacket.hops);
				System.out.println("##############################################################");
				//s.close();
				System.out.println(inpacket.auth);
			}
		} catch(Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
