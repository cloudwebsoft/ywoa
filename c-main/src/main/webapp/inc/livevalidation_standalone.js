// LiveValidation 1.3 (standalone version)
// Copyright (c) 2007-2008 Alec Hill (www.livevalidation.com)
// LiveValidation is licensed under the terms of the MIT License

/*********************************************** LiveValidation class ***********************************/

/**
 *	validates a form field in real-time based on validations you assign to it
 *	
 *	@var element {mixed} - either a dom element reference or the string id of the element to validate
 *	@var optionsObj {Object} - general options, see below for details
 *
 *	optionsObj properties:
 *							validMessage {String} 	- the message to show when the field passes validation
 *													  (DEFAULT: "Thankyou!")
 *							onValid {Function} 		- function to execute when field passes validation
 *													  (DEFAULT: function(){ this.insertMessage(this.createMessageSpan()); this.addFieldClass(); } )	
 *							onInvalid {Function} 	- function to execute when field fails validation
 *													  (DEFAULT: function(){ this.insertMessage(this.createMessageSpan()); this.addFieldClass(); })
 *							insertAfterWhatNode {Int} 	- position to insert default message
 *													  (DEFAULT: the field that is being validated)	
 *              onlyOnBlur {Boolean} - whether you want it to validate as you type or only on blur
 *                            (DEFAULT: false)
 *              wait {Integer} - the time you want it to pause from the last keystroke before it validates (ms)
 *                            (DEFAULT: 0)
 *              onlyOnSubmit {Boolean} - whether should be validated only when the form it belongs to is submitted
 *                            (DEFAULT: false)						
 */
var LiveValidation = function(element, optionsObj){
  	this.initialize(element, optionsObj);
}

LiveValidation.liveErrMsg = "";

LiveValidation.VERSION = '1.3 standalone';

/** element types constants ****/

LiveValidation.TEXTAREA 	= 1;
LiveValidation.TEXT 		= 2;
LiveValidation.PASSWORD 	= 3;
LiveValidation.CHECKBOX 	= 4;
LiveValidation.SELECT 		= 5;
LiveValidation.FILE 		= 6;
LiveValidation.RADIO 		= 7;

/****** Static methods *******/

/**
 *	pass an array of LiveValidation objects and it will validate all of them
 *	
 *	@var validations {Array} - an array of LiveValidation objects
 *	@return {Bool} - true if all passed validation, false if any fail
 *  通过下行可以手工判定表单中的域是否合法
 *  LiveValidation.massValidate(groupNameCtl.formObj.fields)			
 */
LiveValidation.massValidate = function(validations){
    LiveValidation.liveErrMsg = "";
	var returnValue = true;
	for(var i = 0, len = validations.length; i < len; ++i ) {
		var valid = validations[i].validate();
		if (!valid) {
		    var el = validations[i].element;
		    var t = el.getAttribute("title");
		    if (t==null) {
		        t = el.getAttribute("name");
            }
            LiveValidation.liveErrMsg += t + " " + validations[i].message + "<br/>";
        }
		if(returnValue) returnValue = valid;
	}
    return returnValue;
}

// 取消验证 fgf 20140111
LiveValidation.cancelValidate = function(validations){
	for(var i = 0, len = validations.length; i < len; ++i ){
		validations[i].formObj.removeField(validations[i]);
	}
}

// 恢复验证 fgf 20140111
LiveValidation.restoreValidate = function(validations){
	for(var i = 0, len = validations.length; i < len; ++i ){
		validations[i].formObj.addField(validations[i]);
	}
}

// 销毁验证 hw 20160616
LiveValidation.destroyValidate = function(validations){
	for(var i = 0, len = validations.length; i < len; ++i ){
		validations[i].formObj.destroy();
		validations[i].destroy();
	}
}

/****** prototype ******/

LiveValidation.prototype = {

    validClass: 'LV_valid',
    invalidClass: 'LV_invalid',
    messageClass: 'LV_validation_message',
    validFieldClass: 'LV_valid_field',
    invalidFieldClass: 'LV_invalid_field',
	
	getFields: function() {
		return "he";
	},

    /**
     *	initialises all of the properties and events
     *
     * @var - Same as constructor above
     */
    initialize: function(element, optionsObj){
      var self = this;
      if(!element) throw new Error("LiveValidation::initialize - No element reference or element id has been provided!");
      this.element = element.nodeName ? element : document.getElementById(element);
      if(!this.element) {
		 var el = document.getElementsByName(element);
		 if (el!=null) {
			this.element = el[0];
		 }
		 if (!this.element)
		  	return; // throw new Error("LiveValidation::initialize - No element with reference or id of '" + element + "' exists!");
	  }
      // default properties that could not be initialised above
      this.validations = [];
      this.elementType = this.getElementType();
      this.form = this.element.form;
      // options
    	var options = optionsObj || {};
    	this.validMessage = options.validMessage || "";
    	var node = options.insertAfterWhatNode || this.element;
    	// 如果是radio，则用其父节点作为insertAfterWhatNode
    	if (node.getAttribute("type")=="radio") {
            node = node.parentNode;
    	}
		this.insertAfterWhatNode = node.nodeType ? node : document.getElementById(node);
      this.onValid = options.onValid || function(){ this.insertMessage(this.createMessageSpan()); this.addFieldClass(); };
      this.onInvalid = options.onInvalid || function(){ this.insertMessage(this.createMessageSpan()); this.addFieldClass(); };	
    	this.onlyOnBlur =  options.onlyOnBlur || false;
    	this.wait = options.wait || 0;
      this.onlyOnSubmit = options.onlyOnSubmit || false;
      // add to form if it has been provided
      if(this.form){
        this.formObj = LiveValidationForm.getInstance(this.form);
        this.formObj.addField(this);
      }
      // events
      // collect old events
      this.oldOnFocus = this.element.onfocus || function(){};
      this.oldOnBlur = this.element.onblur || function(){};
      this.oldOnClick = this.element.onclick || function(){};
      this.oldOnChange = this.element.onchange || function(){};
      this.oldOnKeyup = this.element.onkeyup || function(){};
      this.element.onfocus = function(e){ self.doOnFocus(e); return self.oldOnFocus.call(this, e); }
      if(!this.onlyOnSubmit){
        switch(this.elementType){
          case LiveValidation.RADIO:
              // 使radio组内的其它元素也能响应点击事件进行验证
              var radioboxs = document.getElementsByName(this.element.name);
              for (i=0; i<radioboxs.length; i++) {
                  if (radioboxs[i].type=="radio") {
                      radioboxs[i].onclick = function(e) {self.validate(); return self.oldOnChange.call(this, e); };
                  }
              }
              break;
          case LiveValidation.CHECKBOX:
            this.element.onclick = function(e){ self.validate(); return self.oldOnClick.call(this, e); }
          // let it run into the next to add a change event too
          case LiveValidation.SELECT:
          case LiveValidation.FILE:
            this.element.onchange = function(e){ self.validate(); return self.oldOnChange.call(this, e); }
            break;
          default:
            if(!this.onlyOnBlur) this.element.onkeyup = function(e){ self.deferValidation(); return self.oldOnKeyup.call(this, e); }
      	    this.element.onblur = function(e){ self.doOnBlur(e); return self.oldOnBlur.call(this, e); }
        }
      }
    },
	
	/**
     *	destroys the instance's events (restoring previous ones) and removes it from any LiveValidationForms
     */
    destroy: function(){
  	  if(this.formObj){
		// remove the field from the LiveValidationForm
		this.formObj.removeField(this);
		// destroy the LiveValidationForm if no LiveValidation fields left in it
		this.formObj.destroy();
	  }
      // remove events - set them back to the previous events
	  this.element.onfocus = this.oldOnFocus;
      if(!this.onlyOnSubmit){
        switch(this.elementType){
          case LiveValidation.CHECKBOX:
            this.element.onclick = this.oldOnClick;
          // let it run into the next to add a change event too
          case LiveValidation.SELECT:
          case LiveValidation.FILE:
            this.element.onchange = this.oldOnChange;
            break;
          default:
            if(!this.onlyOnBlur) this.element.onkeyup = this.oldOnKeyup;
      	    this.element.onblur = this.oldOnBlur;
        }
      }
      this.validations = [];
	  this.removeMessageAndFieldClass();
    },
    
    /**
     *	adds a validation to perform to a LiveValidation object
     *
     *	@var validationFunction {Function} - validation function to be used (ie Validate.Presence )
     *	@var validationParamsObj {Object} - parameters for doing the validation, if wanted or necessary
     * @return {Object} - the LiveValidation object itself so that calls can be chained
     */
    add: function(validationFunction, validationParamsObj){
	  if (!this.validations)
	  	return null;
		
		if (validationFunction==Validate.Presence) {
    	    var span = document.createElement('span');
			var textNode = document.createTextNode("*");
			span.appendChild(textNode);
			span.className = "LV_presence";
			if (this.element.type!="hidden") {
				if(this.insertAfterWhatNode.nextSibling){
					this.insertAfterWhatNode.parentNode.insertBefore(span, this.insertAfterWhatNode.nextSibling);
				}else{
					this.insertAfterWhatNode.parentNode.appendChild(span);
				}
			}
	  }
	  		
      this.validations.push( {type: validationFunction, params: validationParamsObj || {} } );
      return this;
    },
    
	/**
     *	removes a validation from a LiveValidation object - must have exactly the same arguments as used to add it 
     *
     *	@var validationFunction {Function} - validation function to be used (ie Validate.Presence )
     *	@var validationParamsObj {Object} - parameters for doing the validation, if wanted or necessary
     * @return {Object} - the LiveValidation object itself so that calls can be chained
     */
    remove: function(validationFunction, validationParamsObj){
	  var found = false;
	  for( var i = 0, len = this.validations.length; i < len; i++ ){
	  		if( this.validations[i].type == validationFunction ){
				if (this.validations[i].params == validationParamsObj) {
					found = true;
					break;
				}
			}
	  }
      if(found) this.validations.splice(i,1);
      return this;
    },
	
    /**
     * makes the validation wait the alotted time from the last keystroke
     */
    deferValidation: function(e){
      if(this.wait >= 300) this.removeMessageAndFieldClass();
    	var self = this;
      if(this.timeout) clearTimeout(self.timeout);
      this.timeout = setTimeout( function(){ self.validate() }, self.wait); 
    },
        
    /**
     * sets the focused flag to false when field loses focus 
     */
    doOnBlur: function(e){
      this.focused = false;
      this.validate(e);
    },
        
    /**
     * sets the focused flag to true when field gains focus 
     */
    doOnFocus: function(e){
      this.focused = true;
      this.removeMessageAndFieldClass();
    },
    
    /**
     *	gets the type of element, to check whether it is compatible
     *
     *	@var validationFunction {Function} - validation function to be used (ie Validate.Presence )
     *	@var validationParamsObj {Object} - parameters for doing the validation, if wanted or necessary
     */
    getElementType: function(){
      switch(true){
        case (this.element.nodeName.toUpperCase() == 'TEXTAREA'):
        return LiveValidation.TEXTAREA;
      case (this.element.nodeName.toUpperCase() == 'INPUT' && this.element.type.toUpperCase() == 'TEXT'):
        return LiveValidation.TEXT;
      case (this.element.nodeName.toUpperCase() == 'INPUT' && this.element.type.toUpperCase() == 'PASSWORD'):
        return LiveValidation.PASSWORD;
      case (this.element.nodeName.toUpperCase() == 'INPUT' && this.element.type.toUpperCase() == 'CHECKBOX'):
        return LiveValidation.CHECKBOX;
      case (this.element.nodeName.toUpperCase() == 'INPUT' && this.element.type.toUpperCase() == 'FILE'):
        return LiveValidation.FILE;
      case (this.element.nodeName.toUpperCase() == 'INPUT' && this.element.type.toUpperCase() == 'RADIO'):
        return LiveValidation.RADIO;
      case (this.element.nodeName.toUpperCase() == 'SELECT'):
        return LiveValidation.SELECT;
        case (this.element.nodeName.toUpperCase() == 'INPUT'):
        	; // throw new Error('LiveValidation::getElementType - Cannot use LiveValidation on an ' + this.element.type + ' input!');
        default:
        	; // throw new Error('LiveValidation::getElementType - Element must be an input, select, or textarea!');
      }
    },
    
    /**
     *	loops through all the validations added to the LiveValidation object and checks them one by one
     *
     *	@var validationFunction {Function} - validation function to be used (ie Validate.Presence )
     *	@var validationParamsObj {Object} - parameters for doing the validation, if wanted or necessary
     * @return {Boolean} - whether the all the validations passed or if one failed
     */
    doValidations: function(){
      	this.validationFailed = false;
      	for(var i = 0, len = this.validations.length; i < len; ++i){
    	 	var validation = this.validations[i];
    		switch(validation.type){
    		    case Validate.Presence:
                case Validate.Confirmation:
                case Validate.Acceptance:
    		   		this.displayMessageWhenEmpty = true;
    		   		this.validationFailed = !this.validateElement(validation.type, validation.params); 
    				break;
    		   	default:
    		   		this.validationFailed = !this.validateElement(validation.type, validation.params);
    		   		break;
    		}
    		if(this.validationFailed) return false;	
    	}
    	this.message = this.validMessage;
    	return true;
    },
    
    /**
     *	performs validation on the element and handles any error (validation or otherwise) it throws up
     *
     *	@var validationFunction {Function} - validation function to be used (ie Validate.Presence )
     *	@var validationParamsObj {Object} - parameters for doing the validation, if wanted or necessary
     * @return {Boolean} - whether the validation has passed or failed
     */
    validateElement: function(validationFunction, validationParamsObj){
		// alert(this.element.name + " " + this.elementType + " " + LiveValidation.SELECT + " index=" + this.element.selectedIndex);
      	var value = "";
		if (this.elementType == LiveValidation.SELECT) {
			if (this.element.selectedIndex!=-1)
				value = this.element.options[this.element.selectedIndex].value;
		}
		else if (this.elementType == LiveValidation.RADIO) {
		    value = getRadioValue(this.element.name);
        }
		else
			value = this.element.value;
        if(validationFunction == Validate.Acceptance){
    	    if(this.elementType != LiveValidation.CHECKBOX) throw new Error('LiveValidation::validateElement - Element to validate acceptance must be a checkbox!');
    		value = this.element.checked;
    	}
        var isValid = true;
      	try{    
    		validationFunction(value, validationParamsObj);
    	} catch(e) {
    	  	if(e instanceof Validate.Error){
    			if( value !== '' || (value === '' && this.displayMessageWhenEmpty) ){
    				this.validationFailed = true;
    				this.message = e.message;
    				isValid = false;
    			}
    		}else{
    		  	//throw e;
    		}
    	}

		// finally{
    	    return isValid;
        // }
    },
    
    /**
     *	makes it do the all the validations and fires off the onValid or onInvalid callbacks
     *
     * @return {Boolean} - whether the all the validations passed or if one failed
     */
    validate: function(){
      if(!this.element.disabled){
		var isValid = this.doValidations();
		if(isValid){
			this.onValid();
			return true;
		}else {
			this.onInvalid();
			return false;
		}
	  }else{
      return true;
    }
    },
	
 /**
   *  enables the field
   *
   *  @return {LiveValidation} - the LiveValidation object for chaining
   */
  enable: function(){
  	this.element.disabled = false;
	return this;
  },
  
  /**
   *  disables the field and removes any message and styles associated with the field
   *
   *  @return {LiveValidation} - the LiveValidation object for chaining
   */
  disable: function(){
  	this.element.disabled = true;
	this.removeMessageAndFieldClass();
	return this;
  },
    
    /** Message insertion methods ****************************
     * 
     * These are only used in the onValid and onInvalid callback functions and so if you overide the default callbacks,
     * you must either impliment your own functions to do whatever you want, or call some of these from them if you 
     * want to keep some of the functionality
     */
    
    /**
     *	makes a span containg the passed or failed message
     *
     * @return {HTMLSpanObject} - a span element with the message in it
     */
    createMessageSpan: function(){
        var span = document.createElement('span');
		if (this.message.indexOf("img_")==0) {
			var imgNode = document.createElement("img");
			imgNode.src = this.message.substring(4);
			span.appendChild(imgNode);
		}
		else {
			var textNode = document.createTextNode(this.message);
			span.appendChild(textNode);
		}
        return span;
    },
    
    /**
     *	inserts the element containing the message in place of the element that already exists (if it does)
     *
     * @var elementToIsert {HTMLElementObject} - an element node to insert
     */
    insertMessage: function(elementToInsert){
      	this.removeMessage();
      	if( (this.displayMessageWhenEmpty && (this.elementType == LiveValidation.CHECKBOX || this.element.value == ''))
    	  || this.element.value != '' ){
            var className = this.validationFailed ? this.invalidClass : this.validClass;
    	  	elementToInsert.className += ' ' + this.messageClass + ' ' + className;
			try {
				if(this.insertAfterWhatNode.nextSibling){
					  this.insertAfterWhatNode.parentNode.insertBefore(elementToInsert, this.insertAfterWhatNode.nextSibling);
				}else{
					  this.insertAfterWhatNode.parentNode.appendChild(elementToInsert);
				}
			}
			catch (e) {}
    	}
    },
    
    /**
     *	changes the class of the field based on whether it is valid or not
     */
    addFieldClass: function(){
        this.removeFieldClass();
        if(!this.validationFailed){
            if(this.displayMessageWhenEmpty || this.element.value != ''){
                if(this.element.className.indexOf(this.validFieldClass) == -1) this.element.className += ' ' + this.validFieldClass;
            }
        }else{
            if(this.element.className.indexOf(this.invalidFieldClass) == -1) this.element.className += ' ' + this.invalidFieldClass;
        }
    },
    
    /**
     *	removes the message element if it exists, so that the new message will replace it
     */
    removeMessage: function(){
    	var nextEl;
    	var el = this.insertAfterWhatNode;
    	while(el.nextSibling){
    	    if(el.nextSibling.nodeType === 1){
    		  	nextEl = el.nextSibling;
    		  	break;
    		}
    		el = el.nextSibling;
    	}
      	if(nextEl && nextEl.className.indexOf(this.messageClass) != -1) this.insertAfterWhatNode.parentNode.removeChild(nextEl);
    },
    
    /**
     *	removes the class that has been applied to the field to indicte if valid or not
     */
    removeFieldClass: function(){
      if(this.element.className.indexOf(this.invalidFieldClass) != -1) this.element.className = this.element.className.split(this.invalidFieldClass).join('');
      if(this.element.className.indexOf(this.validFieldClass) != -1) this.element.className = this.element.className.split(this.validFieldClass).join(' ');
    },
        
    /**
     *	removes the message and the field class
     */
    removeMessageAndFieldClass: function(){
      this.removeMessage();
      this.removeFieldClass();
    }

} // end of LiveValidation class

/*************************************** LiveValidationForm class ****************************************/
/**
 * This class is used internally by LiveValidation class to associate a LiveValidation field with a form it is icontained in one
 * 
 * It will therefore not really ever be needed to be used directly by the developer, unless they want to associate a LiveValidation 
 * field with a form that it is not a child of
 */

/**
   *	handles validation of LiveValidation fields belonging to this form on its submittal
   *	
   *	@var element {HTMLFormElement} - a dom element reference to the form to turn into a LiveValidationForm
   */
var LiveValidationForm = function(element){
  this.initialize(element);
}

/**
 * namespace to hold instances
 */
LiveValidationForm.instances = {};

/**
   *	gets the instance of the LiveValidationForm if it has already been made or creates it if it doesnt exist
   *	
   *	@var element {HTMLFormElement} - a dom element reference to a form
   */
LiveValidationForm.getInstance = function(element){
  var rand = Math.random() * Math.random();
  if(!element.getAttribute('id')) element.setAttribute('id', 'formId_' + rand.toString().replace(/\./, '') + new Date().valueOf());
  if(!LiveValidationForm.instances[element.getAttribute('id')]) LiveValidationForm.instances[element.getAttribute('id')] = new LiveValidationForm(element);
  return LiveValidationForm.instances[element.getAttribute('id')];
}

LiveValidationForm.prototype = {
  
  /**
   *	constructor for LiveValidationForm - handles validation of LiveValidation fields belonging to this form on its submittal
   *	
   *	@var element {HTMLFormElement} - a dom element reference to the form to turn into a LiveValidationForm
   */
  initialize: function(element){
  	this.name = element.id;
    this.element = element;
    this.fields = [];
    // preserve the old onsubmit event
	this.oldOnSubmit = this.element.onsubmit || function(){};
    var self = this;
    this.element.onsubmit = function(e){
      return (LiveValidation.massValidate(self.fields)) ? self.oldOnSubmit.call(this, e || window.event) !== false : false;
    }
  },
  
  /**
   *	adds a LiveValidation field to the forms fields array
   *	
   *	@var element {LiveValidation} - a LiveValidation object
   */
  addField: function(newField){
    this.fields.push(newField);
  },
  
  /**
   *	removes a LiveValidation field from the forms fields array
   *	
   *	@var victim {LiveValidation} - a LiveValidation object
   */
  removeField: function(victim){
  	var victimless = [];
  	for( var i = 0, len = this.fields.length; i < len; i++){
		if(this.fields[i] !== victim) victimless.push(this.fields[i]);
	}
    this.fields = victimless;
  },
  
  removeFieldByName: function(victim){
	  	var victimless = [];
	  	for( var i = 0, len = this.fields.length; i < len; i++){
			if(this.fields[i].element.name != victim) victimless.push(this.fields[i]);
		}
	    this.fields = victimless;
	    // 去除*
	    var ary = document.getElementsByName(victim);
	    var el;
	    if (ary && ary.length>0) {
	    	el = ary[0];
	    }
	    else {
	    	return;
	    }
	    var nel = el.parentNode.nextSbiling;
	    if (nel) {
		    if (nel.innerText=="*") {
		    	nel.innerText = ""
		    }
	    }
  },
  
  /**
   *	destroy this instance and its events
   *
   * @var force {Boolean} - whether to force the detruction even if there are fields still associated
   */
  destroy: function(force){
  	// only destroy if has no fields and not being forced
  	if (this.fields.length != 0 && !force) return false;
	// remove events - set back to previous events
	this.element.onsubmit = this.oldOnSubmit;
	// remove from the instances namespace
	LiveValidationForm.instances[this.name] = null;
	return true;
  }
   
}// end of LiveValidationForm prototype

/*************************************** Validate class ****************************************/
/**
 * This class contains all the methods needed for doing the actual validation itself
 *
 * All methods are static so that they can be used outside the context of a form field
 * as they could be useful for validating stuff anywhere you want really
 *
 * All of them will return true if the validation is successful, but will raise a ValidationError if
 * they fail, so that this can be caught and the message explaining the error can be accessed ( as just 
 * returning false would leave you a bit in the dark as to why it failed )
 *
 * Can use validation methods alone and wrap in a try..catch statement yourself if you want to access the failure
 * message and handle the error, or use the Validate::now method if you just want true or false
 */

var Validate = {

    /**
     *	validates that the field has been filled in
     *
     *	@var value {mixed} - value to be checked
     *	@var paramsObj {Object} - parameters for this particular validation, see below for details
     *
     *	paramsObj properties:
     *							failureMessage {String} - the message to show when the field fails validation 
     *													  (DEFAULT: "Can't be empty!")
     */
    Presence: function(value, paramsObj){
      	var paramsObj = paramsObj || {};
    	var message = paramsObj.failureMessage || "不能为空!";
    	if(value === '' || value === null || value === undefined){ 
    	  	Validate.fail(message);
    	}
    	return true;
    },
    
    /**
     *	validates that the value is numeric, does not fall within a given range of numbers
     *	
     *	@var value {mixed} - value to be checked
     *	@var paramsObj {Object} - parameters for this particular validation, see below for details
     *
     *	paramsObj properties:
     *							notANumberMessage {String} - the message to show when the validation fails when value is not a number
     *													  	  (DEFAULT: "Must be a number!")
     *							notAnIntegerMessage {String} - the message to show when the validation fails when value is not an integer
     *													  	  (DEFAULT: "Must be a number!")
     *							wrongNumberMessage {String} - the message to show when the validation fails when is param is used
     *													  	  (DEFAULT: "Must be {is}!")
     *							tooLowMessage {String} 		- the message to show when the validation fails when minimum param is used
     *													  	  (DEFAULT: "Must not be less than {minimum}!")
     *							tooHighMessage {String} 	- the message to show when the validation fails when maximum param is used
     *													  	  (DEFAULT: "Must not be more than {maximum}!")
     *							is {Int} 					- the length must be this long 
     *							minimum {Int} 				- the minimum length allowed
     *							maximum {Int} 				- the maximum length allowed
     *                         onlyInteger {Boolean} - if true will only allow integers to be valid
     *                                                             (DEFAULT: false)
     *
     *  NB. can be checked if it is within a range by specifying both a minimum and a maximum
     *  NB. will evaluate numbers represented in scientific form (ie 2e10) correctly as numbers				
     */
    Numericality: function(value, paramsObj){
        var suppliedValue = value;

        // 去掉千分位标志
        value = ('' + value).replaceAll(',', '');

        var value = Number(value);
    	var paramsObj = paramsObj || {};
        var minimum = ((paramsObj.minimum) || (paramsObj.minimum == 0)) ? paramsObj.minimum : null;;
        var maximum = ((paramsObj.maximum) || (paramsObj.maximum == 0)) ? paramsObj.maximum : null;
    	var is = ((paramsObj.is) || (paramsObj.is == 0)) ? paramsObj.is : null;
    	//numeric can not begin with zero modify by jfy 20150604
    	var canNotBeginWithZero =  paramsObj.canNotBeginWithZero || "不能以0开头!";
        if (suppliedValue.substr(0,1) == '0' && suppliedValue.substr(0,2) != '0.'&& suppliedValue.length > 1) Validate.fail(canNotBeginWithZero);
        var notANumberMessage = paramsObj.notANumberMessage || "必须是数字!";
        var notAnIntegerMessage = paramsObj.notAnIntegerMessage || "必须是整数!";
    	var wrongNumberMessage = paramsObj.wrongNumberMessage || "必须是 " + is + "!";
    	var tooLowMessage = paramsObj.tooLowMessage || "不能小于 " + minimum + "!";
    	var tooHighMessage = paramsObj.tooHighMessage || "不能大于 " + maximum + "!";
        if (!isFinite(value)) Validate.fail(notANumberMessage);
        if (paramsObj.onlyInteger && (/\.0+$|\.$/.test(String(suppliedValue))  || value != parseInt(value)) ) Validate.fail(notAnIntegerMessage);
    	switch(true){
    	  	case (is !== null):
    	  		if( value != Number(is) ) Validate.fail(wrongNumberMessage);
    			break;
    	  	case (minimum !== null && maximum !== null):
    	  		Validate.Numericality(value, {tooLowMessage: tooLowMessage, minimum: minimum});
    	  		Validate.Numericality(value, {tooHighMessage: tooHighMessage, maximum: maximum});
    	  		break;
    	  	case (minimum !== null):
    	  		if( value < Number(minimum) ) Validate.fail(tooLowMessage);
    			break;
    	  	case (maximum !== null):
    	  		if( value > Number(maximum) ) Validate.fail(tooHighMessage);
    			break;
    	}
    	return true;
    },
    
    /**
     *	validates against a RegExp pattern
     *	
     *	@var value {mixed} - value to be checked
     *	@var paramsObj {Object} - parameters for this particular validation, see below for details
     *
     *	paramsObj properties:
     *							failureMessage {String} - the message to show when the field fails validation
     *													  (DEFAULT: "Not valid!")
     *							pattern {RegExp} 		- the regular expression pattern
     *													  (DEFAULT: /./)
     *             negate {Boolean} - if set to true, will validate true if the pattern is not matched
   *                           (DEFAULT: false)
     *
     *  NB. will return true for an empty string, to allow for non-required, empty fields to validate.
     *		If you do not want this to be the case then you must either add a LiveValidation.PRESENCE validation
     *		or build it into the regular expression pattern
     */
    Format: function(value, paramsObj){
		var value = String(value);
		var paramsObj = paramsObj || {};
		var message = paramsObj.failureMessage || "无效!";
		var pattern = paramsObj.pattern || /./;
		var negate = paramsObj.negate || false;
		if(!negate && !pattern.test(value)) Validate.fail(message); // normal
		if(negate && pattern.test(value)) Validate.fail(message); // negated
			return true;
    },
    
    /**
     *	validates that the field contains a valid email address
     *	
     *	@var value {mixed} - value to be checked
     *	@var paramsObj {Object} - parameters for this particular validation, see below for details
     *
     *	paramsObj properties:
     *							failureMessage {String} - the message to show when the field fails validation
     *													  (DEFAULT: "Must be a number!" or "Must be an integer!")
     */
    Email: function(value, paramsObj){
    	var paramsObj = paramsObj || {};
    	var message = paramsObj.failureMessage || "Email地址无效！";
    	Validate.Format(value, { failureMessage: message, pattern: /^([^@\s]+)@((?:[-a-z0-9]+\.)+[a-z]{2,})$/i } );
    	return true;
    },
	
    Mobile: function(value, paramsObj){
    	var paramsObj = paramsObj || {};
    	var message = paramsObj.failureMessage || "号码无效！";
		
    	Validate.Format(value, { failureMessage: message, pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/i } );
    	return true;
    },
	
	Date: function(value, paramsObj){
    	var ary = value.trim().split(' ');
    	if (ary.length == 1) {
			if (isDateValid(value) == false) {
				var message = paramsObj.failureMessage || "日期格式错误！";
				Validate.fail(message);
				return false;   
			}
	    	return true;
    	} else if (ary.length == 2) {
    		if (isDateValid(ary[0]) == false) {
				var message = paramsObj.failureMessage || "日期格式错误！";
				Validate.fail(message);
				return false;   
			}
    		if (isTimeValid(ary[1]) == false) {
				var message = paramsObj.failureMessage || "时间格式错误！";
				Validate.fail(message);
				return false;   
			}
	    	return true;
    	} else {
    		return false;
    	}
    },
	
    isNotCN: function(value, paramsObj){
		if (isChinese(value)) {
			var paramsObj = paramsObj || {};
			var message = paramsObj.failureMessage || "不能为中文！";
			Validate.fail(message);
			return false;   
		}
    	return true;
    },
    
    isCN: function(value, paramsObj){
    	var reg = /^[\u4E00-\u9FA5]*$/gi;   /*只允许中文*/
    	var re = reg.test(value); 	
		if (re) {
			return true;   
		}
		else {
			var paramsObj = paramsObj || {};
			var message = paramsObj.failureMessage || "只能填中文！";
			Validate.fail(message);			
	    	return false;
		}
    },    
	
    isSQLInjection: function(value, paramsObj){
		if (sql_inj(value)) {
			var paramsObj = paramsObj || {};
			var message = paramsObj.failureMessage || "不能含SQL注入信息！";
			Validate.fail(message);
			return false;   
		}
    	return true;
    },	
	
	IdCardNo: function(value, paramsObj){
		 num = value;
		 var factorArr = new Array(7,9,10,5,8,4,2,1,6,3,7,9,10,5,8,4,2,1);   
		 var error;
		 var varArray = new Array();
		 var intValue;
		 var lngProduct = 0;   
		 var intCheckDigit;   
		 var intStrLen = num.length;
		 var idNumber = num;       
		 // initialize   
		 if ((intStrLen != 15) && (intStrLen != 18)) {   
			 Validate.fail("输入身份证号码长度不对！");
			 return false;   
		 }       
		 // check and set value   
		 for(i=0;i<intStrLen;i++) {   
			varArray[i] = idNumber.charAt(i);   
			if ((varArray[i] < '0' || varArray[i] > '9') && (i != 17)) {   
				 Validate.fail("错误的身份证号码！");   
				return false;   
			 } else if (i < 17) {   
				 varArray[i] = varArray[i]*factorArr[i];   
			 }   
		 }
		 if (intStrLen == 18) {   
			//check date   
			 var date8 = idNumber.substring(6,14);   
			 if (checkDate(date8) == false) {  
				 Validate.fail("身份证中日期信息不正确！");
				 return false;   
			 }           
			 // calculate the sum of the products   
			 for(i=0;i<17;i++) {   
				 lngProduct = lngProduct + varArray[i];   
			 }           
			 // calculate the check digit   
			 intCheckDigit = 12 - lngProduct % 11;   
			 switch (intCheckDigit) {   
				 case 10:   
					 intCheckDigit = 'X';   
					 break;   
				 case 11:   
					 intCheckDigit = 0;   
					 break;   
				 case 12:   
					 intCheckDigit = 1;   
					 break;   
			 }           
			 // check last digit   
			 if (varArray[17].toUpperCase() != intCheckDigit) {   
				 Validate.fail("身份证效验位错误!"); // 正确为： " + intCheckDigit);
				return false;   
			 }   
		 }    
		 else{	//length is 15   		 
			 //check date   
			 var date6 = idNumber.substring(6,12);   
			 if (checkDate(date6) == false) {   
				Validate.fail("身份证日期信息有误！");
				 return false;   
			 }   
		}   
		
    	return true;
    },	
	
	
    
    /**
     *	validates the length of the value
     *	
     *	@var value {mixed} - value to be checked
     *	@var paramsObj {Object} - parameters for this particular validation, see below for details
     *
     *	paramsObj properties:
     *							wrongLengthMessage {String} - the message to show when the fails when is param is used
     *													  	  (DEFAULT: "Must be {is} characters long!")
     *							tooShortMessage {String} 	- the message to show when the fails when minimum param is used
     *													  	  (DEFAULT: "Must not be less than {minimum} characters long!")
     *							tooLongMessage {String} 	- the message to show when the fails when maximum param is used
     *													  	  (DEFAULT: "Must not be more than {maximum} characters long!")
     *							is {Int} 					- the length must be this long 
     *							minimum {Int} 				- the minimum length allowed
     *							maximum {Int} 				- the maximum length allowed
     *
     *  NB. can be checked if it is within a range by specifying both a minimum and a maximum				
     */
    Length: function(value, paramsObj){
    	var value = String(value);
    	var paramsObj = paramsObj || {};
        var minimum = ((paramsObj.minimum) || (paramsObj.minimum == 0)) ? paramsObj.minimum : null;
    	var maximum = ((paramsObj.maximum) || (paramsObj.maximum == 0)) ? paramsObj.maximum : null;
    	var is = ((paramsObj.is) || (paramsObj.is == 0)) ? paramsObj.is : null;
        var wrongLengthMessage = paramsObj.wrongLengthMessage || "必须是 " + is + " 字符长度!";
    	var tooShortMessage = paramsObj.tooShortMessage || "不能小于 " + minimum + " 字符长度!";
    	var tooLongMessage = paramsObj.tooLongMessage || "不能大于 " + maximum + " 字符长度!";
    	switch(true){
    	  	case (is !== null):
    	  		if( value.length != Number(is) ) Validate.fail(wrongLengthMessage);
    			break;
    	  	case (minimum !== null && maximum !== null):
    	  		Validate.Length(value, {tooShortMessage: tooShortMessage, minimum: minimum});
    	  		Validate.Length(value, {tooLongMessage: tooLongMessage, maximum: maximum});
    	  		break;
    	  	case (minimum !== null):
    	  		if( value.length < Number(minimum) ) Validate.fail(tooShortMessage);
    			break;
    	  	case (maximum !== null):
    	  		if( value.length > Number(maximum) ) Validate.fail(tooLongMessage);
    			break;
    		default:
    			throw new Error("Validate::Length - Length(s) to validate against must be provided!");
    	}
    	return true;
    },
    
    /**
     *	validates that the value falls within a given set of values
     *	
     *	@var value {mixed} - value to be checked
     *	@var paramsObj {Object} - parameters for this particular validation, see below for details
     *
     *	paramsObj properties:
     *							failureMessage {String} - the message to show when the field fails validation
     *													  (DEFAULT: "Must be included in the list!")
     *							within {Array} 			- an array of values that the value should fall in 
     *													  (DEFAULT: [])	
     *							allowNull {Bool} 		- if true, and a null value is passed in, validates as true
     *													  (DEFAULT: false)
     *             partialMatch {Bool} 	- if true, will not only validate against the whole value to check but also if it is a substring of the value 
     *													  (DEFAULT: false)
     *             caseSensitive {Bool} - if false will compare strings case insensitively
     *                          (DEFAULT: true)
     *             negate {Bool} 		- if true, will validate that the value is not within the given set of values
     *													  (DEFAULT: false)			
     */
    Inclusion: function(value, paramsObj){
    	var paramsObj = paramsObj || {};
    	var message = paramsObj.failureMessage || "必须包含在列表里!";
      var caseSensitive = (paramsObj.caseSensitive === false) ? false : true;
    	if(paramsObj.allowNull && value == null) return true;
      if(!paramsObj.allowNull && value == null) Validate.fail(message);
    	var within = paramsObj.within || [];
      //if case insensitive, make all strings in the array lowercase, and the value too
      if(!caseSensitive){ 
        var lowerWithin = [];
        for(var j = 0, length = within.length; j < length; ++j){
        	var item = within[j];
          if(typeof item == 'string') item = item.toLowerCase();
          lowerWithin.push(item);
        }
        within = lowerWithin;
        if(typeof value == 'string') value = value.toLowerCase();
      }
    	var found = false;
    	for(var i = 0, length = within.length; i < length; ++i){
    	  if(within[i] == value) found = true;
        if(paramsObj.partialMatch){ 
          if(value.indexOf(within[i]) != -1) found = true;
        }
    	}
    	if( (!paramsObj.negate && !found) || (paramsObj.negate && found) ) Validate.fail(message);
    	return true;
    },
    
    /**
     *	validates that the value does not fall within a given set of values
     *	
     *	@var value {mixed} - value to be checked
     *	@var paramsObj {Object} - parameters for this particular validation, see below for details
     *
     *	paramsObj properties:
     *							failureMessage {String} - the message to show when the field fails validation
     *													  (DEFAULT: "Must not be included in the list!")
     *							within {Array} 			- an array of values that the value should not fall in 
     *													  (DEFAULT: [])
     *							allowNull {Bool} 		- if true, and a null value is passed in, validates as true
     *													  (DEFAULT: false)
     *             partialMatch {Bool} 	- if true, will not only validate against the whole value to check but also if it is a substring of the value 
     *													  (DEFAULT: false)
     *             caseSensitive {Bool} - if false will compare strings case insensitively
     *                          (DEFAULT: true)			
     */
    Exclusion: function(value, paramsObj){
      var paramsObj = paramsObj || {};
    	paramsObj.failureMessage = paramsObj.failureMessage || "不能被包含在列表里";
      paramsObj.negate = true;
    	Validate.Inclusion(value, paramsObj);
      return true;
    },
    
    /**
     *	validates that the value matches that in another field
     *	
     *	@var value {mixed} - value to be checked
     *	@var paramsObj {Object} - parameters for this particular validation, see below for details
     *
     *	paramsObj properties:
     *							failureMessage {String} - the message to show when the field fails validation
     *													  (DEFAULT: "Does not match!")
     *							match {String} 			- id of the field that this one should match						
     */
    Confirmation: function(value, paramsObj){
      	if(!paramsObj.match) throw new Error("Validate::Confirmation - Error validating confirmation: Id of element to match must be provided!");
    	var paramsObj = paramsObj || {};
    	var message = paramsObj.failureMessage || "不匹配!";
    	var match = paramsObj.match.nodeName ? paramsObj.match : document.getElementById(paramsObj.match);
    	if(!match) throw new Error("Validate::Confirmation - There is no reference with name of, or element with id of '" + paramsObj.match + "'!");
    	if(value != match.value){ 
    	  	Validate.fail(message);
    	}
    	return true;
    },
    
    /**
     *	validates that the value is true (for use primarily in detemining if a checkbox has been checked)
     *	
     *	@var value {mixed} - value to be checked if true or not (usually a boolean from the checked value of a checkbox)
     *	@var paramsObj {Object} - parameters for this particular validation, see below for details
     *
     *	paramsObj properties:
     *							failureMessage {String} - the message to show when the field fails validation 
     *													  (DEFAULT: "Must be accepted!")
     */
    Acceptance: function(value, paramsObj){
      	var paramsObj = paramsObj || {};
    	var message = paramsObj.failureMessage || "必须选中checkbox!";
    	if(!value){ 
    	  	Validate.fail(message);
    	}
    	return true;
    },
    
	 /**
     *	validates against a custom function that returns true or false (or throws a Validate.Error) when passed the value
     *	
     *	@var value {mixed} - value to be checked
     *	@var paramsObj {Object} - parameters for this particular validation, see below for details
     *
     *	paramsObj properties:
     *							failureMessage {String} - the message to show when the field fails validation
     *													  (DEFAULT: "Not valid!")
     *							against {Function} 			- a function that will take the value and object of arguments and return true or false 
     *													  (DEFAULT: function(){ return true; })
     *							args {Object} 		- an object of named arguments that will be passed to the custom function so are accessible through this object within it 
     *													  (DEFAULT: {})
     */
	Custom: function(value, paramsObj){
		var paramsObj = paramsObj || {};
		var against = paramsObj.against || function(){ return true; };
		var args = paramsObj.args || {};
		var message = paramsObj.failureMessage || "无效!";
	    if(!against(value, args)) Validate.fail(message);
	    return true;
	  },
	
    /**
     *	validates whatever it is you pass in, and handles the validation error for you so it gives a nice true or false reply
     *
     *	@var validationFunction {Function} - validation function to be used (ie Validation.validatePresence )
     *	@var value {mixed} - value to be checked if true or not (usually a boolean from the checked value of a checkbox)
     *	@var validationParamsObj {Object} - parameters for doing the validation, if wanted or necessary
     */
    now: function(validationFunction, value, validationParamsObj){
      	if(!validationFunction) throw new Error("Validate::now - Validation function must be provided!");
    	var isValid = true;
        try{    
    		validationFunction(value, validationParamsObj || {});
    	} catch(error) {
    		if(error instanceof Validate.Error){
    			isValid =  false;
    		}else{
    		 	throw error;
    		}
    	}finally{ 
            return isValid 
        }
    },
    
    /**
     * shortcut for failing throwing a validation error
     *
     *	@var errorMessage {String} - message to display
     */
    fail: function(errorMessage){
            throw new Validate.Error(errorMessage);
    },
    
    Error: function(errorMessage){
    	this.message = errorMessage;
    	this.name = 'ValidationError';
    }

}

function isDateValid(dateString) {
	if(dateString.trim()=="")return true;
	// 年月日正则表达式
	var r=dateString.match(/^(\d{1,4})(-|\/)(\d{1,2})\2(\d{1,2})$/); 
	if(r==null){
		// alert("请输入格式正确的日期\n\r日期格式：yyyy-mm-dd\n\r例如：2008-08-08\n\r");
		return false;
	}
	var d=new Date(r[1],r[3]-1,r[4]);   
	var num = (d.getFullYear()==r[1]&&(d.getMonth()+1)==r[3]&&d.getDate()==r[4]);
	if(num==0){
		// alert("请输入格式正确的日期\n\r日期格式：yyyy-mm-dd\n\r例如：2008-08-08\n\r");
		return false;
	}
	return (num!=0);
}

function isTimeValid(timeString) {
	if(timeString.trim()=="")return true;
	// 时间正则表达式
	var r=timeString.match(/^(\d{1,2})(:|\/)(\d{1,2})\2(\d{1,2})$/); 
	if(r==null){
		// alert("请输入格式正确的日期\n\r日期格式：yyyy-mm-dd\n\r例如：2008-08-08\n\r");
		return false;
	}
	var ary = timeString.split(':');
	if (ary.length != 3) {
		return false;
	}
	try {
		if (parseInt(ary[0]) < 0 || parseInt(ary[0]) > 23 || 
				parseInt(ary[1]) < 0 || parseInt(ary[1]) > 59 || 
				parseInt(ary[2]) < 0 || parseInt(ary[2]) > 59) {
			return false
		}
	} catch (e) {
		return false;
	}
	return true;
}

// 判断输入的日期是否正确
function checkDate(INDate) {
if (INDate=="")
   {return true;}
subYY=INDate.substr(0,4)
if(isNaN(subYY) || subYY<=0){
   return true;
}
//转换月份
if(INDate.indexOf('-',0)!=-1){ separate="-"}
else{
   if(INDate.indexOf('/',0)!=-1){separate="/"}
   else {return true;}
   }
   area=INDate.indexOf(separate,0)
   subMM=INDate.substr(area+1,INDate.indexOf(separate,area+1)-(area+1))
   if(isNaN(subMM) || subMM<=0){
   return true;
}
   if(subMM.length<2){subMM="0"+subMM}
//转换日
area=INDate.lastIndexOf(separate)
subDD=INDate.substr(area+1,INDate.length-area-1)
if(isNaN(subDD) || subDD<=0){
   return true;
}
if(eval(subDD)<10){subDD="0"+eval(subDD)}
NewDate=subYY+"-"+subMM+"-"+subDD
if(NewDate.length!=10){return true;}
   if(NewDate.substr(4,1)!="-"){return true;}
   if(NewDate.substr(7,1)!="-"){return true;}
var MM=NewDate.substr(5,2);
var DD=NewDate.substr(8,2);
if((subYY%4==0 && subYY%100!=0)||subYY%400==0){ //判断是否为闰年
   if(parseInt(MM)==2){
    if(DD>29){return true;}
   }
}else{
   if(parseInt(MM)==2){
    if(DD>28){return true;}
   } 
}
var mm=new Array(1,3,5,7,8,10,12); //判断每月中的最大天数
for(i=0;i< mm.length;i++){
   if (parseInt(MM) == mm[i]){
    if(parseInt(DD)>31){return true;}
   }else{
    if(parseInt(DD)>30){return true;}
   }
}
if(parseInt(MM)>12){return true;}
    return false;
}
// 是否含有中文
function isChinese(temp) {
	var reg = /^([a-zA-Z0-9]|[\uFE30-\uFFA0|\/:])*$/gi;
	if(!reg.test(temp)) {
	  return true;  
	}
	return false;
}
function sql_inj(str) {
	var inj_str = "'|and|exec|insert|select|delete|update|count|*|%|chr|mid|master|truncate|char|declare|;";
	var inj_stra = inj_str.split("\\|");
	for (var i = 0; i < inj_stra.length; i++) {
		if (str.indexOf(inj_stra[i]) >= 0) {
			return true;
		}
	}
	return false;
}