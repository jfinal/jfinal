package com.jfinal.core.param;

import com.jfinal.core.Controller;
import com.jfinal.upload.UploadFile;

public class FileParameterGetter extends AbstractParameterGetter<UploadFile> {

	public FileParameterGetter(String parameterName) {
		super(parameterName);
	}

	@Override
	public UploadFile get(Controller c) {
		String parameterName = this.getParameterName();
		if(parameterName.isEmpty()){
			return c.getFile();
		}
		return c.getFile(parameterName);
	}

}
