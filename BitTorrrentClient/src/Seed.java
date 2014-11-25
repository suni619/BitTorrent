import java.io.IOException;


public class Seed {

	public static void main(String[] args) {
		String portNumber = "13001";
		
		// start server
		Server server = new Server();
		try {
			server.startServer(portNumber, "seed/");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
