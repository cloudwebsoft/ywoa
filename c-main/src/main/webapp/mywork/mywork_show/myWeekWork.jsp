<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="com.redmoon.oa.ui.SkinMgr"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
	<title><s:property value="pageTitle"/>的工作周报</title>
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
  <%@ include file="mywork_inc_menu.jsp"%>
  <script>
	o("menu2").className="current";
  </script>
  
    <div class="mywork-list-wrap">
<!--日报时间选择-->
    <div class="mywork-list-time-week">
      <ul>
        <li class="mywork-list-time-span1" id="before" onclick="before()"></li>
        <li style="list-style:none;" class="mywork-list-time-span2" >第<s:property value="dateCond"/>周（<s:property value="dateArea"/>）</li>
        <li class="mywork-list-time-span3" id="after" onclick="after()"></li>
        <li class="mywork-list-time-span4" onclick="returnToday()">返回本周</li>
      </ul>
      <form id="queryFrm" name="queryFrm" action="<%=request.getContextPath()%>/mywork/queryMyWeekWorkForShow" method="post"  enctype="multipart/form-data">
	      <div class="mywork-list-search">
	          <input type="text" name="contentCond" id="contentCond" title="按“Enter”执行查询" value="<s:property value="contentCond"/>" onkeypress="if (event.keyCode == 13) search();">      
	          <img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-search.png" onclick="search()" class="searchImg"/>
	      </div>
          <input type="hidden" name="dateCond" id="dateCond" value="<s:property value="dateCond"/>"/>
          <input type="hidden" name="dayLimit" id="dayLimit" value="<s:property value="dayLimit"/>"/>
          <input type="hidden" name="logType" id="logType"  value="<s:property value="logType"/>"/>
          <input type="hidden" name="curPage" id="curPage"  value="<s:property value="curPage"/>"/>
          <input type="hidden" name="dateArea" id="dateArea"  value="<s:property value="dateArea"/>"/>
          <input type="hidden" name="beforeOrAfter" id="beforeOrAfter"  value="0"/>
          <input type="hidden" name="userName" id="userName" value="<s:property value="userName"/>"/>
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
	          <div class="mywork-list-master-p1"><s:property value="#wl.showTitle"/></div> 
	           <div class="mywork-list-master-tabb1" id="<s:property value='#statu.index'/>" >
	              <s:property value="#wl.content" escape="false"/>
	          </div>
	          <s:if test="%{#wl.content != '暂未填写'}">
		          <div class="divComment">
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
			              <div id="attach_<s:property value='#wl.id'/>">
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
    
	<div class="mywork-list-more" onclick="showMoreWeek()">————&nbsp;&nbsp;查看更多记录&nbsp;&nbsp;———— </div>
	<input type="hidden" value="<s:property value='dayLimit'/>"/>
	<input type="hidden" value="0" id="saveOrCreate"/>
	<input type="hidden" value="<s:property value='isLast'/>" id="isLast"/>
	<input type="hidden" id="tempContent" value=""/>

</body>
  <script type="text/javascript">
  var uEditor = null;

//加载更多日志
function showMoreWeek(){
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
        url: '<%=request.getContextPath()%>/mywork/queryMoreMyWork',
        data: {
             logType: $("#logType").val(),
             curPage: $("#curPage").val(),
             contentCond:contentCond,
             beforeOrAfter:$("#beforeOrAfter").val(),
             lastBeginTime: lastBeginTime,
             dateCond : $("#dateCond").val(),
             dateArea: $("#dateArea").val(),
             userName: $("#userName").val() 
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
                             + '<div class="mywork-list-master-p1">' + data.list[i].showTitle + '</div> '
                             + '<div class="mywork-list-master-tabb1" id="' + logId + '" >'
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
                    inhtml = inhtml + '<div id="review_'+we.id+'"><label class="reviewNameLabel">'+ we.userName + '</label>：' + we.review+'</div>'
                             + '<div class="mywork-list-filleting-r">' + we.reviewTime
                             + '<div class="mywork-list-filleting-restore" id="quoteReplyImg_' + we.id + '"><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-3.png" title="回复" width="20" height="20"></div>'
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
              $("#"+logId).bind("dblclick",{'id':logId, 'content':data.list[i].content},function(v){
                    changeThis('mywork_' + v.data["id"], v.data["id"], v.data["content"]);
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
                            delAttach(v.data["id"] ,"attach_" + v.data["id"]);
                        });
                     }
                
              }
             
              
              if (data.list[i].workLogExpands != null && data.list[i].workLogExpands.length > 0){
                     for (var k = 0; k < data.list[i].workLogExpands.length; k++){
                        var we = data.list[i].workLogExpands[k];
                        $("#quoteReplyImg_" + we.id).bind("click",{'id':we.id} ,function(v){
	                        showQuoteReply(v.data["id"] , "review_"+we.id);
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
            alert(XMLHttpRequest.responseText);
        }
    });
    
}
//回复框显示控制
function showReply(id){
    $('#'+id).toggle();
    if($('#'+id).is(':visible')){
       $("#textarea_"+id.split("_")[1]).focus();
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
	window.location.href = "queryMyMonthWorkForShow?logType=${logType}&userName=" + encodeURI("${userName}");
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
                    $("#newExpands_"+id).html(inHtml);
                    $("#release_"+ exId).bind("click",function(){
                            reply(exId , id);
                        });
                    $("#quoteReply_"+ exId).bind("click",function(){
                            showQuoteReply(exId , "review_"+exId);
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
                 alert(XMLHttpRequest.responseText);
             }
         });
    }
//引用评论
function showQuoteReply(id, contentId){
    var content = $("#"+contentId).text();
    $("#reply_" + id).toggle();
    $("#quote_txt_" + id).html("//@" + content);
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
                    var inHtml = '<label class="reviewNameLabel">' + data.reWle.userName + '：</label>' + data.reWle.review;
                    inHtml = inHtml +'<div class="mywork-list-filleting-r">' + data.reWle.reviewTime;
                    inHtml = inHtml +'<div class="mywork-list-filleting-restore" onclick="showQuoteReply("' + exId + '","review_'+data.reWle.id+'")"><img src="<%=SkinMgr.getSkinPath(request)%>/images/mywork/icon-3.png" title="回复" width="20" height="20">&nbsp;&nbsp;<a href="javascript:void(0)"></a></div>';
                    inHtml = inHtml +'</div>';
                    inHtml = inHtml +'<div id="reply_' + exId + '" style="display:none">';
                    inHtml = inHtml +'<textarea class="mywork-list-restore-box" id="quote_txt_' + exId + '"></textarea>';
                    inHtml = inHtml +'<div class="mywork-list-box-f">';
                    inHtml = inHtml +'<div class="mywork-list-box-f-1" onclick="reply("' + exId + ',' + workLogId + ')">发布</div>'
                    inHtml = inHtml +'</div>'
                    inHtml = inHtml +'</div>';
                    inHtml = inHtml + newExpands;
                    $("#newExpands_"+workLogId).html(inHtml);
                    reviewCount = parseInt($("#reviewCount_" + workLogId).html()) + 1;
                    $("#reviewCount_" + workLogId).html(reviewCount);
                 }
                 
                  $("#reply_" + id).hide();
             },
             complete: function(XMLHttpRequest, status){
             },
             error: function(XMLHttpRequest, textStatus){
                 // 请求出错处理
                 alert(XMLHttpRequest.responseText);
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
     $(window).goToTop({
        showHeight : 1,//设置滚动高度时显示
        speed : 500 //返回顶部的速度以毫秒为单位
    });
});

  </script>
</html>
