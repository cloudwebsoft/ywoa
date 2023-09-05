package com.redmoon.oa.flow.macroctl;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.SelectMgr;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;

/**
 * 文号框，在描述中填写文号头、年份的字段名，以逗号分隔
 * @author lenovo
 *
 */
public class PaperNoCtl extends AbstractMacroCtl {
	
	@Override
	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		StringBuilder str = new StringBuilder();
		String flowId = (String) request.getAttribute("cwsId");
		
		String desc = StrUtil.getNullStr(ff.getDescription());

		String style = "";
		if (!"".equals(ff.getCssWidth())) {
			style = "style='width:" + ff.getCssWidth() + "'";
		}
		else {
			style = "style='width:150px'";
		}
		
		str.append("<input id='" + ff.getName() + "' name='" + ff.getName() + "' " + style + " size=10 />");

		if (request.getAttribute("isPaperNoJS") == null) {
			str.append("<script>ajaxGetJS(\"/flow/macro/macro_paperno_ctl_js.jsp?flowId=" + flowId
					+ "&fieldName=" + ff.getName() + "&desc=" + StrUtil.UrlEncode(desc) + "\", {})</script>");
			request.setAttribute("isPaperNoJS", "y");
		}
		return str.toString();
	}
	
	@Override
	public String getSetCtlValueScript(HttpServletRequest request,
									   IFormDAO IFormDao, FormField ff, String formElementId) {
		if (ff.getValue() == null) {
			return "";
		}
		else if (ff.getValue().equals(ff.getDefaultValue())) {
			return "";
		} else {
			return super.getSetCtlValueScript(request, IFormDao, ff,
					formElementId);
		}
	}
	
	@Override
	public String getDisableCtlScript(FormField ff, String formElementId) {
		// 参数ff来自于数据库，当控件被禁用时，可以根据数据库的值来置被禁用的控件的显示值及需要保存的隐藏type=hidden的值
		// 数据库中没有数据时，当前用户的值将被置为空，否则将被显示为用户的真实姓名，由此实现当前用户宏控件当被禁用时，不会被解析为当前用户
		// 且如果已被置为某个用户，则保持其值不变
		String v = ff.getValue();
		if (ff.getValue() != null && !"".equals(ff.getValue())) {
			if (ff.getValue().equals(ff.getDefaultValueRaw())) {
				v = "";
			}
		}

		return "DisableCtl('" + ff.getName() + "', '" + ff.getType()
				+ "','" + v + "','" + ff.getValue() + "');\n";
	}	
	
	/**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    @Override
	public String getReplaceCtlWithValueScript(FormField ff) {
		String v = ff.getValue();
		if (ff.getValue() != null && !"".equals(ff.getValue())) {
			if (ff.getValue().equals(ff.getDefaultValueRaw())) {
				v = "";
			}
		}
        return "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "','" + v + "');\n";
     }	
	
    /**
     * 有效性验证
     * @param request
     * @param fdao
     */    	
    @Override
    public boolean validate(HttpServletRequest request, IFormDAO fdao, FormField ff, FileUpload fu) throws ErrMsgException {
    	FormDb fd = fdao.getFormDb();
    	String tableName = fd.getTableNameByForm();
    	int flowId = StrUtil.toInt(fu.getFieldValue("flowId"), 0);
    	String val = StrUtil.getNullStr(fu.getFieldValue(ff.getName()));
    	if ("".equals(val) || val.equals(ff.getDefaultValueRaw())) {
			return true;
		}
    	
		String desc = StrUtil.getNullStr(ff.getDescription());
		String[] ary = StrUtil.split(desc, ",");
		if (ary==null || ary.length!=2) {
			throw new ErrMsgException("文号格式非法！");
		}
		
		String prefix = StrUtil.getNullStr(fu.getFieldValue(ary[0]));
		int year = StrUtil.toInt(fu.getFieldValue(ary[1]));
    	
    	String sql = "select id from " + tableName + " where " + ary[0] + "=" + StrUtil.sqlstr(prefix) + " and " + ary[1] + "=" + year + " and " + ff.getName() + "=" + StrUtil.sqlstr(val) + " and flowId<>" + flowId;
    	JdbcTemplate jt = new JdbcTemplate();
    	ResultIterator ri;
		try {
			ri = jt.executeQuery(sql);
	    	if (ri.hasNext()) {
	    		throw new ErrMsgException("存在重复文号！");
	    	}			
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
		}
   		return true;
    }

    @Override
	public Object getValueForSave(FormField ff, int flowId, FormDb fd, FileUpload fu) {
        // 取得以前的数据，ff中已经是数据库中的数据了，因为20130406 fgf 修改了FormDAO.update方法，使其load了数据
        String val = fu.getFieldValue(ff.getName());
        
        // 取得
		String desc = StrUtil.getNullStr(ff.getDescription());
		String[] ary = StrUtil.split(desc, ",");
		int len = 0;
		if (ary!=null) {
			len = ary.length;
		}
		if (len>0) {
			String whtVal = fu.getFieldValue(ary[0]); // wht
			
			if (whtVal==null || "".equals(whtVal)) {
				// 为null有可能是因为流程表单中字段为不可写，没有值的情况
				return ff.getValue();
			}			
			int year = StrUtil.toInt(fu.getFieldValue(ary[1]));
			PaperNoDb pnpd = new PaperNoDb();
			pnpd = pnpd.getPaperNoDb(whtVal, year);
			if (pnpd==null) {
				LogUtil.getLog(getClass()).error("文号头" + whtVal + "可能不存在！");
			}
			
			int curNum = pnpd.getInt("cur_num");
			
			// 如果序列表中取出的值 + 1不等于所传过来的值，说明修改了值，得重新存入序列表中
			// 丰县发122[2016]2号
			// int p = val.lastIndexOf("]");
			// String numStr = val.substring(p+1, val.length()-1); // 去掉“号”字
			// int num = StrUtil.toInt(numStr, 1);
			int num = StrUtil.toInt(val, 1);
			if (curNum + 1 != num) {
				/*
				curNum = num - 1;
				pnpd.set("cur_num", new Integer(curNum));
				try {
					pnpd.save();
				} catch (ResKeyException e) {
					LogUtil.getLog(getClass()).error(e);
				}
				*/
			}
			else {
				pnpd.set("cur_num", num);
				try {
					pnpd.save();
				} catch (ResKeyException e) {
					LogUtil.getLog(getClass()).error(e);
				}
			}
		}
        
        return StrUtil.getNullStr(val);
    }
    
    @Override
	public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
		if (fieldValue==null || fieldValue.equals(ff.getDefaultValue())) {
			return "";
		} else {
			return StrUtil.getNullStr(fieldValue);
		}
    }    

	@Override
	public String getControlValue(String userName, FormField ff) {
		if (ff.getValue()==null || ff.getValue().equals(ff.getDefaultValue())) {
			return "";
		} else {
			return StrUtil.getNullStr(ff.getValue());
		}
	}

	@Override
	public String getControlText(String userName, FormField ff) {
		if (ff.getValue()== null || ff.getValue().equals(ff.getDefaultValue()))	{
			return "";
		} else {
			return StrUtil.getNullStr(ff.getValue());
		}
	}

	@Override
	public String getControlType() {
		return "text";
	}
	
    @Override
	public String getControlOptions(String userName, FormField ff) {
    	return "";
    }	

}
