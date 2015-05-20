/**
 * 
 */
package com.jfinal.upload;

import java.io.File;

import com.jfinal.ext.kit.RandomKit;


/**
 * @author 朱丛启  2015年5月5日 下午6:47:11
 * 随机文件名
 */
public class RandomFileRenamePolicy extends FileRenamePolicyWrapper {

	@Override
	public File nameProcess(File f, String name, String ext) {
		String path = f.getParent();
		this.setSaveDirectory(path);
		return (new File(path,RandomKit.randomMD5Str()+ext));
	}
}
