package com.jfinal.template;

import com.jfinal.kit.Kv;

public class EngineTest {
	public static void main(String[] args) {
		Kv para = Kv.by("key", "value");
		String result = Engine.use().getTemplateByString("#(key)").renderToString(para);
		System.out.println(result);
	}
}
