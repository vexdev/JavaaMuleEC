package com.iukonline.amule.ec.v204;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.DataFormatException;

import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECTag;
import com.iukonline.amule.ec.exceptions.ECTagParsingException;

public class ECPartFileV204 extends ECPartFile {
    
    protected long id;
    protected HashMap <Long, ECPartFileSourceName> sourceNamesMap;

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
    
    protected void parseSourceNamesList(ECTag pt) throws ECTagParsingException {
        
        if (sourceNamesMap == null) {
            sourceNamesMap = new HashMap <Long, ECPartFileSourceName>();
            sourceNames = new ArrayList<ECPartFileSourceName>();
        }
        
        ECTag t = pt.getSubTagByName(ECCodesV204.EC_TAG_PARTFILE_SOURCE_NAMES);
        if (t == null || t.getSubTags() == null) return;
        
        for (ECTag sourceTag : t.getSubTags()) {
            long sourceId;
            try {
                sourceId = sourceTag.getTagValueUInt();
            } catch (DataFormatException e) {
                throw new ECTagParsingException("Invalid tag type for EC_TAG_PARTFILE_SOURCE_NAMES - " + e.getMessage(), e);            
            }
            ECTag countTag = sourceTag.getSubTagByName(ECCodesV204.EC_TAG_PARTFILE_SOURCE_NAMES_COUNTS);
            if (countTag == null) throw new ECTagParsingException("Tag EC_TAG_PARTFILE_SOURCE_NAMES_COUNTS not found");
            int count;
            try {
                count = (int) countTag.getTagValueUInt();
            } catch (DataFormatException e) {
                throw new ECTagParsingException("Invalid tag type for EC_TAG_PARTFILE_SOURCE_NAMES_COUNTS - " + e.getMessage(), e);            }
            
            if (! sourceNamesMap.containsKey(sourceId)) {
                ECTag nameTag = sourceTag.getSubTagByName(ECCodesV204.EC_TAG_PARTFILE_SOURCE_NAMES);
                if (nameTag == null) throw new ECTagParsingException("Tag EC_TAG_PARTFILE_SOURCE_NAMES not found");
                String name;
                try {
                    name = nameTag.getTagValueString();
                } catch (DataFormatException e) {
                    throw new ECTagParsingException("Invalid tag type for EC_TAG_PARTFILE_SOURCE_NAMES - " + e.getMessage(), e);
                }
                
                ECPartFileSourceName newSourceName = new ECPartFileSourceName(name, count);
                sourceNames.add(newSourceName);
                sourceNamesMap.put(sourceId, newSourceName);
            } else {
                if (count > 0) {
                    sourceNamesMap.get(sourceId).setCount(count);
                } else {
                    sourceNames.remove(sourceNamesMap.get(sourceId));
                    sourceNamesMap.remove(sourceId);
                }
            }
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
    
    
    public class ECPartFileSourceNameV204 extends ECPartFile.ECPartFileSourceName {
        
        

        public ECPartFileSourceNameV204(Iterator <ECTag> i) throws ECTagParsingException   {
            ECTag t;
            if (i.hasNext()) {
                t = i.next();
                if (t.getTagName() != ECCodesV204.EC_TAG_PARTFILE_SOURCE_NAMES) throw new ECTagParsingException("Unexpected tag " + t.getTagName() + " while looking for EC_TAG_PARTFILE_SOURCE_NAMES");
                
                ECTag st = t.getSubTagByName(ECCodesV204.EC_TAG_PARTFILE_SOURCE_NAMES);
                if (st == null) {
                    //throw new ECTagParsingException("Missing subtag EC_TAG_PARTFILE_SOURCE_NAMES");
                    // Sometimes this is not present - why?. Let's add an empty sring
                    name = "";
                } else {
                    try {
                        name = new String(st.getTagValueString());
                    } catch (DataFormatException e) {
                        throw new ECTagParsingException("Unexpected tag format for EC_TAG_PARTFILE_SOURCE_NAMES"); 
                    }
                }
                
                st = t.getSubTagByName(ECCodesV204.EC_TAG_PARTFILE_SOURCE_NAMES_COUNTS);
                if (st == null) throw new ECTagParsingException("Missing subtag EC_TAG_PARTFILE_SOURCE_NAMES_COUNTS");
                try {
                    count = (int) st.getTagValueUInt();
                } catch (DataFormatException e) {
                    throw new ECTagParsingException("Unexpected tag format for EC_TAG_PARTFILE_SOURCE_NAMES_COUNTS");                
                }
            }
        }        
    }

}
