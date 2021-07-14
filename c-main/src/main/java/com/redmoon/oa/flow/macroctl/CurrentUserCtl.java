package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
public class CurrentUserCtl extends AbstractMacroCtl {
    public CurrentUserCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        StringBuffer sb = new StringBuffer();
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        // 取得用户名
        String userName = privilege.getUser(request);
        if (ff.getValue()!=null && !"".equals(ff.getValue())) {
        	userName = ff.getValue();
        }
        UserDb ud = new UserDb();
        ud = ud.getUserDb(userName);
        sb.append("<input name='" + ff.getName() + "_realname' value='" + ud.getRealName() + "'");
        if (!"".equals(ff.getCssWidth())) {
        	sb.append(" style='width:" + ff.getCssWidth() + "' ");
        }
        sb.append(" readonly size=15>");
        sb.append("<input name='" + ff.getName() + "' value='" + ud.getName() + "' type='hidden'>");

        if (ff.isEditable()) {
            String desc = ff.getDescription();
            if (!"".equals(desc)) {
                try {
                    JSONObject json = new JSONObject(desc);
                    sb.append("<script>\n");
                    sb.append("$(function() {\n");
                    if (json.has("mobile")) {
                        String fieldName = json.getString("mobile");
                        if (!"".equals(fieldName)) {
                            sb.append(setUserInfo(fieldName, ud.getMobile()));
                        }
                    }
                    if (json.has("address")) {
                        String fieldName = json.getString("address");
                        if (!"".equals(fieldName)) {
                            sb.append(setUserInfo(fieldName, ud.getAddress()));
                        }
                    }
                    if (json.has("idCard")) {
                        String fieldName = json.getString("idCard");
                        if (!"".equals(fieldName)) {
                            sb.append(setUserInfo(fieldName, ud.getIDCard()));
                        }
                    }
                    if (json.has("entryDate")) {
                        String fieldName = json.getString("entryDate");
                        if (!"".equals(fieldName)) {
                            sb.append(setUserInfo(fieldName, DateUtil.format(ud.getEntryDate(), "yyyy-MM-dd")));
                        }
                    }
                    if (json.has("birthday")) {
                        String fieldName = json.getString("birthday");
                        if (!"".equals(fieldName)) {
                            sb.append(setUserInfo(fieldName, DateUtil.format(ud.getBirthday(), "yyyy-MM-dd")));
                        }
                    }
                    sb.append("})\n");
                    sb.append("</script>\n");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    /**
     * 用于手机端
     * @Description:
     * @param ff
     * @return
     */
    @Override
    public String getMetaData(FormField ff) {
        JSONArray arr = new JSONArray();
        String desc = ff.getDescription();
        if (!"".equals(desc)) {
            try {
                // 取得用户名
                String userName = ff.getValue();
                if (userName==null || "".equals(userName)) {
                    return "";
                }
                UserDb ud = new UserDb();
                ud = ud.getUserDb(userName);

                JSONObject json = new JSONObject(desc);
                if (json.has("mobile")) {
                    String fieldName = json.getString("mobile");
                    if (!"".equals(fieldName)) {
                        JSONObject jo = new JSONObject();
                        jo.put("value", ud.getMobile());
                        jo.put("field", fieldName);
                        jo.put("type", "mobile");
                        arr.put(jo);
                    }
                }
                if (json.has("address")) {
                    String fieldName = json.getString("address");
                    if (!"".equals(fieldName)) {
                        JSONObject jo = new JSONObject();
                        jo.put("value", ud.getAddress());
                        jo.put("field", fieldName);
                        jo.put("type", "address");
                        arr.put(jo);
                    }
                }
                if (json.has("idCard")) {
                    String fieldName = json.getString("idCard");
                    if (!"".equals(fieldName)) {
                        JSONObject jo = new JSONObject();
                        jo.put("value", ud.getIDCard());
                        jo.put("field", fieldName);
                        jo.put("type", "idCard");
                        arr.put(jo);
                    }
                }
                if (json.has("entryDate")) {
                    String fieldName = json.getString("entryDate");
                    if (!"".equals(fieldName)) {
                        JSONObject jo = new JSONObject();
                        jo.put("value", DateUtil.format(ud.getEntryDate(), "yyyy-MM-dd"));
                        jo.put("field", fieldName);
                        jo.put("type", "entryDate");
                        arr.put(jo);
                    }
                }
                if (json.has("birthday")) {
                    String fieldName = json.getString("birthday");
                    if (!"".equals(fieldName)) {
                        JSONObject jo = new JSONObject();
                        jo.put("value", DateUtil.format(ud.getBirthday(), "yyyy-MM-dd"));
                        jo.put("field", fieldName);
                        jo.put("type", "birthday");
                        arr.put(jo);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return arr.toString();
    }

    public String setUserInfo(String fieldName, String value) {
        StringBuffer sb = new StringBuffer();
        sb.append("if (o('" + fieldName + "')) {\n");
        sb.append(" o('" + fieldName + "').value='" + value + "';\n");
        sb.append("}\n");
        sb.append("if (o('" + fieldName + "_show')) {\n");
        sb.append(" o('" + fieldName + "_show').innerHTML = '" + value + "';\n");
        sb.append("}\n");
        return sb.toString();
    }
    
    @Override
    public String getHideCtlScript(FormField ff, String formElementId) {
    	String str = super.getHideCtlScript(ff, formElementId);
    	
        str += "\r\nif(o('" + ff.getName() + "_realname" + "')!=null){HideCtl('" + ff.getName() + "_realname" + "', '" +
        	ff.getType() +
        	"', '" + ff.getMacroType() + "');}\n";
        return str; 
    }    

    /**
     * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
     * @return String
     */
    public String getDisableCtlScript(FormField ff, String formElementId) {
        // 参数ff来自于数据库，当控件被禁用时，可以根据数据库的值来置被禁用的控件的显示值及需要保存的隐藏type=hidden的值
        // 数据库中没有数据时，当前用户的值将被置为空，否则将被显示为用户的真实姓名，由此实现当前用户宏控件当被禁用时，不会被解析为当前用户
        // 且如果已被置为某个用户，则保持其值不变
        String realName = "";
        if (ff.getValue()!=null && !ff.getValue().equals("")) {
            UserDb ud = new UserDb();
            ud = ud.getUserDb(ff.getValue());
            if (ud.isLoaded())
                realName = ud.getRealName();
        }

        String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                "','" + realName + "','" + ff.getValue() + "');\n";
        str += "DisableCtl('" + ff.getName() + "_realname', '" + ff.getType() +
                "','" + "" + "','" + ff.getValue() + "');\n";
        return str;
    }

    /**
     * 取得用来保存宏控件原始值及toHtml后的值的表单中的HTML元素，通常前者为textarea，后者为span
     * @return String
     */
    @Override
    public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(
            HttpServletRequest request, FormField ff) {
        // 如果是当前用户宏控件，则检查如果没有赋值就赋予其当前用户名称
        FormField ffNew = new FormField();
        ffNew.setName(ff.getName());
        ffNew.setValue(ff.getValue());
        ffNew.setType(ff.getType());
        ffNew.setFieldType(ff.getFieldType());

        // 如果是当前用户宏控件，则检查如果没有赋值就赋予其当前用户名称
        if (StrUtil.getNullStr(ff.getValue()).equals("")) {
            Privilege privilege = new Privilege();
            // UserDb ud = new UserDb();
            // ud = ud.getUserDb(privilege.getUser(request));
            // ff.setValue(ud.getRealName());
            ffNew.setValue(privilege.getUser(request));
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
    @Override
    public String getReplaceCtlWithValueScript(FormField ff) {
        String v = "";
        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
            UserDb ud = new UserDb();
            ud = ud.getUserDb(ff.getValue());
            v = ud.getRealName();
        }

        // return "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "','" + realName + "');\n";
        String str = "if (o('" + ff.getName() + "_realname')) o('" + ff.getName() + "_realname').parentNode.removeChild(o('" + ff.getName() + "_realname'));\n";
		str += "var val='" + v + "';\n";
        if (!"".equals(v)) {
			str += "val=\"<a href='javascript:;' onclick=\\\"addTab('" + v + "', '" + Global.getRootPath() + "/user_info.jsp?userName=" + StrUtil.UrlEncode(ff.getValue()) + "')\\\">" + v + "</a>\";\n";
		}
        str += "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "', val);\n";
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
        String realName = fieldValue;
        if (fieldValue!=null && !fieldValue.equals("")) {
            UserDb ud = new UserDb();
            ud = ud.getUserDb(fieldValue);
            if (ud.isLoaded()) {
                realName = ud.getRealName();
            }
        }

        return realName;
    }

    @Override
    public String getControlType() {
        return "text";
    }

    public String getControlValue(HttpServletRequest httpServletRequest,
                                  FormField formField) {
        String name = "";
        if (!StrUtil.getNullStr(formField.getValue()).equals("")) {
            UserDb ud = new UserDb();
            ud = ud.getUserDb(formField.getValue());
            name = ud.getName();
        }else{
            com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
            String userName = privilege.getUser(httpServletRequest);
            UserDb ud = new UserDb();
            ud = ud.getUserDb(userName);
            name = ud.getName();
        }
        return name;
    }

    public String getControlText(HttpServletRequest httpServletRequest,
                                 FormField formField) {
       String realName = "";
       if (!StrUtil.getNullStr(formField.getValue()).equals("")) {
           UserDb ud = new UserDb();
           ud = ud.getUserDb(formField.getValue());
           realName = ud.getRealName();
       }else{
           com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
           String userName = privilege.getUser(httpServletRequest);
           UserDb ud = new UserDb();
           ud = ud.getUserDb(userName);
           realName = ud.getRealName();
       }
       return realName;
    }

    public String getControlText(String userName,
                                 FormField formField) {
       String realName = "";
       if (!StrUtil.getNullStr(formField.getValue()).equals("")) {
           UserDb ud = new UserDb();
           ud = ud.getUserDb(formField.getValue());
           realName = ud.getRealName();
       }else{
	       	if (formField.isEditable()) {
	           UserDb ud = new UserDb();
	           ud = ud.getUserDb(userName);
	           realName = ud.getRealName();
	       	}
       }
       return realName;
    }

    public String getControlValue(String userName, FormField ff) {
        String name = "";
        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
            UserDb ud = new UserDb();
            ud = ud.getUserDb(ff.getValue());
            name = ud.getName();
            ff.setValue(name);
        }else{
        	if (ff.isEditable()) {
	            UserDb ud = new UserDb();
	            ud = ud.getUserDb(userName);
	            name = ud.getName();
                ff.setValue(name);
            }
        }
        return name;
    }

    public String getControlOptions(String userName, FormField ff) {
        return "";
    }
    
    /**
	 * 根据名称取值，用于导入Excel数据
	 * 
	 * @return
	 */
	@Override
    public String getValueByName(FormField ff, String name) {
		UserDb user = new UserDb();
		user = user.getUserDbByRealName(name);
		return (user != null && user.isLoaded() ? user.getName() : name);
	}
}
