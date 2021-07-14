package com.cloudweb.oa.service;

import cn.js.fan.util.ErrMsgException;
import com.alibaba.fastjson.JSONObject;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.ui.LocalUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

public interface WorkflowService {

    JSONObject finishAction(HttpServletRequest request, Privilege privilege) throws ErrMsgException;

    JSONObject finishActionByMobile(HttpServletRequest request, Privilege privilege) throws ErrMsgException;

    JSONObject finishActionFree(HttpServletRequest request, Privilege privilege) throws ErrMsgException;

    JSONObject finishActionFreeByMobile(HttpServletRequest request, Privilege privilege) throws ErrMsgException;

    JSONObject createNestSheetRelated(HttpServletRequest request) throws ErrMsgException;

    JSONObject updateNestSheetRelated(HttpServletRequest request) throws ErrMsgException;

    JSONObject runFinishScript(HttpServletRequest request, int flowId, int actionId) throws ErrMsgException;
}
