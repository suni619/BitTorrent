import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pixie.bencoding.BDecoder;


public class Peer {

	public static void main(String[] args) {
		final String portNumber = "13002";
		String peerId = "id_peer2";
		String baseDir = "peer2/";
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
					server.startServer(portNumber);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
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
		for (Map<String, Object> peer : peersList) {
			// if not self
			if (!((String) peer.get("peer_id")).equals(peerId)){
				Client client = new Client();
				String ip = (String) peer.get("ip");
				String port = (String) peer.get("port");
				try {
					client.startClient(ip, port, baseDir + fileName);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
		// recreate the file after downloading all the parts
		
		// stop the server
		server.setRunning(false);
	}

}
