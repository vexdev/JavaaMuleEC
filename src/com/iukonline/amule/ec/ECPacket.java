package com.iukonline.amule.ec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;



public class ECPacket {
    
    
    private byte opCode = ECCodes.EC_OP_NOOP;
    private ArrayList<ECTag> tags;
    
    
    
    // Default values;
    private boolean isUTF8Compressed = false;
    private boolean isZlibCompressed = false;
    private boolean hasId = false;
    private boolean acceptsUTF8 = true;
    private boolean acceptsZlib = true;
    
    
    private ECRawPacket encodedPacket;
    
    public ECPacket() {
        tags = new ArrayList<ECTag>();
    }
    
    public void setUTF8Compressed(boolean isUTF8Compressed) { this.isUTF8Compressed = isUTF8Compressed; }
    public boolean isUTF8Compressed() { return isUTF8Compressed; }
   
    public boolean isZlibCompressed() { return isZlibCompressed; }
    public void setZlibCompressed(boolean isZlibCompressed) { this.isZlibCompressed = isZlibCompressed; }

    public boolean hasId() { return hasId; }
    public void setHasId(boolean hasId) { this.hasId = hasId; }

    public boolean acceptsUTF8() { return acceptsUTF8; }
    public void setAcceptsUTF8(boolean acceptsUTF8) { this.acceptsUTF8 = acceptsUTF8; }

    public boolean acceptsZlib() { return acceptsZlib; }
    public void setAcceptsZlib(boolean acceptsZlib) { this.acceptsZlib = acceptsZlib; }

    public byte getOpCode() { return opCode; }
    public void setOpCode(byte opCode) { this.opCode = opCode; }
    
    public void addTag(ECTag tag) { tags.add(tag); }
    
    public ArrayList<ECTag> getTags() { return tags; }


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
    

    public void writeToStream(OutputStream out) throws IOException, ECException  {
        encodedPacket = new ECRawPacket(this);
        out.write(encodedPacket.asByteArray());
    }
    
    public static ECPacket readFromStream(InputStream in) throws IOException, ECException {
        ECRawPacket raw = new ECRawPacket(in);
        ECPacket n = raw.parse();
        n.encodedPacket = raw;
        return n;
    }
    
    
    
    @Override
    public String toString() {


        
        StringBuilder out = new StringBuilder();
        Formatter f = new Formatter(out);
        
        f.format("isUTF8Compressed: %s, isZlibCompressed: %s, hasId: %s, acceptsUTF8: %s, acceptsZlib: %s\n", isUTF8Compressed, isZlibCompressed, hasId, acceptsUTF8, acceptsZlib); 
        out.append("OP Code: <" + Integer.toHexString(opCode) + ">\n");
        
        if (! tags.isEmpty()) {           
            Iterator<ECTag> itr = tags.iterator();
            while(itr.hasNext()) {
                    out.append(itr.next().toString());
            }
        }
        
        if (encodedPacket != null) {
            out.append("Encoded packet\n");
            out.append(encodedPacket.dump());
        }
        
        return out.toString();
    }
    
}
