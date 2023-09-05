package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;
import com.cloudweb.oa.vo.Result;
import com.redmoon.oa.flow.MyActionDb;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

public interface IWorkflowPlusService {
    boolean rollBack(@RequestParam(value="actionId")Integer actionId, boolean isRollBackData);
}
