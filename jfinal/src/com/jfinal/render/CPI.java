/**
 * Copyright (c) 2011-2012, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.render;

import java.util.Properties;
import com.jfinal.log.Logger;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

/**
 * Cross Package Invoking for render.
 */
public abstract class CPI {
	
	private static final Logger log = Logger.getLogger(CPI.class);
	
	/**
	 * Set freemarker's property.
	 * The value of template_update_delay is 5 seconds.
	 * Example: CPI.setFreeMarkerProperty("template_update_delay", "1600");
	 */
	public static void setFreeMarkerProperty(String propertyName, String propertyValue) {
		try {
			FreeMarkerRender.getConfiguration().setSetting(propertyName, propertyValue);
		} catch (TemplateException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public static void setFreeMarkerProperties(Properties properties) {
		try {
			FreeMarkerRender.getConfiguration().setSettings(properties);
		} catch (TemplateException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public static void setJspRenderSupportActiveRecord(boolean supportActiveRecord) {
		JspRender.setSupportActiveRecord(supportActiveRecord);
	}
	
	public static Configuration getFreeMarkerConfiguration() {
		return FreeMarkerRender.getConfiguration(); 
	}
	
	public static void setVelocityProperties(Properties properties) {
		VelocityRender.setProperties(properties);
	}
}




