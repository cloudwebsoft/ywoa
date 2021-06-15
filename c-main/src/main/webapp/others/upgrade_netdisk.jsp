<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.io.*"%>
<%@ page import="com.cloudwebsoft.framework.util.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.util.file.*"%>
<%@ page import="cn.js.fan.db.*"%>
<html>   <head><title>更新网络硬盘虚拟路径</title><meta http-equiv="Content-Type" content="text/html; charset=utf-8"></head>
   <body>
   <%
	String sql = "select * from netdisk_document_attach order by user_name";
	String pre = "upfile/file_netdisk/";
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = jt.executeQuery(sql);
	Document doc = new Document();
	while (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		String name = rr.getString("name");
		String visualPath = rr.getString("visualpath");
		String fullPath = Global.getRealPath() + visualPath + "/" + rr.getString("diskname");
		doc = doc.getDocument(rr.getInt("doc_id"));
		String dirCode = doc.getDirCode();
		
		Leaf lf = new Leaf();
		lf = lf.getLeaf(dirCode);
		if (lf==null)
			continue;
		
		String parentcode = lf.getParentCode();
		Leaf plf = new Leaf();
		String filePath = "";
		if (!parentcode.equals("-1")) {
			// 非根目录取节点名称
			filePath = lf.getName();
			while (!parentcode.equals(lf.getRootCode())) {
				plf = plf.getLeaf(parentcode);
				if (plf==null)
					break;
				parentcode = plf.getParentCode();
				filePath = plf.getName() + "/" + filePath;
			}
			filePath = lf.getRootCode() + "/" + filePath;
		}
		else {
			// 根目录取节点编码
			filePath = lf.getCode();
		}
				
		// 更新visualpath
		visualPath = filePath;
		
%><%
		
		// 创建转换以后的目录
		String newPath = Global.getRealPath() + "upfile/file_netdisk/" + visualPath;
		File f = new File(newPath);
		
		if (!f.isDirectory())
		 	f.mkdirs();
			
		String newFullPath = newPath + "/" + name;
		out.print("原文件夹--" + fullPath + "&nbsp;&nbsp;&nbsp;新文件夹---" + newFullPath + "<BR>");
		
		// 拷贝至新目录
		FileUtil.CopyFile(fullPath, newFullPath);
		
		// 更新数据库
		String sql2 = "update netdisk_document_attach set visualpath=" + StrUtil.sqlstr(visualPath) + " where id=" + rr.getInt("id");
		jt.executeUpdate(sql2);
		
		// 删除原来文件
		File f2 = new File(fullPath);
		f2.delete();
	}   

    out.println("---------------请手工删除原来文件夹下的垃圾文件及文件夹-----------------<BR>");
   %>
   </body>
</html> 
