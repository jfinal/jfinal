/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
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
import com.oreilly.servlet.multipart.FileRenamePolicy;

/**
 * OreillyCos.
 */
public class OreillyCos {
	
	public static void init(String saveDirectory, int maxPostSize, String encoding) {
		try {
			Class.forName("com.oreilly.servlet.MultipartRequest");
			doInit(saveDirectory, maxPostSize, encoding);
		} catch (ClassNotFoundException e) {
			
		}
	}
	
	public static void setFileRenamePolicy(FileRenamePolicy fileRenamePolicy) {
		if (fileRenamePolicy == null)
			throw new IllegalArgumentException("fileRenamePolicy can not be null.");
		MultipartRequest.fileRenamePolicy = fileRenamePolicy;
	}
	
	private static void doInit(String saveDirectory, int maxPostSize, String encoding) {
		String dir;
		if (StrKit.isBlank(saveDirectory)) {
			dir = PathKit.getWebRootPath() + File.separator + "upload";
		}
		else if (isAbsolutelyPath(saveDirectory)) {
			dir = saveDirectory;
		}
		else {
			dir = PathKit.getWebRootPath() + File.separator + saveDirectory;
		}
		
		// add "/" postfix
		if (dir.endsWith("/") == false && dir.endsWith("\\") == false) {
			dir = dir + File.separator;
		}
		
		MultipartRequest.init(dir, maxPostSize, encoding);
	}
	
	private static boolean isAbsolutelyPath(String saveDirectory) {
		return saveDirectory.startsWith("/") || saveDirectory.indexOf(":") == 1;
	}
}


