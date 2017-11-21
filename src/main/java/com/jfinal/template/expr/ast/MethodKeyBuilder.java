/**
 * Copyright (c) 2011-2017, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.template.expr.ast;

import com.jfinal.kit.HashKit;

/**
 * MethodKeyBuilder
 */
public abstract class MethodKeyBuilder {
	
	/**
	 * 生成指定 class、指定方法名、指定方法形参类型的 key 值，用于缓存
	 */
	public abstract Long getMethodKey(Class<?> targetClass, String methodName, Class<?>[] argTypes);
	
	// 默认使用 FastMethodKeyBuilder
	static MethodKeyBuilder instance = new FastMethodKeyBuilder();
	
	public static MethodKeyBuilder getInstance() {
		return instance;
	}
	
	/**
	 * 切换到 StrictMethodKeyBuilder
	 * 
	 * <pre>
	 * 特别注意：
	 *   如果希望将 configEngine(Engine me) 中的 Engine 切换到 StrictMethodKeyBuilder，
	 *   需要在 YourJFinalConfig extends JFinalConfig 中利用如下代码块才能生效：
	 * 	  static {
	 * 			MethodKeyBuilder.useStrictMethodKeyBuilder();
	 *    }
	 * 
	 *   原因是在 com.jfinal.core.Config 中 new Engine() 时 useStrictMethodKeyBuilder()
	 *   方法并未生效，所以 extension method 生成 method key 时仍然使用的是  FastMethodKeyBuilder
	 *   以至于在运行时，使用 StrictMethodKeyBuilder 生成的 key 找不到 extension method
	 * 
	 * </pre>
	 */
	public static void useStrictMethodKeyBuilder() {
		MethodKeyBuilder.instance = new StrictMethodKeyBuilder();
	}
	
	/**
	 * 切换到用户自定义 MethodKeyBuilder
	 */
	public static void setMethodKeyBuilder(MethodKeyBuilder methodKeyBuilder) {
		if (methodKeyBuilder == null) {
			throw new IllegalArgumentException("methodKeyBuilder can not be null");
		}
		MethodKeyBuilder.instance = methodKeyBuilder;
	}
	
	/**
	 * FastMethodKeyBuilder
	 * 
	 * targetClass、methodName、argTypes 的 hash 直接使用 String.hashCode()
	 * String.hashCode() 会被缓存，性能更好
	 */
	public static class FastMethodKeyBuilder extends MethodKeyBuilder {
		public Long getMethodKey(Class<?> targetClass, String methodName, Class<?>[] argTypes) {
			long hash = HashKit.FNV_OFFSET_BASIS_64;
			hash ^= targetClass.getName().hashCode();
			hash *= HashKit.FNV_PRIME_64;
			
			hash ^= methodName.hashCode();
			hash *= HashKit.FNV_PRIME_64;
			
			if (argTypes != null) {
				for (int i=0; i<argTypes.length; i++) {
					Class<?> type = argTypes[i];
					if (type != null) {
						hash ^= type.getName().hashCode();
						hash *= HashKit.FNV_PRIME_64;
					}
				}
			}
			return hash;
		}
	}
	
	/**
	 * StrictMethodKeyBuilder
	 * 
	 * targetClass、methodName、argTypes 三部分全部使用 fnv1a64 算法计算 hash
	 */
	public static class StrictMethodKeyBuilder extends MethodKeyBuilder {
		public Long getMethodKey(Class<?> targetClass, String methodName, Class<?>[] argTypes) {
			long hash = HashKit.FNV_OFFSET_BASIS_64;
			
			hash = fnv1a64(hash, targetClass.getName());
			hash = fnv1a64(hash, methodName);
			if (argTypes != null) {
				for (int i=0; i<argTypes.length; i++) {
					Class<?> type = argTypes[i];
					if (type != null) {
						hash = fnv1a64(hash, type.getName());
					}
				}
			}
			
			return hash;
		}
		
		private long fnv1a64(long offsetBasis, String key) {
			long hash = offsetBasis;
			for(int i=0, size=key.length(); i<size; i++) {
				hash ^= key.charAt(i);
				hash *= HashKit.FNV_PRIME_64;
			}
			return hash;
		}
	}
}



