/**
 * 
 */
package com.jfinal.upload;

import java.io.File;

import com.jfinal.ext.kit.DateTimeKit;
import com.jfinal.ext.kit.RandomKit;


/**
 * @author 朱丛启 
 * May 5, 201511:14:13 PM
 */
public class DateRandomFileRenamePolicy extends FileRenamePolicyWrapper {

	@Override
	public File nameProcess(File f, String name, String ext) {
		String rename = RandomKit.randomMD5Str();
		String path = f.getParent();
		if (!path.endsWith("/")) {
			path += "/";
		}
		//add year month day
		path += DateTimeKit.formatNowToStyle("yyyy/M/d");
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		return (new File(path,rename+ext));
	}

}
