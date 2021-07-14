package com.redmoon.kit.util;

import java.io.Serializable;
import java.util.Vector;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

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
public class FileUploadStatus implements Serializable {
    static Map uploadFileInfos = Collections.synchronizedMap(new HashMap());

    public FileUploadStatus() {
    }

    public FileUploadStatusInfo get(String serialNo) {
        return (FileUploadStatusInfo)uploadFileInfos.get(serialNo);
    }

    public void add(FileUploadStatusInfo info) {
        uploadFileInfos.put(info.getSerialNo(), info);
    }

    public void del(String serialNo) {
        uploadFileInfos.remove(serialNo);
    }

}
