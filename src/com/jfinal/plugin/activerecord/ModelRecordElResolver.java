/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
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
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.servlet.ServletContext;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;

/**
 * ModelRecordElResolver
 */
@SuppressWarnings("rawtypes")
public class ModelRecordElResolver extends ELResolver {
	
	static JspApplicationContext jspApplicationContext = null;
	
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
		if (isWorking == false)	return null;
		
		if (base instanceof Model) {
			context.setPropertyResolved(true);
			if (property == null)
				return null;
			return ((Model)base).get(property.toString());
		}
		else if (base instanceof Record) {
			context.setPropertyResolved(true);
			if (property == null)
				return null;
			return ((Record)base).get(property.toString());
		}
		return null;
	}
	
	public Class<?> getType(ELContext context, Object base, Object property) {
		if (isWorking == false)	return null;
		
		// return null;
		return (base == null) ? null : Object.class;
	}
	
	public void setValue(ELContext context, Object base, Object property, Object value) {
		if (isWorking == false)	return ;
		
		if (base instanceof Model) {
			context.setPropertyResolved(true);
			if (property == null)
				return ;
			try {
				((Model)base).set(property.toString(), value);
			} catch (Exception e) {
				((Model)base).put(property.toString(), value);
			}
		}
		else if (base instanceof Record) {
			context.setPropertyResolved(true);
			if (property == null)
				return ;
			((Record)base).set(property.toString(), value);
		}
	}
	
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		if (isWorking == false)	return false;
		
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
		if (isWorking == false)	return null;
		
		if (base instanceof Model || base instanceof Record)
			return String.class;
		return null;
	}
}


