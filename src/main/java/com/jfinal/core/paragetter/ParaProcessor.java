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

public class ParaProcessor implements IParaGetter<Object[]>{
	
	private final List<IParaGetter<?>> paraGetters;
	
	private static final Object[] NULL_ARGS = new Object[0];
	
	public ParaProcessor(int paramCount){
		if( paramCount > 0){
			this.paraGetters = new ArrayList<IParaGetter<?>>(paramCount);
		}else{
			this.paraGetters = null;
		}
	}
	public void addParaGetterToHeader(IParaGetter<?> paraGetter){
		if(this.paraGetters != null){
			this.paraGetters.add(0, paraGetter);
		}
	}
	
	public void addParaGetter(IParaGetter<?> paraGetter){
		if(this.paraGetters != null){
			this.paraGetters.add(paraGetter);
		}
	}
	@Override
	public Object[] get(Controller c) {
		if(this.paraGetters != null){
			List<Object> parameters = new ArrayList<Object>(this.paraGetters.size());
			for(IParaGetter<?> paraGetter : this.paraGetters ){
				parameters.add(paraGetter.get(c));
			}
			return parameters.toArray();
		}else{
			return NULL_ARGS;
		}
	}
}
