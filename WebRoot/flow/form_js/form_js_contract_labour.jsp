<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="cn.js.fan.db.ResultIterator" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="cn.js.fan.db.ResultRecord" %>
<%@ page import="java.util.Date" %>
<%@ page import="cn.js.fan.util.DateUtil" %>
<%@ page import="java.sql.SQLException" %><%--
  Created by IntelliJ IDEA.
  User: qcg
  Date: 2018/7/24
  Time: 19:37
  To change this template use File | Settings | File Templates.
--%>
<%
    String action = ParamUtil.get(request,"action");
    String menuItem = ParamUtil.get(request,"menuItem");
    String sxqStr = ParamUtil.get(request,"sxq");
    //取出上次的合同信息
    String parentId = ParamUtil.get(request,"parentId");
    JSONObject json = new JSONObject();
    if ("xq".equals(action)){
        String sql = "";
        if ("".equals(parentId)){
            String id = ParamUtil.get(request,"id");
            parentId = id;
            sql = "select * from form_table_contract_labour where id = ? order by id desc";
        }else{
            sql = "select * from form_table_contract_labour where cws_id = ? order by id desc";
        }
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[]{parentId});
            if (ri.size() > 0) {
                ResultRecord rr = (ResultRecord) ri.next();
                Date sxq = rr.getDate("sxq");
                String shixiaoqi = "";
                if (sxq != null) {
                    shixiaoqi = DateUtil.format(sxq, "yyyy-MM-dd");
                    json.put("ret", "1");
                    json.put("sxq", shixiaoqi);
                }
            } else {
                json.put("ret", "0");
                json.put("msg", "没有相应的合同!");
            }
        }catch (SQLException e){
            e.printStackTrace();
            json.put("ret", "0");
            json.put("msg", "操作失败!");
        }finally {
            jt.close();
        }
        out.print(json);
        return;
    }
%>

function xq(){
    $.ajax({
        url:"<%=request.getContextPath()%>/flow/form_js_contract_labour.jsp?action=xq&parentId=<%=parentId%>&menuItem=<%=menuItem%>",
        type:"get",
        dataType:"json",
        success:function(data, status){
            if(data.ret === "1"){
                var str = data.sxq;
                window.location.href = "<%=request.getContextPath()%>/visual/module_add_relate.jsp?parentId=<%=parentId%>&code=personbasic&formCode=personbasic&formCodeRelated=contract_labour&isShowNav=1&menuItem=<%=menuItem%>&sxq=" + str;
            }
         },
        error:function(XMLHttpRequest, textStatus){
                alert(XMLHttpRequest.responseText);
         }
    });
}
$(function(){
    if("<%=sxqStr%>" != ""){
        $("#sxq").val("<%=sxqStr%>");
    }
});

function xq1(){
    var selectedCount = $(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).length;
    if (selectedCount == 0) {
        jAlert('请选择记录!', '提示');
        return;
    }
    else if (selectedCount>1) {
        jAlert('请选择一条记录!', '提示');
        return;
    }
    var id = "";
    // value!='on' 过滤掉复选框按钮
    $(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).each(function(i) {
        id = $(this).val().substring(3);
    });
    xqId(id);
}

function xqId(id){
    $.ajax({
        url:"<%=request.getContextPath()%>/flow/form_js_contract_labour.jsp?action=xq&id=" + id + "&menuItem=<%=menuItem%>",
        type:"get",
        dataType:"json",
        success:function(data, status){
            if(data.ret === "1"){
                var str = data.sxq;
                window.location.href = "<%=request.getContextPath()%>/visual/module_add.jsp?code=15324301182242111172&sxq=" + str;
            }
        },
        error:function(XMLHttpRequest, textStatus){
            alert(XMLHttpRequest.responseText);
        }
    });
}