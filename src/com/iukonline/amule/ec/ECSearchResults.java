package com.iukonline.amule.ec;

import java.util.ArrayList;

import com.iukonline.amule.ec.exceptions.ECTagParsingException;


public class ECSearchResults {
    
    public ECSearchResults(ArrayList <ECTag> tagList) throws ECTagParsingException {
        updateSearchResults(tagList);
    }
    
    public ECSearchResults(byte detail) {
    }
    
    public ECSearchResults() {
    }

    
    public void updateSearchResults(ArrayList <ECTag> tagList) throws ECTagParsingException {
        
    }
    
    
}
