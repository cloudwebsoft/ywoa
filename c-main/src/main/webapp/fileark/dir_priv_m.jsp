<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.alibaba.fastjson.JSONArray" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ taglib uri="/WEB-INF/tlds/HelpDocTag.tld" prefix="help" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="leafPriv" scope="page" class="com.redmoon.oa.fileark.LeafPriv"/>
<%
    String dirCode = ParamUtil.get(request, "dirCode");
    String orderBy = ParamUtil.get(request, "orderBy");
    if (orderBy.equals("")) {
        orderBy = "name";
    }
    String sort = ParamUtil.get(request, "sort");

    String op = ParamUtil.get(request, "op");

    String isAll = ParamUtil.get(request, "isAll");
    if (dirCode.equals("")) {
        isAll = "y";
    }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>管理目录权限</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script>
        var curOrderBy = "<%=orderBy%>";
        var sort = "<%=sort%>";

        function doSort(orderBy) {
            if (orderBy == curOrderBy)
                if (sort == "asc")
                    sort = "desc";
                else
                    sort = "asc";

            window.location.href = "dir_priv_m.jsp?dirCode=<%=dirCode%>&isAll=<%=isAll%>&orderBy=" + orderBy + "&sort=" + sort;
        }
    </script>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>

    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>

    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>

    <script src="../js/jquery.bgiframe.js"></script>
</head>
<body>
<%@ include file="dir_inc_menu_top.jsp" %>
<script>
    o("menu2").className = "current";
</script>
<%
    if (isAll.equals("y")) {
        dirCode = Leaf.ROOTCODE;
        if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
            out.print(StrUtil.jAlert_Back(Privilege.MSG_INVALID, "提示"));
            return;
        }
    }

    leafPriv.setDirCode(dirCode);
    if (!(leafPriv.canUserDel(privilege.getUser(request)) || leafPriv.canUserExamine(privilege.getUser(request)))) {
        out.print(StrUtil.jAlert_Back(Privilege.MSG_INVALID + " 用户需对该节点拥有删除或审核的权限！", "提示"));
        return;
    }

    Leaf leaf = new Leaf();
    leaf = leaf.getLeaf(dirCode);

    String sql = leafPriv.getListSqlForAll(orderBy, sort);
    Vector result = null;
    if (isAll.equals("y")) {
        result = leafPriv.list(sql);
    } else {
        sql = leafPriv.getListSqlForDir(dirCode, orderBy, sort);
        result = leafPriv.list(sql);
    }
    Iterator ir = result.iterator();
%>
<br/>
<%if (!isAll.equals("y")) {%>
<table class="percent98" width="80%" align="center">
    <tr>
        <td align="right">
            <input class="btn" type="button" onclick="copyPriv()" value="复制" title="复制权限"/>
            <input class="btn" type="button" onclick="openWin('../admin/dept_role_group_sel.jsp', 800, 600)" value="选择" title="选择角色、部门、用户组或人员"/>
            <%--<help:HelpDocTag id="915" type="content" size="200"></help:HelpDocTag>--%>
            <%
                com.alibaba.fastjson.JSONArray arr = new JSONArray();
                DeptMgr deptMgr = new DeptMgr();
                while (ir.hasNext()) {
                    LeafPriv lp = (LeafPriv)ir.next();
                    int type = lp.getType();
                    JSONObject json = new JSONObject();
                    switch (type) {
                        case DocPriv.TYPE_DEPT:
                            DeptDb dd = deptMgr.getDeptDb(lp.getName());
                            if (dd!=null) {
                                json.put("kind", type);
                                json.put("code", dd.getCode());
                                json.put("name", dd.getName());
                                arr.add(json);
                            }
                            break;
                        case DocPriv.TYPE_ROLE:
                            RoleDb rd = new RoleDb();
                            rd = rd.getRoleDb(lp.getName());
                            json.put("kind", type);
                            json.put("code", rd.getCode());
                            json.put("name", rd.getDesc());
                            arr.add(json);
                            break;
                        case DocPriv.TYPE_USERGROUP:
                            UserGroupDb ug = new UserGroupDb();
                            ug = ug.getUserGroupDb(lp.getName());
                            json.put("kind", type);
                            json.put("code", ug.getCode());
                            json.put("name", ug.getDesc());
                            arr.add(json);
                            break;
                        case DocPriv.TYPE_USER:
                            UserDb ud = new UserDb();
                            ud = ud.getUserDb(lp.getName());
                            json.put("kind", type);
                            json.put("code", ud.getName());
                            json.put("name", ud.getRealName());
                            arr.add(json);
                            break;
                    }
                }
            %>
            <textarea id="deptRoleGroup" name="deptRoleGroup" style="display: none"><%=arr.toString()%></textarea>
        </td>
    </tr>
</table>
<%}%>
<table class="tabStyle_1 percent98" cellspacing="0" cellpadding="3" width="95%" align="center">
    <tbody>
    <tr>
        <td class="tabStyle_1_title" nowrap width="12%" style="cursor:pointer" onclick="doSort('name')">名称
            <%
                if (orderBy.equals("name")) {
                    if (sort.equals("asc")) {
                        out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
                    } else {
                        out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
                    }
                }
            %>
        </td>
        <td class="tabStyle_1_title" nowrap width="7%" style="cursor:pointer" onclick="doSort('priv_type')">类型<span class="right-title" style="cursor:pointer">
        <%
            if (orderBy.equals("priv_type")) {
                if (sort.equals("asc")) {
                    out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
                } else {
                    out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
                }
            }
        %>
      </span></td>
        <td class="tabStyle_1_title" nowrap width="14%" onclick="doSort('dir_code')" style="cursor:pointer">目录<span class="right-title" style="cursor:pointer">
        <%
            if (orderBy.equals("dir_code")) {
                if (sort.equals("asc")) {
                    out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
                } else {
                    out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
                }
            }
        %>
      </span></td>
        <td class="tabStyle_1_title" noWrap width="53%">权限</td>
        <td width="14%" nowrap class="tabStyle_1_title">操作</td>
    </tr>
        <%
int i = 0;
Directory dir = new Directory();
DeptMgr deptMgr = new DeptMgr();
ir = result.iterator();
while (ir.hasNext()) {
 	LeafPriv lp = (LeafPriv)ir.next();
	Leaf lf = dir.getLeaf(lp.getDirCode());	
	if (lf==null) {
        continue;
	}
	i++;
	%>
    <form id="form<%=i%>" name="form<%=i%>" action="?op=modify" method=post>
        <tr class="highlight" id="tr<%=i%>" dirPrivId="<%=lp.getId()%>">
            <td>
                <%
                    if (lp.getType() == LeafPriv.TYPE_USER) {
                        UserDb ud = new UserDb();
                        ud = ud.getUserDb(lp.getName());
                        out.print(ud.getRealName());
                    } else if (lp.getType() == LeafPriv.TYPE_ROLE) {
                        RoleDb rd = new RoleDb();
                        rd = rd.getRoleDb(lp.getName());
                        if (rd!=null) {
                            out.print(rd.getDesc());
                        }
                    } else if (lp.getType() == LeafPriv.TYPE_USERGROUP) {
                        UserGroupDb ug = new UserGroupDb();
                        ug = ug.getUserGroupDb(lp.getName());
                        if (ug!=null) {
                            out.print(ug.getDesc());
                        }
                    }
                    else if (lp.getType() == LeafPriv.TYPE_DEPT) {
                        DeptDb deptDb = deptMgr.getDeptDb(lp.getName());
                        if (deptDb!=null) {
                            out.print(deptDb.getName());
                        }
                    }
                %>
                <input type=hidden name="id" value="<%=lp.getId()%>"/>
                <input type=hidden name="dirCode" value="<%=lp.getDirCode()%>"/>
                <input type=hidden name="isAll" value="<%=isAll%>"/></td>
            <td>
                <%
                    if (lp.getType() == LeafPriv.TYPE_USER) {
                %>
                用户
                <%
                } else if (lp.getType() == LeafPriv.TYPE_ROLE) {
                %>
                角色
                <%
                } else if (lp.getType() == LeafPriv.TYPE_USERGROUP) {
                %>
                用户组
                <%
                    } else if (lp.getType() == LeafPriv.TYPE_DEPT) {
                %>
                部门
                <%
                    }
                %>
            </td>
            <td><a href="document_list_m.jsp?dirCode=<%=StrUtil.UrlEncode(lf.getCode())%>"><%=lf.getName()%>
            </a></td>
            <td>
                <input id="see" name="see" type=checkbox <%=lp.getSee() == 1 ? "checked" : ""%> value="1" onclick="checkPrivSee('tr<%=i%>')"/>浏览&nbsp;
                <input name="append" type=checkbox <%=lp.getAppend() == 1 ? "checked" : ""%> value="1" onclick="checkPrivAppend('tr<%=i%>')"/>
                添加 &nbsp;
                <input name="del" type=checkbox <%=lp.getDel() == 1 ? "checked" : ""%> value="1" onclick="checkPrivDel('tr<%=i%>')"/>
                删除&nbsp;
                <input name="modify" type=checkbox <%=lp.getModify() == 1 ? "checked" : ""%> value="1" onclick="checkPrivModify('tr<%=i%>')"/>
                修改
                <input name="downLoad" title="下载附件" type=checkbox <%=lp.getDownLoad() == 1 ? "checked" : ""%> value="1" onclick="checkPrivModify('tr<%=i%>')"/>
                下载附件
                <input name="exportWord" title="查看word" type=checkbox <%=lp.getExportWord() == 1 ? "checked" : ""%> value="1" onclick="checkPrivModify('tr<%=i%>')"/>
                查看word
                <input name="exportPdf" title="查看pdf" type=checkbox <%=lp.getExportPdf() == 1 ? "checked" : ""%> value="1" onclick="checkPrivModify('tr<%=i%>')"/>
                查看pdf
                <input name="examine" type=checkbox <%=lp.getExamine() == 1 ? "checked" : ""%> value="1" onclick="checkPrivExamine('tr<%=i%>')"/>
                管理
            </td>
            <td align="center">
                <input class="btn btn-default btn-modify" type="button" index="<%=i%>" value="修改"/>
                &nbsp;
                <input class="btn btn-default btn-del" type="button" dirPrivId="<%=lp.getId()%>" value="删除"/>
            </td>
        </tr>
    </form>
        <%}%>
</table>
<br/>
</body>
<script>
    $(function () {
       $('.btn-modify').click(function() {
           var i = $(this).attr('index');
           $.ajax({
               type: "post",
               url: "modifyDirPriv.do",
               contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
               data: $('#form' + i).serialize(),
               dataType: "html",
               beforeSend: function (XMLHttpRequest) {
                   $('body').showLoading();
               },
               success: function (data, status) {
                   data = $.parseJSON(data);
                   jAlert(data.msg, "提示");
               },
               complete: function (XMLHttpRequest, status) {
                   $('body').hideLoading();
               },
               error: function (XMLHttpRequest, textStatus) {
                   // 请求出错处理
                   alert(XMLHttpRequest.responseText);
               }
           });
       });

        $('.btn-del').click(function() {
            var self = this;
            jConfirm('您确定要删除吗?', '提示', function (r) {
                if (!r) {
                    return;
                } else {
                    var id = $(self).attr('dirPrivId');
                    $.ajax({
                        type: "post",
                        url: "delDirPriv.do",
                        contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                        data: {
                            id: id,
                            dirCode: '<%=dirCode%>'
                        },
                        dataType: "html",
                        beforeSend: function (XMLHttpRequest) {
                            $('body').showLoading();
                        },
                        success: function (data, status) {
                            data = $.parseJSON(data);
                            jAlert(data.msg, "提示");
                            if (data.ret ==1) {
                                $('tr[dirPrivId=' + id + ']').remove();
                                // 刷新deptRoleGroup
                                window.location.reload();
                            }
                        },
                        complete: function (XMLHttpRequest, status) {
                            $('body').hideLoading();
                        },
                        error: function (XMLHttpRequest, textStatus) {
                            // 请求出错处理
                            alert(XMLHttpRequest.responseText);
                        }
                    });
                }
            });
        });
    });

    function checkPrivSee(trId) {
        var isChecked = $("#" + trId + " input[name='see']").attr("checked");
        if (!isChecked) {
            $("#" + trId + " input[name='append']").attr("checked", false);
            $("#" + trId + " input[name='del']").attr("checked", false);
            $("#" + trId + " input[name='modify']").attr("checked", false);
            // $("#" + trId + " input[name='examine']").attr("checked", false);
        }
    }

    function checkPrivAppend(trId) {
        var isChecked = $("#" + trId + " input[name='append']").attr("checked");
        if (isChecked) {
            $("#" + trId + " input[name='see']").attr("checked", true);
        }
    }

    function checkPrivDel(trId) {
        var isChecked = $("#" + trId + " input[name='del']").attr("checked");
        if (isChecked) {
            $("#" + trId + " input[name='see']").attr("checked", true);
        }
    }

    function checkPrivModify(trId) {
        var isChecked = $("#" + trId + " input[name='modify']").attr("checked");
        if (isChecked) {
            $("#" + trId + " input[name='see']").attr("checked", true);
        }
    }

    function checkPrivExamine(trId) {
        var isChecked = $("#" + trId + " input[name='examine']").attr("checked");
        if (isChecked) {
            // $("#" + trId + " input[name='see']").attr("checked", true);
            // $("#" + trId + " input[name='append']").attr("checked", true);
            // $("#" + trId + " input[name='del']").attr("checked", true);
            // $("#" + trId + " input[name='modify']").attr("checked", true);
        }
    }

    function copyPriv() {
        openWin("dir_sel.jsp", 640, 480);
    }

    function selectNode(code, name) {
        $.ajax({
            type: "post",
            url: "copyPriv.do",
            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
            data: {
                sourceDirCode: code,
                destDirCode: "<%=dirCode%>"
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == 1) {
                    jAlert(data.msg, "提示", function () {
                        window.location.reload();
                    });
                } else {
                    jAlert(data.msg, "提示");
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('body').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    function getDeptRoleGroup() {
        return $('#deptRoleGroup').val();
    }

    function setDeptRoleGroup(jsonArr) {
        $('#deptRoleGroup').val(JSON.stringify(jsonArr));
        $.ajax({
            type: "post",
            url: "setDeptRoleGroupUser.do",
            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
            data: {
                dirCode: "<%=dirCode%>",
                deptRoleGroupUser: JSON.stringify(jsonArr),
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == 1) {
                    jAlert(data.msg, "提示", function () {
                        window.location.reload();
                    });
                } else {
                    jAlert(data.msg, "提示");
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('body').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }
</script>
</html>