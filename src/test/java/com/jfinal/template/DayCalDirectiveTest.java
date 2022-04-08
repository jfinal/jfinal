package com.jfinal.template;

import com.jfinal.template.ext.directive.DayCalDirective;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DayCalDirectiveTest {
    
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
    public void dayCal() {
        engine.addDirective("day", DayCalDirective.class);
        Template template = engine.getTemplate("com/jfinal/template/dayCalDirective.sql");
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        Map<String, Object> map3 = new HashMap<String, Object>();
        map3.put("id", "123");
        map3.put("age", "18");
        map3.put("day", "2020-10-10");
        map3.put("entityIds", list);
        map3.put("long", 777L);
        System.out.println(template.renderToString(map3));
    }
}
