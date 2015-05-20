/**
 * 
 */
package com.jfinal.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 朱丛启 2015年5月6日 下午1:39:48
 *
 */
@SuppressWarnings("unchecked")
public abstract class Service {

	private static Map<Class<? extends Service>, Service> INSTANCE_MAP = new HashMap<Class<? extends Service>, Service>();
    protected Controller controller;
    
	public static <Ser extends Service> Ser getInstance(Class<Ser> clazz, Controller controller) {
		Ser service = (Ser) INSTANCE_MAP.get(clazz);
		if (service == null) {
			try {
				service = clazz.newInstance();
				INSTANCE_MAP.put(clazz, service);
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			}
		}
		service.controller = controller;
		return service;
	}
}
