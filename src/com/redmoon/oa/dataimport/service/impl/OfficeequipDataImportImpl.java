package com.redmoon.oa.dataimport.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.basic.*;
import com.redmoon.oa.dataimport.bean.DataImportBean;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.officeequip.OfficeCache;
import com.redmoon.oa.officeequip.OfficeDb;
import com.redmoon.oa.officeequip.OfficeStocktakingDb;
import com.redmoon.oa.pvg.Privilege;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

/**
 * @Description: 办公用品导入
 * @author: 古月圣
 * @Date: 2015-11-27上午11:21:07
 */
public class OfficeequipDataImportImpl extends AbstractDataImportImpl {

	/**
	 * @Description:
	 * @param application
	 * @param request
	 * @return
	 * @throws ErrMsgException
	 */
	@Override
	public String create(ServletContext application, HttpServletRequest request) {
		JdbcTemplate jt = new JdbcTemplate();
		DataImportBean dib = super.getDataImportBean();
		JSONObject json = new JSONObject();
		try {
			String myname = new Privilege().getUser(request);
			FileUpload fileUpload = doUpload(application, request);
			String datastr = fileUpload.getFieldValue("datastr");
			datastr = URLDecoder.decode(datastr, "utf-8");
			JSONArray jsonAry = JSONArray.fromObject(datastr);
			JSONArray errAry = new JSONArray();
			StringBuilder isb = new StringBuilder();
			StringBuilder usb = new StringBuilder();
			isb.append("insert into ").append(dib.getTable()).append(" (");
			usb.append("update ").append(dib.getTable()).append(" set ");
			for (String field : dib.getFields()) {
				isb.append(field).append(",");
			}
			isb.append("typeid,buyperson,buydate,abstracts,flowid) values (");

			for (int i = 0; i < jsonAry.size(); i++) {
				JSONObject subjson = JSONObject.fromObject(jsonAry.get(i));
				StringBuilder isql = new StringBuilder(isb);
				StringBuilder usql = new StringBuilder(usb);
				boolean codeExist = false;
				String officeName = "";
				int storageCount = 0;
				for (String field : dib.getFields()) {
					String fieldValue = subjson.getString(field);

					if (field.equals("unit_code")) {
						if (!fieldValue.equals("")) {
							DeptDb dd = new DeptDb();
							fieldValue = dd.getCodeByName(fieldValue);
						} else {
							fieldValue = DeptDb.ROOTCODE;
						}
					}

					if (dib.getSplitFields().containsKey(field)) {
						String[] fieldValues = fieldValue.split("\\\\");
						String rootCode = "office_equipment";
						String parentCode = "office_equipment";
						int codeCount = 0;
						boolean isExists = true;
						for (String value : fieldValues) {
							TreeSelectDb tsd = new TreeSelectDb();
							tsd = tsd.getTreeSelectDb(parentCode);

							// 当前节点不存在,则他的子节点均不存在
							if (isExists) {
								TreeSelectMgr dir = new TreeSelectMgr();
								Vector children = dir.getChildren(parentCode);
								int index = 0;
								if (!rootCode.equals(parentCode)) {
									index = parentCode.length();
								}

								if (children.isEmpty()) {
									codeCount = 1;
									isExists = false;
								} else {
									Iterator ri = children.iterator();
									while (ri.hasNext()) {
										TreeSelectDb childlf = (TreeSelectDb) ri
												.next();
										String eachCode = childlf.getCode();
										// 找到同名节点
										if (childlf.getName().equals(value)) {
											codeCount = 0;
											parentCode = childlf.getCode();
											fieldValue = childlf.getCode();
											break;
										}
										int numberCode = StrUtil.toInt(eachCode
												.substring(index), 0);
										if (codeCount <= numberCode) {
											codeCount = numberCode + 1;
										}
									}
								}

								// 未找到同名节点
								if (codeCount != 0) {
									String lastCode = StrUtil.PadString(String
											.valueOf(codeCount), '0', 4, true);
									TreeSelectDb addTsd = new TreeSelectDb();
									addTsd
											.setCode(parentCode
													.equals(rootCode) ? lastCode
													: parentCode + lastCode);
									addTsd.setName(value);
									tsd.AddChild(addTsd);
									parentCode = addTsd.getCode();
									isExists = false;
									fieldValue = addTsd.getCode();
								}
							} else {
								TreeSelectDb addTsd = new TreeSelectDb();
								addTsd.setCode(parentCode + "0001");
								addTsd.setName(value);
								tsd.AddChild(addTsd);
								parentCode = addTsd.getCode();
								fieldValue = addTsd.getCode();
							}
						}
					}
					if (field.equals("officeName")) {
						OfficeDb od = new OfficeDb();
						codeExist = od.isExist(fieldValue);
						officeName = fieldValue;
					} else if (field.equals("storageCount")) {
						storageCount = StrUtil.toInt(fieldValue, 0);
					}
					isql.append(StrUtil.sqlstr(fieldValue)).append(",");
					usql.append(field).append("=").append(
							StrUtil.sqlstr(fieldValue)).append(",");
				}
				if (codeExist) {
					usql.append("buyperson=").append(StrUtil.sqlstr(myname))
							.append(",buydate=now() where officeName=").append(
									StrUtil.sqlstr(officeName));
					jt.addBatch(usql.toString());
				} else {
					isql.append("0,").append(StrUtil.sqlstr(myname)).append(
							",now(),'',0)");
					jt.addBatch(isql.toString());
				}
				// 增加盘点表记录
				OfficeStocktakingDb officest = new OfficeStocktakingDb();
				officest.setEquipmentCode(officeName);
				officest.setStockNum(storageCount);
				officest.saveForImport();
			}
			int[] ret = jt.executeBatch();
			OfficeCache oc = new OfficeCache();
			oc.refreshList();
			for (int i = ret.length - 1; i >= 0; i--) {
				if (ret[i] != 1) {
					errAry.add(jsonAry.get(i));
					jsonAry.remove(i);
				}
			}
			json.put("ret", 1);
			json.put("data", jsonAry);
			json.put("err", errAry);
		} catch (ErrMsgException e) {
			json.put("ret", 0);
			json.put("data", e.getMessage());
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (SQLException e) {
			json.put("ret", 0);
			json.put("data", e.getMessage());
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (UnsupportedEncodingException e) {
			json.put("ret", 0);
			json.put("data", e.getMessage());
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (jt != null) {
				jt.close();
			}
		}
		return json.toString();
	}

}
