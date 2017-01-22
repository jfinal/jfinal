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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import com.jfinal.template.TemplateException;
import com.jfinal.template.stat.Location;

/**
 * ForIteratorStatus
 * 封装 #for( id : expr) 迭代语句状态，便于模板中获取
 * 
 * 使用以下表达式可以模板中获取迭代状态：
 * for.size 被迭代集合元素数量，不支持 Iterator 与 Iterable
 * for.index 从 0 下始的下标
 * for.count 从 1 开始的计数器
 * for.first 是否第一个元素
 * for.last 是否最后一个元素
 * for.odd 是否第奇数个元素
 * for.even 是否第偶数个元素
 * for.outer 获取外层 for 对象，便于获取外层 for 循环状态
 *           例如: for.outer.index
 */
public class ForIteratorStatus {
	
	private Object outer;
	private int index;
	private int size;
	private Iterator<?> iterator;
	private Location location;
	
	public ForIteratorStatus(Object outer, Object target, Location location) {
		this.outer = outer;
		this.index = 0;
		this.location = location;
		init(target);
	}
	
	@SuppressWarnings("unchecked")
	private void init(Object target) {
		if (target == null) {
			size = 0;
			iterator = NullIterator.me;
			return ;
		}
		if (target instanceof Collection) {
			size = ((Collection<?>)target).size();
			iterator = ((Collection<?>)target).iterator();
			return ;
		}
		if (target instanceof Map<?, ?>) {
			size = ((Map<?, ?>)target).size();
			iterator = new MapIterator(((Map<Object, Object>)target).entrySet().iterator());
			return ;
		}
		if (target.getClass().isArray()) {
			size = Array.getLength(target);
			iterator = new ArrayIterator(target, size);
			return ;
		}
		if (target instanceof Iterator) {
			size = -1;
			iterator = (Iterator<?>)target;
			return ;
		}
		if (target instanceof Iterable) {
			size = -1;
			iterator = ((Iterable<?>)target).iterator();
			return ;
		}
		if (target instanceof Enumeration) {
			ArrayList<?> list = Collections.list((Enumeration<?>)target);
			size = list.size();
			iterator = list.iterator();
			return ;
		}
		
		size = 1;
		iterator = new SingleObjectIterator(target);
	}
	
	Iterator<?> getIterator() {
		return iterator;
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
	
	public int getSize() {
		if (size >= 0) {
			return size;
		}
		throw new TemplateException("No such method getSize() of the iterator", location);
	}
	
	public boolean getFirst() {
		return index == 0;
	}
	
	public boolean getLast() {
		return !iterator.hasNext();
	}
	
	public boolean getOdd() {
		return index % 2 == 0;
	}
	
	public boolean getEven() {
		return index % 2 != 0;
	}
}

class MapIterator implements Iterator<Entry<Object, Object>> {
	
	private Iterator<Entry<Object, Object>> iterator;
	
	public MapIterator(Iterator<Entry<Object, Object>> iterator) {
		this.iterator = iterator;
	}
	
	public boolean hasNext() {
		return iterator.hasNext();
	}
	
	public Entry<Object, Object> next() {
		return new ForEntry((Entry<Object, Object>)iterator.next());
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}
}

class ArrayIterator implements Iterator<Object> {
	
	private Object array;
	private int size;
	private int index;
	
	ArrayIterator(Object array, int size) {
		this.array = array;
		this.size = size;
		this.index = 0;
	}
	
	public boolean hasNext() {
		return index < size;
	}
	
	public Object next() {
		return Array.get(array, index++);
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}
}

class SingleObjectIterator implements Iterator<Object> {
	
	private Object target;
	private boolean hasNext = true;
	
	public SingleObjectIterator(Object target) {
		this.target = target;
	}
	
	public boolean hasNext() {
		return hasNext;
	}
	
	public Object next() {
		if (hasNext) {
			hasNext = false;
			return target;
		}
		throw new NoSuchElementException();
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}
}

class NullIterator implements Iterator<Object> {
	
    static final Iterator<?> me = new NullIterator();
    
    private NullIterator() {
    }
    
    public boolean hasNext() {
        return false;
    }
    
    public Object next() {
        throw new NoSuchElementException();
    }
    
    public void remove() {
        throw new UnsupportedOperationException();
    }
}






