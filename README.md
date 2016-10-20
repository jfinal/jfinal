**JFinal 官方网站：[http://www.jfinal.com](http://www.jfinal.com)**

**JFinal github传送门：[https://github.com/jfinal/jfinal](https://github.com/jfinal/jfinal)**

改动：
- 请求日志的输出使用Log
``` 
//System.out.print(sb.toString());
Log.getLog(ActionReporter.class).info(sb.toString());
```
- Action注解
  1. 添加[boolean]参数[withMethod],是否使用方法名作为action的结尾。默认【true】
  2. value以"/"开头表示绝对路径
  3. value不以"/"开头表示相对路径，前面补全controller的路径
  4. value中可以包含"/"

````
// MyConfig.java
public class MyConfig extend JfinalConfig{
    ...
    public void configRoute(Routes me) { 
        me.add('/ctrl',CtrlController.class);
    }
    ...
}

//UserController.java
public class CtrlController extend Controller{
    ...
    public void method(){}       //url: /ctrl/method
    
    @Action("/action")
    public void method1(){}      //url : /action
    
    @Action("action")
    public void method2(){}      //url : /ctrl/action/method2
    
    @Action(value="action",withMethod=false)
    public void method3(){}     //url : /ctrl/action
    
    @Action(value="action1/action2",withMethod=false)
    public void method3(){}     //url : /ctrl/action1/action2
}
````

