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
public class UploadThreadInfo {
    public UploadThreadInfo() {
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public String getFileId() {
        return fileId;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    public int getBlockId() {
        return blockId;
    }

    private String fileId;
    private int begin;
    private int end;
    private int blockId; // 线程所对应的块号


}
