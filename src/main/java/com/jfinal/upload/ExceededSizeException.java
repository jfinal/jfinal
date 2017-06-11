package com.jfinal.upload;

/**
 * 上传文件大小超出范围时抛出该异常
 * 
 * com.oreilly.servlet.multipart.MultipartParser 中会抛出以下异常
 * throw new ExceededSizeException("Posted content length of " + length + " exceeds limit of " + maxSize);
 */
public class ExceededSizeException extends RuntimeException {
	
	private static final long serialVersionUID = -3493615798872340918L;
	
	ExceededSizeException(Throwable t) {
		super(t);
	}
}



