package com.redmoon.oa.flow.macroctl;

import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.visual.FormDAO;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class LinkmanListWinCtl extends AbstractMacroCtl {
    public LinkmanListWinCtl() {
    }

    /**
     * 用于列表中显示宏控件的值
     * @param request HttpServletRequest
     * @param ff FormField
     * @param fieldValue String
     * @return String
     */
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        String v = StrUtil.getNullStr(fieldValue);
        if (!v.equals("")) {
            // LogUtil.getLog(getClass()).info("StrUtil.toInt(v)=" + StrUtil.toInt(v));
            FormDAO fdao = getFormDAOOfLinkMan(StrUtil.toInt(v));
            String str = fdao.getFieldValue("linkmanName");
            // str = "<a title='客户：" + fdao.getFieldValue("customer") + "  部门：" + fdao.getFieldValue("dept") + "' href='" + request.getContextPath() + "/sales/linkman_show.jsp?id=" + v + "&action=&formCode=sales_linkman' target='_blank'>" + str + "</a>";
            return str;
        }
        else
            return "";
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
        String v = "";
        if (ff.getValue() != null && !ff.getValue().equals("")) {
                // LogUtil.getLog(getClass()).info("StrUtil.toInt(v)=" + StrUtil.toInt(v));
                FormDAO fdao = getFormDAOOfLinkMan(StrUtil.toInt(ff.getValue()));
                v = fdao.getFieldValue("linkmanName");
        }
        return "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "','" + v + "');\n";
     }

    public FormDAO getFormDAOOfLinkMan(int id) {
        FormDb fd = new FormDb();
        fd = fd.getFormDb("sales_linkman");
        FormDAO fdao = new FormDAO(id, fd);
        return fdao;
    }

    /**
     *
     *
     * @param request HttpServletRequest
     * @param ff FormField
     * @return String
     * @todo Implement this com.redmoon.oa.base.IFormMacroCtl method
     */
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String str = "";
        String v = "";
        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
            // LogUtil.getLog(getClass()).info("StrUtil.toInt(ff.getValue())=" + StrUtil.toInt(ff.getValue()));
            FormDAO fdao = getFormDAOOfLinkMan(StrUtil.toInt(ff.getValue()));
            // LogUtil.getLog(getClass()).info("mobile=" + fdao.getFieldValue("mobile"));
            v = fdao.getFieldValue("linkmanName");
        }

        int linkmanId = ParamUtil.getInt(request, "linkmanId", -1);
        if (linkmanId!=-1) {
            FormDb fdLinkman = new FormDb();
            fdLinkman = fdLinkman.getFormDb("sales_linkman");
            FormDAO fdaoLinkman = new FormDAO();
            fdaoLinkman = fdaoLinkman.getFormDAO(linkmanId, fdLinkman);
            if (fdaoLinkman.isLoaded()) {
                str += "<input name='" + ff.getName() + "_realshow' value='" +
                        fdaoLinkman.getFieldValue("linkmanName") +
                        "' size=15 readonly>";
                str += "<input name='" + ff.getName() + "' value='" + linkmanId +
                        "' type='hidden'>";
            }
            else {
                str += "<input name='" + ff.getName() + "_realshow' value='" + v +
                        "' size=15 readonly>";
                str += "<input name='" + ff.getName() + "' value='' type='hidden'>";
            }
        }
        else {
            str += "<input name='" + ff.getName() + "_realshow' value='" + v +
                    "' size=15 readonly>";
            str += "<input name='" + ff.getName() + "' value='' type='hidden'>";
        }
        long customerId = ParamUtil.getLong(request, "customerId", -1);
        str +=
                "&nbsp;<input type=button class=btn value='选择' onClick='openWinLinkmanList(" +
                ff.getName() + ", " + customerId + ")'>";
        return str;
    }

    /**
     * 获取用来保存宏控件原始值的表单中的HTML元素中保存的值，生成用以给控件赋值的脚本
     * @return String
     */
    public String getSetCtlValueScript(HttpServletRequest request, IFormDAO IFormDao, FormField ff, String formElementId) {
        int linkmanId = ParamUtil.getInt(request, "linkmanId", -1);
        if (linkmanId!=-1) {
            FormDb fdLinkman = new FormDb();
            fdLinkman = fdLinkman.getFormDb("sales_linkman");
            FormDAO fdaoLinkman = new FormDAO();
            fdaoLinkman = fdaoLinkman.getFormDAO(linkmanId, fdLinkman);
            if (fdaoLinkman.isLoaded()) {
                String str = "setCtlValue('" + ff.getName() + "', '" +
                             ff.getType() +
                             "', '" + linkmanId +
                             "');\n";
                return str;
            }
            else
                return super.getSetCtlValueScript(request, IFormDao, ff, formElementId);
        }
        else
            return super.getSetCtlValueScript(request, IFormDao, ff, formElementId);
    }

    public String getControlType() {
    	return "contactSelect";
    }

    public String getControlValue(String userName, FormField ff) {
    	if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
			return ff.getValue();
		}
		return "";
    }

    public String getControlText(String userName, FormField ff) {
    	String v = "";
		if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
			FormDAO fdao = getFormDAOOfLinkMan(StrUtil.toInt(ff.getValue()));
			v = fdao.getFieldValue("linkmanName");
		}
		return v;
    }

    public String getControlOptions(String userName, FormField ff) {
    	return "";
    	/*com.redmoon.oa.pvg.Privilege priv = new com.redmoon.oa.pvg.Privilege();
		ActionContext ctx = ActionContext.getContext();
		HttpServletRequest request = (HttpServletRequest)ctx.get(ServletActionContext.HTTP_REQUEST);
		HttpSession session = request.getSession();
		UserDb userDb = new UserDb(userName);
		session.setAttribute(Constant.OA_NAME,userName);
		session.setAttribute(Constant.OA_UNITCODE, userDb
				.getUnitCode());
		if (!priv.isUserPrivValid(request, "sales.user")
				&& !priv.isUserPrivValid(request, "sales")
				&& !priv.isUserPrivValid(request, "sales.manager")) {
			return new JSONArray().toString();
		}
		
    	StringBuilder sqlSb = new StringBuilder();
		sqlSb.append(" select a.id from ft_sales_linkman a, ft_sales_customer b where a.customer=b.id  ");
		sqlSb.append("  and a.unit_code = ").append("'").append(
				priv.getUserUnitCode(request)).append("' ");
		if(!priv.isUserPrivValid(request, "admin")
				&& !priv.isUserPrivValid(request, "sales")
				&& !priv.isUserPrivValid(request, "sales.manager") && priv.isUserPrivValid(request, "sales.user")){
			sqlSb.append(" and b.sales_person in ( ")
			.append(StrUtil.sqlstr(userName)).append(")");
		}else if(priv.isUserPrivValid(request, "sales.manager")
				&& !priv.isUserPrivValid(request, "admin")
				&& !priv.isUserPrivValid(request, "sales")){
			// 根据部门管理权限，查看所属部门的客户
			DeptUserDb dud = new DeptUserDb(priv.getUser(request));
			String dept = dud.getDeptCode();
			Vector vec = dud.getAllUsersOfUnit(dept);
			Iterator it = vec.iterator();
			String salesPerson = "";
			while (it.hasNext()) {
				UserDb ud = (UserDb) it.next();
				if (salesPerson.equals("")) {
					salesPerson = StrUtil.sqlstr(ud.getName());
				} else {
					salesPerson += ","
							+ StrUtil.sqlstr(ud.getName());
				}
			}
			if (!salesPerson.equals("")) {
				sqlSb.append(" and b.sales_person in ( ").append(
						salesPerson).append(")");
			}
		}
		FormDAO fdao = new FormDAO();
		JSONArray options = new JSONArray();
		try {
			Vector vector = fdao.list("sales_linkman", sqlSb.toString());
			if(vector != null && vector.size()>0){
				Iterator ir = null;
				ir = vector.iterator();
				while(ir.hasNext()){
					fdao = (FormDAO)ir.next();
					JSONObject contact = new JSONObject();
					contact.put("value",fdao.getId());
					contact.put("name",fdao.getFieldValue("linkmanName"));//客户名称
					options.put(contact);
				}
			}
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(CustomerListWinCtl.class).error(e.getMessage());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(CustomerListWinCtl.class).error(e.getMessage());
		}
		return options.toString();*/
    }

}
