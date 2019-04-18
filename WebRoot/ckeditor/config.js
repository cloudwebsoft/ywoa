/**
 * @license Copyright (c) 2003-2013, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

CKEDITOR.editorConfig = function( config )  
{  
    config.extraPlugins = 'cwvideo';
	
	config.extraPlugins += (config.extraPlugins ? ',pastefromword' : 'pastefromword');

    // Define changes to default configuration here. For example:  
    // config.language = 'fr';  
    // config.uiColor = '#AADC6E';
    config.toolbar = 'MyToolbar';
  
    config.toolbar_MyToolbar =  
    [
        // ['Source','-','Save','NewPage','Preview','-','Templates'],  
		['Source','-','NewPage','Preview','-','Templates'],  
        ['Cut','Copy','Paste','PasteText','PasteFromWord','-','Print', 'SpellChecker', 'Scayt'],  
        ['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],  
        ['Form', 'Checkbox', 'Radio', 'TextField', 'Textarea', 'Select', 'Button', 'ImageButton', 'HiddenField'],  
        '/',  
        ['Bold','Italic','Underline','Strike','-','Subscript','Superscript'],
        ['NumberedList','BulletedList','-','Outdent','Indent','Blockquote'], 
        ['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],  
        ['Link','Unlink','Anchor'],
        ['Image','Flash','cwvideo','Table','HorizontalRule','Smiley','SpecialChar','PageBreak'],  
        '/',  
        ['Styles','Format','Font','FontSize'],  
        ['TextColor','BGColor'],  
        ['Maximize', 'ShowBlocks','-','About']
    ];
	
    config.toolbar_Flow =  
    [
        ['Source','-','Save','NewPage','Preview','-','Templates'],  
        ['Cut','Copy','Paste','PasteText','PasteFromWord','-','Print'],  
        ['Undo','Redo','-','Find','Replace'],  
        ['Outdent','Indent'],
        '/',  
        ['Bold','Italic','Underline','Strike','-','Superscript'],  
        ['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],  
        ['Image','Flash','cwvideo','Table','Smiley','SpecialChar','PageBreak'],  
        ['Link','Unlink','Anchor'],  
        '/',  
        ['Styles','Format','Font','FontSize'],  
        ['TextColor','BGColor'],  
        ['Maximize', 'ShowBlocks']
    ];
	
    config.toolbar_Middle =  
    [
        ['Source'],  
        ['Cut','Copy','Paste','PasteText','PasteFromWord'],  
        ['Undo','Redo'],  
        ['Outdent','Indent'],
        ['Bold','Italic','Underline','Strike','-','Superscript'], 
        '/',		
        ['Styles','Format','Font','FontSize'],  
        ['TextColor','BGColor'],
		['Table','Smiley','SpecialChar']
    ];	
	
    config.toolbar_Simple =  
    [
        ['Cut','Copy','Paste'],  
        ['Undo','Redo'],  
        ['Bold','Underline'], 
        ['Font','FontSize'],  
        ['TextColor','BGColor', 'Source', 'Maximize']
    ];		

	config.font_names='\u5b8b\u4f53/\u5b8b\u4f53;\u9ed1\u4f53/\u9ed1\u4f53;\u4eff\u5b8b/\u4eff\u5b8b_GB2312;\u6977\u4f53/\u6977\u4f53_GB2312;\u96b6\u4e66/\u96b6\u4e66;\u5e7c\u5706/\u5e7c\u5706;\u5fae\u8f6f\u96c5\u9ed1/\u5fae\u8f6f\u96c5\u9ed1;'+ config.font_names;
	
	// 是否强制复制来的内容去除格式 plugins/pastetext/plugin.js    
	config.forcePasteAsPlainText = false//不去除   
	// 是否使用等标签修饰或者代替从word文档中粘贴过来的内容 plugins/pastefromword/plugin.js     
	config.pasteFromWordKeepsStructure = true;   
	// 从word中粘贴内容时是否移除格式 plugins/pastefromword/plugin.js   
	config.pasteFromWordRemoveStyles = false;
	config.pasteFromWordRemoveFontStyles = false;

	// 为了插入jwplayer
	config.extraAllowedContent = 'div[id]; object[id,name,width,height,type,data]; param[name,value]; ' +
		'embed[src,type,allowscriptaccess,allowfullscreen,wmode,width,height]';	
};

CKEDITOR.on('dialogDefinition', function(ev) {
	// Take the dialog window name and its definition from the event data.
	var dialogName = ev.data.name;
	var dialogDefinition = ev.data.definition;
 
	if (dialogName == 'image') {
		dialogDefinition.onShow = function () {
			// This code will open the Advanced tab.
			// this.selectPage('advanced');
		};
		
dialogDefinition.removeContents( 'Link' ); 
dialogDefinition.removeContents( 'advanced' );
dialogDefinition.removeContents( 'Upload' ); 		
	}
});