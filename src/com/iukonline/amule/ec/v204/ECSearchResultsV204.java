package com.iukonline.amule.ec.v204;

import java.util.ArrayList;
import java.util.HashMap;

import com.iukonline.amule.ec.ECTag;

public class ECSearchResultsV204 {
    byte detail;
    HashMap<String, ECSearchResultsV204> resultMap = new HashMap<String, ECSearchResultsV204>();
    
    public ECSearchResultsV204(ArrayList <ECTag> tagList, byte detail) {
        this.detail = detail;
        updateSearchResults(tagList);
    }
    
    public void updateSearchResults(ArrayList <ECTag> tagList) {
        
    }
}
