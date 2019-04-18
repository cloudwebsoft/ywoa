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

    /**
     * 加签
     * 加签MyActionDb中的privMyActionId记录的始终为节点上的原始处理人员
     * @param request HttpServletRequest
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean addPlus(HttpServletRequest request) throws ErrMsgException {
    	int type = ParamUtil.getInt(request, "type", WorkflowActionDb.PLUS_TYPE_BEFORE);
        int mode = ParamUtil.getInt(request, "mode", WorkflowActionDb.PLUS_MODE_ORDER);
        int actionId = ParamUtil.getInt(request, "actionId");
        long myActionId = ParamUtil.getLong(request, "myActionId");
        // String users = ParamUtil.get(request, "users");
        String users = request.getParameter("users"); // 解决ajax中文问题

        Privilege pvg = new Privilege();
        String curUser = pvg.getUser(request);
        String[] ary = StrUtil.split(users, ",");
        users = "";
        
        for (String user : ary) {
        	if (user.equals(curUser)) {
        		continue;
        	}
        	users += (users.equals("") ? "" : ",") + user;
        }
        if (users.equals("")) {
            throw new ErrMsgException("请选择加签人员！");
        }
                
        String cwsWorkflowResult = ParamUtil.get(request, "cwsWorkflowResult");

        // UserMgr um = new UserMgr();
        // Config cfg = new Config();

        // boolean flowNotifyByEmail = cfg.getBooleanProperty("flowNotifyByEmail");

        cn.js.fan.mail.SendMail sendmail = WorkflowDb.getSendMail();

        WorkflowActionDb wa = getWorkflowActionDb(actionId);
        JSONObject json = new JSONObject();
        try {
            json.put("type", type);
            json.put("mode", mode);
            json.put("users", users);
            json.put("from", curUser);
            json.put("internal", wa.getInternalName());
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        wa.setPlus(json.toString());
        boolean re = wa.saveOnlyToDb();
        if (re) {
        	ary = StrUtil.split(users, ",");
            if (ary!=null) {
                MyActionDb mad = new MyActionDb();
                mad = mad.getMyActionDb(myActionId);
                
                if (!cwsWorkflowResult.equals("")) {
                	mad.setResult(cwsWorkflowResult);
                	mad.save();
                }

                WorkflowDb wf = new WorkflowDb();
                wf = wf.getWorkflowDb((int)mad.getFlowId());

                long privMyActionId = mad.getPrivMyActionId();
                MyActionDb privMyActionDb = mad.getMyActionDb(privMyActionId);
                WorkflowActionDb privAction = wa.getWorkflowActionDb((int)privMyActionDb.getActionId());

                // 前加签
                if (type == WorkflowActionDb.PLUS_TYPE_BEFORE) {
                    // 如果为前加签，则本myActionId应该被置为已完成
                    wa.changeMyActionDb(mad, pvg.getUser(request));
                    // 如果为顺序加签，则先发给第一个人待办通知
                    if (mode == WorkflowActionDb.PLUS_MODE_ORDER) {
                        MyActionDb plusmad = wf.notifyUser(ary[0], new java.util.Date(), mad.getId(), privAction, wa,
                                                           WorkflowActionDb.STATE_PLUS, wa.getFlowId());

                        wf.sendNotifyMsgAndEmail(request, plusmad, sendmail);

                    } else { // 否则发给全部加签人员
                        for (int i = 0; i < ary.length; i++) {
                            MyActionDb plusmad = wf.notifyUser(ary[i], new java.util.Date(), mad.getId(), privAction, wa,
                                          WorkflowActionDb.STATE_PLUS, wa.getFlowId());

                            wf.sendNotifyMsgAndEmail(request, plusmad, sendmail);
                        }
                    }
                }
                else if (type == WorkflowActionDb.PLUS_TYPE_CONCURRENT) { // 并签，给每个加签人员发待办通知
                    for (int i = 0; i < ary.length; i++) {
                        MyActionDb plusmad = wf.notifyUser(ary[i], new java.util.Date(), mad.getId(), privAction, wa,
                                      WorkflowActionDb.STATE_PLUS, wa.getFlowId());
                        wf.sendNotifyMsgAndEmail(request, plusmad, sendmail);
                    }
                }
            }
        }
        return re;
    }
}
