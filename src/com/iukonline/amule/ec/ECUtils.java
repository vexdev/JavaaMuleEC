package com.iukonline.amule.ec;

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
    
    static String byteArrayToHexString(byte in[], int max) {
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
}
