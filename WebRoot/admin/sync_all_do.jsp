<%@ page contentType="text/html;charset=utf-8" import="cn.js.fan.util.ParamUtil" import="com.redmoon.oa.dept.DeptDb" %>
<%@ page import="com.redmoon.oa.dept.DeptUserDb" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@page import="com.redmoon.oa.pvg.Privilege" %>
<%@page import="com.redmoon.oa.sso.SyncUtil" %>
<%@page import="com.redmoon.oa.tigase.Config" %>
<%@page import="com.redmoon.oa.tigase.TigaseConnection" %>
<%@page import="com.redmoon.weixin.mgr.WeixinDo" %>
<%@page import="java.util.Iterator" %>
<%@page import="java.util.Vector" %>
<%@ page import="com.redmoon.dingding.service.eventchange.EventChangeService" %>
<%@ page import="com.redmoon.dingding.service.client.DingDingClient" %>
<%@ page import="org.json.JSONObject" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "admin.user";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String op = ParamUtil.get(request, "op");
    String userName = new Privilege().getUser(request);
    if (op.equals("syncWeixin")) { // 一键同步OA部门人员至微信端
        WeixinDo weixinDo = new WeixinDo();
        weixinDo.syncDeptUsers();
        out.print("同步完成!");
    } else if (op.equals("syncWeixinToOA")) {
        WeixinDo weixinDo = new WeixinDo();
        weixinDo.syncWxDeptUserToOA();
        out.print("同步完成");
    } else if (op.equals("syncOAToDingding")) {
        DingDingClient client = new DingDingClient();
        client.syncOAtoDingDing();
        out.print("同步完成");
    } else if (op.equals("syncDingdingToOA")) {
        DingDingClient client = new DingDingClient();
        client.syncDingDingToOA();
        out.print("同步完成");
    } else if (op.equals("syncDing")) {
        //同步钉钉账户所有人员信息至user表中dingding字段
        DingDingClient client = new DingDingClient();
        client.batchUserAddDingDing();
        JSONObject json = new JSONObject();
        json.put("ret", 1);
        json.put("msg", "操作成功！");
        out.print(json.toString());
    } else if (op.equals("sync")) {
        // 如果启用了tigase则用tigase同步,否则用lark同步
        Config cfg = new Config();
        if (cfg.getBooleanProperty("isUse")) {
            TigaseConnection tc = new TigaseConnection();
            tc.deleteAll(userName);
            UserDb ud = new UserDb();
            Vector v = ud.list();
            if (!v.contains(UserDb.SYSTEM)) {
                UserDb sysUd = new UserDb();
                sysUd.setName(UserDb.SYSTEM);
                v.add(sysUd);
            }
            Iterator it = v.iterator();
            while (it.hasNext()) {
                UserDb user = (UserDb) it.next();
                // tc.syncUser(user.getName(), user.getPwdMD5());
                tc.addUser(user.getName());
            }
            // 同步OA信息
            tc.setOAInfo(userName);
            // 加好友
            tc.addFriends();
            // tc.addFriends();
            DeptDb dd = new DeptDb();
            v = dd.list();
            it = v.iterator();
            while (it.hasNext()) {
                DeptDb dept = (DeptDb) it.next();
                tc.syncDept(dept.getCode(), userName);
            }
        } else {
            SyncUtil su = new SyncUtil();
            DeptDb dd = new DeptDb();
            // 先删除
            su.allDelete();
            // 再添加
            String sql = "select code from department where code<>'root' order by layer asc";
            Vector dv = dd.list(sql);
            Iterator dit = dv.iterator();
            while (dit.hasNext()) {
                DeptDb dept = (DeptDb) dit.next();

                // 先加部门
                su.orgSync(dept, SyncUtil.CREATE, new Privilege().getUser(request));

                String deptCode = dept.getCode();
                DeptUserDb dud = new DeptUserDb();
                Vector duv = dud.list(deptCode);
                Iterator uit = duv.iterator();

                // 再加用户
                while (uit.hasNext()) {
                    DeptUserDb du = (DeptUserDb) uit.next();
                    if (du.getUserName().equals("admin")) {
                        continue;
                    }
                    su.userSync(new UserDb(du.getUserName()), deptCode, SyncUtil.CREATE, new Privilege().getUser(request));
                }
            }
        }
        out.print("同步完成！");
    }
%>
