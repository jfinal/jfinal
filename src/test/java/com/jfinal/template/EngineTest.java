package com.jfinal.template;

import com.jfinal.kit.JMap;

public class EngineTest {
	public static void main(String[] args) {
		JMap para = JMap.create("key", "value");
		String result = Engine.use().getTemplateByString("#(key)").renderToString(para);
		System.out.println(result);
	}
}
