package com.iukonline.amule.ec.exceptions;


public class ECTagParsingException extends Exception {

    private static final long serialVersionUID = -1557992414575949145L;
    
    public ECTagParsingException(String detailMessage) {
        super(detailMessage);
    }
     
    public ECTagParsingException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

}
