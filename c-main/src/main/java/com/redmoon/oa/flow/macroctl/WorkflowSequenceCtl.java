package com.redmoon.oa.flow.macroctl;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.Config;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.WorkflowParams;
import com.redmoon.oa.flow.WorkflowSequenceDb;
import com.redmoon.oa.pvg.Privilege;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 格式：prefix{dept}{date:yyyy-MM-dd}{num:year|month|day}{readonly}
 * dept表示发起人所在部门的简称，num冒号后的year表示每年重置为1 {readonly}表示只读 例
 * 序列数据存储格式：[{"dept":"cwsDefault", "num":"1", "date":"2012-01-08 12:12:12"}]
 * cwsDefault表示默认部门，date中表示当前num生成的时间
 * 例如：[{"num":"6","dept":"人事处","date":"2012-01-08 20:41:02"
 * },{"num":"2","dept":"全部","date":"2012-01-08 20:59:04"}]
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class WorkflowSequenceCtl extends AbstractMacroCtl {
	public static final String WORKFLOW_SEQUENCE_NONE = "-1";

	public static final String DEPT_DEFAULT = "cwsDefault";

	private static final int DEFAULT_AS_FORM = 0;

	public WorkflowSequenceCtl() {
	}

	/**
	 * 根据模板字符串，生成序列号
	 * 
	 * @param template
	 *            String
	 * @param deptShortName
	 *            String
	 * @param padLength
	 *            int
	 * @param jsonAry
	 *            JSONArray 数据库中存储的序列JSON
	 * @return String
	 * @throws ErrMsgException
	 */
	public String makeSequence(String template, String deptShortName,
			int padLength, JSONArray jsonAry, boolean isCurrent, WorkflowSequenceDb wsd)
			throws ErrMsgException, JSONException {
		JSONObject json = null;
		if (jsonAry.length() >= 1) {
			if (!deptShortName.equals(DEPT_DEFAULT)) {
				boolean isFound = false;
				for (int i = 0; i < jsonAry.length(); i++) {
					json = (JSONObject) jsonAry.get(i);
					if (json.get("dept").equals(deptShortName)) {
						isFound = true;
						break;
					}
				}
				if (!isFound)
					json = null;
			} else {
				json = (JSONObject) jsonAry.get(0);
				if (!json.get("dept").equals(DEPT_DEFAULT)) {
					json = null;
				}
			}
		}

		// System.out.println(getClass() + " json=" + json + " deptShortName=" +
		// deptShortName + " " + jsonAry.toString());

		boolean isDept = false;
		boolean isDate = false;
		boolean isNum = false;
		boolean isReset = false;
		String str = template;
		
		// 替换-分隔符
		// str = str.replaceAll("-\\{", wsd.getItemSeparator() + "{");
		// 替换全部的分隔符，包括日期中的
		str = str.replaceAll("-", wsd.getItemSeparator());
		
		if (str.indexOf("{dept}") != -1) {
			str = str.replaceAll("\\{dept\\}", deptShortName);
			isDept = true;
		}
		int p = str.indexOf("{date");
		String strdt = "";
		if (p != -1) {
			int q = str.indexOf("}", p);
			String d = str.substring(p, q);
			String[] ary = d.split(":");
			String format = "yyyyMMdd";
			if (ary.length == 2) {
				format = ary[1];
			}
			
			if (wsd.getYearDigit()==2) {
				format = format.replace("yyyy", "yy");
			}
			
			strdt = DateUtil.format(new java.util.Date(), format);
			str = str.substring(0, p) + strdt + str.substring(q + 1);
			isDate = true;
		}
		p = str.indexOf("{num");
		if (p != -1) {
			int q = str.indexOf("}", p);
			if (q == -1) {
				throw new ErrMsgException("缺少}");
			}
			int num = 1;
			String numStr = str.substring(p, q + 1);
			String[] ary = StrUtil.split(numStr, ":");
			String zeroDateSpace = "";
			if (ary.length == 2) {
				zeroDateSpace = ary[1]; // 多长时间复位为1
				isReset = true;
			}
			isNum = true;
			if (json != null) {
				// 比较当前时间与最后一次记录时间的年、月、日
				String nm = null;
				nm = json.getString("num");
				int nmLen = nm.length();
				// System.out.println(getClass() + " nm=" + nm);
				if (nm != null)
					num = StrUtil.toInt(nm);
				String dateStr = null;
				if (json.has("curdate")) {
					dateStr = json.getString("curdate");
					java.util.Date dt = new java.util.Date();
					if (dateStr != null) {
						if (dateStr.length() == 4) {
							dt = DateUtil.parse(dateStr, "yyyy");
						} else if (dateStr.length() >= 10) {
							dt = DateUtil.parse(dateStr, "yyyy-MM-dd");
						} else {
							dt = DateUtil.parse(dateStr, "yyyy-MM");
						}
						if (dt != null) {
							java.util.Date now = new java.util.Date();
							if (zeroDateSpace.equals("year}")) {
								if (DateUtil.getYear(now) != DateUtil
										.getYear(dt))
									num = 0; // 复位
							} else if (zeroDateSpace.equals("month}")) {
								if (DateUtil.getMonth(now) != DateUtil
										.getMonth(dt))
									num = 0; // 复位
							} else if (zeroDateSpace.equals("day}")) {
								if (DateUtil.getDay(now) != DateUtil.getDay(dt)) {
									num = 0; // 复位
								} else if (DateUtil.getMonth(now) != DateUtil
										.getMonth(dt))
									num = 0; // 复位
							}
						}
					}
				}
				json.put("curdate", DateUtil.format(new java.util.Date(),
						"yyyy-MM-dd"));

				if (!isCurrent) {
					num += 1;
				}
				nm = String.valueOf(num);
				if (nmLen > nm.length()) {
					String tempStr = "";
					for (int i = 0; i < 6 - nm.length(); i++) {
						tempStr = "0" + tempStr;
					}
					nm = tempStr + nm;
				}
				json.put("num", nm);
				json.put("date", strdt);
			} else {
				// System.out.println(getClass() + " json=" + json + " " +
				// deptShortName);

				json = new JSONObject();
				if (isDept || deptShortName.equals(DEPT_DEFAULT)) {
					json.put("dept", deptShortName);
				}
				if (isNum) {
					json.put("num", "1");
				}
				if (isDate) {
					json.put("date", DateUtil.format(new java.util.Date(),
							"yyyy-MM-dd HH:mm:ss"));
				}
				if (isReset) {
					json.put("curdate", DateUtil.format(new java.util.Date(),
							"yyyy-MM-dd"));
				}
				if (deptShortName.equals(DEPT_DEFAULT)) {
					if (jsonAry.length() >= 1) {
						for (int i = jsonAry.length() - 1; i >= 0; i--) {
							jsonAry.put(i + 1, jsonAry.get(i));
						}
						jsonAry.put(0, json);
					} else {
						jsonAry.put(json);
					}
				} else {
					jsonAry.put(json);
				}
			}

			String tmp = "" + num;
			if (padLength != 0) {
				tmp = StrUtil.PadString(tmp, '0', padLength, true);
			}
			str = str.substring(0, p) + tmp + str.substring(q + 1);
		}
		return str;
	}

	public String makeSequence(String template, String deptShortName,
			int padLength, JSONArray jsonAry, WorkflowSequenceDb wsd) throws ErrMsgException,
			JSONException {
		return makeSequence(template, deptShortName, padLength, jsonAry, false, wsd);
	}

	public String getSequenceVal(String userName, FormField ff,
			boolean isCurrent) {
		String strId = ff.getDefaultValueRaw();
		int typeId = -1;
		String value = "-1";
		try {
			typeId = Integer.parseInt(strId);
			WorkflowSequenceDb wsd = new WorkflowSequenceDb();
			wsd = wsd.getWorkflowSequenceDb(typeId);
			if (wsd.getType() == WorkflowSequenceDb.TYPE_NUMBER) {
				if (wsd.getLength() == 0)
					value = "" + wsd.getNextId();
				else
					value = StrUtil.PadString("" + wsd.getNextId(), '0', wsd
							.getLength(), true);
			} else {
				String curValue = wsd.getCurValue();
				String template = wsd.getTemplate();
				String deptShortName = DEPT_DEFAULT;
				// 检查模板格式中是否含有dept
				int p = template.indexOf("{dept}");
				// System.out.println(getClass() + " template=" + template +
				// " p=" + p);
				// 如果含有则取出发起人所在部门的编码
				// 有的部门简称可能为空致getValueForSave时保存的json中的dept为空，所以在此一律取简称
				if (p > -1 && !"".equals(userName)) {
					DeptUserDb dud = new DeptUserDb();
					Iterator ir = dud.getDeptsOfUser(userName).iterator();
					if (ir.hasNext()) {
						DeptDb dd = (DeptDb) ir.next();
						deptShortName = dd.getShortName(); // 发起人所在部门的简称
						// deptShortName = dd.getName();// 发起人所在部门的简称
					}
				}

				if (curValue.equals(""))
					curValue = "[]";
				JSONArray jsonAry = new JSONArray(curValue);
				value = makeSequence(wsd.getTemplate(), deptShortName, wsd
						.getLength(), jsonAry, isCurrent, wsd);
				// System.out.println(getClass() + " value=" + value);
				wsd.setCurValue(jsonAry.toString());
				wsd.save();
			}
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(
					"convertToHTMLCtl:" + e.getMessage());
		}

		return value;
	}

	public String getSequenceVal(String userName, FormField ff) {
		return getSequenceVal(userName, ff, false);
	}

	public synchronized String convertToHTMLCtl(HttpServletRequest request,
			FormField ff) {
		String userName = "";
		int flowId = StrUtil.toInt((String) request.getAttribute("cwsId"), -1);
		WorkflowDb wf = new WorkflowDb();
		wf = wf.getWorkflowDb(flowId);
		if (flowId != -1) {
			userName = wf.getUserName();
		} else {
			userName = new Privilege().getUser(request);
		}
		String value = getSequenceVal(userName, ff);
		// if (ff.isEditable()) {
		String str = "<input id='" + ff.getName() + "' name='" + ff.getName()
				+ "' value='" + value + "' size='15' readonly='readonly' />";
		return str;
		// }
		/*
		 * else { String str = "<input readonly id='" + ff.getName() +
		 * "' name='" + ff.getName() + "' value='" + value + "' size=15>";
		 * return str; }
		 */

	}

	public String converToHtml(HttpServletRequest request, FormField ff,
			String fieldValue) {
		Config cfg = new Config();
		if (cfg.getInt("flow_seq_default") == DEFAULT_AS_FORM) {
			return fieldValue;
		} else {
			String strId = ff.getDefaultValueRaw();
			int typeId = -1;
			String value = "-1";
			boolean isNum = false;
			typeId = StrUtil.toInt(strId);
			if (typeId != -1) {
				WorkflowSequenceDb wsd = new WorkflowSequenceDb();
				wsd = wsd.getWorkflowSequenceDb(typeId);
				if (wsd != null && wsd.isLoaded()
						&& wsd.getType() == WorkflowSequenceDb.TYPE_NUMBER) {
					isNum = true;
					if (wsd.getLength() == 0)
						value = fieldValue;
					else
						value = StrUtil.PadString("" + fieldValue, '0', wsd
								.getLength(), true);
				}
			}
			if (request != null) {
				int flowId = -1;
				int actionId = -1;
				WorkflowParams wfp = (WorkflowParams) request
						.getAttribute("workflowParams");
				if (wfp != null) {
					flowId = StrUtil.toInt(wfp.getFileUpload().getFieldValue(
							"flowId"), -1);
					actionId = StrUtil.toInt(wfp.getFileUpload().getFieldValue(
							"actionId"), -1);
				}
				if (flowId != -1 && actionId != -1) {
					WorkflowDb wf = new WorkflowDb();
					wf = wf.getWorkflowDb(flowId);
					WorkflowActionDb wa = new WorkflowActionDb(actionId);
					if (wa.isXorRadiate()) {
						return getSequenceVal(wf.getUserName(), ff, true);
					} else {
						return isNum ? value : fieldValue;
					}
				} else {
					return isNum ? value : fieldValue;
				}
			} else {
				return isNum ? value : fieldValue;
			}
		}
	}

	/**
	 * 获取用来保存宏控件原始值的表单中的HTML元素中保存的值，生成用以给控件赋值的脚本
	 * 
	 * @return String
	 */
	public String getSetCtlValueScript(HttpServletRequest request,
			IFormDAO IFormDao, FormField ff, String formElementId) {
		// 如果序列尚未写入，则此处不返回脚本，以便生成控件时显示下一个序列
		if (ff.getValue() == null
				|| ff.getValue().equals(WORKFLOW_SEQUENCE_NONE)) {
			return "";
		} else {
			return super.getSetCtlValueScript(request, IFormDao, ff,
					formElementId);
		}
	}

	/**
	 * 取得用来保存宏控件原始值的表单中的HTML元素，通常为textarea
	 * 
	 * @return String
	 */
	@Override
    public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(
			HttpServletRequest request, FormField ff) {
		// 如果序列尚未写入
		if (StrUtil.getNullStr(ff.getValue()).equals(WORKFLOW_SEQUENCE_NONE)) {
			FormField ffNew = new FormField();
			ffNew.setName(ff.getName());
			ffNew.setValue("");
			ffNew.setType(ff.getType());
			ffNew.setFieldType(ff.getFieldType());
			return super.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(
					request, ffNew);
		} else {
			// 如果已经写入，则显示已写入的值
			return super.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(
					request, ff);
		}
	}

	/**
	 * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
	 * 
	 * @return String
	 */
	public String getDisableCtlScript(FormField ff, String formElementId) {
		return super.getDisableCtlScript(ff, formElementId);
	}

	/**
	 * 当report时，取得用来替换控件的脚本
	 * 
	 * @param ff
	 *            FormField
	 * @return String
	 */
	public String getReplaceCtlWithValueScript(FormField ff) {
		return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType()
				+ "','" + ff.getValue() + "');\n";
	}

	@Override
	public Object getValueForCreate(int flowId, FormField ff) {
		return WORKFLOW_SEQUENCE_NONE;
	}

	@Override
	public Object getValueForCreate(FormField ff, FileUpload fu, FormDb fd) {
		String value = "";
		String strId = ff.getDefaultValueRaw();
		int typeId = -1;
		boolean isValid = true;
		try {
			typeId = Integer.parseInt(strId);
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(
					"getValueForCreate:" + e.getMessage());
			isValid = false;
		}
		if (isValid) {
			WorkflowSequenceDb wfs = new WorkflowSequenceDb();
			wfs = wfs.getWorkflowSequenceDb(typeId);

			if (wfs.getType() == WorkflowSequenceDb.TYPE_NUMBER) {
				long v = wfs.getNextId();
				value = "" + v;

				// 保存新的序列
				wfs.setCurIndex(v);
			} else {
				String deptShortName = DEPT_DEFAULT;
				JSONArray jsonAry = null;
				try {
					String curValue = wfs.getCurValue();
					String tempValue = wfs.getTemplate();
					int pos = tempValue.indexOf("{dept}");
					if (pos > -1) {
						String formValue = fu.getFieldValue(ff.getName());
						deptShortName = formValue.substring(pos);
						pos = deptShortName.indexOf("-");
						if (pos > -1) {
							deptShortName = deptShortName.substring(0, pos);
						}
					}
					if (curValue.equals(""))
						curValue = "[]";
					jsonAry = new JSONArray(curValue);
					try {
						value = makeSequence(tempValue, deptShortName, wfs
								.getLength(), jsonAry, wfs);
						wfs.setCurValue(jsonAry.toString());
					} catch (ErrMsgException ex1) {
						LogUtil.getLog(getClass()).error(StrUtil.trace(ex1));
					}
				} catch (JSONException ex) {
				}
			}
			wfs.save();
		} else
			value = "" + 0;
		return value;
	}

	@Override
	public Object getValueForSave(FormField ff, int flowId, FormDb fd,
								  FileUpload fu) {
		String value = "";
		// 从数据库中取出序列的值
		String valueFromDb = WORKFLOW_SEQUENCE_NONE;
		FormDAO fdao = new FormDAO(flowId, fd);
		fdao.load();
		Vector vts = fdao.getFields();
		Iterator irt = vts.iterator();
		while (irt.hasNext()) {
			FormField ff2 = (FormField) irt.next();
			if (ff2.getName().equals(ff.getName())) {
				valueFromDb = ff2.getValue();
				break;
			}
		}
		// 如果数据库中尚未保存，则加以保存
		if (valueFromDb == null || valueFromDb.equals(WORKFLOW_SEQUENCE_NONE)) {
			String strId = ff.getDefaultValueRaw();
			int typeId = -1;
			boolean isValid = true;
			try {
				typeId = Integer.parseInt(strId);
			} catch (Exception e) {
				LogUtil.getLog(getClass()).error(
						"getValueForSave:" + e.getMessage());
				isValid = false;
			}
			if (isValid) {
				WorkflowSequenceDb wfs = new WorkflowSequenceDb();
				wfs = wfs.getWorkflowSequenceDb(typeId);

				if (wfs.getType() == WorkflowSequenceDb.TYPE_NUMBER) {
					long v = wfs.getNextId();
					value = "" + v;

					// 保存新的序列
					wfs.setCurIndex(v);
					wfs.save();
				} else {
					WorkflowDb wf = new WorkflowDb();
					wf = wf.getWorkflowDb(flowId);
					DeptUserDb dud = new DeptUserDb();
					String deptShortName = DEPT_DEFAULT;
					if (wfs.getTemplate().indexOf("{dept}") > -1) {
						Iterator ir = dud.getDeptsOfUser(wf.getUserName())
								.iterator();
						if (ir.hasNext()) {
							DeptDb dd = (DeptDb) ir.next();
							deptShortName = dd.getShortName(); // 发起人所在部门的编码
							// deptShortName = dd.getName(); // 发起人所在部门的编码
						}
					}
					JSONArray jsonAry = null;
					try {
						String curValue = wfs.getCurValue();
						if (curValue.equals(""))
							curValue = "[]";
						jsonAry = new JSONArray(curValue);
						try {
							// 当前表单中输入的值，有可能被修改
							String formVal = ff.getValue();

							// 根据序列生成的值
							String seqVal = makeSequence(wfs.getTemplate(),
									deptShortName, wfs.getLength(), jsonAry, wfs);

							JSONObject json = null;
							if (jsonAry.length() >= 1) {
								if (!deptShortName.equals(DEPT_DEFAULT)) {
									boolean isFound = false;
									for (int j = 0; j < jsonAry.length(); j++) {
										json = (JSONObject) jsonAry.get(j);
										if (json.get("dept").equals(
												deptShortName)) {
											isFound = true;
											break;
										}
									}
									if (!isFound)
										json = null;
								} else {
									json = (JSONObject) jsonAry.get(0);
									if (!json.get("dept").equals(DEPT_DEFAULT)) {
										json = null;
									}
								}
							}
							Config cfg = new Config();
							// 如果表单值与生成的值不一样，则说明在表单中改了控件值
							if (!formVal.equals(seqVal)) {
								if (cfg.getInt("") == DEFAULT_AS_FORM) {
									String num = json.getString("num");
									int i = seqVal.indexOf(num);
									if (i > -1) {
										String temp1 = formVal.substring(0, i);
										String temp2 = seqVal.substring(0, i);
										if (temp1.equals(temp2)) {
											temp1 = formVal.substring(i);
											temp2 = seqVal.substring(i);
										}
										// 极端情况,两个人同时发起该流程,3个人就不考虑啦
										// 3个人也考虑了...4个人不考虑了
										if (StrUtil.toInt(temp2)
												- StrUtil.toInt(temp1) == 1
												|| StrUtil.toInt(temp2)
														- StrUtil.toInt(temp1) == 2) {
											value = seqVal;
											wfs.setCurValue(jsonAry.toString());
											wfs.save();
											if (wf.getTitle().contains(formVal)) {
												wf.setTitle(wf.getTitle()
														.replaceAll(formVal,
																value));
												wf.save();
											}
										} else {
											value = formVal;
										}
									} else {
										value = formVal;
									}
								} else {
									value = seqVal;
									wfs.setCurValue(jsonAry.toString());
									wfs.save();
									if (!"".equals(formVal) && wf.getTitle().contains(formVal)) {
										wf.setTitle(wf.getTitle().replaceAll(
												formVal, value));
										wf.save();
									}
								}
							} else {
								value = seqVal;
								wfs.setCurValue(jsonAry.toString());
								wfs.save();
							}
						} catch (ErrMsgException ex1) {
							LogUtil.getLog(getClass())
									.error(StrUtil.trace(ex1));
						}
					} catch (JSONException ex) {
					}
				}
				value = StrUtil.PadString(value, '0', wfs.getLength(), true);
			} else
				value = "" + 0;
		} else {
			// logger.info("save: value = " + ff.getValue() + " macroType=" +
			// ff.getMacroType());
			value = ff.getValue();
		}

		return value;
	}

	/**
	 * 用于visual可视化模块处理，保存前获得表单域的值
	 * 
	 * @param ff
	 *            FormField ff的值取自于数据库中
	 * @param fd
	 *            FormDb
	 * @param formDAOId
	 *            long
	 * @param fu
	 *            FileUpload
	 * @return Object
	 */
	@Override
    public Object getValueForSave(FormField ff, FormDb fd, long formDAOId,
                                  FileUpload fu) {
		String value = "";
		// 从数据库中取出序列的值
		String valueFromDb = WORKFLOW_SEQUENCE_NONE;
		com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
		fdao = fdao.getFormDAO(formDAOId, fd);
		Vector vts = fdao.getFields();
		Iterator irt = vts.iterator();
		while (irt.hasNext()) {
			FormField ff2 = (FormField) irt.next();
			if (ff2.getName().equals(ff.getName())) {
				valueFromDb = ff2.getValue();
				break;
			}
		}
		// 如果数据库中尚未保存，则加以保存
		if (valueFromDb == null || valueFromDb.equals(WORKFLOW_SEQUENCE_NONE)) {
			String strId = ff.getDefaultValueRaw();
			int typeId = -1;
			boolean isValid = true;
			try {
				typeId = Integer.parseInt(strId);
			} catch (Exception e) {
				LogUtil.getLog(getClass()).error(
						"getValueForSave:" + e.getMessage());
				isValid = false;
			}
			if (isValid) {
				WorkflowSequenceDb wfs = new WorkflowSequenceDb();
				wfs = wfs.getWorkflowSequenceDb(typeId);

				if (wfs.getType() == WorkflowSequenceDb.TYPE_NUMBER) {
					long v = wfs.getNextId();
					value = "" + v;

					// 保存新的序列
					wfs.setCurIndex(v);
				} else {
					String userName = fdao.getCreator();
					DeptUserDb dud = new DeptUserDb();
					String deptShortName = "";
					Iterator ir = dud.getDeptsOfUser(userName).iterator();
					if (ir.hasNext()) {
						DeptDb dd = (DeptDb) ir.next();
						deptShortName = dd.getShortName(); // 创建人所在部门的编码
						// deptShortName = dd.getName(); // 创建人所在部门的编码
					}
					JSONArray jsonAry = null;
					try {
						String curValue = wfs.getCurValue();
						if (curValue.equals(""))
							curValue = "[]";
						jsonAry = new JSONArray(curValue);
						try {
							value = makeSequence(wfs.getTemplate(),
									deptShortName, wfs.getLength(), jsonAry, wfs);
							wfs.setCurValue(jsonAry.toString());
						} catch (ErrMsgException ex1) {
							LogUtil.getLog(getClass())
									.error(StrUtil.trace(ex1));
						}
					} catch (JSONException ex) {
					}
				}
				wfs.save();
			} else
				value = "" + 0;
		} else {
			// logger.info("save: value = " + ff.getValue() + " macroType=" +
			// ff.getMacroType());
			value = ff.getValue();
		}
		return value;
	}

	public String getControlType() {
		return "text";
	}

	public String getControlValue(String userName, FormField ff) {
		return getSequenceVal(userName, ff);
	}

	public String getControlText(String userName, FormField ff) {
		return getSequenceVal(userName, ff);
	}

	public String getControlOptions(String userName, FormField ff) {
		return "";
	}

}
