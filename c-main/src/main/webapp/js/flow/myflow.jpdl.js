(function ($) {

    var myflow = $.myflow;

    /*$.extend(true, myflow.config.rect, {
        attr: {
            r: 8,
            fill: '#48a9f1',
            stroke: '#48a9f1',
            "stroke-width": 2
        }
    });*/

    $.extend(true, myflow.config.props.props, {
        name: {
            name: 'name', label: '名称', value: '新建流程', editor: function () {
                return new myflow.editors.inputEditor();
            }
        },
        key: {
            name: 'key', label: '标识', value: '', editor: function () {
                return new myflow.editors.inputEditor();
            }
        },
        desc: {
            name: 'desc', label: '描述', value: '', editor: function () {
                return new myflow.editors.inputEditor();
            }
        }
    });

    var getPath = function(){
        var localObj = window.location;
        var contextPath = localObj.pathname.split("/")[1];
        var location = (window.location+'').split('/');
        return location[0]+'//'+location[2]+'/'+location[3]; 
    };

    var basePath = getPath();

    $.extend(true, myflow.config.tools.states, {
        start: {
            showType: 'text',
            type: 'start',
            name: {text: '<<start>>'},
            text: {text: '开始'},
            img: {src: 'img/16/start.png', width: 16, height: 16},
            // attr: {width: 50, heigth: 50},
            props: {
                text: {
                    name: 'text', label: '显示', value: '', editor: function () {
                        return new myflow.editors.textEditor();
                    }, value: '开始'
                },
                temp1: {
                    name: 'temp1', label: '文本', value: '', editor: function () {
                        return new myflow.editors.inputEditor();
                    }
                },
                temp2: {
                    name: 'temp2', label: '选择', value: '', editor: function () {
                        return new myflow.editors.selectEditor([{name: 'aaa', value: 1}, {name: 'bbb', value: 2}]);
                    }
                }
            }
        },
        end: {
            showType: 'text',
            type: 'end',
            name: {text: '<<end>>'},
            text: {text: '结束'},
            img: {src: 'img/16/end.png', width: 16, height: 16},
            // attr: {width: 50, heigth: 50},
            props: {
                text: {
                    name: 'text', label: '显示', value: '', editor: function () {
                        return new myflow.editors.textEditor();
                    }, value: '结束'
                },
                temp1: {
                    name: 'temp1', label: '文本', value: '', editor: function () {
                        return new myflow.editors.inputEditor();
                    }
                },
                temp2: {
                    name: 'temp2', label: '选择', value: '', editor: function () {
                        return new myflow.editors.selectEditor([{name: 'aaa', value: 1}, {name: 'bbb', value: 2}]);
                    }
                }
            }
        },
        task: {
            showType: 'text',
            type: 'task',
            name: {text: '<<task>>'},
            text: {text: '任务'},
            inDegree: 0,
            img: {src: 'img/16/man.png', width: 16, height: 16},
            props: {
                text: {
                    name: 'text', label: '显示', value: '任务', editor: function () {
                        return new myflow.editors.textEditor();
                    }
                },
                assignee: {
                    name: 'assignee', label: '用户', value: '', editor: function () {
                        return new myflow.editors.inputEditor();
                    }
                },
                form: {
                    name: 'form', label: '表单', value: '', editor: function () {
                        return new myflow.editors.inputEditor();
                    }
                },
                desc: {
                    name: 'desc', label: '描述', value: '', editor: function () {
                        return new myflow.editors.inputEditor();
                    }
                },
                ActionUser: {name: 'ActionUser', label: '', value: ''},
                ActionTitle: {name: 'ActionTitle', label: '', value: ''},
                ActionUserRealName: {name: 'ActionUserRealName', label: '', value: ''},
                ActionJobCode: {name: 'ActionJobCode', label: '', value: ''},
                ActionJobName: {name: 'ActionJobName', label: '', value: ''},
                ActionProxyJobCode: {name: 'ActionProxyJobCode', label: '', value: ''},
                ActionProxyJobName: {name: 'ActionProxyJobName', label: '', value: ''},
                ActionProxyUserName: {name: 'ActionProxyUserName', label: '', value: ''},
                ActionProxyUserRealName: {name: 'ActionProxyUserRealName', label: '', value: ''},
                ActionColorIndex: {name: 'ActionColorIndex', label: '', value: ''},
                ActionFieldWrite: {name: 'ActionFieldWrite', label: '', value: ''},
                ActionCheckState: {name: 'ActionCheckState', label: '', value: ''},
                ActionDept: {name: 'ActionDept', label: '', value: ''},
                ActionFlag: {name: 'ActionFlag', label: '', value: ''},
                ActionDeptMode: {name: 'ActionDeptMode', label: '', value: ''},
                ActionStrategy: {name: 'ActionStrategy', label: '', value: ''},
                ActionItem1: {name: 'ActionItem1', label: '', value: ''},
                ActionItem2: {name: 'ActionItem2', label: '', value: ''},
                ActionIsMsg: {name: 'ActionIsMsg', label: '', value: ''}
            }
        }
    });
})(jQuery);