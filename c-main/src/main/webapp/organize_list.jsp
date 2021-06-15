<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.kernel.*" %>
<%@page import="com.redmoon.oa.person.UserDb" %>
<%@page import="java.util.Iterator" %>
<%@page import="java.util.Vector" %>
<%@page import="cn.js.fan.db.Paginator" %>
<%@page import="cn.js.fan.util.ParamUtil" %>
<%@page import="cn.js.fan.web.SkinUtil" %>
<%@page import="cn.js.fan.util.StrUtil" %>
<%@page import="cn.js.fan.db.ListResult" %>
<%@ page import="com.redmoon.oa.Config" %>
<%@ page import="com.cloudweb.oa.service.IDeptUserService" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.entity.DeptUser" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String op = ParamUtil.get(request, "op");
    String realName = ParamUtil.get(request, "realName");
    String mobile = ParamUtil.get(request, "mobile");
    String email = ParamUtil.get(request, "email");
    String deptCode = ParamUtil.get(request, "deptCode");
    String phone = ParamUtil.get(request, "phone");
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>组织机构</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="inc/common.js"></script>
    <script src="js/jquery-1.9.1.min.js"></script>
    <script src="js/jquery-migrate-1.2.1.min.js"></script>
    <script src='dwr/interface/DeptUserDb.js'></script>
    <script src='dwr/engine.js'></script>
    <script src='dwr/util.js'></script>
    <link rel="stylesheet" href="js/bootstrap/css/bootstrap.min.css"/>
    <script src="js/bootstrap/js/bootstrap.min.js"></script>
</head>
<body>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    if (deptCode.equals("")) {
        deptCode = DeptDb.ROOTCODE;
    }
%>
<div class="container">
    <div class="row">
        <div class="col-lg-12 col-lg-offset-0">
            <div class="table-responsive">
                <form id="formSearch" method="get" action="organize_list.jsp?">
                    <table align="center" style="margin-top: 10px">
                        <tr>
                            <td style="text-align: center">
                                <div class="form-inline form-group">
                                    部门
                                    <select id="deptCode" name="deptCode" class="form-control">
                                        <%
                                            DeptDb deptDb = new DeptDb();
                                            deptDb = deptDb.getDeptDb(privilege.getUserUnitCode(request));
                                            DeptView deptView = new DeptView(deptDb);
                                            deptView.ShowDeptAsOptions(out, deptDb, 1);
                                        %>
                                    </select>
                                    姓名
                                    <input id="realName" name="realName" style="width: 100px" class="form-control" value="<%=realName%>"/>
                                    手机
                                    <input id="mobile" name="mobile" style="width: 100px" autocomplete="off" class="form-control" value="<%=mobile%>"/>
                                    邮箱
                                    <input id="email" name="email" style="width: 100px" autocomplete="off" class="form-control" value="<%=email%>"/>
                                    电话
                                    <input id="phone" name="phone" style="width:100px" autocomplete="off" class="form-control" value="<%=phone%>"/>
                                    <input id="op" name="op" type="hidden" value="search"/>
                                    <button type="submit" class="btn btn-default">搜索</button>
                                    <script>
                                        $(function() {
                                            $('#deptCode').val('<%=deptCode%>');
                                        })
                                    </script>
                                </div>
                            </td>
                        </tr>
                    </table>
                </form>
                <%
                    String depts = "";
                    if (!DeptDb.ROOTCODE.equals(deptCode)) {
                        deptDb = deptDb.getDeptDb(deptCode);
                        Vector dv = new Vector();
                        deptDb.getAllChild(dv, deptDb);
                        depts = StrUtil.sqlstr(deptCode);
                        Iterator ird = dv.iterator();
                        while (ird.hasNext()) {
                            deptDb = (DeptDb) ird.next();
                            depts += "," + StrUtil.sqlstr(deptDb.getCode());
                        }
                    }

                    DeptUserDb deptUserDb = new DeptUserDb();
                    UserDb ud = new UserDb();

                    Config cfg = new Config();
                    boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");
                    String orderField = showByDeptSort ? "du.orders" : "u.orders";

                    int curpage = ParamUtil.getInt(request, "CPages", 1);
                    int pagesize = ParamUtil.getInt(request, "pageSize", 20);

                    IDeptUserService deptUserService = SpringUtil.getBean(IDeptUserService.class);
                    ListResult lr = deptUserService.listBySearch(op, realName, mobile, email, depts, phone, orderField, curpage, pagesize);
                    long total = lr.getTotal();
                    Paginator paginator = new Paginator(request, total, pagesize);
                    // 设置当前页数和总页数
                    int totalpages = paginator.getTotalPages();
                    if (totalpages == 0) {
                        curpage = 1;
                        totalpages = 1;
                    }
                %>
                <table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
                    <tr>
                        <td>&nbsp;</td>
                    </tr>
                    <tr>
                        <td width="95%" align="center" valign="top">
                            <div id="resultTable">
                                <table class="tabStyle_1 percent98" width="100%" cellpadding="4" cellspacing="0">
                                    <tr>
                                        <td width="11%" class="tabStyle_1_title">部门</td>
                                        <td width="14%" class="tabStyle_1_title">人员</td>
                                        <td width="7%" class="tabStyle_1_title">性别</td>
                                        <td width="11%" class="tabStyle_1_title">手机</td>
                                        <td width="11%" class="tabStyle_1_title">邮箱</td>
                                        <td width="11%" class="tabStyle_1_title">电话</td>
                                        <td width="12%" class="tabStyle_1_title">操作</td>
                                    </tr>
                                    <tbody id="postsbody">
                                    <%
                                        Vector v = lr.getResult();
                                        Iterator ir = v.iterator();
                                        DeptDb dd = new DeptDb();
                                        while (ir.hasNext()) {
                                            DeptUser pu = (DeptUser) ir.next();
                                            if (pu.getUserName()!=null && !pu.getUserName().equals("")) {
                                                ud = ud.getUserDb(pu.getUserName());
                                            } else {
                                                continue;
                                            }
                                            dd = deptDb.getDeptDb(pu.getDeptCode());
                                    %>
                                    <tr class="highlight">
                                        <!-- <td colspan="<%=License.getInstance().isGov()?6:4%>">请选择部门</td> -->
                                        <td align="center"><%=dd.getName() %>
                                        </td>
                                        <td align="center"><%=ud.getRealName() %>
                                        </td>
                                        <td align="center"><%=ud.getGender() == 0 ? "男" : "女"%>
                                        </td>
                                        <td><%=ud.getMobile()%>
                                        </td>
                                        <td><%=ud.getEmail()%>
                                        </td>
                                        <td><%=ud.getPhone()%>
                                        </td>
                                        <td align="center">
                                            <!--
                                        <a href="javascript:;" onclick="addTab('网盘共享','netdisk/netdisk_frame.jsp?op=showDirShare&userName=<%=pu.getUserName() %>')">网盘共享</a>-->
                                            <a href="javascript:;" onclick="addTab('<%=ud.getRealName()%>','user_info.jsp?userName=<%=pu.getUserName() %>')">查看</a>&nbsp;&nbsp;&nbsp;
                                        </td>
                                    </tr>
                                    <%} %>
                                    </tbody>
                                </table>
                                <table class="percent98" width="92%" border="0" cellspacing="1" cellpadding="3" align="center">
                                    <tr>
                                        <td height="23" align="right"><%
                                            String querystr = "deptCode=" + StrUtil.UrlEncode(deptCode) + "&realName=" + StrUtil.UrlEncode(realName) + "&mobile=" + mobile + "&email=" + email;
                                            out.print(paginator.getCurPageBlock("organize_list.jsp?" + querystr));
                                        %></td>
                                    </tr>
                                </table>
                            </div>
                        </td>
                    </tr>
                </table>
            </div>
        </div>
    </div>
</div>
</body>
</html>
