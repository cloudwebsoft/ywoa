<%@ page contentType="text/html;charset=gb2312"%>
<%@ page import="com.redmoon.oa.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%
Date d = new Date();
d = DateUtil.addDate(d, -90);
out.print(d.getTime());
// TwoDimensionCode twcode = new TwoDimensionCode();
// twcode.encoderQRCode("http://www.yimihome.com/download_app2.1.html", "d:/2.1.png");		
%>

<div id="drag_612" class="portlet drag_div bor" style="border:0px;padding:0px;" >
    <div id="drag_612_h" style="height:3px;padding:0px;margin:0px; font-size:1px"></div>
    <div id="cont_612" class="portlet_content" style="min-height:141px;_height:141px;padding:0px;margin:0px">
    <div>
    	<section class="slider">
        <div id="flexslider612" class="flexslider">
        <ul class="slides">
        
                  <li>
                  <div class="">
                  <img src="upfile/file_folder/2017/4/14918292637391084758088.png" />
                  </div>
                  </li>
            
                  <li>
                  <div class="">
                  <img src="upfile/file_folder/2017/4/14918290560012112413376.jpg" />
                  </div>
                  </li>
                
      	</ul>
        </div>
        </section>
        
        
        
    </div>
  </div>
</div>

<script>
jQuery(window).load(function(){
  jQuery('#flexslider612').flexslider({
	animation: "slide",
	start: function(slider){
	}
  });
});	
</script>