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
 *    简单一句话：get(...) 比 inject(...) 多了一个目标对象的创建过程
 *    
 * 4：是否要 singleton 与 enhance 根据 Aop.setSingleton(...)、Aop.setEhnace(...) 配置来操作
 * 
 * 5：在目标类上使用注解 Singleton 与注解 Enhance 可以覆盖掉上面的默认配置
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
 *    Aop.inject(...) 会对 OtherService otherSrv 进行注入，并且对 otherSrv 进行 ehnace，
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
 * 注意：后续的 jfinal 3.6 版本将根据目标类是否配置了拦截器而进行增强，会去除一切与 enhance 有关的配置与代码
 *      这里与 enhance 有关的配置仅为 jfinal 3.5 到 jfinal 3.6 的过渡
 * 
 * 
 * 
 * 高级用法：
 * 1：@Inject 注解默认注入属性自身类型的对象，可以通过如下代码指定被注入的类型：
 *    @Inject(UserServiceImpl.class)			// 此处的 UserServiceImpl 为 UserService 的子类或实现类
 *    UserService userService;
 * 
 * 2：被注入对象默认是 singleton 单例，可以通过 Aop.setSingleton(false) 配置默认不为单例
 * 
 * 3：被注入对象默认会被 enhance 增强，可以通过 Aop.setEnhance(false) 配置默认不增强
 * 
 * 4：可以在目标类中中直接配置注解 Singleton 与注解 Enhance：
 *    @Singleton(false)
 *    @Enhance(false)
 *    public class MyService {...}
 *    
 *    注意：如上在配置会覆盖掉 2、3 中 setSingleton()/setEnhance() 方法配置的默认值
 * 
 * 5：如上 2、3、4 中的配置，建议的用法是：先用 /setSingleton()/setEnhance() 配置大多数情况，然后在个别
 *    违反上述配置的情况下使用 Singleton 注解与 Enhance 注解来覆盖默认配置，这样可以节省大量代码
 */
public class Aop {
	
	private static AopFactory aopFactory = new AopFactory();
	
	public static <T> T get(Class<T> targetClass) {
		return aopFactory.get(targetClass);
	}
	
	public static <T> T get(Class<T> targetClass, int injectDepth) {
		return aopFactory.get(targetClass, injectDepth);
	}
	
	public static <T> T inject(T targetObject) {
		return aopFactory.inject(targetObject);
	}
	
	public static <T> T inject(T targetObject, int injectDepth) {
		return aopFactory.inject(targetObject, injectDepth);
	}
	
	/**
	 * 添加单例对象
	 * 
	 * 由于 Aop 创建对象时不支持为构造方法传递参数，故添加此方法
	 * 
	 * <pre>
	 * 示例：
	 * // Service 类的构造方法中传入了两个参数
	 * Service service = new Service(paraAaa, paraBbb);
	 * Aop.addSingletonObject(service);
	 * 
	 * // 上面代码添加完成以后，可以在任何地方通过下面的方式获取单例对象 
	 * service = Aop.get(Service.class);
	 * 
	 * // 被添加进去的对象还可以用于注入
	 * @Inject
	 * Service service;
	 * 
	 * // 在添加为单例对象之前还可以先为其注入依赖对象
	 * Service service = new Service(paraAaa, paraBbb);
	 * Aop.inject(service);		// 这里是对 Service 进行依赖注入
	 * Aop.addSingletonObject(service);
	 * </pre>
	 */
	public static void addSingletonObject(Object singletonObject) {
		aopFactory.addSingletonObject(singletonObject);
	}
	
	/**
	 * 添加父类到子类的映射，或者接口到实现类的映射。
	 * 
	 * 该方法用于为父类、抽象类、或者接口注入子类或者实现类
	 * 
	 * <pre>
	 * 示例：
	 * // 定义接口
	 * public interface IService {
	 *    public void justDoIt();
	 * }
	 * 
	 * // 定义接口的实现类
	 * public class MyService implements IService {
	 *   public void justDoIt() {
	 *      ...
	 *   }
	 * }
	 * 
	 * // 添加接口与实现类的映射关系
	 * Aop.addMapping(IService.class, MyService.class)
	 * 
	 * public class MyController {
	 * 
	 *    // 由于前面添加了接口与实现类的关系，所以下面将被注入实现类 MyService 对象
	 *    @Inject
	 *    IService service
	 *    
	 *    public action() {
	 *       service.justDoIt();
	 *    }
	 * }
	 * 
	 * 如上所示，通过建立了 IService 与 MyService 的映射关系，在 @Inject 注入的时候
	 * 就会注入映射好的实现类，当然也可以通过在 @Inject 注解中指定实现类来实现：
	 * 
	 * @Inject(MyService.class)
	 * IService service
	 * 
	 * 但是上面的的方法是写死在代码中的，不方便改变实现类
	 * 
	 * </pre>
	 * 
	 * @param from 父类或者接口
	 * @param to 父类的子类或者接口的实现类
	 */
	public static <T> void addMapping(Class<T> from, Class<? extends T> to) {
		aopFactory.addMapping(from, to);
	}
	
	/**
	 * 功能与 addMapping(Class<T> from, Class<? extends T> to) 相同，仅仅是第二个参数
	 * 由 Class 改为 String 类型，便于从外部配置文件传递 String 参数过来
	 * 
	 * <pre>
	 * 示例：
	 * Aop.addMapping(IService.class, "com.xxx.MyService")
	 * 
	 * 以上代码的参数 "com.xxx.MyService" 可通过外部配置文件传入，便于通过配置文件切换接口的
	 * 实现类：
	 * Aop.addMapping(IService.class, PropKit.get("ServiceImpl");
	 * 
	 * </pre>
	 */
	public static <T> void addMapping(Class<T> from, String to) {
		aopFactory.addMapping(from, to);
	}
	
	/* 通过 Aop.getAopFactory().inject(...) 可调用如下两个方法，不直接开放出来
	public static void inject(Class<?> targetClass, Object targetObject) {
		aopFactory.inject(targetClass, targetObject);
	}
	
	public static void inject(Class<?> targetClass, Object targetObject, int injectDepth) {
		aopFactory.inject(targetClass, targetObject, injectDepth);
	}*/
	
	/**
	 * 设置 AopFactory，便于扩展自己的 AopFactory 实现
	 */
	public static void setAopFactory(AopFactory aopFactory) {
		if (aopFactory == null) {
			throw new IllegalArgumentException("aopFactory can not be null");
		}
		Aop.aopFactory = aopFactory;
	}
	
	public static AopFactory getAopFactory() {
		return aopFactory;
	}
	
	/**
	 * 设置被注入的对象是否被增强，可使用 @Enhace(boolean) 覆盖此默认值
	 * 
	 * 由于下一版本的 jfinal 3.6 将根据目标类中是否存在 Before 注解
	 * 来决定是否增强，所以该 setEnhance 方法仅仅是一个过渡功能，不建议使用
	 */
	@Deprecated
	public static void setEnhance(boolean enhance) {
		aopFactory.setEnhance(enhance);
	}
	
	/**
	 * 设置被注入的对象是否为单例，可在目标类上使用 @Singleton(boolean) 覆盖此默认值 
	 */
	public static void setSingleton(boolean singleton) {
		aopFactory.setSingleton(singleton);
	}
	
	public static boolean isSingleton() {
		return aopFactory.isSingleton();
	}
	
	/**
	 * 设置注入深度，避免被注入类在具有循环依赖时造成无限循环
	 */
	public static void setInjectDepth(int injectDepth) {
		aopFactory.setInjectDepth(injectDepth);
	}
	
	public static int getInjectDepth() {
		return aopFactory.getInjectDepth();
	}
}




