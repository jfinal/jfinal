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

package com.jfinal.template.expr;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import com.jfinal.kit.JavaKeyword;
import com.jfinal.template.stat.CharTable;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParaToken;
import com.jfinal.template.stat.ParseException;

/**
 * ExprLexer
 */
class ExprLexer {
	
	static final char EOF = (char)-1;
	static final JavaKeyword javaKeyword = new JavaKeyword();
	static final Pattern DOUBLE_QUOTES_PATTERN = Pattern.compile("\\\\\"");
	static final Pattern SINGLE_QUOTES_PATTERN = Pattern.compile("\\\\'");
	
	char[] buf;
	int state = 0;
	int lexemeBegin = 0;
	int forward = 0;
	int beginRow = 1;
	int forwardRow = 1;
	List<Tok> tokens = new ArrayList<Tok>();
	Location location;
	
	public ExprLexer(ParaToken paraToken, Location location) {
		this.location = location;
		StringBuilder content = paraToken.getContent();
		beginRow = paraToken.getRow();
		forwardRow = beginRow;
		if (content == null) {
			buf = new char[]{EOF};
			return ;
		}
		int len = content.length();
		buf = new char[len + 1];
		content.getChars(0, content.length(), buf, 0);
		buf[len] = EOF;
	}
	
	public List<Tok> scan() {
		while (peek() != EOF) {
			skipBlanks();
			lexemeBegin = forward;
			beginRow = forwardRow;
			if (scanId()) {
				continue ;
			}
			if (scanOperator()) {
				continue ;
			}
			if (scanString()) {
				continue ;
			}
			if (scanNumber()) {
				continue ;
			}
			
			if (peek() != EOF) {
				throw new ParseException("Expression not support the char: '" + peek() + "'", location);
			}
		}
		return tokens;
	}
	
	/**
	 * 扫描 ID true false null
	 */
	boolean scanId() {
		if (state != 0) {
			return false;
		}
		
		if (!CharTable.isLetter(peek())) {
			return fail();
		}
		
		while (CharTable.isLetterOrDigit(next())) {
			;
		}
		String id = subBuf(lexemeBegin, forward - 1).toString();
		if ("true".equals(id)) {
			addToken(new Tok(Sym.TRUE, id, beginRow));
		} else if ("false".equals(id)) {
			addToken(new Tok(Sym.FALSE, id, beginRow));
		} else if ("null".equals(id)) {
			addToken(new Tok(Sym.NULL, id, beginRow));
		} else if (CharTable.isBlankOrLineFeed(peek()) && javaKeyword.contains(id)) {
			throw new ParseException("Identifier can not be java keyword : " + id, location);
		} else {
			addToken(new Tok(Sym.ID, id, beginRow));
		}
		return prepareNextScan();
	}
	
	/**
	 * + - * / % ++ --
	 * = == != < <= > >=
	 * ! && ||
	 * ? ?: ?!
	 * . .. : :: , ;
	 * ( ) [ ] { }
	 */
	boolean scanOperator() {
		if (state != 100) {
			return false;
		}
		
		Tok tok;
		switch (peek()) {
		case '+':		// + - * / % ++ --
			if (next() == '+') {
				tok = new Tok(Sym.INC, beginRow);
				next();
			} else {
				tok = new Tok(Sym.ADD, beginRow);
			}
			return ok(tok);
		case '-':
			if (next() == '-') {
				tok = new Tok(Sym.DEC, beginRow);
				next();
			} else {
				tok = new Tok(Sym.SUB, beginRow);
			}
			return ok(tok);
		case '*':
			tok = new Tok(Sym.MUL, beginRow);
			next();
			return ok(tok);
		case '/':
			tok = new Tok(Sym.DIV, beginRow);
			next();
			return ok(tok);
		case '%':
			tok = new Tok(Sym.MOD, beginRow);
			next();
			return ok(tok);
		case '=':		// = == != < <= > >=
			if (next() == '=') {
				tok = new Tok(Sym.EQUAL, beginRow);
				next();
			} else {
				tok = new Tok(Sym.ASSIGN, beginRow);
			}
			return ok(tok);
		case '!':
			if (next() == '=') {
				tok = new Tok(Sym.NOTEQUAL, beginRow);
				next();
			} else {
				tok = new Tok(Sym.NOT, beginRow);
			}
			return ok(tok);
		case '<':
			if (next() == '=') {
				tok = new Tok(Sym.LE, beginRow);
				next();
			} else {
				tok = new Tok(Sym.LT, beginRow);
			}
			return ok(tok);
		case '>':
			if (next() == '=') {
				tok = new Tok(Sym.GE, beginRow);
				next();
			} else {
				tok = new Tok(Sym.GT, beginRow);
			}
			return ok(tok);
		case '&':		// ! && ||
			if (next() == '&') {
				tok = new Tok(Sym.AND, beginRow);
				next();
			} else {
				throw new ParseException("Unsupported operator: '&'", location);
			}
			return ok(tok);
		case '|':
			if (next() == '|') {
				tok = new Tok(Sym.OR, beginRow);
				next();
			} else {
				throw new ParseException("Unsupported operator: '|'", location);
			}
			return ok(tok);
		case '?':		// ? ??
			if (next() == '?') {
				tok = new Tok(Sym.NULL_SAFE, beginRow);
				next();
			} else {
				tok = new Tok(Sym.QUESTION, beginRow);
			}
			return ok(tok);
		case '.':		// . .. : :: , ;
			if (next() == '.') {
				tok = new Tok(Sym.RANGE, beginRow);
				next();
			} else {
				tok = new Tok(Sym.DOT, ".", beginRow);
			}
			return ok(tok);
		case ':':
			if (next() == ':') {
				tok = new Tok(Sym.STATIC, beginRow);
				next();
			} else {
				tok = new Tok(Sym.COLON, beginRow);
			}
			return ok(tok);
		case ',':
			tok = new Tok(Sym.COMMA, beginRow);
			next();
			return ok(tok);
		case ';':
			tok = new Tok(Sym.SEMICOLON, beginRow);
			next();
			return ok(tok);
		case '(':		// ( ) [ ] { }
			tok = new Tok(Sym.LPAREN, beginRow);
			next();
			return ok(tok);
		case ')':
			tok = new Tok(Sym.RPAREN, beginRow);
			next();
			return ok(tok);
		case '[':
			tok = new Tok(Sym.LBRACK, beginRow);
			next();
			return ok(tok);
		case ']':
			tok = new Tok(Sym.RBRACK, beginRow);
			next();
			return ok(tok);
		case '{':
			tok = new Tok(Sym.LBRACE, beginRow);
			next();
			return ok(tok);
		case '}':
			tok = new Tok(Sym.RBRACE, beginRow);
			next();
			return ok(tok);
		default :
			return fail();
		}
	}
	
	boolean ok(Tok tok) {
		tokens.add(tok);
		return prepareNextScan();
	}
	
	boolean scanString() {
		if (state != 200) {
			return false;
		}
		
		char quotes = peek();
		if (quotes != '"' && quotes != '\'') {
			return fail();
		}
		
		for (char c=next(); true; c=next()) {
			if (c == quotes) {
				if (buf[forward - 1] != '\\') {	// 前一个字符不是转义字符
					StringBuilder sb = subBuf(lexemeBegin + 1, forward -1);
					String str;
					if (sb != null) {
						if (quotes == '"') {
							str = DOUBLE_QUOTES_PATTERN.matcher(sb).replaceAll("\"");
						} else {
							str = SINGLE_QUOTES_PATTERN.matcher(sb).replaceAll("'");
						}
					} else {
						str = "";
					}
					
					Tok tok = new Tok(Sym.STR, str, beginRow);
					addToken(tok);
					next();
					return prepareNextScan();
				} else {
					continue ;
				}
			}
			
			if (c == EOF) {
				throw new ParseException("Expression error, the string not ending", location);
			}
		}
	}
	
	boolean scanNumber() {
		if (state != 300) {
			return false;
		}
		
		char c = peek();
		if (!CharTable.isDigit(c)) {
			return fail();
		}
		
		int numStart = lexemeBegin;				// forward;
		int radix = 10;							// 10 进制
		if (c == '0') {
			c = next();
			if (c == 'X' || c == 'x') {
				radix = 16;						// 16 进制
				c = next();
				numStart = numStart + 2;
			} else {
				radix = 8;						// 8 进制
				// numStart = numStart + 1;		// 8 进制不用去掉前缀 0，可被正确转换，去除此行便于正确处理数字 0
			}
		}
		
		c = skipDigit(radix);
		Sym sym = null;
		if (c == '.') {							// 以 '.' 字符结尾是合法的浮点数
			next();
			if (peek() == '.') {				// 处理 [0..9] 这样的表达式
				StringBuilder n = subBuf(numStart, forward - 2);
				if (n == null /* && radix == 16 */) {
					// 16 进制数格式错误，前缀 0x 后缺少 16 进制数字
					throw new ParseException("Error hex format", location);
				}
				NumTok tok = new NumTok(Sym.INT, n.toString(), radix, false, location);
				addToken(tok);
				retract(1);
				return prepareNextScan();
			}
			
			sym = Sym.DOUBLE;					// 浮点型默认为 double
			c = skipDigit(radix);
		}
		
		boolean isScientificNotation = false;
		if (c == 'E' || c == 'e') {				// scientific notation 科学计数法
			c = next();
			if (c == '+' || c == '-') {
				c = next();
			}
			if (!CharTable.isDigit(c)) {
				// 科学计数法后面缺少数字
				throw new ParseException("Error scientific notation format", location);
			}
			isScientificNotation = true;
			sym = Sym.DOUBLE;					// 科学计数法默认类型为 double
			
			c = skipDecimalDigit();				// 科学计数法的指数部分是十进制
		}
		
		StringBuilder num;
		if (c == 'L' || c == 'l') {
			if (sym == Sym.DOUBLE) {
				// 浮点类型不能使用 'L' 或 'l' 后缀
				throw new ParseException("Error float format", location);
			}
			sym = Sym.LONG;
			next();
			num = subBuf(numStart, forward - 2);
		} else if (c == 'F' || c == 'f') {
			sym = Sym.FLOAT;
			next();
			num = subBuf(numStart, forward - 2);
		} else if (c == 'D' || c == 'd') {
			sym = Sym.DOUBLE;
			next();
			num = subBuf(numStart, forward - 2);
		} else {
			if (sym == null) {
				sym = Sym.INT;
			}
			num = subBuf(numStart, forward - 1);
		}
		if (errorFollow()) {
			// "错误的表达式元素 : " + num + peek()
			throw new ParseException("Error expression: " + num + peek(), location);
		}
		if (num == null /* && radix == 16 */) {
			// 16 进制数格式错误，前缀 0x 后缺少 16 进制数字
			throw new ParseException("Error hex format", location);
		}
		
		NumTok tok = new NumTok(sym, num.toString(), radix, isScientificNotation, location);
		addToken(tok);
		return prepareNextScan();
	}
	
	boolean errorFollow() {
		char c = peek();
		return CharTable.isLetterOrDigit(c) || c == '"' || c == '\'';
	}
	
	char skipDigit(int radix) {
		if (radix == 10) {
			return skipDecimalDigit();
		} else if (radix == 16) {
			return skipHexadecimalDigit();
		} else {
			return skipOctalDigit();
		}
	}
	
	char skipDecimalDigit() {
		char c = peek();
		for (; CharTable.isDigit(c);) {
			c = next();
		}
		return c;
	}
	
	char skipHexadecimalDigit() {
		char c = peek();
		for (; CharTable.isHexadecimalDigit(c);) {
			c = next();
		}
		return c;
	}
	
	char skipOctalDigit() {
		char c = peek();
		for (; CharTable.isOctalDigit(c);) {
			c = next();
		}
		return c;
	}
	
	boolean fail() {
		forward = lexemeBegin;
		forwardRow = beginRow;
		
		if (state < 100) {
			state = 100;
		} else if (state < 200) {
			state = 200;
		} else if (state < 300) {
			state = 300;
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
	
	/**
	 * 表达式词法分析需要跳过换行与回车
	 */
	void skipBlanks() {
		while(CharTable.isBlankOrLineFeed(buf[forward])) {
			next();
		}
	}
	
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
	
	boolean prepareNextScan() {
		state = 0;
		lexemeBegin = forward;
		beginRow = forwardRow;
		return true;
	}
	
	void addToken(Tok tok) {
		tokens.add(tok);
	}
	
	void retract(int n) {
		for (int i=0; i<n; i++) {
			forward--;
			if (buf[forward] == '\n') {
				forwardRow--;
			}
		}
	}
}





