package com.iukonline.amule.ec.exceptions;

import com.iukonline.amule.ec.ECPacket;


public class ECClientException extends Exception {

    

    /**
     * 
     */
    private static final long serialVersionUID = 1325784420371315051L;
    ECPacket request;
    ECPacket response;

    public ECClientException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
    
    public ECClientException(String detailMessage) {
        super(detailMessage);
    }
    
    public ECClientException(String detailMessage, ECPacket request, ECPacket response, Throwable throwable) {
        super(detailMessage, throwable);
        this.request = request;
        this.response = response;
    }
    
    public ECClientException(String detailMessage, ECPacket request, ECPacket response) {
        super(detailMessage);
        this.request = request;
        this.response = response;
    }
    
    public ECPacket getRequestPacket() { return request; }
    public ECPacket getResponsePacket() { return response; }

}
