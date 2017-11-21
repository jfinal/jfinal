/**
 * Copyright (c) 2011-2017, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.template.io;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * JdkEncoder
 */
public class JdkEncoder extends Encoder {
	
	private CharsetEncoder ce;
	
	public JdkEncoder(Charset charset) {
		this.ce = charset.newEncoder();
	}
	
	public float maxBytesPerChar() {
		return ce.maxBytesPerChar();
	}
	
	public int encode(char[] chars, int offset, int len, byte[] bytes) {
		ce.reset();
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        CharBuffer cb = CharBuffer.wrap(chars, offset, len);
        try {
            CoderResult cr = ce.encode(cb, bb, true);
            if (!cr.isUnderflow())
                cr.throwException();
            cr = ce.flush(bb);
            if (!cr.isUnderflow())
                cr.throwException();
            return bb.position();
        } catch (CharacterCodingException x) {
            // Substitution is always enabled,
            // so this shouldn't happen
        	throw new RuntimeException("Encode error: " + x.getMessage(), x);
        }
	}
}






