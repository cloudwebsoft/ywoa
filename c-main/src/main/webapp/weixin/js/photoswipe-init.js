// 对document进行初始化
document.writeln("<!-- Root element of PhotoSwipe. Must have class pswp. -->");
document.writeln("<div class=\"pswp\" tabindex=\"-1\" role=\"dialog\" aria-hidden=\"true\">");
document.writeln("");
document.writeln("    <!-- Background of PhotoSwipe.");
document.writeln("         It\'s a separate element as animating opacity is faster than rgba(). -->");
document.writeln("    <div class=\"pswp__bg\"><\/div>");
document.writeln("");
document.writeln("    <!-- Slides wrapper with overflow:hidden. -->");
document.writeln("    <div class=\"pswp__scroll-wrap\">");
document.writeln("");
document.writeln("        <!-- Container that holds slides.");
document.writeln("            PhotoSwipe keeps only 3 of them in the DOM to save memory.");
document.writeln("            Don\'t modify these 3 pswp__item elements, data is added later on. -->");
document.writeln("        <div class=\"pswp__container\">");
document.writeln("            <div class=\"pswp__item\"><\/div>");
document.writeln("            <div class=\"pswp__item\"><\/div>");
document.writeln("            <div class=\"pswp__item\"><\/div>");
document.writeln("        <\/div>");
document.writeln("");
document.writeln("        <!-- Default (PhotoSwipeUI_Default) interface on top of sliding area. Can be changed. -->");
document.writeln("        <div class=\"pswp__ui pswp__ui--hidden\">");
document.writeln("");
document.writeln("            <div class=\"pswp__top-bar\">");
document.writeln("");
document.writeln("                <!--  Controls are self-explanatory. Order can be changed. -->");
document.writeln("");
document.writeln("                <div class=\"pswp__counter\"><\/div>");
document.writeln("");
document.writeln("                <button class=\"pswp__button pswp__button--close\" title=\"Close (Esc)\"><\/button>");
document.writeln("");
// wm modify delete the share button document.writeln("                <button class=\"pswp__button pswp__button--share\" title=\"Share\"><\/button>");
document.writeln("");
//wm modify delete the fullscreen button document.writeln("                <button class=\"pswp__button pswp__button--fs\" title=\"Toggle fullscreen\"><\/button>");
document.writeln("");
document.writeln("                <button class=\"pswp__button pswp__button--zoom\" title=\"Zoom in\/out\"><\/button>");
document.writeln("");
document.writeln("                <!-- Preloader demo http:\/\/codepen.io\/dimsemenov\/pen\/yyBWoR -->");
document.writeln("                <!-- element will get class pswp__preloader--active when preloader is running -->");
document.writeln("                <div class=\"pswp__preloader\">");
document.writeln("                    <div class=\"pswp__preloader__icn\">");
document.writeln("                        <div class=\"pswp__preloader__cut\">");
document.writeln("                            <div class=\"pswp__preloader__donut\"><\/div>");
document.writeln("                        <\/div>");
document.writeln("                    <\/div>");
document.writeln("                <\/div>");
document.writeln("            <\/div>");
document.writeln("");
document.writeln("            <div class=\"pswp__share-modal pswp__share-modal--hidden pswp__single-tap\">");
document.writeln("                <div class=\"pswp__share-tooltip\"><\/div>");
document.writeln("            <\/div>");
document.writeln("");
document.writeln("            <button class=\"pswp__button pswp__button--arrow--left\" title=\"Previous (arrow left)\">");
document.writeln("            <\/button>");
document.writeln("");
document.writeln("            <button class=\"pswp__button pswp__button--arrow--right\" title=\"Next (arrow right)\">");
document.writeln("            <\/button>");
document.writeln("");
document.writeln("            <div class=\"pswp__caption\">");
document.writeln("                <div class=\"pswp__caption__center\"><\/div>");
document.writeln("            <\/div>");
document.writeln("");
document.writeln("        <\/div>");
document.writeln("");
document.writeln("    <\/div>");
document.writeln("");
document.writeln("<\/div>");
// 对js初始化
(function() {
        var initPhotoSwipeFromDOM = function(gallerySelector) {
            var parseThumbnailElements = function(el) {
                var thumbElements = el.childNodes,
                        numNodes = thumbElements.length,
                        items = [],
                        el,
                        childElements,
                        thumbnailEl,
                        size,
                        item;
                for(var i = 0; i < numNodes; i++) {
                    el = thumbElements[i];
                    if(el.nodeType !== 1) {
                        continue;
                    }
                    childElements = el.children;
                    size = el.getAttribute('data-size').split('x');
                    item = {
                        src: el.getAttribute('href'),
                        w: parseInt(size[0], 10),
                        h: parseInt(size[1], 10),
                        author: el.getAttribute('data-author')
                    };
                    item.el = el; // save link to element for getThumbBoundsFn
                    if(childElements.length > 0) {
                        item.msrc = childElements[0].getAttribute('src'); // thumbnail url
                        if(childElements.length > 1) {
                            item.title = childElements[1].innerHTML; // caption (contents of figure)
                        }
                    }
                    var mediumSrc = el.getAttribute('data-med');
                    if(mediumSrc) {
                        size = el.getAttribute('data-med-size').split('x');
                        // "medium-sized" image
                        item.m = {
                            src: mediumSrc,
                            w: parseInt(size[0], 10),
                            h: parseInt(size[1], 10)
                        };
                    }
                    item.o = {
                        src: item.src,
                        w: item.w,
                        h: item.h
                    };
                    items.push(item);
                }
                return items;
            };
            var closest = function closest(el, fn) {
                return el && ( fn(el) ? el : closest(el.parentNode, fn) );
            };
            var onThumbnailsClick = function(e) {
                e = e || window.event;
                e.preventDefault ? e.preventDefault() : e.returnValue = false;
                var eTarget = e.target || e.srcElement;
                var clickedListItem = closest(eTarget, function(el) {
                    return el.tagName === 'A';
                });
                if(!clickedListItem) {
                    return;
                }
                var clickedGallery = clickedListItem.parentNode;
                var childNodes = clickedListItem.parentNode.childNodes,
                        numChildNodes = childNodes.length,
                        nodeIndex = 0,
                        index;
                for (var i = 0; i < numChildNodes; i++) {
                    if(childNodes[i].nodeType !== 1) {
                        continue;
                    }
                    if(childNodes[i] === clickedListItem) {
                        index = nodeIndex;
                        break;
                    }
                    nodeIndex++;
                }
                if(index >= 0) {
                    openPhotoSwipe( index, clickedGallery );
                }
                return false;
            };
            var photoswipeParseHash = function() {
                var hash = window.location.hash.substring(1),
                        params = {};
                if(hash.length < 5) { // pid=1
                    return params;
                }
                var vars = hash.split('&');
                for (var i = 0; i < vars.length; i++) {
                    if(!vars[i]) {
                        continue;
                    }
                    var pair = vars[i].split('=');
                    if(pair.length < 2) {
                        continue;
                    }
                    params[pair[0]] = pair[1];
                }
                if(params.gid) {
                    params.gid = parseInt(params.gid, 10);
                }
                return params;
            };
            var openPhotoSwipe = function(index, galleryElement, disableAnimation, fromURL) {
                var pswpElement = document.querySelectorAll('.pswp')[0],
                        gallery,
                        options,
                        items;
                items = parseThumbnailElements(galleryElement);
                // define options (if needed)
                options = {
                    galleryUID: galleryElement.getAttribute('data-pswp-uid'),
                    getThumbBoundsFn: function(index) {
                        // See Options->getThumbBoundsFn section of docs for more info
                        var thumbnail = items[index].el.children[0],
                                pageYScroll = window.pageYOffset || document.documentElement.scrollTop,
                                rect = thumbnail.getBoundingClientRect();
                        return {x:rect.left, y:rect.top + pageYScroll, w:rect.width};
                    },
                    addCaptionHTMLFn: function(item, captionEl, isFake) {
                        if(!item.title) {
                            captionEl.children[0].innerText = '';
                            return false;
                        }
                        captionEl.children[0].innerHTML = item.title +  '<br/><small>Photo: ' + item.author + '</small>';
                        return true;
                    }
                };
                options.shareEl = false;
                options.fullscreenEl = false;
                if(fromURL) {
                    if(options.galleryPIDs) {
                        for(var j = 0; j < items.length; j++) {
                            if(items[j].pid == index) {
                                options.index = j;
                                break;
                            }
                        }
                    } else {
                        options.index = parseInt(index, 10) - 1;
                    }
                } else {
                    options.index = parseInt(index, 10);
                }
                // exit if index not found
                if( isNaN(options.index) ) {
                    return;
                }
                // Pass data to PhotoSwipe and initialize it
                gallery = new PhotoSwipe( pswpElement, PhotoSwipeUI_Default, items, options);
                // see: http://photoswipe.com/documentation/responsive-images.html
                var realViewportWidth,
                        useLargeImages = false,
                        firstResize = true,
                        imageSrcWillChange;
                gallery.listen('beforeResize', function() {
                    var dpiRatio = window.devicePixelRatio ? window.devicePixelRatio : 1;
                    dpiRatio = Math.min(dpiRatio, 2.5);
                    realViewportWidth = gallery.viewportSize.x * dpiRatio;
                    if(realViewportWidth >= 1200 || (!gallery.likelyTouchDevice && realViewportWidth > 800) || screen.width > 1200 ) {
                        if(!useLargeImages) {
                            useLargeImages = true;
                            imageSrcWillChange = true;
                        }
                    } else {
                        if(useLargeImages) {
                            useLargeImages = false;
                            imageSrcWillChange = true;
                        }
                    }
                    if(imageSrcWillChange && !firstResize) {
                        gallery.invalidateCurrItems();
                    }

                    if(firstResize) {
                        firstResize = false;
                    }
                    imageSrcWillChange = false;

                });

                gallery.listen('gettingData', function(index, item) {
                    if( useLargeImages ) {
                        item.src = item.o.src;
                        item.w = item.o.w;
                        item.h = item.o.h;
                    } else {
                        item.src = item.m.src;
                        item.w = item.m.w;
                        item.h = item.m.h;
                    }
                });

                gallery.init();
            };
            // select all gallery elements
            var galleryElements = document.querySelectorAll( gallerySelector );
            for(var i = 0, l = galleryElements.length; i < l; i++) {
                galleryElements[i].setAttribute('data-pswp-uid', i+1);
                galleryElements[i].onclick = onThumbnailsClick;
            }
            // Parse URL and open gallery if it contains #&pid=3&gid=1
            var hashData = photoswipeParseHash();
            if(hashData.pid && hashData.gid) {
                openPhotoSwipe( hashData.pid,  galleryElements[ hashData.gid - 1 ], true, true );
            }
        };
        initPhotoSwipeFromDOM('.demo-gallery');

    })();