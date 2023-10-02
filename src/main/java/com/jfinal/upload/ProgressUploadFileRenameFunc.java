package com.jfinal.upload;

/**
 * 进度上传文件 rename 策略func
 * @author 山东小木
 */
@FunctionalInterface
public interface ProgressUploadFileRenameFunc {
    /**
     * 处理rename
     * @param directory        文件存放路径
     * @param originFileName   原文件名
     * @return                 返回新文件名
     */
    public String call(String directory, String originFileName);
}
