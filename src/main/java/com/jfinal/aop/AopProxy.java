package com.jfinal.aop;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import com.jfinal.aop.Enhancer;

/**
 * AopProxy 是工具类 Aop 功能的具体实现，详细用法见 Aop
 */
public class AopProxy {
	
	// 单例缓存
	protected ConcurrentHashMap<Class<?>, Object> singletonCache = new ConcurrentHashMap<Class<?>, Object>();
	
	protected static int MAX_INJECT_DEPTH = 7;			// 最大注入深度 
	
	protected YesOrNo enhance = YesOrNo.YES;				// 默认增强
	protected YesOrNo singleton = YesOrNo.YES;			// 默认单例
	protected int injectDepth = 3;						// 默认注入深度
	
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> targetClass) {
		try {
			// Aop.get(obj.getClass()) 可以用 Aop.inject(obj)，所以注掉下一行代码 
			// targetClass = (Class<T>)getUsefulClass(targetClass);
			
			Object ret;
			if (singleton == YesOrNo.NO) {
				ret = createObject(targetClass, enhance);
				inject(targetClass, ret, injectDepth);
				return (T)ret;
			}
			
			ret = singletonCache.get(targetClass);
			if (ret == null) {
				synchronized (targetClass) {
					ret = singletonCache.get(targetClass);
					if (ret == null) {
						ret = createObject(targetClass, enhance);
						inject(targetClass, ret, injectDepth);
						singletonCache.put(targetClass, ret);
					}
				}
			}
			return (T)ret;
		}
		catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
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
			
			YesOrNo enhance = inject.enhance();
			if (enhance == YesOrNo.DEFAULT) {
				enhance = this.enhance;
			}
			
			YesOrNo singleton = inject.singleton();
			if (singleton == YesOrNo.DEFAULT) {
				singleton = this.singleton;
			}
			
			Object fieldInjectedObject = getOrCreateObject(fieldInjectedClass, enhance, singleton);
			field.setAccessible(true);
			field.set(targetObject, fieldInjectedObject);
			
			// 递归调用，为当前被注入的对象进行注入
			this.inject(fieldInjectedObject.getClass(), fieldInjectedObject, injectDepth);
		}
	}
	
	protected Object getOrCreateObject(Class<?> targetClass, YesOrNo enhance, YesOrNo singleton) throws ReflectiveOperationException {
		if (singleton == YesOrNo.NO) {
			return createObject(targetClass, enhance);
		}
		
		Object ret = singletonCache.get(targetClass);
		if (ret == null) {
			synchronized (targetClass) {
				ret = singletonCache.get(targetClass);
				if (ret == null) {
					ret = createObject(targetClass, enhance);
					singletonCache.put(targetClass, ret);
				}
			}
		}
		
		return ret;
	}
	
	/**
	 * 由于上层已经处理过 singleton，所以 Enhancer.enhance() 方法中不必关心 singleton
	 */
	protected Object createObject(Class<?> targetClass, YesOrNo enhance) throws ReflectiveOperationException {
		return (enhance == YesOrNo.YES) ? Enhancer.enhance(targetClass) : targetClass.newInstance();
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
	 * 设置被注入的对象是否被增强，可使用 @Inject(enhance = YesOrNo.NO) 覆盖此默认值
	 */
	public AopProxy setEnhance(boolean enhance) {
		this.enhance = enhance ? YesOrNo.YES : YesOrNo.NO;
		return this;
	}
	
	public boolean isEnhance() {
		return enhance == YesOrNo.YES;
	}
	
	/**
	 * 设置被注入的对象是否为单例，可使用 @Inject(singleton = YesOrNo.NO) 覆盖此默认值 
	 */
	public AopProxy setSingleton(boolean singleton) {
		this.singleton = singleton ? YesOrNo.YES : YesOrNo.NO;
		return this;
	}
	
	public boolean isSingleton() {
		return singleton == YesOrNo.YES;
	}
	
	/**
	 * 设置注入深度，避免被注入类在具有循环依赖时造成无限循环
	 */
	public AopProxy setInjectDepth(int injectDepth) {
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




