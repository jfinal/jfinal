package com.jfinal.core;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.paragetter.JsonRequest;
import javassist.ClassPool;
import javassist.CtMethod;
import javassist.NotFoundException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;

/**
 * LineNumberActionReporter
 * 输出真实行号
 * 建议开发模式下使用(有性能损耗)
 * @author 山东小木
 */
public class LineNumberActionReporter extends ActionReporter {
    @Override
    public void report(String target, Controller controller, Action action) {
        CtMethod ctMethod = null;
        int lineNumber = 1;
        try {
            ctMethod = ClassPool.getDefault().getMethod(controller.getClass().getName(), action.getMethodName());
            lineNumber = ctMethod.getMethodInfo().getLineNumber(0);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder(title).append(sdf.get().format(new Date())).append(" --------------------------\n");
        sb.append("Url         : ").append(controller.getRequest().getMethod()).append(' ').append(target).append('\n');
        Class<? extends Controller> cc = action.getControllerClass();
        sb.append("Controller  : ").append(cc.getName()).append(".(").append(cc.getSimpleName()).append(".java:").append(lineNumber).append(")\n");
        sb.append("Method      : ").append(action.getMethodName()).append('\n');

        String urlParas = controller.getPara();
        if (urlParas != null) {
            sb.append("UrlPara     : ").append(urlParas).append('\n');
        }

        Interceptor[] inters = action.getInterceptors();
        if (inters.length > 0) {
            sb.append("Interceptor : ");
            lineNumber = 1;
            ctMethod = null;
            Class<? extends Interceptor> ic;
            for (int i=0; i<inters.length; i++) {
                lineNumber = 1;
                ctMethod = null;
                if (i > 0) {
                    sb.append("\n              ");
                }
                ic = inters[i].getClass();
                try {
                    ctMethod = ClassPool.getDefault().getMethod(ic.getName(), "intercept");
                    lineNumber = ctMethod.getMethodInfo().getLineNumber(0);
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }
                sb.append(ic.getName()).append(".(").append(ic.getSimpleName()).append(".java:").append(lineNumber).append(')');
            }
            sb.append('\n');
        }

        // print all parameters
        HttpServletRequest request = controller.getRequest();
        if (request instanceof JsonRequest) {
            buildJsonPara(controller, sb);
        } else {
            buildPara(controller, sb);
        }

        sb.append("--------------------------------------------------------------------------------\n");

        try {
            writer.write(sb.toString());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}


