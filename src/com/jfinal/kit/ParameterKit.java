/**
 * 
 */
package com.jfinal.kit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

/**
 * @description <br>
 * @author <a href="mailto:vakinge@gmail.com">vakin</a>
 * @date 2016年7月13日
 */
public class ParameterKit {

	private static final String METHOD_POST = "POST";

	private static final String DEFAULT_PARAM_OBJECT_NAME = "_root";

	public static final String JSON_DATA_KEY = "_json";

	private static ThreadLocal<Map<String, Map<String, Object>>> requestDatas = new ThreadLocal<Map<String, Map<String, Object>>>();

	/**
	 * 获取请求参数组装成map<name,value>
	 * @param request
	 * @param excludeFields 排除字段列表
	 * @return
	 */
	public static Map<String, Object> getParameters(HttpServletRequest request, String... excludeFields) {
		return parseObjectParameters(request, excludeFields).get(DEFAULT_PARAM_OBJECT_NAME);
	}

	/**
	 * 获取请求参数组装成map<name,value>
	 * @param root 如（form name为user.id,user.name,则root为user）
	 * @param request
	 * @param excludeFields 排除字段列表
	 * @return
	 */
	public static Map<String, Object> getParameters(String root, HttpServletRequest request, String... excludeFields) {
		return parseObjectParameters(request, excludeFields).get(root);
	}
	
	/**
	 * 获取请求参数并包装成实体
	 * @param clazz
	 * @param root
	 * @param request
	 * @param excludeFields
	 * @return
	 */
	public static <T> T getParametersToBean(Class<T> clazz,String root, HttpServletRequest request){
		return BeanKit.mapToBean(parseObjectParameters(request).get(root), clazz);
	}
	
	public static <T> T getParametersToBean(Class<T> clazz, HttpServletRequest request){
		return BeanKit.mapToBean(parseObjectParameters(request).get(DEFAULT_PARAM_OBJECT_NAME), clazz);
	}

	/**
	 * 解析并封装对象参数请求（如：user.id）
	 * 
	 * @return
	 */
	private static Map<String, Map<String, Object>> parseObjectParameters(HttpServletRequest request,
			String... excludeFields) {

		Map<String, Map<String, Object>> result = requestDatas.get();
		if (result != null)
			return result;
		result = new HashMap<String, Map<String, Object>>();
		Enumeration<String> e = request.getParameterNames();

		List<String> excludes = excludeFields == null || excludeFields.length == 0 ? null
				: new ArrayList<String>(Arrays.asList(excludeFields));

		if (e.hasMoreElements()) {
			while (e.hasMoreElements()) {
				String name = e.nextElement();
				String[] nameParts = name.indexOf(".") <= 0 ? new String[] { DEFAULT_PARAM_OBJECT_NAME, name }
						: name.split("\\.");
				// 排除
				if (excludes != null && excludes.contains(nameParts[1]))
					continue;

				Map<String, Object> params = result.get(nameParts[0]);
				if (params == null) {
					params = new HashMap<String, Object>();
					result.put(nameParts[0], params);
				}
				String[] values = request.getParameterValues(name);
				if (values.length == 1) {
					if (StringUtils.isNotBlank(values[0]))
						params.put(nameParts[1], values[0]);
				} else {
					params.put(nameParts[1], values);
				}
			}
		}

		// post 请求解析流
		if (request.getMethod().equals(METHOD_POST)) {
			String content = null;
			try {
				content = convertStreamToString(request.getInputStream());
			} catch (Exception e1) {
			}
			if (StringUtils.isNotBlank(content)) {
				// JSON
				if (content.startsWith("{")) {
					// TODO
				} else {
					String[] split = content.split("\\&");
					for (String s : split) {
						String[] split2 = s.split("=");
						if (split2.length == 2 && StringUtils.isNotBlank(split2[1])) {
							String[] nameParts = split2[0].split("\\.");
							if (nameParts.length != 2)
								continue;
							Map<String, Object> params = result.get(nameParts[0]);
							if (params == null) {
								params = new HashMap<String, Object>();
								result.put(nameParts[0], params);
							}
							params.put(nameParts[1], split2[1]);
						}
					}
				}

			}
		}

		requestDatas.set(result);
		return result;
	}

	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
