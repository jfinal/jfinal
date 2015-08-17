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

package com.jfinal.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import com.jfinal.kit.StrKit;

/**
 * Res is used to get the message value from the ResourceBundle of the related Locale.
 */
public class Res {
	
	private final ResourceBundle resourceBundle;
	
	public Res(String baseName, String locale) {
		if (StrKit.isBlank(baseName))
			throw new IllegalArgumentException("baseName can not be blank");
		if (StrKit.isBlank(locale))
			throw new IllegalArgumentException("locale can not be blank, the format like this: zh_CN or en_US");
		
		this.resourceBundle = ResourceBundle.getBundle(baseName, parseLocale(locale));
	}
	
	private Locale parseLocale(String locale) {
		String[] array = locale.split("_");
		if (array.length == 1)
			return new Locale(array[0]);
		return new Locale(array[0], array[1]);
	}
	
	/**
	 * Get the message value from ResourceBundle of the related Locale.
	 * @param key message key
	 * @return message value
	 */
	public String get(String key) {
		return resourceBundle.getString(key);
	}
	
	/**
	 * Get the message value from ResourceBundle by the key then format with the arguments.
	 * Example:<br>
	 * In resource file : msg=Hello {0}, today is{1}.<br>
	 * In java code : res.format("msg", "james", new Date()); <br>
	 * In freemarker template : ${_res.format("msg", "james", new Date())}<br>
	 * The result is : Hello james, today is 2015-04-14.
	 */
	public String format(String key, Object... arguments) {
		return MessageFormat.format(resourceBundle.getString(key), arguments);
	}
	
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
}



