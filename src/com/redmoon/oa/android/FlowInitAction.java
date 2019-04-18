package com.redmoon.oa.android;

import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.oa.visual.ModuleSetupDb;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormMacroCtl;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormParser;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.MatchUserException;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.WorkflowMgr;
import com.redmoon.oa.flow.WorkflowPredefineDb;
import com.redmoon.oa.flow.WorkflowRuler;
import com.redmoon.oa.flow.WorkflowUtil;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.flow.macroctl.ModuleFieldSelectCtl;
import com.redmoon.oa.flow.macroctl.NestSheetCtl;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;

public class FlowInitAction {
	private final static String OFFICE_EQUIPMENT = "office_equipment";
	private final static String RELATE_SELECT = "relate_select";//联动选择 办公用品 库存的
	private String skey = "";
	private String result = "";
	private String code = "";
	private String type = "";
	private String title = "";
	private String mutilDept = "";
	

	public String getMutilDept() {
		return mutilDept;
	}

	public void setMutilDept(String mutilDept) {
		this.mutilDept = mutilDept;
	}

	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * 
	 * @return
	 */
	public String execute() {
		JSONObject json = new JSONObject();
		JSONArray fields = new JSONArray();
		JSONObject result = new JSONObject();
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(getSkey());
		String res = "0";
		String msg = "操作成功";
		long startActionId = 0;
		boolean hasAttach = true;
		try {
			if (re) {
				res = "-2";
				msg = "时间过期";
				json.put("res", res);
				json.put("msg", msg);
				setResult(json.toString());
				return "SUCCESS";
			}
			HttpServletRequest request = ServletActionContext.getRequest();
			privilege.doLogin(request, getSkey());
			// 加入对默认标题的处理 fgf 2015-1-2
			com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
			lf = lf.getLeaf(getCode());
			com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
			String flowTitle = WorkflowMgr.makeTitle(request, pvg, lf);
			MacroCtlMgr mm = new MacroCtlMgr();

			WorkflowMgr wm = new WorkflowMgr();
			if (StrUtil.toInt(getType()) == Leaf.TYPE_FREE) {
				startActionId = wm.initWorkflowFree(privilege
						.getUserName(getSkey()), getCode(), flowTitle, -1, 0);
			} else {
				startActionId = wm.initWorkflow(privilege
						.getUserName(getSkey()), getCode(), flowTitle, -1, 0);
			}
			MyActionDb mad = new MyActionDb();
			mad = mad.getMyActionDb(startActionId);
			
			long flowId = mad.getFlowId();
			WorkflowDb wf = new WorkflowDb();
			wf = wf.getWorkflowDb((int) flowId);

			FormDb fd = new FormDb();
			fd = fd.getFormDb(lf.getFormCode());
			hasAttach = fd.isHasAttachment();

			FormDAO fdao = new FormDAO();
			fdao = fdao.getFormDAO(wf.getId(), fd);

			Vector v = fdao.getFields();
			Iterator ir = v.iterator();
			// 先自动映射
			while (ir.hasNext()) {
				FormField ff = (FormField) ir.next();
				if (ff.getType().equals("macro")) {
					MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
					if (mu == null) {
						LogUtil.getLog(getClass()).error(
								"MactoCtl " + ff.getTitle() + "："
										+ ff.getMacroType() + " is not exist.");
						continue;
					}

					String macroCode = mu.getCode();
					if (macroCode.equals("module_field_select")) {
						String strDesc = StrUtil.getNullStr(ff.getDescription());
						// 向下兼容
						if ("".equals(strDesc)) {
							strDesc = ff.getDefaultValueRaw();
						}
						strDesc = ModuleFieldSelectCtl.formatJSONStr(strDesc);
						try {
							JSONObject jsonField = new JSONObject(strDesc);
							String value = StrUtil.getNullStr(ff.getValue());
							if (value.equals("") || value.equals(ff.getDefaultValueRaw()) || value.equals(ff.getDescription())) {
								if (json.has("requestParam") && !"".equals(json.getString("requestParam"))) {
									// 来自于指定的参数名称
									value = ParamUtil.get(request, json.getString("requestParam"));
								} else {
									// 默认以字段名作为参数从request中获取
									value = ParamUtil.get(request, ff.getName());
								}
								// 如果value中存在值，则说明需自动映射
								if (!"".equals(value)) {
									ModuleFieldSelectCtl mfsc = new ModuleFieldSelectCtl();
									mfsc.autoMap(request, (int)flowId, value, ff);
								}
							}
						}
						catch (JSONException e) {
							e.printStackTrace();
						}


					}
				}
			}
			
			// 重新再载入一次，以免缓存问题
			fdao.load();
			
			UserMgr um = new UserMgr();
			// 这段用来验证字段是否可写
			WorkflowActionDb wa = new WorkflowActionDb();
			int actionId = (int) mad.getActionId();
			wa = wa.getWorkflowActionDb(actionId);
			if (wa == null || !wa.isLoaded()) {
				res = "-1";
				msg = "流程中的相应动作不存在";
				json.put("res", res);
				json.put("msg", msg);
				setResult(json.toString());
				return "SUCCESS";
			}
			// 取可写表单域
			String fieldWrite = StrUtil.getNullString(wa.getFieldWrite())
					.trim();
			String[] fds = fieldWrite.split(",");
			int len = fds.length;

			String userName = privilege.getUserName(getSkey());

			// 自由流程根据用户所属的角色，得到可写表单域
			if (lf.getType() == Leaf.TYPE_FREE) {
				WorkflowPredefineDb wfpd = new WorkflowPredefineDb();
				wfpd = wfpd.getPredefineFlowOfFree(wf.getTypeCode());

				fds = wfpd.getFieldsWriteOfUser(wf, userName);
				len = fds.length;
			}

			String fieldHide = StrUtil.getNullString(wa.getFieldHide()).trim();
			String[] fdsHide = fieldHide.split(",");
			int lenHide = fdsHide.length;

			MacroCtlUnit mu;
			ir = v.iterator();
			json.put("res", "0");
			json.put("msg", "操作成功");
			json.put("sender", um.getUserDb(wf.getUserName()).getRealName());
			json.put("cwsWorkflowTitle", wf.getTitle());
			json.put("actionId", String.valueOf(actionId));
			json.put("myActionId", String.valueOf(startActionId));
			json.put("flowId", String.valueOf(flowId));
			json.put("url", "public/flow_dispose_do.jsp");
			
			// fgf 20180814 显示控制
			String viewJs = WorkflowUtil.doGetViewJSMobile(request, fd, fdao, userName, false);
			json.put("viewJs", viewJs);
			
			WorkflowPredefineDb wfd = new WorkflowPredefineDb();
			wfd = wfd.getPredefineFlowOfFree(wf.getTypeCode());
			boolean isLight = wfd.isLight();
			json.put("isLight", isLight);// 判断时候是@liuchen
		
			boolean canDel = false;
			com.redmoon.oa.flow.FlowConfig conf = new com.redmoon.oa.flow.FlowConfig();			
			if(conf.getIsDisplay("FLOW_BUTTON_DEL")){
				String flag = wa.getFlag();				
				if (flag.length()>=4 && flag.substring(3, 4).equals("1") && mad.getActionStatus()!=WorkflowActionDb.STATE_RETURN) {
					if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {
						canDel = true;
					}
				}
			}
			json.put("canDel", canDel);			
			
			// 结束流程标志
			boolean canFinishAgree = false;
			if(conf.getIsDisplay("FLOW_BUTTON_FINISH")){
				String flag = wa.getFlag();				
				if (wf.isStarted() && flag.length()>=12 && flag.substring(11, 12).equals("1")) {
					if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {
						canFinishAgree = true;
					}
				}
			}
			json.put("canFinishAgree", canFinishAgree);
			
			// 遍历表单字段-------------------------------------------------
			ir = v.iterator();
			while (ir.hasNext()) {
				FormField ff = (FormField) ir.next();

				// 置可写表单域
				boolean finded = false;
				for (int i = 0; i < len; ++i) {
					if (ff.getName().equals(fds[i])) {
						finded = true;
						break;
					}
				}
				if (!(finded)) {
					ff.setEditable(false);
				}

				// 如果不是自由流程
				if (lf.getType() != Leaf.TYPE_FREE) {
					// 置隐藏表单域
					finded = false;
					for (int i = 0; i < lenHide; ++i) {
						if (ff.getName().equals(fdsHide[i])) {
							finded = true;
							break;
						}
					}
					if (finded) {
						LogUtil.getLog(getClass()).info(
								"field:" + ff.getTitle() + " is hidden.");
						ff.setHidden(true);
					}
				}

				JSONObject field = new JSONObject();
				String desc = StrUtil.getNullStr(ff.getDescription());
				field.put("title", ff.getTitle());
				field.put("code", ff.getName());
				field.put("desc", desc);

				// 如果是计算控件，则取出精度和四舍五入属性
				if (ff.getType().equals(FormField.TYPE_CALCULATOR)) {
					FormParser fp = new FormParser();
					String isroundto5 = fp.getFieldAttribute(fd, ff,
							"isroundto5");
					String digit = fp.getFieldAttribute(fd, ff, "digit");
					field.put("formula", ff.getDefaultValueRaw());
					field.put("isroundto5", isroundto5);
					field.put("digit", digit);
				}
				String options = "";
				String macroType = "";
				String controlText = "";
				String controlValue = "";
				JSONArray opinionArr = null;
				JSONObject opinionVal = null;

				String macroCode = "";

				String metaData = "";

				JSONArray js = new JSONArray();
				if (ff.getType().equals("macro")) {
					mu = mm.getMacroCtlUnit(ff.getMacroType());
					if (mu == null) {
						LogUtil.getLog(getClass()).error(
								"MactoCtl " + ff.getTitle() + "："
										+ ff.getMacroType() + " is not exist.");
						continue;
					}
					
					IFormMacroCtl ifmc = mu.getIFormMacroCtl();
					ifmc.setFlowFormDAO(fdao);

					macroCode = mu.getCode();

					macroType = mu.getIFormMacroCtl().getControlType();
					controlText = mu.getIFormMacroCtl().getControlText(
							privilege.getUserName(getSkey()), ff);
					controlValue = mu.getIFormMacroCtl().getControlValue(
							privilege.getUserName(getSkey()), ff);

					// 须放在此位置，因为在当前用户宏控件的getContextValue中更改了ff的值
					metaData = mu.getIFormMacroCtl().getMetaData(ff);

					options = ifmc.getControlOptions(
							privilege.getUserName(getSkey()), ff);
					// options = options.replaceAll("\\\"", "");
					if (options != null && !options.equals("")) {
						// options = options.replaceAll("\\\"", "");
						js = new JSONArray(options);
					}
				} else {
					String type = ff.getType();
					if (type != null && !type.equals("")) {
						if (type.equals("DATE") || type.equals("DATE_TIME")) {
							// 有可能已经通过表单域选择宏控件传值映射带过来了值
							if (ff.getValue()!=null && !"".equals(ff.getValue())) {
								controlValue = ff.getValue();
							}
							else {
								controlValue = ff.getDefaultValueRaw();
							}
						} else {
							if (ff.getValue()!=null && !"".equals(ff.getValue())) {
								controlValue = ff.getValue();
							}
							else {
								controlValue = ff.getDefaultValue();
							}
						}
					} else {
						controlValue = ff.getDefaultValue();
					}
				}
				// 判断是否为意见输入框
				if (macroCode != null && !macroCode.equals("")) {
					if (macroCode.equals("macro_opinion")||macroCode.equals("macro_opinionex")) {
						if (controlText != null
								&& !controlText.trim().equals("")) {
							opinionArr = new JSONArray(controlText);
						}
						if (controlValue != null
								&& !controlValue.trim().equals("")) {
							opinionVal = new JSONObject(controlValue);
						}
					}
					
					if (macroCode.equals("nest_sheet") || macroCode.equals("nest_table") || macroCode.equals("macro_detaillist_ctl")) {
						JSONObject jsonObj = NestSheetCtl.getCtlDesc(ff);
						if (jsonObj!=null) {
							field.put("desc", jsonObj);
						}
					}
					else if (macroCode.equals("module_field_select")) {
						JSONObject jsonObj = ModuleFieldSelectCtl.getCtlDesc(ff);
						if (jsonObj!=null) {
							field.put("desc", jsonObj);
						}
					}
				}
				field.put("type", ff.getType());
				if (ff.getType().equals("select")) {
					// options = fp.getOptionsOfSelect(fd, ff);
					String[][] optionsArray = FormParser
							.getOptionsArrayOfSelect(fd, ff);
					for (int i = 0; i < optionsArray.length; i++) {
						String[] optionsItem = optionsArray[i];
						JSONObject option = new JSONObject();
						try {
							option.put("value", optionsItem[0]);
							option.put("name", optionsItem[1]);
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						js.put(option);
					}
				} else if (ff.getType().equals("radio")) {
					// options = fp.getOptionsOfSelect(fd, ff);
					String[] optionsArray = FormParser.getValuesOfInput(fd, ff);
					for (int i = 0; i < optionsArray.length; i++) {
						JSONObject option = new JSONObject();
						option.put("value", optionsArray[i]);
						option.put("name", optionsArray[i]);
						js.put(option);
					}
				}
				field.put("options", js);
				field.put("text", controlText);
				String level = "";
				if (ff.getType().equals("checkbox")) {
					// level = "个人兴趣";
					level = ff.getTitle();
				}
				field.put("level", level);
				if(macroType.equals("select") && desc.equals(OFFICE_EQUIPMENT)){
					field.put("macroType", RELATE_SELECT);
				}else{
					field.put("macroType", macroType);
				}
				field.put("editable", String.valueOf(ff.isEditable()));
				field.put("isHidden", String.valueOf(ff.isHidden()));
				field.put("isNull", String.valueOf(ff.isCanNull()));
				field.put("fieldType", ff.getFieldTypeDesc());
				if (opinionVal != null) {
					field.put("value", opinionVal);
				} else {
					field.put("value", controlValue);
				}
				if (opinionArr != null && opinionArr.length() > 0) {
					field.put("text", opinionArr);
				} else {
					field.put("text", controlText);
				}
				field.put("macroCode", macroCode);

				// 可传SQL控件条件中的字段
				field.put("metaData", metaData);
				field.put("isReadonly", ff.isReadonly());

				fields.put(field);
			}
			// 遍历表单字段---------------------------------------------------------

			// 置异或发散
			//lzm
			StringBuffer condBuf = new StringBuffer();
			boolean flagXorRadiate = wa.isXorRadiate();
			Vector vMatched = null;
			if (flagXorRadiate) {
				  vMatched = wa.matchNextBranch(wa,privilege
							.getUserName(getSkey()),condBuf,startActionId);
				  String conds = condBuf.toString();
				  boolean hasCond = conds.equals("")?false:true; // 是否含有条件 
				  if(hasCond){
					  flagXorRadiate = true;
				  }else{
					  flagXorRadiate = false;
				  }
				
			}
			json.put("flagXorRadiate", String.valueOf(flagXorRadiate));
			WorkflowRuler wr = new WorkflowRuler();
			// 取得下一步提交的用户--------------------------------------------------
			JSONArray users = new JSONArray();
			Vector vto = wa.getLinkToActions();
			Iterator toir = vto.iterator();
			Iterator userir = null;
			while (toir.hasNext()) {
				WorkflowActionDb towa = (WorkflowActionDb) toir.next();
				if (towa.getJobCode().equals(
						WorkflowActionDb.PRE_TYPE_USER_SELECT)) {
					JSONObject user = new JSONObject();
					user.put("actionTitle", towa.getTitle());
					user.put("roleName", towa.getJobName());
					user.put("internalname", towa.getInternalName());
					user.put("name", "WorkflowAction_" + towa.getId());
					user.put("value", WorkflowActionDb.PRE_TYPE_USER_SELECT);
					user.put("realName", "自选用户");
					user.put("isSelectable", "true");
					// 标志位，能否选择用户
					boolean canSelUser = wr.canUserSelUser(request, towa);
					user.put("canSelUser", String.valueOf(canSelUser));
					
					boolean isStragegyGoDown = towa.isStrategyGoDown(); // 是否为下达
					user.put("isGoDown", String.valueOf(isStragegyGoDown));
					
					users.put(user);
				} else {
					Vector vuser = towa.matchActionUser(request, towa, wa, false,mutilDept);
					userir = vuser.iterator();
					boolean isStrategySelectable = towa.isStrategySelectable();
					boolean isStrategySelected = towa.isStrategySelected();
					
					while (userir != null && userir.hasNext()) {
						UserDb ud = (UserDb) userir.next();
						JSONObject user = new JSONObject();
						user.put("actionTitle", towa.getTitle());
						user.put("roleName", towa.getJobName());
						user.put("internalname", towa.getInternalName());
						user.put("name", "WorkflowAction_" + towa.getId());
						user.put("value", ud.getName());
						user.put("realName", ud.getRealName());
						user.put("isSelectable", String.valueOf(isStrategySelectable));
						user.put("isSelected", String.valueOf(isStrategySelected));

						// 标志位，能否选择用户
						boolean canSelUser = wr.canUserSelUser(request, towa);
						user.put("canSelUser", String.valueOf(canSelUser));
						// System.out.println(getClass() +
						// " 3 actionUserRealName=" + towa.getUserRealName() +
						// " canSelUser=" + canSelUser);
						users.put(user);
					}
				}
			}
			result.put("users", users);
			

		} catch (JSONException e) {
			res = "-1";
			msg = "JSON解析异常";
			Logger.getLogger(FlowInitAction.class).error(e.getMessage());
		} catch (ErrMsgException e1) {
			res = "-1";
			msg = e1.getMessage();
			Logger.getLogger(FlowInitAction.class).error(e1.getMessage());
		} catch (MatchUserException e2) {
			res = "0";
			msg = "手机端兼职处理";
			String userName = privilege.getUserName(skey);
			DeptUserDb dud = new DeptUserDb();
			Vector vu = dud.getDeptsOfUser(userName);
			Iterator irdu = vu.iterator();
			JSONArray deptArr = new JSONArray();
			try {
				while (irdu.hasNext()) {
					DeptDb dept = (DeptDb) irdu.next();
					if (dept.isHide()) {
						continue;
					}
					String code = dept.getCode();
					String name = dept.getName();
					if (!dept.getParentCode().equals(DeptDb.ROOTCODE) && !dept.getCode().equals(DeptDb.ROOTCODE)) {
						name = dept.getDeptDb(dept.getParentCode()).getName() + "->" + dept.getName();
					}
					JSONObject deptObj = new JSONObject();
					deptObj.put("name", name);
					deptObj.put("code", code);
					deptArr.put(deptObj);
				}
				result.put("multiDepts", deptArr);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Logger.getLogger(FlowInitAction.class).error(e2.getMessage());
			}

		} finally {
			try {
				json.put("res", res);
				json.put("msg", msg);
				result.put("fields", fields);
				json.put("result", result);
				json.put("hasAttach", hasAttach);					

			} catch (final JSONException e) {
				// TODO Auto-generated catch block
				Logger.getLogger(FlowInitAction.class).error(e.getMessage());
			}
			setResult(json.toString());
		}
		
		return "SUCCESS";

	}
	
}
