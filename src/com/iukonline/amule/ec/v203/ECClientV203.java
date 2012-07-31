package com.iukonline.amule.ec.v203;

import com.iukonline.amule.ec.ECPacket;
import com.iukonline.amule.ec.exceptions.ECClientException;
import com.iukonline.amule.ec.v204.ECClientV204;

public class ECClientV203 extends ECClientV204 {
    
    @Override
    protected ECPacket buildLoginRequest() throws ECClientException {
        return buildLoginRequest((long) 0x0203);
    }
}
