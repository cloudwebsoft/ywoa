package com.redmoon.oa.ui.desktop;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.person.UserDesktopSetupDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.DesktopMgr;
import com.redmoon.oa.ui.IDesktopUnit;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.ModuleSetupDb;
import com.redmoon.oa.visual.ModuleUtil;
import com.redmoon.oa.visual.SQLBuilder;

/**
 * @Description: 
 * @author: 
 * @Date: 2017-10-31上午10:22:25
 */
public class VisualDesktopUnit implements IDesktopUnit {
    public VisualDesktopUnit() {
    }

    public String getPageList(HttpServletRequest request,
                              UserDesktopSetupDb uds) {
        String moduleCode = uds.getModuleItem();

        DesktopMgr dm = new DesktopMgr();
        com.redmoon.oa.ui.DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String url = du.getPageList() + "?code=" + moduleCode;
        return url;
    }

    @Override
	public String display(HttpServletRequest request, UserDesktopSetupDb uds) {
        DesktopMgr dm = new DesktopMgr();
        com.redmoon.oa.ui.DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String str = "";
        try {
        	com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
            String moduleCode = uds.getModuleItem();
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(moduleCode);
            if (msd==null) {
            	str = "<div class='no_content'>模块" + moduleCode + "不存在</div>";
            	return str;
			}
            String formCode = msd.getString("form_code");
            String op = "";
            
            String orderBy = "id";
            String sort = "desc";
			Privilege pvg = new Privilege();
			String userName = pvg.getUser(request);
        	String filter = StrUtil.getNullStr(msd.getFilter(userName)).trim();
        	boolean isComb = filter.startsWith("<items>") || filter.equals("");
        	// 如果是组合条件，则赋予后台设置的排序字段
        	if (isComb) {
        		orderBy = StrUtil.getNullStr(msd.getString("orderby"));
        		sort = StrUtil.getNullStr(msd.getString("sort"));
        		if ("".equals(orderBy)) {
        			orderBy = "id";
        		}
        		if ("".equals(sort)) {
        			sort = "desc";
        		}
        	}       
            
            FormDb fd = new FormDb();
            fd = fd.getFormDb(formCode);

            // 用于传过滤条件
            request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
            String[] ary = null;
            try {
            	ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
            }
            catch (ErrMsgException e) {
            	LogUtil.getLog(getClass()).error(e.getMessage());
            }

            String metaData = uds.getMetaData();
            String fieldTitle = "", fieldDate = "";
            JSONObject json = null;
            try {
				json = new JSONObject(metaData);
				fieldTitle = json.getString("fieldTitle");
				fieldDate = json.getString("fieldDate");
			} catch (JSONException e) {
				LogUtil.getLog(getClass()).error(e);
			}
			
			MacroCtlMgr mm = new MacroCtlMgr();
			
            String sql = ary[0];
        	ListResult lr = fdao.listResult(formCode, sql, 1, uds.getCount());
            Iterator ir = lr.getResult().iterator();
            if(ir.hasNext()) {
	           	str += "<table class='article_table'>";
	            while (ir.hasNext()) {
	                fdao = (FormDAO) ir.next();
	                String title1 = "", title2 = "";
	                title1 = fdao.getFieldValue(fieldTitle);
	                title2 = fdao.getFieldValue(fieldDate);
	                
	                FormField ff1 = fdao.getFormField(fieldTitle);
	                FormField ff2 = fdao.getFormField(fieldDate);
					if (ff1 != null && ff1.getType().equals(FormField.TYPE_MACRO)) {
						MacroCtlUnit mu = mm.getMacroCtlUnit(ff1.getMacroType());
						if (mu != null) {
							title1 = mu.getIFormMacroCtl().converToHtml(request, ff1, title1);
						}
					}	           
					if (ff2 != null && ff2.getType().equals(FormField.TYPE_MACRO)) {
						MacroCtlUnit mu = mm.getMacroCtlUnit(ff2.getMacroType());
						if (mu != null) {
							title2 = mu.getIFormMacroCtl().converToHtml(request, ff2, title2);
						}
					}	      					
	                
	                FormField ffDate = fdao.getFormField(fieldDate);
	                if (ffDate.getFieldType()==FormField.FIELD_TYPE_DATE || ffDate.getFieldType()==FormField.FIELD_TYPE_DATETIME) {
	                	title2 = "[" + title2 + "]";
	                }
	                
	                String t = StrUtil.getLeft(title1, uds.getWordCount());
	                str += "<tr><td class='article_content'><a title='" + StrUtil.toHtml(title1) + "' href='" + du.getPageShow() + "?id=" + fdao.getId() + "&formCode=" + formCode + "'>" +
	                        t + "</a></td><td class='article_time'>" +
	                        title2 +
	                        "</td></tr>";
	            }
	            str += "</table>";
            }else{
            	str = "<div class='no_content'><img title='暂无' src='images/desktop/no_content.jpg'></div>";
            }
        }
        catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
       
        return str;
    }

}