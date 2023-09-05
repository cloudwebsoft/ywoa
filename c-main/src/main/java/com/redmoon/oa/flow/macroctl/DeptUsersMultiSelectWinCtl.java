package com.redmoon.oa.flow.macroctl;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DeptUsersMultiSelectWinCtl extends AbstractMacroCtl {
	//boolean res = false;
    public DeptUsersMultiSelectWinCtl() {
    }
    
    @Override
	public String getSetCtlValueScript(HttpServletRequest request, IFormDAO IFormDao, FormField ff, String formElementId) {
        return "";
    }

    @Override
	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
    	Privilege priv = new Privilege();
    	String unitCode = priv.getUserUnitCode(request);
    	String myname = priv.getUser(request);
    	UserDb ud = new UserDb(myname);
    	String[] dps = ud.getAdminDepts();
    	String depts = "";
    	if (dps != null) {
			for (String dept : dps) {
				depts += dept + ",";
			}
		}

    	DeptUserDb udd = new DeptUserDb();
    	Vector v = udd.getDeptsOfUser(myname);
    	Iterator it = v.iterator();
    	while (it.hasNext()) {
    		DeptDb dd = (DeptDb) it.next();
    		depts += dd.getCode() + (it.hasNext() ? "," : "");
    	}
    	
    	String actionIdStr = request.getAttribute("workflowActionId")+"";
    	int actionId = 0;
    	boolean res = false;
    	if(!"null".equals(actionIdStr)){
    		actionId = Integer.valueOf(actionIdStr);
    		WorkflowActionDb wa = new WorkflowActionDb();
    		wa = wa.getWorkflowActionDb(actionId);
    		if(wa.isStart==1){
    			res = true;
    		}
    	}
        String str = "";
        String realName = "";
        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
        	String[] ary = StrUtil.split(ff.getValue(), ",");
            UserDb user = new UserDb();
        	for (int i=0; i<ary.length; i++) {
                user = user.getUserDb(ary[i]);      
                if (!user.isLoaded()) {
                	continue;
                }
                if ("".equals(realName)) {
					realName = user.getRealName();
				} else {
					realName += "," + user.getRealName();
				}
        	}
        }
        
        if("".equals(realName)){
        	//if(priv.isUserPrivValid(request, "notice")){
        		//realName = "全部用户";
        	//}
        	//if(priv.isUserPrivValid(request, "notice.dept")){
        		//realName = "单位所有用户";
        	//}
        	realName = "全部用户";
        }
        
        //str += "<table width='100%' style='border:0'><tr><td width='80%' style='text-align:left;border:0'><textarea id='" + ff.getName() + "_realshow' name='" + ff.getName() + "_realshow'" +
                //"' readonly  cols=85 rows=9></textarea>";
        str += "<table width='100%' style='border:0;align:left'><tr><td colspan='2' style='text-align:left;border:0;align:left'>";
        str += "<input id='" + ff.getName() + "' name='" + ff.getName() + "' value='' type='text' style='display:none'></td>";
        if(!"null".equals(actionIdStr)){
//        if(priv.isUserPrivValid(request, "notice")){
//        	str += "<span id='btnc' name='btnc'>&nbsp;<input  id='" + ff.getName() + "_btna' type='checkbox' checked='checked' onClick='selectUsers(0)'>&nbsp;&nbsp;全部用户</span>";
//        }else if(priv.isUserPrivValid(request, "notice.dept")&&!priv.isUserPrivValid(request, "admin")){
//        	str += "<span id='btnc' name='btnc'>&nbsp;<input  id='" + ff.getName() + "_btnd' type='checkbox' checked='checked' onClick='selectUsers(1)'>&nbsp;&nbsp;全部用户</span>";
//        }
        	
        str += "<span id='btnc' name='btnc'><input id='radioall' name='radio' type='radio' onClick='setRadioALLSelected();selectUsers()'><a href='javascript:setRadioALLSelected();selectUsers()'>&nbsp;&nbsp;全部用户</a>";
        str += "&nbsp;&nbsp;&nbsp;&nbsp;<input id='radioselect' name='radio' type='radio' onClick='openWinUsers();setRadioSelected();setIsAll()'><a href='javascript:openWinUsers();setRadioSelected();setIsAll()'>&nbsp;&nbsp;&nbsp;选择用户&nbsp;&nbsp;</a></span></br>";
        }
//        str += "&nbsp;&nbsp;&nbsp;&nbsp;<span name='uspan'><input class='btn1' id='" + ff.getName() + "_btnu' type=button value='&nbsp;&nbsp;选&nbsp;择&nbsp;用&nbsp;户&nbsp;&nbsp;' onClick='openWinUserMultiSelect(" + ff.getName() + ");setIsAll()' disabled/></span></br>";
        str += "<textarea id='" + ff.getName() + "_realshow' name='" + ff.getName() + "_realshow'" +
        "' readonly  cols=85 rows=6 disabled>" + realName + "</textarea>";
        //str += "<td width='20%' style='border:0'>&nbsp;<input class='btn1' id='" + ff.getName() + "_btnu' type=button value='&nbsp;&nbsp;选&nbsp;择&nbsp;用&nbsp;户&nbsp;&nbsp;' onClick='openWinUserMultiSelect(" + ff.getName() + ");setIsAll()'>";
        //if(priv.isUserPrivValid(request, "notice")){
        	//str += "</br>&nbsp;<input class='btn1' id='" + ff.getName() + "_btna' type=button value='&nbsp;&nbsp;全&nbsp;部&nbsp;用&nbsp;户&nbsp;&nbsp;' onClick='selectUsers(0)'>";
        //}else if(priv.isUserPrivValid(request, "notice.dept")&&!priv.isUserPrivValid(request, "admin")){
        	//str += "</br>&nbsp;<input class='btn1' id='" + ff.getName() + "_btnd' type=button value='单位所有用户' onClick='selectUsers(1)'>";
        //}
        //str += "</br>&nbsp;<input class='btn1' id='" + ff.getName() + "_btnc' type=button value='&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;清&nbsp;&nbsp;空&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' onClick='clearAll()'>";
        str += "</td></tr></table>";
//        str += "<textarea id='" + ff.getName() + "_realshow' name='" + ff.getName() + "_realshow'" +
//		        "' readonly  cols=85 rows=9>" + realName + "</textarea>";
//		str += "<input id='" + ff.getName() + "' name='" + ff.getName() + "' value='' type='hidden'>";
//		
//		str += "<span style='v-align:middle' name='btnSpan' id='btnSpan'>&nbsp;<input id='" + ff.getName() + "_btnu' type=button value='&nbsp;&nbsp;选&nbsp;择&nbsp;用&nbsp;户&nbsp;&nbsp;' onClick='openWinUserMultiSelect(" + ff.getName() + ");setIsAll()'>";
//		if(priv.isUserPrivValid(request, "notice")){
//			str += "</br>&nbsp;<input id='" + ff.getName() + "_btna' type=button value='&nbsp;&nbsp;全&nbsp;部&nbsp;用&nbsp;户&nbsp;&nbsp;' onClick='selectUsers(0)'>";
//		}else{
//			str += "</br>&nbsp;<input id='" + ff.getName() + "_btnd' type=button value='单位所有用户' onClick='selectUsers(1)'>";
//		}
//		str += "</br>&nbsp;<input id='" + ff.getName() + "_btnc' type=button value='&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;清&nbsp;&nbsp;空&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' onClick='clearAll()'></span>";
        str += "<script>function selectUsers(){";
        str += "o('"+ff.getName()+"_realshow').value='';";
        str += "o('"+ff.getName()+"').value='';";
        //str += "if(tag=='0'){";
        if(priv.isUserPrivValid(request, "notice")){
//        str += "if(o('"+ff.getName()+"_btna').checked==true){";
//        str += "o('"+ff.getName()+"_btnu').disabled  = true;";
//        //str += "o('"+ff.getName()+"_realshow').disabled = true;";
//        str += "}else{";
//        str += "o('"+ff.getName()+"_btnu').disabled  = false;";
//        //str	+= "o('"+ff.getName()+"_realshow').disabled = false;";
//        str += "}";
        str += "o('"+ff.getName()+"_realshow').value='全部用户';";
        str += "o('"+ff.getName()+"').value='全部用户';";
        str += "o('userList').value='root';";
        str += "o('is_toAll').value='2';";
        }
    	else if(priv.isUserPrivValid(request, "notice.dept")){
        //str += "}";
        //str += "if(tag=='1'){";
//        str += "if(o('"+ff.getName()+"_btnd').checked==true){";
//        str += "o('"+ff.getName()+"_btnu').disabled  = true;";
//        //str += "o('"+ff.getName()+"_realshow').disabled = true;";
//        str += "}else{";
//        str += "o('"+ff.getName()+"_btnu').disabled  = false;";
//        //str	+= "o('"+ff.getName()+"_realshow').disabled = false;";
//        str += "}";
        //str += "o('"+ff.getName()+"_realshow').value='单位所有用户';";
        //str += "o('"+ff.getName()+"').value='单位所有用户';";
        str += "o('"+ff.getName()+"_realshow').value='全部用户';";
        str += "o('"+ff.getName()+"').value='全部用户';";
        str += "o('userList').value='"+unitCode+"';";
        str += "o('is_toAll').value='1';";
    	}
        //str += "}";
        str += "}";
        str += "function clearAll(){";
        //str += "o('"+ff.getName()+"_realshow').value='';";
        //str += "o('"+ff.getName()+"').value='';";
        str += "}";
        str += "function setIsAll(){";
        //str += "o('"+ff.getName()+"').value='';";
        str += "o('is_toAll').value='0';";
        str += "o('userList').value=o('"+ff.getName()+"').value;";
        str += "}";
        
        str += "</script>";
        str += "<script>";
        str += "$('#level').click(function() {";
  		str += "if ($('#level').attr('checked')) {";
  		str +=	"$('#t_color').val('#ff0000');";
  		str += "}";
  		str += "else {";
  		str += "$('#t_color').val('');";
  		str += "}";
  		str += "$('#t_color').change();";
  	    str += "});";
  	    
  	    if(res){
  	    str += "$(document).ready(function(){ ";
  	    if(priv.isUserPrivValid(request, "notice")){
  	    	str += "o('"+ff.getName()+"_realshow').value='" + realName + "';";
  	        str += "o('"+ff.getName()+"').value='" + (ff.getValue().equals("") ? "全部用户" : ff.getValue()) + "';";
  	        str += "o('userList').value='root';";
  	        str += "o('is_toAll').value='2';";
  	        str += "try{disDepts='" + unitCode + "';}catch(e){}";
  	    }else if(priv.isUserPrivValid(request, "notice.dept")){
  	    	//str += "o('"+ff.getName()+"_realshow').value='单位所有用户';";
  	        //str += "o('"+ff.getName()+"').value='单位所有用户';";
  	    	str += "o('"+ff.getName()+"_realshow').value='" + realName + "';";
  	    	str += "o('"+ff.getName()+"').value='" + (ff.getValue().equals("") ? "全部用户" : ff.getValue()) + "';";
  	        str += "o('userList').value='"+unitCode+"';";
  	        str += "o('is_toAll').value='1';";
  	        str += "try{disDepts='" + depts + "';}catch(e){}";
  	    }
  	    str += "});";
  	    }
  	    
  	    str += "function setRadioALLSelected(){";
        str += "$('#radioall').attr('checked','checked');";
        str += "$('#radioselect').removeAttr('checked');";
  	    str += "}";
  	    str += "function setRadioSelected(){";
        str += "$('#radioall').removeAttr('checked');";
        str += "$('#radioselect').attr('checked','checked');";
  	    str += "}";
  	    
  	    str += "function openWinUsers(){";
        str += "openWinUserMultiSelect(o('" + ff.getName() + "'));";
  	    str += "}";
  	  
  	    str += "</script>";
        return str;
    }

    /**
     * 用于列表中显示宏控件的值
     * @param request HttpServletRequest
     * @param ff FormField
     * @param fieldValue String
     * @return String
     */
    @Override
	public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        String v = StrUtil.getNullStr(fieldValue);
        if (v.equals("全部用户") || v.equals(DeptDb.ROOTCODE)) {
        	return "全部用户";
        }
		if (!v.equals("")) {
			UserDb user = new UserDb();
			String[] ary = StrUtil.split(v, ",");
			v = "";
			for (int i = 0; i < ary.length; i++) {
				user = user.getUserDb(ary[i]);
				if ("".equals(v)) {
					v = user.getRealName();
				} else {
					v += "," + user.getRealName();
				}
			}

			return v;
		} else {
			return "";
		}
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    @Override
	public String getReplaceCtlWithValueScript(FormField ff) {
        String v = "";
        if (ff.getValue() != null && !ff.getValue().equals("")) {
            // LogUtil.getLog(getClass()).info("StrUtil.toInt(v)=" + StrUtil.toInt(v));
        	if("全部用户".equals(ff.getValue())){
        		v = "全部用户";
        	//}else if("单位所有用户".equals(ff.getValue())){
        		//v = "单位所有用户";
        	}else {
	            UserDb user = new UserDb();
	            
	        	String[] ary = StrUtil.split(ff.getValue(), ",");
	        	for (int i=0; i<ary.length; i++) {
	                user = user.getUserDb(ary[i]);       
	                if ("".equals(v))
	                	v = user.getRealName();
	                else
	                	v += "," + user.getRealName();
	        	} 
        	}
        }
        return "ReplaceCtlWithValue('" + ff.getName() + "_realshow', '" + ff.getType() + "','" + v + "');\n"
        		+ "try{o('userList').style.display='none';}catch(e){}"
        		+ "try{$('#is_toAll_show').hide();}catch(e){}"
        		+ "try{$('#userList_show').hide();}catch(e){}";
     }

     /**
      * 用于nesttable双击单元格编辑时ajax调用
      * @param request HttpServletRequest
      * @param oldValue String 单元格原来的真实值 （如product的ID）
      * @param oldShowValue String 单元格原来的显示值（如product的名称）
      * @param objId String 单元格原来的显示值的input输入框的ID
      * @return String
      */
     @Override
	public String ajaxOnNestTableCellDBClick(HttpServletRequest request, String formCode, String fieldName,
                                              String oldValue,
                                              String oldShowValue, String objId) {
         String str = "";
         // 注意下面三行的顺序不能变
         str += "<input id=\"" + objId + "_realshow\" size=\"10\" readonly name=\"" + objId + "_realshow\" value=\"" + oldShowValue + "\">";
         str += "<input type=\"hidden\" id=\"" + objId + "\" name=\"" + objId + "\" value=\"" + oldValue + "\">";
         str += "<input type=\"button\" value=\"...\" onclick=\"openWinUserMultiSelect(" + objId + ")\">";
         return str;
     }

     @Override
	public String getDisableCtlScript(FormField ff, String formElementId) {
         String realName = "";
         if (ff.getValue() != null && !ff.getValue().equals("")) {
         	String[] ary = StrUtil.split(ff.getValue(), ",");
            UserDb user = new UserDb();
            if(ary.length==1&&("全部用户".equals(ary[0]))){
            	realName = ary[0];
            }else{
	        	for (int i=0; i<ary.length; i++) {
	                user = user.getUserDb(ary[i]);       
	                if ("".equals(realName))
	                	realName = user.getRealName();
	                else
	                	realName += "," + user.getRealName();
	        	} 
            }
         }

         String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                      "','" + realName + "','" + ff.getValue() + "');\n";
         str += " DisableCtl('" + ff.getName() + "_realshow', '" + ff.getType() +
                 "','" + "" + "','" + ff.getValue() + "');\n";
//         str += " DisableCtl('" + ff.getName() + "_btna', '" + ff.getType() +
//         "','" + "" + "','');\n";
//         str += " DisableCtl('" + ff.getName() + "_btnu', '" + ff.getType() +
//         "','" + "" + "','');\n";
//         str += " DisableCtl('" + ff.getName() + "_btnc', '" + ff.getType() +
//         "','" + "" + "','');\n";
//         str += " var tag = 0;";
//         str += " try{users = o('" + ff.getName() + "_btna').value;}";
//         str += "catch(e){tag = 1;}";
//         str += " if(tag == 0){";
//    	 str += "o('" + ff.getName() + "_btna').style.display='none';}";
//    	 str += "else{";
//    	 str += "o('" + ff.getName() + "_btnd').style.display='none';}\n";
//         str += "o('" + ff.getName() + "_btnu').style.display='none';\n";
//         str += "o('btnc').style.display='none';\n";
         
         str += "o('btnc').style.display='none';\n";
	        str += "o('userList').style.display='none';\n";
  	        str += "o('is_toAll').style.display='none';";
  	        str += "if ($('#is_toAll_show') != null && typeof($('#is_toAll_show')) != undefined){$('#is_toAll_show').hide()}";
  	        str += "if ($('#userList_show') != null && typeof($('#userList_show')) != undefined){$('#userList_show').hide()}";
         
         //str += "if (o('" + ff.getName() + "_btn')) o('" + ff.getName() + "_btn').outerHTML='';";
         return str;
     }

     public String getControlType() {
            return "";
        }

        public String getControlValue(String userName, FormField ff) {
        	return StrUtil.getNullStr(ff.getValue());
        }

        public String getControlText(String userName, FormField ff) {
			if (ff.getValue() == null || "".equals(ff.getValue())) {
				return "";
			} else {
				String str = "";
				UserDb user = new UserDb();
				String users = ff.getValue();
				String[] ary = StrUtil.split(users, ",");
				for (int i=0; i<ary.length; i++) {
					user = user.getUserDb(ary[i]);
					if ("".equals(str)) {
						str = user.getRealName();
					}
					else {
						str += "，" + user.getRealName();
					}
				}
				return str;
			}
        }

        public String getControlOptions(String userName, FormField ff) {
            return "";
    }
        
        public String getUsersByUnitCode(String tag,HttpServletRequest request){
        	String userNames = "";
        	JdbcTemplate jt = new JdbcTemplate();
        	ResultIterator ri = null;
        	ResultRecord rd = null;
        	String sql = "select name from users where isValid = 1";
        	if("dept".equals(tag)){
        		sql += " and unit_code = " +StrUtil.sqlstr(new Privilege().getUserUnitCode(request));
        	}
        	try{
        		ri = jt.executeQuery(sql);
        		while(ri.hasNext()){
        			rd = (ResultRecord)ri.next();
        			if("".equals(userNames)){
        				userNames = rd.getString(1);
        			}else{
        				userNames += ","+rd.getString(1);
        			}
        		}
        	}catch(Exception e){
				LogUtil.getLog(getClass()).error(e);
        	}
        	return userNames;
        }
        
        public String getRealNameByUnitCode(String tag,HttpServletRequest request){
        	String userNames = "";
        	JdbcTemplate jt = new JdbcTemplate();
        	ResultIterator ri = null;
        	ResultRecord rd = null;
        	String sql = "select realName from users where isValid = 1";
        	if("dept".equals(tag)){
        		sql += " and unit_code = " +StrUtil.sqlstr(new Privilege().getUserUnitCode(request));
        	}
        	try{
        		ri = jt.executeQuery(sql);
        		while(ri.hasNext()){
        			rd = (ResultRecord)ri.next();
        			if("".equals(userNames)){
        				userNames = rd.getString(1);
        			}else{
        				userNames += ","+rd.getString(1);
        			}
        		}
        	}catch(Exception e){
				LogUtil.getLog(getClass()).error(e);
        	}
        	return userNames;
        }
}
