package com.ase.utility;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import org.pixie.bencoding.BDecoder;

public class TrackerUtil {

	public static final int CHUNK_SIZE = 256000;

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
			
			String response = getTrackerResponse(file, infoHash, peerId, port,
					uploaded, downloaded, left, event, parts);
			
			Map<String, Object> map = BDecoder.decode(response);
			System.out.println(map);
		} catch (MalformedURLException e) {
			System.out.println(e.getLocalizedMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e.getLocalizedMessage());
			e.printStackTrace();
		}
	
	}

	public static String getTrackerResponse(String trackerUrl, String infoHash,
			String peerId, String port, String uploaded, String downloaded,
			String left, String event, String parts)
			throws UnsupportedEncodingException, MalformedURLException,
			IOException, ProtocolException {
		
		String encodeType = "UTF-8";
		String queryString = 
				"&info_hash=" + URLEncoder.encode(infoHash, encodeType) +
				"&peer_id=" + URLEncoder.encode(peerId, encodeType) +
				"&port=" + URLEncoder.encode(port, encodeType) +
				"&uploaded=" + URLEncoder.encode(uploaded, encodeType) +
				"&downloaded=" + URLEncoder.encode(downloaded, encodeType) +
				"&left=" + URLEncoder.encode(left, encodeType) +
				"&parts=" + URLEncoder.encode(parts, encodeType) +
				"&event=" + URLEncoder.encode(event, encodeType);
		
//			System.out.println(baseUrl + queryString);
		URL url = new URL(trackerUrl + queryString);
		
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
//		System.out.println(response);
		return response;
	}

}
