/**
 * Copyright (c) 2011-2019, 玛雅牛 (myaniu AT gmail.com).
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

import com.jfinal.core.Action;
import com.jfinal.core.Controller;

/**
 * 使用构建好的 IParaGetter 数组获取用于 action 方法实参的参数值
 */
public class ParaProcessor implements IParaGetter<Object[]> {
	
	private int fileParaIndex = -1;
	private IParaGetter<?>[] paraGetters;
	
	public ParaProcessor(int paraCount) {
		paraGetters = paraCount > 0 ? new IParaGetter<?>[paraCount] : null;
	}
	
	public void addParaGetter(int index, IParaGetter<?> paraGetter) {
		// fileParaIndex 记录第一个 File、UploadFile 的数组下标
		if (	fileParaIndex == -1 &&
				(paraGetter instanceof FileGetter || paraGetter instanceof UploadFileGetter)) {
			fileParaIndex = index;
		}
		
		paraGetters[index] = paraGetter;
	}
	
	@Override
	public Object[] get(Action action, Controller c) {
		int len = paraGetters.length;
		Object[] ret = new Object[len];
		
		// 没有 File、UploadFile 参数的 action
		if (fileParaIndex == -1) {
			for (int i=0; i<len; i++) {
				ret[i] = paraGetters[i].get(action, c);
			}
			return ret;
		}
		
		// 有 File、UploadFile 参数的 action，优先获取 File、UploadFile 对象
		Object fileRet = paraGetters[fileParaIndex].get(action, c);
		for (int i=0; i<len; i++) {
			if (i != fileParaIndex) {
				ret[i] = paraGetters[i].get(action, c);	
			} else {
				ret[i] = fileRet;
			}
		}
		return ret;
	}
}






