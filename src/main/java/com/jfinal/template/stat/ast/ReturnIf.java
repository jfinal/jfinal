package com.jfinal.template.stat.ast;

import com.jfinal.template.Env;
import com.jfinal.template.expr.ast.Expr;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.expr.ast.Logic;
import com.jfinal.template.io.Writer;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * #returnIf(expr) 指令，当 expr 为 true 时返回，等价于：
 *     #if (expr)
 *         #return
 *     #end
 */
public class ReturnIf extends Stat {

    final Expr expr;

    public ReturnIf(ExprList exprList, Location location) {
        if (exprList.length() == 0) {
            throw new ParseException("The parameter of #returnIf directive can not be blank", location);
        }
        this.expr = exprList.getActualExpr();
    }

    @Override
    public void exec(Env env, Scope scope, Writer writer) {
        if (Logic.isTrue(expr.eval(scope))) {
            scope.getCtrl().setReturn();
        }
    }
}
