package com.redmoon.oa.flow.macroctl;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.post.PostDb;
import com.redmoon.oa.post.PostUserDb;
import com.redmoon.oa.post.PostUserMgr;
import com.redmoon.oa.pvg.Privilege;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormField;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class CurUserPostSelectCtl extends AbstractMacroCtl {
	public CurUserPostSelectCtl() {
	}

	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		StringBuilder sb = new StringBuilder();
		int post_id = 0;
		String value = StrUtil.getNullStr(ff.getValue());
		if(value.equals("")){
			PostUserMgr pum = new PostUserMgr();
			Privilege privilege = new Privilege();
			String userName = privilege.getUser(request);
			pum.setUserName(userName);
			PostUserDb pud = pum.postByUserName();
			if(pud != null && pud.isLoaded()){
				 post_id = pud.getInt("post_id");
			}
		}else{
			post_id = Integer.parseInt(value);
		}
		
		if(post_id != 0){
			PostDb pd = new PostDb();
			pd = pd.getPostDb(post_id);
			if(pd.isLoaded() ){
				String pName = StrUtil.getNullStr(pd.getString("name"));
				if(ff.isEditable()){
					sb.append("<span>").append(pName).append("</span>");
					sb.append("<input type='text' id='").append(ff.getName())
							.append("'").append(" name = '").append(ff.getName()).append("'").append(" value='").append(post_id).append(
									"' />");
				}else{
					sb.append("<span name = '"+ff.getName()+"'>").append(pName).append("</span>");
				}
				
			}
		}else{
			//当没有岗位的时候  默认显示o
			if(ff.isEditable()){
				sb.append("<input type='text' id='").append(ff.getName())
				.append("'").append(" name = '").append(ff.getName()).append("'").append(" value='").append(post_id).append(
						"' />");
			}
		}
		
		
		return sb.toString();
	}

	@Override
	public String getControlOptions(String userName, FormField arg1) {
		// TODO Auto-generated method stub
	
		return "";
	}

	@Override
	public String getControlText(String userName, FormField ff) {
		// TODO Auto-generated method stub
		String value = StrUtil.getNullStr(ff.getValue());
		if(value.equals("")){
			PostUserMgr pum = new PostUserMgr();
			pum.setUserName(userName);
			PostUserDb pud = pum.postByUserName();
			if(pud != null && pud.isLoaded()){
				value = String.valueOf(pud.getInt("post_id"));
			}
		}
		return postNameById(value);
	}

	@Override
	public String getControlType() {
		// TODO Auto-generated method stub
		return "text";
	}

	@Override
	public String getControlValue(String userName, FormField ff) {
		// TODO Auto-generated method stub
		String value = StrUtil.getNullStr(ff.getValue());
		if(value.equals("")){
			PostUserMgr pum = new PostUserMgr();
			pum.setUserName(userName);
			PostUserDb pud = pum.postByUserName();
			if(pud != null && pud.isLoaded()){
				value = String.valueOf(pud.getInt("post_id"));
			}
		}
		return value;
	}

	public String getSetCtlValueScript(HttpServletRequest request,
			IFormDAO IFormDao, FormField ff, String formElementId) {
			String value = StrUtil.getNullStr(ff.getValue());
			String str = "";
		if (value.equals("") && ff.isEditable()) {
			str += "o('" + ff.getName() + "').style.display='none';";
			PostUserMgr pum = new PostUserMgr();
			Privilege privilege = new Privilege();
			String userName = privilege.getUser(request);
			pum.setUserName(userName);
			PostUserDb pud = pum.postByUserName();
			if(pud != null && pud.isLoaded()){
				 int post_id = pud.getInt("post_id");
				 str += "setCtlValue('" + ff.getName() + "', '"+ff.getType()+"', '" + post_id + "');\n";
			}
		} else {
			
			str += "setCtlValue('" + ff.getName() + "', '"+ff.getType()+"', '" + value + "');\n";
			

		}
		return str;
	}

	public String converToHtml(HttpServletRequest request, FormField ff,
			String fieldValue) {
		return postNameById(fieldValue);
	}

	public String getReplaceCtlWithValueScript(FormField ff) {

		return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType()
				+ "','" + postNameById(ff.getValue()) + "');\n";
	}

	public String postNameById(String id) {
		String res = "";
		try {
			if (id != null && !id.equals("")) {
				PostDb postDb = new PostDb();
				postDb = postDb.getPostDb(Integer.parseInt(id));
				if (postDb != null && postDb.isLoaded()) {
					res = postDb.getString("name");
				}
			}

		} catch (java.lang.NumberFormatException e) {

		}

		return res;

	}
}
