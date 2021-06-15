<%@ page contentType="text/html;charset=utf-8"%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="scheduler_list.jsp"><span>调度列表</span></a></li>
    <li id="menu2"><a href="scheduler_add.jsp"><span>添加流程调度</span></a></li>
    <li id="menu4"><a href="scheduler_add_fileark.jsp"><span>添加全文检索</span></a></li>
	<%
    if (com.redmoon.oa.kernel.License.getInstance().isSrc()) {
    %>    
    <li id="menu3"><a href="scheduler_add_script.jsp"><span>添加脚本调度</span></a></li>
    <%}%>
    <li id="menu5"><a href="scheduler_add_syn_data.jsp"><span>添加订阅调度</span></a></li>
  </ul>
</div>

