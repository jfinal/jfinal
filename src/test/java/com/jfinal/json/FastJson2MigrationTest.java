package com.jfinal.json;

import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.Assert;
import org.junit.Test;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.jfinal.core.ActionHandler;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.BeanGetter;
import com.jfinal.core.paragetter.JsonRequest;

public class FastJson2MigrationTest {

	public static class User {
		private String userName;

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}
	}

	private void users(List<User> users) {
	}

	@Test
	public void referenceDetection() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("self", map);

		String json = FastJson.getJson().toJson(map);
		assertReference(json, "self");

		json = FastJson.getJson().toJson(map, JSONWriter.Feature.PrettyFormat);
		assertReference(json, "self");
	}

	@Test
	public void jsonRequestReferenceDetection() {
		JsonRequest request = new JsonRequest("{\"x\":{\"$ref\":\"$\"}}", emptyRequest());

		assertReference(request.getParameter("x"), "x");
		assertReference(request.getParameterMap().get("x")[0], "x");
	}

	@Test
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void supportSmartMatch() throws Exception {
		User user = FastJson.getJson().parse("{\"user_name\":\"fastJson\"}", User.class);
		Assert.assertEquals("fastJson", user.getUserName());

		Controller controller = new Controller() {};
		boolean resolveJson = ActionHandler.resolveJson;
		ActionHandler.setResolveJson(true);
		try {
			controller.setHttpServletRequest(new JsonRequest("{\"user_name\":\"bean\"}", emptyRequest()));
			BeanGetter<User> beanGetter = new BeanGetter<>(User.class, "user", null);
			Assert.assertEquals("bean", beanGetter.get(null, controller).getUserName());

			controller.setHttpServletRequest(new JsonRequest("{\"user\":{\"user_name\":\"named\"}}", emptyRequest()));
			Assert.assertEquals("named", beanGetter.get(null, controller).getUserName());

			Parameter parameter = getClass().getDeclaredMethod("users", List.class).getParameters()[0];
			controller.setHttpServletRequest(new JsonRequest("{\"users\":[{\"user_name\":\"list\"}]}", emptyRequest()));
			BeanGetter<List> listGetter = new BeanGetter<>(List.class, "users", parameter);
			Assert.assertEquals("list", ((User)listGetter.get(null, controller).get(0)).getUserName());
		} finally {
			ActionHandler.setResolveJson(resolveJson);
		}
	}

	private HttpServletRequest emptyRequest() {
		return (HttpServletRequest)Proxy.newProxyInstance(
			HttpServletRequest.class.getClassLoader(),
			new Class<?>[]{HttpServletRequest.class},
			(proxy, method, args) -> "getParameterMap".equals(method.getName()) ? Collections.emptyMap() : null
		);
	}

	private void assertReference(String json, String key) {
		JSONObject object = JSON.parseObject(json);
		Assert.assertSame(object, object.get(key));
	}
}
