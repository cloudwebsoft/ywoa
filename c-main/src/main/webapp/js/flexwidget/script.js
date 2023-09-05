
	// TODO:
	// - resize axis
	// - redo save widget functionality to fit plugin system
	// - fix placeholder position, currently follows mouse rather than widget
	// - optional size / position values passed with widgets from widget holder
	// - add nested functionality
	// - - optional height passed with nested widget to push other widgets down
	// - determine if widget can be dropped in pos before sortable stop

	$.fn.setFlexGrid = function(options) {
		var defaults = { // default options for grid
			cols: 12, // amount of columns across a zone
			rows: 12, // amount of rows in a zone
			defaultHeight: 3, // default amount of rows a widget will span upon creation
			defaultWidth: 3, // default amount of columns a widget will span upon creation
			minHeight: 1, // minimum number of rows a widget can span ~ should be <= defaultHeight
			maxHeight: null, // if set to numeric value: maximum number of rows a widget can span
			minWidth: 1, // minimum number of columns a widget can span ~ should be <= defaultWidth
			maxWidth: null, // if set to numeric value: maximum number of columns a widget can span
			rowHeight: 1, // value times column width
			nested: true, // if true, widgets will allow nested widget to be dropped into them
			showGridlines: true, // if true, show gridlines on initialization
			animate: true, // determines wether or not the zones should be animated
			nextAxis: 'x', // determines on which axis the widget should find an open column. When X: widget will go to the first open column. When Y: widget will go to the first row that has an open column of x.
			resize: 'both' // determines which axis to resize on ~ default is "both".
		};
		options = $.extend(defaults, options);

		var zoneInner = $(this);
		// var zoneInner = zone.find('.flexgrid-grid');

		$.fn.minWidth = function(minWidth) { // get the min-width value of something
			var item = $(this);
			var minWidth = parseInt(item.css('min-width'));
			return minWidth;
		}
		$.fn.minHeight = function(minHeight) { // get the min-height value of something
			var item = $(this);
			var minHeight = parseInt(item.css('min-height'));
			return minHeight;
		}
		$.fn.maxWidth = function(maxWidth) { // get the min-width value of something
			var item = $(this);
			var maxWidth = parseInt(item.css('max-width'));
			return maxWidth;
		}
		$.fn.maxHeight = function(maxHeight) { // get the min-height value of something
			var item = $(this);
			var maxHeight = parseInt(item.css('max-height'));
			return maxHeight;
		}

		// setup variables to be used in calculations in multiple functions
		$.fn.resetVars = function() {
			// reset variables since we perfected the zone-inner width / height
			var zoneInner = $(this); // we will need to reintialize this variable in every function where the following variables are used
			var zone = zoneInner.closest('.flexgrid-container');
			var zoneWidth = zoneInner.outerWidth();
			var zoneHeight = zoneInner.outerHeight();
			var colWidth = Math.floor(zoneWidth / options.cols); // width of each column
			var rowHeight = options.rowHeight > 1 ? options.rowHeight * colWidth : colWidth; // height of each row

			var res = { zone: zone, zW: zoneWidth, zH: zoneHeight, cW: colWidth, rH: rowHeight };
			return res;
		}
		var re = zoneInner.resetVars(); // reset zone variables
		
		// CREATE COLS
		$.fn.buildGrid = function() { // create rows of columns
			var zoneInner = $(this);
			var re = zoneInner.resetVars(); // reset zone variables
			var colAmount = options.cols; // number of columns in each row
			var rowAmount = options.rows; // number of rows in a zone
			var gridlines = options.showGridlines ? 'fg-gridlines' : '';

			for (var y = 0; y < rowAmount; y++) {
				for (var x = 0; x < colAmount; x++) {
					zoneInner.append('<div class="fg-enabled-col fg-col '+gridlines+'" data-fg-eq="'+x+'" data-fg-row="'+y+'" style="min-width: '+ re.cW +'px; min-height: '+ re.rH +'px; top:'+(re.rH * y)+'px; left: '+(re.cW * x)+'px; "></div>');
					var appended = zoneInner.find('.fg-col[data-fg-row="'+y+'"][data-fg-eq="'+x+'"]');
					var i = zoneInner.find('.fg-col').index(appended);
					appended.attr('data-fg-index', i);
				}
			}
			var lastCol = zoneInner.find('.fg-col').last();
			var rowCount = parseInt(lastCol.attr('data-fg-row'));
			zoneInner.css({ // reset the zone-inner width / height to perfect it.
				'height': (rowCount + 1) * re.rH,
				'width': (re.cW * options.cols)
			});
			re.zone.css({ // reset the zone width / height to perfect it.
				'height': (rowCount + 1) * re.rH + 55, // add some pixels to allow space for the zone-helper
				'width': (re.cW * options.cols) + 15 // add a little bezzel
			});
			enableSortable();
		}

		var re = zoneInner.resetVars(); // reset zone variables

		// set data attributes to find position of widget
		$.fn.setData = function() {
			var widget = $(this);
			var zoneInner = widget.closest('.flexgrid-grid');
			var zoneCol = widget.closest('.fg-col');
			var wigW = Math.floor(widget.width()); // widget width
			var wigH = Math.floor(widget.height()); // widget height

			var re = zoneInner.resetVars(); // reset zone variables
			
			var data_minWidth = parseFloat(Math.round(widget.minWidth() / re.cW));
			var data_minHeight = parseFloat(Math.round(widget.minHeight() / re.rH));
			var data_width = parseFloat(Math.round(wigW / re.cW)) < data_minWidth ? data_minWidth : parseFloat(Math.round(wigW / re.cW)); // number of columns a widget spans
			var data_height = parseFloat(Math.round(wigH / re.rH)) < data_minHeight ? data_minHeight : parseFloat(Math.round(wigH / re.rH)); // number of rows a widget spans
			var data_maxWidth = parseFloat(Math.round(widget.maxWidth() / re.cW));
			var data_maxHeight = parseFloat(Math.round(widget.maxHeight() / re.rH));
			var data_y = zoneCol.attr('data-fg-row'); // row the widget starts on ~ 0 indexed
			var data_x = zoneInner.find('.fg-col[data-fg-row="'+data_y+'"]').index(widget.closest('.fg-col[data-fg-row="'+data_y+'"]')); // column # the widget starts on ~ 0 indexed
			widget.attr({ 'data-fg-width': data_width, 'data-fg-height': data_height, 'data-fg-x': data_x, 'data-fg-y': data_y, 'data-fg-minwidth': data_minWidth, 'data-fg-minheight': data_minHeight, 'data-fg-maxwidth': data_maxWidth, 'data-fg-maxheight': data_maxHeight }); // set these new attributes
		}
		
		$.fn.setOption = function(option, val) {
			var widget = $(this);
			var grid = widget.closest('.flexgrid-grid');
			var re = grid.resetVars(); // reset zone variables
			
			var toggle;
			switch(option) {
				case 'height':
					toggle = 'data-fg-height';
					widget.css('height', val * re.rH);
					break;
				case 'width':
					toggle = 'data-fg-width';
					widget.css('width', val * re.cW);
					break;
				case 'minHeight':
					toggle = 'data-fg-minheight';
					widget.css('min-height', val * re.rH);
					break;
				case 'minWidth':
					toggle = 'data-fg-minwidth';
					widget.css('min-width', val * re.cW);
					break;
				case 'maxHeight':
					toggle = 'data-fg-maxheight';
					widget.css('max-height', val * re.rH);
					break;
				case 'maxWidth':
					toggle = 'data-fg-maxwidth';
					widget.css('max-width', val * re.cW);
					break;
				case 'x':
					toggle = 'data-fg-x';
					var y = widget.attr('data-fg-y');
					var col = grid.find('.fg-col[data-fg-eq="'+val+'"][data-fg-row="'+y+'"]');
					widget.detach();
					col.append(widget);
					break;
				case 'y':
					toggle = 'data-fg-y';
					var x = widget.attr('data-fg-x');
					var col = grid.find('.fg-col[data-fg-eq="'+x+'"][data-fg-row="'+val+'"]');
					widget.detach();
					col.append(widget);
					break;
			}
			widget.attr(toggle, val);
			widget.setData();
		}

		// enable or disable columns depending on the function parameter...
		$.fn.modCols = function(modifier) {
			var zoneCol = $(this);
			var zoneInner = zoneCol.closest('.flexgrid-grid');
			var widget = zoneCol.find('.fg-widget');
			if (modifier == 'disable') widget.setData();

			var rowStart = parseInt(zoneCol.attr('data-fg-row')); // row the widget starts on
			var rowEnd = rowStart + parseInt(widget.attr('data-fg-height')); // row the widget ends on

			var colStart = parseInt(zoneCol.attr('data-fg-eq')); // col the widget starts on
			var colEnd = colStart + parseInt(widget.attr('data-fg-width')); // col the widget ends on

			for (var r = rowStart; r < rowEnd; r++) {
				for (var c = colStart; c < colEnd; c++) {
					var self = zoneInner.find('.fg-col[data-fg-row="'+r+'"][data-fg-eq="'+c+'"]');
					if (modifier == 'enable') { self.addClass('fg-enabled-col').removeClass('fg-disabled-col'); }
					if (modifier == 'disable') { self.removeClass('fg-enabled-col').addClass('fg-disabled-col'); }
				}
			}
		}

		$.fn.zoneOverflow = function() {
			var col = $(this);
			var widget = col.find('.fg-widget');
			var zoneInner = col.closest('.flexgrid-grid');
			var ogParent = widget.data('ogParent'); // original parent column
			var re = zoneInner.resetVars(); // reset zone variables
			var res = {obj: col};

			widget.setData();
			var data_x = parseInt(widget.attr('data-fg-x'));
			var data_y = parseInt(widget.attr('data-fg-y'));
			var data_width = parseInt(widget.attr('data-fg-width'));
			var data_height = parseInt(widget.attr('data-fg-height'));
			var xCon = data_width + data_x > options.cols;
			var yCon = parseInt(col.attr('data-fg-row')) + data_height > parseInt(zoneInner.find('.fg-col').last().attr('data-fg-row')) + 1;
			var dif = ((data_width + data_x) - options.cols) * re.cW;

			if ($(ogParent).length > 0) { // if we dropped widget we should have an 'ogParent'
				if ((options.cols - data_x) * re.cW < widget.minWidth()) {
					var de = col.find('.fg-widget').detach();
					$(ogParent).append(de);
					res = {obj: $(ogParent)};
				} else if (xCon) { // if widget is overflowing zone width but is not at min-width
					widget.css('width', widget.width() - dif);
				}
				res = {obj: col};
			} else if ($(ogParent).length <= 0) { // if we added new widget
				var goHere = col;
				while (xCon) { // detach the widget and append to the next open fg-enabled-col until it no longer overflows the zone width
					var de = goHere.find('.fg-widget').detach();
					goHere = goHere.nextAll('.fg-enabled-col').first();
					if (goHere.length == 0 || goHere == undefined || goHere == null) {
						zoneInner.createRow();
						goHere = zoneInner.find('.fg-widget').last().closest('.fg-col').nextAll('.fg-enabled-col').first();
					}
					goHere.append(de);
					de.setData(); // reset widget attributes

					// reset position parameters to be used in next if statement / loop ...
					data_x = parseInt(widget.attr('data-fg-x'));
					data_y = parseInt(widget.attr('data-fg-y'));
					data_width = parseInt(widget.attr('data-fg-width'));
					data_height = parseInt(widget.attr('data-fg-height'));
					yCon = yCon = parseInt(goHere.attr('data-fg-row')) + data_height > parseInt(zoneInner.find('.fg-col').last().attr('data-fg-row')) + 1;
					xCon = data_width + data_x > options.cols;

					if (!xCon) {
						break;
					}
				}
				col = goHere; // reset col so it can be used in the next if statment...
				res = {obj: col};
			}
			if (yCon) { // if widget is overflowing zone height but is not at min-height
				moreHeight(col); // add more rows...
			}
			
			data_x = parseInt(col.attr('data-fg-eq'));
			data_y = parseInt(col.attr('data-fg-row'));
			
			// tried to only target siblings that shared columns, but was much slower....
			var sibs = col.siblings('.fg-col');
			for (var a = 0; a < sibs.length; a++) {
				var colSib = sibs.eq(a);
				if (colSib.find('.fg-widget').length > 0) {
					var dc = dropCollision(colSib, col);
					col = dc.obj;
					sibs = col.siblings('.fg-col');
				}
			}
			res = {obj: col};
			
			return res;
		}

		// pass in parameters such as: x, y, width, height to position and size the widget
		$.fn.addWidget = function(params) {
			var zoneInner = $(this);
			var re = zoneInner.resetVars(); // reset zone variables
			var goHere;
			var width, height, minWidth, minHeight, maxWidth, maxHeight, nextAxis;

			// if no parameters are passed, use the default value. else check if the parameter has a value, if so use that value, else use default value
			var noParams = params == undefined || params == null ? true : false;
			width = noParams ? options.defaultWidth * re.cW : (params.width != undefined || params.width != null) ? params.width * re.cW : options.defaultWidth * re.cW;
			height = noParams ? options.defaultHeight * re.rH : (params.height != undefined || params.height != null) ? params.height * re.rH : options.defaultHeight * re.rH;
			minWidth = noParams ? options.minWidth * re.cW : (params.minWidth != undefined || params.minWidth != null) ? params.minWidth * re.cW : options.minWidth * re.cW;
			minHeight = noParams ? options.minHeight * re.rH : (params.minHeight != undefined || params.minHeight != null) ? params.minHeight * re.rH : options.minHeight * re.rH;
			maxWidth = noParams ? options.maxWidth * re.cW : (params.maxWidth != undefined || params.maxWidth != null) ? params.maxWidth * re.cW : options.maxWidth * re.cW;
			maxWidth = (maxWidth == 0) ? 'unset' : maxWidth;
			maxHeight = noParams ? options.maxHeight * re.rH : (params.maxHeight != undefined || params.maxHeight != null) ? params.maxHeight * re.rH : options.maxHeight * re.rH;
			maxHeight = (maxHeight == 0) ? 'unset' : maxHeight;
			nextAxis = noParams ? options.nextAxis : (params.nextAxis != undefined || params.nextAxis != null) ? params.nextAxis : options.nextAxis;

			var amountNeeded = ((options.rowHeight * re.rH) * options.defaultHeight) / re.rH;
			amountNeeded = options.rowHeight > 1 ?  amountNeeded : options.defaultHeight;
			if ( zoneInner.find('.fg-enabled-col').length < 1 ) {
				for (l = 0; l < amountNeeded; l++) {
					zoneInner.createRow();
				}
			}
			
			// if we aren't given parameters or positions, then go to the first open column.
			if (noParams || (params.y === undefined || params.y === null || params.y === '') || (params.x === undefined || params.x === null || params.x === '')) {
				var findNext = findNextColumn('x', zoneInner, params.x, params.y);
				goHere = findNext.goHere;
			} else {
				var findNext = findNextColumn(nextAxis, zoneInner, params.x, params.y);
				goHere = findNext.goHere;
			}
			
			if (goHere.length == 0) console.error('Parent column does not exist.');

			// var nested = options.nested == true ? $('<div class="fg-nested-container"></div>') : $('');
			var widget = noParams ? $('<div class="fg-widget"><i class="fa fa-chevron-right fg-resize-widget" aria-hidden="true"></i><i class="fa fa-times fg-remove-widget" title="remove this widget."></i><div class="fg-widget-inner fg-widget-handle"><div class="zone-txt text-center"></div></div></div>') : params.widget === undefined || params.widget === null ? $('<div class="fg-widget"><i class="fa fa-chevron-right fg-resize-widget" aria-hidden="true"></i><i class="fa fa-times remove-widget" title="remove this widget"></i><div class="fg-widget-inner fg-widget-handle"><div class="zone-txt text-center"></div></div></div>') : params.widget;
			// widget.find('.fg-widget-inner').append(nested);

			widget.css({ // set widget style options
				'width': width,
				'min-width': minWidth,
				'max-width': maxWidth,
				'height': height,
				'min-height': minHeight,
				'max-height': maxHeight
			});

			goHere.append(widget); // append the widget
			widget.setData(); // reset the widget attributes

			// DETECT ZONE OVERFLOW
			// 如果启用，则可能出现载入时本应连续多行但中间出现空行的情况
			/*var zo = goHere.zoneOverflow();
			goHere = zo.obj;
			sibs = goHere.siblings('.fg-col');*/

			widget.animateWidget();
			widget.setData();
			goHere.modCols('disable'); // disable overlapped columns

			resize(widget);
			enableSortable();
		}
		
		function findNextColumn(axis, zoneInner, x, y) {
			var goHere;
			switch(axis) {
				case 'x':
					zoneInner.find('.fg-col').each(function() {
						goHere = $(this);
						if ( !goHere.hasClass('fg-disabled-col') && goHere.find('.fg-widget').length == 0 ) {
							goHere = $(this);
							return false; // break when we find an open column
						}
					});
					break;
				case 'y':
					if (x === undefined || x === null || x === '') { // if we still aren't passed an x
						findNextColumn('x', zoneInner);
					}
					goHere = zoneInner.find('.fg-col[data-fg-row="'+y+'"][data-fg-eq="'+x+'"]');
					while (goHere.hasClass('fg-disabled-col')) {
						y++;
						goHere = zoneInner.find('.fg-col[data-fg-row="'+y+'"][data-fg-eq="'+x+'"]');

						if (!goHere.hasClass('fg-disabled-col')) {
							break;
						}
					}
					if (goHere.length == 0) {
						var rowCount = zoneInner.find('.fg-col').last().attr('data-fg-row');
						zoneInner.addRow(y - rowCount); // add as many rows as needed for placing the widget...
						goHere = zoneInner.find('.fg-col[data-fg-row="'+y+'"][data-fg-eq="'+x+'"]');
						goHere = goHere.hasClass('fg-disabled-col') ? goHere.nextAll('.fg-enabled-col[data-fg-row="'+ (y) +'"][data-fg-eq="'+x+'"]').first() : goHere;
					}
					break;
			}

			var result = res = {goHere: goHere};
			return result;
		}

		$.fn.removeWidget = function(widget) {
			var zoneCol = widget.closest('.fg-col');
			zoneCol.modCols('enable');
			var originalContainer = widget.data('originalContainer') != undefined || widget.data('originalContainer') != null ? widget.data('originalContainer') : zoneCol;
			if (!originalContainer.hasClass('fg-col')) { // if the widget came from a different container
				var de = widget.detach();
				originalContainer.append(de);
				de.css({'width':widget.data('ogWidth'), 'height':widget.data('ogHeight'), 'min-width':'', 'min-height':'', 'max-width':'', 'max-height':''});
			} else {
				widget.remove();
			}
			zoneCol.sortable('refresh');
		}

		$.fn.createRow = function() { // add a row to the grid ~ used internally
			var zoneInner = $(this);
			var re = zoneInner.resetVars(); // reset zone variables

			var lastCol = zoneInner.find('.fg-col').last(); // last column in zone
			var appendHere = lastCol[0].offsetTop + re.rH; // new row position
			var rowCount = parseInt(lastCol.attr('data-fg-row')) + 1; // number of rows found within zone
			var colAmount = options.cols; // number of columns spanning a row

			var gridlines = options.showGridlines ? 'fg-gridlines' : ''; // is show gridlines checked?
			for (var i = 0; i < colAmount; i++) {
				zoneInner.append('<div class="fg-enabled-col fg-col ui-sortable '+gridlines+'" data-fg-eq="'+i+'" data-fg-row="'+ rowCount +'" style="min-width: '+re.cW+'px; min-height: '+re.rH+'px; top:'+appendHere+'px; left: '+(re.cW * i)+'px; "></div>');
				var blah = zoneInner.find('.fg-col[data-fg-row="'+ rowCount +'"][data-fg-eq="'+i+'"]');
				var n = zoneInner.find('.fg-col').index(blah);
				blah.attr('data-fg-index', n);
			}
			lastCol =  zoneInner.find('.fg-col').last(); // reset since we added rows
			rowCount = parseInt(lastCol.attr('data-fg-row')) + 1; // reset since we added rows
			zoneInner.height(rowCount * re.rH); // reset since we added rows
			re.zone.height( zoneInner.height() + 50 ); // reset since we added rows
			enableSortable();
		}
		$.fn.addRow = function(val) { // add specified number of rows outside of plugin...
			var zoneInner = $(this);
			val = val === undefined || val === null ? 1 : val;
			for (var i = 0; i < val; i++) {
				zoneInner.createRow();
			}
		}


		$.fn.addRowHere = function(goHere, rowsNeeded) { // rows will be appended after specified columns row
			var zoneInner = $(this);
			var re = zoneInner.resetVars(); // reset zone variables

			var appendHere = goHere[0].offsetTop + re.rH; // new row position
			var rowCount = parseInt(goHere.attr('data-fg-row')) + 1; // number of rows found within zone
			var colAmount = options.cols; // number of columns spanning a row

			var gridlines = options.showGridlines ? 'fg-gridlines' : ''; // is show gridlines checked?
			for (la = 0; la < rowsNeeded; la++) {
				for (var i = 0; i < colAmount; i++) {
					zoneInner.append('<div class="fg-enabled-col fg-col ui-sortable '+gridlines+'" data-fg-eq="'+i+'" data-fg-row="'+rowCount+'" style="min-width: '+re.cW+'px; min-height: '+re.rH+'px; top:'+appendHere+'px; left: '+(re.cW * i)+'px;"></div>');
					var blah = zoneInner.find('.fg-col[data-fg-row="'+(rowCount + 1)+'"][data-fg-eq="'+i+'"]');
					var n = zoneInner.find('.fg-col').index(blah);
					blah.attr('data-fg-index', n);
				}
				appendHere = goHere[0].offsetTop + (re.rH * (la + 2));
				rowCount = rowCount + 1;
			}
			// reset since we added rows
			lastCol =  zoneInner.find('.fg-col').last(); // reset since we added rows
			rowCount = parseInt(lastCol.attr('data-fg-row')) + 1; // reset since we added rows
			zoneInner.height(rowCount * re.rH); // reset since we added rows
			re.zone.height( zoneInner.height() + 50 ); // reset since we added rows
			enableSortable();
		}


		$.fn.removeThisRow = function() { // remove a row from the grid
			var zoneInner = $(this);
			var re = zoneInner.resetVars(); // reset zone variables

			var zoneCol = zoneInner.find('.fg-col');
			var rowCount = parseInt(zoneInner.find('.fg-col').last().attr('data-fg-row')) + 1; // number of rows found in the zone

			if (zoneCol.length > options.cols * options.rows) {
				zoneInner.find('.fg-col[data-fg-row="'+(rowCount - 1)+'"]').remove();
				rowCount = parseInt(zoneInner.find('.fg-col').last().attr('data-fg-row')) + 1;
				zoneInner.css({ 'height': rowCount * re.rH });
				re.zone.css({ 'height': rowCount * re.rH + 55 });
			}

			// check if any widgets are overflowing the zone after we remove rows
			var wigs = zoneInner.find('.fg-widget');
			for (var i = 0; i < wigs.length; i++) {
				var widget = wigs.eq(i);
				var zoneCol = widget.closest('.fg-col');
				widget.setData();
				var data_y = parseInt(widget.attr('data-fg-y'));
				var data_height = parseInt(widget.attr('data-fg-height'));
				var yCon = data_height + data_y > rowCount;
				var dif = ((data_height + data_y) - rowCount) * re.rH;

				// if (yCon) { // if widget is overflowing zone, resize it down to fit
				// 	widget.height(widget.height() - dif);
				// 	widget.setData();
				// }
				if (yCon) {
					moreHeight(widget.closest('.fg-col'));
					widget.setData();
					zoneCol.modCols('disable');
				}
				// if (yCon && widget.height() == options.minHeight * re.rH) { // if we resized down but the widget is as short as it can get, don't remove rows
				// 	moreHeight(widget.closest('.fg-col'));
				// }
			}
			rowCount = parseInt(zoneInner.find('.fg-col').last().attr('data-fg-row')) + 1;
			re.zone.css({ 'height': rowCount * re.rH + 55 });
		}
		$.fn.removeRow = function(val) { // remove specified number of rows outside of plugin...
			var zoneInner = $(this);
			val = val === undefined || val === null ? 1 : val;
			for (var i = 0; i < val; i++) {
				zoneInner.removeThisRow();
			}
		}

		function enableSortable() {
			$('.fg-col').sortable({
				items: '.fg-widget',
				connectWith: '.fg-enabled-col',
				handle: '.fg-widget-handle',
				cursor: 'move',
				placeholder: 'fg-widget-placeholder',
				tolerance: 'intersect',
				start: function(event, ui) {
					var zoneCol = $(this);
					var zoneInner = zoneCol.closest('.flexgrid-grid')
					var sibs = zoneCol.siblings('.fg-col');

					var widget = zoneCol.find('.fg-widget');
					zoneCol.modCols('enable'); // enable overlapped columns

					var wig = $(this).find('.fg-widget-inner'); // set the placeholder's height and width equal to this widget.
					var wigW = wig[0].offsetWidth;
					var wigH = wig[0].offsetHeight;
					zoneInner.find('.fg-widget-placeholder').css({
						'width': wigW,
						'height': wigH
					});

					// set data so we can see if it changes on stop
					ui.item.data({
						'ogIndex': ui.item.closest('.fg-col').index($(this)), // original index value of the widget
						'ogParent': ui.item.closest('.fg-col') // original parent column of the widget
					});
				},
				over: function(event, ui) {
					// refresh sortable columns only when we are dragging over them
					// previously called in sortable start and that caused a lot of lag with multiple widgets...
					var zoneCol = $(this);
					zoneCol.sortable('refresh');
					
					var sibs = zoneCol.siblings('.fg-col');
					var widget = ui.item;
					
					// this is for widgets coming from outside of a grid...
					if (!widget.data('sortableItem').bindings.hasClass('.fg-col')) {
						var originalContainer = widget.data('sortableItem').bindings;
						var ogWidth = widget.data('sortableItem').helperProportions.width;
						var ogHeight = widget.data('sortableItem').helperProportions.height;
						$(this).find('.ui-sortable-placeholder').addClass('fg-widget-placeholder');
						// var minWidth = ui.item.attr('data-fg-minwidth') != options.minWidth ? ui.item.attr('data-fg-minwidth') : options.minWidth * re.cW;
						// var minHeight = ui.item.attr('data-fg-minheight') != options.minHeight ? ui.item.attr('data-fg-minheight') : options.minHeight * re.rH;
						// $(this).find('.ui-sortable-placeholder').css({ 'width': (Math.ceil((ogWidth / re.cW)) * re.cW) - 10, 'height': (Math.ceil((ogHeight / re.rH)) * re.rH) - 10, 'min-width': minWidth - 10, 'min-height':minHeight - 10, 'max-width': options.maxWidth != null && options.maxWidth != undefined ? options.maxWidth * re.cW - 10 : 'unset', 'max-height': options.maxWidth != null && options.maxWidth != undefined ? options.maxWidth * re.cW - 10 : 'unset', 'visibility': 'visible', 'position':'absolute' });
						// ui.item.css({ 'width': Math.ceil((ogWidth / re.cW)) * re.cW, 'height': Math.ceil((ogHeight / re.rH)) * re.rH, 'min-width': minWidth, 'min-height': minHeight, 'max-width': options.maxWidth != null && options.maxWidth != undefined ? options.maxWidth * re.cW : 'unset', 'max-height': options.maxWidth != null && options.maxWidth != undefined ? options.maxWidth * re.cW : 'unset' });
					}
					
					// check if widget can be dropped here
					widget.checkCollision('sort', sibs); // check for collision
					
				},
				receive: function(event, ui) {
					var zoneCol = $(this);
					var zoneInner = zoneCol.closest('.flexgrid-grid');
					var re = zoneInner.resetVars();
					var sibs = zoneCol.siblings('.fg-col');
					var zone = zoneCol.closest('.flexgrid-container');

					ui.item.setData(); // reset here so that zoneOverflow can use the attributes

					if (!ui.item.data('sortableItem').bindings.hasClass('fg-col')) {
						var originalContainer = ui.item.data('sortableItem').bindings; 
						var ogWidth = ui.item.data('sortableItem').helperProportions.width;
						var ogHeight = ui.item.data('sortableItem').helperProportions.height;
						ui.item.data({
							'originalContainer': originalContainer,
							'ogWidth': ogWidth,
							'ogHeight': ogHeight
						}); // set the original container data
						ui.item.css({
							'width': Math.ceil((ogWidth / re.cW)) * re.cW,
							'height': Math.ceil((ogHeight / re.rH)) * re.rH,
							'minWidth': options.minWidth * re.cW,
							'minHeight': options.minHeight * re.rH,
							'maxWidth': options.maxWidth != null && options.maxWidth != undefined ? options.maxWidth * re.cW : 'unset',
							'maxHeight': options.maxWidth != null && options.maxWidth != undefined ? options.maxWidth * re.cW : 'unset'
						});
					}

					var zo = zoneCol.zoneOverflow(); // check if the widget is overflowing the zone
					zoneCol = zo.obj;
					sibs = zoneCol.siblings('.fg-col');

					zoneCol.modCols('disable');

					ui.item.setData(); // reset widget attributes
					ui.item.animateWidget();
					resize(ui.item);
				},
				stop: function(event, ui) {
					var zoneCol = $(this);
					var widget = zoneCol.find('.fg-widget');
					var sibs = zoneCol.siblings('.fg-col');
					var ogParent = ui.item.data('ogParent');

					var zo = zoneCol.zoneOverflow(); // check if the widget is overflowing the zone
					zoneCol = zo.obj;
					sibs = zoneCol.siblings('.fg-col');

					zoneCol.modCols('disable'); // disable overlapped columns

					// ui.item.setData(); // set data attributes
					ui.item.animateWidget();
					resize(ui.item);

					// detect if the item position has changed so that we can remind the user to save...
					if (ui.item.closest('.fg-col').index(zoneCol) != ui.item.data('ogIndex')) {
						console.log('position has changed');
					}
				}
			});
		} enableSortable();

		var re = zoneInner.resetVars(); // reset zone variables
		// Resize function
		function resize(widget) {
			widget.resizable({
				grid: [re.cW, (options.rowHeight > 1 ? options.rowHeight * re.cW : re.cW)],
				handles: 'se',
				containment: zoneInner,
				start: function(event, ui) {
					var widget = ui.element;
					var zoneCol = widget.closest('.fg-col');
					var sibs = zoneCol.siblings('.fg-col');
					zoneCol.modCols('enable'); // enable overlapped columns

					for (var i = 0; i < sibs.length; i++) {
						var colSib = sibs.eq(i);
						if (colSib.children('.fg-widget').length > 0 && (colSib.offset().left == zoneCol.offset().left + zoneCol.width() || colSib.offset().top == zoneCol.offset().top + zoneCol.height()) ) {
							zoneCol.checkCollision('resize', colSib); // check for collision when resizing
						}
					}

					ui.element.data({
						'ogSize': { width: ui.element[0].offsetWidth, height: ui.element[0].offsetHeight }, // find the original size of item so we can later detect if it has changed.
						'ogParent': ui.element.closest('.fg-col') // original parent column of the widget
					});
				},
				resize: function(event, ui) {
					// DETECT COLLISION
					var widget = ui.element;
					var zoneCol = widget.parent();
					var sibs = zoneCol.siblings('.fg-col');
					// so that we can continue to resize once there is no collision
					ui.element.resizable( "option", "maxHeight", null );
					ui.element.resizable( "option", "maxWidth", null );

					for (var i = 0; i < sibs.length; i++) {
						var colSib = sibs.eq(i);
						if (colSib.children('.fg-widget').length > 0 && (colSib.offset().left == zoneCol.offset().left + zoneCol.width() || colSib.offset().top == zoneCol.offset().top + zoneCol.height()) ) {
							zoneCol.checkCollision('resize', colSib); // check for collision when resizing
						}
					}
					widget.setData(); // reset widget attributes
				},
				stop: function(event, ui) {
					var widget = ui.element;
					var zoneCol = widget.closest('.fg-col');
					var sibs = zoneCol.siblings('.fg-col');

					for (var i = 0; i < sibs.length; i++) {
						var colSib = sibs.eq(i);
						if (colSib.find('.fg-widget').length > 0) {
							zoneCol.checkCollision('resize', colSib); // check for collision when resizing
							// 注释掉，因为如果第一行中有两块内容，右侧边有一块，则会出现第二行的块向右拉不到边的情况，会自动缩至与上行左侧的块右侧对齐，但双击文件在本地运行时无此情况
							// dropCollision(colSib, zoneCol); // check for collision on stop, just in case...
						}
					}
					widget.setData(); // reset widget attributes
					zoneCol.modCols('disable'); // disable overlapped columns
					widget.animateWidget();

					// reset max height and width
					widget.resizable( "option", "maxHeight", null );
					widget.resizable( "option", "maxWidth", null );

					// detect if the item size has changed so that we can remind the user to save...
					if (widget[0].offsetWidth != widget.data('ogSize').width || widget[0].offsetHeight != widget.data('ogSize').height) {
						console.log('Size has changed.');
					}
				}
			});
		}
		
		$.fn.checkCollision = function(type, colSib) {
			var el = $(this);
			
			var x1 = el[0].offsetLeft; // widget left position
			var y1 = el[0].offsetTop; // widget top position
			var b1 = y1 + el[0].offsetHeight; // widget bottom position
			var r1 = x1 + el[0].offsetWidth; // widget right position

			var x2 = colSib[0].offsetLeft; // collided widget left position
			var y2 = colSib[0].offsetTop; // collided widget top position
			var b2 = y2 + colSib[0].offsetHeight; // collided widget bottom position
			var r2 = x2 + colSib[0].offsetWidth; // collided widget right position

			// detect when a widget has collided with another during resize, then prevent resizing through that widget...
			if (type == 'resize') {
				// X-AXIS
				if ( (r1 == x2 && y1 == y2 || r1 == x2 && b1 == b2)
						|| (y2 < y1 && b2 > b1 && r2 > r1)
						|| (y1 < y2 && b1 > b2 && r2 > r1)
						|| (y2 < b1 && b2 > b1 && r1 == x2)
						|| (y1 < b2 && b1 >= b2 && y1 >= y2 && r1 >= x2 && x1 < x2))
				{
					$('.ui-resizable-resizing').resizable('option', 'maxWidth', (x2 - x1));
				}
				// Y-AXIS
				if ( (b1 == y2 && x1 == x2 || b1 == y2 && r1 == r2)
						|| (x1 < x2 && r1 > r2 && b1 == y2)
						|| (x2 < x1 && r2 > r1 && b1 == y2)
						|| (r2 > r1 && x2 < r1 && b1 == y2)
						|| (r1 > r2 && x1 < r2 && b1 == y2) )
				{
					$('.ui-resizable-resizing').resizable('option', 'maxHeight', (y2 - y1));
				}
			} else if (type == 'sort') {
// 				var widget = el;
// 				var zoneCol = widget.data().sortableItem.placeholder.parent();
// 				var zoneInner = zoneCol.closest('.flexgrid-grid');
				
// 				var width = parseInt(widget.attr('data-fg-width'));
// 				var height = parseInt(widget.attr('data-fg-height'));
				
// 				var minWidth = parseInt(widget.attr('data-fg-minwidth'));
// 				var minHeight = parseInt(widget.attr('data-fg-minheight'));

// 				var rowStart = parseInt(zoneCol.attr('data-fg-row')) + minWidth; 
// 				var rowEnd = rowStart + height; 
				
// 				var colStart = parseInt(zoneCol.attr('data-fg-eq')) + minHeight; 
// 				var colEnd = colStart + width; 
				
// 				console.log(colStart);

// 				var itemsX = colSib.filter(function() {
// 					var item = $(this);
// 					if (item.hasClass('fg-disabled-col') && item.attr('data-fg-x') >= colStart && item.attr('data-fg-x') <= colEnd) {
// 						console.log('hello');
// 					}
// 				});
			}
			
		};

		// check for collision when dropping a widget, if it overlaps another widget.
		function dropCollision(colSib, col) {
			var widget = col.find('.fg-widget');
			var zoneInner = col.closest('.flexgrid-grid');
			var re = zoneInner.resetVars(); // reset zone variables

			var ogParent = widget.data('ogParent'); // original parent column
			res = {obj: col};

			// column postions
			var x1 = col.offset().left;
			var y1 = col.offset().top;
			var b1 = y1 + col.height();
			var r1 = x1 + col.width();

			// current sibling column positions
			var x2 = colSib.offset().left;
			var y2 = colSib.offset().top;
			var b2 = y2 + colSib.height();
			var r2 = x2 + colSib.width();

			// if the column is colliding with the sibling column at any position
			var con = (r1 > x2 && x1 < x2 && y1 < y2 && b1 > y2) || (r1 > x2 && x1 < x2 && y1 == y2 && b1 == b2) || (r1 > x2 && x1 < x2 && y1 <= y2 && b1 >= b2) || (r1 > x2 && x1 < x2 && y1 >= y2 && b1 <= b2) || (r1 > x2 && x1 < x2 && y1 < b2 && b1 > b2) || (x1 < r2 && r1 > r2 && y1 < y2 && b1 > y2) || (x1 >= x2 && r1 <= r2 && y1 < y2 && b1 > y2);
			// if the columns x-axis is colliding with the sibling columns x-axis
			var xCon = (y1 <= y2 && x1 < x2 && r1 > x2 && b1 > y2) || (y1 < y2 && b1 > b2 && x1 < x2 && r1 > x2) || (b1 >= b2 && y1 < b2 && x1 < x2 && r1 > x2) || (y1 > y2 && b1 < b2 && x1 < x2 && r1 > x2) || (y1 == y1 && b1 == b2 && x1 < x2 && r1 > x2);
			// if the columns y-axis is colliding with the sibling columns y-axis
			var yCon = (x1 < r2 && r1 > r2 && y1 < y2 && b1 > y2) || (x1 >= x2 && r1 <= r2 && y1 < y2 && b1 > y2) || (x1 <= x2 && r1 >= r2 && y1 < y2 && b1 > y2) || (r1 > x2 && x1 < x2 && y1 < y2 && b1 > y2);

			if ($(ogParent).length > 0) { // if we dragged from another column
				if (xCon && yCon) { // if both the x-axis and y-axis have collision
					var xConVal = x2 - x1;
					var yConVal = y2 - y1;
					if (xConVal > yConVal) { // find which axis has more collision and resize for that one
						xCollision();
					} else {
						yCollision();
					}
				} else if (xCon) { // if only x-axis collision, resize for x-axis
					xCollision();
				} else if (yCon) { // if only y-axis collision, resize for y-axis
					yCollision();
				}

				function xCollision() { // x-axis collision
					var saveWidth = widget.width(); // save the width
					widget.width(x2 - x1);
					if (x2 - x1 < widget.minWidth()) { // if new width is less than the minWidth ~
						var de = widget.detach();
						de.width(saveWidth); // ~ resize back to saveWidth
						$(ogParent).append(de); // ~ and revert to ogParent
					}
				}
				function yCollision() { // y-axis collision
					var saveHeight = widget.height(); // save the height
					widget.height(y2 - y1);
					if (y2 - y1 < widget.minHeight()) { // if new height is less than the minHeight ~
						var de = widget.detach();
						de.height(saveHeight); // ~ resize back to saveHeight
						$(ogParent).append(de); // ~ and revert to ogParent
					}
				}
			} else if ($(ogParent).length <= 0) { // if we added a new widget
				var goHere = col;
				while (con) { // detach the widget and append to the next open fg-enabled-col until there is no collision
					var de = goHere.find('.fg-widget').detach();
					goHere = goHere.nextAll('.fg-enabled-col').first();
					if (goHere.length == 0 || goHere == undefined || goHere == null) {
						zoneInner.createRow();
						goHere = zoneInner.find('.fg-widget').last().closest('.fg-col').nextAll('.fg-enabled-col').first();
					}
					goHere.append(de);
					de.setData();

					// check for zone overflow, if true head the widget will head for the next column and check for collision again
					var zo = goHere.zoneOverflow();
					goHere = zo.obj;

					// reset position parameters
					x1 = goHere.offset().left;
					y1 = goHere.offset().top;
					b1 = y1 + goHere.height();
					r1 = x1 + goHere.width();

					// reset condition, otherwise the position variables will still be attached to "el" rather than "goHere"
					con = (r1 > x2 && x1 < x2 && y1 < y2 && b1 > y2) || (r1 > x2 && x1 < x2 && y1 == y2 && b1 == b2) || (r1 > x2 && x1 < x2 && y1 <= y2 && b1 >= b2) || (r1 > x2 && x1 < x2 && y1 >= y2 && b1 <= b2) || (r1 > x2 && x1 < x2 && y1 < b2 && b1 > b2) || (x1 < r2 && r1 > r2 && y1 < y2 && b1 > y2) || (x1 >= x2 && r1 <= r2 && y1 < y2 && b1 > y2);

					if (!con) {
						break;
					}
				}
				res = {obj: goHere};
			}
			return res;
		}

		function moreHeight(el) { // when a widget is appended but it overflows the zone height, append more rows until the height + the data-fg-row == the last rows 'data-fg-row'
			var zoneInner = el.closest('.flexgrid-grid');
			var widget = el.find('.fg-widget');
			var row_and_height = parseInt(el.attr('data-fg-row')) + parseInt(widget.attr('data-fg-height'));
			var last_row_num = parseInt(zoneInner.find('.fg-col').last().attr('data-fg-row'));
			if (row_and_height != last_row_num) {
				var amountNeeded = (row_and_height - last_row_num) - 1;
				for (n = 0; n < amountNeeded; n++) {
					zoneInner.createRow();
				}
			}
			widget.setData();
			widget.modCols('disable');
		}

		$.fn.animateWidget = function() {
			if (options.animate == true) {
				var widget = $(this).find('.fg-widget-inner');
				widget.queue('fx', function(next) {
					$(this).addClass('animate');
					next();
				});
				widget.delay(400).queue('fx', function(next) {
					$(this).removeClass('animate');
					next();
				});
			}
			return 'animation is disabled.';
		}

		$.fn.clearGrid = function() {
			var zoneInner = $(this);
			var widgets = zoneInner.find('.fg-widget');
			for (var i = 0; i < widgets.length; i++) {
				var widget = widgets.eq(i);
				var originalContainer = widget.data('originalContainer') != undefined || widget.data('originalContainer') != null ? widget.data('originalContainer') : widget.closest('.fg-col');
				if (!originalContainer.hasClass('fg-col')) { // if the widget came from a different container
					var de = widget.detach();
					originalContainer.append(de);
					de.css({'width':widget.data('ogWidth'), 'height':widget.data('ogHeight'), 'min-width':'', 'min-height':'', 'max-width':'', 'max-height':''});
				} else { // if the widget was appended by `.addWidget()`
					widget.remove();
				}
			}
			zoneInner.html('');
			zoneInner.buildGrid();
		}
		
		$.fn.getOption = function(option) {
			var res = option === undefined || option === null ? options : options[option];
			return res;
		}
		$.fn.toggleGridlines = function() {
			var zoneInner = $(this);
			options.showGridlines = !options.showGridlines;
			if (!options.showGridlines) {
				$('.fg-gridlines').removeClass('fg-gridlines');
			} else {
				zoneInner.find('.fg-col').addClass('fg-gridlines');
			}
		}
		
		$.fn.saveGrid = function() {
			var zoneInner = $(this);
			var array = [
				{
					cols: options.cols,
					rows: options.rows,
					widgets: []
				}
			];
			var widgets = zoneInner.find('.fg-widget');
			$(widgets).each(function() {
				var widget = $(this);

				var fontColor = widget.attr('fontColor');
				if (fontColor == null) {
					fontColor = '';
				}
				try {
					var rgbaFont = $.parseJSON(fontColor)
					if (rgbaFont.r == 201 && rgbaFont.g == 201 && rgbaFont.b == 201) {
						fontColor = '';
					}
				}
				catch (e) {}
				var bgColor = widget.attr('bgColor');
				if (bgColor == null) {
					bgColor = '';
				}
				try {
					var rgbaBg = $.parseJSON(bgColor)
					if (rgbaBg.r == 201 && rgbaBg.g == 201 && rgbaBg.b == 201) {
						bgColor = '';
					}
				}
				catch (e) {}

				array[0]['widgets'].push({
					data_x: widget.attr('data-fg-x'),
					data_y: widget.attr('data-fg-y'),
					data_width: widget.attr('data-fg-width'),
					data_height: widget.attr('data-fg-height'),
					data_minWidth: widget.attr('data-fg-minwidth'),
					data_minHeight: widget.attr('data-fg-minheight'),
					data_maxWidth: widget.attr('data-fg-maxwidth'),
					data_maxHeight: widget.attr('data-fg-maxheight'),
					innerHtml: widget.find('.fg-widget-inner').html(),

					blockId: widget.attr('blockId')!=null ? widget.attr('blockId') : '',
					type: widget.attr('type')!=null ? widget.attr('type') : '',
					isBold: widget.attr('isBold')!=null ? widget.attr('isBold') : '',
					fontSize: widget.attr('fontSize')!=null ? widget.attr('fontSize') : '',
					align: widget.attr('align')!=null ? widget.attr('align') : '',
					bgImgUrl: widget.attr('bgImgUrl')!=null ? widget.attr('bgImgUrl') : '',
					fontIcon: widget.attr('fontIcon')!=null ? widget.attr('fontIcon') : '',

					bgColor: bgColor,
					fontColor: fontColor,
					imgUrl: widget.attr('imgUrl')!=null ? widget.attr('imgUrl') : '',
					fieldName: widget.attr('fieldName')!=null ? widget.attr('fieldName') : '',
					label: widget.attr('label')!=null ? widget.attr('label') : '',

					paddingLeft: widget.attr('paddingLeft')!=null ? widget.attr('paddingLeft') : '',
					paddingRight: widget.attr('paddingRight')!=null ? widget.attr('paddingRight') : '',
					paddingTop: widget.attr('paddingTop')!=null ? widget.attr('paddingTop') : '',
					paddingBottom: widget.attr('paddingBottom')!=null ? widget.attr('paddingBottom') : ''
				});
			});
			return array;
		}
		
		// end
	}

	$(document).on('resizestop', '.custom-blue-widget', function() {
		/*var text = $(this).find('.inner-icon');
		var width = $(this).attr('data-fg-width');
		var height = $(this).attr('data-fg-height');
		text.text(width+'x'+height);*/
	});
	
	$('.widget-holder').sortable({
		connectWith: '.fg-enabled-col', // connect to the grid columns
		items: '.fg-widget', // make sure the only sortable items are the widgets
		handle: '.fg-widget-handle', // include sortable handle
		helper: 'clone',
		appendTo: 'body'
	});
	
	$('.nested-holder').sortable({
		connectWith: '.fg-nested-container',
		items: '.nested-widget',
		placeholder: 'nested-placeholder',
		handle: '.nested-widget-inner',
		zIndex: 9999,
		start: function(event, ui) {
			if ( ui.item.hasClass('cloner') ) {
				clone = ui.item.clone();
				return clone;
			}
		},
		stop: function(event, ui) {
			if ( ui.item.parent().hasClass('nested-holder') ) {
				ui.item.css({'width':'100px', 'height':'100px', 'min-width':'100px', 'min-height':'100px', 'position':'relative !important'});
			}
			if (ui.item.hasClass('cloner') && ! ui.item.parent('.nested-holder').length ) {
				$(this).append(clone);
			}
		}
	});
	
	$('.fg-nested-container').sortable({
		items: '.nested-widget',
		handle: '.nested-widget-inner',
		placeholder: 'nested-placeholder',
		connectWith: '.fg-nested-container'
	});
	
