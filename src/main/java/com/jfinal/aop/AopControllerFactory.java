package com.jfinal.aop;

import com.jfinal.core.Controller;
import com.jfinal.core.ControllerFactory;

/**
 * AopControllerFactory 用于注入依赖对象并更好支持 AOP，其优点如下：
 * 1：使用 @Inject 自动化注入并 enhance 对象，免去业务层 AOP 必须手动 enhance 的步骤
 * 
 * 2：免去业务层维护单例的样板式代码，例如下面代码可以删掉了：
 *    public static final MyService me = new MyService();
 * 
 * 
 * 基本用法如下：
 * 1：配置
 *    me.setControllerFactory(new AopControllerFactory());
 *    
 * 2：Controller 中注入业务层，也可以注入任何其它类，不一定非得是 Service
 *    public class MyController extends Controller {
 *    
 *       @Inject
 *       MyService service;
 *       
 *       public void index() {
 *          render(service.doIt());
 *       }
 *    }
 *    
 * 3：Service 注入另一个 Service，也可以注入任何其它类，不一定非得是 Service
 *    public class MyService {
 *    
 *       @Inject
 *       OtherService other;		// OtherService 内部还可以继续接着注入
 *       
 *       public void doIt() {
 *          other.doOther();
 *       }
 *    }
 * 
 * 4：AopControllerFactory 默认处理了从 Controller 为源头的依赖与注入链条，如果希望在拦截器
 *    使用注入功能，可以使用如下的方式：
 *    public class MyInterceptor implements Interceptor {
 *      
 *       MyService srv = Aop.get(MyService.class);
 *       
 *       public void intercept(Invocation inv) {
 *          srv.doIt();
 *       }
 *    }
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
public class AopControllerFactory extends ControllerFactory {
	
	@Override
	public Controller getController(Class<? extends Controller> controllerClass) throws ReflectiveOperationException {
		Controller c = controllerClass.newInstance();
		// Aop.getAopFactory().inject((Class)controllerClass, c);
		return Aop.inject(c);
	}
}




