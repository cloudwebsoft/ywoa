package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.visual.FormDAO;

/**
 * @Description: 与联系人相关联的客户显示控件，显示客户名称，方便做报表，用不着了，原来是用于从行动中拉单，因为行动中没有客户字段，但其实是从联系人中拉单，而联系人中已有customer
 * @author: 
 * @Date: 2016-3-6下午07:50:12
 */
public class SalesCustomerRelatedShowCtl extends AbstractMacroCtl {
	public SalesCustomerRelatedShowCtl() {
	}

	public Object getValueForCreate(FormField ff) {
		return ff.getValue();
	}

	public FormDAO getFormDAOOfCustomer(int id) {
		FormDb fd = new FormDb();
		fd = fd.getFormDb("sales_customer");
		FormDAO fdao = new FormDAO(id, fd);
		return fdao;
	}

	/**
	 * 用于列表中显示宏控件的值
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param ff
	 *            FormField
	 * @param fieldValue
	 *            String
	 * @return String
	 */
	public String converToHtml(HttpServletRequest request, FormField ff,
			String fieldValue) {
		String v = StrUtil.getNullStr(fieldValue);
		if (!v.equals("")) {
			// LogUtil.getLog(getClass()).info("StrUtil.toInt(v)=" +
			// StrUtil.toInt(v));
			FormDAO fdao = getFormDAOOfCustomer(StrUtil.toInt(v));
			String str = fdao.getFieldValue("customer");
			return str;
		} else
			return "";
	}

	/**
	 * convertToHTMLCtl
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param ff
	 *            FormField
	 * @return String
	 * @todo Implement this com.redmoon.oa.base.IFormMacroCtl method
	 */
	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		String str = "";
		String v = "";
		if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
			// LogUtil.getLog(getClass()).info("StrUtil.toInt(ff.getValue())=" +
			// StrUtil.toInt(ff.getValue()));
			FormDAO fdao = getFormDAOOfCustomer(StrUtil.toInt(ff.getValue()));
			// LogUtil.getLog(getClass()).info("mobile=" +
			// fdao.getFieldValue("mobile"));
			v = fdao.getFieldValue("customer");
		}
		
		long customerId = -1;
		
		// 如果本表单是“联系人”
		if (ff.getFormCode().equals("sales_linkman")) {
			customerId = ParamUtil.getLong(request, "customerId", -1);
		}
		else if (ff.getFormCode().equals("day_lxr")) {
			long linkmanId = ParamUtil.getLong(request, "linkmanId", -1);
			if (linkmanId!=-1) {
				FormDb fd = new FormDb();
				fd = fd.getFormDb("sales_linkman");
				FormDAO fdao = new FormDAO();
				fdao = fdao.getFormDAO(linkmanId, fd);
				customerId = StrUtil.toLong(fdao.getFieldValue("customer"), -1);
			}
		}

		boolean isCustomerLoaded = false;
		if (customerId != -1) {
			FormDb fdCustomer = new FormDb();
			fdCustomer = fdCustomer.getFormDb("sales_customer");
			FormDAO fdaoCustomer = new FormDAO();
			fdaoCustomer = fdaoCustomer.getFormDAO(customerId, fdCustomer);
			if (fdaoCustomer.isLoaded()) {
				isCustomerLoaded = true;

				str += "<input id='" + ff.getName() + "_realshow' name='"
						+ ff.getName() + "_realshow' value='"
						+ fdaoCustomer.getFieldValue("customer")
						+ "' size=15 readonly>";
				str += "<input id='" + ff.getName() + "' name='" + ff.getName()
						+ "' value='" + customerId + "' style='display:none'>";
			}
		}
		if (!isCustomerLoaded) {
			str += "<input id='" + ff.getName() + "_realshow' name='"
					+ ff.getName() + "_realshow' value='" + v
					+ "' size=15 readonly>";
			str += "<input id='" + ff.getName() + "' name='" + ff.getName()
					+ "' value='' style='display:none'>";
		}

		return str;
	}

	public String getDisableCtlScript(FormField ff, String formElementId) {
		FormDb fdCustomer = new FormDb();
		fdCustomer = fdCustomer.getFormDb("sales_customer");
		FormDAO fdaoCustomer = new FormDAO();
		fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toLong(ff.getValue()), fdCustomer);
		String str = "";
		if (fdaoCustomer.isLoaded()) {
			str = "DisableCtl('" + ff.getName() + "', '" + ff.getType() + "','"
					+ fdaoCustomer.getFieldValue("customer") + "','"
					+ ff.getValue() + "');\n";
		}

		return str;
	}

	/**
	 * 当report时，取得用来替换控件的脚本
	 * 
	 * @param ff
	 *            FormField
	 * @return String
	 */
	public String getReplaceCtlWithValueScript(FormField ff) {
		String v = "";
		if (ff.getValue() != null && !ff.getValue().equals("")) {
			// LogUtil.getLog(getClass()).info("StrUtil.toInt(v)=" +
			// StrUtil.toInt(v));
			FormDAO fdao = getFormDAOOfCustomer(StrUtil.toInt(ff.getValue()));
			v = fdao.getFieldValue("customer");
		}
		return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType()
				+ "','" + v + "');\n";
	}

	/**
	 * 获取用来保存宏控件原始值的表单中的HTML元素中保存的值，生成用以给控件赋值的脚本
	 * 
	 * @return String
	 */
	public String getSetCtlValueScript(HttpServletRequest request,
			IFormDAO IFormDao, FormField ff, String formElementId) {
		long customerId = -1;
		if (ff.getFormCode().equals("sales_linkman")) {
			customerId = ParamUtil.getLong(request, "customerId", -1);
		}
		else if (ff.getFormCode().equals("day_lxr")) {
			long linkmanId = ParamUtil.getLong(request, "linkmanId", -1);
			if (linkmanId!=-1) {
				FormDb fd = new FormDb();
				fd = fd.getFormDb("sales_linkman");
				FormDAO fdao = new FormDAO();
				fdao = fdao.getFormDAO(linkmanId, fd);
				customerId = StrUtil.toLong(fdao.getFieldValue("customer"), -1);
			}
		}		
		
		if (customerId != -1) {
			FormDb fd = new FormDb();
			fd = fd.getFormDb("sales_customer");
			FormDAO fdao = new FormDAO();
			fdao = fdao.getFormDAO(customerId, fd);
			if (fdao.isLoaded()) {
				String str = "setCtlValue('" + ff.getName() + "', '"
						+ ff.getType() + "', '" + customerId + "');\n";
				return str;
			} else
				return super.getSetCtlValueScript(request, IFormDao, ff,
						formElementId);
		} else
			return super.getSetCtlValueScript(request, IFormDao, ff,
					formElementId);
	}

	public String getControlType() {
		return "text";
	}

	public String getControlOptions(String userName, FormField ff) {
		return "";
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
			FormDAO fdao = getFormDAOOfCustomer(StrUtil.toInt(ff.getValue()));
			v = fdao.getFieldValue("customer");
		}
		return v;
	}

	@Override
    public String convertToHTMLCtlForQuery(HttpServletRequest request,
                                           FormField ff) {
		return convertToHTMLCtl(request, ff);
	}
}