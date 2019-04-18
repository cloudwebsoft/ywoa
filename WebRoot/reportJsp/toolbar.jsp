<%@ page contentType="text/html;charset=utf-8" %>
<%	String appmap = request.getContextPath();
	String printImage = "<img src='" + appmap + "/images/print.gif' border=no >";
	String excelImage = "<img src='" + appmap + "/images/excel.gif' border=no >";
	String pdfImage = "<img src='" + appmap + "/images/pdf.gif' border=no >";
    String wordImage = "<img src='" + appmap + "/images/doc.gif' border=no >";
	String firstPageImage = "<img src='" + appmap + "/images/firstpage.gif' border=no >";
	String lastPageImage = "<img src='" + appmap + "/images/lastpage.gif' border=no >";
	String nextPageImage = "<img src='" + appmap + "/images/nextpage.gif' border=no >";
	String prevPageImage = "<img src='" + appmap + "/images/prevpage.gif' border=no >";
	String submitImage = "<img src='" + appmap + "/images/savedata.gif' border=no >";
%>

<div class="btnBar">
  <ul class="left">
    <!--<li class="borderRight submitLi" onClick="_submitTable( report1 );return false;" href="#"> <a title="提交" href="#" class="submit"></a></li>-->
    <li class="toggleBg borderRight">
      <ul class="fileOper">
        <li><a class="ICOhover" href="#" onClick="report1_print();return false;"><span title="打印" class="print"></span></a></li>
        <li><a class="ICOhover" href="#" onClick="report1_saveAsExcel();return false;"><span title="导出excel" class="excel"></span></a></li>
        <li><a class="ICOhover" href="#" onClick="report1_saveAsPdf();return false;"><span title="导出pdf" class="pdf"></span></a></li>
        <li><a class="ICOhover" href="#" onClick="report1_saveAsWord();return false;"><span title="导出word" class="word"></span></a></li>
       </ul>
    </li>
    <li class="floatRight borderLeft">
      <ul class="fileOper">
         <Li><a class="ICOhover" href="#" onClick="try{report1_toPage( 1 );}catch(e){}return false;"><span title="首页" class="begin"></span></a></li>
        <li><a class="ICOhover" href="#" onClick="try{report1_toPage(report1_getCurrPage()-1);}catch(e){}return false;"><span title="上一页" class="pre"></span></a></li>
        <Li><a class="ICOhover" href="#" onClick="try{report1_toPage(report1_getCurrPage()+1);}catch(e){}return false;"><span title="下一页" class="next"></span></a></li>
        <li><a class="ICOhover" href="#" onClick="try{report1_toPage(report1_getTotalPage());}catch(e){}return false;"><span title="尾页" class="end"></span></a></li>    
      </ul>
    </li>
    <li class="floatRight">  <div style="display:inline-block; margin:9px 4px 3px 4px; float:left; ">共<span id="t_page_span"></span>页/第<span id="c_page_span"></span>页&nbsp;&nbsp;</div></li>
  </ul>

</div>
