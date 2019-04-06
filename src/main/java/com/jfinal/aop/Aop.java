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
 * 4：是否要 singleton 与 enhance 根据 AopManager.me().setSingleton(...)、AopManager.me().setEhnace(...) 配置来操作
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
 *    AopManager.me().setEnhance(false) 配置以后，只注入对象，但被注入对象不进行 enhance， Aaa、Bbb 拦截器都不会生效
 *    
 *    
 * 注意：后续的 jfinal 版本将考虑根据目标类是否配置了拦截器进行增强的新设计，可能会去除与 enhance 有关的配置与代码
 *      所以与 enhance 有关的配置已被 @Deprecated，不建议使用
 * 
 * 
 * 
 * 高级用法：
 * 1：@Inject 注解默认注入属性自身类型的对象，可以通过如下代码指定被注入的类型：
 *    @Inject(UserServiceImpl.class)			// 此处的 UserServiceImpl 为 UserService 的子类或实现类
 *    UserService userService;
 * 
 * 2：被注入对象默认是 singleton 单例，可以通过 AopManager.me().setSingleton(false) 配置默认不为单例
 * 
 * 3：被注入对象默认会被 enhance 增强，可以通过 AopManager.me().setEnhance(false) 配置默认不增强
 * 
 * 4：可以在目标类中中直接配置注解 Singleton 与注解 Enhance：
 *    @Singleton(false)
 *    @Enhance(false)
 *    public class MyService {...}
 *    
 *    注意：以上代码中的注解会覆盖掉 2、3 中 setSingleton()/setEnhance() 方法配置的默认值
 * 
 * 5：如上 2、3、4 中的配置，建议的用法是：先用 /setSingleton()/setEnhance() 配置大多数情况，然后在个别
 *    违反上述配置的情况下使用 Singleton 注解与 Enhance 注解来覆盖默认配置，这样可以节省大量代码
 */
public class Aop {
	
	static AopFactory aopFactory = new AopFactory();
	
	public static <T> T get(Class<T> targetClass) {
		return aopFactory.get(targetClass);
	}
	
	public static <T> T inject(T targetObject) {
		return aopFactory.inject(targetObject);
	}
	
	/* 通过 AopManager.me().getAopFactory().inject(...) 可调用如下方法，不直接开放出来
	public static <T> T inject(Class<T> targetClass, T targetObject) {
		return aopFactory.inject(targetClass, targetObject);
	}*/
}




