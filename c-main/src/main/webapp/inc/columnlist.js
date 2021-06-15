var TYPE_STRING         = 0;
var TYPE_NUMBER         = 1;
var TYPE_DATE           = 2;
var TYPE_STRING_NO_CASE = 3;

var SORT_ASCENDING  = 0;
var SORT_DESCENDING = 1;

var ALIGN_AUTO   = 0;
var ALIGN_LEFT   = 1;
var ALIGN_CENTER = 2;
var ALIGN_RIGHT  = 3;

var COL_HEAD_NONE = 0;
var COL_HEAD_EDGE = 1;
var COL_HEAD_OVER = 2;
var COL_HEAD_SIZE = 3;
var COL_HEAD_DOWN = 4;
var COL_HEAD_MOVE = 5;

/*
 * oColumnList = new WebFXColumnList()
 * Default constructor
 */
function WebFXColumnList() {

	/* public properties */
	this.multiple       = true;                                                   // Allow multiple selection (true or false)
	this.colorEvenRows  = true;                                                   // Mark even rows with different color (true or false)
	this.resizeColumns  = true;                                                   // Enable column resizing (true or false)
	this.bodyColResize  = true;                                                   // Resize body columns duing resize operation (true or false)
	this.moveColumns    = true;                                                   // Enable column moving (true or false)
	this.rowSelection   = true;                                                   // Enable row selection (true or false)
	this.columnSorting  = true;                                                   // Enable sorting (true or false)
	this.columnAlign    = true;                                                   // Enable column text alignment (true or false)
	this.sortAscImage   = '../images/asc.png';                                       // Image used to indicate ascending sort order
	this.sortDescImage  = '../images/desc.png';                                      // Image used to indicate descending sort order

	/* public read only properties */
	this.sortCol        = -1;                                                     // Column index currently sorted by, read only
	this.sortDescending = 0;                                                      // Column sort direction, read only (SORT_ASCENDING or SORT_DESCENDING)
	this.error          = '';                                                     // Error message set if an error code was returned, read only.
	this.selectedRows   = [];                                                     // Currently selected rows, read only.

	/* events */
	this.onresize       = null;
	this.onsort         = null;
	this.onselect       = null;

	/* private properties */
	this._eCont         = null;
	this._eHead         = null;
	this._eBody         = null;
	this._eHeadTable    = null;
	this._eBodyTable    = null;
	this._eHeadCols     = null;
	this._eBodyCols     = null;

	this._aColumnTypes  = [];
	this._aColumnAlign  = [];
	this._rows          = 0;
	this._cols          = 0;
	this._headerOper    = COL_HEAD_NONE;
	this._headerData    = null;
}


/*
 * iError = create(eContainer, sColumn[])
 * Transforms the supplied container into a column list.
 * sColumn[]   - Array containing column headers
 * sColumn[][] - Two dimensional array with column headers, widths and types
*/
WebFXColumnList.prototype.create = function(eContainer, aColumns) {
	var eRow, eCell, eDiv, eImg, eTableBody, eColGroup, eCol, a, b;

	for (var i = eContainer.childNodes.length - 1; i >= 0; i--) {
		eContainer.removeChild(eContainer.childNodes[i]);
	}

	/* Create container, header and body */
	this._eCont = eContainer;
	this._eHead = document.createElement('div');
	this._eBody = document.createElement('div');
	this._eCont.className = 'webfx-columnlist';
	this._eHead.className = 'webfx-columnlist-head';
	this._eBody.className = 'webfx-columnlist-body';
	this._eCont.appendChild(this._eHead);
	this._eCont.appendChild(this._eBody);

	/* Populate header */
	this._eHeadTable = document.createElement('table');
	this._eHeadTable.style.width = '100px';                                       // if a width is not set here the overflow: hidden rule will be ignored by mozilla
	this._eHeadTable.cellSpacing = 0;
	this._eHeadTable.cellPadding = 0;
	this._eHead.appendChild(this._eHeadTable);
	eTableBody = document.createElement('tbody');
	this._eHeadTable.appendChild(eTableBody);
	eRow = document.createElement('tr');
	eTableBody.appendChild(eRow);
	for (var i = 0; i < aColumns.length; i++) {
		eCell = document.createElement('td');
		eRow.appendChild(eCell);
		eImg  = document.createElement('img');
		if (typeof(aColumns[i]) == 'object') { eCell.appendChild(document.createTextNode(aColumns[i][0])); }
		else { eCell.appendChild(document.createTextNode(aColumns[i])); }
		eCell.appendChild(eImg);
	}

	/* Create main table, colgroup and col elements */
	this._eBodyTable = document.createElement('table');
	this._eBodyTable.cellSpacing = 0;
	this._eBodyTable.cellPadding = 0;
	this._eBody.appendChild(this._eBodyTable);
	eTableBody = document.createElement('tbody');
	this._eBodyTable.appendChild(eTableBody);
	eColGroup = document.createElement('colgroup');
	this._eBodyTable.appendChild(eColGroup);
	for (var i = 0; i < aColumns.length; i++) {
		eCol = document.createElement('col');
		if ((typeof(aColumns[i]) == 'object') && (aColumns[i].length) && (aColumns[i].length > 1)) { eCol.style.width = aColumns[i][1]; }
		else { eCol.style.width = 'auto'; }
		eColGroup.appendChild(eCol);
	}

	/* Init sortable table */
	this._stl = new SortableTable(this._eBodyTable);

	/* Set column data types */
	a = new Array(); b = false;
	for (var i = 0; i < aColumns.length; i++) {
		if ((typeof(aColumns[i]) == 'object') && (aColumns[i].length) && (aColumns[i].length > 2)) { a.push(aColumns[i][2]); b = true; }
		else { a.push(TYPE_STRING); }
	}

	/* Only set explicitly if type was specified for at least one column */
	if (b) { this.setColumnTypes(a); }

	this._eHeadCols = eRow.cells;
	this._eBodyCols = null;

	this._cols = aColumns.length;
	this._rows = 0;

	this._init();

	return 0;
};

/*
 * iError = bind(eContainer, eHeader, eBody)
 * Binds column list to an existing HTML structure. Use create
 * to generate the strucutr automatically.
*/
WebFXColumnList.prototype.bind = function(eCont, eHead, eBody) {
	try {
		this._eCont      = eCont;
		this._eHead      = eHead;
		this._eBody      = eBody;
		this._eHeadTable = this._eHead.getElementsByTagName('table')[0];
		this._eBodyTable = this._eBody.getElementsByTagName('table')[0];
	 	this._eHeadCols  = this._eHeadTable.tBodies[0].rows[0].cells;
		this._eBodyCols  = this._eBodyTable.tBodies[0].rows[0].cells;
	}
	catch(oe) {
		this.error = 'Unable to bind to elements: ' + oe.message;
		return 1;
	}
	if (this._eHeadCols.length != this._eBodyCols.length) {
		this.error = 'Unable to bind to elements: Number of columns in header and body does not match.';
		return 2;
	}

	this._eHeadCols  = this._eHeadTable.tBodies[0].rows[0].cells;
	this._eBodyCols  = this._eBodyTable.tBodies[0].rows[0].cells;

	this._cols = this._eHeadCols.length;
	this._rows = this._eBodyTable.tBodies[0].rows.length;

	this._stl = new SortableTable(this._eBodyTable);

	/* Set column class names (used for alignment in mozilla) */
	if ((!document.all) && (this.columnAlign)) {
		aRows = this._eBodyTable.tBodies[0].rows;
		this._rows = aRows.length;
		for (i = 0; i < this._rows; i++) {
			for (j = 0; j < this._cols; j++) {
				aRows[i].cells[j].className = 'webfx-columnlist-col-' + j;
	}	}	}

	this._init();

	return 0;
};

/*
 * void _init(iWidth, iHeight)
 * Initializes column list, called by create and bind
*/
WebFXColumnList.prototype._init = function(iWidth, iHeight) {
	if (navigator.product == 'Gecko') {
		/*
		 * Mozilla does not allow the scroll* properties of containers with the
		 * overflow property set to 'hidden' thus we'll have to set it to
		 * '-moz-scrollbars-none' which is basically the same as 'hidden' in IE,
		 * the container has overflow type 'scroll' but no scrollbars are shown.
		*/
		for (var n = 0; n < document.styleSheets.length; n++) {
			if (document.styleSheets[n].href.indexOf('columnlist.css') == -1) { continue; }
			var rules = document.styleSheets[n].cssRules;
			for (var i = 0; i < rules.length; i++) {
				if ((rules[i].type == CSSRule.STYLE_RULE) && (rules[i].selectorText == '.webfx-columnlist-head')) {
					rules[i].style.overflow = '-moz-scrollbars-none';
	}	}	}	}

	/*
	 * Set tab index to allow element to be focused using keyboard, also allows
	 * keyboard events to be captured for Mozilla.
	 */
	this._eCont.tabIndex = '0';

	this.calcSize();
	this._assignEventHandlers();
	if (this.colorEvenRows) { this._colorEvenRows(); }
	if (this.columnAlign)   { this._setAlignment(); }
}

/*
 * void _assignEventHandlers()
 * Assigns event handlers to the grid elements, called by bind.
*/
WebFXColumnList.prototype._assignEventHandlers = function() {
	var oThis = this;
	this._eCont.onclick     = function(e) { oThis._click(e); }
	if (this.resizeColumns) {
		this._eCont.onmousedown = function(e) { oThis._mouseDown(e); }
		this._eCont.onmousemove = function(e) { oThis._mouseMove(e); }
	}
	this._eCont.onmouseup   = function(e) { oThis._mouseUp(e); }
	this._eCont.onselectstart = function(e) { return false; }
	this._eBody.onscroll = function() {
		oThis._eHead.scrollLeft = oThis._eBody.scrollLeft;
	};
	this._eCont.onkeydown = function(e) {
		var el = (e)?e.target:window.event.srcElement;
		var key = (e)?e.keyCode:window.event.keyCode;
		if (oThis._handleRowKey(key)) { return; }
		if (window.event) { window.event.cancelBubble = true; }
		else { e.preventDefault(); e.stopPropagation() }
		return false;
	};
};

/*
 * void calcSize()
 * Used to calculate the desired size of the grid and size it accordingly.
 */
WebFXColumnList.prototype.calcSize = function() {
	if (this._eCont.offsetWidth >= 4) {

		/* Size body */
		var h = this._eCont.clientHeight - this._eHead.offsetHeight - 2;
		if (h >= 0) { this._eBody.style.height = h + 'px'; }
		this._eBody.style.width = this._eCont.offsetWidth - 4 + 'px';
		this._eBody.style.paddingTop = this._eHead.offsetHeight + 'px';
		this._eBodyTable.style.width = this._eBody.clientWidth + 'px';

		/* Size header */
		var bNoScrollbar = ((this._eBody.offsetWidth - this._eBody.clientWidth) == 2);
		this._eHeadTable.style.width = this._eHead.style.width = this._eBody.clientWidth + ((bNoScrollbar)?2:0) + 'px';

		/* Size columns */
		if (this._eBodyCols) {
			var length = this._eBodyCols.length;
			for (var i = 0; i < length; i++) {
				this._eHeadCols[i].style.width = (this._eBodyCols[i].offsetWidth - 4) + ((bNoScrollbar) && (i == length - 1)?2:0) + 'px';
	}	}	}

	this._eHeadTable.style.width = 'auto';
};

/*
 * iErrorCode = selectRow(iRowIndex, bMultiple)
 * Selects the row identified by the sequence number supplied,
 *
 * If bMultiple is specified and multi-select is allowed the
 * the previously selected row will not be deselected. If the
 * specified row is already selected it will be deselected.
 */
WebFXColumnList.prototype.selectRow = function(iRowIndex, bMultiple) {
	if (!this.rowSelection) { return; }

	if ((iRowIndex < 0) || (iRowIndex > this._rows - 1)) {
		this.error = 'Unable to select row, index out of range.';
		return 1;
	}
	var eRows = this._eBodyTable.tBodies[0].rows;
	var bSelect = true;

	/* Normal click */
	if ((!bMultiple) || (!this.multiple)) {

		/* Deselect previously selected rows */
		while (this.selectedRows.length) {
			if (this.colorEvenRows) { eRows[this.selectedRows[0]].className = (this.selectedRows[0] & 1)?'odd':'even'; }
			else { eRows[this.selectedRows[0]].className = ''; }
			this.selectedRows.splice(0, 1);
	}	}

	/* Control + Click */
	else {
		for (var i = 0; i < this.selectedRows.length; i++) {

			/* Deselect clicked row */
			if (this.selectedRows[i] == iRowIndex) {
				if (this.colorEvenRows) { eRows[this.selectedRows[i]].className = (i & 1)?'odd':'even'; }
				else { eRows[this.selectedRows[i]].className = ''; }
				this.selectedRows.splice(i, 1);
				bSelect = false;
				break;
	}	}	}

	/* Select clicked row */
	if (bSelect) {
		this.selectedRows.push(iRowIndex);
		eRows[iRowIndex].className = 'selected';
	}

	var a = (eRows[iRowIndex].offsetTop + this._eHead.offsetHeight) + eRows[iRowIndex].offsetHeight + 1;
	var b = (this._eBody.clientHeight + this._eBody.scrollTop);
	if (a > b) {
		this._eBody.scrollTop = (a - this._eBody.clientHeight);
	}
	var c = eRows[iRowIndex].offsetTop;
	var d = this._eBody.scrollTop;
	if (c < d) {
		this._eBody.scrollTop = c;
	}

	/* Call onselect if defined */
	if (this.onselect) { this.onselect(this.selectedRows); }

	return 0;
};


/*
 * iErrorCode = selectRange(iRowIndex[])
 * iErrorCode = selectRange(iFromRowIndex, iToRowIndex)
 * Selects all rows between iFromRowIndex and iToRowIndex.
 */
WebFXColumnList.prototype.selectRange = function(a, b) {
	var aRowIndex;

	if (!this.rowSelection) { return; }

	if (typeof a == 'number') {
		aRowIndex = new Array();
		for (var i = a; i <= b; i++) { aRowIndex.push(i); }
		for (var i = b; i <= a; i++) { aRowIndex.push(i); }
	}
	else { aRowIndex = a; }

	for (var i = 0; i < aRowIndex.length; i++) {
		if ((aRowIndex[i] < 0) || (aRowIndex[i] > this._rows - 1)) {
			this.error = 'Unable to select rows, index out of range.';
			return 1;
	}	}

	/* Deselect previously selected rows */
	var eRows = this._eBodyTable.tBodies[0].rows;
	while (this.selectedRows.length) {
		if (this.colorEvenRows) { eRows[this.selectedRows[0]].className = (this.selectedRows[0] & 1)?'odd':'even'; }
		else { eRows[this.selectedRows[0]].className = ''; }
		this.selectedRows.splice(0, 1);
	}

	/* Select all rows indicated by range */
	var eRows = this._eBodyTable.tBodies[0].rows;
	var bMatch;
	for (var i = 0; i < aRowIndex.length; i++) {
		bMatch = false;
		for (var j = 0; j < this.selectedRows.length; j++) {
			if (this.selectedRows[j] == aRowIndex[i]) { bMatch = true; break; }
		}
		if (!bMatch) {
			/* Select row */
			this.selectedRows.push(aRowIndex[i]);
			eRows[aRowIndex[i]].className = 'selected';
	}	}

	/* Call onselect if defined */
	if (this.onselect) { this.onselect(this.selectedRows); }

	return 0;
};


/*
 * void resize(iWidth, iHeight)
 * Resize the grid to the given dimensions, the outer (border) size is given, not the inner (content) size.
 */
WebFXColumnList.prototype.resize = function(w, h) {
	this._eCont.style.width = w + 'px';
	this._eCont.style.height = h + 'px';
	this.calcSize();

	/* Call onresize if defined */
	if (this.onresize) { this.onresize(); }
};

/*
 * void _colorEvenRows()
 * Changes the color of even rows (usually to light yellow) to make it easier to read.
 * Also updates the id column to a sequence counter rather than the row ids.
 */
WebFXColumnList.prototype._colorEvenRows = function() {
	if (this._eBodyTable.tBodies.length) {
		var nodes = this._eBodyTable.tBodies[0].rows;
		var len = nodes.length;
		for (var i = 0; i < len; i++) {
			if (nodes[i].className != 'selected') {
				nodes[i].className = (i & 1)?'odd':'even';
	}	}	}
};

/*
 * iErrorCode = addRow(aRowData)
 * Appends supplied row to the column list.
 */
WebFXColumnList.prototype.addRow = function(aRowData) {
	var rc = this._addRow(aRowData);
	if (rc) { return rc; }
	this.calcSize();
	return 0;
};


/*
 * iErrorCode = addRows(aData)
 * Appends supplied rows to the column list.
 */
WebFXColumnList.prototype.addRows = function(aData) {
	for (var i = 0; i < aData.length; i++) {
		var rc = this._addRow(aData[i]);
		if (rc) { return rc; }
	}
	this.calcSize();
	return 0;
};

/*
 * void _colorEvenRows()
 * Changes the color of even rows (usually to light yellow) to make it easier to read.
 * Also updates the id column to a sequence counter rather than the row ids.
 */
WebFXColumnList.prototype._colorEvenRows = function() {
	if (this._eBodyTable.tBodies.length) {
		var nodes = this._eBodyTable.tBodies[0].rows;
		for (var i = 0; i < nodes.length; i++) {
			if (nodes[i].className != 'selected') {
				nodes[i].className = (i & 1)?'odd':'even';
	}	}	}
};

/*
 * iErrorCode = _addRow(aRowData)
 */
WebFXColumnList.prototype._addRow = function(aRowData) {
	var eBody, eRow, eCell, i, len;

	/* Validate column count */
	if (aRowData.length != this._cols) { return 1; }

	/* Construct Body Row */
	eBody = this._eBodyTable.tBodies[0];
	eRow  = document.createElement('tr');
	if (this.colorEvenRows) {
		eRow.className = (this._rows & 1)?'odd':'even';
	}

	for (i = 0; i < this._cols; i++) {
		eCell = document.createElement('td');
		eCell.className = 'webfx-columnlist-col-' + i;
		eCell.appendChild(document.createTextNode(aRowData[i]));
		eRow.appendChild(eCell);
	}
	eBody.appendChild(eRow);

	/* Update row counter */
	this._rows++;

	if (this._eBodyCols == null) {
		this._eBodyCols = this._eBodyTable.tBodies[0].rows[0].cells;
	}

	return 0;
};


/*
 * iErrorCode = removeRow(iRowIndex)
 * Appends supplied row to the grid.
 */
WebFXColumnList.prototype.removeRow = function(iRowIndex) {
	/* Remove row */
	var rc = this._removeRow(iRowIndex);
	if (rc) { return rc; }

	/* Update row counter and select previous row, if any */
	this._rows--;
	this.selectRow((iRowIndex > 1)?iRowIndex-1:0);

	/* Recolor rows, if needed */
	if (this.colorEvenRows) { this._colorEvenRows(); }
	this.calcSize();

	/* Call onselect if defined */
	if (this.onselect) { this.onselect(this.selectedRows); }

	return 0;
};


/*
 * iErrorCode = removeRange(iRowIndex[])
 * iErrorCode = removeRange(iFirstRowIndex, iLastRowIndex)
 * Appends supplied row to the grid.
 */
WebFXColumnList.prototype.removeRange = function(a, b) {
	var aRowIndex = new Array();
	if (typeof a == 'number') {
		for (var i = a; i <= b; i++) { aRowIndex.push(i); }
	}
	else {
		for (var i = 0; i < a.length; i++) {
			aRowIndex.push(a[i]);
		}
		aRowIndex.sort(compareNumericAsc);
	}

	while ((i = aRowIndex.pop()) >= 0) {
		var rc = this._removeRow(i);
		this._rows--;
	}

	/* Recolor rows, if needed */
	if (this.colorEvenRows) { this._colorEvenRows(); }
	this.calcSize();

	/* Call onselect if defined */
	if (this.onselect) { this.onselect(this.selectedRows); }

	return 0;
};


/*
 * iErrorCode = clear()
 * Removes all rows from the column list.
 */
WebFXColumnList.prototype.clear = function() {
	return this.removeRange(0, this._rows - 1);
}

/*
 * iErrorCode = _removeRow(iRowIndex)
 */
WebFXColumnList.prototype._removeRow = function(iRowIndex) {
	if ((iRowIndex < 0) || (iRowIndex > this._rows - 1)) {
		this.error = 'Unable to remove row, row index out of range.';
		return 1;
	}

	/* Remove from selected */
	for (var i = this.selectedRows.length - 1; i >= 0; i--) {
		if (this.selectedRows[i] == iRowIndex) {
			this.selectedRows.splice(i, 1);
	}	}

	this._eBodyTable.tBodies[0].removeChild(this._eBodyTable.tBodies[0].rows[iRowIndex]);
	return 0;
};


/*
 * iRowIndex getSelectedRow()
 * Returns the index of the selected row or -1 if no row is selected.
 */
WebFXColumnList.prototype.getSelectedRow = function() {
	return (this.selectedRows.length)?this.selectedRows[this.selectedRows.length-1]:-1;
};


/*
 * iRowIndex[] getSelectedRange()
 * Returns an array with the row index of all selecteds row or null if no row is selected.
 */
WebFXColumnList.prototype.getSelectedRange = function() {
	return (this.selectedRows.length)?this.selectedRows:-1;
};


/*
 * iRows getRowCount()
 * Returns the nummer of rows.
 */
WebFXColumnList.prototype.getRowCount = function() {
	return this._rows;
};


/*
 * iRows getColumnCount()
 * Returns the nummer of columns.
 */
WebFXColumnList.prototype.getColumnCount = function() {
	return this._cols;
};


/*
 * sValue = getCellValue(iRowIndex, iColumnIndex, bHTML)
 * Returns the content of the specified cell.
 */
WebFXColumnList.prototype.getCellValue = function(iRowIndex, iColIndex, bHTML) {
	var el;

	if ((iRowIndex < 0) || (iRowIndex > this._rows - 1)) {
		this.error = 'Unable to get cell value , row index out of range.';
		return null;
	}
	if ((iColIndex < 0) || (iColIndex > this._cols - 1)) {
		this.error = 'Unable to get cell value , row index out of range.';
		return null;
	}

	el = this._eBodyTable.tBodies[0].rows[iRowIndex].cells[iColIndex];

	return (bHTML)?el.innerHTML:getInnerText(el);
};


/*
 * iError = setCellValue(iRowIndex, iColumnIndex, sValue, bHTML)
 * Sets the content of the specified cell.
 */
WebFXColumnList.prototype.setCellValue = function(iRowIndex, iColIndex, sValue, bHTML) {
	var el;

	if ((iRowIndex < 0) || (iRowIndex > this._rows - 1)) {
		this.error = 'Unable to set cell value , row index out of range.';
		return 1;
	}
	if ((iColIndex < 0) || (iColIndex > this._cols - 1)) {
		this.error = 'Unable to set cell value , row index out of range.';
		return 2;
	}

	el = this._eBodyTable.tBodies[0].rows[iRowIndex].cells[iColIndex];
	if (bHTML) { el.innerHTML = sValue; }
	else {
		while (el.firstChild != el.lastChild) { el.removeChild(el.lastChild); }
		if (el.firstChild) { el.firstChild.nodeValue = sValue; }
		else { el.appendChild(document.createTextNode(sValue)); }
	}

	this.calcSize();

	return 0;
};

/*
 * void setSortTypes(sSortType[]) {
 * Sets the column data types for sorting.
 * Valid options: TYPE_STRING, TYPE_NUMBER, TYPE_DATE, TYPE_STRING_NO_CASE or
 * custom string value that will be passed to sortable table. Can be registered
 * with the SortableTable.prototype.addSortType method.
 */
WebFXColumnList.prototype.setSortTypes = function(aSortTypes) {
	var i, a = new Array();

	this._aColumnTypes = aSortTypes;
	for (i = 0; i < this._cols; i++) {
		switch (aSortTypes[i]) {
			case TYPE_STRING:         a.push('String');                break;
			case TYPE_NUMBER:         a.push('Number');                break;
			case TYPE_DATE:           a.push('Date');                  break;
			case TYPE_STRING_NO_CASE: a.push('CaseInsensitiveString'); break;
			default:                  a.push('String');
		};
	}
	this._stl.setSortTypes(a);
};


/*
 * void setColumnTypes(aColumnTypes[]) {
 * Sets the column data types for sorting, also affects the alignment for columns
 * with alignment set to ALIGN_AUTO (which is the default), strings and dates
 * are left aligned, numbers right aligned.
 * Valid options: TYPE_STRING, TYPE_NUMBER, TYPE_DATE, TYPE_STRING_NO_CASE or
 * custom string value that will be passed to sortable table. Can be registered
 * with the SortableTable.prototype.addSortType method.
 */
WebFXColumnList.prototype.setColumnTypes = function(aColumnTypes) {
	this.setSortTypes(aColumnTypes);
	if (this.columnAlign)   { this._setAlignment(); }
};

/*
 * void setColumnAlignment(iAlignment[])
 * Sets column text alignment, ALIGN_AUTO, ALIGN_LEFT, ALIGN_CENTER or ALIGN_RIGHT.
 */
WebFXColumnList.prototype.setColumnAlignment = function(aAlignment) {
	this._aColumnAlign = aAlignment;
};

/*
 * void sort(iColumnIndex, [bDescending])
 * Sorts the grid by the specified column (zero based index) and, optionally, in the specified direction.
 */
WebFXColumnList.prototype.sort = function(iCol, bDesc) {
	if (!this.columnSorting) { return; }

	/* Hide arrow from header for column currently sorted by */
	if (this.sortCol != -1) {
		var eImg = this._eHeadTable.tBodies[0].rows[0].cells[this.sortCol].getElementsByTagName('img')[0];
		eImg.style.display = 'none';
	}

	/* Determine sort direction */
	if (bDesc == null) {
		bDesc = false;
		if ((!this.sortDescending) && (iCol == this.sortCol)) { bDesc = true; }
	}

	/* Indicate sorting using arrow in header */
	var eImg = this._eHeadTable.tBodies[0].rows[0].cells[iCol].getElementsByTagName('img')[0];
	eImg.src = (bDesc)?this.sortDescImage:this.sortAscImage;
	eImg.style.display = 'inline';

	/* Perform sort operation */
	this._stl.sort(iCol, bDesc);
	this.sortCol = iCol;
	this.sortDescending = bDesc;

	/* Update row coloring */
	if (this.colorEvenRows) { this._colorEvenRows(); }

	/* Update selection */
	var nodes = this._eBodyTable.tBodies[0].rows;
	var len = nodes.length;
	var a = new Array();
	for (var i = 0; i < len; i++) {
		if (nodes[i].className == 'selected') { a.push(i); }
	}
	this.selectRange(a);

	/* Call onsort if defined */
	if (this.onsort) { this.onsort(this.sortCol, this.sortDescending); }

};


/*
 * void _handleRowKey(iKeyCode)
 * Key handler for events on row level.
 */
WebFXColumnList.prototype._handleRowKey = function(iKeyCode, bCtrl, bShift) {
	var iActiveRow = -1;
	if (this.selectedRows.length != 0) { iActiveRow = this.selectedRows[this.selectedRows.length-1]; }
	if ((!bCtrl) && (!bShift)) {
		if (iKeyCode == 38) {                                                       // Up
			if (iActiveRow > 0) { this.selectRow(iActiveRow - 1); }
		}
		else if (iKeyCode == 40) {                                                  // Down
			if (iActiveRow < this._rows - 1) { this.selectRow(iActiveRow + 1); }
		}
		else if (iKeyCode == 33) {                                                  // Page Up
			if (iActiveRow > 10) { this.selectRow(iActiveRow - 10); }
			else { this.selectRow(0); }
		}
		else if (iKeyCode == 34) {                                                  // Page Down
			if (iActiveRow < this._rows - 10) { this.selectRow(iActiveRow + 10); }
			else { this.selectRow(this._rows - 1); }
		}
		else if (iKeyCode == 36) { this.selectRow(0); }                             // Home
		else if (iKeyCode == 35) { this.selectRow(this._rows - 1); }                // End
		else { return true; }
		return false;
	}
};


/*
 * Event Handlers
 */
WebFXColumnList.prototype._mouseMove = function(e) {
	var el, x, w, tw, ox, rx, i, l;

	el = (e)?e.target:window.event.srcElement;
	x = (e)?e.pageX:window.event.x + this._eBody.scrollLeft;

	/*
	 * Column move operation started, create elements required to indicate moving
	 * and set operation flag to COL_HEAD_MOVE.
	 */
	if ((this._headerOper == COL_HEAD_DOWN) && (this.moveColumns)) {
		this._headerOper = COL_HEAD_MOVE;
		this._eCont.style.cursor = 'move';

		w = this._headerData[2] + (x - this._headerData[1]);

		if (!this._moveEl) {
			this._moveEl = document.createElement('div');
			this._moveEl.appendChild(document.createTextNode(this._headerData[0].firstChild.nodeValue));
			this._moveEl.className = 'webfx-columnlist-move-header';
			this._eHead.appendChild(this._moveEl);

			if (this.columnAlign) {
				switch (this._aColumnAlign[this._headerData[0].cellIndex]) {
					case ALIGN_LEFT:   this._moveEl.style.textAlign = 'left';   break;
					case ALIGN_CENTER: this._moveEl.style.textAlign = 'center'; break;
					case ALIGN_RIGHT:  this._moveEl.style.textAlign = 'right';  break;
					case ALIGN_AUTO:
					default:
						switch(this._aColumnTypes[this._headerData[0].cellIndex]) {
							case TYPE_NUMBER: this._moveEl.style.textAlign = 'right'; break;
							default:          this._moveEl.style.textAlign = 'left';
						};
				};
			}


		}
		else { this._moveEl.firstChild.nodeValue = this._headerData[0].firstChild.nodeValue; }
		this._moveEl.style.width = this._headerData[0].clientWidth + 'px';

		if (!this._moveSepEl) {
			this._moveSepEl = document.createElement('div');
			this._moveSepEl.className = 'webfx-columnlist-separator-header';
			this._eHead.appendChild(this._moveSepEl);
	}	}

	/*
	 * Column move operation, determine position of column and move place holder
	 * to that position. Also indicate in between which columns it will be placed.
	 */
	if (this._headerOper == COL_HEAD_MOVE) {
		ox = this._headerData[1] + (x - this._headerData[2]);
		this._moveEl.style.left = ox + 'px';

		ox = 0, rx = x - this._headerData[3];
		for (i = 0; i < this._cols; i++) {
			ox += this._eHeadCols[i].offsetWidth;
			if (ox >= rx) { break; }
		}
		if (i == this._cols) { this._moveSepEl.style.left = (this._eHeadCols[this._cols-1].offsetLeft + this._eHeadCols[this._cols-1].offsetWidth - 1) + 'px'; }
		else { this._moveSepEl.style.left = this._eHeadCols[i].offsetLeft + 'px'; }

		this._headerData[4] = i;
	}

	/*
	 * Column resize operation, determine and set new size based on the original
	 * size and the difference between the current mouse position and the one that
	 * was recorded once the resize operation was started.
	 */
	else if (this._headerOper == COL_HEAD_SIZE) {
		w = this._headerData[1] + x - this._headerData[2];
		tw = ((w - this._headerData[1]) + this._headerData[3]) + 1;
		this._eHeadTable.style.width = tw + 'px';
		if (w > 5) {
			this._headerData[0].style.width = w + 'px';
			if (this.bodyColResize) {
				this._eBodyTable.style.width = tw + 'px';
				this._eBodyTable.getElementsByTagName('colgroup')[0].getElementsByTagName('col')[this._headerData[0].cellIndex].style.width = w + 'px';
	}	}	}

	else { this._checkHeaderOperation(el, x); }

};


WebFXColumnList.prototype._mouseDown = function(e) {
	var el = (e)?e.target:window.event.srcElement;
	var x = (e)?e.pageX:window.event.x + this._eBody.scrollLeft;

	if ((el.tagName == 'TD') && (el.parentNode.parentNode.parentNode.parentNode.className == 'webfx-columnlist-head')) {
		this._checkHeaderOperation(el, x);

		if (this._headerOper == COL_HEAD_EDGE) {
			if (this.bodyColResize) { this._sizeBodyAccordingToHeader(); }
			this._headerOper = COL_HEAD_SIZE;
		}
		else if (this._headerOper == COL_HEAD_OVER) {
			this._headerOper = COL_HEAD_DOWN;
			this._headerData[0].className = 'webfx-columnlist-active-header';
	}	}
};


WebFXColumnList.prototype._mouseUp = function(e) {
	var el = (e)?e.target:window.event.srcElement;
	var x = (e)?e.pageX:window.event.x + this._eBody.scrollLeft;

	if (this._headerOper == COL_HEAD_SIZE) {

	}
	else if (this._headerOper == COL_HEAD_MOVE) {
		if (this._moveEl)    { this._eHead.removeChild(this._moveEl);    this._moveEl    = null; }
		if (this._moveSepEl) { this._eHead.removeChild(this._moveSepEl); this._moveSepEl = null; }
		this._moveColumn(this._headerData[0].cellIndex, this._headerData[4]);
	}
	else if (this._headerOper == COL_HEAD_DOWN) {
		this.sort(el.cellIndex);
	}

	if (this._headerOper != COL_HEAD_NONE) {
		this._headerOper = COL_HEAD_NONE;
		this._eCont.style.cursor = 'default';
		this._headerData[0].className = '';
		this._headerData = null;
		this._sizeBodyAccordingToHeader();
	}

};


WebFXColumnList.prototype._click = function(e) {
	var el = (e)?e.target:window.event.srcElement;
	if (el.tagName == 'NOBR') { el = el.parentNode; }
	if (el.tagName == 'IMG') { el = el.parentNode; }
	if (el.tagName == 'DIV') { el = el.parentNode; }
	if ((el.tagName == 'TD') && (el.parentNode.parentNode.parentNode.parentNode.className == 'webfx-columnlist-body')) {
		if (((e)?e.shiftKey:window.event.shiftKey) && (this.selectedRows.length) && (this.multiple)) {
			this.selectRange(this.selectedRows[this.selectedRows.length-1], el.parentNode.rowIndex);
		}
		else { this.selectRow(el.parentNode.rowIndex, (e)?e.ctrlKey:window.event.ctrlKey); }
}	};


/*
 * Event handler helpers
 */

WebFXColumnList.prototype._checkHeaderOperation = function(el, x) {
	var prev, next, left, right, l, r;

	/*
	 * Checks if the mouse cursor is near the edge of a header
	 * cell, in that case the cursor is set to 'e-resize' and
	 * the operation is set to COL_HEAD_EDGE, if it's over the
	 * header but not near the edge it's set to COL_HEAD_OVER
	 * and finnaly if it's not over the header it's set to
	 * COL_HEAD_NONE. The operation value is used to trigger
	 * column resize, move and sort commands.
	 */

	if ((el.tagName == 'TD') && (el.parentNode.parentNode.parentNode.parentNode.className == 'webfx-columnlist-head')) {
		if (el.tagName == 'IMG') { el = el.parentNode; }

		prev = el.previousSibling;
		next = el.nextSibling;
		left = getLeftPos(el);
		right = left + el.offsetWidth;
		l = (x - 5) - left;
		r = right - x;

		if ((l < 5) && (prev)) {
			this._eCont.style.cursor = 'e-resize';
			this._headerOper         = COL_HEAD_EDGE;
			this._headerData         = [prev, prev.offsetWidth - 5, x, this._eHeadTable.offsetWidth];
		}
		else if (r < 5) {
			this._eCont.style.cursor = 'e-resize';
			this._headerOper         = COL_HEAD_EDGE;
			this._headerData         = [el, el.offsetWidth - 5, x, this._eHeadTable.offsetWidth];
		}
		else {
			this._eCont.style.cursor = 'default';
			this._headerOper         = COL_HEAD_OVER;
			this._headerData         = [el, el.offsetLeft, x, getLeftPos(this._eHead), el.cellIndex];
	}	}
	else {
		this._eCont.style.cursor = 'default';
		this._headerOper         = COL_HEAD_NONE;
		this._headerData         = null;
}	};


WebFXColumnList.prototype._sizeBodyAccordingToHeader = function() {
	var aCols = this._eBodyTable.getElementsByTagName('colgroup')[0].getElementsByTagName('col');
	var length = aCols.length;
	var bNoScrollbar = ((this._eBody.offsetWidth - this._eBody.clientWidth) == 2);
	this._eBodyTable.style.width = this._eHeadTable.offsetWidth - ((bNoScrollbar)?2:0) + 'px';
	for (var i = 0; i < length; i++) {
		aCols[i].style.width = (this._eHeadCols[i].offsetWidth - ((document.all)?2:0)) - (((bNoScrollbar && i) == (length - 1))?2:0) + 'px';
}	};


/*
 * void moveColumn(iColumnIndex, iNewColumnIndex)
 * Moves column from givin column index to new index.
 */
WebFXColumnList.prototype._moveColumn = function(iCol, iNew) {
	var i, oParent, oCol, oBefore, aRows, a;

	if (iCol == iNew) { return; }

	/* Move header */
	oCol    = this._eHeadCols[iCol];
	oParent = oCol.parentNode;
	if (iNew == this._cols) {
		oParent.removeChild(oCol);
		oParent.appendChild(oCol);
	}
	else {
		oBefore = this._eHeadCols[iNew];
		oParent.removeChild(oCol);
		oParent.insertBefore(oCol, oBefore);
	}

	/* Move cols */
	oCol    = this._eBodyTable.getElementsByTagName('colgroup')[0].getElementsByTagName('col')[iCol];
	oParent = oCol.parentNode;
	if (iNew == this._cols) {
		oParent.removeChild(oCol);
		oParent.appendChild(oCol);
	}
	else {
		oBefore = this._eBodyTable.getElementsByTagName('colgroup')[0].getElementsByTagName('col')[iNew];
		oParent.removeChild(oCol);
		oParent.insertBefore(oCol, oBefore);
	}

	/* Move cells */
	aRows = this._eBodyTable.tBodies[0].rows;
	this._rows = aRows.length;
	for (i = 0; i < this._rows; i++) {
		oCol    = aRows[i].cells[iCol];
		oParent = aRows[i];

		if (iNew == this._cols) {
			oParent.removeChild(oCol);
			oParent.appendChild(oCol);
		}
		else {
			oBefore = aRows[i].cells[iNew];
			oParent.removeChild(oCol);
			oParent.insertBefore(oCol, oBefore);
	}	}

	/* Reorganize column type and sort data */
	a = new Array();
	oCol = this._aColumnTypes[iCol];
	for (i = 0; i < this._aColumnTypes.length; i++) {
		if (i == iCol) { continue; }
		if (i == iNew) { a.push(oCol); }
		a.push(this._aColumnTypes[i]);
	}
	if (iNew == this._aColumnTypes.length - 1) { a.push(oCol); }
	this._aColumnTypes = a;
	this.setSortTypes(a);

	/* If sorted by column, update sortCol property */
	if (iCol == this.sortCol) { this.sortCol = iNew; }
};


/*
 * void _setAlignment()
 * Sets column alignment
 */
WebFXColumnList.prototype._setAlignment = function() {
	var i, aRows, aAlign, j;
	aAlign = new Array();
	for (i = 0; i < this._cols; i++) {
		switch (this._aColumnAlign[i]) {
			case ALIGN_LEFT:   align = 'left';   break;
			case ALIGN_CENTER: align = 'center'; break;
			case ALIGN_RIGHT:  align = 'right';  break;
			case ALIGN_AUTO:
			default:
				switch(this._aColumnTypes[i]) {
					case TYPE_NUMBER: align = 'right'; break;
					default:          align = 'left';
				};
		};
		aAlign.push(align);
	}

	/* Set alignment for headers */
	for (i=0;i<this._cols;i++){
		this._eHeadCols[i].style.textAlign = aAlign[i];
	}

	/*
	 * Set alignment for rows.
	 * IE supports the align property on cols in colgorups. As thats the, by far,
	 * fastest way of setting it, that what we'll use.
	 */
	var aCols = this._eBodyTable.getElementsByTagName('colgroup')[0].getElementsByTagName('col');
	var length = aCols.length;
	if (document.all) {
		for (var i = 0; i < length; i++) {
			aCols[i].align = aAlign[i];
	}	}

	/*
	 * Mozilla does not support the align property, so we'll update the style
	 * sheet rule for each column instead. Still a lot faster than setting the
	 * style text-alignment property for all cells.
	 */
	else {
		var ss = null, rules = null;
		for (var n = 0; n < document.styleSheets.length; n++) {
			if (document.styleSheets[n].href.indexOf('columnlist.css') == -1) { continue; }
			ss    = document.styleSheets[n];
			rules = ss.cssRules;
		}
		if (!rules) { return; }

		if (!this._aColRules) { this._aColRules = new Array(); }
		for (var j = 0; j < length; j++) {
			if (!this._aColRules[j]) {
				for (var i = 0; i < rules.length; i++) {
					if ((rules[i].type == CSSRule.STYLE_RULE) && (rules[i].selectorText == '.webfx-columnlist-col-' + j)) {
						this._aColRules[j] = rules[i];
						break;
			}	}	}
			if (this._aColRules[j]) {
				this._aColRules[j].style.textAlign = aAlign[j];
			}
			else { this._aColRules[j] = ss.insertRule('.webfx-columnlist-col-' + j + ' { text-align: ' + aAlign[j] + '};', 0); }
}	}	};


/*
 * Helper functions
 */

function getLeftPos(_el) {
	var x = 0;
	for (var el = _el; el; el = el.offsetParent) {
		x += el.offsetLeft;
	}
	return x;
}


function compareNumericAsc(n1, n2) {
	if (Number(n1) < Number(n2)) { return -1; }
	if (Number(n1) > Number(n2)) { return 1; }
	return 0;
}


function getInnerText(el) {
	if (document.all) { return el.innerText; }
	var str = '';
	var cs = el.childNodes;
	var l = cs.length;
	for (var i = 0; i < l; i++) {
		switch (cs[i].nodeType) {
			case 1: //ELEMENT_NODE
				str += getInnerText(cs[i]);
				break;
			case 3:	//TEXT_NODE
				str += cs[i].nodeValue;
				break;
	}	}
	return str;
}

