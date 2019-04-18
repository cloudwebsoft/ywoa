<%@ page contentType="text/html;charset=gb2312" %>
<%@ page import="java.io.*" import="java.text.*"%>
<%@ page import="java.net.*" %>
<%
      String reportFileHome="/reportFiles";
	   reportFileHome = application.getRealPath(reportFileHome);
	   String jspRootPath = application.getRealPath("/jsp");
	   File reportRoot = new File(reportFileHome);
	   File jspRoot = new File(jspRootPath);

	   String startMenuID = "MENU000";
	   String startJspMenuID = "JSPM000";
	   String appmap = request.getContextPath();
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
<title>目录树</title>
<link href="css/main.css" rel="stylesheet" type="text/css">
<body topMargin=0 leftMargin=0 rightMargin=0 bottomMargin=0>



<table id=titleTable width=100% cellspacing=0 cellpadding=0><tr>
	<td height="22" valign="middle" background="../images/ta51top2.jpg" style="font-size:13px" nowrap></td>
</tr>
<tr height=6><td></td></tr>
</table>
<%!
 void printReportMenu(File root,String parentMenuID,JspWriter out,String contextPath,String reportFileHome,int rootMenu_length)
 throws IOException
{
	StringBuffer spaceHtml=new StringBuffer();
	for(int i=-1;i<(parentMenuID.length()-rootMenu_length)/3;i++)
	{
		spaceHtml.append("<img  src='"+contextPath+"/img/tree/space.gif'>");
	}
	File[] fileList=root.listFiles();
	//对文件名按字母顺序排序
	int l=fileList.length;
	for(int i=1;i<l;i++){
		File tmp=fileList[i-1];
		int tmpi=i-1;
		for(int j=i;j<l;j++){
			if(tmp.getName().compareTo(fileList[j].getName())>0){
				tmp=fileList[j];
				tmpi=j;
			}
		}
		if(tmpi!=i-1){
			fileList[tmpi]=fileList[i-1];
			fileList[i-1]=tmp;
		}

	}
	for(int i=0;i<fileList.length;i++)
	{
		DecimalFormat df = new DecimalFormat( "000" );
		String menuID=parentMenuID+df.format(i);
		File tmpFile=fileList[i];
		if(tmpFile.isDirectory()&&!tmpFile.isHidden())
		{
			out.println("<DIV align='left' id='"+menuID+"Parent' >");
			out.println("<table border='0'  cellpadding='0' cellspacing='0'><tr height='10'><td height='10' nowrap align='left'>");
			out.println(spaceHtml.toString());
			out.println("<img style='font-size:12px; font-family:宋体; color:black;cursor:hand' onclick=expandIt('"+menuID+"') id='"+menuID+"IMG' src='"+contextPath+"/img/tree/menu_closed.gif'>");
			out.println("<span  style='font-size:12px; font-family:宋体; color:black;cursor:hand' onclick=expandIt('"+menuID+"')>"+tmpFile.getName()+"</span>");
			out.println("</td></tr></table>");

			out.println("<DIV align='left' id='"+menuID+"Child' style='display:none'>");
			printReportMenu(tmpFile,menuID,out,contextPath,reportFileHome,rootMenu_length);
			out.println("</DIV>");
			out.println("</DIV>");
		}
	}

	for(int i=0;i<fileList.length;i++)
	{
		DecimalFormat df = new DecimalFormat( "000" );
		String menuID=parentMenuID+df.format(i);
		File tmpFile=fileList[i];
		 if(tmpFile.isFile()&&tmpFile.getName().endsWith(".raq")&&!tmpFile.isHidden())
		{
			String report=tmpFile.getAbsolutePath().substring(reportFileHome.length()).replace('\\','/');

			String reportName=tmpFile.getName().substring(0,(tmpFile.getName().length()-4));
			//report=URLEncoder.encode(report);
			out.println("<DIV align='left' id='"+menuID+"Content' >");
			out.println("<table border='0'  cellpadding='0' cellspacing='0'><tr height='10'><td height='10' nowrap align='left'>");
			out.println(spaceHtml.toString());
			out.println("<img id='"+menuID+"IMG' src='"+contextPath+"/img/tree/report.gif'>");

			out.println("<span nowrap  id='"+menuID+"RPT' selected=false style='font-size:12px; font-family:宋体; color:black;cursor:hand' onclick=showReport('"+menuID+"') reportFile='"+report+"'>"+reportName+"</span>");
			//out.println("<a href='"+contextPath+"/showReport.jsp?report="+report+"' target='runqian'>"+reportName+"</a>");
			out.println("</td></tr></table>");
			out.println("</DIV>");
		}
		if(tmpFile.isFile()&&tmpFile.getName().endsWith(".rpg")&&!tmpFile.isHidden())
		{
			String report=tmpFile.getAbsolutePath().substring(reportFileHome.length()).replace('\\','/');

			String reportName=tmpFile.getName().substring(0,(tmpFile.getName().length()-4));
			//report=URLEncoder.encode(report);
			out.println("<DIV align='left' id='"+menuID+"Content' >");
			out.println("<table border='0'  cellpadding='0' cellspacing='0'><tr height='10'><td height='10' nowrap align='left'>");
			out.println(spaceHtml.toString());
			out.println("<img id='"+menuID+"IMG' src='"+contextPath+"/img/tree/report.gif'>");

			out.println("<span nowrap  id='"+menuID+"RPT' selected=false style='font-size:12px; font-family:宋体; color:black;cursor:hand' onclick=showReportGroup('"+menuID+"') reportFile='"+report+"'>"+reportName+"</span>");
			//out.println("<a href='"+contextPath+"/showReport.jsp?report="+report+"' target='runqian'>"+reportName+"</a>");
			out.println("</td></tr></table>");
			out.println("</DIV>");
		}
	}
}
 void printJspMenu(File root,String parentMenuID,JspWriter out,String contextPath,String reportFileHome,int rootMenu_length)
 throws IOException
{

	StringBuffer spaceHtml=new StringBuffer();
	for(int i=-1;i<(parentMenuID.length()-rootMenu_length)/3;i++)
	{
		spaceHtml.append("<img  src='"+contextPath+"/img/tree/space.gif'>");
	}
	File[] fileList=root.listFiles();
	//对文件名按字母顺序排序
	int l=fileList.length;
	for(int i=1;i<l;i++){
		File tmp=fileList[i-1];
		int tmpi=i-1;
		for(int j=i;j<l;j++){
			if(tmp.getName().compareTo(fileList[j].getName())>0){
				tmp=fileList[j];
				tmpi=j;
			}
		}
		if(tmpi!=i-1){
			fileList[tmpi]=fileList[i-1];
			fileList[i-1]=tmp;
		}

	}
	for(int i=0;i<fileList.length;i++)
	{
		DecimalFormat df = new DecimalFormat( "000" );
		String menuID=parentMenuID+df.format(i);
		File tmpFile=fileList[i];
		if(tmpFile.isDirectory()&&!tmpFile.isHidden())
		{
			out.println("<DIV align='left' id='"+menuID+"Parent' >");
			out.println(spaceHtml.toString());
			out.println("<img style='font-size:12px; font-family:宋体; color:black;cursor:hand' onclick=expandIt('"+menuID+"') id='"+menuID+"IMG' src='"+contextPath+"/img/tree/menu_closed.gif'>");
			out.println("<span  style='font-size:12px; font-family:宋体; color:black;cursor:hand' onclick=expandIt('"+menuID+"')>"+tmpFile.getName()+"</span>");
			out.println("<DIV align='left' id='"+menuID+"Child' style='display:none'>");
			printJspMenu(tmpFile,menuID,out,contextPath,reportFileHome,rootMenu_length);
			out.println("</DIV>");
			out.println("</DIV>");
		}
	}

	for(int i=0;i<fileList.length;i++)
	{
		DecimalFormat df = new DecimalFormat( "000" );
		String menuID=parentMenuID+df.format(i);
		File tmpFile=fileList[i];
		 if(tmpFile.isFile()&&!tmpFile.isHidden())
		{
			//System.out.println(tmpFile.getName());
			String report=tmpFile.getAbsolutePath().substring(reportFileHome.length()).replace('\\','/');
			//report=URLEncoder.encode(report);
			if(!tmpFile.getName().endsWith(".jsp"))
			continue;
			String reportName=tmpFile.getName().substring(0,(tmpFile.getName().length()-4));

			out.println("<DIV align='left' id='"+menuID+"Content' >");
			out.println("<table border='0'  cellpadding='0' cellspacing='0'><tr height='10'><td height='10' nowrap align='left'>");
			out.println(spaceHtml.toString());
			out.println("<img id='"+menuID+"IMG' src='"+contextPath+"/img/tree/jsp.gif'>");

			out.println("<span nowrap  id='"+menuID+"RPT' selected=false style='font-size:12px; font-family:宋体; color:black;cursor:hand' onclick=showJsp('"+menuID+"') reportFile='"+report+"'>"+reportName+"</span>");
			//out.println("<a href='"+contextPath+"/showReport.jsp?report="+report+"' target='runqian'>"+reportName+"</a>");
			out.println("</td></tr></table>");
			out.println("</DIV>");
		}
	}
}
%>
<%

			out.println("<DIV align='left' id='"+startMenuID+"Parent' >");
			out.println("<img style='font-size:12px; font-family:宋体; color:black;cursor:hand' onclick=expandIt('"+startMenuID+"') id='"+startMenuID+"IMG' src='"+request.getContextPath()+"/img/tree/menu_closed.gif'>");
			out.println("<span  style='font-size:12px; font-family:宋体; color:black;cursor:hand' onclick=expandIt('"+startMenuID+"')>报表例子</span>");
			out.println("<DIV align='left' id='"+startMenuID+"Child' style='display:none'>");
                 printReportMenu(reportRoot,startMenuID,out,request.getContextPath(),reportFileHome,startMenuID.length());
			out.println("</DIV>");
			out.println("</DIV>");


			out.println("<DIV align='left' id='"+startJspMenuID+"Parent' >");
			out.println("<img style='font-size:12px; font-family:宋体; color:black;cursor:hand' onclick=expandIt('"+startJspMenuID+"') id='"+startJspMenuID+"IMG' src='"+request.getContextPath()+"/img/tree/menu_closed.gif'>");
			out.println("<span  style='font-size:12px; font-family:宋体; color:black;cursor:hand' onclick=expandIt('"+startJspMenuID+"')>JSP演示例子</span>");
			out.println("<DIV align='left' id='"+startJspMenuID+"Child' style='display:none'>");
             printJspMenu(jspRoot,startJspMenuID,out,request.getContextPath(),jspRootPath,startJspMenuID.length());
			out.println("</DIV>");
			out.println("</DIV>");


%>
</body>
</html>

<script language="javascript">
var selectedRPTID;
var numTotal=0;
NS4 = (document.layers) ? 1 : 0;
IE4 = (document.all) ? 1 : 0;
ver4 = (NS4 || IE4) ? 1 : 0;
if (ver4) {
        with (document) {
                write("<STYLE TYPE='text/css'>");
                if (NS4) {
                        write(".parent {position:absolute; visibility:visible}");
                        write(".child {position:absolute; visibility:visible}");
                        write(".regular {position:absolute; visibility:visible}") ;
                } else {
                        write(".child {display:none}")
                }
                write("</STYLE>");
        }
}

function expandIt(blkObjName) {
	var selectDivObj1;
	selectDivObj = eval(blkObjName + "Child");
	selectImgObj = eval(blkObjName + "IMG");
	//如果被选中的的区域的子区域处于隐藏状态，则显示之
	if(selectDivObj.style.display=="none")
	{
	  for(n=blkObjName.length;n>4;n-=3)
	  {
	    idstr=blkObjName.substring(0,n);
	    divobj = eval(idstr + "Child");
	    divobj.style.display = "block";
	    imgobj = eval(idstr + "IMG");
	    imgobj.src="<%=request.getContextPath()%>/img/tree/menu_open.gif";
	  }
	}
	//否则隐藏之,显示上一级
	else
	{
	  for(n=blkObjName.length-3;n>4;n-=3)
	  {
	    idstr=blkObjName.substring(0,n);
	    divobj = eval(idstr + "Child");
	    divobj.style.display = "block";
	    imgobj = eval(idstr + "IMG");
	    imgobj.src="<%=request.getContextPath()%>/img/tree/menu_open.gif";
	  }
	   selectDivObj.style.display = "none";
	   selectImgObj.src="<%=request.getContextPath()%>/img/tree/menu_closed.gif";
	}
}

function arrange() {
	nextY=document.layers[firstInd].pageY + document.layers[firstInd].document.height;
	for (i=firstInd+1; i<document.layers.length; i++) {
	   whichEl = document.layers[i];
	   if (whichEl.visibility != "hide") {
	        whichEl.pageY = nextY;
	        nextY += whichEl.document.height;
	   }
	}
}
function showReport(menuID)
{


	if(selectedRPTID)
	{
		lastRPTObj=eval(selectedRPTID + "RPT");
		lastIMGObj=eval(selectedRPTID + "IMG");
		//alert("selectedRPTID==="+selectedRPTID)
		lastRPTObj.style.color="#84BCEA";
	}
	selectedRPTID=menuID;
	selectRPTObj = eval(menuID + "RPT");
	selectImgObj = eval(menuID + "IMG");
	selectRPTObj.style.color="#FF0000";
    parent.contentArea.location.href="<%=request.getContextPath()%>/reportJsp/showReport.jsp?raq="+selectRPTObj.reportFile;
}
function showReportGroup(menuID)
{


	if(selectedRPTID)
	{
		lastRPTObj=eval(selectedRPTID + "RPT");
		lastIMGObj=eval(selectedRPTID + "IMG");
		//alert("selectedRPTID==="+selectedRPTID)
		lastRPTObj.style.color="#84BCEA";
	}
	selectedRPTID=menuID;
	selectRPTObj = eval(menuID + "RPT");
	selectImgObj = eval(menuID + "IMG");
	selectRPTObj.style.color="#FF0000";
    parent.contentArea.location.href="<%=request.getContextPath()%>/reportJsp/showReportGroup.jsp?rpg="+selectRPTObj.reportFile;
}
function showJsp(menuID)
{
	if(selectedRPTID)
	{
		lastRPTObj=eval(selectedRPTID + "RPT");
		lastIMGObj=eval(selectedRPTID + "IMG");
		//alert("selectedRPTID==="+selectedRPTID)
		lastRPTObj.style.color="#84BCEA";
	}
	selectedRPTID=menuID;
	selectRPTObj = eval(menuID + "RPT");
	selectImgObj = eval(menuID + "IMG");
	selectRPTObj.style.color="#FF0000";
    parent.contentArea.location.href="<%=request.getContextPath()%>/jsp"+selectRPTObj.reportFile;
}
</script>
