package com.iukonline.amule.ec.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;

import org.junit.Test;

import com.iukonline.amule.ec.ECException;
import com.iukonline.amule.ec.ECRawPacket;

public class ECRawTest {

    


    
    @Test
    public void testTrace() throws IOException, ECException, DataFormatException {
        File clientFile = new File("test/com/iukonline/amule/ec/test/LongClientRun.bin");
        File serverFile = new File("test/com/iukonline/amule/ec/test/LongServerRun.bin");
        
        FileInputStream clientStream = new FileInputStream(clientFile);
        FileInputStream serverStream = new FileInputStream(serverFile);
        
        while (clientStream.available() > 0) {

            System.out.println("------------- REQUEST ---------------------");
            ECRawPacket req = new ECRawPacket(clientStream);
            
            System.out.println(req.dump());
            

            
            if (serverStream.available() > 0) {
                System.out.println("------------- RESPONSE --------------");
                ECRawPacket resp = new ECRawPacket(serverStream);
                
                System.out.println(resp.dump());
                
            }

            
        }
        
    }

}
