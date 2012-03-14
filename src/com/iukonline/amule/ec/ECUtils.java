package com.iukonline.amule.ec;

import java.io.IOException;
import java.io.InputStream;

public class ECUtils {
    
    static byte[] uintToBytes(long uint, int numBytes, boolean MSB) {
        byte[] ret = new byte[numBytes];
        
        long mask = 0xFF;
        for (int i = 0; i < numBytes; i++) {
            ret[MSB ? numBytes - 1 - i : i] = (byte)((uint & (mask << i * 8)) >> (i * 8));
        }
        
        
        return ret;
    }
    
    static long bytesToUint(byte[] input, int numBytes, boolean MSB) {
        return bytesToUint(input, numBytes, MSB, false);
    }
    
    static long bytesToUint(byte[] input, int numBytes, boolean MSB, boolean debug) {
        
        if (debug)
            System.out.println("-- bytesToUint: " + byteArrayToHexString(input, numBytes));
        
        long ret = 0x0L;
        
         
                        
        for (int i = 0; i < numBytes; i++) {
            int index = MSB ? numBytes - 1 - i : i;
            if (debug) {
                System.out.println("Byte      : " + byteArrayToHexString(input, 1, index));
                System.out.println("Wrong Long: " + (long) input[index]);
                System.out.print("Bit       :");
            }
            
            if (debug) {
                for (int j = 7; j >= 0; j--) {
                    int bit = (input[index] >> j) & 0x1;
                    if (debug)
                        System.out.print(" " + bit);
                }
                System.out.print("\n");
            }
            
            for (int j = 7; j >= 0; j--) {
                long bit = (input[index] >> j) & 0x1;
                if (bit > 0) {
                    ret |= bit << (j + i * 8);
                }
            }
            
            if (debug)
                System.out.println("Partial result: " + ret);
        }
        if (debug)
            System.out.println("Final result: " + ret);
        
        return ret;
    }
    
    static String byteArrayToHexString(byte in) {
        return byteArrayToHexString(new byte[] { in });
        
    }
    
    public static String byteArrayToHexString(byte in[]) {
        if (in == null) {
            return null;
        } else {
            return byteArrayToHexString(in, in.length, 0);
        }
    }
    
    public static String byteArrayToHexString(byte in[], int max) {
        return byteArrayToHexString(in, max, 0);
    }
    
    static String byteArrayToHexString(byte in[], int max, int offset) {

        byte ch = 0x00;
        int i = offset; 

        if (in == null || in.length <= 0)
            return null;

        String pseudo[] = {"0", "1", "2","3", "4", "5", "6", "7", "8","9", "A", "B", "C", "D", "E","F"};
        StringBuffer out = new StringBuffer(in.length * 3 - 1);
        while (i < max + offset) {
            if (i > offset)
                out.append(' ');
            ch = (byte) (in[i] & 0xF0); // Strip off high nibble
            ch = (byte) (ch >>> 4);     // shift the bits down
            ch = (byte) (ch & 0x0F);    // must do this as high order bit is on!
            out.append(pseudo[ (int) ch]); // convert the nibble to a String Character

            ch = (byte) (in[i] & 0x0F); // Strip off low nibble 
            out.append(pseudo[ (int) ch]); // convert the nibble to a String Character
            i++;
        }

        String rslt = new String(out);
        return rslt;
    }    
    
    public static void readAllBytes(InputStream in, byte[] buf, int offset, int len) throws IOException {
        int remaining = (int) len;
        int pos = offset;
        int bytes = 0;
        
        while (remaining > 0) {
            bytes = in.read(buf, pos, remaining);
            if (bytes < 0) {
                // TODO: gestire meglio
                throw new IOException("0 bytes read");
            } else {
                remaining -= bytes;
                pos += bytes;
                //System.out.println("Read "+pos+"/"+len+" bytes");
            }
        }
    }
    
    public static void readAllBytes(InputStream in, byte[] buf, int offset, int len, boolean isUTF8Compressed) throws IOException {
        
        boolean debug = false;
        
        if (! isUTF8Compressed) {
            readAllBytes(in, buf, offset, len);
            return;
        }

        byte[] localBuf = new byte[8];
        int bytes = 0;
        bytes = in.read(localBuf, 0, 1);
        if (bytes <= 0)  throw new IOException("0 bytes read"); // TODO: gestire meglio
        
        
        if (debug) System.out.println("Decoding UTF-8 int...");
        if (debug) System.out.println("First byte is " + byteArrayToHexString(localBuf, 1));
        int bitShift = 7;
        while (bitShift > 0 && (((int) (localBuf[0] >> bitShift)) & 0x1) == 0x01) {
            if (debug) System.out.println("Bit " + bitShift + " is set");
            bitShift --;
        }
        if (bitShift == 0 || bitShift == 6) throw new IOException("Invalid UTF-8 sequence");
        
        int bytesToBeRead = (bitShift == 7 ? 0 : 6 - bitShift);
        if (debug) System.out.println("Sequence is " + (bytesToBeRead + 1) + " bytes long");
        
        int encodedBitLen = (bytesToBeRead + 1) * 8; 
        int decodedBitLen = bytesToBeRead * 6 + bitShift;
        int decodedByteLen = bitShift % 8 == 0 ? bitShift / 8 : (bitShift / 8) + 1;
        
        if (debug) System.out.println("Decoded sequence is " + decodedByteLen + " bytes long (" + decodedBitLen + " bits)");
        
        if (decodedByteLen > len) throw new IOException("UTF-8 Sequence is longer than expected");

        if (bytesToBeRead > 0) {
            if (debug) System.out.println("Reading following bytes...");
            readAllBytes(in, localBuf, 1, bytesToBeRead);
        }

        if (debug) System.out.println("Sequence is " + byteArrayToHexString(localBuf, 1 + bytesToBeRead));
        
        //TODO Check that following bytes start with 10

        int pos = len - 1;
        int bitLow = encodedBitLen - 1;
        
        while (bitLow > 0 && pos >= 0) {
            int bitHigh = (bitLow > 10 ? bitLow - 9 : bytesToBeRead + 1);
            int result = 0x0;
            
            if (debug) System.out.println("Reading bits between " + bitHigh + " and " + bitLow);
            
            int j = 0;
            for (int i = bitLow; i >= bitHigh; i--) {
                
                if (i < 8 || i % 8 > 1) {
                
                    int targetByte = i / 8;
                    int targetBit = i % 8;
                    
                    if (debug) System.out.print("Reading byte " + targetByte + " bit " + targetBit + ": " + (0x1 << j) + " x ");
                    
                    int bit = (((int) localBuf[targetByte]) >> (7 - targetBit)) & 0x1;
                    
                    if (debug) System.out.println(bit);
                    result |= bit << j++; 
                }
                
            }
            buf[pos--] = (byte) result;
            bitLow -= 10;
        }
        
        for (int i = 0; i <= pos; i++ ) {
            buf[i] = (byte) 0x0;
        }
        
        if (debug) System.out.println("FINAL DECODED SEQUENCE: " + ECUtils.byteArrayToHexString(buf, len));
        
    }
}
