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

package com.jfinal.kit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * ThreadPoolGroup 任务组
 * <br>
 * 比使用 ThreadPoolKit.getExecutor().invokeAll 简洁一些
 *
 * <pre>
 * ThreadPoolGroup group = ThreadPoolKit.newGroup(size);
 * group.submit(() -> System.out.println("hello1"));
 * group.submit(() -> System.out.println("hello2"));
 * group.waits();
 * </pre>
 */
@SuppressWarnings("unused")
public class ThreadPoolGroup {

    private final List<Future<?>> list;

    public ThreadPoolGroup() {
        this(100);
    }
    public ThreadPoolGroup(int initialCapacity) {
        list = new ArrayList<>(initialCapacity);
    }

    public List<Future<?>> getList() {
        return list;
    }

    /**
     * 提交一个 Runnable 任务到线程池任务队列，等待执行
     */
    public Future<?> submit(final Runnable task) {
        Future<?> ret = ThreadPoolKit.submit(task);
        list.add(ret);
        return ret;
    }

    /**
     * 提交一个 Runnable 任务到线程池任务队列，等待执行
     */
    public <T> Future<T> submit(Runnable task, T result) {
        Future<T> ret = ThreadPoolKit.submit(task, result);
        list.add(ret);
        return ret;
    }

    /**
     * 提交一个 Callable 任务到线程池任务队列，等待执行
     */
    public <T> Future<T> submit(Callable<T> task) {
        Future<T> ret = ThreadPoolKit.submit(task);
        list.add(ret);
        return ret;
    }

    /**
     * 阻塞当前线程，等待所有任务执行完毕
     */
    public void waits(){
        for (Future<?> fu : list) {
            try {
                fu.get();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 阻塞当前线程，等待所有任务执行完毕，每一个任务都设置 等待超时
     */
    public void waits(long timeout, TimeUnit unit){
        for (Future<?> fu : list) {
            try {
                fu.get(timeout, unit);
            } catch (Exception ignored) {
            }
        }
    }
}
