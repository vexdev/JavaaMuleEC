package com.iukonline.amule.ec.exceptions;


public class ECDebugException extends Exception {

    private static final long serialVersionUID = -1557992414575949145L;
    
    public ECDebugException(String detailMessage) {
        super(detailMessage);
    }
     
    public ECDebugException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

}
