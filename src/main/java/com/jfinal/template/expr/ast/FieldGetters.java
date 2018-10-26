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

import java.lang.reflect.Array;
import com.jfinal.kit.StrKit;

/**
 * FieldGetters 封装官方默认 FieldGetter 实现
 */
public class FieldGetters {
	
	/**
	 * NullFieldGetter
	 * 
	 * 用于消除 FieldKit.getFieldGetter(...) 中的 instanceof 判断，并且让 Map fieldGetterCache
	 * 中的 value 不必使用 Object 类型。还消除了 Field.exec(...) 中的 null 值判断
	 */
	public static class NullFieldGetter extends FieldGetter {
		
		public static final NullFieldGetter me = new NullFieldGetter();
		
		public boolean notNull() {
			return false;
		}
		
		public FieldGetter takeOver(Class<?> targetClass, String fieldName) {
			throw new RuntimeException("The method takeOver(Class, String) of NullFieldGetter should not be invoked");
		}
		
		public Object get(Object target, String fieldName) throws Exception {
			throw new RuntimeException("The method get(Object, String) of NullFieldGetter should not be invoked");
		}
	}
	
	/**
	 * GetterMethodFieldGetter
	 * 
	 * 使用 getter 方法获取 target.field 表达式的值
	 */
	public static class GetterMethodFieldGetter extends FieldGetter {
		
		protected java.lang.reflect.Method getterMethod;
		
		public GetterMethodFieldGetter(java.lang.reflect.Method getterMethod) {
			this.getterMethod = getterMethod;
		}
		
		public FieldGetter takeOver(Class<?> targetClass, String fieldName) {
			if (MethodKit.isForbiddenClass(targetClass)) {
				throw new RuntimeException("Forbidden class: " + targetClass.getName());
			}
			
			String getterName = "get" + StrKit.firstCharToUpperCase(fieldName);
			java.lang.reflect.Method[] methodArray = targetClass.getMethods();
			for (java.lang.reflect.Method method : methodArray) {
				if (method.getName().equals(getterName) && method.getParameterTypes().length == 0) {
					// if (MethodKit.isForbiddenMethod(getterName)) {
						// throw new RuntimeException("Forbidden method: " + getterName);
					// }
					
					return new GetterMethodFieldGetter(method);
				}
			}
			
			return null;
		}
		
		public Object get(Object target, String fieldName) throws Exception {
			return getterMethod.invoke(target, ExprList.NULL_OBJECT_ARRAY);
		}
		
		public String toString() {
			return getterMethod.toString();
		}
	}
	
	/**
	 * IsMethodFieldGetter
	 * 
	 * 使用 target.isXxx() 方法获取值，默认不启用该功能，用户可以通过如下方式启用：
	 * Engine.addLastFieldGetter(new FieldGetters.IsMethodFieldGetter());
	 */
	public static class IsMethodFieldGetter extends FieldGetter {
		
		protected java.lang.reflect.Method isMethod;
		
		// 此构造方法仅为了方便在 Engine.addFieldGetter(...) 添加时不用为构造方法传参
		public IsMethodFieldGetter() {
		}
		
		public IsMethodFieldGetter(java.lang.reflect.Method isMethod) {
			this.isMethod = isMethod;
		}
		
		public FieldGetter takeOver(Class<?> targetClass, String fieldName) {
			if (MethodKit.isForbiddenClass(targetClass)) {
				throw new RuntimeException("Forbidden class: " + targetClass.getName());
			}
			
			String isMethodName = "is" + StrKit.firstCharToUpperCase(fieldName);
			java.lang.reflect.Method[] methodArray = targetClass.getMethods();
			for (java.lang.reflect.Method method : methodArray) {
				if (method.getName().equals(isMethodName) && method.getParameterTypes().length == 0) {
					Class<?> returnType = method.getReturnType();
					if (returnType == Boolean.class || returnType == boolean.class) {
						return new IsMethodFieldGetter(method);
					}
				}
			}
			
			return null;
		}
		
		public Object get(Object target, String fieldName) throws Exception {
			return isMethod.invoke(target, ExprList.NULL_OBJECT_ARRAY);
		}
		
		public String toString() {
			return isMethod.toString();
		}
	}
	
	/**
	 * ModelFieldGetter
	 * 
	 * 使用 Model.get(String) 获取值
	 */
	public static class ModelFieldGetter extends FieldGetter {
		
		// 所有 Model 可以共享 ModelFieldGetter 获取属性
		static final ModelFieldGetter singleton = new ModelFieldGetter();
		
		public FieldGetter takeOver(Class<?> targetClass, String fieldName) {
			if (com.jfinal.plugin.activerecord.Model.class.isAssignableFrom(targetClass)) {
				return singleton;
			} else {
				return null;
			}
		}
		
		public Object get(Object target, String fieldName) throws Exception {
			return ((com.jfinal.plugin.activerecord.Model<?>)target).get(fieldName);
		}
	}
	
	/**
	 * RecordFieldGetter
	 * 
	 * 使用 Record.get(String) 获取值
	 */
	public static class RecordFieldGetter extends FieldGetter {
		
		// 所有 Record 可以共享 RecordFieldGetter 获取属性
		static final RecordFieldGetter singleton = new RecordFieldGetter();
		
		public FieldGetter takeOver(Class<?> targetClass, String fieldName) {
			if (com.jfinal.plugin.activerecord.Record.class.isAssignableFrom(targetClass)) {
				return singleton;
			} else {
				return null;
			}
		}
		
		public Object get(Object target, String fieldName) throws Exception {
			return ((com.jfinal.plugin.activerecord.Record)target).get(fieldName);
		}
	}
	
	/**
	 * MapFieldGetter
	 * 
	 * 使用 Map.get(Object) 获取值
	 */
	public static class MapFieldGetter extends FieldGetter {
		
		// 所有 Map 可以共享 MapFieldGetter 获取属性
		static final MapFieldGetter singleton = new MapFieldGetter();
		
		public FieldGetter takeOver(Class<?> targetClass, String fieldName) {
			if (java.util.Map.class.isAssignableFrom(targetClass)) {
				return singleton;
			} else {
				return null;
			}
		}
		
		public Object get(Object target, String fieldName) throws Exception {
			return ((java.util.Map<?, ?>)target).get(fieldName);
		}
	}
	
	/**
	 * RealFieldGetter
	 * 
	 * 使用 target.field 获取值
	 */
	public static class RealFieldGetter extends FieldGetter {
		
		protected java.lang.reflect.Field field;
		
		public RealFieldGetter(java.lang.reflect.Field field) {
			this.field = field;
		}
		
		public FieldGetter takeOver(Class<?> targetClass, String fieldName) {
			java.lang.reflect.Field[] fieldArray = targetClass.getFields();
			for (java.lang.reflect.Field field : fieldArray) {
				if (field.getName().equals(fieldName)) {
					return new RealFieldGetter(field);
				}
			}
			
			return null;
		}
		
		public Object get(Object target, String fieldName) throws Exception {
			return field.get(target);
		}
		
		public String toString() {
			return field.toString();
		}
	}
	
	/**
	 * ArrayLengthGetter
	 * 
	 * 获取数组长度： array.length
	 */
	public static class ArrayLengthGetter extends FieldGetter {
		
		// 所有数组可以共享 ArrayLengthGetter 获取属性
		static final ArrayLengthGetter singleton = new ArrayLengthGetter();
		
		public FieldGetter takeOver(Class<?> targetClass, String fieldName) {
			if ("length".equals(fieldName) && targetClass.isArray()) {
				return singleton;
			} else {
				return null;
			}
		}
		
		public Object get(Object target, String fieldName) throws Exception {
			return Array.getLength(target);
		}
	}
}








