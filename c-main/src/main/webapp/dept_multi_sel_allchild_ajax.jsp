<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@page import="com.redmoon.oa.dept.DeptDb"%>
<%
	String code = ParamUtil.get(request,"code");
	if (!code.equals("")) {
		DeptDb dd = new DeptDb();
		dd = dd.getDeptDb(code);
		Vector v = new Vector();
		dd.getAllChild(v,dd);
		StringBuffer buffer = new StringBuffer();
		// buffer.append(code);
		if(v!=null&&v.size()!=0){
			Iterator ir = v.iterator();
			while(ir.hasNext()){
				DeptDb child = (DeptDb)ir.next();
				buffer.append(","+child.getCode());
			}
		}
		out.print(buffer.toString());
		return;
	}
	
	boolean isUnit = ParamUtil.getBoolean(request, "isUnit", false);
	
	String codes = ParamUtil.get(request, "codes");
	if (!codes.equals("")) {
		// System.out.println(getClass() + " codes=" + codes);
	
		Vector allv = new Vector();
		String[] ary = StrUtil.split(codes, ",");
		DeptDb dd2 = new DeptDb();
		for (int i=0; i<ary.length; i++) {
			DeptDb dd = dd2.getDeptDb(ary[i]);
			allv.add(dd);
		}
		for (int i=0; i<ary.length; i++) {
			DeptDb dd = dd2.getDeptDb(ary[i]);
			Vector v = new Vector();
			dd2.getAllChild(v, dd);
			Iterator ir = v.iterator();
			while (ir.hasNext()) {
				DeptDb a = (DeptDb)ir.next();
				Iterator ir2 = allv.iterator();
				boolean isFound = false;
				while (ir2.hasNext()) {
					DeptDb b = (DeptDb)ir2.next();
					if (b.getCode().equals(a.getCode())) {
						isFound = true;
						break;
					}
				}
				if (!isFound) {
					allv.add(a);
				}
			}
		}
		StringBuffer buffer = new StringBuffer();
		Iterator ir = allv.iterator();
		while(ir.hasNext()){
			dd2 = (DeptDb)ir.next();
			
			if (isUnit) {
				// 如果是单位
				if (dd2.getType()!=DeptDb.TYPE_UNIT)
					continue;
			}

			if (buffer.length()==0) {
				buffer.append(dd2.getCode() + ":" + dd2.getName());
			}
			else {
				buffer.append(","+dd2.getCode() + ":" + dd2.getName());
			}
		}
		out.print(buffer.toString());
		// System.out.println(getClass() + " " + buffer);
	}	
%>
