package com.jfinal.kit;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.jfinal.kit.PropKit;
import com.jfinal.log.Log;

/**
 * 根据路径检索类文件
 * @author 董华健  dongcb678@163.com
 * 在普通方式构建项目中，直接根据固定的路径读取类文件，但在maven构建中，依赖模块和jar文件不方便根据路径读取
 */
public abstract class RoutesLoadKit {

	private static final Log log = Log.getLog(RoutesLoadKit.class);

	/**
	 * 需要扫描的jar
	 */
	private static final List<String> scanJarList = new ArrayList<String>();
	
	/**
	 * 需要扫描的包
	 */
	private static final List<String> scanPkgList = new ArrayList<String>();
	
	
	public static void setScanPkgList(List<String> pkgs){
		scanPkgList.addAll(pkgs);
	}
	
	/**
	 * 获取需要扫描的jar
	 * @return
	 */
	public static List<String> getScanJarList(){
		return scanJarList;
	}

	/**
	 * 需要扫描的包
	 * @return
	 */
	public static List<String> getScanPkgList(){
		return scanPkgList;
	}

    /**
     * 搜索指定类或者接口的子类
     * @param target 指定类或者接口
     * @return
     */
	@Deprecated
    public static List<Class<?>> searchByPath(Class<?> target){
    	// 1.查找classes目录
    	List<String> classFileList = findFiles(PathKit.getRootClassPath());
    	
        // 2.查找lib目录中指定的jar
    	classFileList.addAll(findJarFiles());

        // 3.比对
    	List<Class<?>> list = isAssignableFrom(target, classFileList);
    	
    	return list;
    }
    
	/**
	 * 验证是否子类或者接口
	 * @param target 指定的父类或者接口
	 * @param classFileList 
	 * @return
	 */
	@Deprecated
	@SuppressWarnings({ "unchecked" })
	private static <T> List<Class<? extends T>> isAssignableFrom(Class<?> target, List<String> classFileList) {
        List<Class<? extends T>> classList = new ArrayList<Class<? extends T>>();
        for (String classFile : classFileList) {
            Class<?> classInFile = null;
			try {
				classInFile = Class.forName(classFile);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
            //判断target和classInFile是否相同，或是classInFile的子类或接口
            if (target.isAssignableFrom(classInFile) && target != classInFile) {
                classList.add((Class<? extends T>) classInFile);
            }
        }

        return classList;
    }

    /**
     * 查找classes文件夹内的.class文件
     * @param dirPath
     * @return
     */
	@Deprecated
    private static List<String> findFiles(String dirPath) {
        List<String> classFiles = new ArrayList<String>();
        
        // 判断目录是否存在
        File baseDir = new File(dirPath);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            log.error("baseDirName");
            return classFiles;
        } 

        String tempName = null;
        String[] filelist = baseDir.list();
        for (int i = 0; i < filelist.length; i++) {
            File readfile = new File(dirPath + File.separator + filelist[i]);
            if (readfile.isDirectory()) {
                classFiles.addAll(findFiles(dirPath + File.separator + filelist[i]));
            } else {
                tempName = readfile.getName();
                if(!tempName.endsWith(".class")){
                	continue;
                }
                
                String ablPath = readfile.getAbsoluteFile().getAbsolutePath().replace("\\", "/");
                String classFilePath = ablPath.substring(PathKit.getRootClassPath().length() + 1, ablPath.indexOf(".class")).replace("/", ".");
                
                for (String pkg : scanPkgList) {
                	if(classFilePath.startsWith(pkg)){
                		classFiles.add(classFilePath);
                		continue;
                	}
                }
            }
        }
        return classFiles;
    }
    
    /**
     * 查找lib目录jar中.class文件
     * @return
     */
	@Deprecated
    private static List<String> findJarFiles() {
        List<String> classFiles = new ArrayList<String>();
        try {
            // jar中文件查找
        	int size = scanJarList.size();
            for (int i = 0; i < size; i++) {
                JarFile jarFile = new JarFile(new File(PathKit.getLibPath() + File.separator + scanJarList.get(i)));
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    String entryName = jarEntry.getName();
                    String pkgEntryName = entryName.replaceAll("/", ".");
                    
                    // 去除不需要扫描的包
                    boolean pkgResult = false;
                    for (String pkg : scanPkgList) {
                    	if(pkgEntryName.startsWith(pkg)){
                    		pkgResult = true;
                    		break;
                    	}
                    }
                    
                    // 查找.class文件
                    if (!jarEntry.isDirectory() && pkgResult && entryName.endsWith(".class")) {
                        String className = pkgEntryName.substring(0, entryName.length() - 6);
                        classFiles.add(className);
                    }
                }
                jarFile.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classFiles;
    }
    
	/**
	 * 查找指定报名下target子类
	 * @param pkg
	 * @param target
	 * @return
	 * 
	 * 描述：
	 * 根据ClassLoader读取类文件，此方法更加通用
     * maven环境中，适合使用此方式，根据包名称查找所有加载的class
	 */
	public static Set<Class<?>> searchByClassLoader(Class<?> target) {
		Set<Class<?>> retSet = new HashSet<Class<?>>();

		//classes 目录下的
		List<String> pkgs = getScanPkgList();
		for (String pkg : pkgs) {
			Set<Class<?>> set = getClasses(pkg);
			for (Class<?> class2 : set) {
				if (target.isAssignableFrom(class2) && class2 != target) {
					retSet.add(class2);
	            }
			}
		}
		
		return retSet;
	}

	/**
	 * 从包package中获取所有的Class
	 * 
	 * @param pkg
	 * @return
	 */
	private static Set<Class<?>> getClasses(String pkg) {
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		// 是否循环迭代
		boolean recursive = true;
		String packageName = pkg;
		String packageDirName = packageName.replace('.', '/');
		Enumeration<URL> dirs;
		try {
			dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
			while (dirs.hasMoreElements()) {
				URL url = dirs.nextElement();
				String protocol = url.getProtocol();
				if ("file".equals(protocol)) {
					// 如果是以文件的形式保存在服务器上
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
				} else if ("jar".equals(protocol)) {
					// 如果是jar包文件
					JarFile jar;
					try {
						jar = ((JarURLConnection) url.openConnection()).getJarFile();
						Enumeration<JarEntry> entries = jar.entries();
						while (entries.hasMoreElements()) {
							JarEntry entry = entries.nextElement();
							String name = entry.getName();
							if (name.charAt(0) == '/') {
								name = name.substring(1);
							}
							if (name.startsWith(packageDirName)) {
								int idx = name.lastIndexOf('/');
								if (idx != -1) {
									packageName = name.substring(0, idx).replace('/', '.');
								}
								if ((idx != -1) || recursive) {
									if (name.endsWith(".class") && !entry.isDirectory()) {
										String className = name.substring(packageName.length() + 1, name.length() - 6);
										try {
											classes.add(Class.forName(packageName + '.' + className));
										} catch (ClassNotFoundException e) {
											e.printStackTrace();
										}
									}
								}
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return classes;
	}

	/**
	 * 以文件的形式来获取包下的所有Class
	 * 
	 * @param packageName
	 * @param packagePath
	 * @param recursive
	 * @param classes
	 */
	private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive,
			Set<Class<?>> classes) {
		File dir = new File(packagePath);
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}
		File[] dirfiles = dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
			}
		});
		for (File file : dirfiles) {
			if (file.isDirectory()) {
				findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive,
						classes);
			} else {
				String className = file.getName().substring(0, file.getName().length() - 6);
				try {
					classes.add(
							Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
