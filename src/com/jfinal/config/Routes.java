/**
 * Copyright (c) 2011-2016, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.jfinal.annotation.Ctrl;
import com.jfinal.core.Controller;
import com.jfinal.kit.RoutesLoadKit;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;

/**
 * Routes.
 */
public abstract class Routes {
	
	private static final Log log = Log.getLog(Routes.class);
	
	private static String baseViewPath;
	
	private Map<String, Class<? extends Controller>> map = new HashMap<String, Class<? extends Controller>>();
	private Map<String, String> viewPathMap = new HashMap<String, String>();
	
	/**
	 * you must implement config method and use add method to config route
	 */
	public abstract void config();
	
	public Routes add(Routes routes) {
		if (routes != null) {
			routes.config();	// very important!!!
			
			for (Entry<String, Class<? extends Controller>> e : routes.map.entrySet()) {
				String controllerKey = e.getKey();
				if (this.map.containsKey(controllerKey)) {
					throw new IllegalArgumentException("The controllerKey already exists: " + controllerKey); 
				}
				
				this.map.put(controllerKey, e.getValue());
				this.viewPathMap.put(controllerKey, routes.getViewPath(controllerKey));
			}
		}
		return this;
	}
	
	/**
	 * Add route
	 * @param controllerKey A key can find controller
	 * @param controllerClass Controller Class
	 * @param viewPath View path for this Controller
	 */
	public Routes add(String controllerKey, Class<? extends Controller> controllerClass, String viewPath) {
		if (controllerKey == null)
			throw new IllegalArgumentException("The controllerKey can not be null");
		// if (controllerKey.indexOf(".") != -1)
			// throw new IllegalArgumentException("The controllerKey can not contain dot character: \".\"");
		controllerKey = controllerKey.trim();
		if ("".equals(controllerKey))
			throw new IllegalArgumentException("The controllerKey can not be blank");
		if (controllerClass == null)
			throw new IllegalArgumentException("The controllerClass can not be null");
		if (!controllerKey.startsWith("/"))
			controllerKey = "/" + controllerKey;
		if (map.containsKey(controllerKey))
			throw new IllegalArgumentException("The controllerKey already exists: " + controllerKey);
		
		map.put(controllerKey, controllerClass);
		
		if (viewPath == null || "".equals(viewPath.trim()))	// view path is controllerKey by default
			viewPath = controllerKey;
		
		viewPath = viewPath.trim();
		if (!viewPath.startsWith("/"))					// "/" added to prefix
			viewPath = "/" + viewPath;
		
		if (!viewPath.endsWith("/"))					// "/" added to postfix
			viewPath = viewPath + "/";
		
		if (baseViewPath != null)						// support baseViewPath
			viewPath = baseViewPath + viewPath;
		
		viewPathMap.put(controllerKey, viewPath);
		return this;
	}
	
	/**
	 * Add url mapping to controller. The view path is controllerKey
	 * @param controllerKey A key can find controller
	 * @param controllerClass Controller Class
	 */
	public Routes add(String controllerKey, Class<? extends Controller> controllerClass) {
		return add(controllerKey, controllerClass, controllerKey);
	}
	
	public Set<Entry<String, Class<? extends Controller>>> getEntrySet() {
		return map.entrySet();
	}
	
	public String getViewPath(String key) {
		return viewPathMap.get(key);
	}
	
	/**
	 * Set the base path for all views
	 */
	static void setBaseViewPath(String baseViewPath) {
		if (baseViewPath == null)
			throw new IllegalArgumentException("The baseViewPath can not be null");
		baseViewPath = baseViewPath.trim();
		if ("".equals(baseViewPath))
			throw new IllegalArgumentException("The baseViewPath can not be blank");
		
		if (! baseViewPath.startsWith("/"))			// add prefix "/"
			baseViewPath = "/" + baseViewPath;
		
		if (baseViewPath.endsWith("/"))				// remove "/" in the end of baseViewPath
			baseViewPath = baseViewPath.substring(0, baseViewPath.length() - 1);
		
		Routes.baseViewPath = baseViewPath;
	}
	
	public void clear() {
		map.clear();
		viewPathMap.clear();
		
		map = null;
		viewPathMap = null;
	}
	
	/**
	 * 读取属性配置，扫描指定jar包里的package和指定classes下的package
	 * <pre>
	 *  //配置文件
	 * autoRoutes.paks = com.jfinal.controller1[,com.jfinal.controller2...]
	 * </pre>
	 * 
	 * <pre>
	 *  //JfinalConfig
	 * public void configRoute(Routes me) {
	 *    String paks = PropKit.get("autoRoutes.paks");
	 *    me.autoRoutes(paks);
	 * }
	 * </pre>
	 * @param paks
	 * @param jars
	 */
	public void autoRoutes(String paks){
		List<String> paks_list = new ArrayList<String>();
		if(StrKit.notBlank(paks)){
			paks_list = Arrays.asList(paks.split(","));
		}
		if(null == paks_list || paks_list.size() <= 0){
			throw new RuntimeException("配置包路由为空，自动扫描路由，需要指定包路径！【jar包里的包路径一样可以】");
		}
		autoRoutes(paks_list);
	}
	
	/**
	 * <ol>扫描指定jar包，指定package
	 * <li> 全部classes文件下的指定package下的class文件
	 * <li> 全部WEB-INF/lib下的指定jar文件
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
    public void autoRoutes(List<String> paks){
		//初始化扫描路径
		RoutesLoadKit.setScanPkgList(paks);
		// 查询所有继承BaseController的子类
		Set<Class<?>> controllerClasses = RoutesLoadKit.searchByClassLoader(Controller.class);
		// 循环处理自动注册映射
		for (Class controller : controllerClasses) {
			// 获取注解对象
			Ctrl controllerBind = (Ctrl) controller.getAnnotation(Ctrl.class);
			if (controllerBind == null) {
				log.warn("{}继承了Controller，但是没有注解绑定映射路径!",controller.getName());
				continue;
			}
			// 获取映射路径数组
			String[] controllerKeys = controllerBind.value();
			String viewPath = controllerBind.viewPath();
			for (String controllerKey : controllerKeys) {
				controllerKey.trim();
				if (controllerKey.equals("")) {
					throw new RuntimeException(controller.getName() + "注解错误，映射路径为空");
				}
				// 注册映射
				if(StrKit.notBlank(viewPath)){
					viewPath = viewPath.trim();
					if(viewPath.isEmpty()){
						add(controllerKey, controller, viewPath);
					}
				}else{
					add(controllerKey, controller);
				}
				log.error("Controller注册： controller = " + controller + ", " + controllerKey);
			}
		}
	}
}






