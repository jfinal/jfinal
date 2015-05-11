/**
 * 
 */
package com.jfinal.ext.kit;

/**
 * @author 朱丛启  2015年4月29日 下午3:14:21
 *
 */
public class PageViewKit {

	/**
	 * 工程的WEB-INF目录
	 */
	public static final String WEBINF_DIR = "/WEB-INF/";
	
	/**
	 * 工程的根目录
	 */
	public static final String ROOT_DIR = "/";
	
	/**
	 * jsp文件后缀
	 */
	public static final String JSP = ".jsp";
	
	/**
	 * html 文件后缀
	 */
	public static final String HTML = ".html";
	
	private static final String ERROR_PAGE_PATH = "errorpages/";
	private static final String PAGE_VIEW_PATH = "pageviews/";
	
	/**
	 * 404 Error PageView
	 * @return
	 */
	public static String get404PageView(){
		return getErrorCodePageView("404");
	}
	
	/**
	 * 403 Error PageView
	 * @return
	 */
	public static String get403PageView(){
		return getErrorCodePageView("403");
	}
	
	/**
	 * 500 Error PageView
	 * @return
	 */
	public static String get500PageView(){
		return getErrorCodePageView("500");
	}

	private static String getErrorCodePageView(String errorCode){
		return getPageView(WEBINF_DIR,ERROR_PAGE_PATH,errorCode,HTML);
	}
	
	/**
	 * 获取web-inf下面的页面
	 * @param pageName 页面名称
	 * @return
	 */
	public static String getJSPPageViewFromWebInf(String pageName){
		return getPageView(WEBINF_DIR, PAGE_VIEW_PATH, pageName, JSP);
	}
	
	/**
	 * 获取web-inf下面的pageviews目录中pathRefRootViews子目录下面的页面
	 * @param pathRefRootViews 目录: 加入到/WEB-INF/pageviews/pathRefRootViews下
	 * @param pageName 页面名称
	 * @return
	 */
	public static String getJSPPageViewFromWebInf(String pathRefRootViews,String pageName){
		return getPageView(WEBINF_DIR, PAGE_VIEW_PATH+pathRefRootViews, pageName, JSP);
	}
	
	/**
	 * 获取根目录下面的页面
	 * @param pageName 页面名称
	 * @return
	 */
	public static String getJSPPageViewFromRoot(String pageName){
		return getPageView(ROOT_DIR, PAGE_VIEW_PATH, pageName, JSP);
	}
	
	/**
	 * 获取根目录下面的pageviews目录中pathRefRootViews子目录下面的页面
	 * @param pathRefRootViews 目录: 加入到/pageviews/pathRefRootViews下
	 * @param pageName 页面名称
	 * @return
	 */
	public static String getJSPPageViewFromRoot(String pathRefRootViews,String pageName){
		return getPageView(ROOT_DIR, PAGE_VIEW_PATH+pathRefRootViews, pageName, JSP);
	}
	
	/**
	 * 获取web-inf下面的静态页面
	 * @param pageName 页面名称
	 * @return
	 */
	public static String getHTMLPageViewFromWebInf(String pageName){
		return getPageView(WEBINF_DIR, PAGE_VIEW_PATH, pageName, HTML);
	}
	
	/**
	 * 获取web-inf下面的pageviews目录中pathRefRootViews子目录下面的静态页面
	 * @param pathRefRootViews 目录: 加入到/WEB-INF/pageviews/pathRefRootViews下
	 * @param pageName 页面名称
	 * @return
	 */
	public static String getHTMLPageViewFromWebInf(String pathRefRootViews, String pageName){
		return getPageView(WEBINF_DIR, PAGE_VIEW_PATH+pathRefRootViews, pageName, HTML);
	}
	
	/**
	 * 获取根目录下面的静态页面
	 * @param pageName 页面名称
	 * @return
	 */
	public static String getHTMLPageViewFromRoot(String pageName){
		return getHTMLPageView(ROOT_DIR,PAGE_VIEW_PATH, pageName);
	}
	
	/**
	 * 获取根目录下的pageviews目录中pathRefRootViews子目录下面的静态页面
	 * @param pathRefRootViews 目录: 加入到/pageviews/pathRefRootViews下
	 * @param pageName 页面名称
	 * @return
	 */
	public static String getHTMLPageViewFromRoot(String pathRefRootViews, String pageName){
		return getHTMLPageView(ROOT_DIR,PAGE_VIEW_PATH+pathRefRootViews, pageName);
	}


	/**
	 * 获取静态页面
	 * @param dir 所在目录
	 * @param viewPath view路径
	 * @param pageName view名字
	 * @return
	 */
	public static String getHTMLPageView(String dir, String viewPath, String pageName){
		return getPageView(dir, viewPath, pageName, HTML);
	}
	
	/**
	 * 获取页面
	 * @param dir 所在目录
	 * @param viewPath view路径
	 * @param pageName view名字
	 * @return
	 */
	public static String getJSPPageView(String dir, String viewPath, String pageName){
		return getPageView(dir, viewPath, pageName, JSP);
	}
	
	/**
	 * 获取页面
	 * @param dir 所在目录
	 * @param viewPath view路径
	 * @param pageName view名字
	 * @param fileExtension view后缀
	 * @return
	 */
	public static String getPageView(String dir, String viewPath, String pageName, String fileExtension){
		if (!dir.endsWith("/")) {
			dir = dir + "/";
		}
		if (!viewPath.startsWith("/")) {
			viewPath = "/" + viewPath;
		}
		if (!viewPath.endsWith("/")) {
			viewPath = viewPath + "/";
		}
		return dir+viewPath+pageName+fileExtension;
	}
	
}

