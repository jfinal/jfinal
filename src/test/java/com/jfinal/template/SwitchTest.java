package com.jfinal.template;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.jfinal.kit.Kv;

public class SwitchTest {
	
	Engine engine;
	
	@Before
	public void init() {
		engine = Engine.use();
		engine.setToClassPathSourceFactory();
	}
	
	@After
	public void exit() {
	}
	
	@Test
	public void switch_() {
		Template template = engine.getTemplate("com/jfinal/template/switch.txt");
		Kv kv = Kv.by("date", 123);
		String ret = template.renderToString(kv);
		System.out.println(ret);
	}
}
