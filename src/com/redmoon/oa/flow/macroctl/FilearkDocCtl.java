package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.flow.*;
import com.redmoon.oa.fileark.*;
import com.redmoon.oa.fileark.Document;

/**
 * 文档宏控件，将文档ID作为控件的默认值
 * @author lenovo
 *
 */
public class FilearkDocCtl  extends AbstractMacroCtl {
    public FilearkDocCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        int docId;
        String val = ff.getValue();
        // 如果没有值，则说明以前未赋过值，从参数中获取
        if (val==null) {
        	docId = ParamUtil.getInt(request, "docId", -1);
	        if (docId==-1) {
	        	docId = StrUtil.toInt(ff.getDefaultValue(), -1);
	            if (docId==-1)
	                return "";
	        }
        }
        else {
        	docId = StrUtil.toInt(val, -1);
            if (docId==-1)
                return "";        	
        }
        
        Document doc = new Document();
        doc = doc.getDocument(docId);
        if (doc==null) {
            return "文档" + docId + "不存在";
        }
                
        String str = "<a href='javascript:;' onclick=\"addTab('" + doc.getTitle() + "', '" + request.getContextPath() + "/doc_show.jsp?id=" + doc.getId() + "')\">" + doc.getTitle() + "</a>";
        str += "<input name='" + ff.getName() + "' value='" + doc.getId() + "' type='hidden' />";
        return str;
    }
    
    /**
     * 用于列表中显示宏控件的值
     * @param request HttpServletRequest
     * @param ff FormField
     * @param fieldValue String
     * @return String
     */
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        String val = StrUtil.getNullStr(fieldValue);
        if (!val.equals("")) {
        	int docId = StrUtil.toInt(val);
            Document doc = new Document();
            doc = doc.getDocument(docId);
            return doc.getTitle();
        }
        else
            return "";
    }    

    public String getDisableCtlScript(FormField ff, String formElementId) {
        String str = "if (o('" + ff.getName() + "'))\r\n";
        str += "o('" + ff.getName() + "').style.display='none';\r\n";
        return str;
    }
    

    /**
     * 必须重载此方法，否则setCtlValue('task', 'macro', flowForm.cws_textarea_task.value)会将控件值置为空，当为必填项时通不过
     * @param request HttpServletRequest
     * @param ff FormField
     * @return String
     */
    public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(
            HttpServletRequest request, FormField ff) {
        FormField ffNew = new FormField();
        ffNew.setName(ff.getName());
        ffNew.setValue(ff.getValue());
        ffNew.setType(ff.getType());
        ffNew.setFieldType(ff.getFieldType());
        ffNew.setValue("");

        long docId = ParamUtil.getLong(request, "docId", -1);
        if (docId!=-1) {
            ffNew.setValue(String.valueOf(docId));
        }
        else {
        	docId = StrUtil.toLong(ff.getDefaultValue(), -1);
            if (docId!=-1)
                ffNew.setValue(String.valueOf(docId));
        }

        // System.out.println(getClass() + " getOuterHTMLOfElementsWithRAWValueAndHTMLValue ffNew=" + ffNew.getValue());
        return super.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request,
                ffNew);
    }    

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
        return "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "', '');\n";
    }

	@Override
	public String getControlOptions(String userName, FormField ff) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getControlText(String userName, FormField ff) {
		// TODO Auto-generated method stub
        String val = StrUtil.getNullStr(ff.getValue());
        if (!val.equals("")) {
        	int docId = StrUtil.toInt(val);
            Document doc = new Document();
            doc = doc.getDocument(docId);
            return doc.getTitle();
        }
        else
            return "";

	}

	@Override
	public String getControlType() {
		// TODO Auto-generated method stub
		return "text";
	}

	@Override
	public String getControlValue(String userName, FormField ff) {
		// TODO Auto-generated method stub
		return StrUtil.getNullStr(ff.getValue());
	}
}