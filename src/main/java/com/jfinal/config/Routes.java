/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
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
import java.util.List;
import java.util.function.Predicate;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.InterceptorManager;
import com.jfinal.core.Controller;
import com.jfinal.core.PathScanner;
import com.jfinal.kit.StrKit;

/**
 * Routes.
 */
public abstract class Routes {
	
	private static List<Routes> routesList = new ArrayList<Routes>();
	
	static final boolean DEFAULT_MAPPING_SUPER_CLASS = false;	// 是否映射超类中的方法为路由的默认值
	Boolean mappingSuperClass = null;							// 是否映射超类中的方法为路由
	
	private String baseViewPath = null;
	private List<Route> routeItemList = new ArrayList<Route>();
	private List<Interceptor> interList = new ArrayList<Interceptor>();
	
	private boolean clearAfterMapping = false;
	
	/**
	 * Implement this method to add route, add interceptor and set baseViewPath
	 */
	public abstract void config();
	
	/**
	 * 设置是否映射超类中的方法为路由，默认值为 false
	 * 
	 * 以免 BaseController extends Controller 用法中的 BaseController 中的方法被映射成 action
	 */
	public Routes setMappingSuperClass(boolean mappingSuperClass) {
		this.mappingSuperClass = mappingSuperClass;
		return this;
	}
	
	public boolean getMappingSuperClass() {
		return mappingSuperClass != null ? mappingSuperClass : DEFAULT_MAPPING_SUPER_CLASS;
	}
	
	/**
	 * 扫描路由
	 * 
	 * <pre>
	   1：路由不拆分例子：
	     routes.setBaseViewPath("/_view");
	     routes.scan("com.jfinal.club.");
	   
	   2：前后台路由拆分例子（例子来源于俱乐部项目源码 jfinal-club）：
		// 扫描后台路由
		me.add(new Routes() {
			public void config() {
				// 添加后台管理拦截器，将拦截在此方法中注册的所有 Controller
				this.addInterceptor(new AdminAuthInterceptor());
				this.addInterceptor(new PjaxInterceptor());
				
				this.setBaseViewPath("/_view/_admin");
				
				// 如果被扫描的包在 jar 文件之中，需要添加如下配置：
				// undertow.hotSwapClassPrefix = com.jfinal.club._admin.
				this.scan("com.jfinal.club._admin.");
			}
		});
		
		
		// 扫描前台路由
		me.add(new Routes() {
			public void config() {
				this.setBaseViewPath("/_view");
				
				// 如果被扫描的包在 jar 文件之中，需要添加如下配置：
				// undertow.hotSwapClassPrefix = com.jfinal.club.
				this.scan("com.jfinal.club.", className -> {
					// className 为当前正扫描的类名，返回 true 时表示跳过当前类不扫描
					return className.startsWith("com.jfinal.club._admin.");
				});
			}
		});
		
		注意：
		1：拆分路由是为了可以独立配置 setBaseViewPath(...)、addInterceptor(...)
		2：scan(...) 方法要添加 skip 参数，跳过后台路由，否则后台路由会被扫描到，
		   造成 baseViewPath 以及 routes 级别的拦截器配置错误
		3: 由于 scan(...) 内部避免了重复扫描同一个类，所以需要将扫描前台路由代码
		   放在扫描后台路由之前才能验证没有 skip 参数造成的后果
		
	 * </pre>
	 * 
	 * @param basePackage 进行扫描的基础 package，仅扫描该包及其子包下面的路由
	 * @param skip 跳过不需要被扫描的类
	 */
	public Routes scan(String basePackage, Predicate<String> skip) {
		new PathScanner(basePackage, this, skip).scan();
		return this;
	}
	
	/**
	 * 扫描路由
	 * @param basePackage 进行扫描的基础 package，仅扫描该包及其子包下面的路由
	 */
	public Routes scan(String basePackage) {
		return scan(basePackage, null);
	}
	
	/**
	 * Add Routes
	 */
	public Routes add(Routes routes) {
		routes.config();
		
		/**
		 * 如果子 Routes 没有配置 mappingSuperClass，则使用顶层 Routes 的配置
		 * 主要是为了让 jfinal weixin 用户有更好的体验
		 * 
		 * 因为顶层 Routes 和模块级 Routes 配置都可以生效，减少学习成本
		 */
		if (routes.mappingSuperClass == null) {
			routes.mappingSuperClass = this.mappingSuperClass;
		}
		
		routesList.add(routes);
		return this;
	}
	
	/**
	 * Add route
	 * @param controllerPath path of controller
	 * @param controllerClass Controller Class
	 * @param viewPath View path for this Controller
	 */
	public Routes add(String controllerPath, Class<? extends Controller> controllerClass, String viewPath) {
		routeItemList.add(new Route(controllerPath, controllerClass, viewPath));
		return this;
	}
	
	/**
	 * Add route. The viewPath is controllerPath
	 * @param controllerPath path of controller
	 * @param controllerClass Controller Class
	 */
	public Routes add(String controllerPath, Class<? extends Controller> controllerClass) {
		return add(controllerPath, controllerClass, controllerPath);
	}
	
	/**
	 * Add interceptor for controller in this Routes
	 */
	public Routes addInterceptor(Interceptor interceptor) {
		if (com.jfinal.aop.AopManager.me().isInjectDependency()) {
			com.jfinal.aop.Aop.inject(interceptor);
		}
		interList.add(interceptor);
		return this;
	}
	
	/**
	 * Set base view path for controller in this routes
	 */
	public Routes setBaseViewPath(String baseViewPath) {
		if (StrKit.isBlank(baseViewPath)) {
			throw new IllegalArgumentException("baseViewPath can not be blank");
		}
		
		baseViewPath = baseViewPath.trim();
		if (! baseViewPath.startsWith("/")) {			// add prefix "/"
			baseViewPath = "/" + baseViewPath;
		}
		if (baseViewPath.endsWith("/")) {				// remove "/" in the end of baseViewPath
			baseViewPath = baseViewPath.substring(0, baseViewPath.length() - 1);
		}
		
		this.baseViewPath = baseViewPath;
		return this;
	}
	
	public String getBaseViewPath() {
		return baseViewPath;
	}
	
	public List<Route> getRouteItemList() {
		return routeItemList;
	}
	
	public Interceptor[] getInterceptors() {
		return interList.size() > 0 ?
				interList.toArray(new Interceptor[interList.size()]) :
				InterceptorManager.NULL_INTERS;
	}
	
	public static List<Routes> getRoutesList() {
		return routesList;
	}
	
	/**
	 * 配置是否在路由映射完成之后清除内部数据，以回收内存，默认值为 false.
	 * 
	 * 设置为 false 通常用于在系统启动之后，仍然要使用 Routes 的场景，
	 * 例如希望拿到 Routes 生成用于控制访问权限的数据
	 */
	public void setClearAfterMapping(boolean clearAfterMapping) {
		this.clearAfterMapping = clearAfterMapping;
	}
	
	public void clear() {
		if (clearAfterMapping) {
			routesList = null;
			baseViewPath = null;
			routeItemList = null;
			interList = null;
		}
	}
	
	public static class Route {
		
		private String controllerPath;
		private Class<? extends Controller> controllerClass;
		private String viewPath;
		
		public Route(String controllerPath, Class<? extends Controller> controllerClass, String viewPath) {
			if (StrKit.isBlank(controllerPath)) {
				throw new IllegalArgumentException("controllerPath can not be blank");
			}
			if (controllerClass == null) {
				throw new IllegalArgumentException("controllerClass can not be null");
			}
			if (StrKit.isBlank(viewPath)) {
				// throw new IllegalArgumentException("viewPath can not be blank");
				viewPath = "/";
			}
			
			this.controllerPath = processControllerPath(controllerPath);
			this.controllerClass = controllerClass;
			this.viewPath = processViewPath(viewPath);
		}
		
		private String processControllerPath(String controllerPath) {
			controllerPath = controllerPath.trim();
			if (!controllerPath.startsWith("/")) {
				controllerPath = "/" + controllerPath;
			}
			return controllerPath;
		}
		
		private String processViewPath(String viewPath) {
			viewPath = viewPath.trim();
			if (!viewPath.startsWith("/")) {			// add prefix "/"
				viewPath = "/" + viewPath;
			}
			if (!viewPath.endsWith("/")) {				// add postfix "/"
				viewPath = viewPath + "/";
			}
			return viewPath;
		}
		
		public String getControllerPath() {
			return controllerPath;
		}

		/**
		 * 已更名为 getControllerPath()
		 */
		@Deprecated
		public String getControllerKey() {
			return controllerPath;
		}

		public Class<? extends Controller> getControllerClass() {
			return controllerClass;
		}
		
		public String getFinalViewPath(String baseViewPath) {
			return baseViewPath != null ? baseViewPath + viewPath : viewPath;
		}
	}
}









