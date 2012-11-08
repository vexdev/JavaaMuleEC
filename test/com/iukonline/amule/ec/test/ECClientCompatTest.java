/**
 * 
 */
package com.iukonline.amule.ec.test;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.iukonline.amule.ec.ECCategory;
import com.iukonline.amule.ec.ECClient;
import com.iukonline.amule.ec.ECCodes;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.v204.ECClientV204;

/**
 * @author ***REMOVED***
 *
 */
public class ECClientCompatTest {

    
    protected static String SERVER_HOST = "***REMOVED***";
    protected static int SERVER_PORT = 4712;
    protected static String SERVER_PASSWORD = "***REMOVED***";    
    protected static ECClient cl = new ECClientV204();
    
    
    /*
    protected static String SERVER_HOST = "192.168.56.101";
    protected static int SERVER_PORT = 4712;
    protected static String SERVER_PASSWORD = "test";    
    protected static ECClient cl = new ECClientV204();
    */
    //protected static ECClient cl = new ECClientFake();
    
    protected static Socket socket;
    
    /**
     * @throws java.lang.Exception
     */

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        cl.setPassword(SERVER_PASSWORD);
        cl.setClientName("EC Testing");
        cl.setClientVersion("pre-aplha");
        cl.setTracer(System.out);
        
        socket = new Socket(SERVER_HOST, SERVER_PORT);
        cl.setSocket(socket);
        
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
    public void compatTest() throws Exception {
        
        System.out.println("############### LOGIN");
        cl.login();
        System.out.println("############### GET STATS");
        System.out.println(cl.getStats().toString());
        
        System.out.println("############### ADD ED2K LINK");
        cl.addED2KLink("ed2k://|file|ubuntu-5.10-install-i386.iso|647129088|901E6AA2A6ACDC43A83AE3FC211120B0|/");
        
        System.out.println("############### GET DOWNLOAD LIST");
        HashMap<String, ECPartFile> dlQueue = cl.getDownloadQueue();
        if (dlQueue == null) throw new Exception("Expecting at least one file in list");
        
        for (ECPartFile p : dlQueue.values()) {
            
            System.out.println("PartFile Before refresh\n" + p.toString());
            cl.refreshPartFile(p, ECCodes.EC_DETAIL_FULL);
            System.out.println("PartFile After refresh\n" + p.toString());
        }
        
        System.out.println("############### PARTFILE COMPARATOR - SORT BY STATUS");
        ArrayList <ECPartFile> sorted = new ArrayList <ECPartFile>(dlQueue.values());
        Collections.sort(sorted, new ECPartFile.ECPartFileComparator(ECPartFile.ECPartFileComparator.ComparatorType.STATUS));
        for (ECPartFile p : sorted) {
            System.out.printf("Status: %d - Speed: %d - %s\n", p.getStatus(), p.getSpeed(), p.toString());
        }
        
        System.out.println("############### PARTFILE COMPARATOR - SORT BY SPEED");
        sorted = new ArrayList <ECPartFile>(dlQueue.values());
        Collections.sort(sorted, new ECPartFile.ECPartFileComparator(ECPartFile.ECPartFileComparator.ComparatorType.SPEED));
        for (ECPartFile p : sorted) {
            System.out.printf("Speed: %d - %s\n", p.getSpeed(), p.toString());
        }
        
        System.out.println("############### PARTFILE COMPARATOR - SORT BY PRIO");
        sorted = new ArrayList <ECPartFile>(dlQueue.values());
        Collections.sort(sorted, new ECPartFile.ECPartFileComparator(ECPartFile.ECPartFileComparator.ComparatorType.PRIORITY));
        for (ECPartFile p : sorted) {
            System.out.printf("Priority: %d - %s\n", p.getPrio(), p.toString());
        }
        
        System.out.println("############### PARTFILE COMPARATOR - SORT BY REMAINING");
        sorted = new ArrayList <ECPartFile>(dlQueue.values());
        Collections.sort(sorted, new ECPartFile.ECPartFileComparator(ECPartFile.ECPartFileComparator.ComparatorType.REMAINING));
        for (ECPartFile p : sorted) {
            System.out.printf("Remaining: %d - %s\n", p.getSpeed() == 0 ? null : ((p.getSizeFull() - p.getSizeDone())/ p.getSpeed()), p.toString());
        }
        
        
        
        System.out.println("############### GET PARTFILE DETAILS");
        ECPartFile p = dlQueue.get("90 1E 6A A2 A6 AC DC 43 A8 3A E3 FC 21 11 20 B0");
        if (p == null) throw new Exception("Expecting test file in list");
        cl.refreshPartFile(p, ECCodes.EC_DETAIL_FULL);
        System.out.println(p.toString());
        
        
        
        System.out.println("############### SET PRIORITY");
        cl.setPartFilePriority(p.getHash(), ECPartFile.PR_LOW);
        
        System.out.println("############### A4AF THIS");
        cl.changeDownloadStatus(p.getHash(), ECCodes.EC_OP_PARTFILE_SWAP_A4AF_THIS);
        
        System.out.println("############### A4AF AUTO");
        cl.changeDownloadStatus(p.getHash(), ECCodes.EC_OP_PARTFILE_SWAP_A4AF_THIS_AUTO);
        
        System.out.println("############### A4AF AWAY");
        cl.changeDownloadStatus(p.getHash(), ECCodes.EC_OP_PARTFILE_SWAP_A4AF_OTHERS);
        
        System.out.println("############### PAUSE");
        cl.changeDownloadStatus(p.getHash(), ECCodes.EC_OP_PARTFILE_PAUSE);
        
        System.out.println("############### RESUME");
        cl.changeDownloadStatus(p.getHash(), ECCodes.EC_OP_PARTFILE_RESUME);
        
        System.out.println("############### DELETE");
        cl.changeDownloadStatus(p.getHash(), ECCodes.EC_OP_PARTFILE_DELETE);
        
        cl.refreshDlQueue(dlQueue);
        p = dlQueue.get("90 1E 6A A2 A6 AC DC 43 A8 3A E3 FC 21 11 20 B0");
        if (p != null) throw new Exception("test file not deleted");
        
        System.out.println("############### GET CATEGORIES");
        ECCategory cat[] = cl.getCategories(ECCodes.EC_DETAIL_FULL);
        if (cat.length == 0) throw new Exception("Expecting at least one category");
        for (int i = 0; i < cat.length; i++) {
            System.out.println(cat[i].toString());
        }

        System.out.println("############### ADD CATEGORY");
        String catName = "TEST-" + System.currentTimeMillis();
        cl.createCategory(new ECCategory(catName, "/", "COMMENT", 0L, ECPartFile.PR_AUTO));
        cat = cl.getCategories(ECCodes.EC_DETAIL_FULL);
        ECCategory newCat = null;
        for (int i = 0; i < cat.length; i++) {
            if (cat[i].getTitle().equals(catName)) {
                System.out.println(cat[i].toString());
                newCat = cat[i];
                break;
            }
        }
        if (newCat == null) throw new Exception("Added category " + catName + "not found");
        
        System.out.println("############### DELETE CATEGORY");
        cl.deleteCategory(newCat.getId());
        
        cat = cl.getCategories(ECCodes.EC_DETAIL_FULL);
        for (int i = 0; i < cat.length; i++) {
            if (cat[i].getTitle().equals(catName)) {
                throw new Exception("Category " + catName + "not deleted");
            }
        }

        
    }
}
