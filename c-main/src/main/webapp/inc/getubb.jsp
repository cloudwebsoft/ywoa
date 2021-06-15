<%@ page contentType="text/html;charset=gb2312"%>
<img onclick=Cbold() src="sq/forum/images/UBB/bold.gif" width="23" height="22" alt="粗体" border="0"> 
<img onclick=Citalic() src="sq/forum/images/UBB/italicize.gif" width="23" height="22" alt="斜体" border="0"> 
<img onclick=Cunder() src="sq/forum/images/UBB/underline.gif" width="23" height="22" alt="下划线" border="0"> 
<img onclick=Ccenter() src="sq/forum/images/UBB/center.gif" width="23" height="22" alt="居中" border="0"> 
<img onclick=Curl() src="sq/forum/images/UBB/url1.gif" width="23" height="22" alt="超级连接" border="0"> 
<img onclick=Cemail() src="sq/forum/images/UBB/email1.gif" width="23" height="22" alt="Email连接" border="0"> 
<img onclick=Cimage() src="sq/forum/images/UBB/image.gif" width="23" height="22" alt="图片" border="0"> 
<img onclick=Cswf() src="sq/forum/images/UBB/swf.gif" width="23" height="22" alt="Flash图片" border="0"> 
<img onclick=Cdir() src="sq/forum/images/UBB/Shockwave.gif" width="23" height="22" alt="Shockwave文件" border="0"> 
<img onclick=Crm() src="sq/forum/images/UBB/rm.gif" width="23" height="22" alt="realplay视频文件" border="0"> 
<img onclick=Cwmv() src="sq/forum/images/UBB/mp.gif" width="23" height="22" alt="Media Player视频文件" border="0"> 
<img onclick=Cmov() src="sq/forum/images/UBB/qt.gif" width="23" height="22" alt="QuickTime视频文件" border="0"> 
<img onclick=Cquote() src="sq/forum/images/UBB/quote1.gif" width="23" height="22" alt="引用" border="0"> 
<IMG onclick=Cfly() height=22 alt=飞行字 src="sq/forum/images/UBB/fly.gif" width=23 border=0> 
<IMG onclick=Cmarquee() height=22 alt=移动字 src="sq/forum/images/UBB/move.gif" width=23 border=0> 
<IMG onclick=Cguang() height=22 alt=发光字 src="sq/forum/images/UBB/glow.gif" width=23 border=0> 
<IMG onclick=Cying() height=22 alt=阴影字 src="sq/forum/images/UBB/shadow.gif" width=23 border=0> 
<br>
字体：
<SELECT onchange="if(this.options[this.selectedIndex].value!=''){showfont(this.options[this.selectedIndex].value);this.options[0].selected=true;}else {this.selectedIndex=0;}" name=font>
<option value="宋体" selected>宋体</option>
<option value="楷体_GB2312">楷体</option>
<option value="新宋体">新宋体</option>
<option value="黑体">黑体</option>
<option value="隶书">隶书</option>
<OPTION value="Andale Mono">Andale Mono</OPTION> 
<OPTION value=Arial>Arial</OPTION> 
<OPTION value="Arial Black">Arial Black</OPTION> 
<OPTION value="Book Antiqua">Book Antiqua</OPTION>
<OPTION value="Century Gothic">Century Gothic</OPTION> 
<OPTION value="Comic Sans MS">Comic Sans MS</OPTION>
<OPTION value="Courier New">Courier New</OPTION>
<OPTION value=Georgia>Georgia</OPTION>
<OPTION value=Impact>Impact</OPTION>
<OPTION value=Tahoma>Tahoma</OPTION>
<OPTION value="Times New Roman" >Times New Roman</OPTION>
<OPTION value="Trebuchet MS">Trebuchet MS</OPTION>
<OPTION value="Script MT Bold">Script MT Bold</OPTION>
<OPTION value=Stencil>Stencil</OPTION>
<OPTION value=Verdana>Verdana</OPTION>
<OPTION value="Lucida Console">Lucida Console</OPTION>
</SELECT>&nbsp;&nbsp;&nbsp;&nbsp;
字体大小：<select name="size" onChange="if(this.options[this.selectedIndex].value!=''){showsize(this.options[this.selectedIndex].value);this.options[0].selected=true;}else {this.selectedIndex=0;}">
	<option value="1">1</option>
	<option value="2">2</option>
	<option value="3" selected>3</option>
	<option value="4">4</option>
</select>
&nbsp;&nbsp;&nbsp;<font face="宋体" color=#333333>颜色：</font>
<SELECT onchange="if(this.options[this.selectedIndex].value!=''){showcolor(this.options[this.selectedIndex].value);this.options[0].selected=true;}else {this.selectedIndex=0;}" name=color> 
<option style="background-color:#F0F8FF;color: #F0F8FF" value="#F0F8FF">#F0F8FF</option>
<option style="background-color:#FAEBD7;color: #FAEBD7" value="#FAEBD7">#FAEBD7</option>
<option style="background-color:#00FFFF;color: #00FFFF" value="#00FFFF">#00FFFF</option>
<option style="background-color:#7FFFD4;color: #7FFFD4" value="#7FFFD4">#7FFFD4</option>
<option style="background-color:#F0FFFF;color: #F0FFFF" value="#F0FFFF">#F0FFFF</option>
<option style="background-color:#F5F5DC;color: #F5F5DC" value="#F5F5DC">#F5F5DC</option>
<option style="background-color:#FFE4C4;color: #FFE4C4" value="#FFE4C4">#FFE4C4</option>
<option style="background-color:#000000;color: #000000" value="#000000">#000000</option>
<option style="background-color:#FFEBCD;color: #FFEBCD" value="#FFEBCD">#FFEBCD</option>
<option style="background-color:#0000FF;color: #0000FF" value="#0000FF">#0000FF</option>
<option style="background-color:#8A2BE2;color: #8A2BE2" value="#8A2BE2">#8A2BE2</option>
<option style="background-color:#A52A2A;color: #A52A2A" value="#A52A2A">#A52A2A</option>
<option style="background-color:#DEB887;color: #DEB887" value="#DEB887">#DEB887</option>
<option style="background-color:#5F9EA0;color: #5F9EA0" value="#5F9EA0">#5F9EA0</option>
<option style="background-color:#7FFF00;color: #7FFF00" value="#7FFF00">#7FFF00</option>
<option style="background-color:#D2691E;color: #D2691E" value="#D2691E">#D2691E</option>
<option style="background-color:#FF7F50;color: #FF7F50" value="#FF7F50">#FF7F50</option>
<option style="background-color:#6495ED;color: #6495ED" value="#6495ED" selected>#6495ED</option>
<option style="background-color:#FFF8DC;color: #FFF8DC" value="#FFF8DC">#FFF8DC</option>
<option style="background-color:#DC143C;color: #DC143C" value="#DC143C">#DC143C</option>
<option style="background-color:#00FFFF;color: #00FFFF" value="#00FFFF">#00FFFF</option>
<option style="background-color:#00008B;color: #00008B" value="#00008B">#00008B</option>
<option style="background-color:#008B8B;color: #008B8B" value="#008B8B">#008B8B</option>
<option style="background-color:#B8860B;color: #B8860B" value="#B8860B">#B8860B</option>
<option style="background-color:#A9A9A9;color: #A9A9A9" value="#A9A9A9">#A9A9A9</option>
<option style="background-color:#006400;color: #006400" value="#006400">#006400</option>
<option style="background-color:#BDB76B;color: #BDB76B" value="#BDB76B">#BDB76B</option>
<option style="background-color:#8B008B;color: #8B008B" value="#8B008B">#8B008B</option>
<option style="background-color:#556B2F;color: #556B2F" value="#556B2F">#556B2F</option>
<option style="background-color:#FF8C00;color: #FF8C00" value="#FF8C00">#FF8C00</option>
<option style="background-color:#9932CC;color: #9932CC" value="#9932CC">#9932CC</option>
<option style="background-color:#8B0000;color: #8B0000" value="#8B0000">#8B0000</option>
<option style="background-color:#E9967A;color: #E9967A" value="#E9967A">#E9967A</option>
<option style="background-color:#8FBC8F;color: #8FBC8F" value="#8FBC8F">#8FBC8F</option>
<option style="background-color:#483D8B;color: #483D8B" value="#483D8B">#483D8B</option>
<option style="background-color:#2F4F4F;color: #2F4F4F" value="#2F4F4F">#2F4F4F</option>
<option style="background-color:#00CED1;color: #00CED1" value="#00CED1">#00CED1</option>
<option style="background-color:#9400D3;color: #9400D3" value="#9400D3">#9400D3</option>
<option style="background-color:#FF1493;color: #FF1493" value="#FF1493">#FF1493</option>
<option style="background-color:#00BFFF;color: #00BFFF" value="#00BFFF">#00BFFF</option>
<option style="background-color:#696969;color: #696969" value="#696969">#696969</option>
<option style="background-color:#1E90FF;color: #1E90FF" value="#1E90FF">#1E90FF</option>
<option style="background-color:#B22222;color: #B22222" value="#B22222">#B22222</option>
<option style="background-color:#FFFAF0;color: #FFFAF0" value="#FFFAF0">#FFFAF0</option>
<option style="background-color:#228B22;color: #228B22" value="#228B22">#228B22</option>
<option style="background-color:#FF00FF;color: #FF00FF" value="#FF00FF">#FF00FF</option>
<option style="background-color:#DCDCDC;color: #DCDCDC" value="#DCDCDC">#DCDCDC</option>
<option style="background-color:#F8F8FF;color: #F8F8FF" value="#F8F8FF">#F8F8FF</option>
<option style="background-color:#FFD700;color: #FFD700" value="#FFD700">#FFD700</option>
<option style="background-color:#DAA520;color: #DAA520" value="#DAA520">#DAA520</option>
<option style="background-color:#808080;color: #808080" value="#808080">#808080</option>
<option style="background-color:#008000;color: #008000" value="#008000">#008000</option>
<option style="background-color:#ADFF2F;color: #ADFF2F" value="#ADFF2F">#ADFF2F</option>
<option style="background-color:#F0FFF0;color: #F0FFF0" value="#F0FFF0">#F0FFF0</option>
<option style="background-color:#FF69B4;color: #FF69B4" value="#FF69B4">#FF69B4</option>
<option style="background-color:#CD5C5C;color: #CD5C5C" value="#CD5C5C">#CD5C5C</option>
<option style="background-color:#4B0082;color: #4B0082" value="#4B0082">#4B0082</option>
<option style="background-color:#FFFFF0;color: #FFFFF0" value="#FFFFF0">#FFFFF0</option>
<option style="background-color:#F0E68C;color: #F0E68C" value="#F0E68C">#F0E68C</option>
<option style="background-color:#E6E6FA;color: #E6E6FA" value="#E6E6FA">#E6E6FA</option>
<option style="background-color:#FFF0F5;color: #FFF0F5" value="#FFF0F5">#FFF0F5</option>
<option style="background-color:#7CFC00;color: #7CFC00" value="#7CFC00">#7CFC00</option>
<option style="background-color:#FFFACD;color: #FFFACD" value="#FFFACD">#FFFACD</option>
<option style="background-color:#ADD8E6;color: #ADD8E6" value="#ADD8E6">#ADD8E6</option>
<option style="background-color:#F08080;color: #F08080" value="#F08080">#F08080</option>
<option style="background-color:#E0FFFF;color: #E0FFFF" value="#E0FFFF">#E0FFFF</option>
<option style="background-color:#FAFAD2;color: #FAFAD2" value="#FAFAD2">#FAFAD2</option>
<option style="background-color:#90EE90;color: #90EE90" value="#90EE90">#90EE90</option>
<option style="background-color:#D3D3D3;color: #D3D3D3" value="#D3D3D3">#D3D3D3</option>
<option style="background-color:#FFB6C1;color: #FFB6C1" value="#FFB6C1">#FFB6C1</option>
<option style="background-color:#FFA07A;color: #FFA07A" value="#FFA07A">#FFA07A</option>
<option style="background-color:#20B2AA;color: #20B2AA" value="#20B2AA">#20B2AA</option>
<option style="background-color:#87CEFA;color: #87CEFA" value="#87CEFA">#87CEFA</option>
<option style="background-color:#778899;color: #778899" value="#778899">#778899</option>
<option style="background-color:#B0C4DE;color: #B0C4DE" value="#B0C4DE">#B0C4DE</option>
<option style="background-color:#FFFFE0;color: #FFFFE0" value="#FFFFE0">#FFFFE0</option>
<option style="background-color:#00FF00;color: #00FF00" value="#00FF00">#00FF00</option>
<option style="background-color:#32CD32;color: #32CD32" value="#32CD32">#32CD32</option>
<option style="background-color:#FAF0E6;color: #FAF0E6" value="#FAF0E6">#FAF0E6</option>
<option style="background-color:#FF00FF;color: #FF00FF" value="#FF00FF">#FF00FF</option>
<option style="background-color:#800000;color: #800000" value="#800000">#800000</option>
<option style="background-color:#66CDAA;color: #66CDAA" value="#66CDAA">#66CDAA</option>
<option style="background-color:#0000CD;color: #0000CD" value="#0000CD">#0000CD</option>
<option style="background-color:#BA55D3;color: #BA55D3" value="#BA55D3">#BA55D3</option>
<option style="background-color:#9370DB;color: #9370DB" value="#9370DB">#9370DB</option>
<option style="background-color:#3CB371;color: #3CB371" value="#3CB371">#3CB371</option>
<option style="background-color:#7B68EE;color: #7B68EE" value="#7B68EE">#7B68EE</option>
<option style="background-color:#00FA9A;color: #00FA9A" value="#00FA9A">#00FA9A</option>
<option style="background-color:#48D1CC;color: #48D1CC" value="#48D1CC">#48D1CC</option>
<option style="background-color:#C71585;color: #C71585" value="#C71585">#C71585</option>
<option style="background-color:#191970;color: #191970" value="#191970">#191970</option>
<option style="background-color:#F5FFFA;color: #F5FFFA" value="#F5FFFA">#F5FFFA</option>
<option style="background-color:#FFE4E1;color: #FFE4E1" value="#FFE4E1">#FFE4E1</option>
<option style="background-color:#FFE4B5;color: #FFE4B5" value="#FFE4B5">#FFE4B5</option>
<option style="background-color:#FFDEAD;color: #FFDEAD" value="#FFDEAD">#FFDEAD</option>
<option style="background-color:#000080;color: #000080" value="#000080">#000080</option>
<option style="background-color:#FDF5E6;color: #FDF5E6" value="#FDF5E6">#FDF5E6</option>
<option style="background-color:#808000;color: #808000" value="#808000">#808000</option>
<option style="background-color:#6B8E23;color: #6B8E23" value="#6B8E23">#6B8E23</option>
<option style="background-color:#FFA500;color: #FFA500" value="#FFA500">#FFA500</option>
<option style="background-color:#FF4500;color: #FF4500" value="#FF4500">#FF4500</option>
<option style="background-color:#DA70D6;color: #DA70D6" value="#DA70D6">#DA70D6</option>
<option style="background-color:#EEE8AA;color: #EEE8AA" value="#EEE8AA">#EEE8AA</option>
<option style="background-color:#98FB98;color: #98FB98" value="#98FB98">#98FB98</option>
<option style="background-color:#AFEEEE;color: #AFEEEE" value="#AFEEEE">#AFEEEE</option>
<option style="background-color:#DB7093;color: #DB7093" value="#DB7093">#DB7093</option>
<option style="background-color:#FFEFD5;color: #FFEFD5" value="#FFEFD5">#FFEFD5</option>
<option style="background-color:#FFDAB9;color: #FFDAB9" value="#FFDAB9">#FFDAB9</option>
<option style="background-color:#CD853F;color: #CD853F" value="#CD853F">#CD853F</option>
<option style="background-color:#FFC0CB;color: #FFC0CB" value="#FFC0CB">#FFC0CB</option>
<option style="background-color:#DDA0DD;color: #DDA0DD" value="#DDA0DD">#DDA0DD</option>
<option style="background-color:#B0E0E6;color: #B0E0E6" value="#B0E0E6">#B0E0E6</option>
<option style="background-color:#800080;color: #800080" value="#800080">#800080</option>
<option style="background-color:#FF0000;color: #FF0000" value="#FF0000">#FF0000</option>
<option style="background-color:#BC8F8F;color: #BC8F8F" value="#BC8F8F">#BC8F8F</option>
<option style="background-color:#4169E1;color: #4169E1" value="#4169E1">#4169E1</option>
<option style="background-color:#8B4513;color: #8B4513" value="#8B4513">#8B4513</option>
<option style="background-color:#FA8072;color: #FA8072" value="#FA8072">#FA8072</option>
<option style="background-color:#F4A460;color: #F4A460" value="#F4A460">#F4A460</option>
<option style="background-color:#2E8B57;color: #2E8B57" value="#2E8B57">#2E8B57</option>
<option style="background-color:#FFF5EE;color: #FFF5EE" value="#FFF5EE">#FFF5EE</option>
<option style="background-color:#A0522D;color: #A0522D" value="#A0522D">#A0522D</option>
<option style="background-color:#C0C0C0;color: #C0C0C0" value="#C0C0C0">#C0C0C0</option>
<option style="background-color:#87CEEB;color: #87CEEB" value="#87CEEB">#87CEEB</option>
<option style="background-color:#6A5ACD;color: #6A5ACD" value="#6A5ACD">#6A5ACD</option>
<option style="background-color:#708090;color: #708090" value="#708090">#708090</option>
<option style="background-color:#FFFAFA;color: #FFFAFA" value="#FFFAFA">#FFFAFA</option>
<option style="background-color:#00FF7F;color: #00FF7F" value="#00FF7F">#00FF7F</option>
<option style="background-color:#4682B4;color: #4682B4" value="#4682B4">#4682B4</option>
<option style="background-color:#D2B48C;color: #D2B48C" value="#D2B48C">#D2B48C</option>
<option style="background-color:#008080;color: #008080" value="#008080">#008080</option>
<option style="background-color:#D8BFD8;color: #D8BFD8" value="#D8BFD8">#D8BFD8</option>
<option style="background-color:#FF6347;color: #FF6347" value="#FF6347">#FF6347</option>
<option style="background-color:#40E0D0;color: #40E0D0" value="#40E0D0">#40E0D0</option>
<option style="background-color:#EE82EE;color: #EE82EE" value="#EE82EE">#EE82EE</option>
<option style="background-color:#F5DEB3;color: #F5DEB3" value="#F5DEB3">#F5DEB3</option>
<option style="background-color:#FFFFFF;color: #FFFFFF" value="#FFFFFF">#FFFFFF</option>
<option style="background-color:#F5F5F5;color: #F5F5F5" value="#F5F5F5">#F5F5F5</option>
<option style="background-color:#FFFF00;color: #FFFF00" value="#FFFF00">#FFFF00</option>
<option style="background-color:#9ACD32;color: #9ACD32" value="#9ACD32">#9ACD32</option>
</SELECT>