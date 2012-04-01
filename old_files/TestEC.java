package com.iukonline.amule.ec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.zip.DataFormatException;






public class TestEC {
    
    private static ECClient cl;
    private static Socket socket;
    private static ECPartFile[] dlQueue;
    
    private static void runTest3() throws ECException, IOException {
        ECStats stats = cl.getStats();
        System.out.format("Download rate %d bytes/sec\nUpload rate %d bytes/sec\n", stats.getSpeedDl(), stats.getSpeedUl());
    }
    
    private static void runTest4() throws ECException, IOException {
        runTest3();
        //runTest1();
        
        ECPartFile resumed = null;
                
        for (int i = 0; i < dlQueue.length; i++) {
            if (dlQueue[i].getStatus() == ECPartFile.PS_PAUSED && resumed == null) {
                System.out.println("Resuming file " + dlQueue[i].getFileName() + "...");
                dlQueue[i].resume();
                resumed = dlQueue[i];
                
            }
        }
        
        if (resumed != null) {
            //runTest1();
            for (int i = 0; i < dlQueue.length; i++) {
                if (resumed.getFileName().equals(dlQueue[i].getFileName())) {
                    System.out.println("New Status " + dlQueue[i].getStatus());
                }
            }
        }
    }
    
    private static void runTest5() throws ECException, IOException, DataFormatException {
        runTest3();
        //runTest1();
        
        if (dlQueue.length > 0) {
            dlQueue[0].refresh();
        }
        
    }
    
    private static void runTest6() throws ECException, IOException {
        cl.addED2KURL("ed2k://|file|Amore%20con%20la%20''S''%20maiuscola%20-%202002%20-%2088%20min%20-%20AC3%20Italian%20-%20DVDRip%20CRUSADERS.avi|940548096|1F0ADCD4BA73F3D92F6C60D00DB368C3|h=PEBIXBW64YJC3WU4I6PTM3ASGQ6GGWGK|/");
        //cl.addED2KURL("pippo");
        System.out.println("DONE");
    }
    
    private static void runTest7() throws ECException, IOException {
        runTest3();
        //runTest1();
        
        for (int i = 0; i < dlQueue.length; i++) {
            if (dlQueue[i].getPrio() >= ECPartFile.PR_AUTO) {
                System.out.println("Changing priority of file " + dlQueue[i].getFileName() + "...");
                dlQueue[i].changePriority(ECPartFile.PR_NORMAL);
            }
        }
        
        //runTest1();

    }
    
    private static void runTest8() throws ECException, IOException {
        runTest3();
        //runTest1();
        
        for (int i = 0; i < dlQueue.length; i++) {
            if (dlQueue[i].getStatus() == ECPartFile.PS_READY) {
                System.out.println("Querying details of " + dlQueue[i].getFileName() + "...");
                
                
                System.out.println("DETAIL-------------");
                System.out.println(cl.getDownloadDetails(dlQueue[i].getHash()));
            }
        }
        
        

    }
    
    private static void runTest9()   {
        
        
        String packetDump = new String();

        packetDump += "00 00 00 22 ";  // Flags
        packetDump += "00 00 00 6c ";  // Len
        packetDump += "25 "; // OP Code
        packetDump += "02 "; // Tag count
        packetDump += "e0 ";  
        packetDump += "a0 80 "; 
        packetDump += "09 10 b9 19 3b 2e 2e 5d 02 84 0d f5 2c ";
        packetDump += "7a c2 7d 2f 86 ";
        packetDump += "d8 82 "; // TAG -> 602 -> 301 -> EC_TAG_PARTFILE_NAME
        packetDump += "06 "; // TAG TYPE STRING
        packetDump += "51 "; // LENTGTH
        packetDump += "78 32 36 34 2e 49 6c ";
        packetDump += "2e 4d 69 73 74 65 72 6f 2e 64 65 6c 6c 65 2e 44 ";
        packetDump += "6f 64 69 63 69 2e 53 65 64 69 65 2e 2d 2e 54 68 ";
        packetDump += "65 2e 54 77 65 6c 76 65 2e 43 68 61 69 72 73 2e ";
        packetDump += "2d 2e 4d 2e 42 72 6f 6f 6b 73 2e 31 39 37 30 2e ";
        packetDump += "50 4c 69 53 53 2e 6d 6b 76 00 ";
        
        
        
        
        int len = packetDump.length();
        byte[] buf = new byte[len / 3];
        for (int i = 0; i < len; i += 3) {
            buf[i / 3] = (byte) ((Character.digit(packetDump.charAt(i), 16) << 4)
                                 + Character.digit(packetDump.charAt(i+1), 16));
        }
        
 
        ByteArrayInputStream packetStream = new ByteArrayInputStream(buf);
        ECPacket p = new ECPacket();
        try {
            p.readFromStream(packetStream);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        System.out.println(p.toString());
        
    }
    

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        
        cl = new ECClient();
        try {
            cl.setPassword("***REMOVED***");
            //cl.setPassword("armageddon");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.err.print("Error while generating password hash: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        cl.setClientName("EC Testing");
        cl.setClientVersion("pre-aplha");
        cl.setTracer(System.out);
        
        try {
            //socket = new Socket("192.168.56.101", 4712);
            //socket = new Socket("***REMOVED***", 4712);
            //socket = new Socket("10.51.18.51", 4712);
            
            
            cl.setSocket(socket);
            
            //System.out.print("Logging in...\n");
            //cl.login();
            
            runTest9();
            
            System.out.print("All done!\n");
            socket.close();
        } catch (UnknownHostException e) {
            System.err.print("Unknown host error:\n" + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.err.print("I/O error:\n" + e.getMessage());
            e.printStackTrace();
            System.exit(1);
       /* } catch (ECException e) {
            System.err.print("Client/Server communication error:\n" + e.getMessage());
            if (e.getCausePacket() != null) {
                System.err.print("Packet causing error:\n");
                System.err.print("---\n");
                System.err.print(e.getCausePacket().toString());
                System.err.print("---\n");
            }
            e.printStackTrace();
            System.exit(1);
       /* } catch (InterruptedException e) {
            System.err.println("Interrupted!");
            e.printStackTrace();*/
       /* } catch (DataFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();*/
        }

                      
        System.exit(0);

    }

}
