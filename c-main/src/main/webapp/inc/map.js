function Map() {
	this.elements = new Array();
	this.size = function() {
		return this.elements.length;
	}
	this.put = function(_key, _value) {
		this.elements.push( {key : _key, value : _value});
	}
	this.remove = function(_key) {
		var bln = false;
		try {
			for (i = 0; i < this.elements.length; i++) {
				if (this.elements[i].key == _key) {
					this.elements.splice(i, 1);
					return true;
				}
			}
		} catch (e) {
			bln = false;
		}
		return bln;
	}
	this.containsKey = function(_key) {
		var bln = false;
		try {
			for (i = 0; i < this.elements.length; i++) {
				if (this.elements[i].key == _key) {
					bln = true;
				}
			}
		} catch (e) {
			bln = false;
		}
		return bln;
	}
	// 注意返回的是json，如果需取其值，需用get(key).value
	this.get = function(_key) {
		try {
			for (i = 0; i < this.elements.length; i++) {
				if (this.elements[i].key == _key) {
					return this.elements[i];
				}
			}
		} catch (e) {
			return null;
		}
		return null;
	}
	this.getElements = function() {
		return this.elements;
	}
}
/*
function testMap(){
　　var testmap=new Map();
　　testmap.put("01","michael");
　　testmap.put("02","michael2");
　　alert (testmap.size());
　　var key="02"
　　if (testmap.containsKey(key)){
　　var element=testmap.get(key);
　　alert (element.key+"|"+element.value);
　　}else{
　　alert ("不包含"+key);
　　}

　　testmap.remove("02");
　　if (testmap.containsKey(key)){
　　var element=testmap.get(key);
　　alert (element.key+"|"+element.value);
　　}else{
　　alert ("不包含"+key);
　　}
}
*/