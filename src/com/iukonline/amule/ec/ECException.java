package com.iukonline.amule.ec;



public class ECException extends Exception {
    
    static final long serialVersionUID = 5741767616156419860L;

    /**
     * 
     */
    
    private ECPacket causePacket;

    public ECException(String detailMessage, ECPacket causePacket) {
        super(detailMessage + "\n");
        this.setCausePacket(causePacket);
    }

    
    public ECException(String detailMessage, ECPacket causePacket, Throwable throwable) {
        super(detailMessage, throwable);
        this.setCausePacket(causePacket);
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
    
}
