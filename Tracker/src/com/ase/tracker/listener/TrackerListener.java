package com.ase.tracker.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Application Lifecycle Listener implementation class TrackerListener
 *
 */
@WebListener
public class TrackerListener implements ServletContextListener {

    /**
     * Default constructor. 
     */
    public TrackerListener() {
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0) {
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent init) {
    	Map<String, Object> peersTable = new HashMap<String, Object>();
    	Map<String, Object> seedersTable = new HashMap<String, Object>();
		Map<String, Object> leechersTable = new HashMap<String, Object>();

    	// at least one seed must be present
    	List<String> peerList = new ArrayList<String>();
    	peerList.add("id_peer1");
    	seedersTable.put("file1", peerList);
    	
    	// seed info in the peers table
    	Map<String, String> peerInfo = new HashMap<String, String>();
    	peerInfo.put("ip", "192.168.1.1");
    	peerInfo.put("port", "13000");
    	peerInfo.put("parts", "111");
    	peersTable.put("id_peer1", peerInfo);
    	
    	// add the hash tables to the application context
    	ServletContext application = init.getServletContext();
    	application.setAttribute("peersTable", peersTable);
    	application.setAttribute("seedersTable", seedersTable);
    	application.setAttribute("leechersTable", leechersTable);
    }
	
}
