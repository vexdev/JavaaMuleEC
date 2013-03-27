package com.iukonline.amule.ec;

import java.util.ArrayList;
import java.util.HashMap;

import com.iukonline.amule.ec.exceptions.ECTagParsingException;


public class ECSearchResults {
    
    public HashMap<Object, ECSearchFile> resultMap = new HashMap<Object, ECSearchFile>();
    
    public ECSearchResults(ArrayList <ECTag> tagList) throws ECTagParsingException {
        updateSearchResults(tagList);
    }
    
    public ECSearchResults(byte detail) {
    }
    
    public ECSearchResults() {
    }

    public void updateSearchResults(ArrayList <ECTag> tagList) throws ECTagParsingException {
        
    }
    
    public int getTotalSources() {
        int count = 0;
        for (ECSearchFile s : resultMap.values()) {
            count += s.getSourceCount();
        }
        return count;
    }
    
}
