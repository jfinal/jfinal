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

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.jfinal.core.Const;

/**
 * I18N support.
 * 
 * 1: Config parameters in JFinalConfig
 * 2: Init I18N in JFinal 
 * 3: I18N support text with Locale
 * 4: Controller use I18N.getText(...) with Local setting in I18nInterceptor
 * 5: The resource file in WEB-INF/classes
 * 
 * important: Locale can create with language like new Locale("xxx");
 * 
 * need test
 * Using String get Locale was learned from Strus2
 */
public class I18N {
	
	private static String baseName;
	private static Locale defaultLocale = Locale.getDefault();
	private static int i18nMaxAgeOfCookie = Const.DEFAULT_I18N_MAX_AGE_OF_COOKIE;
	private static final NullResourceBundle NULL_RESOURCE_BUNDLE = new NullResourceBundle();
	private static final ConcurrentMap<String, ResourceBundle> bundlesMap = new ConcurrentHashMap<String, ResourceBundle>();
	
	private static volatile I18N me;
	
	private I18N() {
	}
	
	public static I18N me() {
		if (me == null)
			synchronized (I18N.class) {
				if (me == null)
					me = new I18N();
			}
		return me;
	}
	
	public static void init(String baseName, Locale defaultLocale, Integer i18nMaxAgeOfCookie) {
		I18N.baseName = baseName;
		if (defaultLocale != null)
			I18N.defaultLocale = defaultLocale;
		if (i18nMaxAgeOfCookie != null)
			I18N.i18nMaxAgeOfCookie = i18nMaxAgeOfCookie;
	}
	
	public static Locale getDefaultLocale() {
		return defaultLocale;
	}
	
	final static public int getI18nMaxAgeOfCookie() {
		return i18nMaxAgeOfCookie;
	}
	
	private static ResourceBundle getResourceBundle(Locale locale) {
		String resourceBundleKey = getresourceBundleKey(locale);
		ResourceBundle resourceBundle = bundlesMap.get(resourceBundleKey);
		if (resourceBundle == null) {
			try {
				resourceBundle = ResourceBundle.getBundle(baseName, locale);
				bundlesMap.put(resourceBundleKey, resourceBundle);
			}
			catch (MissingResourceException e) {
				resourceBundle = NULL_RESOURCE_BUNDLE;
			}
		}
		return resourceBundle;
	}
	
	/**
	 * 将来只改这里就可以了: resourceBundleKey的生成规则
	 */
	private static String getresourceBundleKey(Locale locale) {
		// return baseName + "_" + locale.toString();	// "_" 貌似与无关, 为了提升性能, 故去掉
		return baseName +  locale.toString();
	}
	
	public static String getText(String key) {
		return getResourceBundle(defaultLocale).getString(key);
	}
	
	public static String getText(String key, String defaultValue) {
		String result = getResourceBundle(defaultLocale).getString(key);
		return result != null ? result : defaultValue;
	}
	
	public static String getText(String key, Locale locale) {
		return getResourceBundle(locale).getString(key);
	}
	
	public static String getText(String key, String defaultValue, Locale locale) {
		String result = getResourceBundle(locale).getString(key);
		return result != null ? result : defaultValue;
	}
	
	// public static Locale localeFromString(String localeStr, Locale defaultLocale) {
	public static Locale localeFromString(String localeStr) {
        if ((localeStr == null) || (localeStr.trim().length() == 0) || ("_".equals(localeStr))) {
            // return (defaultLocale != null) ? defaultLocale : Locale.getDefault();	// 原实现被注掉
        	return defaultLocale;
        }
        
        int index = localeStr.indexOf('_');
        if (index < 0) {
            return new Locale(localeStr);
        }
        
        String language = localeStr.substring(0, index);
        if (index == localeStr.length()) {
            return new Locale(language);
        }
        
        localeStr = localeStr.substring(index + 1);
        index = localeStr.indexOf('_');
        if (index < 0) {
            return new Locale(language, localeStr);
        }
        
        String country = localeStr.substring(0, index);
        if (index == localeStr.length()) {
            return new Locale(language, country);
        }
        
        localeStr = localeStr.substring(index + 1);
        return new Locale(language, country, localeStr);
    }
	
	private static class NullResourceBundle extends ResourceBundle {
        public Enumeration<String> getKeys() {
            return null; // dummy
        }
        protected Object handleGetObject(String key) {
            return null; // dummy
        }
    }
	
	// 可惜的是使用Local可以被 new 出来, 造成了无法判断相等，后来测试，可以使用 equals方法来判断是否相等
	public static void main(String[] args) {
		// Locale.getDefault();
		// Locale en = Locale.US;
		// Locale us = Locale.US;
		// System.out.println(l.toString());
		// System.out.println(en == us);
		// System.out.println(en.equals(us));
		
		// 下面的 taiwan.getLanguage()值仍为 zh,所以可以确定i18n实现有缺陷,即 language不能唯一确定Local对象
		// 造成了无法通过 language不好还原
		System.out.println(Locale.CHINESE.getLanguage());
		System.out.println(Locale.CHINA.getLanguage());
		System.out.println(Locale.SIMPLIFIED_CHINESE.getLanguage());
		System.out.println(Locale.TRADITIONAL_CHINESE.getLanguage());
		System.out.println(Locale.TAIWAN.getLanguage());
		
		Locale shoudong = new Locale("en");
		System.out.println(shoudong.getLanguage().equals(Locale.US.getLanguage()));
		System.out.println(shoudong.getLanguage().equals(Locale.ENGLISH.getLanguage()));
		System.out.println(shoudong.getLanguage().equals(Locale.CANADA.getLanguage()));
		System.out.println(shoudong.getLanguage().equals(Locale.UK.getLanguage()));
		System.out.println(shoudong.getLanguage().equals(Locale.CANADA_FRENCH.getLanguage()));
	}
}







