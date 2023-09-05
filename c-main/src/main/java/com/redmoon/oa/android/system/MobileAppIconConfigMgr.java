package com.redmoon.oa.android.system;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.ui.menu.Leaf;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

public class MobileAppIconConfigMgr {
	public static final int TYPE_MENU = 1;
	public static final int TYPE_FLOW = 2;
	public static final int TYPE_MODULE = 3;
	public static final int TYPE_LINK = 4;
	public static final int TYPE_FRONT = 5;
	
	public static final int CAN_MOBILE_START = 1;
	private static final String SUPERVIS = "supervis";
	private static final String SUPERVIS_NAME = "部门工作";
	private static final String SUPERVIS_NAME2 = "督办";
	private static final String SUPERVIS_PARENT_CODE = "administration";
	
	public static final String CODE_QRCODE = "qrcode"; // 扫描二维码
	public static final String CODE_LIVE_PUSH = "live_push"; // 现场直播
	public static final String CODE_LIVE_PLAY = "live_play"; // 现场直播

	/**
	 * 菜单项 ID，与手机端APP一致
	 */
	public static final int OA_NOTICE = 1;//通知
	public static final int OA_FLOW = 2;//流程
	public static final int OA_DAYILY = 3;//日报
	public static final int OA_TEAM = 4;//通讯录
	public static final int OA_CRM = 5;//Crm
	public static final int OA_INNER_MSG = 6;//内部邮件
	public static final int OA_SYSTEM_MSG = 7;//系统邮件
	// public static final int OA_NETDISK = 8;//网络硬盘
	public static final int OA_SCHEDULE = 9;//日程安排
	public static final int OA_FILECASE = 10;//文件柜
	public static final int OA_LOACTION = 11;//定位签到
	public static final int OA_LEADER_WORK = 12;//领导督办
	public static final int OA_PROJECT = 13;//项目管理
	public static final int OA_TASK = 14;//任务管理
	public static final int OA_MY_SCORE = 15;//我的积分
	public static final int OA_SIGN_UP = 16;//签退

	public MobileAppIconConfigMgr() {

	}

	public boolean create(HttpServletRequest request) throws ErrMsgException,
            ResKeyException {
		int type = ParamUtil.getInt(request, "type");

		String code = "";
		if (type == 1) {
			code = ParamUtil.get(request, "type_menu_selected");
		} else if (type == 2) {
			code = ParamUtil.get(request, "type_flow_selected");
		} else if (type==3){
			code = ParamUtil.get(request, "moduleCode");
		}
		else {
			code = ParamUtil.get(request, "link");
		}

		String name = ParamUtil.get(request, "name");
		String imgUrl = ParamUtil.get(request, "imgUrl");
		Date setTime = new Date();
		int isMobileStart = ParamUtil.getInt(request, "isMobileStart", 0);
		int isAdd = ParamUtil.getInt(request, "isAdd", 0);
		int orders = ParamUtil.getInt(request, "orders");
		String icon = ParamUtil.get(request, "icon");

		MobileAppIconConfigDb mb = new MobileAppIconConfigDb();
		return mb.create(new JdbcTemplate(), new Object[] { code, name, type,
				imgUrl, setTime, isMobileStart, isAdd, orders, icon });
	}

	public boolean save(HttpServletRequest request) throws ErrMsgException,
            ResKeyException {
		int id = ParamUtil.getInt(request, "id");
		String name = ParamUtil.get(request, "name");
		String imgUrl = ParamUtil.get(request, "imgUrl");
		int isMobileStart = ParamUtil.getInt(request, "isMobileStart", 0);
		Date setTime = new Date();
		int isAdd = ParamUtil.getInt(request, "isAdd", 0);
		int orders = ParamUtil.getInt(request, "orders");
		String icon = ParamUtil.get(request, "icon");

		MobileAppIconConfigDb mb = new MobileAppIconConfigDb();
		mb = mb.getMobileAppIconConfigDb(id);
		mb.set("name", name);
		mb.set("imgUrl", imgUrl);
		mb.set("setTime", setTime);
		mb.set("isMobileStart", isMobileStart);
		mb.set("is_add", isAdd);
		mb.set("orders", orders);
		
		int type = mb.getInt("type");
		if (type==TYPE_FLOW) {
			String typeFlowSelected = ParamUtil.get(request, "type_flow_selected");
			mb.set("code", typeFlowSelected);
		}
		else if (type==TYPE_MODULE) {
			String code = ParamUtil.get(request, "type_module_selected");
			mb.set("code", code);
		}
		else if (type==TYPE_LINK || type == TYPE_FRONT) {
			String link = ParamUtil.get(request, "link");
			mb.set("code", link);			
		}
		else {
			mb.set("code", ParamUtil.get(request, "type_menu_selected"));
		}
		mb.set("icon", icon);

		return mb.save();
	}

	public boolean del(int id) throws ErrMsgException {
		boolean re = false;

		MobileAppIconConfigDb mb = new MobileAppIconConfigDb();
		mb = mb.getMobileAppIconConfigDb(id);

		try {
			re = mb.del();
		} catch (ResKeyException e) {
			throw new ErrMsgException(e.getMessage());
		}

		return re;
	}

	public boolean delBatch(HttpServletRequest request) throws ErrMsgException {
		String strids = ParamUtil.get(request, "ids");
		String[] ids = StrUtil.split(strids, ",");

		if (ids == null) {
			return false;
		}

		boolean re = false;
		for (String id : ids) {
			try {
				re = del(StrUtil.toInt(id));
			} catch (Exception e) {
				LogUtil.getLog(getClass()).error(e);
			}
		}
		return re;
	}

	public boolean isExist(HttpServletRequest request) throws ErrMsgException {
		boolean res = true;
		String sql = "select id from mobile_app_icon_config where code=? and type=?";
		int type = ParamUtil.getInt(request, "myType", -1);
		String code = "";
		if (type == 1) {
			code = ParamUtil.get(request, "type_menu_selected");
		} else if (type == 2) {
			code = ParamUtil.get(request, "type_flow_selected");
		}

		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri;
		try {
			ri = jt.executeQuery(sql, new Object[] { code, type });
			if (ri.hasNext()) {
				res = false;
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
		}

		return res;
	}

	public static String ShowMenuAsOption(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();

		int k = 2;
		com.redmoon.oa.ui.menu.LeafChildrenCacheMgr lccm = new com.redmoon.oa.ui.menu.LeafChildrenCacheMgr(
				com.redmoon.oa.ui.menu.Leaf.CODE_ROOT);
		for (Leaf lf : lccm.getChildren()) {
			if (lf.getCode().equals(Leaf.CODE_BOTTOM) || !lf.isUse()) {
				continue;
			}

			com.redmoon.oa.ui.menu.LeafChildrenCacheMgr lccm2 = new com.redmoon.oa.ui.menu.LeafChildrenCacheMgr( lf.getCode());
			Vector<Leaf> v2 = lccm2.getChildren();

			if (!"".equals(lf.getLink(request))) {
				sb.append("<option value=\"").append(lf.getCode()).append(
						"\"  class=\"a_option\" id=\"").append(lf.getName())
						.append("\">").append(lf.getName()).append("</option>");
			} else {
				sb.append("<option label='├『" + lf.getName() + "』' value=\"")
						.append(lf.getCode()).append(
						"\"  class=\"a_option\" id=\"").append(
						lf.getName()).append("\">├『").append(
						lf.getName()).append("』</option>");
			}

			if (v2.size() > 0) {
				k++;
				for (Leaf lf2 : v2) {
					if (!lf2.isUse()) {
						continue;
					}

					com.redmoon.oa.ui.menu.LeafChildrenCacheMgr lccm3 = new com.redmoon.oa.ui.menu.LeafChildrenCacheMgr(
							lf2.getCode());
					Vector<Leaf> v3 = lccm3.getChildren();
					if (v3.size() == 0) {
						sb.append("<option value=\"").append(lf2.getCode())
								.append("\"  class=\"a_option\" id=\"").append(
								lf2.getName()).append("\">　").append(
								lf2.getName()).append("</option>");
					} else {
						sb.append(
								"<option label='╋" + lf2.getName()
										+ "' value=\"").append(lf2.getCode())
								.append("\"  class=\"a_option\" id=\"").append(
								lf2.getName()).append("\">　╋").append(
								lf2.getName()).append("</option>");
						k++;
						for (Leaf lf3 : v3) {
							if (!lf3.canUserSee(request) || !lf3.isUse()) {
								continue;
							}
							sb.append("<option value=\"").append(lf3.getCode())
									.append("\"  class=\"a_option\" id=\"")
									.append(lf3.getName()).append("\">　　")
									.append(lf3.getName()).append("</option>");
						}
					}
				}
			} else {
				k++;
			}
		}

		return sb.toString();
	}

	public static String ShowFlowAsOption(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();

		String sql1 = "select code,name from flow_directory where parent_code='root' and is_open=1";
		String sql2 = "select code,name from flow_directory where parent_code=? and is_open=1 and is_mobile_start=1";
		String blank = "&nbsp;&nbsp;&nbsp;&nbsp;";

		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator it = null;
		ResultRecord rr = null;

		try {
			it = jt.executeQuery(sql1);
		} catch (SQLException e) {
			LogUtil.getLog(MobileAppIconConfigMgr.class).error(e);
		} finally {
			jt.close();
		}

		while (it.hasNext()) {
			rr = (ResultRecord) it.next();
			String code = rr.getString("code");
			String name = rr.getString("name");

			sb.append("<option value=cannot_selected_ " + code + " id =" + name
					+ ">" + "├ " + name + "</option>");

			JdbcTemplate jte = new JdbcTemplate();
			ResultIterator itr = null;
			ResultRecord rrd = null;

			try {
				itr = jte.executeQuery(sql2, new Object[] { code });
			} catch (SQLException e) {
				LogUtil.getLog(MobileAppIconConfigMgr.class).error(e);
			} finally {
				jte.close();
			}

			while (itr.hasNext()) {
				rrd = (ResultRecord) itr.next();
				String code2 = rrd.getString("code");
				String name2 = rrd.getString("name");

				sb.append("<option value=" + code2 + " id =" + name2 + ">"
						+ blank + "" + name2 + "</option>");
			}
		}
		return sb.toString();
	}

	// 获取imgUrl
	public String getImgUrl(String code, int type) {
		String imgUrl = "";
		String sql = "select imgUrl from mobile_app_icon_config where code=? and type=?";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri;
		ResultRecord rrd;
		try {
			ri = jt.executeQuery(sql, new Object[] { code, type });
			if (ri.hasNext()) {
				rrd = (ResultRecord) ri.next();
				imgUrl = rrd.getString("imgUrl");
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
		}

		return imgUrl;
	}

	/**
	 * LZM 修改 手机端 允许发起 应用
	 * 
	 * @Description:
	 * @param canSendNotice
	 * @return
	 */
	public JSONArray getMobileCanStartInfo(HttpServletRequest request, boolean canSendNotice) {
		JSONArray jsonArr = new JSONArray();
		String sql = "select id from mobile_app_icon_config where isMobileStart=" + CAN_MOBILE_START + " order by orders desc, id asc";
		try {
			MobileAppIconConfigDb mobileAppIconConfigDb = new MobileAppIconConfigDb();
			for (MobileAppIconConfigDb appIconConfigDb : (Iterable<MobileAppIconConfigDb>) mobileAppIconConfigDb.list(sql)) {
				mobileAppIconConfigDb = (MobileAppIconConfigDb) appIconConfigDb;
				int id = mobileAppIconConfigDb.getInt("id");
				String name = mobileAppIconConfigDb.getString("name");
				String imgUrl = mobileAppIconConfigDb.getString("imgUrl");
				String code = mobileAppIconConfigDb.getString("code");
				int type = mobileAppIconConfigDb.getInt("type");
				String icon = mobileAppIconConfigDb.getString("icon");
				JSONObject appJson = new JSONObject();
				appJson.put("mId", id);
				appJson.put("mName", name);
				appJson.put("imgUrl", imgUrl);
				appJson.put("code", code);
				appJson.put("type", type);
				appJson.put("icon", icon);
				if (id == 1) {
					if (canSendNotice) {
						jsonArr.put(appJson);
					}
				} else {
					if (type == TYPE_MENU) {
						if (CODE_QRCODE.equals(code) || CODE_LIVE_PUSH.equals(code) || CODE_LIVE_PLAY.equals(code)) {
							jsonArr.put(appJson);
						} else {
							Leaf lf = new Leaf();
							lf = lf.getLeaf(code);
							if (lf != null && lf.isLoaded()) {
								boolean menuCanSee = lf.canUserSee(request);
								if (menuCanSee
										&& !lf.getCode().equals(Leaf.CODE_BOTTOM)
										&& lf.isUse()) {
									appJson.put("link", lf.getLink(request));
									jsonArr.put(appJson);
								}
							}
						}
					} else {
						jsonArr.put(appJson);
					}
				}
			}
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		}

		return jsonArr;
	}

	/**
	 *LZM 应用菜单可以查看的列表数据
	 * 
	 * @Description:
	 * @param request
	 * @return
	 */
	public JSONArray getAppIcons(HttpServletRequest request) {
		JSONArray jsonArr = new JSONArray();
		String sql = "select id from mobile_app_icon_config order by orders desc, id asc";
		MobileAppIconConfigDb mobileAppIconConfigDb = new MobileAppIconConfigDb();
		try {
			Iterator<MobileAppIconConfigDb> ir = mobileAppIconConfigDb.list(sql).iterator();
			while (ir.hasNext()) {
				mobileAppIconConfigDb = ir.next();
				int id = mobileAppIconConfigDb.getInt("id");
				String name = mobileAppIconConfigDb.getString("name");
				String code = mobileAppIconConfigDb.getString("code");
				String imgUrl = mobileAppIconConfigDb.getString("imgUrl");
				int type = mobileAppIconConfigDb.getInt("type");
				int isAdd = mobileAppIconConfigDb.getInt("is_add");
				String icon = StrUtil.getNullStr(mobileAppIconConfigDb.getString("icon"));
				
				JSONObject appJson = new JSONObject();
				appJson.put("mId", id);
				appJson.put("mName", name);
				appJson.put("imgUrl", "static/" + imgUrl);
				appJson.put("code", code);
				appJson.put("type", type);
				appJson.put("isAdd", isAdd); // 看似多余，因为getMobileCanStartInfo给手机端的添加按钮中并未下发isAdd
				appJson.put("icon", icon);
				if (type == TYPE_MENU) {
					if (CODE_QRCODE.equals(code) || CODE_LIVE_PUSH.equals(code) || CODE_LIVE_PLAY.equals(code)) {
						jsonArr.put(appJson);
					}
					else {
						Leaf lf = new Leaf();
						lf = lf.getLeaf(code);
						if (lf!=null && lf.isLoaded()) {
							boolean menuCanSee = lf.canUserSee(request);
							if (menuCanSee
									&& !lf.getCode().equals(Leaf.CODE_BOTTOM)
									&& lf.isUse()) {
								appJson.put("link", lf.getLink(request));
								jsonArr.put(appJson);
							}
						}
					}
				}
				else if (type == TYPE_FLOW) {
					com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
					lf = lf.getLeaf(code);
					if (lf==null) {
						DebugUtil.e(getClass(), "getAppIcons", "流程类型 " + code + " 不存在");
						continue;
					}
					com.redmoon.oa.flow.DirectoryView dv = new com.redmoon.oa.flow.DirectoryView(lf);
					if (dv.canUserSeeWhenInitFlow(request, lf)) {
						jsonArr.put(appJson);
					}
				}
				else {
					jsonArr.put(appJson);
				}
			}
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		}

		return jsonArr;
	}

}
