package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.flow.MyActionDb;

import javax.servlet.http.HttpServletRequest;

public interface IWorkflowProService {

    boolean addPlus(HttpServletRequest request, long myActionId, int type, int mode) throws ErrMsgException;

    int FinishActionBatch(HttpServletRequest request, String ids) throws ErrMsgException;

    boolean finishActionSingle(HttpServletRequest request, MyActionDb mad, String userName, StringBuffer sb) throws ErrMsgException;
}
