package com.ase.model;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.io.IOUtils;


public class Client {

	public void startClient(String ip, String portNumber, String destFilePath, String downloadFile) throws UnknownHostException, IOException {
		int port = Integer.parseInt(portNumber);
		Socket socket = new Socket(ip, port);
//		System.out.println("Client connected to server");
		DataOutputStream askFile = new DataOutputStream(socket.getOutputStream());
		askFile.writeUTF(downloadFile);
		InputStream is = socket.getInputStream();
		OutputStream os = new FileOutputStream(destFilePath);
		IOUtils.copy(is, os);
		socket.close();
//		System.out.println("Received file from server");
	}
	
	public static void main(final String[] args) {
		Thread clientThread = new Thread(new Runnable(){

			@Override
			public void run() {
				Client client = new Client();
				try {
					client.startClient(args[0], args[1], args[2], args[3]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		});
		clientThread.setDaemon(true);
		clientThread.start();
	}

}
