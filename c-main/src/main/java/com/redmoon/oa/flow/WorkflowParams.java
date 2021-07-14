package com.redmoon.oa.flow;

import com.redmoon.kit.util.FileUpload;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class WorkflowParams {
    private FileUpload fileUpload;
    private HttpServletRequest request;

    public WorkflowParams(HttpServletRequest request, FileUpload fileUpload) {
        this.request = request;
        this.fileUpload = fileUpload;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setFileUpload(FileUpload fileUpload) {
        this.fileUpload = fileUpload;
    }

    public FileUpload getFileUpload() {
        return fileUpload;
    }
}
