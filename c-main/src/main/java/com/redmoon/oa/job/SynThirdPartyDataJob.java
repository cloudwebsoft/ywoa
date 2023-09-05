package com.redmoon.oa.job;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.cloudwebsoft.framework.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.*;

import com.cloudwebsoft.framework.db.DataSource;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.kernel.JobUnitDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.visual.ModuleRelateDb;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.NumberUtil;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 第三方数据表的名称为表单的code
 * @author fgf
 *
 */
//持久化
@PersistJobDataAfterExecution
//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
@DisallowConcurrentExecution
@Slf4j
public class SynThirdPartyDataJob extends QuartzJobBean {

	/**
	 * 获取第三方数据源中的一条记录及其相关表的
	 * @param thirdJt
	 * @param fd 存放第三方数据的主表单
	 * @throws JSONException 
	 */
	public void getRecord(JdbcTemplate thirdJt, FormDb fd, ResultRecord rr, int primaryKeyType, JSONObject dataMap) throws JSONException {

		// String primaryKey = ary[0];
		// String foreignKey = ary[1];

		com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO(fd);
		MacroCtlMgr mm = new MacroCtlMgr();

		JSONObject fieldMap = dataMap.getJSONObject("fieldMap");
		JSONArray aryCleans = dataMap.getJSONArray("cleanMap");
		// 将数据赋予给流程中的主表单
		Iterator ir = fdao.getFields().iterator();
		while (ir.hasNext()) {
			FormField ff = (FormField) ir.next();
			
			if (!fieldMap.has(ff.getName())) {
				continue;
			}
			
			String field = fieldMap.getString(ff.getName());
			
			String val = "";
			if (ff.getFieldType() == FormField.FIELD_TYPE_DATE) {
				java.util.Date dt = rr.getDate(field);
				val = DateUtil.format(dt, FormField.FORMAT_DATE);
			} else if (ff.getFieldType() == FormField.FIELD_TYPE_DATETIME) {
				Timestamp ts = rr.getTimestamp(field);
				String d = "";
				if (ts != null) {
					d = DateUtil.format(new java.util.Date(ts.getTime()),
							FormField.FORMAT_DATE_TIME);
				}
				val = d;
			} else if (ff.getFieldType() == FormField.FIELD_TYPE_DOUBLE) {
				double r = rr.getDouble(field);
				if (r == 0) {
					val = "";
				} else {
					val = String.valueOf(rr.getDouble(field));
				}
			} else if (ff.getFieldType() == FormField.FIELD_TYPE_FLOAT) {
				val = String.valueOf(rr.getFloat(field));
			} else if (ff.getFieldType() == FormField.FIELD_TYPE_PRICE) {
				val = NumberUtil.round(rr.getDouble(field), 2);
			} else {
				val = rr.getString(field);
			}
			
			if (ff.getType().equals(FormField.TYPE_MACRO)) {
				MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
				if (mu != null && !mu.getCode().equals("macro_raty")) {
					// 如果是基础数据宏控件
					boolean isClean = false;
					if (mu.getCode().equals("macro_flow_select") ) {
						JSONObject json = null;
						if (aryCleans!=null) {
							for (int i=0; i<aryCleans.length(); i++) {
								json = aryCleans.getJSONObject(i);
								if (ff.getName().equals(json.get("fieldName"))) {
									isClean = true;
									break;
								}
							}
						}
						// 如果需清洗数据
						if (isClean) {
							val = json.getString(val);
						}
					}
					if (!isClean) {
						val = mu.getIFormMacroCtl().getValueByName(ff, val);
					}
				}
			}
			
			ff.setValue(val);
		}

		fdao.setUnitCode(DeptDb.ROOTCODE);
		try {
			fdao.create();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

		String primaryKey = dataMap.getString("primaryKey");
		String foreignKey = dataMap.getString("foreignKey");		

		String primaryKeyValue = rr.getString(primaryKey);
		if (primaryKeyType == FormField.FIELD_TYPE_DATE) {				
			primaryKeyValue = SQLFilter.getDateStr(primaryKeyValue, "yyyy-MM-dd");
		} else if (primaryKeyType == FormField.FIELD_TYPE_DATETIME) {
			primaryKeyValue = SQLFilter.getDateStr(primaryKeyValue, "yyyy-MM-dd HH:mm:ss");
		} else if (primaryKeyType==FormField.FIELD_TYPE_VARCHAR || primaryKeyType==FormField.FIELD_TYPE_TEXT){
			primaryKeyValue = StrUtil.sqlstr(primaryKeyValue);
		}	
		
		// 获取子表单的数据
		ModuleRelateDb mrd = new ModuleRelateDb();
		ir = mrd.getModulesRelated(fd.getCode()).iterator();
		while (ir.hasNext()) {
			mrd = (ModuleRelateDb) ir.next();
			String formCodeRelated = mrd.getString("relate_code");

			FormDb fdRelated = new FormDb(formCodeRelated);

			String sqlRelate = "select * from " + fdRelated.getCode()
					+ " where " + foreignKey + "=" + primaryKeyValue;
			try {
				ResultIterator rti = thirdJt.executeQuery(sqlRelate);
				while (rti.hasNext()) {
					ResultRecord rrds = (ResultRecord) rti.next();

					com.redmoon.oa.visual.FormDAO fdaoRelated = new com.redmoon.oa.visual.FormDAO(
							fdRelated);

					Vector fieldsRelated = fdaoRelated.getFields();
					setFieldsValue(fdRelated.getCode(), rrds, fieldsRelated);

					fdaoRelated.setUnitCode(DeptDb.ROOTCODE);
					fdaoRelated.create();
				}
			} catch (SQLException e) {
				LogUtil.getLog(getClass()).error(e);
			}
		}

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
			} catch (IllegalArgumentException e) {
				continue;
			}

			if (ff.getFieldType() == FormField.FIELD_TYPE_DATE) {
				java.util.Date dt = rr.getDate(ff.getName());
				ff.setValue(DateUtil.format(dt, FormField.FORMAT_DATE));
			} else if (ff.getFieldType() == FormField.FIELD_TYPE_DATETIME) {
				Timestamp ts = rr.getTimestamp(ff.getName());
				String d = "";
				if (ts != null) {
					d = DateUtil.format(new java.util.Date(ts.getTime()),
							FormField.FORMAT_DATE_TIME);
				}
				ff.setValue(d);
			} else if (ff.getFieldType() == FormField.FIELD_TYPE_DOUBLE) {
				double r = rr.getDouble(ff.getName());
				if (r == 0) {
					ff.setValue("");
				} else {
					ff.setValue("" + rr.getDouble(ff.getName()));
				}
			} else if (ff.getFieldType() == FormField.FIELD_TYPE_FLOAT) {
				ff.setValue("" + rr.getFloat(ff.getName()));
			} else if (ff.getFieldType() == FormField.FIELD_TYPE_PRICE) {
				ff.setValue(NumberUtil.round(rr.getDouble(ff.getName()), 2));
			} else {
				/*
				 * if (ff.getName().equals("billtypeid")) {//查询单据类型表取单据类型名称
				 * String billTypeSql =
				 * "select name from billtype where billtypeid=" +
				 * StrUtil.sqlstr(rr.getString(ff.getName()));
				 * ff.setValue(selectName(billTypeSql));
				 * 
				 * }else
				 */
				ff.setValue(rr.getString(ff.getName()));

			}
			// }
		}
	}

	@Override
	public void executeInternal(JobExecutionContext jobExecutionContext)
			throws JobExecutionException {

		JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
		String str = data.getString("data");
		
		String[] ary = StrUtil.split(str, "\\|");
		
		String strJson = ary[0]; // 调度配置信息，包括表单编码、第三方数据源主键、外键等
		String strId = ary[1]; // 本JobUnit的id
		
        JobUnitDb jud = new JobUnitDb();
        int id = StrUtil.toInt(strId);
        jud = (JobUnitDb)jud.getQObjectDb(id);
		String lastValue = StrUtil.getNullStr(jud.getString("job_data")); // 最后取得的一条记录的主键
        
		JdbcTemplate jt = null;
		JSONObject json;
		try {
			json = new JSONObject(strJson);
			
			String formCode = json.getString("formCode");
			String dbSource = json.getString("dbSource");
			String primaryKey = json.getString("primaryKey");
			String foreignKey = json.getString("foreignKey");
			
			if (!json.has("table")) {
				return;
			}
			
			String table = json.getString("table");

			DataSource ds = new DataSource(dbSource);
			jt = new JdbcTemplate(ds);
			
			FormDb fd = new FormDb();
			fd = fd.getFormDb(formCode);
			
			FormField ff = fd.getFormField(primaryKey);
			
			String sql;
			if ("".equals(lastValue)) {
				sql = "select * from " + table;				
			}
			else {
				if (ff.getFieldType() == FormField.FIELD_TYPE_DATE) {				
					lastValue = SQLFilter.getDateStr(lastValue, "yyyy-MM-dd");
				} else if (ff.getFieldType() == FormField.FIELD_TYPE_DATETIME) {
					lastValue = SQLFilter.getDateStr(lastValue, "yyyy-MM-dd HH:mm:ss");
				} else if (ff.getFieldType()==FormField.FIELD_TYPE_VARCHAR || ff.getFieldType()==FormField.FIELD_TYPE_TEXT){
					lastValue = StrUtil.sqlstr(lastValue);
				}						
				sql = "select * from " + table + " where " + primaryKey + ">" + lastValue;				
			}
			jt.setAutoClose(false);
			ResultIterator ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				ResultRecord rr = ri.next();
				lastValue = rr.getString(primaryKey);
				getRecord(jt, fd, rr, ff.getFieldType(), json);
			}
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		finally {
			if (jt!=null) {
				jt.close();
			}
			
			if (!StrUtil.getNullStr(jud.getString("job_data")).equals(lastValue)) {
				try {
					jud.set("job_data", lastValue);
					jud.save();
				} catch (ResKeyException e) {
					LogUtil.getLog(getClass()).error(e);
				}		
			}
		}

	}

}
