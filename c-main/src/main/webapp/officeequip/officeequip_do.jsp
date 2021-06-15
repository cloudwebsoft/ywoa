<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import = "cn.js.fan.cache.jcs.*"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@ page import = "org.json.*"%>
<%@page import="com.redmoon.oa.basic.TreeSelectDb"%>
<%@page import="com.redmoon.oa.basic.TreeSelectMgr"%>
<%@page import="com.redmoon.oa.basic.TreeSelectCache"%>
<%@page import="com.redmoon.oa.officeequip.OfficeMgr"%>
<%@page import="com.redmoon.oa.officeequip.OfficeStocktakingDb"%>
<%@page import="com.redmoon.oa.officeequip.OfficeOpMgr"%>
<%@page import="com.redmoon.oa.officeequip.OfficeDb"%>
<%@page import="com.redmoon.oa.officeequip.OfficeOpDb"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.basic.TreeSelectMgr"/>
<%
	String root_code = ParamUtil.get(request, "root_code");
	if (root_code.equals("")) {
		root_code = "root";
	}
	String op = ParamUtil.get(request, "op");
	if (op.equals("AddChild")) {
		JSONObject json = new JSONObject();
		String parent_code = ParamUtil.get(request, "parent_code")
				.trim();
		String newName = ParamUtil.get(request, "name").trim();
		boolean re = false;
		try {
			//判断名称是否有重复
			Vector children = dir.getChildren(parent_code);
			Iterator ri = children.iterator();
			while (ri.hasNext()) {
				TreeSelectDb childlf = (TreeSelectDb) ri.next();
				String name = childlf.getName();
				if (name.equals(newName)) {
					json.put("ret", 2);
					json.put("msg", "请检查名称是否有重复！");
					out.print(json.toString());
					return;
				}
			}
			//增加节点
			re = dir.AddChild(request);
			if (!re) {
				out.print(StrUtil.Alert(SkinUtil.LoadString(request,
						"res.label.cms.dir", "add_msg")));
			} else {
				String code = ParamUtil.get(request, "code");
			}
			json.put("ret", 1);
			out.print(json.toString());
		} catch (ErrMsgException e) {
			out.clear();
			out.print(e.getMessage());
		}
		return;
	} else if (op.equals("del")) {
		String delcode = ParamUtil.get(request, "delcode");
		TreeSelectDb delleaf = dir.getTreeSelectDb(delcode);
		if (delleaf == null) {
		} else {
			try {
			
				OfficeMgr om = new OfficeMgr();
				boolean re = false;
					re = om.delTree(request);
				if (re) {
					dir.del(delcode);
					// out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "res.label.cms.dir","success_del"), "dir_top_ajax.jsp?root_code=" + StrUtil.UrlEncode(root_code)));
					JSONObject json = new JSONObject();
					json.put("ret", "1");
					json.put("msg", "删除成功！");
					out.print(json.toString());
				}else{
					JSONObject json = new JSONObject();
					json.put("ret", "2");
					json.put("msg", "删除失败！");
					out.print(json.toString());
				}
			} catch (ErrMsgException e) {
				out.clear();
				out.print(e.getMessage());
			}
		}
		return;
	} else if (op.equals("modify")) {
		JSONObject json = new JSONObject();
		String code = ParamUtil.get(request, "code");
		String newName = ParamUtil.get(request, "name").trim();
		int type = ParamUtil.getInt(request, "type", 0);
		boolean re = true;
		try {
			//判断名称是否有重复
			TreeSelectDb dept = new TreeSelectDb();
			dept = dept.getTreeSelectDb(code);
			String parent_code = dept.getParentCode();
			Vector children = dir.getChildren(parent_code);
			Iterator ri = children.iterator();
			while (ri.hasNext()) {
				TreeSelectDb childlf = (TreeSelectDb) ri.next();
				if (code.equals(childlf.getCode())) {
					continue;
				}
				String name = childlf.getName();
				if (name.equals(newName)) {
					json.put("ret", 2);
					json.put("msg", "请检查名称是否有重复！");
					out.print(json.toString());
					return;
				}
			}
			//修改节点
			re = dir.update(request);
			if (re) {
				json.put("ret", 1);
				out.print(json.toString());
			}

		} catch (ErrMsgException e) {
			out.clear();
			out.print(e.getMessage());
		}
		return;
	} else if (op.equals("move")) {
		JSONObject json = new JSONObject();
		String code = ParamUtil.get(request, "code");
		String parent_code = ParamUtil.get(request, "parent_code");
		int position = Integer.parseInt(ParamUtil.get(request,
				"position"));
		if ("office_equipment".equals(code)) {
			json.put("ret", "0");
			json.put("msg", "根节点不能移动！");
			out.print(json.toString());
			return;
		}
		if ("#".equals(parent_code)) {
			json.put("ret", "0");
			json.put("msg", "不能与根节点平级！");
			out.print(json.toString());
			return;
		}

		TreeSelectDb moveleaf = dir.getTreeSelectDb(code);
		String old_parent_code = moveleaf.getParentCode();
		TreeSelectDb newParentLeaf = dir.getTreeSelectDb(parent_code);
		int p = position + 1;
		// 异级目录
		if (!newParentLeaf.getCode().equals(old_parent_code)) {
			moveleaf.save(parent_code, p);
		} else {
			// 新节点
			Iterator ir = newParentLeaf.getChildren().iterator();

			moveleaf.setOrders(p);
			moveleaf.setParentCode(parent_code);
			moveleaf.save();
			while (ir.hasNext()) {
				TreeSelectDb lf = (TreeSelectDb) ir.next();
				// 跳过自己
				if (lf.getCode().equals(code)) {
					continue;
				}

				if (lf.getOrders() >= p) {
					lf.setOrders(lf.getOrders() + 1);
					lf.save();
				}
			}

			// 原节点下的孩子节点通过修复repairTree处理
			TreeSelectDb rootDeptDb = dir
					.getTreeSelectDb("office_equipment");
			TreeSelectMgr dm = new TreeSelectMgr();
			dm.repairTree(rootDeptDb);

			TreeSelectCache dcm = new TreeSelectCache();
			dcm.removeAllFromCache();
		}

		json.put("ret", "1");
		json.put("msg", "移动成功！");
		out.print(json.toString());
		return;
	} else if (op.equals("parent_name")) {
		JSONObject json = new JSONObject();
		String code = ParamUtil.get(request, "code");
		TreeSelectDb leaf = new TreeSelectDb(code);
		json.put("ret", "1");
		json.put("msg", leaf.getName());
		out.print(json.toString());
		return;
	} else if (op.equals("new_code")) {
		// 取得最初父节点最后一个孩子节点的编码（最初表示在移动之前，以为不能直接将父节点最后一个孩子节点加1变成新节点的编码，因为节点被移动后，编码是不变的，这个节点有可能被移动到其他节点下面了）
		String parentLastChildCode = "";
		String parent_code = ParamUtil.get(request, "parent_code").trim();
		int codeCount = 0;
		int index = 0;
		if (!root_code.equals(parent_code)) {
			index = parent_code.length();
		}
		TreeSelectDb pdd = new TreeSelectDb();
		pdd = pdd.getTreeSelectDb(parent_code);//得到父节点
		Vector children = dir.getChildren(parent_code);
		if (children.isEmpty()) {
			codeCount = 1;
		} else {
			int count = children.size();
			Iterator ri = children.iterator();
			int i = 0;
			int[] arr = new int[count];
			while (ri.hasNext()) {
				TreeSelectDb childlf = (TreeSelectDb) ri.next();
				String eachCode = childlf.getCode();
				try {
					String diffCode = eachCode.substring(index);//去掉父节点code的前缀
					int NumberCode = Integer.valueOf(diffCode);
					arr[i] = NumberCode;
				} catch (Exception e) {
				}
				i++;
			}
			Arrays.sort(arr);
			codeCount = arr[arr.length - 1] + 1;
		}
		
		int num = codeCount;		
		String newNodeCode;
		TreeSelectDb dd = null;
		do {
			if (root_code.equals(parent_code)){
				newNodeCode = StrUtil.PadString(String.valueOf(num), '0', 4, true);
			} else {
				newNodeCode = parent_code + StrUtil.PadString(String.valueOf(num), '0', 4, true);
			}
			num++;
			dd = new TreeSelectDb(newNodeCode);
		} while (dd != null && dd.isLoaded());

		JSONObject json = new JSONObject();
		json.put("ret", "1");
		json.put("msg", newNodeCode);
		System.out.println(getClass() + " " + newNodeCode);
		out.print(json.toString());
		return;
	} else if (op.equals("equip_add")) {
		JSONObject json = new JSONObject();
		OfficeMgr bm = new OfficeMgr();
		boolean re = false;
		try {
			re = bm.create(request);
			if (re) {
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			} else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");
			}
		} catch (ErrMsgException e) {
			json.put("ret", "0");
			json.put("msg", e.getMessage());
		}
		out.print(json.toString());
		return;
	} else if (op.equals("equip_total")) {
		JSONObject json = new JSONObject();
		String office = ParamUtil.get(request, "officeName");
		TreeSelectDb tsd = new TreeSelectDb(office);
		if (tsd.getChildCount() > 0) {
			json.put("ret", "0");
			json.put("msg", "请选择办公用品！");
		} else {
			OfficeStocktakingDb osd = new OfficeStocktakingDb();
			json.put("ret", "1");
			json.put("msg", osd.queryNumByCode(office));
		}
		
		out.print(json.toString());
		return;
	} else if (op.equals("op_add")) {
		JSONObject json = new JSONObject();
		OfficeMgr om = new OfficeMgr();
		OfficeOpMgr oo = new OfficeOpMgr();
		boolean re = false;
		try {
			re = om.chageStorageCount(request);
			if (re) {
				re = oo.create(request);
			}
			if (re) {
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			} else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");
			}
		} catch (ErrMsgException e) {
			json.put("ret", "0");
			json.put("msg", e.getMessage());
		}
		
		out.print(json.toString());
		return;
	} else if (op.equals("op_ret")) {
		JSONObject json = new JSONObject();
		OfficeOpMgr oom = new OfficeOpMgr();
		OfficeMgr om = new OfficeMgr();
		boolean re = false;
		try {
			re = oom.returnOfficeEquip(request);
			if (re) {
				re = om.returnChageStorageCount(request);
			}
			if (re) {
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			} else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");
			}
		} catch (ErrMsgException e) {
			json.put("ret", "0");
			json.put("msg", e.getMessage());
		}
		out.print(json.toString());
		return;
	} else if (op.equals("equip_check")) {
		JSONObject json = new JSONObject();
		String office = ParamUtil.get(request, "officeName");
		TreeSelectDb tsd = new TreeSelectDb(office);
		if (tsd.getChildCount() > 0) {
			json.put("ret", "0");
			json.put("msg", "请选择办公用品！");
		} else {
			json.put("ret", "1");
			OfficeDb od = new OfficeDb();
			od = od.getOfficeDbLastAdded(office);
			if (od!=null) {
				json.put("isFind", true);
				json.put("unit", od.getMeasureUnit());
				json.put("price", od.getPrice());
				json.put("buyPerson", od.getBuyPerson());
			}
			else {
				json.put("isFind", false);
			}
		}
		out.print(json.toString());
		return;
	} else if (op.equals("equip_storecount")) {
		JSONObject json = new JSONObject();
		String office = ParamUtil.get(request, "officeName");
		TreeSelectDb tsd = new TreeSelectDb(office);
		if (tsd.getChildCount() > 0) {
			json.put("ret", "0");
			json.put("msg", "请选择办公用品！");
		} else {
			OfficeDb officeDb = new OfficeDb();
			int inCount = officeDb.querySumByCode(office);
			
			OfficeOpDb officeOpDb = new OfficeOpDb();
			int receiveCount = officeOpDb.queryNumByCode(office, OfficeOpDb.TYPE_RECEIVE);
			int borrowCount = officeOpDb.queryNumByCode(office, OfficeOpDb.TYPE_BORROW);
			int returnCount = officeOpDb.queryNumByCode(office, OfficeOpDb.TYPE_RETURN);

			OfficeStocktakingDb officeStocktakingDb = new OfficeStocktakingDb();
			int storeCount = officeStocktakingDb.queryNumByCode(office);
			
			json.put("ret", "1");
			json.put("inCount", inCount);
			json.put("receiveCount", receiveCount);
			json.put("borrowCount", borrowCount);
			json.put("returnCount", returnCount);
			json.put("storeCount", storeCount);
		}
		
		out.print(json.toString());
		return;
	} else if (op.equals("equip_tookstore")) {
		JSONObject json = new JSONObject();
		String office = ParamUtil.get(request, "officeName");
		OfficeMgr officeMgr = new OfficeMgr();
		boolean re = false;
		try {
			re = officeMgr.tookstock(request);
			if (re) {
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			} else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");
			}
		} catch (ErrMsgException e) {
			json.put("ret", "0");
			json.put("msg", e.getMessage());
		}
		
		out.print(json.toString());
		return;
	} else if (op.equals("equip_return")) {
		JSONObject json = new JSONObject();
		String office = ParamUtil.get(request, "officeName");
		OfficeMgr officeMgr = new OfficeMgr();
		boolean re = false;
		try {
			re = officeMgr.tookstock(request);
			if (re) {
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			} else {
				json.put("ret", "0");
				json.put("msg", "操作失败！");
			}
		} catch (ErrMsgException e) {
			json.put("ret", "0");
			json.put("msg", e.getMessage());
		}
		
		out.print(json.toString());
		return;
	}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<META HTTP-EQUIV="pragma" CONTENT="no-cache"> 
<META HTTP-EQUIV="Cache-Control" CONTENT= "no-cache, must-revalidate"> 
<META HTTP-EQUIV="expires" CONTENT= "Wed, 26 Feb 1997 08:21:57 GMT">
<title><lt:Label res="res.label.cms.dir" key="content"/></title>
<LINK href="default.css" type=text/css rel=stylesheet>
<script src="../inc/common.js"></script>
</head>
<body>
</body>
</html>
