import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.io.IOUtils;

public class Server {
	
	private boolean running = true;

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean getRunning() {
		return this.running;
	}

	public void startServer(String portNumber) throws IOException {
		int port = Integer.parseInt(portNumber);
		ServerSocket server = new ServerSocket(port);
		System.out.println("Server started");
		while (getRunning()) {
			Socket client = server.accept();
			System.out.println("Client connected");
			OutputStream os = client.getOutputStream();
			InputStream in = new FileInputStream("seed/sample.ppt");
			IOUtils.copy(in, os);
			client.close();
			System.out.println("Server sent file to client");
		}
		server.close();
	}

	public static void main(String[] args) {
		Server server = new Server();
		try {
			server.startServer("13000");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
