package com.iukonline.amule.ec.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;

import org.junit.Test;

import com.iukonline.amule.ec.ECCodes;
import com.iukonline.amule.ec.ECException;
import com.iukonline.amule.ec.ECPacket;
import com.iukonline.amule.ec.ECRawPacket;
import com.iukonline.amule.ec.ECTag;

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
    
    @Test
    public void testTags() throws ECException, DataFormatException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_PARTFILE_SWAP_A4AF_THIS);
        byte[] hash = { 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf , 0x10};
        epReq.addTag(new ECTag(ECTag.EC_TAG_PARTFILE, ECTag.EC_TAGTYPE_HASH16, hash));
        ECRawPacket raw = new ECRawPacket(epReq);
        
    }

}
