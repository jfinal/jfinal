package com.jfinal.core.paragetter;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.core.Action;
import com.jfinal.core.Controller;

/**
 * JsonResolver 将 json 格式参数解析为 JsonRequest 对象
 */
public class JsonResolver {
	
	// static final String[] BLANK_STRING_ARRAY = {""};
	
	/**
	 * 处理 json 请求
	 */
	public void resolve(Action action, Controller c) {
		// HashMap<String, Object> jsonPara = JSON.parseObject(c.getRawData(), HashMap.class);
		JSONObject jsonPara = JSON.parseObject(c.getRawData());
		if (jsonPara == null) {
			return;
		}
		
		HashMap<String, String[]> newPara = new HashMap<>();
		
		// 先读取 parameter，否则后续从流中读取 rawData 后将无法读取 parameter（部分 servlet 容器）
		Map<String, String[]> oldPara = c.getRequest().getParameterMap();
		if (oldPara != null && oldPara.size() > 0) {
			newPara.putAll(oldPara);
		}
		
		for (Entry<String, Object> e : jsonPara.entrySet()) {
			String key = e.getKey();
			Object value = e.getValue();
			// 只转换最外面一层 json 数据，如果存在多层 json 结构，仅将其视为 String 留给后续流程转换
			if (value instanceof JSON) {
				newPara.put(key, new String[]{((JSON)value).toJSONString()});
			} else if (value != null) {
				newPara.put(key, new String[]{value.toString()});
			} else {
				// 需要考虑 value 是否转成 String[] array = {""}，ActionRepoter.getParameterValues() 有依赖
				newPara.put(key, null);
			}
		}
		
		// 注入包装类接管 request
		c.setHttpServletRequest(new JsonRequest(c.getRequest(), newPara));
	}
}





