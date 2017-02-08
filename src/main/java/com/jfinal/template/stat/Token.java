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

package com.jfinal.template.stat;

/**
 * Token
 */
class Token {
	
	final Symbol symbol;
	final int row;
	private final String value;
	
	Token(Symbol symbol, String value, int row) {
		if (symbol == null || value == null) {
			throw new IllegalArgumentException("symbol and value can not be null");
		}
		this.symbol = symbol;
		this.value = value;
		this.row = row;
	}
	
	Token(Symbol symbol, int row) {
		this(symbol, symbol.getName(), row);
	}
	
	boolean hasPara() {
		return symbol.hasPara();
	}
	
	boolean noPara() {
		return symbol.noPara();
	}
	
	public String value() {
		return value;
	}
	
	public String toString() {
		return value;
	}
	
	public int getRow() {
		return row;
	}
	
	public void print() {
		System.out.print("[");
		System.out.print(row);
		System.out.print(", ");
		System.out.print(symbol.getName());
		System.out.print(", ");
		System.out.print(value());
		System.out.println("]");
	}
}


