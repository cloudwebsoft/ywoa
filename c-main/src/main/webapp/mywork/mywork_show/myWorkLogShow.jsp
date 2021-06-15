<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="com.redmoon.oa.ui.SkinMgr"%>

<%@ taglib uri="/struts-tags" prefix="s"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
	  <title><s:property value="pageTitle"/></title>

	  <script src="../inc/common.js"></script>
	  <script src="../js/jquery-1.9.1.min.js"></script>
	  <script src="../js/jquery-migrate-1.2.1.min.js"></script>
	  <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css"/>
	  <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
	  <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
	  <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
	  <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/mywork/mywork_list.css"/>
	  <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	  <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	  <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	  <script src="js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
	  <script type="text/javascript" src="<%=request.getContextPath() %>/js/goToTop/goToTop.js"></script>
	  <link type="text/css" rel="stylesheet" href="<%=request.getContextPath() %>/js/goToTop/goToTop.css"/>

	  <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>

	  <script src="<%=request.getContextPath() %>/mywork/js/c_worklog.js"></script>
  </head>
  <body id="dayBody">
    <div class="mywork-list-wrap">
<!--日报时间选择-->
    <div class="mywork-list-time">
      <input type="hidden" name="workLogId" id="workLogId"  value=""/>
      <input type="hidden" name="isPreparedTodys" id="isPreparedTodys"  value="<s:property value="isPreparedTodys"/>"/>
      <input type="hidden" name="lastBeginTime" id="lastBeginTime" value="<s:property value="lastBeginTime"/>"/>
      
      <input type="hidden" name="contextPath" id="contextPath"  value="<%=request.getContextPath()%>"/>
      <input type="hidden" name="skinPath" id="skinPath"  value="<%=SkinMgr.getSkinPath(request)%>"/>
    </div>
         
<!--发布日报-->
    <s:iterator var="wl" value="list" status="statu">
      <div class="mywork-list-master">
          <div  id="mywork_<s:property value="#wl.id"/>">
	          <div class="mywork-list-master-p1"><s:property value="#wl.myDate"/></div> 
	           <div class="mywork-list-master-tabb1" id="<s:property value='#statu.index'/>" >
	              <s:property value="#wl.content" escape="false"/>
	          </div>
	          <s:if test="%{#wl.content != '暂未填写'}">
		          <div class="divComment" >
		          <div class="mywork-list-comment" title="评论" onclick="showReply('div_<s:property value='#wl.id'/>')"></div>
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
			                <a href="javascript:void(0);" name="attDel_<s:property value='#wl.id'/>" onclick="delAttach(<s:property value='#wa.id'/>,'attach_<s:property value='#wl.id'/>')" style="display:none">  <img src="<%=request.getContextPath()%>/images/del.png" width="16" height="16"/></a>
			              </div>  
			          </s:iterator>
		          </div>
	          </s:if>
          </div>
          <div id="div_<s:property value='#wl.id'/>" style="display:none">
           <span class="mywork-list-master-p4">
		       <textarea name="textarea2" rows="2" class="mywork-list-master-comment" id="textarea_<s:property value="#wl.id"/>"></textarea>
	       </span>
		   <div class="mywork-list-master-restore">
		       <div class="mywork-list-master-restore-btn" onclick="replyWorkLog('div_<s:property value='#wl.id'/>',<s:property value="#wl.id"/>)">评论</div>
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
		          
		          		 	<p><span><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-1.png" width="20" height="20"/>&nbsp;全部评论&nbsp;(<span id="reviewCount_<s:property value="#wl.id"/>"><s:property value="#wl.reviewCount"/></span>)</span>&nbsp;&nbsp;</span></p>
			        		<div id="newExpands_<s:property value="#wl.id"/>"></div>
				        	<s:iterator var="we" value="#wl.workLogExpands" status="statu3">
					             <div id="quoteReply_<s:property value="#we.id"/>"><label class="reviewNameLabel"><s:property value="#we.userName"/></label>：<s:property value="#we.review"/></div>
					             <div class="mywork-list-filleting-r"><s:property value="#we.reviewTime"/>
				                     <div class="mywork-list-filleting-restore" onclick="showQuoteReply('<s:property value='#we.id'/>','quoteReply_<s:property value="#we.id"/>')"><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-3.png" title="回复" width="20" height="20"></div>
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
    </div>
    
	<input type="hidden" value="<s:property value='dayLimit'/>" id="dayLimit"/>
	<input type="hidden" value="0" id="saveOrCreate"/>
	<input type="hidden" id="tempContent" value=""/>

</body>
  <script type="text/javascript">
  var uEditor = null;


//回复框显示控制
function showReply(id){
    $('#'+id).toggle();
    if($('#'+id).is(':visible')){
       $("#textarea_"+id.split("_")[1]).focus();
       $("#textarea_"+id.split("_")[1]).val("");
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

//选择时间
function changeDay(){
    var selectDate = $("#sendDate").val();
      //控制头部显示
    var myDate = new Date();
    myDate = myDate.Format("yyyy/MM/dd");    
    if (selectDate == myDate.toLocaleString()){
        $("#after").addClass("mywork-list-time-span3");
        $("#after").removeClass("mywork-list-time-span5");
     }else{
        $("#after").removeClass("mywork-list-time-span3");
        $("#after").addClass("mywork-list-time-span5");
        $("#editDiv").hide();
     }
     $("#dateCond").val(selectDate);
     $("#beforeOrAfter").val('2');
     queryFrm.submit();
    
}

//比较日期大小  
function compareDate(checkStartDate, checkEndDate) {      
    var arys1= new Array();      
    var arys2= new Array();      
	if(checkStartDate != null && checkEndDate != null) {      
	    arys1=checkStartDate.split('-');      
	    var sdate=new Date(arys1[0],parseInt(arys1[1]-1),arys1[2]);      
	    arys2=checkEndDate.split('-');      
	    var edate=new Date(arys2[0],parseInt(arys2[1]-1),arys2[2]);      
		if(sdate > edate) {      
		    return false;         
		}  else {   
		    return true;      
		}   
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
	     jAlert("评论内容不能超过200字！","提示");
	     return false;
	    }
        var reviewCount = 0;
        $.ajax({
             type: "post",
             url: '<%=request.getContextPath()%>/mywork/saveReviewExpands',
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
                    var inHtml = '<div id="replayContent_'+ exId +'"><label class="reviewNameLabel">' + data.reWle.userName + '：</label>' + data.reWle.review + '</div>';
                    inHtml = inHtml +'<div class="mywork-list-filleting-r">' + data.reWle.reviewTime;
                    inHtml = inHtml +'<div class="mywork-list-filleting-restore" id="quoteReply_' + exId + '"><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-3.png" title="回复" width="20" height="20">&nbsp;&nbsp;<a href="javascript:void(0)"></a></div>';
                    inHtml = inHtml +'</div>';
                    inHtml = inHtml +'<div id="reply_' + exId + '" style="display:none">';
                    inHtml = inHtml +'<textarea class="mywork-list-restore-box" id="quote_txt_' + exId + '"></textarea>';
                    inHtml = inHtml +'<div class="mywork-list-box-f">';
                    inHtml = inHtml +'<div class="mywork-list-box-f-1" id="release_' + exId + '" >发布</div>'
                    inHtml = inHtml +'</div>'
                    inHtml = inHtml +'</div>';
                    inHtml = inHtml + newExpands;
                    $("#newExpands_"+id).html(inHtml);
                    $("#release_"+ exId).bind("click",function(){
                            reply(exId , id);
                        });
                    $("#quoteReply_"+ exId).bind("click",function(){
                            showQuoteReply(exId , "replayContent_" + exId);
                        });
                    reviewCount = parseInt($("#reviewCount_" + id).html()) + 1;
                    $("#reviewCount_" + id).html(reviewCount);
                 }else{//不存在评论
                        var inHtml =  '<div class="div_review">'
                        inHtml = inHtml + '<p><span><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-1.png" width="20" height="20"/>&nbsp;全部评论&nbsp;(<span id="reviewCount_' + id + '">1</span>)</span></p>';
                        inHtml = inHtml + '<div id="newExpands_'+id+'"></div>';
                        inHtml = inHtml + '<div id="replayContent_'+exId+'"><label class="reviewNameLabel">' + data.reWle.userName + '：</label>' + data.reWle.review + '</div>';
                        inHtml = inHtml + '<div class="mywork-list-filleting-r">'+data.reWle.reviewTime;
                        inHtml = inHtml + '<div class="mywork-list-filleting-restore" id="quoteReply_' + exId + '"><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-3.png" title="回复" width="20" height="20">&nbsp;&nbsp;<a href="javascript:void(0)"></a></div>';
                        inHtml = inHtml + '</div>';
                        inHtml = inHtml + '<div id="reply_' + exId + '" style="display:none">';
                        inHtml = inHtml + '<textarea class="mywork-list-restore-box" id="quote_txt_' + exId + '"></textarea>';
                        inHtml = inHtml + '<div class="mywork-list-box-f">';
                        inHtml = inHtml + '<div class="mywork-list-box-f-1" id="release_' + exId + '">发布</div>';
                        inHtml = inHtml + '</div>';
                        $("#content_expands_"+id).find(".div_review").remove();
                        $("#content_expands_"+id).append(inHtml);
                        $("#release_"+ exId).bind("click",function(){
                            reply(exId , id);
                        });
                        $("#quoteReply_"+ exId).bind("click",function(){
                            showQuoteReply(exId , 'replayContent_'+exId );
                        });
                 }
                 
                 $("#" + index).hide();
             },
             complete: function(XMLHttpRequest, status){
             },
             error: function(XMLHttpRequest, textStatus){
                 // 请求出错处理
                 jAlert(XMLHttpRequest.responseText,"");
             }
         });
    }
//引用评论
function showQuoteReply(id, contentId){
    $("#reply_" + id).toggle();
    $("#quote_txt_" + id).html("//@" + $("#"+contentId).text());
}
//引用评论回复
function reply(id,workLogId){
    var content = $("#quote_txt_" + id).val();
    if ($.trim(content).length == 0){
            jAlert("评论内容不能为空！","提示");
            return false;
        }
    if (content.length > 200){
     jAlert("评论内容不能超过200字！","提示");
     return false;
    }
    var reviewCount = parseInt($("#reviewCount_" + workLogId).html()) + 1;
    $.ajax({
             type: "post",
             url: '<%=request.getContextPath()%>/mywork/saveReviewExpands',
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
                    var inHtml = '<div id="replayContent_'+exId+'"><label class="reviewNameLabel">' + data.reWle.userName + '：</label>' + data.reWle.review + '</div>';
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
                            showQuoteReply(exId , "replayContent_" + exId);
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
                 jAlert(XMLHttpRequest.responseText,"提示");
             }
         });
}

//初始化页面
$(document).ready(function(){
   
     $(window).goToTop({
        showHeight : 1,//设置滚动高度时显示
        speed : 500 //返回顶部的速度以毫秒为单位
    });
});
  </script>
</html>
