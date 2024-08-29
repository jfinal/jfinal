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
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.StrKit;

/**
 * UploadConfig.
 */
public class UploadConfig {

	static String baseUploadPath;
	static long maxPostSize;
	static String encoding;

	// 允许上传的文件扩展名白名单
	static Set<String> whitelist = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

	// 初始化默认白名单
	static {
		String[] arr = {
				// android 安装文件
				"apk",
				// 压缩
				"rar", "zip", "gzip", "tar", "gz", "dmg",
				// 图片
				"jpg", "png", "jpeg", "webp", "svg", "bmp",
				// 文本
				"css", "js", "json", "xml", "md", "txt",
				// 文档
				"pdf", "doc", "docx", "xls", "xlsx", "pot", "ppt", "pptx", "wps",
				// 音频
				"mp3", "mp2", "m3u", "m3u8", "ra", "mpga", "ram", "wav", "wax", "wma",
				// 视频
				"mp4", "mpeg", "avi", "wvm", "3gp", "asf", "asx", "flv", "mps", "pmv", "mov", "mpa", "mpe", "m4e", "m2v", "ts"
		};
		whitelist.addAll(Arrays.asList(arr));
	}

	/**
	 * 添加允许上传的文件扩展名到白名单
	 * @param fileExtension 允许上传文件的扩展名
	 */
	public static void addWhitelist(String... fileExtension) {
		if (fileExtension != null) {
			for (String fe : fileExtension) {
				whitelist.add(fe.trim());
			}
		}
	}

	/**
	 * 移除白名单中允许上传的文件扩展名
	 * @param fileExtension 需移除的上传文件的扩展名
	 */
	public static void removeWhitelist(String fileExtension) {
		if (fileExtension != null) {
			whitelist.remove(fileExtension.trim());
		}
	}

	/**
	 * 清空白名单，不允许文件上传。清除后可以添加指定允许上传的文件类型
	 */
	public static void clearWhitelist() {
		whitelist.clear();
	}

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


