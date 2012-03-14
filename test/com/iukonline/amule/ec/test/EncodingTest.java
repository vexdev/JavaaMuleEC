package com.iukonline.amule.ec.test;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;

import com.iukonline.amule.ec.ECUtils;

public class EncodingTest {

    @Test
    public void test() throws IOException {
        byte inBuf[] = new byte[2];

        inBuf[0] = (byte) (
                        1 << 7 |
                        1 << 6 |
                        0 << 5 |
                        1 << 4 |
                        0 << 3 |
                        1 << 2 |
                        1 << 1 |
                        1 << 0
                        );
        
        inBuf[1] = (byte) (
                        1 << 7 |
                        0 << 6 |
                        0 << 5 |
                        1 << 4 |
                        0 << 3 |
                        0 << 2 |
                        0 << 1 |
                        0 << 1
                        );
        
    
        
                        
                        
        
        
        byte[] outBuf = new byte[4];
        
        ByteArrayInputStream bs = new ByteArrayInputStream(inBuf);
        ECUtils.readAllBytes(bs, outBuf, 0, 4, true);
        
        System.out.println("Input sequence: " + ECUtils.byteArrayToHexString(inBuf));
        System.out.println("Output sequence: " + ECUtils.byteArrayToHexString(outBuf, 4));
        
        assertTrue(true);

    }

}
