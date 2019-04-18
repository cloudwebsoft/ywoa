<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="com.redmoon.oa.ui.SkinMgr"%>
<%@page import="com.redmoon.oa.worklog.WorkLogForModuleMgr"%>
<%@page import="com.redmoon.oa.flow.FormDb"%>
<%@page import="com.redmoon.oa.visual.FormDAO"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
    <title>工作月报</title>
    
    <script src="../inc/common.js"></script>
    <script src="../js/jquery.js" type="text/javascript"></script>
    <script src="../js/jquery-ui/jquery-ui.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/mywork/mywork_list.css" />
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
    <script src="js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath() %>/js/goToTop/goToTop.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath() %>/js/goToTop/goToTop.css" />
    
    <script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery.toaster.organize.js"></script>
    
    <script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.config.js"></script>
    <script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.all.js"> </script>
    <script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/lang/zh-cn/zh-cn.js"></script>
    <script src="../js/mywork/upload.js" type="text/javascript" charset="utf-8"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
    <script src="../js/ajaxfileupload.js" type="text/javascript"></script>
        <script src="<%=request.getContextPath() %>/mywork/js/c_worklog.js"></script>
  </head>
  <body id="dayBody">
  <%@ include file="mywork_inc_menu.jsp"%>
  <%
  	String code = ParamUtil.get(request,"code");
  	int prjId = ParamUtil.getInt(request,"id",0);
  	long flowId = 0;//yst
	if(!code.equals("")){
		FormDb fd = new FormDb(code);
		FormDAO fdao = new FormDAO(prjId,fd);
		flowId = fdao.getFlowId();
	}
  	String userName = privilege.getUser(request);
  	WorkLogForModuleMgr wlfm = new WorkLogForModuleMgr();
  	String managerName = wlfm.managerUserName(code,prjId);
  	// System.out.println(getClass() + " managerName=" + managerName);
  	if(managerName.equals("")){
  		managerName = userName;
  	}
   %>
  <script>
    o("menu3").className="current";
  </script>
  
    <div class="mywork-list-wrap">
<!--日报时间选择-->
    <div class="mywork-list-time">
      <ul>
    	<%if ("".equals(code)) {%>      
        <li class="mywork-list-time-span1" id="before" onclick="before()"></li>
        <li style="list-style:none;" class="mywork-list-time-span2" ><s:property value="dateCond"/></li>
        <li class="mywork-list-time-span3" id="after" onclick="after()"></li>
        <li class="mywork-list-time-span4" onclick="returnToday()">返回本月</li>
        <%} %>
      </ul>
      <form id="queryFrm" name="queryFrm" action="<%=request.getContextPath()%>/ymoa/queryMyMonthWork" method="post"  enctype="multipart/form-data">
          <div class="mywork-list-search">
	      	  <input type="hidden" name="code" value="<%=code %>" />
	      	  <input type="hidden" name="id" value="<%=prjId %>" />          
              <input type="text" name="contentCond" id="contentCond" title="按“Enter”执行查询" value="<s:property value="contentCond"/>" onkeypress="if (event.keyCode == 13) search();">      
              <img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-search.png" onclick="search()" class="searchImg"/>
          </div>
          <input type="hidden" name="dateCond" id="dateCond" value="<s:property value="dateCond"/>"/>
          <input type="hidden" name="dayLimit" id="dayLimit" value="<s:property value="dayLimit"/>"/>
          <input type="hidden" name="logType" id="logType"  value="<s:property value="logType"/>"/>
          <input type="hidden" name="curPage" id="curPage"  value="<s:property value="curPage"/>"/>
          <input type="hidden" name="beforeOrAfter" id="beforeOrAfter"  value="0"/>
          <input type="hidden" name="code" id="code"  value="<%=code %>"/>
          <input type="hidden" name="id" id="id"  value="<%=prjId %>"/>
      </form>
      <form id="initForm" name="initForm" action="<%=request.getContextPath()%>/ymoa/queryMyMonthWork" method="post"  enctype="multipart/form-data">
        <input type="hidden" name="logType"  value="<s:property value="logType"/>"/>
      </form>
       <input type="hidden" name="contextPath" id="contextPath"  value="<%=request.getContextPath()%>"/>
      <input type="hidden" name="skinPath" id="skinPath"  value="<%=SkinMgr.getSkinPath(request)%>"/>
      <input type="hidden" name="workLogId" id="workLogId"  value=""/>
      <input type="hidden" name="isPreparedTodys" id="isPreparedTodys"  value="<s:property value="isPreparedTodys"/>"/>
      <input type="hidden" name="lastBeginTime" id="lastBeginTime" value="<s:property value="lastBeginTime"/>"/>
    </div>
         
<!--发布日报-->
    <s:iterator var="wl" value="list" status="statu">
      <div class="mywork-list-master">
          <div  id="mywork_<s:property value="#wl.id"/>">
	          <div id="datearea_<s:property value="#wl.id"/>" class="mywork-list-master-p1"><s:property value="#wl.showTitle"/></div> 
	           <div class="mywork-list-master-tabb1" id="content_<s:property value='#wl.id'/>" ondblclick="changeThis('mywork_<s:property value="#wl.id"/>','<s:property value="#wl.id"/>','content_<s:property value='#wl.id'/>')"><s:property value="#wl.content" escape="false"/></div>
	          <s:if test="%{#wl.content != '暂未填写'}">
		          <div class="divComment">
		          		<div class="mywork-list-comment" id="imgReply_<s:property value='#wl.id'/>" title="评论" onclick="showReply('div_<s:property value='#wl.id'/>')"></div>
		            	<% 
		       			boolean isApraise = false;
		       			%>
		       		    <s:if test="%{#wl.workLogPraises.size > 0}">
				             <s:iterator var="wp" value="#wl.workLogPraises" >
				                <s:if test="%{#wp.name == #session.OA_NAME}">
			                		<% 
			       					isApraise = true;
			       					%>
				                </s:if>
				             </s:iterator>
		             	</s:if>
			        	<% if(isApraise){%>
			        		 <div class="mywork-list-praise mywork-list-cancel-praise-bg"  praiseCount="<s:property value="#wl.praiseCount"/>"  title="取消点赞" apraiseType ="0" id="<s:property value="#wl.id"/>"></div>
			        	<%}else{%>
			        		 <div class="mywork-list-praise mywork-list-praise-bg" praiseCount="<s:property value="#wl.praiseCount"/>"   id="<s:property value="#wl.id"/>" apraiseType ="1"   title="点赞"></div>
			        	<%} %>	
		          
		          </div>
              </s:if>
              <s:if test="%{#wl.workLogAttachs.size > 0}">
                  <div class="mywork-list-master-file" id="files_div_<s:property value='#wl.id'/>">
                      <s:iterator var="wa" value="#wl.workLogAttachs" status="statu2">
                          <div id="attach_<s:property value='#wa.id'/>">
                            <img src="<%=SkinMgr.getSkinPath(request)%>/images/message/inbox-adnexa.png" width="15" height="15"/> 
                            <a target="_blank" href="<%=request.getContextPath() %>/mywork/mywork_getfile.jsp?attachId=<s:property value="#wa.id"/>"><s:property value="#wa.name"/></a>
                            <a href="javascript:void(0);" name="attDel_<s:property value='#wl.id'/>" onclick="delAttach(<s:property value='#wa.id'/>,'<s:property value='#wa.id'/>')" style="display:none">  <img src="<%=request.getContextPath()%>/images/del.png" width="16" height="16"/></a>
                          </div>  
                      </s:iterator>
                  </div>
              </s:if>
         
          <div id="div_<s:property value='#wl.id'/>" style="display:none">
           <span class="mywork-list-master-p4">
               <textarea name="textarea2" rows="2" class="mywork-list-master-comment" id="textarea_<s:property value="#wl.id"/>"></textarea>
           </span>
           <div class="mywork-list-master-restore">
               <div class="mywork-list-master-restore-btn" onclick="replyWorkLog('div_<s:property value='#wl.id'/>',<s:property value="#wl.id"/>)">评论</div>
           </div>
          </div>
           </div>
          <div id="expands_<s:property value="#wl.id"/>">
              <div class="mywork-list-triangle"></div>
		      <div class="mywork-list-filleting" id="content_expands_<s:property value="#wl.id"/>">
		      	 <s:if test="%{#wl.workLogPraises != null}">
		      	  	  <p class ="p_praise_detail_<s:property value="#wl.id"/>">
		      	  	  	<img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon_praise_count.png" width="20" height="20"/>
		      	  	  	<span class="span_praisecount">赞(<s:property value="#wl.praiseCount"/>)</span>
		      	  	  	<s:set name="users" value="1" />
		      	  	  	 <s:iterator var="wp" value="#wl.workLogPraises" >
		      	  	  	 	<s:if test="%{#users == 1}">
		      	  	  	 		<s:set name="users" value="#wp.userName" />
		      	  	  	 	</s:if>
		      	  	  	 	<s:else>
		      	  	  	 		<s:set name="users" value="#users+','+#wp.userName" />
		      	  	  	 	</s:else>
		      	  	  	 </s:iterator>
		      	  	  	 <span class="span_praiseusers"><s:property value="#users"/></span>
		      	  	  	 
		      	  	  </p>	
		      	  </s:if>	 
		      	  <div class ="div_review" >
		      	  		 <s:if test="%{#wl.workLogExpands != null}">
					        <p><span><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-1.png" width="20" height="20"/>&nbsp;全部评论&nbsp;(<span id="reviewCount_<s:property value="#wl.id"/>"><s:property value="#wl.reviewCount"/></span>)</span></p>
					        <div id="newExpands_<s:property value="#wl.id"/>"></div>
					        <s:iterator var="we" value="#wl.workLogExpands" status="statu3">
					             <div id="review_<s:property value="#we.id"/>"><label class="reviewNameLabel"><s:property value="#we.userName"/></label>：<s:property value="#we.review"/></div>
					             <div class="mywork-list-filleting-r"><s:property value="#we.reviewTime"/>
				                     <div class="mywork-list-filleting-restore" onclick="showQuoteReply('<s:property value='#we.id'/>','review_<s:property value="#we.id"/>')"><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-3.png" title="回复" width="20" height="20"></div>
				                 </div>
					             <div id="reply_<s:property value='#we.id'/>" style="display:none">
			                        <textarea class="mywork-list-restore-box" id="quote_txt_<s:property value="#we.id"/>"></textarea>
			                        <div class="mywork-list-box-f">
			                          <div class="mywork-list-box-f-1" onclick="reply('<s:property value='#we.id'/>',<s:property value="#wl.id"/>)">发布</div>
			                        </div>
			                     </div>
					        </s:iterator>
					      </s:if>
					      <s:else>
					             	暂无评论
					       </s:else>
		      	  </div>
		         
	         </div>
        </div>
        </div>
        
    </s:iterator>
    <div id="moreInfos" style="dispose:none">
        
    </div>
    </div>
    <%if ("".equals(code)) {%>    
    <div class="mywork-list-more" onclick="showMore()">————&nbsp;&nbsp;查看更多记录&nbsp;&nbsp;———— </div>
    <%} %>
    <input type="hidden" value="<s:property value='dayLimit'/>"/>
    <input type="hidden" value="0" id="saveOrCreate"/>
    <input type="hidden" value="<s:property value='isLast'/>" id="isLast"/>
    <input type="hidden" id="tempContent" value=""/>
    <input type="hidden" id="editState" value=""/>
</body>
  <script type="text/javascript">
  var uEditor = null;
//加载更多日志
function showMore(){
    var lastBeginTime = $("#lastBeginTime").val();
    var contentCond = $("#contentCond").val();
    var dateCond = "";
    if (contentCond != ""){
        $("#beforeOrAfter").val('3');
        dateCond = $("#dateCond").val();
    }else{
        $("#beforeOrAfter").val('0');
        dateCond = lastBeginTime;
    }
    $("#dayBody").showLoading();
    $.ajax({
        type: "post",
        url: '<%=request.getContextPath()%>/ymoa/queryMoreMyWork',
        data: {
             logType: $("#logType").val(),
             curPage: $("#curPage").val(),
             contentCond:contentCond,
             beforeOrAfter:$("#beforeOrAfter").val(),
             lastBeginTime: lastBeginTime,
             dateCond : $("#dateCond").val(),
             code:"<%=code%>",
             id:<%=prjId%>
        },
        dataType: "html",
        beforeSend: function(XMLHttpRequest){
        },
        success: function(data, status){
            data = $.parseJSON(data);
            if( data.message.indexOf("成功") == -1){
                jAlert(data.message,"提示");
                return false;
            }
            //加载查询内容
            for (var i = 0; i < data.list.length; i++){
                var logId = data.list[i].id;
                var inhtml = '<div class="mywork-list-master">'
                             + '<div  id="mywork_'+ logId +'">'
                             + '<div class="mywork-list-master-p1" id="datearea_'+logId +'">' + data.list[i].showTitle + '</div> '
                             + '<div class="mywork-list-master-tabb1" id="content_' + logId + '" >'
                             + data.list[i].content
                             + '</div>';
                 
                 
               if(data.list[i].content != "暂未填写"){
                 inhtml = inhtml +'<div class="divComment">'
                           +  '<div class="mywork-list-comment" title="评论" id="showReplyBtn_' + logId + '"></div>';
                 var p_res = praiseStatus('<%=privilege.getUser(request)%>',data.list[0].workLogPraises);
                 if(p_res){
                	 	inhtml += '<div class="mywork-list-praise mywork-list-cancel-praise-bg"  praiseCount="'+data.list[i].praiseCount+'"  title="取消点赞" apraiseType ="0" id="'+logId+'"></div>';
                  }else{
  					inhtml += '<div class="mywork-list-praise mywork-list-praise-bg" praiseCount="'+data.list[i].praiseCount+'"  id="'+logId+'" apraiseType ="1"   title="点赞"></div>';
					
                  }
                 inhtml += '</div>';
               }
               if(data.list[i].workLogAttachs !=null && data.list[i].workLogAttachs.length > 0){
                  inhtml = inhtml + '<div class="mywork-list-master-file" id="files_div_' + logId + '">';
                  for(var j =0 ; j < data.list[i].workLogAttachs.length; j++){
                      var wa = data.list[i].workLogAttachs[j];
                      var waId = wa.id;
                      inhtml =inhtml + '<div id="attach_' + logId + '">'
                              + '<img src="<%=SkinMgr.getSkinPath(request)%>/images/message/inbox-adnexa.png" width="15" height="15"/> '
                              + '<a target="_blank" href="<%=request.getContextPath() %>/mywork/mywork_getfile.jsp?attachId=' + waId + '">' + data.list[i].workLogAttachs[j].name + '</a>'
                              + '<a href="javascript:void(0);" id="del_' + waId + '" name="attDel_' + logId + '" style="display:none">  <img src="<%=request.getContextPath()%>/images/del.png" width="16" height="16"/></a>'
                              + '</div> ';
                      
                  }
                  inhtml = inhtml + '</div>';
               }
               inhtml = inhtml + ' </div>'
                        + '<div id="div_' + logId+ '" style="display:none">'
                        + '<span class="mywork-list-master-p4">'
                        + '<textarea name="textarea2" rows="2" class="mywork-list-master-comment" id="textarea_' + logId + '"></textarea>'
                        + '</span>'
                        + '<div class="mywork-list-master-restore">'
                        + '<div class="mywork-list-master-restore-btn" id="replyBtn_' + logId + '">评论</div>'
                        + '</div>'
                        + '</div>'
                        + '<div id="expands_' + logId + '">'
                        + '<div class="mywork-list-triangle"></div>'
                        + '<div class="mywork-list-filleting " id="content_expands_'+logId+'">';
				
				if (data.list[i].workLogPraises != null && data.list[i].workLogPraises.length > 0){

					var p_name = praiseUsers(data.list[i].workLogPraises);
					 inhtml += '<p class ="p_praise_detail_'+logId+'">';
                     inhtml += '<img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon_praise_count.png" width="20" height="20"/>';
					 inhtml += '<span class="span_praisecount">赞('+data.list[i].praiseCount+')</span>';
					 inhtml += ' <span class="span_praiseusers">'+p_name+'</span>';
					 inhtml += '</p>'
				}
               
				   inhtml += '<div class = "div_review">'
              if (data.list[i].workLogExpands != null && data.list[i].workLogExpands.length > 0){
                  inhtml = inhtml + ' <p><span><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-1.png" width="20" height="20"/>&nbsp;全部评论&nbsp;'
                           + '(<span id="reviewCount_' + logId + '">' + data.list[i].reviewCount + '</span>)</span></p>'
                           + '<div id="newExpands_' + logId + '"></div>';
                 
                  for (var k = 0; k < data.list[i].workLogExpands.length; k++){
                    var we = data.list[i].workLogExpands[k];
                    inhtml = inhtml + '<div id="review_'+we.id+'"><label class="reviewNameLabel">'+ we.userName + '</label>：' + we.review + '</div>'
                             + '<div class="mywork-list-filleting-r">' + we.reviewTime
                             + '<div class="mywork-list-filleting-restore" id="quoteReplyImg_' + we.id + '"><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-3.png" title="回复" width="20" height="20">&nbsp;&nbsp;<a href="javascript:void(0);"></a></div>'
                             + '</div>'
                             + '<div id="reply_' + we.id + '" style="display:none">'
                             + '<textarea class="mywork-list-restore-box" id="quote_txt_' + we.id +'"></textarea>'
                             + '<div class="mywork-list-box-f">'
                             + '<div class="mywork-list-box-f-1" id="quoteReplyBtn_' + we.id + '">发布</div>'
                             + '</div>'
                             + '</div>';
                    
                  }
              
              }else{
                inhtml = inhtml + '暂无评论';
              }     
				   inhtml += "</div>";     
              inhtml = inhtml + '</div>'
                       + '</div>'
                       + '</div>';
              $("#moreInfos").before(inhtml);
              $("#content_"+logId).bind("dblclick",{'id':logId, 'content':data.list[i].content},function(v){
                    changeThis('mywork_' + v.data["id"], v.data["id"], "content_"+v.data["id"]);
                 });
              if(data.list[i].content != "暂未填写"){
                   $("#showReplyBtn_"+logId).bind("click",{'id':logId},function(v){
                        showReply("div_" + v.data["id"]);
                   });
               }
               $("#replyBtn_" + logId).bind("click", {'id':logId}, function(v){
                    replyWorkLog("div_" + v.data["id"] ,v.data["id"]);
               });
                $("#addPraise_" + logId).bind("click",{'id':logId},function(v){
                        addPraiseCount(v.data["id"]);
                  });  
              if(data.list[i].workLogAttachs !=null && data.list[i].workLogAttachs.length > 0){
                     for(var j =0 ; j < data.list[i].workLogAttachs.length; j++){
                        var wa = data.list[i].workLogAttachs[j];
                        var waId = wa.id;
                        $("#del_" + waId).bind("click",{'id':waId}, function(v){
                            delAttach(v.data["id"] ,v.data["id"]);
                        });
                     }
                
              }
             
              
              if (data.list[i].workLogExpands != null && data.list[i].workLogExpands.length > 0){
                     for (var k = 0; k < data.list[i].workLogExpands.length; k++){
                        var we = data.list[i].workLogExpands[k];
                        $("#quoteReplyImg_" + we.id).bind("click",{'id':we.id} ,function(v){
                            showQuoteReply(v.data["id"] , "review_"+v.data['id']);
                         });
                        $("#quoteReplyBtn_" + we.id).bind("click",{'id':logId, 'weId':we.id}, function(v){
                          reply(v.data["weId"], v.data["id"]);
                        });  
                     }
              }
                    
            }
             $("#lastBeginTime").val(data.lastBeginTime);
             $("#curPage").val(data.curPage);
        },
        complete: function(XMLHttpRequest, status){
          $("#dayBody").hideLoading();    
        },
        error: function(XMLHttpRequest, textStatus){
            // 请求出错处理
            jAlert(XMLHttpRequest.responseText,'提示');
        }
    });
    
}
//回复框显示控制
function showReply(id){
    $('#'+id).toggle();
    if($('#'+id).is(':visible')){
       $("#textarea_"+id.split("_")[1]).val("");
       $("#textarea_"+id.split("_")[1]).focus();
    }
}

//编辑日志
function changeThis(index, id, contentId){
	
	//alert(index+","+id+","+contentId);
		<%
		if(!managerName.equals(userName)){
		%>
			jAlert("您只有查看权限","提示");
			return;
		<%}
			if(flowId!=0 && !wlfm.isCanReport(managerName, flowId)) {//yst
	%>
		jAlert("请先复命再汇报","提示");
		return;
		<%}
	%>
      var content = $("#" + contentId).html();
      if(uEditor != null){
        jAlert("不能同时编辑多条记录！","提示");
        return false;
      }

      var flag = true;
      $("#dayBody").showLoading();
	      $.ajax({
	           type: "post",
	           url: '<%=request.getContextPath()%>/ymoa/checkCando',
	           async: false,
	           data: {
	        	   logType: '2',
	               workLogId: id,
	               dateArea: $("#datearea_" + id).html()
	           },
	           dataType: "json",
	           beforeSend: function(XMLHttpRequest){
	           },
	           success: function(data, status){
	        	   if( data.message.indexOf("成功") == -1){
	                   jAlert(data.message,"提示");
	                   flag = false;
	               }
	           },
	           complete: function(XMLHttpRequest, status){
	             $("#dayBody").hideLoading();    
	           },
	           error: function(XMLHttpRequest, textStatus){
	               // 请求出错处理
	               jAlert(XMLHttpRequest.responseText,'提示');
	           }
	      });

	  if (!flag) {
		return;
	  }
      
      $("#tempContent").val($("#"+index).html());
      //删除原评论输入隐藏内容
      $("#div_"+ id).hide();
      $("#div_"+ id).empty();
      $("#div_"+ id).remove();
      var filesHtml = $("#files_div_"+id).html();
      $("#"+index).empty();
      var editDiv = '<div class="mywork-list-master" id="editDiv">';
      editDiv = editDiv + '<div id="editArear">';
      if (content != "暂未填写"){
        editDiv = editDiv + ' <div id="myEditor" class="editUeditorBorde"></div>';
      }else{
        editDiv = editDiv + ' <div id="myEditor" ></div>';
      }
      editDiv = editDiv + '  <div class="mywork-list-master-p3">';
      editDiv = editDiv + '  <div>';
      //editDiv = editDiv + '  <script>initUpload()</script'+'>';
      editDiv = editDiv + '<div class="upload" id="upfileDiv"><span style="float:left;margin-top:-3px;">添加文件</span><a id="upfilePanelHidden" href="javascript:void(0);"></a></div>';
      editDiv = editDiv + '<div id="upfilePanelShow" onload="addUploadFiles" style="margin-top:5px"></div>';
   
      editDiv = editDiv + '  </div>';
      editDiv = editDiv + ' <div class="mywork-list-master-cancel" id="cancelBtn">取消</div>';
      editDiv = editDiv + ' <div class="mywork-list-master-release" onclick="submitContent()">发布</div>';
      editDiv = editDiv + '</div>';
      editDiv = editDiv + '</div>';
      editDiv = editDiv + ' </div>';
      if (filesHtml != null && filesHtml != ""){
          editDiv = editDiv + '<div class="mywork-list-master-file" id="files_div_' + id + '">';
          editDiv = editDiv + filesHtml;
          editDiv = editDiv + '</div>';
      }
      $("#"+index).html(editDiv);
      $("#cancelBtn").bind("click",function(){
            cancel(index);
      });
      //初始化上传按钮
       upfileCount = 0;
      addUploadFiles();
      //初始化编辑器
      
      uEditor = UE.getEditor('myEditor',{
          initialContent : "",//初始化编辑器的内容  
          //allowDivTransToP: false,//阻止转换div 为p
          toolleipi:true,//是否显示，设计器的 toolbars
          textarea: 'content',
          enableAutoSave: false,  
          //选择自己需要的工具按钮名称,此处仅选择如下五个
          toolbars:[[
          'fullscreen','undo', 'redo', '|',
                     'bold', 'italic', 'underline','|','forecolor',
                     'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                     'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                     'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify'
          ]],
          //focus时自动清空初始化时的内容
          //autoClearinitialContent:true,
          //关闭字数统计
          wordCount:false,
          //关闭elementPath
          elementPathEnabled:false,
          //默认的编辑区域高度
          initialFrameHeight:100,
          initialFrameWidth:1056
          ///,iframeCssUrl:"css/bootstrap/css/bootstrap.css" //引入自身 css使编辑器兼容你网站css
          //更多其他参数，请参考ueditor.config.js中的配置项
      });
      
      $("#saveOrCreate").val('1');
      
      $("#workLogId").val(id);
      $("a[name='attDel_" + id+"']").show();
      uEditor.ready(function(){
        if(content == ""){
               uEditor.setContent("");
           }else{
               if (content != "暂未填写"){
	             uEditor.setContent(content);
	           }else{
	             uEditor.setContent("");
	           }
           }
           uEditor.focus(true); 
    });
      $("#dayBody").hideLoading();
      
      
  }
//取消编辑
function cancel(id){
    if($("#editState").val()=="1"){
        jAlert("附件有变动，不允许取消！","提示");
        return false;
    }
    if (uEditor != null){
       uEditor.ready(function(){
           uEditor.destroy();
           
        });
    }
    $("#" + id).empty();
    $("#" + id).html($("#tempContent").val());
    uEditor = null;
    var tempId = id.split("_")[1];
    $("#content_" + tempId).unbind("dblclick");
    $("#content_"+tempId).removeAttr("ondblclick");
    $("#content_" + tempId).bind("dblclick",function(){
           changeThis("mywork_" + tempId,tempId,"content_"+tempId);
   });
   //回复按钮绑定事件
   $("#imgReply_"+tempId).unbind("click");
   $("#imgReply_"+tempId).removeAttr("onclick");
   $("#imgReply_"+tempId).bind("click",function(){
       showReply("div_"+ tempId);
   });
}
//保存日志
function submitContent(){
		var prjId = "<%=prjId%>";
        var work_log = 0;
        $("#editState").val("");
        var content = uEditor.getContent().replace(/\"/g,"'");
        if (content == ""){
            jAlert("请输入内容！", "提示");
            return false;
        }
        if ($("#saveOrCreate").val()!="0"){//修改
            if (upfileCount == 1){//无附件，ajax提交
                $("#dayBody").showLoading();
                $.ajax({
                    type: "post",
                    url: '<%=request.getContextPath()%>/ymoa/saveMyWork',
                    data: {
                         content: encodeURIComponent(content), 
                         logType: '2',
                         dateCond: $("#dateCond").val(),
                         
						 code: '<%=code%>',
						 id: <%=prjId%>,
						                          
                         workLogId: $("#workLogId").val()
                    },
                    dataType: "html",
                    beforeSend: function(XMLHttpRequest){
                    }, 
                    success: function(data, status){
                        data = $.parseJSON(data);
                        work_log = data.workLog.id;
                        if (data.message.indexOf("含有非法类型文件") == -1){
                           if( data.message.indexOf("成功") == -1){
                               jAlert(data.message,"提示");
                               return false;
                           } else {
                           		$.toaster({priority : 'info', message : '发布成功' });
                           }
                        }else{
                           jAlert(data.message,"提示");
                        }
                        var inHtml = '<div class="mywork-list-master-p1"  id="datearea_'+data.workLog.id +'">'+ data.workLog.showTitle +'</div> ';
                        inHtml = inHtml + ' <div class="mywork-list-master-tabb1" id="content_'+ data.workLog.id +'">';
                        inHtml = inHtml + HTMLDecode(data.workLog.content);
                        inHtml = inHtml + '</div>';
                        inHtml = inHtml + '<div class="divComment">';
                        inHtml = inHtml + '<div class="mywork-list-comment" title="评论" id="imgReply_' + data.workLog.id + '"></div>';
                        var p_res = praiseStatus('<%=privilege.getUser(request)%>',data.workLog.workLogPraises);
                        if(p_res){
                       	 	inHtml += '<div class="mywork-list-praise mywork-list-cancel-praise-bg"  praiseCount="'+data.workLog.praiseCount+'"  title="取消点赞" apraiseType ="0" id="'+data.workLog.id +'"></div>';
                         }else{
         					inHtml += '<div class="mywork-list-praise mywork-list-praise-bg" praiseCount="'+data.workLog.praiseCount+'"  id="'+data.workLog.id+'" apraiseType ="1"   title="点赞"></div>';
       					
                         }

                        
                        inHtml = inHtml + '</div>';
                        if(data.workLog.workLogAttachs != null){
                            inHtml = inHtml + '<div class="mywork-list-master-file" id="files_div_' + data.workLog.id + '">';
                            for(var j = 0; j<data.workLog.workLogAttachs.length; j++){
                             var attId = data.workLog.workLogAttachs[j].id;
                             inHtml = inHtml + '<div id="attach_' + attId  + '">';
                             inHtml = inHtml + '<img src="<%=SkinMgr.getSkinPath(request)%>/images/message/inbox-adnexa.png" width="15" height="15"/>  '; 
                             inHtml = inHtml + '<a target="_blank" href="<%=request.getContextPath() %>/mywork/mywork_getfile.jsp?attachId=' + attId + '">'+ data.workLog.workLogAttachs[j].name +'</a>';
                             inHtml = inHtml + '<a href="javascript:void(0);"  id="attDel_' + attId  + '" name="attDel_' + attId + '" style="display:none" onclick="delAttach('+attId+','+attId+')">  <img src="<%=request.getContextPath()%>/images/del.png" width="16" height="16"/></a></br>';
                             inHtml = inHtml + '</div>';
                            }
                            inHtml = inHtml + '</div>';
                        }
                        inHtml = inHtml + '<div id="div_'+ data.workLog.id +'" style="display:none">';
                        inHtml = inHtml + '<span class="mywork-list-master-p4">';
                        inHtml = inHtml + '<textarea name="textarea2" rows="2" class="mywork-list-master-comment" id="textarea_'+data.workLog.id+'"></textarea>';
                        inHtml = inHtml + '</span>';
                        inHtml = inHtml + '<div class="mywork-list-master-restore">';
                        inHtml = inHtml + '<div class="mywork-list-master-restore-btn" id="addReply_'+data.workLog.id+'">评论</div>';
                        inHtml = inHtml + '</div>';
                        inHtml = inHtml + '</div>';
                        if($("#mywork_" + data.workLog.id).length > 0){
                            $("#mywork_" + data.workLog.id).empty();
                            $("#mywork_" + data.workLog.id).html(inHtml);
                        }else{
                             $("#mywork_" + $("#workLogId").val()).empty();
                             $("#mywork_" + $("#workLogId").val()).attr("id", "mywork_" + data.workLog.id);
                             $("#div_" + $("#workLogId").val()).attr("id", "div_" + data.workLog.id);
                             $("#expands_" + $("#workLogId").val()).attr("id", "expands_" + data.workLog.id);
                             $("#content_expands_" + $("#workLogId").val()).attr("id", "content_expands_" + data.workLog.id);
                             $("#mywork_" + data.workLog.id).html(inHtml);
                        }
                        //绑定双击事件
                        $("#content_"+data.workLog.id).bind("dblclick",function(){
                                changeThis("mywork_" + data.workLog.id,data.workLog.id, "content_" + data.workLog.id);
                        });
                        //绑定评论事件
                        $("#addReply_"+data.workLog.id).bind("click",function(){
                                replyWorkLog("div_" + data.workLog.id , data.workLog.id)
                        });
                        //回复按钮绑定事件
                        $("#imgReply_"+data.workLog.id).bind("click",function(){
                            showReply("div_"+ data.workLog.id);
                        });
                        //隐藏删除
                        $("a[name='attDel_" + data.workLog.id+"']").hide();
                        //设置为新增状态
                        $("#saveOrCreate").val('0');
                        uEditor.destroy();
                        uEditor = null;
                    },
                    complete: function(XMLHttpRequest, status){
                      $("#dayBody").hideLoading();
                    },
                    error: function(XMLHttpRequest, textStatus){
                        // 请求出错处理
                        jAlert(XMLHttpRequest.responseText,"提示");
                    }
                }); 
           }else if(upfileCount > 1){//有附件
                var fileAtts = new Array();
                for(var i = 1; i< upfileCount; i++ ){
                   //fileAtts = fileAtts + "'f" + i + "'," ;
                   fileAtts[i - 1] = "f" + i;
                }
                $("#dayBody").showLoading();
                $.ajaxFileUpload({
                        url:'<%=request.getContextPath()%>/ymoa/saveMyWork',  //用于文件上传的服务器端请求地址
                        secureuri:false,//一般设置为false
                        fileElementId: fileAtts,//文件上传空间的id属性  <input type="file" id="file" name="file" />
                        dataType: 'json',//返回值类型 一般设置为json
                        data: { 
                            content: encodeURIComponent(content), 
                            logType: '2',
                            dateCond: $("#dateCond").val(),
                            
							code: '<%=code%>',
						 	id: <%=prjId%>,
						                             
                            workLogId: $("#workLogId").val()
                        },  
                        success:function(data,status){
                            if (data.message.indexOf("含有非法类型文件") == -1){
	                           if( data.message.indexOf("成功") == -1){
	                               jAlert(data.message,"提示");
	                               return false;
	                           } else {
	                            	$.toaster({priority : 'info', message : '发布成功' });
	                            }
	                        }else{
	                           jAlert(data.message,"提示");
	                        }
                             var inHtml = '<div class="mywork-list-master-p1  id="datearea_'+data.workLog.id +'">'+ data.workLog.showTitle +'</div> ';
                             inHtml = inHtml + ' <div class="mywork-list-master-tabb1" id="content_'+ data.workLog.id +'">';
                             inHtml = inHtml + HTMLDecode(data.workLog.content);
                             inHtml = inHtml + '</div>';
                             inHtml = inHtml + '<div class="divComment">';
                             inHtml = inHtml + '<div class="mywork-list-comment" title="评论" id="imgReply_' + data.workLog.id + '"></div>';
                             var p_res = praiseStatus('<%=privilege.getUser(request)%>',data.workLog.workLogPraises);
                             if(p_res){
                            	 	inHtml += '<div class="mywork-list-praise mywork-list-cancel-praise-bg"  praiseCount="'+data.workLog.praiseCount+'"  title="取消点赞" apraiseType ="0" id="'+data.workLog.id +'"></div>';
                              }else{
              					inHtml += '<div class="mywork-list-praise mywork-list-praise-bg" praiseCount="'+data.workLog.praiseCount+'"  id="'+data.workLog.id+'" apraiseType ="1"   title="点赞"></div>';
            					
                              }
                                                      
                             inHtml = inHtml + '</div>';
                             inHtml = inHtml + '<div class="mywork-list-master-file" id="files_div_' + data.workLog.id + '">';
                             if(data.workLog.workLogAttachs != null){
                                 for(var j = 0; j<data.workLog.workLogAttachs.length; j++){
                                      var attId = data.workLog.workLogAttachs[j].id;
                                      inHtml = inHtml + '<div id="attach_' + attId  + '">';
                                      inHtml = inHtml + '<img src="<%=SkinMgr.getSkinPath(request)%>/images/message/inbox-adnexa.png" width="15" height="15"/>  '; 
                                      inHtml = inHtml + '<a target="_blank" href="<%=request.getContextPath() %>/mywork/mywork_getfile.jsp?attachId=' + attId + '">'+ data.workLog.workLogAttachs[j].name +'</a>';
                                      inHtml = inHtml + '<a href="javascript:void(0);"  id="attDel_' + attId  + '" name="attDel_' + data.workLog.id + '" style="display:none" onclick="delAttach('+attId+','+attId+')">  <img src="<%=request.getContextPath()%>/images/del.png" width="16" height="16"/></a></br>';
                                      inHtml = inHtml + '</div>';
                                 }
                             }
                             inHtml = inHtml + '</div>';
                             inHtml = inHtml + '<div id="div_' + data.workLog.id + '" style="display:none" >';
                             inHtml = inHtml + '<span class="mywork-list-master-p4">';
                             inHtml = inHtml + '<textarea name="textarea2" rows="2" class="mywork-list-master-comment" id="textarea_'+ data.workLog.id +'"></textarea>';
                             inHtml = inHtml + '</span>';
                             inHtml = inHtml + '<div class="mywork-list-master-restore">';
                             inHtml = inHtml + '<div class="mywork-list-master-restore-btn" id="addReply_'+data.workLog.id+'">评论</div>';
                             inHtml = inHtml + '</div>';
                             inHtml = inHtml + '</div>';
                             if($("#mywork_" + data.workLog.id).length > 0){
                                $("#mywork_" + data.workLog.id).empty();
                                $("#mywork_" + data.workLog.id).html(inHtml);
                            }else{
                                 $("#mywork_" + $("#workLogId").val()).empty();
                                 $("#mywork_" + $("#workLogId").val()).attr("id", "mywork_" + data.workLog.id);
                                 $("#div_" + $("#workLogId").val()).attr("id", "div_" + data.workLog.id);
                                 $("#expands_" + $("#workLogId").val()).attr("id", "expands_" + data.workLog.id);
                                 $("#content_expands_" + $("#workLogId").val()).attr("id",  "content_expands_" + data.workLog.id);
                                 $("#mywork_" + data.workLog.id).html(inHtml);
                            }
                              //绑定双击事件
                             $("#content_"+data.workLog.id).bind("dblclick",function(){
                                changeThis("mywork_" + data.workLog.id,data.workLog.id, "content_" + data.workLog.id);
                             });
                             //绑定评论事件
                            $("#addReply_"+data.workLog.id).bind("click",function(){
                                    replyWorkLog("div_" + data.workLog.id , data.workLog.id)
                            });
                             //回复按钮绑定事件
                            $("#imgReply_" + data.workLog.id).bind("click",function(){
                                showReply("div_" + data.workLog.id);
                            });
                            //隐藏删除
                            $("a[name='attDel_" + data.workLog.id+"']").hide();
                            //设置为新增状态
                            $("#saveOrCreate").val('0');
                            uEditor.destroy();
                            uEditor = null;
                        },
                        complete: function(XMLHttpRequest, status){
                            $("#dayBody").hideLoading();
                        },
                        error: function(XMLHttpRequest, textStatus){
                            // 请求出错处理
                            jAlert(XMLHttpRequest.responseText,"提示");
                        }
                });
           }
       }
       
    }
//HTML反转义
function HTMLDecode(text){
        var temp = document.createElement("div");
        temp.innerHTML = text;
        var output = temp.innerText || temp.textContent;
        temp = null;
        return output;
    }
//前一周
function before(){
       $("#after").removeClass("mywork-list-time-span3");
       $("#after").addClass("mywork-list-time-span5");
       $("#beforeOrAfter").val('0');
       queryFrm.submit();
    }
//后一周
function after(){
     if ($("#isLast").val() != "true"){
          $("#beforeOrAfter").val('1');
          queryFrm.submit();
     }
}
//返回本周
function returnToday(){
       initForm.submit();
}
//删除附件
function delAttach(attachId, imgId){
    $("#dayBody").showLoading();
    $.ajax({
         type: "post",
         url: '<%=request.getContextPath()%>/ymoa/delAttach',
         data: {
              attachId:attachId 
         },
         dataType: "json",
         beforeSend: function(XMLHttpRequest){
         },
         success: function(data, status){
             if(data.message=="删除成功！"){
                $("#editState").val("1"); //删除成功
                 $("#attach_"+imgId).empty();
                 $("#attach_"+imgId).remove();
             }else{
                 jAlert(data.message,"提示");
             }
         },
         complete: function(XMLHttpRequest, status){
           $("#dayBody").hideLoading();    
         },
         error: function(XMLHttpRequest, textStatus){
             // 请求出错处理
             jAlert(XMLHttpRequest.responseText,'提示');
         }
    });
}
//根据内容搜索
function search(){
    var contentCond = $("#contentCond").val();
    $("#curPage").val("0");
    if(contentCond != ""){
     $("#beforeOrAfter").val("2");
     queryFrm.submit();
    }else{
       $("#beforeOrAfter").val("0");
       $("#dateCond").val("");
       queryFrm.submit();
    }
}

//评论
function replyWorkLog(index,id){
        var reviewContent = $("#textarea_"+id).val();
        if ($.trim(reviewContent).length == 0){
            jAlert("评论内容不能为空！","提示");
            return false;
        }
        if (reviewContent.length > 200){
            jAlert("评论字数超过200，请修改！","提示");
            return false;
        }
        var reviewCount = 0;
        $.ajax({
             type: "post",
             url: '<%=request.getContextPath()%>/ymoa/saveReviewExpands',
             data: {
                  reviewContent: encodeURIComponent(reviewContent), 
                  workLogId:id
             },
             dataType: "html",
             beforeSend: function(XMLHttpRequest){
             },
             success: function(data, status){
                 data = $.parseJSON(data);
                 if( data.message.indexOf("成功") == -1){
                   jAlert(data.message,"提示");
                   return false;
                 }
                 var exId = data.reWle.id;
                 if($("#newExpands_"+id).length > 0){//存在评论
                    var newExpands = $("#newExpands_"+id).html();
                    var inHtml = '<div id="review_'+exId+'"><label class="reviewNameLabel">' + data.reWle.userName + '：</label>' + data.reWle.review + '</div>';
                    inHtml = inHtml +'<div class="mywork-list-filleting-r">' + data.reWle.reviewTime;
                    inHtml = inHtml +'<div class="mywork-list-filleting-restore" id="quoteReply_' + exId + '"><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-3.png" title="回复" width="20" height="20">&nbsp;&nbsp;<a href="javascript:void(0)"></a></div>';
                    inHtml = inHtml +'</div>';
                    inHtml = inHtml +'<div id="reply_' + exId + '" style="display:none">';
                    inHtml = inHtml +'<textarea class="mywork-list-restore-box" id="quote_txt_' + exId + '"></textarea>';
                    inHtml = inHtml +'<div class="mywork-list-box-f">';
                    inHtml = inHtml +'<div class="mywork-list-box-f-1" id="release_' + exId + '">发布</div>'
                    inHtml = inHtml +'</div>'
                    inHtml = inHtml +'</div>';
                    inHtml = inHtml + newExpands;
                    $("#newExpands_"+id).html(inHtml);
                    $("#release_"+ exId).bind("click",function(){
                            reply(exId , id);
                        });
                    $("#quoteReply_"+ exId).bind("click",function(){
                            showQuoteReply(exId , 'review_' + data.reWle.id);
                        });
                    reviewCount = parseInt($("#reviewCount_" + id).html()) + 1;
                    $("#reviewCount_" + id).html(reviewCount);
                 }else{//不存在评论
                		 var inHtml = '<div class="div_review">'
                        inHtml = inHtml + '<p><span><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-1.png" width="20" height="20"/>&nbsp;全部评论&nbsp;(<span id="reviewCount_' + id + '">1</span>)</span>&nbsp;&nbsp;</span></p>';
                        inHtml = inHtml + '<div id="newExpands_'+id+'"></div>';
                        inHtml = inHtml + '<div id="review_'+exId+'"><label class="reviewNameLabel">' + data.reWle.userName + '：</label>' + data.reWle.review + '</div>';
                        inHtml = inHtml + '<div class="mywork-list-filleting-r">'+data.reWle.reviewTime;
                        inHtml = inHtml + '<div class="mywork-list-filleting-restore" id="quoteReply_' + exId + '"><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-3.png" title="回复" width="20" height="20">&nbsp;&nbsp;<a href="javascript:void(0)"></a></div>';
                        inHtml = inHtml + '</div>';
                        inHtml = inHtml + '<div id="reply_' + exId + '" style="display:none">';
                        inHtml = inHtml + '<textarea class="mywork-list-restore-box" id="quote_txt_' + exId + '"></textarea>';
                        inHtml = inHtml + '<div class="mywork-list-box-f">';
                        inHtml = inHtml + '<div class="mywork-list-box-f-1" id="release_' + exId + '">发布</div>';
                        inHtml = inHtml + '</div>';
                        inHtml = inHtml + '</div>';
                        inHtml = inHtml + '</div>';
                   		$("#content_expands_"+id).find(".div_review").remove();
                        $("#content_expands_"+id).append(inHtml);
                        $("#release_"+ exId).bind("click",function(){
                            reply(exId , id);
                        });
                        $("#quoteReply_"+ exId).bind("click",function(){
                            showQuoteReply(exId , "review_"+exId);
                        });
                 }
                 
                 $("#" + index).hide();
             },
             complete: function(XMLHttpRequest, status){
             },
             error: function(XMLHttpRequest, textStatus){
                 // 请求出错处理
                jAlert(XMLHttpRequest.responseText,'提示');
             }
         });
    }
//引用评论
function showQuoteReply(id, contentId){
    var content = $("#" + contentId).text();
    $("#reply_" + id).toggle();
    $("#quote_txt_" + id).html("//@" + content);
    $("#quote_txt_" + id).focus();
}
//引用评论回复
function reply(id,workLogId){
    var content = $("#quote_txt_" + id).val();
    if ($.trim(content).length == 0){
            jAlert("评论内容不能为空！","提示");
            return false;
        }
    if (content.length > 200){
            jAlert("评论字数超过200，请修改！","提示");
            return false;
        }
    var reviewCount = parseInt($("#reviewCount_" + workLogId).html()) + 1;
    $.ajax({
             type: "post",
             url: '<%=request.getContextPath()%>/ymoa/saveReviewExpands',
             data: {
                  reviewContent: encodeURIComponent(content), 
                  workLogId:workLogId
             },
             dataType: "html",
             beforeSend: function(XMLHttpRequest){
             },
             success: function(data, status){
                 data = $.parseJSON(data);
                 var exId = data.reWle.id;
                 if($("#newExpands_"+workLogId).length > 0){//存在评论
                    var newExpands = $("#newExpands_"+workLogId).html();
                    var inHtml = '<div id="review_'+exId+'"><label class="reviewNameLabel">' + data.reWle.userName + '：</label>' + data.reWle.review+'</div>';
                    inHtml = inHtml +'<div class="mywork-list-filleting-r">' + data.reWle.reviewTime;
                    inHtml = inHtml +'<div class="mywork-list-filleting-restore" id="quoteReply_' + exId + '"><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-3.png" title="回复" width="20" height="20">&nbsp;&nbsp;<a href="javascript:void(0)"></a></div>';
                    inHtml = inHtml +'</div>';
                    inHtml = inHtml +'<div id="reply_' + exId + '" style="display:none">';
                    inHtml = inHtml +'<textarea class="mywork-list-restore-box" id="quote_txt_' + exId + '"></textarea>';
                    inHtml = inHtml +'<div class="mywork-list-box-f">';
                    inHtml = inHtml +'<div class="mywork-list-box-f-1" id="release_' + exId + '">发布</div>'
                    inHtml = inHtml +'</div>'
                    inHtml = inHtml +'</div>';
                    inHtml = inHtml + newExpands;
                    $("#newExpands_"+workLogId).html(inHtml);
                    $("#release_"+ exId).bind("click",function(){
                            reply(exId , workLogId);
                        });
                    $("#quoteReply_"+ exId).bind("click",function(){
                            showQuoteReply(exId , "review_"+exId);
                        });
                    reviewCount = parseInt($("#reviewCount_" + workLogId).html()) + 1;
                    $("#reviewCount_" + workLogId).html(reviewCount);
                 }
                 
                  $("#reply_" + id).hide();
             },
             complete: function(XMLHttpRequest, status){
             },
             error: function(XMLHttpRequest, textStatus){
                 // 请求出错处理
                 jAlert(XMLHttpRequest.responseText,'提示');
             }
         });
    
    
}

//初始化页面
$(document).ready(function(){
    if ($("#isLast").val() == "true"){
        $("#after").addClass("mywork-list-time-span3");
        $("#after").removeClass("mywork-list-time-span5");
     }else{
        $("#after").removeClass("mywork-list-time-span3");
        $("#after").addClass("mywork-list-time-span5");
        $("#editDiv").hide();
     }
     $.toaster({priority : 'info', message : '双击汇报内容可编辑' });
     $(window).goToTop({
        showHeight : 1,//设置滚动高度时显示
        speed : 500 //返回顶部的速度以毫秒为单位
    });
});

  </script>
</html>
