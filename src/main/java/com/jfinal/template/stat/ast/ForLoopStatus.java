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

package com.jfinal.template.stat.ast;

/**
 * ForLoopStatus
 * 封装 #for( init; cond; update) 循环的状态，便于模板中获取
 * 
 * 如下表达式可从模板中获取循环状态：
 * for.index 从 0 下始的下标
 * for.count 从 1 开始的计数器
 * for.first 是否第一个元素
 * for.odd 是否第奇数个元素
 * for.even 是否第偶数个元素
 * for.outer 获取外层 for 对象，便于获取外层 for 循环状态
 *           例如: for.outer.index
 * 
 * 注意：比迭代型循环语句少支持两个状态取值表达式：for.size、for.last
 */
public class ForLoopStatus {
	
	private Object outer;
	private int index;
	
	public ForLoopStatus(Object outer) {
		this.outer = outer;
		this.index = 0;
	}
	
	void nextState() {
		index++;
	}
	
	public Object getOuter() {
		return outer;
	}
	
	public int getIndex() {
		return index;
	}
	
	public int getCount() {
		return index + 1;
	}
	
	public boolean getFirst() {
		return index == 0;
	}
	
	public boolean getOdd() {
		return index % 2 == 0;
	}
	
	public boolean getEven() {
		return index % 2 != 0;
	}
}



