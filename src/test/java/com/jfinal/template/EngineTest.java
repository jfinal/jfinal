package com.jfinal.template;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import com.jfinal.kit.Kv;

public class EngineTest {
	
	static Engine engine;
	
	@BeforeClass
	public static void init() {
		engine = Engine.use();
		engine.setToClassPathSourceFactory();
	}
	
	@AfterClass
	public static void exit() {
	}
	
	@Test
	public void renderToString() {
		Kv para = Kv.by("key", "value");
		String result = engine.getTemplateByString("#(key)").renderToString(para);
		Assert.assertEquals("value", result);
	}
}

