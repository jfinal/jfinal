/**
 * Copyright (c) 2011-2019, James Zhan 詹波 (jfinal@126.com).
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

import com.jfinal.core.Const;

/**
 * AopManager
 */
public class AopManager {
	
	private boolean injectDependency = Const.DEFAULT_INJECT_DEPENDENCY;
	
	private static final AopManager me = new AopManager();
	
	private AopManager() {}
	
	public static AopManager me() {
		return me;
	}
	
	/**
	 * 设置对 Controller、Interceptor、Validator 进行依赖注入，默认值为 false
	 * 
	 * 被注入对象默认为 singleton，可以通过 AopManager.me().setSingleton(boolean) 配置
	 * 该默认值。
	 * 
	 * 也可通过在被注入的目标类上使用 Singleton 注解覆盖上述默认值，注解配置
	 * 优先级高于默认配置
	 * 
	 * 注意：该配置仅针对于配置 jfinal web。而 Aop.get(...)、Aop.inject(...) 默认就会进行注入，
	 *      无需配置
	 */
	public void setInjectDependency(boolean injectDependency) {
		this.injectDependency = injectDependency;
	}
	
	public boolean isInjectDependency() {
		return injectDependency;
	}
	
	/**
	 * 设置是否对超类进行注入
	 */
	public void setInjectSuperClass(boolean injectSuperClass) {
		Aop.aopFactory.setInjectSuperClass(injectSuperClass);
	}
	
	public boolean isInjectSuperClass() {
		return Aop.aopFactory.isInjectSuperClass();
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
	 * AopManager.me().addSingletonObject(service);
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
	 * AopManager.me().addSingletonObject(service);
	 * </pre>
	 */
	public void addSingletonObject(Object singletonObject) {
		Aop.aopFactory.addSingletonObject(singletonObject);
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
	 * AopManager.me().addMapping(IService.class, MyService.class)
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
	public <T> void addMapping(Class<T> from, Class<? extends T> to) {
		Aop.aopFactory.addMapping(from, to);
	}
	
	/**
	 * 功能与 addMapping(Class<T> from, Class<? extends T> to) 相同，仅仅是第二个参数
	 * 由 Class 改为 String 类型，便于从外部配置文件传递 String 参数过来
	 * 
	 * <pre>
	 * 示例：
	 * AopManager.me().addMapping(IService.class, "com.xxx.MyService")
	 * 
	 * 以上代码的参数 "com.xxx.MyService" 可通过外部配置文件传入，便于通过配置文件切换接口的
	 * 实现类：
	 * AopManager.me().addMapping(IService.class, PropKit.get("ServiceImpl");
	 * 
	 * </pre>
	 */
	public <T> void addMapping(Class<T> from, String to) {
		Aop.aopFactory.addMapping(from, to);
	}
	
	/**
	 * 设置 AopFactory，便于扩展自己的 AopFactory 实现
	 */
	public void setAopFactory(AopFactory aopFactory) {
		if (aopFactory == null) {
			throw new IllegalArgumentException("aopFactory can not be null");
		}
		Aop.aopFactory = aopFactory;
	}
	
	public AopFactory getAopFactory() {
		return Aop.aopFactory;
	}
	
	/**
	 * 设置被注入的对象是否被增强，可使用 @Enhace(boolean) 覆盖此默认值
	 * 
	 * 后续的 jfinal 版本将考虑根据目标类是否配置了拦截器进行增强的新设计，
	 * 可能会去除与 enhance 有关的配置与代码，所以与 enhance 有关的配置
	 * 已被 @Deprecated，不建议使用
	 */
	@Deprecated
	public void setEnhance(boolean enhance) {
		Aop.aopFactory.setEnhance(enhance);
	}
	
	/**
	 * 设置被注入的对象是否为单例，可在目标类上使用 @Singleton(boolean) 覆盖此默认值 
	 */
	public void setSingleton(boolean singleton) {
		Aop.aopFactory.setSingleton(singleton);
	}
	
	public boolean isSingleton() {
		return Aop.aopFactory.isSingleton();
	}
}
