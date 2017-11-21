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

package com.jfinal.template.ext.sharedmethod;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * SharedMethodLib 共享方法库，逐步添加一些最常用的共享方法
 * 
 * <br>
 * 3.3 版本之前的 Logic.isTrue(Object) 方法不再对  Collection、
 * Map、数组、Iterator、Iterable 进行为空的判断，这部分逻辑已转移至
 * SharedMethodLib.isEmpty(Object)
 */
public class SharedMethodLib {
	
	/**
	 * 判断 Collection、Map、数组、Iterator、Iterable 类型对象中的元素个数是否为 0
	 * 规则：
	 * 1：null 返回 true
	 * 2：List、Set 等一切继承自 Collection 的，返回 isEmpty()
	 * 3：Map 返回 isEmpty()
	 * 4：数组返回 length == 0
	 * 5：Iterator 返回  ! hasNext()
	 * 6：Iterable 返回  ! iterator().hasNext()
	 * 
	 * 注意：原先 Logic.isTrue(Object) 中对集合与数组类型为空的判断转移到此方法中
	 */
	public Boolean isEmpty(Object v) {
		if (v == null) {
			return true;
		}
		
		if (v instanceof Collection) {
			return ((Collection<?>)v).isEmpty();
		}
		if (v instanceof Map) {
			return ((Map<?, ?>)v).isEmpty();
		}
		
		if (v.getClass().isArray()) {
			return Array.getLength(v) == 0;
		}
		
		if (v instanceof Iterator) {
			return ! ((Iterator<?>)v).hasNext();
		}
		if (v instanceof Iterable) {
			return ! ((Iterable<?>)v).iterator().hasNext();
		}
		
		throw new IllegalArgumentException("isEmpty(...) 方法只能接受 Collection、Map、数组、Iterator、Iterable 类型参数");
	}
	
	public Boolean notEmpty(Object v) {
		return !isEmpty(v);
	}
}






