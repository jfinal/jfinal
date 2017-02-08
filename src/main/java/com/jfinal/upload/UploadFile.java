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

package com.jfinal.upload;

import java.io.File;

/**
 * UploadFile.
 */
public class UploadFile {
	
	private String parameterName;
	
	private String uploadPath;
	private String fileName;
	private String originalFileName;
	private String contentType;
	
	public UploadFile(String parameterName, String uploadPath, String filesystemName, String originalFileName, String contentType) {
		this.parameterName = parameterName;
		this.uploadPath = uploadPath;
		this.fileName = filesystemName;
		this.originalFileName = originalFileName;
		this.contentType = contentType;
	}
	
	public String getParameterName() {
		return parameterName;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public String getOriginalFileName() {
		return originalFileName;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public String getUploadPath() {
		return uploadPath;
	}
	
	public File getFile() {
		if (uploadPath == null || fileName == null) {
			return null;
		} else {
			return new File(uploadPath + File.separator + fileName);
		}
	}
}






