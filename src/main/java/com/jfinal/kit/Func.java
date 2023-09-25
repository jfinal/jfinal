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

package com.jfinal.kit;

/**
 * lambda 函数工具箱，主要用来解决 JDK 函数接口参数过少的问题，同时还降低了学习成本
 * 
 * JDK java.util.function 包里面的函数接口有如下缺点：
 * 1：设计繁琐，相比动态语言的闭包在理解和学习成本上要高
 * 2：函数支持的参数过少，超过两个参数时就没法使用了
 * 3：基于 java 接口的闭包实现方案，转移了用户对于函数本身这个核心要点的关注，
 *    接口名、方法名带来了噪声干扰
 */
public interface Func {
	
	/**
	 * 0 参 0 返回函数
	 */
	@FunctionalInterface
	public interface F00 {
		void call();
	}
	
	/**
	 * 1 参 0 返回函数
	 */
	@FunctionalInterface
	public interface F10<T> {
		void call(T t);
	}
	
	/**
	 * 2 参 0 返回函数
	 */
	@FunctionalInterface
	public interface F20<T, U> {
		void call(T t, U u);
	}
	
	/**
	 * 3 参 0 返回函数
	 */
	@FunctionalInterface
	public interface F30<T, U, V> {
		void call(T t, U u, V v);
	}
	
	/**
	 * 4 参 0 返回函数
	 */
	@FunctionalInterface
	public interface F40<T, U, V, W> {
		void call(T t, U u, V v, W w);
	}
	
	/**
	 * 5 参 0 返回函数
	 */
	@FunctionalInterface
	public interface F50<T, U, V, W, X> {
		void call(T t, U u, V v, W w, X x);
	}
	
	/**
	 * 6 参 0 返回函数
	 */
	@FunctionalInterface
	public interface F60<T, U, V, W, X, Y> {
		void call(T t, U u, V v, W w, X x, Y y);
	}
	
	/**
	 * 7 参 0 返回函数
	 */
	@FunctionalInterface
	public interface F70<T, U, V, W, X, Y, Z> {
		void call(T t, U u, V v, W w, X x, Y y, Z z);
	}
	
	// ---------------------------------------------
	
	/**
	 * 0 参 1 返回函数
	 */
	@FunctionalInterface
	public interface F01<R> {
		R call();
	}
	
	/**
	 * 1 参 1 返回函数
	 */
	@FunctionalInterface
	public interface F11<T, R> {
		R call(T t);
	}
	
	/**
	 * 2 参 1 返回函数
	 */
	@FunctionalInterface
	public interface F21<T, U, R> {
		R call(T t, U u);
	}
	
	/**
	 * 3 参 1 返回函数
	 */
	@FunctionalInterface
	public interface F31<T, U, V, R> {
		R call(T t, U u, V v);
	}
	
	/**
	 * 4 参 1 返回函数
	 */
	@FunctionalInterface
	public interface F41<T, U, V, W, R> {
		R call(T t, U u, V v, W w);
	}
	
	/**
	 * 5 参 1 返回函数
	 */
	@FunctionalInterface
	public interface F51<T, U, V, W, X, R> {
		R call(T t, U u, V v, W w, X x);
	}
	
	/**
	 * 6 参 1 返回函数
	 */
	@FunctionalInterface
	public interface F61<T, U, V, W, X, Y, R> {
		R call(T t, U u, V v, W w, X x, Y y);
	}
	
	/**
	 * 7 参 1 返回函数
	 */
	@FunctionalInterface
	public interface F71<T, U, V, W, X, Y, Z, R> {
		R call(T t, U u, V v, W w, X x, Y y, Z z);
	}
}


