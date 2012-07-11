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
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.DataFormatException;

import com.iukonline.amule.ec.exceptions.ECClientException;
import com.iukonline.amule.ec.exceptions.ECPacketParsingException;
import com.iukonline.amule.ec.exceptions.ECServerException;
import com.iukonline.amule.ec.exceptions.ECTagParsingException;




public class ECClient {
    
    private final static boolean FORCE_TRY_LOGIN_BEFORE = true; 
    // Must be true to ensure compatibiliy with 2.2.2.
    // 2.2.2 Close TCP socket when receiving unauthenticated request

    private byte defaultDetailLevel = ECCodes.EC_DETAIL_FULL;
    
    private String clientName;
    private String clientVersion;
    private byte[] hashedPassword;
    private Socket socket;
    private PrintStream tracer;
    
    private String serverVersion;
    
    private boolean isLoggedIn = false;
    
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
    
    public ECPacket sendRequestAndWaitResponse(ECPacket epReq, boolean tryLogin) throws IOException, ECPacketParsingException, ECServerException, ECClientException {

        if (FORCE_TRY_LOGIN_BEFORE && tryLogin && !isLoggedIn) login();
        
        OutputStream os = socket.getOutputStream();
        if (tracer != null) tracer.print("Sending EC packet...\n" + epReq.toString() + "\n\n");

        try {
            epReq.writeToStream(os);
        } catch (ECPacketParsingException e) {
            throw new ECPacketParsingException("Error sending request", epReq.getRawPacket(), e);
        }
        if (tracer != null) tracer.print("Sent EC packet...\n" + epReq.toString() + "\n\n");


        BufferedInputStream is = new BufferedInputStream(socket.getInputStream());
        ECPacket epResp;
        try {
            epResp = ECPacket.readFromStream(is);
        } catch (ECPacketParsingException e) {
            throw new ECPacketParsingException("Error reading response", e.getCausePacket(), e);
        }
        
        if (tracer != null) tracer.print("Received EC packet...\n" + epResp.toString() + "\n\n");
        
        if (epResp.getOpCode() == ECCodes.EC_OP_FAILED) {
            
            String errMsg = "No error returned.";
            ECTag tagError = epResp.getTagByName((short) ECCodes.EC_TAG_STRING);
            if (tagError != null) {
                try {
                    errMsg = tagError.getTagValueString();
                } catch (DataFormatException e) {
                    throw new ECPacketParsingException("Cannot read returned error message", epResp.getRawPacket(), e);
                }
            }
            
            throw new ECServerException(errMsg, epReq, epResp);
        }
        
        return epResp;

    }
    
    public ECPacket sendRequestAndWaitResponse(ECPacket epReq) throws IOException, ECPacketParsingException, ECServerException, ECClientException {
        return sendRequestAndWaitResponse(epReq, true);
    }
    
    

    public boolean login() throws IOException, ECPacketParsingException, ECServerException, ECClientException  {
        
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_AUTH_REQ);
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_CLIENT_NAME, clientName));
            epReq.addTag(new ECTag(ECCodes.EC_TAG_CLIENT_VERSION, clientVersion));
            epReq.addTag(new ECTag(ECCodes.EC_TAG_PROTOCOL_VERSION, ECTagTypes.EC_TAGTYPE_UINT16, (long) ECCodes.EC_CURRENT_PROTOCOL_VERSION));
            epReq.addTag(new ECTag(ECCodes.EC_TAG_PASSWD_HASH, ECTagTypes.EC_TAGTYPE_HASH16, hashedPassword));
        } catch (DataFormatException e) {
            throw new ECClientException("Cannot create login request", e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq, false);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_AUTH_OK:
            ECTag versionTag = epResp.getTagByName(ECCodes.EC_TAG_SERVER_VERSION);
            if (versionTag == null) throw new ECPacketParsingException("Server version not present in auth response", epResp.getRawPacket());
            try {
                serverVersion = versionTag.getTagValueString();
            } catch (DataFormatException e) {
                throw new ECPacketParsingException("Unexpected format for server version", epResp.getRawPacket(), e);
            }            
            isLoggedIn = true;
            return true;
        case ECCodes.EC_OP_AUTH_FAIL:
            String errMsg = "No error returned.";
            ECTag tagError = epResp.getTagByName((short) ECCodes.EC_TAG_STRING);
            if (tagError != null) {
                try {
                    errMsg = tagError.getTagValueString();
                } catch (DataFormatException e) {
                    throw new ECPacketParsingException("Cannot read returned error message", epResp.getRawPacket(), e);
                }
            }
            throw new ECServerException("Login failed - " + errMsg, epReq, epResp);
        default:
            throw new ECPacketParsingException("Unexpected response to login request", epResp.getRawPacket());
        }
    }
    
    

    
    
    private ECPacket sendGetDloadQueueReq(byte[] hash, byte detailLevel) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
            ECPacket epReq = new ECPacket();
            
            try {
                epReq.addTag(new ECTag(ECCodes.EC_TAG_DETAIL_LEVEL, ECTagTypes.EC_TAGTYPE_UINT8, detailLevel));
            } catch (DataFormatException e) {
                // Should nevere happen
                throw new ECClientException("Cannot greate GetDloadQueue request", e);
            }
            
            if (hash != null) {
                epReq.setOpCode(ECCodes.EC_OP_GET_DLOAD_QUEUE_DETAIL);
                try {
                    epReq.addTag(new ECTag(ECCodes.EC_TAG_PARTFILE, ECTagTypes.EC_TAGTYPE_HASH16, hash ));
                } catch (DataFormatException e) {
                    throw new ECClientException("Invalid hash provided", e);
                }
            } else {
                epReq.setOpCode(ECCodes.EC_OP_GET_DLOAD_QUEUE);
            }
            
            ECPacket epResp;
            epResp = sendRequestAndWaitResponse(epReq);

            
            switch (epResp.getOpCode()) {
            case ECCodes.EC_OP_DLOAD_QUEUE:
                if (hash != null && epResp.getTags().size() > 1) throw new ECPacketParsingException("Unexpected response for single part file GET_DLOAD_QUEUE", epResp.getRawPacket());
                return epResp;
            default:
                throw new ECPacketParsingException("Unexpected response for GET_DLOAD_QUEUE", epResp.getRawPacket());
            }        
    }
    
    public ECPartFile getPartFile(byte[] hash, byte detailLevel) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        ECPacket partFilePacket = sendGetDloadQueueReq(hash, detailLevel);
        try {
            return (partFilePacket.getTags().size() == 0) ? null : new ECPartFile(partFilePacket.getTagByName(ECCodes.EC_TAG_PARTFILE), detailLevel);
        } catch (ECTagParsingException e) {
            throw new ECPacketParsingException("Error parsing partFile packet", partFilePacket.getRawPacket(), e);
        }
    }
    
    public ECPartFile getPartFile(byte[] hash) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        return getPartFile(hash, defaultDetailLevel);
    }
    
    public void refreshPartFile(ECPartFile p, byte detailLevel) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        ECPacket partFilePacket = sendGetDloadQueueReq(p.getHash(), detailLevel);
        if (partFilePacket.getTags().isEmpty()) throw new ECClientException("Part file not found");
        try {
            p.fillFromTag(partFilePacket.getTagByName(ECCodes.EC_TAG_PARTFILE), detailLevel);
        } catch (ECTagParsingException e) {
            throw new ECPacketParsingException("Error parsing partFile packet", partFilePacket.getRawPacket(), e);
        }

    }
    
    public void refreshPartFile(ECPartFile p) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        refreshPartFile(p, defaultDetailLevel);
    }
    
    public HashMap<String, ECPartFile> getDownloadQueue(byte detailLevel) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        ECPacket partFilePacket = sendGetDloadQueueReq(null, detailLevel);
        
        ArrayList <ECTag> tags = partFilePacket.getTags();
        if (tags.isEmpty()) return null;
        
        HashMap<String, ECPartFile> dlQueue = new HashMap<String, ECPartFile>(tags.size());
        
        for (int i = 0; i < tags.size(); i++) {
            ECPartFile p;
            try {
                p = new ECPartFile(tags.get(i), detailLevel);
            } catch (ECTagParsingException e) {
                throw new ECPacketParsingException("Error parsing partFile packet", partFilePacket.getRawPacket(), e);
            }
            dlQueue.put(ECUtils.byteArrayToHexString(p.getHash()), p);
        }
        return dlQueue;
    }
    
    public HashMap<String, ECPartFile> getDownloadQueue() throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        return getDownloadQueue(defaultDetailLevel);
    }
    
    public void refreshDlQueue(HashMap<String, ECPartFile> previousQueue) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        refreshDlQueue(previousQueue, defaultDetailLevel);
    }
    
    public void refreshDlQueue(HashMap<String, ECPartFile> previousQueue, byte detailLevel) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        
        if (previousQueue == null) return;
        
        HashMap<String, ECPartFile> newQueue = getDownloadQueue(defaultDetailLevel);
        ArrayList <String> hashesToBeRemoved = new ArrayList<String>();
        
        Iterator<ECPartFile> iPrev = previousQueue.values().iterator();
        while (iPrev.hasNext()) {
            ECPartFile p = iPrev.next();
            String hString = p.getHashAsString();
            if (newQueue.containsKey(hString)) {
                p.copyValuesFromPartFile(newQueue.get(hString));
                newQueue.remove(hString);
            } else {
                hashesToBeRemoved.add(hString);
            }
        }
        
        Iterator<String> iRemove = hashesToBeRemoved.iterator();
        while (iRemove.hasNext()) {
            previousQueue.remove(iRemove.next());
        }
        
        Iterator<ECPartFile> iNew = newQueue.values().iterator();
        while (iNew.hasNext()) {
            ECPartFile p = iNew.next();
            previousQueue.put(p.getHashAsString(), p);
        }
        
    }
    
    public ECStats getStats() throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        return getStats(defaultDetailLevel);
    }
    
    public ECStats getStats(byte detailLevel) throws IOException, ECClientException, ECPacketParsingException, ECServerException  {
        
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_STAT_REQ);
        
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_DETAIL_LEVEL, ECTagTypes.EC_TAGTYPE_UINT8, detailLevel));
        } catch (DataFormatException e) {
            throw new ECClientException("Cannot build get stats request", e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_STATS:
            try {
                return new ECStats(epResp, detailLevel);
            } catch (ECTagParsingException e) {
                throw new ECPacketParsingException("Error parsing response to OP_STAT_REQ", epResp.getRawPacket(), e);
            }
        default:
            throw new ECPacketParsingException("Unexpected response to OP_STAT_REQ", epResp.getRawPacket());
        }
        
    }

    
    public void changeDownloadStatus(byte[] hash, byte opCode) throws IOException, ECClientException, ECPacketParsingException, ECServerException {

        ECPacket epReq = new ECPacket();
        epReq.setOpCode(opCode);
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_PARTFILE, ECTagTypes.EC_TAGTYPE_HASH16, hash));
        } catch (DataFormatException e) {
           
            throw new ECClientException("Cannot create change download status request", e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_NOOP:
            // TODO Do something?
            return;
        default:
            throw new ECPacketParsingException("Unexpected response to change download status request", epResp.getRawPacket());        
        }
    }

    public void addED2KLink(String url) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_ADD_LINK);
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_PARTFILE_ED2K_LINK, ECTagTypes.EC_TAGTYPE_STRING, url));
        } catch (DataFormatException e) {
            throw new ECClientException("Invalid URI provided", e);
        }
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_NOOP:
            return;
        default:
            throw new ECPacketParsingException("Unexpected response to add link request", epResp.getRawPacket());        
        }
    }

    
    public void renamePartFile(byte[] hash, String newName) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_RENAME_FILE);
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_KNOWNFILE, ECTagTypes.EC_TAGTYPE_HASH16, hash));
            epReq.addTag(new ECTag(ECCodes.EC_TAG_PARTFILE_NAME, ECTagTypes.EC_TAGTYPE_STRING, newName));
        } catch (DataFormatException e) {
            throw new ECClientException("Error creating rename request", e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_NOOP:
            // TODO Do something?
            return;
        default:
            throw new ECPacketParsingException("Unexpected response to rename request", epResp.getRawPacket());        
        }        
    }
    
    
    public void setPartFilePriority(byte[] hash, byte prio) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_PARTFILE_PRIO_SET);
        try {
            ECTag t = new ECTag(ECCodes.EC_TAG_PARTFILE, ECTagTypes.EC_TAGTYPE_HASH16, hash);
            t.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_PRIO, ECTagTypes.EC_TAGTYPE_UINT8, prio));
            epReq.addTag(t);
            
        } catch (DataFormatException e) {
            throw new ECClientException("Cannot create set priorityrequest", e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_NOOP:
            // TODO Do something?
            return;
        default:
            throw new ECPacketParsingException("Unexpected response to set priority", epResp.getRawPacket());        
        }
    }
    

    
    public ECCategory[] getCategories(byte detailLevel) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_GET_PREFERENCES);
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_SELECT_PREFS, ECTagTypes.EC_TAGTYPE_UINT32, ECCodes.EC_PREFS_CATEGORIES));
            epReq.addTag(new ECTag(ECCodes.EC_TAG_DETAIL_LEVEL, ECTagTypes.EC_TAGTYPE_UINT8, detailLevel));
        } catch (DataFormatException e) {
            throw new ECClientException("Cannot create get categories request", e);
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
                if (t1.getTagName() != ECCodes.EC_TAG_CATEGORY) throw new ECPacketParsingException("Unexpected tag " + t1.getTagName() + " found while looking for EC_TAG_CATEGORY", epResp.getRawPacket());
                try {
                    categoryList[i] = new ECCategory(t1, detailLevel);
                } catch (ECTagParsingException e) {
                    throw new ECPacketParsingException("Error parsing response to get categories", epResp.getRawPacket(), e);
                }
            }
            return categoryList;
            
        default:
            throw new ECPacketParsingException("Unexpected response to get categories", epResp.getRawPacket());        
            
        }
    }
    
    
    public void createCategory(ECCategory c) throws IOException, ECPacketParsingException, ECServerException, ECClientException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_CREATE_CATEGORY);
        epReq.addTag(c.toECTag());

        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_NOOP:
            // TODO Do something?
            return;
        default:
            throw new ECPacketParsingException("Unexpected response to create category", epResp.getRawPacket());        
        }
    }
    
    public void updateCategory(ECCategory c) throws IOException, ECPacketParsingException, ECServerException, ECClientException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_UPDATE_CATEGORY);
        epReq.addTag(c.toECTag());
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_NOOP:
            // TODO Do something?
            return;
        default:
            throw new ECPacketParsingException("Unexpected response to update category", epResp.getRawPacket());        
        }
    }
    
    public void deleteCategory(long id) throws IOException, ECPacketParsingException, ECServerException, ECClientException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_DELETE_CATEGORY);
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_CATEGORY, ECTagTypes.EC_TAGTYPE_UINT32, id));
        } catch (DataFormatException e) {
            throw new ECClientException("Cannot craete delete category request", e);
        }
      
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_NOOP:
            // TODO Do something?
            return;
        default:
            throw new ECPacketParsingException("Unexpected response to delete category", epResp.getRawPacket());        
        }
    }
    
    
    
    
    
    
    
    
    

    @Override
    public String toString() {
        //return String.format("ECClient [clientName=%s, clientVersion=%s, socket=%s]", clientName, clientVersion, socket);
        return String.format("ECClient [socket=%s]", socket);
    }
}
