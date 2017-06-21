package com.jfinal.plugin.controller;

/**
 * Created by GongRui on 5/12/2017.
 */
import com.jfinal.config.Routes;
import com.jfinal.plugin.IPlugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

public class JControllerPlugin implements IPlugin{

    private String packageName;
    private String baseViewPath;
    private Routes routes = new Routes() {@Override public void config(){}};

    public JControllerPlugin() {}

    public JControllerPlugin(String packageName) {
        this.packageName = packageName;
    }

    public JControllerPlugin(String packageName,String baseViewPath) {
        this.packageName = packageName;
        this.baseViewPath = baseViewPath;
    }

    @Override
    public boolean start() {
        if(baseViewPath != null && baseViewPath.trim().length() > 0) {
            routes.setBaseViewPath(baseViewPath);
        }
        if(this.packageName == null) this.packageName = "";
        String basePath = this.getClass().getClassLoader().getResource("").getPath();
        String baseFilePath = new File(basePath).getAbsolutePath();
        String url = basePath + this.packageName.replaceAll("\\.","/");
        File file = new File(url);
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            for(File f : files) {
                if(f.isDirectory()) {
                    listFile(baseFilePath, f.listFiles());
                } else {
                    getClassFile(baseFilePath, f);
                }
            }
        }
        Routes.getRoutesList().add(this.routes);
        return true;
    }

    /**
     *  遍历文件
     * @param baseFilePath
     * @param files
     */
    private void listFile(String baseFilePath, File[] files) {
        for(File f : files) {
            if(f.isDirectory()) {
                listFile(baseFilePath, f.listFiles());
            } else {
                getClassFile(baseFilePath, f);
            }
        }
    }

    /**
     * 判断class文件
     * @param baseFilePath
     * @param f
     */
    private void getClassFile(String baseFilePath, File f) {
        if(!f.getName().endsWith(".class")) return;
        String controllerFilePath = f.getAbsolutePath().substring(baseFilePath.length() + 1);
        controllerFilePath = controllerFilePath.substring(0, controllerFilePath.length() - 6);
        controllerFilePath = controllerFilePath.replaceAll("\\\\", "\\.").replaceAll("/","\\.");
        addController(controllerFilePath);
    }

    /**
     * 将有Jcontroller注解的类添加到路由中
     * @param cls
     */
    private void addController(String cls) {
        try {
            JController controller = Class.forName(cls).getAnnotation(JController.class);
            if(controller != null) {
                if(controller.reqPath().equalsIgnoreCase("/")
                        && controller.viewPath().equalsIgnoreCase("")) {
                    Routes.class.getMethod("add"
                            ,String.class, Class.class, String.class).
                            invoke(routes, controller.reqPath(), Class.forName(cls), controller.reqPath());
                } else {
                    Routes.class.getMethod("add",String.class, Class.class, String.class).
                            invoke(routes, controller.reqPath(), Class.forName(cls), controller.viewPath());
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean stop() {
        return true;
    }
}
