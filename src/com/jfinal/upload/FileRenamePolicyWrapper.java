/**
 * 
 */
package com.jfinal.upload;

import java.io.File;
import java.io.IOException;

import com.oreilly.servlet.multipart.FileRenamePolicy;

/**
 * @author 朱丛启  2015年5月5日 下午6:49:13
 *
 */
public abstract class FileRenamePolicyWrapper implements FileRenamePolicy {

	@Override
	public File rename(File f) {
		if (null == f) {
			return null;
		}
		String name = f.getName();
		 String body = "";
		String ext = "";
		int dot = name.lastIndexOf(".");
		if (dot != -1) {
			body = name.substring(0, dot);
			ext = name.substring(dot);
		 }
		return this.nameProcess(f,body, ext);
	}
	
	/**
	 * 文件名字处理
	 * @param f 文件
	 * @param name 原名称
	 * @param fileExt 文件扩展名
	 * @return
	 */
	public abstract File nameProcess(File f, String name, String ext);

	 protected boolean createNewFile(File f) {
		try {
			return f.createNewFile();
		} catch (IOException ignored) {
		}
		return false;
	}
}
