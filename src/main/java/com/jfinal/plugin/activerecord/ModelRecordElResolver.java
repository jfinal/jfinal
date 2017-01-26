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

package com.jfinal.plugin.activerecord;

import java.beans.FeatureDescriptor;
import java.lang.reflect.Method;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.servlet.ServletContext;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;
import com.jfinal.kit.StrKit;

/**
 * ModelRecordElResolver
 */
@SuppressWarnings("rawtypes")
public class ModelRecordElResolver extends ELResolver {
	
	static JspApplicationContext jspApplicationContext = null;
	private static final Object[] NULL_ARGUMENT = new Object[0];
	
	private static boolean resolveBeanAsModel = false;
	
	/**
	 * 设置为 true 时，使用生成器生成的实现了 IBean 接口的 Class 将被当成  Model 来处理，
	 * getter 不被 jsp/jstl 用来输出数据，仍然使用 model.get(String attr) 来输出数据。
	 * 
	 * 有利于在关联查询时输出无 getter 方法的字段值。建议mysql数据表中的字段采用驼峰命名，
	 * 表名仍然用下划线方式命名。 resolveBeanAsModel 默认值为 false。
	 * 
	 * 注意：这里所指的 Bean 仅仅指用 BaseModelGenerator 生成的实现了 IBean接口后的类文件
	 * <pre>
	 * 使用方式， 在 YourJFinalConfig 中创建方法，并调用本方法：
	 * public void afterJFinalStart() {
	 *    ModelRecordElResolver.setResolveBeanAsModel(true);
	 * }
	 * </pre>
	 */
	public static void setResolveBeanAsModel(boolean resolveBeanAsModel) {
		ModelRecordElResolver.resolveBeanAsModel = resolveBeanAsModel;
	}
	
	/**
	 * Compatible for JspRender.setSupportActiveRecord(true);
	 * Delete it in the future
	 */
	private static boolean isWorking = true;
	
	public static void setWorking(boolean isWorking) {
		ModelRecordElResolver.isWorking = isWorking;
	}
	
	public synchronized static void init(ServletContext servletContext) {
	    JspApplicationContext jac = JspFactory.getDefaultFactory().getJspApplicationContext(servletContext);
	    if (jspApplicationContext != jac) {
	    	jspApplicationContext = jac;
	    	jspApplicationContext.addELResolver(new ModelRecordElResolver());
	    }
	}
	
	public static void init() {
		init(com.jfinal.core.JFinal.me().getServletContext());
	}
	
	public Object getValue(ELContext context, Object base, Object property) {
		if (isWorking == false || property == null) {
			return null;
		}
		// if (resolveBeanAsModel == false && base instanceof IBean) {
		//	return null;
		// }
		if (base instanceof IBean) {
			Method getter = findGetter(base, property.toString());
			if (getter != null) {
				context.setPropertyResolved(true);
				try {
					return getter.invoke(base, NULL_ARGUMENT);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		if (base instanceof Model) {
			context.setPropertyResolved(true);
			return ((Model)base).get(property.toString());
		}
		else if (base instanceof Record) {
			context.setPropertyResolved(true);
			return ((Record)base).get(property.toString());
		}
		return null;
	}
	
	private Method findGetter(Object base, String property) {
		String getter = "get" + StrKit.firstCharToUpperCase(property);
		Method[] methods = base.getClass().getMethods();
		for (Method m : methods) {
			if (m.getName().equals(getter) && m.getParameterTypes().length == 0) {
				return m;
			}
		}
		return null;
	}
	
	public Class<?> getType(ELContext context, Object base, Object property) {
		if (isWorking == false) {
			return null;
		}
		if (resolveBeanAsModel == false && base instanceof IBean) {
			return null;
		}
		
		// return null;
		return (base == null) ? null : Object.class;
	}
	
	public void setValue(ELContext context, Object base, Object property, Object value) {
		if (isWorking == false || property == null) {
			return ;
		}
		if (resolveBeanAsModel == false && base instanceof IBean) {
			return ;
		}
		
		if (base instanceof Model) {
			context.setPropertyResolved(true);
			try {
				((Model)base).set(property.toString(), value);
			} catch (Exception e) {
				((Model)base).put(property.toString(), value);
			}
		}
		else if (base instanceof Record) {
			context.setPropertyResolved(true);
			((Record)base).set(property.toString(), value);
		}
	}
	
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		if (isWorking == false) {
			return false;
		}
		if (resolveBeanAsModel == false && base instanceof IBean) {
			return false;
		}
		
		if (base instanceof Model || base instanceof Record) {
			context.setPropertyResolved(true);
			return false;
		}
		return false;
	}
	
	// Do not invoke context.setPropertyResolved(true) for this method
	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
		return null;
	}
	
	// Do not invoke context.setPropertyResolved(true) for this method
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		if (isWorking == false) {
			return null;
		}
		if (resolveBeanAsModel == false && base instanceof IBean) {
			return null;
		}
		
		if (base instanceof Model || base instanceof Record)
			return String.class;
		return null;
	}
}


