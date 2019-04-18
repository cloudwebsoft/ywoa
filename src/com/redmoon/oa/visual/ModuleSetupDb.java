package com.redmoon.oa.visual;

import java.sql.SQLException;
import java.util.Iterator;

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
	 * 列表树形视图
	 */
	public static final int VIEW_LIST_TREE = 4;
	
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

	public boolean create() throws ResKeyException, ErrMsgException {
		if (this.getInt("is_use") == 1 && !License.getInstance().isPlatformSrc() && getTotalInUsed() >= 3) {
			throw new ErrMsgException("非平台版用户最多只能体验2个智能模块！");
		}
		return super.create();
	}

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
		}
		return count;
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
					if (c == 6)
						break;
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
		} else
			return vsd;
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

}
