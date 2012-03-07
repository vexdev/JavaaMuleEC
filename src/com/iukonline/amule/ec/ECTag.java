package com.iukonline.amule.ec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.DataFormatException;



public class ECTag implements ECCodes, ECTagTypes {
    
    private short       tagName; 
    private byte        tagType = EC_TAGTYPE_UNKNOWN;
    
    private int         nestingLevel = 0;
    
    private String      tagValueString;
    private long        tagValueUInt;
    private InetAddress tagValueIPv4;  
    private byte[]      tagValueHash;   
    private byte[]      tagValueCustom;
    
    ArrayList<ECTag>    subTags;        // Note: this is not synchronized... Take care if used in multiple thread
        
    public ECTag() {
        subTags = new ArrayList<ECTag>();
    }
    
    public ECTag(short tagName, byte tagType) throws DataFormatException {
        this();
        this.setTagName(tagName);
        this.setTagType(tagType);
    }
    
    public ECTag(short tagName, byte tagType, String tagValue) throws DataFormatException {
        this(tagName, tagType);
        this.setTagValueString(tagValue);
    }
    
    public ECTag(short tagName, byte tagType, long tagValue) throws DataFormatException {
        this(tagName, tagType);
        this.setTagValueUInt(tagValue);
    }
    
    public ECTag(short tagName, String tagValue) throws DataFormatException {
        this(tagName, EC_TAGTYPE_STRING, tagValue);
    }

    public ECTag(short tagName, byte tagType, InetAddress tagValue) throws DataFormatException {
        this(tagName, tagType);
        this.setTagValueIPv4(tagValue);
    }

    public ECTag(short tagName, InetAddress tagValue) throws DataFormatException {
        this(tagName, EC_TAGTYPE_IPV4, tagValue);
    }

    public ECTag(short tagName, byte tagType, byte[] tagValue) throws DataFormatException {
        this(tagName, tagType);
        switch (tagType) {
        case EC_TAGTYPE_HASH16:
            setTagValueHash(tagValue);
            break;
        case EC_TAGTYPE_CUSTOM:
            setTagValueCustom(tagValue);
            break;
        default:
            // TODO Raise Exception
            
        }
            
    }
    
    public String getTagValueString() throws DataFormatException {
        if (this.tagType == EC_TAGTYPE_STRING) {
            return tagValueString;
        } else {
            throw new DataFormatException("Tag type is not STRING");
        }        
    }

    public void setTagValueString(String tagValueString) throws DataFormatException {
        if (this.tagType == EC_TAGTYPE_STRING) {
            this.tagValueString = tagValueString;
        } else {
            throw new DataFormatException("Tag type is not STRING");
        }
    }

    public long getTagValueUInt() throws DataFormatException {
        switch (tagType) {
        case EC_TAGTYPE_UINT8:
        case EC_TAGTYPE_UINT16:
        case EC_TAGTYPE_UINT32:
        case EC_TAGTYPE_UINT64:
            return tagValueUInt;
        default:
            throw new DataFormatException("Tag type is not UINT*");
        }
                
    }

    public void setTagValueUInt(long tagValueUInt) throws DataFormatException {
        switch (tagType) {
        case EC_TAGTYPE_UINT8:
        case EC_TAGTYPE_UINT16:
        case EC_TAGTYPE_UINT32:
        case EC_TAGTYPE_UINT64:
            this.tagValueUInt = tagValueUInt;
            break;
        default:
            throw new DataFormatException("Tag type is not UINT*");
        }

    }
    
    public InetAddress getTagValueIPv4() throws DataFormatException {
        if (tagType == EC_TAGTYPE_IPV4) {
            return tagValueIPv4;
        } else {
            throw new DataFormatException("Tag type is not IPV4");
        }
    }

    public void setTagValueIPv4(InetAddress tagValueIPv4) throws DataFormatException {
        if (tagType == EC_TAGTYPE_IPV4) {
            this.tagValueIPv4 = tagValueIPv4;
        } else {
            throw new DataFormatException("Tag type is not IPV4");
        }
    }

    public byte[] getTagValueHash() throws DataFormatException {
        if (tagType == EC_TAGTYPE_HASH16) {
            return tagValueHash;
        } else {
            throw new DataFormatException("Tag type is not HASH16");
        }
    }

    public void setTagValueHash(byte[] tagValueHash) throws DataFormatException {
        if (tagType == EC_TAGTYPE_HASH16) {
            this.tagValueHash = tagValueHash;
        } else {
            throw new DataFormatException("Tag type is not HASH16");
        }
    }

    public byte[] getTagValueCustom() throws DataFormatException {
        if (tagType == EC_TAGTYPE_CUSTOM) {
            return tagValueCustom;
        } else {
            throw new DataFormatException("Tag type is not CUSTOM");
        }

    }

    public void setTagValueCustom(byte[] tagValueCustom) throws DataFormatException {
        if (tagType == EC_TAGTYPE_CUSTOM) {
            this.tagValueCustom = tagValueCustom;
        } else {
            throw new DataFormatException("Tag type is not CUSTOM");
        }

    }
    
    
    public int getTagName() {
        return tagName;
    }

    public void setTagName(short tagName) {
        this.tagName = tagName;
    }

    public byte getTagType() {
        return tagType;
    }

    public void setTagType(byte tagType) throws DataFormatException {
        switch (tagType) {
        case EC_TAGTYPE_CUSTOM:
        case EC_TAGTYPE_UINT8:
        case EC_TAGTYPE_UINT16:
        case EC_TAGTYPE_UINT32:
        case EC_TAGTYPE_UINT64:
        case EC_TAGTYPE_STRING:
        case EC_TAGTYPE_IPV4:
        case EC_TAGTYPE_HASH16:
            this.tagType = tagType;
            break;
        case EC_TAGTYPE_DOUBLE:
            throw new DataFormatException("Don't know how to handle tag type DOUBLE");
        default:
            throw new DataFormatException("Unknown tag type " + ECUtils.byteArrayToHexString(tagType));
        }
    }
    
    
    
    
    
    
    
    
    public ArrayList<ECTag> getSubTags() {
        return subTags;
    }

    public void addSubTag(ECTag subTag) {
        subTag.setNestingLevel(nestingLevel + 1);
        this.subTags.add(subTag);
    }
    
    private void setNestingLevel(int nestingLevel) {
        this.nestingLevel = nestingLevel;
        if (! subTags.isEmpty()) {
            Iterator<ECTag> itr = subTags.iterator();
            while(itr.hasNext()) {
                itr.next().setNestingLevel(nestingLevel + 1);
            }
        }
    }
    
    public ECTag getSubTagByName(short tagName) {
        if (! subTags.isEmpty()) {            
            Iterator<ECTag> itr = subTags.iterator();
            while(itr.hasNext()) {
                ECTag tag = itr.next();
                if (tag.getTagName() == tagName) {
                    return tag;
                }
            }
        }
        return null;
    }
    
    public long getLength(boolean withHeader) {
        long len = withHeader ? 7 : 0; // ec_tagname_t - uint16, ec_tagtype_t - uint8, ec_tagtype_t - uint32
        
        switch (tagType) {
        case EC_TAGTYPE_CUSTOM:
            len += tagValueCustom.length;
            break;
        case EC_TAGTYPE_UINT8:
            len += 1;
            break;
        case EC_TAGTYPE_UINT16:
            len += 2;
            break;
        case EC_TAGTYPE_UINT32:
            len += 4;
            break;
        case EC_TAGTYPE_UINT64:
            len += 8;
            break;
        case EC_TAGTYPE_STRING:
            try {
                len += tagValueString.getBytes("UTF-8").length + 1;
            } catch (UnsupportedEncodingException e) {
                // THIS SHOULD NEVERE HAPPEN
            }
            break;
        case EC_TAGTYPE_DOUBLE:
            // TODO Implement this. Today won't happen as DOUBLE trhows execption in set method
            break;
        case EC_TAGTYPE_IPV4:
            len += 4; 
            break;
        case EC_TAGTYPE_HASH16:
            len += 16;
            break;
        default:
            // Don't need an exception. tagType has been checked in set method
        }
        
        if (! subTags.isEmpty()) {
            if (withHeader) len += 2; // Tag count

            Iterator<ECTag> itr = subTags.iterator();
            while(itr.hasNext()) {
                len += itr.next().getLength(true);
            }
        }
        
        return len;
    }
    
    public long getLength() {
        return getLength(false);
    }
      
    public void writeToStream(OutputStream out) throws IOException {
        
        out.write(ECUtils.uintToBytes(subTags.isEmpty() ? tagName << 1 : ((tagName << 1)| 0x1), 2, true)); // Last bit set to 1 if subtags are present
        out.write(tagType);
        out.write(ECUtils.uintToBytes(getLength(), 4, true));

        if (! subTags.isEmpty()) {
            out.write(ECUtils.uintToBytes(subTags.size(), 2, true));
            
            Iterator<ECTag> itr = subTags.iterator();
            while(itr.hasNext()) {
                itr.next().writeToStream(out);
            }
        }

        switch (tagType) {
        case EC_TAGTYPE_UINT8:
            out.write(ECUtils.uintToBytes(tagValueUInt, 1, true));
            break;
        case EC_TAGTYPE_UINT16:
            out.write(ECUtils.uintToBytes(tagValueUInt, 2, true));
            break;
        case EC_TAGTYPE_UINT32:
            out.write(ECUtils.uintToBytes(tagValueUInt, 4, true));
            break;
        case EC_TAGTYPE_UINT64:
            out.write(ECUtils.uintToBytes(tagValueUInt, 8, true));
            break;
        case EC_TAGTYPE_STRING:
            out.write(tagValueString.getBytes("UTF-8"));
            out.write((byte) 0x00);
            break;
        case EC_TAGTYPE_DOUBLE:            
            // TODO Implement this. Today won't happen as DOUBLE trhows execption in set method
            break;
        case EC_TAGTYPE_IPV4:
            out.write(tagValueIPv4.getAddress());
            break;
        case EC_TAGTYPE_HASH16:
            out.write(tagValueHash);
            break;
        default:
            // Don't need an exception. tagType has been checked in set method
        }

    }
    
    public void readAllBytes(InputStream in, byte[] buf, int offset, int len) throws IOException {
        
        int remaining = (int) len;
        int pos = 0;
        int bytes = 0;
        
        while (remaining > 0) {
            bytes = in.read(buf, pos, remaining);
            if (bytes < 0) {
                throw new IOException("0 bytes read");
            } else {
                remaining -= bytes;
                pos += bytes;
                //System.out.println("Read "+pos+"/"+len+" bytes");
            }
        }
    }
    
    public void readFromStream(InputStream in) throws IOException {
        
        boolean debug = false;  // TODO remove debug...
        
        byte bufUint[] = new byte[8];
        
        int bytes = -1;

        this.readAllBytes(in, bufUint, 0, 2);
        //bytes = in.readAll(bufUint, 0, 2);

        long tagName = ECUtils.bytesToUint(bufUint, 2, true);
        if (debug) System.out.println("Found raw tag " + Long.toHexString(tagName));
        boolean hasSubTags = (tagName & 0x1L) == 0x1L ? true : false;
        tagName = (tagName & 0xfffffffffffffffeL) >> 1;
        if (debug) System.out.println("Found tag " + Long.toHexString(tagName));
        setTagName((short) tagName);
        
        try {
            bytes = in.read();
            if (bytes < 0) throw new IOException("Not all bytes were read. Expecting 1 read 0");
            setTagType((byte) bytes);
        } catch (DataFormatException e) {
            throw new IOException("Error reading tag type - " + e.getMessage());
        }
        
        if (debug) System.out.println("Found tag type " + ECUtils.byteArrayToHexString( new byte[] { tagType } ));
        
        //bytes = in.readAll(bufUint, 0, 4);
        //if (bytes < 4) throw new IOException("Not all bytes were read. Expecting 4 read " + bytes);
        this.readAllBytes(in, bufUint, 0,4);
        
        long len = ECUtils.bytesToUint(bufUint, 4, true, debug);
        long originalLength = len;
        if (debug) System.out.println("----- Tag Length: " + len);
        
        if (hasSubTags) {
            
            if (debug) System.out.println("---------------------------------------- HAS SUBTAGS!!!! ------------");
            
            //bytes = in.readAll(bufUint, 0, 2);
            //if (bytes < 2) throw new IOException("Not all bytes were read. Expecting 2 read " + bytes);
            this.readAllBytes(in, bufUint, 0, 2);
            int subTagCount = (int) ECUtils.bytesToUint(bufUint, 2, true);
            
            
            for (int i = 0; i < subTagCount; i++) {
                ECTag subTag = new ECTag();
                subTag.readFromStream(in);
                len -= subTag.getLength(true);
                addSubTag(subTag);
                if (debug) System.out.println("----- Remaining Tag Length: " + len);
            }

        }
        
        if (debug) System.out.println("----- Reading " + len + " bytes...");
        
        byte[] buf = new byte[(int) len];
        
        
        if (len > 0) {
            //bytes = in.readAll(buf, 0, (int) len);
            //if (bytes < len) throw new IOException("Not all bytes were read Expecting " + len + " read " + bytes);
            this.readAllBytes(in, buf, 0, (int)len);
        }
        
        
        
        /*int remaining = (int) len;
        int pos = 0;
        
        while (remaining > 0) {
            bytes = in.read(buf, pos, remaining);
            if (bytes < 0) {
                throw new IOException("0 bytes read");
            } else {
                remaining -= bytes;
                pos += bytes;
            }
        }*/
        
        
        
        try {
            switch (tagType) {
            case EC_TAGTYPE_CUSTOM:
                if (debug) System.out.println("TYPE: CUSTOM");
                setTagValueCustom(buf);
                break;
            case EC_TAGTYPE_UINT8:
                if (debug) System.out.println("TYPE: UINT8");
                if (len == 0)
                    this.setTagValueUInt(0);                    
                else if (len != 1)
                    throw new IOException("Wrong length for UINT8 tag (" + len + ")");
                else
                    this.setTagValueUInt(ECUtils.bytesToUint(buf, (int) len, true));            
                break;
            case EC_TAGTYPE_UINT16:
                if (debug) System.out.println("TYPE: UINT16");
                if (len != 2)
                    throw new IOException("Wrong length for UINT8 tag (" + len + ")");
                else
                    this.setTagValueUInt(ECUtils.bytesToUint(buf, (int) len, true));            
                break;
            case EC_TAGTYPE_UINT32:
                if (debug) System.out.println("TYPE: UINT32");
                if (len != 4)
                    throw new IOException("Wrong length for UINT8 tag (" + len + ")");
                else
                    this.setTagValueUInt(ECUtils.bytesToUint(buf, (int) len, true));            
                break;
            case EC_TAGTYPE_UINT64:
                if (debug) System.out.println("TYPE: UINT64");
                if (len != 8)
                    throw new IOException("Wrong length for UINT8 tag (" + len + ")");
                else
                    this.setTagValueUInt(ECUtils.bytesToUint(buf, (int) len, true));            
                break;
            case EC_TAGTYPE_STRING:
                if (debug) System.out.println("TYPE: STRING - LENGTH " + Long.toString(len));
                if (debug) System.out.println(buf);
                if (debug) System.out.println(new String(buf, 0, (int) len - 1, "UTF-8"));
                
                if (buf[buf.length - 1] != 0x00)
                    throw new IOException("String is not terminated with 00... Wrong length?");
                else
                    this.setTagValueString(new String(buf, 0, (int) len - 1, "UTF-8"));
                
                if (debug) System.out.println(this.getTagValueString());
                if (debug) System.out.println("LEN IN PACKET: " + len);
                if (debug) System.out.println("LEN IN STRING: " + this.getTagValueString().getBytes("UTF-8").length);
                
                break;
                
            case EC_TAGTYPE_DOUBLE:
                if (debug) System.out.println("TYPE: DOUBLE");
                // TODO Handle doubles
                throw new IOException("HELP: NON SO GESTIRE I DOUBLE!!!");
                //break;
            case EC_TAGTYPE_IPV4:
                if (debug) System.out.println("TYPE: IPV4");
                if (len != 4)
                    throw new IOException("Wrong length for IPV4 (" + len + ")");
                else
                    this.setTagValueIPv4(InetAddress.getByAddress(buf));
                break;
            case EC_TAGTYPE_HASH16:
                if (debug) System.out.println("TYPE: HASH16");
                if (len != 16)
                    throw new IOException("Wrong length for HASH16 (" + len + ")");
                else
                    setTagValueHash(buf);
                break;
            default:
                // TODO Raise exception
            }
        } catch (IOException e) {
            throw e;
        } catch (DataFormatException e) {
            throw new IOException("Severe error. This shouldn't happen here - " + e.getMessage());
        }
        
        if (debug) System.out.println("Final tag:\n" + toString());
        if (originalLength != this.getLength()) 
            throw new IOException(
               "Tag len on packet (" + originalLength + ") is not the same as calculated one ( " + this.getLength() + ")");
        

    }

    @Override
    public String toString() {
        
        StringBuffer sbIndent = new StringBuffer("        ");
        for (int i = 0; i < nestingLevel; i++) {
            sbIndent.append("        ");
        }
        
        String indent = sbIndent.toString();
        
        StringBuffer out = new StringBuffer(indent);
        out.append("Tag Name: <" + Integer.toHexString(tagName) + ">\n");

        String type;
        String value;

        switch (tagType) {
        case EC_TAGTYPE_CUSTOM:
            type = "CUSTOM";
            value = ECUtils.byteArrayToHexString(tagValueCustom);
            break;
        case EC_TAGTYPE_UINT8:
            type = "UINT8";
            value = Long.toString(tagValueUInt);
            break;
        case EC_TAGTYPE_UINT16:
            type = "UINT16";
            value = Long.toString(tagValueUInt);
            break;
        case EC_TAGTYPE_UINT32:
            type = "UINT32";
            value = Long.toString(tagValueUInt);
            break;
        case EC_TAGTYPE_UINT64:
            type = "UINT64";
            value = Long.toString(tagValueUInt);
            break;
        case EC_TAGTYPE_STRING:
            type = "STRING";
            value = tagValueString;
            break;
        case EC_TAGTYPE_DOUBLE:
            // TODO
            type = "DOUBLE";
            value = "DONT KNOW HOW TO DECODE";
            break;
        case EC_TAGTYPE_IPV4:
            type = "IPV4";
            value = tagValueIPv4.toString();
            break;
        case EC_TAGTYPE_HASH16:
            type = "HASH16";
            value = ECUtils.byteArrayToHexString(tagValueHash);
            break;
        default:
            type = "UNKNOWN TYPE (" + Integer.toHexString(tagType) + ")";
            value = "";

        }
                
        out.append(indent + "Tag Type: <" + type + ">\n");
        out.append(indent + "Tag Length: <" + this.getLength() + ">\n");
        
        
        if (! this.subTags.isEmpty()) {            
            out.append(indent + "Subtags (" + subTags.size() + ")\n");
            Iterator<ECTag> itr = subTags.iterator();
            while(itr.hasNext()) {
                out.append(itr.next().toString());
            }
        }
        
        out.append(indent + "Tag Value: <" + value + ">\n");
        return out.toString();
    }    
    
}
