/**
 * Copyright (c) 2011-2017, 玛雅牛 (myaniu AT gmail.com).
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
package com.jfinal.core.paragetter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.jfinal.core.Controller;

public class ParameterGetterBuilder {
	
	private static ParameterGetterBuilder me = new ParameterGetterBuilder();
	
	private ParameterGetterBuilder(){}
	
	public static ParameterGetterBuilder me(){return me;}
	
	public ParameterGetterProcessor build(Class<? extends Controller> controllerClass, Method method){
		final int parameterCount = method.getParameterCount();
		ParameterGetterProcessor opag = new ParameterGetterProcessor(parameterCount);
		if( 0 == parameterCount){
			return opag;
		}
		for(Parameter p : method.getParameters()){
			IParameterGetter<?> pg = createParameterGetter(controllerClass, method, p);
			if(pg instanceof FileParameterGetter || pg instanceof FileArrayParameterGetter){
				opag.addParameterGetterToHeader(pg);
			}else{
				opag.addParameterGetter(pg);
			}
		}		
		return opag;
	}
	
	private IParameterGetter<?> createParameterGetter(Class<? extends Controller> controllerClass, Method method, Parameter p){
		String parameterName = p.getName();
		String defaultValue  = null;
		String type          = p.getParameterizedType().getTypeName();
		Para  ParaAnn      = p.getAnnotation(Para.class);
		if(ParaAnn != null){
			parameterName = ParaAnn.value().trim();
			defaultValue  = ParaAnn.defaultValue().trim();
			if(defaultValue.isEmpty()){
				defaultValue = null;
			}
		}
		if(type.equals("java.lang.String")){
			if(defaultValue != null){
				return new StringParameterGetter(parameterName, defaultValue);
			}else{
				return new StringParameterGetter(parameterName, null);
			}
		}else if(type.equals("int") || type.equals("java.lang.Integer")){
			if(defaultValue != null){
				return new IntParameterGetter(parameterName, Integer.parseInt(defaultValue));
			}
			if(type.equals("int")){
				return new IntParameterGetter(parameterName,0);
			}else{
				return new IntParameterGetter(parameterName,null);
			}
		}else if(type.equals("long") || type.equals("java.lang.Long")){
			if(defaultValue != null){
				return new LongParameterGetter(parameterName, Long.parseLong(defaultValue));
			}
			if(type.equals("long")){
				return new LongParameterGetter(parameterName,0L);
			}else{
				return new LongParameterGetter(parameterName,null);
			}
		}else if(type.equals("short") || type.equals("java.lang.Short")){
			if(defaultValue != null){
				return new ShortParameterGetter(parameterName, Short.parseShort(defaultValue));
			}
			if(type.equals("short")){
				return new ShortParameterGetter(parameterName,(short)0);
			}else{
				return new ShortParameterGetter(parameterName,null);
			}
		}else if(type.equals("float") || type.equals("java.lang.Float")){
			if(defaultValue != null){
				return new FloatParameterGetter(parameterName, Float.parseFloat(defaultValue));
			}
			if(type.equals("float")){
				return new FloatParameterGetter(parameterName,0.0f);
			}else{
				return new FloatParameterGetter(parameterName,null);
			}
		}else if(type.equals("double") || type.equals("java.lang.Double")){
			if(defaultValue != null){
				return new DoubleParameterGetter(parameterName, Double.parseDouble(defaultValue));
			}
			if(type.equals("double")){
				return new DoubleParameterGetter(parameterName,0.0);
			}else{
				return new DoubleParameterGetter(parameterName,null);
			}
		}else if(type.equals("boolean") || type.equals("java.lang.Boolean")){
			if(defaultValue != null){
				return new BooleanParameterGetter(parameterName, Boolean.parseBoolean(defaultValue));
			}
			if(type.equals("boolean")){
				return new BooleanParameterGetter(parameterName,Boolean.FALSE);
			}else{
				return new BooleanParameterGetter(parameterName,null);
			}
		}else if(type.equals("java.util.Date")){
			if(defaultValue != null){
				return new DateParameterGetter(parameterName, defaultValue);
			}else{
				return new DateParameterGetter(parameterName, null);
			}
		}else if(type.equals("java.math.BigDecimal")){
			if(defaultValue != null){
				return new BigDecimalParameterGetter(parameterName, BigDecimal.valueOf(Double.parseDouble(defaultValue)));
			}else{
				return new BigDecimalParameterGetter(parameterName, null);
			}
		}else if(type.equals("java.math.BigInteger")){
			if(defaultValue != null){
				return new BigIntegerParameterGetter(parameterName, BigInteger.valueOf(Long.parseLong(defaultValue)));
			}else{
				return new BigIntegerParameterGetter(parameterName, null);
			}
		}else if(type.equals("com.jfinal.upload.UploadFile")){
			return new FileParameterGetter(parameterName);
		}else if(type.equals("java.util.List<com.jfinal.upload.UploadFile>")){
			return new FileArrayParameterGetter();
		}else if(type.equals("java.lang.String[]")){
			return new StringArrayParameterGetter(parameterName);
		}else if(type.equals("long[]") || type.equals("java.lang.Long[]")){
			return new LongArrayParameterGetter(parameterName);
		}else if(type.equals("int[]") || type.equals("java.lang.Integer[]")){
			return new IntArrayParameterGetter(parameterName);
		}else{
			//判断是否是com.jfinal.plugin.activerecord.Model的子类
			if(isSubClassOf(p.getType(), com.jfinal.plugin.activerecord.Model.class)){
				return new ModelParameterGetter<>(p.getType(), parameterName);
			}else{
				return new BeanParameterGetter<>(p.getType(), parameterName);
			}
		}
	}
	
	private static  boolean isSubClassOf(Class<?> clazz ,Class<?> superClass){
		Class<?> c = clazz;
		boolean isSubClass = false;
		int deep = 1;
		int maxDeep = 5;
		String superClassName = superClass.getName();
		while(c != null && deep < maxDeep){
			String typeName = c.getName();
			if(superClassName.equals(typeName)){
				isSubClass = true;
				break;
			}else if(typeName.startsWith("java.lang")){
				break;
			}
			c = c.getSuperclass();
			deep++;
		}
		return isSubClass;
	}
}
