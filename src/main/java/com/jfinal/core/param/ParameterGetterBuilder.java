package com.jfinal.core.param;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import com.jfinal.core.Controller;
import com.jfinal.log.Log;

public class ParameterGetterBuilder {
	
	private static final Log LOG = Log.getLog("core");
	
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
			ParameterGetter<?> pg = createParameterGetter(controllerClass, method, p);
			if(pg instanceof FileParameterGetter || pg instanceof FileArrayParameterGetter){
				opag.addParameterGetterToHeader(pg);
			}else{
				opag.addParameterGetter(pg);
			}
		}		
		return opag;
	}
	
	private ParameterGetter<?> createParameterGetter(Class<? extends Controller> controllerClass, Method method, Parameter p){
		String parameterName = p.getName();
		String defaultValue  = null;
		String type          = p.getParameterizedType().getTypeName();
		Param  ParamAnn      = p.getAnnotation(Param.class);
		if(ParamAnn != null){
			parameterName = ParamAnn.value().trim();
			defaultValue  = ParamAnn.defaultValue().trim();
			if(defaultValue.isEmpty()){
				defaultValue = null;
			}
		}
		if(type.equals("java.lang.String")){
			if(defaultValue != null){
				return new StringParameterGetter(parameterName, defaultValue);
			}else{
				return new StringParameterGetter(parameterName);
			}
		}else if(type.equals("int") || type.equals("java.lang.Integer")){
			if(defaultValue != null){
				return new IntParameterGetter(parameterName, defaultValue);
			}else{
				return new IntParameterGetter(parameterName);
			}
		}else if(type.equals("long") || type.equals("java.lang.Long")){
			if(defaultValue != null){
				return new LongParameterGetter(parameterName, defaultValue);
			}else{
				return new LongParameterGetter(parameterName);
			}
		}else if(type.equals("boolean") || type.equals("java.lang.Boolean")){
			if(defaultValue != null){
				return new BooleanParameterGetter(parameterName, defaultValue);
			}else{
				return new BooleanParameterGetter(parameterName);
			}
		}else if(type.equals("java.util.Date")){
			if(defaultValue != null){
				return new DateParameterGetter(parameterName, defaultValue);
			}else{
				return new DateParameterGetter(parameterName);
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
				String msg = "can not create ParameterGetter:"+ controllerClass.getName() + "." + method.getName()  +" " + type + " " + parameterName;
				LOG.warn(msg);
				return new NullParameterGetter(parameterName);
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
