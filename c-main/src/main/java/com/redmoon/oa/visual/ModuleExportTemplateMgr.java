package com.redmoon.oa.visual;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.StrUtil;

import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;

public class ModuleExportTemplateMgr {
	public static Vector<ModuleExportTemplateDb> getTempaltes(HttpServletRequest request, String formCode) {
		Privilege pvg = new Privilege();
		Vector<ModuleExportTemplateDb> result = new Vector<ModuleExportTemplateDb>();
		
		UserDb user = new UserDb();		
		user = user.getUserDb(pvg.getUser(request));
		
		ModuleExportTemplateDb metd = new ModuleExportTemplateDb();
		String sql = metd.getTable().getSql("listForForm");
		Vector<ModuleExportTemplateDb> v = metd.list(sql, new Object[]{formCode});
		Iterator<ModuleExportTemplateDb> ir = v.iterator();
		while (ir.hasNext()) {
			metd = ir.next();
			String roles = StrUtil.getNullStr(metd.getString("roles"));
			// 检查是否拥有权限
			if (!pvg.isUserPrivValid(request, "admin")) {
				boolean canSeeBtn = false;
				if (!"".equals(roles)) {
					String[] codeAry = StrUtil.split(roles, ",");
					if (codeAry!=null) {
						RoleDb[] rdAry = user.getRoles();
						if (rdAry!=null) {
							for (RoleDb rd : rdAry) {
								String roleCode = rd.getCode();
								for (String codeAllowed : codeAry) {
									if (roleCode.equals(codeAllowed)) {
										canSeeBtn = true;
										break;
									}
								}
								if (canSeeBtn) {
								    break;
								}
							}
						}
					}
					else {
						canSeeBtn = true;
					}
				}
				else {
					canSeeBtn = true;
				}
				
				if (!canSeeBtn) {
					continue;
				}
			}	
			result.addElement(metd);			
		}
		return result;
	}
}