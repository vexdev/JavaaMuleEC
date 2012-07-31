package com.iukonline.amule.ec.fake;

import java.util.zip.DataFormatException;

import com.iukonline.amule.ec.ECCodes;
import com.iukonline.amule.ec.ECPacket;
import com.iukonline.amule.ec.ECStats;
import com.iukonline.amule.ec.ECTag;
import com.iukonline.amule.ec.exceptions.ECTagParsingException;

public class ECStatsFake extends ECStats {
    
    public ECStatsFake(ECPacket p, byte d) throws ECTagParsingException  {
        
        detailLevel = d;
        ECTag t;

        try {
            
                t = p.getTagByName(ECCodes.EC_TAG_STATS_UL_SPEED);
                if (t != null) ulSpeed = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_STATS_UL_SPEED in server response");
                
                t = p.getTagByName(ECCodes.EC_TAG_STATS_DL_SPEED);
                if (t != null) dlSpeed = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_STATS_DL_SPEED in server response");
            
        } catch (DataFormatException e) {
            throw new ECTagParsingException("One or more unexpected type in EC_STATS tags", e);
        }
        
    }

    

}
