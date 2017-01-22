package com.jfinal.json;

import com.jfinal.json.FastJson;
import com.jfinal.json.IJsonFactory;
import com.jfinal.json.JFinalJson;
import com.jfinal.json.Json;

/**
 * JFinalJson 与 FastJson 混合做 json 转换
 * toJson 用 JFinalJson，parse 用 FastJson
 * 
 * 注意：
 * 1：需要添加 fastjson 相关 jar 包
 * 2：parse 方法转对象依赖于 setter 方法
 * 3：MixedJson 内部使用了 static 共享变量，在使用时不要改变其内部属性值，以免影响其它线程
 */
public class MixedJsonFactory implements IJsonFactory {
	
	private static final MixedJsonFactory me = new MixedJsonFactory();
	
	public static MixedJsonFactory me() {
		return me;
	}

	private static MixedJson mixedJson =  new MixedJson();

	public Json getJson() {
		return mixedJson;
	}

	private static class MixedJson extends Json {

		private static JFinalJson jFinalJson = JFinalJson.getJson();
		private static FastJson fastJson = FastJson.getJson();

		public String toJson(Object object) {
			return jFinalJson.toJson(object);
		}

		public <T> T parse(String jsonString, Class<T> type) {
			return fastJson.parse(jsonString, type);
		}
	}
}
