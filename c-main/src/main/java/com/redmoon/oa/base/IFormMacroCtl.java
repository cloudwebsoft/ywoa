package com.redmoon.oa.base;

import javax.servlet.http.*;

import cn.js.fan.util.*;
import com.alibaba.fastjson.JSONObject;
import com.redmoon.kit.util.*;
import com.redmoon.oa.flow.*;
import org.jsoup.nodes.Document;

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
public interface IFormMacroCtl {
    /**
     * 用于显示列表页中的字段
     *
     * @param request HttpServletRequest
     * @param ff      FormField
     * @return String
     */
    String converToHtml(HttpServletRequest request, FormField ff, String fieldValue);

    /**
     * 取得导出时的值
     * @param request
     * @param ff
     * @param fieldValue
     * @return
     */
    String getValueForExport(HttpServletRequest request, FormField ff, String fieldValue);

    /**
     * 生成用于替换展开宏控件的字符串，如用户列表控件展开后为<select id="fieldName" name="fieldName"><option value="userName">userRealName</option><option>...</option></select>
     *
     * @param request HttpServletRequest
     * @param ff      FormField
     * @return String
     */
    String convertToHTMLCtl(HttpServletRequest request, FormField ff);

    /**
     * 将宏控件展开为查询界面的控件，默认仅为输入框
     *
     * @param request HttpServletRequest
     * @param ff      FormField
     * @return String
     */
    String convertToHTMLCtlForQuery(HttpServletRequest request,
                                    FormField ff);

    /**
     * 将表单中的宏控件用convertToHTMLCtl生成的字符串来替代
     *
     * @param request        HttpServletRequest
     * @param macroFormField FormField
     * @param doc        String
     * @return String
     */
    void replaceMacroCtlWithHTMLCtl(HttpServletRequest request,
                                      FormField macroFormField,
                                      Document doc);

    /**
     * 当rend时，取得用来保存宏控件原始值及toHtml后的值的表单中的HTML元素，通常前者为textarea，后者为span
     *
     * @return String
     */
    String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(HttpServletRequest request, FormField ff);

    /**
     * 当表单域不可写或显示详情页时，用来取得用来显示控件值的字符串
     *
     * @param request
     * @param ff
     * @return
     */
    String getOuterHTMLOfElementWithHTMLValue(HttpServletRequest request, FormField ff);

    /**
     * 当rend时，获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
     *
     * @return String
     */
    String getDisableCtlScript(FormField ff, String formElementId);

    /**
     * 当rend时，生成隐藏单元格或嵌套表列的脚本
     *
     * @param ff            FormField
     * @param formElementId String
     * @return String
     */
    String getHideCtlScript(FormField ff, String formElementId);

    /**
     * 当rend时，获取用来保存宏控件原始值的表单中的HTML元素中保存的值，生成用以给控件赋值的脚本
     *
     * @return String
     */
    String getSetCtlValueScript(HttpServletRequest request,
                                IFormDAO IFormDao, FormField ff,
                                String formElementId);

    /**
     * 当report时，取得用来替换控件的脚本
     *
     * @param ff FormField
     * @return String
     */
    String getReplaceCtlWithValueScript(FormField ff);

    /**
     * 替代原getReplaceCtlWithValueScript(FormField ff)方法 ，增加了ifdao参数
     *
     * @param ifdao
     * @param ff
     * @return
     */
    String getReplaceCtlWithValueScript(IFormDAO ifdao, FormField ff);

    /**
     * 用于流程处理时，生成表单默认值，如基础数据宏控件，取其默认值
     *
     * @param ff FormField
     * @return Object
     */
    Object getValueForCreate(int flowId, FormField ff);

    /**
     * 用于visual可视化模块处理，创建表单获取相关值
     *
     * @param ff FormField
     * @param fu FileUpload
     * @param fd FormDb
     * @return Object
     */
    Object getValueForCreate(FormField ff, FileUpload fu, FormDb fd);

    /**
     * 取得保存表单时，控件应保存的值，用于流程中表单的处理，被调用前，流程中的附件已被保存
     *
     * @param ff     FormField
     * @param flowId int
     * @param fd     FormDb
     * @return Object
     */
    Object getValueForSave(FormField ff, int flowId, FormDb fd,
                           FileUpload fu);

    /**
     * 取得保存表单时，控件应保存的值，用于visula可视化模块中的处理
     *
     * @param ff FormField
     * @param fu FileUpload
     * @return Object
     */
    Object getValueForSave(FormField ff, FormDb fd, long formDAOId,
                           FileUpload fu);

    /**
     * 2.2版及之前版本的fieldType均为FIELD_TYPE_VARCHAR
     *
     * @return int
     */
    int getFieldType(FormField ff);

    /**
     * 在智能模块设计中为嵌套表单创建记录
     *
     * @param macroField FormField 嵌套表单宏控件
     * @param cwsId      String 嵌套表单的父表单记录的ID
     * @param creator    String
     * @param fu         FileUpload
     * @return int
     */
    int createForNestCtl(HttpServletRequest request,
                         FormField macroField, String cwsId,
                         String creator, FileUpload fu) throws
            ErrMsgException;

    /**
     * 为嵌套表单保存记录
     *
     * @param request    HttpServletRequest
     * @param macroField FormField
     * @param cwsId      String
     * @param creator    String
     * @param fu         FileUpload
     * @return int
     * @throws ErrMsgException
     */
    int saveForNestCtl(HttpServletRequest request, FormField macroField,
                       String cwsId, String creator, FileUpload fu) throws
            ErrMsgException;

    /**
     * 当删除嵌套表单的父表单记录时，同步删除子表单相关数据
     *
     * @param macroField FormField
     * @param cwsId      String
     * @return int
     */
    int onDelNestCtlParent(FormField macroField, String cwsId);

    /**
     * 当创建流程时，为表单中的嵌套表单在数据库中创建一条空记录
     *
     * @param macroField FormField
     * @param flowId     int
     * @param userName   String
     * @return boolean
     */
    boolean initNestCtlOnInitWorkflow(FormField macroField, int flowId,
                                      String userName);

    /**
     * 用于nesttable双击单元格编辑时ajax调用
     *
     * @param request      HttpServletRequest
     * @param oldValue     String 单元格原来的真实值 （如product的ID）
     * @param oldShowValue String 单元格原来的显示值（如product的名称）
     * @param objId        String 单元格原来的显示值的input输入框的ID
     * @return String
     */
    String ajaxOnNestTableCellDBClick(HttpServletRequest request,
                                      String formCode, String fieldName,
                                      String oldValue,
                                      String oldShowValue, String objId);

    /**
     * 取得控件的类型，text select textarea...，用于手机客户端生成表单
     *
     * @return String
     */
    String getControlType();

    /**
     * 如果不是select型，则options返回为空，用于手机客户端生成表单
     *
     * @return String
     */
    String getControlOptions(String userName, FormField ff);

    /**
     * 实际的值（如用户名），用于手机客户端生成表单
     *
     * @return String
     */
    String getControlValue(String userName, FormField ff);

    /**
     * 显示的值（如真实姓名），用于手机客户端生成表单
     *
     * @return String
     */
    String getControlText(String userName, FormField ff);

    /**
     * 在验证前获取表单域的值，用于附件、图片宏控件不能为空的检查
     *
     * @param request
     * @param fu
     * @param ff
     */
    void setValueForValidate(HttpServletRequest request, FileUpload fu, FormField ff) throws CheckErrException;

    /**
     * 替换宏控件过程中，有的宏控件（如第三方数据宏控件）可能会修改其它字段的值，因此需要传入fdao
     *
     * @param fdao
     */
    void setIFormDAO(com.redmoon.oa.base.IFormDAO fdao);

    /**
     * getControlValue中可能需要用到myActionId，如OpinionCtl中
     *
     * @param myActionId
     */
    void setMyActionId(long myActionId);

    /**
     * 数据的预处理，在展开所有宏控件之前调用
     *
     * @param request
     * @param formField
     */
    void preProcessData(HttpServletRequest request, FormField formField);

    /**
     * 在com.redmoon.oa.flow.FormDAO.save时先进行有效性验证
     *
     * @param request
     * @param fu
     * @return
     * @throws ErrMsgException
     */
    boolean validate(HttpServletRequest request, IFormDAO fdao, FormField ff, FileUpload fu) throws ErrMsgException;

    /**
     * 根据名称取值，用于导入Excel数据
     *
     * @return
     */
    String getValueByName(FormField formField, String name);

    /**
     * 取得对应的表单编码，用于模块添加的时候，可以自动关联主表
     *
     * @param request
     * @param formField
     * @return
     */
    String getFormCode(HttpServletRequest request, FormField formField);

    /**
     * 当智能模块创建记录后调用，如对文件组控件对应的字段中的文件ID进行处理
     * @param request
     * @param fu
     * @param fdaoId
     */
    // void doAfterCreate(HttpServletRequest request, FileUpload fu, FormField ff, com.redmoon.oa.visual.FormDAO fdao);

    /**
     * 取得元数据，比如：SQL控件中取得条件中的字段{$fieldName}
     */
    String getMetaData(FormField ff);

    /**
     * 取得根据名称（而不是值）查询时需用到的SQL语句，如果没有特定的SQL语句，则返回空字符串
     *
     * @param request
     * @param ff      当前被查询的字段
     * @param value
     * @param isBlur  是否模糊查询
     * @return
     */
    String getSqlForQuery(HttpServletRequest request, FormField ff, String value, boolean isBlur);

    /**
     * 当表单内容保存后调用，如用于手写板宏控件，为base64编码的图片生成文件
     *
     * @param request
     * @param field
     * @param fu
     * @return
     * @throws ErrMsgException
     */
    void onFormDAOSave(HttpServletRequest request, IFormDAO ifdao, FormField field,
                       FileUpload fu) throws ErrMsgException;

}
