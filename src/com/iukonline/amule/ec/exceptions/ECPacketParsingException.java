package com.iukonline.amule.ec.exceptions;

import com.iukonline.amule.ec.ECRawPacket;

public class ECPacketParsingException extends Exception {

    ECRawPacket causePacket;
    


    private static final long serialVersionUID = -1557992414575949145L;
    
    public ECPacketParsingException(String detailMessage, ECRawPacket causePacket, Throwable throwable) {
        super(detailMessage, throwable);
        this.causePacket = causePacket;
    }
    
    public ECPacketParsingException(String detailMessage, ECRawPacket causePacket) {
        super(detailMessage);
        this.causePacket = causePacket;
    }

    public ECPacketParsingException(String detailMessage) {
        super(detailMessage);
    }
     
    
    public ECPacketParsingException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
    
    public ECRawPacket getCausePacket() {
        return causePacket;
    }

}
