package com.iukonline.amule.ec;

import java.io.IOException;
import java.util.zip.DataFormatException;

public class ECFakeTestClient extends ECClient {
    ECFakeTestServer server = new ECFakeTestServer();
    
    @Override
    public ECPacket sendRequestAndWaitResponse(ECPacket epReq, boolean tryLogin) throws IOException, ECException {
        ECPacket epResp = server.parseRequestAndGenerateResponde(epReq);
        
        if ((epResp.getOpCode() == ECCodes.EC_OP_FAILED) || (tryLogin && epResp.getOpCode() == ECCodes.EC_OP_AUTH_FAIL)) {
            String errMsg = "No error returned.";
            ECTag tagError = epResp.getTagByName((short) ECTag.EC_TAG_STRING);
            if (tagError != null) {
                try {
                    errMsg = tagError.getTagValueString();
                } catch (DataFormatException e) {
                    throw new ECException("Cannot read returned error message", epResp, e);
                }
            }
            
            if (tryLogin && epResp.getOpCode() == ECCodes.EC_OP_AUTH_FAIL) {
                boolean result = false;

                try {
                    result = this.login();
                } catch (Exception e) {
                    // Catch any exception. If login fails, the original error must be returned, not this new one.
                }
                
                if (result) {
                    return this.sendRequestAndWaitResponse(epReq, false);
                }
            }
            
            throw new ECException("Request failed: " + errMsg);
        }
        return epResp;
    }

}
