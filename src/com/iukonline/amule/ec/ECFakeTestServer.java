package com.iukonline.amule.ec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.DataFormatException;

public class ECFakeTestServer {
    
    
    static HashMap <String, ECTag> partFileList = new HashMap<String, ECTag> ();
    
    static {
        try {
            byte[] hash1 = { 
                            0x10, 0x11, 0x12, 0x13, 0x14,
                            0x15, 0x16, 0x17, 0x18, 0x19,
                            0x1a, 0x1b, 0x1c, 0x1d, 0x1e,
                            0x1f, 0x20, 0x21, 0x22, 0x23
            };
            
            ECTag pf1;
            
            pf1 = new ECTag(ECTag.EC_TAG_PARTFILE, ECTag.EC_TAGTYPE_HASH16, hash1);
            pf1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_NAME, ECTag.EC_TAGTYPE_STRING, "FILE number 1.xxx"));
            
            pf1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_NAME, ECTag.EC_TAGTYPE_STRING, "FILE number 1.xxx"));
            pf1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_ED2K_LINK, ECTag.EC_TAGTYPE_STRING, "ed2k://sssss/"));
            pf1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_STATUS, ECTag.EC_TAGTYPE_UINT8, ECPartFile.PS_READY));
            pf1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_PRIO, ECTag.EC_TAGTYPE_UINT8, ECPartFile.PR_AUTO_HIGH));
            pf1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SPEED, ECTag.EC_TAGTYPE_UINT8, 100));
            pf1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SIZE_FULL, ECTag.EC_TAGTYPE_UINT16, 30000));
            pf1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SIZE_DONE, ECTag.EC_TAGTYPE_UINT16, 10000));
            pf1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_COUNT, ECTag.EC_TAGTYPE_UINT8, 100));
            pf1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_COUNT_A4AF, ECTag.EC_TAGTYPE_UINT8, 5));
            pf1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_COUNT_XFER, ECTag.EC_TAGTYPE_UINT8, 1));
            pf1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_COUNT_NOT_CURRENT, ECTag.EC_TAGTYPE_UINT8, 2));
            pf1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_LAST_SEEN_COMP, ECTag.EC_TAGTYPE_UINT8, 1));
            
            ECTag com1 = new ECTag(ECTag.EC_TAG_PARTFILE_COMMENTS, ECTag.EC_TAGTYPE_UINT8, 0);
            com1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_COMMENTS, ECTag.EC_TAGTYPE_STRING, "AUTHOR 1"));
            com1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_COMMENTS, ECTag.EC_TAGTYPE_STRING, "PART NAME 1"));
            com1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_NAMES, ECTag.EC_TAGTYPE_UINT8, 1));
            com1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_COMMENTS, ECTag.EC_TAGTYPE_STRING, "COMMENT 1"));
            com1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_COMMENTS, ECTag.EC_TAGTYPE_STRING, "AUTHOR 2"));
            com1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_COMMENTS, ECTag.EC_TAGTYPE_STRING, "PART NAME 2"));
            com1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_NAMES, ECTag.EC_TAGTYPE_UINT8, 2));
            com1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_COMMENTS, ECTag.EC_TAGTYPE_STRING, "COMMENT 2"));
            com1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_COMMENTS, ECTag.EC_TAGTYPE_STRING, "AUTHOR 3"));
            com1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_COMMENTS, ECTag.EC_TAGTYPE_STRING, "PART NAME 3"));
            com1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_NAMES, ECTag.EC_TAGTYPE_UINT8, 3));
            com1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_COMMENTS, ECTag.EC_TAGTYPE_STRING, "COMMENT 3"));
            pf1.addSubTag(com1);
            
            ECTag sn1 = new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_NAMES, ECTag.EC_TAGTYPE_UINT8, 0);
            
            sn1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_NAMES, ECTag.EC_TAGTYPE_STRING, "PART NAME 1"));
            sn1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_NAMES, ECTag.EC_TAGTYPE_UINT8, 1));
            sn1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_NAMES, ECTag.EC_TAGTYPE_STRING, "PART NAME 2"));
            sn1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_NAMES, ECTag.EC_TAGTYPE_UINT8, 2));
            sn1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_NAMES, ECTag.EC_TAGTYPE_STRING, "PART NAME 3"));
            sn1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_NAMES, ECTag.EC_TAGTYPE_UINT8, 3));
            sn1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_NAMES, ECTag.EC_TAGTYPE_STRING, "PART NAME 4"));
            sn1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_NAMES, ECTag.EC_TAGTYPE_UINT8, 4));
            sn1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_NAMES, ECTag.EC_TAGTYPE_STRING, "PART NAME 5"));
            sn1.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_NAMES, ECTag.EC_TAGTYPE_UINT8, 5));
            
            pf1.addSubTag(sn1);
            
            byte[] hash2 = { 
                            0x10, 0x11, 0x12, 0x13, 0x14,
                            0x15, 0x16, 0x17, 0x18, 0x19,
                            0x1a, 0x1b, 0x1c, 0x1d, 0x1e,
                            0x1f, 0x20, 0x21, 0x22, 0x24
            };
            
            ECTag pf2;
            
            pf2 = new ECTag(ECTag.EC_TAG_PARTFILE, ECTag.EC_TAGTYPE_HASH16, hash2);
            pf2.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_NAME, ECTag.EC_TAGTYPE_STRING, "FILE number 2.xxx"));
            
            pf2.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_NAME, ECTag.EC_TAGTYPE_STRING, "FILE number 2.xxx"));
            pf2.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_ED2K_LINK, ECTag.EC_TAGTYPE_STRING, "ed2k://ttttt/"));
            pf2.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_STATUS, ECTag.EC_TAGTYPE_UINT8, ECPartFile.PS_READY));
            pf2.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_PRIO, ECTag.EC_TAGTYPE_UINT8, ECPartFile.PR_AUTO_HIGH));
            pf2.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SPEED, ECTag.EC_TAGTYPE_UINT8, 100));
            pf2.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SIZE_FULL, ECTag.EC_TAGTYPE_UINT16, 30000));
            pf2.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SIZE_DONE, ECTag.EC_TAGTYPE_UINT16, 10000));
            pf2.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_COUNT, ECTag.EC_TAGTYPE_UINT8, 100));
            pf2.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_COUNT_A4AF, ECTag.EC_TAGTYPE_UINT8, 5));
            pf2.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_COUNT_XFER, ECTag.EC_TAGTYPE_UINT8, 1));
            pf2.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_SOURCE_COUNT_NOT_CURRENT, ECTag.EC_TAGTYPE_UINT8, 2));
            pf2.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_LAST_SEEN_COMP, ECTag.EC_TAGTYPE_UINT8, 1));
            
            partFileList.put(ECUtils.byteArrayToHexString(hash1), pf1);
            partFileList.put(ECUtils.byteArrayToHexString(hash2), pf2);
            
        
        } catch (DataFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    
    
    
    public ECPacket parseRequestAndGenerateResponde(ECPacket epReq) throws IOException {
        ECPacket epResp;
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        switch (epReq.getOpCode()) {
        case ECPacket.EC_OP_GET_DLOAD_QUEUE:
            epResp = generateDlQueuePacket();
            break;
            
        case ECPacket.EC_OP_GET_DLOAD_QUEUE_DETAIL:
            epResp = generateDlQueuePacketDetail(epReq);
            break;
            
        case ECPacket.EC_OP_STAT_REQ:
            epResp = generateStatsPacket();
            break;
            
        case ECPacket.EC_OP_PARTFILE_PAUSE:
        case ECPacket.EC_OP_PARTFILE_RESUME:
        case ECPacket.EC_OP_PARTFILE_DELETE:
        case ECPacket.EC_OP_PARTFILE_SWAP_A4AF_THIS:
        case ECPacket.EC_OP_PARTFILE_SWAP_A4AF_THIS_AUTO:
        case ECPacket.EC_OP_PARTFILE_SWAP_A4AF_OTHERS:
            epResp = doActionOnPartFile(epReq);
            break;
        case ECPacket.EC_OP_PARTFILE_PRIO_SET:
            epResp = changePartFilePrio(epReq);
            break;
        case ECPacket.EC_OP_RENAME_FILE:
            epResp = renamePartFile(epReq);
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
    
    private ECPacket generateStatsPacket() {
        ECPacket stats = new ECPacket();
        stats.setOpCode(ECPacket.EC_OP_STATS);
        try {
            stats.addTag(new ECTag(ECTag.EC_TAG_STATS_DL_SPEED, ECTag.EC_TAGTYPE_UINT16, 100));
            stats.addTag(new ECTag(ECTag.EC_TAG_STATS_UL_SPEED, ECTag.EC_TAGTYPE_UINT16, 200));
        } catch (DataFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        
        return stats;
        
    }
    
    private ECPacket generateDlQueuePacket() {
        ECPacket dlQueue = new ECPacket();
        dlQueue.setOpCode(ECPacket.EC_OP_DLOAD_QUEUE);
        
        Iterator <ECTag> i = partFileList.values().iterator();
        while (i.hasNext()) {
            dlQueue.addTag(i.next());
        }
        return dlQueue;
    }
    
    private ECPacket generateDlQueuePacketDetail(ECPacket epReq) {
        ECPacket detail = new ECPacket();
        
        
        try {
            byte[] hash = epReq.getTagByName(ECTag.EC_TAG_PARTFILE).getTagValueHash();
            ECTag t = partFileList.get(ECUtils.byteArrayToHexString(hash));
            
            if (t == null) {
                detail.setOpCode(ECTag.EC_OP_FAILED);
                detail.addTag(new ECTag(ECTag.EC_TAG_STRING, ECTag.EC_TAGTYPE_STRING, "Invalid partfile requested"));
            } else {
                detail.setOpCode(ECPacket.EC_OP_DLOAD_QUEUE);
                detail.addTag(partFileList.get(ECUtils.byteArrayToHexString(hash)));
            }
            
        } catch (DataFormatException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return detail;
    }
    
    private ECPacket doActionOnPartFile(ECPacket epReq) {
        ECPacket result = new ECPacket();
        
        try {
            byte[] hash = epReq.getTagByName(ECTag.EC_TAG_PARTFILE).getTagValueHash();
            ECTag t = partFileList.get(ECUtils.byteArrayToHexString(hash));
            
            if (t == null) {
                result.setOpCode(ECTag.EC_OP_FAILED);
                result.addTag(new ECTag(ECTag.EC_TAG_STRING, ECTag.EC_TAGTYPE_STRING, "Invalid partfile requested"));
            } else {
                
                switch (epReq.getOpCode()) {
                case ECPacket.EC_OP_PARTFILE_PAUSE:
                    t.getSubTagByName(ECTag.EC_TAG_PARTFILE_STATUS).setTagValueUInt(ECPartFile.PS_PAUSED);
                    t.getSubTagByName(ECTag.EC_TAG_PARTFILE_SPEED).setTagValueUInt(0);
                    t.getSubTagByName(ECTag.EC_TAG_PARTFILE_SOURCE_COUNT_XFER).setTagValueUInt(0);
                    break;
                case ECPacket.EC_OP_PARTFILE_RESUME:
                    t.getSubTagByName(ECTag.EC_TAG_PARTFILE_STATUS).setTagValueUInt(ECPartFile.PS_READY);
                    t.getSubTagByName(ECTag.EC_TAG_PARTFILE_SPEED).setTagValueUInt(100);
                    t.getSubTagByName(ECTag.EC_TAG_PARTFILE_SOURCE_COUNT_XFER).setTagValueUInt(1);
                    break;

                case ECPacket.EC_OP_PARTFILE_DELETE:
                    partFileList.remove(ECUtils.byteArrayToHexString(hash));
                    
                case ECPacket.EC_OP_PARTFILE_SWAP_A4AF_THIS:
                case ECPacket.EC_OP_PARTFILE_SWAP_A4AF_THIS_AUTO:
                    t.getSubTagByName(ECTag.EC_TAG_PARTFILE_SOURCE_COUNT_A4AF).setTagValueUInt(0);
                    break;
                case ECPacket.EC_OP_PARTFILE_SWAP_A4AF_OTHERS:
                    t.getSubTagByName(ECTag.EC_TAG_PARTFILE_SOURCE_COUNT_A4AF).setTagValueUInt(1);
                    break;
                }
                
                result.setOpCode(ECTag.EC_OP_NOOP);
                
            }
            
        } catch (DataFormatException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        return result;
        
    }
    
    private ECPacket changePartFilePrio(ECPacket epReq) {
        ECPacket result = new ECPacket();
        
        try {
            byte[] hash = epReq.getTagByName(ECTag.EC_TAG_PARTFILE).getTagValueHash();
            ECTag t = partFileList.get(ECUtils.byteArrayToHexString(hash));
            
            if (t == null) {
                result.setOpCode(ECTag.EC_OP_FAILED);
                result.addTag(new ECTag(ECTag.EC_TAG_STRING, ECTag.EC_TAGTYPE_STRING, "Invalid partfile requested"));
            } else {
                result.setOpCode(ECTag.EC_OP_NOOP);
                t.getSubTagByName(ECTag.EC_TAG_PARTFILE_PRIO).setTagValueUInt(epReq.getTagByName(ECTag.EC_TAG_PARTFILE).getSubTagByName(ECTag.EC_TAG_PARTFILE_PRIO).getTagValueUInt());
            }
        } catch (DataFormatException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        return result;
        
    }

    private ECPacket renamePartFile(ECPacket epReq) {
        ECPacket result = new ECPacket();
        
        try {
            byte[] hash = epReq.getTagByName(ECTag.EC_TAG_PARTFILE).getTagValueHash();
            ECTag t = partFileList.get(ECUtils.byteArrayToHexString(hash));
            
            if (t == null) {
                result.setOpCode(ECTag.EC_OP_FAILED);
                result.addTag(new ECTag(ECTag.EC_TAG_STRING, ECTag.EC_TAGTYPE_STRING, "Invalid partfile requested"));
            } else {
                result.setOpCode(ECTag.EC_OP_NOOP);
                t.getSubTagByName(ECTag.EC_TAG_PARTFILE_NAME).setTagValueString(epReq.getTagByName(ECTag.EC_TAG_PARTFILE).getSubTagByName(ECTag.EC_TAG_PARTFILE_NAME).getTagValueString());
            }
        } catch (DataFormatException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        return result;
        
    }
    
}
