/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab filetype=css: */
/*! 
 * Base CSS for pdf2htmlEX
 * Copyright 2012,2013 Lu Wang <coolwanglu@gmail.com> 
 * https://github.com/coolwanglu/pdf2htmlEX/blob/master/share/LICENSE
 */
/* Part 1: Web Page Layout: Free to modify, except for a few of them which are required by pdf2htmlEX.js, see the comments */
#sidebar { /* Sidebar */
  position:absolute;
  top:0;
  left:0;
  bottom:0;
  width:250px;
  padding:0;
  margin:0px;
  overflow:auto;
}
#page-container { /* PDF container */
  position:absolute; /* required for calculating relative positions of pages in pdf2htmlEX.js */
  top:0;
  left:0px;
  margin:0; 
  padding:0;
  border:0; /* required for lazy page loading in pdf2htmlEX.js (page visibility test) */
}
@media screen {
  /* for sidebar */
  #sidebar.opened + #page-container { left:250px; }
  #page-container {
    /* `bottom' and `right' are required for lazy page loading in pdf2htmlEX.js (page visibility test)
     * alternatively you may set width and height
     */
    bottom:0;
    right:0;
    overflow:auto;
  }
  .loading-indicator {
    display:none;
  }
  .loading-indicator.active {
    display:block;
    position:absolute;
    width:64px;
    height:64px;
    top:50%;
    left:50%;
    margin-top:-32px;
    margin-left:-32px;
  }
  .loading-indicator img {
    position:absolute;
    top:0;
    left:0;
    bottom:0;
    right:0;
  }
}
@media print { 
  @page { margin:0; }
  html { margin:0; }
  body { 
    margin:0; 
    -webkit-print-color-adjust:exact; /* enable printing background images for WebKit */
  }
  #sidebar { display:none; }
  #page-container {
    width:auto;
    height:auto;
    overflow:visible;
    background-color:transparent;
  }
  .d { display:none; }
}
/* Part 2: Page Elements: Modify with caution
 * The followings are base classes, some of which are meant to be override by PDF specific classes
 * So do not increase the specificity (e.g. ".classname" -> "#page-container .classname")
 */
.pf { /* page */
  position:relative;
  background-color:white;
  overflow: hidden;
  margin:0; 
  border:0; /* required by pdf2htmlEX.js for page visibility test */
}
.pc { /* content of a page */
  position:absolute;
  border:0;
  padding:0;
  margin:0;
  top:0;
  left:0;
  width:100%;
  height:100%;
  overflow:hidden;
  display:block;
  /* set transform-origin for scaling */
  transform-origin:0% 0%;
  -ms-transform-origin:0% 0%;
  -webkit-transform-origin:0% 0%;
}
.pc.opened { /* used by pdf2htmlEX.js, to show/hide pages */
  display:block;
}
.bf { /* images that occupies the whole page */
  position:absolute;
  border:0;
  margin:0;
  top:0;
  bottom:0;
  width:100%;
  height:100%;
  -ms-user-select:none;
  -moz-user-select:none;
  -webkit-user-select:none;
  user-select:none;
}
.bi { /* images that cover only a part of the page */
  position:absolute;
  border:0;
  margin:0;
  -ms-user-select:none;
  -moz-user-select:none;
  -webkit-user-select:none;
  user-select:none;
}
@media print {
  .pf {
    margin:0;
    box-shadow:none;
    page-break-after:always;
    page-break-inside:avoid;
  }
  @-moz-document url-prefix() {
    /* fix page truncation for FireFox */
    .pf {
      overflow:visible;
      border:1px solid #FFFFFF;
    }
    .pc {overflow:visible;}
  }
}
.c { /* clip box */
  position:absolute;
  border:0;
  padding:0;
  margin:0;
  overflow:hidden;
  display:block;
}
.t { /* text line */
  position:absolute;
  white-space:pre;
  font-size:1px;
  transform-origin:0% 100%;
  -ms-transform-origin:0% 100%;
  -webkit-transform-origin:0% 100%;
  unicode-bidi:bidi-override;/* For rtl languages, e.g. Hebrew, we don't want the default Unicode behaviour */
  -moz-font-feature-settings:"liga" 0;/* We don't want Firefox to recognize ligatures */
}
.t:after { /* webkit #35443 */
  content: '';
}
.t:before { /* Workaround Blink(up to 41)/Webkit bug of word-spacing with leading spaces (chromium #404444 and pdf2htmlEX #412) */
  content: '';
  display: inline-block;
}
.t span { /* text blocks within a line */
  /* Blink(up to 41)/Webkit have bug with negative word-spacing and inline-block (pdf2htmlEX #416), so keep normal span inline. */
  position:relative;
  unicode-bidi:bidi-override; /* For rtl languages, e.g. Hebrew, we don't want the default Unicode behaviour */
}
._ { /* text shift */
  /* Blink(up to 41)/Webkit have bug with inline element, continuous spaces and word-spacing. Workaround by inline-block. */
  display: inline-block;
  color: transparent;
  z-index: -1;
}
/* selection background should not be opaque, for fallback mode */
::selection{
  background: rgba(127,255,255,0.4);
}
::-moz-selection{
  background: rgba(127,255,255,0.4);
}
.pi { /* info for Javascript */
  display:none;
}
.l { /* annotation links */
}
/* transparent color - WebKit */
.d { /* css drawing */
  position:absolute;
  transform-origin:0% 100%;
  -ms-transform-origin:0% 100%;
  -webkit-transform-origin:0% 100%;
}
/* for the forms */
.it {
  border: none;
  background-color: rgba(255, 255, 255, 0.0);
}

.ir:hover {
  cursor: pointer;
}

/* Base CSS END */
