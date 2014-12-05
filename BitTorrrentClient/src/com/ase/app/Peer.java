package com.ase.app;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import org.pixie.bencoding.BDecoder;

import com.ase.model.Client;
import com.ase.model.Server;
import com.ase.utility.FileUtil;
import com.ase.utility.TrackerUtil;


public class Peer {

	@SuppressWarnings({ "unchecked", "unused" })
	public static void main(String[] args) {
		String peerConfig = "2";
		if (args.length == 1){
			peerConfig = args[0];
		} else if (args.length == 0) {
			// OK
		} else {
			// invalid usage
			System.out.println("Invalid command");
			System.out.println("Usage: java -jar Peer.java <Peer number>");
			System.exit(0);
		}
		final String portNumber = "1300" + peerConfig;
		String peerId = "id_peer" + peerConfig;
		final String baseDir = "peer" + peerConfig + "/";
		String fileName = "sample.ppt";
		String torrentName = fileName + ".torrent";
		
		String infoHash = "infohash";
		String uploaded = "0";
		String downloaded = "0";
		String left = "6";
		String event = "started";
		String parts = "000000";
		
		// start server
		final Server server = new Server();
		Thread serverThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					server.startServer(portNumber, baseDir);
				} catch (IOException e) {
					System.out.println("There is a problem with creating server");
				}
			}
		});
//		serverThread.setDaemon(true);
		serverThread.start();
		
		// read torrentfile
		Map<String, Object> metainfo = FileUtil.readMetaInfoFile(baseDir + torrentName);
		// get tracker url
		String trackerUrl = (String) metainfo.get("url");
	
		Map<String, Object> peersInfo = new HashMap<String, Object>();
		// connect to tracker and get response
		try {
			String response = TrackerUtil.getTrackerResponse(trackerUrl, infoHash, peerId, portNumber,
					uploaded, downloaded, left, event, parts);
			peersInfo = BDecoder.decode(response);
		} catch (UnsupportedEncodingException e) {
			System.out.println("Decoding failed");
		} catch (MalformedURLException e) {
			System.out.println("Decoding failed");
		} catch (ProtocolException e) {
			System.out.println("Decoding failed");
		} catch (IOException e) {
			System.out.println("Decoding failed");
		}
		
		// get peers list
		List<Map<String, Object>> peersList = (List<Map<String, Object>>) peersInfo.get("peers");

		// connect to peers and download
		System.out.println("Peers in swarm: " + peersList);
		
		// piece download strategy
		Map<String, Object> strategyMap = new TreeMap<String, Object>();
		for (Map<String, Object> peer : peersList) {
			// if not self
			if (!((String) peer.get("peer_id")).equals(peerId)) {
				String partsBits = (String) peer.get("parts");
				for (int i = 0; i < partsBits.length(); i++) {
					// if the peer has that part
					if ( partsBits.charAt(i) == '1') {
						List<Map<String, Object>> peerList = (List<Map<String, Object>>) strategyMap.get(i+"");
						if (peerList == null) {
							peerList = new ArrayList<Map<String,Object>>();
						}
						peerList.add(peer);
						strategyMap.put(i+"", peerList);
					}
				}
			}
		}
		System.out.println("General piece download strategy: " + strategyMap);
		
		// get rarest first order
		  
		Map<String, Object> rarestFirstMap = sortByComparator(strategyMap);
		System.out.println("Rarest first strategy" + rarestFirstMap);
		
		// get parts
		for (Entry<String, Object> partInfo : rarestFirstMap.entrySet()) {
			String part = partInfo.getKey();
			List<Map<String, String>> partPeers = (List<Map<String, String>>) partInfo.getValue();
			
			boolean downloadFailed;
			do {
				downloadFailed = false;
				int peerNumber = new Random().nextInt(partPeers.size());
				Map<String, String> partPeer = partPeers.get(peerNumber);
				String port = (String) partPeer.get("port");
				String ip = (String) partPeer.get("ip");
				String partPeerId = (String) partPeer.get("peer_id");
				
				// download logic 
				System.out.println("Download part " + part + " from " + partPeerId);
				Client client = new Client();
				try {
					client.startClient(ip, port, baseDir + fileName + ".part" + part, fileName + ".part" + part);
				} catch (IOException e) {
					System.out.println("Peer not available or might have choked");
					downloadFailed = true;
				}
			} while(downloadFailed);
		}
		
		// recreate the file after downloading all the parts
		int numParts = parts.length();
		if (FileUtil.recreateFile(baseDir, fileName, numParts)) {
			System.out.println(fileName + " downloaded.");
			
			// inform tracker
			Map<String, Object> informInfo = new HashMap<String, Object>();
			// connect to tracker and get response
			try {
				uploaded = "0";
				downloaded = "6";
				left = "0";
				event = "completed";
				parts = "111111";
				String response = TrackerUtil.getTrackerResponse(trackerUrl, infoHash, peerId, portNumber,
						uploaded, downloaded, left, event, parts);
				informInfo = BDecoder.decode(response);
			} catch (UnsupportedEncodingException e) {
				System.out.println("Decoding failed");
			} catch (MalformedURLException e) {
				System.out.println("Decoding failed");
			} catch (ProtocolException e) {
				System.out.println("Decoding failed");
			} catch (IOException e) {
				System.out.println("Decoding failed");
			}
//			System.out.println("Informed: " + informInfo);
			
		} else {
			System.out.println("Download error: All parts not downloaded.");
		}
		
		// stop the server
//		server.setRunning(false);
	}
	
	private static Map<String, Object> sortByComparator(Map<String, Object> unsortMap)
    {

        List<Entry<String, Object>> list = new LinkedList<Entry<String, Object>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Object>>()
        {
            @SuppressWarnings("unchecked")
			public int compare(Entry<String, Object> o1,
                    Entry<String, Object> o2)
            {
                    return ((Integer)((List<Map<String, Object>>)o1.getValue()).size()).compareTo((Integer)((List<Map<String, Object>>)o2.getValue()).size());
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Object> sortedMap = new LinkedHashMap<String, Object>();
        for (Entry<String, Object> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

}
