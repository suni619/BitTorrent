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


public class Peer {

	public static void main(String[] args) {
		final String portNumber = "13003";
		String peerId = "id_peer3";
		final String baseDir = "peer3/";
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
					// TODO Auto-generated catch block
					e.printStackTrace();
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
			BDecoder bdecoder = new BDecoder();
			peersInfo = bdecoder.decode(response);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// get peers list
		List<Map<String, Object>> peersList = (List<Map<String, Object>>) peersInfo.get("peers");

		// connect to peers and download
		System.out.println(peersList);
//		for (Map<String, Object> peer : peersList) {
//			// if not self
//			if (!((String) peer.get("peer_id")).equals(peerId)){
//				Client client = new Client();
//				String ip = (String) peer.get("ip");
//				String port = (String) peer.get("port");
//				try {
//					client.startClient(ip, port, baseDir + fileName);
//				} catch (IOException e) {
//					System.out.println("Peer not available or might have choked");
//					e.printStackTrace();
//				}
//				
//			}
//		}
		
		// start 
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
		System.out.println(strategyMap);
		
//		List<Map<String, String>> temp = (List<Map<String, String>>) strategyMap.get("0");
//		Map<String, String> map = new HashMap<String, String>();
//		map.put("port", "15000");
//		map.put("parts", "111111");
//		map.put("peer_id", "id_peer5");
//		map.put("ip", "localhost");
//		temp.add(map);
//		strategyMap.put("0", temp);
//		System.out.println(strategyMap);
		
		// get rarest first order
		  
		Map<String, Object> rarestFirstMap = sortByComparator(strategyMap);
		System.out.println(rarestFirstMap);
		
		// get parts
		for (Entry<String, Object> partInfo : rarestFirstMap.entrySet()) {
			String part = partInfo.getKey();
			List<Map<String, String>> partPeers = (List<Map<String, String>>) partInfo.getValue();
			
			int peerNumber = new Random().nextInt(partPeers.size());
			Map<String, String> partPeer = partPeers.get(peerNumber);
			String port = (String) partPeer.get("port");
			String ip = (String) partPeer.get("ip");
			String partPeerId = (String) partPeer.get("peer_id");
			
			// download logic here
			System.out.println("Download part " + part + " from " + partPeerId);
			Client client = new Client();
			try {
				client.startClient(ip, port, baseDir + fileName + ".part" + part, fileName + ".part" + part);
			} catch (IOException e) {
				System.out.println("Peer not available or might have choked");
				e.printStackTrace();
			}
		}
		
		// end
		
		// recreate the file after downloading all the parts
		int numParts = parts.length();
		if (FileUtil.recreateFile(baseDir, fileName, numParts)) {
			System.out.println("Downloaded successfully");
			
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
				BDecoder bdecoder = new BDecoder();
				informInfo = bdecoder.decode(response);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (ProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Informed: " + informInfo);
			
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
