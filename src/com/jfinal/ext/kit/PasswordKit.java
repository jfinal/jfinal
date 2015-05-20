/**
 * 
 */
package com.jfinal.ext.kit;

import com.jfinal.kit.EncryptionKit;

/**
 * @author 朱丛启  2015年5月2日 下午6:23:35
 *
 */
public class PasswordKit {
	
	public enum EncryptType{
		MD5,
		SHA1,
		SHA256,
		SHA384,
		SHA512,
	}
 
	/**
	 * md5密码
	 * @param srcStr
	 * @return
	 */
	public static String md5Password(String srcStr){
		return password(srcStr,EncryptType.MD5);
	}
	
	/**
	 * 加密
	 * @param srcStr
	 * @param algorithm 加密算法
	 * @see EncryptAlgorithm
	 * @return
	 */
	public static String password(String srcStr,EncryptType algorithm){
		String encrpted = null;
		switch (algorithm) {
		case MD5:{
			encrpted = EncryptionKit.md5Encrypt(srcStr);
		}
			break;
			
		case SHA1:{
			encrpted = EncryptionKit.sha1Encrypt(srcStr);
		}
			break;
		case SHA256:{
			encrpted = EncryptionKit.sha256Encrypt(srcStr);
		}
			break;
		case SHA384:{
			encrpted = EncryptionKit.sha384Encrypt(srcStr);
		}
			break;
		case SHA512:{
			encrpted = EncryptionKit.sha512Encrypt(srcStr);
		}
			break;
		}
		return encrpted;
	}
	
	/**
	 * 加入salt的密码=> salt+srcStr+salt
	 * @param srcStr
	 * @param salt
	 * @return
	 */
	public static String passwordAddedSalt(String srcStr, String salt){
		return MD5(MD5(salt)+MD5(srcStr)+MD5(salt));
	}
	
	/**
	 * 校验密码
	 * @param originalPasswrd 原始密码
	 * @param passwordAddedSalt  已经加盐的密码
	 * @param salt 盐
	 * @return
	 */
	public static boolean validatePasswordAddedSalt(String originalPasswrod,String passwordAddedSalt, String salt){
		return passwordAddedSalt(originalPasswrod, salt).equals(passwordAddedSalt);
	}
	
	/**
	 * Md5加密
	 * @param srcStr
	 * @return
	 */
	public static String MD5(String srcStr){
		return EncryptionKit.md5Encrypt(srcStr);
	}
	
	/**
	 * Sha1加密
	 * @param srcStr
	 * @return
	 */
	public static String SHA1(String srcStr){
		return EncryptionKit.sha1Encrypt(srcStr);
	}
	
	
}
