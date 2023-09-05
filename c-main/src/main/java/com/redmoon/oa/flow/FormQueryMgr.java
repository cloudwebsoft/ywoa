package com.redmoon.oa.flow;

import cn.js.fan.util.*;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.*;

import com.cloudweb.oa.entity.Menu;
import com.cloudweb.oa.service.IMenuService;
import com.cloudweb.oa.utils.SpringUtil;
import java.util.*;
import com.redmoon.oa.pvg.Privilege;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.db.SequenceManager;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.cloudwebsoft.framework.util.LogUtil;


/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class FormQueryMgr {

    public FormQueryMgr() {
    }

    public FormQueryDb getFormQueryDb(int id) {
        FormQueryDb aqd = new FormQueryDb();
        return aqd.getFormQueryDb(id);
    }
    
    public int createForScript(HttpServletRequest request) throws ErrMsgException {
    	Privilege privilege = new Privilege();
    	String script = ParamUtil.get(request, "script");
    	String queryName = ParamUtil.get(request, "queryName");
    	
    	String isSystem = ParamUtil.get(request, "isSystem");
    	
        FormQueryDb fqd = new FormQueryDb();    	
		fqd.setQueryName(queryName);
		fqd.setScripts(script);
		fqd.setUserName(privilege.getUser(request));
		fqd.setSaved(true);
		fqd.setScript(true);
		fqd.setSystem(isSystem.equals("true"));
		fqd.setTimePoint(new java.util.Date());
		fqd.create();
		return fqd.getId();
    }
    
    public boolean modifyForScript(HttpServletRequest request) throws ErrMsgException {
    	Privilege privilege = new Privilege();
    	String script = ParamUtil.get(request, "script");
    	String queryName = ParamUtil.get(request, "queryName");
    	String statDesc = ParamUtil.get(request, "statDesc");
        FormQueryDb fqd = new FormQueryDb();    	
        int id = ParamUtil.getInt(request, "id", -1);        
    	fqd = fqd.getFormQueryDb(id);
		fqd.setQueryName(queryName);	
		fqd.setScripts(script);
		fqd.setUserName(privilege.getUser(request));	
		fqd.setStatDesc(statDesc);
		return fqd.save();
    }
    

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        FormQueryDb aqd = new FormQueryDb();
        boolean re = false;
        String queryName = "", tableFullCode = "", showFieldCode = "",conditionFieldCode = "",orderFieldCode = "",deptCode = "";

        queryName = ParamUtil.get(request, "queryName");
        tableFullCode = ParamUtil.get(request, "tableFullCodeStr");
        showFieldCode = ParamUtil.get(request, "showFieldCodeStr");
        conditionFieldCode = ParamUtil.get(request,"conditionFieldCodeStr");
        orderFieldCode = ParamUtil.get(request, "orderFieldCodeStr");
        deptCode = ParamUtil.get(request,"deptCodeStr");
        String sTimePoint = ParamUtil.get(request,"sTimePoint");
        Date timePoint = DateUtil.parse(sTimePoint, "yyyy-MM-dd");

        String queryRelated = ParamUtil.get(request, "queryRelated");
        int flowStatus = ParamUtil.getInt(request, "flowStatus", 1000);

        java.util.Date flowBeginDate1 = DateUtil.parse(ParamUtil.get(request, "flowBeginDate1"), "yyyy-MM-dd");
        java.util.Date flowBeginDate2 = DateUtil.parse(ParamUtil.get(request, "flowBeginDate2"), "yyyy-MM-dd");
    	
        String isSystem = ParamUtil.get(request, "isSystem");

        Privilege pvg = new Privilege();

        aqd.setQueryName(queryName);
        aqd.setTableCode(tableFullCode);
        aqd.setShowFieldCode(showFieldCode);
        aqd.setConditionFieldCode(conditionFieldCode);
        aqd.setOrderFieldCode(orderFieldCode);
        aqd.setDeptCode(deptCode);
        aqd.setTimePoint(timePoint);
        aqd.setUserName(pvg.getUser(request));
        aqd.setQueryRelated(queryRelated);
        aqd.setFlowStatus(flowStatus);
        aqd.setFlowBeginDate1(flowBeginDate1);
        aqd.setFlowBeginDate2(flowBeginDate2);
        aqd.setSystem(isSystem.equals("true"));
        re = aqd.create();
        if(re) {
            FormQueryConditionMgr aqcm = new FormQueryConditionMgr();
            re = aqcm.create(aqd);
        }
        /*
        // 判断是否需要刷新缓存
        if(!TimePointMgr.isCreate(timePoint)) {
            ArchiveUserInfoMgr auim = new ArchiveUserInfoMgr();
            try {
                auim.refreshArchiveUserInfo(timePoint);
            } catch(Exception e) {
            }
            TimePointDb tpd = new TimePointDb();
            int id = (int) SequenceManager.nextID(SequenceManager.OA_ARCHIVE_TIME_POINT);
            try {
                tpd.create(new JdbcTemplate(), new Object[] {
                    new Integer(id),
                    timePoint,
                    new Integer(aqd.getId()),
                    Calendar.getInstance().getTime(),
                    pvg.getUser(request)
                });
            } catch(Exception e) {
            }
        }*/

        return re;
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        String ids = ParamUtil.get(request, "ids");
        
        String[] ary = StrUtil.split(ids, ",");
        if (ary==null) {
        	throw new ErrMsgException("请选择记录！");
        }
        boolean re = false;
        for (int i=0; i<ary.length; i++) {
        	int id = StrUtil.toInt(ary[i]);
            FormQueryDb aqd = getFormQueryDb(id);
            FormQueryConditionMgr aqcm = new FormQueryConditionMgr();
            re = aqcm.del(id);
            if (aqd == null || !aqd.isLoaded()) {
                throw new ErrMsgException("该项已不存在！");
            }
            // 删除相关的权限
            FormQueryPrivilegeDb aqpd = new FormQueryPrivilegeDb();
            aqpd.delOfQuery(id);
            
            // 删除菜单项
            IMenuService menuService = SpringUtil.getBean(IMenuService.class);
            Menu menu = menuService.getMenu("query_" + id);
            if (menu != null) {
                menuService.del(menu);
            }
			
            aqd.del();
        }
        return re;

    }

    public boolean modify(HttpServletRequest request) throws ErrMsgException {
        boolean re = true;
        String query_name = "", table_code = "", show_field_code = "",
                save_condition_field_code = "",
                                            order_field_code = "";
        int id = ParamUtil.getInt(request, "id");
        query_name = ParamUtil.get(request, "query_name");
        table_code = ParamUtil.get(request, "table_code");
        show_field_code = ParamUtil.get(request, "show_field_code");
        save_condition_field_code = ParamUtil.get(request,"save_condition_field_code");
        order_field_code = ParamUtil.get(request, "order_field_code");
        String sTimePoint = ParamUtil.get(request,"sTimePoint");
        Date timePoint = DateUtil.parse(sTimePoint, "yyyy-MM-dd");

        String queryRelated = ParamUtil.get(request, "queryRelated");
        int flowStatus = ParamUtil.getInt(request, "flowStatus", 1000);
        java.util.Date flowBeginDate1 = DateUtil.parse(ParamUtil.get(request, "flowBeginDate1"), "yyyy-MM-dd");
        java.util.Date flowBeginDate2 = DateUtil.parse(ParamUtil.get(request, "flowBeginDate2"), "yyyy-MM-dd");

        FormQueryDb aqd = getFormQueryDb(id);
        aqd.setQueryName(query_name);
        aqd.setTableCode(table_code);
        aqd.setShowFieldCode(show_field_code);
        aqd.setConditionFieldCode(save_condition_field_code);
        aqd.setOrderFieldCode(order_field_code);
        aqd.setTimePoint(timePoint);
        aqd.setQueryRelated(queryRelated);
        aqd.setFlowStatus(flowStatus);
        aqd.setFlowBeginDate1(flowBeginDate1);
        aqd.setFlowBeginDate2(flowBeginDate2);
        re = aqd.save();
        return re;
    }

    public boolean modifyTimepoint(HttpServletRequest request) throws
            ErrMsgException {
        boolean re = false;

        int id = ParamUtil.getInt(request, "id");
        String timePoint = ParamUtil.get(request, "timePoint");

        FormQueryDb aqd = getFormQueryDb(id);
        aqd.setTimePoint(DateUtil.parse(timePoint, "yyyy-MM-dd"));
        re = aqd.save();
        return re;
    }

    public boolean modifyDeptCode(HttpServletRequest request) throws
            ErrMsgException {
        boolean re = false;

        int id = ParamUtil.getInt(request, "id");
        String deptCodes = ParamUtil.get(request, "deptCodes");

        if (deptCodes == "") {
            throw new ErrMsgException("请选择查询部门！");
        }

        FormQueryDb aqd = getFormQueryDb(id);
        aqd.setDeptCode(deptCodes.replaceAll(" ", ""));
        re = aqd.save();
        return re;
    }

    public boolean modifyTableCode(HttpServletRequest request) throws
            ErrMsgException {
        boolean re = false;
        String tableCode = "";
        int id = ParamUtil.getInt(request, "id");
        String[] tableCodeArr = ParamUtil.getParameters(request, "tableCode");
        if (tableCodeArr == null) {
            throw new ErrMsgException("请选择查询范围！");
        }

        FormQueryDb aqd = getFormQueryDb(id);
        aqd.setTableCode(tableCode);
        re = aqd.save();
        return re;
    }

    public boolean modifyShowFieldCode(HttpServletRequest request) throws
            ErrMsgException {
        boolean re = false;
        String showFieldCode = "";
        int id = ParamUtil.getInt(request, "id");

        String[] fieldCodeArr = ParamUtil.getParameters(request, "fieldCode");
        if (fieldCodeArr == null) {
            throw new ErrMsgException("请选择显示字段！");
        }

        int i = 0;
        while (i < fieldCodeArr.length) {
            showFieldCode += fieldCodeArr[i];
            if (i < fieldCodeArr.length - 1) {
                showFieldCode += ",";
            }
            i++;
        }

        FormQueryDb aqd = getFormQueryDb(id);
        aqd.setShowFieldCode(showFieldCode);
        re = aqd.save();
        return re;
    }

    public boolean modifyConditionFieldCode(HttpServletRequest request) throws
            ErrMsgException {
        boolean re = true;
        int id = ParamUtil.getInt(request, "id");
        FormQueryConditionMgr aqcm = new FormQueryConditionMgr();
        String saveConditionFieldCode = aqcm.getQueryCondition(request);

        re = aqcm.del(id);

        FormQueryDb aqd = getFormQueryDb(id);
        aqd.setConditionFieldCode(saveConditionFieldCode);
        re = aqcm.create(aqd);
        return re;
    }

    public boolean modifyOrderFieldCode(HttpServletRequest request) throws
            ErrMsgException {
        boolean re = false;
        String orderFieldCode = "";
        int id = ParamUtil.getInt(request, "id");

        String[] fieldCodeArr = ParamUtil.getParameters(request, "fieldCode");
        if (fieldCodeArr == null) {
            throw new ErrMsgException("请选择显示字段！");
        }

        int i = 0;
        while (i < fieldCodeArr.length) {
            if (!fieldCodeArr[i].equals("")) {
                if (orderFieldCode.equals(""))
                    orderFieldCode += fieldCodeArr[i];
                else
                    orderFieldCode += "," + fieldCodeArr[i];
            }
            i++;
        }

        FormQueryDb aqd = getFormQueryDb(id);
        aqd.setOrderFieldCode(orderFieldCode);
        re = aqd.save();
        return re;
    }
}
