<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.android.Privilege"/>
<%
	/*
	 - 功能描述：取得文章评论列表
	 - 访问规则：来自手机客户端
	 - 过程描述：
	 - 注意事项：
	 - 创建者：fgf 
	 - 创建时间：2013-9-8
	 ==================
	 - 修改者：
	 - 修改时间：
	 - 修改原因
	 - 修改点：
	 */

	// System.out.println(getClass() + " here");
	String skey = ParamUtil.get(request, "skey");
	JSONObject json = new JSONObject();
	boolean re = privilege.Auth(skey);
	if (re) {
		try {
			json.put("res", "-2");
			json.put("msg", "时间过期");
			out.print(json.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}

	String userName = privilege.getUserName(skey);
	int id = ParamUtil.getInt(request, "id");
	Comment cmt = new Comment();
	String sql = "select id from cms_comment where doc_id=" + id
			+ " order by id desc";

	int curpage = ParamUtil.getInt(request, "pagenum", 1); //第几页
	int pagesize = ParamUtil.getInt(request, "pagesize", 10); //每页显示多少条

	try {
		ListResult lr = cmt.listResult(sql, curpage, pagesize);
		int total = lr.getTotal();
		json.put("res", "0");
		json.put("msg", "操作成功");
		json.put("total", String.valueOf(total));
		Vector v = lr.getResult();
		Iterator ir = null;
		if (v != null)
			ir = v.iterator();
		JSONObject result = new JSONObject();
		UserDb user = new UserDb();
		result.put("count", String.valueOf(pagesize));
		JSONArray wldArray = new JSONArray();
		while (ir != null && ir.hasNext()) {
			cmt = (Comment) ir.next();
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("id", String.valueOf(cmt.getId()));
			jsonObj.put("date", cmt.getAddDate());
			// wlds.put("content",privilege.delHTMLTag(StrUtil.getLeft(wld.getContent(), 120)));				
			jsonObj.put("content", StrUtil.getAbstract(request, cmt
					.getContent(), 20000, "\r\n").trim());
			jsonObj.put("realName", user.getUserDb(cmt.getNick()).getRealName());
			wldArray.put(jsonObj);
		}
		result.put("comments", wldArray);
		json.put("result", result);
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (ErrMsgException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	out.print(json);
%>