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

package com.jfinal.ext.handler;

import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.jfinal.handler.Handler;
import com.jfinal.kit.StrKit;

/**
 * Skip the excluded url request from browser.
 * The skiped url will be handled by next Filter after JFinalFilter
 * <p>
 * Example: me.add(new UrlSkipHandler(".+\\.\\w{1,4}", false));
 */
public class UrlSkipHandler extends Handler {
	
	private Pattern skipedUrlPattern;
	
	public UrlSkipHandler(String skipedUrlRegx, boolean isCaseSensitive) {
		if (StrKit.isBlank(skipedUrlRegx)) {
			throw new IllegalArgumentException("The para excludedUrlRegx can not be blank.");
		}
		skipedUrlPattern = isCaseSensitive ? Pattern.compile(skipedUrlRegx) : Pattern.compile(skipedUrlRegx, Pattern.CASE_INSENSITIVE);
	}
	
	public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
		if (skipedUrlPattern.matcher(target).matches()) {
			return ;
		} else {
			next.handle(target, request, response, isHandled);
		}
	}
}


