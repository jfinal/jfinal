package com.jfinal.template.ext.directive;

import com.jfinal.template.Directive;
import com.jfinal.template.Env;
import com.jfinal.template.expr.ast.Expr;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.ext.sharedmethod.DayCalDateLib;
import com.jfinal.template.io.Writer;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

import java.util.Date;

/**
 * #day 日期格式计算,默认返回日期格式 yyyyMMdd
 * 对 {@link DateDirective} 扩展
 * 四种用法：
 * 1：#day(createAt,'-2M', 'YYYYMMED') 根据createAt，按规则计算日期
 * 2：#day(createAt) 用默认 datePattern 配置，输出 createAt 变量中的日期值
 * 3：#day(createAt, "yyyy-MM-dd") 用第二个参数指定的 datePattern，输出 createAt 变量中的日期值
 * 4：#day() 用默认 datePattern 配置，输出 “当前” 日期值
 * 注意：
 * 1：#day 指令中的参数可以是变量，例如：#day(d, p) 中的 d 与 p 可以全都是变量
 * 2：默认 datePattern 可通过 Engine.setDatePattern(...) 进行配置
 * 3：jfinal 4.9.02 版新增支持 java 8 的 LocalDateTime、LocalDate、LocalTime
 *
 * @author lanvendar
 * @version 1.0.0 ,2022/01/25
 * @date 2022/01/25
 */
public class DayCalDirective extends Directive {
    
    /**
     * 日期参数.
     */
    private Expr dateExpr;
    
    /**
     * 规则参数.
     * {@link DayCalDateLib}
     */
    private Expr ruleExpr;
    
    /**
     * 日期格式.
     * {@link DayCalDateLib}
     */
    private Expr patternExpr;
    
    /**
     * 校验表达式.
     *
     * @param exprList 表达式
     */
    @Override
    public void setExprList(ExprList exprList) {
        int paraNum = exprList.length();
        if (paraNum == 0) {
            dateExpr = null;
            ruleExpr = null;
            patternExpr = null;
        } else if (paraNum == 1) {
            dateExpr = exprList.getExpr(0);
            ruleExpr = null;
            patternExpr = null;
        } else if (paraNum == 2) {
            dateExpr = exprList.getExpr(0);
            ruleExpr = null;
            patternExpr = exprList.getExpr(1);
        } else if (paraNum == 3) {
            dateExpr = exprList.getExpr(0);
            ruleExpr = exprList.getExpr(1);
            patternExpr = exprList.getExpr(2);
        } else {
            throw new ParseException("Wrong number parameter of #day directive, two parameters allowed at most",
                    location);
        }
    }
    
    @Override
    public void exec(Env env, Scope scope, Writer writer) {
        String date;
        String writeDate;
        if (dateExpr == null) {
            date = DayCalDateLib.format(new Date(), DayCalDateLib.FORMAT_STDAY);
            date = DayCalDateLib.formatPattern(date);
        } else {
            date = dateExpr.eval(scope).toString();
            date = DayCalDateLib.formatPattern(date);
        }
        writeDate = date;
        if (ruleExpr == null && patternExpr != null) {
            String calRule =
                    date + DayCalDateLib.COMMA + "0" + DayCalDateLib.COMMA + patternExpr.eval(scope).toString();
            writeDate = DayCalDateLib.dayCalculateFormatter(calRule, date);
        }
        if (ruleExpr != null && patternExpr != null) {
            String calRule = date + DayCalDateLib.COMMA + ruleExpr.eval(scope).toString() + DayCalDateLib.COMMA
                    + patternExpr.eval(scope).toString();
            writeDate = DayCalDateLib.dayCalculateFormatter(calRule, date);
        }
        //写入模板
        write(writer, writeDate);
    }
}
