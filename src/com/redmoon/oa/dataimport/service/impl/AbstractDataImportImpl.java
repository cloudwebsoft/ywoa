package com.redmoon.oa.dataimport.service.impl;

import java.io.*;
import java.net.URLDecoder;
import java.sql.*;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.*;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.*;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.dataimport.bean.DataImportBean;
import com.redmoon.oa.dataimport.service.IDataImport;
import com.redmoon.oa.dataimport.util.FileUpMgr;
import com.redmoon.oa.dept.DeptDb;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

/**
 * @Description:
 * @author: 古月圣
 * @Date: 2015-11-25下午04:06:40
 */
public abstract class AbstractDataImportImpl implements IDataImport {

	private DataImportBean dataImportBean;

	private HashMap<String, ArrayList<String>> resultSet = new HashMap<String, ArrayList<String>>();

	/**
	 * @Description:
	 * @param dataImportBean
	 */
	@Override
	public void setDataImportBean(DataImportBean dataImportBean) {
		this.dataImportBean = dataImportBean;
	}

	/**
	 * @Description:
	 * @return
	 */
	@Override
	public DataImportBean getDataImportBean() {
		return this.dataImportBean;
	}

	/**
	 * @Description: 获取模板路径
	 * @param request
	 * @return
	 */
	@Override
	public String getTemplatePath(HttpServletRequest request) {
		return Global.getFullRootPath(request) + "/data_import/"
				+ dataImportBean.getCode() + "_template.xls";
	}

	/**
	 * @Description: 导入excel
	 * @param application
	 * @param request
	 * @return
	 */
	@Override
	public String importExcel(ServletContext application,
			HttpServletRequest request) {
		JSONObject json = new JSONObject();
		try {
			json = readExcel(application, request);
			if (json.toString().equals("") || !json.containsKey("data")) {
				json.put("ret", 0);
				json.put("data", "上传失败");
			} else {
				json.put("ret", 1);
				JSONArray jsonAry = json.getJSONArray("data");
				JSONArray errjsonAry = new JSONArray();
				for (int i = jsonAry.size() - 1; i >= 0; i--) {
					JSONObject subjson = JSONObject.fromObject(jsonAry.get(i));
					JSONObject errsubjson = new JSONObject();
					boolean isValid = true;
					for (String field : dataImportBean.getFields()) {
						String value = subjson.getString(field);
						errsubjson.put(field, value);
						if (!checkIsNotEmpty(field, value)) {
							errsubjson.put("err_" + field,
									DataImportBean.ERR_IS_NULL);
							isValid = false;
						} else if (!checkIsUnique(field, value)) {
							errsubjson.put("err_" + field,
									DataImportBean.ERR_UN_UNIQUE);
							isValid = false;
						} else if (!checkIsFloat(field, value)) {
							errsubjson.put("err_" + field,
									DataImportBean.ERR_NOT_FLOAT);
							isValid = false;
						} else if (!checkIsInteger(field, value)) {
							errsubjson.put("err_" + field,
									DataImportBean.ERR_NOT_INTEGER);
							isValid = false;
						} else if (!checkIsUnit(field, value)) {
							errsubjson.put("err_" + field,
									DataImportBean.ERR_NOT_UNIT);
							isValid = false;
						} else if (!checkRange(field, value)) {
							errsubjson.put("err_" + field,
									DataImportBean.ERR_OUT_RANGE);
							isValid = false;
						}
					}
					if (!isValid) {
						jsonAry.remove(i);
						errjsonAry.add(errsubjson);
					}
				}
				if (!errjsonAry.isEmpty()) {
					json.put("err", errjsonAry);
				}
			}
		} catch (ErrMsgException e) {
			json.put("ret", 0);
			json.put("data", e.getMessage());
		}
		return json.toString();
	}

	/**
	 * @Description: 读取excel中的内容
	 * @param application
	 * @param request
	 * @return 返回JSONObject对象
	 * @throws ErrMsgException
	 */
	public JSONObject readExcel(ServletContext application,
			HttpServletRequest request) throws ErrMsgException {
		FileUpMgr fum = new FileUpMgr();
		String excelFile = "";
		try {
			excelFile = fum.uploadExcel(application, request);
			if (excelFile.equals("")) {
				throw new ErrMsgException("请上传excel文件");
				// $('#popup_overlay').hide();
			}
		} catch (ErrMsgException e) {
			if (excelFile.equals("")) {
				throw new ErrMsgException("请上传excel文件");
				// $('#popup_overlay').hide();
			}
		}

		JSONObject json = new JSONObject();
		FileInputStream in = null;
		try {
			in = new FileInputStream(excelFile);
			if (excelFile.endsWith("xls")) {
				HSSFWorkbook w = (HSSFWorkbook) WorkbookFactory.create(in);
				HSSFSheet sheet = w.getSheetAt(0);
				if (sheet != null) {
					JSONArray jsonAry = new JSONArray();
					int rowcount = sheet.getLastRowNum();
					if (rowcount == 0) {
						throw new ErrMsgException("没有数据");
					}
					for (int k = 1; k <= rowcount; k++) {
						HSSFRow row = sheet.getRow(k);
						JSONObject subjson = new JSONObject();
						if (row != null) {
							for (int i = 0; i < dataImportBean.getFields()
									.size(); i++) {
								HSSFCell cell = row.getCell(i);
								if (cell == null) {
									subjson.put(dataImportBean.getFields().get(
											i), "");
								} else {
									cell.setCellType(HSSFCell.CELL_TYPE_STRING);
									subjson.put(dataImportBean.getFields().get(
											i), StrUtil.getNullStr(cell
											.getStringCellValue()));
								}
							}
							jsonAry.add(subjson);
						}
					}
					json.put("data", jsonAry);
				}
			} else if (excelFile.endsWith("xlsx")) {
				XSSFWorkbook w = (XSSFWorkbook) WorkbookFactory.create(in);
				XSSFSheet sheet = w.getSheetAt(0);
				if (sheet != null) {
					JSONArray jsonAry = new JSONArray();
					int rowcount = sheet.getLastRowNum();
					if (rowcount == 0) {
						throw new ErrMsgException("没有数据");
					}
					for (int k = 1; k <= rowcount; k++) {
						XSSFRow row = sheet.getRow(k);
						JSONObject subjson = new JSONObject();
						if (row != null) {
							for (int i = 0; i < 6; i++) {
								XSSFCell cell = row.getCell(i);
								if (cell == null) {
									subjson.put(dataImportBean.getFields().get(
											i), "");
								} else {
									cell.setCellType(XSSFCell.CELL_TYPE_STRING);
									subjson.put(dataImportBean.getFields().get(
											i), StrUtil.getNullStr(cell
											.getStringCellValue()));
								}
							}
							jsonAry.add(subjson);
						}
					}
					json.put("data", jsonAry);
				}
			}
		} catch (IOException e) {
			throw new ErrMsgException(e.getMessage());
		} catch (InvalidFormatException e) {
			throw new ErrMsgException(e.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					throw new ErrMsgException(e.getMessage());
				}
			}
		}
		java.io.File file = new java.io.File(excelFile);
		file.delete();
		return json;
	}

	public boolean checkIsNotEmpty(String field, String value) {
		if (dataImportBean.getNotEmptyFields().contains(field)) {
			if (value == null || value.equals("")) {
				return false;
			}
		}
		return true;
	}

	public boolean checkIsUnique(String field, String value) {
		if (dataImportBean.getUniqueFields().contains(field)) {
			if (resultSet.containsKey(field)) {
				ArrayList<String> list = resultSet.get(field);
				if (list.contains(value)) {
					return false;
				} else {
					list.add(value);
				}
			} else {
				ArrayList<String> list = new ArrayList<String>();
				list.add(value);
				resultSet.put(field, list);
			}
			JdbcTemplate jt = new JdbcTemplate();
			String sql = "select count(" + field + ") from "
					+ dataImportBean.getTable() + " where " + field + "="
					+ StrUtil.sqlstr(value);
			try {
				ResultIterator ri = jt.executeQuery(sql);
				if (ri.hasNext()) {
					ResultRecord rr = (ResultRecord) ri.next();
					if (rr.getInt(1) > 0) {
						return false;
					}
				}
			} catch (SQLException e) {
				return false;
			} finally {
				if (jt != null) {
					jt.close();
				}
			}
		}
		return true;
	}

	public boolean checkIsFloat(String field, String value) {
		if (dataImportBean.getFloatFields().contains(field)) {
			try {
				Double.parseDouble(value);
			} catch (Exception e) {
				return false;
			}
		}
		return true;
	}

	public boolean checkIsInteger(String field, String value) {
		if (dataImportBean.getIntegerFields().contains(field)) {
			try {
				Integer.parseInt(value);
			} catch (Exception e) {
				return false;
			}
		}
		return true;
	}

	public boolean checkIsUnit(String field, String value) {
		if (dataImportBean.getUnitFields().contains(field)) {
			if (value.equals("")) {
				return true;
			}
			DeptDb dd = new DeptDb();
			String code = dd.getCodeByName(value);
			dd = dd.getDeptDb(code);
			if (!dd.isLoaded() || dd.getType() != DeptDb.TYPE_UNIT) {
				return false;
			}
		}
		return true;
	}

	public boolean checkRange(String field, String value) {
		if (dataImportBean.getRangeFields().containsKey(field)) {
			String t = dataImportBean.getRangeFields().get(field);
			String[] ts = t.split(",");
			for (String temp : ts) {
				temp = temp.trim();
				String split = "";
				if (temp.startsWith("min>=")) {
					split = ">=";
				} else if (temp.startsWith("min>")) {
					split = ">";
				} else if (temp.startsWith("max<=")) {
					split = "<=";
				} else if (temp.startsWith("max<")) {
					split = "<";
				}
				String[] ary = temp.split(split);
				if (ary.length != 2) {
					return true;
				}
				float valueNum = StrUtil.toFloat(value);
				float rangeNum = StrUtil.toFloat(ary[1]);
				if (split.equals(">=")) {
					if (valueNum < rangeNum) {
						return false;
					}
				} else if (split.equals(">")) {
					if (valueNum <= rangeNum) {
						return false;
					}
				} else if (split.equals("<=")) {
					if (valueNum > rangeNum) {
						return false;
					}
				} else if (split.equals("<")) {
					if (valueNum >= rangeNum) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public FileUpload doUpload(ServletContext application,
			HttpServletRequest request) throws ErrMsgException {
		FileUpMgr fum = new FileUpMgr();
		return fum.doUpload(application, request);
	}

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
		JSONObject json = new JSONObject();
		try {
			FileUpload fileUpload = doUpload(application, request);
			String datastr = fileUpload.getFieldValue("datastr");
			datastr = URLDecoder.decode(datastr, "utf-8");
			JSONArray jsonAry = JSONArray.fromObject(datastr);
			JSONArray errAry = new JSONArray();
			StringBuilder sb = new StringBuilder();
			sb.append("insert into ").append(dataImportBean.getTable()).append(
					" (");
			for (int i = 0; i < dataImportBean.getFields().size(); i++) {
				String field = dataImportBean.getFields().get(i);
				sb.append(field).append(
						i == dataImportBean.getFields().size() - 1 ? ")" : ",");
			}
			sb.append(" values (");

			for (int i = 0; i < jsonAry.size(); i++) {
				JSONObject subjson = JSONObject.fromObject(jsonAry.get(i));
				StringBuilder sql = new StringBuilder(sb);
				for (int j = 0; j < dataImportBean.getFields().size(); j++) {
					String field = dataImportBean.getFields().get(j);
					sql
							.append(StrUtil.sqlstr(subjson.getString(field)))
							.append(
									j == dataImportBean.getFields().size() - 1 ? ")"
											: ",");
				}
				jt.addBatch(sql.toString());
			}
			int[] ret = jt.executeBatch();
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
		} catch (SQLException e) {
			json.put("ret", 0);
			json.put("data", e.getMessage());
		} catch (UnsupportedEncodingException e) {
			json.put("ret", 0);
			json.put("data", e.getMessage());
		} finally {
			if (jt != null) {
				jt.close();
			}
		}
		return json.toString();
	}

	/**
	 * @Description: 获取可能的错误提示
	 * @return
	 */
	@Override
	public String getErrorMessages() {
		String[] errMsgs = new String[DataImportBean.ERR_TYPE];
		for (int i = 0; i < dataImportBean.getFields().size(); i++) {
			String field = dataImportBean.getFields().get(i);
			String name = dataImportBean.getFieldsName().get(i);
			if (dataImportBean.getNotEmptyFields().contains(field)) {
				if (errMsgs[DataImportBean.ERR_IS_NULL] == null
						|| errMsgs[DataImportBean.ERR_IS_NULL].equals("")) {
					errMsgs[DataImportBean.ERR_IS_NULL] = name;
				} else {
					errMsgs[DataImportBean.ERR_IS_NULL] += "," + name;
				}
			}
			if (dataImportBean.getUniqueFields().contains(field)) {
				if (errMsgs[DataImportBean.ERR_UN_UNIQUE] == null
						|| errMsgs[DataImportBean.ERR_UN_UNIQUE].equals("")) {
					errMsgs[DataImportBean.ERR_UN_UNIQUE] = name;
				} else {
					errMsgs[DataImportBean.ERR_UN_UNIQUE] += "," + name;
				}
			}
			if (dataImportBean.getFloatFields().contains(field)) {
				if (errMsgs[DataImportBean.ERR_NOT_FLOAT] == null
						|| errMsgs[DataImportBean.ERR_NOT_FLOAT].equals("")) {
					errMsgs[DataImportBean.ERR_NOT_FLOAT] = name;
				} else {
					errMsgs[DataImportBean.ERR_NOT_FLOAT] += "," + name;
				}
			}
			if (dataImportBean.getIntegerFields().contains(field)) {
				if (errMsgs[DataImportBean.ERR_NOT_INTEGER] == null
						|| errMsgs[DataImportBean.ERR_NOT_INTEGER].equals("")) {
					errMsgs[DataImportBean.ERR_NOT_INTEGER] = name;
				} else {
					errMsgs[DataImportBean.ERR_NOT_INTEGER] += "," + name;
				}
			}
			if (dataImportBean.getUnitFields().contains(field)) {
				if (errMsgs[DataImportBean.ERR_NOT_UNIT] == null
						|| errMsgs[DataImportBean.ERR_NOT_UNIT].equals("")) {
					errMsgs[DataImportBean.ERR_NOT_UNIT] = name;
				} else {
					errMsgs[DataImportBean.ERR_NOT_UNIT] += "," + name;
				}
			}
			if (dataImportBean.getRangeFields().containsKey(field)) {
				String value = dataImportBean.getRangeFields().get(field);
				if (errMsgs[DataImportBean.ERR_OUT_RANGE] == null
						|| errMsgs[DataImportBean.ERR_OUT_RANGE].equals("")) {
					errMsgs[DataImportBean.ERR_OUT_RANGE] = DataImportBean
							.getRangeMsg(name, value);
				} else {
					errMsgs[DataImportBean.ERR_OUT_RANGE] += ","
							+ DataImportBean.getRangeMsg(name, value);
				}
			}
		}
		String errMsg = "";
		for (int i = 0; i < DataImportBean.ERR_TYPE; i++) {
			errMsg += (i + 1)
					+ "."
					+ errMsgs[i]
					+ (i == DataImportBean.ERR_OUT_RANGE ? "" : DataImportBean
							.getErrMsg(i))
					+ (i == DataImportBean.ERR_TYPE - 1 ? "。" : "； ");
		}
		return errMsg;
	}
	
	/**
	 * @Description: 获取越界的错误提示
	 * @return
	 */
	@Override
	public String getRangeErrMsg(String field, String value) {
		if (dataImportBean.getRangeFields().containsKey(field)) {
			String t = dataImportBean.getRangeFields().get(field);
			String[] ts = t.split(",");
			for (String temp : ts) {
				temp = temp.trim();
				String split = "";
				if (temp.startsWith("min>=")) {
					split = ">=";
				} else if (temp.startsWith("min>")) {
					split = ">";
				} else if (temp.startsWith("max<=")) {
					split = "<=";
				} else if (temp.startsWith("max<")) {
					split = "<";
				}
				String[] ary = temp.split(split);
				if (ary.length != 2) {
					return "";
				}
				float valueNum = StrUtil.toFloat(value);
				float rangeNum = StrUtil.toFloat(ary[1]);
				if (split.equals(">=")) {
					if (valueNum < rangeNum) {
						return "不能小于" + ary[1];
					}
				} else if (split.equals(">")) {
					if (valueNum <= rangeNum) {
						return "不能小于等于" + ary[1];
					}
				} else if (split.equals("<=")) {
					if (valueNum > rangeNum) {
						return "不能大于" + ary[1];
					}
				} else if (split.equals("<")) {
					if (valueNum >= rangeNum) {
						return "不能大于等于" + ary[1];
					}
				}
			}
		}
		return "";
	}
}
