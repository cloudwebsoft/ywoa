//Simple Sidebar v1.0.3 by DcDeiv https://github.com/dcdeiv
// GPLv2 http://www.gnu.org/licenses/gpl-2.0-standalone.html
(function( $ ) {
	$.fn.simpleSidebar = function( options ) {
		//declaring all global variables
		var sbw, align, callbackA, callbackB,
			//allowing user customisation
			defaults  = {
				settings: {
					opener: undefined,
					wrapper: undefined, //HTML tag is not safe. Please, use a container/wrapper div
					ignore: undefined,
					data: 'ssbplugin',
					animation: {
						duration: 500, //milliseconds (0.5s = 500ms)
						easing: 'swing'
					}
				},
				sidebar: {
					align: undefined,
					width: 350, //pixels
					gap: 64, //pixels
					closingLinks: 'a',
					style: {
						zIndex: 3000
					}
				},
				mask: {
					style: {
						backgroundColor: 'black',
						opacity: 0.5,
						filter: 'Alpha(opacity=50)' //IE8 and earlier
					}
				}
			},
			config    = $.extend( true, defaults, options ),
			$sidebar  = this,
			$opener   = $( config.settings.opener ),
			$wrapper  = $( config.settings.wrapper ),
			$ignore   = $( config.settings.ignore ),
			dataName  = config.settings.data,
			duration  = config.settings.animation.duration,
			easing    = config.settings.animation.easing,
			defAlign  = config.sidebar.align,
			sbMaxW    = config.sidebar.width,
			gap       = config.sidebar.gap,
			$links    = config.sidebar.closingLinks,
			defStyle  = config.sidebar.style,
			maskDef   = config.mask.style,
			winMaxW   = sbMaxW + gap,
			//selecting all fixed elements except the sidebar and the ignore elements
			$fixedEl  = $( '*' )
				.not( $ignore )
				.not( $sidebar )
				.filter(function() {
					return $( this ).css( 'position' ) == 'fixed';
				}),
			$absolEl  = $( '*' )
				.not( $ignore )
				.filter(function() {
					return $( this ).css( 'position' ) == 'absolute';
				}),
			//selecting all elements.
			$elements = $fixedEl
				.add( $absolEl )
				.add( $sidebar )
				.add( $wrapper )
				.not( $ignore ),
			w         = $( window ).width(),
			MaskDef = {
				position: 'fixed',
				top: -200,
				right: -200,
				left: -200,
				bottom: -200,
				zIndex: config.sidebar.style.zIndex - 1
			},
			maskStyle = $.extend( {},  maskDef, MaskDef );
		
		//adding default style to $sidebar
		var subWrapperDiv = '<div data-' + dataName + '="sub-wrapper"></div>';
		$sidebar
			.css( defStyle )
			//wrapping inner content to let it overflow
			.wrapInner(subWrapperDiv);
		
		var subWrapper = $sidebar.children().filter(function() {
			return $( this ).data( dataName ) === 'sub-wrapper' ;
		});
		
		subWrapper.css({
			width: '100%',
			height: '100%',
			overflow: 'auto'
		});
			
		//Appending to 'body' the mask-div and adding its style
		$( 'body' ).append( '<div data-' + dataName + '="mask"></div>' );
		
		var maskDiv = $( 'body' ).children().filter(function(){
			return $( this ).data( dataName ) === 'mask' ;
		});
		
		maskDiv
			.css( maskStyle )
			.hide();

		var isAnimating = false;
		//Animate $elements to the right
		var animateToRight = function() {
			if (isAnimating)
				return;
			
			isAnimating = true;
			
			var nsbw = $sidebar.width();
			
			$elements.each(function() {
				$( this ).animate({
					marginLeft: '+=' + nsbw,
					marginRight: '-=' + nsbw
				}, {
					duration: duration,
					easing: easing,
					complete: callbackA
				});
			});			
		},
			//animate $elements to the left
			animateToLeft = function() {
				if (isAnimating)
					return;
			
				isAnimating = true;
			
				var nsbw = $sidebar.width();
				
				$elements.each(function() {
					$( this ).animate({
						marginLeft: '-=' + nsbw,
						marginRight: '+=' + nsbw
					}, {
						duration: duration,
						easing: easing,
						complete: callbackB
					});
				});
			},
			//hiding overflow [callback(A/B)]
			overflowTrue = function() {
				$( 'body, html' ).css({
					overflow: 'hidden'
				});
				
				$( maskDiv ).fadeIn();
				isAnimating = false;
			},
			//adding overflow [callback(A/B)]
			overflowFalse = function() {
				$( maskDiv ).fadeOut(function() {
					$( 'body, html' ).css({
						overflow: 'auto'
					});
				});
				
				isAnimating = false;
			};
		
		//assigning value to sbw
		if ( w < winMaxW ) {
			sbw = w - gap;
		} else {
			sbw = sbMaxW;
		}
		
		//testing config.sidebar.align
		if( defAlign === undefined || defAlign === 'left' ) {
			align = 'left';
		} else {
			align = 'right';
		}
		
		//Sidebar initial position
		if ( 'left' === align ) {
			$sidebar.css({
				position: 'fixed',
				top: 0,
				left: 0,
				bottom: 0,
				width: sbw,
				marginLeft: -sbw
			});
			
			callbackA = overflowTrue;
			callbackB = overflowFalse;
			
			$opener.click( animateToRight );
			
			maskDiv.click( animateToLeft );
			
			$sidebar.on( 'click', $links, animateToLeft );
		} else {
			$sidebar.css({
				position: 'fixed',
				top: 0,
				bottom: 0,
				right: 0,
				width: sbw,
				marginRight: -sbw
			});
			
			callbackA = overflowFalse;
			callbackB = overflowTrue;
			
			$opener.click( animateToLeft );
			
			maskDiv.click( animateToRight );
			
			$sidebar.on( 'click', $links, animateToRight );
		}
		
		//Adding responsive to $sidebar
		$( window ).resize(function() {
			var rsbw, sbMar,
				w = $( this ).width();
				
			if ( w < winMaxW ) {
				rsbw = w - gap;
			} else {
				rsbw = sbMaxW;
			}
			
			$sidebar.css({
				width: rsbw
			});
			
			//fixing $element position according to $sidebar new width (rsbw)
			if ( 'left' === align ) {
				sbMar = parseInt( $sidebar.css( 'margin-left' ) );
				
				if ( 0 > sbMar ) {
					$sidebar.css({
						marginLeft: -rsbw
					});
				} else {
					$elements.not( $sidebar )
						.css({
							marginLeft: + rsbw,
							marginRight: - rsbw
						});
				}
			} else {
				sbMar = parseInt( $sidebar.css( 'margin-right' ) );
				
				if ( 0 > sbMar ) {
					$sidebar.css({
						marginRight: -rsbw
					});
				} else {
					$elements.not( $sidebar )
						.css({
							marginLeft: - rsbw,
							marginRight: + rsbw
						});
				}
			}
		});
		
		return this;
	};
})( jQuery );
