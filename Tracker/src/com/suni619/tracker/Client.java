package com.suni619.tracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class Client {

	public static void main(String[] args) {
		try {
			String file = "file1";
			String infoHash = "infohash";
			String peerId = "id_peer2";
			String port = "13002";
			String uploaded = "0";
			String downloaded = "0";
			String left = "3";
			String event = "started";
			String parts = "000";
			
			String baseUrl = "http://localhost:8080/Tracker/rest.tracker?";
			String encodeType = "UTF-8";
			String queryString = "file=" + URLEncoder.encode(file, encodeType) +
					"&info_hash=" + URLEncoder.encode(infoHash, encodeType) +
					"&peer_id=" + URLEncoder.encode(peerId, encodeType) +
					"&port=" + URLEncoder.encode(port, encodeType) +
					"&uploaded=" + URLEncoder.encode(uploaded, encodeType) +
					"&downloaded=" + URLEncoder.encode(downloaded, encodeType) +
					"&left=" + URLEncoder.encode(left, encodeType) +
					"&parts=" + URLEncoder.encode(parts, encodeType) +
					"&event=" + URLEncoder.encode(event, encodeType);
			
//			System.out.println(baseUrl + queryString);
			URL url = new URL(baseUrl + queryString);
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "text/plain");
//			System.out.println("Response code: " + connection.getResponseCode());
			if (connection.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ connection.getResponseCode());
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String response;
			response = reader.readLine();
			System.out.println(response);
		} catch (MalformedURLException e) {
			System.out.println(e.getLocalizedMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e.getLocalizedMessage());
			e.printStackTrace();
		}
		
	}

}
