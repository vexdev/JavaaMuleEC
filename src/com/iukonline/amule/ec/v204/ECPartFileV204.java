package com.iukonline.amule.ec.v204;

import java.util.zip.DataFormatException;

import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECTag;
import com.iukonline.amule.ec.exceptions.ECTagParsingException;

public class ECPartFileV204 extends ECPartFile {
    
    protected long id;

    public ECPartFileV204(ECTag t, byte d) throws ECTagParsingException {
        fillFromTag(t, d);
    }
    
    @Override
    protected byte[] getHashFromTag(ECTag pt) throws ECTagParsingException {
        ECTag t = pt.getSubTagByName(ECCodesV204.EC_TAG_PARTFILE_HASH);
        if (t == null) throw new ECTagParsingException("Subtag EC_TAG_PARTFILE_HASH not found");
        try {
            return t.getTagValueHash();
        } catch (DataFormatException e) {
            throw new ECTagParsingException("Unexpected tag type " + pt.getTagType(), e); 
        }
    }
    
    @Override
    public void fillFromTag(ECTag pt, byte d) throws ECTagParsingException {
        super.fillFromTag(pt, d);
        try {
            id = pt.getTagValueUInt();
        } catch (DataFormatException e) {
            throw new ECTagParsingException("Unexpected tag type " + pt.getTagType(), e);        
        }
    }

    public long getId() {
        return id;
    }
    
    @Override
    public String toString() {
        return String.format(
                        "ECPartFile [detailLevel=%s, id=%d, hash=%s, fileName=%s, ed2kLink=%s, status=%s, prio=%s, cat=%s, sourceCount=%s, metID=%s, sourceA4AF=%s, sourceXfer=%s, sourceNotCurrent=%s, sizeXfer=%s, sizeFull=%s, sizeDone=%s, speed=%s, lastSeenComp=%s, lastRecv=%s, partStatus=%s, gapStatus=%s, reqStatus=%s, commentCount=%s, comments=%s, sourceNames=%s]",
                        detailLevel, id, getHashAsString(), fileName, ed2kLink, status, prio, cat, sourceCount, metID, sourceA4AF, sourceXfer, sourceNotCurrent,
                        sizeXfer, sizeFull, sizeDone, speed, lastSeenComp, lastRecv, partStatus, gapStatus, reqStatus, commentCount, comments, sourceNames);
    }

}
