<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import = "cn.js.fan.cache.jcs.*"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@ page import = "org.json.*"%>
<%@page import="com.redmoon.oa.sso.SyncUtil"%>
<%@page import="com.redmoon.oa.pvg.Privilege"%>
<%@page import="com.redmoon.oa.tigase.Config"%>
<%@page import="com.redmoon.oa.tigase.TigaseConnection"%>
<%@ page import="com.redmoon.weixin.mgr.WXDeptMgr" %>
<%@ page import="com.redmoon.dingding.service.department.DepartmentService" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.dept.DeptMgr"/>
<%
String root_code = ParamUtil.get(request, "root_code");
if (root_code.equals("")) {
	root_code = "root";
}
String op = ParamUtil.get(request, "op");
if (op.equals("AddChild")) {
	JSONObject json = new JSONObject();
    String parent_code = ParamUtil.get(request, "parent_code").trim();
    String newName = ParamUtil.get(request, "name").trim();
   // int level = ParamUtil.get(request,"deptLayerLevel").split("\\\\").length;         //根部门归属获取部门层级
	boolean re = false;
	try {
		//判断名称是否有重复
		Vector children = dir.getChildren(parent_code);
	    Iterator ri = children.iterator();
	    while (ri.hasNext()) {
	    	DeptDb childlf = (DeptDb) ri.next();
	    	String name = childlf.getName();
	    	if(name.equals(newName)){
	    		json.put("ret", 2);	
	    		json.put("msg", "请检查名称是否有重复");	
				out.print(json.toString());	
				return;	
	    	}
	    }
	    //增加节点
		re = dir.AddChild(request);
		if (!re) {
			out.print(StrUtil.Alert(SkinUtil.LoadString(request, "res.label.cms.dir","add_msg")));
		}
		else {
			String code = ParamUtil.get(request, "code");
		}
		json.put("ret", 1);	
		out.print(json.toString());		
	}
	catch (ErrMsgException e) {
		// out.clear();
		// out.print(e.getMessage());
		json.put("ret", 3);	
		json.put("msg", e.getMessage());	
		out.print(json.toString());	
	}
	return;
}
else if (op.equals("del")) {
	String delcode = ParamUtil.get(request, "delcode");
	DeptDb delleaf = dir.getDeptDb(delcode);
	String selectCode = delleaf.getParentCode();
	JSONObject json = new JSONObject();
	if (delleaf==null) {
	}else {
		try {
			dir.del(delcode, new com.redmoon.oa.pvg.Privilege().getUser(request));
			// out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "res.label.cms.dir","success_del"), "dir_top_ajax.jsp?root_code=" + StrUtil.UrlEncode(root_code)));
			json.put("ret", "1");
			json.put("msg", "删除成功！");
			json.put("selectCode",selectCode);
		}
		catch (ErrMsgException e) {
			json.put("ret", "2");
			json.put("msg",e.getMessage() );
			json.put("selectCode",delcode);
			// out.clear();
			// out.print(e.getMessage());
		}
	}
	out.print(json.toString());	
	return;
}
else if (op.equals("modify")) {
	JSONObject json = new JSONObject();
	String code = ParamUtil.get(request, "code");
	String newName = ParamUtil.get(request, "name").trim();
	int type = ParamUtil.getInt(request, "type", DeptDb.TYPE_DEPT);
	boolean re = true;
	try {
		// 判断名称是否有重复
		DeptDb dept = new DeptDb();
		dept = dept.getDeptDb(code);
		String parent_code = dept.getParentCode();
		Vector children = dir.getChildren(parent_code);
	    Iterator ri = children.iterator();
	    while (ri.hasNext()) {
	    	DeptDb childlf = (DeptDb) ri.next();
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
			// out.print(StrUtil.Alert(SkinUtil.LoadString(request, "res.label.cms.dir","modify_finished")));
			// 修改目录
			DeptDb lf = new DeptDb();
			lf = lf.getDeptDb(code);
			// 同步用户信息中的unit_code
			DeptDb deptDb = new DeptDb(code);
			Vector vector = new Vector();
			vector = deptDb.getAllChild(vector, deptDb);
			vector.add(deptDb);
			
			JdbcTemplate jt = new JdbcTemplate();
			jt.setAutoClose(false);
			Iterator it = vector.iterator();
			// 如果是单位则将用户信息中的unit_code同步为当前部门code,如果不是单位则将用户信息中的unit_code同步为当前部门所属单位的code
			while (it.hasNext()) {
				DeptDb ddb = (DeptDb)it.next();
				String sql = "update users set unit_code=" 
					+ StrUtil.sqlstr(type == DeptDb.TYPE_UNIT ? code : deptDb.getUnitOfDept(deptDb).getCode()) 
					+ " where exists (select id from dept_user where user_name=name and dept_code=" 
					+ StrUtil.sqlstr(ddb.getCode())
					+ ")";
				try {
					jt.executeUpdate(sql);
				} catch (SQLException e) {
					out.print(StrUtil.Alert(e.getMessage()));
				}
			}
			jt.close();
			// 清缓存
			RMCache rmcache = RMCache.getInstance();
			rmcache.clear();
			json.put("ret", 1);	
			out.print(json.toString());		
		}
	}
	catch (ErrMsgException e) {
		out.clear();
		out.print(e.getMessage());
	}
	return;
}
else if (op.equals("move")) {
	JSONObject json = new JSONObject();
	String code = ParamUtil.get(request, "code");
	String parent_code = ParamUtil.get(request, "parent_code");
	int position = Integer.parseInt(ParamUtil.get(request, "position"));
	if("root".equals(code)){
		json.put("ret", "0");
		json.put("msg", "根节点不能移动！");
		out.print(json.toString());	
		return;
	}
	if("#".equals(parent_code)){
		json.put("ret", "0");
		json.put("msg", "不能与根节点平级！");
		out.print(json.toString());	
		return;
	}
	
	DeptDb moveleaf = dir.getDeptDb(code);
	int old_position = moveleaf.getOrders();//得到被移动节点原来的位置
	String old_parent_code = moveleaf.getParentCode();
	DeptDb newParentLeaf = dir.getDeptDb(parent_code); 
	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	if (cfg.getBooleanProperty("isLarkUsed")) {
		if (!newParentLeaf.getCode().equals(old_parent_code)) {
			json.put("ret", "0");
			json.put("msg", "因启用了lark精灵，所以只能在同级部门之间移动！");
			out.print(json.toString());	
			return;		
		}	
	}
	
	// 新节点
	Iterator ir = newParentLeaf.getChildren().iterator();
	
	moveleaf.setParentCode(parent_code);
	int p = position + 1;
	moveleaf.setOrders(p);
	moveleaf.save();

	// 同步至精灵
	com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
	SyncUtil su = new SyncUtil();
	if (ssoCfg.getBooleanProperty("isUse")) {
		su.orgSync(moveleaf, SyncUtil.MODIFY, new Privilege().getUser(request));
    }
	
	// 同步至tigase
	com.redmoon.oa.tigase.Config tigaseCfg = new com.redmoon.oa.tigase.Config();
	if (tigaseCfg.getBooleanProperty("isUse")) {
		TigaseConnection tc = new TigaseConnection();
		tc.syncDept(code, new Privilege().getUser(request));
	}
	// 同步至微信
	com.redmoon.weixin.Config weixinCfg = com.redmoon.weixin.Config.getInstance();
	if (weixinCfg.getBooleanProperty("isUse")) {
		if (!weixinCfg.getBooleanProperty("isSyncWxToOA")) {
			WXDeptMgr _wxDpetMgr = new WXDeptMgr();
			_wxDpetMgr.updateWxDept(moveleaf);
		}
	}
	// 同步至钉钉
	com.redmoon.dingding.Config dingDingCfg = com.redmoon.dingding.Config.getInstance();
	if(dingDingCfg.isUseDingDing() && !dingDingCfg.getBooleanProperty("isSyncDingDingToOA")) {
		DepartmentService _departmentService = new DepartmentService();
		_departmentService.updateDept(code);
	}

	while (ir.hasNext()) {
		DeptDb lf = (DeptDb)ir.next();
		// 跳过自己
		if (lf.getCode().equals(code)) {
			continue;
		}
		if(p<old_position){//上移
			if (lf.getOrders()>=p) {
				lf.setOrders(lf.getOrders() + 1);
				lf.save();
			}
		}else{//下移
			if(lf.getOrders()<=p && lf.getOrders()>old_position){
				lf.setOrders(lf.getOrders() - 1);
				lf.save();
			}
		}

		// 同步至精灵
		if (ssoCfg.getBooleanProperty("isUse")) {
    		su.orgSync(lf, SyncUtil.MODIFY, new Privilege().getUser(request));
	    }
		
		// 同步至tigase
		if (tigaseCfg.getBooleanProperty("isUse")) {
			TigaseConnection tc = new TigaseConnection();
			tc.syncDept(lf.getCode(), new Privilege().getUser(request));
		}

		if (weixinCfg.getBooleanProperty("isUse")) {
			if (!weixinCfg.getBooleanProperty("isSyncWxToOA")) {
				WXDeptMgr _wxDpetMgr = new WXDeptMgr();
				_wxDpetMgr.updateWxDept(lf);
			}
		}
		if(dingDingCfg.isUseDingDing() && !dingDingCfg.getBooleanProperty("isSyncDingDingToOA")) {
			DepartmentService _departmentService = new DepartmentService();
			_departmentService.updateDept(lf.getCode());
		}
	}
	
	// 原节点下的孩子节点通过修复repairTree处理
	DeptDb rootDeptDb = dir.getDeptDb(DeptDb.ROOTCODE);
	DeptMgr dm = new DeptMgr();
	dm.repairTree(rootDeptDb);
		
    DeptCache dcm = new DeptCache();
	dcm.removeAllFromCache();
	
	json.put("ret", "1");
	json.put("msg", "移动成功！");
	out.print(json.toString());	
	return;
}
%>