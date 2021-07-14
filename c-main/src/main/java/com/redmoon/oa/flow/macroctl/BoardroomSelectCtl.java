package com.redmoon.oa.flow.macroctl;

import java.util.Iterator;

import cn.js.fan.util.StrUtil;

import com.redmoon.oa.meeting.BoardroomDb;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redmoon.oa.flow.FormField;

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
public class BoardroomSelectCtl extends AbstractMacroCtl  {
    public BoardroomSelectCtl() {
    }

	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		String str = "";
		if (ff.isEditable()) {
			str += "<select name='" + ff.getName() + "'>";
			BoardroomDb bd = new BoardroomDb();
			Iterator ir = bd.list().iterator();
			int i = 0;
			while (ir.hasNext()) {
				bd = (BoardroomDb) ir.next();
				str += "<option value='" + bd.getId() + "'"
						+ (i++ == 0 ? " selected" : "") + ">" + bd.getName()
						+ "</option>";
			}
			str += "</select>";
		} else {
			String value = String.valueOf(ff.getValue());
			if (value.trim().equals("")) {
				return "";
			}
			BoardroomDb bd = new BoardroomDb(Integer.parseInt(ff.getValue()));
			if (!bd.isLoaded()) {
				return "";
			}
			str += "<span>" + bd.getName() + "</span>";
		}

		return str;
	}
    
    public String getDisableCtlScript(FormField ff, String formElementId) {
        String v = StrUtil.getNullStr(ff.getValue());

        if (!v.equals("")) {
        	BoardroomDb bd = new BoardroomDb();
        	bd = bd.getBoardroomDb(StrUtil.toInt(v));
        	v = bd.getName();
       }
       String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                "','" + v + "','" + v + "');\n";
       return str;    	
    }    

    @Override
    public String convertToHTMLCtlForQuery(HttpServletRequest request, FormField ff) {
        String str = "";
        str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "'>";
        str += "<option value=''>无</option>";
        BoardroomDb bd = new BoardroomDb();
        Iterator ir = bd.list().iterator();
        while (ir.hasNext()) {
            bd = (BoardroomDb)ir.next();
            str += "<option value='" + bd.getId() + "'>" + bd.getName() +
                    "</option>";
        }
        str += "</select>";
        return str;
    }

      public String getControlType() {
          return "select";
      }

      public String getControlValue(String userName, FormField ff) {
          return ff.getValue();
      }

      public String getControlText(String userName, FormField ff) {
          String v = StrUtil.getNullStr(ff.getValue());

          if (!v.equals("")) {
          	BoardroomDb bd = new BoardroomDb();
          	bd = bd.getBoardroomDb(StrUtil.toInt(v));
          	v = bd.getName();
          }
          return v;
      }

      public String getControlOptions(String userName, FormField ff) {
          BoardroomDb bd = new BoardroomDb();
          Iterator ir = bd.list().iterator();
          JSONArray selects = new JSONArray();
          while (ir.hasNext()) {
              bd = (BoardroomDb)ir.next();
              JSONObject select = new JSONObject();
              try {
                  select.put("name", bd.getName());
                  select.put("value",String.valueOf(bd.getId()));
                  selects.put(select);
              } catch (JSONException ex) {
                  ex.printStackTrace();
              }
          }
          return selects.toString();
      }

      public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
          String v = StrUtil.getNullStr(fieldValue);

          if (!v.equals("")) {
          	BoardroomDb bd = new BoardroomDb();
          	bd = bd.getBoardroomDb(StrUtil.toInt(v));
          	v = bd.getName();
          }
          return v;
      }     

      public String getReplaceCtlWithValueScript(FormField ff) {
    	  String v = StrUtil.getNullStr(ff.getValue());

          if (!v.equals("")) {
          	BoardroomDb bd = new BoardroomDb();
          	bd = bd.getBoardroomDb(StrUtil.toInt(v));
          	v = bd.getName();
          }
          
          String str = "if (o('" + ff.getName() + "_realname')) o('" + ff.getName() + "_realname').parentNode.removeChild(o('" + ff.getName() + "_realname'));\n";
          str += "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "','" + v + "');\n";
          return str;
      }
}
