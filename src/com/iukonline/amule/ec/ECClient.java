package com.iukonline.amule.ec;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.DataFormatException;




public class ECClient {
    

    private String clientName;
    private String clientVersion;
    private byte[] hashedPassword;
    private Socket socket;
    private PrintStream tracer;
    
    private boolean loggedIn = false;
   
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }
    
    public void setHashedPassword(byte[] hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
    
    public void setPassword(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException  {
        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
        digest.update(password.getBytes("UTF-8"));
        setHashedPassword(digest.digest());
    }
    
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setTracer(PrintStream tracer) {
        this.tracer = tracer;
    }
    
    public ECPacket sendRequestAndWaitResponse(ECPacket epReq, boolean tryLogin) throws IOException, ECException {
        ECPacket epResp = new ECPacket();
        
        OutputStream os = socket.getOutputStream();
        if (tracer != null) {
            tracer.print("Sending EC packet...\n" + epReq.toString() + "\n\n");
        }
        epReq.writeToStream(os);
        BufferedInputStream is = new BufferedInputStream(socket.getInputStream());
        epResp.readFromStream(is);
        
        if (tracer != null) {
            tracer.print("Received EC packet...\n" + epResp.toString() + "\n\n");
        }
        
        if ((epResp.getOpCode() == ECPacket.EC_OP_FAILED) || (tryLogin && epResp.getOpCode() == ECPacket.EC_OP_AUTH_FAIL)) {
            String errMsg = "No error returned.";
            ECTag tagError = epResp.getTagByName((short) ECTag.EC_TAG_STRING);
            if (tagError != null) {
                try {
                    errMsg = tagError.getTagValueString();
                } catch (DataFormatException e) {
                    throw new ECException("Cannot read returned error message", epResp, e);
                }
            }
            
            if (tryLogin && epResp.getOpCode() == ECPacket.EC_OP_AUTH_FAIL) {
                boolean result = false;

                try {
                    result = this.login();
                } catch (Exception e) {
                    // Catch any exception. If login fails, the original error must be returned, not this new one.
                }
                
                if (result) {
                    return this.sendRequestAndWaitResponse(epReq, false);
                }
            }
            
            throw new ECException("Request failed: " + errMsg);
        }
        
        return epResp;

    }
    
    public ECPacket sendRequestAndWaitResponse(ECPacket epReq) throws IOException, ECException {
        return sendRequestAndWaitResponse(epReq, true);
    }

    public boolean login() throws IOException, ECException {
        
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECPacket.EC_OP_AUTH_REQ);
        
        try {
            epReq.addTag(new ECTag(ECTag.EC_TAG_CLIENT_NAME, clientName));
            epReq.addTag(new ECTag(ECTag.EC_TAG_CLIENT_VERSION, clientVersion));
            epReq.addTag(new ECTag(ECTag.EC_TAG_PROTOCOL_VERSION, ECTag.EC_TAGTYPE_UINT16, (long) ECTag.EC_CURRENT_PROTOCOL_VERSION));
            epReq.addTag(new ECTag(ECTag.EC_TAG_PASSWD_HASH, ECTag.EC_TAGTYPE_HASH16, hashedPassword));
        } catch (DataFormatException e) {
            throw new ECException("Severe error, this should never happen here", epReq, e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq, false);

        switch (epResp.getOpCode()) {
        case ECPacket.EC_OP_AUTH_OK:
            // TODO Save server version
            this.loggedIn = true;
            return true;
        case ECPacket.EC_OP_AUTH_FAIL:
            this.loggedIn = false;
            return false;
        default:
            throw new ECException("Unexpected response to login request", epResp);
        }
    }
    
    public void addED2KURL(String url) throws ECException, IOException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECPacket.EC_OP_ADD_LINK);
        try {
            epReq.addTag(new ECTag(ECTag.EC_TAG_PARTFILE_ED2K_LINK, ECTag.EC_TAGTYPE_STRING, url));
        } catch (DataFormatException e) {
            throw new ECException("Invalid string provided", epReq);
        }
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECPacket.EC_OP_NOOP:
            // TODO Do something?
            return;
        default:
            throw new ECException("Unexpected response download queue request", epResp);        
        }
    }
    
    public ECPartFile[] getDownloadQueue() throws ECException, IOException {
        return getDownloadQueue(null, null);
    }
    
    public ECPartFile getDownloadQueueItem(byte[] hash) throws ECException, IOException {
        ECPartFile[] list = getDownloadQueue(hash, null);
        return list.length > 0 ? list[0] : null;
    }
    
    public ECPartFile getDownloadQueueItem(ECPartFile p) throws ECException, IOException {
        ECPartFile[] list = getDownloadQueue(p.getHash(), null);
        return list.length > 0 ? list[0] : null;
    }
    
    
    private ECPartFile[] getDownloadQueue(byte[] hash, ECPartFile p) throws ECException, IOException {
        
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECPacket.EC_OP_GET_DLOAD_QUEUE);
        if (hash != null) {
            try {
                epReq.addTag(new ECTag(ECTag.EC_TAG_PARTFILE, ECTag.EC_TAGTYPE_HASH16, hash ));
            } catch (DataFormatException e) {
                throw new ECException("Invalid hash provided", epReq);
            }
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        
        switch (epResp.getOpCode()) {
        case ECPacket.EC_OP_DLOAD_QUEUE:
            // Do Something
   
            ArrayList<ECTag> tags = epResp.getTags();
            
            if (! tags.isEmpty()) {
                ECPartFile[] dlQueue = new ECPartFile[tags.size()];
                Iterator<ECTag> itr = tags.iterator();
                int i = 0;
                while (itr.hasNext()) {
                    ECTag dlTag = itr.next();
                    try {
                        if (p != null) {
                            if (i > 0) {
                                throw new ECException("Error parsing response", epResp);
                            }
                            p.fillFromTag(dlTag);
                            dlQueue[i] = p;
                        } else {
                            dlQueue[i] = new ECPartFile(dlTag);
                            
                        }
                        dlQueue[i].setClient(this);

                    } catch (DataFormatException e) {
                        throw new ECException("Error parsing response", epResp, e);
                    }
                    
                    i++;
                }
                
                return dlQueue;
                
            } else {
                return null;
            }
        default:
            throw new ECException("Unexpected response download queue request", epResp);
        }        
    }
    
    public ECPartFile getDownloadDetails(ECPartFile p) throws IOException, ECException {
        return getDownloadDetails(p.getHash(), p);
    }
    
    public ECPartFile getDownloadDetails(byte[] hash) throws IOException, ECException {
        return getDownloadDetails(hash, null);
    }
    
    public ECPartFile getDownloadDetails(byte[] hash, ECPartFile p) throws IOException, ECException {

        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECPacket.EC_OP_GET_DLOAD_QUEUE_DETAIL);
        try {
            epReq.addTag(new ECTag(ECTag.EC_TAG_PARTFILE, ECTag.EC_TAGTYPE_HASH16, hash));
        } catch (DataFormatException e) {
            // TODO Auto-generated catch block
            throw new ECException("Error creating request", epReq, e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECPacket.EC_OP_DLOAD_QUEUE:
            
            ECPartFile dl;
            try {
                if (p != null) {
                    p.fillFromTag(epResp.getTagByName(ECPacket.EC_TAG_PARTFILE));
                    dl = p;
                } else {
                    dl = new ECPartFile(epResp.getTagByName(ECPacket.EC_TAG_PARTFILE));
                }
            } catch (DataFormatException e) {
                // TODO Auto-generated catch block
                throw new ECException("Unexpected response download queue request", epResp);
            }
            dl.setClient(this);
            return dl;
            
        default:
            throw new ECException("Unexpected response download queue request", epResp);
            //dlDetail = new ECPartFile();
        }

    }
    
    public ECStats getStats() throws ECException, IOException {
       /*if (! this.loggedIn) {
            throw new ECException("Client not logged in");
        }*/
        
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECPacket.EC_OP_STAT_REQ);
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECPacket.EC_OP_STATS:
            
            ECStats stats = new ECStats();
            try {
                stats.setClient(this);
                stats.setSpeedDl(epResp.getTagByName(ECTag.EC_TAG_STATS_DL_SPEED).getTagValueUInt());
                stats.setSpeedUl(epResp.getTagByName(ECTag.EC_TAG_STATS_UL_SPEED).getTagValueUInt());
            } catch (DataFormatException e) {
                throw new ECException("Error parsing response", epResp, e);
            }
            
            return stats;
        default:
            throw new ECException("Unexpected response download queue request", epResp);
        }
        
    }
    
    void changeDownloadStatus(byte[] hash, byte opCode) throws ECException, IOException {
       /* if (! this.loggedIn) {
            throw new ECException("Client not logged in");
        }*/
        
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(opCode);
        try {
            epReq.addTag(new ECTag(ECTag.EC_TAG_PARTFILE, ECTag.EC_TAGTYPE_HASH16, hash));
        } catch (DataFormatException e) {
           
            throw new ECException("Error creating request", epReq, e);
        }
        
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECPacket.EC_OP_NOOP:
            // TODO Do something?
            return;
        default:
            throw new ECException("Unexpected response download queue request", epResp);        
        }
        
    }
    
    public void renameDownload(byte[] hash, String newName) throws ECException, IOException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECPacket.EC_OP_RENAME_FILE);
        try {
            epReq.addTag(new ECTag(ECTag.EC_TAG_KNOWNFILE, ECTag.EC_TAGTYPE_HASH16, hash));
            epReq.addTag(new ECTag(ECTag.EC_TAG_PARTFILE_NAME, ECTag.EC_TAGTYPE_STRING, newName));
        } catch (DataFormatException e) {
            // TODO Auto-generated catch block
            throw new ECException("Error creating request", epReq, e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECPacket.EC_OP_NOOP:
            // TODO Do something?
            return;
        default:
            throw new ECException("Unexpected response to set priority", epResp);        
        }        
        
    }
    
    public void setDownloadPriority(byte[] hash, byte prio) throws ECException, IOException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECPacket.EC_OP_PARTFILE_PRIO_SET);
        try {
            ECTag t = new ECTag(ECTag.EC_TAG_PARTFILE, ECTag.EC_TAGTYPE_HASH16, hash);
            t.addSubTag(new ECTag(ECTag.EC_TAG_PARTFILE_PRIO, ECTag.EC_TAGTYPE_UINT8, prio));
            epReq.addTag(t);
            
            //epReq.addTag(new ECTag(ECTag.EC_TAG_PARTFILE_PRIO, ECTag.EC_TAGTYPE_UINT8, prio));
            //epReq.addTag(new ECTag(ECTag.EC_TAG_PARTFILE, ECTag.EC_TAGTYPE_HASH16, hash));
            
        } catch (DataFormatException e) {
            // TODO Auto-generated catch block
            throw new ECException("Error creating request", epReq, e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECPacket.EC_OP_NOOP:
            // TODO Do something?
            return;
        default:
            throw new ECException("Unexpected response to set priority", epResp);        
        }
    }

    @Override
    public String toString() {
        //return String.format("ECClient [clientName=%s, clientVersion=%s, socket=%s]", clientName, clientVersion, socket);
        return String.format("ECClient [socket=%s]", socket);
    }
}
