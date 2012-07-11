package com.iukonline.amule.ec.exceptions;

import com.iukonline.amule.ec.ECPacket;

public class ECServerException extends Exception {

    private static final long serialVersionUID = 6404826402028888302L;
    ECPacket request;
    ECPacket response;
    
    public ECServerException(String detailMessage, ECPacket request, ECPacket response, Throwable throwable) {
        super(detailMessage, throwable);
        this.request = request;
        this.response = response;
    }
    
    public ECServerException(String detailMessage, ECPacket request, ECPacket response) {
        super(detailMessage);
        this.request = request;
        this.response = response;
    }

}
