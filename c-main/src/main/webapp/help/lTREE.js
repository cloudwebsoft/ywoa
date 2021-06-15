String.prototype.trim=function() {
	var m=this.match(/^\s*(\S+(\s+\S+)*)\s*$/);return (m==null)?"": m[1];
};
var lDOM={
	$:function(s){
		var o=typeof(s)=="string"?document.getElementById(s):s;
		try{return o;} finally {o=null;}
	},
	hasClass:function(e,c){
		if(!(e=this.$(e))) return false;
		e=e.className || " ";
		e=" " + e + " ";
		c=" " + c + " ";return (e.indexOf(c)!=-1);
	},
	addClass:function(e,c){
		if(!(e=this.$(e))) return;
		if(this.hasClass(e,c)){return;}
		e.className=e.className+" "+c;
	},
	delClass:function(e,c){
		if(this.hasClass(e,c)){
			e=this.$(e);
			var a=e.className.split(" ");
			a.remove(function(s){
				return s==c;
			});
			e.className=a.join(" ");
		}
	}
};
lDOM.BV=(function(){
	var u="indexOf",
		n=navigator,
		a=n.userAgent.toLowerCase(),
		b=(document.getElementById?true:false),
		c=((a[u]("msie")!=-1) && (a[u]("opera")==-1) && (a[u]("omniweb")==-1)),
		d=b && (n.appName=="Netscape"),
		e=a[u]("opera")!=-1,
		f=a[u]("gecko") !=-1,
		g=d && a[u]("firefox") !=-1,
		h=f && a[u]("safari") !=-1;return {AGT:a,isW3C:b,isIE:c,isFF:g,isNS6:d,isOP:e,isSF:h,isGecko:f};
	})();
Array.prototype.each=function(f,r){
	if(r===-1){
		for(var i=this.length-1;i>=0;i--)f(this[i],i);
	}else{
		for(var i=0,l=this.length;i<l;i++)f(this[i],i);
	}
};
Array.prototype.indexOf=function(v,b){
    var idx=-1;
	if(b===true && typeof(v)=="function"){
		for (var i=0,l=this.length;i<l;i++) {
			if(v(this[i])){idx=i; break;}
		}
	}else {
		for (var i=0,l=this.length;i<l;i++) {
			if(this[i]===v){idx=i; break;}
		}
	}return idx;
};
Array.prototype.lastIndexOf=function(v,b){
    var idx=-1;
	if(b===true && typeof(v)=="function"){
		for (var i=this.length-1;i>=0;i--) {
			if(v(this[i])){idx=i; break;}
		}
	}else {
		for (var i=this.length-1;i>=0;i--) {
			if(this[i]===v){idx=i; break;}
		}
	}return idx;
};
Array.prototype.remove=function(f){
	var ME=this;
	if(typeof(f)=="function"){
		ME.each(function(s,i){
			if(f(s,i))ME.splice(i,1);
		},-1);
	}return ME;
};
lDOM.fixAttr=function(){
	return{
		"for": "htmlFor",
		"class": "className",
		"float": this.BV.isIE ? "styleFloat": "cssFloat",
		cssFloat: this.BV.isIE ? "styleFloat": "cssFloat",
		innerHTML: "innerHTML",
		className: "className",
		value: "value",
		disabled: "disabled",
		checked: "checked",
		readonly: "readOnly",
		selected: "selected",
		tagName: "tagName"
	};
};
lDOM.hasAttr=function(o,a){
	var b=true;
	var fix=this.fixAttr(),r;
	for (var x in a){
		if(fix[x]){
			r=o[fix[x]];
		} else{
			r=o.getAttribute(x);
		}
		if(typeof(a[x])=="function"){
			if(!a[x](r)){b=false;break;}
		}else {
			if(r!=a[x]){b=false;break;}
		}
	}return b;
};
lDOM.each=function(a,b,f,t){
	if(!(b=this.$(b))) b=document.body;
	if(!b.length)b=[b];
	a=(function(p){
		if(typeof(p)!="string") return [];
		p=p.trim();
		p=p.replace(/\s+/g," ").split(" ");
		var r=[],ns=0,ne=0;
		p.each(function(s,i){
			var n=s.indexOf(".");
			if(n>-1){
				if(n==0)s="*"+s;
			}else {
				s=s+".*";
			}
			s=s.split(".");
			var t=s[0].toUpperCase(),c=s[1];
			p[i]=[t,c];
		});
		p.each(function(s,i){
			var m=r.indexOf(function(x,k){
					return s[0]==x[0];
				},true);
				if(m>-1){
					r[m][2]+=1;
					r[m][1].push(s[1]);
				}else {
					r.push([s[0],[s[1]],1]);
				}
		},-1);
		r.reverse();
		a=null;
		r.each(function(s,i){
			ne=p.lastIndexOf(function(x){
				return x[0]==s[0];
			},true)+1;
			s[2]=0;
			p.slice(ns,ne).each(function(y){
				if(y[0]==s[0])s[2]+=1;
			});
			ns=ne;
		});
		r.each(function(s){
			s[1]=s[1].slice(0,s[2]);
		});return r;})(a);
		var ME=this,r=[];
		function AR(a,f,d){
			if(d===-1){
				for(var i=a.length-1; i>=0;i--){
					f(a[i],i);
				}
			}else {
				for(var i=0,l=a.length; i<l;i++){
					f(a[i],i);
				}
			}
		}
		function $N(sp,t,p){
			var o=sp,tn=t[0],cls=t[1],cnt=t[2],n=0,b=false;
			if(tn=="*")tn=o.tagName;
			while(o!=p && n<cnt){
				if(o.tagName==tn){
					if(cls[n]!="*"){
						if(ME.hasClass(o,cls[n]))n+=1;
					}else {
						n+=1;
					}
				}
				o=o.parentNode;
			}
			if(n==cnt){
				b=true;
				while(o!=p && b){
					if(o.tagName==tn){
						b=false;
					}
					o=o.parentNode;
				}
			}return b;
		}
		function $C(sp,t,p){
			var o=sp,tn=t[0],cls=t[1],cnt=t[2],n=0;
			if(tn=="*")tn=o.tagName;
			while(o!=p && n<cnt){
				if(o.tagName==tn){
					if(cls[n]!="*"){
						if(ME.hasClass(o,cls[n])){
							n+=1;
						}
					}else {
							n+=1;
					}
				}
				o=o.parentNode;
			}return n==cnt;
		}
		function $F(t,p,f){
			var tn=t[0],R=[];
			AR(p,function(o,i){
				var nodes=o.getElementsByTagName(tn);
				AR(nodes,function(c,j){
						if(f(c,t,o)){
							R.push(c);
						}
				});
			});return R;
		}
		function $L(t,p,f,b){
			var cm=(b===true?$N:$C);
			if(typeof(f)=="function"){
				var tn=t[0],R=[];
				AR(p,function(o,i){
					var nodes=o.getElementsByTagName(tn);
					AR(nodes,function(c,j){
							if(cm(c,t,o)){
								if(f(c,R.length)){
									R.push(c);
								}
							}
					});
				});return R;
			}else {
				return $F(t,p,cm);
			}
		}
		function $A(t,p,f,m){
			var l=t.length-1;
			AR(t,function(s,i){
				if(i>=l){
					p=$L(s,p,f,m);
				}else {
					p=$F(s,p,$N);
				}
			});return p;
		}
		r=$A(a,b,f,t);return r;
};
lDOM.find=function(a,p,A,t){
	var ME=this,R=[];
	if(typeof(A)=="object"){
		var f=function(x,i){
			return ME.hasAttr(x,A);
		};
		R=ME.each(a,p,f,t);
	}else {
		R=ME.each(a,p,null,t);
	}return R;
};
var lTREE=function(){
	var ME=this;
	this.author="CN.LEI <cnlei.y.l@gmail.com>";
	this.version="2.0.1";
	this.onclick=null;
	this.item=[];
	this._RG="<DD\([\^>]\*\)>";
	this._RW="<DD$1><button></button>";
	this.configs={
		path: "DL DD",
		func: null,
		mode: false
	};
	this.classNames={
		folderClose: "folderClose",
		lastChild: "lastChild"
	};
	this.tagNames={
		folder: "DD",
		file: "DT"
	};
};
lTREE.prototype={
	click:function(o){
		o=o.parentNode;
		var a=this.classNames,c=a.folderClose,b=lDOM.hasClass(o,c);
		if(!b){
			lDOM.addClass(o,c);
		}else {
			lDOM.delClass(o,c);
		}
		if(typeof(this.onclick)=="function"){
			this.onclick(o,b);
		}return b;
	},
	getNo:function(o){
		var b=o.parentNode.childNodes,m=0,n=0,tn=o.tagName;
		for(var i=0,l=b.length; i<l;i++) {
			if(b[i].tagName==tn){
				m+=1;
				if(b[i]==o)n=m;
			}
		}return [m,n];
	},
	_push4IE:function(s){
		var tn=this.tagNames,DD=tn["folder"],DT=tn["file"],b,lc=this.classNames.lastChild;
		if(s.getElementsByTagName(DD).length<1){
			b=s.getElementsByTagName(DT);
			if(b.length-1>0){
				lDOM.addClass(b[b.length-1],lc);
			}
		}
		b=s.parentNode.childNodes;
		for(var i=b.length-1; i>=0;i--) {
			if(b[i].nodeType==1){
				if(b[i].tagName==DD){
					lDOM.addClass(b[i],lc);
					break;
				}else if(b[i].tagName==DT){
					lDOM.addClass(b[i],lc);
					break;
				}
			}
		}
	},
	set4NS:function(s,i){
		var ME=this,o=document.createElement("button");
		o.onclick=function(){
			ME.click(this);
		};
		o.onfocus=function(){
			this.blur();return false;
		};
		s.insertBefore(o,s.firstChild);
		o=null;
		ME._push4IE(s);return true;
	},
	set4IE:function(s,i){
		var ME=this,o=s.firstChild;
		o.onclick=function(){
			ME.click(this);
		};
		o.onfocus=function(){
			this.blur();return false;
		};
		o=null;
		ME._push4IE(s);return true;
	},
	changAll:function(n){
		var c=this.classNames.folderClose;
		if(n>0){
			this.item.each(function(s){
				lDOM.delClass(s,c);
			});
		}else {
			this.item.each(function(s){
				lDOM.addClass(s,c);
			});
		}
	},
	config:function(a,t){
			var ME=this;
			if(a){
				if(typeof(a)=="object"){
					var o;
					if(!t)t="base";
					if(t=="base"){
						o=this.configs;
						for(var x in a)o[x]=a[x];
					}else if(t=="tagName"){
						o=this.tagNames;
						for(var x in a)o[x]=a[x];
					}else if(t=="className"){
						o=this.classNames;
						for(var x in a)o[x]=a[x];
					}
				}
			}
	},
	tagName:function(a){
		this.config(a,"tagName");
	},
	className:function(a){
		this.config(a,"className");
	},
	build:function(p){
		var ME=this;
		if((p=lDOM.$(p))){
			var o;
			o=this.configs;
			var ME=this,cn="s";
			var f=o.func;
			var BV=lDOM.BV,forRP=(BV.isIE || BV.isOP || BV.isSF);
			if(typeof(f)!="function"){
				if(forRP){
					f=function(x,i){ME.set4IE(x,i);return true;};
				}else {
					f=function(x,i){ME.set4NS(x,i);return true;};
				}
			}
			if(forRP){
				var fd=ME.tagNames.folder,rg=ME._RG.replace(/DD/g,fd),rw=ME._RW.replace(/DD/g,fd);
				p.innerHTML=p.innerHTML.replace(new RegExp(rg,"ig"),rw);
			}
			this.item=lDOM.each(o.path,p,f,o.mode);
		}return function(x){
			return ME.build(x);
		};
	}
};