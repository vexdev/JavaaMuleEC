package com.iukonline.amule.ec;



public class ECException extends Exception {
    
    static final long serialVersionUID = 5741767616156419860L;

    private ECPacket causePacket;
    private ECRawPacket rawCausePacket;

    public ECException(String detailMessage, ECPacket causePacket) {
        super(detailMessage);
        this.setCausePacket(causePacket);
    }
    
    public ECException(String detailMessage, ECRawPacket causePacket) {
        super(detailMessage);
        this.setRawCausePacket(rawCausePacket);
    }

    public ECException(String detailMessage, ECPacket causePacket, Throwable throwable) {
        super(detailMessage, throwable);
        this.setCausePacket(causePacket);
    }
    
    public ECException(String detailMessage, ECRawPacket rawCausePacket, Throwable throwable) {
        super(detailMessage, throwable);
        this.setRawCausePacket(rawCausePacket);
    }

    public ECException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }


    public ECException(String detailMessage) {
        super(detailMessage);
    }

    public ECPacket getCausePacket() {
        return causePacket;
    }

    public void setCausePacket(ECPacket causePacket) {
        this.causePacket = causePacket;
    }

    public ECRawPacket getRawCausePacket() {
        return rawCausePacket;
    }

    public void setRawCausePacket(ECRawPacket rawCausePacket) {
        this.rawCausePacket = rawCausePacket;
    }

    
}
