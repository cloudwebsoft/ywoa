package com.redmoon.oa.flow.macroctl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.Config;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.WorkflowPredefineDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.stamp.StampDb;
import com.redmoon.oa.stamp.StampPriv;

/**
 * <?xml version='1.0' encoding='utf-8'?> <myaction id="myactionId">
 * <content></content> <realName></realName> <time></time> </myaction>
 * 白马湖项目
 * 扩展支持手写签名
 * @author Administrator
 * 
 */
public class OpinionExCtl extends AbstractMacroCtl {

	private final static int ORDER_TIME_DESC = 0;
	private final static int ORDER_TIME_ASC = 1;
	// private final static int HAND_WRITING = 1;
	private final static int INPUT_WRITING = 0;

	public OpinionExCtl() {
	}

	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		StringBuffer sb = new StringBuffer();
		if (request.getAttribute("isOpinionJS") == null) {
			sb.append("<link type=\"text/css\" rel=\"stylesheet\" href=\""
					+ request.getContextPath()
					+ "/flow/macro/macro_opinion_ctl.css\" />");
			request.setAttribute("isOpinionJS", "y");
		}

		String val = StrUtil.getNullString(ff.getValue());
		long myActionId = StrUtil.toLong((String) request
				.getAttribute("myActionId"), -1);

		// 如果之前有写过，则将意见置于输入框中
		UserDb user = new UserDb();
		Privilege pvg = new Privilege();
		user = user.getUserDb(pvg.getUser(request));
		String cnt = "";
		if (val.equals(ff.getDefaultValue())) {
			cnt = val;
			val = "";
		} else {
			cnt = StrUtil.getNullStr(getOpinionProp(val, myActionId,
					"content"));
		}
		
		sb.append("<div><textarea class='opinionTextarea' id='" + ff.getName()
				+ "' name='" + ff.getName() + "' rows=8 cols=80 title='意见框'>"
				+ cnt + "</textarea>");

		StampPriv sp = new StampPriv();
		StampDb sd = sp.getPersonalStamp(user.getName());

		sb.append("<div id='" + ff.getName() + "_sign' class='opinionCurUT'>");
		sb
				.append("<span id='"
						+ ff.getName()
						+ "_btn' onmouseover=\"tipPhrase('"
						+ ff.getName()
						+ "', this)\"><img src=\""
						+ request.getContextPath()
						+ "/images/@flowico_13.png\" width=\"23\" height=\"23\" align=\"absmiddle\" title=\"常用语句\" />&nbsp;&nbsp;</span>");

		if (sd != null) {
			sb.append("<span class='opinionUser'><img src='"
					+ request.getContextPath()+ "/showImg.do?path=" + sd.getImageUrl()
					+ "' /></span><span class='opinionTime'>"
					+ DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm")
					+ "</span>");
		} else {
			sb.append("<span class='opinionUser'>签名：" + user.getRealName()
					+ "</span><span class='opinionTime'>"
					+ DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm")
					+ "</span>");
		}
		sb.append("</div>");
		sb.append("</div>");

		sb.append(rend(request, val, myActionId));

		return sb.toString();
	}

	/**
	 * 获取用来保存宏控件原始值的表单中的HTML元素中保存的值，生成用以给控件赋值的脚本
	 * 
	 * @return String
	 */
	public String getSetCtlValueScript(HttpServletRequest request,
			IFormDAO IFormDao, FormField ff, String formElementId) {
		return "";
	}

	/**
	 * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
	 * 
	 * @return String
	 */
	public String getDisableCtlScript(FormField ff, String formElementId) {
		String str = "try{o('" + ff.getName()
				+ "_sign').style.display = 'none';}catch(e){}\r\n";
		str += "DisableCtl('" + ff.getName() + "', '" + ff.getType()
				+ "','', '');\n";
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
		String str = "o('" + ff.getName()
				+ "_sign').style.display = 'none';\r\n";
		str += "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType()
				+ "','');\n";
		return str;
	}

	@Override
    public Object getValueForSave(FormField ff, int flowId, FormDb fd,
                                  FileUpload fu) {
		// 意见框，追加数据
		// 取得以前的数据，ff中已经是数据库中的数据了，因为20130406 fgf 修改了FormDAO.update方法，使其load了数据
		long myActionId = StrUtil.toLong(fu.getFieldValue("myActionId"), -1);
		long actionId = StrUtil.toLong(fu.getFieldValue("actionId"), -1);
		String valueFromDb = "";
		FormDAO fdao = new FormDAO(flowId, fd);
		fdao.load();
		Vector vts = fdao.getFields();
		Iterator irt = vts.iterator();
		while (irt.hasNext()) {
			FormField ff2 = (FormField) irt.next();
			if (ff2.getName().equals(ff.getName())) {
				valueFromDb = StrUtil.getNullStr(ff2.getValue()).trim();
				LogUtil.getLog(getClass()).info(
						"save: valueFromDb=" + valueFromDb + " ff.getValue()="
								+ ff.getValue());
				break;
			}
		}
		WorkflowActionDb wa = new WorkflowActionDb();
		wa = wa.getWorkflowActionDb((int) actionId);

		WorkflowDb wf = new WorkflowDb();
		wf = wf.getWorkflowDb(flowId);

		Leaf lf = new Leaf();
		lf = lf.getLeaf(wf.getTypeCode());

		String[] fds = null;

		if (lf.getType() == Leaf.TYPE_FREE) {
			WorkflowPredefineDb wfpd = new WorkflowPredefineDb();
			wfpd = wfpd.getPredefineFlowOfFree(wf.getTypeCode());
			Privilege pvg = new Privilege();
			fds = wfpd.getFieldsWriteOfUser(wf, pvg.getUser(fu.getRequest()));
		} else {
			String fieldWrite = StrUtil.getNullString(wa.getFieldWrite())
					.trim();
			fds = fieldWrite.split(",");
		}

		int len = fds.length;
		boolean isEditable = false;
		if (fds != null && len > 0) {
			for (String editable : fds) {
				if (editable.equals(ff.getName())) {
					isEditable = true;
					break;
				}
			}
		}
		/**
		 * 判断是不是手写板
		 */
		String key = ff.getName() + "_isHandWrite";
		String isHandWriteStr = StrUtil.getNullStr(fu.getFieldValue(key));
		String opinion = fu.getFieldValue(ff.getName());
		if (isHandWriteStr.equals("")) {
			isHandWriteStr = String.valueOf(INPUT_WRITING);
		}
		return makeOpinion(valueFromDb, myActionId, opinion, isEditable,
				isHandWriteStr, valueFromDb.equals(ff.getDefaultValue()));
	}

	public String getControlType() {
		return "textarea";
	}

	public String converToHtml(HttpServletRequest request, FormField ff,
			String fieldValue) {
		if (fieldValue != null && !fieldValue.equals("")) {
			return rendForMobile(fieldValue, "web");
		} else {
			return "";
		}
	}

	public String rendForMobile(String content, String type) {
		return rendForMobile(null, content, type);
	}

	public String rendForMobile(HttpServletRequest request, String content,
			String type) {
		JSONArray jsonArray = null;
		StringBuilder sb = new StringBuilder();
		try {
			SAXBuilder parser = new SAXBuilder();
			org.jdom.Document doc = parser.build(new InputSource(
					new StringReader(content)));
			Element root = doc.getRootElement();
			List v = root.getChildren();
			int size = v.size();
			if (size > 0) {
				Config cfg = new Config();
				int order = cfg.getInt("opinion_ctl_order");
				// 倒排
				switch (order) {
				case ORDER_TIME_DESC:
					for (int i = size - 1; i >= 0; i--) {
						Element e = (Element) v.get(i);
						if (type != null && !type.equals("")) {
							if (type.equals("web")) {
								sb.append(pareseElement(request, e));
							} else if (type.equals("mobile")) {
								JSONObject opinion = pareseElementToJSON(
										request, e);

								if (jsonArray == null) {
									jsonArray = new JSONArray();
								}
								jsonArray.put(opinion);
							}
						}
					}
					break;
				case ORDER_TIME_ASC:
					for (int i = 0; i < size; i++) {
						Element e = (Element) v.get(i);
						if (type != null && !type.equals("")) {
							if (type.equals("web")) {
								sb.append(pareseElement(request, e));
							} else if (type.equals("mobile")) {
								JSONObject opinion = pareseElementToJSON(
										request, e);

								if (jsonArray == null) {
									jsonArray = new JSONArray();
								}
								jsonArray.put(opinion);
							}
						}
					}
					break;
				default:
					break;
				}
				if (jsonArray != null) {
					sb = new StringBuilder(jsonArray.toString());
				}
			}
		} catch (IOException ex) {
			// LogUtil.getLog(getClass()).error(ex);
		} catch (JDOMException ex) {
			// LogUtil.getLog(getClass()).error(ex);
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return sb.toString();
	}

	public String getControlText(String userName, FormField ff) {
		if (ff.getValue() != null && !ff.getValue().equals("")) {
			return rendForMobile(ff.getValue(), "mobile");
		} else {
			return "";
		}
	}

	public String getControlValue(String userName, FormField ff) {
		String cnt = "";
		StampPriv sp = new StampPriv();
		UserDb userDb = new UserDb(userName);
		StampDb sd = sp.getPersonalStamp(userName);
		JSONObject opinion = new JSONObject();
		int isHandWrite = INPUT_WRITING;
		if (ff.getValue() != null) {
			cnt = StrUtil.getNullStr(getOpinionProp(ff.getValue(), myActionId,
					"content"));
			String isHandWriteStr = StrUtil.getNullStr(getOpinionProp(ff
					.getValue(), myActionId, "isHandWrite"));
			if (!isHandWriteStr.equals("")) {
				isHandWrite = Integer.parseInt(isHandWriteStr);
			}
		}
		try {
			opinion.put("myActionId", myActionId);
			opinion.put("opinionRealName", userDb.getRealName());
			opinion.put("opinionName", userName);// 审批人姓名
			opinion.put("isHandWrite", isHandWrite);// 是否是手写板
			if (isHandWrite == INPUT_WRITING) {
				String temp = "";
				try {
					temp = URLDecoder.decode(cnt, "utf-8");
				} catch (Exception ex) {
					temp = cnt;
				}
				opinion.put("opinionContent", temp);// 审批意见
			} else {
				opinion.put("opinionContent", cnt);// 审批意见
			}
			if (sd != null) {
				opinion.put("existStamp", true);
			} else {
				opinion.put("existStamp", false);
			}
			if (sd != null) {
				opinion.put("existStamp", true);
			} else {
				opinion.put("existStamp", false);
			}
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(
					"JSONeException==" + e.getMessage());
		}
		return opinion.toString();
	}

	public String getControlOptions(String userName, FormField ff) {
		return "";
	}

	/**
	 * 组装意见，删除原来的意见，添加新意见
	 * 
	 * @param content
	 * @param myActionId
	 * @return
	 */
	public String makeOpinion(String content, long myActionId, String opinion,
			boolean isEditable, String isHandWrite, boolean isDefaultValue) {
		MyActionDb mad = new MyActionDb();
		mad = mad.getMyActionDb(myActionId);
		UserDb user = new UserDb();
		user = user.getUserDb(mad.getUserName());

		opinion = StrUtil.getNullStr(opinion);

		// 如果原来意见为空或者意见为默认值
		if (content.equals("") || isDefaultValue) {
			if (opinion.equals("")) {
				if (isEditable) {
					String val = "<?xml version=\"1.0\" encoding=\"utf-8\"?><myactions><myaction id=\""
							+ myActionId
							+ "\"><content>"
							+ "</content><userName>"
							+ user.getName()
							+ "</userName><realName>"
							+ user.getRealName()
							+ "</realName><time>"
							+ DateUtil.format(new java.util.Date(),
									"yyyy-MM-dd HH:mm:ss")
							+ "</time>"
							+ "<isHandWrite>"
							+ isHandWrite
							+ "</isHandWrite>"
							+ "</myaction></myactions>";
					return val;
				} else {
					String val = "<?xml version=\"1.0\" encoding=\"utf-8\"?><myactions></myactions>";
					return val;
				}

			} else {
				String val = "<?xml version=\"1.0\" encoding=\"utf-8\"?><myactions><myaction id=\""
						+ myActionId
						+ "\"><content>"
						+ StrUtil.UrlEncode(opinion)
						+ "</content><userName>"
						+ user.getName()
						+ "</userName><realName>"
						+ user.getRealName()
						+ "</realName><time>"
						+ DateUtil.format(new java.util.Date(),
								"yyyy-MM-dd HH:mm:ss")
						+ "</time>"
						+ "<isHandWrite>"
						+ isHandWrite
						+ "</isHandWrite></myaction></myactions>";
				return val;
			}
		}

		try {
			SAXBuilder parser = new SAXBuilder();
			org.jdom.Document doc = parser.build(new InputSource(
					new StringReader(content)));
			Element root = doc.getRootElement();
			List v = root.getChildren();
			if (v.size() > 0) {
				Iterator ir = v.iterator();
				while (ir.hasNext()) {
					Element e = (Element) ir.next();
					// 删除原先的意见
					if (e.getAttribute("id").getValue().equals(
							String.valueOf(myActionId))) {
						root.removeContent(e);
						break;
					}
				}
			}

			// 添加新意见
			if (isEditable) {
				Element e = new Element("myaction");
				e.setAttribute("id", String.valueOf(myActionId));
				e.addContent(new Element("content").setText(StrUtil
						.getNullStr(opinion)));
				e.addContent(new Element("userName").setText(user.getName()));
				e.addContent(new Element("realName")
						.setText(user.getRealName()));
				e.addContent(new Element("isHandWrite").setText(isHandWrite));
				e.addContent(new Element("time").setText(DateUtil.format(
						new java.util.Date(), "yyyy-MM-dd HH:mm:ss")));
				root.addContent(e);

				ByteArrayOutputStream byteRsp = new ByteArrayOutputStream();
				String indent = "    ";
				Format format = Format.getPrettyFormat();
				format.setIndent(indent);
				format.setEncoding("utf-8");
				XMLOutputter xmlOut = new XMLOutputter(format);
				try {
					xmlOut.output(doc, byteRsp);
					content = byteRsp.toString("utf-8");
				} catch (Exception ex) {
					LogUtil.getLog(getClass()).error(ex);
				}
			}
		} catch (IOException ex) {
			LogUtil.getLog(getClass()).error(ex);
		} catch (JDOMException ex) {
			LogUtil.getLog(getClass()).error(ex);
		}

		return content;
	}
	
	/**
	 * 组装意见，删除原来的意见，添加新意见
	 * 
	 * @param content
	 * @param myActionId
	 * @return
	 */
	public String makeOpinion(String content, long myActionId, String opinion, String isHandWrite,
			boolean isEditable) {
		return makeOpinion(content, myActionId, opinion, isEditable, isHandWrite, false);
	}

	/**
	 * 不显示当前myActionId对应的意见
	 * 
	 * @param content
	 * @param myActionId
	 * @return
	 */
	public String rend(HttpServletRequest request, String content,
			long myActionId) {
		StringBuffer sb = new StringBuffer();
		try {
			SAXBuilder parser = new SAXBuilder();
			org.jdom.Document doc = parser.build(new InputSource(
					new StringReader(content)));
			Element root = doc.getRootElement();
			List v = root.getChildren();
			int size = v.size();
			if (size > 0) {
				Config cfg = new Config();
				int order = cfg.getInt("opinion_ctl_order");
				// 倒排
				switch (order) {
				case ORDER_TIME_DESC:
					for (int i = size - 1; i >= 0; i--) {
						Element e = (Element) v.get(i);
						if (e.getAttribute("id").getValue().equals(
								String.valueOf(myActionId))) {
							continue;
						}
						sb.append(pareseElement(request, e));
					}
					break;
				case ORDER_TIME_ASC:
					for (int i = 0; i < size; i++) {
						Element e = (Element) v.get(i);
						if (e.getAttribute("id").getValue().equals(
								String.valueOf(myActionId))) {
							continue;
						}
						sb.append(pareseElement(request, e));
					}
					break;
				}
			}
		} catch (IOException ex) {
			LogUtil.getLog(getClass()).error(ex);
		} catch (JDOMException ex) {
			// LogUtil.getLog(getClass()).error(ex);
		}
		return sb.toString();
	}

	/**
	 * 取得节点上的属性
	 *
	 * @param property
	 * @return
	 */
	public String getOpinionProp(String content, long myActionId,
			String property) {
		if (content.equals(""))
			return "";

		// WorkflowPredefineDb wpd = new WorkflowPredefineDb();
		// wpd = wpd.getDefaultPredefineFlow(flowTypeCode);

		try {
			SAXBuilder parser = new SAXBuilder();

			org.jdom.Document doc = parser.build(new InputSource(
					new StringReader(content)));

			Element root = doc.getRootElement();
			Iterator ir = root.getChildren().iterator();
			while (ir.hasNext()) {
				Element e = (Element) ir.next();
				// LogUtil.getLog("WorkflowActionDb").info("getActionProperty: internalName="
				// + e.getAttribute("internalName").getValue());
				if (e.getAttribute("id").getValue().equals(
						String.valueOf(myActionId))) {
					String prop = e.getChildText(property);
					return prop;
				}
			}
		} catch (IOException ex) {
			LogUtil.getLog(getClass()).error(ex);
		} catch (JDOMException ex) {
			LogUtil.getLog(getClass()).error(ex);
		}

		return null;
	}

	private String pareseElement(HttpServletRequest request, Element e)
			throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		String cnt = e.getChildText("content").trim();
		String isHandWriteStr = StrUtil.getNullStr(e
				.getChildText("isHandWrite"));
		int isHandWrite = INPUT_WRITING;
		if (!isHandWriteStr.equals("")) {
			isHandWrite = Integer.parseInt(isHandWriteStr);
		}

		if (!"".equals(cnt)) {
			sb.append("<div id='opinion" + e.getAttribute("id").getValue()
					+ "' class='opinion'>");
			sb.append("<div class='opinionCnt'>");
			if (isHandWrite == INPUT_WRITING) {
				String temp = "";
				try {
					temp = URLDecoder.decode(cnt, "utf-8");
				} catch (Exception ex) {
					temp = cnt;
				}
				sb.append(temp.replaceAll("\r\n", "</br>").replaceAll("\n",
						"</br>"));
			} else {
				sb
						.append(
								"<img class='base64-imgview' src='data:image/png;base64,")
						.append(cnt).append("'  />");
			}
			sb.append("</div>");
			StampPriv sp = new StampPriv();
			StampDb sd = sp.getPersonalStamp(e.getChildText("userName"));

			if (sd != null) {
				sb
						.append("<div class='opinionUT'><span class='opinionUser'><img src='"
								+ request.getContextPath()+ "/showImg.do?path=" + sd.getImageUrl()
								+ "' /></span><span class='opinionTime'>"
								+ DateUtil.format(DateUtil.parse(e
										.getChildText("time"),
										"yyyy-MM-dd HH:mm:ss"),
										"yyyy-MM-dd HH:mm") + "</span></div>");
			} else {
				sb.append("<div class='opinionUT'><span class='opinionUser'>"
						+ e.getChildText("realName")
						+ "</span><span class='opinionTime'>"
						+ DateUtil.format(DateUtil.parse(
								e.getChildText("time"), "yyyy-MM-dd HH:mm:ss"),
								"yyyy-MM-dd HH:mm") + "</span></div>");
			}
			sb.append("</div>");
		}
		return sb.toString();
	}

	private JSONObject pareseElementToJSON(HttpServletRequest request, Element e)
			throws UnsupportedEncodingException, JSONException {
		JSONObject opinion = new JSONObject();
		String isHandWriteStr = StrUtil.getNullStr(e
				.getChildText("isHandWrite"));
		int isHandWrite = INPUT_WRITING;
		if (!isHandWriteStr.equals("")) {
			isHandWrite = Integer.parseInt(isHandWriteStr);
		}
		String contentOpinion = e.getChildText("content");
		opinion.put("myActionId", Long.parseLong(e.getAttribute("id")
				.getValue()));
		opinion.put("opinionRealName", e.getChildText("realName"));// 审批人真实姓名
		opinion.put("opinionName", e.getChildText("userName"));// 审批人姓名
		if (!contentOpinion.equals("null")) {
			if (isHandWrite == INPUT_WRITING) {
				String temp = "";
				try {
					temp = URLDecoder.decode(contentOpinion, "utf-8");
				} catch (Exception ex) {
					temp = contentOpinion;
				}
				opinion.put("opinionContent", temp);// 审批意见
			} else {
				opinion.put("opinionContent", contentOpinion);
			}
		} else {
			opinion.put("opinionContent", "");// 审批意见
		}
		opinion.put("isHandWrite", isHandWrite);

		String dtStr = DateUtil.format(DateUtil.parse(e.getChildText("time"),
				"yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm");// 审批时间
		opinion.put("opinionTime", dtStr);
		StampPriv sp = new StampPriv();
		StampDb sd = sp.getPersonalStamp(e.getChildText("userName"));
		if (sd != null) {
			opinion.put("existStamp", true);
			// opinion.put("stampUrl","public/flow_getfile.jsp?op=stamp");
		} else {
			opinion.put("existStamp", false);
		}
		return opinion;
	}
}
