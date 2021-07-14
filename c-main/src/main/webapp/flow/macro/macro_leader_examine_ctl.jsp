<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.SkinUtil"%>
<%
	response.setHeader("X-Content-Type-Options", "nosniff");
	response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
	response.setContentType("text/javascript;charset=utf-8");

	String fieldName = ParamUtil.get(request, "fieldName");
	String code = "leader";
	SelectMgr sm = new SelectMgr();
    SelectDb sd = sm.getSelect(code);
	Vector v = sd.getOptions(new JdbcTemplate());
    Iterator ir = v.iterator();
	String examines = "";
    while (ir.hasNext()) {
	   SelectOptionDb sod = (SelectOptionDb) ir.next();
	   if(examines.equals("")){
		  examines = "'"+sod.getName()+"'";
	   }else{
		  examines += ","+"'"+sod.getName()+"'";
	   }
	}
	//WorkflowConfig cfg = WorkflowConfig.getInstance();
	//String examines = cfg.getProperty("examine");
%>
var nowid;
var totalid;
var can1press = false;
var wordafter;
var wordbefor;
$(document).ready(function(){
	if (!$("#<%=fieldName%>")[0])
    	return;
    $("#<%=fieldName%>").css({"line-height":"18px",color:"#000000",width:"200px",height:"18px",border:"1px solid #999"}); 
    var ctlleft = $("#<%=fieldName%>").offset().left;
	var ctltop = $("#<%=fieldName%>").offset().top;
    $("#<%=fieldName%>").mouseover(function(){ //文本框获得焦点，插入语句提示层
        $("#myword").remove();
        $(this).after("<div id='myword' style='width:202px;float:left;cursor:pointer;line-height:18px;height:auto;background:#fff; color:#6B6B6B;position:absolute;left:"+ (ctlleft-2) +"px; top:"+ (ctltop+21) +"px; border:1px solid #ccc;z-index:5px; '></div>");
        if($("#myword").html()){
            $("#myword").css("display","block");
			$(".newword").css("width",$("#myword").width());
			can1press = true;
        } else {
             $("#myword").css("display","none");
			can1press = false;
        }		
    }).mouseover(function(){ //文本框悬浮文字时，显示提示层和常用语句
		var press = $("#<%=fieldName%>").val();
		if(press==null||press==""){
		   press = "请输入内容";
		}
		if (press!="" || press!=null){
		var wordtxt = "";
		var wordvar = new Array(<%=examines%>);
		totalid = wordvar.length;
			var wordmy = "<div class='newword' style='width:170px; color:#6B6B6B; overflow:hidden;'><font color='#D33022'>" + press + "</font></div>";
			for(var i=0; i < wordvar.length; i++) {
				wordtxt = wordtxt + "<div class='newword' style='width:170px; color:#6B6B6B; overflow:hidden;'><font color='#D33022'></font>" + wordvar[i] + "</div>"
			}	
			$("#myword").html(wordmy+wordtxt);
			if($("#myword").html()){
				 $("#myword").css("display","block");
				 $(".newword").css("width",$("#myword").width());
				 can1press = true;
			} else {
				 $("#myword").css("display","none");
				 can1press = false;
			}
			beforepress = press;
		}
		if (press=="" || press==null){
		    $("#myword").html("");		
		     $("#myword").css("display","none");    
		}				
    })
	$(document).click(function(){ //文本框失焦时删除层
        if(can1press){
			$("#myword").remove();
			can1press = false;	
			if($("#<%=fieldName%>").focus()){
			    can1press = false;
			}
		}
    })
    $(".newword").live("mouseover",function(){ //鼠标经过提示语句时，高亮显示
        $(".newword").css("background","#FFF");
        $(this).css("background","#CACACA");		
		$(this).focus();
		nowid = $(this).index();
    }).live("click",function(){ //鼠标点击语句时，文本框内容替换成该条语句，并删除提示层
        var newhtml = $(this).html();
        newhtml = newhtml.replace(/<.*?>/g,"");
        $("#<%=fieldName%>").val(newhtml); 
        $("#myword").remove();
    })
	$(document).bind("keydown",function(e)  
	{     
		if(can1press){
			switch(e.which)     
			{            
				case 38:
				if (nowid > 0){		
					$(".newword").css("background","#FFF");
					$(".newword").eq(nowid).prev().css("background","#CACACA").focus();
					nowid = nowid-1;		
				}
				if(!nowid){
					nowid = 0;
					$(".newword").css("background","#FFF");
					$(".newword").eq(nowid).css("background","#CACACA");		
					$(".newword").eq(nowid).focus();				
				}
				break;       
		
				case 40:
				if (nowid < totalid){				
					$(".newword").css("background","#FFF");
					$(".newword").eq(nowid).next().css("background","#CACACA").focus();	
					nowid = nowid+1;			
				}
				if(!nowid){
					nowid = 0;
					$(".newword").css("background","#FFF");
					$(".newword").eq(nowid).css("background","#CACACA");		
					$(".newword").eq(nowid).focus();				
				}
				break;  
		
				case 13:
				var newhtml = $(".newword").eq(nowid).html();
				if(newhtml!=null){ //判定文本不为null
				newhtml = newhtml.replace(/<.*?>/g,"");
				$("#<%=fieldName%>").val(newhtml); 
				$("#myword").remove();
				}
			}
		}   
	})
})
