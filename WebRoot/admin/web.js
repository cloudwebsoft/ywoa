doc_html_html="<html><head>\n<title>�����ı��༭��</title>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=gb2312\">\n\n<style type=\"text/css\">\n.yToolbar\n{\n}\nTABLE.Toolbar\n{\n	BORDER-RIGHT: #F8FCF8 1px solid;\n}\nTABLE.Toolbar TD\n{\n	BACKGROUND-COLOR: #D0D0C8;\n	BORDER-BOTTOM: #808080	1px solid;\n	BORDER-RIGHT: #808080 1px solid;\n	BORDER-TOP:	#F8FCF8	1px solid;\n	HEIGHT: 27px;\n	LEFT: 0px;\n	POSITION: relative;\n	TOP: 0px;\n}\n.Btn\n{\n	BACKGROUND-COLOR: #D0D0C8;\n	BORDER-BOTTOM: #D0D0C8 1px solid;\n	BORDER-LEFT: #D0D0C8 1px	solid;\n	BORDER-RIGHT: #D0D0C8 1px solid;\n	BORDER-TOP:	#D0D0C8 1px solid;\n	HEIGHT: 23px;\n	POSITION: absolute;\n	TOP: 1px;\n	WIDTH: 23px;\n}\n.TBSep\n{\n	BORDER-LEFT: #808080 1px solid;\n	BORDER-RIGHT: #F8FCF8 1px solid;\n	FONT-SIZE: 0px;\n	HEIGHT: 22px;\n	POSITION: absolute;\n	TOP: 1px;\n	WIDTH:1px\n}\n.TBGen\n{\n	FONT: 8pt arial,sans-serif;\n	HEIGHT: 22px;\n	POSITION: absolute;\n	TOP: 2px\n}\n.TBHandle\n{\n	BACKGROUND-COLOR: #D0D0C8;\n	BORDER-LEFT: #F8FCF8 1px solid;\n	BORDER-RIGHT: #808080 1px solid;\n	BORDER-TOP:	#F8FCF8	1px solid;\n	FONT-SIZE: 1px;\n	HEIGHT: 22px;\n	POSITION: absolute;\n	TOP: 1px;\n	WIDTH: 3px\n}\n.Ico\n{\n	HEIGHT: 22px;\n	LEFT: -1px;\n	POSITION: absolute;\n	TOP: -1px;\n	WIDTH: 22px\n}\n.BtnMouseOverUp\n{\n	BACKGROUND-COLOR: #B5BED6;\n	BORDER-BOTTOM: #08246B	1px solid;\n	BORDER-LEFT: #08246B 1px solid;\n	BORDER-RIGHT: #08246B 1px solid;\n	BORDER-TOP:	#08246B	1px solid;\n	HEIGHT: 23px;\n	POSITION: absolute;\n	TOP: 1px;\n	WIDTH: 24px\n}\n.BtnMouseOverDown\n{\n	BACKGROUND-COLOR: #8492B5;\n	BORDER-BOTTOM: #08246B 1px solid;\n	BORDER-LEFT: #08246B 1px solid;\n	BORDER-RIGHT: #08246B 1px solid;\n	BORDER-TOP:	#08246B 1px solid;\n	HEIGHT: 23px;\n	POSITION: absolute;\n	TOP: 1px;\n	WIDTH: 24px\n}\n.BtnDown\n{\n	BACKGROUND-COLOR: #DCDCDC;\n	BORDER-BOTTOM: #F8FCF8 1px solid;\n	BORDER-LEFT: #808080 1px solid;\n	BORDER-RIGHT: #F8FCF8 1px solid;\n	BORDER-TOP:	#808080 1px solid;\n	HEIGHT: 23px;\n	POSITION: absolute;\n	TOP: 1px;\n	WIDTH: 24px\n}\n.IcoDown\n{\n	HEIGHT: 23px;\n	LEFT: 0px;\n	POSITION: absolute;\n	TOP: 0px;\n	WIDTH: 24px\n}\n.IcoDownPressed\n{\n	LEFT: 1px;\n	POSITION: absolute;\n	TOP: 1px\n}\n\nBODY\n{\n	BACKGROUND-COLOR:#FFFFFF;\n	MARGIN: 0px;\n	PADDING: 0px;\n}\nSELECT\n{\n    BACKGROUND: #eeeeee;\n    FONT: 8pt verdana,arial,sans-serif\n}\nTABLE\n{\n    POSITION: relative\n}\n.Composition\n{\n    BACKGROUND-COLOR: #cccccc;\n    POSITION: relative\n}\n</style>\n\n<Script Language=Javascript>\n/*\n*������������������������������������\n*��                                                                  ��\n*��                  WebEditor�����ı��༭��                         ��\n*��                                                                  ��\n*��  ��Ȩ����: downlove.com                                          ��\n*��                                                                  ��\n*��  ��������: ��ҫ                                                  ��\n*��            email:ayao@downlove.com                               ��\n*��            ���������                                            ��\n*��                                                                  ��\n*������������������������������������\n*/\n\n// ������汾���\nvar BrowserInfo = new Object() ;\nBrowserInfo.MajorVer = navigator.appVersion.match(/MSIE (.)/)[1] ;\nBrowserInfo.MinorVer = navigator.appVersion.match(/MSIE .\\.(.)/)[1] ;\nBrowserInfo.IsIE55OrMore = BrowserInfo.MajorVer >= 6 || ( BrowserInfo.MajorVer >= 5 && BrowserInfo.MinorVer >= 5 ) ;\n\nvar yToolbars = new Array();  // ����������\n\n// ���ĵ���ȫ����ʱ�����г�ʼ��\nvar bInitialized = false;\nfunction document.onreadystatechange(){\n	if (document.readyState!=\"complete\") return;\n	if (bInitialized) return;\n	bInitialized = true;\n\n	var i, s, curr;\n\n	// ��ʼÿ��������\n	for (i=0; i<document.body.all.length;i++){\n		curr=document.body.all[i];\n		if (curr.className == \"yToolbar\"){\n			InitTB(curr);\n			yToolbars[yToolbars.length] = curr;\n		}\n	}\n\n	if (ContentFlag.value==\"0\") { \n		ContentEdit.value = objContent.value;\n		ContentLoad.value = objContent.value;\n		ContentFlag.value = \"1\";\n	}\n\n	WebEditor.document.designMode=\"On\";\n	WebEditor.document.open();\n	WebEditor.document.write(bodyTag+ContentEdit.value)\n	WebEditor.document.close();\n	setLinkedField() ;\n	WebEditor.document.body.onpaste = onPaste ;\n	WebEditor.focus();\n}\n\n// ��ʼ��һ���������ϵİ�ť\nfunction InitBtn(btn) {\n	btn.onmouseover = BtnMouseOver;\n	btn.onmouseout = BtnMouseOut;\n	btn.onmousedown = BtnMouseDown;\n	btn.onmouseup = BtnMouseUp;\n	btn.ondragstart = YCancelEvent;\n	btn.onselectstart = YCancelEvent;\n	btn.onselect = YCancelEvent;\n	btn.YUSERONCLICK = btn.onclick;\n	btn.onclick = YCancelEvent;\n	btn.YINITIALIZED = true;\n	return true;\n}\n\n//Initialize a toolbar. \nfunction InitTB(y) {\n	// Set initial size of toolbar to that of the handle\n	y.TBWidth = 0;\n		\n	// Populate the toolbar with its contents\n	if (! PopulateTB(y)) return false;\n	\n	// Set the toolbar width and put in the handle\n	y.style.posWidth = y.TBWidth;\n	\n	return true;\n}\n\n\n// Hander that simply cancels an event\nfunction YCancelEvent() {\n	event.returnValue=false;\n	event.cancelBubble=true;\n	return false;\n}\n\n// Toolbar button onmouseover handler\nfunction BtnMouseOver() {\n	if (event.srcElement.tagName != \"IMG\") return false;\n	var image = event.srcElement;\n	var element = image.parentElement;\n	\n	// Change button look based on current state of image.\n	if (image.className == \"Ico\") element.className = \"BtnMouseOverUp\";\n	else if (image.className == \"IcoDown\") element.className = \"BtnMouseOverDown\";\n\n	event.cancelBubble = true;\n}\n\n// Toolbar button onmouseout handler\nfunction BtnMouseOut() {\n	if (event.srcElement.tagName != \"IMG\") {\n		event.cancelBubble = true;\n		return false;\n	}\n\n	var image = event.srcElement;\n	var element = image.parentElement;\n	yRaisedElement = null;\n	\n	element.className = \"Btn\";\n	image.className = \"Ico\";\n\n	event.cancelBubble = true;\n}\n\n// Toolbar button onmousedown handler\nfunction BtnMouseDown() {\n	if (event.srcElement.tagName != \"IMG\") {\n		event.cancelBubble = true;\n		event.returnValue=false;\n		return false;\n	}\n\n	var image = event.srcElement;\n	var element = image.parentElement;\n\n	element.className = \"BtnMouseOverDown\";\n	image.className = \"IcoDown\";\n\n	event.cancelBubble = true;\n	event.returnValue=false;\n	return false;\n}\n\n// Toolbar button onmouseup handler\nfunction BtnMouseUp() {\n	if (event.srcElement.tagName != \"IMG\") {\n		event.cancelBubble = true;\n		return false;\n	}\n\n	var image = event.srcElement;\n	var element = image.parentElement;\n\n	if (element.YUSERONCLICK) eval(element.YUSERONCLICK + \"anonymous()\");\n\n	element.className = \"BtnMouseOverUp\";\n	image.className = \"Ico\";\n\n	event.cancelBubble = true;\n	return false;\n}\n\n// Populate a toolbar with the elements within it\nfunction PopulateTB(y) {\n	var i, elements, element;\n\n	// Iterate through all the top-level elements in the toolbar\n	elements = y.children;\n	for (i=0; i<elements.length; i++) {\n		element = elements[i];\n		if (element.tagName == \"SCRIPT\" || element.tagName == \"!\") continue;\n		\n		switch (element.className) {\n		case \"Btn\":\n			if (element.YINITIALIZED == null) {\n				if (! InitBtn(element)) {\n					alert(\"Problem initializing:\" + element.id);\n					return false;\n				}\n			}\n			\n			element.style.posLeft = y.TBWidth;\n			y.TBWidth += element.offsetWidth + 1;\n			break;\n			\n		case \"TBGen\":\n			element.style.posLeft = y.TBWidth;\n			y.TBWidth += element.offsetWidth + 1;\n			break;\n			\n		case \"TBSep\":\n			element.style.posLeft = y.TBWidth + 2;\n			y.TBWidth += 5;\n			break;\n			\n		case \"TBHandle\":\n			element.style.posLeft = 2;\n			y.TBWidth += element.offsetWidth + 7;\n			break;\n			\n		default:\n			alert(\"Invalid class: \" + element.className + \" on Element: \" + element.id + \" <\" + element.tagName + \">\");\n			return false;\n		}\n	}\n\n	y.TBWidth += 1;\n	return true;\n}\n\n\n// ���������������ύ��reset�¼�\nfunction setLinkedField() {\n	if (! objContent) return ;\n	var oForm = objContent.form ;\n	if (!oForm) return ;\n	// ����submit�¼�\n	oForm.attachEvent(\"onsubmit\", AttachSubmit) ;\n	if (! oForm.submitEditor) oForm.submitEditor = new Array() ;\n	oForm.submitEditor[oForm.submitEditor.length] = AttachSubmit ;\n	if (! oForm.originalSubmit) {\n		oForm.originalSubmit = oForm.submit ;\n		oForm.submit = function() {\n			if (this.submitEditor) {\n				for (var i = 0 ; i < this.submitEditor.length ; i++) {\n					this.submitEditor[i]() ;\n				}\n			}\n			this.originalSubmit() ;\n		}\n	}\n	// ����reset�¼�\n	oForm.attachEvent(\"onreset\", AttachReset) ;\n	if (! oForm.resetEditor) oForm.resetEditor = new Array() ;\n	oForm.resetEditor[oForm.resetEditor.length] = AttachReset ;\n	if (! oForm.originalReset) {\n		oForm.originalReset = oForm.reset ;\n		oForm.reset = function() {\n			if (this.resetEditor) {\n				for (var i = 0 ; i < this.resetEditor.length ; i++) {\n					this.resetEditor[i]() ;\n				}\n			}\n			this.originalReset() ;\n		}\n	}\n}\n\n// ����submit�ύ�¼�,����������ύ,����WebEditor�е�����\nfunction AttachSubmit() { \n	if (!bEditMode) setMode(\'EDIT\');\n\n	ContentEdit.value = getHTML();\n	objContent.value = ContentEdit.value;\n\n	var oForm = objContent.form ;\n	if (!oForm) return ;\n\n	//��������ֵ�趨������ֵ��102399�����ǵ�������Ϊһ��\n	var FormLimit = 50000 ;\n\n	//ȡ��ǰ������ֵ \n	var TempVar = new String ;\n	TempVar = objContent.value ;\n\n	// δ�ύ�ɹ��ٴδ���ʱ���ȸ���ֵ\n	for (var i=1;i<parent.document.getElementsByName(sContentName).length;i++) {\n		parent.document.getElementsByName(sContentName)[i].value = \"\";\n	}\n\n	//�������ֵ�������ƣ���ɶ������\n	if (TempVar.length > FormLimit) { \n		objContent.value = TempVar.substr(0, FormLimit) ;\n		TempVar = TempVar.substr(FormLimit) ;\n\n		while (TempVar.length > 0) { \n			var objTEXTAREA = objContent.document.createElement(\"TEXTAREA\") ;\n			objTEXTAREA.name = sContentName ;\n			objTEXTAREA.style.display = \"none\" ;\n			objTEXTAREA.value = TempVar.substr(0, FormLimit) ;\n			oForm.appendChild(objTEXTAREA) ;\n\n			TempVar = TempVar.substr(FormLimit) ;\n		} \n	} \n} \n\n// ����Reset�¼�\nfunction AttachReset() {\n	if (!bEditMode) setMode(\'EDIT\');\n	if(bEditMode){\n		WebEditor.document.body.innerHTML = ContentLoad.value;\n	}else{\n		WebEditor.document.body.innerText = ContentLoad.value;\n	}\n}\n\n// ճ��ʱ�Զ�����Ƿ���Դ��Word��ʽ\nfunction onPaste() {\n	if (config.AutoDetectPasteFromWord && BrowserInfo.IsIE55OrMore) {\n		var sHTML = GetClipboardHTML() ;\n		var re = /<\\w[^>]* class=\"?MsoNormal\"?/gi ;\n		if ( re.test( sHTML ) )\n		{\n			if ( confirm( \"��Ҫճ�������ݺ����Ǵ�Word�п������ģ��Ƿ�Ҫ�����Word��ʽ��ճ����\" ) )\n			{\n				cleanAndPaste( sHTML ) ;\n				return false ;\n			}\n		}\n	}\n	else\n		return true ;\n}\n\nfunction GetClipboardHTML() {\n	var oDiv = document.getElementById(\"divTemp\")\n	oDiv.innerHTML = \"\" ;\n	\n	var oTextRange = document.body.createTextRange() ;\n	oTextRange.moveToElementText(oDiv) ;\n	oTextRange.execCommand(\"Paste\") ;\n	\n	var sData = oDiv.innerHTML ;\n	oDiv.innerHTML = \"\" ;\n	\n	return sData ;\n}\n\nfunction cleanAndPaste( html ) {\n	// Remove all SPAN tags\n	html = html.replace(/<\\/?SPAN[^>]*>/gi, \"\" );\n	// Remove Class attributes\n	html = html.replace(/<(\\w[^>]*) class=([^ |>]*)([^>]*)/gi, \"<$1$3\") ;\n	// Remove Style attributes\n	html = html.replace(/<(\\w[^>]*) style=\"([^\"]*)\"([^>]*)/gi, \"<$1$3\") ;\n	// Remove Lang attributes\n	html = html.replace(/<(\\w[^>]*) lang=([^ |>]*)([^>]*)/gi, \"<$1$3\") ;\n	// Remove XML elements and declarations\n	html = html.replace(/<\\\\?\\?xml[^>]*>/gi, \"\") ;\n	// Remove Tags with XML namespace declarations: <o:p></o:p>\n	html = html.replace(/<\\/?\\w+:[^>]*>/gi, \"\") ;\n	// Replace the &nbsp;\n	html = html.replace(/&nbsp;/, \" \" );\n	// Transform <P> to <DIV>\n	var re = new RegExp(\"(<P)([^>]*>.*?)(<\\/P>)\",\"gi\") ;	// Different because of a IE 5.0 error\n	html = html.replace( re, \"<div$2</div>\" ) ;\n	\n	insertHTML( html ) ;\n}\n\n// �ڵ�ǰ�ĵ�λ�ò���.\nfunction insertHTML(html) {\n	if (!validateMode()) return;\n	if (WebEditor.document.selection.type.toLowerCase() != \"none\")\n		WebEditor.document.selection.clear() ;\n	WebEditor.document.selection.createRange().pasteHTML(html) ; \n}\n\n// ���ñ༭��������\nfunction setHTML(html) {\n	if (!validateMode()) return;\n	ContentEdit.value = html;\n	if(bEditMode){\n		WebEditor.document.body.innerHTML = html;\n	}else{\n		WebEditor.document.body.innerText = html;\n	}\n}\n\n// ȡ�༭��������\nfunction getHTML() {\n	if(bEditMode){\n		return WebEditor.document.body.innerHTML;\n	}else{\n		return WebEditor.document.body.innerText;\n	}\n}\n\n// ��β��׷������\nfunction appendHTML(html) {\n	if (!validateMode()) return;\n	if(bEditMode){\n		WebEditor.document.body.innerHTML += html;\n	}else{\n		WebEditor.document.body.innerText += html;\n	}\n}\n\n// ��Word��ճ����ȥ����ʽ\nfunction PasteWord(){\n	if (!validateMode()) return;\n	WebEditor.focus();\n	if (BrowserInfo.IsIE55OrMore)\n		cleanAndPaste( GetClipboardHTML() ) ;\n	else if ( confirm( \"�˹���Ҫ��IE5.5�汾���ϣ��㵱ǰ���������֧�֣��Ƿ񰴳���ճ�����У�\" ) )\n		format(\"paste\") ;\n	WebEditor.focus();\n}\n\n// ճ�����ı�\nfunction PasteText(){\n	if (!validateMode()) return;\n	WebEditor.focus();\n	var sText = HTMLEncode( clipboardData.getData(\"Text\") ) ;\n	insertHTML(sText);\n	WebEditor.focus();\n}\n\n// ��⵱ǰ�Ƿ������༭\nfunction validateMode() {\n	if (bEditMode) return true;\n	alert(\"��ת��Ϊ�༭״̬�����ʹ�ñ༭���ܣ�\");\n	WebEditor.focus();\n	return false;\n}\n\n// ��ʽ���༭���е�����\nfunction format(what,opt) {\n	if (!validateMode()) return;\n	WebEditor.focus();\n	if (opt==\"RemoveFormat\") {\n		what=opt;\n		opt=null;\n	}\n\n	if (opt==null) WebEditor.document.execCommand(what);\n	else WebEditor.document.execCommand(what,\"\",opt);\n	\n	WebEditor.focus();\n}\n\n// ȷ�������� WebEditor ��\nfunction VerifyFocus() {\n	if ( WebEditor )\n		WebEditor.focus();\n}\n\n// �ı�ģʽ�����롢�༭��Ԥ��\nfunction setMode(NewMode){\n	if (NewMode!=sCurrMode){\n		// ��ͼƬ\n		document.all[\"WebEditor_CODE\"].style.display = \"none\";\n		document.all[\"WebEditor_EDIT\"].style.display = \"none\";\n		document.all[\"WebEditor_VIEW\"].style.display = \"none\";\n		document.all[\"WebEditor_\"+NewMode].style.display = \"block\";\n		// ������\n		switch (NewMode){\n		case \"CODE\":\n			if (WebEditor.document.designMode==\"On\") {\n				WebEditor.document.body.innerText=WebEditor.document.body.innerHTML;\n			}else {\n				var temp=WebEditor.document.body.innerHTML;\n				WebEditor.document.designMode=\"On\";\n				WebEditor.document.open();\n				WebEditor.document.write(bodyTag);\n				WebEditor.document.body.innerText=temp;\n				WebEditor.document.close();\n				temp=null;\n			}\n			bEditMode=false;\n			break;\n		case \"EDIT\":\n			WebEditor.document.body.disabled=false;\n			if (WebEditor.document.designMode==\"On\") {\n				WebEditor.document.body.innerHTML=WebEditor.document.body.innerText;\n			}else {\n				var temp=WebEditor.document.body.innerHTML;\n				WebEditor.document.designMode=\"On\";\n				WebEditor.document.open();\n				WebEditor.document.write(bodyTag);\n				WebEditor.document.body.innerHTML=temp;\n				WebEditor.document.close();\n				temp=null;\n			}\n			bEditMode=true;\n			break;\n		case \"VIEW\":\n			var temp;\n			if(bEditMode){\n				temp = WebEditor.document.body.innerHTML;\n			}else{\n				temp = WebEditor.document.body.innerText;\n			}\n			WebEditor.document.designMode=\"off\";\n			WebEditor.document.open();\n			WebEditor.document.write(bodyTag+temp);\n			WebEditor.document.close();\n			bEditMode=false;\n			break;\n		}\n		sCurrMode=NewMode;\n		for (var i=0;i<WebEditor_Tool.children.length;i++){\n			WebEditor_Tool.children[i].disabled=(!bEditMode);\n		}\n	}\n	WebEditor.focus();\n}\n\n// ��ʾ��ģʽ�Ի���\nfunction ShowDialog(url, width, height, optValidate) {\n	if (optValidate) {\n		if (!validateMode()) return;\n	}\n	WebEditor.focus();\n	var arr = showModalDialog(url, window, \"dialogWidth:\" + width + \"px;dialogHeight:\" + height + \"px;help:no;scroll:no;status:no\");\n	WebEditor.focus();\n}\n\n// ȫ���༭\nfunction Maximize() {\n    window.open(\"editorDialog/fullscreen.htm?style=\"+config.StyleName, \'FullScreen\'+sContentName, \'toolbar=no,location=no,directories=no,status=yes,menubar=no,scrollbars=yes,resizable=yes,fullscreen==yes\');\n}\n\n// �滻�����ַ�\nfunction HTMLEncode(text){\n	text = text.replace(/&/g, \"&amp;\") ;\n	text = text.replace(/\"/g, \"&quot;\") ;\n	text = text.replace(/</g, \"&lt;\") ;\n	text = text.replace(/>/g, \"&gt;\") ;\n	text = text.replace(/\'/g, \"&#146;\") ;\n	text = text.replace(/\\\\ /g,\"&nbsp;\");\n	text = text.replace(/\\\\n/g,\"<br>\");\n	text = text.replace(/\\\\t/g,\"&nbsp;&nbsp;&nbsp;&nbsp;\");\n	return text;\n}\n\n// �����������\nfunction insert(what) {\n	if (!validateMode()) return;\n	WebEditor.focus();\n	var sel = WebEditor.document.selection.createRange();\n\n	switch(what){\n	case \"excel\":		// ����EXCEL����\n		insertHTML(\"<object classid=\'clsid:0002E510-0000-0000-C000-000000000046\' id=\'Spreadsheet1\' codebase=\'file:\\\\\\\\Bob\\\\software\\\\office2000\\\\msowc.cab\' width=\'100%\' height=\'250\'><param name=\'HTMLURL\' value><param name=\'HTMLData\' value=\'&lt;html xmlns:x=&quot;urn:schemas-microsoft-com:office:excel&quot;xmlns=&quot;http://www.w3.org/TR/REC-html40&quot;&gt;&lt;head&gt;&lt;style type=&quot;text/css&quot;&gt;&lt;!--tr{mso-height-source:auto;}td{black-space:nowrap;}.wc4590F88{black-space:nowrap;font-family:����;mso-number-format:General;font-size:auto;font-weight:auto;font-style:auto;text-decoration:auto;mso-background-source:auto;mso-pattern:auto;mso-color-source:auto;text-align:general;vertical-align:bottom;border-top:none;border-left:none;border-right:none;border-bottom:none;mso-protection:locked;}--&gt;&lt;/style&gt;&lt;/head&gt;&lt;body&gt;&lt;!--[if gte mso 9]&gt;&lt;xml&gt;&lt;x:ExcelWorkbook&gt;&lt;x:ExcelWorksheets&gt;&lt;x:ExcelWorksheet&gt;&lt;x:OWCVersion&gt;9.0.0.2710&lt;/x:OWCVersion&gt;&lt;x:Label Style=\'border-top:solid .5pt silver;border-left:solid .5pt silver;border-right:solid .5pt silver;border-bottom:solid .5pt silver\'&gt;&lt;x:Caption&gt;Microsoft Office Spreadsheet&lt;/x:Caption&gt; &lt;/x:Label&gt;&lt;x:Name&gt;Sheet1&lt;/x:Name&gt;&lt;x:WorksheetOptions&gt;&lt;x:Selected/&gt;&lt;x:Height&gt;7620&lt;/x:Height&gt;&lt;x:Width&gt;15240&lt;/x:Width&gt;&lt;x:TopRowVisible&gt;0&lt;/x:TopRowVisible&gt;&lt;x:LeftColumnVisible&gt;0&lt;/x:LeftColumnVisible&gt; &lt;x:ProtectContents&gt;False&lt;/x:ProtectContents&gt; &lt;x:DefaultRowHeight&gt;210&lt;/x:DefaultRowHeight&gt; &lt;x:StandardWidth&gt;2389&lt;/x:StandardWidth&gt; &lt;/x:WorksheetOptions&gt; &lt;/x:ExcelWorksheet&gt;&lt;/x:ExcelWorksheets&gt; &lt;x:MaxHeight&gt;80%&lt;/x:MaxHeight&gt;&lt;x:MaxWidth&gt;80%&lt;/x:MaxWidth&gt;&lt;/x:ExcelWorkbook&gt;&lt;/xml&gt;&lt;![endif]--&gt;&lt;table class=wc4590F88 x:str&gt;&lt;col width=&quot;56&quot;&gt;&lt;tr height=&quot;14&quot;&gt;&lt;td&gt;&lt;/td&gt;&lt;/tr&gt;&lt;/table&gt;&lt;/body&gt;&lt;/html&gt;\'> <param name=\'DataType\' value=\'HTMLDATA\'> <param name=\'AutoFit\' value=\'0\'><param name=\'DisplayColHeaders\' value=\'-1\'><param name=\'DisplayGridlines\' value=\'-1\'><param name=\'DisplayHorizontalScrollBar\' value=\'-1\'><param name=\'DisplayRowHeaders\' value=\'-1\'><param name=\'DisplayTitleBar\' value=\'-1\'><param name=\'DisplayToolbar\' value=\'-1\'><param name=\'DisplayVerticalScrollBar\' value=\'-1\'> <param name=\'EnableAutoCalculate\' value=\'-1\'> <param name=\'EnableEvents\' value=\'-1\'><param name=\'MoveAfterReturn\' value=\'-1\'><param name=\'MoveAfterReturnDirection\' value=\'0\'><param name=\'RightToLeft\' value=\'0\'><param name=\'ViewableRange\' value=\'1:65536\'></object>\");\n		break;\n	case \"nowdate\":		// ���뵱ǰϵͳ����\n		var d = new Date();\n		insertHTML(d.toLocaleDateString());\n		break;\n	case \"nowtime\":		// ���뵱ǰϵͳʱ��\n		var d = new Date();\n		insertHTML(d.toLocaleTimeString());\n		break;\n	case \"br\":			// ���뻻�з�\n		insertHTML(\"<br>\")\n		break;\n	case \"code\":		// ����Ƭ����ʽ\n		insertHTML(\'<table width=95% border=\"0\" align=\"Center\" cellpadding=\"6\" cellspacing=\"0\" style=\"border: 1px Dotted #6595d6; TABLE-LAYOUT: fixed\"><tr><td bgcolor=#e8f4ff style=\"WORD-WRAP: break-word\"><font style=\"color: #990000;font-weight:bold\">�����Ǵ���Ƭ�Σ�</font><br>\'+HTMLEncode(sel.text)+\'</td></tr></table>\');\n		break;\n	case \"quote\":		// ����Ƭ����ʽ\n		insertHTML(\'<table width=95% border=\"0\" align=\"Center\" cellpadding=\"6\" cellspacing=\"0\" style=\"border: 1px Dotted #6595d6; TABLE-LAYOUT: fixed\"><tr><td bgcolor=#e8f4ff style=\"WORD-WRAP: break-word\"><font style=\"color: #990000;font-weight:bold\">����������Ƭ�Σ�</font><br>\'+HTMLEncode(sel.text)+\'</td></tr></table>\');\n		break;\n	case \"big\":			// ������\n		insertHTML(\"<big>\" + sel.text + \"</big>\");\n		break;\n	case \"small\":		// �����С\n		insertHTML(\"<small>\" + sel.text + \"</small>\");\n		break;\n	default:\n		alert(\"����������ã�\");\n		break;\n	}\n	sel=null;\n}\n</Script>\n<Script Language=Javascript>\nvar bEditMode=true;\nvar sCurrMode = \"EDIT\";\nvar bodyTag = \"<head><style type=\\\"text/css\\\">body,a,table,div,span,td,th,input,select{font-size:9pt;font-family:\\\"����,Verdana,Arial\\\";Color:#000000;}</style><meta http-equiv=Content-Type content=\\\"text/html; charset=gb2312\\\"></head><BODY bgcolor=\\\"#FFFFFF\\\" MONOSPACE>\" ;\nvar sContentName = \"content\" ;\nvar objContent = parent.document.getElementsByName(sContentName)[0];\n\n// ȫ�����ö���\nvar config = new Object() ;\nconfig.Version = \"1.1.3\" ;\nconfig.ReleaseDate = \"2003-12-11\" ;\nconfig.StyleName = \"standard\";\nconfig.AutoDetectPasteFromWord = true;\n</Script>\n\n<script language=\"javascript\" event=\"onerror(msg, url, line)\" for=\"window\">\nreturn true ;	 // ���ش���\n</script>\n\n</head>\n\n<body SCROLLING=no SCROLL=no onConTextMenu=\"event.returnValue=false;\" onfocus=\"VerifyFocus()\" STYLE=\"background-color:transparent\">\n\n<table border=0 cellpadding=0 cellspacing=0 width=\'100%\' height=\'100%\' TABINDEX=-1>\n<tr><td id=\"WebEditor_Tool\">\n\n	<table border=0 cellpadding=0 cellspacing=0 width=\'100%\' class=\'Toolbar\'><tr><td><div class=yToolbar><DIV CLASS=\"TBHandle\"></DIV><SELECT CLASS=\"TBGen\" onchange=\"format(\'FormatBlock\',this[this.selectedIndex].value);this.selectedIndex=0\"><option selected>������ʽ</option>\n<option value=\"&lt;P&gt;\">��ͨ</option>\n<option value=\"&lt;H1&gt;\">����һ</option>\n<option value=\"&lt;H2&gt;\">�����</option>\n<option value=\"&lt;H3&gt;\">������</option>\n<option value=\"&lt;H4&gt;\">������</option>\n<option value=\"&lt;H5&gt;\">������</option>\n<option value=\"&lt;H6&gt;\">������</option>\n<option value=\"&lt;p&gt;\">����</option>\n<option value=\"&lt;dd&gt;\">����</option>\n<option value=\"&lt;dt&gt;\">���ﶨ��</option>\n<option value=\"&lt;dir&gt;\">Ŀ¼�б�</option>\n<option value=\"&lt;menu&gt;\">�˵��б�</option>\n<option value=\"&lt;PRE&gt;\">�ѱ��Ÿ�ʽ</option></SELECT><SELECT CLASS=\"TBGen\" onchange=\"format(\'fontname\',this[this.selectedIndex].value);this.selectedIndex=0\"><option selected>����</option>\n<option value=\"����\">����</option>\n<option value=\"����\">����</option>\n<option value=\"����_GB2312\">����</option>\n<option value=\"����_GB2312\">����</option>\n<option value=\"����\">����</option>\n<option value=\"��Բ\">��Բ</option>\n<option value=\"Arial\">Arial</option>\n<option value=\"Arial Black\">Arial Black</option>\n<option value=\"Arial Narrow\">Arial Narrow</option>\n<option value=\"Brush Script	MT\">Brush Script MT</option>\n<option value=\"Century Gothic\">Century Gothic</option>\n<option value=\"Comic Sans MS\">Comic Sans MS</option>\n<option value=\"Courier\">Courier</option>\n<option value=\"Courier New\">Courier New</option>\n<option value=\"MS Sans Serif\">MS Sans Serif</option>\n<option value=\"Script\">Script</option>\n<option value=\"System\">System</option>\n<option value=\"Times New Roman\">Times New Roman</option>\n<option value=\"Verdana\">Verdana</option>\n<option value=\"Wide Latin\">Wide Latin</option>\n<option value=\"Wingdings\">Wingdings</option></SELECT><SELECT CLASS=\"TBGen\" onchange=\"format(\'fontsize\',this[this.selectedIndex].value);this.selectedIndex=0\"><option selected>�ֺ�</option>\n<option value=\"7\">һ��</option>\n<option value=\"6\">����</option>\n<option value=\"5\">����</option>\n<option value=\"4\">�ĺ�</option>\n<option value=\"3\">���</option>\n<option value=\"2\">����</option>\n<option value=\"1\">�ߺ�</option></SELECT><DIV CLASS=\"TBSep\"></DIV><DIV CLASS=\"Btn\" TITLE=\"����\" onclick=\"format(\'cut\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/cut[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"����\" onclick=\"format(\'copy\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/copy[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"����ճ��\" onclick=\"format(\'paste\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/paste[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"���ı�ճ��\" onclick=\"PasteText()\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/pastetext[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"��Word��ճ��\" onclick=\"PasteWord()\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/pasteword[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"ɾ��\" onclick=\"format(\'delete\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/delete[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"ɾ�����ָ�ʽ\" onclick=\"format(\'RemoveFormat\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/RemoveFormat[1].gif\"></DIV><DIV CLASS=\"TBSep\"></DIV><DIV CLASS=\"Btn\" TITLE=\"����\" onclick=\"format(\'undo\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/undo[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"�ָ�\" onclick=\"format(\'redo\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/redo[1].gif\"></DIV></div></td></tr><tr><td><div class=yToolbar><DIV CLASS=\"TBHandle\"></DIV><DIV CLASS=\"Btn\" TITLE=\"����\" onclick=\"format(\'bold\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/bold[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"б��\" onclick=\"format(\'italic\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/italic[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"�»���\" onclick=\"format(\'underline\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/underline[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"�л���\" onclick=\"format(\'StrikeThrough\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/strikethrough[1].gif\"></DIV><DIV CLASS=\"TBSep\"></DIV><DIV CLASS=\"Btn\" TITLE=\"�����\" onclick=\"format(\'justifyleft\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/JustifyLeft[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"���ж���\" onclick=\"format(\'justifycenter\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/JustifyCenter[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"�Ҷ���\" onclick=\"format(\'justifyright\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/JustifyRight[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"���˶���\" onclick=\"format(\'JustifyFull\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/JustifyFull[1].gif\"></DIV><DIV CLASS=\"TBSep\"></DIV><DIV CLASS=\"Btn\" TITLE=\"���\" onclick=\"format(\'insertorderedlist\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/insertorderedlist[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"��Ŀ����\" onclick=\"format(\'insertunorderedlist\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/insertunorderedlist[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"����������\" onclick=\"format(\'indent\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/indent[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"����������\" onclick=\"format(\'outdent\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/outdent[1].gif\"></DIV><DIV CLASS=\"TBSep\"></DIV><DIV CLASS=\"Btn\" TITLE=\"�ϱ�\" onclick=\"format(\'superscript\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/superscript[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"�±�\" onclick=\"format(\'subscript\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/subscript[1].gif\"></DIV><DIV CLASS=\"TBSep\"></DIV><DIV CLASS=\"Btn\" TITLE=\"������ɫ\" onclick=\"ShowDialog(\'editorDialog/selcolor.htm?action=forecolor\', 280, 250, true)\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/forecolor[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"���屳����ɫ\" onclick=\"ShowDialog(\'editorDialog/selcolor.htm?action=backcolor\', 280, 250, true)\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/backcolor[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"���󱳾���ɫ\" onclick=\"ShowDialog(\'editorDialog/selcolor.htm?action=bgcolor\', 280, 250, true)\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/bgcolor[1].gif\"></DIV><DIV CLASS=\"TBSep\"></DIV><DIV CLASS=\"Btn\" TITLE=\"ȫ��ѡ��\" onclick=\"format(\'SelectAll\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/selectAll[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"ȡ��ѡ��\" onclick=\"format(\'Unselect\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/unselect[1].gif\"></DIV></div></td></tr><tr><td><div class=yToolbar><DIV CLASS=\"TBHandle\"></DIV><DIV CLASS=\"Btn\" TITLE=\"������޸ı���\" onclick=\"ShowDialog(\'editorDialog/table.htm\', 350, 320, true)\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/table[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"������޸���Ŀ��\" onclick=\"ShowDialog(\'editorDialog/fieldset.htm\', 350, 170, true)\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/fieldset[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"������޸���ҳ֡\" onclick=\"ShowDialog(\'editorDialog/iframe.htm\', 350, 200, true)\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/iframe[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"����ˮƽ��\" onclick=\"format(\'InsertHorizontalRule\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/InsertHorizontalRule[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"������޸���Ļ\" onclick=\"ShowDialog(\'editorDialog/marquee.htm\', 395, 150, true)\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/Marquee[1].gif\"></DIV><DIV CLASS=\"TBSep\"></DIV><DIV CLASS=\"Btn\" TITLE=\"������޸ĳ�������\" onclick=\"format(\'CreateLink\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/CreateLink[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"ȡ���������ӻ��ǩ\" onclick=\"format(\'UnLink\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/Unlink[1].gif\"></DIV><DIV CLASS=\"TBSep\"></DIV><DIV CLASS=\"Btn\" TITLE=\"������޸�ͼƬ\" onclick=\"ShowDialog(\'editorDialog/img.htm\', 350, 315, true)\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/img[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"����ͼƬ\" onclick=\"ShowDialog(\'editorDialog/backimage.htm\', 350, 280, true)\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/bgpic[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"����Flash����\" onclick=\"ShowDialog(\'editorDialog/flash.htm\', 350, 200, true)\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/flash[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"�����Զ����ŵ�ý���ļ�\" onclick=\"ShowDialog(\'editorDialog/media.htm\', 350, 200, true)\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/Media[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"���������ļ�\" onclick=\"ShowDialog(\'editorDialog/file.htm\', 350, 150, true)\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/file[1].gif\"></DIV><DIV CLASS=\"TBSep\"></DIV><DIV CLASS=\"Btn\" TITLE=\"���������ַ�\" onclick=\"ShowDialog(\'editorDialog/symbol.htm\', 350, 220, true)\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/symbol[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"�������ͼ��\" onclick=\"ShowDialog(\'editorDialog/emot.htm\', 400, 300, true)\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/emot[1].gif\"></DIV><DIV CLASS=\"TBSep\"></DIV><DIV CLASS=\"Btn\" TITLE=\"������ʽ\" onclick=\"insert(\'quote\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/quote[1].gif\"></DIV><DIV CLASS=\"Btn\" TITLE=\"������ʽ\" onclick=\"insert(\'code\')\"><IMG CLASS=\"Ico\" SRC=\"editorImgs/code[1].gif\"></DIV><DIV CLASS=\"TBSep\"></DIV></div></td></tr></table>\n\n</td></tr>\n<tr><td height=\'100%\'>\n\n	<table border=0 cellpadding=0 cellspacing=0 width=\'100%\' height=\'100%\'>\n	<tr><td height=\'100%\'>\n	<input type=\"hidden\" ID=\"ContentEdit\" value=\"\">\n	<input type=\"hidden\" ID=\"ContentLoad\" value=\"\">\n	<input type=\"hidden\" ID=\"ContentFlag\" value=\"0\">\n	<iframe class=\"Composition\" ID=\"WebEditor\" MARGINHEIGHT=\"1\" MARGINWIDTH=\"1\" width=\"100%\" height=\"100%\" scrolling=\"auto\"> \n	</iframe>\n	</td></tr>\n	</table>\n\n</td></tr>\n\n\n<tr><td height=18 valign=top>\n\n	<TABLE border=\"0\" cellPadding=\"0\" cellSpacing=\"0\" width=\"100%\">\n	<TR>\n\n	<td align=\"left\" valign=\"top\" id=\'WebEditor_CODE\' style=\"display:none\">\n	<map name=\'WebEditor_Map1\'>\n	<area shape=\"polygon\" coords=\"50, 1, 46, 7, 50, 14, 90, 14, 95, 2\" alt=\"�༭״̬\" onclick=\"setMode(\'EDIT\')\">\n	<area shape=\"polygon\" coords=\"128, 13, 134, 0, 96, 0, 93, 10, 96, 14\" alt=\"Ԥ��״̬\" onclick=\"setMode(\'VIEW\')\">\n	</map> <img SRC=\"editorImgs/modecode[1].gif\" style=\"cursor:hand\" height=\"15\" width=\"135\" usemap=\'#WebEditor_Map1\' border=\"0\"></td>\n\n	<td align=\"left\" valign=\"top\" id=\'WebEditor_EDIT\'>\n	<map name=\'WebEditor_Map2\'>\n	<area shape=\"polygon\" coords=\"5, 3, 12, 14, 43, 14, 49, 6, 43, 0\" alt=\"����״̬\" onclick=\"setMode(\'CODE\')\">\n	<area shape=\"polygon\" coords=\"97, 0, 94, 7, 98, 14, 127, 14, 134, 0\" alt=\"Ԥ��״̬\" onclick=\"setMode(\'VIEW\')\">\n	</map> <img SRC=\"editorImgs/modeedit[1].gif\" style=\"cursor:hand\" height=\"15\" width=\"135\" usemap=\'#WebEditor_Map2\' border=\"0\"></td>\n\n	<td align=\"left\" valign=\"top\" id=\'WebEditor_VIEW\' style=\"display:none\"><map name=\'WebEditor_Map3\'>\n	<area shape=\"polygon\" coords=\"3, 2, 10, 14, 41, 14, 50, 0\" alt=\"����״̬\" onclick=\"setMode(\'CODE\')\">\n	<area shape=\"polygon\" coords=\"87, 14, 91, 5, 87, 0, 50, 0, 46, 9, 49, 14\" alt=\"�༭״̬\" onclick=\"setMode(\'EDIT\')\">\n	</map> <img SRC=\"editorImgs/modepreview[1].gif\" style=\"cursor:hand\" height=\"15\" width=\"135\" usemap=\'#WebEditor_Map3\' border=\"0\"></td>\n\n	</TR>\n	</Table>\n\n</td></tr>\n\n\n</table>\n\n<div id=\"divTemp\" style=\"VISIBILITY: hidden; OVERFLOW: hidden; POSITION: absolute; WIDTH: 1px; HEIGHT: 1px\"></div>\n</body>\n</html>";
if(parent.parent)parent.parent.doc_html_html = doc_html_html;