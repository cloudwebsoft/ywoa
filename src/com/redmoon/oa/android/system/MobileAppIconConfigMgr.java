package com.redmoon.oa.android.system;

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.ui.menu.Leaf;
import com.redmoon.oa.ui.menu.MenuController;

public class MobileAppIconConfigMgr {
	public static final int TYPE_MENU = 1;
	public static final int TYPE_FLOW = 2;
	public static final int TYPE_MODULE = 3;
	public static final int TYPE_LINK = 4;
	
	public static final int CAN_MOBILE_START = 1;
	private static final String SUPERVIS = "supervis";
	private static final String SUPERVIS_NAME = "部门工作";
	private static final String SUPERVIS_NAME2 = "督办";
	private static final String SUPERVIS_PARENT_CODE = "administration";
	
	public static final String CODE_QRCODE = "qrcode"; // 扫描二维码
	public static final String CODE_LIVE_PUSH = "live_push"; // 现场直播
	public static final String CODE_LIVE_PLAY = "live_play"; // 现场直播

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

		MobileAppIconConfigDb mb = new MobileAppIconConfigDb();
		return mb.create(new JdbcTemplate(), new Object[] { code, name, type,
				imgUrl, setTime, isMobileStart, isAdd, orders });
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
			String type_flow_selected = ParamUtil.get(request, "type_flow_selected");
			mb.set("code", type_flow_selected);
		}
		if (type==TYPE_MODULE) {
			String code = ParamUtil.get(request, "type_module_selected");
			mb.set("code", code);
		}
		else if (type==TYPE_LINK) {
			String link = ParamUtil.get(request, "link");
			mb.set("code", link);			
		}
		else {
			mb.set("code", ParamUtil.get(request, "type_menu_selected"));
		}

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

	public void delBatch(HttpServletRequest request) throws ErrMsgException {
		String strids = ParamUtil.get(request, "ids");
		String[] ids = StrUtil.split(strids, ",");

		if (ids == null) {
			return;
		}

		int len = ids.length;
		for (int i = 0; i < len; i++) {
			try {
				del(StrUtil.toInt(ids[i]));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isExist(HttpServletRequest request) throws ErrMsgException {
		boolean res = true;
		String sql = "select id from mobile_app_icon_config where code=? and type=?";

		int type = ParamUtil.getInt(request, "type");

		String code = "";
		if (type == 1) {
			code = ParamUtil.get(request, "type_menu_selected");
		} else if (type == 2) {
			code = ParamUtil.get(request, "type_flow_selected");
		}

		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;

		try {
			ri = jt.executeQuery(sql, new Object[] { code, type });
			if (ri.hasNext()) {
				res = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			jt.close();
		}

		return res;
	}

	public static String ShowMenuAsOption(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();

		com.redmoon.oa.ui.menu.LeafChildrenCacheMgr lccm = new com.redmoon.oa.ui.menu.LeafChildrenCacheMgr(
				com.redmoon.oa.ui.menu.Leaf.CODE_ROOT);
		Iterator ir = lccm.getChildren().iterator();

		int k = 2;
		while (ir.hasNext()) {
			com.redmoon.oa.ui.menu.Leaf lf = (com.redmoon.oa.ui.menu.Leaf) ir
					.next();
			if (lf.getCode().equals(com.redmoon.oa.ui.menu.Leaf.CODE_BOTTOM)
					|| !lf.isUse()) {
				continue;
			}

			com.redmoon.oa.ui.menu.LeafChildrenCacheMgr lccm2 = new com.redmoon.oa.ui.menu.LeafChildrenCacheMgr(
					lf.getCode());
			Vector v2 = lccm2.getChildren();

			if (!lf.getLink(request).equals("")) {
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
				Iterator ir2 = v2.iterator();
				while (ir2.hasNext()) {
					com.redmoon.oa.ui.menu.Leaf lf2 = (com.redmoon.oa.ui.menu.Leaf) ir2
							.next();
					if (!lf2.isUse()) {
						continue;
					}

					com.redmoon.oa.ui.menu.LeafChildrenCacheMgr lccm3 = new com.redmoon.oa.ui.menu.LeafChildrenCacheMgr(
							lf2.getCode());
					Vector v3 = lccm3.getChildren();
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
						Iterator ir3 = v3.iterator();
						while (ir3.hasNext()) {
							com.redmoon.oa.ui.menu.Leaf lf3 = (com.redmoon.oa.ui.menu.Leaf) ir3
									.next();
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
		StringBuffer sb = new StringBuffer();

		String sql1 = "select code,name from flow_directory where parent_code='root' and is_open=1";
		String sql2 = "select code,name from flow_directory where parent_code=? and is_open=1 and is_mobile_start=1";
		String blank = "&nbsp;&nbsp;&nbsp;&nbsp;";

		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator it = null;
		ResultRecord rr = null;

		try {
			it = jt.executeQuery(sql1);
		} catch (SQLException e) {
			e.printStackTrace();
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
				e.printStackTrace();
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
		ResultIterator ri = null;
		ResultRecord rrd = null;

		try {
			ri = jt.executeQuery(sql, new Object[] { code, type });
			if (ri.hasNext()) {
				rrd = (ResultRecord) ri.next();
				imgUrl = rrd.getString("imgUrl");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			jt.close();
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
	public JSONArray getMobileCanStartInfo(boolean canSendNotice) {
		HttpServletRequest request = ServletActionContext.getRequest();
		JSONArray jsonArr = new JSONArray();
		String sql = "select id,name,imgUrl,type,code from mobile_app_icon_config where isMobileStart="
				+ CAN_MOBILE_START;
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		ResultRecord rrd = null;
		try {
			ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				rrd = (ResultRecord) ri.next();
				int id = rrd.getInt("id");
				String name = rrd.getString("name");
				String imgUrl = rrd.getString("imgUrl");
				String code = rrd.getString("code");
				int type = rrd.getInt("type");
				JSONObject appJson = new JSONObject();
				appJson.put("mId", id);
				appJson.put("mName", name);
				appJson.put("imgUrl", imgUrl);
				appJson.put("code", code);
				appJson.put("type", type);
				if (id == 1) {
					if (canSendNotice) {
						jsonArr.put(appJson);
					}
				} else {
					if (type == TYPE_MENU) {
						if (CODE_QRCODE.equals(code) || CODE_LIVE_PUSH.equals(code) || CODE_LIVE_PLAY.equals(code)) {
							jsonArr.put(appJson);
						}
						else {
							Leaf lf = new Leaf(code);
							if (lf != null && lf.isLoaded()) {
								boolean menuCanSee = false;
								if (lf.getCode().equals(SUPERVIS)
										|| (lf.getParentCode().equals(
												SUPERVIS_PARENT_CODE) && (lf.getName()
												.equals(SUPERVIS_NAME) || lf.getName()
												.equals(SUPERVIS_NAME2)))) {
									menuCanSee = MenuController.canUserSee(request, lf);
								} else {
									menuCanSee = lf.canUserSee(request);
								}
		
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
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(MobileAppIconConfigMgr.class)
					.error(e.getMessage());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(MobileAppIconConfigMgr.class)
					.error(e.getMessage());

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
		com.redmoon.oa.pvg.Privilege pe = new com.redmoon.oa.pvg.Privilege();
		JSONArray jsonArr = new JSONArray();
/*		String sql = "select id,code,name,imgUrl,type from mobile_app_icon_config where type = "
				+ TYPE_MENU + " or type =" + TYPE_MODULE;*/
		
		String sql = "select id,code,name,imgUrl,type,is_add from mobile_app_icon_config order by orders desc, id asc";		
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		ResultRecord rrd = null;
		try {
			ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				rrd = (ResultRecord) ri.next();
				int id = rrd.getInt("id");
				String name = rrd.getString("name");
				String code = rrd.getString("code");
				String imgUrl = rrd.getString("imgUrl");
				int type = rrd.getInt("type");
				int isAdd = rrd.getInt("is_add");
				
				JSONObject appJson = new JSONObject();
				appJson.put("mId", id);
				appJson.put("mName", name);
				appJson.put("imgUrl", imgUrl);
				appJson.put("code", code);
				appJson.put("type", type);
				appJson.put("isAdd", isAdd);
				
				if (type == TYPE_MENU) {
					if (CODE_QRCODE.equals(code) || CODE_LIVE_PUSH.equals(code) || CODE_LIVE_PLAY.equals(code)) {
						jsonArr.put(appJson);
					}
					else {
						Leaf lf = new Leaf(code);
						if (lf != null && lf.isLoaded()) {
							boolean menuCanSee = false;
							if (lf.getCode().equals(SUPERVIS)
									|| (lf.getParentCode().equals(
											SUPERVIS_PARENT_CODE) && (lf.getName()
											.equals(SUPERVIS_NAME) || lf.getName()
											.equals(SUPERVIS_NAME2)))) {
								menuCanSee = MenuController.canUserSee(request, lf);
							} else {
								menuCanSee = lf.canUserSee(request);
							}
	
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
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(MobileAppIconConfigMgr.class)
					.error(e.getMessage());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(MobileAppIconConfigMgr.class)
					.error(e.getMessage());

		}

		return jsonArr;

	}

}
