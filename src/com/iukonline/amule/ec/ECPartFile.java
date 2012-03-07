package com.iukonline.amule.ec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.zip.DataFormatException;



public class ECPartFile {
    
    public final static byte PS_READY                        = 0x0;
    public final static byte PS_EMPTY                        = 0x1;
    public final static byte PS_WAITINGFORHASH               = 0x2;
    public final static byte PS_HASHING                      = 0x3;
    public final static byte PS_ERROR                        = 0x4;
    public final static byte PS_INSUFFICIENT                 = 0x5;
    public final static byte PS_UNKNOWN                      = 0x6;
    public final static byte PS_PAUSED                       = 0x7;
    public final static byte PS_COMPLETING                   = 0x8;
    public final static byte PS_COMPLETE                     = 0x9;
    public final static byte PS_ALLOCATING                   = 0x10;
    
    public final static byte PR_LOW                          = 0x0;
    public final static byte PR_NORMAL                       = 0x1;
    public final static byte PR_HIGH                         = 0x2;
    public final static byte PR_AUTO                         = 0x5; 
    public final static byte PR_AUTO_LOW                     = 0xA;
    public final static byte PR_AUTO_NORMAL                  = 0xB;
    public final static byte PR_AUTO_HIGH                    = 0xC;
    
    public final static byte RATING_NOT_RATED                = 0x0;
    public final static byte RATING_INVALID                  = 0x1;
    public final static byte RATING_POOR                     = 0x2;
    public final static byte RATING_FAIR                     = 0x3;
    public final static byte RATING_GOOD                     = 0x4;
    public final static byte RATING_EXCELLENT                = 0x5;
    
    

    
    private ECClient client; 
    private boolean hasDetail;
    
    private byte[] hash;            // EC_TAG_PARTFILE                           = 0x0300;
    private String fileName;        // EC_TAG_PARTFILE_NAME                      = 0x0301;
    private String ed2kLink;        // EC_TAG_PARTFILE_ED2K_LINK                 = 0x030E;    
    private byte status;            // EC_TAG_PARTFILE_STATUS                    = 0x0308; 0 Waiting 1 Downloading .. 7 Paused
    private byte prio;              // EC_TAG_PARTFILE_PRIO                      = 0x0309; 0 Low 1 Normal
    private int sourceCount;        // EC_TAG_PARTFILE_SOURCE_COUNT              = 0x030A;

    private int sourceA4AF;         // EC_TAG_PARTFILE_SOURCE_COUNT_A4AF         = 0x030B;
    private int sourceXfer;         // EC_TAG_PARTFILE_SOURCE_COUNT_XFER         = 0x030D;
    private int sourceNotCurrent;   // EC_TAG_PARTFILE_SOURCE_COUNT_NOT_CURRENT  = 0x030C;

    private long sizeFull;          // EC_TAG_PARTFILE_SIZE_FULL                 = 0x0303; in bytes
    private long sizeDone;          // EC_TAG_PARTFILE_SIZE_DONE                 = 0x0306; in bytes
    private long speed;             // EC_TAG_PARTFILE_SPEED                     = 0x0307; in bytes/s
    
    private Date lastSeenComplete;  //EC_TAG_PARTFILE_LAST_SEEN_COMP            = 0x0311;;
    
    private int commentCount;
    private ArrayList<ECPartFileComment> comments;
    
    private ArrayList<ECPartFileSourceName> sourceNames;
    

    public ECPartFile() {
        
        comments = new ArrayList<ECPartFileComment>();
        sourceNames = new ArrayList<ECPartFileSourceName>();
        
        hasDetail = false;
    }
    
    public ECPartFile(ECTag t) throws DataFormatException {
        fillFromTag(t);
    }
    
    void fillFromTag(ECTag t) throws DataFormatException {
        setHash(t.getTagValueHash());
        setFileName(t.getSubTagByName(ECTag.EC_TAG_PARTFILE_NAME).getTagValueString());
        setEd2kLink(t.getSubTagByName(ECTag.EC_TAG_PARTFILE_ED2K_LINK).getTagValueString());
        setStatus((byte)t.getSubTagByName(ECTag.EC_TAG_PARTFILE_STATUS).getTagValueUInt());
        setPrio((byte) t.getSubTagByName(ECTag.EC_TAG_PARTFILE_PRIO).getTagValueUInt());
        setSpeed((int)t.getSubTagByName(ECTag.EC_TAG_PARTFILE_SPEED).getTagValueUInt());
        setSizeFull(t.getSubTagByName(ECTag.EC_TAG_PARTFILE_SIZE_FULL).getTagValueUInt());
        setSizeDone(t.getSubTagByName(ECTag.EC_TAG_PARTFILE_SIZE_DONE).getTagValueUInt());
        setSourceCount((int) t.getSubTagByName(ECTag.EC_TAG_PARTFILE_SOURCE_COUNT).getTagValueUInt());
        setSourceA4AF((int) t.getSubTagByName(ECTag.EC_TAG_PARTFILE_SOURCE_COUNT_A4AF).getTagValueUInt());
        setSourceXfer((int) t.getSubTagByName(ECTag.EC_TAG_PARTFILE_SOURCE_COUNT_XFER).getTagValueUInt());
        setSourceNotCurrent((int) t.getSubTagByName(ECTag.EC_TAG_PARTFILE_SOURCE_COUNT_NOT_CURRENT).getTagValueUInt());
        
        ECTag lsc = t.getSubTagByName(ECTag.EC_TAG_PARTFILE_LAST_SEEN_COMP);
        setLastSeenComplete(lsc == null ? null : lsc.getTagValueUInt());
        
        
        // TODO set has comments even when no detail are fatched... check returned value
        
        if (comments != null) {
            comments.clear();
        } else {
            comments = new ArrayList<ECPartFileComment>();
        }
        ECTag c = t.getSubTagByName(ECTag.EC_TAG_PARTFILE_COMMENTS);
        if (c == null) {
            setCommentCount(0);
        } else {
            if (c.getSubTags() != null && ! c.getSubTags().isEmpty()) {
                Iterator<ECTag> itr = c.getSubTags().iterator();
                while(itr.hasNext()) {
                    comments.add(new ECPartFileComment(itr));
                }
                setCommentCount(comments.size());
            } else {
                setCommentCount((int) c.getTagValueUInt());
            }
        }
        
        if (sourceNames != null) {
            sourceNames.clear();
        } else {
            sourceNames = new ArrayList<ECPartFileSourceName>();
        }
        ECTag s = t.getSubTagByName(ECTag.EC_TAG_PARTFILE_SOURCE_NAMES);
        if (s != null && s.getSubTags() != null && ! s.getSubTags().isEmpty()) {
            Iterator<ECTag> itr = s.getSubTags().iterator();
            while(itr.hasNext()) {
                sourceNames.add(new ECPartFileSourceName(itr));
            }
        }
        
        // Normalize content...
        if (getStatus() == PS_EMPTY && getSourceCount() > 0) setStatus(PS_READY);
        
    }
    
    public void setClient(ECClient client) {
        this.client = client;
    }
    
    public byte[] getHash() {
        return hash;
    }
    public void setHash(byte[] hash) {
        this.hash = hash;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getEd2kLink() {
        return ed2kLink;
    }
    public void setEd2kLink(String ed2kLink) {
        this.ed2kLink = ed2kLink;
    }
    public byte getStatus() {
        return status;
    }
    public void setStatus(byte status) {
        this.status = status;
    }
    
    public byte getPrio() {
        return prio;
    }

    public void setPrio(byte prio) {
        this.prio = prio;
    }

    public int getSourceCount() {
        return sourceCount;
    }
    public void setSourceCount(int sourceCount) {
        this.sourceCount = sourceCount;
    }
    public long getSizeFull() {
        return sizeFull;
    }
    public void setSizeFull(long sizeFull) {
        this.sizeFull = sizeFull;
    }
    public long getSizeDone() {
        return sizeDone;
    }
    public void setSizeDone(long sizeDone) {
        this.sizeDone = sizeDone;
    }
    public long getSpeed() {
        return speed;
    }
    public void setSpeed(long speed) {
        this.speed = speed;
    }

    public int getSourceA4AF() {
        return sourceA4AF;
    }

    public void setSourceA4AF(int sourceA4AF) {
        this.sourceA4AF = sourceA4AF;
    }

    public int getSourceXfer() {
        return sourceXfer;
    }

    public void setSourceXfer(int sourceXfer) {
        this.sourceXfer = sourceXfer;
    }

    public int getSourceNotCurrent() {
        return sourceNotCurrent;
    }

    public void setSourceNotCurrent(int sourceNotCurrent) {
        this.sourceNotCurrent = sourceNotCurrent;
    }
    
    public Date getLastSeenComplete() {
        return lastSeenComplete;
    }

    public void setLastSeenComplete(Date lastSeenComplete) {
        this.lastSeenComplete = lastSeenComplete;
    }
    
    public void setLastSeenComplete(long lastSeenComplete) {
        this.lastSeenComplete = new Date(lastSeenComplete);
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public ArrayList<ECPartFileComment> getComments() {
        return comments;
    }

    public ArrayList<ECPartFileSourceName> getSourceNames() {
        return sourceNames;
    }   
    
    public byte getWorstRating() {
        byte ret = -1;
        
        if (comments != null) {
            Iterator <ECPartFileComment> i = comments.iterator();
            while (i.hasNext()) {
                byte thisRating = i.next().rating;
                if (ret == -1 || ret > thisRating) ret = thisRating;
            }
        }
        
        return (ret == -1 ? RATING_NOT_RATED : ret);
    }
    
    
    
    
    
    
    
    
    
    
    
    


    public void pause() throws ECException, IOException {
        client.changeDownloadStatus(hash, ECPacket.EC_OP_PARTFILE_PAUSE);
    }
    
    public void resume() throws ECException, IOException {
        client.changeDownloadStatus(hash, ECPacket.EC_OP_PARTFILE_RESUME); 
    }    
    
    public void rename(String newName) throws ECException, IOException {
        client.renameDownload(hash, newName);
    }
    
    public void remove() throws ECException, IOException {
        client.changeDownloadStatus(hash, ECPacket.EC_OP_PARTFILE_DELETE);
    }
    
    public void swapA4AFThis() throws ECException, IOException {
        client.changeDownloadStatus(hash, ECPacket.EC_OP_PARTFILE_SWAP_A4AF_THIS);
    }
    
    public void swapA4AFThisAuto() throws ECException, IOException {
        client.changeDownloadStatus(hash, ECPacket.EC_OP_PARTFILE_SWAP_A4AF_THIS_AUTO);
    }
    
    public void swapA4AFOthers() throws ECException, IOException {
        client.changeDownloadStatus(hash, ECPacket.EC_OP_PARTFILE_SWAP_A4AF_OTHERS);
    }
    
    public void changePriority(byte prio) throws ECException, IOException {
        client.setDownloadPriority(hash, prio);
    }
    
    public void refresh() throws ECException, IOException {
        refresh(hasDetail);
    }
    
    public void refresh(boolean getDetail) throws ECException, IOException {
        if (getDetail) {
            client.getDownloadDetails(this);
        } else {
            client.getDownloadQueueItem(this);
        }
    }
    
    public void getDetails() throws ECException, IOException {
        refresh(true);
        hasDetail = true;
    }
    
    
    @Override
    public String toString() {
        return String.format(
                        "ECPartFile [\n\t" +
                        "client=%s,\n\t" +
                        "hash=%s,\n\t" +
                        "fileName=%s,\n\t" +
                        "ed2kLink=%s,\n\t" +
                        "status=%s,\n\t" +
                        "prio=%s,\n\t" +
                        "sourceCount=%s,\n\t" +
                        "sourceA4AF=%s,\n\t" +
                        "sourceXfer=%s,\n\t" +
                        "sourceNotCurrent=%s,\n\t" +
                        "sizeFull=%s,\n\t" +
                        "sizeDone=%s,\n\t" +
                        "speed=%s,\n\t" +
                        "lastSeenComplete=%s\n\t" + 
                        "comments=%s,\n\t" +
                        "sourceNames=%s\n" +
                        "]",
                        client, ECUtils.byteArrayToHexString(hash,16), fileName, ed2kLink, status, prio, sourceCount, sourceA4AF, sourceXfer, sourceNotCurrent, sizeFull, sizeDone, speed,
                        lastSeenComplete, comments, sourceNames);
    }


    public class ECPartFileComment {
        public String author;
        public String sourceName;
        public byte rating;
        public String comment;
        
        public ECPartFileComment(Iterator <ECTag> i) throws DataFormatException {
            ECTag t;
            if (i.hasNext()) {
                t = i.next();
                author = new String(t.getTagValueString());

                if (i.hasNext()) {
                    t = i.next();
                    sourceName = new String(t.getTagValueString());
                    
                    if (i.hasNext()) {
                        t = i.next();
                        rating = (byte) t.getTagValueUInt();
                        
                        if (i.hasNext()) {
                            t = i.next();
                            comment = new String(t.getTagValueString());
                            return;
                        }
                    }
                }
            }
            
            throw new DataFormatException("Error while reading comment tags");
        }

        @Override
        public String toString() {
            return String.format("ECPartFileComment [author=%s, sourceName=%s, rating=%s, comment=%s]", author, sourceName, rating, comment);
        }
        
    }
    
    public class ECPartFileSourceName {
        public String name;
        public int count;
        
        public ECPartFileSourceName(Iterator <ECTag> i) throws DataFormatException {
            ECTag t;
            if (i.hasNext()) {
                t = i.next();
                name = new String(t.getTagValueString());

                if (i.hasNext()) {
                    t = i.next();
                    count = (int) t.getTagValueUInt();
                    return;
                }
            }
            
            throw new DataFormatException("Error while reading source name tags");
        }

        @Override
        public String toString() {
            return String.format("ECPartFileSourceName [name=%s, count=%s]", name, count);
        }
        
    }
    
    public static class ECPartFileComparator implements Comparator<ECPartFile> {
        
        public enum ComparatorType {
            FILENAME, STATUS, TRANSFERED, PROGRESS
        }
        
        private ComparatorType compType;
        

        
        public ECPartFileComparator(ComparatorType compType) {
            this.compType = compType;
        }

        @Override
        public int compare(ECPartFile object1, ECPartFile object2) {
            switch (compType) {
            case STATUS:
                return object1.getStatus() - object2.getStatus();
            case TRANSFERED:
                return (int)(object1.getSizeDone() - object2.getSizeDone());
            case PROGRESS:
                float p1 = ((float) object1.getSizeDone()) * 100f / ((float) object1.getSizeFull());
                float p2 = ((float) object2.getSizeDone()) * 100f / ((float) object2.getSizeFull());
                return p2 > p1 ? 1 : (p2 < p1 ? -1 : 0);
            case FILENAME:
            default:
                return object1.getFileName().compareToIgnoreCase(object2.getFileName());
            }
        }
    }

    
/*

public final static short;
public final static short       
public final static short       EC_TAG_PARTFILE_PARTMETID                 = 0x0302;

public final static short       EC_TAG_PARTFILE_SIZE_XFER                 = 0x0304;
public final static short       EC_TAG_PARTFILE_SIZE_XFER_UP              = 0x0305;

public final static short       
public final static short       
public final static short       EC_TAG_PARTFILE_PRIO                      = 0x0309;
public final static short       
public final static short       EC_TAG_PARTFILE_SOURCE_COUNT_A4AF         = 0x030B;
public final static short       EC_TAG_PARTFILE_SOURCE_COUNT_NOT_CURRENT  = 0x030C;
public final static short       EC_TAG_PARTFILE_SOURCE_COUNT_XFER         = 0x030D;

public final static short       EC_TAG_PARTFILE_CAT                       = 0x030F;
public final static short       EC_TAG_PARTFILE_LAST_RECV                 = 0x0310;
public final static short       EC_TAG_PARTFILE_LAST_SEEN_COMP            = 0x0311;
public final static short       EC_TAG_PARTFILE_PART_STATUS               = 0x0312;
public final static short       EC_TAG_PARTFILE_GAP_STATUS                = 0x0313;
public final static short       EC_TAG_PARTFILE_REQ_STATUS                = 0x0314;
public final static short       EC_TAG_PARTFILE_SOURCE_NAMES              = 0x0315;
public final static short       EC_TAG_PARTFILE_COMMENTS                  = 0x0316;
 */
    
    
/*
 * Example with comments (rating excellent)
 * 
 * Received EC packet...
Transmission flags: <20>
Length: <2731>
Length in packet: <0>
OP Code: <1f>
        Tag Name: <300>
        Tag Type: <HASH16>
        Tag Length: <2719>
        Subtags (21)
                Tag Name: <308>
                Tag Type: <UINT8>
                Tag Length: <1>
                Tag Value: <0>
                Tag Name: <30a>
                Tag Type: <UINT8>
                Tag Length: <1>
                Tag Value: <36>
                Tag Name: <317>
                Tag Type: <UINT8>
                Tag Length: <1>
                Tag Value: <29>
                Tag Name: <30c>
                Tag Type: <UINT8>
                Tag Length: <1>
                Tag Value: <3>
                Tag Name: <30d>
                Tag Type: <UINT8>
                Tag Length: <1>
                Tag Value: <3>
                Tag Name: <30b>
                Tag Type: <UINT8>
                Tag Length: <1>
                Tag Value: <0>
                Tag Name: <304>
                Tag Type: <UINT32>
                Tag Length: <4>
                Tag Value: <602604328>
                Tag Name: <306>
                Tag Type: <UINT32>
                Tag Length: <4>
                Tag Value: <603105523>
                Tag Name: <307>
                Tag Type: <UINT32>
                Tag Length: <4>
                Tag Value: <135212>
                Tag Name: <309>
                Tag Type: <UINT8>
                Tag Length: <1>
                Tag Value: <1>
                Tag Name: <30f>
                Tag Type: <UINT8>
                Tag Length: <1>
                Tag Value: <0>
                Tag Name: <311>
                Tag Type: <UINT32>
                Tag Length: <4>
                Tag Value: <1325574627>
                Tag Name: <315>
                Tag Type: <UINT8>
                Tag Length: <926>
                Subtags (32)
                        Tag Name: <315>
                        Tag Type: <STRING>
                        Tag Length: <16>
                        Tag Value: <Ratatouille.mkv>
                        Tag Name: <315>
                        Tag Type: <UINT8>
                        Tag Length: <1>
                        Tag Value: <2>
                        Tag Name: <315>
                        Tag Type: <STRING>
                        Tag Length: <16>
                        Tag Value: <RATATOUILLE.mkv>
                        Tag Name: <315>
                        Tag Type: <UINT8>
                        Tag Length: <1>
                        Tag Value: <3>
                        Tag Name: <315>
                        Tag Type: <STRING>
                        Tag Length: <85>
                        Tag Value: <[BluRay - DivX - ITA] Walt Disney Pixar - Ratatouille - [720px264 - AC3-ITA.ENG].mkv>
                        Tag Name: <315>
                        Tag Type: <UINT8>
                        Tag Length: <1>
                        Tag Value: <8>
                        Tag Name: <315>
                        Tag Type: <STRING>
                        Tag Length: <20>
                        Tag Value: <ratatouille mkv.mkv>
                        Tag Name: <315>
                        Tag Type: <UINT8>
                        Tag Length: <1>
                        Tag Value: <2>
                        Tag Name: <315>
                        Tag Type: <STRING>
                        Tag Length: <46>
                        Tag Value: <RATATOUILLE - [ITALIANO HD 720p. BLU RAY].mkv>
                        Tag Name: <315>
                        Tag Type: <UINT8>
                        Tag Length: <1>
                        Tag Value: <1>
                        Tag Name: <315>
                        Tag Type: <STRING>
                        Tag Length: <54>
                        Tag Value: <[x264.-.AC3-ITA.ENG].RATATOUILLE.-.BluRay.-.720p .mkv>
                        Tag Name: <315>
                        Tag Type: <UINT8>
                        Tag Length: <1>
                        Tag Value: <4>
                        Tag Name: <315>
                        Tag Type: <STRING>
                        Tag Length: <53>
                        Tag Value: <RATATOUILLE.-.BluRay.- [x264.-.AC3-ITA.ENG] 720p.mkv>
                        Tag Name: <315>
                        Tag Type: <UINT8>
                        Tag Length: <1>
                        Tag Value: <1>
                        Tag Name: <315>
                        Tag Type: <STRING>
                        Tag Length: <77>
                        Tag Value: <Ratatouille (BLUERAY VERSION) (PIXAR - 2007 - Brad Bird and Jan Pinkava).mkv>
                        Tag Name: <315>
                        Tag Type: <UINT8>
                        Tag Length: <1>
                        Tag Value: <2>
                        Tag Name: <315>
                        Tag Type: <STRING>
                        Tag Length: <52>
                        Tag Value: <RATATOUILLE [x264.-.AC3-ITA.ENG-.BluRay.-.720p].mkv>
                        Tag Name: <315>
                        Tag Type: <UINT8>
                        Tag Length: <1>
                        Tag Value: <1>
                        Tag Name: <315>
                        Tag Type: <STRING>
                        Tag Length: <50>
                        Tag Value: <RATATOUILLE.BluRay.[x264.-.AC3-ITA.ENG].720p .mkv>
                        Tag Name: <315>
                        Tag Type: <UINT8>
                        Tag Length: <1>
                        Tag Value: <2>
                        Tag Name: <315>
                        Tag Type: <STRING>
                        Tag Length: <53>
                        Tag Value: <[x264.-.AC3-ITA.ENG].RATATOUILLE.-.BluRay.-.720p.mkv>
                        Tag Name: <315>
                        Tag Type: <UINT8>
                        Tag Length: <1>
                        Tag Value: <2>
                        Tag Name: <315>
                        Tag Type: <STRING>
                        Tag Length: <25>
                        Tag Value: <Ratatouille - BluRay.mkv>
                        Tag Name: <315>
                        Tag Type: <UINT8>
                        Tag Length: <1>
                        Tag Value: <1>
                        Tag Name: <315>
                        Tag Type: <STRING>
                        Tag Length: <34>
                        Tag Value: <Ratatouille (Blueray Version).mkv>
                        Tag Name: <315>
                        Tag Type: <UINT8>
                        Tag Length: <1>
                        Tag Value: <1>
                        Tag Name: <315>
                        Tag Type: <STRING>
                        Tag Length: <46>
                        Tag Value: <RATATOUILLE-BluRay-720p[x264-AC3-ITA.ENG].mkv>
                        Tag Name: <315>
                        Tag Type: <UINT8>
                        Tag Length: <1>
                        Tag Value: <1>
                        Tag Name: <315>
                        Tag Type: <STRING>
                        Tag Length: <15>
                        Tag Value: <Ratatuille.mkv>
                        Tag Name: <315>
                        Tag Type: <UINT8>
                        Tag Length: <1>
                        Tag Value: <1>
                        Tag Name: <315>
                        Tag Type: <STRING>
                        Tag Length: <43>
                        Tag Value: <Ratatouille - [720px264 - AC3-ITA.ENG].mkv>
                        Tag Name: <315>
                        Tag Type: <UINT8>
                        Tag Length: <1>
                        Tag Value: <1>
                Tag Value: <0>
                Tag Name: <316>
                Tag Type: <UINT8>
                Tag Length: <81>
                Subtags (4)
                        Tag Name: <316>
                        Tag Type: <STRING>
                        Tag Length: <7>
                        Tag Value: <SENSEI>
                        Tag Name: <316>
                        Tag Type: <STRING>
                        Tag Length: <34>
                        Tag Value: <Ratatouille (Blueray Version).mkv>
                        Tag Name: <316>
                        Tag Type: <UINT8>
                        Tag Length: <1>
                        Tag Value: <5>
                        Tag Name: <316>
                        Tag Type: <STRING>
                        Tag Length: <10>
                        Tag Value: <BY SENSEI>
                Tag Value: <0>
                Tag Name: <301>
                Tag Type: <STRING>
                Tag Length: <54>
                Tag Value: <[x264.-.AC3-ITA.ENG].RATATOUILLE.-.BluRay.-.720p .mkv>
                Tag Name: <302>
                Tag Type: <UINT8>
                Tag Length: <1>
                Tag Value: <7>
                Tag Name: <303>
                Tag Type: <UINT32>
                Tag Length: <4>
                Tag Value: <4037976272>
                Tag Name: <30e>
                Tag Type: <STRING>
                Tag Length: <115>
                Tag Value: <ed2k://|file|[x264.-.AC3-ITA.ENG].RATATOUILLE.-.BluRay.-.720p%20.mkv|4037976272|D292DC0B05584033100F5D195CFB5270|/>
                Tag Name: <312>
                Tag Type: <CUSTOM>
                Tag Length: <413>
                Tag Value: <18 16 16 03 17 16 16 06 17 16 16 04 17 18 17 16 16 02 18 17 16 17 16 16 04 17 16 16 03 17 16 17 16 18 17 16 16 02 17 16 17 17 02 16 16 04 17 16 18 16 16 02 17 17 03 16 18 18 02 16 18 16 16 03 18 17 18 16 16 04 17 16 16 04 18 17 16 17 17 02 16 16 02 17 16 16 05 18 16 16 03 18 16 16 05 18 16 16 03 17 17 02 16 16 02 17 17 02 16 17 17 03 16 18 18 02 16 17 18 17 16 17 17 04 16 17 16 16 02 17 17 02 16 16 02 17 16 16 02 18 18 02 16 17 16 17 16 17 17 03 16 16 06 18 16 16 04 17 17 02 16 16 02 17 17 02 16 16 02 17 16 17 16 16 02 17 16 17 18 17 16 16 02 17 16 17 17 02 16 16 05 17 16 18 16 18 17 17 02 16 16 06 17 18 16 16 02 17 18 18 02 17 16 17 18 18 02 17 16 16 04 17 17 02 16 16 05 17 16 16 02 18 17 16 18 17 18 16 16 07 17 16 16 06 17 16 16 06 17 16 16 02 17 16 16 02 17 16 18 18 02 17 16 17 16 16 02 18 16 16 02 17 16 16 04 17 16 16 05 17 17 02 16 17 16 16 02 18 16 17 16 16 02 17 16 16 02 18 18 02 16 17 18 16 18 17 16 17 16 16 08 18 17 16 16 02 17 16 18 16 16 04 17 17 03 16 17 17 05 16 17 18 17 16 16 05 17 16 16 02 17 16 16 02 17 16 16 04 17 16 16 05 17 17 02 16 16 02 17 16 17 16 17 17 02 16 16 05 17 17 02 16 17 17 02 16 16 06 17 17 02 16 16 05 17 17 02 16 16 02 18 16 16 02 18 18 02 17 17 02 16 16 03 17 17 02 16 18 16 17 18 18 02>
                Tag Name: <313>
                Tag Type: <CUSTOM>
                Tag Length: <790>
                Tag Value: <00 00 00 3C 00 00 0C 06 60 CF FF 00 00 04 06 F5 40 00 00 05 07 89 AF FF 00 00 04 08 1E 20 00 00 05 09 DB 6F FF 00 00 04 0A 6F E0 00 00 05 0C C1 9F FF 00 00 04 0D 56 10 00 00 05 10 3C 3F FF 00 00 04 11 65 20 00 00 05 13 22 6F FF 00 00 04 13 B6 E0 00 00 05 14 DF BF FF 00 00 04 15 74 30 00 00 05 16 08 9F FF 00 00 04 16 6C 0D EE 00 00 04 17 C5 EF FF 00 00 04 18 5A 60 00 00 05 18 EE CF FF 00 00 04 19 83 40 00 00 05 1A AC 1F FF 00 00 04 1B 40 90 00 00 05 1B D4 FF FF 02 00 00 04 1C FD E0 00 00 05 1E 26 BF FF 00 00 04 1F 4F A0 00 00 05 22 35 CF FF 00 00 04 22 CA 40 00 00 05 23 5E AF FF 00 00 04 23 F3 20 00 00 05 26 D9 4F FF 00 00 04 27 6D C0 00 00 05 2C 11 3F FF 00 00 04 2C A5 B0 00 00 05 2D 3A 1F FF 00 00 04 2D CE 90 00 00 05 32 72 0F FF 00 00 04 33 06 80 00 00 05 34 2F 5F FF 00 00 04 34 C3 D0 00 00 05 3C E1 EF FF 00 00 04 3D 76 60 00 00 05 40 5C 8F FF 00 00 04 40 F1 00 00 06 43 D7 2F FF 00 00 04 44 6B A0 00 00 05 4C 89 BF FF 00 00 04 4D 1E 30 00 00 05 4F 6F EF FF 00 00 04 50 04 60 00 00 05 53 7E FF FF 02 00 00 04 54 13 70 00 00 05 58 B6 EF FF 00 00 04 59 4B 60 00 00 05 62 92 5F FF 00 00 04 62 93 5E 38 00 00 04 63 26 CF FF 00 00 04 63 BB 40 00 00 05 66 0C FF FF 02 00 00 04 66 35 50 00 00 05 68 5E BF FF 00 00 04 69 87 A0 00 00 05 6A 1C 0F FF 00 00 04 6A B0 80 00 00 05 6C 6D CF FF 00 00 04 6C 6E CE 65 00 00 04 6D 96 AF FF 00 00 04 6E 2B 20 00 00 05 6E BF 8F FF 00 00 04 6F 54 00 00 06 71 A5 BF FF 00 00 04 72 3A 30 00 00 05 73 F7 7F FF 00 00 04 74 8B F0 00 00 05 75 B4 CF FF 00 00 04 76 49 40 00 00 05 79 2F 6F FF 00 00 04 79 B9 BE D5 00 00 04 81 E1 FF FF 02 00 00 04 82 76 70 00 00 05 8C 51 DF FF 00 00 04 8C E6 50 00 00 05 8E A3 9F FF 00 00 04 8F 38 10 00 00 05 91 89 CF FF 00 00 04 92 1E 40 00 00 05 97 EA 9F FF 00 00 04 99 13 80 00 00 05 9B 65 3F FF 00 00 04 9B F9 B0 00 00 05 A0 9D 2F FF 00 00 04 A0 9E 58 31 00 00 04 A4 AC 3F FF 00 00 04 A5 D5 20 00 00 05 B0 44 FF FF 02 00 00 04 B0 D9 70 00 00 05 BF 58 5F FF 00 00 04 BF EC D0 00 00 05 C2 D2 FF FF 02 00 00 04 C3 FB E0 00 00 05 CA F1 1F FF 00 00 04 CA F2 1E 17 00 00 04 CD D7 4F FF 00 00 04 CD D7 CE F6 00 00 04 D4 CC 8F FF 00 00 04 D6 89 E0 00 00 05 DA 98 EF FF 00 00 04 DB 2D 60 00 00 05 DE A7 FF FF 02 00 00 04 DF 3C 70 00 00 05 E4 74 5F FF 00 00 04 E5 08 D0 00 00 05 E7 5A 8F FF 00 00 04 E7 EF 00 00 06 E9 AC 4F FF 00 00 04 EA 40 C0 00 00 05 EC 92 7F FF 00 00 04 EC 93 29 85 00 00 04 F0 0D 1F FF>
                Tag Name: <314>
                Tag Type: <CUSTOM>
                Tag Length: <144>
                Tag Value: <00 00 00 00 79 B9 40 00 00 00 00 00 79 BC 0F FF 00 00 00 00 79 BC 10 00 00 00 00 00 79 BE DF FF 00 00 00 00 66 34 60 00 00 00 00 00 66 37 2F FF 00 00 00 00 66 37 30 00 00 00 00 00 66 39 FF FF 00 00 00 00 79 BE E0 00 00 00 00 00 79 C1 AF FF 00 00 00 00 16 6B 10 00 00 00 00 00 16 6D DF FF 00 00 00 00 66 3A 00 00 00 00 00 00 66 3C CF FF 00 00 00 00 16 6D E0 00 00 00 00 00 16 70 AF FF 00 00 00 00 16 70 B0 00 00 00 00 00 16 73 7F FF>
        Tag Value: <D2 92 DC 0B 05 58 40 33 10 0F 5D 19 5C FB 52 70>
 *     
 *     
 */

}
