package com.iukonline.amule.ec.v204;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DataFormatException;

import com.iukonline.amule.ec.ECClient;
import com.iukonline.amule.ec.ECCodes;
import com.iukonline.amule.ec.ECPacket;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECTag;
import com.iukonline.amule.ec.ECTagTypes;
import com.iukonline.amule.ec.ECUtils;
import com.iukonline.amule.ec.exceptions.ECClientException;
import com.iukonline.amule.ec.exceptions.ECPacketParsingException;
import com.iukonline.amule.ec.exceptions.ECServerException;

public class ECClientV204 extends ECClient {
    
    public ECClientV204() {
        packetParser = ECRawPacketV204.class;
        partFileBuilder = ECPartFileV204.class; 
    }

    @Override
    protected ECPacket buildLoginRequest() throws ECClientException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodesV204.EC_OP_AUTH_REQ);
        try {
            epReq.addTag(new ECTag(ECCodesV204.EC_TAG_CLIENT_NAME, clientName));
            epReq.addTag(new ECTag(ECCodesV204.EC_TAG_CLIENT_VERSION, clientVersion));
            epReq.addTag(new ECTag(ECCodesV204.EC_TAG_PROTOCOL_VERSION, ECTagTypes.EC_TAGTYPE_UINT16, (long) ECCodesV204.EC_CURRENT_PROTOCOL_VERSION));
            if (acceptUTF8) {
                epReq.addTag(new ECTag(ECCodesV204.EC_TAG_CAN_UTF8_NUMBERS, ECTagTypes.EC_TAGTYPE_CUSTOM, new byte[0]));
            }
            if (acceptZlib) {
                epReq.addTag(new ECTag(ECCodesV204.EC_TAG_CAN_ZLIB, ECTagTypes.EC_TAGTYPE_CUSTOM, new byte[0]));
            }

        } catch (DataFormatException e) {
            throw new ECClientException("Cannot create login request", e);
        }
        return epReq;
    }

    @Override
    protected boolean parseLoginResponse(ECPacket epReq, ECPacket epResp) throws ECPacketParsingException, ECServerException, ECClientException, IOException {
        switch (epResp.getOpCode()) {
        case ECCodesV204.EC_OP_AUTH_SALT:
            
            MessageDigest digest;
            try {
                digest = java.security.MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new ECClientException("Cannot get and MD5 digest", e);
            }
            
            
            // Reverse engineered...
            long passSalt;
            try {
                passSalt = epResp.getTagByName(ECCodesV204.EC_TAG_PASSWD_SALT).getTagValueUInt();
            } catch (DataFormatException e) {
                throw new ECPacketParsingException("Unexpected format for password salt", epResp.getRawPacket(), e);
            }
            if (tracer != null) tracer.println("Got auth salt (long) " + passSalt);
            
            
            // TODO: Check what happens if salt has leading zeros
            
            
            //byte saltHexBytes[] = ECUtils.uintToBytes(passSalt, 8, true);
            //if (tracer != null) tracer.println("Got auth salt " + ECUtils.byteArrayToHexString(saltHexBytes, 8, 0, null));

            String hexBytesString = String.format("%X", passSalt);
            if (tracer != null) tracer.println("Got auth salt " + hexBytesString);
            
            //byte[] saltHash = digest.digest(ECUtils.byteArrayToHexString(saltHexBytes, 8, 0, null).getBytes());
            byte[] saltHash = digest.digest(hexBytesString.getBytes());
            if (tracer != null) tracer.println("Hashed salt " + ECUtils.byteArrayToHexString(saltHash, 16, 0, null));

            digest.reset();
            digest.update(ECUtils.byteArrayToHexString(hashedPassword, 16, 0, null).toLowerCase().getBytes());
            digest.update(ECUtils.byteArrayToHexString(saltHash, 16, 0, null).toLowerCase().getBytes());
            
            byte responseHash[] = digest.digest();
            if (tracer != null) tracer.println("Sending challenge response " + ECUtils.byteArrayToHexString(responseHash, 16, 0, null));

            ECPacket epPassword = new ECPacket();
            epPassword.setOpCode(ECCodesV204.EC_OP_AUTH_PASSWD);
            try {
                epPassword.addTag(new ECTag(ECCodesV204.EC_TAG_PASSWD_HASH, ECTagTypes.EC_TAGTYPE_HASH16, responseHash));
            } catch (DataFormatException e) {
                throw new ECClientException("Error building authentication response", e);
            }            

            ECPacket epFinalResp = sendRequestAndWaitResponse(epPassword, false);
            return super.parseLoginResponse(epPassword, epFinalResp);
            
        default:
            throw new ECPacketParsingException("Unexpected response to login request", epResp.getRawPacket());
        }     
    }
    
    @Override
    protected ECPacket sendGetDloadQueueReq(ECPartFile p, byte detailLevel) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        if (p != null && p instanceof ECPartFileV204) {
            long id = ((ECPartFileV204) p).getId();
            ECPacket epReq = new ECPacket();
            
            try {
                epReq.addTag(new ECTag(ECCodes.EC_TAG_DETAIL_LEVEL, ECTagTypes.EC_TAGTYPE_UINT8, detailLevel));
            } catch (DataFormatException e) {
                // Should never happen
                throw new ECClientException("Cannot greate GetDloadQueue request", e);
            }
            
            epReq.setOpCode(ECCodesV204.EC_OP_GET_DLOAD_QUEUE);
            try {
                epReq.addTag(new ECTag(ECCodesV204.EC_TAG_PARTFILE, ECTagTypes.EC_TAGTYPE_UINT32, id ));
            } catch (DataFormatException e) {
                throw new ECClientException("Invalid id provided", e);
            } 
            
            ECPacket epResp;
            epResp = sendRequestAndWaitResponse(epReq);
            
            switch (epResp.getOpCode()) {
            case ECCodes.EC_OP_DLOAD_QUEUE:
                if (epResp.getTags().size() > 1) throw new ECPacketParsingException("Unexpected response for single part file GET_DLOAD_QUEUE", epResp.getRawPacket());
                return epResp;
            default:
                throw new ECPacketParsingException("Unexpected response for GET_DLOAD_QUEUE", epResp.getRawPacket());
            }
        } else {
            return super.sendGetDloadQueueReq(p, detailLevel);
        }
}
    
    
}
