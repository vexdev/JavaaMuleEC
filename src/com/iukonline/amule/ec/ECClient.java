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
import java.util.zip.DataFormatException;




public class ECClient {

    private byte defaultDetailLevel = ECCodes.EC_DETAIL_FULL;
    
    private String clientName;
    private String clientVersion;
    private byte[] hashedPassword;
    private Socket socket;
    private PrintStream tracer;
    
    private String serverVersion;
    
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
    
    public String getServerVersion() {
        return serverVersion;
    }
    
    
    
    public ECPacket sendRequestAndWaitResponse(ECPacket epReq, boolean tryLogin) throws IOException, ECException {

        OutputStream os = socket.getOutputStream();
        if (tracer != null) tracer.print("Sending EC packet...\n" + epReq.toString() + "\n\n");

        try {
            epReq.writeToStream(os);
        } catch (ECException e) {
            throw new ECException("Error sending request", epReq, e);
        }
        if (tracer != null) tracer.print("Sent EC packet...\n" + epReq.toString() + "\n\n");


        BufferedInputStream is = new BufferedInputStream(socket.getInputStream());
        ECPacket epResp;
        try {
            epResp = ECPacket.readFromStream(is);
        } catch (ECException e) {
            throw new ECException("Error reading response", epReq, e);
        }
        
        if (tracer != null) tracer.print("Received EC packet...\n" + epResp.toString() + "\n\n");
        
        if ((epResp.getOpCode() == ECCodes.EC_OP_FAILED) || (tryLogin && epResp.getOpCode() == ECCodes.EC_OP_AUTH_FAIL)) {
            
            
            String errMsg = "No error returned.";
            ECTag tagError = epResp.getTagByName((short) ECCodes.EC_TAG_STRING);
            if (tagError != null) {
                try {
                    errMsg = tagError.getTagValueString();
                } catch (DataFormatException e) {
                    throw new ECException("Cannot read returned error message", epResp, e);
                }
            }
            if (tryLogin && epResp.getOpCode() == ECCodes.EC_OP_AUTH_FAIL) {
                boolean result = false;

                try {
                    result = this.login();
                } catch (ECException e) {
                    throw new ECException("Error while logging in", e);
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
    
    

    public boolean login() throws IOException, ECException  {
        
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_AUTH_REQ);
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_CLIENT_NAME, clientName));
            epReq.addTag(new ECTag(ECCodes.EC_TAG_CLIENT_VERSION, clientVersion));
            epReq.addTag(new ECTag(ECCodes.EC_TAG_PROTOCOL_VERSION, ECTagTypes.EC_TAGTYPE_UINT16, (long) ECCodes.EC_CURRENT_PROTOCOL_VERSION));
            epReq.addTag(new ECTag(ECCodes.EC_TAG_PASSWD_HASH, ECTagTypes.EC_TAGTYPE_HASH16, hashedPassword));
        } catch (DataFormatException e) {
            throw new ECException("Severe error, this should never happen here", epReq, e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq, false);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_AUTH_OK:
            ECTag versionTag = epResp.getTagByName(ECCodes.EC_TAG_SERVER_VERSION);
            if (versionTag == null) throw new ECException("Server version not present in auth response", epResp);
            try {
                serverVersion = versionTag.getTagValueString();
            } catch (DataFormatException e) {
                throw new ECException("Unexpected format for server version", epResp, e);
            }            
            return true;
        case ECCodes.EC_OP_AUTH_FAIL:
            return false;
        default:
            throw new ECException("Unexpected response to login request", epResp);
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    private ECPacket sendGetDloadQueueReq(byte[] hash, byte detailLevel) throws IOException, ECException {
            ECPacket epReq = new ECPacket();
            
            try {
                epReq.addTag(new ECTag(ECCodes.EC_TAG_DETAIL_LEVEL, ECTagTypes.EC_TAGTYPE_UINT8, detailLevel));
            } catch (DataFormatException e) {
                // Should nevere happen
                throw new ECException("Severe exception. This should never have been happened.", e);
            }
            
            if (hash != null) {
                epReq.setOpCode(ECCodes.EC_OP_GET_DLOAD_QUEUE_DETAIL);
                try {
                    epReq.addTag(new ECTag(ECCodes.EC_TAG_PARTFILE, ECTagTypes.EC_TAGTYPE_HASH16, hash ));
                } catch (DataFormatException e) {
                    throw new ECException("Invalid hash provided", epReq, e);
                }
            } else {
                epReq.setOpCode(ECCodes.EC_OP_GET_DLOAD_QUEUE);
            }
            
            ECPacket epResp;
            try {
                epResp = sendRequestAndWaitResponse(epReq);
            } catch (ECException e) {
                throw new ECException("Error fetching download queue", e);

            }
            
            switch (epResp.getOpCode()) {
            case ECCodes.EC_OP_DLOAD_QUEUE:
                if (hash != null && epResp.getTags().size() > 1) throw new ECException("Unexpected response for single part file GET_DLOAD_QUEUE", epResp);
                return epResp;
            default:
                throw new ECException("Unexpected response for GET_DLOAD_QUEUE", epResp);
            }        
    }
    
    public ECPartFile getPartFile(byte[] hash, byte detailLevel) throws IOException, ECException {
        ECPacket partFilePacket = sendGetDloadQueueReq(hash, detailLevel);
        try {
            return (partFilePacket.getTags().size() == 0) ? null : new ECPartFile(partFilePacket.getTagByName(ECCodes.EC_TAG_PARTFILE), detailLevel);
        } catch (ECException e) {
            throw new ECException("Error building ECPartFile object", partFilePacket, e);
        }
    }
    
    public ECPartFile getPartFile(byte[] hash) throws IOException, ECException {
        return getPartFile(hash, defaultDetailLevel);
    }
    
    public void refreshPartFile(ECPartFile p, byte detailLevel) throws ECException, IOException {
        ECPacket partFilePacket = sendGetDloadQueueReq(p.getHash(), detailLevel);
        if (partFilePacket.getTags().isEmpty()) throw new ECException("Part file not found", partFilePacket);
        try {
            p.fillFromTag(partFilePacket.getTagByName(ECCodes.EC_TAG_PARTFILE), detailLevel);
        } catch (ECException e) {
            throw new ECException("Error refreshing ECPartFile object", partFilePacket, e);
        }
    }
    
    public void refreshPartFile(ECPartFile p) throws ECException, IOException {
        refreshPartFile(p, defaultDetailLevel);
    }
    
    public ECPartFile[] getDownloadQueue(byte detailLevel) throws IOException, ECException {
        ECPacket partFilePacket = sendGetDloadQueueReq(null, detailLevel);
        
        ArrayList <ECTag> tags = partFilePacket.getTags();
        if (tags.isEmpty()) return null;
        
        ECPartFile[] dlQueue = new ECPartFile[tags.size()];
        
        for (int i = 0; i < tags.size(); i++) {
            try {
                dlQueue[i] = new ECPartFile(tags.get(i), detailLevel);
            } catch (ECException e) {
                throw new ECException("Error building ECPartFile object", partFilePacket, e);
            }
        }
        return dlQueue;
    }
    
    public ECPartFile[] getDownloadQueue() throws IOException, ECException {
        return getDownloadQueue(defaultDetailLevel);
    }
    

    
    public ECStats getStats() throws IOException, ECException {
        return getStats(defaultDetailLevel);
    }
    
    public ECStats getStats(byte detailLevel) throws IOException, ECException  {
        
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_STAT_REQ);
        
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_DETAIL_LEVEL, ECTagTypes.EC_TAGTYPE_UINT8, detailLevel));
        } catch (DataFormatException e) {
            throw new ECException("Severe error: unable to set detail level on request.", epReq, e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_STATS:
            return new ECStats(epResp, detailLevel);
        default:
            throw new ECException("Unexpected response to OP_STAT_REQ", epResp);
        }
        
    }

    
    
    void changeDownloadStatus(byte[] hash, byte opCode) throws ECException, IOException {

        ECPacket epReq = new ECPacket();
        epReq.setOpCode(opCode);
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_PARTFILE, ECTagTypes.EC_TAGTYPE_HASH16, hash));
        } catch (DataFormatException e) {
           
            throw new ECException("Error creating request", epReq, e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_NOOP:
            // TODO Do something?
            return;
        default:
            throw new ECException("Unexpected response to change download status request", epResp);        
        }
        
    }

    
    public void addED2KLink(String url) throws ECException, IOException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_ADD_LINK);
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_PARTFILE_ED2K_LINK, ECTagTypes.EC_TAGTYPE_STRING, url));
        } catch (DataFormatException e) {
            throw new ECException("Invalid string provided", epReq, e);
        }
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_NOOP:
            return;
        default:
            throw new ECException("Unexpected response download queue request", epResp);        
        }
    }

    
    public void renamePartFile(byte[] hash, String newName) throws ECException, IOException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_RENAME_FILE);
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_KNOWNFILE, ECTagTypes.EC_TAGTYPE_HASH16, hash));
            epReq.addTag(new ECTag(ECCodes.EC_TAG_PARTFILE_NAME, ECTagTypes.EC_TAGTYPE_STRING, newName));
        } catch (DataFormatException e) {
            throw new ECException("Error creating request", epReq, e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_NOOP:
            // TODO Do something?
            return;
        default:
            throw new ECException("Error while renaming Part File", epResp);        
        }        
    }
    
    
    public void setPartFilePriority(byte[] hash, byte prio) throws ECException, IOException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_PARTFILE_PRIO_SET);
        try {
            ECTag t = new ECTag(ECCodes.EC_TAG_PARTFILE, ECTagTypes.EC_TAGTYPE_HASH16, hash);
            t.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_PRIO, ECTagTypes.EC_TAGTYPE_UINT8, prio));
            epReq.addTag(t);
            
        } catch (DataFormatException e) {
            throw new ECException("Error creating request", epReq, e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_NOOP:
            // TODO Do something?
            return;
        default:
            throw new ECException("Unexpected response to set priority", epResp);        
        }
    }
    

    
    public ECCategory[] getCategories(byte detailLevel) throws IOException, ECException {
        
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_GET_PREFERENCES);
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_SELECT_PREFS, ECTagTypes.EC_TAGTYPE_UINT32, ECCodes.EC_PREFS_CATEGORIES));
            epReq.addTag(new ECTag(ECCodes.EC_TAG_DETAIL_LEVEL, ECTagTypes.EC_TAGTYPE_UINT8, detailLevel));
        } catch (DataFormatException e) {
            throw new ECException("Severe Exception. This should never happen", epReq, e);

        }
      
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_SET_PREFERENCES:

            ECTag t = epResp.getTagByName(ECCodes.EC_TAG_PREFS_CATEGORIES);
            if (t == null) return null;
            
            ArrayList <ECTag> l = t.getSubTags();
            if (l.size() == 0) return null;
            
            ECCategory[] categoryList = new ECCategory[l.size()];
            for (int i = 0; i < l.size(); i++) {
                ECTag t1 = l.get(i);
                if (t1.getTagName() != ECCodes.EC_TAG_CATEGORY) throw new ECException("Unexpected tag " + t1.getTagName() + " found while looking for EC_TAG_CATEGORY", epResp);
                try {
                    categoryList[i] = new ECCategory(t1, detailLevel);
                } catch (ECException e) {
                    throw new ECException("Error while building ECCategory Object", epResp, e);
                }
            }
            return categoryList;
            
        default:
            throw new ECException("Unexpected response to get categories", epResp);        
            
        }
    }
    
    
    public void createCategory(ECCategory c) throws ECException, IOException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_CREATE_CATEGORY);
        epReq.addTag(c.toECTag());

        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_NOOP:
            // TODO Do something?
            return;
        default:
            throw new ECException("Unexpected response to create category", epResp);        
        }
    }
    
    public void updateCategory(ECCategory c) throws ECException, IOException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_UPDATE_CATEGORY);
        epReq.addTag(c.toECTag());
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_NOOP:
            // TODO Do something?
            return;
        default:
            throw new ECException("Unexpected response to update category", epResp);        
        }
    }
    
    public void deleteCategory(long id) throws ECException, IOException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_DELETE_CATEGORY);
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_CATEGORY, ECTagTypes.EC_TAGTYPE_UINT32, id));
        } catch (DataFormatException e) {
            throw new ECException("Severe Exception. This should never happen", epReq, e);
        }
      
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_NOOP:
            // TODO Do something?
            return;
        default:
            throw new ECException("Unexpected response to delete category", epResp);        
        }
    }
    
    
    
    
    
    
    
    
    

    @Override
    public String toString() {
        //return String.format("ECClient [clientName=%s, clientVersion=%s, socket=%s]", clientName, clientVersion, socket);
        return String.format("ECClient [socket=%s]", socket);
    }
}
