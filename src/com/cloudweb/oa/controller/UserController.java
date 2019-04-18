package com.cloudweb.oa.controller;

import cn.js.fan.base.ObjectBlockIterator;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.module.pvg.Priv;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.account.AccountDb;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptMgr;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.dept.DeptUserMgr;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.hr.SalaryMgr;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.person.UserSetupMgr;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sso.SyncUtil;
import com.redmoon.oa.video.VideoMgr;
import com.redmoon.oa.visual.FormDAO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by fgf on 2018/11/30.
 */

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private HttpServletRequest request;

    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST, produces={"text/html;", "application/json;charset=UTF-8;"})
    public String list() {
        JSONObject jobject = new JSONObject();
        Privilege privilege = new Privilege();
        if (!privilege.isUserPrivValid(request, "admin.user")) {
            JSONObject json = new JSONObject();
            try {
                json.put("ret", "0");
                json.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }
        String unitCode = "";
        if (unitCode.equals(""))
            unitCode = privilege.getUserUnitCode(request);

        String searchType = ParamUtil.get(request, "searchType"); // realName-姓名、userName-帐户、account-工号、mobile-手机、Email
        if ("".equals(searchType)) {
            searchType = "realName";
        }

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean is_bind_mobile = cfg.getBooleanProperty("is_bind_mobile");
        boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");

        String op = StrUtil.getNullString(request.getParameter("op"));
        String condition = ParamUtil.get(request,"condition");
        int isValid = ParamUtil.getInt(request,"isValid",1);    //1:在职  0：离职
        String orderBy = ParamUtil.get(request, "orderBy");
        if (orderBy.equals(""))
            orderBy = "regDate";
        String sort = ParamUtil.get(request, "sort");
        if (sort.equals(""))
            sort = "desc";
        String deptCode = ParamUtil.get(request,"deptCode");

        DeptMgr dm = new DeptMgr();

        String sql = "";
        if (DeptDb.ROOTCODE.equals(deptCode)){
            sql = "select DISTINCT name from users where name<>"+StrUtil.sqlstr("system") + " and name<>'admin'";
            if (op.equals("search")){
                if("".equals(condition)) {
                    sql += " and isValid = " + isValid;
                } else {
                    sql += " and isValid = " + isValid;

                    if ("realName".equals(searchType)) {
                        sql += " and realName like "+StrUtil.sqlstr("%"+condition+"%");
                    }
                    else if ("userName".equals(searchType)) {
                        sql += " and name like "+StrUtil.sqlstr("%"+condition+"%");
                    }
                    else if ("mobile".equals(searchType)) {
                        sql += " and mobile like "+StrUtil.sqlstr("%"+condition+"%");
                    }
                    else if ("account".equals(searchType)) {
                        sql = "select DISTINCT u.name from users u, account a where u.name=a.username and u.name<>"+StrUtil.sqlstr("system");
                        sql += " and isValid = " + isValid + " and a.name like "+StrUtil.sqlstr("%"+condition+"%");
                    }
                    else if ("email".equals(searchType)) {
                        sql += " and email like "+StrUtil.sqlstr("%"+condition+"%");
                    }
                }
            } else {
                sql += " and isValid=" + isValid;
            }
            if (!unitCode.equals(DeptDb.ROOTCODE)) {
                sql += " and unit_code=" + unitCode;
            }
            sql +=  " order by orders desc," + orderBy + " " + sort;
        } else {
            if(op.equals("search")) {
                sql = "select DISTINCT u.name from users u,department d,dept_user du where u.name = du.user_name and d.code = du.dept_code and u.name<>"+StrUtil.sqlstr("system");
                if("".equals(condition)) {
                    sql += " and u.isValid = " + isValid;
                } else {
                    sql += " and u.isValid = " + isValid;
                    if ("realName".equals(searchType)) {
                        sql += " and realName like "+StrUtil.sqlstr("%"+condition+"%");
                    }
                    else if ("userName".equals(searchType)) {
                        sql += " and name like "+StrUtil.sqlstr("%"+condition+"%");
                    }
                    else if ("mobile".equals(searchType)) {
                        sql += " and mobile like "+StrUtil.sqlstr("%"+condition+"%");
                    }
                    else if ("account".equals(searchType)) {
                        sql = "select DISTINCT u.name from users u, account a where u.name=a.username and u.name<>"+StrUtil.sqlstr("system");
                        sql += " and isValid = " + isValid + " and a.name like "+StrUtil.sqlstr("%"+condition+"%");
                    }
                    else if ("email".equals(searchType)) {
                        sql += " and email like "+StrUtil.sqlstr("%"+condition+"%");
                    }
                }
            } else {
                sql = "select DISTINCT u.name from users u,department d,dept_user du  where u.name = du.user_name and d.code = du.dept_code and isValid = 1 and u.name<>"+StrUtil.sqlstr("system");
            }
            String cond = "";
            if (!DeptDb.ROOTCODE.equals(deptCode)){
                List<String> list = new ArrayList<String>();
                list = dm.getBranchDeptCode(deptCode,list);
                cond = " and ( ";
                for(int i=0;i<list.size();i++){
                    String code = list.get(i);
                    if (i==0){
                        cond += " du.dept_code = "+StrUtil.sqlstr(code);
                    } else {
                        cond += " or du.dept_code = "+StrUtil.sqlstr(code);
                    }
                    if (i==list.size()-1){
                        cond += " ) ";
                    }
                }
            }
            if (!unitCode.equals(DeptDb.ROOTCODE)) {
                sql += " and u.unit_code=" + unitCode;
            }
            if (showByDeptSort) {
                orderBy = "du.orders";
                sort = "desc";
                sql += cond + " order by " + orderBy + " " + sort;
            } else {
                sql += cond + " order by u.orders desc," + orderBy + " " + sort;
            }
        }

        // System.out.println(getClass() + " " + sql);

        UserDb userdb = new UserDb();
        int total = userdb.getUserCount(sql);

        try {
            JSONArray rows = new JSONArray();
            int pageSize = ParamUtil.getInt(request, "rp", 20);
            int curPage = ParamUtil.getInt(request, "page", 1);

            jobject.put("rows", rows);
            jobject.put("page", curPage);
            jobject.put("total", total);

            int start = (curPage-1)*pageSize;
            int end = curPage*pageSize;

            DeptUserDb du = new DeptUserDb();
            ObjectBlockIterator ir = userdb.getObjects(sql, start, end);
            AccountDb acc2 = new AccountDb();
            UserSetupDb usd = new UserSetupDb();
            while (ir.hasNext()) {
                UserDb user = (UserDb)ir.next();
                AccountDb acc = acc2.getUserAccount(user.getName());
                usd = usd.getUserSetupDb(user.getName());
                JSONObject jo = new JSONObject();

                jo.put("id", String.valueOf(user.getId()));

                if (showByDeptSort && !"".equals(deptCode)) {
                    du = du.getDeptUserDb(user.getName(), deptCode);
                    if (du!=null) {
                        jo.put("deptOrder", String.valueOf(du.getOrders()));
                    }
                    else {
                        jo.put("deptOrder", "");
                        du = new DeptUserDb();
                    }
                }
                jo.put("name", "<a href='javascript:;' onclick=\"addTab('" + user.getRealName() + "', '" + request.getContextPath() + "/admin/organize/user_edit.jsp?name=" + StrUtil.UrlEncode(user.getName()) + "')\">" + user.getName() + "</a>");
                jo.put("realName", user.getRealName());
                if (acc!=null) {
                    jo.put("account", acc.getName());
                }
                else {
                    jo.put("account", "");
                }
                jo.put("sex", user.getGender()==0?"男":"女");
                jo.put("mobile", user.getMobile());

                StringBuffer deptNames = new StringBuffer();
                Iterator ir2 = du.getDeptsOfUser(user.getName()).iterator();
                int k = 0;
                while (ir2.hasNext()) {
                    DeptDb dd = (DeptDb)ir2.next();
                    String deptName;
                    if (!dd.getParentCode().equals(DeptDb.ROOTCODE) && !dd.getCode().equals(DeptDb.ROOTCODE)) {
                        deptName = dm.getDeptDb(dd.getParentCode()).getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dd.getName();
                    }
                    else
                        deptName = dd.getName();
                    StrUtil.concat(deptNames, "，", deptName);
                    k++;
                }
                jo.put("deptNames", deptNames.toString());

                com.redmoon.oa.pvg.RoleDb[] rld = user.getRoles();
                int rolelen = 0;
                if (rld!=null)
                    rolelen = rld.length;
                String roleDescs = "";
                for (int m=0; m<rolelen; m++) {
                    if (rld[m]==null)
                        continue;
                    if (rld[m].getCode().equals(com.redmoon.oa.pvg.RoleDb.CODE_MEMBER)) {
                        continue;
                    }

                    if (roleDescs.equals("")) {
                        roleDescs = "<a href=\"javascript:;\" onclick=\"addTab('" + rld[m].getDesc() + "', '" + request.getContextPath() + "/admin/user_role_priv.jsp?roleCode=" + rld[m].getCode() + "')\">" + StrUtil.getNullStr(rld[m].getDesc()) + "</a>";
                    }
                    else {
                        roleDescs += "，" + "<a href=\"javascript:;\" onclick=\"addTab('" + rld[m].getDesc() + "', '" + request.getContextPath() + "/admin/user_role_priv.jsp?roleCode=" + rld[m].getCode() + "')\">" + StrUtil.getNullStr(rld[m].getDesc()) + "</a>";
                    }
                }
                jo.put("roleName", roleDescs);

                if (user.getValid() == 1) {
                    //out.print("已启用");
                    jo.put("status", "<img title='已启用' width=16 src='" + request.getContextPath() + "/skin/images/organize/icon-finish.png'/>");
                } else {
                    //out.print("未启用");
                    jo.put("status", "<img title='未启用' width=16 src='" + request.getContextPath() + "/skin/images/organize/stop.png'/>");
                }

                if (is_bind_mobile){
                    if (usd.isBindMobile())
                        jo.put("isBindMobile", "已绑定");
                    else
                        jo.put("isBindMobile", "未绑定");
                }
                rows.put(jo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jobject.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/editOrder", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String editOrder(@RequestParam(value = "id", required = true) int id, String colName, String original_value, String update_value, String deptCode) {
        JSONObject json = new JSONObject();
        Privilege privilege = new Privilege();
        if (!privilege.isUserPrivValid(request, "admin.user")) {
            try {
                json.put("ret", "0");
                json.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }
        if (update_value.equals(original_value)) {
            try {
                json.put("ret", "-1");
                json.put("msg", "值未更改！");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }
        boolean re = false;
        UserDb user = new UserDb();
        user = user.getUserDb(id);
        DeptUserDb du = new DeptUserDb();
        du = du.getDeptUserDb(user.getName(), deptCode);
        if (du!=null) {
            du.setOrders(StrUtil.toInt(update_value, 0));
            re = du.save();
        }

        try {
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/delUsers", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String delUsers(@RequestParam(value = "ids", required = true) String ids) {
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            Privilege privilege = new Privilege();
            if (!privilege.isUserPrivValid(request, "admin.user")) {
                json.put("ret", "0");
                json.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
                return json.toString();
            }
            String[] ary = StrUtil.split(ids, ",");
            if (ary == null) {
                json.put("ret", "0");
                json.put("msg", "请选择用户！");
                return json.toString();
            }

            UserMgr um = new UserMgr();
            UserDb user = new UserDb();
            String[] userNames = new String[ary.length];
            for (int i = 0; i < ary.length; i++) {
                user = user.getUserDb(StrUtil.toInt(ary[i]));
                userNames[i] = user.getName();
            }
            for (int i = 0; i < userNames.length; i++) {
                um.del(userNames[i]);

                //删除成功之后要更新表user_recently_selected，此表用来记录最近选择的用户
                String delSql = "delete from user_recently_selected where name = "+ StrUtil.sqlstr(userNames[i])+" or userName = "+ StrUtil.sqlstr(userNames[i]);
                JdbcTemplate jt = new JdbcTemplate();
                try {
                    jt.executeUpdate(delSql);
                } catch (SQLException e) {
                } finally {
                    jt.close();
                }
            }

            VideoMgr vmgr = new VideoMgr();
            if (vmgr.validate())
                vmgr.delUserByArr(userNames);
            re = true;
        } catch (ResKeyException e) {
            e.printStackTrace();
        } catch (ErrMsgException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/leaveOffBatch", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String leaveOffBatch(@RequestParam(value = "ids", required = true) String ids) {
        JSONObject json = new JSONObject();
        Privilege privilege = new Privilege();
        if (!privilege.isUserPrivValid(request, "admin.user")) {
            try {
                json.put("ret", "0");
                json.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }
        boolean re = false;
        try {
            String[] ary = StrUtil.split(ids, ",");
            if (ary == null) {
                try {
                    json.put("ret", "0");
                    json.put("msg", "请选择用户！");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return json.toString();
            }

            String opUser = privilege.getUser(request);
            UserMgr um = new UserMgr();
            UserDb user = new UserDb();
            for (int i = 0; i < ary.length; i++) {
                user = user.getUserDb(StrUtil.toInt(ary[i]));
                um.leaveOffice(request, user.getName(), opUser);
            }

            re = true;
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }
        try {
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/enableBatch", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String enableBatch(@RequestParam(value = "ids", required = true) String ids) {
        JSONObject json = new JSONObject();
        Privilege privilege = new Privilege();
        if (!privilege.isUserPrivValid(request, "admin.user")) {
            try {
                json.put("ret", "0");
                json.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }
        boolean re = true;
        String[] ary = StrUtil.split(ids, ",");
        if (ary == null) {
            try {
                json.put("ret", "0");
                json.put("msg", "请选择用户！");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }

        try {
            UserMgr um = new UserMgr();
            for (int i = 0; i < ary.length; i++) {
                re = um.reEmploryment(request, StrUtil.toInt(ary[i], -1), privilege.getUser(request));
            }
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * 调出
     * @param ids
     * @param deptCodes
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/changeDepts", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String changeDepts(@RequestParam(value = "ids", required = true) String ids, String deptCodes) {
        JSONObject json = new JSONObject();
        Privilege privilege = new Privilege();
        if (!privilege.isUserPrivValid(request, "admin.user")) {
            try {
                json.put("ret", "0");
                json.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }
        boolean re = false;

        String[] ary = StrUtil.split(ids, ",");
        if (ary == null) {
            try {
                json.put("ret", "0");
                json.put("msg", "请选择用户！");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }

        DeptUserMgr dum = new DeptUserMgr();
        String curUserName = privilege.getUser(request);

        try {
            UserDb user = new UserDb();
            for (int i = 0; i < ary.length; i++) {
                user = user.getUserDb(StrUtil.toInt(ary[i]));
                re = dum.changeDeptOfUser(user.getName(), deptCodes, curUserName);
            }
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * 调入
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/transferUsers", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String transferUsers() {
        JSONObject json = new JSONObject();
        Privilege privilege = new Privilege();
        if (!privilege.isUserPrivValid(request, "admin.user")) {
            try {
                json.put("ret", "0");
                json.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }
        boolean re = false;
        DeptUserMgr dum = new DeptUserMgr();
        try {
            re = dum.add(request);
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/bindBatch", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String bindBatch(@RequestParam(value = "ids", required = true) String ids) {
        JSONObject json = new JSONObject();
        Privilege privilege = new Privilege();
        if (!privilege.isUserPrivValid(request, "admin.user")) {
            try {
                json.put("ret", "0");
                json.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }
        boolean re = true;

        String[] ary = StrUtil.split(ids, ",");
        if (ary == null) {
            try {
                json.put("ret", "0");
                json.put("msg", "请选择用户！");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }
        try {
            UserSetupDb usd = new UserSetupDb();
            UserDb user = new UserDb();
            for (int i = 0; i < ary.length; i++) {
                user = user.getUserDb(StrUtil.toInt(ary[i]));
                usd = usd.getUserSetupDb(user.getName());
                usd.setBindMobile(true);
                re = usd.save();
            }
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/unbindBatch", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String unbindBatch(@RequestParam(value = "ids", required = true) String ids) {
        JSONObject json = new JSONObject();
        boolean re = true;
        String[] ary = StrUtil.split(ids, ",");
        if (ary == null) {
            try {
                json.put("ret", "0");
                json.put("msg", "请选择用户！");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }
        try {
            UserSetupMgr usm = new UserSetupMgr();
            UserDb user = new UserDb();
            for (int i = 0; i < ary.length; i++) {
                user = user.getUserDb(StrUtil.toInt(ary[i]));
                re = usm.unbindMoible(user.getName());
            }
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/setUserInfo", method = RequestMethod.POST, produces = { "text/html;charset=UTF-8;","application/json;" })
    public String setUserInfo(@RequestParam(value = "email", required = true) String email) {
        JSONObject json = new JSONObject();
        boolean re = true;
        try {
            Privilege pvg = new Privilege();
            UserDb user = new UserDb();
            user = user.getUserDb(pvg.getUser(request));
            user.setEmail(email);
            re = user.save();
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

}
