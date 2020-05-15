/**
 * Copyright (c) 2011-2020, James Zhan 詹波 (jfinal@126.com).
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

import java.util.function.Function;
import com.jfinal.json.JFinalJsonKit.JsonResult;
import com.jfinal.json.JFinalJsonKit.ToJson;

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
public class JFinalJson extends Json {
	
	protected static final JFinalJsonKit kit = JFinalJsonKit.me;
	
	protected static final ThreadLocal<JsonResult> TL = ThreadLocal.withInitial(() -> new JsonResult());
	
	protected static int defaultConvertDepth = 16;
	
	protected int convertDepth = defaultConvertDepth;
	protected String timestampPattern = "yyyy-MM-dd HH:mm:ss";
	
	public static JFinalJson getJson() {
		return new JFinalJson();
	}
	
	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public String toJson(Object object) {
		if (object == null) {
			return "null";
		}
		
		JsonResult ret = TL.get();
		try {
			// 优先使用对象级的属性 datePattern, 然后才是全局性的 defaultDatePattern
			String dp = datePattern != null ? datePattern : getDefaultDatePattern();
			ret.init(dp, timestampPattern);
			ToJson toJson = kit.getToJson(object);
			toJson.toJson(object, convertDepth, ret);
			return ret.toString();
		}
		finally {
			ret.clear();
		}
	}
	
	/**
	 * 添加 ToJson 转换接口实现类，自由定制任意类型数据的转换规则
	 * <pre>
	 * 例子：
	 *     ToJson<Timestamp> toJson = (value, depth, ret) -> {
	 *       ret.addLong(value.getTime());
	 *     };
	 *     
	 *     JFinalJson.addToJson(Timestamp.class, toJson);
	 *     
	 *     以上代码为 Timestamp 类型的 json 转换定制了转换规则
	 *     将其转换成了 long 型数据
	 * </pre>
	 */
	public static void addToJson(Class<?> type, ToJson<?> toJson) {
		JFinalJsonKit.addToJson(type, toJson);
	}
	
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
		this.timestampPattern = timestampPattern;
		return this;
	}
	
	public static void setMaxBufferSize(int maxBufferSize) {
		JFinalJsonKit.setMaxBufferSize(maxBufferSize);
	}
	
	/**
	 * 将 Model 当成 Bean 只对 getter 方法进行转换
	 * 
	 * 默认值为 false，将使用 Model 内的 Map attrs 属性进行转换，不对 getter 方法进行转换
	 * 优点是可以转换 sql 关联查询产生的动态字段，还可以转换 Model.put(...) 进来的数据
	 * 
	 * 配置为 true 时，将 Model 当成是传统的 java bean 对其 getter 方法进行转换，
	 * 使用生成器生成过 base model 的情况下才可以使用此配置
	 */
	public static void setTreatModelAsBean(boolean treatModelAsBean) {
		JFinalJsonKit.setTreatModelAsBean(treatModelAsBean);
	}
	
	/**
	 * 配置 Model、Record 字段名的转换函数
	 * 
	 * <pre>
	 * 例子：
	 *    JFinalJson.setModelAndRecordFieldNameConverter(fieldName -> {
	 *		   return StrKit.toCamelCase(fieldName, true);
	 *	  });
	 *  
	 *  以上例子中的方法 StrKit.toCamelCase(...) 的第二个参数可以控制大小写转化的细节
	 *  可以查看其方法上方注释中的说明了解详情
	 * </pre>
	 */
	public static void setModelAndRecordFieldNameConverter(Function<String, String>converter) {
		JFinalJsonKit.setModelAndRecordFieldNameConverter(converter);
	}
	
	/**
	 * 配置将 Model、Record 字段名转换为驼峰格式
	 * 
	 * 转换用到了 StrKit.toCamelCase(fieldName, true)，具体转换规则参考
	 * 该方法注释中的说明，注意字段名首先会被转换成小写字母，如果要改变转换规则
	 * 可以使用 setModelAndRecordFieldNameConverter(Function func)
	 * 定制自己的转换函数
	 */
	public static void setModelAndRecordFieldNameToCamelCase() {
		JFinalJsonKit.setModelAndRecordFieldNameToCamelCase();
	}
	
	public <T> T parse(String jsonString, Class<T> type) {
		throw new RuntimeException("jfinal " + com.jfinal.core.Const.JFINAL_VERSION + 
		"默认 json 实现暂不支持 json 到 object 的转换,建议使用 active recrord 的 Generator 生成 base model，" +
		"再通过 me.setJsonFactory(new MixedJsonFactory()) 来支持");
	}
}



