(function ($) {
    var LINE_TYPE = {
        "TOWARD": "0",  // 连接线
        "RETURN": "1",  // 返回线
        "BOTH": "2"     // 连接与返回共用一极线
    };

    var ACTION_TYPE = {
        "START": "start",
        "TASK": "task",
        "END": "end"
    };

    // var myflowBasePath = getContextPath() + "/js/flow/";
    var ACTION_IMG = {
        "START": '/resource/img/16/start.png',
        "TASK": '/resource/img/16/man.png',
        "END": '/resource/img/16/end.png'
    };

    var Default_RECT_COUNT = 5;

    var myflow = {};
    myflow.config = {
        editable: true,
        allowStateMultiLine: false,  // 允许节点上连接多条线
        expireUnit: "小时", // 到期时间单位
        licenseDefaultInfo: "测试版，仅供测试，请勿正式使用",
        trialDefaultInfo: "试用版，请勿正式使用",
        licenseKey: '', // license.dat中的key值
        isLicenseValid: true,
        cloudUrl:'',
        textEllipsis: false, // 如果节点的text过长，是否显示省略号
        textMaxLen: 18, // 与ellipsis联用，当ellipsis为true时生效
        moving: {
            flag: false,
            prepdot: {x: 0, y: 0},
            dots: [],
            isNewDot: false,
            temp: [],
            preRect: null
        },
        historys: [],
        lineHeight: 0,
        rootPath: getContextPath(),
        basePath: getContextPath() + "/js/flow/",
        rect: {// 状态
            attr: {
                // x: 10,
                // y: 10,
                width: 100,
                height: 50,
                r: 8,
                fill: '#48a9f1',
                stroke: '#48a9f1',
                "stroke-width": 2
            },
            showType: 'image&text', // image,text,image&text
            type: 'state',
            name: {
                text: 'state',
                // 'font-style': 'italic'
                'font-size': 12,
            },
            text: {
                text: '状态',
                'font-size': 12
            },
            margin: 5,
            props: [],
            img: {}
        },
        path: {// 路径转换
            attr: {
                path: {
                    path: 'M10 10L100 100',
                    stroke: '#808080',
                    fill: "none",
                    "stroke-width": 2,
                    cursor: "pointer"
                },
                pathReturn: {
                    path: 'M10 10L100 100',
                    stroke: '#808080',
                    "stroke-dasharray": "-",
                    fill: "none",
                    "stroke-width": 2,
                    cursor: "pointer"
                },
                markPath: {
                    fill: "none",
                    stroke: "white",
                    "stroke-miterlimit": 10,
                    "stroke-width": 14,
                    "-webkit-tap-highlight-color": "rgba(0, 0, 0, 0)",
                    "visibility": "hidden",
                    "pointer-events": "stroke",
                    "cursor": "crosshair"
                },
                arrow: {
                    path: 'M10 10L10 10',
                    stroke: '#808080',
                    fill: "#808080",
                    "stroke-width": 2,
                    radius: 4
                },
                arrowReturn: {
                    path: 'M10 10L10 10',
                    stroke: '#cccccc',
                    fill: "#cccccc",
                    "stroke-width": 2,
                    radius: 4
                },
                fromDot: {
                    width: 5,
                    height: 5,
                    stroke: '#fff',
                    fill: '#000',
                    cursor: "move",
                    "stroke-width": 2
                },
                toDot: {
                    width: 5,
                    height: 5,
                    stroke: '#fff',
                    fill: '#000',
                    cursor: "move",
                    "stroke-width": 2
                },
                bigDot: {
                    width: 5,
                    height: 5,
                    stroke: '#fff',
                    fill: '#000',
                    cursor: "move",
                    "stroke-width": 2
                },
                smallDot: {
                    width: 5,
                    height: 5,
                    stroke: '#fff',
                    fill: '#000',
                    cursor: "move",
                    "stroke-width": 3
                },
                text: {
                    cursor: "move",
                    'background': '#000'
                }
            },
            text: {
                patten: '',
                textPos: {
                    x: 0,
                    y: -10
                }
            },
            props: {
                text: {
                    name: 'text',
                    label: '显示',
                    value: '',
                    editor: function () {
                        return new myflow.editors.textEditor();
                    }
                }
            }
        },
        tools: {// 工具栏
            attr: {
                left: 10,
                top: 30
            },
            pointer: {},
            path: {},
            states: {},
            save: {
                onclick: function (data) {
                    alert(data);
                }
            }
        },
        props: {// 属性编辑器
            attr: {
                top: 30,
                right: 30
            },
            props: {}
        },
        restore: '',
        activeRects: {// 正处理
            rects: [],
            rectAttr: {
                stroke: '#ffb718',
                fill: '#ffb718',
                "stroke-width": 2
            }
        },
        finishRects: { // 已完成
            rects: [],
            rectAttr: {
                stroke: "#00d3e7",
                fill: '#00d3e7',
                "stroke-width": 2
            }
        },
        returnRects: { // 返回
            rects: [],
            rectAttr: {
                stroke: "#ff773a",
                fill: '#ff773a',
                "stroke-width": 2
            }
        },
        ignoreRects: { // 被忽略
            rects: [],
            rectAttr: {
                stroke: "#eeeeee",
                fill: '#eeeeee',
                "stroke-width": 2
            }
        },
        discardRects: { // 被放弃
            rects: [],
            rectAttr: {
                stroke: "#aaaaaa",
                fill: '#aaaaaa',
                "stroke-width": 2
            }
        },
        historyRects: { // 历史激活状态（暂无用）
            rects: [],
            pathAttr: {
                path: {
                    stroke: '#00ff00'
                },
                arrow: {
                    stroke: '#00ff00',
                    fill: "#00ff00"
                }
            }
        }
    };

    // 判断是否为NaN，不有直接用isNan，因为字符串也会返回true
    function isNaNum(n) {
        if(n !== n) {
            return true;
        } else {
            return false;
        }
    }

    function replaceAll(s, s1, s2) {
        if (isNaNum(s)) {
            console.log(s + " isNaN");
            return "";
        }
        if (isNumeric(s)) {
            return s;
        }
        if (s == 0) {
            return s;
        }
        // console.log("myflow.js", "replaceAll s=" + s);
        return s.replace(new RegExp(s1, "gm"), s2);
    }

    myflow.util = {
        // @task: 注意在控件的link中不能存储"号
        encodeStr: function (str) {
            str = replaceAll(str, ":", "\\colon");
            str = replaceAll(str, ";", "\\semicolon");
            str = replaceAll(str, ",", "\\comma");
            str = replaceAll(str, "\n\r", "\\newline");
            str = replaceAll(str, "\n", "\\newline"); // textarea中的换行是\n
            str = replaceAll(str, "\r", "\\newline"); // IE8 textarea中的换行是\r
            return replaceAll(str, "\"", "\\quot");
        },
        decodeStr: function (str) {
            str = replaceAll(str, "\\\\colon", ":");
            str = replaceAll(str, "\\\\semicolon", ";");
            str = replaceAll(str, "\\\\comma", ",");
            str = replaceAll(str, "\\\\newline", "\n");
            str = replaceAll(str, "\\\\newline", "\r\n");
            return replaceAll(str, "\\\\quot", "\"");
        },
        isLine: function (p1, p2, p3) {// 三个点是否在一条直线上
            var s, p2y;
            if ((p1.x - p3.x) == 0)
                s = 1;
            else
                s = (p1.y - p3.y) / (p1.x - p3.x);
            p2y = (p2.x - p3.x) * s + p3.y;
            // $('body').append(p2.y+'-'+p2y+'='+(p2.y-p2y)+', ');
            if ((p2.y - p2y) < 10 && (p2.y - p2y) > -10) {
                p2.y = p2y;
                return true;
            }
            return false;
        },
        center: function (p1, p2) {// 两个点的中间点
            return {
                x: (p1.x - p2.x) / 2 + p2.x,
                y: (p1.y - p2.y) / 2 + p2.y
            };
        },
        // nextId: (function () {
        //     var uid = 0;
        //     return function () {
        //         return ++uid;
        //     };
        // })(),
        nextId: function () {
            return new Date().getTime();
        },
        connPoint: function (rect, p) {// 计算矩形中心到p的连线与矩形的交叉点
            var start = p, end = {
                x: rect.x + rect.width / 2,
                y: rect.y + rect.height / 2
            };
            // 计算正切角度
            var tag = (end.y - start.y) / (end.x - start.x);
            tag = isNaN(tag) ? 0 : tag;

            var rectTag = rect.height / rect.width;
            // 计算箭头位置
            var xFlag = start.y < end.y ? -1 : 1, yFlag = start.x < end.x
                ? -1
                : 1, arrowTop, arrowLeft;
            // 按角度判断箭头位置
            if (Math.abs(tag) > rectTag && xFlag == -1) {// top边
                arrowTop = end.y - rect.height / 2;
                arrowLeft = end.x + xFlag * rect.height / 2 / tag;
            } else if (Math.abs(tag) > rectTag && xFlag == 1) {// bottom边
                arrowTop = end.y + rect.height / 2;
                arrowLeft = end.x + xFlag * rect.height / 2 / tag;
            } else if (Math.abs(tag) < rectTag && yFlag == -1) {// left边
                arrowTop = end.y + yFlag * rect.width / 2 * tag;
                arrowLeft = end.x - rect.width / 2;
            } else if (Math.abs(tag) < rectTag && yFlag == 1) {// right边
                arrowTop = end.y + rect.width / 2 * tag;
                arrowLeft = end.x + rect.width / 2;
            }
            return {
                x: arrowLeft,
                y: arrowTop
            };
        },

        arrow: function (p1, p2, r) {// 画箭头，p1 开始位置,p2 结束位置, r前头的边长
            var atan = Math.atan2(p1.y - p2.y, p2.x - p1.x) * (180 / Math.PI);

            var centerX = p2.x - r * Math.cos(atan * (Math.PI / 180));
            var centerY = p2.y + r * Math.sin(atan * (Math.PI / 180));

            var x2 = centerX + r * Math.cos((atan + 120) * (Math.PI / 180));
            var y2 = centerY - r * Math.sin((atan + 120) * (Math.PI / 180));

            var x3 = centerX + r * Math.cos((atan + 240) * (Math.PI / 180));
            var y3 = centerY - r * Math.sin((atan + 240) * (Math.PI / 180));
            return [p2, {
                x: x2,
                y: y2
            }, {
                x: x3,
                y: y3
            }];
        }
    };

    myflow.rect = function (o, r, id) {
        // 补上img属性，否则会致文字在渲染时靠在左边，因为resize()中取_o.img.width为undefined
        if(!o['img']) {
            o['img'] = {src:'/resource/img/16/man.png', width:14, height:14};
        }
        else {
            o['img'].width = 16;
            o['img'].height = 16;
        }
        if (o.inDegree == 0) {
            o.type = ACTION_TYPE.START;
            o['img'].src = ACTION_IMG.START;
        } else {
            // o.type = ACTION_TYPE.TASK;
            // o.img.src = ACTION_IMG.TASK;
        }
        // console.log("myflow.config.basePath=" + myflow.config.basePath);
        // console.log("rect", "inDegree", o.inDegree);
        // console.log("rect", "src", o.img.src);

        var _this = this, _uid = myflow.util.nextId(), _o = $.extend(true, {}, myflow.config.rect, o),
            _id = id || 'rect' + _uid, _r = r, // Raphael画笔
            _rect, _img, // 图标
            _name, // 状态名称
            _text, // 显示文本
            _type = _o.type, // 节点类型：开始、任务、结束
            _ox, _oy; // 拖动时，保存起点位置;

        _rect = _r.rect(_o.attr.x, _o.attr.y, _o.attr.width, _o.attr.height,
            _o.attr.r).hide().attr(_o.attr);

        console.log('myflow.config.basePath + _o.img.src', myflow.config.basePath + _o.img.src);
        _img = _r.image(myflow.config.basePath + _o.img.src,
            _o.attr.x + _o.img.width / 2,
            _o.attr.y + (_o.attr.height - _o.img.height) / 2, _o.img.width,
            _o.img.height).hide();
        _o.name.text = ''; // _o.name为state，置为空以不显示
        _name = _r.text(
            _o.attr.x + _o.img.width + (_o.attr.width - _o.img.width) / 2,
            _o.attr.y + myflow.config.lineHeight / 2, _o.name.text).hide()
            .attr(_o.name);
        _text = _r.text(
            _o.attr.x + _o.img.width + (_o.attr.width - _o.img.width) / 2,
            _o.attr.y + (_o.attr.height - myflow.config.lineHeight) / 2
            + myflow.config.lineHeight, _o.text.text).hide()
            .attr(_o.text); // 文本

        // 拖动处理----------------------------------------
        _rect.drag(function (dx, dy) {
            dragMove(dx, dy);
        }, function () {
            dragStart()
        }, function () {
            dragUp();
        });
        _img.drag(function (dx, dy) {
            dragMove(dx, dy);
        }, function () {
            dragStart()
        }, function () {
            dragUp();
        });
        _name.drag(function (dx, dy) {
            dragMove(dx, dy);
        }, function () {
            dragStart()
        }, function () {
            dragUp();
        });
        _text.drag(function (dx, dy) {
            dragMove(dx, dy);
        }, function () {
            dragStart()
        }, function () {
            dragUp();
        });

        var dragMove = function (dx, dy) {// 拖动中
            if (!myflow.config.editable)
                return;

            var x = (_ox + dx); // -((_ox+dx)%10);
            var y = (_oy + dy); // -((_oy+dy)%10);

            _bbox.x = x - _o.margin;
            _bbox.y = y - _o.margin;
            resize();
        };

        var dragStart = function () {// 开始拖动
            _ox = _rect.attr("x");
            _oy = _rect.attr("y");
            _rect.attr({
                opacity: 0.5
            });
            _img.attr({
                opacity: 0.5
            });
            _text.attr({
                opacity: 0.5
            });
        };

        var dragUp = function () {// 拖动结束
            _rect.attr({
                opacity: 1
            });
            _img.attr({
                opacity: 1
            });
            _text.attr({
                opacity: 1
            });
        };

        // 改变大小的边框
        var _bpath, _bdots = {}, _bw = 5, _bbox = {
            x: _o.attr.x - _o.margin,
            y: _o.attr.y - _o.margin,
            width: _o.attr.width + _o.margin * 2,
            height: _o.attr.height + _o.margin * 2
        };

        _bpath = _r.path('M0 0L1 1').hide();
        _bdots['t'] = _r.rect(0, 0, _bw, _bw).attr({
            fill: '#000',
            stroke: '#fff',
            cursor: 's-resize'
        }).hide().drag(function (dx, dy) {
            bdragMove(dx, dy, 't');
        }, function () {
            bdragStart(this.attr('x') + _bw / 2, this.attr('y') + _bw
                / 2, 't');
        }, function () {
        }); // 上
        _bdots['lt'] = _r.rect(0, 0, _bw, _bw).attr({
            fill: '#000',
            stroke: '#fff',
            cursor: 'nw-resize'
        }).hide().drag(function (dx, dy) {
            bdragMove(dx, dy, 'lt');
        }, function () {
            bdragStart(this.attr('x') + _bw / 2, this.attr('y') + _bw
                / 2, 'lt');
        }, function () {
        }); // 左上
        _bdots['l'] = _r.rect(0, 0, _bw, _bw).attr({
            fill: '#000',
            stroke: '#fff',
            cursor: 'w-resize'
        }).hide().drag(function (dx, dy) {
            bdragMove(dx, dy, 'l');
        }, function () {
            bdragStart(this.attr('x') + _bw / 2, this.attr('y') + _bw
                / 2, 'l');
        }, function () {
        }); // 左
        _bdots['lb'] = _r.rect(0, 0, _bw, _bw).attr({
            fill: '#000',
            stroke: '#fff',
            cursor: 'sw-resize'
        }).hide().drag(function (dx, dy) {
            bdragMove(dx, dy, 'lb');
        }, function () {
            bdragStart(this.attr('x') + _bw / 2, this.attr('y') + _bw
                / 2, 'lb');
        }, function () {
        }); // 左下
        _bdots['b'] = _r.rect(0, 0, _bw, _bw).attr({
            fill: '#000',
            stroke: '#fff',
            cursor: 's-resize'
        }).hide().drag(function (dx, dy) {
            bdragMove(dx, dy, 'b');
        }, function () {
            bdragStart(this.attr('x') + _bw / 2, this.attr('y') + _bw
                / 2, 'b');
        }, function () {
        }); // 下
        _bdots['rb'] = _r.rect(0, 0, _bw, _bw).attr({
            fill: '#000',
            stroke: '#fff',
            cursor: 'se-resize'
        }).hide().drag(function (dx, dy) {
            bdragMove(dx, dy, 'rb');
        }, function () {
            bdragStart(this.attr('x') + _bw / 2, this.attr('y') + _bw
                / 2, 'rb');
        }, function () {
        }); // 右下
        _bdots['r'] = _r.rect(0, 0, _bw, _bw).attr({
            fill: '#000',
            stroke: '#fff',
            cursor: 'w-resize'
        }).hide().drag(function (dx, dy) {
            bdragMove(dx, dy, 'r');
        }, function () {
            bdragStart(this.attr('x') + _bw / 2, this.attr('y') + _bw
                / 2, 'r')
        }, function () {
        }); // 右
        _bdots['rt'] = _r.rect(0, 0, _bw, _bw).attr({
            fill: '#000',
            stroke: '#fff',
            cursor: 'ne-resize'
        }).hide().drag(function (dx, dy) {
            bdragMove(dx, dy, 'rt');
        }, function () {
            bdragStart(this.attr('x') + _bw / 2, this.attr('y') + _bw
                / 2, 'rt')
        }, function () {
        }); // 右上
        $([_bdots['t'].node, _bdots['lt'].node, _bdots['l'].node, _bdots['lb'].node, _bdots['b'].node, _bdots['rb'].node, _bdots['r'].node, _bdots['rt'].node]).click(function () {
            return false;
        });

        var bdragMove = function (dx, dy, t) {
            if (!myflow.config.editable)
                return;
            var x = _bx + dx, y = _by + dy;
            switch (t) {
                case 't':
                    _bbox.height += _bbox.y - y;
                    _bbox.y = y;
                    break;
                case 'lt':
                    _bbox.width += _bbox.x - x;
                    _bbox.height += _bbox.y - y;
                    _bbox.x = x;
                    _bbox.y = y;
                    break;
                case 'l':
                    _bbox.width += _bbox.x - x;
                    _bbox.x = x;
                    break;
                case 'lb':
                    _bbox.height = y - _bbox.y;
                    _bbox.width += _bbox.x - x;
                    _bbox.x = x;
                    break;
                case 'b':
                    _bbox.height = y - _bbox.y;
                    break;
                case 'rb':
                    _bbox.height = y - _bbox.y;
                    _bbox.width = x - _bbox.x;
                    break;
                case 'r':
                    _bbox.width = x - _bbox.x;
                    break;
                case 'rt':
                    _bbox.width = x - _bbox.x;
                    _bbox.height += _bbox.y - y;
                    _bbox.y = y;
                    break;
            }
            resize();
            // $('body').append(t);
        };
        var bdragStart = function (ox, oy, t) {
            _bx = ox;
            _by = oy;
        };

        // 点击事件处理
        $([_rect.node, _text.node, _name.node, _img.node]).bind('click',
            function () {
                if (!myflow.config.editable) {
                    return;
                }

                showBox();
                myflow.config.tools.clickRect(_this.getId(), _this.toJson());
                var mod = $(_r).data('mod');
                switch (mod) {
                    case 'pointer':
                        $(_r).data('currNode', _this);
                        break;
                    case 'path':
                    case 'pathReturn':
                        // 判断是否为返回线
                        var isReturn = mod == 'pathReturn';
                        var pre = $(_r).data('currNode');
                        var moving = myflow.config.moving;

                        // 连线类型
                        var lineType = LINE_TYPE.TOWARD;
                        if (isReturn) {
                            lineType = LINE_TYPE.RETURN;
                        }

                        // 禁止两个状态间有重复的线
                        if (!myflow.config.allowStateMultiLine) {
                            var paths = myflow.config.tempData.paths;
                            // 是否存在重复的连线标志
                            var flag = false;
                            for (var k in paths) {
                                if (paths[k]) {
                                    // 如果起点与终点一样，则认为存在有重复的线
                                    if ((moving.preRect && moving.preRect.getId() == paths[k].from().getId()) && (_this.getId() == paths[k].to().getId())) {
                                        flag = true;
                                        break;
                                    }
                                    // 如果起点与终点相反，则判断是否共用一条线
                                    if ((moving.preRect && moving.preRect.getId() == paths[k].to().getId()) && (_this.getId() == paths[k].from().getId())) {
                                        if (paths[k].getType() == LINE_TYPE.TOWARD) {
                                            // 如果当前为返回
                                            if (isReturn) {
                                                lineType = LINE_TYPE.BOTH;
                                                paths[k].setType(lineType);

                                                // 清除临时连接线
                                                myflow.config.moving = {
                                                    flag: false,
                                                    prepdot: {x: 0, y: 0},
                                                    dots: [],
                                                    isNewDot: false,
                                                    preRect: null,
                                                    temp: []
                                                };
                                                moving.temp.map(function (item, index) {
                                                    item.remove();
                                                });
                                                return;
                                            }
                                        } else if (paths[k].getType() == LINE_TYPE.RETURN) {
                                            if (!isReturn) {
                                                lineType = LINE_TYPE.BOTH;
                                                paths[k].setType(lineType);

                                                // 清除临时连接线
                                                myflow.config.moving = {
                                                    flag: false,
                                                    prepdot: {x: 0, y: 0},
                                                    dots: [],
                                                    isNewDot: false,
                                                    preRect: null,
                                                    temp: []
                                                };
                                                moving.temp.map(function (item, index) {
                                                    item.remove();
                                                });
                                                return;
                                            }
                                        } else {
                                            flag = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (flag) {
                                alert('节点间已存在连线');
                                break;
                            }
                        }

                        if ((moving.preRect && moving.preRect == _this)) {
                            break;
                        }

                        moving.flag = true;
                        if (moving.preRect) {
                            $(_r).trigger('addpath', [moving.preRect, _this, moving.dots, isReturn, lineType]);
                            myflow.config.moving = {
                                flag: false,
                                prepdot: {x: 0, y: 0},
                                dots: [],
                                isNewDot: false,
                                preRect: null,
                                temp: []
                            };

                            moving.temp.map(function (item, index) {
                                item.remove();
                            });

                            $(_r).data('currNode', null);
                            break;
                        }
                        // 如果pre是rect，且其ID不为当前rect的ID，则画线
                        if (pre && pre.getId().substring(0, 4) == 'rect') {
                            if (pre.getId() != _id) {
                                $(_r).trigger('addpath', [pre, _this, null, isReturn, lineType]);
                            }
                        }
                        moving.preRect = _this;
                        $(_r).data('currNode', _this);
                        break;
                }
                $(_r).trigger('click', _this);
                return false;
            });

        var clickHandler = function (e, src) {
            if (!myflow.config.editable)
                return;

            if (myflow.config.moving.flag) {
                if (src.getId().substring(0, 4) == '0000') {
                    myflow.config.moving.isNewDot = true;
                }

                if (myflow.config.moving.preRect == src && myflow.config.moving.temp.length > 2) {
                    myflow.config.moving.temp.pop().remove();
                    myflow.config.moving.temp.pop().remove();
                    myflow.config.moving.isNewDot = true;
                }
            }

            if (src.getId() == _id) {
                $(_r).trigger('showprops', [_o.props, src]);
            } else {
                hideBox();
            }
        };
        $(_r).bind('click', clickHandler);

        var textchangeHandler = function (e, text, src) {
            if (src.getId() == _id) {
                _text.attr({
                    text: text
                });
            }
        };
        $(_r).bind('textchange', textchangeHandler);

        // 私有函数-----------------------
        // 边框路径
        function getBoxPathString() {
            return 'M' + _bbox.x + ' ' + _bbox.y + 'L' + _bbox.x + ' '
                + (_bbox.y + _bbox.height) + 'L' + (_bbox.x + _bbox.width)
                + ' ' + (_bbox.y + _bbox.height) + 'L'
                + (_bbox.x + _bbox.width) + ' ' + _bbox.y + 'L' + _bbox.x
                + ' ' + _bbox.y;
        }

        // 显示边框
        function showBox() {
            _bpath.show();
            for (var k in _bdots) {
                _bdots[k].show();
            }
        }

        // 隐藏
        function hideBox() {
            _bpath.hide();
            for (var k in _bdots) {
                _bdots[k].hide();
            }
        }

        // 根据_bbox，更新位置信息
        function resize() {
            var rx = _bbox.x + _o.margin, ry = _bbox.y + _o.margin, rw = _bbox.width
                - _o.margin * 2, rh = _bbox.height - _o.margin * 2;

            _rect.attr({
                x: rx,
                y: ry,
                width: rw,
                height: rh
            });
            switch (_o.showType) {
                case 'image':
                    _img.attr({
                        x: rx + (rw - _o.img.width) / 2,
                        y: ry + (rh - _o.img.height) / 2
                    }).show();
                    break;
                case 'text':
                    _rect.show();
                    _text.attr({
                        x: rx + rw / 2,
                        y: ry + rh / 2
                    }).show(); // 文本
                    _img.attr({
                        x: rx + _o.img.width / 2,
                        y: ry
                    }).show();
                    break;
                case 'image&text':
                    _rect.show();

                    var textShow = _text.node.textContent;
                    if (myflow.config.textEllipsis) {
                        if (textShow.length > myflow.config.textMaxLen) {
                            var ary = textShow.split('：');
                            if (ary.length == 2) {
                                var maxRoleLen = myflow.config.textMaxLen - ary[1].length - 1; // -1为减去冒号的长度
                                textShow = textShow.substr(0, maxRoleLen) + '...' + '：' + ary[1];
                            } else {
                                textShow = textShow.substr(0, myflow.config.textMaxLen) + ' ...';
                            }
                        }
                    }
                    _name.attr({
                        x: rx + _o.img.width + (rw - _o.img.width) / 2,
                        y: ry + (rh - myflow.config.lineHeight) / 2
                            + myflow.config.lineHeight,
                        text: textShow
                    }).show();

                    // _name.attr({
                    //     x: rx + _o.img.width + (rw - _o.img.width) / 2,
                    //     y: ry + myflow.config.lineHeight / 2
                    // }).show();
                    // _text.attr({
                    //     x: rx + _o.img.width + (rw - _o.img.width) / 2,
                    //     y: ry + (rh - myflow.config.lineHeight) / 2
                    //         + myflow.config.lineHeight
                    // }).show(); // 文本
                    console.log('_o.img', _o.img);
                    console.log('rx + _o.img.width / 2', rx + _o.img.width / 2);
                    console.log('ry + _o.img.height / 2', ry + _o.img.height / 2);
                    _img.attr({
                        x: rx + _o.img.width / 2,
                        y: ry + (rh - _o.img.height) / 2
                    }).show();
                    break;
            }

            _bdots['t'].attr({
                x: _bbox.x + _bbox.width / 2 - _bw / 2,
                y: _bbox.y - _bw / 2
            }); // 上
            _bdots['lt'].attr({
                x: _bbox.x - _bw / 2,
                y: _bbox.y - _bw / 2
            }); // 左上
            _bdots['l'].attr({
                x: _bbox.x - _bw / 2,
                y: _bbox.y - _bw / 2 + _bbox.height / 2
            }); // 左
            _bdots['lb'].attr({
                x: _bbox.x - _bw / 2,
                y: _bbox.y - _bw / 2 + _bbox.height
            }); // 左下
            _bdots['b'].attr({
                x: _bbox.x - _bw / 2 + _bbox.width / 2,
                y: _bbox.y - _bw / 2 + _bbox.height
            }); // 下
            _bdots['rb'].attr({
                x: _bbox.x - _bw / 2 + _bbox.width,
                y: _bbox.y - _bw / 2 + _bbox.height
            }); // 右下
            _bdots['r'].attr({
                x: _bbox.x - _bw / 2 + _bbox.width,
                y: _bbox.y - _bw / 2 + _bbox.height / 2
            }); // 右
            _bdots['rt'].attr({
                x: _bbox.x - _bw / 2 + _bbox.width,
                y: _bbox.y - _bw / 2
            }); // 右上
            _bpath.attr({
                path: getBoxPathString()
            });

            $(_r).trigger('rectresize', _this);
        }

        // 函数----------------
        // 转化json字串
        this.toJson = function () {
            /*console.log(_o);
            console.log("degree:" + _o.inDegree);
            console.log(_text);
            */
            var data = "{type:'" + _o.type + "',ID:'" + (!_o.ID ? _id : _o.ID) + "',text:{text:'"
                + (!_text.node ? "" : _text.node.textContent) + "'}, inDegree: " + _o.inDegree + ", attr:{ x:"
                + Math.round(_rect.attr('x')) + ", y:"
                + Math.round(_rect.attr('y')) + ", width:"
                + Math.round(_rect.attr('width')) + ", height:"
                + Math.round(_rect.attr('height')) + "},";
            data += "props: {"
            for (var k in _o.props) {
                // data += k + ":{value:'"+ _o.props[k].value + "'},";
                data += _o.props[k].name + ": {name:'" + _o.props[k].name + "', value:'" + _o.props[k].value + "'},";
            }
            if (data.substring(data.length - 1, data.length) == ',')
                data = data.substring(0, data.length - 1);
            data += "}";
            data += "}";
            return data;
        };
        // 从数据中恢复图
        this.restore = function (data) {
            var obj = data;
            // if (typeof data === 'string')
            // obj = eval(data);

            _o = $.extend(true, _o, data);
            // console.log(obj.text.text);
            _text.attr({
                text: myflow.util.decodeStr(obj.text.text)
            });
            resize();
        };
        this.getBBox = function () {
            return _bbox;
        };
        this.getId = function () {
            return _id;
        };
        this.remove = function () {
            _rect.remove();
            _text.remove();
            _name.remove();
            _img.remove();
            _bpath.remove();
            for (var k in _bdots) {
                _bdots[k].remove();
            }
        };
        this.text = function () {
            return _text.attr('text');
        };
        this.attr = function (attr) {
            if (attr)
                _rect.attr(attr);
        };
        this.setText = function (text) {
            _text.attr('text', text);
        };
        this.getInDegree = function () {
            return _o.inDegree;
        };
        this.setInDegree = function (inDegree) {
            _o.inDegree = inDegree;
        };
        this.clearProps = function () {
            _o.props = {};
        };
        this.getPropVal = function (propName) {
            if (_o.props[propName]) {
                // console.log("propName=" + propName + " propVal1=" + _o.props[propName].value);
                var propVal = myflow.util.decodeStr(_o.props[propName].value);
                // console.log("propName=" + propName + " propVal2=" + propVal);
                return propVal;
            } else {
                console.log("属性 " + propName + " 不存在");
                return "";
            }
        };
        this.getPropValRaw = function (propName) {
            if (_o.props[propName]) {
                // console.log("propName=" + propName + " propVal1=" + _o.props[propName].value);
                return _o.props[propName].value;
                // console.log("propName=" + propName + " propVal2=" + propVal);
            } else {
                console.log("属性 " + propName + " 不存在");
                return "";
            }
        };
        this.getType = function () {
            return _type;
        };
        this.setType = function (type) {
            _type = type;
            _o.type = type;

            if (type == ACTION_TYPE.TASK) {
                // 更改图片
                _img.attr("src", myflow.config.basePath + ACTION_IMG.TASK);
                // console.log("setType", "src", _img.attrs.src);
            } else if (type == ACTION_TYPE.END) {
                _img.attr("src", myflow.config.basePath + ACTION_IMG.END);
            } else {
                _img.attr("src", myflow.config.basePath + ACTION_IMG.START);
            }
        };
        this.resize = function () {
            resize();
        };
        this.setProp = function (propName, propVal) {
            propVal = myflow.util.encodeStr(propVal);
            console.log("setProp", propName, propVal);
            var isFound = false;
            for (var k in _o.props) {
                if (_o.props[k].name == propName) {
                    _o.props[k].value = propVal;
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                _o.props[propName] = {};
                _o.props[propName].name = propName;
                _o.props[propName].value = propVal;
            }
            // console.log(_o.props);
        };

        resize(); // 初始化位置
    };

    // 画线
    myflow.path = function (o, r, from, to, guid, ec, dots, id, isReturn, lineType) {
        var _this = this, _r = r, _o = $.extend(true, {}, myflow.config.path), _path, _markpath, _arrow, _text, _textPos = _o.text.textPos, _ox, _oy, _from = from, _to = to,
            _id = id || 'path' + myflow.util.nextId(),
            _dotList, _autoText = true;
        _o.lineID = guid;
        oec = (ec > 0 ? (parseInt(ec) == 1 ? 25 : parseInt(ec) * 9 + 22) : 0);
        // 另一端的箭头，当为LINE_TYPE.BOTH时，才会显示
        var _arrowOther;
        // 是否为返回线
        var _isReturn = isReturn != null ? isReturn : false;
        // 线型
        var _lineType = lineType;

        // 点
        function dot(type, pos, left, right) {
            var _this = this, _t = type, _n, _lt = left, _rt = right, _ox, _oy, // 缓存移动前时位置
                _pos = pos; // 缓存位置信息{x,y}, 注意：这是计算出中心点

            switch (_t) {
                case 'from':
                    _n = _r.rect(pos.x - _o.attr.fromDot.width / 2,
                        pos.y - _o.attr.fromDot.height / 2,
                        _o.attr.fromDot.width, _o.attr.fromDot.height)
                        .attr(_o.attr.fromDot);
                    break;
                case 'big':
                    _n = _r.rect(pos.x - _o.attr.bigDot.width / 2,
                        pos.y - _o.attr.bigDot.height / 2,
                        _o.attr.bigDot.width, _o.attr.bigDot.height)
                        .attr(_o.attr.bigDot);
                    break;
                case 'small':
                    _n = _r.rect(pos.x - _o.attr.smallDot.width / 2,
                        pos.y - _o.attr.smallDot.height / 2,
                        _o.attr.smallDot.width, _o.attr.smallDot.height)
                        .attr(_o.attr.smallDot);
                    break;
                case 'to':
                    _n = _r.rect(pos.x - _o.attr.toDot.width / 2,
                        pos.y - _o.attr.toDot.height / 2,
                        _o.attr.toDot.width, _o.attr.toDot.height)
                        .attr(_o.attr.toDot);
                    break;
            }
            if (_n && (_t == 'big' || _t == 'small')) {
                _n.drag(function (dx, dy) {
                    dragMove(dx, dy);
                }, function () {
                    dragStart()
                }, function () {
                    dragUp();
                });

                // 初始化拖动
                var dragMove = function (dx, dy) {// 拖动中
                    var x = (_ox + dx), y = (_oy + dy);
                    _this.moveTo(x, y);
                };

                var dragStart = function () {// 开始拖动
                    if (_t == 'big') {
                        _ox = _n.attr("x") + _o.attr.bigDot.width / 2;
                        _oy = _n.attr("y") + _o.attr.bigDot.height / 2;
                    }
                    if (_t == 'small') {
                        _ox = _n.attr("x") + _o.attr.smallDot.width / 2;
                        _oy = _n.attr("y") + _o.attr.smallDot.height / 2;
                    }
                };

                var dragUp = function () {// 拖动结束

                };
            }
            $(_n.node).click(function () {
                return false;
            });

            this.type = function (t) {
                if (t)
                    _t = t;
                else
                    return _t;
            };
            this.node = function (n) {
                if (n)
                    _n = n;
                else
                    return _n;
            };
            this.left = function (l) {
                if (l)
                    _lt = l;
                else
                    return _lt;
            };
            this.right = function (r) {
                if (r)
                    _rt = r;
                else
                    return _rt;
            };
            this.remove = function () {
                _lt = null;
                _rt = null;
                _n.remove();
            };
            this.pos = function (pos) {
                if (pos) {
                    _pos = pos;
                    _n.attr({
                        x: _pos.x - _n.attr('width') / 2,
                        y: _pos.y - _n.attr('height') / 2
                    });
                    return this;
                } else {
                    return _pos
                }
            };

            this.moveTo = function (x, y) {
                this.pos({
                    x: x,
                    y: y
                });

                switch (_t) {
                    case 'from':
                        if (_rt && _rt.right() && _rt.right().type() == 'to') {
                            _rt.right().pos(myflow.util.connPoint(
                                _to.getBBox(), _pos));
                        }
                        if (_rt && _rt.right()) {
                            _rt.pos(myflow.util.center(_pos, _rt.right()
                                .pos()));
                        }
                        break;
                    case 'big':
                        if (_rt && _rt.right() && _rt.right().type() == 'to') {
                            _rt.right().pos(myflow.util.connPoint(_to.getBBox(), _pos));
                        }
                        if (_lt && _lt.left() && _lt.left().type() == 'from') {
                            _lt.left().pos(myflow.util.connPoint(_from.getBBox(), _pos));
                        }
                        if (_rt && _rt.right()) {
                            _rt.pos(myflow.util.center(_pos, _rt.right().pos()));
                        }
                        if (_lt && _lt.left()) {
                            _lt.pos(myflow.util.center(_pos, _lt.left().pos()));
                        }
                        // 三个大点在一条线上，移除中间的小点
                        var pos = {
                            x: _pos.x,
                            y: _pos.y
                        };
                        if (myflow.util.isLine(_lt.left().pos(), pos, _rt.right().pos())) {
                            _t = 'small';
                            _n.attr(_o.attr.smallDot);
                            this.pos(pos);
                            var lt = _lt;
                            _lt.left().right(_lt.right());
                            _lt = _lt.left();
                            lt.remove();
                            var rt = _rt;
                            _rt.right().left(_rt.left());
                            _rt = _rt.right();
                            rt.remove();
                        }
                        break;
                    case 'small': // 移动小点时，转变为大点，增加俩个小点
                        if (_lt && _rt && !myflow.util.isLine(_lt.pos(), {
                            x: _pos.x,
                            y: _pos.y
                        }, _rt.pos())) {
                            _t = 'big';

                            _n.attr(_o.attr.bigDot);
                            var lt = new dot('small', myflow.util.center(_lt
                                .pos(), _pos), _lt, _lt
                                .right());
                            _lt.right(lt);
                            _lt = lt;

                            var rt = new dot('small', myflow.util.center(_rt
                                    .pos(), _pos), _rt.left(),
                                _rt);
                            _rt.left(rt);
                            _rt = rt;
                        }
                        break;
                    case 'to':
                        if (_lt && _lt.left() && _lt.left().type() == 'from') {
                            _lt.left().pos(myflow.util.connPoint(_from.getBBox(), _pos));
                        }
                        if (_lt && _lt.left()) {
                            _lt.pos(myflow.util.center(_pos, _lt.left().pos()));
                        }
                        break;
                }

                refreshpath();
            };
        }

        function dotList() {
            // if(!_from) throw '没有from节点!';
            var _fromDot, _toDot, _fromBB = _from.getBBox(), _toBB = _to
                .getBBox(), _fromPos, _toPos;

            _fromPos = myflow.util.connPoint(_fromBB, {
                x: _toBB.x + _toBB.width / 2,
                y: _toBB.y + _toBB.height / 2
            });
            _toPos = myflow.util.connPoint(_toBB, _fromPos);

            _fromDot = new dot('from', _fromPos, null, new dot('small', {
                x: (_fromPos.x + _toPos.x) / 2 + oec,
                y: (_fromPos.y + _toPos.y) / 2
            }));
            _fromDot.right().left(_fromDot);
            _toDot = new dot('to', _toPos, _fromDot.right(), null);
            _fromDot.right().right(_toDot);

            // 转换为path格式的字串
            this.toPathString = function () {
                if (!_fromDot)
                    return '';

                var d = _fromDot, p = 'M' + d.pos().x + ' ' + d.pos().y, arr = '', arrOther = '';
                // 线的路径
                while (d.right()) {
                    d = d.right();
                    p += 'L' + d.pos().x + ' ' + d.pos().y;
                }
                // 箭头路径
                var arrPos = myflow.util.arrow(d.left().pos(), d.pos(), _o.attr.arrow.radius);
                arr = 'M' + arrPos[0].x + ' ' + arrPos[0].y + 'L' + arrPos[1].x
                    + ' ' + arrPos[1].y + 'L' + arrPos[2].x + ' '
                    + arrPos[2].y + 'z';

                if (_lineType == LINE_TYPE.BOTH) {
                    arrPos = myflow.util.arrow(_fromDot.right().pos(), _fromDot.pos(), _o.attr.arrow.radius);
                    arrOther = 'M' + arrPos[0].x + ' ' + arrPos[0].y + 'L' + arrPos[1].x
                        + ' ' + arrPos[1].y + 'L' + arrPos[2].x + ' '
                        + arrPos[2].y + 'z';
                }
                return [p, arr, arrOther];
            };
            this.toJson = function () {
                var data = "[", d = _fromDot;

                while (d) {
                    if (d.type() == 'big')
                        data += "{x:" + Math.round(d.pos().x) + ",y:"
                            + Math.round(d.pos().y) + "},";
                    d = d.right();
                }
                if (data.substring(data.length - 1, data.length) == ',')
                    data = data.substring(0, data.length - 1);
                data += "]";
                return data;
            };
            this.restore = function (data) {
                var obj = data, d = _fromDot.right();
                for (var i = 0; i < obj.length; i++) {
                    if (!d) {
                        break;
                    }
                    d.moveTo(obj[i].x, obj[i].y);
                    d.moveTo(obj[i].x, obj[i].y);
                    d = d.right();
                }

                this.hide();
            };

            this.fromDot = function () {
                return _fromDot;
            };
            this.toDot = function () {
                return _toDot;
            };
            this.midDot = function () {// 返回中间点
                var mid = _fromDot.right(), end = _fromDot.right().right();
                while (end.right() && end.right().right()) {
                    end = end.right().right();
                    mid = mid.right();
                }
                return mid;
            };
            this.show = function () {
                var d = _fromDot;
                while (d) {
                    d.node().show();
                    d = d.right();
                }
            };
            this.hide = function () {
                var d = _fromDot;
                while (d) {
                    d.node().hide();
                    d = d.right();
                }
            };
            this.remove = function () {
                var d = _fromDot;
                while (d) {
                    if (d.right()) {
                        d = d.right();
                        d.left().remove();
                    } else {
                        d.remove();
                        d = null;
                    }
                }
            };
        }

        // 初始化操作
        _o = $.extend(true, _o, o);
        // 生成_arrowOther
        _arrowOther = _r.path(_o.attr.arrowReturn.path).attr(_o.attr.arrowReturn);

        if (lineType == LINE_TYPE.TOWARD) {
            _path = _r.path(_o.attr.path.path).attr(_o.attr.path);
            _markpath = _r.path(_o.attr.path.path).attr(_o.attr.markPath);
            _arrow = _r.path(_o.attr.arrow.path).attr(_o.attr.arrow);
        } else {
            _path = _r.path(_o.attr.pathReturn.path).attr(_o.attr.pathReturn);
            _markpath = _r.path(_o.attr.pathReturn.path).attr(_o.attr.markPath);
            if (lineType == LINE_TYPE.RETURN) {
                _arrow = _r.path(_o.attr.arrowReturn.path).attr(_o.attr.arrowReturn);
            } else {
                _arrow = _r.path(_o.attr.arrow.path).attr(_o.attr.arrow);
            }
        }

        _dotList = new dotList();
        _dotList.hide();

        _text = _r.text(0, 0, _o.text.text || _o.text.patten.replace('{from}', _from.text()).replace('{to}',
            _to.text())).attr(_o.attr.text);
        _text.attr({
            // "fill": "#ff0000",
            "font-size": "13px",
            "text-anchor": "start"
        });
        _text.drag(function (dx, dy) {
            if (!myflow.config.editable)
                return;
            _text.attr({
                x: _ox + dx,
                y: _oy + dy
            });
        }, function () {
            _ox = _text.attr('x');
            _oy = _text.attr('y');
        }, function () {
            var mid = _dotList.midDot().pos();
            _textPos = {
                x: _text.attr('x') - mid.x,
                y: _text.attr('y') - mid.y
            };
        });

        refreshpath(); // 初始化路径

        // 事件处理--------------------
        $([_path.node, _markpath.node, _arrow.node, _text.node]).bind('click', function () {
            if (!myflow.config.editable)
                return;
            $(_r).trigger('click', _this);
            $(_r).data('currNode', _this);
            myflow.config.tools.clickPath(_id, _this.toJson());
            return false;
        });

        // 处理点击事件，线或矩形
        var clickHandler = function (e, src) {
            if (!myflow.config.editable)
                return;

            if (src && src.getId() == _id) {
                _dotList.show();
                $(_r).trigger('showprops', [_o.props, _this]);
            } else {
                _dotList.hide();
            }

            var mod = $(_r).data('mod');
            switch (mod) {
                case 'pointer':
                    //console.log("点击的是点")
                    break;
                case 'path':
                    //console.log("点击的是线")
                    break;
            }
        };
        $(_r).bind('click', clickHandler);

        // 删除事件处理
        var removerectHandler = function (e, src) {
            if (!myflow.config.editable)
                return;
            if (src
                && (src.getId() == _from.getId() || src.getId() == _to.getId())) {
                $(_r).trigger('removepath', _this);
            }
        };
        $(_r).bind('removerect', removerectHandler);

        // 矩形移动时间处理
        var rectresizeHandler = function (e, src) {
            if (!myflow.config.editable)
                return;
            if (_from && _from.getId() == src.getId()) {
                var rp;
                if (_dotList.fromDot().right().right().type() == 'to') {
                    rp = {
                        x: _to.getBBox().x + _to.getBBox().width / 2,
                        y: _to.getBBox().y + _to.getBBox().height / 2
                    };
                } else {
                    rp = _dotList.fromDot().right().right().pos();
                }
                var p = myflow.util.connPoint(_from.getBBox(), rp);
                _dotList.fromDot().moveTo(p.x, p.y);
                refreshpath();
            }
            if (_to && _to.getId() == src.getId()) {
                var rp;
                if (_dotList.toDot().left().left().type() == 'from') {
                    rp = {
                        x: _from.getBBox().x + _from.getBBox().width / 2,
                        y: _from.getBBox().y + _from.getBBox().height / 2
                    };
                } else {
                    rp = _dotList.toDot().left().left().pos();
                }
                var p = myflow.util.connPoint(_to.getBBox(), rp);
                _dotList.toDot().moveTo(p.x, p.y);
                refreshpath();
            }
        };
        $(_r).bind('rectresize', rectresizeHandler);

        var textchangeHandler = function (e, v, src) {
            if (src.getId() == _id) {// 改变自身文本
                _text.attr({
                    text: v
                });
                _autoText = false;
            }
            //$('body').append('['+_autoText+','+_text.attr('text')+','+src.getId()+','+_to.getId()+']');
            if (_autoText) {
                if (_to.getId() == src.getId()) {
                    //$('body').append('change!!!');
                    _text.attr({
                        text: _o.text.patten.replace('{from}',
                            _from.text()).replace('{to}', v)
                    });
                } else if (_from.getId() == src.getId()) {
                    //$('body').append('change!!!');
                    _text.attr({
                        text: _o.text.patten.replace('{from}', v)
                            .replace('{to}', _to.text())
                    });
                }
            }
        };
        $(_r).bind('textchange', textchangeHandler);

        // 函数-------------------------------------------------
        this.from = function () {
            return _from;
        };
        this.to = function () {
            return _to;
        };
        // 转化json数据
        this.toJson = function () {
            /*console.log(_o);
            console.log(_o.props);
            console.log(_path);*/

            var data = "{lineID:'" + (!_o.lineID ? _id : _o.lineID) + "',from:'" + _from.getId() + "',to:'" + _to.getId()
                + "', dots:" + _dotList.toJson() + ",text:{text:'"
                + _text.attr('text') + "',textPos:{x:"
                + Math.round(_textPos.x) + ",y:" + Math.round(_textPos.y)
                + "}}, type: " + _lineType + ", props:{";
            for (var k in _o.props) {
                // data += _o.props[k].name + ":'"+ _o.props[k].value + "',";
                data += _o.props[k].name + ": {name:'" + _o.props[k].name + "', value:'" + _o.props[k].value + "'},";
            }
            if (data.substring(data.length - 1, data.length) == ',') {
                data = data.substring(0, data.length - 1);
            }
            data += '}}';
            return data;
        };
        this.getText = function () {
            return _o.text.text;
        };
        // 恢复
        this.restore = function (data) {
            var obj = data;

            _o = $.extend(true, _o, data);
            if (_text.attr('text') != _o.text.text) {
                _text.attr({text: _o.text.text});
                _autoText = false;
            }

            _dotList.restore(obj.dots);
        };
        // 删除
        this.remove = function () {
            _dotList.remove();
            _path.remove();
            _markpath.remove();
            _arrow.remove();
            if (_arrowOther) {
                _arrowOther.remove();
            }
            _text.remove();
            try {
                $(_r).unbind('click', clickHandler);
            } catch (e) {
            }
            try {
                $(_r).unbind('removerect', removerectHandler);
            } catch (e) {
            }
            try {
                $(_r).unbind('rectresize', rectresizeHandler);
            } catch (e) {
            }
            try {
                $(_r).unbind('textchange', textchangeHandler);
            } catch (e) {
            }
        };

        // 重绘路径
        function refreshpath() {
            if (_lineType == LINE_TYPE.BOTH) {
                _path.attr(_o.attr.pathReturn);
                _arrowOther.attr(_o.attr.arrowReturn);
            }

            // 生成线条路径
            var p = _dotList.toPathString();
            var mid = _dotList.midDot().pos();
            _path.attr({
                path: p[0]
            });
            _markpath.attr({
                path: p[0]
            });
            _arrow.attr({
                path: p[1]
            });
            // 另一端的箭头
            if (p[2]) {
                _arrowOther.attr({
                    path: p[2]
                });
            } else {
                _arrowOther.attr({});
            }
            _text.attr({
                x: mid.x + _textPos.x,
                y: mid.y + _textPos.y
            });
        }

        this.getId = function () {
            return _id;
        };
        this.text = function () {
            return _text.attr('text');
        };
        this.attr = function (attr) {
            if (attr && attr.path)
                _path.attr(attr.path);
            if (attr && attr.arrow)
                _arrow.attr(attr.arrow);
            if (attr && attr.arrowOther) {
                _arrow.attr(attr.arrowOther);
            }
        };
        this.getType = function () {
            return _lineType;
        };
        this.setType = function (lineType) {
            _lineType = lineType;
            refreshpath();
        };
        this.clearProps = function () {
            _o.props = {};
        };
        this.setText = function (text) {
            _text.attr('text', text);
        };
        this.getPropVal = function (propName) {
            if (_o.props[propName]) {
                var propVal = myflow.util.decodeStr(_o.props[propName].value);
                console.log("propName=" + propName + " propVal=" + propVal);
                return propVal;
            } else {
                console.log("连接线属性 " + propName + " 不存在");
                return "";
            }
        };
        this.getPropValRaw = function (propName) {
            if (_o.props[propName]) {
                return _o.props[propName].value;
            } else {
                console.log("连接线属性 " + propName + " 不存在");
                return "";
            }
        };
        this.setProp = function (propName, propVal) {
            propVal = myflow.util.encodeStr(propVal);
            console.log("path setProp", propName, propVal);
            // 置连接线属性
            if (!_o.props) {
                _o.props = {};
            }
            _o.props[propName] = {};
            _o.props[propName].name = propName;
            _o.props[propName].value = propVal;
            // console.log(_o);
        };

        if (dots) {
            _dotList.restore(dots);
            rectresizeHandler(null, _to);
            if (!isReturn) {
                $('#path').click();
            } else {
                $('#pathReturn').click();
            }
            $(_r).data('currNode', null);
        }
    };

    myflow.props = function (o, r) {
        var _this = this, _pdiv = $('#myflow_props').hide().draggable({
            handle: '#myflow_props_handle'
        }).resizable().css(myflow.config.props.attr).bind('click',
            function () {
                return false;
            }), _tb = _pdiv.find('table'), _r = r, _src;

        var showpropsHandler = function (e, props, src) {
            if (_src && _src.getId() == src.getId()) {// 连续点击不刷新
                return;
            }
            _src = src;
            $(_tb).find('.editor').each(function () {
                var e = $(this).data('editor');
                if (e)
                    e.destroy();
            });

            _tb.empty();
            _pdiv.show();
            for (var k in props) {
                _tb.append('<tr><th>' + props[k].label + '</th><td><div id="p'
                    + k + '" class="editor"></div></td></tr>');
                if (props[k].editor)
                    props[k].editor().init(props, k, 'p' + k, src, _r);
                // $('body').append(props[i].editor+'a');
            }

            _tb.append('<tr id="myflowDelTR"><th>删除</th><td><input type="button" value="删除" onclick="if(confirm(\'确认删除？！\'))$(document).trigger(\'keydown\',true);"/></td></tr>');
        };
        $(_r).bind('showprops', showpropsHandler);

    };

    // 属性编辑器
    myflow.editors = {
        textEditor: function () {
            var _props, _k, _div, _src, _r;
            this.init = function (props, k, div, src, r) {
                _props = props;
                _k = k;
                _div = div;
                _src = src;
                _r = r;

                $('<input style="width:100%;"/>').val(_src.text()).change(
                    function () {
                        props[_k].value = $(this).val();
                        $(_r).trigger('textchange', [$(this).val(), _src]);
                    }).appendTo('#' + _div);

                $('#' + _div).data('editor', this);
            };
            this.destroy = function () {
                $('#' + _div + ' input').each(function () {
                    _props[_k].value = $(this).val();
                    $(_r).trigger('textchange', [$(this).val(), _src]);
                });
                // $('body').append('destroy.');
            };
        }
    };

    // 初始化流程
    myflow.init = function (c, o) {
        defaultMaxRectCount = Default_RECT_COUNT; // 默认节点数为5，不能超过5个
        maxRectCount = 5;

        // _states为全部节点 _paths为全部连接线
        var _w = $(window).width(), _h = $(window).height(), _r = Raphael(c, _w * 1.5, _h * 1.5), _states = {}, _paths = {};

        var licenseText = _r.text(10, 15, myflow.config.licenseDefaultInfo);
        licenseText.attr({
            "fill": "#cecece",
            "font-size": "14px",
            "text-anchor": "start"
        });

        var trialText = _r.text(10, -35, myflow.config.trialDefaultInfo);
        trialText.attr({
            "fill": "#cecece",
            "font-size": "16px",
            "text-anchor": "start",
            "opacity": 0.6
        });

        var licenseInvalidText =  _r.text(window.screen.width/2 - 100, window.screen.height/2 - 80, '');
        licenseInvalidText.attr({
            "fill": "red",
            "font-size": "14px",
            "text-anchor": "start"
        });

        $.extend(true, myflow.config, o);

        // myflow.config.basePath = myflow.config.rootPath + "/js/flow/";
        myflow.config.basePath = '';

        /**
         * 删除： 删除状态时，触发removerect事件，连接在这个状态上当路径监听到这个事件，触发removepath删除自身；
         * 删除路径时，触发removepath事件
         */
        $(document).keydown(function (arg, byButton) {
            if (!myflow.config.editable)
                return;

            if (arg.keyCode == 46 || (arg.originalEvent && arg.originalEvent.code == 'Backspace') || byButton) {
                var c = $(_r).data('currNode');
                if (c) {
                    // 长度为32，但不以rect开头，则是A版设计器转换升级过来的
                    if (c.getId().substring(0, 4) == 'rect' || c.getId().length == 32) {
                        //添加到历史记录
                        myflow.config.historys.push({state: "removerect", object: c, data: getJson()});

                        myflow.config.tools.deleteRect(c.getId(), c.toJson());
                        $(_r).trigger('removerect', c);

                        /*清除自定义轨迹*/
                        myflow.config.moving.temp.map(function (item, index) {
                            item.remove();
                        });
                        myflow.config.moving = {
                            flag: false,
                            prepdot: {x: 0, y: 0},
                            dots: [],
                            isNewDot: false,
                            preRect: null,
                            temp: []
                        };
                    } else if (c.getId().substring(0, 4) == 'path') {
                        //添加到历史记录
                        myflow.config.historys.push({state: "removepath", object: c, data: getJson()});

                        // 计算节点的入度
                        var _paths = myflow.getPaths();
                        var inDegree = 0;
                        for (var k in _paths) {
                            var _path = _paths[k];
                            // 便于开发时调试，实际不应出现_path为null的情况
                            if (!_path) {
                                continue;
                            } else {
                                console.warn("连接线：" + k + " 不存在");
                            }
                            if (_path.getId() == c.getId()) {
                                continue;
                            }
                            // console.log("_path.getType()=" + _path.getType() + " _path.to().getId()=" + _path.to().getId() + " to.getId()=" + to.getId());
                            if (_path.getType() == LINE_TYPE.BOTH || _path.getType() == LINE_TYPE.TOWARD) {
                                if (_path.to().getId() == c.to().getId()) {
                                    inDegree++;
                                }
                            }
                        }
                        c.to().setInDegree(inDegree);
                        // console.log("removepath", "inDegree", inDegree);
                        if (inDegree == 0) {
                            c.to().setType(ACTION_TYPE.START);
                        }

                        myflow.config.tools.deletePath(c.getId());
                        $(_r).trigger('removepath', c);
                    }
                    $(_r).removeData('currNode');
                }
            }
        });

        $(document).click(function () {
            $(_r).data('currNode', null);

            myflow.config.tempData = {
                paths: _paths,
                states: _states
            };

            $(_r).trigger('click', {
                getId: function () {
                    return '00000000';
                }
            });
            $(_r).trigger('showprops', [myflow.config.props.props, {
                getId: function () {
                    return '00000000';
                }
            }]);
        });

        // 删除事件
        var removeHandler = function (e, src) {
            if (!myflow.config.editable)
                return;
            // id的长度为32表示从A版设计器转换升级过来的
            if (src.getId().substring(0, 4) == 'rect' || src.getId().length==32) {
                _states[src.getId()] = null;
                src.remove();
            } else if (src.getId().substring(0, 4) == 'path') {
                _paths[src.getId()] = null;
                src.remove();
            }
        };
        $(_r).bind('removepath', removeHandler);
        $(_r).bind('removerect', removeHandler);

        // 添加状态
        $(_r).bind('addrect', function (e, type, o) {
            // console.log("_states.length=" + Object.keys(_states).length + " maxRectCount=" + maxRectCount);
            var len = Object.keys(_states).length;
            if (len >= maxRectCount) {
                alert("受许可证限制，节点数量不能大于" + maxRectCount + "个");
                return;
            }

            var data = getJson();
            var rect = new myflow.rect($.extend(true, {}, myflow.config.tools.states[type], o), _r);
            myflow.config.tools.addRect(rect.getId(), rect.toJson());
            _states[rect.getId()] = rect;

            // 添加到历史记录
            myflow.config.historys.push({state: "addrect", object: rect, data: data});
        });

        function getNodeID(obj) {
            var json = obj.toJson();
            var str = json.split(',')[1];
            return str.substring(4, str.length - 1);
        }

        // 添加路径
        var addpathHandler = function (e, from, to, dots, isReturn, lineType) {
            var data = getJson();
            var path = new myflow.path({}, _r, from, to, null, null, dots, null, isReturn, lineType);
            myflow.config.tools.addPath(path.getId(), path.toJson());
            _paths[path.getId()] = path;
            // 计算节点的入度
            var inDegree = 0;
            for (var k in _paths) {
                var _path = _paths[k];
                // 便于开发时调试，实际不应出现_path为null的情况
                if (!_path) {
                    continue;
                } else {
                    console.warn("连接线：" + k + " 不存在");
                }
                // console.log("_path.getType()=" + _path.getType() + " _path.to().getId()=" + _path.to().getId() + " to.getId()=" + to.getId());
                if (_path.getType() == LINE_TYPE.BOTH || _path.getType() == LINE_TYPE.TOWARD) {
                    if (_path.to().getId() == to.getId()) {
                        inDegree++;
                    }
                }
            }

            to.setInDegree(inDegree);
            // console.log("addpathHandler", "inDegree", inDegree);
            // console.log("addpathHandler", "to.getType()", to.getType());

            if (inDegree > 0 && to.getType() == ACTION_TYPE.START) {
                // 检查所有的节点，如果不存在有其它为START的节点，则本节点仍保持为开始节点，否则置为普通任务节点
                var hasOtherStart = false;
                for (var k in _states) {
                    if (_states[k].getType() == ACTION_TYPE.START && _states[k].getId() != to.getId()) {
                        hasOtherStart = true;
                        break;
                    }
                }
                console.log("addpathHandler", "hasOtherStart", hasOtherStart);
                if (hasOtherStart) {
                    to.setType(ACTION_TYPE.TASK);
                }
                // to.resize();
            }
            /*var states = new myflow.getStates();
            var toState = states[to.getId()];
            toState.setInDegree(inDegree);
            console.log(toState.toJson());*/
            /*var states = new myflow.getStates();
            for (var k in states) {
                console.log(states[k].getInDegree());
            }*/

            // 添加到历史记录
            myflow.config.historys.push({state: "addpath", object: path, data: data});
        };
        $(_r).bind('addpath', addpathHandler);

        var path, rect, circle;
        $("#myflow").mousemove(function (e) {
            var moving = myflow.config.moving;
            if (moving.flag) {
                var pre = $(_r).data('currNode');

                if (path && !moving.isNewDot) {
                    path.remove();
                    circle.remove();
                } else {
                    moving.isNewDot = false;
                }

                var dot = moving.prepdot;

                if (pre && pre.getBBox()) {
                    dot = myflow.util.connPoint(pre.getBBox(), {x: e.pageX, y: e.pageY});
                }
                var x = e.pageX - 10, y = e.pageY - 10;
                circle = _r.circle(x, y, 6).attr({fill: 'red', stroke: '#fff', cursor: 'move'});

                path = _r.path('M' + dot.x + ' ' + dot.y + 'L' + x + ' ' + y + 'z')
                    .attr({stroke: '#808080', fill: "none", "stroke-width": 2, cursor: "pointer"});

                moving.temp.push(circle);
                moving.temp.push(path);
            }
        });

        // 取消描点
        document.oncontextmenu = function (e) {
            if (myflow.config.editable) {
                var mod = $(_r).data('mod');
                if (mod == 'path') {
                    $("#path").click();
                } else {
                    $("#pathReturn").click();
                }
                // 使点击鼠标右键后回到”选择“模式                
                $("#pointer").click();
                return false;
            }
        };

        $("#myflow").click(function (e) {
            if (myflow.config.moving.flag) {
                var dot = {
                    x: e.pageX - 10,
                    y: e.pageY - 10
                };
                myflow.config.moving.prepdot = dot;
                myflow.config.moving.dots.push(dot);
            }
        });

        // 置为选择模式
        $(_r).data('mod', 'pointer');
        if (myflow.config.editable) {
            // 工具栏
            $("#myflow_tools").draggable({
                handle: '#myflow_tools_handle'
            }).css(myflow.config.tools.attr);

            $('#myflow_tools .node').hover(function () {
                $(this).addClass('mover');
            }, function () {
                $(this).removeClass('mover');
            });

            // 初始化工具条点击事件
            $('#myflow_tools .selectable').click(function () {
                $('.selected').removeClass('selected');
                $(this).addClass('selected');
                $(_r).data('mod', this.id);
            });

            $('#myflow_tools .state').each(function () {
                $(this).draggable({
                    helper: 'clone'
                });
            });

            $(c).droppable({
                accept: '.state',
                drop: function (event, ui) {
                    //console.log(ui.helper.context);
                    var temp = ui.helper.context.innerHTML;
                    var id = temp.substring(temp.indexOf(">") + 1, temp.length).replace(/^\s\s*/, '').replace(/\s\s*$/, '');
                    $(_r).trigger('addrect', [ui.helper.attr('type'), {
                        attr: {
                            x: ui.helper.offset().left,
                            y: ui.helper.offset().top
                        }
                    }, id]);
                }
            });

            function getJson() {
                var data = '{states:{';
                for (var k in _states) {
                    if (_states[k]) {
                        data += "'" + _states[k].getId() + "':" + _states[k].toJson() + ",";
                    }
                }
                if (data.substring(data.length - 1, data.length) == ',')
                    data = data.substring(0, data.length - 1);
                data += '},paths:{';
                for (var k in _paths) {
                    if (_paths[k]) {
                        data += "'" + _paths[k].getId() + "':" + _paths[k].toJson() + ",";
                    }
                }
                if (data.substring(data.length - 1, data.length) == ',')
                    data = data.substring(0, data.length - 1);
                //data += '},props:{props:{';
                data += '}}';
                return data;
            }

            $('#myflow_save').click(function () {// 保存
                myflow.config.tools.save(getJson())
            });
            $('#myflow_publish').click(function () {// 发布
                myflow.config.tools.publish(getJson())
            });
            $('#myflow_del').click(function () {// 保存
                $(document).trigger('keydown', true);
                /*jConfirm("您确认要删除么？", "提示", function (r) {
                    if (!r) {
                        return;
                    } else {
                        $(document).trigger('keydown', true);
                    }
                });*/
            });
            $('#myflow_revoke').click(function () {//撤销
                var temp = myflow.config.historys.pop();
                if (temp) {
                    switch (temp.state) {
                        case "addpath":
                            $(_r).trigger('removepath', temp.object);
                            break;
                        case "addrect":
                            $(_r).trigger('removerect', temp.object);
                            break;
                        case "removepath":
                            restore(eval("(" + temp.data + ")"));
                            break;
                        case "removerect":
                            restore(eval("(" + temp.data + ")"));
                            break;
                    }
                } else {
                    alert("无撤销项！");
                }
            });

            // 重绘
            $("#myflow_redraw").click(function () {
                if (_states) {
                    for (var k in _states) {
                        _states[k].remove();
                    }
                }
                if (_paths) {
                    for (var k in _paths) {
                        _paths[k].remove();
                    }
                }

                // 不能直接按下列方式清除，需先调用各个元素的remove方法，否则选择节点时会报错
                // 但是注意在上面remove后，_states数组中可能会有残留，所以这儿需置为{}
                _states = {};
                _paths = {};

                myflow.config.moving.temp.map(function (item, index) {
                    item.remove();
                });
                myflow.config.moving = {
                    flag: false,
                    prepdot: {x: 0, y: 0},
                    dots: [],
                    isNewDot: false,
                    preRect: null,
                    temp: []
                };
            });

            $("#pointer").click(function () { //重绘
                myflow.config.moving.temp.map(function (item, index) {
                    item.remove();
                })
                myflow.config.moving = {
                    flag: false,
                    prepdot: {x: 0, y: 0},
                    dots: [],
                    isNewDot: false,
                    preRect: null,
                    temp: []
                };
            });

            // 不显示属性框
            // new myflow.props({}, _r);
        } else {
            $("#myflow_tools").hide();
            $("#myflow_props").hide();
        }

        // 恢复
        if (o.restore) {
            restore(o.restore);
        }

        // 从localStorage中恢复
        function restore(data) {
            var rmap = {};
            if (data.states) {
                for (var k in data.states) {
                    // console.log(data.states[k]);
                    if (!_states[k]) {
                        var rect = new myflow.rect(
                            $.extend(
                                true,
                                {},
                                myflow.config.tools.states[data.states[k].type],
                                data.states[k]), _r, k);
                        rect.restore(data.states[k]);
                        rmap[k] = rect;
                        _states[rect.getId()] = rect;
                    }
                }
            }
            if (data.paths) {
                // console.log(data.paths);
                for (var k in data.paths) {
                    if (!_paths[k]) {
                        var from = rmap && rmap[data.paths[k].from] || _states[data.paths[k].from];
                        var to = rmap && rmap[data.paths[k].to] || _states[data.paths[k].to];
                        // console.log(data.paths[k]);
                        var p = new myflow.path($.extend(true, {}, myflow.config.tools.path, data.paths[k]), _r, from, to, null, null, null, k, data.paths[k].isReturn, data.paths[k].type);
                        p.restore(data.paths[k]);
                        _paths[p.getId()] = p;
                    }
                }
            }
        }

        // 历史状态
        var hr = myflow.config.historyRects,
            ar = myflow.config.activeRects,
            fr = myflow.config.finishRects,
            ir = myflow.config.ignoreRects,
            dr = myflow.config.discardRects,
            rr = myflow.config.returnRects;
        if (hr.rects.length || ar.rects.length || fr.rects.length || ir.rects.length || dr.rects.length || rr.rects.length) {
            for (var i = 0; i < hr.rects.length; i++) {
                if (_states[hr.rects[i].ID])
                    _states[hr.rects[i].ID].rect.attr(hr.rectAttr);
            }
            for (var i = 0; i < ar.rects.length; i++) {
                // console.log(ar.rects[i].ID);
                // console.log(_states[ar.rects[i].ID]);
                if (_states[ar.rects[i].ID])
                    _states[ar.rects[i].ID].attr(ar.rectAttr);
            }
            for (var i = 0; i < fr.rects.length; i++) {
                if ( _states[fr.rects[i].ID])
                    _states[fr.rects[i].ID].attr(fr.rectAttr);
            }
            for (var i = 0; i < ir.rects.length; i++) {
                if (_states[ir.rects[i].ID])
                    _states[ir.rects[i].ID].attr(ir.rectAttr);
            }
            for (var i = 0; i < dr.rects.length; i++) {
                if (_states[dr.rects[i].ID])
                    _states[dr.rects[i].ID].attr(dr.rectAttr);
            }
            for (var i = 0; i < rr.rects.length; i++) {
                if (_states[rr.rects[i].ID])
                    _states[rr.rects[i].ID].attr(rr.rectAttr);
            }
        }

        myflow.states = _states;
        myflow.paths = _paths;

        myflow.getJson = getJson;

        function randomHexColor() { //随机生成十六进制颜色
            var hex = Math.floor(Math.random() * 16777216).toString(16); //生成ffffff以内16进制数
            while (hex.length < 6) { //while循环判断hex位数，少于6位前面加0凑够6位
                hex = '0' + hex;
            }
            return '#' + hex; //返回‘#'开头16进制颜色
        }

        var mykey = "8";
        mykey += "88";
        mykey += "96";
        mykey += "956";
    };

    myflow.getStates = function () {
        return myflow.states;
    };

    myflow.getPaths = function () {
        return myflow.paths;
    };

    var activateCode = "";
    myflow.getWorkflow = function () {
        if (activateCode == "") {
            // 获取激活码
            activateCode = ajaxPost('/public/lic/getAc');
            /* $.ajax({
                async: false,
                type: "post",
                url: myflow.config.rootPath + "/activex/ac.dat?time=" + new Date().getTime(),
                data: {
                },
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                },
                complete: function (XMLHttpRequest, status) {
                },
                success: function (data, status) {
                    activateCode = data;
                    // console.log("activateCode=" + data);
                },
                error: function (XMLHttpRequest, textStatus) {
                    console.log(XMLHttpRequest.responseText);
                    alert("获取激活码失败");
                }
            }); */
        }

        var flowString = "";
        var url = "";
        if (myflow.config.cloudUrl.indexOf("http")===0) {
            // 远程
            url = myflow.config.cloudUrl + "/public/myflow/generateFlowString.do";
        }
        else {
            // 本地
            url = myflow.config.cloudUrl + "/myflow/generateFlowString.do";
        }
        $.ajax({
            async: false,
            type: "post",
            url: url,
            contentType: "application/x-www-form-urlencoded; charset=utf-8",
            data: {
                flowJson: myflow.getJson(),
                activateCode: activateCode,
                key: myflow.config.licenseKey.substr(0, 8),
                serverName: document.domain
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
            },
            success: function (data, status) {
                // console.log(data);
                data = $.parseJSON(data.trim());
                if (data.ret == 1) {
                    flowString = data.flowString;
                }
                else {
                    alert(data.msg);
                }
            },
            complete: function (XMLHttpRequest, status) {
            },
            error: function (XMLHttpRequest, textStatus) {
                alert("错误：0X002，网络连接出错");
                console.log(XMLHttpRequest.responseText);
            }
        });

        return flowString;
        /*
        直接生成flowString，可用，但为了防破解的原因，改为在云端生成
        var flowString = "paper:2592,2160;\r\n";

        for (var k in myflow.states) {
            var state = myflow.states[k];

            if (state == null) {
                // @task: 删除节点时，可能未从states中清掉
                continue;
            }

            var stateStr = "workflow_action:";

            var stateJson = eval("(" + state.toJson() + ")");
            stateStr += stateJson.attr.x; // 0、left
            stateStr += "," + stateJson.attr.y; // 1、top
            stateStr += "," + (stateJson.attr.x + stateJson.attr.width); // 2、right
            stateStr += "," + (stateJson.attr.y + stateJson.attr.height); // 3、bottom
            stateStr += "," + state.getPropValRaw("ActionTitle"); // 4、title
            stateStr += "," + state.getId(); // 5、internalname
            stateStr += "," + 0; // 6、GetGroup()始终为0
            stateStr += "," + state.getPropValRaw("ActionUser"); // 7、userName
            stateStr += "," + 0; // GetKind()=0
            stateStr += ","; // radiate=''
            stateStr += ","; // aggregate=''
            stateStr += "," + state.getPropValRaw("ActionCheckState"); // 11、status
            stateStr += ","; // 12、reason=''
            stateStr += "," + state.getPropValRaw("ActionUserRealName"); // 13、userRealName
            stateStr += "," + state.getPropValRaw("ActionJobCode"); // 14、jobCode
            stateStr += "," + state.getPropValRaw("ActionJobName"); // 15、jobName
            stateStr += "," + state.getPropValRaw("ActionProxyJobCode"); // 16、direction(proxyJobCode)
            stateStr += "," + state.getPropValRaw("ActionProxyJobName"); // 17、rankCode(proxyJobName)
            stateStr += "," + state.getPropValRaw("ActionProxyUserName"); // 18、rankName(proxyUserName)
            stateStr += "," + state.getPropValRaw("ActionProxyUserRealName"); // 19、relateRoleToOrganization(1或0, proxyUserRealName)
            stateStr += ","; // result=''
            stateStr += "," + state.getPropValRaw("ActionFieldWrite"); // fieldWrite
            stateStr += "," + state.getPropValRaw("ActionColorIndex"); // officeColorIndex
            stateStr += "," + state.getPropValRaw("ActionDept"); // dept
            stateStr += "," + state.getPropValRaw("ActionFlag"); // flag
            stateStr += "," + state.getPropValRaw("ActionDeptMode"); // nodeMode(m_deptMode) 0表示NODE_MODE_ROLE
            stateStr += "," + state.getPropValRaw("ActionStrategy"); // strategy
            stateStr += "," + state.getPropValRaw("ActionItem1"); // item1，是否为结束型节点
            stateStr += "," + state.getPropValRaw("ActionItem2"); // item2
            stateStr += ","; // item3='' 冗余
            stateStr += ","; // item4='' 冗余
            stateStr += ","; // item5='' 冗余
            stateStr += "," + state.getPropValRaw("ActionIsMsg"); // msg(1|0)
            stateStr += ",A_END;\r\n";

            flowString += stateStr;
        }

        for (var k in myflow.paths) {
            var path = myflow.paths[k];
            if (path == null) {
                // @task: 删除节点时，可能未从paths中清掉
                continue;
            }

            var pathJson = eval("(" + path.toJson() + ")");

            var pathStr = "workflow_link:";
            pathStr += "2"; // fromtype(连接位置)
            pathStr += ",1";    // totype(连接位置)
            pathStr += "," + path.getPropValRaw("title");  // 2、writetitle
            pathStr += "," + path.from().getId(); // 3、from
            pathStr += "," + path.to().getId(); // 4、to

            var stateJsonFrom = eval("(" + path.from().toJson() + ")");
            var fromX = stateJsonFrom.attr.x;
            var fromW = stateJsonFrom.attr.width;
            var fromY = stateJsonFrom.attr.y;
            var fromH = stateJsonFrom.attr.height;

            var stateJsonTo = eval("(" + path.to().toJson() + ")");
            var toX = stateJsonTo.attr.x;
            var toW = stateJsonTo.attr.width;
            var toY = stateJsonTo.attr.y;
            var toH = stateJsonTo.attr.height;

            var startX = fromX + fromW;
            var startY = fromY + fromH / 2; // 开始点位置于from节点的右侧中间点
            pathStr += "," + startX; // start.x
            pathStr += "," + startY; // start.y

            var endX = toX;
            var endY = toY + toH / 2;
            pathStr += "," + endX; // end.x
            pathStr += "," + endY; // end.y

            var joint1X = "", joint1Y = "", joint2X = "", joint2Y = "";
            // console.log(pathJson);
            var dots = pathJson["dots"];
            console.log("getWorkflow", "dots", dots);
            if (dots.length == 0) {
                // 如果没有dots，则取开始与结束节点之间的中间点
                joint1X = (startX + endX) / 2;
                joint1Y = startY;
            } else {
                joint1X = dots[0].x;
                joint1Y = dots[0].y;
            }
            if (dots.length > 1) {
                joint2X = dots[1].x;
                joint2Y = dots[1].y;
            } else {
                joint2X = joint1X;
                joint2Y = endY;
            }
            console.log("joint1X=" + joint1X + " joint1Y=" + joint1Y + " joint2X=" + joint2X + " joint2Y=" + joint2Y);
            pathStr += "," + joint1X;
            pathStr += "," + joint1Y;
            pathStr += "," + joint2X;
            pathStr += "," + joint2Y;
            pathStr += ",0";    // 13、isSpeedup(1|0)
            pathStr += ",2020"; // 14、speedupDate_y
            pathStr += ",12";   // 15、speedupDate_m
            pathStr += ",03";   // 16、speedupDate_d
            pathStr += "," + path.getType(); // 17、TYPE_TOWARD等

            pathStr += "," + path.getPropValRaw("desc");    // 18、condDesc
            pathStr += "," + path.getPropValRaw("conditionType"); // 19、condType(item1)
            pathStr += "," + path.getPropValRaw("expireHour");   // 20、expireAction(item3)
            pathStr += "," + path.getPropValRaw("expireAction");   // 21、expireHour(item2)
            pathStr += ","; // item4 冗余
            pathStr += ","; // item5 冗余
            pathStr += ",L_END;\r\n";

            flowString += pathStr;
        }

        flowString += "version:1, 3, 0, 0;";
        return flowString;
        */
    };

    myflow.getJson = function () {
        return myflow.getJson;
    };

    // 添加jquery方法
    $.fn.myflow = function (o) {
        return this.each(function () {
            myflow.init(this, o);
        });
    };

    $.myflow = myflow;

    $.extend($.fn, {
        // 重置节点的状态
        resetAllRectStatus: function () {
            var states = myflow.getStates();
            for (var k in states) {
                var state = states[k];
                state.attr(myflow.config.rect.attr);
            }
        },
        // 置节点状态，用于回放
        setRectStatus: function (id, status) {
            var states = myflow.getStates();
            for (var k in states) {
                if (states[k].getId() == id) {
                    var state = states[k];
                    if (status == 'active') {
                        state.attr(myflow.config.activeRects.rectAttr);
                    } else if (status == 'finish') {
                        state.attr(myflow.config.finishRects.rectAttr);
                    } else if (status == 'return') {
                        state.attr(myflow.config.returnRects.rectAttr);
                    } else if (status == 'ignore') {
                        state.attr(myflow.config.ignoreRects.rectAttr);
                    } else if (status == 'discard') {
                        state.attr(myflow.config.discardRects.rectAttr);
                    }
                    break;
                }
            }
        }
    });
})(jQuery);