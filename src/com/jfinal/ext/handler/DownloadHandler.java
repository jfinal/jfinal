/**
 * Copyright (c) 2011-2012, James Zhan 詹波 (jfinal@126.com).
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.jfinal.handler.Handler;
import com.jfinal.util.StringKit;

/**
 * 处理文件下载调用 action 时无扩展名的问题
 * 1: actionKey + 下载文件扩展名如: actionKey.rar
 * 2: add(new DwonloadHandler("download"));
 * 3: actionKey.rar?download
 */
public class DownloadHandler extends Handler {
	
	private String downloadPara;
	
	public DownloadHandler() {
		downloadPara = "download";
	}
	
	public DownloadHandler(String downloadPara) {
		if (StringKit.isBlank(downloadPara))
			throw new IllegalArgumentException("Parameter can not be blank.");
		this.downloadPara = downloadPara;
	}
	
	public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
		if (request.getParameter(downloadPara) != null) {
			int index = target.lastIndexOf(".");
			if (index != -1) {
				nextHandler.handle(target.substring(0, index), request, response, isHandled);
				return ;
			}
		}
		
		nextHandler.handle(target, request, response, isHandled);
	}
}





