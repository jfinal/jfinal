package com.jfinal.plugin.druid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.alibaba.druid.stat.DruidStatService;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.util.IPRange;
import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alibaba.druid.util.IOUtils;
import com.jfinal.handler.Handler;
import com.jfinal.util.HandlerKit;

/**
 * 替代 StatViewServlet
 */
@SuppressWarnings("unused")
public class DruidStatViewHandler extends Handler {
	
	private IDruidStatViewAuth auth;
	private String visitPath = "/druid";
	private StatViewServlet servlet = new JFinalStatViewServlet();
	
	public DruidStatViewHandler(String visitPath) {
		this.visitPath = visitPath;
		this.auth = new IDruidStatViewAuth(){
			public boolean isPermitted(HttpServletRequest request) {
				return true;
			}};
	}
	
	public DruidStatViewHandler(String visitPath , IDruidStatViewAuth druidStatViewAuth) {
		this.visitPath = visitPath;
		this.auth = druidStatViewAuth;
	}
	
	public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
		if (target.startsWith(visitPath)) {
			isHandled[0] = true;
			
			if (target.equals(visitPath) && !target.endsWith("/index.html")) {
				HandlerKit.redirect(target += "/index.html", request, response, isHandled);
				return ;
			}
			
			try {
				servlet.service(request, response);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		else {
			nextHandler.handle(target, request, response, isHandled);
		}
	}
	
	@SuppressWarnings("serial")
	class JFinalStatViewServlet extends StatViewServlet {
		public boolean isPermittedRequest(HttpServletRequest request) {
			return auth.isPermitted(request);
		}
		
		public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	        HttpSession session = request.getSession();
	        String contextPath = request.getContextPath();
	        // String servletPath = request.getServletPath();
	        String requestURI = request.getRequestURI();

	        response.setCharacterEncoding("utf-8");

	        if (contextPath == null) { // root context
	            contextPath = "";
	        }
	        // String uri = contextPath + servletPath;
	        // String path = requestURI.substring(contextPath.length() + servletPath.length());
	        int index = contextPath.length() + visitPath.length();
	        String uri = requestURI.substring(0, index);
	        String path = requestURI.substring(index);

	        if (!isPermittedRequest(request)) {
	            path = "/nopermit.html";
	            returnResourceFile(path, uri, response);
	            return;
	        }

	        if ("/submitLogin".equals(path)) {
	            String usernameParam = request.getParameter(PARAM_NAME_USERNAME);
	            String passwordParam = request.getParameter(PARAM_NAME_PASSWORD);
	            if (username.equals(usernameParam) && password.equals(passwordParam)) {
	                request.getSession().setAttribute(SESSION_USER_KEY, username);
	                response.getWriter().print("success");
	            } else {
	                response.getWriter().print("error");
	            }
	            return;
	        }

	        if (isRequireAuth()
	            && session.getAttribute(SESSION_USER_KEY) == null
	            && !("/login.html".equals(path) || path.startsWith("/css") || path.startsWith("/js") || path.startsWith("/img"))) {
	            if (contextPath == null || contextPath.equals("") || contextPath.equals("/")) {
	                response.sendRedirect("/login.html");
	            } else {
	                response.sendRedirect("login.html");
	            }
	            return;
	        }

	        if ("".equals(path)) {
	            if (contextPath == null || contextPath.equals("") || contextPath.equals("/")) {
	                response.sendRedirect("/druid/index.html");
	            } else {
	                response.sendRedirect("druid/index.html");
	            }
	            return;
	        }

	        if ("/".equals(path)) {
	            response.sendRedirect("index.html");
	            return;
	        }

	        if (path.indexOf(".json") >= 0) {
	            String fullUrl = path;
	            if (request.getQueryString() != null && request.getQueryString().length() > 0) {
	                fullUrl += "?" + request.getQueryString();
	            }
	            response.getWriter().print(statService.service(fullUrl));
	            return;
	        }

	        // find file in resources path
	        returnResourceFile(path, uri, response);
	    }
		
		private void returnResourceFile(String fileName, String uri,
				HttpServletResponse response) throws ServletException,
				IOException {
			if (fileName.endsWith(".jpg")) {
				byte[] bytes = IOUtils.readByteArrayFromResource(RESOURCE_PATH
						+ fileName);
				if (bytes != null) {
					response.getOutputStream().write(bytes);
				}

				return;
			}

			String text = IOUtils.readFromResource(RESOURCE_PATH + fileName);
			if (text == null) {
				response.sendRedirect(uri + "/index.html");
				return;
			}
			if (fileName.endsWith(".css")) {
				response.setContentType("text/css;charset=utf-8");
			} else if (fileName.endsWith(".js")) {
				response.setContentType("text/javascript;charset=utf-8");
			}
			response.getWriter().write(text);
		}
	}
	
    private final static Log    LOG                         = LogFactory.getLog(StatViewServlet.class);

    private static final long   serialVersionUID            = 1L;

    public static final String  PARAM_NAME_RESET_ENABLE     = "resetEnable";
    public static final String  PARAM_NAME_ALLOW            = "allow";
    public static final String  PARAM_NAME_DENY             = "deny";

    public static final String  PARAM_NAME_USERNAME         = "loginUsername";
    public static final String  PARAM_NAME_PASSWORD         = "loginPassword";

    public static final String  SESSION_USER_KEY            = "druid-user";

    private final static String RESOURCE_PATH               = "support/http/resources";
    private final static String TEMPLATE_PAGE_RESOURCE_PATH = RESOURCE_PATH + "/template.html";

    private DruidStatService    statService                 = DruidStatService.getInstance();

    public String               templatePage;

    private List<IPRange>       allowList                   = new ArrayList<IPRange>();
    private List<IPRange>       denyList                    = new ArrayList<IPRange>();

    private String              username                    = null;
    private String              password                    = null;
}




