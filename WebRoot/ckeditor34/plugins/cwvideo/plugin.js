CKEDITOR.plugins.add('cwvideo',
{
    init: function(editor)    
    {        
        //plugin code goes here
        var pluginName = 'cwvideo';        
        CKEDITOR.dialog.add(pluginName, this.path + 'dialogs/cwvideo.js');        
        editor.addCommand(pluginName, new CKEDITOR.dialogCommand(pluginName));        
        /*editor.addCommand('flvPlayer', new CKEDITOR.dialogCommand('flvPlayer'));
        CKEDITOR.dialog.add('flvPlayer',
        function(editor) {
            return {
                title: '插入Flv视频',
                minWidth: 350,
                minHeight: 100,
                contents: [{
                    id: 'tab1',
                    label: 'First Tab',
                    title: 'First Tab',
                    elements: [{
                        id: 'pagetitle',
                        type: 'text',
                        label: '请输入下一页文章标题<br />(不输入默认使用当前标题+数字形式)'
                    }]
                }],
                onOk: function() {
                    editor.insertHtml(" [page = "+this.getValueOf( 'tab1', 'pagetitle' )+"]");
                }
            };
        });*/
        editor.ui.addButton('cwvideo',
        {               
            label: '插入视频',
            command: pluginName
        });
    }
});
