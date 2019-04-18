<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.ui.menu.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.ui.menu.Directory"/>
<%
String flag = ParamUtil.get(request, "flag");//判断是否从引导页面跳转过来的
String priv="admin";
if (!privilege.isUserPrivValid(request,priv)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

Leaf leaf = new Leaf();
String op = ParamUtil.get(request, "op");
if (op.equals("save"))
{
    boolean re = true;
    try {
        String code = ParamUtil.get(request, "code");
        boolean isUseType = ParamUtil.getBoolean(request, "isUse", false);
        leaf = leaf.getLeaf(code);
        leaf.setUse(isUseType);
        
        re = leaf.modifyLeafStyle(leaf);
    }
    catch (ErrMsgException e) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));     
        return;
    }
    if (re)
        out.print(StrUtil.Alert(SkinUtil.LoadString(request, "res.label.forum.admin.menu_bottom", "edit_success")));
}
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=8">
<title>功能中心</title>
<script src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/hopscotch/hopscotch.js"></script>
<link href="<%=SkinMgr.getSkinPath(request)%>/css/admin/functionManage/function_edit.css" rel="stylesheet" type="text/css" />
<link type="text/css" rel="stylesheet" href="../js/hopscotch/css/hopscotch.css" />
<style>
.hopscotch-nav-button .next .hopscotch-next{
	
}
</style>
</head>
<script>
    //页面加载完成后执行内容，设置全选按钮
    $(document).ready(function(){
        checkAllStyle();
    });
    //判断是否需要切换全选按钮状态
    function checkAllStyle()
    {
        //设置初级全选按钮
        var junior = 0; 
        $(".junior").each(function(i){
            if (this.src.indexOf("checkbox_sel") != -1)
            {
                junior++;
            }
        });
        if (junior == 0)
        {
            $("#junior").attr("src","<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkall_not.png");
        }
        else
        {
            $("#junior").attr("src","<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkall_sel.png");
        }
         //设置中级全选按钮
        var intermediate = 0; 
        $(".intermediate").each(function(i){
            if (this.src.indexOf("checkbox_sel") != -1)
            {
                intermediate++;
            }
        });
        if (intermediate == 0)
        {
            $("#intermediate").attr("src","<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkall_not.png");
        }
        else
        {
            $("#intermediate").attr("src","<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkall_sel.png");
        }
         //设置高级全选按钮
        var senior = 0; 
        $(".senior").each(function(i){
            if (this.src.indexOf("checkbox_sel") != -1)
            {
                senior++;
            }
        });
        if (senior==0)
        {
            $("#senior").attr("src","<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkall_not.png");
        }
        else
        {
            $("#senior").attr("src","<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkall_sel.png");
        }
    }
    //复选框图点击
    function changeCheck(strId,name)
    {
        var strIds = strId.split(",");
        //若是图为选中图，则点击后切换为非选中图，启用改为不启用  启用：1 不启用：0
        var isUse = false;
        if (document.getElementById(strId).src.indexOf("checkbox_sel") != -1 )
        {
            document.getElementById(strId).src = "<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png";
            
        }
        else
        {
            document.getElementById(strId).src = "<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png";
            isUse = true;
        }
        for (var i=0; i < strIds.length;i++)
        {
            $.ajax({
                type: "post",
                url: "functionManage.jsp",
                data: {
                    op: "save",
                    code:strIds[i],
                    isUse: isUse
                },
                dataType: "html",
                beforeSend: function(XMLHttpRequest){
                },
                success: function(data, status){
                },
                complete: function(XMLHttpRequest, status){
                },
                error: function(XMLHttpRequest, textStatus){
                    // 请求出错处理
                    alert(XMLHttpRequest.responseText);
                }
            });
        
        }
        checkAllStyle();
    }
    //全选或者取消全选
    function checkAll(id)
    {
        if ($("#" + id).attr("src").indexOf("checkall_sel") != -1)
        {
           $("#" + id).attr("src", "<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkall_not.png");
           $("." + id).each(function(i){
                 this.src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png";
                 var thisIds = this.id.split(",");
                 for (i = 0; i < thisIds.length; i++)
                 {
                     $.ajax({
                        type: "post",
                        url: "functionManage.jsp",
                        data: {
                            op: "save",
                            code:thisIds[i],
                            isUse: false
                        },
                        dataType: "html",
                        beforeSend: function(XMLHttpRequest){
                        },
                        success: function(data, status){
                        },
                        complete: function(XMLHttpRequest, status){
                        },
                        error: function(XMLHttpRequest, textStatus){
                            // 请求出错处理
                            alert(XMLHttpRequest.responseText);
                        }
                    }); 
                }
            });
        }else{
            $("#" + id).attr("src", "<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkall_sel.png");
            $("." + id).each(function(i){
               this.src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png";
               var thisIds = this.id.split(",");
                 for (i = 0; i < thisIds.length; i++)
                 {
                     $.ajax({
                        type: "post",
                        url: "functionManage.jsp",
                        data: {
                            op: "save",
                            code:thisIds[i],
                            isUse: true
                        },
                        dataType: "html",
                        beforeSend: function(XMLHttpRequest){
                        },
                        success: function(data, status){
                        },
                        complete: function(XMLHttpRequest, status){
                        },
                        error: function(XMLHttpRequest, textStatus){
                            // 请求出错处理
                            alert(XMLHttpRequest.responseText);
                        }
                    }); 
                }
            });
        }
       checkAllStyle();
    }
    function showChild(thisId,id)
    {
        if ($("#" + thisId).attr("class").indexOf("function_arrow") != -1)
        {
            $("#" + thisId).attr("class","function_down");
            $("#" + id).hide();
        }
        else
        {
             $("#" + thisId).attr("class","function_arrow");
            $("#" + id).show();
        }
    }

</script>
<body>
<div class="functionbox">
  <div class="function_top">
    <!--<div class="function_btn"><a href="javascript:void(0)"><img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/function_btn.png" width="147" height="52" /></a></div>
  --></div>
  <div >
<!--初级功能--><div class="function_left" >
      <div><span><img class="allCheck" src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkall_sel.png" id="junior" width="61" height="24" onClick="checkAll('junior')"/></span>
           <span class="span_font">(请选择需要的功能)</span></div>
      <div class="function_list">
        <div></div>
        <div class="function_title" onClick="showChild('workFlow4J','workFlow4Junior')">工作流程
          <div class="function_arrow" id="workFlow4J" ></div>
        </div>
        <div id="workFlow4Junior">
        <div>
        <span class="checkbox"><a href="javascript:void(0)">
            <% 
            int isUse = leaf.getLeafFromDb("503660360");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="junior" width="19" height="19" onClick="changeCheck('503660360','@流程')" id="503660360"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="junior" width="19" height="19" onClick="changeCheck('503660360','@流程')" id="503660360"/>
            <%} %>
        </a></span><span  id="radioButton">工作交办</span></div>
        <div>
        <span class="checkbox"><a href="javascript:void(0)">
            <% 
              isUse = leaf.getLeafFromDb("flow_wait");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="junior" width="19" height="19" onClick="changeCheck('flow_wait','待办流程')" id="flow_wait"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="junior" width="19" height="19" onClick="changeCheck('flow_wait','待办流程')" id="flow_wait"/>
            <%} %>
        
        </a></span>待办流程</div>
        <div>
        <span class="checkbox"><a ref="javascript:void(0)">
             <% 
              isUse = leaf.getLeafFromDb("myflow");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="junior" width="19" height="19" onClick="changeCheck('myflow','我的流程')" id="myflow"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="junior" width="19" height="19" onClick="changeCheck('myflow','我的流程')" id="myflow"/>
            <%} %>   
         </a></span>我的流程</div>
        <div>
        <span class="checkbox"><a href="javascript:void(0)">
             <% 
              isUse = leaf.getLeafFromDb("flow_query");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="junior" width="19" height="19" onClick="changeCheck('flow_query','流程查询')" id="flow_query"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="junior" width="19" height="19" onClick="changeCheck('flow_query','流程查询')" id="flow_query"/>
            <%} %>
        </a></span>流程查询</div>
        <div>        
      <span class="checkbox"><a href="javascript:void(0)">
         <% 
          isUse = leaf.getLeafFromDb("flow_launch");
          if (isUse == 1)
          {
          %>
              <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="junior" width="19" height="19" onClick="changeCheck('flow_launch,1541789744','发起流程')" id="flow_launch"/>
          <%}
          else{%>
              <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="junior" width="19" height="19" onClick="changeCheck('flow_launch,1541789744','发起流程')" id="flow_launch"/>
          <%} %>
      </a></span>发起流程</div>
        </div>
      </div>
      <div class="function_list">
        <div></div>
        <div class="function_title" onClick="showChild('admin4J','administration4Junior')">行政管理
          <div class="function_arrow" id="admin4J" ></div>
        </div>
        <div id="administration4Junior">
        <div>        
      <span class="checkbox">
        <a href="javascript:void(0)">
            <% 
                isUse = leaf.getLeafFromDb("notice");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="junior" width="19" height="19" onClick="changeCheck('notice','通知公告')" id="notice"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="junior" width="19" height="19" onClick="changeCheck('notice','通知公告')" id="notice"/>
            <%} %>
        </a>
       </span><span>通知公告</span></div>
        <div>
        <span class="checkbox"><a href="javascript:void(0)">
         <% 
               isUse = leaf.getLeafFromDb("mywork");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="junior" width="19" height="19" onClick="changeCheck('mywork','工作报告')" id="mywork"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="junior" width="19" height="19" onClick="changeCheck('mywork','工作报告')" id="mywork"/>
            <%} %>
        </a></span>工作报告</div>
        <div>        
      <span class="checkbox">
        <a href="javascript:void(0)">
            <% 
              isUse = leaf.getLeafFromDb("kaoqin");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="junior" width="19" height="19" onClick="changeCheck('kaoqin','在线考勤')" id="kaoqin"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="junior" width="19" height="19" onClick="changeCheck('kaoqin','在线考勤')" id="kaoqin"/>
            <%} %>
        
        </a></span>在线考勤</div>
        </div>
      </div>
      
      <div class="function_list" >
        <div></div>
        <div class="function_title" onClick="showChild('hmR4J','humanResourse4Junior')">人力资源
          <div class="function_arrow" id="hmR4J" ></div>
        </div>
        <div id="humanResourse4Junior">
        
        <div>
        <span class="checkbox"><a ref="javascript:void(0)">
             <% 
              isUse = leaf.getLeafFromDb("2013222645");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="junior" width="19" height="19" onClick="changeCheck('2013222645','考勤管理')" id="2013222645"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="junior" width="19" height="19" onClick="changeCheck('2013222645','考勤管理')" id="2013222645"/>
            <%} %>
        </a></span>考勤管理</div>
        </div>
      </div>
       <div class="function_list" >
        <div></div>
        <div class="function_title" onClick="showChild('repCenter4J','reportCenter4Junior')">报表中心
          <div class="function_arrow" id="repCenter4J" ></div>
        </div>
        <div id="reportCenter4Junior">
        <div>
        <span class="checkbox"><a href="javascript:void(0)">
             <% 
              isUse = leaf.getLeafFromDb("1646734196");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="junior" width="19" height="19" onClick="changeCheck('91536622','加班统计表')" id="1646734196"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="junior" width="19" height="19" onClick="changeCheck('91536622','加班统计表')" id="1646734196"/>
            <%} %>
        </a></span>模板列表</div>
        </div>
      </div>
</div>
<!--中级功能--><div class="function_middle" >
      <div><span><img class="allCheck" src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkall_sel.png" id="intermediate" width="61" height="24" onClick="checkAll('intermediate')"/></span>
           <span class="span_font">(请选择需要的功能)</span></div>
      <div class="function_list">
        <div></div>
        <div class="function_title" onClick="showChild('workFlow4I','workFlow4Intermediate')">工作流程
          <div class="function_arrow" id="workFlow4I" ></div>
        </div>
        <div id="workFlow4Intermediate">
        
        <div>
      <span class="checkbox"><a href="javascript:void(0)">
         <% 
              isUse = leaf.getLeafFromDb("proxy");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="intermediate" width="19" height="19" onClick="changeCheck('proxy','设置代理')" id="proxy"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="intermediate" width="19" height="19" onClick="changeCheck('proxy','设置代理')" id="proxy"/>
            <%} %>
      </a></span>设置代理</div>
      </div>
      </div>
      <div class="function_list">
        <div></div>
        <div class="function_title" onClick="showChild('projManage4I','projManage4Intermediate')">行政管理
          <div class="function_arrow" id="projManage4I" ></div>
        </div>
        <div id="projManage4Intermediate">
        <div>
        <span class="checkbox"><a href="javascript:void(0)">
             <% 
              isUse = leaf.getLeafFromDb("workplan");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="intermediate" width="19" height="19" onClick="changeCheck('workplan','工作计划')" id="workplan"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="intermediate" width="19" height="19" onClick="changeCheck('workplan','工作计划')" id="workplan"/>
            <%} %>
        </a></span>工作计划</div>
        </div>
      </div> 
      <div class="function_list">
        <div></div>
        <div class="function_title" onClick="showChild('saleManage4I','saleManage4Intermediate')">销售管理
          <div class="function_arrow" id="saleManage4I" ></div>
        </div>
        <div id="saleManage4Intermediate">
        <div>
        <span class="checkbox"><a href="javascript:void(0)">
             <% 
              isUse = leaf.getLeafFromDb("sales");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="intermediate" width="19" height="19" onClick="changeCheck('sales','销售管理')" id="sales"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="intermediate" width="19" height="19" onClick="changeCheck('sales','销售管理')" id="sales"/>
            <%} %>
        </a></span>销售管理</div>
        <div>
        <span class="checkbox"><a href="javascript:void(0)">
             <% 
              isUse = leaf.getLeafFromDb("customer");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="intermediate" width="19" height="19" onClick="changeCheck('customer','客户管理')" id="customer"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="intermediate" width="19" height="19" onClick="changeCheck('customer','客户管理')" id="customer"/>
            <%} %>
        </a></span>客户管理</div>
        <div>
        <span class="checkbox"><a href="javascript:void(0)">
             <% 
              isUse = leaf.getLeafFromDb("sales_m");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="intermediate" width="19" height="19" onClick="changeCheck('sales_m','销售管理')" id="sales_m"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="intermediate" width="19" height="19" onClick="changeCheck('sales_m','销售管理')" id="sales_m"/>
            <%} %>
        </a></span>销售管理</div>
        <div>
        <span class="checkbox"><a href="javascript:void(0)">
             <% 
              isUse = leaf.getLeafFromDb("provider");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="intermediate" width="19" height="19" onClick="changeCheck('provider','供应商')" id="provider"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="intermediate" width="19" height="19" onClick="changeCheck('provider','供应商')" id="provider"/>
            <%} %>
        </a></span>供应商</div>
        </div>
      </div>
       <div class="function_list">
        <div></div>
        <div class="function_title" onClick="showChild('superManage4I','superManage4Intermediate')">超级管理
          <div class="function_arrow" id="superManage4I" ></div>
        </div>
        <div id="superManage4Intermediate">
        <div>
        <span class="checkbox"><a href="javascript:void(0)">
            <% 
              isUse = leaf.getLeafFromDb("447300664");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="intermediate" width="19" height="19" onClick="changeCheck('447300664','CRM')" id="447300664"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="intermediate" width="19" height="19" onClick="changeCheck('447300664','CRM')" id="447300664"/>
            <%} %>
        </a></span>CRM</div>
        </div>
      </div>
      <%
      if (License.getInstance().isBiz())
      {
      %>
      <div class="function_list">
        <div></div>
        <div class="function_title" onClick="showChild('busiApps4I','busiApps4Intermediate')">商务应用
          <div class="function_arrow" id="busiApps4I" ></div>
        </div>
        <div id="busiApps4Intermediate">
        <div>        
      <span class="checkbox"><a href="javascript:void(0)">
         <% 
              isUse = leaf.getLeafFromDb("344394775");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="intermediate" width="19" height="19" onClick="changeCheck('344394775','商务应用')" id="344394775"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="intermediate" width="19" height="19" onClick="changeCheck('344394775','商务应用')" id="344394775"/>
            <%} %>
      </a></span>商务应用</div>
        <div>        
      <span class="checkbox"><a href="javascript:void(0)">
         <% 
              isUse = leaf.getLeafFromDb("1337054259");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="intermediate" width="19" height="19" onClick="changeCheck('1337054259','携程')" id="1337054259"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="intermediate" width="19" height="19" onClick="changeCheck('1337054259','携程')" id="1337054259"/>
            <%} %>
      </a></span>携程商旅</div>
       
      </div>
      </div>  
      <%} %> 
</div>
<!--高级功能--><div class="function_right" > 
      <div><span><img class="allCheck" src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkall_sel.png"  id="senior"  width="61" height="24" onClick="checkAll('senior')"/></span>
           <span class="span_font">(请选择需要的功能)</span></div>
      <div class="function_list">
        <div></div>
        <div class="function_title" onClick="showChild('document4S','document4Senior')">公文管理
          <div class="function_arrow" id="document4S" ></div>
        </div>
        <div id="document4Senior">
        <div>        
      <span class="checkbox"><a href="javascript:void(0)">
        <% 
              isUse = leaf.getLeafFromDb("2077723061");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="senior" width="19" height="19" onClick="changeCheck('2077723061','公文管理')" id="2077723061"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="senior" width="19" height="19" onClick="changeCheck('2077723061','公文管理')" id="2077723061"/>
            <%} %>
       </a></span>公文管理</div>
        <div>        
      <span class="checkbox"><a href="javascript:void(0)">
        <% 
              isUse = leaf.getLeafFromDb("1959332872");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="senior" width="19" height="19" onClick="changeCheck('1959332872','发文处理')" id="1959332872"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="senior" width="19" height="19" onClick="changeCheck('1959332872','发文处理')" id="1959332872"/>
            <%} %>
       </a></span>发文处理</div>
       <div>        
      <span class="checkbox"><a href="javascript:void(0)">
        <% 
              isUse = leaf.getLeafFromDb("161833996");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="senior" width="19" height="19" onClick="changeCheck('161833996','收文处理')" id="161833996"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="senior" width="19" height="19" onClick="changeCheck('161833996','收文处理')" id="161833996"/>
            <%} %>
       </a></span>收文处理</div>
       <div>        
      <span class="checkbox"><a href="javascript:void(0)">
        <% 
              isUse = leaf.getLeafFromDb("368505840");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="senior" width="19" height="19" onClick="changeCheck('368505840','归档记录')" id="368505840"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="senior" width="19" height="19" onClick="changeCheck('368505840','归档记录')" id="368505840"/>
            <%} %>
       </a></span>归档记录</div>
       <div>        
      <span class="checkbox"><a href="javascript:void(0)">
        <% 
              isUse = leaf.getLeafFromDb("1497474072");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="senior" width="19" height="19" onClick="changeCheck('1497474072','流程归档')" id="1497474072"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="senior" width="19" height="19" onClick="changeCheck('1497474072','流程归档')" id="1497474072"/>
            <%} %>
       </a></span>流程归档</div>
       <div>        
      <span class="checkbox"><a href="javascript:void(0)">
        <% 
              isUse = leaf.getLeafFromDb("1022784147");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="senior" width="19" height="19" onClick="changeCheck('1022784147','流程分发')" id="1022784147"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="senior" width="19" height="19" onClick="changeCheck('1022784147','流程分发')" id="1022784147"/>
            <%} %>
       </a></span>流程分发</div>
       </div>
      </div>
      <div class="function_list">
        <div></div>
        <div class="function_title" onClick="showChild('superManage4S','superManage4Senior')">超级管理
          <div class="function_arrow" id="superManage4S" ></div>
        </div>
        <div id="superManage4Senior">
        <div>
        <span class="checkbox"><a href="javascript:void(0)">
            <% 
              isUse = leaf.getLeafFromDb("1951950907");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="senior" width="19" height="19" onClick="changeCheck('1951950907','即时通讯')" id="1951950907"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="senior" width="19" height="19" onClick="changeCheck('1951950907','即时通讯')" id="1951950907"/>
            <%} %>
        </a></span>即时消息</div>
        <div>
        <span class="checkbox"><a href="javascript:void(0)">
            <% 
              isUse = leaf.getLeafFromDb("1316755744");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="senior" width="19" height="19" onClick="changeCheck('1316755744','短信管理')" id="1316755744"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="senior" width="19" height="19" onClick="changeCheck('1316755744','短信管理')" id="1316755744"/>
            <%} %>
        </a></span>短信管理</div>
       
        
        <div>
        <span class="checkbox"><a href="javascript:void(0)">
            <% 
              isUse = leaf.getLeafFromDb("862746277");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="senior" width="19" height="19" onClick="changeCheck('862746277','到期提醒')" id="862746277"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="senior" width="19" height="19" onClick="changeCheck('862746277','到期提醒')" id="862746277"/>
            <%} %>
        </a></span>到期提醒</div>
        <div>
        <span class="checkbox"><a href="javascript:void(0)">
            <% 
              isUse = leaf.getLeafFromDb("scheduler");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="senior" width="19" height="19" onClick="changeCheck('scheduler','调度中心')" id="scheduler"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="senior" width="19" height="19" onClick="changeCheck('scheduler','调度中心')" id="scheduler"/>
            <%} %>
        </a></span>调度中心</div>
        <div>
        <span class="checkbox"><a href="javascript:void(0)">
            <% 
              isUse = leaf.getLeafFromDb("1000700963");
            if (isUse == 1)
            {
            %>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png" class="senior" width="19" height="19" onClick="changeCheck('1000700963','印章管理')" id="1000700963"/>
            <%}
            else{%>
                <img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" class="senior" width="19" height="19" onClick="changeCheck('1000700963','印章管理')" id="1000700963"/>
            <%} %>
        </a></span>印章管理</div>
        </div>
      </div>  
</div>
  </div>
</div>
<%
	if("introduction".equals(flag)){
		%>
		<script>
    		jQuery(document).ready(function(){
			   	var tour = {
					id : "hopscotch",
					steps : [ {
						title : "提示",
						content : "如果要启用模块，请勾选此处",
						target : "radioButton",
						placement : "right",
						showNextButton : true,
						width : "120px",
						yOffset : -8,
						arrowOffset : -1
					},{
						title : "提示",
						content : "“全选”可以启用本列所有的功能模块",
						target : "intermediate",
						placement : "right",
						width : "170px",
						yOffset : -18,
						showNextButton : false,
					}],
					i18n : {
						nextBtn : "下一步"
					}
				};
				hopscotch.startTour(tour);
			});
		</script>
    		<%
	}
 %>

</body>
</html>
