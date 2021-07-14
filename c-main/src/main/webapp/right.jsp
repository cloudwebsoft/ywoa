<%@ page contentType="text/html;charset=gb2312" %>
<HTML><HEAD><TITLE>Left Menu</TITLE>
<link rel="stylesheet" href="oa.css">
<SCRIPT language=JavaScript src="lefttree.js"></script>

<META content="Microsoft FrontPage 4.0" name=GENERATOR></HEAD>
<BODY bgColor=white class=menubar leftMargin=4 
oncontextmenu=self.event.returnValue=false;showmenu() 
ondragstart=self.event.returnValue=false 
onselectstart=self.event.returnValue=false rightMargin=0 topMargin=8 onload="AllClose();">
<BR>
<DIV id=menutool style="DISPLAY: none">
<TABLE cellPadding=0 cellSpacing=0 class=menus>
  <TBODY>
  <TR>
        <TD width=80 height=20 align="center" bgcolor="#333333"> <NOBR><font color="#ffffff">腾图&nbsp;&nbsp;OA</font></NOBR></TD>
      </TR>  
  <TR>
    <TD height=20 onclick=AllOpen();hidemenu() 
    onmouseout="this.style.backgroundColor='';this.style.color='';" 
    onmouseover="this.style.backgroundColor='darkblue';this.style.color='white';" 
    width=80><NOBR>&nbsp;&nbsp;全部打开</NOBR></TD></TR>
  <TR>
    <TD height=20 onclick=AllClose();hidemenu() 
    onmouseout="this.style.backgroundColor='';this.style.color='';" 
    onmouseover="this.style.backgroundColor='darkblue';this.style.color='white';">
    <NOBR>&nbsp;&nbsp;全部关闭</NOBR></TD></TR>
  <TR>
    <TD height=20 onclick=self.location.reload();hidemenu() 
    onmouseout="this.style.backgroundColor='';this.style.color='';" 
    onmouseover="this.style.backgroundColor='darkblue';this.style.color='white';">
    <NOBR>&nbsp;&nbsp;刷&nbsp;&nbsp;新</NOBR></TD></TR></TBODY></TABLE></DIV>
<DIV><IMG src="images/left/icon_unctitle.gif"></DIV>
<DIV></DIV>
<DIV><img align=absMiddle alt="" border=0 src="images/left/t.gif"><IMG 
align=absMiddle alt="" border=0 src="images/left/icon_newmail.gif"> <A 
class=item href="javascript:;" 
onclick="onCoolInfo()">查邮件</A></DIV>


<DIV><IMG align=absMiddle alt="" border=0 id=xingzheng 
onclick=swapimg(xingzheng,foldx,openx) src="images/left/tminus.gif" 
style="CURSOR: hand"><img align=absMiddle alt="" border=0 class=havechild
id=openx src="images/left/icon_folderopen.gif">&nbsp;行政管理</DIV>
<DIV class=off id=foldx>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_inbox.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('tongzhi.jsp')">内部通知</A> 
</DIV>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_draft.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('learn.jsp')">文件学习</A> 
</DIV>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_sent.gif"> <A class=folderlink href="javascript:;"
onclick="load_Href('addfile.jsp')">上报文件</A> 
</DIV></DIV>

<!--- my add start -->
<DIV><IMG align=absMiddle alt="" border=0 id=public 
onclick=swapimg(public,foldu,openu) src="images/left/tminus.gif" 
style="CURSOR: hand"><IMG align=absMiddle alt="" border=0 class=havechild 
id=openu src="images/left/icon_folderopen.gif"> 公共信息</DIV>
<DIV class=off id=foldu>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_inbox.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('wtongzhi.jsp')">公共留言</A> 
</DIV>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_inbox.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('tel.jsp')">常用电话</A> 
</DIV>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_draft.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('url.jsp')">常用网址</A> 
</DIV>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_sent.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('postcode.jsp')">邮编区号</A> 
</DIV>
<DIV><NOBR><IMG align=absMiddle src="images/left//i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_delete.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('shouji.jsp')">手机IP查询</A> 
</DIV>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_delete.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('rili/cal.htm')">万年历</A> 
</DIV></DIV>

<!--- my add start -->
<DIV><IMG align=absMiddle alt="" border=0 id=communication 
onclick=swapimg(communication,foldt,opent) src="images/left/tminus.gif" 
style="CURSOR: hand"><IMG align=absMiddle alt="" border=0 class=havechild 
id=opent src="images/left/icon_folderopen.gif"> 交流中心</DIV>
<DIV class=off id=foldt>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_inbox.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('bbs.jsp')">讨论中心</A> 
</DIV>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_draft.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('chat.jsp')">会议中心</A> 
</DIV>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_sent.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('soft.jsp')">软件下载</A> 
</DIV></DIV>

<DIV><IMG align=absMiddle alt="" border=0 id=person 
onclick=swapimg(person,foldp,openp) src="images/left/tminus.gif" 
style="CURSOR: hand"><IMG align=absMiddle alt="" border=0 class=havechild 
id=openp src="images/left/icon_folderopen.gif"> 个人助理</DIV>
<DIV class=off id=foldp>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_inbox.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('txl.jsp')">通讯录</A> 
</DIV>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_draft.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('calendar.jsp')">日程安排</A> 
</DIV>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_sent.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('passwd.jsp')">修改资料</A> 
</DIV>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_sent.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('archives.jsp')">个人档案</A> 
</DIV></DIV>

<DIV><IMG align=absMiddle alt="" border=0 id=mailbox 
onclick=swapimg(mailbox,foldm,openm) src="images/left/tminus.gif" 
style="CURSOR: hand"><IMG align=absMiddle alt="" border=0 class=havechild 
id=openm src="images/left/icon_folderopen.gif"> 个人信箱</DIV>
<DIV class=off id=foldm>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_draft.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('mailbox.jsp?mailbox=0')">收件箱</A> 
</DIV>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_sent.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('mailbox.jsp?mailbox=1')">发件箱</A> 
</DIV>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_sent.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('mailbox.jsp?mailbox=2')">垃圾箱</A> 
</DIV>
</DIV>

<DIV><IMG align=absMiddle alt="" border=0 id=admin 
onclick=swapimg(admin,folda,opena) src="images/left/tminus.gif" 
style="CURSOR: hand"><IMG align=absMiddle alt="" border=0 class=havechild 
id=opena src="images/left/icon_folderopen.gif"> 超级管理</DIV>
<DIV class=off id=folda>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_sent.gif"> <A class=folderlink href="javascript:load_Href('mm.jsp')">单位管理</A> 
</DIV>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_draft.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('userchk.jsp')">用户管理</A> 
</DIV>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_inbox.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('afile.jsp')">文件管理</A> 
</DIV>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_sent.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('shouqu.jsp')">报文管理</A> 
</DIV>
</DIV>

<DIV><IMG align=absMiddle alt="" border=0 id=system 
onclick=swapimg(system,folds,opens) src="images/left/tminus.gif" 
style="CURSOR: hand"><IMG align=absMiddle alt="" border=0 class=havechild 
id=opens src="images/left/icon_folderopen.gif"> 系统管理</DIV>
<DIV class=off id=folds>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_inbox.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('admin_bckdb.jsp?kind=1')">数据备份</A> 
</DIV>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_draft.gif"> <A class=folderlink href="javascript:;" 
onclick="load_Href('admin_bckdb.jsp?kind=2')">数据恢复</A> 
</DIV>
<DIV><NOBR><IMG align=absMiddle src="images/left/i.gif"><IMG align=absMiddle 
src="images/left/t.gif"><IMG align=absMiddle alt=Folder 
src="images/left/icon_sent.gif"> <A class=folderlink href="mailto:zccraft@public.qd.sd.cn">技术支持</A> 
</DIV></DIV>
<!--  my add end-->

<DIV><IMG align=absMiddle alt="" border=0 src="images/left/l.gif"><IMG 
align=absMiddle alt="" border=0 src="images/left/icon_exit.gif"> <A 
href="javascript:onCoolExit()">退出</A></DIV>
</BODY></HTML>
