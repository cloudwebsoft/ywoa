CKEDITOR.dialog.add('cwvideo',　function(editor){
　　　　var　escape　=　function(value){
　　　　　　　　return　value;
　　　　};
　　　　return　{
　　　　　　　　title:　'插入视频',
　　　　　　　　resizable:　CKEDITOR.DIALOG_RESIZE_BOTH,
　　　　　　　　minWidth: 350,
              minHeight: 300,
　　　　　　　　contents:　[{
　　　　　　　　　　id: 'info',  
                    label: '常规',
                    accessKey: 'P',
                    elements:[
                        {
                        type: 'hbox',
			            widths : [ '80%', '20%' ],
                        children:[{
                                id: 'src',
                                type: 'text',
                                label: '源文件'
                            }
							/*
							,{
                                type: 'button',
                                id: 'browse',
								action:'Browse',
                                filebrowser: 'info:src',
								
									onClick: function(){
										
                                        var d = this.getDialog();
										
                                        var src =  d.getContentElement('info','src');
										
										var fn = CKEDITOR.tools.addFunction(function(path){
										var inputId = src.getInputElement().$.id;
										document.getElementById(inputId).value = path;
										});
											
                                        var url = 'ckeditor/video_frame.jsp?action=selectImageForEditor&fn=' + fn;
										
										window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,width=800,height=600");										
                                   },
																
                                hidden: false,
                                align: 'center',
                                label: '浏览服务器'
                            }*/
							]
                        },
                        {
                        	type: 'hbox',
			            widths : [ '35%', '35%', '30%' ],
                        children:[{
                        type:　'text',
　　　　　　　　　　　　　　label:　'视频宽度',
　　　　　　　　　　　　　　id:　'mywidth',
　　　　　　　　　　　　　　'default':　'470px',
　　　　　　　　　　　　　　style:　'width:50px'
                        },{
                            type:　'text',
　　　　　　　　　　　　　　label:　'视频高度',
　　　　　　　　　　　　　　id:　'myheight',
　　　　　　　　　　　　　　'default':　'320px',
　　　　　　　　　　　　　　style:　'width:50px'
                        },{
                            type:　'select',
　　　　　　　　　　　　　　label:　'自动播放',
　　　　　　　　　　　　　　id:　'myloop',
　　　　　　　　　　　　　　required:　true,
　　　　　　　　　　　　　　'default':　'false',
　　　　　　　　　　　　　　items:　[['是',　'true'],　['否',　'false']]
                        }]//children finish
                        },{
　　　　　　　　　　        type:　'textarea',
　　　　　　　　　　　　　　style:　'width:300px;height:220px',
　　　　　　　　　　　　　　label:　'预览',
　　　　　　　　　　　　　　id:　'code'
　　　　　　　　　　    }]
                    }, {
                        id: 'Upload',
                        hidden: true,
                        filebrowser: 'uploadButton',
                        label: '上传',
                        elements: [{
                            type: 'file',
                            id: 'upload',
                            label: '上传',
                            size: 38
                        },
                        {
                            type: 'fileButton',
                            id: 'uploadButton',
                            label: '发送到服务器',
                            filebrowser: 'info:src',
                            'for': ['Upload', 'upload']//'page_id', 'element_id' 
                        }]
　　　　　　　　}],
　　　　　　　　onOk:　function(){
　　　　　　　　　　　　mywidth　=　this.getValueOf('info',　'mywidth');
　　　　　　　　　　　　myheight　=　this.getValueOf('info',　'myheight');
　　　　　　　　　　　　myloop　=　this.getValueOf('info',　'myloop');
　　　　　　　　　　　　mysrc　=　this.getValueOf('info',　'src');
　　　　　　　　　　　　html　=　''　+　escape(mysrc)　+　'';

					 var ext = mysrc.substring(mysrc.length-3);
					 if (ext.toLowerCase()=="flv") {
						 // 根据视频格式，插入相应代码
	　　　　　　　　　　　　editor.insertHtml("<embed height="　+　myheight　+　" width="　+　mywidth　+　" autostart="　+　myloop　+　" flashvars=\"file="　+　html　+　"\" allowfullscreen=\"true\" allowscriptaccess=\"always\" bgcolor=\"#ffffff\" src=\"ckeditor/plugins/cwvideo/jwplayer.swf\"></embed>");
					 }
					 else {
						var str = "<object width=" + mywidth + " height=" + myheight + " classid=\"clsid:6BF52A52-394A-11D3-B153-00C04F79FAA6\" codebase=\"http://microsoft.com/windows/mediaplayer/en/download/\">";
						str += "<param name=\"menu\" value=\"true\"/>";
						str += "<param name=\"url\" value=\"" + mysrc + "\"/>";
						str += "<param name=\"autostart\" value=\"" + myloop + "\"/>";
						str += "<param name=\"loop\" value=\"true\"/>";
						str += "</object>";
						editor.insertHtml(str);
					 }
　　　　　　　　},
　　　　　　　　onLoad:　function(){
　　　　　　　　}
　　　　};
});
