package com.ase.app;
import java.io.IOException;

import com.ase.model.Server;


public class Seed {

	public static void main(String[] args) {
		String portNumber = "13001";
		
		// start server
		Server server = new Server();
		try {
			server.startServer(portNumber, "seed/");
		} catch (IOException e) {
			System.out.println("Problem with creating seed.");
		}
	}

}
