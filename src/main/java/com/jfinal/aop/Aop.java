package com.jfinal.aop;

/**
 * Aop 支持在任意时空便捷使用 Aop
 * 
 * Aop 主要功能：
 * 1：Aop.get(Class) 根据 Class 去创建对象，然后对创建好的对象进行依赖注入
 * 
 * 2：Aop.inject(Object) 对传入的对象进行依赖注入
 * 
 * 3：Aop.inject(...) 与 Aop.get(...) 的区别是前者只针对传入的对象之中的属性进行注入。
 *    而后者先要使用 Class 去创建对象，创建完对象以后对该对象之中的属性进行注入。
 *    简单一句话：get(...) 比 inject(...) 多了一个对象创建的过程
 *    
 * 4：是否要 enhance 与 singleton 根据 Aop.setEhnace(...)、Aop.setSingleton(...) 配置来操作
 * 
 * 5：在 @Inject(...) 指定 enhance 与 singleton 的配置可以覆盖掉默认配置
 * 
 * 
 * 基本用法：
 * 1：先定义业务
 *    public class Service {
 *       @Inject
 *       OtherService otherSrv;
 *       
 *       @Before(Aaa.class)
 *       public void doIt() {
 *          ...
 *       }
 *    }
 *    
 *    public class OtherService {
 *       @Before(Bbb.class)
 *       public void doOther() {
 *          ...
 *       }
 *    }
 *    
 * 
 * 2：只进行注入，对象自己创建
 *    Service srv = Aop.inject(new Service());
 *    srv.doIt();
 *    Aop.injectd(...) 会对 OtherService otherSrv 进行注入，并且对 otherSrv 进行 ehnace，
 *    所以 OtherService.doOther() 方法上的 Bbb 拦截器会生效
 *    
 * 3：创建对象并注入
 *    Service srv = Aop.get(Service.class);
 *    srv.doIt();
 *    Aop.get(...) 用法对 OtherService otherSrv 的处理方式完全一样，在此基础之上 Service 自身也会被
 *    enhance，所以 doIt() 上的 Aaa 拦截器会生效，而 Aop.inject(...) 用法下 Aaa 拦截器不起用用
 * 
 * 4：以上两点中的 enhance 还取决于配置
 *    Aop.setEnhance(false) 配置以后，只注入对象，但被注入对象不进行 enhance， Aaa、Bbb 拦截器都不会生效
 *    
 * 
 * 
 * 
 * 高级用法：
 * 1：@Inject 注解默认注入属性自身类型的对象，可以通过如下代码指定被注入的类型：
 *    @Inject(UserServiceImpl.class)			// 此处的 UserServiceImpl 为 UserService 的子类或实现类
 *    UserService userService;
 * 
 * 2：被注入对象默认会被 enhance 增强，可以通过 Aop.setEnhance(false) 配置默认不增强
 * 
 * 3：被注入对象默认是 singleton 单例，可以通过 Aop.setSingleton(false) 配置默认不为单例
 * 
 * 4：可以在 @Inject 注解中直接配置 enhance 增强与 singleton 单例：
 *    @Inject(enhance=YesOrNo.NO, singleton=YesOrNo.YES)
 *    注意：如上在 @Inject 直接配置会覆盖掉 2、3 中 setEnhance()/setSingleton() 方法配置的默认值
 * 
 * 5：如上 2、3、4 中的配置，建议的用法是：先用 setEnhance()/setSingleton() 配置大多数情况，然后在个别
 *    违反上述配置的情况下在 @Inject 中直接 enhance、singleton 来覆盖默认配置，这样可以节省大量代码
 */
public class Aop {
	
	private static AopProxy aopProxy = new AopProxy();
	
	public static <T> T get(Class<T> targetClass) {
		return aopProxy.get(targetClass);
	}
	
	public static <T> T inject(T targetObject) {
		return aopProxy.inject(targetObject);
	}
	
	public static <T> T inject(T targetObject, int injectDepth) {
		return aopProxy.inject(targetObject, injectDepth);
	}
	
	/* 通过 Aop.getAopProxy().inject(...) 可调用如下两个方法，不直接开放出来
	public static void inject(Class<?> targetClass, Object targetObject) throws ReflectiveOperationException {
		aopProxy.inject(targetClass, targetObject);
	}
	
	public static void inject(Class<?> targetClass, Object targetObject, int injectDepth) throws ReflectiveOperationException {
		aopProxy.inject(targetClass, targetObject, injectDepth);
	}*/
	
	/**
	 * 设置 AopProxy，便于扩展自己的 AopProxy 实现
	 */
	public static void setAopProxy(AopProxy aopProxy) {
		if (aopProxy == null) {
			throw new IllegalArgumentException("aopProxy can not be null");
		}
		Aop.aopProxy = aopProxy;
	}
	
	public static AopProxy getAopProxy() {
		return aopProxy;
	}
	
	/**
	 * 设置被注入的对象是否被增强，可使用 @Inject(enhance = YesOrNo.NO) 覆盖此默认值
	 */
	public static void setEnhance(boolean enhance) {
		aopProxy.setEnhance(enhance);
	}
	
	public static boolean isEnhance() {
		return aopProxy.isEnhance();
	}
	
	/**
	 * 设置被注入的对象是否为单例，可使用 @Inject(singleton = YesOrNo.NO) 覆盖此默认值 
	 */
	public static void setSingleton(boolean singleton) {
		aopProxy.setSingleton(singleton);
	}
	
	public static boolean isSingleton() {
		return aopProxy.isSingleton();
	}
	
	/**
	 * 设置注入深度，避免被注入类在具有循环依赖时造成无限循环
	 */
	public static void setInjectDepth(int injectDepth) {
		aopProxy.setInjectDepth(injectDepth);
	}
	
	public static int getInjectDepth() {
		return aopProxy.getInjectDepth();
	}
}




