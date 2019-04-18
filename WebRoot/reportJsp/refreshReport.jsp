<%@ page contentType="text/html;charset=GBK"%>
<%@ taglib uri="/WEB-INF/runqianReport4.tld" prefix="report"%>
<html>
	<head>
		<title>刷新报表定义并清除其缓存</title>
		<style>
			.tableHeader {
				background-color: #308dbb;
				color: white;
				font-family: verdana, arial, helvetica, sans-serif;
				font-size: 11px;
				font-weight: bold;
				text-align: center;
				padding-right: 3px;
				padding-left: 3px;
				padding-top: 4px;
				padding-bottom: 4px;
				margin: 0px;
				border-right-style: solid;
				border-right-width: 1px;
				border-color: white;
			}
			
			.button {
				font-family: verdana, arial, helvetica, sans-serif;
				font-size: 12px;
				border: 1px solid #A5C6CC;
				padding: 1px;
			}			
			
			.title {font-size: 20px; color: #C61313; font-weight: bold; }
		</style>
		<script type="text/javascript">
			function selectDo(form,markvalue){
			  var subGo=0;
			  for(var i = 0; i < form.elements.length; i++){
		        if( form.elements[i].type=="checkbox" ){
				  if( form.elements[i].checked ){
					  subGo++;
				   }
			    }
			   }
		
			    if(subGo<1){
				   alert("您没有选择！");
				   return ;
			    }
			    if ( markvalue == 'refresh'){
			         if(window.confirm("确认此操作吗？ 请确认！")==false)return;
			    }
		          
		          form.mark.value = markvalue;
		          form.submit();
		      }
		      

			 function selectAll(form ){
				  for(var i=0; i<form.elements.length; i++){
			        if(form.elements[i].type=="checkbox"){
						form.elements[i].checked = true;
				     }
				   }
			 	}
			 	
			 function clearAll(form ){
				  for( var i=0; i<form.elements.length; i++){
			        if(form.elements[i].type=="checkbox"){
						form.elements[i].checked = false;
				     }
				   }
			 	}		      
		</script>
	</head>
     <%
      com.runqian.report4.cache.CacheManager cacheManager = com.runqian.report4.cache.CacheManager.getInstance();


			if ( "refresh".equals( request.getParameter( "mark" ) ) ){
				String[] keys = request.getParameterValues( "keys" );
             if ( keys != null ){
                  for ( int i=0 ;i < keys.length ; i++ ){
                       String key = keys[i];
                       cacheManager.modifyReportDefine( key );
                  }
             }				
			}
		%>
	<body>
	  <div align="center">
		    <span class=title>刷新报表定义并清除其缓存</span>
	  </div>

		
		<form method=post action="" name=cacheForm>
		    <input type=hidden name='mark'>
			<table border=1  align=center cellSpacing=0 cellPadding=3 width="90%"  style="BORDER-COLLAPSE: collapse">
         <tr>                
             <td colspan=2 class=tableHeader>报表缓存列表</td>	
         </tr>
				<%
					java.util.Map entMap = cacheManager.getReportEntries();
					for (java.util.Iterator it = entMap.keySet().iterator(); it.hasNext();) {
						Object key = it.next();
				%>
				<tr>
					<td width=3%>
						<input type=checkbox name="keys" value="<%=key%>">
					</td>
					<td style="font-size: 12px">
						<%=key%>
					</td>
				</tr>
				<%
				}
				%>
			</table>
			<table border=0 width="90%" align=center >
				<tr>
				   <td>
					   <input type=button name=all value="全选" class="button" onclick="selectAll(this.form)">
					   <input type=button name=no value="全否" class="button" onclick="clearAll(this.form)">
					   <input type=button name=show value="清除缓存" class="button" onclick="selectDo(this.form , 'refresh')">
					   <input type=button name=show value="显示报表" class="button" onclick="selectDo(this.form , 'show')">
					   <input type=button name=refresh value="刷新列表" class="button" onclick="document.location=document.location">
					   <!--<input type=button name=del value="删除缓存" class="button" onclick="selectDo(this.form , 'del')">-->
					    
				   </td>
				</tr>
			</table>
		</form>

<%
			if ( "show".equals( request.getParameter( "mark" ) ) ){//show
				      String[] keys = request.getParameterValues( "keys" );
              if ( keys != null ){
                   String key = keys[0];
                  %>
					          <center>
									   <report:html name="report1" srcType="defineBean" beanName="<%=key%>"
												width="740" height="600" funcBarLocation=""
												needPageMark="yes"
												generateParamForm="no" 
												selectText="yes"  scrollWidth="580"
												scrollHeight="430" scrollBorder="border:none" />
										</center>
                  <%
               }			    
			}

%>		

	</body>
</html>
