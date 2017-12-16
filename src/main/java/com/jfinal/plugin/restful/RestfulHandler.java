package com.jfinal.plugin.restful;

import com.jfinal.core.Action;
import com.jfinal.core.JFinal;
import com.jfinal.handler.Handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.TreeMap;

/**
 * 拦截所有匹配routes的请求，根据target及method匹配Action后，重设target使ActionHandler可正确匹配该Action。
 * 尝试用种优雅的方式让JFinal支持多参数Restful Url，减少对JFinal核心逻辑的侵入或破坏。
 * 需特别按照多参数Restful Url处理时，使用@ActionKey("@GET/v1/company/:companyId/staff/:staffId")形式注解
 */
public class RestfulHandler extends Handler {
    private TreeMap<String, Action> actionTreeMap;
    private boolean initialized;

    public void init() {
        actionTreeMap = new TreeMap<String, Action>(new RestfulKeyComparator());
        JFinal jf = JFinal.me();
        List<String> actionKeys = jf.getAllActionKeys();
        String[] empty = new String[]{null};
        for (String k : actionKeys) {
            if (!k.startsWith("@")) {
                continue;
            }
            actionTreeMap.put(k, jf.getAction(k, empty));
        }
        initialized = true;
    }

    @Override
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
        if (!initialized) {
            init();
        }

        String actionKey = matchActionKey(target, request.getMethod());

        if (actionKey != null) {
            parseUrlPara(target, actionKey, request);
            target = actionKey;
        }

        next.handle(target, request, response, isHandled);
    }

    private String matchActionKey(String url, String method) {
        Action action = actionTreeMap.get("@" + method + url);// @GET/v1/company/:companyId/staff/:staffId
        if (action == null) {
            return null;
        }
        return action.getActionKey();
    }

    private void parseUrlPara(String target, String actionKey, HttpServletRequest request) {
        String[] targetSegments = target.split("/");
        String[] actionKeySegments = actionKey.split("/");

        for (int i = 0, j = targetSegments.length; i < j; i++) {
            if (!actionKeySegments[i].startsWith(":")) {
                continue;
            }

            String name = actionKeySegments[i].substring(1);

            request.setAttribute(name, targetSegments[i]);
        }
    }
}
