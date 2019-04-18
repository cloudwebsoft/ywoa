(function( $ ){
  var methods = {
    init: function(options) {
      var settings = {
        color: $(this).css("background-color"),
        reach: 20,
        speed: 1000,
        pause: 0,
        glow: true,
        repeat: true,
        onHover: false
      };
      $(this).css({
        "-moz-outline-radius": $(this).css("border-top-left-radius"),
        "-webkit-outline-radius": $(this).css("border-top-left-radius"),
        "outline-radius": $(this).css("border-top-left-radius")
      });

      if (options) {
        $.extend(settings, options);
      }
      settings.color = $("<div style='background:" + settings.color + "'></div>").css("background-color");
      if(settings.repeat !== true && !isNaN(settings.repeat) && settings.repeat > 0) {
        settings.repeat -=1;
      }

      return this.each(function() {
        if(settings.onHover) {
          $(this).bind("mouseover", function () {pulse(settings, this, 0);})
                 .bind("mouseout", function (){$(this).pulsate("destroy");});
        } else {
          pulse(settings, this, 0);
        }
      });
    },
    destroy: function() {
      return this.each(function() {
        clearTimeout(this.timer);
        $(this).css("outline",0);
      });
    }
  };

  var pulse = function(options, el, count) {
    var reach = options.reach,
        count = count>reach ? 0 : count,
        opacity = (reach-count)/reach,
        colorarr = options.color.split(","),
        color = "rgba(" + colorarr[0].split("(")[1] + "," + colorarr[1] + "," + colorarr[2].split(")")[0] + "," + opacity + ")",
        cssObj = {
          "outline": "2px solid " + color
        };
    if(options.glow) {
      cssObj["box-shadow"] = "0px 0px " + parseInt((count/1.5)) + "px " + color;
      userAgent = navigator.userAgent || '';
      if(/(chrome)[ \/]([\w.]+)/.test(userAgent.toLowerCase())) {
        cssObj["outline-offset"] = count + "px";
        cssObj["outline-radius"] = "100 px";
      }
    } else {
      cssObj["outline-offset"] = count + "px";
    }
    $(el).css(cssObj);

    var innerfunc = function () {
      if(count>=reach && !options.repeat) {
        $(el).pulsate("destroy");
        return false;
      } else if(count>=reach && options.repeat !== true && !isNaN(options.repeat) && options.repeat > 0) {
        options.repeat = options.repeat-1;
      } else if(options.pause && count>=reach) {
        pause(options, el, count+1);
        return false;
      }
      pulse(options, el, count+1);
    };
    
    if(el.timer){
      clearTimeout(el.timer);
    }
    el.timer = setTimeout(innerfunc, options.speed/reach);
  };

  var pause = function (options, el, count) {
    innerfunc = function () {
      pulse(options, el, count);
    };
    el.timer = setTimeout(innerfunc, options.pause);
  };

  $.fn.pulsate = function( method ) {
    // Method calling logic
    if ( methods[method] ) {
      return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
    } else if ( typeof method === 'object' || ! method ) {
      return methods.init.apply( this, arguments );
    } else {
      $.error( 'Method ' +  method + ' does not exist on jQuery.pulsate' );
    }

  };
})( jQuery );

