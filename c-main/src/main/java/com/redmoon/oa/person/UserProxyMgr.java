package com.redmoon.oa.person;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.message.MessageDb;
import java.util.Date;
import cn.js.fan.web.SkinUtil;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.flow.MyActionDb;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import java.util.*;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.dept.*;
import java.util.Vector;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.db.SQLFilter;

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
public class UserProxyMgr {

    public UserProxyMgr() {
    }

    public int matchProxy(HttpServletRequest request, MyActionDb mad, UserProxyDb upd, boolean isUseMsg,
                              boolean isToMobile) {
        boolean re = false;
        if (upd.getInt("proxy_type") == UserProxyDb.TYPE_DEFAULT) {
            LogUtil.getLog(getClass()).info("mad.getProxyUserName()=" + mad.getProxyUserName() + " id=" + mad.getId() + " proxy=" + upd.getString("proxy"));
            if (!mad.getProxyUserName().equals(upd.getString("proxy"))) {
                mad.setProxyUserName(upd.getString("proxy"));
                re = mad.save();
            } else {
                return -1;
            }
        }
        else {
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb((int) mad.getFlowId());
            // 取得发起人
            String starter = wf.getUserName();

            DeptUserDb dud = new DeptUserDb();
            Vector v = dud.getDeptsOfUser(starter);
            Iterator ir = v.iterator();
            String depts = upd.getString("starter_param");
            while (ir.hasNext()) {
                DeptDb dd = (DeptDb) ir.next();
                // 按发起人所在的部门匹配
                if (("," + depts + ",").indexOf("," + dd.getCode() + ",") != -1) {
                    mad.setProxyUserName(upd.getString("proxy"));
                    re = mad.save();
                }
            }
        }

        return re?1:0;
    }

    public void resetProxy(HttpServletRequest request) throws ErrMsgException {
        String userName = ParamUtil.get(request, "userName");

        // 先匹配部门型的，如果匹配不到，则再匹配默认的，以免多次匹配，发送重复消息，如果已匹配，则删除，不再参与匹配
        java.util.Date now = new Date();
        String sql = "select id from user_proxy where user_name=" + StrUtil.sqlstr(userName) + " and begin_date<=" + SQLFilter.getDateStr(DateUtil.format(now, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss") + " and end_date>" + SQLFilter.getDateStr(DateUtil.format(now, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss") + " order by proxy_type desc";
        UserProxyDb upd = new UserProxyDb();
        Vector v = upd.list(sql);

        boolean isUseMsg = ParamUtil.getBoolean(request, "isUseMsg", false);
        boolean isToMobile = ParamUtil.getBoolean(request, "isToMobile", false);

        UserDb ud = new UserDb();
        ud = ud.getUserDb(userName);

        Map map = new HashMap();

        // 找出用户所有的待办理记录
        MyActionDb mad = new MyActionDb();
        sql = "select id from " + mad.getTableName() + " where user_name=" + StrUtil.sqlstr(userName) + " and (is_checked=0 or is_checked=" + MyActionDb.CHECK_STATUS_SUSPEND + ")";
        Iterator ir = mad.list(sql).iterator();
        while (ir.hasNext()) {
            mad = (MyActionDb) ir.next();

            boolean isMatched = false;
            Iterator ir2 = v.iterator();
            while (ir2.hasNext()) {
                upd = (UserProxyDb) ir2.next();
                // 如果匹配成功，则从ir2中删去
                if (matchProxy(request, mad, upd, isUseMsg, isToMobile)==1) {
                    isMatched = true;

                    if (map.get(upd.getString("proxy"))==null) {
                        map.put(upd.getString("proxy"), upd.getString("proxy"));

                        String t = SkinUtil.LoadString(request,
                                                       "res.module.user",
                                                       "msg_set_proxy_title");
                        t = t.replaceFirst("\\$user", ud.getRealName());
                        String c = SkinUtil.LoadString(request,
                                                       "res.module.user",
                                                       "msg_set_proxy_content");
                        c = c.replaceFirst("\\$proxyBeginDate",
                                           DateUtil.format(upd.getDate("begin_date"), "yyyy-MM-dd HH:mm:ss"));
                        c = c.replaceFirst("\\$proxyEndDate",
                                           DateUtil.format(upd.getDate("end_date"), "yyyy-MM-dd HH:mm:ss"));
                        if (isUseMsg) {
                            MessageDb md = new MessageDb();
                            try {
                                md.sendSysMsg(upd.getString("proxy"), t, c);
                            } catch (ErrMsgException ex) {
                                LogUtil.getLog(getClass()).error(ex);
                            }
                        }

                        if (isToMobile) {
                            IMsgUtil imu = SMSFactory.getMsgUtil();
                            if (imu != null) {
                                try {
                                    imu.send(ud, t + "，" + c, MessageDb.SENDER_SYSTEM);
                                } catch (ErrMsgException ex1) {
                                    ex1.printStackTrace();
                                }
                            }
                        }
                    }

                    break;
                }
            }

            // 如果没有匹配成功，则说明没有符合的代理，重置为空，清除代理
            if (!isMatched) {
                mad.setProxyUserName("");
                mad.save();
            }
        }
    }

    /**
     * 取得用户在某流程中相应的代理人
     * @param userName String
     * @param flowId int
     * @return String
     */
    public static String getProxy(String userName, int flowId) {
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);
        // 取得发起人
        String starter = wf.getUserName();
        
        Leaf leaf = new Leaf(wf.getTypeCode());
        
        if (leaf == null || !leaf.isLoaded()) {
        	return "";
        }

        String flowCodes = StrUtil.sqlstr(leaf.getCode());
        while (leaf != null && leaf.isLoaded() && !leaf.getCode().equals(Leaf.CODE_ROOT)) {
        	flowCodes += "," + StrUtil.sqlstr(leaf.getParentCode());
        	leaf = leaf.getLeaf(leaf.getParentCode());
        }
        
        java.util.Date now = new Date();
        // 先匹配部门型的，如果匹配不到，则再匹配默认的
        String sql = "select id from user_proxy where user_name=" + StrUtil.sqlstr(userName) + " and flow_code in (" + flowCodes + ") and begin_date<=" + SQLFilter.getDateStr(DateUtil.format(now, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss") + " and end_date>" + SQLFilter.getDateStr(DateUtil.format(now, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss") +
                     " order by proxy_type desc";
        UserProxyDb upd = new UserProxyDb();
        Vector v = upd.list(sql);
        LogUtil.getLog(UserProxyMgr.class).info("getProxy=" + v.size() + " sql=" + sql);
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            upd = (UserProxyDb)ir.next();

            String proxy = StrUtil.getNullStr(upd.getString("proxy"));

            LogUtil.getLog(UserProxyMgr.class).info("proxyType=" + upd.getInt("proxy_type") + " proxy=" + proxy);

            if (upd.getInt("proxy_type")==UserProxyDb.TYPE_DEFAULT) {
                return proxy;
            }
            else {
                String depts = upd.getString("starter_param");
                DeptUserDb dud = new DeptUserDb();
                Iterator ir2 = dud.getDeptsOfUser(starter).iterator();
                while (ir2.hasNext()) {
                    DeptDb dd = (DeptDb) ir2.next();
                    if (("," + depts + ",").indexOf("," + dd.getCode() + ",") != -1) {
                        return proxy;
                    }
                }
            }
        }
        return "";
    }
}
