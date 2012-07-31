package com.iukonline.amule.ec.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DataFormatException;

import org.junit.Test;

import com.iukonline.amule.ec.ECCodes;
import com.iukonline.amule.ec.ECPacket;
import com.iukonline.amule.ec.ECRawPacket;
import com.iukonline.amule.ec.ECTag;
import com.iukonline.amule.ec.ECTagTypes;
import com.iukonline.amule.ec.ECUtils;
import com.iukonline.amule.ec.exceptions.ECPacketParsingException;

public class ECRawTest {

    
    @Test public void testSalt() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        
        /*
        DEBUG: saltString = F8A6A7612E8E090A
        DEBUG: saltHash = 1f00022ab969d5de848c62c47a2c53fb
        DEBUG: m_connectionPassword before = 098F6BCD4621D373CADE4E832627B4F6
        DEBUG: m_connectionPassword after = f0da283ec2405883101ec4b6dc2d3b43
        */
        
        long salt = -529551870334727926L;
        
        
        byte[] saltHexBytes = ECUtils.uintToBytes(salt, 8, true);
        System.out.printf("SALT PRINTF: %X\n",  salt);
        System.out.println("SALT BYTES: " + ECUtils.byteArrayToHexString(saltHexBytes));
        
        //byte[] saltHash = MessageDigest.getInstance("MD5").digest(ECUtils.byteArrayToHexString(saltHexBytes, 8, 0, null).getBytes());
        byte[] saltHash = MessageDigest.getInstance("MD5").digest(String.format("%X", salt).getBytes());
        System.out.println("SALT HASH: " + ECUtils.byteArrayToHexString(saltHash));
        
        byte[] passwd = MessageDigest.getInstance("MD5").digest(new String("test").getBytes("UTF-8"));
        System.out.println("HASH PASSWD: " + ECUtils.byteArrayToHexString(passwd));
        
        
        MessageDigest digest = MessageDigest.getInstance("MD5");
        
        digest.update(ECUtils.byteArrayToHexString(passwd, 16, 0, null).toLowerCase().getBytes());
        digest.update(ECUtils.byteArrayToHexString(saltHash, 16, 0, null).toLowerCase().getBytes());
        System.out.println("RESPONSE: " + ECUtils.byteArrayToHexString(digest.digest()));
        
    }

    
    @Test
    public void testTrace() throws IOException, DataFormatException, ECPacketParsingException {
        File clientFile = new File("test/com/iukonline/amule/ec/test/LongClientRun.bin");
        File serverFile = new File("test/com/iukonline/amule/ec/test/LongServerRun.bin");
        
        FileInputStream clientStream = new FileInputStream(clientFile);
        FileInputStream serverStream = new FileInputStream(serverFile);
        
        while (clientStream.available() > 0) {
        //for (int i = 0; i < 5; i++) {

            System.out.println("------------- REQUEST ---------------------");
            /*
              ECRawPacket req = new ECRawPacket(clientStream);

            req.parse();
            
            System.out.println(req.dump());
            
            */
            
            ECPacket p = ECPacket.readFromStream(clientStream, ECRawPacket.class);
            System.out.println(p.getEncodedPacket().dump());
            

            
            if (serverStream.available() > 0) {
                System.out.println("------------- RESPONSE --------------");
                /*ECRawPacket resp = new ECRawPacket(serverStream);
                resp.parse();*/
                
                ECPacket p2 = ECPacket.readFromStream(serverStream, ECRawPacket.class);
                System.out.println(p2.getEncodedPacket().dump());
                
                // System.out.println(resp.dump());
                
            }
        }
    }
    
    @Test
    public void testTags() throws DataFormatException, ECPacketParsingException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_PARTFILE_SWAP_A4AF_THIS);
        byte[] hash = { 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf , 0x10};
        epReq.addTag(new ECTag(ECCodes.EC_TAG_PARTFILE, ECTagTypes.EC_TAGTYPE_HASH16, hash));
        @SuppressWarnings("unused")
        ECRawPacket raw = new ECRawPacket(epReq);
    }

}
