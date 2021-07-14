<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.help.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import="java.io.*"%>
<%
com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();

int id = 0;
String type = ParamUtil.get(request, "type");
String dirCode = ParamUtil.get(request, "dir_code");
boolean isDirArticle = false;
Leaf lf = new Leaf();

Document doc = null;
DocumentMgr docmgr = new DocumentMgr();
UserMgr um = new UserMgr();

if (!dirCode.equals("")) {
	lf = lf.getLeaf(dirCode);
	if (lf!=null) {
		if (lf.getType()==1) {
			// id = lf.getDocID();
			doc = docmgr.getDocumentByCode(request, dirCode, privilege);
			id = doc.getID();
			isDirArticle = true;
		}
	}
}

if (id==0) {
	try {
		id = ParamUtil.getInt(request, "id");
		doc = docmgr.getDocument(id);
	}
	catch (ErrMsgException e) {
		out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
}

if (!doc.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "该文章不存在！"));
	return;
}

// System.out.println(getClass() + " type=" + type);

int size = ParamUtil.getInt(request, "size", 0);

String content = "";
if (type.equals("title")) {
	content = doc.getTitle();
}
else {
	content = doc.getContent(1);
}

if (size>0) {
	content = StrUtil.getAbstract(request, content, size);
	content += "...";
}
%>
</head>
<body>
<style>
a.helpLink {
	color:white;
	text-decoration:none;
}
a.helpLink:hover {
	color:yellow;
}
</style>
<a class="helpLink" href="javascript:;" onClick="addTab('帮助', '<%=request.getContextPath()%>/help/doc_show.jsp?id=<%=id%>')"><%=content%></a>
</body>
</html>
