<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>现场直播</title>
<script src="../../inc/common.js"></script>
<script src="../../js/jquery-1.9.1.min.js"></script>
<script src="../../js/jquery-migrate-1.2.1.min.js"></script>

<link type="text/css" rel="stylesheet" href="../../ueditor/js/ueditor/third-party/video-js/video-js.css"/>
<script language="javascript" type="text/javascript" src="../../ueditor/js/ueditor/third-party/video-js/video.js"></script>
<script language="javascript" type="text/javascript" src="../../ueditor/js/ueditor/third-party/video-js/html5media.min.js"></script>
</head>
<body>
<video
    id="my-player"
	class="edui-upload-video  vjs-default-skin  video-js"     
    controls=""
    width="420"
    height="280"
    preload="auto"
    poster="//vjs.zencdn.net/v/oceans.png"
    data-setup='{}'>
    <source src='rtmp://192.168.0.140:1935/live/12345' type='rtmp/flv'/>  
</video>
<script type="text/javascript">
   var player = videojs('my-player');
   var options = {};

   var player = videojs('my-player', options, function onPlayerReady() {
     videojs.log('Your player is ready!');
     // In this context, `this` is the player that was created by Video.js.
     this.play();
     // How about an event listener?
     this.on('ended', function() {
       videojs.log('Awww...over so soon?!');
     });
   });
</script>
</body>
</html>