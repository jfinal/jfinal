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

import java.text.ParseException;
import java.util.Date;

import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;

public class DateParameterGetter extends ParameterGetter<Date> {
	
	public DateParameterGetter(String parameterName, String defaultValue) {
		super(parameterName, defaultValue);
	}

	@Override
	public Date get(Controller c) {
		return c.getParaToDate(getParameterName(), getDefaultValue());
	}

	@Override
	protected Date to(String v) {
		if(StrKit.notBlank(v)){
			try {
				return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(v);
			} catch (ParseException e) {
				return null;
			}
		}
		return null;
	}

}
