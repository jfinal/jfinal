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
