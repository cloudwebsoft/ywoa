<%--Listfile.jsp--%>
<%@ page import="java.io.*"%>
<%@ page import="com.cloudwebsoft.framework.util.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.util.file.*"%>
<%@ page contentType="text/html;charset=GB2312" language="java" %>
<html>   <head><title>遍历文件目录</title></head>
   <body>
   <%!
   // 恢复网盘文件
   UserDb user;
   UserMgr um = new UserMgr();
       public  void travelDirectory(String directory,JspWriter out) throws IOException,ErrMsgException
       {
           File dir = new File(directory);
           if(dir.isFile())            //判断是否是文件，如果是文件则返回。
               return;
           File [] files=dir.listFiles();        //列出当前目录下的所有文件和目录
           for(int i=0;i<files.length;i++)
           {
               if(files[i].isDirectory()) {      //如果是目录，则继续遍历该目录
                   travelDirectory(files[i].getAbsolutePath(),out);
				   // out.print(files[i].getAbsolutePath() + "<BR>");
			   }
			   
			   if (!files[i].isDirectory()) {
			   	
				   out.println( files[i].getName() + "---");    //输出该目录或者文件的名字
				   out.println( files[i].getAbsolutePath() + "<BR>");    //输出该目录或者文件的名字
				   // 检查该文件路径中，处于第几级
				   String path = files[i].getAbsolutePath();
				   String[] ary = path.split("\\\\");
				  
				   
				   String realName = ary[3];
				   String userName = Cn2Spell.converterToFirstSpell(realName);
					// 从第三个文件夹开始，分别是用户名、目录名
					// 检查该用户是否已存在，如果不存在则创建该用户
					user = um.getUserDb(userName);
					
					if (!user.isLoaded()) {
						user.create(userName, realName, "1", "139", "root");
						Leaf lf = new Leaf();
						lf.initRootOfUser(userName);
						// 为用户创建文件夹
						File f = new File(Global.realPath + "upfile/file_netdisk/" + userName);
						if (!f.isDirectory()) {
							f.mkdirs();
						}
					}
					// 为该目录创建文档
					com.redmoon.oa.netdisk.Document doc = new com.redmoon.oa.netdisk.Document();
					int docId = doc.getIDOrCreateByCode(userName, userName);
					
					System.out.println("docId:" + docId);

					
					// 将文件拷贝至用户文件夹的根目录内
					String srcFile = path;
					String ext = FileUtil.getFileExt(srcFile);
					String diskFileName = RandomSecquenceCreator.getId(20) + "." + ext;
					String fileName = ary[ary.length-1];
					
					FileUtil.CopyFile(srcFile, Global.realPath + "upfile/file_netdisk/" + userName + "/" + diskFileName );
					// 为文档添加附件
					com.redmoon.oa.netdisk.Attachment att = new com.redmoon.oa.netdisk.Attachment();
					att.setDocId(docId);
					att.setName(fileName);
					att.setDiskName(diskFileName);
					
				   // 检查该用户的文件夹是否在数据库中已存在，如果不存在，则为其建立，并将文件放至该用户的文件夹内
				   String visualPath = "upfile/file_netdisk/" + userName;
				   /*
				   for (int k=3; k<ary.length-1; k++) {
					visualPath += "/" + ary[k];
				   }
				   */
					System.out.println("srcFile:" + srcFile);
					System.out.println("newPath:" + Global.realPath + "upfile/file_netdisk/" + userName + "/" + diskFileName);
					System.out.println("visualPath:" + visualPath);
					
					att.setVisualPath(visualPath);
					att.setPageNum(1);
					att.setOrders(1);
					// 取得文件的大小
					long size = 0;
					File f = new File(srcFile);
					if (f.exists())
						size = f.length();				
					att.setSize(size);
					att.setExt(ext);
					// att.setUploadDate(new java.util.Date());
					att.setUserName(userName);
					att.create();
				}
				
           }
       }   %>
    <%
       //将当前web程序目录结构输出到控制台
       String dir="e:/userbak/file_folder";//pageContext.getServletContext().getRealPath("/images");
       out.println("--------------------------------<BR>");
       travelDirectory(dir,out);
       out.println("--------------------------------<BR>");
   %>
   </body>
</html> 
