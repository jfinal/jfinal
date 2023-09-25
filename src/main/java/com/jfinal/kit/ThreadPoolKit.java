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

import java.util.concurrent.*;
import com.jfinal.log.Log;

/**
 * ThreadPoolKit
 *
 * execute 与 submit（核心区别：前者提交不需要返回值的任务，后者提交需要返回值的任务）
 * 0: submit 会吃掉 task 中的异常，所以需要在 task 的 run/call 方法中使用 try catch
 *    对异常做日志
 *
 *    确切地说 submit 方式是将 task 中的异常暂存起来，如果后续调用其返回值的 Future.get() 方法，
 *    暂存的异常会被抛出来，可以通过 try catch 得到异常
 *
 *    execute 方式会直接向外层抛出 task 中的异常，并且会丢掉原有线程，新建线程用于后续调度，
 *    所以也要用 try catch 来保障不对上层抛出异常
 *
 *    总的来说，都要使用 try catch 管理异常，做好日志
 *
 * 1：submit 方法仅仅是对 Callable、Runnable 参数进行包装，最终仍然是转调了 execute 方法提交线程，
 *    从而会有轻微的性能损耗。如果无需通过 submit 返回的 Future 获取返回值则尽量使用 execute 方法
 *
 *    注意：在使用 execute 时，一定要在 Runnable 中 try catch 处理好异常，不要让其抛到线程池框架
 *         否则新的线程会不断被 new 出来，得不偿失
 *
 * 2: 当被调度的 task 出现异常时，submit 方法可以正确回收线程用于下次调用
 *    而 execute 方法会不断创建新线程用于下次调用。
 *
 *    其原因是 submit 方法会将 task 包装在一个 FutureTask 对象中，线程池最终是对该对象进行调度
 *    该对象的 run 再转调 task 内的 run/call 方法。而 FutureTask 的 run 方法中使用了
 *    try catch 进行了异常处理。
 *
 *    而 execute 方法并没有 FutureTask 机制，而是在ThreadPoolExecutor.runWorker(Worker w)
 *    方法中向外抛出了异常。
 *
 *    所以，在使用 execute 时要用 try catch 处理好线程内部的异常，不要将其抛给调度框架。
 *
 * 3: execute 与 submit 都会将 task 放入队列。也即 execute 并不是直接执行线程，同样是被线程池调度
 *
 * 4: execute 只支持 Runnable, submit 除了支持 Runnable 以外还支持 Callable (方便支持返回值)
 *
 * 5: execute 无返回值，submit 返回 Future 对象，便于异步处理线程执行的结果
 *
 * 6: 不需要返回值的时候，尽可能使用 execute，提升性能
 *
 * 7：Future.get() 获取返回值时，会阻塞当前线程直到任务执行完
 *
 * Runnable 与 Callable
 * 1: 接口方法不同，前者为 run() 后者为 call()
 *
 * 2: 本质上最大区别在于对于返回值的支持与否，前者接口方法无返回值，后者有返回值
 *
 * ThreadPoolExecutor 构造参数 maximumPoolSize 需要满足以下两个条件才起作用：
 * 1: 线程池 corePoolSize 个数的线程已被占用
 * 2: 任务队列已满
 *    所以，当任务队列为无界队列时 maximumPoolSize 不起作用，等同于无效
 * LinkedBlockingQueue 虽然是有界队列，但要注意默认 size 是 Integer.MAX_VALUE
 * 要等到队列装满 Integer.MAX_VALUE 任务之后 maximumPoolSize 才会起作用
 *
 * 补充：
 * 1：ScheduledExecutorService 的 schedule、scheduleAtFixedRate、scheduleWithFixedDelay
 *    在碰到异常时将停止调度，注意用 try catch 处理好
 * 2：综上，所有 ExecutorService 的调度方法都要使用 try catch 处理好异常，除非明确知道无需处理异常
 *
 */
public class ThreadPoolKit {

	private static ExecutorService executor = null;

	private ThreadPoolKit() {}

	/**
	 * 初始化
	 *
	 * @param nThreads the number of threads in the pool
	 */
	public static void init(int nThreads) {
		init(nThreads, nThreads);
	}

	/**
	 * 初始化
	 *
	 * @param corePoolSize the number of threads to keep in the pool,
	 *                     even if they are idle, unless allowCoreThreadTimeOut is set
	 * @param maximumPoolSize the maximum number of threads to allow in the pool
	 */
	public synchronized static void init(int corePoolSize, int maximumPoolSize) {
		if (executor == null) {
			// executor = Executors.newFixedThreadPool(nThreads);
			executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		} else {
			Log.getLog(ThreadPoolKit.class).warn(ThreadPoolKit.class.getName() + " 已经初始化");
		}
	}

	/**
	 * 传递 ExecutorService 对象进行初始化，从而完全掌控线程池参数
	 */
	public synchronized static void init(ExecutorService executor) {
		if (ThreadPoolKit.executor == null) {
			ThreadPoolKit.executor = executor;
		} else {
			Log.getLog(ThreadPoolKit.class).warn(ThreadPoolKit.class.getName() + " 已经初始化");
		}
	}

	public static ExecutorService getExecutor() {
		if (executor == null) {
			init(5, 128);
		}
		return executor;
	}

	/**
	 * 提交一个 Runnable 任务到线程池任务队列，等待执行
	 *
	 * 在不需要返回值时，尽可能使用 execute，并用 try catch 对异常做好日志
	 * 以及处理好异常。否则使用 submit 时会吃掉异常，漏掉解决问题的异常信息
	 *
	 * 要在 task 内部使用 try catch 处理好异常，不要将异常抛给线程池框架
	 * 否则会不断 new Thread 进行后续的线程调度。
	 *
	 * 使用 submit 没有这个问题，因为 submit 内部使用 FutureTask 对 taks
	 * 进行了封装，其 run 方法转调了 task 的 run/call 方法，而其 run 方法中
	 * 使用了 try catch 方法避免了向外层抛出异常
	 */
	public static void execute(Runnable task) {
		getExecutor().execute(task);
	}

	/**
	 * 提交一个 Runnable 任务到线程池任务队列，等待执行
	 *
	 * 调用返回值 Future 对象的 get() 方法，始终返回 null，除非有异常抛出
	 *
	 * 下面的代码可以得到线程执行过程中抛出的异常，但会立即阻塞当前线程：
	 *  try {
	 *      submit(task).get();
	 *  } catch(Exception e){
	 *      ...
	 *  }
	 */
	public static Future<?> submit(Runnable task) {
		return getExecutor().submit(task);
	}

	/**
	 * 提交一个 Runnable 任务到线程池任务队列，等待执行
	 *
	 * 调用返回值 Future 对象的 get() 方法，始终返回 submit 方法的 result 参数值，除非有异常抛出
	 *
	 * T result 参数无法在 Runnable 中被获取并使用，除非使用如下方式：
	 *    Ret result = Ret.create();
	 *    Future<Ret> future = ThreadPoolKit.submit(() -> {
	 *        result.set("key", "value");
	 *    }, result);
	 *
	 *    try {
	 *        Ret ret = future.get();
	 *        System.out.println(ret.get("key"));
	 *    } catch (InterruptedException | ExecutionException e) {
	 *        // 处理异常
	 *    }
	 *
	 * 注意：submit(task, result).get(); 会阻塞当前线程直到 task 运行完毕
	 */
	public static <T> Future<T> submit(Runnable task, T result) {
		return getExecutor().submit(task, result);
	}

	/**
	 * 提交一个 Callable 任务到线程池任务队列，等待执行
	 *
	 * 调用返回值 Future 对象的 get() 方法，可获取到 Callable.call() 方法的返回值，除非有异常抛出
	 * 所以，该方法非常适用于需要得到 task 执行结果的场景
	 *
	 * 注意：submit(task).get(); 会阻塞当前线程直到 task 运行完毕
	 */
	public static <T> Future<T> submit(Callable<T> task) {
		return getExecutor().submit(task);
	}

	/**
	 * 等待正在执行的线程执行完毕以后，关闭线程池
	 */
	public static void shutdown() {
		if (executor != null) {
			executor.shutdown();
		}
	}

	/**
	 * 停掉正在执行的线程，关闭线程池
	 */
	public static void shutdownNow() {
		if (executor != null) {
			executor.shutdownNow();
		}
	}

	/**
	 * 在 shutdown 线程池之后，阻塞等待所有任务执行完，或发生超时，或当前线程中断，以先发生者为准
	 */
	public static boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return executor == null || executor.awaitTermination(timeout, unit);
	}
}






