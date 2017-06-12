package com.jfinal.core.param;

import java.util.List;

import com.jfinal.core.Controller;
import com.jfinal.upload.UploadFile;

public class FileArrayParameterGetter extends AbstractParameterGetter<List<UploadFile>> {

	public FileArrayParameterGetter() {
		super("");
	}

	@Override
	public List<UploadFile> get(Controller c) {
		return c.getFiles();
	}

}
