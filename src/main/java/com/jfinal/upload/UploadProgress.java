package com.jfinal.upload;

/**
 * 文件上传进度
 * @author 山东小木
 */
public class UploadProgress {
    /**
     * 内容顺序第几个
     */
    private int itemIndex;
    /**
     * 文件总长度
     */
    private long contentLength;
    /**
     * 当前已读长度
     */
    private long bytesRead;

    public UploadProgress() {
    }

    public UploadProgress(int itemIndex, long contentLength, long bytesRead) {
        this.itemIndex = itemIndex;
        this.contentLength = contentLength;
        this.bytesRead = bytesRead;
    }

    public int getItemIndex() {
        return itemIndex;
    }

    public void setItemIndex(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public long getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(long bytesRead) {
        this.bytesRead = bytesRead;
    }
}
