/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.aop;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.proxy.Proxy;
import com.jfinal.validate.Validator;

/**
 * AopFactory 是工具类 Aop 功能的具体实现，详细用法见 Aop
 */
public class AopFactory {
	
	// 单例缓存
	protected ConcurrentHashMap<Class<?>, Object> singletonCache = new ConcurrentHashMap<Class<?>, Object>();
	
	// 支持循环注入
	protected ThreadLocal<HashMap<Class<?>, Object>> singletonTl = ThreadLocal.withInitial(() -> new HashMap<>());
	protected ThreadLocal<HashMap<Class<?>, Object>> prototypeTl = ThreadLocal.withInitial(() -> new HashMap<>());
	
	// 父类到子类、接口到实现类之间的映射关系
	protected HashMap<Class<?>, Class<?>> mapping = null;
	
	protected boolean singleton = true;					// 默认单例
	
	protected boolean injectSuperClass = false;			// 默认不对超类进行注入
	
	public <T> T get(Class<T> targetClass) {
		try {
			return doGet(targetClass);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T doGet(Class<T> targetClass) throws ReflectiveOperationException {
		// Aop.get(obj.getClass()) 可以用 Aop.inject(obj)，所以注掉下一行代码
		// targetClass = (Class<T>)getUsefulClass(targetClass);
		
		targetClass = (Class<T>)getMappingClass(targetClass);
		
		Singleton si = targetClass.getAnnotation(Singleton.class);
		boolean singleton = (si != null ? si.value() : this.singleton);
		
		if (singleton) {
			return doGetSingleton(targetClass);
		} else {
			return doGetPrototype(targetClass);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T doGetSingleton(Class<T> targetClass) throws ReflectiveOperationException {
		Object ret = singletonCache.get(targetClass);
		if (ret != null) {
			return (T)ret;
		}
		
		HashMap<Class<?>, Object> map = singletonTl.get();
		int size = map.size();
		if (size > 0) {
			ret = map.get(targetClass);
			if (ret != null) {		// 发现循环注入
				return (T)ret;
			}
		}
		
		synchronized (this) {
			try {
				ret = singletonCache.get(targetClass);
				if (ret == null) {
					ret = createObject(targetClass);
					map.put(targetClass, ret);
					doInject(targetClass, ret);
					singletonCache.put(targetClass, ret);
				}
				return (T)ret;
			} finally {
				if (size == 0) {		// 仅顶层才需要 remove()
					singletonTl.remove();
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T doGetPrototype(Class<T> targetClass) throws ReflectiveOperationException {
		Object ret;
		
		HashMap<Class<?>, Object> map = prototypeTl.get();
		int size = map.size();
		if (size > 0) {
			ret = map.get(targetClass);
			if (ret != null) {		// 发现循环注入
				// return (T)ret;
				return (T)createObject(targetClass);
			}
		}
		
		try {
			ret = createObject(targetClass);
			map.put(targetClass, ret);
			doInject(targetClass, ret);
			return (T)ret;
		} finally {
			if (size == 0) {		// 仅顶层才需要 clear()
				map.clear();
			}
		}
	}
	
	public <T> T inject(T targetObject) {
		try {
			doInject(targetObject.getClass(), targetObject);
			return targetObject;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
	
	// 方法原型的参数测试过可以是：Class<? super T> targetClass, T targetObject
	public <T> T inject(Class<T> targetClass, T targetObject) {
		try {
			doInject(targetClass, targetObject);
			return targetObject;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void doInject(Class<?> targetClass, Object targetObject) throws ReflectiveOperationException {
		targetClass = getUsefulClass(targetClass);
		Field[] fields = targetClass.getDeclaredFields();
		if (fields.length != 0) {
			for (Field field : fields) {
				Inject inject = field.getAnnotation(Inject.class);
				if (inject == null) {
					continue ;
				}
				
				Class<?> fieldInjectedClass = inject.value();
				if (fieldInjectedClass == Void.class) {
					fieldInjectedClass = field.getType();
				}
				
				Object fieldInjectedObject = doGet(fieldInjectedClass);
				field.setAccessible(true);
				field.set(targetObject, fieldInjectedObject);
			}
		}
		
		// 是否对超类进行注入
		if (injectSuperClass) {
			Class<?> c = targetClass.getSuperclass();
			if (c != Controller.class && c != Object.class && c != Validator.class && c != Model.class && c != null) {
				doInject(c, targetObject);
			}
		}
	}
	
	protected Object createObject(Class<?> targetClass) throws ReflectiveOperationException {
		return Proxy.get(targetClass);
	}
	
	/**
	 * 字符串包含判断之 "_$$_" 支持 javassist，"$$Enhancer" 支持 cglib
	 * 
	 * 被 cglib、guice 增强过的类需要通过本方法获取到被增强之前的类型
	 * 否则调用其 targetClass.getDeclaredFields() 方法时
	 * 获取到的是一堆 cglib guice 生成类中的 Field 对象
	 * 而被增强前的原类型中的 Field 反而获取不到
	 */
	protected Class<?> getUsefulClass(Class<?> clazz) {
		// com.demo.blog.Blog$$EnhancerByCGLIB$$69a17158
		// return (Class<? extends Model>)((modelClass.getName().indexOf("EnhancerByCGLIB") == -1 ? modelClass : modelClass.getSuperclass()));
		// return (Class<?>)(clazz.getName().indexOf("$$EnhancerBy") == -1 ? clazz : clazz.getSuperclass());
	    String n = clazz.getName();
	    return (Class<?>)(n.indexOf("_$$_") > -1 || n.indexOf("$$Enhancer") > -1 ? clazz.getSuperclass() : clazz);
	}
	
	/**
	 * 设置被注入的对象是否为单例，可使用 @Singleton(boolean) 覆盖此默认值 
	 */
	public AopFactory setSingleton(boolean singleton) {
		this.singleton = singleton;
		return this;
	}
	
	public boolean isSingleton() {
		return singleton;
	}
	
	/**
	 * 设置是否对超类进行注入
	 */
	public AopFactory setInjectSuperClass(boolean injectSuperClass) {
		this.injectSuperClass = injectSuperClass;
		return this;
	}
	
	public boolean isInjectSuperClass() {
		return injectSuperClass;
	}
	
	public AopFactory addSingletonObject(Class<?> type, Object singletonObject) {
		if (type == null) {
			throw new IllegalArgumentException("type can not be null");
		}
		if (singletonObject == null) {
			throw new IllegalArgumentException("singletonObject can not be null");
		}
		if (singletonObject instanceof Class) {
			throw new IllegalArgumentException("singletonObject can not be Class type");
		}
		
		if ( ! (type.isAssignableFrom(singletonObject.getClass())) ) {
			throw new IllegalArgumentException(singletonObject.getClass().getName() + " can not cast to " + type.getName());
		}
		
		// Class<?> type = getUsefulClass(singletonObject.getClass());
		if (singletonCache.putIfAbsent(type, singletonObject) != null) {
			throw new RuntimeException("Singleton object already exists for type : " + type.getName());
		}
		
		return this;
	}
	
	public AopFactory addSingletonObject(Object singletonObject) {
		Class<?> type = getUsefulClass(singletonObject.getClass());
		return addSingletonObject(type, singletonObject);
	}
	
	public synchronized <T> AopFactory addMapping(Class<T> from, Class<? extends T> to) {
		if (from == null || to == null) {
			throw new IllegalArgumentException("The parameter from and to can not be null");
		}
		
		if (mapping == null) {
			mapping = new HashMap<Class<?>, Class<?>>(128, 0.25F);
		} else if (mapping.containsKey(from)) {
			throw new RuntimeException("Class already mapped : " + from.getName());
		}
		
		mapping.put(from, to);
		return this;
	}
	
	public <T> AopFactory addMapping(Class<T> from, String to) {
		try {
			@SuppressWarnings("unchecked")
			Class<T> toClass = (Class<T>)Class.forName(to.trim());
			if (from.isAssignableFrom(toClass)) {
				return addMapping(from, toClass);
			} else {
				throw new IllegalArgumentException("The parameter \"to\" must be the subclass or implementation of the parameter \"from\"");
			}
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 获取父类到子类的映射值，或者接口到实现类的映射值
	 * @param from 父类或者接口 
	 * @return 如果映射存在则返回映射值，否则返回参数 from 的值
	 */
	public Class<?> getMappingClass(Class<?> from) {
		if (mapping != null) {
			Class<?> ret = mapping.get(from);
			return ret != null ? ret : from;
		} else {
			return from;
		}
	}
}




