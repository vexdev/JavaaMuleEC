package com.iukonline.amule.ec;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;



public class ECPacket implements ECCodes, ECTagTypes {
    
    private final static int ECPACKET_HANDLED_FLAGS = 0x20 | EC_FLAG_ACCEPTS;
    
    private int transmissionFlags = 0x20 | EC_FLAG_ACCEPTS;
    private int accepts = 0x20;
    
    private byte opCode = EC_OP_NOOP;
    private ArrayList<ECTag> tags;
    
    private long len = -1;
    
    public ECPacket() {
        tags = new ArrayList<ECTag>();
    }
    
    public int getTransmissionFlags() {
        return transmissionFlags;
    }

    public void setTransmissionFlags(int transmissionFlags) {
        this.transmissionFlags = transmissionFlags;
    }

    public int getAccepts() {
        return accepts;
    }

    public void setAccepts(int accepts) {
        this.accepts = accepts;
    }
    
    public void addAccepts(int accepts) {
        this.accepts |= accepts;
    }

    public byte getOpCode() {
        return opCode;
    }
    
    public void setOpCode(byte opCode) {
        this.opCode = opCode;
    }
    
    public void addTag(ECTag tag) {
        tags.add(tag);
    }
    
    public ArrayList<ECTag> getTags() {
        return tags;
    }

    public boolean hasAccetps() {
        return (transmissionFlags & EC_FLAG_ACCEPTS) == EC_FLAG_ACCEPTS;
    }
    
    public boolean isUTF8Compressed() {
        return (transmissionFlags & EC_FLAG_UTF8_NUMBERS) == EC_FLAG_UTF8_NUMBERS;
    }
    
    public ECTag getTagByName(short tagName) {
        if (! tags.isEmpty()) {            
            Iterator<ECTag> itr = tags.iterator();
            while(itr.hasNext()) {
                ECTag tag = itr.next();
                if (tag.getTagName() == tagName) {
                    return tag;
                }
            }
        }
        return null;
    }
    

    
    
    public void writeToStream(OutputStream out) throws IOException {
        
        BufferedOutputStream bout = new BufferedOutputStream(out);
        
        //out.write(uintToBytes(transmissionFlags, 4, true));
        
        bout.write(ECUtils.uintToBytes(transmissionFlags, 4, true));
        if (this.hasAccetps()) {
            bout.write(ECUtils.uintToBytes(accepts, 4, true));
        }
        
        bout.write(ECUtils.uintToBytes(getLength(), 4, true));
        bout.write(opCode);        
		bout.write(ECUtils.uintToBytes(tags.size(), 2, true));
		
        if (! tags.isEmpty()) {            
            
            Iterator<ECTag> itr = tags.iterator();
            while(itr.hasNext()) {
                itr.next().writeToStream(bout);
            }
        }
        
        bout.flush();
    }
    
    private boolean parseTrasmissionFlags(int flags) {
        return (flags & ECPACKET_HANDLED_FLAGS) == flags;
    }
    
    public void readFromStream(InputStream in) throws IOException {
        boolean debug = false;
        
        byte bufUint[] = new byte[8];
        
        in.read(bufUint, 0, 4);
        int trFlags = (int) ECUtils.bytesToUint(bufUint, 4, true);
        if (! parseTrasmissionFlags(trFlags)) {
            // TODO Raise exception
        }
        setTransmissionFlags(trFlags);

        if (hasAccetps()) {
            in.read(bufUint, 0, 4);
            int accepts = (int) ECUtils.bytesToUint(bufUint, 4, true);
            // No need to parse accepts
            setAccepts(accepts);
        }
        
        ECUtils.readAllBytes(in, bufUint, 0, 4, false); // Len is never UTF-8 compressed
        //in.read(bufUint, 0, 4);
        len = ECUtils.bytesToUint(bufUint, 4, true, debug);
        if (debug) System.out.println("----- Packet Length: " + len);
        
        if (len < 3) {
            // TODO Gestire meglio
            throw new IOException("Invalid packet length " + len);
        }
        
        
        
        
        
        //setOpCode((byte) in.read());
        //in.read(bufUint, 0, 2);
        
        ECUtils.readAllBytes(in, bufUint, 0, 1);
        setOpCode((byte) bufUint[0]);
        ECUtils.readAllBytes(in, bufUint, 0, 2, isUTF8Compressed());
        len -= 3;
        
        int tagCount = (int) ECUtils.bytesToUint(bufUint, 2, true);
        
        if (debug) System.out.println("--- Packet contains " + tagCount + " tags");
        
        for (int i = 0; i < tagCount; i++) {
            
            if (debug) System.out.println("--- Fetching tag " + i);
            
            ECTag tag = new ECTag();
            try {
                tag.readFromStream(in, isUTF8Compressed());
            } catch (IOException e) {
                // Let's save what we read anyway
                addTag(tag);
                throw e;
            }
            addTag(tag);
            len -= tag.getLength(true);
            if (len < 0) {
                // TODO Gestire meglio
                // TODO Rimosso per problema lettura UTF-8
                // throw new IOException("Tags exceed packet length " + len);
            }
        }
        
        if (len != 0) {
            // TODO Gestire meglio
         // TODO Rimosso per problema lettura UTF-8
            // throw new IOException("After tag parsing " + len + " more bytes should be present");
        }
        
    }
    
    public long getLength() {
        long len = 3; // 1 opcode + 2 tag count;

        if (! tags.isEmpty()) {            
            Iterator<ECTag> itr = tags.iterator();
            while(itr.hasNext()) {
                len += itr.next().getLength(true);
            }
        }
        
        return len;
    }

    @Override
    public String toString() {
        StringBuffer out = new StringBuffer("Transmission flags: <" + Integer.toHexString(transmissionFlags) + ">\n");
        
        if (hasAccetps()) {
            out.append("Accepts: <" + Integer.toHexString(accepts) + ">\n");
        }
        out.append("Length: <" + this.getLength() + ">\n");
        if (len >= 0) {
            out.append("Length in packet: <" + len + ">\n");
        }
        out.append("OP Code: <" + Integer.toHexString(opCode) + ">\n");
        if (! tags.isEmpty()) {            
            Iterator<ECTag> itr = tags.iterator();
            while(itr.hasNext()) {
                out.append(itr.next().toString());
            }
        }
        
        return out.toString();

    }
    
    
}
