import java.io.*;
import java.net.*;

public class UDPClient implements Runnable{
	int port, iterations;
	String username = null, password = null, format = null, request_type, time_to_set;
	UDPClient(int port, String tts, String uname, String pass, String format, String req, int iterations){
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
		// buffers to handle request and response packets
		byte[] inbuffer = new byte[1000];
		byte[] outbuffer = new byte[1000];
		DatagramPacket inpacket, outpacket;
		try {
			System.out.println("##############################################################");
			System.out.println("UDP client started");
			long start, end, duration;
			DatagramSocket soc = new DatagramSocket();
			// get the address of the server i.e. localhost
			InetAddress server_address = InetAddress.getByName("localhost");
			Packet out = new Packet();
			out.username = username;
			out.password = password;
			out.format = format;
			out.request_type = request_type;
			out.time = time_to_set;
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			if(request_type.equals("gettime")){
				System.out.println("UDP client requesting server for time");
				// getting time for n number of times
				for(int i = 0; i < iterations; i++){
					// serialize and send packet to the server
					oos.writeObject(out);
					outbuffer = os.toByteArray();
					outpacket = new DatagramPacket(outbuffer, outbuffer.length, server_address, port);
					start = System.currentTimeMillis();
					soc.send(outpacket);
					inpacket = new DatagramPacket(inbuffer,inbuffer.length);
					// receive packet from the server
					soc.receive(inpacket);
					end = System.currentTimeMillis();
					ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(inpacket.getData()));
					// de serializing the packet received from the server
					Response in = (Response)ois.readObject();
					//soc.close();
					// displaying the round trip time and the number of hops
					//System.out.println("Packet received at client");
					System.out.println("The time is " + in.send_time);
					duration = end - start;
					System.out.println("RTT: " + duration + " milliseconds");
					System.out.println("Number of hops = " + in.hops);
					System.out.println("##############################################################");
				}
			}
			if(request_type.equals("settime")){
				System.out.println("UDP client modifying time ");
				// serialize and send packet to the server
				oos.writeObject(out);
				outbuffer = os.toByteArray();
				outpacket = new DatagramPacket(outbuffer, outbuffer.length, server_address, port);
				start = System.currentTimeMillis();
				soc.send(outpacket);
				inpacket = new DatagramPacket(inbuffer,inbuffer.length);
				// receive packet from the server
				soc.receive(inpacket);
				end = System.currentTimeMillis();
				// de serializing the packet received from the server
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(inpacket.getData()));
				Response in = (Response)ois.readObject();
				//soc.close();
				// displaying the round trip time and the number of hops
				System.out.println(in.auth);
				duration = end - start;
				System.out.println("RTT: " + duration + " milliseconds");
				System.out.println("Number of hops = " + in.hops);
				System.out.println("##############################################################");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
