package com.redmoon.oa.visual;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.WorkflowPredefineDb;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>Title: </p>
 * 
 * <p>Description: </p>
 * 20140322考虑到原来的模块以code为主键，涉及到很多的方法，因此还是以code为主键，但是增加form_code字段及kind字段，kind默认为0，表示主模块，1表示副模块
 * 当为主模块时，code等于formCode，否则为随机数
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ModuleSetupDb extends QObjectDb {
	/**
	 * 主模块
	 */
	public static final int KIND_MAIN = 0;
	/**
	 * 副模块
	 */
	public static final int KIND_SUB = 1;

	/**
	 * 默认视图
	 */
	public static final int VIEW_DEFAULT = 0;
	
	/**
	 * 甘特图
	 */
	public static final int VIEW_LIST_GANTT = 1;
	
	/**
	 * 甘特图及列表
	 */
	public static final int VIEW_LIST_GANTT_LIST = 2;
	
	/**
	 * 自定义视图
	 */
	public static final int VIEW_LIST_CUSTOM = 3;
	
	/**
	 * 列表树形视图(bootstrap风格）
	 */
	public static final int VIEW_LIST_TREE = 4;

	/**
	 * 日历看板
	 */
	public static final int VIEW_LIST_CALENDAR = 5;

	/**
	 * 日历看板及列表
	 */
	public static final int VIEW_LIST_CALENDAR_LIST = 6;

	/**
	 * 列表树形视图(jstree风格，且可维护）
	 */
	public static final int VIEW_LIST_MODULE_TREE = 7;
	
	/**
	 * 编辑自定义视图
	 */
	public static final int VIEW_EDIT_CUSTOM = 1000000;
	
	/**
	 * 显示自定义视图
	 */
	public static final int VIEW_SHOW_CUSTOM = 1000001;
	
	/**
	 * 显示树形视图
	 */
	public static final int VIEW_SHOW_TREE = 1000002;

	public ModuleSetupDb() {
		super();
	}

	public ModuleSetupDb getModuleSetupDb(String code) {
		return (ModuleSetupDb) getQObjectDb(code);
	}

	@Override
	public boolean create() throws ResKeyException, ErrMsgException {
		if (this.getInt("is_use") == 1 && !License.getInstance().isPlatformSrc() && getTotalInUsed() >= 3) {
			throw new ErrMsgException("非平台版用户最多只能体验2个智能模块！");
		}
		return super.create();
	}

	@Override
	public boolean save() throws ResKeyException {
		ModuleSetupDb msd = new ModuleSetupDb();
		msd.refreshList();
		msd = msd.getModuleSetupDb(this.getString("code"));
		if (msd.getInt("is_system") != 1 && msd.getInt("is_use") != 1 && this.getInt("is_use") == 1 && !License.getInstance().isPlatformSrc() && getTotalInUsed() >= 3) {
			throw new ResKeyException("非平台版用户最多只能体验2个智能模块！");
		}
		return super.save();
	}

	private int getTotalInUsed() {
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "select count(code) from visual_module_setup where is_use=1";
		int count = 0;
		try {
			ResultIterator ri = jt.executeQuery(sql);
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				count = rr.getInt(1);
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			e.printStackTrace();
		}
		return count;
	}

	public Vector<ModuleSetupDb> listUsed() {
		String sql = "select code from visual_module_setup where is_use=1 order by code";
		return list(sql);
	}

	public ModuleSetupDb getModuleSetupDbOrInit(String code) {
		ModuleSetupDb vsd = getModuleSetupDb(code);
		if (vsd == null) {
			try {
				FormDb fd = new FormDb();
				fd = fd.getFormDb(code);
				String listField = "", listFieldWidth = "", queryField = "";
				String listFieldOrder = "", listFieldLink = "";
				String msgProp = "", validateProp="", validateMsg="";
				
				int viewList = VIEW_DEFAULT;
				String fieldBeginDate = "''";
				String fieldEndDate = "''";

				// 取前6个字段置入列表，不含id, cws_creator
				int c = 0;
				Iterator ir = fd.getFields().iterator();
				while (ir.hasNext()) {
					FormField ff = (FormField) ir.next();
					if (listField.equals("")) {
						listField = ff.getName();
						listFieldWidth = "150";
						listFieldOrder = String.valueOf(c);
						listFieldLink = "#";
					} else {
						listField += "," + ff.getName();
						listFieldWidth += "," + "150";
						listFieldOrder += "," + String.valueOf(c);
						listFieldLink += "," + "#";
					}

					c++;
					if (c == 6) {
						break;
					}
				}
				
				create(new JdbcTemplate(), new Object[] { code, listField,
						queryField, fd.getName(), new Integer(KIND_MAIN), code,
						listFieldWidth, listFieldOrder, listFieldLink, msgProp, validateProp, validateMsg,
						viewList, fieldBeginDate, fieldEndDate, "", "", "", "", "", ""});
				
				// LogUtil.getLog(getClass()).info("re=" + re);
			} catch (ResKeyException e) {
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			}

			LogUtil.getLog(getClass()).info("vsd=" + getModuleSetupDb(code));

			return getModuleSetupDb(code);
		} else {
			return vsd;
		}
	}

	public String getCss(String pageType) {
		String beginStr = "//[" + pageType + "_begin]\r\n";

		String scripts = StrUtil.getNullStr(getString("css"));
		int b = scripts.indexOf(beginStr);
		if (b == -1) {
			beginStr = "//[" + pageType + "_begin]\n";
			b = scripts.indexOf(beginStr);
		}
		if (b != -1) {
			String endStr = "//[" + pageType + "_end]\r\n";
			int e = scripts.indexOf(endStr);
			if (e == -1) {
				endStr = "//[" + pageType + "_end]\n";
				e = scripts.indexOf(endStr);
			}
			if (e != -1) {
				return scripts.substring(b + beginStr.length(), e);
			}
		}
		return null;
	}

	public boolean saveCss(String pageType, String pageCss) throws ErrMsgException, ResKeyException {
		String css = StrUtil.getNullStr(getString("css"));
		String beginStr = "//[" + pageType + "_begin]\r\n";
		String endStr = "//[" + pageType + "_end]\r\n";

		int b = css.indexOf(beginStr);
		int e = css.indexOf(endStr);
		if (b == -1 || e == -1) {
			css += "\r\n" + beginStr;
			css += pageCss;
			css += "\r\n" + endStr;
		} else {
			String str = css.substring(0, b);
			str += "\r\n" + beginStr;
			str += pageCss;
			str += "\r\n" + css.substring(e);

			css = str;
		}
		set("css", css);
		return save();
	}

	public String getScript(String eventType) {
		String beginStr = "//[" + eventType + "_begin]\r\n";

		String scripts = StrUtil.getNullStr(getString("scripts"));
		int b = scripts.indexOf(beginStr);
		if (b == -1) {
			beginStr = "//[" + eventType + "_begin]\n";
			b = scripts.indexOf(beginStr);
		}
		if (b != -1) {
			String endStr = "//[" + eventType + "_end]\r\n";
			int e = scripts.indexOf(endStr);
			if (e == -1) {
				endStr = "//[" + eventType + "_end]\n";
				e = scripts.indexOf(endStr);
			}
			if (e != -1) {
				return scripts.substring(b + beginStr.length(), e);
			}
		}
		return null;
	}
	
	public boolean saveScript(String eventType, String script) throws ErrMsgException, ResKeyException {
		if (!License.getInstance().isSrc()) {
			throw new ErrMsgException("开发版才有脚本编写功能！");
		}
	    
		// 检查
		if (eventType.equals("validate")) {
			if (!script.equals("") && script.indexOf("ret") == -1) {
				throw new ErrMsgException("请添加返回值，ret=true或者false");
			}
		}

		String scripts = StrUtil.getNullStr(getString("scripts"));
		String beginStr = "//[" + eventType + "_begin]\r\n";
		String endStr = "//[" + eventType + "_end]\r\n";

		int b = scripts.indexOf(beginStr);
		int e = scripts.indexOf(endStr);
		if (b == -1 || e == -1) {
			scripts += "\r\n" + beginStr;
			scripts += script;
			scripts += "\r\n" + endStr;
		} else {
			String str = scripts.substring(0, b);
			str += "\r\n" + beginStr;
			str += script;
			str += "\r\n" + scripts.substring(e);

			scripts = str;
		}
		set("scripts", scripts);
		return save();
	}

	/**
	 * 取得模块的列的属性
	 * @param isIncludeHide true则过滤掉不显示的列
	 * @param fieldName
	 * @return
	 */
	public String[] getColAry(boolean isIncludeHide, String fieldName) {
		String str = StrUtil.getNullStr(getString(fieldName));
		String listField = StrUtil.getNullStr(getString("list_field"));
		String[] aryField = StrUtil.split(listField, ",");

		String[] ary = StrUtil.split(str, ",");
		boolean isNeedRepair = false;
		if (ary == null) {
		    if (!"list_field".equals(fieldName)) {
                if (!"".equals(listField)) {
					isNeedRepair = true;
                }
                else {
                    return new String[0];
                }
            }
            else {
                return new String[0];
            }
		}
		else {
			if (ary.length!=aryField.length) {
				isNeedRepair = true;
			}
		}

		if (isNeedRepair) {
			ary = new String[aryField.length];
			for (int i = 0; i < aryField.length; i++) {
				if ("list_field_show".equals(fieldName)) {
					ary[i] = "1";
				}
				else if ("list_field_align".equals(fieldName)) {
					ary[i] = "center";
				}
				else {
					ary[i] = "#";
				}
			}
		}

		if (isIncludeHide) {
			return ary;
		}

		String listFieldShow = StrUtil.getNullStr(getString("list_field_show"));
		// list_field_show是后来新增的，所以要检查并初始化以兼容之前的版本
		if ("".equals(listFieldShow)) {
			return ary;
		}
		String[] fieldsShow = StrUtil.split(listFieldShow, ",");
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<ary.length; i++) {
			if ("1".equals(fieldsShow[i])) {
				StrUtil.concat(sb, ",", ary[i]);
			}
		}
		return StrUtil.split(sb.toString(), ",");
	}

	public String getCode() {
		return getString("code");
	}

	public String getName() {
		return getString("name");
	}

	public boolean isReloadAfterUpdate() {
		String pageSetup = getString("page_setup");
		if (StringUtils.isEmpty(pageSetup)) {
			return true;
		}
		com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(pageSetup);
		com.alibaba.fastjson.JSONObject editJson = jsonObject.getJSONObject("editPage");
		boolean re = true;
		if (editJson != null) {
			re = editJson.getBoolean("isReloadAfterUpdate");
		}
		return re;
	}

	public boolean isEditPageTabStyleHor() {
		String pageSetup = getString("page_setup");
		if (StringUtils.isEmpty(pageSetup)) {
			return true;
		}
		com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(pageSetup);
		com.alibaba.fastjson.JSONObject editJson = jsonObject.getJSONObject("editPage");
		boolean re = true;
		if (editJson != null) {
			re = editJson.getBoolean("isTabStyleHor");
		}
		return re;
	}

	public boolean isShowPageTabStyleHor() {
		String pageSetup = getString("page_setup");
		if (StringUtils.isEmpty(pageSetup)) {
			return true;
		}
		com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(pageSetup);
		com.alibaba.fastjson.JSONObject editJson = jsonObject.getJSONObject("showPage");
		boolean re = true;
		if (editJson != null) {
			re = editJson.getBoolean("isTabStyleHor");
		}
		return re;
	}

	public int getPageStyle() {
		int pageStyle = ConstUtil.PAGE_STYLE_DEFAULT;

		String pageSetup = getString("page_setup");
		if (StringUtils.isEmpty(pageSetup)) {
			return pageStyle;
		}
		com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(pageSetup);
		com.alibaba.fastjson.JSONObject commonPage = jsonObject.getJSONObject("commonPage");
		if (commonPage != null) {
			pageStyle = commonPage.getIntValue("pageStyle");
		}
		return pageStyle;
	}

	public JSONArray getBtnProps(String pageType) {
		String pageSetup = getString("page_setup");
		if (StringUtils.isEmpty(pageSetup)) {
			pageSetup = "{}";
		}
		JSONArray ary = new JSONArray();
		com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(pageSetup);
		String pType;
		if (ConstUtil.PAGE_TYPE_SHOW.equals(pageType)) {
			pType = "showPage";
		}
		else {
			pType = "editPage";
		}
		com.alibaba.fastjson.JSONObject showJson = jsonObject.getJSONObject(pType); // showPage editPage
		JSONArray btnProps = null;
		if (showJson.containsKey("btnProps")) {
			btnProps = showJson.getJSONArray("btnProps");
		}
		return btnProps;
	}

	public JSONArray getButtons(HttpServletRequest request, String pageType, FormDAO fdao, int isShowNav) {
		String moduleCode = getString("code");
		Privilege privilege = new Privilege();
		String pageSetup = getString("page_setup");
		if (StringUtils.isEmpty(pageSetup)) {
			pageSetup = "{}";
		}
		JSONArray ary = new JSONArray();
		com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(pageSetup);
		String pType;
		if (ConstUtil.PAGE_TYPE_SHOW.equals(pageType)) {
			pType = "showPage";
		}
		else {
			pType = "editPage";
		}
		com.alibaba.fastjson.JSONObject pageJson = jsonObject.getJSONObject(pType); // showPage editPage
		JSONArray btnProps = null;
		if (pageJson!=null && pageJson.containsKey("btnProps")) {
			btnProps = pageJson.getJSONArray("btnProps");
			for (int i = 0; i < btnProps.size(); i++) {
				com.alibaba.fastjson.JSONObject json = btnProps.getJSONObject(i);
				boolean enabled = true;
				if (json.containsKey("enabled")) {
					enabled = json.getBoolean("enabled");
				}
				if (!enabled) {
					continue;
				}

				String linkId = json.getString("id");
				String linkName = StrUtil.getNullStr(json.getString("name"));
				String linkCond = StrUtil.getNullStr(json.getString("cond"));
				String linkValue = StrUtil.getNullStr(json.getString("value"));
				String linkEvent = StrUtil.getNullStr(json.getString("event"));
				String linkRole = StrUtil.getNullStr(json.getString("role"));
				String linkHref = StrUtil.getNullStr(json.getString("href"));
				String title = StrUtil.getNullStr(json.getString("title"));

				// 检查是否拥有权限
				if (!privilege.isUserPrivValid(request, Privilege.ADMIN)) {
					boolean canSeeLink = false;
					if (!"".equals(linkRole)) {
						String[] codeAry = StrUtil.split(linkRole, ",");
						if (codeAry != null) {
							UserDb user = new UserDb();
							user = user.getUserDb(privilege.getUser(request));
							RoleDb[] rdAry = user.getRoles();
							if (rdAry != null) {
								for (RoleDb rd : rdAry) {
									String roleCode = rd.getCode();
									for (String codeAllowed : codeAry) {
										if (roleCode.equals(codeAllowed)) {
											canSeeLink = true;
											break;
										}
									}
								}
							}
						} else {
							canSeeLink = true;
						}
					} else {
						canSeeLink = true;
					}

					if (!canSeeLink) {
						continue;
					}
				}

				if (!ModuleUtil.isLinkShow(request, this, fdao, linkCond, "", linkValue)) {
					continue;
				}

				// 当有编辑或管理权限时，编辑按钮才出现
				if (ConstUtil.BTN_EDIT.equals(linkId)) {
					ModulePrivDb mpd = new ModulePrivDb(moduleCode);
					if (!mpd.canUserModify(privilege.getUser(request)) && !mpd.canUserManage(privilege.getUser(request))) {
						continue;
					}
				}

				com.alibaba.fastjson.JSONObject jsonObj = new JSONObject();
				jsonObj.put("id", linkId);
				jsonObj.put("name", linkName);
				switch (linkId) {
					case ConstUtil.BTN_PRINT:
						linkEvent = "click";
						jsonObj.put("href", "showFormReport()");
						jsonObj.put("target", "newTab");
						if ("".equals(linkName)) {
							jsonObj.put("name", ModuleUtil.getBtnDefaultName(linkId));
						}
						break;
					case ConstUtil.BTN_EDIT:
						jsonObj.put("href", "module_edit.jsp?parentId=" + fdao.getId() + "&id=" + fdao.getId() + "&isShowNav=" + isShowNav + "&code=" + moduleCode);
						jsonObj.put("target", "curTab");
						if ("".equals(linkName)) {
							jsonObj.put("name", ModuleUtil.getBtnDefaultName(linkId));
						}
						break;
					case ConstUtil.BTN_OK:
						linkEvent = "click";
						jsonObj.put("href", "");
						jsonObj.put("target", "curTab");
						if ("".equals(linkName)) {
							jsonObj.put("name", ModuleUtil.getBtnDefaultName(linkId));
						}
						break;
					default:
						jsonObj.put("target", "newTab");
						int flag = 1;
						jsonObj.put("href", ModuleUtil.renderLinkUrl(request, fdao, linkHref, linkId, moduleCode, flag, pageType));
						break;
				}
				jsonObj.put("event", linkEvent);
				jsonObj.put("title", title);
				ary.add(jsonObj);
			}
		}
		return ary;
	}
}
