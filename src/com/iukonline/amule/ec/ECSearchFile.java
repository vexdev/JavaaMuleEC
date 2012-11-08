package com.iukonline.amule.ec;

import com.iukonline.amule.ec.exceptions.ECTagParsingException;


public class ECSearchFile {
    
    protected byte[] hash;           
    protected String fileName;        
    protected byte status;      
    protected int sourceCount;  
    protected int sourceXfer;   

    protected long sizeFull;       

    public ECSearchFile() {
        
    }
    
    public ECSearchFile(ECTag st) throws ECTagParsingException {
        this.updateFromECTag(st);
    }
    
    public void updateFromECTag(ECTag st) throws ECTagParsingException {
    }

    public byte[] getHash() {
        return hash;
    }

    public String getFileName() {
        return fileName;
    }

    public byte getStatus() {
        return status;
    }

    public int getSourceCount() {
        return sourceCount;
    }

    public int getSourceXfer() {
        return sourceXfer;
    }

    public long getSizeFull() {
        return sizeFull;
    }
    
}
