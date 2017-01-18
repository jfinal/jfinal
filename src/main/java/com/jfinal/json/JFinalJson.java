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

package com.jfinal.json;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * Json 转换 JFinal 实现.
 * 
 * json 到 java 类型转换规则:
 * string			java.lang.String
 * number			java.lang.Number
 * true|false		java.lang.Boolean
 * null				null
 * array			java.util.List
 * object			java.util.Map
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class JFinalJson extends Json {
	
	private static int defaultConvertDepth = 15;
	
	protected int convertDepth = defaultConvertDepth;
	protected String timestampPattern = "yyyy-MM-dd HH:mm:ss";
	protected String datePattern = "yyyy-MM-dd";
	
	/**
	 * 设置全局性默认转换深度
	 */
	public static void setDefaultConvertDepth(int defaultConvertDepth) {
		if (defaultConvertDepth < 2) {
			throw new IllegalArgumentException("defaultConvertDepth depth can not less than 2.");
		}
		JFinalJson.defaultConvertDepth = defaultConvertDepth;
	}
	
	public JFinalJson setConvertDepth(int convertDepth) {
		if (convertDepth < 2) {
			throw new IllegalArgumentException("convert depth can not less than 2.");
		}
		this.convertDepth = convertDepth;
		return this;
	}
	
	public JFinalJson setTimestampPattern(String timestampPattern) {
		if (StrKit.isBlank(timestampPattern)) {
			throw new IllegalArgumentException("timestampPattern can not be blank.");
		}
		this.timestampPattern = timestampPattern;
		return this;
	}
	
	public Json setDatePattern(String datePattern) {
		if (StrKit.isBlank(datePattern)) {
			throw new IllegalArgumentException("datePattern can not be blank.");
		}
		this.datePattern = datePattern;
		return this;
	}
	
	public static JFinalJson getJson() {
		return new JFinalJson();
	}
	
	protected String mapToJson(Map map, int depth) {
		if(map == null) {
			return "null";
		}
        StringBuilder sb = new StringBuilder();
        boolean first = true;
		Iterator iter = map.entrySet().iterator();
		
        sb.append('{');
		while(iter.hasNext()){
            if(first)
                first = false;
            else
                sb.append(',');
            
			Map.Entry entry = (Map.Entry)iter.next();
			toKeyValue(String.valueOf(entry.getKey()),entry.getValue(), sb, depth);
		}
        sb.append('}');
		return sb.toString();
	}
	
	protected String toKeyValue(String key, Object value, StringBuilder sb, int depth){
		sb.append('\"');
        if(key == null)
            sb.append("null");
        else
            escape(key, sb);
		sb.append('\"').append(':');
		
		sb.append(toJson(value, depth));
		
		return sb.toString();
	}
	
	protected String iteratorToJson(Iterator iter, int depth) {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        
        sb.append('[');
		while(iter.hasNext()){
            if(first)
                first = false;
            else
                sb.append(',');
            
			Object value = iter.next();
			if(value == null){
				sb.append("null");
				continue;
			}
			sb.append(toJson(value, depth));
		}
        sb.append(']');
		return sb.toString();
	}
	
	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters (U+0000 through U+001F).
	 */
	protected String escape(String s) {
		if(s == null)
			return null;
        StringBuilder sb = new StringBuilder();
        escape(s, sb);
        return sb.toString();
    }
	
	protected void escape(String s, StringBuilder sb) {
		for(int i=0; i<s.length(); i++){
			char ch = s.charAt(i);
			switch(ch){
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			//case '/':
			//	sb.append("\\/");
			//	break;
			default:
				if((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')) {
					String str = Integer.toHexString(ch);
					sb.append("\\u");
					for(int k=0; k<4-str.length(); k++) {
						sb.append('0');
					}
					sb.append(str.toUpperCase());
				}
				else{
					sb.append(ch);
				}
			}
		}
	}
	
	public String toJson(Object object) {
		return toJson(object, convertDepth);
	}
	
	protected String toJson(Object value, int depth) {
		if(value == null || (depth--) < 0)
			return "null";
		
		if(value instanceof String)
			return "\"" + escape((String)value) + "\"";
		
		if(value instanceof Double){
			if(((Double)value).isInfinite() || ((Double)value).isNaN())
				return "null";
			else
				return value.toString();
		}
		
		if(value instanceof Float){
			if(((Float)value).isInfinite() || ((Float)value).isNaN())
				return "null";
			else
				return value.toString();
		}
		
		if(value instanceof Number)
			return value.toString();
		
		if(value instanceof Boolean)
			return value.toString();
		
		if (value instanceof java.util.Date) {
			if (value instanceof java.sql.Timestamp) {
				return "\"" + new SimpleDateFormat(timestampPattern).format(value) + "\"";
			}
			if (value instanceof java.sql.Time) {
				return "\"" + value.toString() + "\"";
			}
			// 优先使用对象级的属性 datePattern, 然后才是全局性的 defaultDatePattern
			String dp = datePattern != null ? datePattern : getDefaultDatePattern();
			if (dp != null) {
				return "\"" + new SimpleDateFormat(dp).format(value) + "\"";
			} else {
				return "" + ((java.util.Date)value).getTime();
			}
		}
		
		if(value instanceof Collection) {
			return iteratorToJson(((Collection)value).iterator(), depth);
		}
		
		if(value instanceof Map) {
			return mapToJson((Map)value, depth);
		}
		
		String result = otherToJson(value, depth);
		if (result != null)
			return result;
		
		// 类型无法处理时当作字符串处理,否则ajax调用返回时js无法解析
		// return value.toString();
		return "\"" + escape(value.toString()) + "\"";
	}
	
	protected String otherToJson(Object value, int depth) {
		if (value instanceof Character) {
			return "\"" + escape(value.toString()) + "\"";
		}
		
		if (value instanceof Model) {
			Map map = com.jfinal.plugin.activerecord.CPI.getAttrs((Model)value);
			return mapToJson(map, depth);
		}
		if (value instanceof Record) {
			Map map = ((Record)value).getColumns();
			return mapToJson(map, depth);
		}
		if (value.getClass().isArray()) {
			int len = Array.getLength(value);
			List<Object> list = new ArrayList<Object>(len);
			for (int i=0; i<len; i++) {
				list.add(Array.get(value, i));
			}
			return iteratorToJson(list.iterator(), depth);
		}
		if (value instanceof Iterator) {
			return iteratorToJson((Iterator)value, depth);
		}
		if (value instanceof Enumeration) {
			ArrayList<?> list = Collections.list((Enumeration<?>)value);
			return iteratorToJson(list.iterator(), depth);
		}
		if (value instanceof Enum) {
			return "\"" + ((Enum)value).toString() + "\"";
		}
		
		return beanToJson(value, depth);
	}
	
	protected String beanToJson(Object model, int depth) {
		Map map = new HashMap();
		Method[] methods = model.getClass().getMethods();
		for (Method m : methods) {
			String methodName = m.getName();
			int indexOfGet = methodName.indexOf("get");
			if (indexOfGet == 0 && methodName.length() > 3) {	// Only getter
				String attrName = methodName.substring(3);
				if (!attrName.equals("Class")) {				// Ignore Object.getClass()
					Class<?>[] types = m.getParameterTypes();
					if (types.length == 0) {
						try {
							Object value = m.invoke(model);
							map.put(StrKit.firstCharToLowerCase(attrName), value);
						} catch (Exception e) {
							throw new RuntimeException(e.getMessage(), e);
						}
					}
				}
			}
			else {
               int indexOfIs = methodName.indexOf("is");
               if (indexOfIs == 0 && methodName.length() > 2) {
                  String attrName = methodName.substring(2);
                  Class<?>[] types = m.getParameterTypes();
                  if (types.length == 0) {
                      try {
                          Object value = m.invoke(model);
                          map.put(StrKit.firstCharToLowerCase(attrName), value);
                      } catch (Exception e) {
                          throw new RuntimeException(e.getMessage(), e);
                      }
                  }
               }
            }
		}
		return mapToJson(map, depth);
	}
	
	public <T> T parse(String jsonString, Class<T> type) {
		throw new RuntimeException("jfinal " + com.jfinal.core.Const.JFINAL_VERSION + 
		"默认 json 实现暂不支持 json 到 object 的转换,建议使用 active recrord 的 Generator 生成 base model，" +
		"再通过 me.setJsonFactory(new JacksonFactory()) 来支持");
	}
}







