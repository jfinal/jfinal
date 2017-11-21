/*
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 */

package com.jfinal.template.io;

import java.io.IOException;

public class LongWriter {
	
    private static final byte[] minValueBytes = "-9223372036854775808".getBytes();
    private static final char[] minValueChars = "-9223372036854775808".toCharArray();
    
	public static void write(ByteWriter byteWriter, long value) throws IOException {
		if (value == Long.MIN_VALUE) {
			byteWriter.out.write(minValueBytes, 0, minValueBytes.length);
            return ;
		}
		
        int size = (value < 0) ? stringSize(-value) + 1 : stringSize(value);
        char[] chars = byteWriter.chars;
        byte[] bytes = byteWriter.bytes;
        getChars(value, size, chars);
        
        // int len = Utf8Encoder.me.encode(chars, 0, size, bytes);
        // byteWriter.out.write(bytes, 0, len);
        
        for (int j=0; j<size; j++) {
        	bytes[j] = (byte)chars[j];
        }
        byteWriter.out.write(bytes, 0, size);
    }
	
	public static void write(CharWriter charWriter, long i) throws IOException {
		if (i == Long.MIN_VALUE) {
            charWriter.out.write(minValueChars, 0, minValueChars.length);
			return ;
		}
		
        int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);
        char[] chars = charWriter.chars;
        getChars(i, size, chars);
        charWriter.out.write(chars, 0, size);
    }
	
	static int stringSize(long x) {
        long p = 10;
        for (int i=1; i<19; i++) {
            if (x < p)
                return i;
            p = 10*p;
        }
        return 19;
    }
	
	static void getChars(long i, int index, char[] buf) {
        long q;
        int r;
        int charPos = index;
        char sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i > Integer.MAX_VALUE) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = (int)(i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            buf[--charPos] = IntegerWriter.DigitOnes[r];
            buf[--charPos] = IntegerWriter.DigitTens[r];
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int)i;
        while (i2 >= 65536) {
            q2 = i2 / 100;
            // really: r = i2 - (q * 100);
            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
            i2 = q2;
            buf[--charPos] = IntegerWriter.DigitOnes[r];
            buf[--charPos] = IntegerWriter.DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i2 <= 65536, i2);
        for (;;) {
            q2 = (i2 * 52429) >>> (16+3);
            r = i2 - ((q2 << 3) + (q2 << 1));  // r = i2-(q2*10) ...
            buf[--charPos] = IntegerWriter.digits[r];
            i2 = q2;
            if (i2 == 0) break;
        }
        if (sign != 0) {
            buf[--charPos] = sign;
        }
    }
}


