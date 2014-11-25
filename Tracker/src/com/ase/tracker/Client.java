package com.ase.tracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import org.pixie.bencoding.BDecoder;

public class Client {

	public static void main(String[] args) {
		try {
			String file = "sample.ppt";
			String infoHash = "infohash";
			String peerId = "id_peer2";
			String port = "13002";
			String uploaded = "0";
			String downloaded = "0";
			String left = "0";
			String event = "completed";
			String parts = "111111";
			
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
			
		       String bstring = "8:failInfo6:Reason8:warnInfo7:Message8:interval2:608:complete1:16:incomp1:15:peersld7:peer_id8:id_peer12:ip9:localhost4:port5:13001ed7:peer_id8:id_peer12:ip9:localhost4:port5:13001ee";
			BDecoder bdecoder = new BDecoder();
			Map<String, Object> map = bdecoder.decode(response);
			System.out.println(map);
		} catch (MalformedURLException e) {
			System.out.println(e.getLocalizedMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e.getLocalizedMessage());
			e.printStackTrace();
		}
		
	}

}
