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
		return (new File(f.getParent(),RandomKit.randomMD5Str()+ext));
	}
}
