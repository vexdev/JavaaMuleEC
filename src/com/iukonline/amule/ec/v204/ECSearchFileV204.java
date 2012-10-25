package com.iukonline.amule.ec.v204;

import java.util.zip.DataFormatException;

import com.iukonline.amule.ec.ECTag;
import com.iukonline.amule.ec.exceptions.ECTagParsingException;


public class ECSearchFileV204 {
    
    protected byte detailLevel;

    protected long id;
    protected long parent;

    
    protected byte[] hash;           
    protected String fileName;        
    protected byte status;      
    protected int sourceCount;  
    protected int sourceXfer;   

    protected long sizeFull;       

    public ECSearchFileV204(ECTag st, byte d) throws ECTagParsingException {
        detailLevel = d;
        ECTag t;
        
        try {
            id = st.getTagValueUInt();
            
            switch (detailLevel) {
            case ECCodesV204.EC_DETAIL_FULL:
            case ECCodesV204.EC_DETAIL_WEB:
            case ECCodesV204.EC_DETAIL_CMD:

                t = st.getSubTagByName(ECCodesV204.EC_TAG_PARTFILE_SOURCE_COUNT);
                if (t != null) sourceCount = (int) t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_SOURCE_COUNT in server response");
    
                t = st.getSubTagByName(ECCodesV204.EC_TAG_PARTFILE_SOURCE_COUNT_XFER);
                if (t != null) sourceXfer = (int) t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_SOURCE_COUNT_XFER in server response");
    
                t = st.getSubTagByName(ECCodesV204.EC_TAG_PARTFILE_STATUS);
                if (t != null) status = (byte) t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_STATUS in server response");

            case ECCodesV204.EC_DETAIL_UPDATE:
            
                t = st.getSubTagByName(ECCodesV204.EC_TAG_PARTFILE_NAME);
                if (t != null) fileName = t.getTagValueString();
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_NAME in server response");           
                
                t = st.getSubTagByName(ECCodesV204.EC_TAG_PARTFILE_SIZE_FULL);
                if (t != null) sizeFull = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_SIZE_FULL in server response");
                
                t = st.getSubTagByName(ECCodesV204.EC_TAG_PARTFILE_HASH);
                if (t != null) sizeFull = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_SIZE_HASH in server response");
                
                t = st.getSubTagByName(ECCodesV204.EC_TAG_SEARCH_PARENT);
                if (t != null) parent = t.getTagValueUInt();
                else parent = -1;
                
                break;
            
            default:
                throw new ECTagParsingException("Detail level " + d + " not supported");
        }

            
        } catch (DataFormatException e) {
            throw new ECTagParsingException("One or more unexpected type in EC_TAG_SEARCHFILE tag", e);        
        }
    }

    public long getId() {
        return id;
    }

    public long getParent() {
        return parent;
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
