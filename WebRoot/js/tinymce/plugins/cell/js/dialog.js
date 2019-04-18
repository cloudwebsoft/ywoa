tinyMCEPopup.requireLangPack();

var CellDialog = {
	init : function() {
		var f = document.forms[0];

		// Get the selected contents as text and place it in the input
		// f.someval.value = tinyMCEPopup.editor.selection.getContent({format : 'text'});
		var obj = tinyMCEPopup.editor.selection.getNode();
		alert(obj.outerHTML);
		if (obj.getAttribute("value")==null) {
			alert("请选择控件！");
			tinyMCEPopup.close();
		}
		f.someval.value = obj.getAttribute("value");
		var w = obj.getAttribute("width");
		if (w==null)
			w = 100;
		f.width.value = w;
		var t = obj.getAttribute("sumType");
		if (t==null)
			t = "";
		f.sumType.value = t;
		f.somearg.value = tinyMCEPopup.getWindowArg('some_custom_arg');
	},

	insert : function() {
		// Insert the contents from the input into the document
		// tinyMCEPopup.editor.execCommand('mceInsertContent', false, document.forms[0].someval.value);
		
		var obj = tinyMCEPopup.editor.selection.getNode();
		obj.setAttribute("value", document.forms[0].someval.value);
		obj.setAttribute("width", document.forms[0].width.value);
		obj.setAttribute("sumType", document.forms[0].sumType.value);
		
		var str = obj.outerHTML;
		// $(obj).remove();
		
		tinyMCEPopup.editor.selection.setContent(str);

		tinyMCEPopup.close();
	}
};

tinyMCEPopup.onInit.add(CellDialog.init, CellDialog);
