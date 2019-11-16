package com.redmoon.oa.flow.macroctl;

import com.redmoon.oa.base.IFormMacroCtl;

import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormParser;

import javax.servlet.http.HttpServletRequest;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.redmoon.oa.flow.FormDb;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormDAO;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;

import cn.js.fan.util.CheckErrException;
import cn.js.fan.util.ErrMsgException;

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
public abstract class AbstractMacroCtl implements IFormMacroCtl {
	com.redmoon.oa.flow.FormDAO formDaoFlow;
	com.redmoon.oa.visual.FormDAO formDaoVisual;
	
	long myActionId;

    public AbstractMacroCtl() {
    }

    /**
     * 用于列表中显示宏控件的值
     * @param request HttpServletRequest
     * @param ff FormField
     * @param fieldValue String
     * @return String
     */
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        return fieldValue;
    }

    /**
     * 将宏控件展开为HTML字符串
     * @param request HttpServletRequest
     * @param ff FormField
     * @return String
     */
    abstract public String convertToHTMLCtl(HttpServletRequest request, FormField ff);

    /**
     * 将宏控件展开为用于查询的HTML字符串
     * @param request HttpServletRequest
     * @param ff FormField
     * @return String
     */
    public String convertToHTMLCtlForQuery(HttpServletRequest request, FormField ff) {
        return "<input name=\"" + ff.getName() + "\" />";
    }

    /**
     * 替换宏控件输入框为html，原方法取消，改为htmlparser fgf 20170923
     * @param macroFormField FormField
     * @param content String
     * @param htmlCtl String
     * @return String
     */
    public String doReplaceMacroCtlWithHTMLCtl(FormField macroFormField,
                                             String content, String htmlCtl) {
        Parser parser;
		try {
			parser = new Parser(content);
			parser.setEncoding("utf-8");//
			AndFilter filter = new AndFilter(new TagNameFilter("input"),
					new HasAttributeFilter("name", macroFormField.getName()));
			NodeList nodes = parser.parse(filter);//
			
			if (nodes == null || nodes.size() == 0) {
				return content;
			}

			Node node = nodes.elementAt(0);
			// node.setText(ctlHTML);
			int s = node.getStartPosition();
			int e = node.getEndPosition();
			
			String c = content.substring(0, s);
			c += htmlCtl;
			c += content.substring(e);
			return c;
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		return content;        
    }    
    
    /**
     * 将表单中的宏控件用strHtmlCtl来替代
     * @param request HttpServletRequest
     * @param macroFormField FormField
     * @param content String
     * @return String
     */
    public String replaceMacroCtlWithHTMLCtl(HttpServletRequest request,
                                             FormField macroFormField,
                                             String content) {
        String strHtmlCtl = convertToHTMLCtl(request, macroFormField);
        return doReplaceMacroCtlWithHTMLCtl(macroFormField, content, strHtmlCtl);
    }

    /**
     * 获取用来保存宏控件原始值的表单中的HTML元素中保存的值，生成用以给控件赋值的脚本
     * @return String
     */
    public String getSetCtlValueScript(HttpServletRequest request, IFormDAO IFormDao, FormField ff, String formElementId) {
        return FormField.getSetCtlValueScript(request, IFormDao, ff, formElementId);
    }

    /**
     * 取得用来保存宏控件原始值及toHtml后的值的表单中的HTML元素，通常前者为textarea，后者为span
     * @return String
     */
    public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(HttpServletRequest request, FormField ff) {
        return FormField.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
    }

    /**
     * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
     * @return String
     */
    public String getDisableCtlScript(FormField ff, String formElementId) {
        return FormField.getDisableCtlScript(ff, formElementId);
    }

    public String getHideCtlScript(FormField ff, String formElementId) {
        return FormField.getHideCtlScript(ff, formElementId);
    }


    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
        return FormField.getReplaceCtlWithValueScript(ff);
    }
    
    /**
     * 替代原getReplaceCtlWithValueScript(FormField ff)方法 ，增加了ifdao参数
     * @param ifdao
     * @param ff
     * @return
     */    
    public String getReplaceCtlWithValueScript(IFormDAO ifdao, FormField ff) {
    	return getReplaceCtlWithValueScript(ff);
    }    

    /**
     * 用于流程处理时，生成表单默认值，如基础数据宏控件，取其默认值
     * @param ff FormField
     * @return Object
     */
    public Object getValueForCreate(int flowId, FormField ff) {
        return ff.getDefaultValue();
    }

    /**
     * 用于visual可视化模块处理
     * @param ff FormField
     * @param fu FileUpload
     * @param fd FormDb
     * @return Object
     */
    public Object getValueForCreate(FormField ff, FileUpload fu, FormDb fd) {
    	String val = "";
    	try {
    		val = StrUtil.getNullStr(fu.getFieldValue(ff.getName(), false));
    	}
    	catch (ClassCastException e) {
    		// 使表单域选择宏控件支持多选的时候，这里得到的应是数组，将其拼装成,号分隔的字符串
    		String[] ary = fu.getFieldValues(ff.getName());
    		for (String str : ary) {
    			if ("".equals(val)) {
    				val = str;
    			}
    			else {
    				val += "," + str;
    			}
    		}
    	}
    	return val;
    }

    /**
     * 取得保存表单时，控件应保存的值，用于流程中表单的处理，被调用前，流程中的附件已被保存
     * @param ff FormField 来自于数据库
     * @param flowId int
     * @param fd FormDb
     * @param fu FileUpload
     * @return Object
     */
    public Object getValueForSave(FormField ff, int flowId, FormDb fd, FileUpload fu) {
        return ff.getValue();
    }

    /**
     * 用于visual可视化模块处理，保存前获得表单域的值
     * @param ff FormField ff的值取自于fu中，在FormDAOMgr中通过getFieldsByForm已取值
     * @param fd FormDb
     * @param formDAOId long
     * @param fu FileUpload
     * @return Object
     */
    public Object getValueForSave(FormField ff, FormDb fd, long formDAOId, FileUpload fu) {
        return ff.getValue();
    }

    /**
     * 取得表单域的类型
     * @return int
     */
    public int getFieldType() {
        return FormField.FIELD_TYPE_VARCHAR;
    }

    /**
     * 当创建父记录时，同步创建嵌套表单的记录（用于visual模块，流程中用不到，因为流程中事先生成了空的表单
     * @param request HttpServletRequest
     * @param ff FormField
     * @param cwsId String 关联ID
     * @param creator String
     * @param fu FileUpload
     * @return int
     * @throws ErrMsgException
     */
    public int createForNestCtl(HttpServletRequest request, FormField ff, String cwsId, String creator, FileUpload fu) throws ErrMsgException{
        return 0;
    }

    /**
     * 保存嵌套表单中的记录，智能模块与流程中共用本方法
     * @param request HttpServletRequest
     * @param ff FormField
     * @param cwsId String
     * @param creator String
     * @param fu FileUpload
     * @return int
     * @throws ErrMsgException
     */
    public int saveForNestCtl(HttpServletRequest request, FormField ff, String cwsId, String creator, FileUpload fu) throws ErrMsgException{
        return 0;
    }

    /**
     * 删除父表单时，同步删除嵌套表的关联记录
     * @param macroField FormField
     * @param cwsId String
     * @return int
     */
    public int onDelNestCtlParent(FormField macroField, String cwsId) {
        return 0;
    }

    /**
     * 当初始化流程时，创建一条空记录
     * @param macroField FormField
     * @param flowId int
     * @param userName String
     * @return boolean
     */
    public boolean initNestCtlOnInitWorkflow(FormField macroField, int flowId, String userName) {
        return true;
    }

    /**
     * 用于nesttable双击单元格编辑时ajax调用
     * @param request HttpServletRequest
     * @param oldValue String 单元格原来的真实值 （如product的ID）
     * @param oldShowValue String 单元格原来的显示值（如product的名称）
     * @param objId String 单元格原来的显示值的input输入框的ID
     * @return String
     */
    public String ajaxOnNestTableCellDBClick(HttpServletRequest request, String formCode, String fieldName, String oldValue, String oldShowValue, String objId) {
        return "";
    }
    
    /**
     * 在验证前获取表单域的值，用于附件、图片宏控件不能为空的检查
     * @param request
     * @param fu
     * @param ff
     */
    public void setValueForValidate(HttpServletRequest request, FileUpload fu, FormField ff) throws CheckErrException {
    }
    
    public void setFlowFormDAO(com.redmoon.oa.flow.FormDAO fdao) {
    	this.formDaoFlow = fdao;
    }
    
    public void setVisualFormDAO(com.redmoon.oa.visual.FormDAO fdao) {
    	this.formDaoVisual = fdao;
    }
    
    /**
     * 数据的预处理，在展开所有宏控件之前调用
     * @param request
     * @param formField
     */    
    public void preProcessData(HttpServletRequest request, FormField formField) {
    
    }
    
    /**
     * 在com.redmoon.oa.flow.FormDAO.save时先进行有效性验证
     * @param request
     * @param fu
     * @return
     * @throws ErrMsgException
     */    
    public boolean validate(HttpServletRequest request, IFormDAO fdao, FormField ff, FileUpload fu) throws ErrMsgException {
    	return true;
    }

    /**
     * getControlValue中可能需要用到myActionId，如OpinionCtl中
     * @param myActionId
     */
    public void setMyActionId(long myActionId) {
    	this.myActionId = myActionId;
    }
    
    /**
     * 根据名称取值，用于导入Excel数据
     * @return
     */
    public String getValueByName(FormField formField, String name) {
    	return name;
    }
    
    /**
     * 取得对应的表单编码，用于模块添加的时候，可以自动关联主表
     * @param request
     * @param formField
     * @return
     */    
    public String getFormCode(HttpServletRequest request, FormField formField) {
    	return "";
    }
    
    /**
     * 当智能模块创建记录后调用，如对文件组控件对应的字段中的文件ID进行处理
     * @param request
     * @param fu
     * @param fdaoId
     */
/*    public void doAfterCreate(HttpServletRequest request, FileUpload fu, FormField ff, com.redmoon.oa.visual.FormDAO fdao) {
    	
    }*/

    /**
     * 取得元数据，比如：SQL控件中取得条件中的字段{$fieldName}
     */
    public String getMetaData(FormField ff) {
    	return "";
    }

    /**
     * 取得根据名称（而不是值）查询时需用到的SQL语句，如果没有特定的SQL语句，则返回空字符串
     * @param request
     * @param ff 当前被查询的字段
     * @param value
     * @param isBlur 是否模糊查询
     * @return
     */
    public String getSqlForQuery(HttpServletRequest request, FormField ff, String value, boolean isBlur) {
		return "";
	}

    public void onFormDAOSave(HttpServletRequest request, IFormDAO ifdao, FormField field,
                              FileUpload fu) throws ErrMsgException {}
}
