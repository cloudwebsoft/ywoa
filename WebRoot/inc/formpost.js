// JavaScript Document
function loadDataToWebeditCtrlWithHTMLCode(obj, htmledit, htmlcode) {
   htmledit.Clear();
   // 用以记录已处理过的radio和checkbox
   var aryradio = new Array();
   var arychk = new Array();
   var pradio = -1; // 最后一个插入的处理过的元素的名称在数组中的索引值
   var pchk = -1;
   
   for(var i=0;i<obj.elements.length;i++)
   {
   	if (obj.elements[i].type=="radio") {
		var isAccessed = false;
		// 检查该radio是否已处理过
		for (k=0; k<=pradio; k++)
			if (aryradio[k]==obj.elements[i].name) {
				isAccessed = true;
				break;
			}
		if (!isAccessed) {
   			htmledit.AddField(obj.elements[i].name , getradio(obj.elements[i].name));
			pradio ++;
			aryradio[pradio] = obj.elements[i].name;
		}
   	}
   	else if (obj.elements[i].type=="checkbox"){
		var isAccessed = false;
		// 检查该checkbox是否已处理过
		for (m=0; m<=pchk; m++)
			if (arychk[m]==obj.elements[i].name) {
				isAccessed = true;
				break;
			}
		if (!isAccessed) {
			if (obj.elements[i].checked) // 20070827 modify
	   			htmledit.AddField(obj.elements[i].name , getcheckbox(obj.elements[i].name));
			pchk ++;
			arychk[pchk] = obj.elements[i].name;
		}
   	}
   	else{
   		htmledit.AddField(obj.elements[i].name , obj.elements[i].value);
   	}
   }
	
	htmledit.SetHtmlCode(htmlcode);
}


function loadDataToWebeditCtrl(obj, htmledit) {
   htmledit.Clear();
   // 用以记录已处理过的radio和checkbox
   var aryradio = new Array();
   var arychk = new Array();
   var pradio = -1; // 最后一个插入的处理过的元素的名称在数组中的索引值
   var pchk = -1;

   for(var i=0;i<obj.elements.length;i++)
   {
   	if (obj.elements[i].type=="radio") {
		var isAccessed = false;
		// 检查该radio是否已处理过
		for (k=0; k<=pradio; k++)
			if (aryradio[k]==obj.elements[i].name) {
				isAccessed = true;
				break;
			}
		if (!isAccessed) {
   			htmledit.AddField(obj.elements[i].name , getradio(obj.elements[i].name));
			pradio ++;
			aryradio[pradio] = obj.elements[i].name;
		}
   	}
   	else if (obj.elements[i].type=="checkbox"){
		var isAccessed = false;
		// 检查该checkbox是否已处理过
		for (m=0; m<=pchk; m++)
			if (arychk[m]==obj.elements[i].name) {
				isAccessed = true;
				break;
			}
		if (!isAccessed) {
   			htmledit.AddField(obj.elements[i].name , getcheckbox(obj.elements[i].name));
			pchk ++;
			arychk[pchk] = obj.elements[i].name;
		}
   	}
   	else{
   		htmledit.AddField(obj.elements[i].name , obj.elements[i].value);
   	}
   }
	
	//---------------------------------------------
	var htmlcode = IframeID.document.body.innerHTML; // oEdit1.getHTMLBody();
	// alert(htmlcode);
	if (htmlcode=="")
		htmlcode = " ";
	
	htmledit.SetHtmlCode(htmlcode);
}

function getradio(radionname) {
	var radioboxs = document.getElementsByName(radionname);
	if (radioboxs!=null)
	{
		for (i=0; i<radioboxs.length; i++)
		{
			if (radioboxs[i].type=="radio" && radioboxs[i].checked)
			{ 
				return radioboxs[i].value;
			}
		}
		return radioboxs.value
	}
	return "";
}

function getcheckbox(checkboxname) {
	var checkboxboxs = document.getElementsByName(checkboxname);
	var CheckboxValue = '';
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			if (checkboxboxs.checked) {
				return checkboxboxs.value;
			}
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			if (checkboxboxs[i].type=="checkbox" && checkboxboxs[i].checked)
			{
				if (CheckboxValue==''){
					CheckboxValue += checkboxboxs[i].value;
				}
				else{
					CheckboxValue += ","+ checkboxboxs[i].value;
				}
			}
		}
		//return checkboxboxs.value
	}
	return CheckboxValue;
}
