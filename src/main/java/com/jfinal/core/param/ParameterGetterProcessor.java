package com.jfinal.core.param;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.core.Controller;

public class ParameterGetterProcessor implements ParameterGetter<Object[]>{
	
	private final List<ParameterGetter<?>> parameterGetters;
	
	private static final Object[] NULL_ARGS = new Object[0];
	
	public ParameterGetterProcessor(int parameterCount){
		if( parameterCount > 0){
			this.parameterGetters = new ArrayList<ParameterGetter<?>>(parameterCount);
		}else{
			this.parameterGetters = null;
		}
	}
	public void addParameterGetterToHeader(ParameterGetter<?> parameterGetter){
		if(this.parameterGetters != null){
			this.parameterGetters.add(0, parameterGetter);
		}
	}
	
	public void addParameterGetter(ParameterGetter<?> parameterGetter){
		if(this.parameterGetters != null){
			this.parameterGetters.add(parameterGetter);
		}
	}
	@Override
	public Object[] get(Controller c) {
		if(this.parameterGetters != null){
			List<Object> parameters = new ArrayList<Object>(this.parameterGetters.size());
			for(ParameterGetter<?> parameterGetter : this.parameterGetters ){
				parameters.add(parameterGetter.get(c));
			}
			return parameters.toArray();
		}else{
			return NULL_ARGS;
		}
	}
}
