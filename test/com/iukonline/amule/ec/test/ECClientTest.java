/**
 * 
 */
package com.iukonline.amule.ec.test;

import static org.junit.Assert.assertTrue;

import java.net.Socket;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.iukonline.amule.ec.ECCategory;
import com.iukonline.amule.ec.ECClient;
import com.iukonline.amule.ec.ECCodes;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECStats;

/**
 * @author ***REMOVED***
 *
 */
public class ECClientTest {

    
    final static String SERVER_HOST = "***REMOVED***";
    final static int SERVER_PORT = 4712;
    final static String SERVER_PASSWORD = "***REMOVED***";
    
    static ECClient cl;
    static Socket socket;
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        cl = new ECClient();
        try {
            cl.setPassword(SERVER_PASSWORD);
        } catch (Exception e) {
            System.err.print("Error while generating password hash: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        cl.setClientName("EC Testing");
        cl.setClientVersion("pre-aplha");
        cl.setTracer(System.out);
        
        socket = new Socket(SERVER_HOST, SERVER_PORT);
        cl.setSocket(socket);
        
        //cl.enableUTF8Compression();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void fetchDlQueue() throws Exception {
        
        
        System.out.print("Running fetchDlQueue...\n");
        ECPartFile[] dlQueue = cl.getDownloadQueue(ECCodes.EC_DETAIL_CMD);
        
        if (dlQueue != null) {
            for (int i = 0; i < dlQueue.length; i++) {
                
                //ECPartFile d = cl.getDownloadDetails(dlQueue[i]);
                
                System.out.println(dlQueue[i].toString());
                cl.refreshPartFile(dlQueue[i], ECCodes.EC_DETAIL_CMD);
                
                
                
                
               /* System.out.format("Filename: %s\nHash: %s\nStatus: %d - Done: %d/%d - Speed: %d - Prio: %d\n\n", 
                                dlQueue[i].getFileName(), 
                                ECUtils.byteArrayToHexString(dlQueue[i].getHash(), 16),
                                dlQueue[i].getStatus(),
                                dlQueue[i].getSizeDone(), 
                                dlQueue[i].getSizeFull(),
                                dlQueue[i].getSpeed(),
                                dlQueue[i].getPrio()
                                );*/
            }
            
            //dlQueue[0].swapA4AFThis();
            //dlQueue[0].changePriority(dlQueue[0].getPrio());
            
            for (int i = 0; i < dlQueue.length; i++) {
                if (dlQueue[i].getSourceNames().size() > 0) System.out.println("hasSourceNames --- " + dlQueue[i].toString());
            }
            
            
        }
        
        assertTrue(dlQueue != null);
        
        
        

    }
    
    @Test
    public void getStats() throws Exception {
        System.out.println("Running get stats...");
        ECStats stats = cl.getStats(ECCodes.EC_DETAIL_FULL);
        if (stats != null) {
            System.out.println(stats.toString());
            System.out.format("isConnectedEd2k: %s, isConnectedKad: %s, isKadRunning: %s, isKadFirewalled: %s\n", 
                            stats.getConnState().isConnectedEd2k(),
                            stats.getConnState().isConnectedKad(),
                            stats.getConnState().isKadRunning(),
                            stats.getConnState().isKadFirewalled());
        }
        assertTrue(stats != null);
    }
    
    @Test
    public void testCategories() throws Exception {
        String comment = "TEST CATEGORY FOR ECClientTest";
        System.out.println("Adding test category...");
        
        cl.createCategory(new ECCategory("ECClientTest", "/share/Download/", comment, ECPartFile.PR_HIGH, (byte)0xff0000));
        
        System.out.println("Running get categories...");
        ECCategory[] catList = cl.getCategories(ECCodes.EC_DETAIL_FULL);
        ECCategory newCat = null;
        
        if (catList != null) {
            for (int i = 0; i < catList.length; i++) {
                System.out.println(catList[i].toString());
                if (catList[i].getComment().equals(comment)) newCat = catList[i];
            }
        }
        
        if (newCat != null) {
            newCat.setComment("UDPATED!");
            System.out.println("Updating test category...");
            cl.updateCategory(newCat);
            
            System.out.println("Running get categories...");
            catList = cl.getCategories(ECCodes.EC_DETAIL_FULL);
            
            for (int i = 0; i < catList.length; i++) {
                System.out.println(catList[i].toString());
            }
            
            System.out.println("Deleting test category...");
            cl.deleteCategory(newCat.getId());

            System.out.println("Running get categories...");
            catList = cl.getCategories(ECCodes.EC_DETAIL_FULL);
            
            for (int i = 0; i < catList.length; i++) {
                System.out.println(catList[i].toString());
            }
        }
        
        assertTrue(newCat != null);
        

        
    }
    
    

}
