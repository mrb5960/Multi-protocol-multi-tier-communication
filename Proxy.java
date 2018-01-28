import java.io.*;
import java.net.*;

public class Proxy {
	static int tport_hit, uport_hit;
	Proxy(String protocol, int tport_listen, int uport_listen, int tport_hit, int uport_hit) throws Exception{
		// get details from Tsapp
		Proxy.tport_hit = tport_hit;
		Proxy.uport_hit = uport_hit;
		// start threads for listening to the requests from the clients
		new Thread(new TCPListener(tport_listen, protocol)).start();
		new Thread(new UDPListener(uport_listen, protocol)).start();
	}
}

// Proxy server that listens to the TCP client requests
class TCPListener implements Runnable{
	int port;
	String proxy_protocol;
	ServerSocket soc;
	TCPListener(int port, String protocol){
		// set port and protocol that will be used by the proxy to contact the server
		this.port = port;
		this.proxy_protocol = protocol;
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
				System.out.println("Proxy TCP Server started at port " + port);
				// wait for clients to connect
				Socket s = soc.accept();
				//System.out.println("Connection established with proxy");
				// used to serialize and de serialize objects
				ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				Packet packet_from_client = (Packet)ois.readObject();
				// if proxy is using TCP protocol or the one used by the client to connect to the server 
				if(proxy_protocol.equals("default") || proxy_protocol.equals("TCP")){
					// socket to connect to the server
					Socket hit = new Socket("localhost", Proxy.tport_hit);
					// used to send objects to the server
					ObjectOutputStream oos_hit = new ObjectOutputStream(hit.getOutputStream());
					// send packet object to the server
					oos_hit.writeObject(packet_from_client);
					ObjectInputStream ois_hit = new ObjectInputStream(hit.getInputStream());
					// get response from the server
					Response packet_from_server = (Response)ois_hit.readObject();
					// increase the number of hops
					packet_from_server.hops++;
					//System.out.println("Response " + packet_from_server.auth);
					// send the response to the client
					oos.writeObject(packet_from_server);
					hit.close();
				}
				// proxy is using UDP to connect to the server
				else{
					// buffers used to send and receive packets from the server
					byte[] outbuffer = new byte[1000];
					byte[] inbuffer = new byte[1000];
					// udp socket to connect to the server
					DatagramSocket hit = new DatagramSocket();
					InetAddress server_address = InetAddress.getByName("localhost");
					// used to serialize and de serialize objects
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					//ObjectOutputStream oos_hit = new ObjectOutputStream(bos);
					outbuffer = bos.toByteArray();
					DatagramPacket out_packet = new DatagramPacket(outbuffer, outbuffer.length, server_address, Proxy.uport_hit);
					// send packet to the server
					hit.send(out_packet);
					DatagramPacket in_packet = new DatagramPacket(inbuffer, inbuffer.length);
					// receive response from the server
					hit.receive(in_packet);
					ObjectInputStream ois_hit = new ObjectInputStream(new ByteArrayInputStream(in_packet.getData()));
					Response packet_from_server = (Response)ois_hit.readObject();
					// increase the number of hops
					packet_from_server.hops++;
					// send packet to the client
					oos.writeObject(packet_from_server);
					hit.close();
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}

//Proxy server that listens to the TCP client requests
class UDPListener implements Runnable{
	int port;
	String proxy_protocol;
	UDPListener(int port, String protocol){
		// set port and protocol that will be used by the proxy to contact the server
		this.port = port;
		this.proxy_protocol = protocol;
	}
	public void run(){
		try {
			System.out.println("Proxy UDP Server started at port " + port);
			// socket to receive datagrams from the client
			DatagramSocket soc = new DatagramSocket(port);
			DatagramPacket inpacket, outpacket;
			InetAddress client_address;
			int client_port;
			while(true){
				// buffers to handle request and response packets
				byte[] inbuffer = new byte[1000];
				byte[] outbuffer = new byte[1000];
				inpacket = new DatagramPacket(inbuffer,inbuffer.length);
				// receive packet from the client
				soc.receive(inpacket);
				//System.out.println("Packet received at proxy");
				// get the address of the client
				client_address = inpacket.getAddress();
				// get the port of the client where packets are to be delivered
				client_port = inpacket.getPort();
				// get the request packet from the client
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(inpacket.getData()));
				Packet in = (Packet)ois.readObject();
				// used for serializing and de serializing of objects
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				// if proxy is using UDP protocol or the one used by the client to connect to the server
				if(proxy_protocol.equals("default") || proxy_protocol.equals("UDP")){
					// buffers to handle request and response packets 
					byte[] outbuf = new byte[1000];
					byte[] inbuf = new byte[1000];
					// socket to connect with the server
					DatagramSocket hit = new DatagramSocket();
					InetAddress server_address = InetAddress.getByName("localhost");
					// used for serializing and de serializing objects
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ObjectOutputStream oos_hit = new ObjectOutputStream(baos);
					// serialize the object
					oos_hit.writeObject(in);
					outbuf = baos.toByteArray();
					DatagramPacket out_packet = new DatagramPacket(outbuf, outbuf.length, server_address, Proxy.uport_hit);
					// send object to the server
					hit.send(out_packet);
					//System.out.println("Packet sent to server");
					DatagramPacket in_packet = new DatagramPacket(inbuf, inbuf.length);
					// receive response from the server
					hit.receive(in_packet);
					//System.out.println("Packet received from server");
					ObjectInputStream ois1 = new ObjectInputStream(new ByteArrayInputStream(in_packet.getData()));
					Response packet_from_server = (Response)ois1.readObject();
					// increasing the number of hops
					packet_from_server.hops++;
					//System.out.println("Response " + packet_from_server.auth);
					baos = new ByteArrayOutputStream();
					oos_hit = new ObjectOutputStream(baos);
					oos_hit.writeObject(packet_from_server);
					outbuf = baos.toByteArray();
					DatagramPacket out = new DatagramPacket(outbuf, outbuf.length, client_address, client_port);
					// send response to the client
					soc.send(out);
					hit.close();
					//System.out.println("Packet sent to client");
				}
				// proxy is using TCP to connect to the server
				else{
					// socket to connect with the server
					Socket hit = new Socket("localhost", Proxy.tport_hit);
					ObjectOutputStream oos_hit = new ObjectOutputStream(hit.getOutputStream());
					// send request to the server
					oos_hit.writeObject(in);
					//System.out.println("Packet sent from proxy TCP to server");
					ObjectInputStream ois_hit = new ObjectInputStream(hit.getInputStream());
					// receive response from the server
					Response packet_from_server = (Response)ois_hit.readObject();
					// increasing number of hops
					packet_from_server.hops++;
					//System.out.println("Packet received from server at TCP proxy");
					oos.writeObject(packet_from_server);
					outbuffer = bos.toByteArray();
					outpacket = new DatagramPacket(outbuffer, outbuffer.length, client_address, client_port);
					// send response to the client
					soc.send(outpacket);
					hit.close();
				}
				//hit.close();
				//soc.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

