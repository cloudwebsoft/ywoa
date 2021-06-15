package com.redmoon.oa.flow.macroctl;

import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.util.RequestUtil;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.FuncUtil;

import javax.servlet.http.HttpServletRequest;

public class QuoteDoubleFieldCtl extends AbstractMacroCtl {
    public QuoteDoubleFieldCtl() {
    }

    @Override
	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		String str = "<input id='" + ff.getName() + "_realshow' name='" + ff.getName() + "_realshow' value='' style='width:" + ff.getCssWidth() + "' readonly />";
		str += "<input id='" + ff.getName() + "' name='" + ff.getName() + "' type='hidden' />";
		return str;
    }

	/**
	 * 取得用来保存宏控件原始值及toHtml后的值的表单中的HTML元素，通常前者为textarea，后者为span
	 * @return String
	 */
	@Override
	public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(HttpServletRequest request, FormField ff) {
		if (iFormDAO !=null) {
			String[] r = getQuoteFieldHtmlValue(request, iFormDAO, ff);
			FormField ffShow = new FormField();
			ffShow.setName(ff.getName());
			ffShow.setValue(r[1]);
			ffShow.setType(ff.getType());
			ffShow.setFieldType(ff.getFieldType());

			FormField ffRaw = new FormField();
			ffRaw.setName(ff.getName());
			ffRaw.setValue(r[0]);
			ffRaw.setType(ff.getType());
			ffRaw.setFieldType(ff.getFieldType());
			return FormField.getOuterHTMLOfElementWithRAWValue(request, ffRaw) + super.getOuterHTMLOfElementWithHTMLValue(request, ffShow);
		}
		else {
			return super.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
		}
	}

	/**
	 * 用于详情页，注意详情页中不会调用getOuterHTMLOfElementsWithRAWValueAndHTMLValue，而只会调用getOuterHTMLOfElementWithHTMLValue
	 * @param request
	 * @param ff
	 * @return
	 */
	@Override
	public String getOuterHTMLOfElementWithHTMLValue(HttpServletRequest request, FormField ff) {
		if (iFormDAO != null) {
			String[] r = getQuoteFieldHtmlValue(request, iFormDAO, ff);
			FormField ffShow = new FormField();
			ffShow.setName(ff.getName());
			ffShow.setValue(r[1]);
			ffShow.setType(ff.getType());
			ffShow.setFieldType(ff.getFieldType());
			return super.getOuterHTMLOfElementWithHTMLValue(request, ffShow);
		} else {
			return super.getOuterHTMLOfElementWithHTMLValue(request, ff);
		}
	}

	/**
	 * 获取用来保存宏控件原始值的表单中的HTML元素中保存的值，生成用以给控件赋值的脚本
	 * @return String
	 */
	@Override
	public String getSetCtlValueScript(HttpServletRequest request, IFormDAO iFormDao, FormField ff, String formElementId) {
		String str = "$('#" + ff.getName() + "_realshow').val($('#cws_span_" + ff.getName() + "').html());\n";
		str += "$('#" + ff.getName() + "').val($('#cws_textarea_" + ff.getName() + "').val());\n";
		return str;
	}

	@Override
	public String getDisableCtlScript(FormField ff, String formElementId) {
		String str = "DisableCtl('" + ff.getName() + "_realshow', '" + ff.getType()
				+ "',$('#cws_span_" + ff.getName() + "').html(), $('#cws_span_" + ff.getName() + "').html());\n";
		str += "DisableCtl('" + ff.getName() + "', '" + ff.getType()
				+ "','" + "" + "', $('#cws_textarea_" + ff.getName() + "').val());\n";
		return str;
	}

	@Override
	public String getReplaceCtlWithValueScript(FormField ff) {
		String str = "$('#" + ff.getName() + "_realshow').hide();\n";
		str += "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType() + "'," + "cws_span_" + ff.getName() + ".innerHTML);\n";
		return str;
	}

    /**
     * 用于模块列表中显示宏控件的值
     * @param request HttpServletRequest
     * @param ff FormField 表单域的描述，其中的value值为空
     * @param fieldValue String 表单域的值
     * @return String
     */
    @Override
	public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
		if (request == null) {
			return fieldValue;
		}

		IFormDAO ifdao = RequestUtil.getFormDAO(request);
		// 如果是嵌入在流程中的脚本查询，则ifdao为null
		if (ifdao == null) {
			return fieldValue;
		}

		return getQuoteFieldHtmlValue(request, ifdao, ff)[1];
    }

    public String[] getQuoteFieldHtmlValue(HttpServletRequest request, IFormDAO ifdao, FormField ff) {
		String[] r = new String[2];
		FormDAO fdao = new FormDAO();
		FormDb fd = new FormDb();
		fd = fd.getFormDb(ifdao.getCwsQuoteForm());
		fdao = fdao.getFormDAO(ifdao.getCwsQuoteId(), fd);
		if (fdao.isLoaded()) {
			String quoteField;
			String desc = ff.getDescription();
			if (!"".equals(desc)) {
				quoteField = desc;
			}
			else {
				quoteField = ff.getName();
			}

			String quoteField2 = ff.getName();
			FormField quoteFormField2;
			FormDAO fdao2 = new FormDAO();
			FormDb fd2 = new FormDb();
			fd2 = fd2.getFormDb(fdao.getCwsQuoteForm());
			fdao2 = fdao2.getFormDAO(fdao.getCwsQuoteId(), fd2);
			if (fdao2.isLoaded()) {
				quoteFormField2 = fdao2.getFormField(quoteField);
				if (quoteFormField2 == null) {
					DebugUtil.e(getClass(), "getQuoteFieldHtmlValue", quoteField + " 不存在");
					r[0] = "无";
					r[1] = "无";
					return r;
				}

				MacroCtlMgr mm = new MacroCtlMgr();
				if (quoteFormField2.getType().equals(FormField.TYPE_MACRO)) {
					MacroCtlUnit mu = mm.getMacroCtlUnit(quoteFormField2.getMacroType());
					if (mu != null) {
						r[0] = fdao2.getFieldValue(quoteField2);
						r[1] = mu.getIFormMacroCtl().converToHtml(request, quoteFormField2, fdao2.getFieldValue(quoteField2));
						return r;
					}
				} else {
					r[0] = FuncUtil.renderFieldValue(fdao, quoteFormField2);
					r[1] = r[0];
					return r;
				}
			}
		}

		r[0] = "无";
		r[1] = "无";
		return r;
	}

    @Override
	public String getControlType() {
        return "text";
    }

    @Override
	public String getControlOptions(String userName, FormField ff) {
		return "";
    }

	@Override
	public String getControlText(String userName, FormField ff) {
		if (iFormDAO == null) {
			return ff.getValue();
		}

		HttpServletRequest request = SpringUtil.getRequest();
		return getQuoteFieldHtmlValue(request, iFormDAO, ff)[1];
	}

	@Override
	public String getControlValue(String userName, FormField ff) {
		if (iFormDAO == null) {
			return ff.getValue();
		}
		HttpServletRequest request = SpringUtil.getRequest();
		return getQuoteFieldHtmlValue(request, iFormDAO, ff)[0];
	}
}