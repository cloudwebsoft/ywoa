$(function () {
    // var flowdata = window.localStorage.getItem("data");
    var flowdata = '{"paths":{"path1358380964":{"dots":[],"lineID":"path1358380964","from":"rect90d7e14a37f94fd6874d2852a945a160","to":"rect13033d55c2fa4087a5084d9405b3b55a","text":{"textPos":{"x":0,"y":-10},"text":""},"type":0,"props":{"text":{"value":""}}}},"states":{"rect90d7e14a37f94fd6874d2852a945a160":{"ID":"rect90d7e14a37f94fd6874d2852a945a160","text":{"text":"本人:发起"},"type":"start","attr":{"x":20,"width":100,"y":216,"height":50}},"rect13033d55c2fa4087a5084d9405b3b55a":{"ID":"rect13033d55c2fa4087a5084d9405b3b55a","text":{"text":"总经理:审批"},"type":"task","attr":{"x":168,"width":100,"y":216,"height":50}}}}';
    // var flowdata = '{"paths":{"path189471796":{"dots":[{"x":454,"y":319},{"x":454,"y":383}],"lineID":"path189471796","from":"rect1b17aa8cb43749bc9f7076796b64656f","to":"recta2d23d3c28564c2ca967e799aef3dd56","text":{"textPos":{"x":0,"y":-10},"text":""},"type":0,"props":{"text":{"value":""}}},"path853486140":{"dots":[{"x":454,"y":447},{"x":454,"y":383}],"lineID":"path853486140","from":"rect298b6a816a8f405baa41a6290bee2fab","to":"recta2d23d3c28564c2ca967e799aef3dd56","text":{"textPos":{"x":0,"y":-10},"text":""},"type":0,"props":{"text":{"value":""}}},"path419259529":{"dots":[],"lineID":"path419259529","from":"rect9191be9dc264435383ae71e33d342bac","to":"rect6664cccc64c941d78cfb4d91d682bea2","text":{"textPos":{"x":0,"y":-10},"text":""},"type":2,"props":{"text":{"value":""}}},"path201109990":{"dots":[{"x":530,"y":264},{"x":74,"y":264}],"lineID":"path201109990","from":"recta2d23d3c28564c2ca967e799aef3dd56","to":"rect9191be9dc264435383ae71e33d342bac","text":{"textPos":{"x":0,"y":-10},"text":""},"type":1,"props":{"text":{"value":""}}},"path1840027190":{"dots":[{"x":302,"y":383},{"x":302,"y":447}],"lineID":"path1840027190","from":"rect6664cccc64c941d78cfb4d91d682bea2","to":"rect298b6a816a8f405baa41a6290bee2fab","text":{"textPos":{"x":0,"y":-10},"text":""},"type":0,"props":{"text":{"value":""}}},"path1000893589":{"dots":[{"x":302,"y":383},{"x":302,"y":319}],"lineID":"path1000893589","from":"rect6664cccc64c941d78cfb4d91d682bea2","to":"rect1b17aa8cb43749bc9f7076796b64656f","text":{"textPos":{"x":0,"y":-10},"text":""},"type":0,"props":{"text":{"value":""}}}},"states":{"recta2d23d3c28564c2ca967e799aef3dd56":{"ID":"recta2d23d3c28564c2ca967e799aef3dd56","text":{"text":"总经理:审批"},"type":"end","attr":{"x":480,"width":100,"y":360,"height":50}},"rect298b6a816a8f405baa41a6290bee2fab":{"ID":"rect298b6a816a8f405baa41a6290bee2fab","text":{"text":"人事总监:审批"},"type":"task","attr":{"x":328,"width":100,"y":424,"height":50}},"rect1b17aa8cb43749bc9f7076796b64656f":{"ID":"rect1b17aa8cb43749bc9f7076796b64656f","text":{"text":"财务总监:审批"},"type":"task","attr":{"x":328,"width":100,"y":296,"height":50}},"rect6664cccc64c941d78cfb4d91d682bea2":{"ID":"rect6664cccc64c941d78cfb4d91d682bea2","text":{"text":"行政总监:审批"},"type":"task","attr":{"x":176,"width":100,"y":360,"height":50}},"rect9191be9dc264435383ae71e33d342bac":{"ID":"rect9191be9dc264435383ae71e33d342bac","text":{"text":"本人:发起"},"type":"start","attr":{"x":24,"width":100,"y":360,"height":50}}}}';
    $('#myflow').myflow({
        basePath: "",
        allowStateMultiLine: false,
        editable: true,
        // restore: eval("(" + flowdata + ")"),
        activeRects: {"rects": [{"paths": [], "name": "总经理:审批"}]},
        finishRects: {"rects": [{"paths": [], "name": "本人:发起"}]},
        tools: {
            save: function (data) {
                console.log("保存", data);
                // alert(data);
                // console.log(data);
                // window.localStorage.setItem("data", data)
            },
            /*publish: function (data) {
                console.log("发布", eval("(" + data + ")"));
            },*/
            addPath: function (id, data) {
                console.log("添加路径", id, eval("(" + data + ")"));
            },
            addRect: function (id, data) {
                console.log("添加状态", id, eval("(" + data + ")"));
            },
            clickPath: function (id, data) {
                console.log("点击线", id, eval("(" + data + ")"));
            },
            clickRect: function (id, data) {
                console.log("点击状态", id, eval("(" + data + ")"));
                OpenModifyWin(eval("(" + data + ")"));
            },
            deletePath: function (id) {
                console.log("删除线", id);
            },
            deleteRect: function (id, data) {
                console.log("删除状态", id, eval("(" + data + ")"));
            },
            revoke: function (id) {
                console.log("撤销", id);
            }
        }
    });
});