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
 * FieldGetter 用于支持 target.field 表达式的取值，
 * 以及支持用户扩展自定义的 FieldGetter 实现方式
 */
public abstract class FieldGetter {
	
	/**
	 * 接管 target.fieldName 表达式，如果可以接管则返回接管对象，否则返回 null
	 * @param targetClass target.fieldName 表达式中 target 的 Class 类型
	 * @param fieldName target.fieldName 表达式中的 fieldName 部分
	 * @return 如果可以接管 targetClass.fieldName 则返回接管对象，否则返回 null
	 */
	public abstract FieldGetter takeOver(Class<?> targetClass, String fieldName);
	
	/**
	 * 获取 target.fieldName 表达式的值
	 * @param target 目标对象
	 * @param fieldName 字段名称
	 * @return target.fieldName 表达式的值
	 */
	public abstract Object get(Object target, String fieldName) throws Exception;
	
	/**
	 * 仅仅 NullFieldGetter 会覆盖此方法并返回 false
	 * 
	 * 用于消除 FieldKit.getFieldGetter(...) 中的 instanceof 判断，
	 * 并且让 Map fieldGetterCache 的 value 值不必使用 Object 类型，
	 * 还消除了 Field.exec(...) 中的 null 值判断
	 */
	public boolean notNull() {
		return true;
	}
}







