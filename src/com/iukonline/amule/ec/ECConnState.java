package com.iukonline.amule.ec;

import java.util.zip.DataFormatException;

public class ECConnState {
    
    private final static int CONNECTED_ED2K             = 0x1;
    private final static int CONNECTED_ED2K_CONNECTING  = 0x2;
    private final static int CONNECTED_KAD              = 0x4;
    private final static int CONNECTED_KAD_FIREWALLED   = 0x8;
    private final static int CONNECTED_KAD_RUNNING      = 0x10;
    
    
    
    private byte detailLevel;
    
    private int connState;
    private long clientId;
    private long ed2kId;
    private ECServer server;
    
    
    public ECConnState(ECTag t1, byte d) throws ECException   {
        
        detailLevel = d;
        ECTag t;

        try {
            
            switch (detailLevel) {
            case ECCodes.EC_DETAIL_FULL:
            case ECCodes.EC_DETAIL_WEB:
            case ECCodes.EC_DETAIL_CMD:
                
                connState = (int) t1.getTagValueUInt();
                
                if (isConnectedEd2k()) {
                    t = t1.getSubTagByName(ECCodes.EC_TAG_SERVER);
                    if (t != null) server = new ECServer(t, detailLevel);
                    else throw new ECException("Missing EC_TAG_SERVER in server response");
                    
                    t = t1.getSubTagByName(ECCodes.EC_TAG_ED2K_ID);
                    if (t != null) ed2kId = t.getTagValueUInt();
                    else throw new ECException("Missing EC_TAG_ED2K_ID in server response");           
                }
                
                t = t1.getSubTagByName(ECCodes.EC_TAG_CLIENT_ID);
                if (t != null) clientId = t.getTagValueUInt();
                else throw new ECException("Missing EC_TAG_CLIENT_ID in server response");     
                
                break;
                
            default:
                throw new ECException("Unknown detail level " + detailLevel + " for EC_TAG_SERVER");

            }
            
        } catch (DataFormatException e) {
            throw new ECException("One or more unexpected type in EC_TAG_SERVER tags", e);
        }
        
    }
    
    public boolean isConnectedEd2k() {
        return ((connState & CONNECTED_ED2K) == CONNECTED_ED2K);
    }
    
    public boolean isConnectingEd2k() {
        return ((connState & CONNECTED_ED2K_CONNECTING) == CONNECTED_ED2K_CONNECTING);
    }
    
    
    public boolean isConnectedKad() {
        return ((connState & CONNECTED_KAD) == CONNECTED_KAD);
    }
    
    public boolean isKadRunning() {
        return ((connState & CONNECTED_KAD_RUNNING) == CONNECTED_KAD_RUNNING);
    }
    
    public boolean isKadFirewalled() {
        return ((connState & CONNECTED_KAD_FIREWALLED) == CONNECTED_KAD_FIREWALLED);
    }
    
    


    public byte getDetailLevel() {
        return detailLevel;
    }
    
    
    public int getConnState() {
        return connState;
    }
    
    
    public long getClientId() {
        return clientId;
    }
    
    
    public long getEd2kId() {
        return ed2kId;
    }
    
    
    public ECServer getServer() {
        return server;
    }


    @Override
    public String toString() {
        return String.format("ECConnState [detailLevel=%s, connState=%s, clientId=%s, ed2kId=%s, server=%s]", detailLevel, connState, clientId, ed2kId, server);
    }

}
