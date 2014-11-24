import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.io.IOUtils;


public class Client {

	public void startClient(String ip, String portNumber, String destFilePath) throws UnknownHostException, IOException {
		int port = Integer.parseInt(portNumber);
		Socket socket = new Socket(ip, port);
		System.out.println("Client connected to server");
		InputStream is = socket.getInputStream();
		OutputStream os = new FileOutputStream(destFilePath);
		IOUtils.copy(is, os);
		socket.close();
		System.out.println("Received file from server");
	}
	
	public static void main(String[] args) {
		Client client = new Client();
		try {
			client.startClient("localhost", "13000", "received_sample.ppt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
