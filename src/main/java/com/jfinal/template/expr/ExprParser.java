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
import java.util.LinkedHashMap;
import java.util.List;
import com.jfinal.template.EngineConfig;
import com.jfinal.template.expr.Sym;
import com.jfinal.template.expr.ast.Arith;
import com.jfinal.template.expr.ast.Array;
import com.jfinal.template.expr.ast.Assign;
import com.jfinal.template.expr.ast.Compare;
import com.jfinal.template.expr.ast.Const;
import com.jfinal.template.expr.ast.Expr;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.expr.ast.Field;
import com.jfinal.template.expr.ast.ForCtrl;
import com.jfinal.template.expr.ast.Id;
import com.jfinal.template.expr.ast.IncDec;
import com.jfinal.template.expr.ast.Index;
import com.jfinal.template.expr.ast.Logic;
import com.jfinal.template.expr.ast.Map;
import com.jfinal.template.expr.ast.Method;
import com.jfinal.template.expr.ast.NullSafe;
import com.jfinal.template.expr.ast.RangeArray;
import com.jfinal.template.expr.ast.SharedMethod;
import com.jfinal.template.expr.ast.StaticField;
import com.jfinal.template.expr.ast.StaticMethod;
import com.jfinal.template.expr.ast.Ternary;
import com.jfinal.template.expr.ast.Unary;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParaToken;
import com.jfinal.template.stat.ParseException;

/**
 * ExprParser
 */
public class ExprParser {
	
	static final Tok EOF = new Tok(Sym.EOF, -1);
	
	Tok peek = null;
	int forward = 0;
	List<Tok> tokenList;
	Location location;
	
	ParaToken paraToken;
	EngineConfig engineConfig;
	
	public ExprParser(ParaToken paraToken, EngineConfig engineConfig, String fileName) {
		this.paraToken = paraToken;
		this.engineConfig = engineConfig;
		this.location = new Location(fileName, paraToken.getRow());
	}
	
	void initPeek() {
		peek = tokenList.get(forward);
	}
	
	Tok peek() {
		return peek;
	}
	
	Tok move() {
		peek = tokenList.get(++forward);
		return peek;
	}
	void resetForward(int position) {
		forward = position;
		peek = tokenList.get(forward);
	}
	
	Tok match(Sym sym) {
		Tok current = peek();
		if (current.sym == sym) {
			move();
			return current;
		}
		throw new ParseException("Expression error: can not match the symbol \"" + sym.value() + "\"", location);
	}
	
	public ExprList parseExprList() {
		return (ExprList)parse(true);
	}
	
	public ForCtrl parseForCtrl() {
		Expr forCtrl = parse(false);
		if (forCtrl instanceof ForCtrl) {
			return (ForCtrl)forCtrl;
		} else {
			throw new ParseException("The expression of #for directive is error", location);
		}
	}
	
	Expr parse(boolean isExprList) {
		tokenList = new ExprLexer(paraToken, location).scan();
		if (tokenList.size() == 0) {
			return ExprList.NULL_EXPR_LIST;
		}
		tokenList.add(EOF);
		initPeek();
		Expr expr = isExprList ? exprList() : forCtrl();
		if (peek() != EOF) {
			throw new ParseException("Expression error: can not match \"" + peek().value() + "\"", location);
		}
		return expr;
	}
	
	/**
	 * exprList : expr (',' expr)*
	 */
	Expr exprList() {
		List<Expr> exprList = new ArrayList<Expr>();
		while (true) {
			Expr stat = expr();
			if (stat != null) {
				exprList.add(stat);
				if (peek().sym == Sym.COMMA) {
					move();
					if (peek() == EOF) {
						throw new ParseException("Expression error: can not match the char of comma ','", location);
					}
					continue ;
				}
			}
			break ;
		}
		return new ExprList(exprList);
	}
	
	Expr expr() {
		return assign();
	}
	
	/**
	 * assign : <assoc=right> ID ( '[' expr ']' )? '=' expr
	 */
	Expr assign() {
		Tok idTok = peek();
		if (idTok.sym != Sym.ID) {
			return ternary();
		}
		
		int begin = forward;
		// ID = expr
		if (move().sym == Sym.ASSIGN) {
			move();
			return new Assign(idTok.value(), expr(), location);
		}
		
		// array、map 赋值：ID [ expr ] = expr
		if (peek().sym == Sym.LBRACK) {
			move();
			Expr index = expr();
			match(Sym.RBRACK);
			if (peek().sym == Sym.ASSIGN) {
				move();
				return new Assign(idTok.value(), index, expr(), location);	// 右结合无限连
			}
		}
		
		resetForward(begin);
		return ternary();
	}
	
	/**
	 * ternary : expr '?' expr ':' expr
	 */
	Expr ternary() {
		Expr cond = or();
		if (peek().sym == Sym.QUESTION) {
			move();
			Expr exprOne = expr();
			match(Sym.COLON);
			return new Ternary(cond, exprOne, expr(), location);
		}
		return cond;
	}
	
	/**
	 * or : expr '||' expr
	 */
	Expr or() {
		Expr expr = and();
		for (Tok tok=peek(); tok.sym==Sym.OR; tok=peek()) {
			move();
			expr = new Logic(Sym.OR, expr, and(), location);
		}
		return expr;
	}
	
	/**
	 * and : expr '&&' expr
	 */
	Expr and() {
		Expr expr = equalNotEqual();
		for (Tok tok=peek(); tok.sym==Sym.AND; tok=peek()) {
			move();
			expr = new Logic(Sym.AND, expr, equalNotEqual(), location);
		}
		return expr;
	}
	
	/**
	 * equalNotEqual : expr ('==' | '!=') expr
	 */
	Expr equalNotEqual() {
		Expr expr = greaterLess();
		for (Tok tok=peek(); tok.sym==Sym.EQUAL || tok.sym==Sym.NOTEQUAL; tok=peek()) {
			move();
			expr = new Compare(tok.sym, expr, greaterLess(), location);
		}
		return expr;
	}
	
	/**
	 * compare expr ('<=' | '>=' | '>' | '<') expr
	 * 不支持无限连： > >= < <=
	 */
	Expr greaterLess() {
		Expr expr = addSub();
		Tok tok = peek();
		if (tok.sym == Sym.LT || tok.sym == Sym.LE || tok.sym == Sym.GT || tok.sym == Sym.GE) {
			move();
			return new Compare(tok.sym, expr, addSub(), location);
		}
		return expr;
	}
	
	/**
	 * addSub : expr ('+'|'-') expr
	 */
	Expr addSub() {
		Expr expr = mulDivMod();
		for (Tok tok=peek(); tok.sym==Sym.ADD || tok.sym==Sym.SUB; tok=peek()) {
			move();
			expr = new Arith(tok.sym, expr, mulDivMod(), location);
		}
		return expr;
	}
	
	/**
	 * mulDivMod : expr ('*'|'/'|'%') expr
	 */
	Expr mulDivMod() {
		Expr expr = nullSafe();
		for (Tok tok=peek(); tok.sym==Sym.MUL || tok.sym==Sym.DIV || tok.sym==Sym.MOD; tok=peek()) {
			move();
			expr = new Arith(tok.sym, expr, nullSafe(), location);
		}
		return expr;
	}
	
	/**
	 * nullSafe : expr '??' expr
	 */
	Expr nullSafe() {
		Expr expr = unary();
		for (Tok tok=peek(); tok.sym==Sym.NULL_SAFE; tok=peek()) {
			move();
			expr = new NullSafe(expr, unary(), location);
		}
		return expr;
	}
	
	/**
	 * unary : ('!' | '+' | '-'| '++' | '--') expr
	 */
	Expr unary() {
		Tok tok = peek();
		switch (tok.sym) {
		case NOT:
			move();
			return new Logic(tok.sym, unary(), location);
		case ADD:
		case SUB:
			move();
			return new Unary(tok.sym, unary(), location);
		case INC:
		case DEC:
			move();
			return new IncDec(tok.sym, false, incDec(), location);
		default:
			return incDec();
		}
	}
	
	/**
	 * incDec : expr ('++' | '--')
	 */
	Expr incDec() {
		Expr expr = staticMember();
		Tok tok = peek();
		if (tok.sym == Sym.INC || tok.sym == Sym.DEC) {
			move();
			return new IncDec(tok.sym, true, expr, location);
		}
		
		return expr;
	}
	
	/**
	 * staticMember
	 * : ID_list '::' ID
	 * | ID_list '::' ID '(' exprList? ')'
	 */
	Expr staticMember() {
		if (peek().sym != Sym.ID) {
			return sharedMethod();
		}
		
		int begin = forward;
		while (move().sym == Sym.DOT && move().sym == Sym.ID) {
			;
		}
		// ID.ID.ID::
		if (peek().sym != Sym.STATIC || tokenList.get(forward - 1).sym != Sym.ID) {
			resetForward(begin);
			return sharedMethod();
		}
		
		String clazz = getClazz(begin);
		match(Sym.STATIC);
		String memberName = match(Sym.ID).value();
		
		// com.jfinal.kit.Str::isBlank(str)
		if (peek().sym == Sym.LPAREN) {
			move();
			if (peek().sym == Sym.RPAREN) {
				move();
				return new StaticMethod(clazz, memberName, location);
			}
			
			ExprList exprList = (ExprList)exprList();
			match(Sym.RPAREN);
			return new StaticMethod(clazz, memberName, exprList, location);
		}
		
		// com.jfinal.core.Const::JFINAL_VERSION
		return new StaticField(clazz, memberName, location);
	}
	
	String getClazz(int begin) {
		StringBuilder clazz = new StringBuilder();
		for (int i=begin; i<forward; i++) {
			clazz.append(tokenList.get(i).value());
		}
		return clazz.toString();
	}
	
	/**
	 * sharedMethod : ID '(' exprList? ')'
	 */
	Expr sharedMethod() {
		Tok tok = peek();
		if (tok.sym != Sym.ID) {
			return indexMethodField(null);
		}
		if (move().sym != Sym.LPAREN) {
			resetForward(forward - 1);
			return indexMethodField(null);
		}
		
		move();
		if (peek().sym == Sym.RPAREN) {
			SharedMethod sharedMethod = new SharedMethod(engineConfig.getSharedMethodKit(), tok.value(), ExprList.NULL_EXPR_LIST, location);
			move();
			return indexMethodField(sharedMethod);
		}
		
		ExprList exprList = (ExprList)exprList();
		SharedMethod sharedMethod = new SharedMethod(engineConfig.getSharedMethodKit(), tok.value(), exprList, location);
		match(Sym.RPAREN);
		return indexMethodField(sharedMethod);
	}
	
	/**
	 * index : expr '[' expr ']'
	 * method : expr '.' ID '(' exprList? ')'
	 * field :  expr '.' ID
	 */
	Expr indexMethodField(Expr expr) {
		if (expr == null) {
			expr = map();
		}
		
		// Expr expr = map();
		while (true) {
			Tok tok = peek();
			// expr [ expr ]
			if (tok.sym == Sym.LBRACK) {
				move();
				Expr index = expr();
				match(Sym.RBRACK);
				expr = new Index(expr, index, location);
				continue;
			}
			if (tok.sym != Sym.DOT) {
				return expr;
			}
			if ((tok = move()).sym != Sym.ID) {
				resetForward(forward - 1);
				return expr;
			}
			
			move();
			if (peek().sym != Sym.LPAREN) {
				expr = new Field(expr, tok.value(), location);
				continue;
			}
			
			move();
			// expr '.' ID '(' ')'
			if (peek().sym == Sym.RPAREN) {
				move();
				expr = new Method(expr, tok.value(), location);
				continue;
			}
			
			// expr '.' ID '(' exprList ')'
			ExprList exprList = (ExprList)exprList();
			match(Sym.RPAREN);
			expr = new Method(expr, tok.value(), exprList, location);
		}
	}
	
	/**
	 * map : '{' (mapEntry ( , mapEntry ) * ) ? '}'
	 * mapEntry : (ID | STR) ':' expr
	 */
	Expr map() {
		if (peek().sym != Sym.LBRACE) {
			return array();
		}
		
		LinkedHashMap<Object, Expr> mapEntry = new LinkedHashMap<Object, Expr>();
		Map map = new Map(mapEntry);
		move();
		if (peek().sym == Sym.RBRACE) {
			move();
			return map;
		}
		
		buildMapEntry(mapEntry);
		while (peek().sym == Sym.COMMA) {
			move();
			buildMapEntry(mapEntry);
		}
		match(Sym.RBRACE);
		return map;
	}
	
	/**
	 * mapEntry : (ID | STR) ':' expr
	 */
	void buildMapEntry(LinkedHashMap<Object, Expr> map) {
		Tok tok = peek();
		if (tok.sym == Sym.ID || tok.sym == Sym.STR) {
			move();
			match(Sym.COLON);
			Expr value = expr();
			if (value == null) {
				throw new ParseException("Expression error: the value on the right side of map entry can not be blank", location);
			}
			map.put(tok.value(), value);
			return ;
		}
		throw new ParseException("Expression error: the value of map key must be identifier or String", location);
	}
	
	/**
	 * array : '[' exprList ? | range ? ']'
	 * exprList : expr (',' expr)*
	 * range : expr .. expr
	 */
	Expr array() {
		if (peek().sym != Sym.LBRACK) {
			return atom();
		}
		
		move();
		if (peek().sym == Sym.RBRACK) {
			move();
			return new Array(ExprList.NULL_EXPR_ARRAY, location);
		}
		ExprList exprList = (ExprList)exprList();
		if (exprList.length() == 1 && peek().sym == Sym.RANGE) {
			move();
			Expr end = expr();
			match(Sym.RBRACK);
			return new RangeArray(exprList.getExprArray()[0], end, location);
		}
		
		match(Sym.RBRACK);
		return new Array(exprList.getExprArray(), location);
	}
	
	/**
	 * atom : '(' expr ')' | ID | STR | 'true' | 'false' | 'null'
	 * 		| INT | LONG | FLOAT | DOUBLE
	 */
	Expr atom() {
		Tok tok = peek();
		switch (tok.sym) {
		case LPAREN:
			move();
			Expr expr = expr();
			match(Sym.RPAREN);
			return expr;
		case ID:
			move();
			return new Id(tok.value());
		case STR:
		case INT:
		case LONG:
		case FLOAT:
		case DOUBLE:
			move();
			return new Const(tok.sym, tok.value());
		case TRUE:
			move();
			return Const.TRUE;
		case FALSE:
			move();
			return Const.FALSE;
		case NULL:
			move();
			return Const.NULL;
		case COMMA:
		case SEMICOLON:
		case QUESTION:	// support "c ?? ? a : b"
		case AND: case OR: case EQUAL: case NOTEQUAL:	// support "a.b ?? && expr"
		case RPAREN:	// support "(a.b ??)"
		case RBRACK:	// support "[start .. end ??]"
		case RBRACE:	// support "{key : value ??}"
		case RANGE:		// support "[start ?? .. end]"
		case EOF:
			return null;
		default :
			throw new ParseException("Expression error: can not match the symbol \"" + tok.value() + "\"", location);
		}
	}
	
	/**
	 * forControl : ID : expr | exprList? ';' expr? ';' exprList?
	 */
	Expr forCtrl() {
		ExprList exprList = (ExprList)exprList();
		if (peek().sym == Sym.SEMICOLON) {
			move();
			Expr cond = expr();
			match(Sym.SEMICOLON);
			Expr update = exprList();
			return new ForCtrl(exprList, cond, update, location);
		}
		
		if (exprList.length() == 1) {
			Expr expr = exprList.getExprArray()[0];
			if (expr instanceof Id) {
				match(Sym.COLON);
				return new ForCtrl(((Id)expr), expr(), location);
			}
		}
		throw new ParseException("The expression of #for directive is error", location);
	}
}






