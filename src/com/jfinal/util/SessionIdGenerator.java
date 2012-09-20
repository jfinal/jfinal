/**
 * Copyright (c) 2011-2012, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.util;

import java.security.SecureRandom;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SessionIdGenerator.
 */
public class SessionIdGenerator {
	
    protected static Random random;
    private static boolean weakRandom;
    private static volatile Object lock = new Object();
    
    private static final SessionIdGenerator me = new SessionIdGenerator();
    
    private SessionIdGenerator() {
    	try {
			// This operation may block on some systems with low entropy. See
			// this page
			// for workaround suggestions:
			// http://docs.codehaus.org/display/JETTY/Connectors+slow+to+startup
			System.out.println("Init SecureRandom.");
			random = new SecureRandom();
			weakRandom = false;
		} catch (Exception e) {
			System.err.println("Could not generate SecureRandom for session-id randomness");
			random = new Random();
			weakRandom = true;
		}
    }
    
    public static final SessionIdGenerator me() {
    	return me;
    }
    
	public String generate(HttpServletRequest request, HttpServletResponse response) {
        synchronized (lock) {
            String id = null;
            while (id == null || id.length() == 0) {	//)||idInUse(id))
                long r0 = weakRandom ? (hashCode()^Runtime.getRuntime().freeMemory()^random.nextInt()^(((long)request.hashCode())<<32)) : random.nextLong();
                long r1 = random.nextLong();
                if (r0<0) r0 = -r0;
                if (r1<0) r1 = -r1;
                id=Long.toString(r0,36)+Long.toString(r1,36);
            }
            return id;
        }
	}
}



