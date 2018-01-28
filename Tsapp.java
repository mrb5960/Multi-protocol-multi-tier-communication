import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Tsapp {
	
	public static void main(String[] args) throws Exception {
		// initializing the variables which will help in comparing them with other 
		String time = "blank", username = "blank", password = "blank", protocol = "blank", time_type = "EPOCH";
		
		int uport = 0, tport = 0, hit_port_udp = 0, hit_port_tcp = 0, iterations = 1, set_protocol = 0;
		time = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
		
		// if server is to be started
		if(args[0].equals("-s")){
			for(int i = 1; i < args.length; i++){
				switch(args[i]){
					case "-T":
						time = args[i+1];
						break;
					case "--user":
						username = args[i+1];
						break;
					case "--pass":
						password = args[i+1];
				}
			}
			// get the last two values of arg[] as the udp and tcp port
			uport = Integer.parseInt(args[args.length-2]);
			tport = Integer.parseInt(args[args.length-1]);
			//System.out.println("Server " + time + " " + uport + " " + tport + " " + username + " " + password);
			// start tcp server
			new Thread(new TCPServer(tport, username, password, time)).start();
			// start udp server
			new Thread(new UDPServer(uport, username, password, time)).start();
		}
		// if proxy is to be started
		else if(args[0].equals("-p")){
			// variables used to set the type of protocol that will be used
			int check_udp = 0, check_tcp = 0;
			for(int i = 2; i < args.length; i++){
				switch(args[i]){
				case "-t":
					protocol = "TCP";
					set_protocol = 1;
					break;
				case "--proxy-udp":
					hit_port_udp = Integer.parseInt(args[i+1]);
					check_udp = 1;
					break;
				case "--proxy-tcp":
					hit_port_tcp = Integer.parseInt(args[i+1]);
					check_tcp = 1;
					break;
				default:
					// default protocol will be udp
					if(set_protocol == 0)
						protocol = "UDP";
					// if both udp and tcp ports are provided
					if(check_tcp==1 && check_udp==1)
						protocol = "default";
				}
				
			}
			// get the last two values of arg[] as the udp and tcp port
			uport = Integer.parseInt(args[args.length-2]);
			tport = Integer.parseInt(args[args.length-1]);
			//System.out.println("Proxy " + protocol + " " + hit_port_udp + " " + hit_port_tcp + " " + uport + " " + tport);
			// start the proxy server
			new Proxy(protocol, tport, uport, hit_port_tcp, hit_port_udp);
		}
		// if client is to be started
		else if(args[0].equals("-c")){
			set_protocol = 0;
			for(int i = 2; i < args.length; i++){
				switch(args[i]){
					case "-T":
						time = args[i+1];
						break;
					case "-t":
						protocol = "TCP";
						tport = Integer.parseInt(args[i+1]);
						set_protocol = 1;
						break;
					case "-u":
						protocol = "UDP";
						uport = Integer.parseInt(args[i+1]);
						set_protocol = 1;
						break;
					case "-n":
						iterations = Integer.parseInt(args[i+1]);
						break;
					case "-z":
						time_type = "UTC";
						break;
					case "--user":
						username = args[i+1];
						break;
					case "--pass":
						password = args[i+1];
						break;
					default:
						// default protocol used by the client will be UDP
						if(set_protocol == 0){
							protocol = "UDP";
							// get the last value in arg[] as the udp port
							uport = Integer.parseInt(args[args.length-1]);
						}
				}
				
			}
			//System.out.println("Client " + protocol + " " + time + " " + username + " " + password + " " + uport + " " + tport + " " + iterations);
			if(protocol.equals("UDP")){
				// if no username and password is provided the request is of the type gettime
				if(username.equals("blank") && password.equals("blank")){
					// start udp client
					new Thread(new UDPClient(uport, time, username, password, time_type, "gettime", iterations)).start();
				}
				else{
					// if username and password is provided the request is of the type settime
					// start udp client
					new Thread(new UDPClient(uport, time, username, password, time_type, "settime", iterations)).start();
				}				
			}
			else{
				// if no username and password is provided the request is of the type gettime
				if(username.equals("blank") && password.equals("blank")){
					// start tcp client
					new Thread(new TCPClient(tport, time, username, password, time_type, "gettime", iterations)).start();
				}
				else{
					// if username and password is provided the request is of the type settime
					// start tcp client
					new Thread(new TCPClient(tport, time, username, password, time_type, "settime", iterations)).start();
				}
			}
		}
	}
}
