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

package com.jfinal.template.expr.ast;

/**
 * FieldKeyBuilder
 * 
 * 用于生成缓存 FieldGetter 的 key
 */
public abstract class FieldKeyBuilder {
	
	public abstract Object getFieldKey(Class<?> targetClass, long fieldFnv1a64Hash);
	
	// 假定是超大规模项目，并且假定其 Map/Model/Record + field 组合数量超级庞大，默认使用 StrictFieldKeyBuilder
	static FieldKeyBuilder instance = new StrictFieldKeyBuilder();
	
	public static FieldKeyBuilder getInstance() {
		return instance;
	}
	
	/**
	 * 设置为官方提供的 FastFieldKeyBuilder 实现，性能更高
	 */
	public static void setToFastFieldKeyBuilder() {
		instance = new FastFieldKeyBuilder();
	}
	
	/**
	 * 设置为自定义 FieldKeyBuilder
	 */
	public static void setFieldKeyBuilder(FieldKeyBuilder fieldKeyBuilder) {
		if (fieldKeyBuilder == null) {
			throw new IllegalArgumentException("fieldKeyBuilder can not be null");
		}
		instance = fieldKeyBuilder;
	}
	
	// ------------------------------------------------------------------------
	
	/**
	 * FastFieldKeyBuilder
	 */
	public static class FastFieldKeyBuilder extends FieldKeyBuilder {
		public Object getFieldKey(Class<?> targetClass, long fieldFnv1a64Hash) {
			return targetClass.getName().hashCode() ^ fieldFnv1a64Hash;
		}
	}
	
	// ------------------------------------------------------------------------
	
	/**
	 * StrictFieldKeyBuilder
	 */
	public static class StrictFieldKeyBuilder extends FieldKeyBuilder {
		public Object getFieldKey(Class<?> targetClass, long fieldFnv1a64Hash) {
			return new FieldKey(targetClass.getName().hashCode(), fieldFnv1a64Hash);
		}
	}
	
	// ------------------------------------------------------------------------
	
	/**
	 * FieldKey
	 * 
	 * FieldKey 用于封装 targetClass、fieldName 这两部分的 hash 值，
	 * 确保不会出现 key 值碰撞
	 * 
	 * 这两部分 hash 值在不同 class 与 field 的组合下出现碰撞的
	 * 概率完全可以忽略不计
	 * 
	 * 备忘：
	 * 可以考虑用 ThreadLocal 重用 FieldKey 对象，但要注意放入 Map fieldGetterCache
	 * 中的 FieldKey 对象需要 clone 出来，确保线程安全。由于 FieldKey 占用空间不大，
	 * 所以 ThreadLocal 方案大概率并无优势，从 ThreadLocal 中获取数据时，除了耗时也无法
	 * 避免创建对象
	 */
	public static class FieldKey {
		
		final long classHash;
		final long fieldHash;
		
		public FieldKey(long classHash, long fieldHash) {
			this.classHash = classHash;
			this.fieldHash = fieldHash;
		}
		
		public int hashCode() {
			return (int)(classHash ^ fieldHash);
		}
		
		/**
		 * FieldKey 的核心价值在于此 equals 方法通过比较两部分 hash 值，避免超大规模场景下可能的 key 值碰撞
		 * 
		 * 不必判断 if (fieldKey instanceof FieldKey)，因为所有 key 类型必须要相同
		 * 不必判断 if (this == fieldKey)，因为每次用于取值的 FieldKey 都是新建的
		 */
		public boolean equals(Object fieldKey) {
			FieldKey fk = (FieldKey)fieldKey;
			return classHash == fk.classHash && fieldHash == fk.fieldHash;
		}
		
		public String toString() {
			return "classHash = " + classHash + "\nfieldHash = " + fieldHash;
		}
	}
}








