package com.ase.tracker;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ase.tracker.util.BencodeUtil;

/**
 * Servlet implementation class TrackerController
 */
@WebServlet("*.tracker")
public class TrackerController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public TrackerController() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String file = request.getParameter("file");
		String infoHash = request.getParameter("info_hash");
		String peerId = request.getParameter("peer_id");
		String port = request.getParameter("port");
		String uploaded = request.getParameter("uploaded");
		String downloaded = request.getParameter("downloaded");
		String left = request.getParameter("left");
		String event = request.getParameter("event");
		String parts = request.getParameter("parts");
		String ip = "localhost";

		// process received info
		System.out.println("Received: " + file + " " + infoHash + " " + peerId
				+ " " + port + " " + uploaded + " " + downloaded + " " + left
				+ " " + event + " " + parts + " ");

		// retrieve all the hash tables
		ServletContext application = getServletContext();
		Map<String, Object> peersTable = (Map<String, Object>) application
				.getAttribute("peersTable");
		Map<String, Object> seedersTable = (Map<String, Object>) application
				.getAttribute("seedersTable");
		Map<String, Object> leechersTable = (Map<String, Object>) application
				.getAttribute("leechersTable");

		if (event.equals("started")) {
			// create an entry in peers table
			Map<String, String> peerMap = new HashMap<String, String>();
			peerMap.put("ip", ip);
			peerMap.put("port", port);
			peerMap.put("parts", parts);
			peersTable.put(peerId, peerMap);
				
			// update leechers table to add a new leecher
			System.out.println("updating leechers table");
			List<String> leechers = (List<String>) leechersTable.get(file);
			if (leechers == null) {
				leechers = new ArrayList<String>();
			}
			if (!leechers.contains(peerId)) {
				leechers.add(peerId);
			}
			leechersTable.put(file, leechers);
			
		} else if (event.equals("stopped")) {
			System.out.println("handling stop event");

		} else if (event.equals("completed")) {
			// update seeders table
			System.out.println("updating seeders table");
			List<String> seeders = (List<String>) seedersTable.get(file);
			seeders.add(peerId);
			seedersTable.put(file, seeders);

		} else {
			// update peers table for parts
			System.out.println("Updating peers table");
			((Map<String, String>) peersTable.get(peerId)).put("parts", parts);

		}

		// create tracker response
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap.put("failInfo", "Reason");
		responseMap.put("warnInfo", "Message");
		responseMap.put("interval", 60 + "");
		responseMap.put("complete", seedersTable.size() + "");
		responseMap.put("incomp", leechersTable.size() + "");

		// get the list of peers with the file required
		List<String> peersWithFile = new ArrayList<String>();
		peersWithFile.addAll((List<String>)seedersTable.get(file));
		peersWithFile.addAll((List<String>) leechersTable.get(file));
		
		// get the peer info for all the peers having that form the swarm
		List<Object> peersList = new ArrayList<Object>();
		Map<String, Object> peerMap = new HashMap<String, Object>();

		for (String peer : peersWithFile) {
			peerMap = (Map<String, Object>) peersTable.get(peer);
			peerMap.put("peer_id", peer);
			peersList.add(peerMap);
		}
		
		responseMap.put("peers", peersList);
		System.out.println("Response map: " + responseMap);
	
		// get the bencoded response
		String bencodedResponse = BencodeUtil.bencode(responseMap);

		// send the response to the client
		PrintWriter out = response.getWriter();
		response.setContentType("text/plain");
		out.write(bencodedResponse);
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
