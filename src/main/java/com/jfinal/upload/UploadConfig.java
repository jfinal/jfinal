/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.upload;

import java.io.File;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.StrKit;

/**
 * UploadConfig.
 */
public class UploadConfig {
	
	static String baseUploadPath;
	static long maxPostSize;
	static String encoding;
	
	public static void init(String uploadPath, long maxPostSize, String encoding) {
		if (StrKit.isBlank(uploadPath)) {
			throw new IllegalArgumentException("uploadPath can not be blank.");
		}
		
		uploadPath = uploadPath.trim();
		uploadPath = uploadPath.replaceAll("\\\\", "/");
		
		String baseUploadPath;
		if (PathKit.isAbsolutePath(uploadPath)) {
			baseUploadPath = uploadPath;
		} else {
			baseUploadPath = PathKit.getWebRootPath() + File.separator + uploadPath;
		}
		
		// remove "/" postfix
		if (baseUploadPath.equals("/") == false) {
			if (baseUploadPath.endsWith("/")) {
				baseUploadPath = baseUploadPath.substring(0, baseUploadPath.length() - 1);
			}
		}
		
		UploadConfig.baseUploadPath = baseUploadPath;
		UploadConfig.maxPostSize = maxPostSize;
		UploadConfig.encoding = encoding;
	}
	
	public static String getBaseUploadPath() {
		return baseUploadPath;
	}
	
	public static long getMaxPostSize() {
		return maxPostSize;
	}
	
	public static String getEncoding() {
		return encoding;
	}
}


