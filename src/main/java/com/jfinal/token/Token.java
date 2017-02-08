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

package com.jfinal.token;

import java.io.Serializable;

/**
 * Token.
 */
public class Token implements Serializable {
	
	private static final long serialVersionUID = -3667914001133777991L;
	
	private String id;
	private long expirationTime;
	
	public Token(String id, long expirationTime) {
		if (id == null) {
			throw new IllegalArgumentException("id can not be null");
		}
		
		this.expirationTime = expirationTime;
		this.id = id;
	}
	
	public Token(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id can not be null");
		}
		
		this.id = id;
	}
	
	/**
	 * Returns a string containing the unique identifier assigned to this token.
	 */
	public String getId() {
		return id;
	}
	
	public long getExpirationTime() {
		return expirationTime;
	}
	
	/**
	 * expirationTime 不予考虑, 因为就算 expirationTime 不同也认为是相同的 token.
	 */
	public int hashCode() {
		return id.hashCode();
	}
	
	public boolean equals(Object object) {
		if (object instanceof Token) {
			return ((Token)object).id.equals(this.id);
		}
		return false;
	}
}


