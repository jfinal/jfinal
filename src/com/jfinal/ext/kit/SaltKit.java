/**
 * 
 */
package com.jfinal.ext.kit;

/**
 * @author 朱丛启  2015年5月2日 下午6:30:44
 *
 */
public class SaltKit {

	/**
	 * 生成salt
	 * @return
	 */
	public static String salt(){
		return PasswordKit.MD5(RandomKit.randomStr());
	}
}
