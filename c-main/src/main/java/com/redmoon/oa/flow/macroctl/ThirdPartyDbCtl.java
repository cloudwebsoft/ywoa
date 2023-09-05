package com.redmoon.oa.flow.macroctl;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.cloudwebsoft.framework.db.DataSource;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.visual.ModuleRelateDb;

import cn.js.fan.db.*;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.NumberUtil;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.pvg.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ThirdPartyDbCtl extends AbstractMacroCtl {
    public ThirdPartyDbCtl() {
       super();
    }
    
    /**
     * 数据的预处理，在展开所有宏控件之前调用
     * @param request
     * @param formField
     */
    @Override
    public void preProcessData(HttpServletRequest request, FormField formField) {
		int thirdId = ParamUtil.getInt(request, "thirdId", -1);

		if (thirdId != -1 && StrUtil.getNullStr(formField.getValue()).equals("")) {
			DataSource ds = new DataSource("thirdpartydb");

			Privilege pvg = new Privilege();
			
			int flowId = StrUtil.toInt((String) request.getAttribute("cwsId"), -1);
			/*
			FormDb fd = new FormDb(formField.getFormCode());
			FormDAO fdao = new FormDAO();
			fdao.getFormDAO(flowId, fd);
			*/
			
			WorkflowDb wf = new WorkflowDb();
			wf = wf.getWorkflowDb(flowId);

			Vector fields = iFormDAO.getFields();
			
			// 第三方系统表中关联的明细表的外键，置于宏控件的默认值中
			String defaultVal = formField.getDefaultValue(); // mainTable-primaryKey, foreignKey
			String[] ary = StrUtil.split(defaultVal, ",");
			String primaryKey = ary[0];
			String foreignKey = ary[1];
			
			String sql = "select * from " + formField.getFormCode() + " where " + primaryKey + "=" + thirdId;
			// 从数据库中获取第三方数据库的连接
			JdbcTemplate jt = new JdbcTemplate(ds);
			ResultIterator rir = null;
			try {
				rir = jt.executeQuery(sql);
				while(rir.hasNext()) {
					ResultRecord rrd = (ResultRecord) rir.next();
					// 将数据赋予给流程中的主表单
					setFieldsValue(formField.getFormCode(), rrd, fields);
				}
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
						
			try {
				//给宏控件赋值防止数据重复填充
				iFormDAO.setFieldValue(formField.getName(), "1");
				// 将数据保存主表单
				iFormDAO.save();
			} catch (ErrMsgException e) {
				LogUtil.getLog(getClass()).error(e);
			}
			
			// 获取子表单的数据
			ModuleRelateDb mrd = new ModuleRelateDb();
			Iterator ir = mrd.getModulesRelated(formField.getFormCode()).iterator();
			while (ir.hasNext()) {
				mrd = (ModuleRelateDb) ir.next();
				String formCodeRelated = mrd.getString("relate_code");
				
				FormDb fdRelated = new FormDb(formCodeRelated);
				
				String sqlRelate = "select * from " + fdRelated.getCode() + " where " + foreignKey + "=" + thirdId;
				JdbcTemplate jt1 = new JdbcTemplate(ds);
				try {
					ResultIterator rti = jt1.executeQuery(sqlRelate);
					while (rti.hasNext()) {
						ResultRecord rrds = (ResultRecord)rti.next();
						
						com.redmoon.oa.visual.FormDAO fdaoRelated = new com.redmoon.oa.visual.FormDAO(fdRelated);

						Vector fieldsRelated = fdaoRelated.getFields();
						setFieldsValue(fdRelated.getCode(), rrds, fieldsRelated);
						
						fdaoRelated.setCwsId(String.valueOf(flowId));
						fdaoRelated.setFlowTypeCode(wf.getTypeCode());
						fdaoRelated.setUnitCode(pvg.getUserUnitCode(request));
						fdaoRelated.create();						
					}
				} catch (SQLException e) {
					LogUtil.getLog(getClass()).error(e);
				}
			}
		} else {
			thirdId = StrUtil.toInt(formField.getValue());
		}
           	
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField formField) {
	   int thirdId = ParamUtil.getInt(request, "thirdId", -1);

       String str = "<input id='" + formField.getName() + "' name='" + formField.getName() + "' value='" + thirdId + "' title='提取关键字'>";
       return str;
    }
    
    /*
     * 
     * 将第三方数据中的一行记录赋予给表单中的字段（主表表单或子表表单中的一行记录）
     */
    public void setFieldsValue(String formCode, ResultRecord rr, Vector fields) {
		// 将数据赋予给流程中的表单
		Iterator ir = fields.iterator();
		while (ir.hasNext()) {
			FormField ff = (FormField) ir.next();
			
			// 过滤掉在第三方数据表中不存在的字段	
			try {
				rr.get(ff.getName());
			}
			catch(IllegalArgumentException e) {
				continue;
			}
			
			// if (!ff.getName().equals("third")&& !ff.getName().equals("orderdetail")&& !ff.getName().equals("sorderdetail")&& !ff.getName().equals("ldyj")) {
				if (ff.getFieldType() == FormField.FIELD_TYPE_DATE) {
					java.util.Date dt = rr.getDate(ff.getName());
					ff.setValue(DateUtil.format(dt, FormField.FORMAT_DATE));
				} else if (ff.getFieldType() == FormField.FIELD_TYPE_DATETIME) {
					Timestamp ts = rr.getTimestamp(ff.getName());
					String d = "";
					if (ts != null)
						d = DateUtil.format(new java.util.Date(ts.getTime()),
								FormField.FORMAT_DATE_TIME);
					ff.setValue(d);
				} else if (ff.getFieldType() == FormField.FIELD_TYPE_DOUBLE) {
					double r = rr.getDouble(ff.getName());
					if (r == 0)
						ff.setValue("");
					else
						ff.setValue("" + rr.getDouble(ff.getName()));
				} else if (ff.getFieldType() == FormField.FIELD_TYPE_FLOAT) {
					ff.setValue("" + rr.getFloat(ff.getName()));
				} else if (ff.getFieldType() == FormField.FIELD_TYPE_PRICE) {
					ff.setValue(NumberUtil.round(rr.getDouble(ff
									.getName()), 2));
				} else {
					/*
					if (ff.getName().equals("billtypeid")) {//查询单据类型表取单据类型名称
						String billTypeSql = "select name from billtype where billtypeid="
								+ StrUtil.sqlstr(rr.getString(ff.getName()));
						ff.setValue(selectName(billTypeSql));

					}else
					*/
						ff.setValue(rr.getString(ff.getName()));
					
				}
			//}
		}
    }
     
    /**
     * 备用，根据第三数据库对应码表的Id 根据Id查询出相应的名称
     * 其实可以在第三方数据库中建视图来解决码表问题
     */
    public String selectName(String sql){
    	DataSource ds = new DataSource("thirdpartydb");
    	JdbcTemplate jt = new JdbcTemplate(ds);
		ResultIterator ri = null;
		String name = "";
		try {
			ri = jt.executeQuery(sql);
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				name = rr.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(getClass()).error(e);
		}
    	return name;
    } 
   
    @Override
	public Object getValueForCreate(int flowId, FormField ff) {

        return "";
    }
    
    public String getSetCtlValueScript(HttpServletRequest request, IFormDAO IFormDao, FormField ff, String formElementId) {
        String id = ParamUtil.get(request, "id");
        if (!"".equals(id)) {
        	ff.setValue(id);
        }
    	return super.getSetCtlValueScript(request, IFormDao, ff, formElementId);
    }

    @Override
    public String getHideCtlScript(FormField formField, String string) {
        return "";
    }

    public String getControlType() {
        return "";
    }

    public String getControlValue(String userName, FormField ff) {
        return "";
    }

    public String getControlText(String userName, FormField ff) {
        return "";
    }

    public String getControlOptions(String userName, FormField ff) {
        return "";
    }

}

