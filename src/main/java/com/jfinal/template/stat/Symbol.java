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

import java.util.HashMap;
import java.util.Map;

/**
 * Symbol
 */
enum Symbol {
	
	TEXT("text", false),
	
	OUTPUT("output", true),
	
	DEFINE("define", true),
	CALL("call", true),
	CALL_IF_DEFINED("callIfDefined", true),
	SET("set", true),
	SET_LOCAL("setLocal", true),
	SET_GLOBAL("setGlobal", true),
	INCLUDE("include", true),
	
	FOR("for", true),
	IF("if", true),
	ELSEIF("elseif", true),
	ELSE("else", false),
	END("end", false),
	CONTINUE("continue", false),
	BREAK("break", false),
	RETURN("return", false),
	
	ID("ID", false),				// 标识符：下划线或字母开头 ^[A-Za-z_][A-Za-z0-9_]*$
	PARA("PARA", false),
	
	EOF("EOF", false);
	
	private final String name;
	private final boolean hasPara;	// 是否有参
	
	/**
	 * Lexer 中确定为系统指令以后，必须得到正确的后续 Token 序列，否则报异常
	 * 扩展指令在得到 # id ( 序列以后才要求得到正确的后续 Token 序列，否则仅仅 return fail()
	 */
	@SuppressWarnings("serial")
	private static final Map<String, Symbol> keywords = new HashMap<String, Symbol>() {{
		put(Symbol.IF.getName(), IF);
		put(Symbol.ELSEIF.getName(), ELSEIF);
		put(Symbol.ELSE.getName(), ELSE);
		put(Symbol.END.getName(), END);
		put(Symbol.FOR.getName(), FOR);
		put(Symbol.BREAK.getName(), BREAK);
		put(Symbol.CONTINUE.getName(), CONTINUE);
		put(Symbol.RETURN.getName(), RETURN);
		
		put(Symbol.DEFINE.getName(), DEFINE);
		put(Symbol.SET.getName(), SET);
		put(Symbol.SET_LOCAL.getName(), SET_LOCAL);
		put(Symbol.SET_GLOBAL.getName(), SET_GLOBAL);
		put(Symbol.INCLUDE.getName(), INCLUDE);
	}};
	
	private Symbol(String name, boolean hasPara) {
		this.name = name;
		this.hasPara = hasPara;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}
	
	boolean hasPara() {
		return hasPara;
	}
	
	boolean noPara() {
		return !hasPara;
	}
	
	public static Symbol getKeywordSym(String name) {
		return keywords.get(name);
	}
}




