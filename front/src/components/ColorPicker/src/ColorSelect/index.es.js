var __defProp = Object.defineProperty;
var __defNormalProp = (obj, key, value) => key in obj ? __defProp(obj, key, { enumerable: true, configurable: true, writable: true, value }) : obj[key] = value;
var __publicField = (obj, key, value) => {
  __defNormalProp(obj, typeof key !== "symbol" ? key + "" : key, value);
  return value;
};
import { getCurrentInstance, onMounted, nextTick, watch, getCurrentScope, onScopeDispose, unref, shallowRef, ref, defineComponent, reactive, computed, openBlock, createElementBlock, normalizeClass, createElementVNode, normalizeStyle, pushScopeId, popScopeId, Fragment, renderList, createCommentVNode, withKeys, resolveComponent, createBlock, createVNode, inject, withDirectives, vShow, toDisplayString, renderSlot, provide, mergeProps, Transition, withCtx, resolveDynamicComponent } from "vue";
var index = "";
/*!
 * is-plain-object <https://github.com/jonschlinkert/is-plain-object>
 *
 * Copyright (c) 2014-2017, Jon Schlinkert.
 * Released under the MIT License.
 */
function isObject$1(o2) {
  return Object.prototype.toString.call(o2) === "[object Object]";
}
function isPlainObject$1(o2) {
  var ctor, prot;
  if (isObject$1(o2) === false)
    return false;
  ctor = o2.constructor;
  if (ctor === void 0)
    return true;
  prot = ctor.prototype;
  if (isObject$1(prot) === false)
    return false;
  if (prot.hasOwnProperty("isPrototypeOf") === false) {
    return false;
  }
  return true;
}
function t$1() {
  return t$1 = Object.assign ? Object.assign.bind() : function(e2) {
    for (var t2 = 1; t2 < arguments.length; t2++) {
      var r2 = arguments[t2];
      for (var n2 in r2)
        Object.prototype.hasOwnProperty.call(r2, n2) && (e2[n2] = r2[n2]);
    }
    return e2;
  }, t$1.apply(this, arguments);
}
function r$1(e2, t2) {
  if (null == e2)
    return {};
  var r2, n2, i2 = {}, o2 = Object.keys(e2);
  for (n2 = 0; n2 < o2.length; n2++)
    t2.indexOf(r2 = o2[n2]) >= 0 || (i2[r2] = e2[r2]);
  return i2;
}
const n$1 = { silent: false, logLevel: "warn" }, i$1 = ["validator"], o$1 = Object.prototype, a$1 = o$1.toString, s$1 = o$1.hasOwnProperty, u$1 = /^\s*function (\w+)/;
function l$1(e2) {
  var t2;
  const r2 = null !== (t2 = null == e2 ? void 0 : e2.type) && void 0 !== t2 ? t2 : e2;
  if (r2) {
    const e3 = r2.toString().match(u$1);
    return e3 ? e3[1] : "";
  }
  return "";
}
const c$1 = isPlainObject$1, f$1 = (e2) => e2;
let d$1 = f$1;
const p$1 = (e2, t2) => s$1.call(e2, t2), y$1 = Number.isInteger || function(e2) {
  return "number" == typeof e2 && isFinite(e2) && Math.floor(e2) === e2;
}, v$1 = Array.isArray || function(e2) {
  return "[object Array]" === a$1.call(e2);
}, h$1 = (e2) => "[object Function]" === a$1.call(e2), b$1 = (e2) => c$1(e2) && p$1(e2, "_vueTypes_name"), g$1 = (e2) => c$1(e2) && (p$1(e2, "type") || ["_vueTypes_name", "validator", "default", "required"].some((t2) => p$1(e2, t2)));
function O$1(e2, t2) {
  return Object.defineProperty(e2.bind(t2), "__original", { value: e2 });
}
function m$1(e2, t2, r2 = false) {
  let n2, i2 = true, o2 = "";
  n2 = c$1(e2) ? e2 : { type: e2 };
  const a2 = b$1(n2) ? n2._vueTypes_name + " - " : "";
  if (g$1(n2) && null !== n2.type) {
    if (void 0 === n2.type || true === n2.type)
      return i2;
    if (!n2.required && void 0 === t2)
      return i2;
    v$1(n2.type) ? (i2 = n2.type.some((e3) => true === m$1(e3, t2, true)), o2 = n2.type.map((e3) => l$1(e3)).join(" or ")) : (o2 = l$1(n2), i2 = "Array" === o2 ? v$1(t2) : "Object" === o2 ? c$1(t2) : "String" === o2 || "Number" === o2 || "Boolean" === o2 || "Function" === o2 ? function(e3) {
      if (null == e3)
        return "";
      const t3 = e3.constructor.toString().match(u$1);
      return t3 ? t3[1] : "";
    }(t2) === o2 : t2 instanceof n2.type);
  }
  if (!i2) {
    const e3 = `${a2}value "${t2}" should be of type "${o2}"`;
    return false === r2 ? (d$1(e3), false) : e3;
  }
  if (p$1(n2, "validator") && h$1(n2.validator)) {
    const e3 = d$1, o3 = [];
    if (d$1 = (e4) => {
      o3.push(e4);
    }, i2 = n2.validator(t2), d$1 = e3, !i2) {
      const e4 = (o3.length > 1 ? "* " : "") + o3.join("\n* ");
      return o3.length = 0, false === r2 ? (d$1(e4), i2) : e4;
    }
  }
  return i2;
}
function j$1(e2, t2) {
  const r2 = Object.defineProperties(t2, { _vueTypes_name: { value: e2, writable: true }, isRequired: { get() {
    return this.required = true, this;
  } }, def: { value(e3) {
    return void 0 === e3 ? (p$1(this, "default") && delete this.default, this) : h$1(e3) || true === m$1(this, e3, true) ? (this.default = v$1(e3) ? () => [...e3] : c$1(e3) ? () => Object.assign({}, e3) : e3, this) : (d$1(`${this._vueTypes_name} - invalid default value: "${e3}"`), this);
  } } }), { validator: n2 } = r2;
  return h$1(n2) && (r2.validator = O$1(n2, r2)), r2;
}
function _$1(e2, t2) {
  const r2 = j$1(e2, t2);
  return Object.defineProperty(r2, "validate", { value(e3) {
    return h$1(this.validator) && d$1(`${this._vueTypes_name} - calling .validate() will overwrite the current custom validator function. Validator info:
${JSON.stringify(this)}`), this.validator = O$1(e3, this), this;
  } });
}
function T$1(e2, t2, n2) {
  const o2 = function(e3) {
    const t3 = {};
    return Object.getOwnPropertyNames(e3).forEach((r2) => {
      t3[r2] = Object.getOwnPropertyDescriptor(e3, r2);
    }), Object.defineProperties({}, t3);
  }(t2);
  if (o2._vueTypes_name = e2, !c$1(n2))
    return o2;
  const { validator: a2 } = n2, s2 = r$1(n2, i$1);
  if (h$1(a2)) {
    let { validator: e3 } = o2;
    e3 && (e3 = null !== (l2 = (u2 = e3).__original) && void 0 !== l2 ? l2 : u2), o2.validator = O$1(e3 ? function(t3) {
      return e3.call(this, t3) && a2.call(this, t3);
    } : a2, o2);
  }
  var u2, l2;
  return Object.assign(o2, s2);
}
function $$1(e2) {
  return e2.replace(/^(?!\s*$)/gm, "  ");
}
const w$1 = () => _$1("any", {}), P$1 = () => _$1("function", { type: Function }), x$1 = () => _$1("boolean", { type: Boolean }), E$1 = () => _$1("string", { type: String }), N$1 = () => _$1("number", { type: Number }), q$1 = () => _$1("array", { type: Array }), A$1 = () => _$1("object", { type: Object }), V$1 = () => j$1("integer", { type: Number, validator: (e2) => y$1(e2) }), S$1 = () => j$1("symbol", { validator: (e2) => "symbol" == typeof e2 });
function D$1(e2, t2 = "custom validation failed") {
  if ("function" != typeof e2)
    throw new TypeError("[VueTypes error]: You must provide a function as argument");
  return j$1(e2.name || "<<anonymous function>>", { type: null, validator(r2) {
    const n2 = e2(r2);
    return n2 || d$1(`${this._vueTypes_name} - ${t2}`), n2;
  } });
}
function L$1(e2) {
  if (!v$1(e2))
    throw new TypeError("[VueTypes error]: You must provide an array as argument.");
  const t2 = `oneOf - value should be one of "${e2.join('", "')}".`, r2 = e2.reduce((e3, t3) => {
    if (null != t3) {
      const r3 = t3.constructor;
      -1 === e3.indexOf(r3) && e3.push(r3);
    }
    return e3;
  }, []);
  return j$1("oneOf", { type: r2.length > 0 ? r2 : void 0, validator(r3) {
    const n2 = -1 !== e2.indexOf(r3);
    return n2 || d$1(t2), n2;
  } });
}
function F$1(e2) {
  if (!v$1(e2))
    throw new TypeError("[VueTypes error]: You must provide an array as argument");
  let t2 = false, r2 = [];
  for (let n3 = 0; n3 < e2.length; n3 += 1) {
    const i2 = e2[n3];
    if (g$1(i2)) {
      if (b$1(i2) && "oneOf" === i2._vueTypes_name && i2.type) {
        r2 = r2.concat(i2.type);
        continue;
      }
      if (h$1(i2.validator) && (t2 = true), true === i2.type || !i2.type) {
        d$1('oneOfType - invalid usage of "true" or "null" as types.');
        continue;
      }
      r2 = r2.concat(i2.type);
    } else
      r2.push(i2);
  }
  r2 = r2.filter((e3, t3) => r2.indexOf(e3) === t3);
  const n2 = r2.length > 0 ? r2 : null;
  return j$1("oneOfType", t2 ? { type: n2, validator(t3) {
    const r3 = [], n3 = e2.some((e3) => {
      const n4 = m$1(b$1(e3) && "oneOf" === e3._vueTypes_name ? e3.type || null : e3, t3, true);
      return "string" == typeof n4 && r3.push(n4), true === n4;
    });
    return n3 || d$1(`oneOfType - provided value does not match any of the ${r3.length} passed-in validators:
${$$1(r3.join("\n"))}`), n3;
  } } : { type: n2 });
}
function Y$1(e2) {
  return j$1("arrayOf", { type: Array, validator(t2) {
    let r2 = "";
    const n2 = t2.every((t3) => (r2 = m$1(e2, t3, true), true === r2));
    return n2 || d$1(`arrayOf - value validation error:
${$$1(r2)}`), n2;
  } });
}
function B$1(e2) {
  return j$1("instanceOf", { type: e2 });
}
function I$1(e2) {
  return j$1("objectOf", { type: Object, validator(t2) {
    let r2 = "";
    const n2 = Object.keys(t2).every((n3) => (r2 = m$1(e2, t2[n3], true), true === r2));
    return n2 || d$1(`objectOf - value validation error:
${$$1(r2)}`), n2;
  } });
}
function J$1(e2) {
  const t2 = Object.keys(e2), r2 = t2.filter((t3) => {
    var r3;
    return !(null === (r3 = e2[t3]) || void 0 === r3 || !r3.required);
  }), n2 = j$1("shape", { type: Object, validator(n3) {
    if (!c$1(n3))
      return false;
    const i2 = Object.keys(n3);
    if (r2.length > 0 && r2.some((e3) => -1 === i2.indexOf(e3))) {
      const e3 = r2.filter((e4) => -1 === i2.indexOf(e4));
      return d$1(1 === e3.length ? `shape - required property "${e3[0]}" is not defined.` : `shape - required properties "${e3.join('", "')}" are not defined.`), false;
    }
    return i2.every((r3) => {
      if (-1 === t2.indexOf(r3))
        return true === this._vueTypes_isLoose || (d$1(`shape - shape definition does not include a "${r3}" property. Allowed keys: "${t2.join('", "')}".`), false);
      const i3 = m$1(e2[r3], n3[r3], true);
      return "string" == typeof i3 && d$1(`shape - "${r3}" property validation error:
 ${$$1(i3)}`), true === i3;
    });
  } });
  return Object.defineProperty(n2, "_vueTypes_isLoose", { writable: true, value: false }), Object.defineProperty(n2, "loose", { get() {
    return this._vueTypes_isLoose = true, this;
  } }), n2;
}
const M$1 = ["name", "validate", "getter"], R$1 = /* @__PURE__ */ (() => {
  var e2;
  return (e2 = class {
    static get any() {
      return w$1();
    }
    static get func() {
      return P$1().def(this.defaults.func);
    }
    static get bool() {
      return x$1().def(this.defaults.bool);
    }
    static get string() {
      return E$1().def(this.defaults.string);
    }
    static get number() {
      return N$1().def(this.defaults.number);
    }
    static get array() {
      return q$1().def(this.defaults.array);
    }
    static get object() {
      return A$1().def(this.defaults.object);
    }
    static get integer() {
      return V$1().def(this.defaults.integer);
    }
    static get symbol() {
      return S$1();
    }
    static get nullable() {
      return { type: null };
    }
    static extend(e3) {
      if (v$1(e3))
        return e3.forEach((e4) => this.extend(e4)), this;
      const { name: t2, validate: n2 = false, getter: i2 = false } = e3, o2 = r$1(e3, M$1);
      if (p$1(this, t2))
        throw new TypeError(`[VueTypes error]: Type "${t2}" already defined`);
      const { type: a2 } = o2;
      if (b$1(a2))
        return delete o2.type, Object.defineProperty(this, t2, i2 ? { get: () => T$1(t2, a2, o2) } : { value(...e4) {
          const r2 = T$1(t2, a2, o2);
          return r2.validator && (r2.validator = r2.validator.bind(r2, ...e4)), r2;
        } });
      let s2;
      return s2 = i2 ? { get() {
        const e4 = Object.assign({}, o2);
        return n2 ? _$1(t2, e4) : j$1(t2, e4);
      }, enumerable: true } : { value(...e4) {
        const r2 = Object.assign({}, o2);
        let i3;
        return i3 = n2 ? _$1(t2, r2) : j$1(t2, r2), r2.validator && (i3.validator = r2.validator.bind(i3, ...e4)), i3;
      }, enumerable: true }, Object.defineProperty(this, t2, s2);
    }
  }).defaults = {}, e2.sensibleDefaults = void 0, e2.config = n$1, e2.custom = D$1, e2.oneOf = L$1, e2.instanceOf = B$1, e2.oneOfType = F$1, e2.arrayOf = Y$1, e2.objectOf = I$1, e2.shape = J$1, e2.utils = { validate: (e3, t2) => true === m$1(t2, e3, true), toType: (e3, t2, r2 = false) => r2 ? _$1(e3, t2) : j$1(e3, t2) }, e2;
})();
function z$1(e2 = { func: () => {
}, bool: true, string: "", number: 0, array: () => [], object: () => ({}), integer: 0 }) {
  var r2;
  return (r2 = class extends R$1 {
    static get sensibleDefaults() {
      return t$1({}, this.defaults);
    }
    static set sensibleDefaults(r3) {
      this.defaults = false !== r3 ? t$1({}, true !== r3 ? r3 : e2) : {};
    }
  }).defaults = t$1({}, e2), r2;
}
class C$1 extends z$1() {
}
function _typeof(obj) {
  "@babel/helpers - typeof";
  return _typeof = "function" == typeof Symbol && "symbol" == typeof Symbol.iterator ? function(obj2) {
    return typeof obj2;
  } : function(obj2) {
    return obj2 && "function" == typeof Symbol && obj2.constructor === Symbol && obj2 !== Symbol.prototype ? "symbol" : typeof obj2;
  }, _typeof(obj);
}
var trimLeft = /^\s+/;
var trimRight = /\s+$/;
function tinycolor(color, opts) {
  color = color ? color : "";
  opts = opts || {};
  if (color instanceof tinycolor) {
    return color;
  }
  if (!(this instanceof tinycolor)) {
    return new tinycolor(color, opts);
  }
  var rgb = inputToRGB(color);
  this._originalInput = color, this._r = rgb.r, this._g = rgb.g, this._b = rgb.b, this._a = rgb.a, this._roundA = Math.round(100 * this._a) / 100, this._format = opts.format || rgb.format;
  this._gradientType = opts.gradientType;
  if (this._r < 1)
    this._r = Math.round(this._r);
  if (this._g < 1)
    this._g = Math.round(this._g);
  if (this._b < 1)
    this._b = Math.round(this._b);
  this._ok = rgb.ok;
}
tinycolor.prototype = {
  isDark: function isDark() {
    return this.getBrightness() < 128;
  },
  isLight: function isLight() {
    return !this.isDark();
  },
  isValid: function isValid() {
    return this._ok;
  },
  getOriginalInput: function getOriginalInput() {
    return this._originalInput;
  },
  getFormat: function getFormat() {
    return this._format;
  },
  getAlpha: function getAlpha() {
    return this._a;
  },
  getBrightness: function getBrightness() {
    var rgb = this.toRgb();
    return (rgb.r * 299 + rgb.g * 587 + rgb.b * 114) / 1e3;
  },
  getLuminance: function getLuminance() {
    var rgb = this.toRgb();
    var RsRGB, GsRGB, BsRGB, R2, G2, B2;
    RsRGB = rgb.r / 255;
    GsRGB = rgb.g / 255;
    BsRGB = rgb.b / 255;
    if (RsRGB <= 0.03928)
      R2 = RsRGB / 12.92;
    else
      R2 = Math.pow((RsRGB + 0.055) / 1.055, 2.4);
    if (GsRGB <= 0.03928)
      G2 = GsRGB / 12.92;
    else
      G2 = Math.pow((GsRGB + 0.055) / 1.055, 2.4);
    if (BsRGB <= 0.03928)
      B2 = BsRGB / 12.92;
    else
      B2 = Math.pow((BsRGB + 0.055) / 1.055, 2.4);
    return 0.2126 * R2 + 0.7152 * G2 + 0.0722 * B2;
  },
  setAlpha: function setAlpha(value) {
    this._a = boundAlpha(value);
    this._roundA = Math.round(100 * this._a) / 100;
    return this;
  },
  toHsv: function toHsv() {
    var hsv = rgbToHsv(this._r, this._g, this._b);
    return {
      h: hsv.h * 360,
      s: hsv.s,
      v: hsv.v,
      a: this._a
    };
  },
  toHsvString: function toHsvString() {
    var hsv = rgbToHsv(this._r, this._g, this._b);
    var h2 = Math.round(hsv.h * 360), s2 = Math.round(hsv.s * 100), v2 = Math.round(hsv.v * 100);
    return this._a == 1 ? "hsv(" + h2 + ", " + s2 + "%, " + v2 + "%)" : "hsva(" + h2 + ", " + s2 + "%, " + v2 + "%, " + this._roundA + ")";
  },
  toHsl: function toHsl() {
    var hsl = rgbToHsl(this._r, this._g, this._b);
    return {
      h: hsl.h * 360,
      s: hsl.s,
      l: hsl.l,
      a: this._a
    };
  },
  toHslString: function toHslString() {
    var hsl = rgbToHsl(this._r, this._g, this._b);
    var h2 = Math.round(hsl.h * 360), s2 = Math.round(hsl.s * 100), l2 = Math.round(hsl.l * 100);
    return this._a == 1 ? "hsl(" + h2 + ", " + s2 + "%, " + l2 + "%)" : "hsla(" + h2 + ", " + s2 + "%, " + l2 + "%, " + this._roundA + ")";
  },
  toHex: function toHex(allow3Char) {
    return rgbToHex(this._r, this._g, this._b, allow3Char);
  },
  toHexString: function toHexString(allow3Char) {
    return "#" + this.toHex(allow3Char);
  },
  toHex8: function toHex8(allow4Char) {
    return rgbaToHex(this._r, this._g, this._b, this._a, allow4Char);
  },
  toHex8String: function toHex8String(allow4Char) {
    return "#" + this.toHex8(allow4Char);
  },
  toRgb: function toRgb() {
    return {
      r: Math.round(this._r),
      g: Math.round(this._g),
      b: Math.round(this._b),
      a: this._a
    };
  },
  toRgbString: function toRgbString() {
    return this._a == 1 ? "rgb(" + Math.round(this._r) + ", " + Math.round(this._g) + ", " + Math.round(this._b) + ")" : "rgba(" + Math.round(this._r) + ", " + Math.round(this._g) + ", " + Math.round(this._b) + ", " + this._roundA + ")";
  },
  toPercentageRgb: function toPercentageRgb() {
    return {
      r: Math.round(bound01(this._r, 255) * 100) + "%",
      g: Math.round(bound01(this._g, 255) * 100) + "%",
      b: Math.round(bound01(this._b, 255) * 100) + "%",
      a: this._a
    };
  },
  toPercentageRgbString: function toPercentageRgbString() {
    return this._a == 1 ? "rgb(" + Math.round(bound01(this._r, 255) * 100) + "%, " + Math.round(bound01(this._g, 255) * 100) + "%, " + Math.round(bound01(this._b, 255) * 100) + "%)" : "rgba(" + Math.round(bound01(this._r, 255) * 100) + "%, " + Math.round(bound01(this._g, 255) * 100) + "%, " + Math.round(bound01(this._b, 255) * 100) + "%, " + this._roundA + ")";
  },
  toName: function toName() {
    if (this._a === 0) {
      return "transparent";
    }
    if (this._a < 1) {
      return false;
    }
    return hexNames[rgbToHex(this._r, this._g, this._b, true)] || false;
  },
  toFilter: function toFilter(secondColor) {
    var hex8String = "#" + rgbaToArgbHex(this._r, this._g, this._b, this._a);
    var secondHex8String = hex8String;
    var gradientType = this._gradientType ? "GradientType = 1, " : "";
    if (secondColor) {
      var s2 = tinycolor(secondColor);
      secondHex8String = "#" + rgbaToArgbHex(s2._r, s2._g, s2._b, s2._a);
    }
    return "progid:DXImageTransform.Microsoft.gradient(" + gradientType + "startColorstr=" + hex8String + ",endColorstr=" + secondHex8String + ")";
  },
  toString: function toString(format) {
    var formatSet = !!format;
    format = format || this._format;
    var formattedString = false;
    var hasAlpha = this._a < 1 && this._a >= 0;
    var needsAlphaFormat = !formatSet && hasAlpha && (format === "hex" || format === "hex6" || format === "hex3" || format === "hex4" || format === "hex8" || format === "name");
    if (needsAlphaFormat) {
      if (format === "name" && this._a === 0) {
        return this.toName();
      }
      return this.toRgbString();
    }
    if (format === "rgb") {
      formattedString = this.toRgbString();
    }
    if (format === "prgb") {
      formattedString = this.toPercentageRgbString();
    }
    if (format === "hex" || format === "hex6") {
      formattedString = this.toHexString();
    }
    if (format === "hex3") {
      formattedString = this.toHexString(true);
    }
    if (format === "hex4") {
      formattedString = this.toHex8String(true);
    }
    if (format === "hex8") {
      formattedString = this.toHex8String();
    }
    if (format === "name") {
      formattedString = this.toName();
    }
    if (format === "hsl") {
      formattedString = this.toHslString();
    }
    if (format === "hsv") {
      formattedString = this.toHsvString();
    }
    return formattedString || this.toHexString();
  },
  clone: function clone() {
    return tinycolor(this.toString());
  },
  _applyModification: function _applyModification(fn3, args) {
    var color = fn3.apply(null, [this].concat([].slice.call(args)));
    this._r = color._r;
    this._g = color._g;
    this._b = color._b;
    this.setAlpha(color._a);
    return this;
  },
  lighten: function lighten() {
    return this._applyModification(_lighten, arguments);
  },
  brighten: function brighten() {
    return this._applyModification(_brighten, arguments);
  },
  darken: function darken() {
    return this._applyModification(_darken, arguments);
  },
  desaturate: function desaturate() {
    return this._applyModification(_desaturate, arguments);
  },
  saturate: function saturate() {
    return this._applyModification(_saturate, arguments);
  },
  greyscale: function greyscale() {
    return this._applyModification(_greyscale, arguments);
  },
  spin: function spin() {
    return this._applyModification(_spin, arguments);
  },
  _applyCombination: function _applyCombination(fn3, args) {
    return fn3.apply(null, [this].concat([].slice.call(args)));
  },
  analogous: function analogous() {
    return this._applyCombination(_analogous, arguments);
  },
  complement: function complement() {
    return this._applyCombination(_complement, arguments);
  },
  monochromatic: function monochromatic() {
    return this._applyCombination(_monochromatic, arguments);
  },
  splitcomplement: function splitcomplement() {
    return this._applyCombination(_splitcomplement, arguments);
  },
  triad: function triad() {
    return this._applyCombination(polyad, [3]);
  },
  tetrad: function tetrad() {
    return this._applyCombination(polyad, [4]);
  }
};
tinycolor.fromRatio = function(color, opts) {
  if (_typeof(color) == "object") {
    var newColor = {};
    for (var i2 in color) {
      if (color.hasOwnProperty(i2)) {
        if (i2 === "a") {
          newColor[i2] = color[i2];
        } else {
          newColor[i2] = convertToPercentage(color[i2]);
        }
      }
    }
    color = newColor;
  }
  return tinycolor(color, opts);
};
function inputToRGB(color) {
  var rgb = {
    r: 0,
    g: 0,
    b: 0
  };
  var a2 = 1;
  var s2 = null;
  var v2 = null;
  var l2 = null;
  var ok = false;
  var format = false;
  if (typeof color == "string") {
    color = stringInputToObject(color);
  }
  if (_typeof(color) == "object") {
    if (isValidCSSUnit(color.r) && isValidCSSUnit(color.g) && isValidCSSUnit(color.b)) {
      rgb = rgbToRgb(color.r, color.g, color.b);
      ok = true;
      format = String(color.r).substr(-1) === "%" ? "prgb" : "rgb";
    } else if (isValidCSSUnit(color.h) && isValidCSSUnit(color.s) && isValidCSSUnit(color.v)) {
      s2 = convertToPercentage(color.s);
      v2 = convertToPercentage(color.v);
      rgb = hsvToRgb(color.h, s2, v2);
      ok = true;
      format = "hsv";
    } else if (isValidCSSUnit(color.h) && isValidCSSUnit(color.s) && isValidCSSUnit(color.l)) {
      s2 = convertToPercentage(color.s);
      l2 = convertToPercentage(color.l);
      rgb = hslToRgb(color.h, s2, l2);
      ok = true;
      format = "hsl";
    }
    if (color.hasOwnProperty("a")) {
      a2 = color.a;
    }
  }
  a2 = boundAlpha(a2);
  return {
    ok,
    format: color.format || format,
    r: Math.min(255, Math.max(rgb.r, 0)),
    g: Math.min(255, Math.max(rgb.g, 0)),
    b: Math.min(255, Math.max(rgb.b, 0)),
    a: a2
  };
}
function rgbToRgb(r2, g2, b2) {
  return {
    r: bound01(r2, 255) * 255,
    g: bound01(g2, 255) * 255,
    b: bound01(b2, 255) * 255
  };
}
function rgbToHsl(r2, g2, b2) {
  r2 = bound01(r2, 255);
  g2 = bound01(g2, 255);
  b2 = bound01(b2, 255);
  var max2 = Math.max(r2, g2, b2), min2 = Math.min(r2, g2, b2);
  var h2, s2, l2 = (max2 + min2) / 2;
  if (max2 == min2) {
    h2 = s2 = 0;
  } else {
    var d2 = max2 - min2;
    s2 = l2 > 0.5 ? d2 / (2 - max2 - min2) : d2 / (max2 + min2);
    switch (max2) {
      case r2:
        h2 = (g2 - b2) / d2 + (g2 < b2 ? 6 : 0);
        break;
      case g2:
        h2 = (b2 - r2) / d2 + 2;
        break;
      case b2:
        h2 = (r2 - g2) / d2 + 4;
        break;
    }
    h2 /= 6;
  }
  return {
    h: h2,
    s: s2,
    l: l2
  };
}
function hslToRgb(h2, s2, l2) {
  var r2, g2, b2;
  h2 = bound01(h2, 360);
  s2 = bound01(s2, 100);
  l2 = bound01(l2, 100);
  function hue2rgb(p3, q2, t2) {
    if (t2 < 0)
      t2 += 1;
    if (t2 > 1)
      t2 -= 1;
    if (t2 < 1 / 6)
      return p3 + (q2 - p3) * 6 * t2;
    if (t2 < 1 / 2)
      return q2;
    if (t2 < 2 / 3)
      return p3 + (q2 - p3) * (2 / 3 - t2) * 6;
    return p3;
  }
  if (s2 === 0) {
    r2 = g2 = b2 = l2;
  } else {
    var q = l2 < 0.5 ? l2 * (1 + s2) : l2 + s2 - l2 * s2;
    var p2 = 2 * l2 - q;
    r2 = hue2rgb(p2, q, h2 + 1 / 3);
    g2 = hue2rgb(p2, q, h2);
    b2 = hue2rgb(p2, q, h2 - 1 / 3);
  }
  return {
    r: r2 * 255,
    g: g2 * 255,
    b: b2 * 255
  };
}
function rgbToHsv(r2, g2, b2) {
  r2 = bound01(r2, 255);
  g2 = bound01(g2, 255);
  b2 = bound01(b2, 255);
  var max2 = Math.max(r2, g2, b2), min2 = Math.min(r2, g2, b2);
  var h2, s2, v2 = max2;
  var d2 = max2 - min2;
  s2 = max2 === 0 ? 0 : d2 / max2;
  if (max2 == min2) {
    h2 = 0;
  } else {
    switch (max2) {
      case r2:
        h2 = (g2 - b2) / d2 + (g2 < b2 ? 6 : 0);
        break;
      case g2:
        h2 = (b2 - r2) / d2 + 2;
        break;
      case b2:
        h2 = (r2 - g2) / d2 + 4;
        break;
    }
    h2 /= 6;
  }
  return {
    h: h2,
    s: s2,
    v: v2
  };
}
function hsvToRgb(h2, s2, v2) {
  h2 = bound01(h2, 360) * 6;
  s2 = bound01(s2, 100);
  v2 = bound01(v2, 100);
  var i2 = Math.floor(h2), f2 = h2 - i2, p2 = v2 * (1 - s2), q = v2 * (1 - f2 * s2), t2 = v2 * (1 - (1 - f2) * s2), mod = i2 % 6, r2 = [v2, q, p2, p2, t2, v2][mod], g2 = [t2, v2, v2, q, p2, p2][mod], b2 = [p2, p2, t2, v2, v2, q][mod];
  return {
    r: r2 * 255,
    g: g2 * 255,
    b: b2 * 255
  };
}
function rgbToHex(r2, g2, b2, allow3Char) {
  var hex = [pad2(Math.round(r2).toString(16)), pad2(Math.round(g2).toString(16)), pad2(Math.round(b2).toString(16))];
  if (allow3Char && hex[0].charAt(0) == hex[0].charAt(1) && hex[1].charAt(0) == hex[1].charAt(1) && hex[2].charAt(0) == hex[2].charAt(1)) {
    return hex[0].charAt(0) + hex[1].charAt(0) + hex[2].charAt(0);
  }
  return hex.join("");
}
function rgbaToHex(r2, g2, b2, a2, allow4Char) {
  var hex = [pad2(Math.round(r2).toString(16)), pad2(Math.round(g2).toString(16)), pad2(Math.round(b2).toString(16)), pad2(convertDecimalToHex(a2))];
  if (allow4Char && hex[0].charAt(0) == hex[0].charAt(1) && hex[1].charAt(0) == hex[1].charAt(1) && hex[2].charAt(0) == hex[2].charAt(1) && hex[3].charAt(0) == hex[3].charAt(1)) {
    return hex[0].charAt(0) + hex[1].charAt(0) + hex[2].charAt(0) + hex[3].charAt(0);
  }
  return hex.join("");
}
function rgbaToArgbHex(r2, g2, b2, a2) {
  var hex = [pad2(convertDecimalToHex(a2)), pad2(Math.round(r2).toString(16)), pad2(Math.round(g2).toString(16)), pad2(Math.round(b2).toString(16))];
  return hex.join("");
}
tinycolor.equals = function(color1, color2) {
  if (!color1 || !color2)
    return false;
  return tinycolor(color1).toRgbString() == tinycolor(color2).toRgbString();
};
tinycolor.random = function() {
  return tinycolor.fromRatio({
    r: Math.random(),
    g: Math.random(),
    b: Math.random()
  });
};
function _desaturate(color, amount) {
  amount = amount === 0 ? 0 : amount || 10;
  var hsl = tinycolor(color).toHsl();
  hsl.s -= amount / 100;
  hsl.s = clamp01(hsl.s);
  return tinycolor(hsl);
}
function _saturate(color, amount) {
  amount = amount === 0 ? 0 : amount || 10;
  var hsl = tinycolor(color).toHsl();
  hsl.s += amount / 100;
  hsl.s = clamp01(hsl.s);
  return tinycolor(hsl);
}
function _greyscale(color) {
  return tinycolor(color).desaturate(100);
}
function _lighten(color, amount) {
  amount = amount === 0 ? 0 : amount || 10;
  var hsl = tinycolor(color).toHsl();
  hsl.l += amount / 100;
  hsl.l = clamp01(hsl.l);
  return tinycolor(hsl);
}
function _brighten(color, amount) {
  amount = amount === 0 ? 0 : amount || 10;
  var rgb = tinycolor(color).toRgb();
  rgb.r = Math.max(0, Math.min(255, rgb.r - Math.round(255 * -(amount / 100))));
  rgb.g = Math.max(0, Math.min(255, rgb.g - Math.round(255 * -(amount / 100))));
  rgb.b = Math.max(0, Math.min(255, rgb.b - Math.round(255 * -(amount / 100))));
  return tinycolor(rgb);
}
function _darken(color, amount) {
  amount = amount === 0 ? 0 : amount || 10;
  var hsl = tinycolor(color).toHsl();
  hsl.l -= amount / 100;
  hsl.l = clamp01(hsl.l);
  return tinycolor(hsl);
}
function _spin(color, amount) {
  var hsl = tinycolor(color).toHsl();
  var hue = (hsl.h + amount) % 360;
  hsl.h = hue < 0 ? 360 + hue : hue;
  return tinycolor(hsl);
}
function _complement(color) {
  var hsl = tinycolor(color).toHsl();
  hsl.h = (hsl.h + 180) % 360;
  return tinycolor(hsl);
}
function polyad(color, number) {
  if (isNaN(number) || number <= 0) {
    throw new Error("Argument to polyad must be a positive number");
  }
  var hsl = tinycolor(color).toHsl();
  var result = [tinycolor(color)];
  var step = 360 / number;
  for (var i2 = 1; i2 < number; i2++) {
    result.push(tinycolor({
      h: (hsl.h + i2 * step) % 360,
      s: hsl.s,
      l: hsl.l
    }));
  }
  return result;
}
function _splitcomplement(color) {
  var hsl = tinycolor(color).toHsl();
  var h2 = hsl.h;
  return [tinycolor(color), tinycolor({
    h: (h2 + 72) % 360,
    s: hsl.s,
    l: hsl.l
  }), tinycolor({
    h: (h2 + 216) % 360,
    s: hsl.s,
    l: hsl.l
  })];
}
function _analogous(color, results, slices) {
  results = results || 6;
  slices = slices || 30;
  var hsl = tinycolor(color).toHsl();
  var part = 360 / slices;
  var ret = [tinycolor(color)];
  for (hsl.h = (hsl.h - (part * results >> 1) + 720) % 360; --results; ) {
    hsl.h = (hsl.h + part) % 360;
    ret.push(tinycolor(hsl));
  }
  return ret;
}
function _monochromatic(color, results) {
  results = results || 6;
  var hsv = tinycolor(color).toHsv();
  var h2 = hsv.h, s2 = hsv.s, v2 = hsv.v;
  var ret = [];
  var modification = 1 / results;
  while (results--) {
    ret.push(tinycolor({
      h: h2,
      s: s2,
      v: v2
    }));
    v2 = (v2 + modification) % 1;
  }
  return ret;
}
tinycolor.mix = function(color1, color2, amount) {
  amount = amount === 0 ? 0 : amount || 50;
  var rgb1 = tinycolor(color1).toRgb();
  var rgb2 = tinycolor(color2).toRgb();
  var p2 = amount / 100;
  var rgba = {
    r: (rgb2.r - rgb1.r) * p2 + rgb1.r,
    g: (rgb2.g - rgb1.g) * p2 + rgb1.g,
    b: (rgb2.b - rgb1.b) * p2 + rgb1.b,
    a: (rgb2.a - rgb1.a) * p2 + rgb1.a
  };
  return tinycolor(rgba);
};
tinycolor.readability = function(color1, color2) {
  var c1 = tinycolor(color1);
  var c2 = tinycolor(color2);
  return (Math.max(c1.getLuminance(), c2.getLuminance()) + 0.05) / (Math.min(c1.getLuminance(), c2.getLuminance()) + 0.05);
};
tinycolor.isReadable = function(color1, color2, wcag2) {
  var readability = tinycolor.readability(color1, color2);
  var wcag2Parms, out;
  out = false;
  wcag2Parms = validateWCAG2Parms(wcag2);
  switch (wcag2Parms.level + wcag2Parms.size) {
    case "AAsmall":
    case "AAAlarge":
      out = readability >= 4.5;
      break;
    case "AAlarge":
      out = readability >= 3;
      break;
    case "AAAsmall":
      out = readability >= 7;
      break;
  }
  return out;
};
tinycolor.mostReadable = function(baseColor, colorList, args) {
  var bestColor = null;
  var bestScore = 0;
  var readability;
  var includeFallbackColors, level, size;
  args = args || {};
  includeFallbackColors = args.includeFallbackColors;
  level = args.level;
  size = args.size;
  for (var i2 = 0; i2 < colorList.length; i2++) {
    readability = tinycolor.readability(baseColor, colorList[i2]);
    if (readability > bestScore) {
      bestScore = readability;
      bestColor = tinycolor(colorList[i2]);
    }
  }
  if (tinycolor.isReadable(baseColor, bestColor, {
    level,
    size
  }) || !includeFallbackColors) {
    return bestColor;
  } else {
    args.includeFallbackColors = false;
    return tinycolor.mostReadable(baseColor, ["#fff", "#000"], args);
  }
};
var names = tinycolor.names = {
  aliceblue: "f0f8ff",
  antiquewhite: "faebd7",
  aqua: "0ff",
  aquamarine: "7fffd4",
  azure: "f0ffff",
  beige: "f5f5dc",
  bisque: "ffe4c4",
  black: "000",
  blanchedalmond: "ffebcd",
  blue: "00f",
  blueviolet: "8a2be2",
  brown: "a52a2a",
  burlywood: "deb887",
  burntsienna: "ea7e5d",
  cadetblue: "5f9ea0",
  chartreuse: "7fff00",
  chocolate: "d2691e",
  coral: "ff7f50",
  cornflowerblue: "6495ed",
  cornsilk: "fff8dc",
  crimson: "dc143c",
  cyan: "0ff",
  darkblue: "00008b",
  darkcyan: "008b8b",
  darkgoldenrod: "b8860b",
  darkgray: "a9a9a9",
  darkgreen: "006400",
  darkgrey: "a9a9a9",
  darkkhaki: "bdb76b",
  darkmagenta: "8b008b",
  darkolivegreen: "556b2f",
  darkorange: "ff8c00",
  darkorchid: "9932cc",
  darkred: "8b0000",
  darksalmon: "e9967a",
  darkseagreen: "8fbc8f",
  darkslateblue: "483d8b",
  darkslategray: "2f4f4f",
  darkslategrey: "2f4f4f",
  darkturquoise: "00ced1",
  darkviolet: "9400d3",
  deeppink: "ff1493",
  deepskyblue: "00bfff",
  dimgray: "696969",
  dimgrey: "696969",
  dodgerblue: "1e90ff",
  firebrick: "b22222",
  floralwhite: "fffaf0",
  forestgreen: "228b22",
  fuchsia: "f0f",
  gainsboro: "dcdcdc",
  ghostwhite: "f8f8ff",
  gold: "ffd700",
  goldenrod: "daa520",
  gray: "808080",
  green: "008000",
  greenyellow: "adff2f",
  grey: "808080",
  honeydew: "f0fff0",
  hotpink: "ff69b4",
  indianred: "cd5c5c",
  indigo: "4b0082",
  ivory: "fffff0",
  khaki: "f0e68c",
  lavender: "e6e6fa",
  lavenderblush: "fff0f5",
  lawngreen: "7cfc00",
  lemonchiffon: "fffacd",
  lightblue: "add8e6",
  lightcoral: "f08080",
  lightcyan: "e0ffff",
  lightgoldenrodyellow: "fafad2",
  lightgray: "d3d3d3",
  lightgreen: "90ee90",
  lightgrey: "d3d3d3",
  lightpink: "ffb6c1",
  lightsalmon: "ffa07a",
  lightseagreen: "20b2aa",
  lightskyblue: "87cefa",
  lightslategray: "789",
  lightslategrey: "789",
  lightsteelblue: "b0c4de",
  lightyellow: "ffffe0",
  lime: "0f0",
  limegreen: "32cd32",
  linen: "faf0e6",
  magenta: "f0f",
  maroon: "800000",
  mediumaquamarine: "66cdaa",
  mediumblue: "0000cd",
  mediumorchid: "ba55d3",
  mediumpurple: "9370db",
  mediumseagreen: "3cb371",
  mediumslateblue: "7b68ee",
  mediumspringgreen: "00fa9a",
  mediumturquoise: "48d1cc",
  mediumvioletred: "c71585",
  midnightblue: "191970",
  mintcream: "f5fffa",
  mistyrose: "ffe4e1",
  moccasin: "ffe4b5",
  navajowhite: "ffdead",
  navy: "000080",
  oldlace: "fdf5e6",
  olive: "808000",
  olivedrab: "6b8e23",
  orange: "ffa500",
  orangered: "ff4500",
  orchid: "da70d6",
  palegoldenrod: "eee8aa",
  palegreen: "98fb98",
  paleturquoise: "afeeee",
  palevioletred: "db7093",
  papayawhip: "ffefd5",
  peachpuff: "ffdab9",
  peru: "cd853f",
  pink: "ffc0cb",
  plum: "dda0dd",
  powderblue: "b0e0e6",
  purple: "800080",
  rebeccapurple: "663399",
  red: "f00",
  rosybrown: "bc8f8f",
  royalblue: "4169e1",
  saddlebrown: "8b4513",
  salmon: "fa8072",
  sandybrown: "f4a460",
  seagreen: "2e8b57",
  seashell: "fff5ee",
  sienna: "a0522d",
  silver: "c0c0c0",
  skyblue: "87ceeb",
  slateblue: "6a5acd",
  slategray: "708090",
  slategrey: "708090",
  snow: "fffafa",
  springgreen: "00ff7f",
  steelblue: "4682b4",
  tan: "d2b48c",
  teal: "008080",
  thistle: "d8bfd8",
  tomato: "ff6347",
  turquoise: "40e0d0",
  violet: "ee82ee",
  wheat: "f5deb3",
  white: "fff",
  whitesmoke: "f5f5f5",
  yellow: "ff0",
  yellowgreen: "9acd32"
};
var hexNames = tinycolor.hexNames = flip$2(names);
function flip$2(o2) {
  var flipped = {};
  for (var i2 in o2) {
    if (o2.hasOwnProperty(i2)) {
      flipped[o2[i2]] = i2;
    }
  }
  return flipped;
}
function boundAlpha(a2) {
  a2 = parseFloat(a2);
  if (isNaN(a2) || a2 < 0 || a2 > 1) {
    a2 = 1;
  }
  return a2;
}
function bound01(n2, max2) {
  if (isOnePointZero(n2))
    n2 = "100%";
  var processPercent = isPercentage(n2);
  n2 = Math.min(max2, Math.max(0, parseFloat(n2)));
  if (processPercent) {
    n2 = parseInt(n2 * max2, 10) / 100;
  }
  if (Math.abs(n2 - max2) < 1e-6) {
    return 1;
  }
  return n2 % max2 / parseFloat(max2);
}
function clamp01(val) {
  return Math.min(1, Math.max(0, val));
}
function parseIntFromHex(val) {
  return parseInt(val, 16);
}
function isOnePointZero(n2) {
  return typeof n2 == "string" && n2.indexOf(".") != -1 && parseFloat(n2) === 1;
}
function isPercentage(n2) {
  return typeof n2 === "string" && n2.indexOf("%") != -1;
}
function pad2(c2) {
  return c2.length == 1 ? "0" + c2 : "" + c2;
}
function convertToPercentage(n2) {
  if (n2 <= 1) {
    n2 = n2 * 100 + "%";
  }
  return n2;
}
function convertDecimalToHex(d2) {
  return Math.round(parseFloat(d2) * 255).toString(16);
}
function convertHexToDecimal(h2) {
  return parseIntFromHex(h2) / 255;
}
var matchers = function() {
  var CSS_INTEGER = "[-\\+]?\\d+%?";
  var CSS_NUMBER = "[-\\+]?\\d*\\.\\d+%?";
  var CSS_UNIT = "(?:" + CSS_NUMBER + ")|(?:" + CSS_INTEGER + ")";
  var PERMISSIVE_MATCH3 = "[\\s|\\(]+(" + CSS_UNIT + ")[,|\\s]+(" + CSS_UNIT + ")[,|\\s]+(" + CSS_UNIT + ")\\s*\\)?";
  var PERMISSIVE_MATCH4 = "[\\s|\\(]+(" + CSS_UNIT + ")[,|\\s]+(" + CSS_UNIT + ")[,|\\s]+(" + CSS_UNIT + ")[,|\\s]+(" + CSS_UNIT + ")\\s*\\)?";
  return {
    CSS_UNIT: new RegExp(CSS_UNIT),
    rgb: new RegExp("rgb" + PERMISSIVE_MATCH3),
    rgba: new RegExp("rgba" + PERMISSIVE_MATCH4),
    hsl: new RegExp("hsl" + PERMISSIVE_MATCH3),
    hsla: new RegExp("hsla" + PERMISSIVE_MATCH4),
    hsv: new RegExp("hsv" + PERMISSIVE_MATCH3),
    hsva: new RegExp("hsva" + PERMISSIVE_MATCH4),
    hex3: /^#?([0-9a-fA-F]{1})([0-9a-fA-F]{1})([0-9a-fA-F]{1})$/,
    hex6: /^#?([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})$/,
    hex4: /^#?([0-9a-fA-F]{1})([0-9a-fA-F]{1})([0-9a-fA-F]{1})([0-9a-fA-F]{1})$/,
    hex8: /^#?([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})$/
  };
}();
function isValidCSSUnit(color) {
  return !!matchers.CSS_UNIT.exec(color);
}
function stringInputToObject(color) {
  color = color.replace(trimLeft, "").replace(trimRight, "").toLowerCase();
  var named = false;
  if (names[color]) {
    color = names[color];
    named = true;
  } else if (color == "transparent") {
    return {
      r: 0,
      g: 0,
      b: 0,
      a: 0,
      format: "name"
    };
  }
  var match;
  if (match = matchers.rgb.exec(color)) {
    return {
      r: match[1],
      g: match[2],
      b: match[3]
    };
  }
  if (match = matchers.rgba.exec(color)) {
    return {
      r: match[1],
      g: match[2],
      b: match[3],
      a: match[4]
    };
  }
  if (match = matchers.hsl.exec(color)) {
    return {
      h: match[1],
      s: match[2],
      l: match[3]
    };
  }
  if (match = matchers.hsla.exec(color)) {
    return {
      h: match[1],
      s: match[2],
      l: match[3],
      a: match[4]
    };
  }
  if (match = matchers.hsv.exec(color)) {
    return {
      h: match[1],
      s: match[2],
      v: match[3]
    };
  }
  if (match = matchers.hsva.exec(color)) {
    return {
      h: match[1],
      s: match[2],
      v: match[3],
      a: match[4]
    };
  }
  if (match = matchers.hex8.exec(color)) {
    return {
      r: parseIntFromHex(match[1]),
      g: parseIntFromHex(match[2]),
      b: parseIntFromHex(match[3]),
      a: convertHexToDecimal(match[4]),
      format: named ? "name" : "hex8"
    };
  }
  if (match = matchers.hex6.exec(color)) {
    return {
      r: parseIntFromHex(match[1]),
      g: parseIntFromHex(match[2]),
      b: parseIntFromHex(match[3]),
      format: named ? "name" : "hex"
    };
  }
  if (match = matchers.hex4.exec(color)) {
    return {
      r: parseIntFromHex(match[1] + "" + match[1]),
      g: parseIntFromHex(match[2] + "" + match[2]),
      b: parseIntFromHex(match[3] + "" + match[3]),
      a: convertHexToDecimal(match[4] + "" + match[4]),
      format: named ? "name" : "hex8"
    };
  }
  if (match = matchers.hex3.exec(color)) {
    return {
      r: parseIntFromHex(match[1] + "" + match[1]),
      g: parseIntFromHex(match[2] + "" + match[2]),
      b: parseIntFromHex(match[3] + "" + match[3]),
      format: named ? "name" : "hex"
    };
  }
  return false;
}
function validateWCAG2Parms(parms) {
  var level, size;
  parms = parms || {
    level: "AA",
    size: "small"
  };
  level = (parms.level || "AA").toUpperCase();
  size = (parms.size || "small").toLowerCase();
  if (level !== "AA" && level !== "AAA") {
    level = "AA";
  }
  if (size !== "small" && size !== "large") {
    size = "small";
  }
  return {
    level,
    size
  };
}
const double = (num) => {
  return Math.round(num * 100) / 100;
};
class Color {
  constructor(input) {
    __publicField(this, "instance");
    __publicField(this, "alphaValue", 0);
    __publicField(this, "redValue", 0);
    __publicField(this, "greenValue", 0);
    __publicField(this, "blueValue", 0);
    __publicField(this, "hueValue", 0);
    __publicField(this, "saturationValue", 0);
    __publicField(this, "brightnessValue", 0);
    __publicField(this, "hslSaturationValue", 0);
    __publicField(this, "lightnessValue", 0);
    __publicField(this, "initAlpha", () => {
      const initAlpha = this.instance.getAlpha();
      this.alphaValue = Math.min(1, initAlpha) * 100;
    });
    __publicField(this, "initLightness", () => {
      const { s: s2, l: l2 } = this.instance.toHsl();
      this.hslSaturationValue = double(s2);
      this.lightnessValue = double(l2);
    });
    __publicField(this, "initRgb", () => {
      const { r: r2, g: g2, b: b2 } = this.instance.toRgb();
      this.redValue = double(r2);
      this.greenValue = double(g2);
      this.blueValue = double(b2);
    });
    __publicField(this, "initHsb", () => {
      const { h: h2, s: s2, v: v2 } = this.instance.toHsv();
      this.hueValue = Math.min(360, Math.ceil(h2));
      this.saturationValue = double(s2);
      this.brightnessValue = double(v2);
    });
    __publicField(this, "toHexString", () => {
      return this.instance.toHexString();
    });
    __publicField(this, "toRgbString", () => {
      return this.instance.toRgbString();
    });
    this.instance = tinycolor(input);
    this.initRgb();
    this.initHsb();
    this.initLightness();
    this.initAlpha();
  }
  toString(format) {
    return this.instance.toString(format);
  }
  get hex() {
    return this.instance.toHex();
  }
  set hex(hexString) {
    this.instance = tinycolor(hexString);
    this.initHsb();
    this.initRgb();
    this.initAlpha();
    this.initLightness();
  }
  set hue(value) {
    if (this.saturation === 0 && this.brightness === 0) {
      this.saturationValue = 1;
      this.brightnessValue = 1;
    }
    this.instance = tinycolor({
      h: double(value),
      s: this.saturation,
      v: this.brightness,
      a: this.alphaValue / 100
    });
    this.initRgb();
    this.initLightness();
    this.hueValue = double(value);
  }
  get hue() {
    return this.hueValue;
  }
  set saturation(value) {
    this.instance = tinycolor({
      h: this.hue,
      s: double(value),
      v: this.brightness,
      a: this.alphaValue / 100
    });
    this.initRgb();
    this.initLightness();
    this.saturationValue = double(value);
  }
  get saturation() {
    return this.saturationValue;
  }
  set brightness(value) {
    this.instance = tinycolor({
      h: this.hue,
      s: this.saturation,
      v: double(value),
      a: this.alphaValue / 100
    });
    this.initRgb();
    this.initLightness();
    this.brightnessValue = double(value);
  }
  get brightness() {
    return this.brightnessValue;
  }
  set lightness(value) {
    this.instance = tinycolor({
      h: this.hue,
      s: this.hslSaturationValue,
      l: double(value),
      a: this.alphaValue / 100
    });
    this.initRgb();
    this.initHsb();
    this.lightnessValue = double(value);
  }
  get lightness() {
    return this.lightnessValue;
  }
  set red(value) {
    const rgb = this.instance.toRgb();
    this.instance = tinycolor({
      ...rgb,
      r: double(value),
      a: this.alphaValue / 100
    });
    this.initHsb();
    this.initLightness();
    this.redValue = double(value);
  }
  get red() {
    return this.redValue;
  }
  set green(value) {
    const rgb = this.instance.toRgb();
    this.instance = tinycolor({
      ...rgb,
      g: double(value),
      a: this.alphaValue / 100
    });
    this.initHsb();
    this.initLightness();
    this.greenValue = double(value);
  }
  get green() {
    return this.greenValue;
  }
  set blue(value) {
    const rgb = this.instance.toRgb();
    this.instance = tinycolor({
      ...rgb,
      b: double(value),
      a: this.alphaValue / 100
    });
    this.initHsb();
    this.initLightness();
    this.blueValue = double(value);
  }
  get blue() {
    return this.blueValue;
  }
  set alpha(value) {
    this.instance.setAlpha(value / 100);
    this.alphaValue = value;
  }
  get alpha() {
    return this.alphaValue;
  }
  get RGB() {
    return [this.red, this.green, this.blue, this.alpha / 100];
  }
  get HSB() {
    return [this.hue, this.saturation, this.brightness, this.alpha / 100];
  }
  get HSL() {
    return [this.hue, this.hslSaturationValue, this.lightness, this.alpha / 100];
  }
}
function rgbaColor(r2, g2, b2, a2) {
  return `rgba(${[r2, g2, b2, a2 / 100].join(",")})`;
}
const clamp = (value, min2, max2) => {
  return min2 < max2 ? value < min2 ? min2 : value > max2 ? max2 : value : value < max2 ? max2 : value > min2 ? min2 : value;
};
const HistoryColorKey = "color-history";
const MAX_STORAGE_LENGTH = 8;
function tryOnScopeDispose(fn3) {
  if (getCurrentScope()) {
    onScopeDispose(fn3);
    return true;
  }
  return false;
}
const isClient = typeof window !== "undefined";
const isString = (val) => typeof val === "string";
const noop = () => {
};
function createFilterWrapper(filter, fn3) {
  function wrapper(...args) {
    filter(() => fn3.apply(this, args), { fn: fn3, thisArg: this, args });
  }
  return wrapper;
}
const bypassFilter = (invoke) => {
  return invoke();
};
function debounceFilter(ms, options = {}) {
  let timer;
  let maxTimer;
  const filter = (invoke) => {
    const duration = unref(ms);
    const maxDuration = unref(options.maxWait);
    if (timer)
      clearTimeout(timer);
    if (duration <= 0 || maxDuration !== void 0 && maxDuration <= 0) {
      if (maxTimer) {
        clearTimeout(maxTimer);
        maxTimer = null;
      }
      return invoke();
    }
    if (maxDuration && !maxTimer) {
      maxTimer = setTimeout(() => {
        if (timer)
          clearTimeout(timer);
        maxTimer = null;
        invoke();
      }, maxDuration);
    }
    timer = setTimeout(() => {
      if (maxTimer)
        clearTimeout(maxTimer);
      maxTimer = null;
      invoke();
    }, duration);
  };
  return filter;
}
function useDebounceFn(fn3, ms = 200, options = {}) {
  return createFilterWrapper(debounceFilter(ms, options), fn3);
}
var __getOwnPropSymbols$9 = Object.getOwnPropertySymbols;
var __hasOwnProp$9 = Object.prototype.hasOwnProperty;
var __propIsEnum$9 = Object.prototype.propertyIsEnumerable;
var __objRest$5 = (source, exclude) => {
  var target = {};
  for (var prop in source)
    if (__hasOwnProp$9.call(source, prop) && exclude.indexOf(prop) < 0)
      target[prop] = source[prop];
  if (source != null && __getOwnPropSymbols$9)
    for (var prop of __getOwnPropSymbols$9(source)) {
      if (exclude.indexOf(prop) < 0 && __propIsEnum$9.call(source, prop))
        target[prop] = source[prop];
    }
  return target;
};
function watchWithFilter(source, cb, options = {}) {
  const _a = options, {
    eventFilter = bypassFilter
  } = _a, watchOptions = __objRest$5(_a, [
    "eventFilter"
  ]);
  return watch(source, createFilterWrapper(eventFilter, cb), watchOptions);
}
function tryOnMounted(fn3, sync = true) {
  if (getCurrentInstance())
    onMounted(fn3);
  else if (sync)
    fn3();
  else
    nextTick(fn3);
}
function whenever(source, cb, options) {
  return watch(source, (v2, ov, onInvalidate) => {
    if (v2)
      cb(v2, ov, onInvalidate);
  }, options);
}
function unrefElement(elRef) {
  var _a;
  const plain = unref(elRef);
  return (_a = plain == null ? void 0 : plain.$el) != null ? _a : plain;
}
const defaultWindow = isClient ? window : void 0;
function useEventListener(...args) {
  let target;
  let event;
  let listener;
  let options;
  if (isString(args[0])) {
    [event, listener, options] = args;
    target = defaultWindow;
  } else {
    [target, event, listener, options] = args;
  }
  if (!target)
    return noop;
  let cleanup = noop;
  const stopWatch = watch(() => unref(target), (el) => {
    cleanup();
    if (!el)
      return;
    el.addEventListener(event, listener, options);
    cleanup = () => {
      el.removeEventListener(event, listener, options);
      cleanup = noop;
    };
  }, { immediate: true, flush: "post" });
  const stop = () => {
    stopWatch();
    cleanup();
  };
  tryOnScopeDispose(stop);
  return stop;
}
function onClickOutside(target, handler, options = {}) {
  const { window: window2 = defaultWindow, event = "pointerdown" } = options;
  if (!window2)
    return;
  const listener = (event2) => {
    const el = unrefElement(target);
    if (!el)
      return;
    if (el === event2.target || event2.composedPath().includes(el))
      return;
    handler(event2);
  };
  return useEventListener(window2, event, listener, { passive: true });
}
const StorageSerializers = {
  boolean: {
    read: (v2) => v2 === "true",
    write: (v2) => String(v2)
  },
  object: {
    read: (v2) => JSON.parse(v2),
    write: (v2) => JSON.stringify(v2)
  },
  number: {
    read: (v2) => Number.parseFloat(v2),
    write: (v2) => String(v2)
  },
  any: {
    read: (v2) => v2,
    write: (v2) => String(v2)
  },
  string: {
    read: (v2) => v2,
    write: (v2) => String(v2)
  },
  map: {
    read: (v2) => new Map(JSON.parse(v2)),
    write: (v2) => JSON.stringify(Array.from(v2.entries()))
  },
  set: {
    read: (v2) => new Set(JSON.parse(v2)),
    write: (v2) => JSON.stringify(Array.from(v2.entries()))
  }
};
function useStorage(key, initialValue, storage = ((_a) => (_a = defaultWindow) == null ? void 0 : _a.localStorage)(), options = {}) {
  var _a2;
  const {
    flush = "pre",
    deep = true,
    listenToStorageChanges = true,
    writeDefaults = true,
    shallow,
    window: window2 = defaultWindow,
    eventFilter,
    onError = (e2) => {
      console.error(e2);
    }
  } = options;
  const rawInit = unref(initialValue);
  const type = rawInit == null ? "any" : rawInit instanceof Set ? "set" : rawInit instanceof Map ? "map" : typeof rawInit === "boolean" ? "boolean" : typeof rawInit === "string" ? "string" : typeof rawInit === "object" ? "object" : Array.isArray(rawInit) ? "object" : !Number.isNaN(rawInit) ? "number" : "any";
  const data = (shallow ? shallowRef : ref)(initialValue);
  const serializer = (_a2 = options.serializer) != null ? _a2 : StorageSerializers[type];
  function read2(event) {
    if (!storage || event && event.key !== key)
      return;
    try {
      const rawValue = event ? event.newValue : storage.getItem(key);
      if (rawValue == null) {
        data.value = rawInit;
        if (writeDefaults && rawInit !== null)
          storage.setItem(key, serializer.write(rawInit));
      } else {
        data.value = serializer.read(rawValue);
      }
    } catch (e2) {
      onError(e2);
    }
  }
  read2();
  if (window2 && listenToStorageChanges)
    useEventListener(window2, "storage", (e2) => setTimeout(() => read2(e2), 0));
  if (storage) {
    watchWithFilter(data, () => {
      try {
        if (data.value == null)
          storage.removeItem(key);
        else
          storage.setItem(key, serializer.write(data.value));
      } catch (e2) {
        onError(e2);
      }
    }, {
      flush,
      deep,
      eventFilter
    });
  }
  return data;
}
function useLocalStorage(key, initialValue, options = {}) {
  const { window: window2 = defaultWindow } = options;
  return useStorage(key, initialValue, window2 == null ? void 0 : window2.localStorage, options);
}
var SwipeDirection;
(function(SwipeDirection2) {
  SwipeDirection2["UP"] = "UP";
  SwipeDirection2["RIGHT"] = "RIGHT";
  SwipeDirection2["DOWN"] = "DOWN";
  SwipeDirection2["LEFT"] = "LEFT";
  SwipeDirection2["NONE"] = "NONE";
})(SwipeDirection || (SwipeDirection = {}));
var t = "undefined" != typeof globalThis ? globalThis : "undefined" != typeof window ? window : "undefined" != typeof global ? global : "undefined" != typeof self ? self : {};
function e(t2) {
  var e2 = { exports: {} };
  return t2(e2, e2.exports), e2.exports;
}
var n = function(t2) {
  return t2 && t2.Math == Math && t2;
}, r = n("object" == typeof globalThis && globalThis) || n("object" == typeof window && window) || n("object" == typeof self && self) || n("object" == typeof t && t) || function() {
  return this;
}() || Function("return this")(), o = function(t2) {
  try {
    return !!t2();
  } catch (t3) {
    return true;
  }
}, i = !o(function() {
  return 7 != Object.defineProperty({}, 1, { get: function() {
    return 7;
  } })[1];
}), u = {}.propertyIsEnumerable, a = Object.getOwnPropertyDescriptor, c = { f: a && !u.call({ 1: 2 }, 1) ? function(t2) {
  var e2 = a(this, t2);
  return !!e2 && e2.enumerable;
} : u }, l = function(t2, e2) {
  return { enumerable: !(1 & t2), configurable: !(2 & t2), writable: !(4 & t2), value: e2 };
}, f = {}.toString, s = function(t2) {
  return f.call(t2).slice(8, -1);
}, d = "".split, v = o(function() {
  return !Object("z").propertyIsEnumerable(0);
}) ? function(t2) {
  return "String" == s(t2) ? d.call(t2, "") : Object(t2);
} : Object, p = function(t2) {
  if (null == t2)
    throw TypeError("Can't call method on " + t2);
  return t2;
}, g = function(t2) {
  return v(p(t2));
}, h = function(t2) {
  return "object" == typeof t2 ? null !== t2 : "function" == typeof t2;
}, y = function(t2, e2) {
  if (!h(t2))
    return t2;
  var n2, r2;
  if (e2 && "function" == typeof (n2 = t2.toString) && !h(r2 = n2.call(t2)))
    return r2;
  if ("function" == typeof (n2 = t2.valueOf) && !h(r2 = n2.call(t2)))
    return r2;
  if (!e2 && "function" == typeof (n2 = t2.toString) && !h(r2 = n2.call(t2)))
    return r2;
  throw TypeError("Can't convert object to primitive value");
}, m = {}.hasOwnProperty, S = function(t2, e2) {
  return m.call(t2, e2);
}, x = r.document, b = h(x) && h(x.createElement), E = function(t2) {
  return b ? x.createElement(t2) : {};
}, w = !i && !o(function() {
  return 7 != Object.defineProperty(E("div"), "a", { get: function() {
    return 7;
  } }).a;
}), O = Object.getOwnPropertyDescriptor, T = { f: i ? O : function(t2, e2) {
  if (t2 = g(t2), e2 = y(e2, true), w)
    try {
      return O(t2, e2);
    } catch (t3) {
    }
  if (S(t2, e2))
    return l(!c.f.call(t2, e2), t2[e2]);
} }, A = function(t2) {
  if (!h(t2))
    throw TypeError(String(t2) + " is not an object");
  return t2;
}, k = Object.defineProperty, R = { f: i ? k : function(t2, e2, n2) {
  if (A(t2), e2 = y(e2, true), A(n2), w)
    try {
      return k(t2, e2, n2);
    } catch (t3) {
    }
  if ("get" in n2 || "set" in n2)
    throw TypeError("Accessors not supported");
  return "value" in n2 && (t2[e2] = n2.value), t2;
} }, I = i ? function(t2, e2, n2) {
  return R.f(t2, e2, l(1, n2));
} : function(t2, e2, n2) {
  return t2[e2] = n2, t2;
}, j = function(t2, e2) {
  try {
    I(r, t2, e2);
  } catch (n2) {
    r[t2] = e2;
  }
  return e2;
}, C = r["__core-js_shared__"] || j("__core-js_shared__", {}), L = Function.toString;
"function" != typeof C.inspectSource && (C.inspectSource = function(t2) {
  return L.call(t2);
});
var P, M, _, D = C.inspectSource, U = r.WeakMap, N = "function" == typeof U && /native code/.test(D(U)), F = e(function(t2) {
  (t2.exports = function(t3, e2) {
    return C[t3] || (C[t3] = void 0 !== e2 ? e2 : {});
  })("versions", []).push({ version: "3.8.3", mode: "global", copyright: "\xA9 2021 Denis Pushkarev (zloirock.ru)" });
}), W = 0, z = Math.random(), $ = function(t2) {
  return "Symbol(" + String(void 0 === t2 ? "" : t2) + ")_" + (++W + z).toString(36);
}, B = F("keys"), Y = function(t2) {
  return B[t2] || (B[t2] = $(t2));
}, G = {}, H = r.WeakMap;
if (N) {
  var X = C.state || (C.state = new H()), V = X.get, K = X.has, q = X.set;
  P = function(t2, e2) {
    return e2.facade = t2, q.call(X, t2, e2), e2;
  }, M = function(t2) {
    return V.call(X, t2) || {};
  }, _ = function(t2) {
    return K.call(X, t2);
  };
} else {
  var Q = Y("state");
  G[Q] = true, P = function(t2, e2) {
    return e2.facade = t2, I(t2, Q, e2), e2;
  }, M = function(t2) {
    return S(t2, Q) ? t2[Q] : {};
  }, _ = function(t2) {
    return S(t2, Q);
  };
}
var J = { set: P, get: M, has: _, enforce: function(t2) {
  return _(t2) ? M(t2) : P(t2, {});
}, getterFor: function(t2) {
  return function(e2) {
    var n2;
    if (!h(e2) || (n2 = M(e2)).type !== t2)
      throw TypeError("Incompatible receiver, " + t2 + " required");
    return n2;
  };
} }, Z = e(function(t2) {
  var e2 = J.get, n2 = J.enforce, o2 = String(String).split("String");
  (t2.exports = function(t3, e3, i2, u2) {
    var a2, c2 = !!u2 && !!u2.unsafe, l2 = !!u2 && !!u2.enumerable, f2 = !!u2 && !!u2.noTargetGet;
    "function" == typeof i2 && ("string" != typeof e3 || S(i2, "name") || I(i2, "name", e3), (a2 = n2(i2)).source || (a2.source = o2.join("string" == typeof e3 ? e3 : ""))), t3 !== r ? (c2 ? !f2 && t3[e3] && (l2 = true) : delete t3[e3], l2 ? t3[e3] = i2 : I(t3, e3, i2)) : l2 ? t3[e3] = i2 : j(e3, i2);
  })(Function.prototype, "toString", function() {
    return "function" == typeof this && e2(this).source || D(this);
  });
}), tt = r, et = function(t2) {
  return "function" == typeof t2 ? t2 : void 0;
}, nt = function(t2, e2) {
  return arguments.length < 2 ? et(tt[t2]) || et(r[t2]) : tt[t2] && tt[t2][e2] || r[t2] && r[t2][e2];
}, rt = Math.ceil, ot = Math.floor, it = function(t2) {
  return isNaN(t2 = +t2) ? 0 : (t2 > 0 ? ot : rt)(t2);
}, ut = Math.min, at = function(t2) {
  return t2 > 0 ? ut(it(t2), 9007199254740991) : 0;
}, ct = Math.max, lt = Math.min, ft = function(t2, e2) {
  var n2 = it(t2);
  return n2 < 0 ? ct(n2 + e2, 0) : lt(n2, e2);
}, st = function(t2) {
  return function(e2, n2, r2) {
    var o2, i2 = g(e2), u2 = at(i2.length), a2 = ft(r2, u2);
    if (t2 && n2 != n2) {
      for (; u2 > a2; )
        if ((o2 = i2[a2++]) != o2)
          return true;
    } else
      for (; u2 > a2; a2++)
        if ((t2 || a2 in i2) && i2[a2] === n2)
          return t2 || a2 || 0;
    return !t2 && -1;
  };
}, dt = { includes: st(true), indexOf: st(false) }, vt = dt.indexOf, pt = function(t2, e2) {
  var n2, r2 = g(t2), o2 = 0, i2 = [];
  for (n2 in r2)
    !S(G, n2) && S(r2, n2) && i2.push(n2);
  for (; e2.length > o2; )
    S(r2, n2 = e2[o2++]) && (~vt(i2, n2) || i2.push(n2));
  return i2;
}, gt = ["constructor", "hasOwnProperty", "isPrototypeOf", "propertyIsEnumerable", "toLocaleString", "toString", "valueOf"], ht = gt.concat("length", "prototype"), yt = { f: Object.getOwnPropertyNames || function(t2) {
  return pt(t2, ht);
} }, mt = { f: Object.getOwnPropertySymbols }, St = nt("Reflect", "ownKeys") || function(t2) {
  var e2 = yt.f(A(t2)), n2 = mt.f;
  return n2 ? e2.concat(n2(t2)) : e2;
}, xt = function(t2, e2) {
  for (var n2 = St(e2), r2 = R.f, o2 = T.f, i2 = 0; i2 < n2.length; i2++) {
    var u2 = n2[i2];
    S(t2, u2) || r2(t2, u2, o2(e2, u2));
  }
}, bt = /#|\.prototype\./, Et = function(t2, e2) {
  var n2 = Ot[wt(t2)];
  return n2 == At || n2 != Tt && ("function" == typeof e2 ? o(e2) : !!e2);
}, wt = Et.normalize = function(t2) {
  return String(t2).replace(bt, ".").toLowerCase();
}, Ot = Et.data = {}, Tt = Et.NATIVE = "N", At = Et.POLYFILL = "P", kt = Et, Rt = T.f, It = function(t2, e2) {
  var n2, o2, i2, u2, a2, c2 = t2.target, l2 = t2.global, f2 = t2.stat;
  if (n2 = l2 ? r : f2 ? r[c2] || j(c2, {}) : (r[c2] || {}).prototype)
    for (o2 in e2) {
      if (u2 = e2[o2], i2 = t2.noTargetGet ? (a2 = Rt(n2, o2)) && a2.value : n2[o2], !kt(l2 ? o2 : c2 + (f2 ? "." : "#") + o2, t2.forced) && void 0 !== i2) {
        if (typeof u2 == typeof i2)
          continue;
        xt(u2, i2);
      }
      (t2.sham || i2 && i2.sham) && I(u2, "sham", true), Z(n2, o2, u2, t2);
    }
}, jt = function(t2, e2) {
  var n2 = [][t2];
  return !!n2 && o(function() {
    n2.call(null, e2 || function() {
      throw 1;
    }, 1);
  });
}, Ct = Object.defineProperty, Lt = {}, Pt = function(t2) {
  throw t2;
}, Mt = function(t2, e2) {
  if (S(Lt, t2))
    return Lt[t2];
  e2 || (e2 = {});
  var n2 = [][t2], r2 = !!S(e2, "ACCESSORS") && e2.ACCESSORS, u2 = S(e2, 0) ? e2[0] : Pt, a2 = S(e2, 1) ? e2[1] : void 0;
  return Lt[t2] = !!n2 && !o(function() {
    if (r2 && !i)
      return true;
    var t3 = { length: -1 };
    r2 ? Ct(t3, 1, { enumerable: true, get: Pt }) : t3[1] = 1, n2.call(t3, u2, a2);
  });
}, _t = dt.indexOf, Dt = [].indexOf, Ut = !!Dt && 1 / [1].indexOf(1, -0) < 0, Nt = jt("indexOf"), Ft = Mt("indexOf", { ACCESSORS: true, 1: 0 });
function Wt(t2, e2) {
  if (!(t2 instanceof e2))
    throw new TypeError("Cannot call a class as a function");
}
function zt(t2, e2) {
  for (var n2 = 0; n2 < e2.length; n2++) {
    var r2 = e2[n2];
    r2.enumerable = r2.enumerable || false, r2.configurable = true, "value" in r2 && (r2.writable = true), Object.defineProperty(t2, r2.key, r2);
  }
}
function $t(t2, e2, n2) {
  return e2 && zt(t2.prototype, e2), n2 && zt(t2, n2), t2;
}
It({ target: "Array", proto: true, forced: Ut || !Nt || !Ft }, { indexOf: function(t2) {
  return Ut ? Dt.apply(this, arguments) || 0 : _t(this, t2, arguments.length > 1 ? arguments[1] : void 0);
} });
(function() {
  function t2() {
    Wt(this, t2);
  }
  return $t(t2, null, [{ key: "isInBrowser", value: function() {
    return "undefined" != typeof window;
  } }, { key: "isServer", value: function() {
    return "undefined" == typeof window;
  } }, { key: "getUA", value: function() {
    return t2.isInBrowser() ? window.navigator.userAgent.toLowerCase() : "";
  } }, { key: "isMobile", value: function() {
    return /Mobile|mini|Fennec|Android|iP(ad|od|hone)/.test(navigator.appVersion);
  } }, { key: "isOpera", value: function() {
    return -1 !== navigator.userAgent.indexOf("Opera");
  } }, { key: "isIE", value: function() {
    var e2 = t2.getUA();
    return "" !== e2 && e2.indexOf("msie") > 0;
  } }, { key: "isIE9", value: function() {
    var e2 = t2.getUA();
    return "" !== e2 && e2.indexOf("msie 9.0") > 0;
  } }, { key: "isEdge", value: function() {
    var e2 = t2.getUA();
    return "" !== e2 && e2.indexOf("edge/") > 0;
  } }, { key: "isChrome", value: function() {
    var e2 = t2.getUA();
    return "" !== e2 && /chrome\/\d+/.test(e2) && !t2.isEdge();
  } }, { key: "isPhantomJS", value: function() {
    var e2 = t2.getUA();
    return "" !== e2 && /phantomjs/.test(e2);
  } }, { key: "isFirefox", value: function() {
    var e2 = t2.getUA();
    return "" !== e2 && /firefox/.test(e2);
  } }]), t2;
})();
var Yt = [].join, Gt = v != Object, Ht = jt("join", ",");
It({ target: "Array", proto: true, forced: Gt || !Ht }, { join: function(t2) {
  return Yt.call(g(this), void 0 === t2 ? "," : t2);
} });
var Xt, Vt, Kt = function(t2) {
  return Object(p(t2));
}, qt = Array.isArray || function(t2) {
  return "Array" == s(t2);
}, Qt = !!Object.getOwnPropertySymbols && !o(function() {
  return !String(Symbol());
}), Jt = Qt && !Symbol.sham && "symbol" == typeof Symbol.iterator, Zt = F("wks"), te = r.Symbol, ee = Jt ? te : te && te.withoutSetter || $, ne = function(t2) {
  return S(Zt, t2) || (Qt && S(te, t2) ? Zt[t2] = te[t2] : Zt[t2] = ee("Symbol." + t2)), Zt[t2];
}, re = ne("species"), oe = function(t2, e2) {
  var n2;
  return qt(t2) && ("function" != typeof (n2 = t2.constructor) || n2 !== Array && !qt(n2.prototype) ? h(n2) && null === (n2 = n2[re]) && (n2 = void 0) : n2 = void 0), new (void 0 === n2 ? Array : n2)(0 === e2 ? 0 : e2);
}, ie = function(t2, e2, n2) {
  var r2 = y(e2);
  r2 in t2 ? R.f(t2, r2, l(0, n2)) : t2[r2] = n2;
}, ue = nt("navigator", "userAgent") || "", ae = r.process, ce = ae && ae.versions, le = ce && ce.v8;
le ? Vt = (Xt = le.split("."))[0] + Xt[1] : ue && (!(Xt = ue.match(/Edge\/(\d+)/)) || Xt[1] >= 74) && (Xt = ue.match(/Chrome\/(\d+)/)) && (Vt = Xt[1]);
var fe = Vt && +Vt, se = ne("species"), de = function(t2) {
  return fe >= 51 || !o(function() {
    var e2 = [];
    return (e2.constructor = {})[se] = function() {
      return { foo: 1 };
    }, 1 !== e2[t2](Boolean).foo;
  });
}, ve = de("splice"), pe = Mt("splice", { ACCESSORS: true, 0: 0, 1: 2 }), ge = Math.max, he = Math.min;
It({ target: "Array", proto: true, forced: !ve || !pe }, { splice: function(t2, e2) {
  var n2, r2, o2, i2, u2, a2, c2 = Kt(this), l2 = at(c2.length), f2 = ft(t2, l2), s2 = arguments.length;
  if (0 === s2 ? n2 = r2 = 0 : 1 === s2 ? (n2 = 0, r2 = l2 - f2) : (n2 = s2 - 2, r2 = he(ge(it(e2), 0), l2 - f2)), l2 + n2 - r2 > 9007199254740991)
    throw TypeError("Maximum allowed length exceeded");
  for (o2 = oe(c2, r2), i2 = 0; i2 < r2; i2++)
    (u2 = f2 + i2) in c2 && ie(o2, i2, c2[u2]);
  if (o2.length = r2, n2 < r2) {
    for (i2 = f2; i2 < l2 - r2; i2++)
      a2 = i2 + n2, (u2 = i2 + r2) in c2 ? c2[a2] = c2[u2] : delete c2[a2];
    for (i2 = l2; i2 > l2 - r2 + n2; i2--)
      delete c2[i2 - 1];
  } else if (n2 > r2)
    for (i2 = l2 - r2; i2 > f2; i2--)
      a2 = i2 + n2 - 1, (u2 = i2 + r2 - 1) in c2 ? c2[a2] = c2[u2] : delete c2[a2];
  for (i2 = 0; i2 < n2; i2++)
    c2[i2 + f2] = arguments[i2 + 2];
  return c2.length = l2 - r2 + n2, o2;
} });
var ye = {};
ye[ne("toStringTag")] = "z";
var me = "[object z]" === String(ye), Se = ne("toStringTag"), xe = "Arguments" == s(function() {
  return arguments;
}()), be = me ? s : function(t2) {
  var e2, n2, r2;
  return void 0 === t2 ? "Undefined" : null === t2 ? "Null" : "string" == typeof (n2 = function(t3, e3) {
    try {
      return t3[e3];
    } catch (t4) {
    }
  }(e2 = Object(t2), Se)) ? n2 : xe ? s(e2) : "Object" == (r2 = s(e2)) && "function" == typeof e2.callee ? "Arguments" : r2;
}, Ee = me ? {}.toString : function() {
  return "[object " + be(this) + "]";
};
me || Z(Object.prototype, "toString", Ee, { unsafe: true });
var we = function() {
  var t2 = A(this), e2 = "";
  return t2.global && (e2 += "g"), t2.ignoreCase && (e2 += "i"), t2.multiline && (e2 += "m"), t2.dotAll && (e2 += "s"), t2.unicode && (e2 += "u"), t2.sticky && (e2 += "y"), e2;
};
function Oe(t2, e2) {
  return RegExp(t2, e2);
}
var Te, Ae, ke = { UNSUPPORTED_Y: o(function() {
  var t2 = Oe("a", "y");
  return t2.lastIndex = 2, null != t2.exec("abcd");
}), BROKEN_CARET: o(function() {
  var t2 = Oe("^r", "gy");
  return t2.lastIndex = 2, null != t2.exec("str");
}) }, Re = RegExp.prototype.exec, Ie = String.prototype.replace, je = Re, Ce = (Te = /a/, Ae = /b*/g, Re.call(Te, "a"), Re.call(Ae, "a"), 0 !== Te.lastIndex || 0 !== Ae.lastIndex), Le = ke.UNSUPPORTED_Y || ke.BROKEN_CARET, Pe = void 0 !== /()??/.exec("")[1];
(Ce || Pe || Le) && (je = function(t2) {
  var e2, n2, r2, o2, i2 = this, u2 = Le && i2.sticky, a2 = we.call(i2), c2 = i2.source, l2 = 0, f2 = t2;
  return u2 && (-1 === (a2 = a2.replace("y", "")).indexOf("g") && (a2 += "g"), f2 = String(t2).slice(i2.lastIndex), i2.lastIndex > 0 && (!i2.multiline || i2.multiline && "\n" !== t2[i2.lastIndex - 1]) && (c2 = "(?: " + c2 + ")", f2 = " " + f2, l2++), n2 = new RegExp("^(?:" + c2 + ")", a2)), Pe && (n2 = new RegExp("^" + c2 + "$(?!\\s)", a2)), Ce && (e2 = i2.lastIndex), r2 = Re.call(u2 ? n2 : i2, f2), u2 ? r2 ? (r2.input = r2.input.slice(l2), r2[0] = r2[0].slice(l2), r2.index = i2.lastIndex, i2.lastIndex += r2[0].length) : i2.lastIndex = 0 : Ce && r2 && (i2.lastIndex = i2.global ? r2.index + r2[0].length : e2), Pe && r2 && r2.length > 1 && Ie.call(r2[0], n2, function() {
    for (o2 = 1; o2 < arguments.length - 2; o2++)
      void 0 === arguments[o2] && (r2[o2] = void 0);
  }), r2;
});
var Me = je;
It({ target: "RegExp", proto: true, forced: /./.exec !== Me }, { exec: Me });
var _e = RegExp.prototype, De = _e.toString, Ue = o(function() {
  return "/a/b" != De.call({ source: "a", flags: "b" });
}), Ne = "toString" != De.name;
(Ue || Ne) && Z(RegExp.prototype, "toString", function() {
  var t2 = A(this), e2 = String(t2.source), n2 = t2.flags;
  return "/" + e2 + "/" + String(void 0 === n2 && t2 instanceof RegExp && !("flags" in _e) ? we.call(t2) : n2);
}, { unsafe: true });
var Fe = ne("species"), We = !o(function() {
  var t2 = /./;
  return t2.exec = function() {
    var t3 = [];
    return t3.groups = { a: "7" }, t3;
  }, "7" !== "".replace(t2, "$<a>");
}), ze = "$0" === "a".replace(/./, "$0"), $e = ne("replace"), Be = !!/./[$e] && "" === /./[$e]("a", "$0"), Ye = !o(function() {
  var t2 = /(?:)/, e2 = t2.exec;
  t2.exec = function() {
    return e2.apply(this, arguments);
  };
  var n2 = "ab".split(t2);
  return 2 !== n2.length || "a" !== n2[0] || "b" !== n2[1];
}), Ge = function(t2, e2, n2, r2) {
  var i2 = ne(t2), u2 = !o(function() {
    var e3 = {};
    return e3[i2] = function() {
      return 7;
    }, 7 != ""[t2](e3);
  }), a2 = u2 && !o(function() {
    var e3 = false, n3 = /a/;
    return "split" === t2 && ((n3 = {}).constructor = {}, n3.constructor[Fe] = function() {
      return n3;
    }, n3.flags = "", n3[i2] = /./[i2]), n3.exec = function() {
      return e3 = true, null;
    }, n3[i2](""), !e3;
  });
  if (!u2 || !a2 || "replace" === t2 && (!We || !ze || Be) || "split" === t2 && !Ye) {
    var c2 = /./[i2], l2 = n2(i2, ""[t2], function(t3, e3, n3, r3, o2) {
      return e3.exec === Me ? u2 && !o2 ? { done: true, value: c2.call(e3, n3, r3) } : { done: true, value: t3.call(n3, e3, r3) } : { done: false };
    }, { REPLACE_KEEPS_$0: ze, REGEXP_REPLACE_SUBSTITUTES_UNDEFINED_CAPTURE: Be }), f2 = l2[0], s2 = l2[1];
    Z(String.prototype, t2, f2), Z(RegExp.prototype, i2, 2 == e2 ? function(t3, e3) {
      return s2.call(t3, this, e3);
    } : function(t3) {
      return s2.call(t3, this);
    });
  }
  r2 && I(RegExp.prototype[i2], "sham", true);
}, He = ne("match"), Xe = function(t2) {
  var e2;
  return h(t2) && (void 0 !== (e2 = t2[He]) ? !!e2 : "RegExp" == s(t2));
}, Ve = function(t2) {
  if ("function" != typeof t2)
    throw TypeError(String(t2) + " is not a function");
  return t2;
}, Ke = ne("species"), qe = function(t2) {
  return function(e2, n2) {
    var r2, o2, i2 = String(p(e2)), u2 = it(n2), a2 = i2.length;
    return u2 < 0 || u2 >= a2 ? t2 ? "" : void 0 : (r2 = i2.charCodeAt(u2)) < 55296 || r2 > 56319 || u2 + 1 === a2 || (o2 = i2.charCodeAt(u2 + 1)) < 56320 || o2 > 57343 ? t2 ? i2.charAt(u2) : r2 : t2 ? i2.slice(u2, u2 + 2) : o2 - 56320 + (r2 - 55296 << 10) + 65536;
  };
}, Qe = { codeAt: qe(false), charAt: qe(true) }, Je = Qe.charAt, Ze = function(t2, e2, n2) {
  return e2 + (n2 ? Je(t2, e2).length : 1);
}, tn = function(t2, e2) {
  var n2 = t2.exec;
  if ("function" == typeof n2) {
    var r2 = n2.call(t2, e2);
    if ("object" != typeof r2)
      throw TypeError("RegExp exec method returned something other than an Object or null");
    return r2;
  }
  if ("RegExp" !== s(t2))
    throw TypeError("RegExp#exec called on incompatible receiver");
  return Me.call(t2, e2);
}, en = [].push, nn = Math.min, rn = !o(function() {
  return !RegExp(4294967295, "y");
});
Ge("split", 2, function(t2, e2, n2) {
  var r2;
  return r2 = "c" == "abbc".split(/(b)*/)[1] || 4 != "test".split(/(?:)/, -1).length || 2 != "ab".split(/(?:ab)*/).length || 4 != ".".split(/(.?)(.?)/).length || ".".split(/()()/).length > 1 || "".split(/.?/).length ? function(t3, n3) {
    var r3 = String(p(this)), o2 = void 0 === n3 ? 4294967295 : n3 >>> 0;
    if (0 === o2)
      return [];
    if (void 0 === t3)
      return [r3];
    if (!Xe(t3))
      return e2.call(r3, t3, o2);
    for (var i2, u2, a2, c2 = [], l2 = (t3.ignoreCase ? "i" : "") + (t3.multiline ? "m" : "") + (t3.unicode ? "u" : "") + (t3.sticky ? "y" : ""), f2 = 0, s2 = new RegExp(t3.source, l2 + "g"); (i2 = Me.call(s2, r3)) && !((u2 = s2.lastIndex) > f2 && (c2.push(r3.slice(f2, i2.index)), i2.length > 1 && i2.index < r3.length && en.apply(c2, i2.slice(1)), a2 = i2[0].length, f2 = u2, c2.length >= o2)); )
      s2.lastIndex === i2.index && s2.lastIndex++;
    return f2 === r3.length ? !a2 && s2.test("") || c2.push("") : c2.push(r3.slice(f2)), c2.length > o2 ? c2.slice(0, o2) : c2;
  } : "0".split(void 0, 0).length ? function(t3, n3) {
    return void 0 === t3 && 0 === n3 ? [] : e2.call(this, t3, n3);
  } : e2, [function(e3, n3) {
    var o2 = p(this), i2 = null == e3 ? void 0 : e3[t2];
    return void 0 !== i2 ? i2.call(e3, o2, n3) : r2.call(String(o2), e3, n3);
  }, function(t3, o2) {
    var i2 = n2(r2, t3, this, o2, r2 !== e2);
    if (i2.done)
      return i2.value;
    var u2 = A(t3), a2 = String(this), c2 = function(t4, e3) {
      var n3, r3 = A(t4).constructor;
      return void 0 === r3 || null == (n3 = A(r3)[Ke]) ? e3 : Ve(n3);
    }(u2, RegExp), l2 = u2.unicode, f2 = (u2.ignoreCase ? "i" : "") + (u2.multiline ? "m" : "") + (u2.unicode ? "u" : "") + (rn ? "y" : "g"), s2 = new c2(rn ? u2 : "^(?:" + u2.source + ")", f2), d2 = void 0 === o2 ? 4294967295 : o2 >>> 0;
    if (0 === d2)
      return [];
    if (0 === a2.length)
      return null === tn(s2, a2) ? [a2] : [];
    for (var v2 = 0, p2 = 0, g2 = []; p2 < a2.length; ) {
      s2.lastIndex = rn ? p2 : 0;
      var h2, y2 = tn(s2, rn ? a2 : a2.slice(p2));
      if (null === y2 || (h2 = nn(at(s2.lastIndex + (rn ? 0 : p2)), a2.length)) === v2)
        p2 = Ze(a2, p2, l2);
      else {
        if (g2.push(a2.slice(v2, p2)), g2.length === d2)
          return g2;
        for (var m2 = 1; m2 <= y2.length - 1; m2++)
          if (g2.push(y2[m2]), g2.length === d2)
            return g2;
        p2 = v2 = h2;
      }
    }
    return g2.push(a2.slice(v2)), g2;
  }];
}, !rn);
var on = "	\n\v\f\r \xA0\u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u202F\u205F\u3000\u2028\u2029\uFEFF", un = "[" + on + "]", an = RegExp("^" + un + un + "*"), cn = RegExp(un + un + "*$"), ln = function(t2) {
  return function(e2) {
    var n2 = String(p(e2));
    return 1 & t2 && (n2 = n2.replace(an, "")), 2 & t2 && (n2 = n2.replace(cn, "")), n2;
  };
}, fn = { start: ln(1), end: ln(2), trim: ln(3) }, sn = fn.trim;
It({ target: "String", proto: true, forced: function(t2) {
  return o(function() {
    return !!on[t2]() || "\u200B\x85\u180E" != "\u200B\x85\u180E"[t2]() || on[t2].name !== t2;
  });
}("trim") }, { trim: function() {
  return sn(this);
} });
var dn = de("slice"), vn = Mt("slice", { ACCESSORS: true, 0: 0, 1: 2 }), pn = ne("species"), gn = [].slice, hn = Math.max;
It({ target: "Array", proto: true, forced: !dn || !vn }, { slice: function(t2, e2) {
  var n2, r2, o2, i2 = g(this), u2 = at(i2.length), a2 = ft(t2, u2), c2 = ft(void 0 === e2 ? u2 : e2, u2);
  if (qt(i2) && ("function" != typeof (n2 = i2.constructor) || n2 !== Array && !qt(n2.prototype) ? h(n2) && null === (n2 = n2[pn]) && (n2 = void 0) : n2 = void 0, n2 === Array || void 0 === n2))
    return gn.call(i2, a2, c2);
  for (r2 = new (void 0 === n2 ? Array : n2)(hn(c2 - a2, 0)), o2 = 0; a2 < c2; a2++, o2++)
    a2 in i2 && ie(r2, o2, i2[a2]);
  return r2.length = o2, r2;
} });
var yn = Object.keys || function(t2) {
  return pt(t2, gt);
}, mn = o(function() {
  yn(1);
});
It({ target: "Object", stat: true, forced: mn }, { keys: function(t2) {
  return yn(Kt(t2));
} });
var Sn, xn = function(t2) {
  if (Xe(t2))
    throw TypeError("The method doesn't accept regular expressions");
  return t2;
}, bn = ne("match"), En = T.f, wn = "".startsWith, On = Math.min, Tn = function(t2) {
  var e2 = /./;
  try {
    "/./"[t2](e2);
  } catch (n2) {
    try {
      return e2[bn] = false, "/./"[t2](e2);
    } catch (t3) {
    }
  }
  return false;
}("startsWith"), An = !(Tn || (Sn = En(String.prototype, "startsWith"), !Sn || Sn.writable));
function kn(t2) {
  return (kn = "function" == typeof Symbol && "symbol" == typeof Symbol.iterator ? function(t3) {
    return typeof t3;
  } : function(t3) {
    return t3 && "function" == typeof Symbol && t3.constructor === Symbol && t3 !== Symbol.prototype ? "symbol" : typeof t3;
  })(t2);
}
It({ target: "String", proto: true, forced: !An && !Tn }, { startsWith: function(t2) {
  var e2 = String(p(this));
  xn(t2);
  var n2 = at(On(arguments.length > 1 ? arguments[1] : void 0, e2.length)), r2 = String(t2);
  return wn ? wn.call(e2, r2, n2) : e2.slice(n2, n2 + r2.length) === r2;
} });
var jn = function(t2) {
  return "string" == typeof t2;
}, Mn = function(t2) {
  return null !== t2 && "object" === kn(t2);
}, Vn = function() {
  function t2() {
    Wt(this, t2);
  }
  return $t(t2, null, [{ key: "isWindow", value: function(t3) {
    return t3 === window;
  } }, { key: "addEventListener", value: function(t3, e2, n2) {
    var r2 = arguments.length > 3 && void 0 !== arguments[3] && arguments[3];
    t3 && e2 && n2 && t3.addEventListener(e2, n2, r2);
  } }, { key: "removeEventListener", value: function(t3, e2, n2) {
    var r2 = arguments.length > 3 && void 0 !== arguments[3] && arguments[3];
    t3 && e2 && n2 && t3.removeEventListener(e2, n2, r2);
  } }, { key: "triggerDragEvent", value: function(e2, n2) {
    var r2 = false, o2 = function(t3) {
      var e3;
      null === (e3 = n2.drag) || void 0 === e3 || e3.call(n2, t3);
    }, i2 = function e3(i3) {
      var u2;
      t2.removeEventListener(document, "mousemove", o2), t2.removeEventListener(document, "mouseup", e3), document.onselectstart = null, document.ondragstart = null, r2 = false, null === (u2 = n2.end) || void 0 === u2 || u2.call(n2, i3);
    };
    t2.addEventListener(e2, "mousedown", function(e3) {
      var u2;
      r2 || (document.onselectstart = function() {
        return false;
      }, document.ondragstart = function() {
        return false;
      }, t2.addEventListener(document, "mousemove", o2), t2.addEventListener(document, "mouseup", i2), r2 = true, null === (u2 = n2.start) || void 0 === u2 || u2.call(n2, e3));
    });
  } }, { key: "getBoundingClientRect", value: function(t3) {
    return t3 && Mn(t3) && 1 === t3.nodeType ? t3.getBoundingClientRect() : null;
  } }, { key: "hasClass", value: function(t3, e2) {
    return !!(t3 && Mn(t3) && jn(e2) && 1 === t3.nodeType) && t3.classList.contains(e2.trim());
  } }, { key: "addClass", value: function(e2, n2) {
    if (e2 && Mn(e2) && jn(n2) && 1 === e2.nodeType && (n2 = n2.trim(), !t2.hasClass(e2, n2))) {
      var r2 = e2.className;
      e2.className = r2 ? r2 + " " + n2 : n2;
    }
  } }, { key: "removeClass", value: function(t3, e2) {
    if (t3 && Mn(t3) && jn(e2) && 1 === t3.nodeType && "string" == typeof t3.className) {
      e2 = e2.trim();
      for (var n2 = t3.className.trim().split(" "), r2 = n2.length - 1; r2 >= 0; r2--)
        n2[r2] = n2[r2].trim(), n2[r2] && n2[r2] !== e2 || n2.splice(r2, 1);
      t3.className = n2.join(" ");
    }
  } }, { key: "toggleClass", value: function(t3, e2, n2) {
    t3 && Mn(t3) && jn(e2) && 1 === t3.nodeType && t3.classList.toggle(e2, n2);
  } }, { key: "replaceClass", value: function(e2, n2, r2) {
    e2 && Mn(e2) && jn(n2) && jn(r2) && 1 === e2.nodeType && (n2 = n2.trim(), r2 = r2.trim(), t2.removeClass(e2, n2), t2.addClass(e2, r2));
  } }, { key: "getScrollTop", value: function(t3) {
    var e2 = "scrollTop" in t3 ? t3.scrollTop : t3.pageYOffset;
    return Math.max(e2, 0);
  } }, { key: "setScrollTop", value: function(t3, e2) {
    "scrollTop" in t3 ? t3.scrollTop = e2 : t3.scrollTo(t3.scrollX, e2);
  } }, { key: "getRootScrollTop", value: function() {
    return window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0;
  } }, { key: "setRootScrollTop", value: function(e2) {
    t2.setScrollTop(window, e2), t2.setScrollTop(document.body, e2);
  } }, { key: "getElementTop", value: function(e2, n2) {
    if (t2.isWindow(e2))
      return 0;
    var r2 = n2 ? t2.getScrollTop(n2) : t2.getRootScrollTop();
    return e2.getBoundingClientRect().top + r2;
  } }, { key: "getVisibleHeight", value: function(e2) {
    return t2.isWindow(e2) ? e2.innerHeight : e2.getBoundingClientRect().height;
  } }, { key: "isHidden", value: function(t3) {
    if (!t3)
      return false;
    var e2 = window.getComputedStyle(t3), n2 = "none" === e2.display, r2 = null === t3.offsetParent && "fixed" !== e2.position;
    return n2 || r2;
  } }, { key: "triggerEvent", value: function(t3, e2) {
    if ("createEvent" in document) {
      var n2 = document.createEvent("HTMLEvents");
      n2.initEvent(e2, false, true), t3.dispatchEvent(n2);
    }
  } }, { key: "calcAngle", value: function(t3, e2) {
    var n2 = t3.getBoundingClientRect(), r2 = n2.left + n2.width / 2, o2 = n2.top + n2.height / 2, i2 = Math.abs(r2 - e2.clientX), u2 = Math.abs(o2 - e2.clientY), a2 = u2 / Math.sqrt(Math.pow(i2, 2) + Math.pow(u2, 2)), c2 = Math.acos(a2), l2 = Math.floor(180 / (Math.PI / c2));
    return e2.clientX > r2 && e2.clientY > o2 && (l2 = 180 - l2), e2.clientX == r2 && e2.clientY > o2 && (l2 = 180), e2.clientX > r2 && e2.clientY == o2 && (l2 = 90), e2.clientX < r2 && e2.clientY > o2 && (l2 = 180 + l2), e2.clientX < r2 && e2.clientY == o2 && (l2 = 270), e2.clientX < r2 && e2.clientY < o2 && (l2 = 360 - l2), l2;
  } }, { key: "querySelector", value: function(t3, e2) {
    return e2 ? e2.querySelector(t3) : document.querySelector(t3);
  } }, { key: "createElement", value: function(t3) {
    for (var e2 = document.createElement(t3), n2 = arguments.length, r2 = new Array(n2 > 1 ? n2 - 1 : 0), o2 = 1; o2 < n2; o2++)
      r2[o2 - 1] = arguments[o2];
    for (var i2 = 0; i2 < r2.length; i2++)
      r2[i2] && e2.classList.add(r2[i2]);
    return e2;
  } }, { key: "appendChild", value: function(t3) {
    for (var e2 = 0; e2 < (arguments.length <= 1 ? 0 : arguments.length - 1); e2++)
      t3.appendChild(e2 + 1 < 1 || arguments.length <= e2 + 1 ? void 0 : arguments[e2 + 1]);
  } }, { key: "getWindow", value: function(t3) {
    if ("[object Window]" !== t3.toString()) {
      var e2 = t3.ownerDocument;
      return e2 && e2.defaultView || window;
    }
    return t3;
  } }, { key: "isElement", value: function(t3) {
    return t3 instanceof this.getWindow(t3).Element || t3 instanceof Element;
  } }, { key: "isHTMLElement", value: function(t3) {
    return t3 instanceof this.getWindow(t3).HTMLElement || t3 instanceof HTMLElement;
  } }, { key: "isShadowRoot", value: function(t3) {
    return "undefined" != typeof ShadowRoot && (t3 instanceof this.getWindow(t3).ShadowRoot || t3 instanceof ShadowRoot);
  } }, { key: "getWindowScroll", value: function(t3) {
    var e2 = this.getWindow(t3);
    return { scrollLeft: e2.pageXOffset || 0, scrollTop: e2.pageYOffset || 0 };
  } }]), t2;
}(), Kn = Math.floor, qn = "".replace, Qn = /\$([$&'`]|\d\d?|<[^>]*>)/g, Jn = /\$([$&'`]|\d\d?)/g, Zn = function(t2, e2, n2, r2, o2, i2) {
  var u2 = n2 + t2.length, a2 = r2.length, c2 = Jn;
  return void 0 !== o2 && (o2 = Kt(o2), c2 = Qn), qn.call(i2, c2, function(i3, c3) {
    var l2;
    switch (c3.charAt(0)) {
      case "$":
        return "$";
      case "&":
        return t2;
      case "`":
        return e2.slice(0, n2);
      case "'":
        return e2.slice(u2);
      case "<":
        l2 = o2[c3.slice(1, -1)];
        break;
      default:
        var f2 = +c3;
        if (0 === f2)
          return i3;
        if (f2 > a2) {
          var s2 = Kn(f2 / 10);
          return 0 === s2 ? i3 : s2 <= a2 ? void 0 === r2[s2 - 1] ? c3.charAt(1) : r2[s2 - 1] + c3.charAt(1) : i3;
        }
        l2 = r2[f2 - 1];
    }
    return void 0 === l2 ? "" : l2;
  });
}, tr = Math.max, er = Math.min;
Ge("replace", 2, function(t2, e2, n2, r2) {
  var o2 = r2.REGEXP_REPLACE_SUBSTITUTES_UNDEFINED_CAPTURE, i2 = r2.REPLACE_KEEPS_$0, u2 = o2 ? "$" : "$0";
  return [function(n3, r3) {
    var o3 = p(this), i3 = null == n3 ? void 0 : n3[t2];
    return void 0 !== i3 ? i3.call(n3, o3, r3) : e2.call(String(o3), n3, r3);
  }, function(t3, r3) {
    if (!o2 && i2 || "string" == typeof r3 && -1 === r3.indexOf(u2)) {
      var a2 = n2(e2, t3, this, r3);
      if (a2.done)
        return a2.value;
    }
    var c2 = A(t3), l2 = String(this), f2 = "function" == typeof r3;
    f2 || (r3 = String(r3));
    var s2 = c2.global;
    if (s2) {
      var d2 = c2.unicode;
      c2.lastIndex = 0;
    }
    for (var v2 = []; ; ) {
      var p2 = tn(c2, l2);
      if (null === p2)
        break;
      if (v2.push(p2), !s2)
        break;
      "" === String(p2[0]) && (c2.lastIndex = Ze(l2, at(c2.lastIndex), d2));
    }
    for (var g2, h2 = "", y2 = 0, m2 = 0; m2 < v2.length; m2++) {
      p2 = v2[m2];
      for (var S2 = String(p2[0]), x2 = tr(er(it(p2.index), l2.length), 0), b2 = [], E2 = 1; E2 < p2.length; E2++)
        b2.push(void 0 === (g2 = p2[E2]) ? g2 : String(g2));
      var w2 = p2.groups;
      if (f2) {
        var O2 = [S2].concat(b2, x2, l2);
        void 0 !== w2 && O2.push(w2);
        var T2 = String(r3.apply(void 0, O2));
      } else
        T2 = Zn(S2, l2, x2, b2, w2, r3);
      x2 >= y2 && (h2 += l2.slice(y2, x2) + T2, y2 = x2 + S2.length);
    }
    return h2 + l2.slice(y2);
  }];
});
(function() {
  function t2() {
    Wt(this, t2);
  }
  return $t(t2, null, [{ key: "camelize", value: function(t3) {
    return t3.replace(/-(\w)/g, function(t4, e2) {
      return e2 ? e2.toUpperCase() : "";
    });
  } }, { key: "capitalize", value: function(t3) {
    return t3.charAt(0).toUpperCase() + t3.slice(1);
  } }]), t2;
})();
(function() {
  function t2() {
    Wt(this, t2);
  }
  return $t(t2, null, [{ key: "_clone", value: function() {
  } }]), t2;
})();
var or = ne("isConcatSpreadable"), ir = fe >= 51 || !o(function() {
  var t2 = [];
  return t2[or] = false, t2.concat()[0] !== t2;
}), ur = de("concat"), ar = function(t2) {
  if (!h(t2))
    return false;
  var e2 = t2[or];
  return void 0 !== e2 ? !!e2 : qt(t2);
};
It({ target: "Array", proto: true, forced: !ir || !ur }, { concat: function(t2) {
  var e2, n2, r2, o2, i2, u2 = Kt(this), a2 = oe(u2, 0), c2 = 0;
  for (e2 = -1, r2 = arguments.length; e2 < r2; e2++)
    if (ar(i2 = -1 === e2 ? u2 : arguments[e2])) {
      if (c2 + (o2 = at(i2.length)) > 9007199254740991)
        throw TypeError("Maximum allowed index exceeded");
      for (n2 = 0; n2 < o2; n2++, c2++)
        n2 in i2 && ie(a2, c2, i2[n2]);
    } else {
      if (c2 >= 9007199254740991)
        throw TypeError("Maximum allowed index exceeded");
      ie(a2, c2++, i2);
    }
  return a2.length = c2, a2;
} });
var cr, lr = function(t2, e2, n2) {
  if (Ve(t2), void 0 === e2)
    return t2;
  switch (n2) {
    case 0:
      return function() {
        return t2.call(e2);
      };
    case 1:
      return function(n3) {
        return t2.call(e2, n3);
      };
    case 2:
      return function(n3, r2) {
        return t2.call(e2, n3, r2);
      };
    case 3:
      return function(n3, r2, o2) {
        return t2.call(e2, n3, r2, o2);
      };
  }
  return function() {
    return t2.apply(e2, arguments);
  };
}, fr = [].push, sr = function(t2) {
  var e2 = 1 == t2, n2 = 2 == t2, r2 = 3 == t2, o2 = 4 == t2, i2 = 6 == t2, u2 = 7 == t2, a2 = 5 == t2 || i2;
  return function(c2, l2, f2, s2) {
    for (var d2, p2, g2 = Kt(c2), h2 = v(g2), y2 = lr(l2, f2, 3), m2 = at(h2.length), S2 = 0, x2 = s2 || oe, b2 = e2 ? x2(c2, m2) : n2 || u2 ? x2(c2, 0) : void 0; m2 > S2; S2++)
      if ((a2 || S2 in h2) && (p2 = y2(d2 = h2[S2], S2, g2), t2))
        if (e2)
          b2[S2] = p2;
        else if (p2)
          switch (t2) {
            case 3:
              return true;
            case 5:
              return d2;
            case 6:
              return S2;
            case 2:
              fr.call(b2, d2);
          }
        else
          switch (t2) {
            case 4:
              return false;
            case 7:
              fr.call(b2, d2);
          }
    return i2 ? -1 : r2 || o2 ? o2 : b2;
  };
}, dr = { forEach: sr(0), map: sr(1), filter: sr(2), some: sr(3), every: sr(4), find: sr(5), findIndex: sr(6), filterOut: sr(7) }, vr = i ? Object.defineProperties : function(t2, e2) {
  A(t2);
  for (var n2, r2 = yn(e2), o2 = r2.length, i2 = 0; o2 > i2; )
    R.f(t2, n2 = r2[i2++], e2[n2]);
  return t2;
}, pr = nt("document", "documentElement"), gr = Y("IE_PROTO"), hr = function() {
}, yr = function(t2) {
  return "<script>" + t2 + "<\/script>";
}, mr = function() {
  try {
    cr = document.domain && new ActiveXObject("htmlfile");
  } catch (t3) {
  }
  var t2, e2;
  mr = cr ? function(t3) {
    t3.write(yr("")), t3.close();
    var e3 = t3.parentWindow.Object;
    return t3 = null, e3;
  }(cr) : ((e2 = E("iframe")).style.display = "none", pr.appendChild(e2), e2.src = String("javascript:"), (t2 = e2.contentWindow.document).open(), t2.write(yr("document.F=Object")), t2.close(), t2.F);
  for (var n2 = gt.length; n2--; )
    delete mr.prototype[gt[n2]];
  return mr();
};
G[gr] = true;
var Sr = Object.create || function(t2, e2) {
  var n2;
  return null !== t2 ? (hr.prototype = A(t2), n2 = new hr(), hr.prototype = null, n2[gr] = t2) : n2 = mr(), void 0 === e2 ? n2 : vr(n2, e2);
}, xr = ne("unscopables"), br = Array.prototype;
null == br[xr] && R.f(br, xr, { configurable: true, value: Sr(null) });
var Er = function(t2) {
  br[xr][t2] = true;
}, wr = dr.find, Or = true, Tr = Mt("find");
"find" in [] && Array(1).find(function() {
  Or = false;
}), It({ target: "Array", proto: true, forced: Or || !Tr }, { find: function(t2) {
  return wr(this, t2, arguments.length > 1 ? arguments[1] : void 0);
} }), Er("find");
var Ar = dr.findIndex, kr = true, Rr = Mt("findIndex");
"findIndex" in [] && Array(1).findIndex(function() {
  kr = false;
}), It({ target: "Array", proto: true, forced: kr || !Rr }, { findIndex: function(t2) {
  return Ar(this, t2, arguments.length > 1 ? arguments[1] : void 0);
} }), Er("findIndex");
var Ir = function(t2, e2, n2, r2, o2, i2, u2, a2) {
  for (var c2, l2 = o2, f2 = 0, s2 = !!u2 && lr(u2, a2, 3); f2 < r2; ) {
    if (f2 in n2) {
      if (c2 = s2 ? s2(n2[f2], f2, e2) : n2[f2], i2 > 0 && qt(c2))
        l2 = Ir(t2, e2, c2, at(c2.length), l2, i2 - 1) - 1;
      else {
        if (l2 >= 9007199254740991)
          throw TypeError("Exceed the acceptable array length");
        t2[l2] = c2;
      }
      l2++;
    }
    f2++;
  }
  return l2;
}, jr = Ir;
It({ target: "Array", proto: true }, { flat: function() {
  var t2 = arguments.length ? arguments[0] : void 0, e2 = Kt(this), n2 = at(e2.length), r2 = oe(e2, 0);
  return r2.length = jr(r2, e2, e2, n2, 0, void 0 === t2 ? 1 : it(t2)), r2;
} });
var Cr = function(t2) {
  var e2 = t2.return;
  if (void 0 !== e2)
    return A(e2.call(t2)).value;
}, Lr = function(t2, e2, n2, r2) {
  try {
    return r2 ? e2(A(n2)[0], n2[1]) : e2(n2);
  } catch (e3) {
    throw Cr(t2), e3;
  }
}, Pr = {}, Mr = ne("iterator"), _r = Array.prototype, Dr = function(t2) {
  return void 0 !== t2 && (Pr.Array === t2 || _r[Mr] === t2);
}, Ur = ne("iterator"), Nr = function(t2) {
  if (null != t2)
    return t2[Ur] || t2["@@iterator"] || Pr[be(t2)];
}, Fr = ne("iterator"), Wr = false;
try {
  var zr = 0, $r = { next: function() {
    return { done: !!zr++ };
  }, return: function() {
    Wr = true;
  } };
  $r[Fr] = function() {
    return this;
  }, Array.from($r, function() {
    throw 2;
  });
} catch (t2) {
}
var Br = function(t2, e2) {
  if (!e2 && !Wr)
    return false;
  var n2 = false;
  try {
    var r2 = {};
    r2[Fr] = function() {
      return { next: function() {
        return { done: n2 = true };
      } };
    }, t2(r2);
  } catch (t3) {
  }
  return n2;
}, Yr = !Br(function(t2) {
  Array.from(t2);
});
It({ target: "Array", stat: true, forced: Yr }, { from: function(t2) {
  var e2, n2, r2, o2, i2, u2, a2 = Kt(t2), c2 = "function" == typeof this ? this : Array, l2 = arguments.length, f2 = l2 > 1 ? arguments[1] : void 0, s2 = void 0 !== f2, d2 = Nr(a2), v2 = 0;
  if (s2 && (f2 = lr(f2, l2 > 2 ? arguments[2] : void 0, 2)), null == d2 || c2 == Array && Dr(d2))
    for (n2 = new c2(e2 = at(a2.length)); e2 > v2; v2++)
      u2 = s2 ? f2(a2[v2], v2) : a2[v2], ie(n2, v2, u2);
  else
    for (i2 = (o2 = d2.call(a2)).next, n2 = new c2(); !(r2 = i2.call(o2)).done; v2++)
      u2 = s2 ? Lr(o2, f2, [r2.value, v2], true) : r2.value, ie(n2, v2, u2);
  return n2.length = v2, n2;
} });
var Gr = function(t2) {
  return function(e2, n2, r2, o2) {
    Ve(n2);
    var i2 = Kt(e2), u2 = v(i2), a2 = at(i2.length), c2 = t2 ? a2 - 1 : 0, l2 = t2 ? -1 : 1;
    if (r2 < 2)
      for (; ; ) {
        if (c2 in u2) {
          o2 = u2[c2], c2 += l2;
          break;
        }
        if (c2 += l2, t2 ? c2 < 0 : a2 <= c2)
          throw TypeError("Reduce of empty array with no initial value");
      }
    for (; t2 ? c2 >= 0 : a2 > c2; c2 += l2)
      c2 in u2 && (o2 = n2(o2, u2[c2], c2, i2));
    return o2;
  };
}, Hr = { left: Gr(false), right: Gr(true) }, Xr = "process" == s(r.process), Vr = Hr.left, Kr = jt("reduce"), qr = Mt("reduce", { 1: 0 });
It({ target: "Array", proto: true, forced: !Kr || !qr || !Xr && fe > 79 && fe < 83 }, { reduce: function(t2) {
  return Vr(this, t2, arguments.length, arguments.length > 1 ? arguments[1] : void 0);
} }), Er("flat");
var Qr, Jr, Zr, to = !o(function() {
  return Object.isExtensible(Object.preventExtensions({}));
}), eo = e(function(t2) {
  var e2 = R.f, n2 = $("meta"), r2 = 0, o2 = Object.isExtensible || function() {
    return true;
  }, i2 = function(t3) {
    e2(t3, n2, { value: { objectID: "O" + ++r2, weakData: {} } });
  }, u2 = t2.exports = { REQUIRED: false, fastKey: function(t3, e3) {
    if (!h(t3))
      return "symbol" == typeof t3 ? t3 : ("string" == typeof t3 ? "S" : "P") + t3;
    if (!S(t3, n2)) {
      if (!o2(t3))
        return "F";
      if (!e3)
        return "E";
      i2(t3);
    }
    return t3[n2].objectID;
  }, getWeakData: function(t3, e3) {
    if (!S(t3, n2)) {
      if (!o2(t3))
        return true;
      if (!e3)
        return false;
      i2(t3);
    }
    return t3[n2].weakData;
  }, onFreeze: function(t3) {
    return to && u2.REQUIRED && o2(t3) && !S(t3, n2) && i2(t3), t3;
  } };
  G[n2] = true;
}), no = function(t2, e2) {
  this.stopped = t2, this.result = e2;
}, ro = function(t2, e2, n2) {
  var r2, o2, i2, u2, a2, c2, l2, f2 = n2 && n2.that, s2 = !(!n2 || !n2.AS_ENTRIES), d2 = !(!n2 || !n2.IS_ITERATOR), v2 = !(!n2 || !n2.INTERRUPTED), p2 = lr(e2, f2, 1 + s2 + v2), g2 = function(t3) {
    return r2 && Cr(r2), new no(true, t3);
  }, h2 = function(t3) {
    return s2 ? (A(t3), v2 ? p2(t3[0], t3[1], g2) : p2(t3[0], t3[1])) : v2 ? p2(t3, g2) : p2(t3);
  };
  if (d2)
    r2 = t2;
  else {
    if ("function" != typeof (o2 = Nr(t2)))
      throw TypeError("Target is not iterable");
    if (Dr(o2)) {
      for (i2 = 0, u2 = at(t2.length); u2 > i2; i2++)
        if ((a2 = h2(t2[i2])) && a2 instanceof no)
          return a2;
      return new no(false);
    }
    r2 = o2.call(t2);
  }
  for (c2 = r2.next; !(l2 = c2.call(r2)).done; ) {
    try {
      a2 = h2(l2.value);
    } catch (t3) {
      throw Cr(r2), t3;
    }
    if ("object" == typeof a2 && a2 && a2 instanceof no)
      return a2;
  }
  return new no(false);
}, oo = function(t2, e2, n2) {
  if (!(t2 instanceof e2))
    throw TypeError("Incorrect " + (n2 ? n2 + " " : "") + "invocation");
  return t2;
}, io = R.f, uo = ne("toStringTag"), ao = function(t2, e2, n2) {
  t2 && !S(t2 = n2 ? t2 : t2.prototype, uo) && io(t2, uo, { configurable: true, value: e2 });
}, co = Object.setPrototypeOf || ("__proto__" in {} ? function() {
  var t2, e2 = false, n2 = {};
  try {
    (t2 = Object.getOwnPropertyDescriptor(Object.prototype, "__proto__").set).call(n2, []), e2 = n2 instanceof Array;
  } catch (t3) {
  }
  return function(n3, r2) {
    return A(n3), function(t3) {
      if (!h(t3) && null !== t3)
        throw TypeError("Can't set " + String(t3) + " as a prototype");
    }(r2), e2 ? t2.call(n3, r2) : n3.__proto__ = r2, n3;
  };
}() : void 0), lo = function(t2, e2, n2) {
  for (var r2 in e2)
    Z(t2, r2, e2[r2], n2);
  return t2;
}, fo = !o(function() {
  function t2() {
  }
  return t2.prototype.constructor = null, Object.getPrototypeOf(new t2()) !== t2.prototype;
}), so = Y("IE_PROTO"), vo = Object.prototype, po = fo ? Object.getPrototypeOf : function(t2) {
  return t2 = Kt(t2), S(t2, so) ? t2[so] : "function" == typeof t2.constructor && t2 instanceof t2.constructor ? t2.constructor.prototype : t2 instanceof Object ? vo : null;
}, go = ne("iterator"), ho = false;
[].keys && ("next" in (Zr = [].keys()) ? (Jr = po(po(Zr))) !== Object.prototype && (Qr = Jr) : ho = true), (null == Qr || o(function() {
  var t2 = {};
  return Qr[go].call(t2) !== t2;
})) && (Qr = {}), S(Qr, go) || I(Qr, go, function() {
  return this;
});
var yo = { IteratorPrototype: Qr, BUGGY_SAFARI_ITERATORS: ho }, mo = yo.IteratorPrototype, So = function() {
  return this;
}, xo = yo.IteratorPrototype, bo = yo.BUGGY_SAFARI_ITERATORS, Eo = ne("iterator"), wo = function() {
  return this;
}, Oo = function(t2, e2, n2, r2, o2, i2, u2) {
  !function(t3, e3, n3) {
    var r3 = e3 + " Iterator";
    t3.prototype = Sr(mo, { next: l(1, n3) }), ao(t3, r3, false), Pr[r3] = So;
  }(n2, e2, r2);
  var a2, c2, f2, s2 = function(t3) {
    if (t3 === o2 && h2)
      return h2;
    if (!bo && t3 in p2)
      return p2[t3];
    switch (t3) {
      case "keys":
      case "values":
      case "entries":
        return function() {
          return new n2(this, t3);
        };
    }
    return function() {
      return new n2(this);
    };
  }, d2 = e2 + " Iterator", v2 = false, p2 = t2.prototype, g2 = p2[Eo] || p2["@@iterator"] || o2 && p2[o2], h2 = !bo && g2 || s2(o2), y2 = "Array" == e2 && p2.entries || g2;
  if (y2 && (a2 = po(y2.call(new t2())), xo !== Object.prototype && a2.next && (po(a2) !== xo && (co ? co(a2, xo) : "function" != typeof a2[Eo] && I(a2, Eo, wo)), ao(a2, d2, true))), "values" == o2 && g2 && "values" !== g2.name && (v2 = true, h2 = function() {
    return g2.call(this);
  }), p2[Eo] !== h2 && I(p2, Eo, h2), Pr[e2] = h2, o2)
    if (c2 = { values: s2("values"), keys: i2 ? h2 : s2("keys"), entries: s2("entries") }, u2)
      for (f2 in c2)
        (bo || v2 || !(f2 in p2)) && Z(p2, f2, c2[f2]);
    else
      It({ target: e2, proto: true, forced: bo || v2 }, c2);
  return c2;
}, To = ne("species"), Ao = R.f, ko = eo.fastKey, Ro = J.set, Io = J.getterFor;
!function(t2, e2, n2) {
  var i2 = -1 !== t2.indexOf("Map"), u2 = -1 !== t2.indexOf("Weak"), a2 = i2 ? "set" : "add", c2 = r[t2], l2 = c2 && c2.prototype, f2 = c2, s2 = {}, d2 = function(t3) {
    var e3 = l2[t3];
    Z(l2, t3, "add" == t3 ? function(t4) {
      return e3.call(this, 0 === t4 ? 0 : t4), this;
    } : "delete" == t3 ? function(t4) {
      return !(u2 && !h(t4)) && e3.call(this, 0 === t4 ? 0 : t4);
    } : "get" == t3 ? function(t4) {
      return u2 && !h(t4) ? void 0 : e3.call(this, 0 === t4 ? 0 : t4);
    } : "has" == t3 ? function(t4) {
      return !(u2 && !h(t4)) && e3.call(this, 0 === t4 ? 0 : t4);
    } : function(t4, n3) {
      return e3.call(this, 0 === t4 ? 0 : t4, n3), this;
    });
  };
  if (kt(t2, "function" != typeof c2 || !(u2 || l2.forEach && !o(function() {
    new c2().entries().next();
  }))))
    f2 = n2.getConstructor(e2, t2, i2, a2), eo.REQUIRED = true;
  else if (kt(t2, true)) {
    var v2 = new f2(), p2 = v2[a2](u2 ? {} : -0, 1) != v2, g2 = o(function() {
      v2.has(1);
    }), y2 = Br(function(t3) {
      new c2(t3);
    }), m2 = !u2 && o(function() {
      for (var t3 = new c2(), e3 = 5; e3--; )
        t3[a2](e3, e3);
      return !t3.has(-0);
    });
    y2 || ((f2 = e2(function(e3, n3) {
      oo(e3, f2, t2);
      var r2 = function(t3, e4, n4) {
        var r3, o2;
        return co && "function" == typeof (r3 = e4.constructor) && r3 !== n4 && h(o2 = r3.prototype) && o2 !== n4.prototype && co(t3, o2), t3;
      }(new c2(), e3, f2);
      return null != n3 && ro(n3, r2[a2], { that: r2, AS_ENTRIES: i2 }), r2;
    })).prototype = l2, l2.constructor = f2), (g2 || m2) && (d2("delete"), d2("has"), i2 && d2("get")), (m2 || p2) && d2(a2), u2 && l2.clear && delete l2.clear;
  }
  s2[t2] = f2, It({ global: true, forced: f2 != c2 }, s2), ao(f2, t2), u2 || n2.setStrong(f2, t2, i2);
}("Set", function(t2) {
  return function() {
    return t2(this, arguments.length ? arguments[0] : void 0);
  };
}, { getConstructor: function(t2, e2, n2, r2) {
  var o2 = t2(function(t3, u3) {
    oo(t3, o2, e2), Ro(t3, { type: e2, index: Sr(null), first: void 0, last: void 0, size: 0 }), i || (t3.size = 0), null != u3 && ro(u3, t3[r2], { that: t3, AS_ENTRIES: n2 });
  }), u2 = Io(e2), a2 = function(t3, e3, n3) {
    var r3, o3, a3 = u2(t3), l2 = c2(t3, e3);
    return l2 ? l2.value = n3 : (a3.last = l2 = { index: o3 = ko(e3, true), key: e3, value: n3, previous: r3 = a3.last, next: void 0, removed: false }, a3.first || (a3.first = l2), r3 && (r3.next = l2), i ? a3.size++ : t3.size++, "F" !== o3 && (a3.index[o3] = l2)), t3;
  }, c2 = function(t3, e3) {
    var n3, r3 = u2(t3), o3 = ko(e3);
    if ("F" !== o3)
      return r3.index[o3];
    for (n3 = r3.first; n3; n3 = n3.next)
      if (n3.key == e3)
        return n3;
  };
  return lo(o2.prototype, { clear: function() {
    for (var t3 = u2(this), e3 = t3.index, n3 = t3.first; n3; )
      n3.removed = true, n3.previous && (n3.previous = n3.previous.next = void 0), delete e3[n3.index], n3 = n3.next;
    t3.first = t3.last = void 0, i ? t3.size = 0 : this.size = 0;
  }, delete: function(t3) {
    var e3 = this, n3 = u2(e3), r3 = c2(e3, t3);
    if (r3) {
      var o3 = r3.next, a3 = r3.previous;
      delete n3.index[r3.index], r3.removed = true, a3 && (a3.next = o3), o3 && (o3.previous = a3), n3.first == r3 && (n3.first = o3), n3.last == r3 && (n3.last = a3), i ? n3.size-- : e3.size--;
    }
    return !!r3;
  }, forEach: function(t3) {
    for (var e3, n3 = u2(this), r3 = lr(t3, arguments.length > 1 ? arguments[1] : void 0, 3); e3 = e3 ? e3.next : n3.first; )
      for (r3(e3.value, e3.key, this); e3 && e3.removed; )
        e3 = e3.previous;
  }, has: function(t3) {
    return !!c2(this, t3);
  } }), lo(o2.prototype, n2 ? { get: function(t3) {
    var e3 = c2(this, t3);
    return e3 && e3.value;
  }, set: function(t3, e3) {
    return a2(this, 0 === t3 ? 0 : t3, e3);
  } } : { add: function(t3) {
    return a2(this, t3 = 0 === t3 ? 0 : t3, t3);
  } }), i && Ao(o2.prototype, "size", { get: function() {
    return u2(this).size;
  } }), o2;
}, setStrong: function(t2, e2, n2) {
  var r2 = e2 + " Iterator", o2 = Io(e2), u2 = Io(r2);
  Oo(t2, e2, function(t3, e3) {
    Ro(this, { type: r2, target: t3, state: o2(t3), kind: e3, last: void 0 });
  }, function() {
    for (var t3 = u2(this), e3 = t3.kind, n3 = t3.last; n3 && n3.removed; )
      n3 = n3.previous;
    return t3.target && (t3.last = n3 = n3 ? n3.next : t3.state.first) ? "keys" == e3 ? { value: n3.key, done: false } : "values" == e3 ? { value: n3.value, done: false } : { value: [n3.key, n3.value], done: false } : (t3.target = void 0, { value: void 0, done: true });
  }, n2 ? "entries" : "values", !n2, true), function(t3) {
    var e3 = nt(t3), n3 = R.f;
    i && e3 && !e3[To] && n3(e3, To, { configurable: true, get: function() {
      return this;
    } });
  }(e2);
} });
var jo = Qe.charAt, Co = J.set, Lo = J.getterFor("String Iterator");
Oo(String, "String", function(t2) {
  Co(this, { type: "String Iterator", string: String(t2), index: 0 });
}, function() {
  var t2, e2 = Lo(this), n2 = e2.string, r2 = e2.index;
  return r2 >= n2.length ? { value: void 0, done: true } : (t2 = jo(n2, r2), e2.index += t2.length, { value: t2, done: false });
});
var Po = { CSSRuleList: 0, CSSStyleDeclaration: 0, CSSValueList: 0, ClientRectList: 0, DOMRectList: 0, DOMStringList: 0, DOMTokenList: 1, DataTransferItemList: 0, FileList: 0, HTMLAllCollection: 0, HTMLCollection: 0, HTMLFormElement: 0, HTMLSelectElement: 0, MediaList: 0, MimeTypeArray: 0, NamedNodeMap: 0, NodeList: 1, PaintRequestList: 0, Plugin: 0, PluginArray: 0, SVGLengthList: 0, SVGNumberList: 0, SVGPathSegList: 0, SVGPointList: 0, SVGStringList: 0, SVGTransformList: 0, SourceBufferList: 0, StyleSheetList: 0, TextTrackCueList: 0, TextTrackList: 0, TouchList: 0 }, Mo = J.set, _o = J.getterFor("Array Iterator"), Do = Oo(Array, "Array", function(t2, e2) {
  Mo(this, { type: "Array Iterator", target: g(t2), index: 0, kind: e2 });
}, function() {
  var t2 = _o(this), e2 = t2.target, n2 = t2.kind, r2 = t2.index++;
  return !e2 || r2 >= e2.length ? (t2.target = void 0, { value: void 0, done: true }) : "keys" == n2 ? { value: r2, done: false } : "values" == n2 ? { value: e2[r2], done: false } : { value: [r2, e2[r2]], done: false };
}, "values");
Pr.Arguments = Pr.Array, Er("keys"), Er("values"), Er("entries");
var Uo = ne("iterator"), No = ne("toStringTag"), Fo = Do.values;
for (var Wo in Po) {
  var zo = r[Wo], $o = zo && zo.prototype;
  if ($o) {
    if ($o[Uo] !== Fo)
      try {
        I($o, Uo, Fo);
      } catch (t2) {
        $o[Uo] = Fo;
      }
    if ($o[No] || I($o, No, Wo), Po[Wo]) {
      for (var Bo in Do)
        if ($o[Bo] !== Do[Bo])
          try {
            I($o, Bo, Do[Bo]);
          } catch (t2) {
            $o[Bo] = Do[Bo];
          }
    }
  }
}
(function() {
  function t2() {
    Wt(this, t2);
  }
  return $t(t2, null, [{ key: "deduplicate", value: function(t3) {
    return Array.from(new Set(t3));
  } }, { key: "flat", value: function(e2) {
    return e2.reduce(function(e3, n2) {
      var r2 = Array.isArray(n2) ? t2.flat(n2) : n2;
      return e3.concat(r2);
    }, []);
  } }, { key: "find", value: function(t3, e2) {
    return t3.find(e2);
  } }, { key: "findIndex", value: function(t3, e2) {
    return t3.findIndex(e2);
  } }]), t2;
})();
(function() {
  function t2() {
    Wt(this, t2);
  }
  return $t(t2, null, [{ key: "today", value: function() {
    return new Date();
  } }]), t2;
})();
(function() {
  function t2() {
    Wt(this, t2);
  }
  return $t(t2, null, [{ key: "range", value: function(t3, e2, n2) {
    return Math.min(Math.max(t3, e2), n2);
  } }, { key: "clamp", value: function(t3, e2, n2) {
    return e2 < n2 ? t3 < e2 ? e2 : t3 > n2 ? n2 : t3 : t3 < n2 ? n2 : t3 > e2 ? e2 : t3;
  } }]), t2;
})();
var freeGlobal = typeof global == "object" && global && global.Object === Object && global;
var freeGlobal$1 = freeGlobal;
var freeSelf = typeof self == "object" && self && self.Object === Object && self;
var root = freeGlobal$1 || freeSelf || Function("return this")();
var root$1 = root;
var Symbol$1 = root$1.Symbol;
var Symbol$2 = Symbol$1;
var objectProto$a = Object.prototype;
var hasOwnProperty$8 = objectProto$a.hasOwnProperty;
var nativeObjectToString$1 = objectProto$a.toString;
var symToStringTag$1 = Symbol$2 ? Symbol$2.toStringTag : void 0;
function getRawTag(value) {
  var isOwn = hasOwnProperty$8.call(value, symToStringTag$1), tag = value[symToStringTag$1];
  try {
    value[symToStringTag$1] = void 0;
    var unmasked = true;
  } catch (e2) {
  }
  var result = nativeObjectToString$1.call(value);
  if (unmasked) {
    if (isOwn) {
      value[symToStringTag$1] = tag;
    } else {
      delete value[symToStringTag$1];
    }
  }
  return result;
}
var objectProto$9 = Object.prototype;
var nativeObjectToString = objectProto$9.toString;
function objectToString(value) {
  return nativeObjectToString.call(value);
}
var nullTag = "[object Null]", undefinedTag = "[object Undefined]";
var symToStringTag = Symbol$2 ? Symbol$2.toStringTag : void 0;
function baseGetTag(value) {
  if (value == null) {
    return value === void 0 ? undefinedTag : nullTag;
  }
  return symToStringTag && symToStringTag in Object(value) ? getRawTag(value) : objectToString(value);
}
function isObjectLike(value) {
  return value != null && typeof value == "object";
}
var isArray = Array.isArray;
var isArray$1 = isArray;
function isObject(value) {
  var type = typeof value;
  return value != null && (type == "object" || type == "function");
}
function identity(value) {
  return value;
}
var asyncTag = "[object AsyncFunction]", funcTag$1 = "[object Function]", genTag = "[object GeneratorFunction]", proxyTag = "[object Proxy]";
function isFunction(value) {
  if (!isObject(value)) {
    return false;
  }
  var tag = baseGetTag(value);
  return tag == funcTag$1 || tag == genTag || tag == asyncTag || tag == proxyTag;
}
var coreJsData = root$1["__core-js_shared__"];
var coreJsData$1 = coreJsData;
var maskSrcKey = function() {
  var uid = /[^.]+$/.exec(coreJsData$1 && coreJsData$1.keys && coreJsData$1.keys.IE_PROTO || "");
  return uid ? "Symbol(src)_1." + uid : "";
}();
function isMasked(func) {
  return !!maskSrcKey && maskSrcKey in func;
}
var funcProto$2 = Function.prototype;
var funcToString$2 = funcProto$2.toString;
function toSource(func) {
  if (func != null) {
    try {
      return funcToString$2.call(func);
    } catch (e2) {
    }
    try {
      return func + "";
    } catch (e2) {
    }
  }
  return "";
}
var reRegExpChar = /[\\^$.*+?()[\]{}|]/g;
var reIsHostCtor = /^\[object .+?Constructor\]$/;
var funcProto$1 = Function.prototype, objectProto$8 = Object.prototype;
var funcToString$1 = funcProto$1.toString;
var hasOwnProperty$7 = objectProto$8.hasOwnProperty;
var reIsNative = RegExp(
  "^" + funcToString$1.call(hasOwnProperty$7).replace(reRegExpChar, "\\$&").replace(/hasOwnProperty|(function).*?(?=\\\()| for .+?(?=\\\])/g, "$1.*?") + "$"
);
function baseIsNative(value) {
  if (!isObject(value) || isMasked(value)) {
    return false;
  }
  var pattern = isFunction(value) ? reIsNative : reIsHostCtor;
  return pattern.test(toSource(value));
}
function getValue(object, key) {
  return object == null ? void 0 : object[key];
}
function getNative(object, key) {
  var value = getValue(object, key);
  return baseIsNative(value) ? value : void 0;
}
var objectCreate = Object.create;
var baseCreate = function() {
  function object() {
  }
  return function(proto) {
    if (!isObject(proto)) {
      return {};
    }
    if (objectCreate) {
      return objectCreate(proto);
    }
    object.prototype = proto;
    var result = new object();
    object.prototype = void 0;
    return result;
  };
}();
var baseCreate$1 = baseCreate;
function apply(func, thisArg, args) {
  switch (args.length) {
    case 0:
      return func.call(thisArg);
    case 1:
      return func.call(thisArg, args[0]);
    case 2:
      return func.call(thisArg, args[0], args[1]);
    case 3:
      return func.call(thisArg, args[0], args[1], args[2]);
  }
  return func.apply(thisArg, args);
}
function copyArray(source, array) {
  var index2 = -1, length = source.length;
  array || (array = Array(length));
  while (++index2 < length) {
    array[index2] = source[index2];
  }
  return array;
}
var HOT_COUNT = 800, HOT_SPAN = 16;
var nativeNow = Date.now;
function shortOut(func) {
  var count = 0, lastCalled = 0;
  return function() {
    var stamp = nativeNow(), remaining = HOT_SPAN - (stamp - lastCalled);
    lastCalled = stamp;
    if (remaining > 0) {
      if (++count >= HOT_COUNT) {
        return arguments[0];
      }
    } else {
      count = 0;
    }
    return func.apply(void 0, arguments);
  };
}
function constant(value) {
  return function() {
    return value;
  };
}
var defineProperty = function() {
  try {
    var func = getNative(Object, "defineProperty");
    func({}, "", {});
    return func;
  } catch (e2) {
  }
}();
var defineProperty$1 = defineProperty;
var baseSetToString = !defineProperty$1 ? identity : function(func, string) {
  return defineProperty$1(func, "toString", {
    "configurable": true,
    "enumerable": false,
    "value": constant(string),
    "writable": true
  });
};
var baseSetToString$1 = baseSetToString;
var setToString = shortOut(baseSetToString$1);
var setToString$1 = setToString;
var MAX_SAFE_INTEGER$1 = 9007199254740991;
var reIsUint = /^(?:0|[1-9]\d*)$/;
function isIndex(value, length) {
  var type = typeof value;
  length = length == null ? MAX_SAFE_INTEGER$1 : length;
  return !!length && (type == "number" || type != "symbol" && reIsUint.test(value)) && (value > -1 && value % 1 == 0 && value < length);
}
function baseAssignValue(object, key, value) {
  if (key == "__proto__" && defineProperty$1) {
    defineProperty$1(object, key, {
      "configurable": true,
      "enumerable": true,
      "value": value,
      "writable": true
    });
  } else {
    object[key] = value;
  }
}
function eq(value, other) {
  return value === other || value !== value && other !== other;
}
var objectProto$7 = Object.prototype;
var hasOwnProperty$6 = objectProto$7.hasOwnProperty;
function assignValue(object, key, value) {
  var objValue = object[key];
  if (!(hasOwnProperty$6.call(object, key) && eq(objValue, value)) || value === void 0 && !(key in object)) {
    baseAssignValue(object, key, value);
  }
}
function copyObject(source, props, object, customizer) {
  var isNew = !object;
  object || (object = {});
  var index2 = -1, length = props.length;
  while (++index2 < length) {
    var key = props[index2];
    var newValue = customizer ? customizer(object[key], source[key], key, object, source) : void 0;
    if (newValue === void 0) {
      newValue = source[key];
    }
    if (isNew) {
      baseAssignValue(object, key, newValue);
    } else {
      assignValue(object, key, newValue);
    }
  }
  return object;
}
var nativeMax = Math.max;
function overRest(func, start2, transform) {
  start2 = nativeMax(start2 === void 0 ? func.length - 1 : start2, 0);
  return function() {
    var args = arguments, index2 = -1, length = nativeMax(args.length - start2, 0), array = Array(length);
    while (++index2 < length) {
      array[index2] = args[start2 + index2];
    }
    index2 = -1;
    var otherArgs = Array(start2 + 1);
    while (++index2 < start2) {
      otherArgs[index2] = args[index2];
    }
    otherArgs[start2] = transform(array);
    return apply(func, this, otherArgs);
  };
}
function baseRest(func, start2) {
  return setToString$1(overRest(func, start2, identity), func + "");
}
var MAX_SAFE_INTEGER = 9007199254740991;
function isLength(value) {
  return typeof value == "number" && value > -1 && value % 1 == 0 && value <= MAX_SAFE_INTEGER;
}
function isArrayLike(value) {
  return value != null && isLength(value.length) && !isFunction(value);
}
function isIterateeCall(value, index2, object) {
  if (!isObject(object)) {
    return false;
  }
  var type = typeof index2;
  if (type == "number" ? isArrayLike(object) && isIndex(index2, object.length) : type == "string" && index2 in object) {
    return eq(object[index2], value);
  }
  return false;
}
function createAssigner(assigner) {
  return baseRest(function(object, sources) {
    var index2 = -1, length = sources.length, customizer = length > 1 ? sources[length - 1] : void 0, guard = length > 2 ? sources[2] : void 0;
    customizer = assigner.length > 3 && typeof customizer == "function" ? (length--, customizer) : void 0;
    if (guard && isIterateeCall(sources[0], sources[1], guard)) {
      customizer = length < 3 ? void 0 : customizer;
      length = 1;
    }
    object = Object(object);
    while (++index2 < length) {
      var source = sources[index2];
      if (source) {
        assigner(object, source, index2, customizer);
      }
    }
    return object;
  });
}
var objectProto$6 = Object.prototype;
function isPrototype(value) {
  var Ctor = value && value.constructor, proto = typeof Ctor == "function" && Ctor.prototype || objectProto$6;
  return value === proto;
}
function baseTimes(n2, iteratee) {
  var index2 = -1, result = Array(n2);
  while (++index2 < n2) {
    result[index2] = iteratee(index2);
  }
  return result;
}
var argsTag$1 = "[object Arguments]";
function baseIsArguments(value) {
  return isObjectLike(value) && baseGetTag(value) == argsTag$1;
}
var objectProto$5 = Object.prototype;
var hasOwnProperty$5 = objectProto$5.hasOwnProperty;
var propertyIsEnumerable = objectProto$5.propertyIsEnumerable;
var isArguments = baseIsArguments(function() {
  return arguments;
}()) ? baseIsArguments : function(value) {
  return isObjectLike(value) && hasOwnProperty$5.call(value, "callee") && !propertyIsEnumerable.call(value, "callee");
};
var isArguments$1 = isArguments;
function stubFalse() {
  return false;
}
var freeExports$2 = typeof exports == "object" && exports && !exports.nodeType && exports;
var freeModule$2 = freeExports$2 && typeof module == "object" && module && !module.nodeType && module;
var moduleExports$2 = freeModule$2 && freeModule$2.exports === freeExports$2;
var Buffer$1 = moduleExports$2 ? root$1.Buffer : void 0;
var nativeIsBuffer = Buffer$1 ? Buffer$1.isBuffer : void 0;
var isBuffer = nativeIsBuffer || stubFalse;
var isBuffer$1 = isBuffer;
var argsTag = "[object Arguments]", arrayTag = "[object Array]", boolTag = "[object Boolean]", dateTag = "[object Date]", errorTag = "[object Error]", funcTag = "[object Function]", mapTag = "[object Map]", numberTag = "[object Number]", objectTag$1 = "[object Object]", regexpTag = "[object RegExp]", setTag = "[object Set]", stringTag = "[object String]", weakMapTag = "[object WeakMap]";
var arrayBufferTag = "[object ArrayBuffer]", dataViewTag = "[object DataView]", float32Tag = "[object Float32Array]", float64Tag = "[object Float64Array]", int8Tag = "[object Int8Array]", int16Tag = "[object Int16Array]", int32Tag = "[object Int32Array]", uint8Tag = "[object Uint8Array]", uint8ClampedTag = "[object Uint8ClampedArray]", uint16Tag = "[object Uint16Array]", uint32Tag = "[object Uint32Array]";
var typedArrayTags = {};
typedArrayTags[float32Tag] = typedArrayTags[float64Tag] = typedArrayTags[int8Tag] = typedArrayTags[int16Tag] = typedArrayTags[int32Tag] = typedArrayTags[uint8Tag] = typedArrayTags[uint8ClampedTag] = typedArrayTags[uint16Tag] = typedArrayTags[uint32Tag] = true;
typedArrayTags[argsTag] = typedArrayTags[arrayTag] = typedArrayTags[arrayBufferTag] = typedArrayTags[boolTag] = typedArrayTags[dataViewTag] = typedArrayTags[dateTag] = typedArrayTags[errorTag] = typedArrayTags[funcTag] = typedArrayTags[mapTag] = typedArrayTags[numberTag] = typedArrayTags[objectTag$1] = typedArrayTags[regexpTag] = typedArrayTags[setTag] = typedArrayTags[stringTag] = typedArrayTags[weakMapTag] = false;
function baseIsTypedArray(value) {
  return isObjectLike(value) && isLength(value.length) && !!typedArrayTags[baseGetTag(value)];
}
function baseUnary(func) {
  return function(value) {
    return func(value);
  };
}
var freeExports$1 = typeof exports == "object" && exports && !exports.nodeType && exports;
var freeModule$1 = freeExports$1 && typeof module == "object" && module && !module.nodeType && module;
var moduleExports$1 = freeModule$1 && freeModule$1.exports === freeExports$1;
var freeProcess = moduleExports$1 && freeGlobal$1.process;
var nodeUtil = function() {
  try {
    var types = freeModule$1 && freeModule$1.require && freeModule$1.require("util").types;
    if (types) {
      return types;
    }
    return freeProcess && freeProcess.binding && freeProcess.binding("util");
  } catch (e2) {
  }
}();
var nodeUtil$1 = nodeUtil;
var nodeIsTypedArray = nodeUtil$1 && nodeUtil$1.isTypedArray;
var isTypedArray = nodeIsTypedArray ? baseUnary(nodeIsTypedArray) : baseIsTypedArray;
var isTypedArray$1 = isTypedArray;
var objectProto$4 = Object.prototype;
var hasOwnProperty$4 = objectProto$4.hasOwnProperty;
function arrayLikeKeys(value, inherited) {
  var isArr = isArray$1(value), isArg = !isArr && isArguments$1(value), isBuff = !isArr && !isArg && isBuffer$1(value), isType = !isArr && !isArg && !isBuff && isTypedArray$1(value), skipIndexes = isArr || isArg || isBuff || isType, result = skipIndexes ? baseTimes(value.length, String) : [], length = result.length;
  for (var key in value) {
    if ((inherited || hasOwnProperty$4.call(value, key)) && !(skipIndexes && (key == "length" || isBuff && (key == "offset" || key == "parent") || isType && (key == "buffer" || key == "byteLength" || key == "byteOffset") || isIndex(key, length)))) {
      result.push(key);
    }
  }
  return result;
}
function overArg(func, transform) {
  return function(arg) {
    return func(transform(arg));
  };
}
function nativeKeysIn(object) {
  var result = [];
  if (object != null) {
    for (var key in Object(object)) {
      result.push(key);
    }
  }
  return result;
}
var objectProto$3 = Object.prototype;
var hasOwnProperty$3 = objectProto$3.hasOwnProperty;
function baseKeysIn(object) {
  if (!isObject(object)) {
    return nativeKeysIn(object);
  }
  var isProto = isPrototype(object), result = [];
  for (var key in object) {
    if (!(key == "constructor" && (isProto || !hasOwnProperty$3.call(object, key)))) {
      result.push(key);
    }
  }
  return result;
}
function keysIn(object) {
  return isArrayLike(object) ? arrayLikeKeys(object, true) : baseKeysIn(object);
}
var nativeCreate = getNative(Object, "create");
var nativeCreate$1 = nativeCreate;
function hashClear() {
  this.__data__ = nativeCreate$1 ? nativeCreate$1(null) : {};
  this.size = 0;
}
function hashDelete(key) {
  var result = this.has(key) && delete this.__data__[key];
  this.size -= result ? 1 : 0;
  return result;
}
var HASH_UNDEFINED$1 = "__lodash_hash_undefined__";
var objectProto$2 = Object.prototype;
var hasOwnProperty$2 = objectProto$2.hasOwnProperty;
function hashGet(key) {
  var data = this.__data__;
  if (nativeCreate$1) {
    var result = data[key];
    return result === HASH_UNDEFINED$1 ? void 0 : result;
  }
  return hasOwnProperty$2.call(data, key) ? data[key] : void 0;
}
var objectProto$1 = Object.prototype;
var hasOwnProperty$1 = objectProto$1.hasOwnProperty;
function hashHas(key) {
  var data = this.__data__;
  return nativeCreate$1 ? data[key] !== void 0 : hasOwnProperty$1.call(data, key);
}
var HASH_UNDEFINED = "__lodash_hash_undefined__";
function hashSet(key, value) {
  var data = this.__data__;
  this.size += this.has(key) ? 0 : 1;
  data[key] = nativeCreate$1 && value === void 0 ? HASH_UNDEFINED : value;
  return this;
}
function Hash(entries) {
  var index2 = -1, length = entries == null ? 0 : entries.length;
  this.clear();
  while (++index2 < length) {
    var entry = entries[index2];
    this.set(entry[0], entry[1]);
  }
}
Hash.prototype.clear = hashClear;
Hash.prototype["delete"] = hashDelete;
Hash.prototype.get = hashGet;
Hash.prototype.has = hashHas;
Hash.prototype.set = hashSet;
function listCacheClear() {
  this.__data__ = [];
  this.size = 0;
}
function assocIndexOf(array, key) {
  var length = array.length;
  while (length--) {
    if (eq(array[length][0], key)) {
      return length;
    }
  }
  return -1;
}
var arrayProto = Array.prototype;
var splice = arrayProto.splice;
function listCacheDelete(key) {
  var data = this.__data__, index2 = assocIndexOf(data, key);
  if (index2 < 0) {
    return false;
  }
  var lastIndex = data.length - 1;
  if (index2 == lastIndex) {
    data.pop();
  } else {
    splice.call(data, index2, 1);
  }
  --this.size;
  return true;
}
function listCacheGet(key) {
  var data = this.__data__, index2 = assocIndexOf(data, key);
  return index2 < 0 ? void 0 : data[index2][1];
}
function listCacheHas(key) {
  return assocIndexOf(this.__data__, key) > -1;
}
function listCacheSet(key, value) {
  var data = this.__data__, index2 = assocIndexOf(data, key);
  if (index2 < 0) {
    ++this.size;
    data.push([key, value]);
  } else {
    data[index2][1] = value;
  }
  return this;
}
function ListCache(entries) {
  var index2 = -1, length = entries == null ? 0 : entries.length;
  this.clear();
  while (++index2 < length) {
    var entry = entries[index2];
    this.set(entry[0], entry[1]);
  }
}
ListCache.prototype.clear = listCacheClear;
ListCache.prototype["delete"] = listCacheDelete;
ListCache.prototype.get = listCacheGet;
ListCache.prototype.has = listCacheHas;
ListCache.prototype.set = listCacheSet;
var Map$1 = getNative(root$1, "Map");
var Map$2 = Map$1;
function mapCacheClear() {
  this.size = 0;
  this.__data__ = {
    "hash": new Hash(),
    "map": new (Map$2 || ListCache)(),
    "string": new Hash()
  };
}
function isKeyable(value) {
  var type = typeof value;
  return type == "string" || type == "number" || type == "symbol" || type == "boolean" ? value !== "__proto__" : value === null;
}
function getMapData(map, key) {
  var data = map.__data__;
  return isKeyable(key) ? data[typeof key == "string" ? "string" : "hash"] : data.map;
}
function mapCacheDelete(key) {
  var result = getMapData(this, key)["delete"](key);
  this.size -= result ? 1 : 0;
  return result;
}
function mapCacheGet(key) {
  return getMapData(this, key).get(key);
}
function mapCacheHas(key) {
  return getMapData(this, key).has(key);
}
function mapCacheSet(key, value) {
  var data = getMapData(this, key), size = data.size;
  data.set(key, value);
  this.size += data.size == size ? 0 : 1;
  return this;
}
function MapCache(entries) {
  var index2 = -1, length = entries == null ? 0 : entries.length;
  this.clear();
  while (++index2 < length) {
    var entry = entries[index2];
    this.set(entry[0], entry[1]);
  }
}
MapCache.prototype.clear = mapCacheClear;
MapCache.prototype["delete"] = mapCacheDelete;
MapCache.prototype.get = mapCacheGet;
MapCache.prototype.has = mapCacheHas;
MapCache.prototype.set = mapCacheSet;
var getPrototype = overArg(Object.getPrototypeOf, Object);
var getPrototype$1 = getPrototype;
var objectTag = "[object Object]";
var funcProto = Function.prototype, objectProto = Object.prototype;
var funcToString = funcProto.toString;
var hasOwnProperty = objectProto.hasOwnProperty;
var objectCtorString = funcToString.call(Object);
function isPlainObject(value) {
  if (!isObjectLike(value) || baseGetTag(value) != objectTag) {
    return false;
  }
  var proto = getPrototype$1(value);
  if (proto === null) {
    return true;
  }
  var Ctor = hasOwnProperty.call(proto, "constructor") && proto.constructor;
  return typeof Ctor == "function" && Ctor instanceof Ctor && funcToString.call(Ctor) == objectCtorString;
}
function stackClear() {
  this.__data__ = new ListCache();
  this.size = 0;
}
function stackDelete(key) {
  var data = this.__data__, result = data["delete"](key);
  this.size = data.size;
  return result;
}
function stackGet(key) {
  return this.__data__.get(key);
}
function stackHas(key) {
  return this.__data__.has(key);
}
var LARGE_ARRAY_SIZE = 200;
function stackSet(key, value) {
  var data = this.__data__;
  if (data instanceof ListCache) {
    var pairs = data.__data__;
    if (!Map$2 || pairs.length < LARGE_ARRAY_SIZE - 1) {
      pairs.push([key, value]);
      this.size = ++data.size;
      return this;
    }
    data = this.__data__ = new MapCache(pairs);
  }
  data.set(key, value);
  this.size = data.size;
  return this;
}
function Stack(entries) {
  var data = this.__data__ = new ListCache(entries);
  this.size = data.size;
}
Stack.prototype.clear = stackClear;
Stack.prototype["delete"] = stackDelete;
Stack.prototype.get = stackGet;
Stack.prototype.has = stackHas;
Stack.prototype.set = stackSet;
var freeExports = typeof exports == "object" && exports && !exports.nodeType && exports;
var freeModule = freeExports && typeof module == "object" && module && !module.nodeType && module;
var moduleExports = freeModule && freeModule.exports === freeExports;
var Buffer2 = moduleExports ? root$1.Buffer : void 0, allocUnsafe = Buffer2 ? Buffer2.allocUnsafe : void 0;
function cloneBuffer(buffer, isDeep) {
  if (isDeep) {
    return buffer.slice();
  }
  var length = buffer.length, result = allocUnsafe ? allocUnsafe(length) : new buffer.constructor(length);
  buffer.copy(result);
  return result;
}
var Uint8Array2 = root$1.Uint8Array;
var Uint8Array$1 = Uint8Array2;
function cloneArrayBuffer(arrayBuffer) {
  var result = new arrayBuffer.constructor(arrayBuffer.byteLength);
  new Uint8Array$1(result).set(new Uint8Array$1(arrayBuffer));
  return result;
}
function cloneTypedArray(typedArray, isDeep) {
  var buffer = isDeep ? cloneArrayBuffer(typedArray.buffer) : typedArray.buffer;
  return new typedArray.constructor(buffer, typedArray.byteOffset, typedArray.length);
}
function initCloneObject(object) {
  return typeof object.constructor == "function" && !isPrototype(object) ? baseCreate$1(getPrototype$1(object)) : {};
}
function createBaseFor(fromRight) {
  return function(object, iteratee, keysFunc) {
    var index2 = -1, iterable = Object(object), props = keysFunc(object), length = props.length;
    while (length--) {
      var key = props[fromRight ? length : ++index2];
      if (iteratee(iterable[key], key, iterable) === false) {
        break;
      }
    }
    return object;
  };
}
var baseFor = createBaseFor();
var baseFor$1 = baseFor;
function assignMergeValue(object, key, value) {
  if (value !== void 0 && !eq(object[key], value) || value === void 0 && !(key in object)) {
    baseAssignValue(object, key, value);
  }
}
function isArrayLikeObject(value) {
  return isObjectLike(value) && isArrayLike(value);
}
function safeGet(object, key) {
  if (key === "constructor" && typeof object[key] === "function") {
    return;
  }
  if (key == "__proto__") {
    return;
  }
  return object[key];
}
function toPlainObject(value) {
  return copyObject(value, keysIn(value));
}
function baseMergeDeep(object, source, key, srcIndex, mergeFunc, customizer, stack) {
  var objValue = safeGet(object, key), srcValue = safeGet(source, key), stacked = stack.get(srcValue);
  if (stacked) {
    assignMergeValue(object, key, stacked);
    return;
  }
  var newValue = customizer ? customizer(objValue, srcValue, key + "", object, source, stack) : void 0;
  var isCommon = newValue === void 0;
  if (isCommon) {
    var isArr = isArray$1(srcValue), isBuff = !isArr && isBuffer$1(srcValue), isTyped = !isArr && !isBuff && isTypedArray$1(srcValue);
    newValue = srcValue;
    if (isArr || isBuff || isTyped) {
      if (isArray$1(objValue)) {
        newValue = objValue;
      } else if (isArrayLikeObject(objValue)) {
        newValue = copyArray(objValue);
      } else if (isBuff) {
        isCommon = false;
        newValue = cloneBuffer(srcValue, true);
      } else if (isTyped) {
        isCommon = false;
        newValue = cloneTypedArray(srcValue, true);
      } else {
        newValue = [];
      }
    } else if (isPlainObject(srcValue) || isArguments$1(srcValue)) {
      newValue = objValue;
      if (isArguments$1(objValue)) {
        newValue = toPlainObject(objValue);
      } else if (!isObject(objValue) || isFunction(objValue)) {
        newValue = initCloneObject(srcValue);
      }
    } else {
      isCommon = false;
    }
  }
  if (isCommon) {
    stack.set(srcValue, newValue);
    mergeFunc(newValue, srcValue, srcIndex, customizer, stack);
    stack["delete"](srcValue);
  }
  assignMergeValue(object, key, newValue);
}
function baseMerge(object, source, srcIndex, customizer, stack) {
  if (object === source) {
    return;
  }
  baseFor$1(source, function(srcValue, key) {
    stack || (stack = new Stack());
    if (isObject(srcValue)) {
      baseMergeDeep(object, source, key, srcIndex, baseMerge, customizer, stack);
    } else {
      var newValue = customizer ? customizer(safeGet(object, key), srcValue, key + "", object, source, stack) : void 0;
      if (newValue === void 0) {
        newValue = srcValue;
      }
      assignMergeValue(object, key, newValue);
    }
  }, keysIn);
}
var merge = createAssigner(function(object, source, srcIndex) {
  baseMerge(object, source, srcIndex);
});
var merge$1 = merge;
var Alpha_vue_vue_type_style_index_0_scoped_true_lang = "";
var _export_sfc = (sfc, props) => {
  const target = sfc.__vccOpts || sfc;
  for (const [key, val] of props) {
    target[key] = val;
  }
  return target;
};
const _sfc_main$b = defineComponent({
  name: "Alpha",
  props: {
    color: C$1.instanceOf(Color),
    size: C$1.oneOf(["small", "default"]).def("default")
  },
  emits: ["change"],
  setup(props, { emit }) {
    const barElement = ref(null);
    const cursorElement = ref(null);
    let color = props.color || new Color();
    const state = reactive({
      red: color.red,
      green: color.green,
      blue: color.blue,
      alpha: color.alpha
    });
    watch(
      () => props.color,
      (value) => {
        if (value) {
          color = value;
          merge$1(state, {
            red: value.red,
            green: value.green,
            blue: value.blue,
            alpha: value.alpha
          });
        }
      },
      { deep: true }
    );
    const getBackgroundStyle = computed(() => {
      const startColor = rgbaColor(state.red, state.green, state.blue, 0);
      const endColor = rgbaColor(state.red, state.green, state.blue, 100);
      return {
        background: `linear-gradient(to right, ${startColor} , ${endColor})`
      };
    });
    const getCursorLeft = () => {
      if (barElement.value && cursorElement.value) {
        const alpha = state.alpha / 100;
        const rect = barElement.value.getBoundingClientRect();
        const offsetWidth = cursorElement.value.offsetWidth;
        return Math.round(alpha * (rect.width - offsetWidth) + offsetWidth / 2);
      }
      return 0;
    };
    const getCursorStyle = computed(() => {
      const left2 = getCursorLeft();
      return {
        left: left2 + "px",
        top: 0
      };
    });
    const onClickSider = (event) => {
      const target = event.target;
      if (target !== barElement.value) {
        onMoveBar(event);
      }
    };
    const onMoveBar = (event) => {
      event.stopPropagation();
      if (barElement.value && cursorElement.value) {
        const rect = barElement.value.getBoundingClientRect();
        const offsetWidth = cursorElement.value.offsetWidth;
        let left2 = event.clientX - rect.left;
        left2 = Math.max(offsetWidth / 2, left2);
        left2 = Math.min(left2, rect.width - offsetWidth / 2);
        const alpha = Math.round((left2 - offsetWidth / 2) / (rect.width - offsetWidth) * 100);
        color.alpha = alpha;
        state.alpha = alpha;
        emit("change", alpha);
      }
    };
    tryOnMounted(() => {
      const dragConfig = {
        drag: (event) => {
          onMoveBar(event);
        },
        end: (event) => {
          onMoveBar(event);
        }
      };
      if (barElement.value && cursorElement.value) {
        Vn.triggerDragEvent(barElement.value, dragConfig);
      }
    });
    return { barElement, cursorElement, getCursorStyle, getBackgroundStyle, onClickSider };
  }
});
const _withScopeId$5 = (n2) => (pushScopeId("data-v-7dca4a44"), n2 = n2(), popScopeId(), n2);
const _hoisted_1$b = /* @__PURE__ */ _withScopeId$5(() => /* @__PURE__ */ createElementVNode("div", { class: "vc-alpha-slider__bar-handle" }, null, -1));
const _hoisted_2$a = [
  _hoisted_1$b
];
function _sfc_render$b(_ctx, _cache, $props, $setup, $data, $options) {
  return openBlock(), createElementBlock("div", {
    class: normalizeClass(["vc-alpha-slider", "transparent", { "small-slider": _ctx.size === "small" }])
  }, [
    createElementVNode("div", {
      ref: "barElement",
      class: "vc-alpha-slider__bar",
      style: normalizeStyle(_ctx.getBackgroundStyle),
      onClick: _cache[0] || (_cache[0] = (...args) => _ctx.onClickSider && _ctx.onClickSider(...args))
    }, [
      createElementVNode("div", {
        class: normalizeClass(["vc-alpha-slider__bar-pointer", { "small-bar": _ctx.size === "small" }]),
        ref: "cursorElement",
        style: normalizeStyle(_ctx.getCursorStyle)
      }, _hoisted_2$a, 6)
    ], 4)
  ], 2);
}
var Alpha = /* @__PURE__ */ _export_sfc(_sfc_main$b, [["render", _sfc_render$b], ["__scopeId", "data-v-7dca4a44"]]);
var Palette_vue_vue_type_style_index_0_scoped_true_lang = "";
const defaultColors = [
  [
    "#fcc02e",
    "#f67c01",
    "#e64a19",
    "#d81b43",
    "#8e24aa",
    "#512da7",
    "#1f87e8",
    "#008781",
    "#05a045"
  ],
  [
    "#fed835",
    "#fb8c00",
    "#f5511e",
    "#eb1d4e",
    "#9c28b1",
    "#5d35b0",
    "#2097f3",
    "#029688",
    "#4cb050"
  ],
  [
    "#ffeb3c",
    "#ffa727",
    "#fe5722",
    "#eb4165",
    "#aa47bc",
    "#673bb7",
    "#42a5f6",
    "#26a59a",
    "#83c683"
  ],
  [
    "#fff176",
    "#ffb74e",
    "#ff8a66",
    "#f1627e",
    "#b968c7",
    "#7986cc",
    "#64b5f6",
    "#80cbc4",
    "#a5d6a7"
  ],
  [
    "#fff59c",
    "#ffcc80",
    "#ffab91",
    "#fb879e",
    "#cf93d9",
    "#9ea8db",
    "#90caf8",
    "#b2dfdc",
    "#c8e6ca"
  ],
  [
    "transparent",
    "#ffffff",
    "#dedede",
    "#a9a9a9",
    "#4b4b4b",
    "#353535",
    "#212121",
    "#000000",
    "advance"
  ]
];
const _sfc_main$a = defineComponent({
  name: "Palette",
  emits: ["change"],
  setup(_props, { emit }) {
    const computedBgStyle = (color) => {
      if (color === "transparent") {
        return color;
      }
      if (color === "advance") {
        return {};
      }
      return { background: tinycolor(color).toRgbString() };
    };
    const onColorChange = (color) => {
      emit("change", color);
    };
    return { palettes: defaultColors, computedBgStyle, onColorChange };
  }
});
const _hoisted_1$a = { class: "vc-compact" };
const _hoisted_2$9 = ["onClick"];
function _sfc_render$a(_ctx, _cache, $props, $setup, $data, $options) {
  return openBlock(), createElementBlock("div", _hoisted_1$a, [
    (openBlock(true), createElementBlock(Fragment, null, renderList(_ctx.palettes, (v2, i2) => {
      return openBlock(), createElementBlock("div", {
        key: i2,
        class: "vc-compact__row"
      }, [
        (openBlock(true), createElementBlock(Fragment, null, renderList(v2, (v1, k2) => {
          return openBlock(), createElementBlock("div", {
            key: k2,
            class: "vc-compact__color-cube--wrap",
            onClick: ($event) => _ctx.onColorChange(v1)
          }, [
            createElementVNode("div", {
              class: normalizeClass([
                "vc-compact__color_cube",
                {
                  advance: v1 === "advance",
                  transparent: v1 === "transparent"
                }
              ]),
              style: normalizeStyle(_ctx.computedBgStyle(v1))
            }, null, 6)
          ], 8, _hoisted_2$9);
        }), 128))
      ]);
    }), 128))
  ]);
}
var Palette = /* @__PURE__ */ _export_sfc(_sfc_main$a, [["render", _sfc_render$a], ["__scopeId", "data-v-437e0455"]]);
var Board_vue_vue_type_style_index_0_scoped_true_lang = "";
const _sfc_main$9 = defineComponent({
  name: "Board",
  props: {
    color: C$1.instanceOf(Color),
    round: C$1.bool.def(false),
    hide: C$1.bool.def(true)
  },
  emits: ["change"],
  setup(props, { emit }) {
    var _a, _b, _c;
    const instance = getCurrentInstance();
    const hueHsv = {
      h: ((_a = props.color) == null ? void 0 : _a.hue) || 0,
      s: 1,
      v: 1
    };
    const hueColor = new Color(hueHsv).toHexString();
    const state = reactive({
      hueColor,
      saturation: ((_b = props.color) == null ? void 0 : _b.saturation) || 0,
      brightness: ((_c = props.color) == null ? void 0 : _c.brightness) || 0
    });
    const cursorTop = ref(0);
    const cursorLeft = ref(0);
    const cursorElement = ref();
    const boardElement = ref();
    const getCursorStyle = computed(() => {
      return {
        top: cursorTop.value + "px",
        left: cursorLeft.value + "px"
      };
    });
    const updatePosition = () => {
      if (instance) {
        const el = instance.vnode.el;
        cursorLeft.value = state.saturation * (el == null ? void 0 : el.clientWidth);
        cursorTop.value = (1 - state.brightness) * (el == null ? void 0 : el.clientHeight);
      }
    };
    const onClickBoard = (event) => {
      const target = event.target;
      if (target !== boardElement.value) {
        handleDrag(event);
      }
    };
    const handleDrag = (event) => {
      if (instance) {
        const el = instance.vnode.el;
        const rect = el == null ? void 0 : el.getBoundingClientRect();
        let left2 = event.clientX - rect.left;
        let top2 = event.clientY - rect.top;
        left2 = clamp(left2, 0, rect.width);
        top2 = clamp(top2, 0, rect.height);
        const saturation = left2 / rect.width;
        const bright = clamp(-(top2 / rect.height) + 1, 0, 1);
        cursorLeft.value = left2;
        cursorTop.value = top2;
        state.saturation = saturation;
        state.brightness = bright;
        emit("change", saturation, bright);
      }
    };
    tryOnMounted(() => {
      if (instance && instance.vnode.el && cursorElement.value) {
        Vn.triggerDragEvent(cursorElement.value, {
          drag: (event) => {
            handleDrag(event);
          },
          end: (event) => {
            handleDrag(event);
          }
        });
        updatePosition();
      }
    });
    whenever(
      () => props.color,
      (value) => {
        merge$1(state, {
          hueColor: new Color({ h: value.hue, s: 1, v: 1 }).toHexString(),
          saturation: value.saturation,
          brightness: value.brightness
        });
        updatePosition();
      },
      { deep: true }
    );
    return { state, cursorElement, getCursorStyle, onClickBoard };
  }
});
const _withScopeId$4 = (n2) => (pushScopeId("data-v-f44c3938"), n2 = n2(), popScopeId(), n2);
const _hoisted_1$9 = /* @__PURE__ */ _withScopeId$4(() => /* @__PURE__ */ createElementVNode("div", { class: "vc-saturation__white" }, null, -1));
const _hoisted_2$8 = /* @__PURE__ */ _withScopeId$4(() => /* @__PURE__ */ createElementVNode("div", { class: "vc-saturation__black" }, null, -1));
const _hoisted_3$6 = /* @__PURE__ */ _withScopeId$4(() => /* @__PURE__ */ createElementVNode("div", null, null, -1));
const _hoisted_4$5 = [
  _hoisted_3$6
];
function _sfc_render$9(_ctx, _cache, $props, $setup, $data, $options) {
  return openBlock(), createElementBlock("div", {
    ref: "boardElement",
    class: normalizeClass(["vc-saturation", { "vc-saturation__chrome": _ctx.round, "vc-saturation__hidden": _ctx.hide }]),
    style: normalizeStyle({ backgroundColor: _ctx.state.hueColor }),
    onClick: _cache[0] || (_cache[0] = (...args) => _ctx.onClickBoard && _ctx.onClickBoard(...args))
  }, [
    _hoisted_1$9,
    _hoisted_2$8,
    createElementVNode("div", {
      class: "vc-saturation__cursor",
      ref: "cursorElement",
      style: normalizeStyle(_ctx.getCursorStyle)
    }, _hoisted_4$5, 4)
  ], 6);
}
var Board = /* @__PURE__ */ _export_sfc(_sfc_main$9, [["render", _sfc_render$9], ["__scopeId", "data-v-f44c3938"]]);
var Hue_vue_vue_type_style_index_0_scoped_true_lang = "";
const _sfc_main$8 = defineComponent({
  name: "Hue",
  props: {
    color: C$1.instanceOf(Color),
    size: C$1.oneOf(["small", "default"]).def("default")
  },
  emits: ["change"],
  setup(props, { emit }) {
    const barElement = ref(null);
    const cursorElement = ref(null);
    let color = props.color || new Color();
    const state = reactive({
      hue: color.hue || 0
    });
    watch(
      () => props.color,
      (value) => {
        if (value) {
          color = value;
          merge$1(state, { hue: color.hue });
        }
      },
      { deep: true }
    );
    const getCursorLeft = () => {
      if (barElement.value && cursorElement.value) {
        const rect = barElement.value.getBoundingClientRect();
        const offsetWidth = cursorElement.value.offsetWidth;
        if (state.hue === 360) {
          return rect.width - offsetWidth / 2;
        }
        return state.hue % 360 * (rect.width - offsetWidth) / 360 + offsetWidth / 2;
      }
      return 0;
    };
    const getCursorStyle = computed(() => {
      const left2 = getCursorLeft();
      return {
        left: left2 + "px",
        top: 0
      };
    });
    const onClickSider = (event) => {
      const target = event.target;
      if (target !== barElement.value) {
        onMoveBar(event);
      }
    };
    const onMoveBar = (event) => {
      event.stopPropagation();
      if (barElement.value && cursorElement.value) {
        const rect = barElement.value.getBoundingClientRect();
        const offsetWidth = cursorElement.value.offsetWidth;
        let left2 = event.clientX - rect.left;
        left2 = Math.min(left2, rect.width - offsetWidth / 2);
        left2 = Math.max(offsetWidth / 2, left2);
        const hue = Math.round((left2 - offsetWidth / 2) / (rect.width - offsetWidth) * 360);
        color.hue = hue;
        state.hue = hue;
        emit("change", hue);
      }
    };
    tryOnMounted(() => {
      const dragConfig = {
        drag: (event) => {
          onMoveBar(event);
        },
        end: (event) => {
          onMoveBar(event);
        }
      };
      if (barElement.value && cursorElement.value) {
        Vn.triggerDragEvent(barElement.value, dragConfig);
      }
    });
    return { barElement, cursorElement, getCursorStyle, onClickSider };
  }
});
const _withScopeId$3 = (n2) => (pushScopeId("data-v-226b0276"), n2 = n2(), popScopeId(), n2);
const _hoisted_1$8 = /* @__PURE__ */ _withScopeId$3(() => /* @__PURE__ */ createElementVNode("div", { class: "vc-hue-slider__bar-handle" }, null, -1));
const _hoisted_2$7 = [
  _hoisted_1$8
];
function _sfc_render$8(_ctx, _cache, $props, $setup, $data, $options) {
  return openBlock(), createElementBlock("div", {
    class: normalizeClass(["vc-hue-slider", { "small-slider": _ctx.size === "small" }])
  }, [
    createElementVNode("div", {
      ref: "barElement",
      class: "vc-hue-slider__bar",
      onClick: _cache[0] || (_cache[0] = (...args) => _ctx.onClickSider && _ctx.onClickSider(...args))
    }, [
      createElementVNode("div", {
        class: normalizeClass(["vc-hue-slider__bar-pointer", { "small-bar": _ctx.size === "small" }]),
        ref: "cursorElement",
        style: normalizeStyle(_ctx.getCursorStyle)
      }, _hoisted_2$7, 6)
    ], 512)
  ], 2);
}
var Hue = /* @__PURE__ */ _export_sfc(_sfc_main$8, [["render", _sfc_render$8], ["__scopeId", "data-v-226b0276"]]);
var Lightness_vue_vue_type_style_index_0_scoped_true_lang = "";
const _sfc_main$7 = defineComponent({
  name: "Lightness",
  props: {
    color: C$1.instanceOf(Color),
    size: C$1.oneOf(["small", "default"]).def("default")
  },
  emits: ["change"],
  setup(props, { emit }) {
    const barElement = ref(null);
    const cursorElement = ref(null);
    let color = props.color || new Color();
    const [h2, s2, l2] = color.HSL;
    const state = reactive({
      hue: h2,
      saturation: s2,
      lightness: l2
    });
    watch(
      () => props.color,
      (value) => {
        if (value) {
          color = value;
          const [hue, saturation, lightness] = color.HSL;
          merge$1(state, {
            hue,
            saturation,
            lightness
          });
        }
      },
      { deep: true }
    );
    const getBackgroundStyle = computed(() => {
      const color1 = tinycolor({
        h: state.hue,
        s: state.saturation,
        l: 0.8
      }).toPercentageRgbString();
      const color2 = tinycolor({
        h: state.hue,
        s: state.saturation,
        l: 0.6
      }).toPercentageRgbString();
      const color3 = tinycolor({
        h: state.hue,
        s: state.saturation,
        l: 0.4
      }).toPercentageRgbString();
      const color4 = tinycolor({
        h: state.hue,
        s: state.saturation,
        l: 0.2
      }).toPercentageRgbString();
      return {
        background: [
          `-webkit-linear-gradient(left, rgb(255, 255, 255), ${color1}, ${color2}, ${color3}, ${color4}, rgb(0, 0, 0))`,
          `-moz-linear-gradient(left, rgb(255, 255, 255), ${color1}, ${color2}, ${color3}, ${color4}, rgb(0, 0, 0))`,
          `-ms-linear-gradient(left, rgb(255, 255, 255), ${color1}, ${color2}, ${color3}, ${color4}, rgb(0, 0, 0))`
        ]
      };
    });
    const getCursorLeft = () => {
      if (barElement.value && cursorElement.value) {
        const lightness = state.lightness;
        const rect = barElement.value.getBoundingClientRect();
        const offsetWidth = cursorElement.value.offsetWidth;
        return (1 - lightness) * (rect.width - offsetWidth) + offsetWidth / 2;
      }
      return 0;
    };
    const getCursorStyle = computed(() => {
      const left2 = getCursorLeft();
      return {
        left: left2 + "px",
        top: 0
      };
    });
    const onClickSider = (event) => {
      const target = event.target;
      if (target !== barElement.value) {
        onMoveBar(event);
      }
    };
    const onMoveBar = (event) => {
      event.stopPropagation();
      if (barElement.value && cursorElement.value) {
        const rect = barElement.value.getBoundingClientRect();
        const offsetWidth = cursorElement.value.offsetWidth;
        let left2 = event.clientX - rect.left;
        left2 = Math.max(offsetWidth / 2, left2);
        left2 = Math.min(left2, rect.width - offsetWidth / 2);
        const light = 1 - (left2 - offsetWidth / 2) / (rect.width - offsetWidth);
        color.lightness = light;
        emit("change", light);
      }
    };
    tryOnMounted(() => {
      const dragConfig = {
        drag: (event) => {
          onMoveBar(event);
        },
        end: (event) => {
          onMoveBar(event);
        }
      };
      if (barElement.value && cursorElement.value) {
        Vn.triggerDragEvent(barElement.value, dragConfig);
      }
    });
    return { barElement, cursorElement, getCursorStyle, getBackgroundStyle, onClickSider };
  }
});
const _withScopeId$2 = (n2) => (pushScopeId("data-v-d7149f6c"), n2 = n2(), popScopeId(), n2);
const _hoisted_1$7 = /* @__PURE__ */ _withScopeId$2(() => /* @__PURE__ */ createElementVNode("div", { class: "vc-lightness-slider__bar-handle" }, null, -1));
const _hoisted_2$6 = [
  _hoisted_1$7
];
function _sfc_render$7(_ctx, _cache, $props, $setup, $data, $options) {
  return openBlock(), createElementBlock("div", {
    class: normalizeClass(["vc-lightness-slider", { "small-slider": _ctx.size === "small" }])
  }, [
    createElementVNode("div", {
      ref: "barElement",
      class: "vc-lightness-slider__bar",
      style: normalizeStyle(_ctx.getBackgroundStyle),
      onClick: _cache[0] || (_cache[0] = (...args) => _ctx.onClickSider && _ctx.onClickSider(...args))
    }, [
      createElementVNode("div", {
        class: normalizeClass(["vc-lightness-slider__bar-pointer", { "small-bar": _ctx.size === "small" }]),
        ref: "cursorElement",
        style: normalizeStyle(_ctx.getCursorStyle)
      }, _hoisted_2$6, 6)
    ], 4)
  ], 2);
}
var Lightness = /* @__PURE__ */ _export_sfc(_sfc_main$7, [["render", _sfc_render$7], ["__scopeId", "data-v-d7149f6c"]]);
var History_vue_vue_type_style_index_0_scoped_true_lang = "";
const _sfc_main$6 = defineComponent({
  name: "History",
  props: {
    colors: C$1.arrayOf(String).def(() => []),
    round: C$1.bool.def(false)
  },
  emits: ["change"],
  setup(_props, { emit }) {
    const onColorSelect = (v2) => {
      emit("change", v2);
    };
    return { onColorSelect };
  }
});
const _hoisted_1$6 = {
  key: 0,
  class: "vc-colorPicker__record"
};
const _hoisted_2$5 = { class: "color-list" };
const _hoisted_3$5 = ["onClick"];
function _sfc_render$6(_ctx, _cache, $props, $setup, $data, $options) {
  return _ctx.colors && _ctx.colors.length > 0 ? (openBlock(), createElementBlock("div", _hoisted_1$6, [
    createElementVNode("div", _hoisted_2$5, [
      (openBlock(true), createElementBlock(Fragment, null, renderList(_ctx.colors, (v2, i2) => {
        return openBlock(), createElementBlock("div", {
          key: i2,
          class: normalizeClass(["color-item", "transparent", { "color-item__round": _ctx.round }]),
          onClick: ($event) => _ctx.onColorSelect(v2)
        }, [
          createElementVNode("div", {
            class: "color-item__display",
            style: normalizeStyle({ backgroundColor: v2 })
          }, null, 4)
        ], 10, _hoisted_3$5);
      }), 128))
    ])
  ])) : createCommentVNode("", true);
}
var History = /* @__PURE__ */ _export_sfc(_sfc_main$6, [["render", _sfc_render$6], ["__scopeId", "data-v-2eb640df"]]);
var Display_vue_vue_type_style_index_0_scoped_true_lang = "";
const _sfc_main$5 = defineComponent({
  name: "Display",
  props: {
    color: C$1.instanceOf(Color),
    disableAlpha: C$1.bool.def(false)
  },
  emits: ["update:color", "change"],
  setup(props, { emit }) {
    var _a, _b, _c;
    const state = reactive({
      color: props.color,
      previewBgColor: (_a = props.color) == null ? void 0 : _a.toRgbString(),
      alpha: ((_b = props.color) == null ? void 0 : _b.alpha) || 100,
      hex: (_c = props.color) == null ? void 0 : _c.hex
    });
    const getBgColorStyle = computed(() => {
      return {
        background: state.previewBgColor
      };
    });
    const onAlphaBlur = (evt) => {
      const target = evt.target;
      const opacity = parseInt(target.value.replace("%", ""));
      if (!isNaN(opacity) && state.color) {
        state.alpha = opacity;
        state.color.alpha = opacity;
      }
    };
    const onInputChange = (event) => {
      const target = event.target;
      const hex = target.value.replace("#", "");
      if (tinycolor(hex).isValid() && state.color) {
        state.color.hex = hex;
      }
    };
    whenever(
      () => props.color,
      (value) => {
        if (value) {
          state.color = value;
        }
      },
      { deep: true }
    );
    whenever(
      () => state.color,
      () => {
        if (state.color) {
          state.previewBgColor = state.color.toRgbString();
          state.alpha = state.color.alpha;
          state.hex = state.color.hex;
          emit("update:color", state.color);
          emit("change", state.color);
        }
      },
      { deep: true }
    );
    return { state, getBgColorStyle, onAlphaBlur, onInputChange };
  }
});
const _hoisted_1$5 = { class: "vc-display" };
const _hoisted_2$4 = { class: "vc-current-color vc-transparent" };
const _hoisted_3$4 = { class: "vc-color-input" };
const _hoisted_4$4 = ["value"];
const _hoisted_5$4 = {
  key: 0,
  class: "vc-alpha-input"
};
const _hoisted_6$2 = ["value"];
function _sfc_render$5(_ctx, _cache, $props, $setup, $data, $options) {
  return openBlock(), createElementBlock("div", _hoisted_1$5, [
    createElementVNode("div", _hoisted_2$4, [
      createElementVNode("div", {
        class: "color-cube",
        style: normalizeStyle(_ctx.getBgColorStyle)
      }, null, 4)
    ]),
    createElementVNode("div", _hoisted_3$4, [
      createElementVNode("input", {
        value: _ctx.state.hex,
        onBlur: _cache[0] || (_cache[0] = (...args) => _ctx.onInputChange && _ctx.onInputChange(...args)),
        onKeydown: _cache[1] || (_cache[1] = withKeys((...args) => _ctx.onInputChange && _ctx.onInputChange(...args), ["enter"]))
      }, null, 40, _hoisted_4$4)
    ]),
    !_ctx.disableAlpha ? (openBlock(), createElementBlock("div", _hoisted_5$4, [
      createElementVNode("input", {
        class: "vc-alpha-input__inner",
        value: _ctx.state.alpha + "%",
        onBlur: _cache[2] || (_cache[2] = (...args) => _ctx.onAlphaBlur && _ctx.onAlphaBlur(...args)),
        onKeydown: _cache[3] || (_cache[3] = withKeys((...args) => _ctx.onAlphaBlur && _ctx.onAlphaBlur(...args), ["enter"]))
      }, null, 40, _hoisted_6$2)
    ])) : createCommentVNode("", true)
  ]);
}
var Display = /* @__PURE__ */ _export_sfc(_sfc_main$5, [["render", _sfc_render$5], ["__scopeId", "data-v-bf0586ee"]]);
var FkColorPicker_vue_vue_type_style_index_0_scoped_true_lang = "";
const _sfc_main$4 = defineComponent({
  name: "FkColorPicker",
  components: { Display, Alpha, Palette, Board, Hue, Lightness, History },
  props: {
    color: C$1.instanceOf(Color),
    disableHistory: C$1.bool.def(false),
    roundHistory: C$1.bool.def(false),
    disableAlpha: C$1.bool.def(false)
  },
  emits: ["update:color", "change", "advanceChange"],
  setup(props, { emit }) {
    const colorInstance = props.color || new Color();
    const state = reactive({
      color: colorInstance,
      hex: colorInstance.toHexString(),
      rgb: colorInstance.toRgbString()
    });
    const advancePanelShow = ref(false);
    const previewStyle = computed(() => {
      return { background: state.rgb };
    });
    const onBack = () => {
      advancePanelShow.value = false;
      emit("advanceChange", false);
    };
    const historyColors = useLocalStorage(HistoryColorKey, [], {});
    const updateColorHistoryFn = useDebounceFn(() => {
      if (props.disableHistory) {
        return;
      }
      const rgbString = state.color.toRgbString();
      historyColors.value = historyColors.value.filter((value) => {
        return !tinycolor.equals(value, rgbString);
      });
      if (historyColors.value.includes(rgbString)) {
        return;
      }
      while (historyColors.value.length > MAX_STORAGE_LENGTH) {
        historyColors.value.pop();
      }
      historyColors.value.unshift(rgbString);
    }, 500);
    const onCompactChange = (color) => {
      if (color === "advance") {
        advancePanelShow.value = true;
        emit("advanceChange", true);
      } else {
        state.color.hex = color;
        emit("advanceChange", false);
      }
    };
    const onAlphaChange = (alpha) => {
      state.color.alpha = alpha;
    };
    const onHueChange = (hue) => {
      state.color.hue = hue;
    };
    const onBoardChange = (saturation, brightness) => {
      state.color.saturation = saturation;
      state.color.brightness = brightness;
    };
    const onLightChange = (light) => {
      state.color.lightness = light;
    };
    const onInputChange = (event) => {
      const target = event.target;
      const hex = target.value.replace("#", "");
      if (tinycolor(hex).isValid()) {
        state.color.hex = hex;
      }
    };
    whenever(
      () => props.color,
      (value) => {
        if (value) {
          state.color = value;
        }
      },
      { deep: true }
    );
    whenever(
      () => state.color,
      () => {
        state.hex = state.color.hex;
        state.rgb = state.color.toRgbString();
        updateColorHistoryFn();
        emit("update:color", state.color);
        emit("change", state.color);
      },
      { deep: true }
    );
    return {
      state,
      advancePanelShow,
      onBack,
      onCompactChange,
      onAlphaChange,
      onHueChange,
      onBoardChange,
      onLightChange,
      onInputChange,
      previewStyle,
      historyColors
    };
  }
});
const _withScopeId$1 = (n2) => (pushScopeId("data-v-167618b1"), n2 = n2(), popScopeId(), n2);
const _hoisted_1$4 = { class: "vc-fk-colorPicker" };
const _hoisted_2$3 = { class: "vc-fk-colorPicker__inner" };
const _hoisted_3$3 = { class: "vc-fk-colorPicker__header" };
const _hoisted_4$3 = /* @__PURE__ */ _withScopeId$1(() => /* @__PURE__ */ createElementVNode("div", { class: "back" }, null, -1));
const _hoisted_5$3 = [
  _hoisted_4$3
];
function _sfc_render$4(_ctx, _cache, $props, $setup, $data, $options) {
  const _component_Palette = resolveComponent("Palette");
  const _component_Board = resolveComponent("Board");
  const _component_Hue = resolveComponent("Hue");
  const _component_Lightness = resolveComponent("Lightness");
  const _component_Alpha = resolveComponent("Alpha");
  const _component_Display = resolveComponent("Display");
  const _component_History = resolveComponent("History");
  return openBlock(), createElementBlock("div", _hoisted_1$4, [
    createElementVNode("div", _hoisted_2$3, [
      createElementVNode("div", _hoisted_3$3, [
        _ctx.advancePanelShow ? (openBlock(), createElementBlock("span", {
          key: 0,
          style: { "cursor": "pointer" },
          onClick: _cache[0] || (_cache[0] = (...args) => _ctx.onBack && _ctx.onBack(...args))
        }, _hoisted_5$3)) : createCommentVNode("", true)
      ]),
      !_ctx.advancePanelShow ? (openBlock(), createBlock(_component_Palette, {
        key: 0,
        onChange: _ctx.onCompactChange
      }, null, 8, ["onChange"])) : createCommentVNode("", true),
      _ctx.advancePanelShow ? (openBlock(), createBlock(_component_Board, {
        key: 1,
        color: _ctx.state.color,
        onChange: _ctx.onBoardChange
      }, null, 8, ["color", "onChange"])) : createCommentVNode("", true),
      _ctx.advancePanelShow ? (openBlock(), createBlock(_component_Hue, {
        key: 2,
        color: _ctx.state.color,
        onChange: _ctx.onHueChange
      }, null, 8, ["color", "onChange"])) : createCommentVNode("", true),
      !_ctx.advancePanelShow ? (openBlock(), createBlock(_component_Lightness, {
        key: 3,
        color: _ctx.state.color,
        onChange: _ctx.onLightChange
      }, null, 8, ["color", "onChange"])) : createCommentVNode("", true),
      !_ctx.disableAlpha ? (openBlock(), createBlock(_component_Alpha, {
        key: 4,
        color: _ctx.state.color,
        onChange: _ctx.onAlphaChange
      }, null, 8, ["color", "onChange"])) : createCommentVNode("", true),
      createVNode(_component_Display, {
        color: _ctx.state.color,
        "disable-alpha": _ctx.disableAlpha
      }, null, 8, ["color", "disable-alpha"]),
      !_ctx.disableHistory ? (openBlock(), createBlock(_component_History, {
        key: 5,
        round: _ctx.roundHistory,
        colors: _ctx.historyColors,
        onChange: _ctx.onCompactChange
      }, null, 8, ["round", "colors", "onChange"])) : createCommentVNode("", true)
    ])
  ]);
}
var FkColorPicker = /* @__PURE__ */ _export_sfc(_sfc_main$4, [["render", _sfc_render$4], ["__scopeId", "data-v-167618b1"]]);
var ChromeColorPicker_vue_vue_type_style_index_0_scoped_true_lang = "";
const _sfc_main$3 = defineComponent({
  name: "ChromeColorPicker",
  components: { Display, Alpha, Board, Hue, History },
  props: {
    color: C$1.instanceOf(Color),
    disableHistory: C$1.bool.def(false),
    roundHistory: C$1.bool.def(false),
    disableAlpha: C$1.bool.def(false)
  },
  emits: ["update:color", "change"],
  setup(props, { emit }) {
    const colorInstance = props.color || new Color();
    const state = reactive({
      color: colorInstance,
      hex: colorInstance.toHexString(),
      rgb: colorInstance.toRgbString()
    });
    const previewStyle = computed(() => {
      return { background: state.rgb };
    });
    const historyColors = useLocalStorage(HistoryColorKey, [], {});
    const updateColorHistoryFn = useDebounceFn(() => {
      if (props.disableHistory) {
        return;
      }
      const rgbString = state.color.toRgbString();
      historyColors.value = historyColors.value.filter((value) => {
        return !tinycolor.equals(value, rgbString);
      });
      if (historyColors.value.includes(rgbString)) {
        return;
      }
      while (historyColors.value.length > MAX_STORAGE_LENGTH) {
        historyColors.value.pop();
      }
      historyColors.value.unshift(rgbString);
    }, 500);
    const onAlphaChange = (alpha) => {
      state.color.alpha = alpha;
    };
    const onHueChange = (hue) => {
      state.color.hue = hue;
    };
    const onBoardChange = (saturation, brightness) => {
      state.color.saturation = saturation;
      state.color.brightness = brightness;
    };
    const onCompactChange = (color) => {
      if (color !== "advance") {
        state.color.hex = color;
      }
    };
    whenever(
      () => props.color,
      (value) => {
        if (value) {
          state.color = value;
        }
      },
      { deep: true }
    );
    whenever(
      () => state.color,
      () => {
        state.hex = state.color.hex;
        state.rgb = state.color.toRgbString();
        updateColorHistoryFn();
        emit("update:color", state.color);
        emit("change", state.color);
      },
      { deep: true }
    );
    return {
      state,
      previewStyle,
      historyColors,
      onAlphaChange,
      onHueChange,
      onBoardChange,
      onCompactChange
    };
  }
});
const _hoisted_1$3 = { class: "vc-chrome-colorPicker" };
const _hoisted_2$2 = { class: "vc-chrome-colorPicker-body" };
const _hoisted_3$2 = { class: "chrome-controls" };
const _hoisted_4$2 = { class: "chrome-color-wrap transparent" };
const _hoisted_5$2 = { class: "chrome-sliders" };
function _sfc_render$3(_ctx, _cache, $props, $setup, $data, $options) {
  const _component_Board = resolveComponent("Board");
  const _component_Hue = resolveComponent("Hue");
  const _component_Alpha = resolveComponent("Alpha");
  const _component_Display = resolveComponent("Display");
  const _component_History = resolveComponent("History");
  return openBlock(), createElementBlock("div", _hoisted_1$3, [
    createVNode(_component_Board, {
      round: true,
      hide: false,
      color: _ctx.state.color,
      onChange: _ctx.onBoardChange
    }, null, 8, ["color", "onChange"]),
    createElementVNode("div", _hoisted_2$2, [
      createElementVNode("div", _hoisted_3$2, [
        createElementVNode("div", _hoisted_4$2, [
          createElementVNode("div", {
            class: "current-color",
            style: normalizeStyle(_ctx.previewStyle)
          }, null, 4)
        ]),
        createElementVNode("div", _hoisted_5$2, [
          createVNode(_component_Hue, {
            size: "small",
            color: _ctx.state.color,
            onChange: _ctx.onHueChange
          }, null, 8, ["color", "onChange"]),
          !_ctx.disableAlpha ? (openBlock(), createBlock(_component_Alpha, {
            key: 0,
            size: "small",
            color: _ctx.state.color,
            onChange: _ctx.onAlphaChange
          }, null, 8, ["color", "onChange"])) : createCommentVNode("", true)
        ])
      ]),
      createVNode(_component_Display, {
        color: _ctx.state.color,
        "disable-alpha": _ctx.disableAlpha
      }, null, 8, ["color", "disable-alpha"]),
      !_ctx.disableHistory ? (openBlock(), createBlock(_component_History, {
        key: 0,
        round: _ctx.roundHistory,
        colors: _ctx.historyColors,
        onChange: _ctx.onCompactChange
      }, null, 8, ["round", "colors", "onChange"])) : createCommentVNode("", true)
    ])
  ]);
}
var ChromeColorPicker = /* @__PURE__ */ _export_sfc(_sfc_main$3, [["render", _sfc_render$3], ["__scopeId", "data-v-5185c8a7"]]);
const calcAngle = (element, event) => {
  const rect = element.getBoundingClientRect();
  const originX = rect.left + rect.width / 2;
  const originY = rect.top + rect.height / 2;
  const x2 = Math.abs(originX - event.clientX);
  const y2 = Math.abs(originY - event.clientY);
  const z2 = Math.sqrt(Math.pow(x2, 2) + Math.pow(y2, 2));
  const cos = y2 / z2;
  const rad = Math.acos(cos);
  let angle = Math.floor(180 / (Math.PI / rad));
  if (event.clientX > originX && event.clientY > originY) {
    angle = 180 - angle;
  }
  if (event.clientX == originX && event.clientY > originY) {
    angle = 180;
  }
  if (event.clientX > originX && event.clientY == originY) {
    angle = 90;
  }
  if (event.clientX < originX && event.clientY > originY) {
    angle = 180 + angle;
  }
  if (event.clientX < originX && event.clientY == originY) {
    angle = 270;
  }
  if (event.clientX < originX && event.clientY < originY) {
    angle = 360 - angle;
  }
  return angle;
};
let isDragging = false;
const triggerDragEvent = (element, options) => {
  const moveFn = function(event) {
    var _a;
    (_a = options.drag) == null ? void 0 : _a.call(options, event);
  };
  const upFn = function(event) {
    var _a;
    document.removeEventListener("mousemove", moveFn, false);
    document.removeEventListener("mouseup", upFn, false);
    document.onselectstart = null;
    document.ondragstart = null;
    isDragging = false;
    (_a = options.end) == null ? void 0 : _a.call(options, event);
  };
  if (element) {
    element.addEventListener("mousedown", (event) => {
      var _a;
      if (isDragging)
        return;
      document.onselectstart = () => false;
      document.ondragstart = () => false;
      document.addEventListener("mousemove", moveFn, false);
      document.addEventListener("mouseup", upFn, false);
      isDragging = true;
      (_a = options.start) == null ? void 0 : _a.call(options, event);
    });
  }
  return;
};
const angleProps = {
  angle: {
    type: Number,
    default: 0
  },
  size: {
    type: Number,
    default: 16,
    validator: (value) => {
      return value >= 16;
    }
  },
  borderWidth: {
    type: Number,
    default: 1,
    validator: (value) => {
      return value >= 1;
    }
  },
  borderColor: {
    type: String,
    default: "#666"
  }
};
var Angle = defineComponent({
  name: "Angle",
  props: angleProps,
  emits: ["update:angle", "change"],
  setup(props, {
    emit
  }) {
    const angleRef = ref(null);
    const rotate = ref(props.angle);
    watch(() => props.angle, (angle) => {
      rotate.value = angle;
    });
    const updateAngle = () => {
      let value = Number(rotate.value);
      if (!isNaN(value)) {
        value = value > 360 || value < 0 ? props.angle : value;
        rotate.value = value === 360 ? 0 : value;
        emit("update:angle", rotate.value);
        emit("change", rotate.value);
      }
    };
    const getStyle = computed(() => {
      return {
        width: props.size + "px",
        height: props.size + "px",
        borderWidth: props.borderWidth + "px",
        borderColor: props.borderColor,
        transform: `rotate(${rotate.value}deg)`
      };
    });
    const handleDrag = (event) => {
      if (angleRef.value) {
        rotate.value = calcAngle(angleRef.value, event) % 360;
        updateAngle();
      }
    };
    onMounted(() => {
      const dragConfig = {
        drag: (event) => {
          handleDrag(event);
        },
        end: (event) => {
          handleDrag(event);
        }
      };
      if (angleRef.value) {
        triggerDragEvent(angleRef.value, dragConfig);
      }
    });
    return () => {
      return createVNode("div", {
        "class": "bee-angle"
      }, [createVNode("div", {
        "class": "bee-angle__round",
        "ref": angleRef,
        "style": getStyle.value
      }, null)]);
    };
  }
});
var style = "";
const ColorPickerProviderKey = "Vue3ColorPickerProvider";
var GradientColorPicker_vue_vue_type_style_index_0_scoped_true_lang = "";
const _sfc_main$2 = defineComponent({
  name: "GradientColorPicker",
  components: { Angle, Display, Alpha, Palette, Board, Hue, Lightness, History },
  props: {
    startColor: C$1.instanceOf(Color).isRequired,
    endColor: C$1.instanceOf(Color).isRequired,
    startColorStop: C$1.number.def(0),
    endColorStop: C$1.number.def(100),
    angle: C$1.number.def(0),
    disableHistory: C$1.bool.def(false),
    roundHistory: C$1.bool.def(false),
    disableAlpha: C$1.bool.def(false)
  },
  emits: [
    "update:startColor",
    "update:endColor",
    "update:angle",
    "update:startColorStop",
    "update:endColorStop",
    "startColorChange",
    "endColorChange",
    "advanceChange",
    "angleChange",
    "startColorStopChange",
    "endColorStopChange"
  ],
  setup(props, { emit }) {
    const state = reactive({
      startActive: true,
      startColor: props.startColor,
      endColor: props.endColor,
      startColorStop: props.startColorStop,
      endColorStop: props.endColorStop,
      angle: props.angle,
      startColorRgba: props.startColor.toRgbString(),
      endColorRgba: props.endColor.toRgbString()
    });
    const parent = inject(ColorPickerProviderKey);
    const advancePanelShow = ref(false);
    const startGradientRef = ref();
    const stopGradientRef = ref();
    const colorRangeRef = ref();
    const currentColor = computed({
      get: () => {
        return state.startActive ? state.startColor : state.endColor;
      },
      set: (v2) => {
        if (state.startActive) {
          state.startColor = v2;
          return;
        }
        state.endColor = v2;
      }
    });
    const getStartColorLeft = computed(() => {
      if (colorRangeRef.value && startGradientRef.value) {
        const alpha = state.startColorStop / 100;
        const rect = colorRangeRef.value.getBoundingClientRect();
        const offsetWidth = startGradientRef.value.offsetWidth;
        return Math.round(alpha * (rect.width - offsetWidth) + offsetWidth / 2);
      }
      return 0;
    });
    const getEndColorLeft = computed(() => {
      if (colorRangeRef.value && stopGradientRef.value) {
        const alpha = state.endColorStop / 100;
        const rect = colorRangeRef.value.getBoundingClientRect();
        const offsetWidth = stopGradientRef.value.offsetWidth;
        return Math.round(alpha * (rect.width - offsetWidth) + offsetWidth / 2);
      }
      return 0;
    });
    const gradientBg = computed(() => {
      return {
        background: `linear-gradient(${state.angle}deg, ${state.startColorRgba} ${state.startColorStop}%, ${state.endColorRgba} ${state.endColorStop}%)`
      };
    });
    const dragStartRange = (evt) => {
      var _a;
      state.startActive = true;
      if (colorRangeRef.value && startGradientRef.value) {
        const rect = (_a = colorRangeRef.value) == null ? void 0 : _a.getBoundingClientRect();
        let left2 = evt.clientX - rect.left;
        left2 = Math.max(startGradientRef.value.offsetWidth / 2, left2);
        left2 = Math.min(left2, rect.width - startGradientRef.value.offsetWidth / 2);
        state.startColorStop = Math.round(
          (left2 - startGradientRef.value.offsetWidth / 2) / (rect.width - startGradientRef.value.offsetWidth) * 100
        );
        emit("update:startColorStop", state.startColorStop);
        emit("startColorStopChange", state.startColorStop);
      }
    };
    const dragEndRange = (evt) => {
      var _a;
      state.startActive = false;
      if (colorRangeRef.value && stopGradientRef.value) {
        const rect = (_a = colorRangeRef.value) == null ? void 0 : _a.getBoundingClientRect();
        let left2 = evt.clientX - rect.left;
        left2 = Math.max(stopGradientRef.value.offsetWidth / 2, left2);
        left2 = Math.min(left2, rect.width - stopGradientRef.value.offsetWidth / 2);
        state.endColorStop = Math.round(
          (left2 - stopGradientRef.value.offsetWidth / 2) / (rect.width - stopGradientRef.value.offsetWidth) * 100
        );
        emit("update:endColorStop", state.endColorStop);
        emit("endColorStopChange", state.endColorStop);
      }
    };
    const onDegreeBlur = (evt) => {
      const target = evt.target;
      const degree = parseInt(target.value.replace("\xB0", ""));
      if (!isNaN(degree)) {
        state.angle = degree % 360;
      }
      emit("update:angle", state.angle);
      emit("angleChange", state.angle);
    };
    const onDegreeChange = (angle) => {
      state.angle = angle;
      emit("update:angle", state.angle);
      emit("angleChange", state.angle);
    };
    const onCompactChange = (color) => {
      if (color === "advance") {
        advancePanelShow.value = true;
        emit("advanceChange", true);
      } else {
        currentColor.value.hex = color;
        emit("advanceChange", false);
      }
      doColorChange();
    };
    const onAlphaChange = (alpha) => {
      currentColor.value.alpha = alpha;
      doColorChange();
    };
    const onHueChange = (hue) => {
      currentColor.value.hue = hue;
      doColorChange();
    };
    const onBoardChange = (saturation, brightness) => {
      currentColor.value.saturation = saturation;
      currentColor.value.brightness = brightness;
      doColorChange();
    };
    const onLightChange = (light) => {
      currentColor.value.lightness = light;
      doColorChange();
    };
    const doColorChange = () => {
      if (state.startActive) {
        emit("update:startColor", state.startColor);
        emit("startColorChange", state.startColor);
      } else {
        emit("update:endColor", state.endColor);
        emit("endColorChange", state.endColor);
      }
    };
    const onBack = () => {
      advancePanelShow.value = false;
      emit("advanceChange", false);
    };
    const historyColors = useLocalStorage(HistoryColorKey, [], {});
    const updateColorHistoryFn = useDebounceFn(() => {
      if (props.disableHistory) {
        return;
      }
      const rgbString = currentColor.value.toRgbString();
      historyColors.value = historyColors.value.filter((value) => {
        return !tinycolor.equals(value, rgbString);
      });
      if (historyColors.value.includes(rgbString)) {
        return;
      }
      while (historyColors.value.length > MAX_STORAGE_LENGTH) {
        historyColors.value.pop();
      }
      historyColors.value.unshift(rgbString);
    }, 500);
    tryOnMounted(() => {
      if (stopGradientRef.value && startGradientRef.value) {
        Vn.triggerDragEvent(stopGradientRef.value, {
          drag: (event) => {
            dragEndRange(event);
          },
          end: (event) => {
            dragEndRange(event);
          }
        });
        Vn.triggerDragEvent(startGradientRef.value, {
          drag: (event) => {
            dragStartRange(event);
          },
          end: (event) => {
            dragStartRange(event);
          }
        });
      }
    });
    whenever(
      () => state.startColor,
      (value) => {
        state.startColorRgba = value.toRgbString();
      },
      { deep: true }
    );
    whenever(
      () => state.endColor,
      (value) => {
        state.endColorRgba = value.toRgbString();
      },
      { deep: true }
    );
    whenever(
      () => currentColor.value,
      () => {
        updateColorHistoryFn();
      },
      { deep: true }
    );
    return {
      startGradientRef,
      stopGradientRef,
      colorRangeRef,
      state,
      currentColor,
      getStartColorLeft,
      getEndColorLeft,
      gradientBg,
      advancePanelShow,
      onDegreeBlur,
      onCompactChange,
      onAlphaChange,
      onHueChange,
      onBoardChange,
      onLightChange,
      historyColors,
      onBack,
      onDegreeChange,
      lang: parent == null ? void 0 : parent.lang
    };
  }
});
const _withScopeId = (n2) => (pushScopeId("data-v-01e2f60f"), n2 = n2(), popScopeId(), n2);
const _hoisted_1$2 = { class: "vc-gradient-picker" };
const _hoisted_2$1 = { class: "vc-gradient-picker__header" };
const _hoisted_3$1 = /* @__PURE__ */ _withScopeId(() => /* @__PURE__ */ createElementVNode("div", { class: "back" }, null, -1));
const _hoisted_4$1 = [
  _hoisted_3$1
];
const _hoisted_5$1 = { class: "vc-gradient-picker__body" };
const _hoisted_6$1 = {
  class: "vc-color-range",
  ref: "colorRangeRef"
};
const _hoisted_7 = { class: "vc-color-range__container" };
const _hoisted_8 = { class: "vc-gradient__stop__container" };
const _hoisted_9 = ["title"];
const _hoisted_10 = /* @__PURE__ */ _withScopeId(() => /* @__PURE__ */ createElementVNode("span", { class: "vc-gradient__stop--inner" }, null, -1));
const _hoisted_11 = [
  _hoisted_10
];
const _hoisted_12 = ["title"];
const _hoisted_13 = /* @__PURE__ */ _withScopeId(() => /* @__PURE__ */ createElementVNode("span", { class: "vc-gradient__stop--inner" }, null, -1));
const _hoisted_14 = [
  _hoisted_13
];
const _hoisted_15 = { class: "vc-picker-degree-input vc-degree-input" };
const _hoisted_16 = { class: "vc-degree-input__control" };
const _hoisted_17 = ["value"];
const _hoisted_18 = { class: "vc-degree-input__panel" };
const _hoisted_19 = { class: "vc-degree-input__disk" };
function _sfc_render$2(_ctx, _cache, $props, $setup, $data, $options) {
  const _component_Angle = resolveComponent("Angle");
  const _component_Palette = resolveComponent("Palette");
  const _component_Board = resolveComponent("Board");
  const _component_Hue = resolveComponent("Hue");
  const _component_Lightness = resolveComponent("Lightness");
  const _component_Alpha = resolveComponent("Alpha");
  const _component_Display = resolveComponent("Display");
  const _component_History = resolveComponent("History");
  return openBlock(), createElementBlock("div", _hoisted_1$2, [
    withDirectives(createElementVNode("div", _hoisted_2$1, [
      createElementVNode("span", {
        style: { "cursor": "pointer" },
        onClick: _cache[0] || (_cache[0] = (...args) => _ctx.onBack && _ctx.onBack(...args))
      }, _hoisted_4$1)
    ], 512), [
      [vShow, _ctx.advancePanelShow]
    ]),
    createElementVNode("div", _hoisted_5$1, [
      createElementVNode("div", _hoisted_6$1, [
        createElementVNode("div", _hoisted_7, [
          createElementVNode("div", {
            class: "vc-background",
            style: normalizeStyle(_ctx.gradientBg)
          }, null, 4),
          createElementVNode("div", _hoisted_8, [
            createElementVNode("div", {
              class: normalizeClass([
                "vc-gradient__stop",
                {
                  "vc-gradient__stop--current": _ctx.state.startActive
                }
              ]),
              ref: "startGradientRef",
              title: _ctx.lang === "ZH-cn" ? "\u5F00\u59CB" : "Start",
              style: normalizeStyle({ left: _ctx.getStartColorLeft + "px" })
            }, _hoisted_11, 14, _hoisted_9),
            createElementVNode("div", {
              class: normalizeClass([
                "vc-gradient__stop",
                {
                  "vc-gradient__stop--current": !_ctx.state.startActive
                }
              ]),
              ref: "stopGradientRef",
              title: _ctx.lang === "ZH-cn" ? "\u7ED3\u675F" : "End",
              style: normalizeStyle({ left: _ctx.getEndColorLeft + "px" })
            }, _hoisted_14, 14, _hoisted_12)
          ])
        ])
      ], 512),
      createElementVNode("div", _hoisted_15, [
        createElementVNode("div", _hoisted_16, [
          createElementVNode("input", {
            value: _ctx.state.angle,
            onBlur: _cache[1] || (_cache[1] = (...args) => _ctx.onDegreeBlur && _ctx.onDegreeBlur(...args)),
            onKeydown: _cache[2] || (_cache[2] = withKeys((...args) => _ctx.onDegreeBlur && _ctx.onDegreeBlur(...args), ["enter"]))
          }, null, 40, _hoisted_17)
        ]),
        createElementVNode("div", _hoisted_18, [
          createElementVNode("div", _hoisted_19, [
            createVNode(_component_Angle, {
              angle: _ctx.state.angle,
              "onUpdate:angle": _cache[3] || (_cache[3] = ($event) => _ctx.state.angle = $event),
              size: 40,
              onChange: _ctx.onDegreeChange
            }, null, 8, ["angle", "onChange"])
          ])
        ])
      ])
    ]),
    !_ctx.advancePanelShow ? (openBlock(), createBlock(_component_Palette, {
      key: 0,
      onChange: _ctx.onCompactChange
    }, null, 8, ["onChange"])) : createCommentVNode("", true),
    _ctx.advancePanelShow ? (openBlock(), createBlock(_component_Board, {
      key: 1,
      color: _ctx.currentColor,
      onChange: _ctx.onBoardChange
    }, null, 8, ["color", "onChange"])) : createCommentVNode("", true),
    _ctx.advancePanelShow ? (openBlock(), createBlock(_component_Hue, {
      key: 2,
      color: _ctx.currentColor,
      onChange: _ctx.onHueChange
    }, null, 8, ["color", "onChange"])) : createCommentVNode("", true),
    !_ctx.advancePanelShow ? (openBlock(), createBlock(_component_Lightness, {
      key: 3,
      color: _ctx.currentColor,
      onChange: _ctx.onLightChange
    }, null, 8, ["color", "onChange"])) : createCommentVNode("", true),
    !_ctx.disableAlpha ? (openBlock(), createBlock(_component_Alpha, {
      key: 4,
      color: _ctx.currentColor,
      onChange: _ctx.onAlphaChange
    }, null, 8, ["color", "onChange"])) : createCommentVNode("", true),
    createVNode(_component_Display, {
      color: _ctx.currentColor,
      "disable-alpha": _ctx.disableAlpha
    }, null, 8, ["color", "disable-alpha"]),
    !_ctx.disableHistory ? (openBlock(), createBlock(_component_History, {
      key: 5,
      round: _ctx.roundHistory,
      colors: _ctx.historyColors,
      onChange: _ctx.onCompactChange
    }, null, 8, ["round", "colors", "onChange"])) : createCommentVNode("", true)
  ]);
}
var GradientColorPicker = /* @__PURE__ */ _export_sfc(_sfc_main$2, [["render", _sfc_render$2], ["__scopeId", "data-v-01e2f60f"]]);
var WrapContainer_vue_vue_type_style_index_0_scoped_true_lang = "";
const _sfc_main$1 = defineComponent({
  name: "WrapContainer",
  props: {
    showTab: C$1.bool.def(false),
    activeKey: C$1.oneOf(["pure", "gradient"]).def("pure")
  },
  emits: ["update:activeKey", "change"],
  setup(props, { emit }) {
    const state = reactive({
      activeKey: props.activeKey
    });
    const parent = inject(ColorPickerProviderKey);
    const onActiveKeyChange = (key) => {
      state.activeKey = key;
      emit("update:activeKey", key);
      emit("change", key);
    };
    whenever(
      () => props.activeKey,
      (value) => {
        state.activeKey = value;
      }
    );
    return { state, onActiveKeyChange, lang: parent == null ? void 0 : parent.lang };
  }
});
const _hoisted_1$1 = { class: "vc-colorpicker" };
const _hoisted_2 = { class: "vc-colorpicker--container" };
const _hoisted_3 = {
  key: 0,
  class: "vc-colorpicker--tabs"
};
const _hoisted_4 = { class: "vc-colorpicker--tabs__inner" };
const _hoisted_5 = { class: "vc-btn__content" };
const _hoisted_6 = { class: "vc-btn__content" };
function _sfc_render$1(_ctx, _cache, $props, $setup, $data, $options) {
  return openBlock(), createElementBlock("div", _hoisted_1$1, [
    createElementVNode("div", _hoisted_2, [
      _ctx.showTab ? (openBlock(), createElementBlock("div", _hoisted_3, [
        createElementVNode("div", _hoisted_4, [
          createElementVNode("div", {
            class: normalizeClass([
              "vc-colorpicker--tabs__btn",
              {
                "vc-btn-active": _ctx.state.activeKey === "pure"
              }
            ]),
            onClick: _cache[0] || (_cache[0] = ($event) => _ctx.onActiveKeyChange("pure"))
          }, [
            createElementVNode("button", null, [
              createElementVNode("div", _hoisted_5, toDisplayString(_ctx.lang === "ZH-cn" ? "\u7EAF\u8272" : "Pure"), 1)
            ])
          ], 2),
          createElementVNode("div", {
            class: normalizeClass([
              "vc-colorpicker--tabs__btn",
              {
                "vc-btn-active": _ctx.state.activeKey === "gradient"
              }
            ]),
            onClick: _cache[1] || (_cache[1] = ($event) => _ctx.onActiveKeyChange("gradient"))
          }, [
            createElementVNode("button", null, [
              createElementVNode("div", _hoisted_6, toDisplayString(_ctx.lang === "ZH-cn" ? "\u6E10\u53D8\u8272" : "Gradient"), 1)
            ])
          ], 2),
          createElementVNode("div", {
            class: "vc-colorpicker--tabs__bg",
            style: normalizeStyle({
              width: `50%`,
              left: `calc(${_ctx.state.activeKey === "gradient" ? 50 : 0}%)`
            })
          }, null, 4)
        ])
      ])) : createCommentVNode("", true),
      renderSlot(_ctx.$slots, "default", {}, void 0, true)
    ])
  ]);
}
var WrapContainer = /* @__PURE__ */ _export_sfc(_sfc_main$1, [["render", _sfc_render$1], ["__scopeId", "data-v-d3ada6a8"]]);
var top = "top";
var bottom = "bottom";
var right = "right";
var left = "left";
var auto = "auto";
var basePlacements = [top, bottom, right, left];
var start = "start";
var end = "end";
var clippingParents = "clippingParents";
var viewport = "viewport";
var popper = "popper";
var reference = "reference";
var variationPlacements = /* @__PURE__ */ basePlacements.reduce(function(acc, placement) {
  return acc.concat([placement + "-" + start, placement + "-" + end]);
}, []);
var placements = /* @__PURE__ */ [].concat(basePlacements, [auto]).reduce(function(acc, placement) {
  return acc.concat([placement, placement + "-" + start, placement + "-" + end]);
}, []);
var beforeRead = "beforeRead";
var read = "read";
var afterRead = "afterRead";
var beforeMain = "beforeMain";
var main = "main";
var afterMain = "afterMain";
var beforeWrite = "beforeWrite";
var write = "write";
var afterWrite = "afterWrite";
var modifierPhases = [beforeRead, read, afterRead, beforeMain, main, afterMain, beforeWrite, write, afterWrite];
function getNodeName(element) {
  return element ? (element.nodeName || "").toLowerCase() : null;
}
function getWindow(node) {
  if (node == null) {
    return window;
  }
  if (node.toString() !== "[object Window]") {
    var ownerDocument = node.ownerDocument;
    return ownerDocument ? ownerDocument.defaultView || window : window;
  }
  return node;
}
function isElement(node) {
  var OwnElement = getWindow(node).Element;
  return node instanceof OwnElement || node instanceof Element;
}
function isHTMLElement(node) {
  var OwnElement = getWindow(node).HTMLElement;
  return node instanceof OwnElement || node instanceof HTMLElement;
}
function isShadowRoot(node) {
  if (typeof ShadowRoot === "undefined") {
    return false;
  }
  var OwnElement = getWindow(node).ShadowRoot;
  return node instanceof OwnElement || node instanceof ShadowRoot;
}
function applyStyles(_ref) {
  var state = _ref.state;
  Object.keys(state.elements).forEach(function(name) {
    var style2 = state.styles[name] || {};
    var attributes = state.attributes[name] || {};
    var element = state.elements[name];
    if (!isHTMLElement(element) || !getNodeName(element)) {
      return;
    }
    Object.assign(element.style, style2);
    Object.keys(attributes).forEach(function(name2) {
      var value = attributes[name2];
      if (value === false) {
        element.removeAttribute(name2);
      } else {
        element.setAttribute(name2, value === true ? "" : value);
      }
    });
  });
}
function effect$2(_ref2) {
  var state = _ref2.state;
  var initialStyles = {
    popper: {
      position: state.options.strategy,
      left: "0",
      top: "0",
      margin: "0"
    },
    arrow: {
      position: "absolute"
    },
    reference: {}
  };
  Object.assign(state.elements.popper.style, initialStyles.popper);
  state.styles = initialStyles;
  if (state.elements.arrow) {
    Object.assign(state.elements.arrow.style, initialStyles.arrow);
  }
  return function() {
    Object.keys(state.elements).forEach(function(name) {
      var element = state.elements[name];
      var attributes = state.attributes[name] || {};
      var styleProperties = Object.keys(state.styles.hasOwnProperty(name) ? state.styles[name] : initialStyles[name]);
      var style2 = styleProperties.reduce(function(style3, property) {
        style3[property] = "";
        return style3;
      }, {});
      if (!isHTMLElement(element) || !getNodeName(element)) {
        return;
      }
      Object.assign(element.style, style2);
      Object.keys(attributes).forEach(function(attribute) {
        element.removeAttribute(attribute);
      });
    });
  };
}
var applyStyles$1 = {
  name: "applyStyles",
  enabled: true,
  phase: "write",
  fn: applyStyles,
  effect: effect$2,
  requires: ["computeStyles"]
};
function getBasePlacement(placement) {
  return placement.split("-")[0];
}
var max = Math.max;
var min = Math.min;
var round = Math.round;
function getUAString() {
  var uaData = navigator.userAgentData;
  if (uaData != null && uaData.brands) {
    return uaData.brands.map(function(item) {
      return item.brand + "/" + item.version;
    }).join(" ");
  }
  return navigator.userAgent;
}
function isLayoutViewport() {
  return !/^((?!chrome|android).)*safari/i.test(getUAString());
}
function getBoundingClientRect(element, includeScale, isFixedStrategy) {
  if (includeScale === void 0) {
    includeScale = false;
  }
  if (isFixedStrategy === void 0) {
    isFixedStrategy = false;
  }
  var clientRect = element.getBoundingClientRect();
  var scaleX = 1;
  var scaleY = 1;
  if (includeScale && isHTMLElement(element)) {
    scaleX = element.offsetWidth > 0 ? round(clientRect.width) / element.offsetWidth || 1 : 1;
    scaleY = element.offsetHeight > 0 ? round(clientRect.height) / element.offsetHeight || 1 : 1;
  }
  var _ref = isElement(element) ? getWindow(element) : window, visualViewport = _ref.visualViewport;
  var addVisualOffsets = !isLayoutViewport() && isFixedStrategy;
  var x2 = (clientRect.left + (addVisualOffsets && visualViewport ? visualViewport.offsetLeft : 0)) / scaleX;
  var y2 = (clientRect.top + (addVisualOffsets && visualViewport ? visualViewport.offsetTop : 0)) / scaleY;
  var width = clientRect.width / scaleX;
  var height = clientRect.height / scaleY;
  return {
    width,
    height,
    top: y2,
    right: x2 + width,
    bottom: y2 + height,
    left: x2,
    x: x2,
    y: y2
  };
}
function getLayoutRect(element) {
  var clientRect = getBoundingClientRect(element);
  var width = element.offsetWidth;
  var height = element.offsetHeight;
  if (Math.abs(clientRect.width - width) <= 1) {
    width = clientRect.width;
  }
  if (Math.abs(clientRect.height - height) <= 1) {
    height = clientRect.height;
  }
  return {
    x: element.offsetLeft,
    y: element.offsetTop,
    width,
    height
  };
}
function contains(parent, child) {
  var rootNode = child.getRootNode && child.getRootNode();
  if (parent.contains(child)) {
    return true;
  } else if (rootNode && isShadowRoot(rootNode)) {
    var next = child;
    do {
      if (next && parent.isSameNode(next)) {
        return true;
      }
      next = next.parentNode || next.host;
    } while (next);
  }
  return false;
}
function getComputedStyle(element) {
  return getWindow(element).getComputedStyle(element);
}
function isTableElement(element) {
  return ["table", "td", "th"].indexOf(getNodeName(element)) >= 0;
}
function getDocumentElement(element) {
  return ((isElement(element) ? element.ownerDocument : element.document) || window.document).documentElement;
}
function getParentNode(element) {
  if (getNodeName(element) === "html") {
    return element;
  }
  return element.assignedSlot || element.parentNode || (isShadowRoot(element) ? element.host : null) || getDocumentElement(element);
}
function getTrueOffsetParent(element) {
  if (!isHTMLElement(element) || getComputedStyle(element).position === "fixed") {
    return null;
  }
  return element.offsetParent;
}
function getContainingBlock(element) {
  var isFirefox = /firefox/i.test(getUAString());
  var isIE = /Trident/i.test(getUAString());
  if (isIE && isHTMLElement(element)) {
    var elementCss = getComputedStyle(element);
    if (elementCss.position === "fixed") {
      return null;
    }
  }
  var currentNode = getParentNode(element);
  if (isShadowRoot(currentNode)) {
    currentNode = currentNode.host;
  }
  while (isHTMLElement(currentNode) && ["html", "body"].indexOf(getNodeName(currentNode)) < 0) {
    var css = getComputedStyle(currentNode);
    if (css.transform !== "none" || css.perspective !== "none" || css.contain === "paint" || ["transform", "perspective"].indexOf(css.willChange) !== -1 || isFirefox && css.willChange === "filter" || isFirefox && css.filter && css.filter !== "none") {
      return currentNode;
    } else {
      currentNode = currentNode.parentNode;
    }
  }
  return null;
}
function getOffsetParent(element) {
  var window2 = getWindow(element);
  var offsetParent = getTrueOffsetParent(element);
  while (offsetParent && isTableElement(offsetParent) && getComputedStyle(offsetParent).position === "static") {
    offsetParent = getTrueOffsetParent(offsetParent);
  }
  if (offsetParent && (getNodeName(offsetParent) === "html" || getNodeName(offsetParent) === "body" && getComputedStyle(offsetParent).position === "static")) {
    return window2;
  }
  return offsetParent || getContainingBlock(element) || window2;
}
function getMainAxisFromPlacement(placement) {
  return ["top", "bottom"].indexOf(placement) >= 0 ? "x" : "y";
}
function within(min$1, value, max$1) {
  return max(min$1, min(value, max$1));
}
function withinMaxClamp(min2, value, max2) {
  var v2 = within(min2, value, max2);
  return v2 > max2 ? max2 : v2;
}
function getFreshSideObject() {
  return {
    top: 0,
    right: 0,
    bottom: 0,
    left: 0
  };
}
function mergePaddingObject(paddingObject) {
  return Object.assign({}, getFreshSideObject(), paddingObject);
}
function expandToHashMap(value, keys) {
  return keys.reduce(function(hashMap, key) {
    hashMap[key] = value;
    return hashMap;
  }, {});
}
var toPaddingObject = function toPaddingObject2(padding, state) {
  padding = typeof padding === "function" ? padding(Object.assign({}, state.rects, {
    placement: state.placement
  })) : padding;
  return mergePaddingObject(typeof padding !== "number" ? padding : expandToHashMap(padding, basePlacements));
};
function arrow(_ref) {
  var _state$modifiersData$;
  var state = _ref.state, name = _ref.name, options = _ref.options;
  var arrowElement = state.elements.arrow;
  var popperOffsets2 = state.modifiersData.popperOffsets;
  var basePlacement = getBasePlacement(state.placement);
  var axis = getMainAxisFromPlacement(basePlacement);
  var isVertical = [left, right].indexOf(basePlacement) >= 0;
  var len = isVertical ? "height" : "width";
  if (!arrowElement || !popperOffsets2) {
    return;
  }
  var paddingObject = toPaddingObject(options.padding, state);
  var arrowRect = getLayoutRect(arrowElement);
  var minProp = axis === "y" ? top : left;
  var maxProp = axis === "y" ? bottom : right;
  var endDiff = state.rects.reference[len] + state.rects.reference[axis] - popperOffsets2[axis] - state.rects.popper[len];
  var startDiff = popperOffsets2[axis] - state.rects.reference[axis];
  var arrowOffsetParent = getOffsetParent(arrowElement);
  var clientSize = arrowOffsetParent ? axis === "y" ? arrowOffsetParent.clientHeight || 0 : arrowOffsetParent.clientWidth || 0 : 0;
  var centerToReference = endDiff / 2 - startDiff / 2;
  var min2 = paddingObject[minProp];
  var max2 = clientSize - arrowRect[len] - paddingObject[maxProp];
  var center = clientSize / 2 - arrowRect[len] / 2 + centerToReference;
  var offset2 = within(min2, center, max2);
  var axisProp = axis;
  state.modifiersData[name] = (_state$modifiersData$ = {}, _state$modifiersData$[axisProp] = offset2, _state$modifiersData$.centerOffset = offset2 - center, _state$modifiersData$);
}
function effect$1(_ref2) {
  var state = _ref2.state, options = _ref2.options;
  var _options$element = options.element, arrowElement = _options$element === void 0 ? "[data-popper-arrow]" : _options$element;
  if (arrowElement == null) {
    return;
  }
  if (typeof arrowElement === "string") {
    arrowElement = state.elements.popper.querySelector(arrowElement);
    if (!arrowElement) {
      return;
    }
  }
  if (!contains(state.elements.popper, arrowElement)) {
    return;
  }
  state.elements.arrow = arrowElement;
}
var arrow$1 = {
  name: "arrow",
  enabled: true,
  phase: "main",
  fn: arrow,
  effect: effect$1,
  requires: ["popperOffsets"],
  requiresIfExists: ["preventOverflow"]
};
function getVariation(placement) {
  return placement.split("-")[1];
}
var unsetSides = {
  top: "auto",
  right: "auto",
  bottom: "auto",
  left: "auto"
};
function roundOffsetsByDPR(_ref) {
  var x2 = _ref.x, y2 = _ref.y;
  var win = window;
  var dpr = win.devicePixelRatio || 1;
  return {
    x: round(x2 * dpr) / dpr || 0,
    y: round(y2 * dpr) / dpr || 0
  };
}
function mapToStyles(_ref2) {
  var _Object$assign2;
  var popper2 = _ref2.popper, popperRect = _ref2.popperRect, placement = _ref2.placement, variation = _ref2.variation, offsets = _ref2.offsets, position = _ref2.position, gpuAcceleration = _ref2.gpuAcceleration, adaptive = _ref2.adaptive, roundOffsets = _ref2.roundOffsets, isFixed = _ref2.isFixed;
  var _offsets$x = offsets.x, x2 = _offsets$x === void 0 ? 0 : _offsets$x, _offsets$y = offsets.y, y2 = _offsets$y === void 0 ? 0 : _offsets$y;
  var _ref3 = typeof roundOffsets === "function" ? roundOffsets({
    x: x2,
    y: y2
  }) : {
    x: x2,
    y: y2
  };
  x2 = _ref3.x;
  y2 = _ref3.y;
  var hasX = offsets.hasOwnProperty("x");
  var hasY = offsets.hasOwnProperty("y");
  var sideX = left;
  var sideY = top;
  var win = window;
  if (adaptive) {
    var offsetParent = getOffsetParent(popper2);
    var heightProp = "clientHeight";
    var widthProp = "clientWidth";
    if (offsetParent === getWindow(popper2)) {
      offsetParent = getDocumentElement(popper2);
      if (getComputedStyle(offsetParent).position !== "static" && position === "absolute") {
        heightProp = "scrollHeight";
        widthProp = "scrollWidth";
      }
    }
    offsetParent = offsetParent;
    if (placement === top || (placement === left || placement === right) && variation === end) {
      sideY = bottom;
      var offsetY = isFixed && offsetParent === win && win.visualViewport ? win.visualViewport.height : offsetParent[heightProp];
      y2 -= offsetY - popperRect.height;
      y2 *= gpuAcceleration ? 1 : -1;
    }
    if (placement === left || (placement === top || placement === bottom) && variation === end) {
      sideX = right;
      var offsetX = isFixed && offsetParent === win && win.visualViewport ? win.visualViewport.width : offsetParent[widthProp];
      x2 -= offsetX - popperRect.width;
      x2 *= gpuAcceleration ? 1 : -1;
    }
  }
  var commonStyles = Object.assign({
    position
  }, adaptive && unsetSides);
  var _ref4 = roundOffsets === true ? roundOffsetsByDPR({
    x: x2,
    y: y2
  }) : {
    x: x2,
    y: y2
  };
  x2 = _ref4.x;
  y2 = _ref4.y;
  if (gpuAcceleration) {
    var _Object$assign;
    return Object.assign({}, commonStyles, (_Object$assign = {}, _Object$assign[sideY] = hasY ? "0" : "", _Object$assign[sideX] = hasX ? "0" : "", _Object$assign.transform = (win.devicePixelRatio || 1) <= 1 ? "translate(" + x2 + "px, " + y2 + "px)" : "translate3d(" + x2 + "px, " + y2 + "px, 0)", _Object$assign));
  }
  return Object.assign({}, commonStyles, (_Object$assign2 = {}, _Object$assign2[sideY] = hasY ? y2 + "px" : "", _Object$assign2[sideX] = hasX ? x2 + "px" : "", _Object$assign2.transform = "", _Object$assign2));
}
function computeStyles(_ref5) {
  var state = _ref5.state, options = _ref5.options;
  var _options$gpuAccelerat = options.gpuAcceleration, gpuAcceleration = _options$gpuAccelerat === void 0 ? true : _options$gpuAccelerat, _options$adaptive = options.adaptive, adaptive = _options$adaptive === void 0 ? true : _options$adaptive, _options$roundOffsets = options.roundOffsets, roundOffsets = _options$roundOffsets === void 0 ? true : _options$roundOffsets;
  var commonStyles = {
    placement: getBasePlacement(state.placement),
    variation: getVariation(state.placement),
    popper: state.elements.popper,
    popperRect: state.rects.popper,
    gpuAcceleration,
    isFixed: state.options.strategy === "fixed"
  };
  if (state.modifiersData.popperOffsets != null) {
    state.styles.popper = Object.assign({}, state.styles.popper, mapToStyles(Object.assign({}, commonStyles, {
      offsets: state.modifiersData.popperOffsets,
      position: state.options.strategy,
      adaptive,
      roundOffsets
    })));
  }
  if (state.modifiersData.arrow != null) {
    state.styles.arrow = Object.assign({}, state.styles.arrow, mapToStyles(Object.assign({}, commonStyles, {
      offsets: state.modifiersData.arrow,
      position: "absolute",
      adaptive: false,
      roundOffsets
    })));
  }
  state.attributes.popper = Object.assign({}, state.attributes.popper, {
    "data-popper-placement": state.placement
  });
}
var computeStyles$1 = {
  name: "computeStyles",
  enabled: true,
  phase: "beforeWrite",
  fn: computeStyles,
  data: {}
};
var passive = {
  passive: true
};
function effect(_ref) {
  var state = _ref.state, instance = _ref.instance, options = _ref.options;
  var _options$scroll = options.scroll, scroll = _options$scroll === void 0 ? true : _options$scroll, _options$resize = options.resize, resize = _options$resize === void 0 ? true : _options$resize;
  var window2 = getWindow(state.elements.popper);
  var scrollParents = [].concat(state.scrollParents.reference, state.scrollParents.popper);
  if (scroll) {
    scrollParents.forEach(function(scrollParent) {
      scrollParent.addEventListener("scroll", instance.update, passive);
    });
  }
  if (resize) {
    window2.addEventListener("resize", instance.update, passive);
  }
  return function() {
    if (scroll) {
      scrollParents.forEach(function(scrollParent) {
        scrollParent.removeEventListener("scroll", instance.update, passive);
      });
    }
    if (resize) {
      window2.removeEventListener("resize", instance.update, passive);
    }
  };
}
var eventListeners = {
  name: "eventListeners",
  enabled: true,
  phase: "write",
  fn: function fn2() {
  },
  effect,
  data: {}
};
var hash$1 = {
  left: "right",
  right: "left",
  bottom: "top",
  top: "bottom"
};
function getOppositePlacement(placement) {
  return placement.replace(/left|right|bottom|top/g, function(matched) {
    return hash$1[matched];
  });
}
var hash = {
  start: "end",
  end: "start"
};
function getOppositeVariationPlacement(placement) {
  return placement.replace(/start|end/g, function(matched) {
    return hash[matched];
  });
}
function getWindowScroll(node) {
  var win = getWindow(node);
  var scrollLeft = win.pageXOffset;
  var scrollTop = win.pageYOffset;
  return {
    scrollLeft,
    scrollTop
  };
}
function getWindowScrollBarX(element) {
  return getBoundingClientRect(getDocumentElement(element)).left + getWindowScroll(element).scrollLeft;
}
function getViewportRect(element, strategy) {
  var win = getWindow(element);
  var html = getDocumentElement(element);
  var visualViewport = win.visualViewport;
  var width = html.clientWidth;
  var height = html.clientHeight;
  var x2 = 0;
  var y2 = 0;
  if (visualViewport) {
    width = visualViewport.width;
    height = visualViewport.height;
    var layoutViewport = isLayoutViewport();
    if (layoutViewport || !layoutViewport && strategy === "fixed") {
      x2 = visualViewport.offsetLeft;
      y2 = visualViewport.offsetTop;
    }
  }
  return {
    width,
    height,
    x: x2 + getWindowScrollBarX(element),
    y: y2
  };
}
function getDocumentRect(element) {
  var _element$ownerDocumen;
  var html = getDocumentElement(element);
  var winScroll = getWindowScroll(element);
  var body = (_element$ownerDocumen = element.ownerDocument) == null ? void 0 : _element$ownerDocumen.body;
  var width = max(html.scrollWidth, html.clientWidth, body ? body.scrollWidth : 0, body ? body.clientWidth : 0);
  var height = max(html.scrollHeight, html.clientHeight, body ? body.scrollHeight : 0, body ? body.clientHeight : 0);
  var x2 = -winScroll.scrollLeft + getWindowScrollBarX(element);
  var y2 = -winScroll.scrollTop;
  if (getComputedStyle(body || html).direction === "rtl") {
    x2 += max(html.clientWidth, body ? body.clientWidth : 0) - width;
  }
  return {
    width,
    height,
    x: x2,
    y: y2
  };
}
function isScrollParent(element) {
  var _getComputedStyle = getComputedStyle(element), overflow = _getComputedStyle.overflow, overflowX = _getComputedStyle.overflowX, overflowY = _getComputedStyle.overflowY;
  return /auto|scroll|overlay|hidden/.test(overflow + overflowY + overflowX);
}
function getScrollParent(node) {
  if (["html", "body", "#document"].indexOf(getNodeName(node)) >= 0) {
    return node.ownerDocument.body;
  }
  if (isHTMLElement(node) && isScrollParent(node)) {
    return node;
  }
  return getScrollParent(getParentNode(node));
}
function listScrollParents(element, list) {
  var _element$ownerDocumen;
  if (list === void 0) {
    list = [];
  }
  var scrollParent = getScrollParent(element);
  var isBody = scrollParent === ((_element$ownerDocumen = element.ownerDocument) == null ? void 0 : _element$ownerDocumen.body);
  var win = getWindow(scrollParent);
  var target = isBody ? [win].concat(win.visualViewport || [], isScrollParent(scrollParent) ? scrollParent : []) : scrollParent;
  var updatedList = list.concat(target);
  return isBody ? updatedList : updatedList.concat(listScrollParents(getParentNode(target)));
}
function rectToClientRect(rect) {
  return Object.assign({}, rect, {
    left: rect.x,
    top: rect.y,
    right: rect.x + rect.width,
    bottom: rect.y + rect.height
  });
}
function getInnerBoundingClientRect(element, strategy) {
  var rect = getBoundingClientRect(element, false, strategy === "fixed");
  rect.top = rect.top + element.clientTop;
  rect.left = rect.left + element.clientLeft;
  rect.bottom = rect.top + element.clientHeight;
  rect.right = rect.left + element.clientWidth;
  rect.width = element.clientWidth;
  rect.height = element.clientHeight;
  rect.x = rect.left;
  rect.y = rect.top;
  return rect;
}
function getClientRectFromMixedType(element, clippingParent, strategy) {
  return clippingParent === viewport ? rectToClientRect(getViewportRect(element, strategy)) : isElement(clippingParent) ? getInnerBoundingClientRect(clippingParent, strategy) : rectToClientRect(getDocumentRect(getDocumentElement(element)));
}
function getClippingParents(element) {
  var clippingParents2 = listScrollParents(getParentNode(element));
  var canEscapeClipping = ["absolute", "fixed"].indexOf(getComputedStyle(element).position) >= 0;
  var clipperElement = canEscapeClipping && isHTMLElement(element) ? getOffsetParent(element) : element;
  if (!isElement(clipperElement)) {
    return [];
  }
  return clippingParents2.filter(function(clippingParent) {
    return isElement(clippingParent) && contains(clippingParent, clipperElement) && getNodeName(clippingParent) !== "body";
  });
}
function getClippingRect(element, boundary, rootBoundary, strategy) {
  var mainClippingParents = boundary === "clippingParents" ? getClippingParents(element) : [].concat(boundary);
  var clippingParents2 = [].concat(mainClippingParents, [rootBoundary]);
  var firstClippingParent = clippingParents2[0];
  var clippingRect = clippingParents2.reduce(function(accRect, clippingParent) {
    var rect = getClientRectFromMixedType(element, clippingParent, strategy);
    accRect.top = max(rect.top, accRect.top);
    accRect.right = min(rect.right, accRect.right);
    accRect.bottom = min(rect.bottom, accRect.bottom);
    accRect.left = max(rect.left, accRect.left);
    return accRect;
  }, getClientRectFromMixedType(element, firstClippingParent, strategy));
  clippingRect.width = clippingRect.right - clippingRect.left;
  clippingRect.height = clippingRect.bottom - clippingRect.top;
  clippingRect.x = clippingRect.left;
  clippingRect.y = clippingRect.top;
  return clippingRect;
}
function computeOffsets(_ref) {
  var reference2 = _ref.reference, element = _ref.element, placement = _ref.placement;
  var basePlacement = placement ? getBasePlacement(placement) : null;
  var variation = placement ? getVariation(placement) : null;
  var commonX = reference2.x + reference2.width / 2 - element.width / 2;
  var commonY = reference2.y + reference2.height / 2 - element.height / 2;
  var offsets;
  switch (basePlacement) {
    case top:
      offsets = {
        x: commonX,
        y: reference2.y - element.height
      };
      break;
    case bottom:
      offsets = {
        x: commonX,
        y: reference2.y + reference2.height
      };
      break;
    case right:
      offsets = {
        x: reference2.x + reference2.width,
        y: commonY
      };
      break;
    case left:
      offsets = {
        x: reference2.x - element.width,
        y: commonY
      };
      break;
    default:
      offsets = {
        x: reference2.x,
        y: reference2.y
      };
  }
  var mainAxis = basePlacement ? getMainAxisFromPlacement(basePlacement) : null;
  if (mainAxis != null) {
    var len = mainAxis === "y" ? "height" : "width";
    switch (variation) {
      case start:
        offsets[mainAxis] = offsets[mainAxis] - (reference2[len] / 2 - element[len] / 2);
        break;
      case end:
        offsets[mainAxis] = offsets[mainAxis] + (reference2[len] / 2 - element[len] / 2);
        break;
    }
  }
  return offsets;
}
function detectOverflow(state, options) {
  if (options === void 0) {
    options = {};
  }
  var _options = options, _options$placement = _options.placement, placement = _options$placement === void 0 ? state.placement : _options$placement, _options$strategy = _options.strategy, strategy = _options$strategy === void 0 ? state.strategy : _options$strategy, _options$boundary = _options.boundary, boundary = _options$boundary === void 0 ? clippingParents : _options$boundary, _options$rootBoundary = _options.rootBoundary, rootBoundary = _options$rootBoundary === void 0 ? viewport : _options$rootBoundary, _options$elementConte = _options.elementContext, elementContext = _options$elementConte === void 0 ? popper : _options$elementConte, _options$altBoundary = _options.altBoundary, altBoundary = _options$altBoundary === void 0 ? false : _options$altBoundary, _options$padding = _options.padding, padding = _options$padding === void 0 ? 0 : _options$padding;
  var paddingObject = mergePaddingObject(typeof padding !== "number" ? padding : expandToHashMap(padding, basePlacements));
  var altContext = elementContext === popper ? reference : popper;
  var popperRect = state.rects.popper;
  var element = state.elements[altBoundary ? altContext : elementContext];
  var clippingClientRect = getClippingRect(isElement(element) ? element : element.contextElement || getDocumentElement(state.elements.popper), boundary, rootBoundary, strategy);
  var referenceClientRect = getBoundingClientRect(state.elements.reference);
  var popperOffsets2 = computeOffsets({
    reference: referenceClientRect,
    element: popperRect,
    strategy: "absolute",
    placement
  });
  var popperClientRect = rectToClientRect(Object.assign({}, popperRect, popperOffsets2));
  var elementClientRect = elementContext === popper ? popperClientRect : referenceClientRect;
  var overflowOffsets = {
    top: clippingClientRect.top - elementClientRect.top + paddingObject.top,
    bottom: elementClientRect.bottom - clippingClientRect.bottom + paddingObject.bottom,
    left: clippingClientRect.left - elementClientRect.left + paddingObject.left,
    right: elementClientRect.right - clippingClientRect.right + paddingObject.right
  };
  var offsetData = state.modifiersData.offset;
  if (elementContext === popper && offsetData) {
    var offset2 = offsetData[placement];
    Object.keys(overflowOffsets).forEach(function(key) {
      var multiply = [right, bottom].indexOf(key) >= 0 ? 1 : -1;
      var axis = [top, bottom].indexOf(key) >= 0 ? "y" : "x";
      overflowOffsets[key] += offset2[axis] * multiply;
    });
  }
  return overflowOffsets;
}
function computeAutoPlacement(state, options) {
  if (options === void 0) {
    options = {};
  }
  var _options = options, placement = _options.placement, boundary = _options.boundary, rootBoundary = _options.rootBoundary, padding = _options.padding, flipVariations = _options.flipVariations, _options$allowedAutoP = _options.allowedAutoPlacements, allowedAutoPlacements = _options$allowedAutoP === void 0 ? placements : _options$allowedAutoP;
  var variation = getVariation(placement);
  var placements$1 = variation ? flipVariations ? variationPlacements : variationPlacements.filter(function(placement2) {
    return getVariation(placement2) === variation;
  }) : basePlacements;
  var allowedPlacements = placements$1.filter(function(placement2) {
    return allowedAutoPlacements.indexOf(placement2) >= 0;
  });
  if (allowedPlacements.length === 0) {
    allowedPlacements = placements$1;
  }
  var overflows = allowedPlacements.reduce(function(acc, placement2) {
    acc[placement2] = detectOverflow(state, {
      placement: placement2,
      boundary,
      rootBoundary,
      padding
    })[getBasePlacement(placement2)];
    return acc;
  }, {});
  return Object.keys(overflows).sort(function(a2, b2) {
    return overflows[a2] - overflows[b2];
  });
}
function getExpandedFallbackPlacements(placement) {
  if (getBasePlacement(placement) === auto) {
    return [];
  }
  var oppositePlacement = getOppositePlacement(placement);
  return [getOppositeVariationPlacement(placement), oppositePlacement, getOppositeVariationPlacement(oppositePlacement)];
}
function flip(_ref) {
  var state = _ref.state, options = _ref.options, name = _ref.name;
  if (state.modifiersData[name]._skip) {
    return;
  }
  var _options$mainAxis = options.mainAxis, checkMainAxis = _options$mainAxis === void 0 ? true : _options$mainAxis, _options$altAxis = options.altAxis, checkAltAxis = _options$altAxis === void 0 ? true : _options$altAxis, specifiedFallbackPlacements = options.fallbackPlacements, padding = options.padding, boundary = options.boundary, rootBoundary = options.rootBoundary, altBoundary = options.altBoundary, _options$flipVariatio = options.flipVariations, flipVariations = _options$flipVariatio === void 0 ? true : _options$flipVariatio, allowedAutoPlacements = options.allowedAutoPlacements;
  var preferredPlacement = state.options.placement;
  var basePlacement = getBasePlacement(preferredPlacement);
  var isBasePlacement = basePlacement === preferredPlacement;
  var fallbackPlacements = specifiedFallbackPlacements || (isBasePlacement || !flipVariations ? [getOppositePlacement(preferredPlacement)] : getExpandedFallbackPlacements(preferredPlacement));
  var placements2 = [preferredPlacement].concat(fallbackPlacements).reduce(function(acc, placement2) {
    return acc.concat(getBasePlacement(placement2) === auto ? computeAutoPlacement(state, {
      placement: placement2,
      boundary,
      rootBoundary,
      padding,
      flipVariations,
      allowedAutoPlacements
    }) : placement2);
  }, []);
  var referenceRect = state.rects.reference;
  var popperRect = state.rects.popper;
  var checksMap = /* @__PURE__ */ new Map();
  var makeFallbackChecks = true;
  var firstFittingPlacement = placements2[0];
  for (var i2 = 0; i2 < placements2.length; i2++) {
    var placement = placements2[i2];
    var _basePlacement = getBasePlacement(placement);
    var isStartVariation = getVariation(placement) === start;
    var isVertical = [top, bottom].indexOf(_basePlacement) >= 0;
    var len = isVertical ? "width" : "height";
    var overflow = detectOverflow(state, {
      placement,
      boundary,
      rootBoundary,
      altBoundary,
      padding
    });
    var mainVariationSide = isVertical ? isStartVariation ? right : left : isStartVariation ? bottom : top;
    if (referenceRect[len] > popperRect[len]) {
      mainVariationSide = getOppositePlacement(mainVariationSide);
    }
    var altVariationSide = getOppositePlacement(mainVariationSide);
    var checks = [];
    if (checkMainAxis) {
      checks.push(overflow[_basePlacement] <= 0);
    }
    if (checkAltAxis) {
      checks.push(overflow[mainVariationSide] <= 0, overflow[altVariationSide] <= 0);
    }
    if (checks.every(function(check) {
      return check;
    })) {
      firstFittingPlacement = placement;
      makeFallbackChecks = false;
      break;
    }
    checksMap.set(placement, checks);
  }
  if (makeFallbackChecks) {
    var numberOfChecks = flipVariations ? 3 : 1;
    var _loop = function _loop2(_i2) {
      var fittingPlacement = placements2.find(function(placement2) {
        var checks2 = checksMap.get(placement2);
        if (checks2) {
          return checks2.slice(0, _i2).every(function(check) {
            return check;
          });
        }
      });
      if (fittingPlacement) {
        firstFittingPlacement = fittingPlacement;
        return "break";
      }
    };
    for (var _i = numberOfChecks; _i > 0; _i--) {
      var _ret = _loop(_i);
      if (_ret === "break")
        break;
    }
  }
  if (state.placement !== firstFittingPlacement) {
    state.modifiersData[name]._skip = true;
    state.placement = firstFittingPlacement;
    state.reset = true;
  }
}
var flip$1 = {
  name: "flip",
  enabled: true,
  phase: "main",
  fn: flip,
  requiresIfExists: ["offset"],
  data: {
    _skip: false
  }
};
function getSideOffsets(overflow, rect, preventedOffsets) {
  if (preventedOffsets === void 0) {
    preventedOffsets = {
      x: 0,
      y: 0
    };
  }
  return {
    top: overflow.top - rect.height - preventedOffsets.y,
    right: overflow.right - rect.width + preventedOffsets.x,
    bottom: overflow.bottom - rect.height + preventedOffsets.y,
    left: overflow.left - rect.width - preventedOffsets.x
  };
}
function isAnySideFullyClipped(overflow) {
  return [top, right, bottom, left].some(function(side) {
    return overflow[side] >= 0;
  });
}
function hide(_ref) {
  var state = _ref.state, name = _ref.name;
  var referenceRect = state.rects.reference;
  var popperRect = state.rects.popper;
  var preventedOffsets = state.modifiersData.preventOverflow;
  var referenceOverflow = detectOverflow(state, {
    elementContext: "reference"
  });
  var popperAltOverflow = detectOverflow(state, {
    altBoundary: true
  });
  var referenceClippingOffsets = getSideOffsets(referenceOverflow, referenceRect);
  var popperEscapeOffsets = getSideOffsets(popperAltOverflow, popperRect, preventedOffsets);
  var isReferenceHidden = isAnySideFullyClipped(referenceClippingOffsets);
  var hasPopperEscaped = isAnySideFullyClipped(popperEscapeOffsets);
  state.modifiersData[name] = {
    referenceClippingOffsets,
    popperEscapeOffsets,
    isReferenceHidden,
    hasPopperEscaped
  };
  state.attributes.popper = Object.assign({}, state.attributes.popper, {
    "data-popper-reference-hidden": isReferenceHidden,
    "data-popper-escaped": hasPopperEscaped
  });
}
var hide$1 = {
  name: "hide",
  enabled: true,
  phase: "main",
  requiresIfExists: ["preventOverflow"],
  fn: hide
};
function distanceAndSkiddingToXY(placement, rects, offset2) {
  var basePlacement = getBasePlacement(placement);
  var invertDistance = [left, top].indexOf(basePlacement) >= 0 ? -1 : 1;
  var _ref = typeof offset2 === "function" ? offset2(Object.assign({}, rects, {
    placement
  })) : offset2, skidding = _ref[0], distance = _ref[1];
  skidding = skidding || 0;
  distance = (distance || 0) * invertDistance;
  return [left, right].indexOf(basePlacement) >= 0 ? {
    x: distance,
    y: skidding
  } : {
    x: skidding,
    y: distance
  };
}
function offset(_ref2) {
  var state = _ref2.state, options = _ref2.options, name = _ref2.name;
  var _options$offset = options.offset, offset2 = _options$offset === void 0 ? [0, 0] : _options$offset;
  var data = placements.reduce(function(acc, placement) {
    acc[placement] = distanceAndSkiddingToXY(placement, state.rects, offset2);
    return acc;
  }, {});
  var _data$state$placement = data[state.placement], x2 = _data$state$placement.x, y2 = _data$state$placement.y;
  if (state.modifiersData.popperOffsets != null) {
    state.modifiersData.popperOffsets.x += x2;
    state.modifiersData.popperOffsets.y += y2;
  }
  state.modifiersData[name] = data;
}
var offset$1 = {
  name: "offset",
  enabled: true,
  phase: "main",
  requires: ["popperOffsets"],
  fn: offset
};
function popperOffsets(_ref) {
  var state = _ref.state, name = _ref.name;
  state.modifiersData[name] = computeOffsets({
    reference: state.rects.reference,
    element: state.rects.popper,
    strategy: "absolute",
    placement: state.placement
  });
}
var popperOffsets$1 = {
  name: "popperOffsets",
  enabled: true,
  phase: "read",
  fn: popperOffsets,
  data: {}
};
function getAltAxis(axis) {
  return axis === "x" ? "y" : "x";
}
function preventOverflow(_ref) {
  var state = _ref.state, options = _ref.options, name = _ref.name;
  var _options$mainAxis = options.mainAxis, checkMainAxis = _options$mainAxis === void 0 ? true : _options$mainAxis, _options$altAxis = options.altAxis, checkAltAxis = _options$altAxis === void 0 ? false : _options$altAxis, boundary = options.boundary, rootBoundary = options.rootBoundary, altBoundary = options.altBoundary, padding = options.padding, _options$tether = options.tether, tether = _options$tether === void 0 ? true : _options$tether, _options$tetherOffset = options.tetherOffset, tetherOffset = _options$tetherOffset === void 0 ? 0 : _options$tetherOffset;
  var overflow = detectOverflow(state, {
    boundary,
    rootBoundary,
    padding,
    altBoundary
  });
  var basePlacement = getBasePlacement(state.placement);
  var variation = getVariation(state.placement);
  var isBasePlacement = !variation;
  var mainAxis = getMainAxisFromPlacement(basePlacement);
  var altAxis = getAltAxis(mainAxis);
  var popperOffsets2 = state.modifiersData.popperOffsets;
  var referenceRect = state.rects.reference;
  var popperRect = state.rects.popper;
  var tetherOffsetValue = typeof tetherOffset === "function" ? tetherOffset(Object.assign({}, state.rects, {
    placement: state.placement
  })) : tetherOffset;
  var normalizedTetherOffsetValue = typeof tetherOffsetValue === "number" ? {
    mainAxis: tetherOffsetValue,
    altAxis: tetherOffsetValue
  } : Object.assign({
    mainAxis: 0,
    altAxis: 0
  }, tetherOffsetValue);
  var offsetModifierState = state.modifiersData.offset ? state.modifiersData.offset[state.placement] : null;
  var data = {
    x: 0,
    y: 0
  };
  if (!popperOffsets2) {
    return;
  }
  if (checkMainAxis) {
    var _offsetModifierState$;
    var mainSide = mainAxis === "y" ? top : left;
    var altSide = mainAxis === "y" ? bottom : right;
    var len = mainAxis === "y" ? "height" : "width";
    var offset2 = popperOffsets2[mainAxis];
    var min$1 = offset2 + overflow[mainSide];
    var max$1 = offset2 - overflow[altSide];
    var additive = tether ? -popperRect[len] / 2 : 0;
    var minLen = variation === start ? referenceRect[len] : popperRect[len];
    var maxLen = variation === start ? -popperRect[len] : -referenceRect[len];
    var arrowElement = state.elements.arrow;
    var arrowRect = tether && arrowElement ? getLayoutRect(arrowElement) : {
      width: 0,
      height: 0
    };
    var arrowPaddingObject = state.modifiersData["arrow#persistent"] ? state.modifiersData["arrow#persistent"].padding : getFreshSideObject();
    var arrowPaddingMin = arrowPaddingObject[mainSide];
    var arrowPaddingMax = arrowPaddingObject[altSide];
    var arrowLen = within(0, referenceRect[len], arrowRect[len]);
    var minOffset = isBasePlacement ? referenceRect[len] / 2 - additive - arrowLen - arrowPaddingMin - normalizedTetherOffsetValue.mainAxis : minLen - arrowLen - arrowPaddingMin - normalizedTetherOffsetValue.mainAxis;
    var maxOffset = isBasePlacement ? -referenceRect[len] / 2 + additive + arrowLen + arrowPaddingMax + normalizedTetherOffsetValue.mainAxis : maxLen + arrowLen + arrowPaddingMax + normalizedTetherOffsetValue.mainAxis;
    var arrowOffsetParent = state.elements.arrow && getOffsetParent(state.elements.arrow);
    var clientOffset = arrowOffsetParent ? mainAxis === "y" ? arrowOffsetParent.clientTop || 0 : arrowOffsetParent.clientLeft || 0 : 0;
    var offsetModifierValue = (_offsetModifierState$ = offsetModifierState == null ? void 0 : offsetModifierState[mainAxis]) != null ? _offsetModifierState$ : 0;
    var tetherMin = offset2 + minOffset - offsetModifierValue - clientOffset;
    var tetherMax = offset2 + maxOffset - offsetModifierValue;
    var preventedOffset = within(tether ? min(min$1, tetherMin) : min$1, offset2, tether ? max(max$1, tetherMax) : max$1);
    popperOffsets2[mainAxis] = preventedOffset;
    data[mainAxis] = preventedOffset - offset2;
  }
  if (checkAltAxis) {
    var _offsetModifierState$2;
    var _mainSide = mainAxis === "x" ? top : left;
    var _altSide = mainAxis === "x" ? bottom : right;
    var _offset = popperOffsets2[altAxis];
    var _len = altAxis === "y" ? "height" : "width";
    var _min = _offset + overflow[_mainSide];
    var _max = _offset - overflow[_altSide];
    var isOriginSide = [top, left].indexOf(basePlacement) !== -1;
    var _offsetModifierValue = (_offsetModifierState$2 = offsetModifierState == null ? void 0 : offsetModifierState[altAxis]) != null ? _offsetModifierState$2 : 0;
    var _tetherMin = isOriginSide ? _min : _offset - referenceRect[_len] - popperRect[_len] - _offsetModifierValue + normalizedTetherOffsetValue.altAxis;
    var _tetherMax = isOriginSide ? _offset + referenceRect[_len] + popperRect[_len] - _offsetModifierValue - normalizedTetherOffsetValue.altAxis : _max;
    var _preventedOffset = tether && isOriginSide ? withinMaxClamp(_tetherMin, _offset, _tetherMax) : within(tether ? _tetherMin : _min, _offset, tether ? _tetherMax : _max);
    popperOffsets2[altAxis] = _preventedOffset;
    data[altAxis] = _preventedOffset - _offset;
  }
  state.modifiersData[name] = data;
}
var preventOverflow$1 = {
  name: "preventOverflow",
  enabled: true,
  phase: "main",
  fn: preventOverflow,
  requiresIfExists: ["offset"]
};
function getHTMLElementScroll(element) {
  return {
    scrollLeft: element.scrollLeft,
    scrollTop: element.scrollTop
  };
}
function getNodeScroll(node) {
  if (node === getWindow(node) || !isHTMLElement(node)) {
    return getWindowScroll(node);
  } else {
    return getHTMLElementScroll(node);
  }
}
function isElementScaled(element) {
  var rect = element.getBoundingClientRect();
  var scaleX = round(rect.width) / element.offsetWidth || 1;
  var scaleY = round(rect.height) / element.offsetHeight || 1;
  return scaleX !== 1 || scaleY !== 1;
}
function getCompositeRect(elementOrVirtualElement, offsetParent, isFixed) {
  if (isFixed === void 0) {
    isFixed = false;
  }
  var isOffsetParentAnElement = isHTMLElement(offsetParent);
  var offsetParentIsScaled = isHTMLElement(offsetParent) && isElementScaled(offsetParent);
  var documentElement = getDocumentElement(offsetParent);
  var rect = getBoundingClientRect(elementOrVirtualElement, offsetParentIsScaled, isFixed);
  var scroll = {
    scrollLeft: 0,
    scrollTop: 0
  };
  var offsets = {
    x: 0,
    y: 0
  };
  if (isOffsetParentAnElement || !isOffsetParentAnElement && !isFixed) {
    if (getNodeName(offsetParent) !== "body" || isScrollParent(documentElement)) {
      scroll = getNodeScroll(offsetParent);
    }
    if (isHTMLElement(offsetParent)) {
      offsets = getBoundingClientRect(offsetParent, true);
      offsets.x += offsetParent.clientLeft;
      offsets.y += offsetParent.clientTop;
    } else if (documentElement) {
      offsets.x = getWindowScrollBarX(documentElement);
    }
  }
  return {
    x: rect.left + scroll.scrollLeft - offsets.x,
    y: rect.top + scroll.scrollTop - offsets.y,
    width: rect.width,
    height: rect.height
  };
}
function order(modifiers) {
  var map = /* @__PURE__ */ new Map();
  var visited = /* @__PURE__ */ new Set();
  var result = [];
  modifiers.forEach(function(modifier) {
    map.set(modifier.name, modifier);
  });
  function sort(modifier) {
    visited.add(modifier.name);
    var requires = [].concat(modifier.requires || [], modifier.requiresIfExists || []);
    requires.forEach(function(dep) {
      if (!visited.has(dep)) {
        var depModifier = map.get(dep);
        if (depModifier) {
          sort(depModifier);
        }
      }
    });
    result.push(modifier);
  }
  modifiers.forEach(function(modifier) {
    if (!visited.has(modifier.name)) {
      sort(modifier);
    }
  });
  return result;
}
function orderModifiers(modifiers) {
  var orderedModifiers = order(modifiers);
  return modifierPhases.reduce(function(acc, phase) {
    return acc.concat(orderedModifiers.filter(function(modifier) {
      return modifier.phase === phase;
    }));
  }, []);
}
function debounce(fn3) {
  var pending;
  return function() {
    if (!pending) {
      pending = new Promise(function(resolve) {
        Promise.resolve().then(function() {
          pending = void 0;
          resolve(fn3());
        });
      });
    }
    return pending;
  };
}
function mergeByName(modifiers) {
  var merged = modifiers.reduce(function(merged2, current) {
    var existing = merged2[current.name];
    merged2[current.name] = existing ? Object.assign({}, existing, current, {
      options: Object.assign({}, existing.options, current.options),
      data: Object.assign({}, existing.data, current.data)
    }) : current;
    return merged2;
  }, {});
  return Object.keys(merged).map(function(key) {
    return merged[key];
  });
}
var DEFAULT_OPTIONS = {
  placement: "bottom",
  modifiers: [],
  strategy: "absolute"
};
function areValidElements() {
  for (var _len = arguments.length, args = new Array(_len), _key = 0; _key < _len; _key++) {
    args[_key] = arguments[_key];
  }
  return !args.some(function(element) {
    return !(element && typeof element.getBoundingClientRect === "function");
  });
}
function popperGenerator(generatorOptions) {
  if (generatorOptions === void 0) {
    generatorOptions = {};
  }
  var _generatorOptions = generatorOptions, _generatorOptions$def = _generatorOptions.defaultModifiers, defaultModifiers2 = _generatorOptions$def === void 0 ? [] : _generatorOptions$def, _generatorOptions$def2 = _generatorOptions.defaultOptions, defaultOptions = _generatorOptions$def2 === void 0 ? DEFAULT_OPTIONS : _generatorOptions$def2;
  return function createPopper2(reference2, popper2, options) {
    if (options === void 0) {
      options = defaultOptions;
    }
    var state = {
      placement: "bottom",
      orderedModifiers: [],
      options: Object.assign({}, DEFAULT_OPTIONS, defaultOptions),
      modifiersData: {},
      elements: {
        reference: reference2,
        popper: popper2
      },
      attributes: {},
      styles: {}
    };
    var effectCleanupFns = [];
    var isDestroyed = false;
    var instance = {
      state,
      setOptions: function setOptions(setOptionsAction) {
        var options2 = typeof setOptionsAction === "function" ? setOptionsAction(state.options) : setOptionsAction;
        cleanupModifierEffects();
        state.options = Object.assign({}, defaultOptions, state.options, options2);
        state.scrollParents = {
          reference: isElement(reference2) ? listScrollParents(reference2) : reference2.contextElement ? listScrollParents(reference2.contextElement) : [],
          popper: listScrollParents(popper2)
        };
        var orderedModifiers = orderModifiers(mergeByName([].concat(defaultModifiers2, state.options.modifiers)));
        state.orderedModifiers = orderedModifiers.filter(function(m2) {
          return m2.enabled;
        });
        runModifierEffects();
        return instance.update();
      },
      forceUpdate: function forceUpdate() {
        if (isDestroyed) {
          return;
        }
        var _state$elements = state.elements, reference3 = _state$elements.reference, popper3 = _state$elements.popper;
        if (!areValidElements(reference3, popper3)) {
          return;
        }
        state.rects = {
          reference: getCompositeRect(reference3, getOffsetParent(popper3), state.options.strategy === "fixed"),
          popper: getLayoutRect(popper3)
        };
        state.reset = false;
        state.placement = state.options.placement;
        state.orderedModifiers.forEach(function(modifier) {
          return state.modifiersData[modifier.name] = Object.assign({}, modifier.data);
        });
        for (var index2 = 0; index2 < state.orderedModifiers.length; index2++) {
          if (state.reset === true) {
            state.reset = false;
            index2 = -1;
            continue;
          }
          var _state$orderedModifie = state.orderedModifiers[index2], fn3 = _state$orderedModifie.fn, _state$orderedModifie2 = _state$orderedModifie.options, _options = _state$orderedModifie2 === void 0 ? {} : _state$orderedModifie2, name = _state$orderedModifie.name;
          if (typeof fn3 === "function") {
            state = fn3({
              state,
              options: _options,
              name,
              instance
            }) || state;
          }
        }
      },
      update: debounce(function() {
        return new Promise(function(resolve) {
          instance.forceUpdate();
          resolve(state);
        });
      }),
      destroy: function destroy() {
        cleanupModifierEffects();
        isDestroyed = true;
      }
    };
    if (!areValidElements(reference2, popper2)) {
      return instance;
    }
    instance.setOptions(options).then(function(state2) {
      if (!isDestroyed && options.onFirstUpdate) {
        options.onFirstUpdate(state2);
      }
    });
    function runModifierEffects() {
      state.orderedModifiers.forEach(function(_ref3) {
        var name = _ref3.name, _ref3$options = _ref3.options, options2 = _ref3$options === void 0 ? {} : _ref3$options, effect2 = _ref3.effect;
        if (typeof effect2 === "function") {
          var cleanupFn = effect2({
            state,
            name,
            instance,
            options: options2
          });
          var noopFn = function noopFn2() {
          };
          effectCleanupFns.push(cleanupFn || noopFn);
        }
      });
    }
    function cleanupModifierEffects() {
      effectCleanupFns.forEach(function(fn3) {
        return fn3();
      });
      effectCleanupFns = [];
    }
    return instance;
  };
}
var defaultModifiers = [eventListeners, popperOffsets$1, computeStyles$1, applyStyles$1, offset$1, flip$1, preventOverflow$1, arrow$1, hide$1];
var createPopper = /* @__PURE__ */ popperGenerator({
  defaultModifiers
});
var GradientParser = GradientParser || {};
GradientParser.stringify = function() {
  var visitor = {
    "visit_linear-gradient": function(node) {
      return visitor.visit_gradient(node);
    },
    "visit_repeating-linear-gradient": function(node) {
      return visitor.visit_gradient(node);
    },
    "visit_radial-gradient": function(node) {
      return visitor.visit_gradient(node);
    },
    "visit_repeating-radial-gradient": function(node) {
      return visitor.visit_gradient(node);
    },
    "visit_gradient": function(node) {
      var orientation = visitor.visit(node.orientation);
      if (orientation) {
        orientation += ", ";
      }
      return node.type + "(" + orientation + visitor.visit(node.colorStops) + ")";
    },
    "visit_shape": function(node) {
      var result = node.value, at2 = visitor.visit(node.at), style2 = visitor.visit(node.style);
      if (style2) {
        result += " " + style2;
      }
      if (at2) {
        result += " at " + at2;
      }
      return result;
    },
    "visit_default-radial": function(node) {
      var result = "", at2 = visitor.visit(node.at);
      if (at2) {
        result += at2;
      }
      return result;
    },
    "visit_extent-keyword": function(node) {
      var result = node.value, at2 = visitor.visit(node.at);
      if (at2) {
        result += " at " + at2;
      }
      return result;
    },
    "visit_position-keyword": function(node) {
      return node.value;
    },
    "visit_position": function(node) {
      return visitor.visit(node.value.x) + " " + visitor.visit(node.value.y);
    },
    "visit_%": function(node) {
      return node.value + "%";
    },
    "visit_em": function(node) {
      return node.value + "em";
    },
    "visit_px": function(node) {
      return node.value + "px";
    },
    "visit_literal": function(node) {
      return visitor.visit_color(node.value, node);
    },
    "visit_hex": function(node) {
      return visitor.visit_color("#" + node.value, node);
    },
    "visit_rgb": function(node) {
      return visitor.visit_color("rgb(" + node.value.join(", ") + ")", node);
    },
    "visit_rgba": function(node) {
      return visitor.visit_color("rgba(" + node.value.join(", ") + ")", node);
    },
    "visit_color": function(resultColor, node) {
      var result = resultColor, length = visitor.visit(node.length);
      if (length) {
        result += " " + length;
      }
      return result;
    },
    "visit_angular": function(node) {
      return node.value + "deg";
    },
    "visit_directional": function(node) {
      return "to " + node.value;
    },
    "visit_array": function(elements) {
      var result = "", size = elements.length;
      elements.forEach(function(element, i2) {
        result += visitor.visit(element);
        if (i2 < size - 1) {
          result += ", ";
        }
      });
      return result;
    },
    "visit": function(element) {
      if (!element) {
        return "";
      }
      var result = "";
      if (element instanceof Array) {
        return visitor.visit_array(element, result);
      } else if (element.type) {
        var nodeVisitor = visitor["visit_" + element.type];
        if (nodeVisitor) {
          return nodeVisitor(element);
        } else {
          throw Error("Missing visitor visit_" + element.type);
        }
      } else {
        throw Error("Invalid node.");
      }
    }
  };
  return function(root2) {
    return visitor.visit(root2);
  };
}();
var GradientParser = GradientParser || {};
GradientParser.parse = function() {
  var tokens = {
    linearGradient: /^(\-(webkit|o|ms|moz)\-)?(linear\-gradient)/i,
    repeatingLinearGradient: /^(\-(webkit|o|ms|moz)\-)?(repeating\-linear\-gradient)/i,
    radialGradient: /^(\-(webkit|o|ms|moz)\-)?(radial\-gradient)/i,
    repeatingRadialGradient: /^(\-(webkit|o|ms|moz)\-)?(repeating\-radial\-gradient)/i,
    sideOrCorner: /^to (left (top|bottom)|right (top|bottom)|left|right|top|bottom)/i,
    extentKeywords: /^(closest\-side|closest\-corner|farthest\-side|farthest\-corner|contain|cover)/,
    positionKeywords: /^(left|center|right|top|bottom)/i,
    pixelValue: /^(-?(([0-9]*\.[0-9]+)|([0-9]+\.?)))px/,
    percentageValue: /^(-?(([0-9]*\.[0-9]+)|([0-9]+\.?)))\%/,
    emValue: /^(-?(([0-9]*\.[0-9]+)|([0-9]+\.?)))em/,
    angleValue: /^(-?(([0-9]*\.[0-9]+)|([0-9]+\.?)))deg/,
    startCall: /^\(/,
    endCall: /^\)/,
    comma: /^,/,
    hexColor: /^\#([0-9a-fA-F]+)/,
    literalColor: /^([a-zA-Z]+)/,
    rgbColor: /^rgb/i,
    rgbaColor: /^rgba/i,
    number: /^(([0-9]*\.[0-9]+)|([0-9]+\.?))/
  };
  var input = "";
  function error(msg) {
    var err = new Error(input + ": " + msg);
    err.source = input;
    throw err;
  }
  function getAST() {
    var ast = matchListDefinitions();
    if (input.length > 0) {
      error("Invalid input not EOF");
    }
    return ast;
  }
  function matchListDefinitions() {
    return matchListing(matchDefinition);
  }
  function matchDefinition() {
    return matchGradient(
      "linear-gradient",
      tokens.linearGradient,
      matchLinearOrientation
    ) || matchGradient(
      "repeating-linear-gradient",
      tokens.repeatingLinearGradient,
      matchLinearOrientation
    ) || matchGradient(
      "radial-gradient",
      tokens.radialGradient,
      matchListRadialOrientations
    ) || matchGradient(
      "repeating-radial-gradient",
      tokens.repeatingRadialGradient,
      matchListRadialOrientations
    );
  }
  function matchGradient(gradientType, pattern, orientationMatcher) {
    return matchCall(pattern, function(captures) {
      var orientation = orientationMatcher();
      if (orientation) {
        if (!scan(tokens.comma)) {
          error("Missing comma before color stops");
        }
      }
      return {
        type: gradientType,
        orientation,
        colorStops: matchListing(matchColorStop)
      };
    });
  }
  function matchCall(pattern, callback) {
    var captures = scan(pattern);
    if (captures) {
      if (!scan(tokens.startCall)) {
        error("Missing (");
      }
      var result = callback(captures);
      if (!scan(tokens.endCall)) {
        error("Missing )");
      }
      return result;
    }
  }
  function matchLinearOrientation() {
    return matchSideOrCorner() || matchAngle();
  }
  function matchSideOrCorner() {
    return match("directional", tokens.sideOrCorner, 1);
  }
  function matchAngle() {
    return match("angular", tokens.angleValue, 1);
  }
  function matchListRadialOrientations() {
    var radialOrientations, radialOrientation = matchRadialOrientation(), lookaheadCache;
    if (radialOrientation) {
      radialOrientations = [];
      radialOrientations.push(radialOrientation);
      lookaheadCache = input;
      if (scan(tokens.comma)) {
        radialOrientation = matchRadialOrientation();
        if (radialOrientation) {
          radialOrientations.push(radialOrientation);
        } else {
          input = lookaheadCache;
        }
      }
    }
    return radialOrientations;
  }
  function matchRadialOrientation() {
    var radialType = matchCircle() || matchEllipse();
    if (radialType) {
      radialType.at = matchAtPosition();
    } else {
      var extent = matchExtentKeyword();
      if (extent) {
        radialType = extent;
        var positionAt = matchAtPosition();
        if (positionAt) {
          radialType.at = positionAt;
        }
      } else {
        var defaultPosition = matchPositioning();
        if (defaultPosition) {
          radialType = {
            type: "default-radial",
            at: defaultPosition
          };
        }
      }
    }
    return radialType;
  }
  function matchCircle() {
    var circle = match("shape", /^(circle)/i, 0);
    if (circle) {
      circle.style = matchLength() || matchExtentKeyword();
    }
    return circle;
  }
  function matchEllipse() {
    var ellipse = match("shape", /^(ellipse)/i, 0);
    if (ellipse) {
      ellipse.style = matchDistance() || matchExtentKeyword();
    }
    return ellipse;
  }
  function matchExtentKeyword() {
    return match("extent-keyword", tokens.extentKeywords, 1);
  }
  function matchAtPosition() {
    if (match("position", /^at/, 0)) {
      var positioning = matchPositioning();
      if (!positioning) {
        error("Missing positioning value");
      }
      return positioning;
    }
  }
  function matchPositioning() {
    var location = matchCoordinates();
    if (location.x || location.y) {
      return {
        type: "position",
        value: location
      };
    }
  }
  function matchCoordinates() {
    return {
      x: matchDistance(),
      y: matchDistance()
    };
  }
  function matchListing(matcher) {
    var captures = matcher(), result = [];
    if (captures) {
      result.push(captures);
      while (scan(tokens.comma)) {
        captures = matcher();
        if (captures) {
          result.push(captures);
        } else {
          error("One extra comma");
        }
      }
    }
    return result;
  }
  function matchColorStop() {
    var color = matchColor();
    if (!color) {
      error("Expected color definition");
    }
    color.length = matchDistance();
    return color;
  }
  function matchColor() {
    return matchHexColor() || matchRGBAColor() || matchRGBColor() || matchLiteralColor();
  }
  function matchLiteralColor() {
    return match("literal", tokens.literalColor, 0);
  }
  function matchHexColor() {
    return match("hex", tokens.hexColor, 1);
  }
  function matchRGBColor() {
    return matchCall(tokens.rgbColor, function() {
      return {
        type: "rgb",
        value: matchListing(matchNumber)
      };
    });
  }
  function matchRGBAColor() {
    return matchCall(tokens.rgbaColor, function() {
      return {
        type: "rgba",
        value: matchListing(matchNumber)
      };
    });
  }
  function matchNumber() {
    return scan(tokens.number)[1];
  }
  function matchDistance() {
    return match("%", tokens.percentageValue, 1) || matchPositionKeyword() || matchLength();
  }
  function matchPositionKeyword() {
    return match("position-keyword", tokens.positionKeywords, 1);
  }
  function matchLength() {
    return match("px", tokens.pixelValue, 1) || match("em", tokens.emValue, 1);
  }
  function match(type, pattern, captureIndex) {
    var captures = scan(pattern);
    if (captures) {
      return {
        type,
        value: captures[captureIndex]
      };
    }
  }
  function scan(regexp) {
    var captures, blankCaptures;
    blankCaptures = /^[\n\r\t\s]+/.exec(input);
    if (blankCaptures) {
      consume(blankCaptures[0].length);
    }
    captures = regexp.exec(input);
    if (captures) {
      consume(captures[0].length);
    }
    return captures;
  }
  function consume(size) {
    input = input.substr(size);
  }
  return function(code) {
    input = code.toString();
    return getAST();
  };
}();
var parse = GradientParser.parse;
var stringify = GradientParser.stringify;
const colorPickerProps = {
  isWidget: C$1.bool.def(false),
  pickerType: C$1.oneOf(["fk", "chrome"]).def("fk"),
  shape: C$1.oneOf(["circle", "square"]).def("square"),
  value: {
    type: [String, Object],
    default: "#000000"
  },
  pureColor: {
    type: [String, Object],
    default: "#000000"
  },
  gradientColor: C$1.string.def("#000"),
  format: {
    type: String,
    default: "rgb"
  },
  disableAlpha: C$1.bool.def(false),
  disableHistory: C$1.bool.def(false),
  roundHistory: C$1.bool.def(false),
  useType: C$1.oneOf(["pure", "gradient", "both"]).def("pure"),
  activeKey: C$1.oneOf(["pure", "gradient"]).def("pure"),
  lang: {
    type: String,
    default: "ZH-cn"
  },
  zIndex: C$1.number.def(9999),
  trigger: C$1.oneOf(["click", "hover"]).def("click")
};
var ColorPicker_vue_vue_type_style_index_0_scoped_true_lang = "";
const _sfc_main = defineComponent({
  name: "ColorPicker",
  components: { FkColorPicker, ChromeColorPicker, GradientColorPicker, WrapContainer },
  inheritAttrs: false,
  props: colorPickerProps,
  emits: [
    "change",
    "update:pureColor",
    "update:value",
    "pureColorChange",
    "update:gradientColor",
    "gradientColorChange",
    "update:activeKey",
    "activeKeyChange"
  ],
  setup(props, { emit }) {
    const state = reactive({
      pureColor: props.value || "",
      activeKey: props.useType === "gradient" ? "gradient" : "pure",
      isAdvanceMode: false
    });
    provide(ColorPickerProviderKey, {
      lang: computed(() => props.lang || "ZH-cn")
    });
    const instance = new Color(state.pureColor);
    const colorInstance = ref(instance);
    const startColor = new Color("#000");
    const endColor = new Color("#000");
    const gradientState = reactive({
      startColor,
      endColor,
      startColorStop: 0,
      endColorStop: 100,
      angle: 0,
      gradientColor: props.gradientColor
    });
    const showPicker = ref(false);
    const colorCubeRef = ref(null);
    const pickerRef = ref(null);
    const getBgColorStyle = computed(() => {
      const bgColor = state.activeKey !== "gradient" ? tinycolor(state.pureColor).toRgbString() : gradientState.gradientColor;
      return {
        background: bgColor
      };
    });
    const getComponentName = computed(() => {
      if (state.activeKey === "gradient") {
        return GradientColorPicker.name;
      }
      return props.pickerType === "fk" ? FkColorPicker.name : ChromeColorPicker.name;
    });
    const getBindArgs = computed(() => {
      if (state.activeKey === "gradient") {
        return {
          startColor: gradientState.startColor,
          endColor: gradientState.endColor,
          onStartColorChange: (v2) => {
            gradientState.startColor = v2;
            onGradientChange();
          },
          onEndColorChange: (v2) => {
            gradientState.endColor = v2;
            onGradientChange();
          },
          angle: gradientState.angle,
          startColorStop: gradientState.startColorStop,
          endColorStop: gradientState.endColorStop,
          onStartColorStopChange: (v2) => {
            gradientState.startColorStop = v2;
            onGradientChange();
          },
          onEndColorStopChange: (v2) => {
            gradientState.endColorStop = v2;
            onGradientChange();
          },
          onAngleChange: (v2) => {
            gradientState.angle = v2;
            onGradientChange();
          },
          onAdvanceChange: (v2) => {
            state.isAdvanceMode = v2;
          }
        };
      }
      return {
        disableAlpha: props.disableAlpha,
        disableHistory: props.disableHistory,
        roundHistory: props.roundHistory,
        color: colorInstance.value,
        onChange: onColorChange,
        onAdvanceChange
      };
    });
    const onAdvanceChange = (isAdvance) => {
      state.isAdvanceMode = isAdvance;
    };
    const onShowPicker = () => {
      showPicker.value = true;
    };
    const onHidePicker = () => {
      showPicker.value = false;
    };
    const parseGradientColor = () => {
      var _a, _b, _c, _d;
      try {
        const [colorNode] = parse(gradientState.gradientColor);
        if (colorNode && colorNode.type === "linear-gradient" && ((_a = colorNode.orientation) == null ? void 0 : _a.type) === "angular" && colorNode.colorStops.length >= 2) {
          const startColorVal = colorNode.colorStops[0];
          const endColorVal = colorNode.colorStops[1];
          gradientState.startColorStop = Number((_b = startColorVal.length) == null ? void 0 : _b.value) || 0;
          gradientState.endColorStop = Number((_c = endColorVal.length) == null ? void 0 : _c.value) || 0;
          gradientState.angle = Number((_d = colorNode.orientation) == null ? void 0 : _d.value) || 0;
          const [r2, g2, b2, a2] = startColorVal.value;
          const [r1, g1, b1, a1] = startColorVal.value;
          gradientState.startColor = new Color({
            r: Number(r2),
            g: Number(g2),
            b: Number(b2),
            a: Number(a2)
          });
          gradientState.startColor = new Color({
            r: Number(r1),
            g: Number(g1),
            b: Number(b1),
            a: Number(a1)
          });
        }
      } catch (e2) {
        console.log(`[Parse Color]: ${e2}`);
      }
    };
    const onGradientChange = () => {
      const nodes = color2GradientNode();
      try {
        gradientState.gradientColor = stringify(nodes);
        emit("update:gradientColor", gradientState.gradientColor);
        emit("gradientColorChange", gradientState.gradientColor);
      } catch (e2) {
        console.log(e2);
      }
    };
    const color2GradientNode = () => {
      const nodes = [];
      const startColorArr = gradientState.startColor.RGB.map((v2) => v2.toString());
      const endColorArr = gradientState.endColor.RGB.map((v2) => v2.toString());
      nodes.push({
        type: "linear-gradient",
        orientation: { type: "angular", value: gradientState.angle + "" },
        colorStops: [
          {
            type: "rgba",
            value: [startColorArr[0], startColorArr[1], startColorArr[2], startColorArr[3]],
            length: { value: gradientState.startColorStop + "", type: "%" }
          },
          {
            type: "rgba",
            value: [endColorArr[0], endColorArr[1], endColorArr[2], endColorArr[3]],
            length: { value: gradientState.endColorStop + "", type: "%" }
          }
        ]
      });
      return nodes;
    };
    const onInit = () => {
      if (colorCubeRef.value && pickerRef.value) {
        createPopper(colorCubeRef.value, pickerRef.value, {
          placement: "auto",
          modifiers: [
            {
              name: "flip",
              options: {
                fallbackPlacements: ["top", "left"]
              }
            }
          ]
        });
      }
    };
    const onColorChange = (v2) => {
      colorInstance.value = v2;
      state.pureColor = v2.toString(props.format);
      emitColorChange();
    };
    const emitColorChange = () => {
      emit("update:pureColor", state.pureColor);
      emit("update:value", state.pureColor);
      emit("change", state.pureColor);
      emit("pureColorChange", state.pureColor);
    };
    onClickOutside(pickerRef, () => {
      onHidePicker();
    });
    const onActiveKeyChange = (key) => {
      state.activeKey = key;
      emit("update:activeKey", key);
      emit("activeKeyChange", key);
    };
    tryOnMounted(() => {
      onInit();
      emitColorChange();
      parseGradientColor();
      onGradientChange();
    });
    whenever(
      () => props.gradientColor,
      (value) => {
        if (value != gradientState.gradientColor) {
          gradientState.gradientColor = value;
        }
      }
    );
    whenever(
      () => gradientState.gradientColor,
      () => {
        parseGradientColor();
      }
    );
    whenever(
      () => props.activeKey,
      (value) => {
        state.activeKey = value;
      }
    );
    whenever(
      () => props.useType,
      (value) => {
        if (state.activeKey !== "gradient" && value === "gradient") {
          state.activeKey = "gradient";
        } else {
          state.activeKey = "pure";
        }
      }
    );
    whenever(
      () => props.value,
      (value) => {
        const equal = tinycolor.equals(value, state.pureColor);
        if (!equal) {
          state.pureColor = value;
          colorInstance.value = new Color(value);
          emitColorChange();
        }
      },
      { deep: true }
    );
    const showWrapContainer = ref(false);
    const handleDropdownClick = (e2) => {
      showWrapContainer.value = !showWrapContainer.value;
    };
    const handleDropdownEnter = (e2) => {
      showWrapContainer.value = true;
    };
    const handleDropdownLeave = () => {
      showWrapContainer.value = false;
    };
    const colorContentStyle = ref({});
    const getMenuEvents = computed(() => {
      const trigger = props.trigger;
      if (trigger === "click") {
        return {
          onMouseleave: handleDropdownLeave
        };
      }
      if (trigger === "hover") {
        return {
          onMouseenter: handleDropdownEnter,
          onMouseleave: handleDropdownLeave
        };
      }
    });
    return {
      colorCubeRef,
      pickerRef,
      showPicker,
      colorInstance,
      getBgColorStyle,
      onColorChange,
      onShowPicker,
      onActiveKeyChange,
      getComponentName,
      getBindArgs,
      state,
      showWrapContainer,
      getMenuEvents,
      colorContentStyle,
      handleDropdownClick
    };
  }
});
const _hoisted_1 = { class: "dropdown-content" };
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  const _component_WrapContainer = resolveComponent("WrapContainer");
  return openBlock(), createElementBlock(Fragment, null, [
    !_ctx.isWidget ? (openBlock(), createElementBlock("div", {
      key: 0,
      class: normalizeClass(["vc-color-wrap", { round: _ctx.shape === "circle" }]),
      ref: "colorCubeRef"
    }, [
      createElementVNode("div", mergeProps({ class: "dropdown" }, _ctx.getMenuEvents), [
        createElementVNode("div", {
          class: normalizeClass(["current-color", "transparent", { "current-color-radius": _ctx.shape === "circle" }]),
          style: normalizeStyle(_ctx.getBgColorStyle),
          onClick: _cache[0] || (_cache[0] = (...args) => _ctx.handleDropdownClick && _ctx.handleDropdownClick(...args))
        }, null, 6),
        createVNode(Transition, { name: "bounce" }, {
          default: withCtx(() => [
            withDirectives(createElementVNode("div", _hoisted_1, [
              createVNode(_component_WrapContainer, {
                "show-tab": _ctx.useType === "both" && !_ctx.state.isAdvanceMode,
                "active-key": _ctx.state.activeKey,
                "onUpdate:activeKey": _cache[1] || (_cache[1] = ($event) => _ctx.state.activeKey = $event),
                onChange: _ctx.onActiveKeyChange
              }, {
                default: withCtx(() => [
                  (openBlock(), createBlock(resolveDynamicComponent(_ctx.getComponentName), mergeProps({ key: _ctx.getComponentName }, _ctx.getBindArgs), null, 16))
                ]),
                _: 1
              }, 8, ["show-tab", "active-key", "onChange"])
            ], 512), [
              [vShow, _ctx.showWrapContainer]
            ])
          ]),
          _: 1
        })
      ], 16)
    ], 2)) : createCommentVNode("", true),
    _ctx.isWidget ? (openBlock(), createBlock(_component_WrapContainer, {
      key: 1,
      "active-key": _ctx.state.activeKey,
      "onUpdate:activeKey": _cache[2] || (_cache[2] = ($event) => _ctx.state.activeKey = $event),
      "show-tab": _ctx.useType === "both",
      onChange: _ctx.onActiveKeyChange,
      style: normalizeStyle({ zIndex: _ctx.zIndex })
    }, {
      default: withCtx(() => [
        (openBlock(), createBlock(resolveDynamicComponent(_ctx.getComponentName), mergeProps({ key: _ctx.getComponentName }, _ctx.getBindArgs), null, 16))
      ]),
      _: 1
    }, 8, ["active-key", "show-tab", "onChange", "style"])) : createCommentVNode("", true)
  ], 64);
}
var ColorPicker = /* @__PURE__ */ _export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-20ec07d0"]]);
const Vue3ColorPicker = {
  install: (app) => {
    app.component(ColorPicker.name, ColorPicker);
    app.component("Vue3" + ColorPicker.name, ColorPicker);
  }
};
export { ColorPicker, Vue3ColorPicker as default };
