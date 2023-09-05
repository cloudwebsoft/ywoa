package com.redmoon.oa.flow;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ErrMsgException;
import org.json.JSONObject;
import org.json.*;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.message.MessageDb;
import cn.js.fan.web.Global;
import com.redmoon.oa.person.UserDb;
import cn.js.fan.web.SkinUtil;
import java.util.Iterator;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.Config;
import com.cloudwebsoft.framework.util.LogUtil;

public class WorkflowActionMgr {
    public WorkflowActionMgr() {
    }

    public WorkflowActionDb getWorkflowActionDb(int id) {
        WorkflowActionDb wa = new WorkflowActionDb(id);
        return wa.getWorkflowActionDb(id);
    }
}
