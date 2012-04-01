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

import com.iukonline.amule.ec.ECClient;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECStats;
import com.iukonline.amule.ec.ECUtils;

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
        
        socket = new Socket("***REMOVED***", 4712);
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
        ECPartFile[] dlQueue = cl.getDownloadQueue();
        
        if (dlQueue != null) {
            for (int i = 0; i < dlQueue.length; i++) {
                
                ECPartFile d = cl.getDownloadDetails(dlQueue[i]);
                
                System.out.format("Filename: %s\nHash: %s\nStatus: %d - Done: %d/%d - Speed: %d - Prio: %d\n\n", 
                                d.getFileName(), 
                                ECUtils.byteArrayToHexString(d.getHash(), 16),
                                d.getStatus(),
                                d.getSizeDone(), 
                                d.getSizeFull(),
                                d.getSpeed(),
                                d.getPrio()
                                );
            }
        }
        
        assertTrue(dlQueue != null);
        

    }
    
    @Test
    public void getStats() throws Exception {
        System.out.print("Running get stats...\n");
        ECStats stats = cl.getStats();
        if (stats != null) System.out.format("Download rate %d bytes/sec\nUpload rate %d bytes/sec\n", stats.getSpeedDl(), stats.getSpeedUl());
        assertTrue(stats != null);
    }

}
