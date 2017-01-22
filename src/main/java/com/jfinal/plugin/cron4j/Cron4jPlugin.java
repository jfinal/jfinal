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

package com.jfinal.plugin.cron4j;

import it.sauronsoftware.cron4j.ProcessTask;
import it.sauronsoftware.cron4j.Scheduler;
import java.util.ArrayList;
import java.util.List;
import com.jfinal.kit.Prop;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.IPlugin;
import it.sauronsoftware.cron4j.Task;

/**
 * Cron4jPlugin 封装 cron4j，使用 cron 表达式调试 Task 执行
 *
 * cron 表达式由五部分组成：分 时 天 月 周
 * 分 ：从 0 到 59
 * 时 ：从 0 到 23
 * 天 ：从 1 到 31，字母 L 可以表示月的最后一天
 * 月 ：从 1 到 12，可以别名：jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov" and "dec"
 * 周 ：从 0 到 6，0 表示周日，6 表示周六，可以使用别名： "sun", "mon", "tue", "wed", "thu", "fri" and "sat"
 *
 * 数字 n：表示一个具体的时间点，例如 5 * * * * 表示 5 分这个时间点时执行
 * 逗号 , ：表示指定多个数值，例如 3,5 * * * * 表示 3 和 5 分这两个时间点执行
 * 减号 -：表示范围，例如 1-3 * * * * 表示 1 分、2 分再到 3 分这三个时间点执行
 * 星号 *：表示每一个时间点，例如 * * * * * 表示每分钟执行
 * 除号 /：表示指定一个值的增加幅度。例如 n/m表示从 n 开始，每次增加 m 的时间点执行
 *
 * 一、配置文件用法
 * cp = new Cron4jPlugin("cron4j.txt");
 * me.add(cp);
 *
 * 配置文件：
 * cron4j=task1, task2
 * task1.cron=* * * * *
 * task1.class=com.xxx.TaskAaa
 * task1.daemon=true
 * task1.enable=true
 *
 * task2.cron=* * * * *
 * task2.class=com.xxx.TaskBbb
 * task2.daemon=true
 * task2.enable=false
 *
 * cron4j 是所有配置的入口，用来配置有哪些 task 需要被调度，多个任务名称可用逗号分隔，例如上例中的 task1、task2
 * 后面的配置项均以 task1、task2 为前缀进行配置，具体意义如下
 * task1.cron 表示 task1 使用 cron 表达式调试任务
 * task1.class 表示 执行任务的类文件
 * task1.daemon 表示调试线程是否设置为守护线程，默认值为 true，守护线程会在 tomcat 关闭时自动关闭
 * task1.enable 表示该任务是否有效，默认值为 true，为 false 时该任务无效，不会被调用
 * task2 的配置与 task1 类似，不在赘述
 *
 * 此外：cron4j 这个配置项入口可以在 new Cron4jPlugin(...) 时指定，例如下面的代码将指定配置项入口为 "myCron4jConfig"
 * Cron4jPlugin("config.txt", "myCron4jConfig")，当指定配置入口为 "myCron4jConfig" 以后，配置就变成了如下的形式：
 * myCron4jConfig=task1, task2
 * 后面的配置完全不变
 *
 * 二、java 代码用法用法
 * cp = new Cron4jPlugin();
 * cp.addTask("* * * * *", new MyTask());
 * me.add(cp);
 * 还需要添加：schedule(Task task) 功能
 * 官方的例子证明可以调用系统的脚本，这个对于调用数据库备份来说很方便：
 *
 * 三、ProcessTask 调用系统程序的用法(How to schedule a system process)
 * System processes can be easily scheduled using the ProcessTask class:
 * ProcessTask task = new ProcessTask("C:\\Windows\\System32\\notepad.exe");
 * Scheduler scheduler = new Scheduler();
 * scheduler.schedule("* * * * *", task);
 * scheduler.start();
 *
 * Arguments for the process can be supplied by using a string array instead of a single command string:
 * String[] command = { "C:\\Windows\\System32\\notepad.exe", "C:\\File.txt" };
 * ProcessTask task = new ProcessTask(command);
 *
 * Environment variables for the process can be supplied using a second string array, whose elements have to be in the NAME=VALUE form:
 * String[] command = { "C:\\tomcat\\bin\\catalina.bat", "start" };
 * String[] envs = { "CATALINA_HOME=C:\\tomcat", "JAVA_HOME=C:\\jdks\\jdk5" };
 * ProcessTask task = new ProcessTask(command, envs);
 *
 * The default working directory for the process can be changed using a third parameter in the constructor:
 * String[] command = { "C:\\tomcat\\bin\\catalina.bat", "start" };
 * String[] envs = { "CATALINA_HOME=C:\\tomcat", "JAVA_HOME=C:\\jdks\\jdk5" };
 * File directory = "C:\\MyDirectory";
 * ProcessTask task = new ProcessTask(command, envs, directory);
 *
 * If you want to change the default working directory but you have not any environment variable, the envs parameter of the constructor can be set to null:
 * ProcessTask task = new ProcessTask(command, null, directory);
 */
public class Cron4jPlugin implements IPlugin {
	
	private List<TaskInfo> taskInfoList = new ArrayList<TaskInfo>();
	public static final String defaultConfigName = "cron4j";

	public Cron4jPlugin() {

	}
	
	public Cron4jPlugin(String configFile) {
		this(new Prop(configFile), defaultConfigName);
	}
	
	public Cron4jPlugin(Prop configProp) {
		this(configProp, defaultConfigName);
	}

	public Cron4jPlugin(String configFile, String configName) {
		this(new Prop(configFile), configName);
	}
	
	public Cron4jPlugin(Prop configProp, String configName) {
		try {
			addTask(configProp, configName);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * 考虑添加对 ProcessTask 的配置支持，目前不支持 ProcessTask 对象的构造方法的参数配置
	 * 对于 ProcessTask 型的任务调度，建议对 ProcessTask 的创建使用 java 代码
	 */
	private void addTask(Prop configProp, String configName) throws Exception {
		String configNameValue = configProp.get(configName);
		if (StrKit.isBlank(configNameValue)) {
			throw new IllegalArgumentException("The value of configName: " + configName + " can not be blank.");
		}
		String[] taskNameArray = configNameValue.trim().split(",");
		for (String taskName : taskNameArray) {
			if (StrKit.isBlank(taskName)) {
				throw new IllegalArgumentException("taskName can not be blank.");
			}
			taskName = taskName.trim();

			String taskCron = configProp.get(taskName + ".cron");
			if (StrKit.isBlank(taskCron)) {
				throw new IllegalArgumentException(taskName + ".cron" + " not found.");
			}
			taskCron = taskCron.trim();

			String taskClass = configProp.get(taskName + ".class");
			if (StrKit.isBlank(taskClass)) {
				throw new IllegalArgumentException(taskName + ".class" + " not found.");
			}
			taskClass = taskClass.trim();

			Object taskObj = Class.forName(taskClass).newInstance();
			if ( !(taskObj instanceof Runnable) && !(taskObj instanceof Task) ) {
				throw new IllegalArgumentException("Task 必须是 Runnable、ITask、ProcessTask 或者 Task 类型");
			}

			boolean taskDaemon  = configProp.getBoolean(taskName + ".daemon", true);
			boolean taskEnable  = configProp.getBoolean(taskName + ".enable", true);
			taskInfoList.add(new TaskInfo(taskCron, taskObj, taskDaemon, taskEnable));
		}
	}

	public Cron4jPlugin addTask(String cron, Runnable task, boolean daemon, boolean enable) {
		taskInfoList.add(new TaskInfo(cron, task, daemon, enable));
		return this;
	}

	public Cron4jPlugin addTask(String cron, Runnable task, boolean daemon) {
		return addTask(cron, task, daemon, true);
	}

	public Cron4jPlugin addTask(String cron, Runnable task) {
		return addTask(cron, task, true, true);
	}

	public Cron4jPlugin addTask(String cron, ProcessTask processTask, boolean daemon, boolean enable) {
		taskInfoList.add(new TaskInfo(cron, processTask, daemon, enable));
		return this;
	}

	public Cron4jPlugin addTask(String cron, ProcessTask processTask, boolean daemon) {
		return addTask(cron, processTask, daemon, true);
	}

	public Cron4jPlugin addTask(String cron, ProcessTask processTask) {
		return addTask(cron, processTask, true, true);
	}

	public Cron4jPlugin addTask(String cron, Task task, boolean daemon, boolean enable) {
		taskInfoList.add(new TaskInfo(cron, task, daemon, enable));
		return this;
	}

	public Cron4jPlugin addTask(String cron, Task task, boolean daemon) {
		return addTask(cron, task, daemon, true);
	}

	public Cron4jPlugin addTask(String cron, Task task) {
		return addTask(cron, task, true, true);
	}
	
	public boolean start() {
		for (TaskInfo taskInfo : taskInfoList) {
			taskInfo.schedule();
		}
		for (TaskInfo taskInfo : taskInfoList) {
			taskInfo.start();
		}
		return true;
	}
	
	public boolean stop() {
		for (TaskInfo taskInfo : taskInfoList) {
			taskInfo.stop();
		}
		return true;
	}
	
	private static class TaskInfo {
		Scheduler scheduler;

		String cron;
		Object task;
		boolean daemon;
		boolean enable;

		TaskInfo(String cron, Object task, boolean daemon, boolean enable) {
			if (StrKit.isBlank(cron)) {
				throw new IllegalArgumentException("cron 不能为空.");
			}
			if (task == null) {
				throw new IllegalArgumentException("task 不能为 null.");
			}

			this.cron = cron.trim();
			this.task = task;
			this.daemon = daemon;
			this.enable = enable;
		}

		void schedule() {
			if (enable) {
				scheduler = new Scheduler();
				if (task instanceof Runnable) {
					scheduler.schedule(cron, (Runnable) task);
				} else if (task instanceof Task) {
					scheduler.schedule(cron, (Task) task);
				} else {
					scheduler = null;
					throw new IllegalStateException("Task 必须是 Runnable、ITask、ProcessTask 或者 Task 类型");
				}
				scheduler.setDaemon(daemon);
			}
		}

		void start() {
			if (enable) {
				scheduler.start();
			}
		}

		void stop() {
			if (enable) {
				if (task instanceof ITask) {   // 如果任务实现了 ITask 接口，则回调 ITask.stop() 方法
					((ITask)task).stop();
				}
				scheduler.stop();
			}
		}
	}
}


