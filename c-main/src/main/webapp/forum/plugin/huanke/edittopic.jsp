<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.sql.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.setup.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.huanke.*"%>
<%@ page import="com.redmoon.forum.plugin.score.*"%>
<script src="<%=request.getContextPath()%>/inc/common.js"></script>
<script>
function alterexchange(v)
{
	if(v!="detail")
	{
		document.getElementById('exchangedetail').style.display="none";
	}else
	{
		document.getElementById('exchangedetail').style.display="";
	}
}
</script>
<%
long msgId = ParamUtil.getLong(request, "editid");
HuankeGoodsDb hgd = new HuankeGoodsDb();
hgd = hgd.getHuankeGoodsDb(msgId);
if (!hgd.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "该贴不是换客贴！"));
}
%>
<TABLE width="100%" border=0 align=center cellPadding=2 cellSpacing=1 bgcolor="#CCCCCC">
  <TBODY>
	<TR>
		<TD width="20%" align="left" bgcolor="#F9FAF3">物品类别：</TD>
		<TD height=23 align="left" bgcolor="#F9FAF3">
		<select name="catalogCode">
		<%
            com.redmoon.forum.plugin.huanke.Directory dir = new com.redmoon.forum.plugin.huanke.Directory();
            com.redmoon.forum.plugin.huanke.Leaf lf = dir.getLeaf("root");
            com.redmoon.forum.plugin.huanke.DirectoryView dv = new com.redmoon.forum.plugin.huanke.DirectoryView(lf);
            StringBuffer sb = new StringBuffer();
			dv.ShowDirectoryAsOptionsToString(sb, lf, lf.getLayer());
			out.print(sb);
		%>
	    </select>
		<script>
		frmAnnounce.catalogCode.value = "<%=hgd.getCatalogCode()%>"
		</script>
		<input type="hidden" name="pluginCode" value="<%=HuankeUnit.code%>"/></TD>
	</TR>	
	<TR>
        <TD width="20%" align="left" bgcolor="#F9FAF3">物品名称：</TD> 
        <TD width="80%" height=23 align="left" bgcolor="#F9FAF3"><input type="text" name="goods" value="<%=hgd.getGoods()%>"/></TD>
    </TR>
	<TR>
	  <TD align="left" bgcolor="#F9FAF3">新旧程度：</TD>
	  <TD height=23 align="left" bgcolor="#F9FAF3"><select name="depreciation">
	  <option value="全新" selected>全新</option>
	  <option value="九成新">九成新</option>
	  <option value="八成新">八成新</option>
	  <option value="七成新">七成新</option>
	  <option value="六成新">六成新</option>
	  <option value="五成新">五成新</option>
	  <option value="四成新">四成新</option>
	  <option value="三成新">三成新</option>
	  <option value="二成新">二成新</option>
	  <option value="一成新">一成新</option>
	  <option value="坏品">坏品</option>
      </select>
	  <script>
		frmAnnounce.depreciation.value = "<%=hgd.getDepreciation()%>"
	  </script>
	  </TD>
    </TR>
	<TR>
	  <TD align="left" bgcolor="#F9FAF3">交换地点：</TD>
	  <TD height=23 align="left" bgcolor="#F9FAF3">
	    <input type="text" name="exchangeProvince" value="<%=hgd.getExchangeProvince()%>"/></TD>
    </TR>
	<TR>
	  <TD align="left" bgcolor="#F9FAF3">联系方式：</TD>
	  <TD height=23 align="left" bgcolor="#F9FAF3"><input type="text" name="contact" value="<%=hgd.getContact()%>"/></TD>
    </TR>
	<TR>
	  <TD align="left" bgcolor="#F9FAF3">换品估价</TD>
	  <TD height=23 align="left" bgcolor="#F9FAF3"><input type="text" name="price" value="<%=hgd.getPrice()%>"/></TD>
    </TR>
	<TR>
	  <TD align="left" bgcolor="#F9FAF3">交换的物品地点：</TD>
	  <TD height=23 align="left" bgcolor="#F9FAF3"><input type="text" name="province" value="<%=hgd.getProvince()%>"/></TD>
    </TR>
	<TR>
	  <TD align="left" bgcolor="#F9FAF3">交换条件：</TD>
	  <TD height=23 align="left" bgcolor="#F9FAF3">
	    <select name="exchangeCondition" onchange="alterexchange(this.value);">
			<option value="其它任意物品">其它任意物品</option>
			<option value="随便，喜欢就好">随便，喜欢就好</option>
			<option value="其它等价物品">其它等价物品</option>
			<option value="detail">更详细编辑交换条件</option>
	    </select>
	    <script>
			frmAnnounce.exchangeCondition.value = "<%=hgd.getExchangeCondition()%>"
	    </script>
		</TD>
    </TR>
	<TR id="exchangedetail" style="display:none">
	  <TD height="23" colspan="2" align="left" bgcolor="#F9FAF3"><table width="100%" border=0 align=center cellPadding=2 cellSpacing=1 bgcolor="#CCCCCC">
        <tr>
          <td width="20%" align="left" bgcolor="#F9FAF3">想交换的物品类型：</td>
          <td width="80%" align="left" bgcolor="#F9FAF3"><select name="exchangeCatalogCode" value="<%=hgd.getExchangeProvince()%>">
            <%
            com.redmoon.forum.plugin.huanke.Directory change_dir = new com.redmoon.forum.plugin.huanke.Directory();
            com.redmoon.forum.plugin.huanke.Leaf change_lf = change_dir.getLeaf("root");
            com.redmoon.forum.plugin.huanke.DirectoryView change_dv = new com.redmoon.forum.plugin.huanke.DirectoryView(change_lf);
            StringBuffer change_sb = new StringBuffer();
			dv.ShowDirectoryAsOptionsToString(change_sb, change_lf, change_lf.getLayer());
			out.print(change_sb);
		%>
          </select>
		<script>
		frmAnnounce.exchangeCatalogCode.value = "<%=hgd.getExchangeCatalogCode()%>"
		</script>
		  </td>
        </tr>
        <tr>
          <td align="left" bgcolor="#F9FAF3">想交换的换品名称：</td>
          <td align="left" bgcolor="#F9FAF3"><input type="text" name="exchangeGoods" value="<%=hgd.getExchangeGoods()%>"/></td>
        </tr>
        <tr>
          <td align="left" bgcolor="#F9FAF3">交换条件说明：</td>
          <td align="left" bgcolor="#F9FAF3"><textarea name="exchangeDescription" rows="5" cols="25"><%=hgd.getExchangeDescription()%></textarea></td>
        </tr>
      </table></TD>
    </TR>
  </TBODY>
</TABLE>
<script>alterexchange("<%=hgd.getExchangeCondition()%>")</script>