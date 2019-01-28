package com.jfinal.template;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import com.jfinal.kit.Kv;

public class SwitchTest {
	
	static Engine engine;
	
	@BeforeClass
	public static void init() {
		engine = Engine.use();
		engine.setToClassPathSourceFactory();
	}
	
	@AfterClass
	public static void exit() {
	}
	
	@Ignore
	@Test
	public void switch_() {
		Template template = engine.getTemplate("com/jfinal/template/switch.txt");
		Kv kv = Kv.by("data", 123).set("b", 123);
		String ret = template.renderToString(kv);
		System.out.print(ret);
	}
}
