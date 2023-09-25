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

/**
 * 上传文件大小超出范围时抛出该异常
 *
 * com.oreilly.servlet.multipart.MultipartParser 中会抛出以下异常
 * throw new ExceededSizeException("Posted content length of " + length + " exceeds limit of " + maxSize);
 */
public class ExceededSizeException extends com.oreilly.servlet.multipart.ExceededSizeException {

	private static final long serialVersionUID = -3493615798872340918L;

	ExceededSizeException(Throwable t) {
		super(t);
	}
}



