package com.iukonline.amule.ec;

import java.io.IOException;
import java.util.zip.DataFormatException;

public class ECFakeTestServer {

    public ECPacket parseRequestAndGenerateResponde(ECPacket epReq) throws IOException {
        ECPacket epResp;
        
        switch (epReq.getOpCode()) {
        case ECPacket.EC_OP_GET_DLOAD_QUEUE:
            epResp = generateDlQueuePacket();
            break;
            
        default:
            epResp = new ECPacket();
            epResp.setOpCode(ECPacket.EC_OP_FAILED);
            try {
                epResp.addTag(new ECTag(ECTag.EC_TAG_STRING, ECTag.EC_TAGTYPE_STRING, "FAKE SERVER - Unknown op code received"));
            } catch (DataFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            break;
        }
        return epResp;
    }
    
    private ECPacket generateDlQueuePacket() {
        ECPacket dlQueue = new ECPacket();
        dlQueue.setOpCode(ECPacket.EC_OP_DLOAD_QUEUE);
        
        
        byte[] hash = { 
                        0x10, 0x11, 0x12, 0x13, 0x14,
                        0x15, 0x16, 0x17, 0x18, 0x19,
                        0x1a, 0x1b, 0x1c, 0x1d, 0x1e,
                        0x1f, 0x20, 0x21, 0x22, 0x23
        };
        
        ECTag pf;
        try {
            pf = new ECTag(ECTag.EC_TAG_PARTFILE, ECTag.EC_TAGTYPE_HASH16, hash);
            pf.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_NAME, ECTag.EC_TAGTYPE_STRING, "FILE numer 1.xxx"));
            
            pf.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_NAME, ECTag.EC_TAGTYPE_STRING, "FILE numer 1.xxx"));
            pf.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_ED2K_LINK, ECTag.EC_TAGTYPE_STRING, "ed2k://sssss/"));
            pf.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_STATUS, ECTag.EC_TAGTYPE_UINT8, ECPartFile.PS_READY));
            pf.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_PRIO, ECTag.EC_TAGTYPE_UINT8, ECPartFile.PR_AUTO_HIGH));
            pf.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SPEED, ECTag.EC_TAGTYPE_UINT8, 100));
            pf.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SIZE_FULL, ECTag.EC_TAGTYPE_UINT16, 30000));
            pf.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SIZE_DONE, ECTag.EC_TAGTYPE_UINT16, 10000));
            pf.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_COUNT, ECTag.EC_TAGTYPE_UINT8, 100));
            pf.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_COUNT_A4AF, ECTag.EC_TAGTYPE_UINT8, 5));
            pf.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_COUNT_XFER, ECTag.EC_TAGTYPE_UINT8, 1));
            pf.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_COUNT_NOT_CURRENT, ECTag.EC_TAGTYPE_UINT8, 2));
            pf.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_LAST_SEEN_COMP, ECTag.EC_TAGTYPE_UINT8, 1));
        
            dlQueue.addTag(pf);
            
        } catch (DataFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        

        
        
        
        
        


        return dlQueue;
    }

}
