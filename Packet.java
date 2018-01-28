import java.io.Serializable;

// request packet
class Packet implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String time, username, password, request_type, format;
}

// response packet
class Response implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String send_time, auth;
	int hops;
}
