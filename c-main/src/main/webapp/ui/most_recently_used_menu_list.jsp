<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.ui.menu.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>

<%
	String userName = ParamUtil.get(request, "user_name");
	String skincode = UserSet.getSkin(request);
	if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
	SkinMgr skm = new SkinMgr();
	Skin skin = skm.getSkin(skincode);
	String skinPath = skin.getPath();
	request.setAttribute("skinPath", skinPath);	
	//System.out.println(getClass() + " " + userName);
	
	int x = 2000;
	
	MostRecentlyUsedMenuDb mrum = new MostRecentlyUsedMenuDb();
	Vector<MostRecentlyUsedMenuDb> v = mrum.getMenuBeingStored(userName);
	if(v!=null && !v.isEmpty()) {
		Iterator<MostRecentlyUsedMenuDb> iterator = v.iterator();
		String menuCode = "";
		int times = -1;
		int k = 0;
		while(iterator.hasNext()) {
			mrum = iterator.next();
			menuCode = mrum.getString("menu_code");
			times = mrum.getInt("times");
			com.redmoon.oa.ui.menu.Leaf leaf = new com.redmoon.oa.ui.menu.Leaf();
			leaf = leaf.getLeaf(menuCode);
			if (leaf==null)
				continue;
%>
	<li id="menuItem<%=x%>"  onclick="selectMenuItem(<%=x%>);setBackGround('menuItem<%=x%>');" onmouseover="setDivBackground('menuItem<%=x%>','#d6dadc')" onmouseout="setDivBackground('menuItem<%=x++%>','')" isSelected=0> 
		<div class="listbox" onclick="storeMenu('<%=leaf.getCode()%>', false);openPage(this, '<%=leaf.getName()%>','<%=leaf.isCanRepeat()%>','true','<%=leaf.getLink(request)%>','<%=leaf.getTarget()%>')"><span class="new_f-1">■</span> <a onclick="storeMenu('<%=leaf.getCode()%>', false)" href="javascript:void(0);" link="<%=leaf.getLink(request)%>" hidefocus="true" canRepeat="<%=leaf.isCanRepeat()%>" target="<%=leaf.getTarget()%>"><%=leaf.getName()%></a></div>
	</li>

	<%
			k++;
			if (k==10)
				break;
		}
	}
%>
