package com.redmoon.kit.util;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class FileUploadStatusInfo {
    int bytesRead = 0;
    String serialNo;

    public FileUploadStatusInfo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public int getBytesRead() {
        return bytesRead;
    }

    public boolean isCancel() {
        return cancel;
    }

    public boolean isFinish() {
        return finish;
    }

    public int getRequestContentLength() {
        return requestContentLength;
    }

    public void setBytesRead(int bytesRead) {
        this.bytesRead = bytesRead;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    public void setRequestContentLength(int requestContentLength) {
        this.requestContentLength = requestContentLength;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public int getRet() {
        return ret;
    }

    private boolean cancel = false;
    private boolean finish = false;
    private int requestContentLength = 0;

    private int ret = 0;


}
