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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * DKFF(Dynamic Key Feature Forward) Lexer
 */
class Lexer {
	
	static final char EOF = (char)-1;
	static final int TEXT_STATE_DIAGRAM = 999;
	
	char[] buf;
	int state = 0;
	int lexemeBegin = 0;
	int forward = 0;
	int beginRow = 1;
	int forwardRow = 1;
	TextToken previousTextToken = null;
	
	List<Token> tokens = new ArrayList<Token>();
	LinkedList<String> stack = new LinkedList<String>();
	String fileName;
	
	public Lexer(StringBuilder content, String fileName) {
		int len = content.length();
		buf = new char[len + 1];
		content.getChars(0, content.length(), buf, 0);
		buf[len] = EOF;
		this.fileName = fileName;
	}
	
	/**
	 * 进入每个扫描方法之前 peek() 处于可用状态，不需要 next()
	 * 每个扫描方法内部是否要 next() 移动，取决定具体情况
	 * 每个扫描方法成功返回前，将 forward 置于下一次扫描需要处理的地方
	 * 让下个扫描方法不必 next()
	 * 紧靠 scanText() 之前的扫描方法在失败后必须保持住forward
	 * 这是 scanText() 可以一直向前的保障 
	 */
	public List<Token> scan() {
		while (peek() != EOF) {
			if (peek() == '#') {
				if (scanDire()) {
					continue ;
				}
				if (scanSingleLineComment()) {
					continue ;
				}
				if (scanMultiLineComment()) {
					continue ;
				}
				if (scanNoParse()) {
					continue ;
				}
			}
			
			scanText();
		}
		return tokens;
	}
	
	/**
	 * 指令模式与解析规则
	 * 1：指令 pattern
	 *   #(p)
	 *   #id(p)
	 *   #define id(p)
	 *   #@id(p) / #@id?(p)
	 *   #else / #end
	 *   
	 * 2：关键字类型指令在获取到关键字以后，必须要正确解析出后续内容，否则抛异常
	 * 
	 * 3：非关键字类型指令只有在本行内出现 # id ( 三个序列以后，才要求正确解析出后续内容
	 *    否则当成普通文本 
	 */
	boolean scanDire() {
		String id = null;
		StringBuilder para = null;
		Token idToken = null;
		Token paraToken = null;
		while(true) {
			switch (state) {
			case 0:
				if (peek() == '#') {					// #
					next();
					skipBlanks();
					state = 1;
					continue ;
				}
				return fail();
			case 1:
				if (peek() == '(') {					// # (
					para = scanPara("");
					idToken = new Token(Symbol.OUTPUT, beginRow);
					paraToken = new ParaToken(para, beginRow);
					return addOutputToken(idToken, paraToken);
				}
				if (CharTable.isLetter(peek())) {		// # id
					state = 10;
					continue ;
				}
				if (peek() == '@') {					// # @
					next();
					skipBlanks();
					if (CharTable.isLetter(peek())) {	// # @ id
						state = 20;
						continue ;
					}
				}
				return fail();
			// -----------------------------------------------------
			case 10:	// # id
				id = scanId();
				Symbol symbol = Symbol.getKeywordSym(id);
				// 非关键字指令
				if (symbol == null) {
					state = 11;
					continue ;
				}
				
				// define 指令
				if (symbol == Symbol.DEFINE) {
					state = 12;
					continue ;
				}
				
				// 无参关键字指令
				if (symbol.noPara()) {
					return addNoParaToken(new Token(symbol, id, beginRow));
				}
				
				// 有参关键字指令
				skipBlanks();
				if (peek() == '(') {
					para = scanPara(id);
					idToken = new Token(symbol, beginRow);
					paraToken = new ParaToken(para, beginRow);
					return addIdParaToken(idToken, paraToken);
				}
				throw new ParseException("#" + id + " directive requires parentheses \"()\"", new Location(fileName, beginRow));
			case 11: 	// 用户自定义指令必须有参数
				skipBlanks();
				if (peek() == '(') {
					para = scanPara(id);
					idToken = new Token(Symbol.ID, id, beginRow);
					paraToken = new ParaToken(para, beginRow);
					return addIdParaToken(idToken, paraToken);
				}
				return fail();	// 用户自定义指令在没有左括号的情况下当作普通文本
			case 12:			// 处理 "# define id (para)" 指令
				skipBlanks();
				if (CharTable.isLetter(peek())) {
					id = scanId();	// 模板函数名称
					skipBlanks();
					if (peek() == '(') {
						para = scanPara("define " + id);
						idToken = new Token(Symbol.DEFINE, id, beginRow);
						paraToken = new ParaToken(para, beginRow);
						return addIdParaToken(idToken, paraToken);
					}
					throw new ParseException("#define " + id + " : template function definition requires parentheses \"()\"", new Location(fileName, beginRow));
				}
				throw new ParseException("#define directive requires identifier as a function name", new Location(fileName, beginRow));
			case 20:	// # @ id
				id = scanId();
				skipBlanks();
				boolean hasQuestionMark = peek() == '?';
				if (hasQuestionMark) {
					next();
					skipBlanks();
				}
				if (peek() == '(') {
					para = scanPara(hasQuestionMark ? "@" + id + "?" : "@" + id);
					idToken = new Token(hasQuestionMark ? Symbol.CALL_IF_DEFINED : Symbol.CALL, id, beginRow);
					paraToken = new ParaToken(para, beginRow);
					return addIdParaToken(idToken, paraToken);
				}
				return fail();
			default :
				return fail();
			}
		}
	}
	
	/**
	 * 调用者已确定以字母或下划线开头，故一定可以获取到 id值
	 */
	String scanId() {
		int idStart = forward;
		while (CharTable.isLetterOrDigit(next())) {
			;
		}
		return subBuf(idStart, forward - 1).toString();
	}
	
	/**
	 * 扫描指令参数，成功则返回，否则抛出词法分析异常
	 */
	StringBuilder scanPara(String id) {
		stack.clear();
		char quotes = '"';
		int localState = 0;
		stack.push("(");
		next();
		int paraStart = forward;
		while (true) {
			switch (localState) {
			case 0:
				for (char c=peek(); true; c=next()) {
					if (c == ')') {
						stack.pop();
						if (stack.size() == 0) {
							next();
							return subBuf(paraStart, forward - 2);
						}
						continue ;
					}
					
					if (c == '(') {
						stack.push("(");
						continue ;
					}
					
					if (c == '"' || c == '\'') {
						quotes = c;
						localState = 1;
						break ;
					}
					
					if (CharTable.isExprChar(c)) {
						continue ;
					}
					
					if (c == EOF) {
						throw new ParseException("#" + id + " parameter can not match the end char ')'", new Location(fileName, beginRow));
					}
					
					throw new ParseException("#" + id + " parameter exists illegal char: '" + c + "'", new Location(fileName, beginRow));
				}
				break ;
			case 1:
				for (char c=next(); true; c=next()) {
					if (c == quotes) {
						if (buf[forward - 1] != '\\') {	// 前一个字符不是转义字符
							next();
							localState = 0;
							break ;
						} else {
							continue ;
						}
					}
					
					if (c == EOF) {
						throw new ParseException("#" + id + " parameter error, the string parameter not ending", new Location(fileName, beginRow));
					}
				}
				break ;
			}
		}
	}
	
	/**
	 * 单行注释，开始状态 100，关注换行与 EOF
	 */
	boolean scanSingleLineComment() {
		while (true) {
			switch (state) {
			case 100:
				if (peek() == '#' && next() == '#' && next() == '#') {
					state = 101;
					continue ;
				}
				return fail();
			case 101:
				for (char c=next(); true; c=next()) {
					if (c == '\n') {
						if (deletePreviousTextTokenBlankTails()) {
							return prepareNextScan(1);
						} else {
							return prepareNextScan(0);
						}
					}
					if (c == EOF) {
						deletePreviousTextTokenBlankTails();
						return prepareNextScan(0);
					}
				}
			default :
				return fail();
			}
		}
	}
	
	/**
	 * 多行注释，开始状态 200，关注结尾标记与 EOF
	 */
	boolean scanMultiLineComment() {
		while (true) {
			switch (state) {
			case 200:
				if (peek() == '#' && next() == '-' && next() == '-') {
					state = 201;
					continue ;
				}
				return fail();
			case 201:
				for (char c=next(); true; c=next()) {
					if (c == '-' && buf[forward + 1] == '-' && buf[forward + 2] == '#') {
						forward = forward + 3;
						if (lookForwardLineFeedAndEof() && deletePreviousTextTokenBlankTails()) {
							return prepareNextScan(peek() != EOF ? 1 : 0);
						} else {
							return prepareNextScan(0);
						}
					}
					if (c == EOF) {
						throw new ParseException("The multiline comment start block \"#--\" can not match the end block: \"--#\"", new Location(fileName, beginRow));
					}
				}
			default :
				return fail();
			}
		}
	}
	
	/**
	 * 非解析块，开始状态 300，关注结尾标记与 EOF
	 */
	boolean scanNoParse() {
		while (true) {
			switch (state) {
			case 300:
				if (peek() == '#' && next() == '[' && next() == '[') {
					state = 301;
					continue ;
				}
				return fail();
			case 301:
				for (char c=next(); true; c=next()) {
					if (c == ']' && buf[forward + 1] == ']' && buf[forward + 2] == '#') {
						addTextToken(subBuf(lexemeBegin + 3, forward - 1));	// NoParse 块使用 TextToken
						return prepareNextScan(3);
					}
					if (c == EOF) {
						throw new ParseException("The \"no parse\" start block \"#[[\" can not match the end block: \"]]#\"", new Location(fileName, beginRow));
					}
				}
			default :
				return fail();
			}
		}
	}
	
	boolean scanText() {
		for (char c=peek(); true; c=next()) {
			if (c == '#' || c == EOF) {
				addTextToken(subBuf(lexemeBegin, forward - 1));
				return prepareNextScan(0);
			}
		}
	}
	
	boolean fail() {
		if (state < 300) {
			forward = lexemeBegin;
			forwardRow = beginRow;
		}
		if (state < 100) {
			state = 100;
		} else if (state < 200) {
			state = 200;
		} else if (state < 300) {
			state = 300;
		} else {
			state = TEXT_STATE_DIAGRAM;
		}
		return false;
	}
	
	char next() {
		if (buf[forward] == '\n') {
			forwardRow++;
		}
		return buf[++forward];
	}
	
	char peek() {
		return buf[forward];
	}
	
	void skipBlanks() {
		while(CharTable.isBlank(buf[forward])) {
			next();
		}
	}
	
	/**
	 * scanPara 与 scanNoParse 存在 start > end 的情况
	 */
	StringBuilder subBuf(int start, int end) {
		if (start > end) {
			return null;
		}
		StringBuilder ret = new StringBuilder(end - start + 1);
		for (int i=start; i<=end; i++) {
			ret.append(buf[i]);
		}
		return ret;
	}
	
	boolean prepareNextScan(int moveForward) {
		for (int i=0; i<moveForward; i++) {
			next();
		}
		
		state = 0;
		lexemeBegin = forward;
		beginRow = forwardRow;
		return true;
	}
	
	void addTextToken(StringBuilder text) {
		if (text == null || text.length() == 0) {
			return ;
		}
		
		if (previousTextToken != null) {
			previousTextToken.append(text);
		} else {
			previousTextToken = new TextToken(text, beginRow);
			tokens.add(previousTextToken);
		}
	}
	
	// 输出指令不对前后空白与换行进行任何处理，直接调用 tokens.add(...)
	boolean addOutputToken(Token idToken, Token paraToken) {
		tokens.add(idToken);
		tokens.add(paraToken);
		previousTextToken = null;
		return prepareNextScan(0);
	}
	
	// 向前看后续是否跟随的是空白 + 换行或者是空白 + EOF，是则表示当前指令后续没有其它有用内容
	boolean lookForwardLineFeedAndEof() {
		int forwardBak = forward;
		int forwardRowBak = forwardRow;
		for (char c=peek(); true; c=next()) {
			if (CharTable.isBlank(c)) {
				continue ;
			}
			if (c == '\n' || c == EOF) {
				return true;
			}
			forward = forwardBak;
			forwardRow = forwardRowBak;
			return false;
		}
	}
	
	/**
	 * 带参指令处于独立行时删除前后空白字符，并且再删除一个后续的换行符
	 * 处于独立行是指：向前看无有用内容，在前面情况成立的基础之上
	 *             再向后看如果也无可用内容，前一个条件成立才开执行后续动作
	 * 
	 * 向前看时 forward 在移动，意味着正在删除空白字符(通过 lookForwardLineFeed()方法)
	 * 向后看时也会在碰到空白 + '\n' 时删空白字符 (通过 deletePreviousTextTokenBlankTails()方法)
	 */
	boolean addIdParaToken(Token idToken, Token paraToken) {
		tokens.add(idToken);
		tokens.add(paraToken);
		
		// if (lookForwardLineFeed() && (deletePreviousTextTokenBlankTails() || lexemeBegin == 0)) {
		if (lookForwardLineFeedAndEof() && deletePreviousTextTokenBlankTails()) {
			prepareNextScan(peek() != EOF ? 1 : 0);
		} else {
			prepareNextScan(0);
		}
		previousTextToken = null;
		return true;
	}
	
	// 处理前后空白的逻辑与 addIdParaToken() 基本一样，仅仅多了一个对于紧随空白的 next() 操作
	boolean addNoParaToken(Token noParaToken) {
		tokens.add(noParaToken);
		if (CharTable.isBlank(peek())) {
			next();	// 无参指令之后紧随的一个空白字符仅为分隔符，不参与后续扫描
		}
		
		if (lookForwardLineFeedAndEof() && deletePreviousTextTokenBlankTails()) {
			prepareNextScan(peek() != EOF ? 1 : 0);
		} else {
			prepareNextScan(0);
		}
		previousTextToken = null;
		return true;
	}
	
	/**
	 * 1：当前指令前方仍然是指令 (previousTextToken 为 null)，直接返回 true
	 * 2：当前指令前方为 TextToken 时的处理逻辑与返回值完全依赖于 TextToken.deleteBlankTails()
	 */
	boolean deletePreviousTextTokenBlankTails() {
		// return previousTextToken != null ? previousTextToken.deleteBlankTails() : false;
		return previousTextToken == null || previousTextToken.deleteBlankTails();
	}
}




