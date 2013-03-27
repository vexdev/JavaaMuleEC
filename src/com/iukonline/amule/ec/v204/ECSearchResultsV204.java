package com.iukonline.amule.ec.v204;

import java.util.ArrayList;
import java.util.zip.DataFormatException;

import com.iukonline.amule.ec.ECSearchFile;
import com.iukonline.amule.ec.ECSearchResults;
import com.iukonline.amule.ec.ECTag;
import com.iukonline.amule.ec.exceptions.ECTagParsingException;

public class ECSearchResultsV204 extends ECSearchResults {
    
    public ECSearchResultsV204(ArrayList <ECTag> tagList) throws ECTagParsingException {
        updateSearchResults(tagList);
    }
    
    public ECSearchResultsV204(byte detail) {
    }
    
    public void updateSearchResults(ArrayList <ECTag> tagList) throws ECTagParsingException {
        
        for (ECTag t : tagList) {
            if (t.getTagName() != ECCodesV204.EC_TAG_SEARCHFILE) throw new ECTagParsingException("Unexpected tag " + t.getTagName() + " found while looking for EC_TAG_SEARCHFILE");

            long id;
            try {
                id = t.getTagValueUInt();
            } catch (DataFormatException e) {
                throw new ECTagParsingException("Unexpected tag type for EC_TAG_SEARCHFILE", e);
            }
            
            if (resultMap.containsKey(id)) {
                resultMap.get(id).updateFromECTag(t);
            } else {
                resultMap.put(id, new ECSearchFileV204(t));
            }
        }
        
        for (ECSearchFile s : resultMap.values()) {
            ECSearchFileV204 s204 = (ECSearchFileV204) s;
            long parentId = s204.getParentId();
            if (parentId > 0) {
                ECSearchFileV204 p = (ECSearchFileV204) resultMap.get(parentId);
                if (p == null) throw new ECTagParsingException("Search result is referring to an unknown parent " + parentId);
                s204.setParent(p);
                p.addChild(s204);
            }
        }
        
    }

    @Override
    public String toString() {
        return String.format("ECSearchResultsV204 [resultMap=%s]", resultMap);
    }
    
    
}
