package com.jfinal.aop;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AopFactory 是工具类 Aop 功能的具体实现，详细用法见 Aop
 */
public class AopFactory {
	
	// 单例缓存
	protected ConcurrentHashMap<Class<?>, Object> singletonCache = new ConcurrentHashMap<Class<?>, Object>();
	
	// 父类到子类、接口到实现类之间的映射关系
	protected HashMap<Class<?>, Class<?>> mapping = null;
	
	protected static int MAX_INJECT_DEPTH = 7;			// 最大注入深度
	
	protected boolean singleton = true;					// 默认单例
	protected boolean enhance = true;					// 默认增强
	protected int injectDepth = 3;						// 默认注入深度
	
	public <T> T get(Class<T> targetClass) {
		try {
			return doGet(targetClass, injectDepth);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
	
	public <T> T get(Class<T> targetClass, int injectDepth) {
		try {
			return doGet(targetClass, injectDepth);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T doGet(Class<T> targetClass, int injectDepth) throws ReflectiveOperationException {
		// Aop.get(obj.getClass()) 可以用 Aop.inject(obj)，所以注掉下一行代码
		// targetClass = (Class<T>)getUsefulClass(targetClass);
		
		targetClass = (Class<T>)getMappingClass(targetClass);
		
		Singleton si = targetClass.getAnnotation(Singleton.class);
		boolean singleton = (si != null ? si.value() : this.singleton);
		
		Object ret;
		if ( ! singleton ) {
			ret = createObject(targetClass);
			inject(targetClass, ret, injectDepth);
			return (T)ret;
		}
		
		ret = singletonCache.get(targetClass);
		if (ret == null) {
			synchronized (this) {
				ret = singletonCache.get(targetClass);
				if (ret == null) {
					ret = createObject(targetClass);
					inject(targetClass, ret, injectDepth);
					singletonCache.put(targetClass, ret);
				}
			}
		}
		
		return (T)ret;
	}
	
	public <T> T inject(T targetObject) {
		try {
			inject(targetObject.getClass(), targetObject, injectDepth);
			return targetObject;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
	
	public <T> T inject(T targetObject, int injectDepth) {
		try {
			inject(targetObject.getClass(), targetObject, injectDepth);
			return targetObject;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
	
	// 方法原型的参数测试过可以是：Class<? super T> targetClass, T targetObject
	public <T> T inject(Class<T> targetClass, T targetObject) throws ReflectiveOperationException {
		inject(targetClass, targetObject, injectDepth);
		return targetObject;
	}
	
	public void inject(Class<?> targetClass, Object targetObject, int injectDepth) throws ReflectiveOperationException {
		if ((injectDepth--) <= 0) {
			return ;
		}
		
		targetClass = getUsefulClass(targetClass);
		Field[] fields = targetClass.getDeclaredFields();
		if (fields.length == 0) {
			return ;
		}
		
		for (Field field : fields) {
			Inject inject = field.getAnnotation(Inject.class);
			if (inject == null) {
				continue ;
			}
			
			Class<?> fieldInjectedClass = inject.value();
			if (fieldInjectedClass == Void.class) {
				fieldInjectedClass = field.getType();
			}
			
			Object fieldInjectedObject = doGet(fieldInjectedClass, injectDepth);
			field.setAccessible(true);
			field.set(targetObject, fieldInjectedObject);
		}
	}
	
	/**
	 * 由于上层已经处理过 singleton，所以 Enhancer.enhance() 方法中不必关心 singleton
	 */
	@SuppressWarnings("deprecation")
	protected Object createObject(Class<?> targetClass) throws ReflectiveOperationException {
		Enhance en = targetClass.getAnnotation(Enhance.class);
		boolean enhance = (en != null ? en.value() : this.enhance);
		
		return enhance ? com.jfinal.aop.Enhancer.enhance(targetClass) : targetClass.newInstance();
	}
	
	/**
	 * 被 cglib、guice 增强过的类需要通过本方法获取到被增强之前的类型
	 * 否则调用其 targetClass.getDeclaredFields() 方法时
	 * 获取到的是一堆 cglib guice 生成类中的 Field 对象
	 * 而被增强前的原类型中的 Field 反而获取不到
	 */
	protected Class<?> getUsefulClass(Class<?> clazz) {
		// com.demo.blog.Blog$$EnhancerByCGLIB$$69a17158
		// return (Class<? extends Model>)((modelClass.getName().indexOf("EnhancerByCGLIB") == -1 ? modelClass : modelClass.getSuperclass()));
		return (Class<?>)(clazz.getName().indexOf("$$EnhancerBy") == -1 ? clazz : clazz.getSuperclass());
	}
	
	/**
	 * 设置被注入的对象是否被增强，可使用 @Enhace(boolean) 覆盖此默认值
	 * 
	 * 由于下一版本的 jfinal 3.6 将根据目标类中是否配置了拦截器来决定是否增强，
	 * 所以该 setEnhance 方法仅仅是一个过渡功能，不建议使用
	 */
	@Deprecated
	public AopFactory setEnhance(boolean enhance) {
		this.enhance = enhance;
		return this;
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
	 * 设置注入深度，避免被注入类在具有循环依赖时造成无限循环
	 */
	public AopFactory setInjectDepth(int injectDepth) {
		if (injectDepth <= 0) {
			throw new IllegalArgumentException("注入层数必须大于 0");
		}
		if (injectDepth > MAX_INJECT_DEPTH) {
			throw new IllegalArgumentException("为保障性能，注入层数不能超过 " + MAX_INJECT_DEPTH);
		}
		
		this.injectDepth = injectDepth;
		return this;
	}
	
	public int getInjectDepth() {
		return injectDepth;
	}
	
	public AopFactory addSingletonObject(Object singletonObject) {
		if (singletonObject == null) {
			throw new IllegalArgumentException("singletonObject can not be null");
		}
		if (singletonObject instanceof Class) {
			throw new IllegalArgumentException("singletonObject can not be Class type");
		}
		
		Class<?> type = getUsefulClass(singletonObject.getClass());
		if (singletonCache.putIfAbsent(type, singletonObject) != null) {
			throw new RuntimeException("Singleton object already exists for type : " + type.getName());
		}
		
		return this;
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


/* 未来考虑不再支持对象的 Aop，只支持 Class 的 Aop
public <T> T get(T targetObject) {
	try {
		inject(injectDepth, targetObject.getClass(), targetObject);
		return targetObject;
	}
	catch (Exception e) {
		throw new RuntimeException(e);
	}
}*/




