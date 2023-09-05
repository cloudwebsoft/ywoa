import { defineComponent as Po, inject as ze, ref as $, reactive as bn, toRef as Me, nextTick as tt, watch as xe, onMounted as So, computed as xt, createVNode as xn, getCurrentScope as ga, onScopeDispose as ya, unref as ma, provide as Ie, onBeforeUnmount as ba, withDirectives as xa, vShow as wa } from "vue";
function _a(c) {
  return c && c.__esModule && Object.prototype.hasOwnProperty.call(c, "default") ? c.default : c;
}
var Eo = { exports: {} };
(function(c, y) {
  (function(m) {
    c.exports = m();
  })(function() {
    var m = {};
    Object.defineProperty(m, "__esModule", { value: !0 }), m.default = void 0, m.default = function(e) {
      return !(!e || !e.Window) && e instanceof e.Window;
    };
    var b = {};
    Object.defineProperty(b, "__esModule", { value: !0 }), b.init = L, b.getWindow = function(e) {
      return (0, m.default)(e) ? e : (e.ownerDocument || e).defaultView || M.window;
    }, b.window = b.realWindow = void 0;
    var k = void 0;
    b.realWindow = k;
    var M = void 0;
    function L(e) {
      b.realWindow = k = e;
      var t = e.document.createTextNode("");
      t.ownerDocument !== e.document && typeof e.wrap == "function" && e.wrap(t) === t && (e = e.wrap(e)), b.window = M = e;
    }
    b.window = M, typeof window < "u" && window && L(window);
    var v = {};
    function oe(e) {
      return (oe = typeof Symbol == "function" && typeof Symbol.iterator == "symbol" ? function(t) {
        return typeof t;
      } : function(t) {
        return t && typeof Symbol == "function" && t.constructor === Symbol && t !== Symbol.prototype ? "symbol" : typeof t;
      })(e);
    }
    Object.defineProperty(v, "__esModule", { value: !0 }), v.default = void 0;
    var te = function(e) {
      return !!e && oe(e) === "object";
    }, K = function(e) {
      return typeof e == "function";
    }, ge = { window: function(e) {
      return e === b.window || (0, m.default)(e);
    }, docFrag: function(e) {
      return te(e) && e.nodeType === 11;
    }, object: te, func: K, number: function(e) {
      return typeof e == "number";
    }, bool: function(e) {
      return typeof e == "boolean";
    }, string: function(e) {
      return typeof e == "string";
    }, element: function(e) {
      if (!e || oe(e) !== "object")
        return !1;
      var t = b.getWindow(e) || b.window;
      return /object|function/.test(oe(t.Element)) ? e instanceof t.Element : e.nodeType === 1 && typeof e.nodeName == "string";
    }, plainObject: function(e) {
      return te(e) && !!e.constructor && /function Object\b/.test(e.constructor.toString());
    }, array: function(e) {
      return te(e) && e.length !== void 0 && K(e.splice);
    } };
    v.default = ge;
    var le = {};
    function pe(e) {
      var t = e.interaction;
      if (t.prepared.name === "drag") {
        var n = t.prepared.axis;
        n === "x" ? (t.coords.cur.page.y = t.coords.start.page.y, t.coords.cur.client.y = t.coords.start.client.y, t.coords.velocity.client.y = 0, t.coords.velocity.page.y = 0) : n === "y" && (t.coords.cur.page.x = t.coords.start.page.x, t.coords.cur.client.x = t.coords.start.client.x, t.coords.velocity.client.x = 0, t.coords.velocity.page.x = 0);
      }
    }
    function He(e) {
      var t = e.iEvent, n = e.interaction;
      if (n.prepared.name === "drag") {
        var r = n.prepared.axis;
        if (r === "x" || r === "y") {
          var o = r === "x" ? "y" : "x";
          t.page[o] = n.coords.start.page[o], t.client[o] = n.coords.start.client[o], t.delta[o] = 0;
        }
      }
    }
    Object.defineProperty(le, "__esModule", { value: !0 }), le.default = void 0;
    var ue = { id: "actions/drag", install: function(e) {
      var t = e.actions, n = e.Interactable, r = e.defaults;
      n.prototype.draggable = ue.draggable, t.map.drag = ue, t.methodDict.drag = "draggable", r.actions.drag = ue.defaults;
    }, listeners: { "interactions:before-action-move": pe, "interactions:action-resume": pe, "interactions:action-move": He, "auto-start:check": function(e) {
      var t = e.interaction, n = e.interactable, r = e.buttons, o = n.options.drag;
      if (o && o.enabled && (!t.pointerIsDown || !/mouse|pointer/.test(t.pointerType) || (r & n.options.drag.mouseButtons) != 0))
        return e.action = { name: "drag", axis: o.lockAxis === "start" ? o.startAxis : o.lockAxis }, !1;
    } }, draggable: function(e) {
      return v.default.object(e) ? (this.options.drag.enabled = e.enabled !== !1, this.setPerAction("drag", e), this.setOnEvents("drag", e), /^(xy|x|y|start)$/.test(e.lockAxis) && (this.options.drag.lockAxis = e.lockAxis), /^(xy|x|y)$/.test(e.startAxis) && (this.options.drag.startAxis = e.startAxis), this) : v.default.bool(e) ? (this.options.drag.enabled = e, this) : this.options.drag;
    }, beforeMove: pe, move: He, defaults: { startAxis: "xy", lockAxis: "xy" }, getCursor: function() {
      return "move";
    } }, we = ue;
    le.default = we;
    var X = {};
    Object.defineProperty(X, "__esModule", { value: !0 }), X.default = void 0;
    var Q = { init: function(e) {
      var t = e;
      Q.document = t.document, Q.DocumentFragment = t.DocumentFragment || Oe, Q.SVGElement = t.SVGElement || Oe, Q.SVGSVGElement = t.SVGSVGElement || Oe, Q.SVGElementInstance = t.SVGElementInstance || Oe, Q.Element = t.Element || Oe, Q.HTMLElement = t.HTMLElement || Q.Element, Q.Event = t.Event, Q.Touch = t.Touch || Oe, Q.PointerEvent = t.PointerEvent || t.MSPointerEvent;
    }, document: null, DocumentFragment: null, SVGElement: null, SVGSVGElement: null, SVGElementInstance: null, Element: null, HTMLElement: null, Event: null, Touch: null, PointerEvent: null };
    function Oe() {
    }
    var Ge = Q;
    X.default = Ge;
    var z = {};
    Object.defineProperty(z, "__esModule", { value: !0 }), z.default = void 0;
    var F = { init: function(e) {
      var t = X.default.Element, n = e.navigator || {};
      F.supportsTouch = "ontouchstart" in e || v.default.func(e.DocumentTouch) && X.default.document instanceof e.DocumentTouch, F.supportsPointerEvent = n.pointerEnabled !== !1 && !!X.default.PointerEvent, F.isIOS = /iP(hone|od|ad)/.test(n.platform), F.isIOS7 = /iP(hone|od|ad)/.test(n.platform) && /OS 7[^\d]/.test(n.appVersion), F.isIe9 = /MSIE 9/.test(n.userAgent), F.isOperaMobile = n.appName === "Opera" && F.supportsTouch && /Presto/.test(n.userAgent), F.prefixedMatchesSelector = "matches" in t.prototype ? "matches" : "webkitMatchesSelector" in t.prototype ? "webkitMatchesSelector" : "mozMatchesSelector" in t.prototype ? "mozMatchesSelector" : "oMatchesSelector" in t.prototype ? "oMatchesSelector" : "msMatchesSelector", F.pEventTypes = F.supportsPointerEvent ? X.default.PointerEvent === e.MSPointerEvent ? { up: "MSPointerUp", down: "MSPointerDown", over: "mouseover", out: "mouseout", move: "MSPointerMove", cancel: "MSPointerCancel" } : { up: "pointerup", down: "pointerdown", over: "pointerover", out: "pointerout", move: "pointermove", cancel: "pointercancel" } : null, F.wheelEvent = X.default.document && "onmousewheel" in X.default.document ? "mousewheel" : "wheel";
    }, supportsTouch: null, supportsPointerEvent: null, isIOS7: null, isIOS: null, isIe9: null, isOperaMobile: null, prefixedMatchesSelector: null, pEventTypes: null, wheelEvent: null }, ie = F;
    z.default = ie;
    var S = {};
    function Z(e) {
      var t = e.parentNode;
      if (v.default.docFrag(t)) {
        for (; (t = t.host) && v.default.docFrag(t); )
          ;
        return t;
      }
      return t;
    }
    function q(e, t) {
      return b.window !== b.realWindow && (t = t.replace(/\/deep\//g, " ")), e[z.default.prefixedMatchesSelector](t);
    }
    Object.defineProperty(S, "__esModule", { value: !0 }), S.nodeContains = function(e, t) {
      if (e.contains)
        return e.contains(t);
      for (; t; ) {
        if (t === e)
          return !0;
        t = t.parentNode;
      }
      return !1;
    }, S.closest = function(e, t) {
      for (; v.default.element(e); ) {
        if (q(e, t))
          return e;
        e = Z(e);
      }
      return null;
    }, S.parentNode = Z, S.matchesSelector = q, S.indexOfDeepestElement = function(e) {
      for (var t, n = [], r = 0; r < e.length; r++) {
        var o = e[r], i = e[t];
        if (o && r !== t)
          if (i) {
            var s = H(o), a = H(i);
            if (s !== o.ownerDocument)
              if (a !== o.ownerDocument)
                if (s !== a) {
                  n = n.length ? n : qe(i);
                  var l = void 0;
                  if (i instanceof X.default.HTMLElement && o instanceof X.default.SVGElement && !(o instanceof X.default.SVGSVGElement)) {
                    if (o === a)
                      continue;
                    l = o.ownerSVGElement;
                  } else
                    l = o;
                  for (var u = qe(l, i.ownerDocument), f = 0; u[f] && u[f] === n[f]; )
                    f++;
                  var d = [u[f - 1], u[f], n[f]];
                  if (d[0])
                    for (var h = d[0].lastChild; h; ) {
                      if (h === d[1]) {
                        t = r, n = u;
                        break;
                      }
                      if (h === d[2])
                        break;
                      h = h.previousSibling;
                    }
                } else
                  g = o, p = i, (parseInt(b.getWindow(g).getComputedStyle(g).zIndex, 10) || 0) >= (parseInt(b.getWindow(p).getComputedStyle(p).zIndex, 10) || 0) && (t = r);
              else
                t = r;
          } else
            t = r;
      }
      var g, p;
      return t;
    }, S.matchesUpTo = function(e, t, n) {
      for (; v.default.element(e); ) {
        if (q(e, t))
          return !0;
        if ((e = Z(e)) === n)
          return q(e, t);
      }
      return !1;
    }, S.getActualElement = function(e) {
      return e.correspondingUseElement || e;
    }, S.getScrollXY = ae, S.getElementClientRect = Pe, S.getElementRect = function(e) {
      var t = Pe(e);
      if (!z.default.isIOS7 && t) {
        var n = ae(b.getWindow(e));
        t.left += n.x, t.right += n.x, t.top += n.y, t.bottom += n.y;
      }
      return t;
    }, S.getPath = function(e) {
      for (var t = []; e; )
        t.push(e), e = Z(e);
      return t;
    }, S.trySelector = function(e) {
      return !!v.default.string(e) && (X.default.document.querySelector(e), !0);
    };
    var H = function(e) {
      return e.parentNode || e.host;
    };
    function qe(e, t) {
      for (var n, r = [], o = e; (n = H(o)) && o !== t && n !== o.ownerDocument; )
        r.unshift(o), o = n;
      return r;
    }
    function ae(e) {
      return { x: (e = e || b.window).scrollX || e.document.documentElement.scrollLeft, y: e.scrollY || e.document.documentElement.scrollTop };
    }
    function Pe(e) {
      var t = e instanceof X.default.SVGElement ? e.getBoundingClientRect() : e.getClientRects()[0];
      return t && { left: t.left, right: t.right, top: t.top, bottom: t.bottom, width: t.width || t.right - t.left, height: t.height || t.bottom - t.top };
    }
    var P = {};
    Object.defineProperty(P, "__esModule", { value: !0 }), P.default = function(e, t) {
      for (var n in t)
        e[n] = t[n];
      return e;
    };
    var B = {};
    function Ae(e, t) {
      (t == null || t > e.length) && (t = e.length);
      for (var n = 0, r = Array(t); n < t; n++)
        r[n] = e[n];
      return r;
    }
    function Ve(e, t, n) {
      return e === "parent" ? (0, S.parentNode)(n) : e === "self" ? t.getRect(n) : (0, S.closest)(n, e);
    }
    Object.defineProperty(B, "__esModule", { value: !0 }), B.getStringOptionResult = Ve, B.resolveRectLike = function(e, t, n, r) {
      var o, i = e;
      return v.default.string(i) ? i = Ve(i, t, n) : v.default.func(i) && (i = i.apply(void 0, function(s) {
        if (Array.isArray(s))
          return Ae(s);
      }(o = r) || function(s) {
        if (typeof Symbol < "u" && Symbol.iterator in Object(s))
          return Array.from(s);
      }(o) || function(s, a) {
        if (s) {
          if (typeof s == "string")
            return Ae(s, a);
          var l = Object.prototype.toString.call(s).slice(8, -1);
          return l === "Object" && s.constructor && (l = s.constructor.name), l === "Map" || l === "Set" ? Array.from(s) : l === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(l) ? Ae(s, a) : void 0;
        }
      }(o) || function() {
        throw new TypeError(`Invalid attempt to spread non-iterable instance.
In order to be iterable, non-array objects must have a [Symbol.iterator]() method.`);
      }())), v.default.element(i) && (i = (0, S.getElementRect)(i)), i;
    }, B.rectToXY = function(e) {
      return e && { x: "x" in e ? e.x : e.left, y: "y" in e ? e.y : e.top };
    }, B.xywhToTlbr = function(e) {
      return !e || "left" in e && "top" in e || ((e = (0, P.default)({}, e)).left = e.x || 0, e.top = e.y || 0, e.right = e.right || e.left + e.width, e.bottom = e.bottom || e.top + e.height), e;
    }, B.tlbrToXywh = function(e) {
      return !e || "x" in e && "y" in e || ((e = (0, P.default)({}, e)).x = e.left || 0, e.y = e.top || 0, e.width = e.width || (e.right || 0) - e.x, e.height = e.height || (e.bottom || 0) - e.y), e;
    }, B.addEdges = function(e, t, n) {
      e.left && (t.left += n.x), e.right && (t.right += n.x), e.top && (t.top += n.y), e.bottom && (t.bottom += n.y), t.width = t.right - t.left, t.height = t.bottom - t.top;
    };
    var Te = {};
    Object.defineProperty(Te, "__esModule", { value: !0 }), Te.default = function(e, t, n) {
      var r = e.options[n], o = r && r.origin || e.options.origin, i = (0, B.resolveRectLike)(o, e, t, [e && t]);
      return (0, B.rectToXY)(i) || { x: 0, y: 0 };
    };
    var ve = {};
    function wt(e) {
      return e.trim().split(/ +/);
    }
    Object.defineProperty(ve, "__esModule", { value: !0 }), ve.default = function e(t, n, r) {
      if (r = r || {}, v.default.string(t) && t.search(" ") !== -1 && (t = wt(t)), v.default.array(t))
        return t.reduce(function(l, u) {
          return (0, P.default)(l, e(u, n, r));
        }, r);
      if (v.default.object(t) && (n = t, t = ""), v.default.func(n))
        r[t] = r[t] || [], r[t].push(n);
      else if (v.default.array(n))
        for (var o = 0; o < n.length; o++) {
          var i;
          i = n[o], e(t, i, r);
        }
      else if (v.default.object(n))
        for (var s in n) {
          var a = wt(s).map(function(l) {
            return "".concat(t).concat(l);
          });
          e(a, n[s], r);
        }
      return r;
    };
    var De = {};
    Object.defineProperty(De, "__esModule", { value: !0 }), De.default = void 0, De.default = function(e, t) {
      return Math.sqrt(e * e + t * t);
    };
    var se = {};
    function _t(e, t) {
      for (var n in t) {
        var r = _t.prefixedPropREs, o = !1;
        for (var i in r)
          if (n.indexOf(i) === 0 && r[i].test(n)) {
            o = !0;
            break;
          }
        o || typeof t[n] == "function" || (e[n] = t[n]);
      }
      return e;
    }
    Object.defineProperty(se, "__esModule", { value: !0 }), se.default = void 0, _t.prefixedPropREs = { webkit: /(Movement[XY]|Radius[XY]|RotationAngle|Force)$/, moz: /(Pressure)$/ };
    var _n = _t;
    se.default = _n;
    var E = {};
    function Ue(e) {
      return e instanceof X.default.Event || e instanceof X.default.Touch;
    }
    function Fe(e, t, n) {
      return e = e || "page", (n = n || {}).x = t[e + "X"], n.y = t[e + "Y"], n;
    }
    function ct(e, t) {
      return t = t || { x: 0, y: 0 }, z.default.isOperaMobile && Ue(e) ? (Fe("screen", e, t), t.x += window.scrollX, t.y += window.scrollY) : Fe("page", e, t), t;
    }
    function Wt(e, t) {
      return t = t || {}, z.default.isOperaMobile && Ue(e) ? Fe("screen", e, t) : Fe("client", e, t), t;
    }
    function Re(e) {
      var t = [];
      return v.default.array(e) ? (t[0] = e[0], t[1] = e[1]) : e.type === "touchend" ? e.touches.length === 1 ? (t[0] = e.touches[0], t[1] = e.changedTouches[0]) : e.touches.length === 0 && (t[0] = e.changedTouches[0], t[1] = e.changedTouches[1]) : (t[0] = e.touches[0], t[1] = e.touches[1]), t;
    }
    function Xt(e) {
      for (var t = { pageX: 0, pageY: 0, clientX: 0, clientY: 0, screenX: 0, screenY: 0 }, n = 0; n < e.length; n++) {
        var r = e[n];
        for (var o in t)
          t[o] += r[o];
      }
      for (var i in t)
        t[i] /= e.length;
      return t;
    }
    Object.defineProperty(E, "__esModule", { value: !0 }), E.copyCoords = function(e, t) {
      e.page = e.page || {}, e.page.x = t.page.x, e.page.y = t.page.y, e.client = e.client || {}, e.client.x = t.client.x, e.client.y = t.client.y, e.timeStamp = t.timeStamp;
    }, E.setCoordDeltas = function(e, t, n) {
      e.page.x = n.page.x - t.page.x, e.page.y = n.page.y - t.page.y, e.client.x = n.client.x - t.client.x, e.client.y = n.client.y - t.client.y, e.timeStamp = n.timeStamp - t.timeStamp;
    }, E.setCoordVelocity = function(e, t) {
      var n = Math.max(t.timeStamp / 1e3, 1e-3);
      e.page.x = t.page.x / n, e.page.y = t.page.y / n, e.client.x = t.client.x / n, e.client.y = t.client.y / n, e.timeStamp = n;
    }, E.setZeroCoords = function(e) {
      e.page.x = 0, e.page.y = 0, e.client.x = 0, e.client.y = 0;
    }, E.isNativePointer = Ue, E.getXY = Fe, E.getPageXY = ct, E.getClientXY = Wt, E.getPointerId = function(e) {
      return v.default.number(e.pointerId) ? e.pointerId : e.identifier;
    }, E.setCoords = function(e, t, n) {
      var r = t.length > 1 ? Xt(t) : t[0];
      ct(r, e.page), Wt(r, e.client), e.timeStamp = n;
    }, E.getTouchPair = Re, E.pointerAverage = Xt, E.touchBBox = function(e) {
      if (!e.length)
        return null;
      var t = Re(e), n = Math.min(t[0].pageX, t[1].pageX), r = Math.min(t[0].pageY, t[1].pageY), o = Math.max(t[0].pageX, t[1].pageX), i = Math.max(t[0].pageY, t[1].pageY);
      return { x: n, y: r, left: n, top: r, right: o, bottom: i, width: o - n, height: i - r };
    }, E.touchDistance = function(e, t) {
      var n = t + "X", r = t + "Y", o = Re(e), i = o[0][n] - o[1][n], s = o[0][r] - o[1][r];
      return (0, De.default)(i, s);
    }, E.touchAngle = function(e, t) {
      var n = t + "X", r = t + "Y", o = Re(e), i = o[1][n] - o[0][n], s = o[1][r] - o[0][r];
      return 180 * Math.atan2(s, i) / Math.PI;
    }, E.getPointerType = function(e) {
      return v.default.string(e.pointerType) ? e.pointerType : v.default.number(e.pointerType) ? [void 0, void 0, "touch", "pen", "mouse"][e.pointerType] : /touch/.test(e.type || "") || e instanceof X.default.Touch ? "touch" : "mouse";
    }, E.getEventTargets = function(e) {
      var t = v.default.func(e.composedPath) ? e.composedPath() : e.path;
      return [S.getActualElement(t ? t[0] : e.target), S.getActualElement(e.currentTarget)];
    }, E.newCoords = function() {
      return { page: { x: 0, y: 0 }, client: { x: 0, y: 0 }, timeStamp: 0 };
    }, E.coordsToEvent = function(e) {
      return { coords: e, get page() {
        return this.coords.page;
      }, get client() {
        return this.coords.client;
      }, get timeStamp() {
        return this.coords.timeStamp;
      }, get pageX() {
        return this.coords.page.x;
      }, get pageY() {
        return this.coords.page.y;
      }, get clientX() {
        return this.coords.client.x;
      }, get clientY() {
        return this.coords.client.y;
      }, get pointerId() {
        return this.coords.pointerId;
      }, get target() {
        return this.coords.target;
      }, get type() {
        return this.coords.type;
      }, get pointerType() {
        return this.coords.pointerType;
      }, get buttons() {
        return this.coords.buttons;
      }, preventDefault: function() {
      } };
    }, Object.defineProperty(E, "pointerExtend", { enumerable: !0, get: function() {
      return se.default;
    } });
    var Ke = {};
    function Yt(e, t) {
      for (var n = 0; n < t.length; n++) {
        var r = t[n];
        r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), Object.defineProperty(e, r.key, r);
      }
    }
    function O(e, t, n) {
      return t in e ? Object.defineProperty(e, t, { value: n, enumerable: !0, configurable: !0, writable: !0 }) : e[t] = n, e;
    }
    Object.defineProperty(Ke, "__esModule", { value: !0 }), Ke.BaseEvent = void 0;
    var Y = function() {
      function e(r) {
        (function(o, i) {
          if (!(o instanceof i))
            throw new TypeError("Cannot call a class as a function");
        })(this, e), O(this, "type", void 0), O(this, "target", void 0), O(this, "currentTarget", void 0), O(this, "interactable", void 0), O(this, "_interaction", void 0), O(this, "timeStamp", void 0), O(this, "immediatePropagationStopped", !1), O(this, "propagationStopped", !1), this._interaction = r;
      }
      var t, n;
      return t = e, (n = [{ key: "preventDefault", value: function() {
      } }, { key: "stopPropagation", value: function() {
        this.propagationStopped = !0;
      } }, { key: "stopImmediatePropagation", value: function() {
        this.immediatePropagationStopped = this.propagationStopped = !0;
      } }]) && Yt(t.prototype, n), e;
    }();
    Ke.BaseEvent = Y, Object.defineProperty(Y.prototype, "interaction", { get: function() {
      return this._interaction._proxy;
    }, set: function() {
    } });
    var I = {};
    Object.defineProperty(I, "__esModule", { value: !0 }), I.find = I.findIndex = I.from = I.merge = I.remove = I.contains = void 0, I.contains = function(e, t) {
      return e.indexOf(t) !== -1;
    }, I.remove = function(e, t) {
      return e.splice(e.indexOf(t), 1);
    };
    var J = function(e, t) {
      for (var n = 0; n < t.length; n++) {
        var r = t[n];
        e.push(r);
      }
      return e;
    };
    I.merge = J, I.from = function(e) {
      return J([], e);
    };
    var R = function(e, t) {
      for (var n = 0; n < e.length; n++)
        if (t(e[n], n, e))
          return n;
      return -1;
    };
    I.findIndex = R, I.find = function(e, t) {
      return e[R(e, t)];
    };
    var j = {};
    function re(e) {
      return (re = typeof Symbol == "function" && typeof Symbol.iterator == "symbol" ? function(t) {
        return typeof t;
      } : function(t) {
        return t && typeof Symbol == "function" && t.constructor === Symbol && t !== Symbol.prototype ? "symbol" : typeof t;
      })(e);
    }
    function Be(e, t) {
      for (var n = 0; n < t.length; n++) {
        var r = t[n];
        r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), Object.defineProperty(e, r.key, r);
      }
    }
    function or(e, t) {
      return (or = Object.setPrototypeOf || function(n, r) {
        return n.__proto__ = r, n;
      })(e, t);
    }
    function Lo(e, t) {
      return !t || re(t) !== "object" && typeof t != "function" ? We(e) : t;
    }
    function We(e) {
      if (e === void 0)
        throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
      return e;
    }
    function On(e) {
      return (On = Object.setPrototypeOf ? Object.getPrototypeOf : function(t) {
        return t.__proto__ || Object.getPrototypeOf(t);
      })(e);
    }
    function Ze(e, t, n) {
      return t in e ? Object.defineProperty(e, t, { value: n, enumerable: !0, configurable: !0, writable: !0 }) : e[t] = n, e;
    }
    Object.defineProperty(j, "__esModule", { value: !0 }), j.DropEvent = void 0;
    var No = function(e) {
      (function(a, l) {
        if (typeof l != "function" && l !== null)
          throw new TypeError("Super expression must either be null or a function");
        a.prototype = Object.create(l && l.prototype, { constructor: { value: a, writable: !0, configurable: !0 } }), l && or(a, l);
      })(s, e);
      var t, n, r, o, i = (r = s, o = function() {
        if (typeof Reflect > "u" || !Reflect.construct || Reflect.construct.sham)
          return !1;
        if (typeof Proxy == "function")
          return !0;
        try {
          return Boolean.prototype.valueOf.call(Reflect.construct(Boolean, [], function() {
          })), !0;
        } catch {
          return !1;
        }
      }(), function() {
        var a, l = On(r);
        if (o) {
          var u = On(this).constructor;
          a = Reflect.construct(l, arguments, u);
        } else
          a = l.apply(this, arguments);
        return Lo(this, a);
      });
      function s(a, l, u) {
        var f;
        (function(p, x) {
          if (!(p instanceof x))
            throw new TypeError("Cannot call a class as a function");
        })(this, s), Ze(We(f = i.call(this, l._interaction)), "target", void 0), Ze(We(f), "dropzone", void 0), Ze(We(f), "dragEvent", void 0), Ze(We(f), "relatedTarget", void 0), Ze(We(f), "draggable", void 0), Ze(We(f), "timeStamp", void 0), Ze(We(f), "propagationStopped", !1), Ze(We(f), "immediatePropagationStopped", !1);
        var d = u === "dragleave" ? a.prev : a.cur, h = d.element, g = d.dropzone;
        return f.type = u, f.target = h, f.currentTarget = h, f.dropzone = g, f.dragEvent = l, f.relatedTarget = l.target, f.draggable = l.interactable, f.timeStamp = l.timeStamp, f;
      }
      return t = s, (n = [{ key: "reject", value: function() {
        var a = this, l = this._interaction.dropState;
        if (this.type === "dropactivate" || this.dropzone && l.cur.dropzone === this.dropzone && l.cur.element === this.target)
          if (l.prev.dropzone = this.dropzone, l.prev.element = this.target, l.rejected = !0, l.events.enter = null, this.stopImmediatePropagation(), this.type === "dropactivate") {
            var u = l.activeDrops, f = I.findIndex(u, function(h) {
              var g = h.dropzone, p = h.element;
              return g === a.dropzone && p === a.target;
            });
            l.activeDrops.splice(f, 1);
            var d = new s(l, this.dragEvent, "dropdeactivate");
            d.dropzone = this.dropzone, d.target = this.target, this.dropzone.fire(d);
          } else
            this.dropzone.fire(new s(l, this.dragEvent, "dragleave"));
      } }, { key: "preventDefault", value: function() {
      } }, { key: "stopPropagation", value: function() {
        this.propagationStopped = !0;
      } }, { key: "stopImmediatePropagation", value: function() {
        this.immediatePropagationStopped = this.propagationStopped = !0;
      } }]) && Be(t.prototype, n), s;
    }(Ke.BaseEvent);
    j.DropEvent = No;
    var $t = {};
    function ir(e, t) {
      for (var n = 0; n < e.slice().length; n++) {
        var r = e.slice()[n], o = r.dropzone, i = r.element;
        t.dropzone = o, t.target = i, o.fire(t), t.propagationStopped = t.immediatePropagationStopped = !1;
      }
    }
    function Pn(e, t) {
      for (var n = function(i, s) {
        for (var a = i.interactables, l = [], u = 0; u < a.list.length; u++) {
          var f = a.list[u];
          if (f.options.drop.enabled) {
            var d = f.options.drop.accept;
            if (!(v.default.element(d) && d !== s || v.default.string(d) && !S.matchesSelector(s, d) || v.default.func(d) && !d({ dropzone: f, draggableElement: s })))
              for (var h = v.default.string(f.target) ? f._context.querySelectorAll(f.target) : v.default.array(f.target) ? f.target : [f.target], g = 0; g < h.length; g++) {
                var p = h[g];
                p !== s && l.push({ dropzone: f, element: p, rect: f.getRect(p) });
              }
          }
        }
        return l;
      }(e, t), r = 0; r < n.length; r++) {
        var o = n[r];
        o.rect = o.dropzone.getRect(o.element);
      }
      return n;
    }
    function ar(e, t, n) {
      for (var r = e.dropState, o = e.interactable, i = e.element, s = [], a = 0; a < r.activeDrops.length; a++) {
        var l = r.activeDrops[a], u = l.dropzone, f = l.element, d = l.rect;
        s.push(u.dropCheck(t, n, o, i, f, d) ? f : null);
      }
      var h = S.indexOfDeepestElement(s);
      return r.activeDrops[h] || null;
    }
    function Sn(e, t, n) {
      var r = e.dropState, o = { enter: null, leave: null, activate: null, deactivate: null, move: null, drop: null };
      return n.type === "dragstart" && (o.activate = new j.DropEvent(r, n, "dropactivate"), o.activate.target = null, o.activate.dropzone = null), n.type === "dragend" && (o.deactivate = new j.DropEvent(r, n, "dropdeactivate"), o.deactivate.target = null, o.deactivate.dropzone = null), r.rejected || (r.cur.element !== r.prev.element && (r.prev.dropzone && (o.leave = new j.DropEvent(r, n, "dragleave"), n.dragLeave = o.leave.target = r.prev.element, n.prevDropzone = o.leave.dropzone = r.prev.dropzone), r.cur.dropzone && (o.enter = new j.DropEvent(r, n, "dragenter"), n.dragEnter = r.cur.element, n.dropzone = r.cur.dropzone)), n.type === "dragend" && r.cur.dropzone && (o.drop = new j.DropEvent(r, n, "drop"), n.dropzone = r.cur.dropzone, n.relatedTarget = r.cur.element), n.type === "dragmove" && r.cur.dropzone && (o.move = new j.DropEvent(r, n, "dropmove"), o.move.dragmove = n, n.dropzone = r.cur.dropzone)), o;
    }
    function En(e, t) {
      var n = e.dropState, r = n.activeDrops, o = n.cur, i = n.prev;
      t.leave && i.dropzone.fire(t.leave), t.enter && o.dropzone.fire(t.enter), t.move && o.dropzone.fire(t.move), t.drop && o.dropzone.fire(t.drop), t.deactivate && ir(r, t.deactivate), n.prev.dropzone = o.dropzone, n.prev.element = o.element;
    }
    function sr(e, t) {
      var n = e.interaction, r = e.iEvent, o = e.event;
      if (r.type === "dragmove" || r.type === "dragend") {
        var i = n.dropState;
        t.dynamicDrop && (i.activeDrops = Pn(t, n.element));
        var s = r, a = ar(n, s, o);
        i.rejected = i.rejected && !!a && a.dropzone === i.cur.dropzone && a.element === i.cur.element, i.cur.dropzone = a && a.dropzone, i.cur.element = a && a.element, i.events = Sn(n, 0, s);
      }
    }
    Object.defineProperty($t, "__esModule", { value: !0 }), $t.default = void 0;
    var lr = { id: "actions/drop", install: function(e) {
      var t = e.actions, n = e.interactStatic, r = e.Interactable, o = e.defaults;
      e.usePlugin(le.default), r.prototype.dropzone = function(i) {
        return function(s, a) {
          if (v.default.object(a)) {
            if (s.options.drop.enabled = a.enabled !== !1, a.listeners) {
              var l = (0, ve.default)(a.listeners), u = Object.keys(l).reduce(function(f, d) {
                return f[/^(enter|leave)/.test(d) ? "drag".concat(d) : /^(activate|deactivate|move)/.test(d) ? "drop".concat(d) : d] = l[d], f;
              }, {});
              s.off(s.options.drop.listeners), s.on(u), s.options.drop.listeners = u;
            }
            return v.default.func(a.ondrop) && s.on("drop", a.ondrop), v.default.func(a.ondropactivate) && s.on("dropactivate", a.ondropactivate), v.default.func(a.ondropdeactivate) && s.on("dropdeactivate", a.ondropdeactivate), v.default.func(a.ondragenter) && s.on("dragenter", a.ondragenter), v.default.func(a.ondragleave) && s.on("dragleave", a.ondragleave), v.default.func(a.ondropmove) && s.on("dropmove", a.ondropmove), /^(pointer|center)$/.test(a.overlap) ? s.options.drop.overlap = a.overlap : v.default.number(a.overlap) && (s.options.drop.overlap = Math.max(Math.min(1, a.overlap), 0)), "accept" in a && (s.options.drop.accept = a.accept), "checker" in a && (s.options.drop.checker = a.checker), s;
          }
          return v.default.bool(a) ? (s.options.drop.enabled = a, s) : s.options.drop;
        }(this, i);
      }, r.prototype.dropCheck = function(i, s, a, l, u, f) {
        return function(d, h, g, p, x, _, w) {
          var T = !1;
          if (!(w = w || d.getRect(_)))
            return !!d.options.drop.checker && d.options.drop.checker(h, g, T, d, _, p, x);
          var D = d.options.drop.overlap;
          if (D === "pointer") {
            var A = (0, Te.default)(p, x, "drag"), N = E.getPageXY(h);
            N.x += A.x, N.y += A.y;
            var ee = N.x > w.left && N.x < w.right, W = N.y > w.top && N.y < w.bottom;
            T = ee && W;
          }
          var U = p.getRect(x);
          if (U && D === "center") {
            var Ee = U.left + U.width / 2, Ne = U.top + U.height / 2;
            T = Ee >= w.left && Ee <= w.right && Ne >= w.top && Ne <= w.bottom;
          }
          return U && v.default.number(D) && (T = Math.max(0, Math.min(w.right, U.right) - Math.max(w.left, U.left)) * Math.max(0, Math.min(w.bottom, U.bottom) - Math.max(w.top, U.top)) / (U.width * U.height) >= D), d.options.drop.checker && (T = d.options.drop.checker(h, g, T, d, _, p, x)), T;
        }(this, i, s, a, l, u, f);
      }, n.dynamicDrop = function(i) {
        return v.default.bool(i) ? (e.dynamicDrop = i, n) : e.dynamicDrop;
      }, (0, P.default)(t.phaselessTypes, { dragenter: !0, dragleave: !0, dropactivate: !0, dropdeactivate: !0, dropmove: !0, drop: !0 }), t.methodDict.drop = "dropzone", e.dynamicDrop = !1, o.actions.drop = lr.defaults;
    }, listeners: { "interactions:before-action-start": function(e) {
      var t = e.interaction;
      t.prepared.name === "drag" && (t.dropState = { cur: { dropzone: null, element: null }, prev: { dropzone: null, element: null }, rejected: null, events: null, activeDrops: [] });
    }, "interactions:after-action-start": function(e, t) {
      var n = e.interaction, r = (e.event, e.iEvent);
      if (n.prepared.name === "drag") {
        var o = n.dropState;
        o.activeDrops = null, o.events = null, o.activeDrops = Pn(t, n.element), o.events = Sn(n, 0, r), o.events.activate && (ir(o.activeDrops, o.events.activate), t.fire("actions/drop:start", { interaction: n, dragEvent: r }));
      }
    }, "interactions:action-move": sr, "interactions:after-action-move": function(e, t) {
      var n = e.interaction, r = e.iEvent;
      n.prepared.name === "drag" && (En(n, n.dropState.events), t.fire("actions/drop:move", { interaction: n, dragEvent: r }), n.dropState.events = {});
    }, "interactions:action-end": function(e, t) {
      if (e.interaction.prepared.name === "drag") {
        var n = e.interaction, r = e.iEvent;
        sr(e, t), En(n, n.dropState.events), t.fire("actions/drop:end", { interaction: n, dragEvent: r });
      }
    }, "interactions:stop": function(e) {
      var t = e.interaction;
      if (t.prepared.name === "drag") {
        var n = t.dropState;
        n && (n.activeDrops = null, n.events = null, n.cur.dropzone = null, n.cur.element = null, n.prev.dropzone = null, n.prev.element = null, n.rejected = !1);
      }
    } }, getActiveDrops: Pn, getDrop: ar, getDropEvents: Sn, fireDropEvents: En, defaults: { enabled: !1, accept: null, overlap: "pointer" } }, Ho = lr;
    $t.default = Ho;
    var Lt = {};
    function Mn(e) {
      var t = e.interaction, n = e.iEvent, r = e.phase;
      if (t.prepared.name === "gesture") {
        var o = t.pointers.map(function(u) {
          return u.pointer;
        }), i = r === "start", s = r === "end", a = t.interactable.options.deltaSource;
        if (n.touches = [o[0], o[1]], i)
          n.distance = E.touchDistance(o, a), n.box = E.touchBBox(o), n.scale = 1, n.ds = 0, n.angle = E.touchAngle(o, a), n.da = 0, t.gesture.startDistance = n.distance, t.gesture.startAngle = n.angle;
        else if (s) {
          var l = t.prevEvent;
          n.distance = l.distance, n.box = l.box, n.scale = l.scale, n.ds = 0, n.angle = l.angle, n.da = 0;
        } else
          n.distance = E.touchDistance(o, a), n.box = E.touchBBox(o), n.scale = n.distance / t.gesture.startDistance, n.angle = E.touchAngle(o, a), n.ds = n.scale - t.gesture.scale, n.da = n.angle - t.gesture.angle;
        t.gesture.distance = n.distance, t.gesture.angle = n.angle, v.default.number(n.scale) && n.scale !== 1 / 0 && !isNaN(n.scale) && (t.gesture.scale = n.scale);
      }
    }
    Object.defineProperty(Lt, "__esModule", { value: !0 }), Lt.default = void 0;
    var Tn = { id: "actions/gesture", before: ["actions/drag", "actions/resize"], install: function(e) {
      var t = e.actions, n = e.Interactable, r = e.defaults;
      n.prototype.gesturable = function(o) {
        return v.default.object(o) ? (this.options.gesture.enabled = o.enabled !== !1, this.setPerAction("gesture", o), this.setOnEvents("gesture", o), this) : v.default.bool(o) ? (this.options.gesture.enabled = o, this) : this.options.gesture;
      }, t.map.gesture = Tn, t.methodDict.gesture = "gesturable", r.actions.gesture = Tn.defaults;
    }, listeners: { "interactions:action-start": Mn, "interactions:action-move": Mn, "interactions:action-end": Mn, "interactions:new": function(e) {
      e.interaction.gesture = { angle: 0, distance: 0, scale: 1, startAngle: 0, startDistance: 0 };
    }, "auto-start:check": function(e) {
      if (!(e.interaction.pointers.length < 2)) {
        var t = e.interactable.options.gesture;
        if (t && t.enabled)
          return e.action = { name: "gesture" }, !1;
      }
    } }, defaults: {}, getCursor: function() {
      return "";
    } }, Go = Tn;
    Lt.default = Go;
    var Nt = {};
    function qo(e, t, n, r, o, i, s) {
      if (!t)
        return !1;
      if (t === !0) {
        var a = v.default.number(i.width) ? i.width : i.right - i.left, l = v.default.number(i.height) ? i.height : i.bottom - i.top;
        if (s = Math.min(s, Math.abs((e === "left" || e === "right" ? a : l) / 2)), a < 0 && (e === "left" ? e = "right" : e === "right" && (e = "left")), l < 0 && (e === "top" ? e = "bottom" : e === "bottom" && (e = "top")), e === "left")
          return n.x < (a >= 0 ? i.left : i.right) + s;
        if (e === "top")
          return n.y < (l >= 0 ? i.top : i.bottom) + s;
        if (e === "right")
          return n.x > (a >= 0 ? i.right : i.left) - s;
        if (e === "bottom")
          return n.y > (l >= 0 ? i.bottom : i.top) - s;
      }
      return !!v.default.element(r) && (v.default.element(t) ? t === r : S.matchesUpTo(r, t, o));
    }
    function ur(e) {
      var t = e.iEvent, n = e.interaction;
      if (n.prepared.name === "resize" && n.resizeAxes) {
        var r = t;
        n.interactable.options.resize.square ? (n.resizeAxes === "y" ? r.delta.x = r.delta.y : r.delta.y = r.delta.x, r.axes = "xy") : (r.axes = n.resizeAxes, n.resizeAxes === "x" ? r.delta.y = 0 : n.resizeAxes === "y" && (r.delta.x = 0));
      }
    }
    Object.defineProperty(Nt, "__esModule", { value: !0 }), Nt.default = void 0;
    var Xe = { id: "actions/resize", before: ["actions/drag"], install: function(e) {
      var t = e.actions, n = e.browser, r = e.Interactable, o = e.defaults;
      Xe.cursors = function(i) {
        return i.isIe9 ? { x: "e-resize", y: "s-resize", xy: "se-resize", top: "n-resize", left: "w-resize", bottom: "s-resize", right: "e-resize", topleft: "se-resize", bottomright: "se-resize", topright: "ne-resize", bottomleft: "ne-resize" } : { x: "ew-resize", y: "ns-resize", xy: "nwse-resize", top: "ns-resize", left: "ew-resize", bottom: "ns-resize", right: "ew-resize", topleft: "nwse-resize", bottomright: "nwse-resize", topright: "nesw-resize", bottomleft: "nesw-resize" };
      }(n), Xe.defaultMargin = n.supportsTouch || n.supportsPointerEvent ? 20 : 10, r.prototype.resizable = function(i) {
        return function(s, a, l) {
          return v.default.object(a) ? (s.options.resize.enabled = a.enabled !== !1, s.setPerAction("resize", a), s.setOnEvents("resize", a), v.default.string(a.axis) && /^x$|^y$|^xy$/.test(a.axis) ? s.options.resize.axis = a.axis : a.axis === null && (s.options.resize.axis = l.defaults.actions.resize.axis), v.default.bool(a.preserveAspectRatio) ? s.options.resize.preserveAspectRatio = a.preserveAspectRatio : v.default.bool(a.square) && (s.options.resize.square = a.square), s) : v.default.bool(a) ? (s.options.resize.enabled = a, s) : s.options.resize;
        }(this, i, e);
      }, t.map.resize = Xe, t.methodDict.resize = "resizable", o.actions.resize = Xe.defaults;
    }, listeners: { "interactions:new": function(e) {
      e.interaction.resizeAxes = "xy";
    }, "interactions:action-start": function(e) {
      (function(t) {
        var n = t.iEvent, r = t.interaction;
        if (r.prepared.name === "resize" && r.prepared.edges) {
          var o = n, i = r.rect;
          r._rects = { start: (0, P.default)({}, i), corrected: (0, P.default)({}, i), previous: (0, P.default)({}, i), delta: { left: 0, right: 0, width: 0, top: 0, bottom: 0, height: 0 } }, o.edges = r.prepared.edges, o.rect = r._rects.corrected, o.deltaRect = r._rects.delta;
        }
      })(e), ur(e);
    }, "interactions:action-move": function(e) {
      (function(t) {
        var n = t.iEvent, r = t.interaction;
        if (r.prepared.name === "resize" && r.prepared.edges) {
          var o = n, i = r.interactable.options.resize.invert, s = i === "reposition" || i === "negate", a = r.rect, l = r._rects, u = l.start, f = l.corrected, d = l.delta, h = l.previous;
          if ((0, P.default)(h, f), s) {
            if ((0, P.default)(f, a), i === "reposition") {
              if (f.top > f.bottom) {
                var g = f.top;
                f.top = f.bottom, f.bottom = g;
              }
              if (f.left > f.right) {
                var p = f.left;
                f.left = f.right, f.right = p;
              }
            }
          } else
            f.top = Math.min(a.top, u.bottom), f.bottom = Math.max(a.bottom, u.top), f.left = Math.min(a.left, u.right), f.right = Math.max(a.right, u.left);
          for (var x in f.width = f.right - f.left, f.height = f.bottom - f.top, f)
            d[x] = f[x] - h[x];
          o.edges = r.prepared.edges, o.rect = f, o.deltaRect = d;
        }
      })(e), ur(e);
    }, "interactions:action-end": function(e) {
      var t = e.iEvent, n = e.interaction;
      if (n.prepared.name === "resize" && n.prepared.edges) {
        var r = t;
        r.edges = n.prepared.edges, r.rect = n._rects.corrected, r.deltaRect = n._rects.delta;
      }
    }, "auto-start:check": function(e) {
      var t = e.interaction, n = e.interactable, r = e.element, o = e.rect, i = e.buttons;
      if (o) {
        var s = (0, P.default)({}, t.coords.cur.page), a = n.options.resize;
        if (a && a.enabled && (!t.pointerIsDown || !/mouse|pointer/.test(t.pointerType) || (i & a.mouseButtons) != 0)) {
          if (v.default.object(a.edges)) {
            var l = { left: !1, right: !1, top: !1, bottom: !1 };
            for (var u in l)
              l[u] = qo(u, a.edges[u], s, t._latestPointer.eventTarget, r, o, a.margin || Xe.defaultMargin);
            l.left = l.left && !l.right, l.top = l.top && !l.bottom, (l.left || l.right || l.top || l.bottom) && (e.action = { name: "resize", edges: l });
          } else {
            var f = a.axis !== "y" && s.x > o.right - Xe.defaultMargin, d = a.axis !== "x" && s.y > o.bottom - Xe.defaultMargin;
            (f || d) && (e.action = { name: "resize", axes: (f ? "x" : "") + (d ? "y" : "") });
          }
          return !e.action && void 0;
        }
      }
    } }, defaults: { square: !1, preserveAspectRatio: !1, axis: "xy", margin: NaN, edges: null, invert: "none" }, cursors: null, getCursor: function(e) {
      var t = e.edges, n = e.axis, r = e.name, o = Xe.cursors, i = null;
      if (n)
        i = o[r + n];
      else if (t) {
        for (var s = "", a = ["top", "bottom", "left", "right"], l = 0; l < a.length; l++) {
          var u = a[l];
          t[u] && (s += u);
        }
        i = o[s];
      }
      return i;
    }, defaultMargin: null }, Vo = Xe;
    Nt.default = Vo;
    var Ht = {};
    Object.defineProperty(Ht, "__esModule", { value: !0 }), Ht.default = void 0;
    var Uo = { id: "actions", install: function(e) {
      e.usePlugin(Lt.default), e.usePlugin(Nt.default), e.usePlugin(le.default), e.usePlugin($t.default);
    } };
    Ht.default = Uo;
    var je = {};
    Object.defineProperty(je, "__esModule", { value: !0 }), je.default = void 0;
    var Ye, nt, cr = 0, Ko = { request: function(e) {
      return Ye(e);
    }, cancel: function(e) {
      return nt(e);
    }, init: function(e) {
      if (Ye = e.requestAnimationFrame, nt = e.cancelAnimationFrame, !Ye)
        for (var t = ["ms", "moz", "webkit", "o"], n = 0; n < t.length; n++) {
          var r = t[n];
          Ye = e["".concat(r, "RequestAnimationFrame")], nt = e["".concat(r, "CancelAnimationFrame")] || e["".concat(r, "CancelRequestAnimationFrame")];
        }
      Ye = Ye && Ye.bind(e), nt = nt && nt.bind(e), Ye || (Ye = function(o) {
        var i = Date.now(), s = Math.max(0, 16 - (i - cr)), a = e.setTimeout(function() {
          o(i + s);
        }, s);
        return cr = i + s, a;
      }, nt = function(o) {
        return clearTimeout(o);
      });
    } };
    je.default = Ko;
    var Je = {};
    Object.defineProperty(Je, "__esModule", { value: !0 }), Je.getContainer = Gt, Je.getScroll = Ot, Je.getScrollSize = function(e) {
      return v.default.window(e) && (e = window.document.body), { x: e.scrollWidth, y: e.scrollHeight };
    }, Je.getScrollSizeDelta = function(e, t) {
      var n = e.interaction, r = e.element, o = n && n.interactable.options[n.prepared.name].autoScroll;
      if (!o || !o.enabled)
        return t(), { x: 0, y: 0 };
      var i = Gt(o.container, n.interactable, r), s = Ot(i);
      t();
      var a = Ot(i);
      return { x: a.x - s.x, y: a.y - s.y };
    }, Je.default = void 0;
    var C = { defaults: { enabled: !1, margin: 60, container: null, speed: 300 }, now: Date.now, interaction: null, i: 0, x: 0, y: 0, isScrolling: !1, prevTime: 0, margin: 0, speed: 0, start: function(e) {
      C.isScrolling = !0, je.default.cancel(C.i), e.autoScroll = C, C.interaction = e, C.prevTime = C.now(), C.i = je.default.request(C.scroll);
    }, stop: function() {
      C.isScrolling = !1, C.interaction && (C.interaction.autoScroll = null), je.default.cancel(C.i);
    }, scroll: function() {
      var e = C.interaction, t = e.interactable, n = e.element, r = e.prepared.name, o = t.options[r].autoScroll, i = Gt(o.container, t, n), s = C.now(), a = (s - C.prevTime) / 1e3, l = o.speed * a;
      if (l >= 1) {
        var u = { x: C.x * l, y: C.y * l };
        if (u.x || u.y) {
          var f = Ot(i);
          v.default.window(i) ? i.scrollBy(u.x, u.y) : i && (i.scrollLeft += u.x, i.scrollTop += u.y);
          var d = Ot(i), h = { x: d.x - f.x, y: d.y - f.y };
          (h.x || h.y) && t.fire({ type: "autoscroll", target: n, interactable: t, delta: h, interaction: e, container: i });
        }
        C.prevTime = s;
      }
      C.isScrolling && (je.default.cancel(C.i), C.i = je.default.request(C.scroll));
    }, check: function(e, t) {
      var n;
      return (n = e.options[t].autoScroll) == null ? void 0 : n.enabled;
    }, onInteractionMove: function(e) {
      var t = e.interaction, n = e.pointer;
      if (t.interacting() && C.check(t.interactable, t.prepared.name))
        if (t.simulation)
          C.x = C.y = 0;
        else {
          var r, o, i, s, a = t.interactable, l = t.element, u = t.prepared.name, f = a.options[u].autoScroll, d = Gt(f.container, a, l);
          if (v.default.window(d))
            s = n.clientX < C.margin, r = n.clientY < C.margin, o = n.clientX > d.innerWidth - C.margin, i = n.clientY > d.innerHeight - C.margin;
          else {
            var h = S.getElementClientRect(d);
            s = n.clientX < h.left + C.margin, r = n.clientY < h.top + C.margin, o = n.clientX > h.right - C.margin, i = n.clientY > h.bottom - C.margin;
          }
          C.x = o ? 1 : s ? -1 : 0, C.y = i ? 1 : r ? -1 : 0, C.isScrolling || (C.margin = f.margin, C.speed = f.speed, C.start(t));
        }
    } };
    function Gt(e, t, n) {
      return (v.default.string(e) ? (0, B.getStringOptionResult)(e, t, n) : e) || (0, b.getWindow)(n);
    }
    function Ot(e) {
      return v.default.window(e) && (e = window.document.body), { x: e.scrollLeft, y: e.scrollTop };
    }
    var Zo = { id: "auto-scroll", install: function(e) {
      var t = e.defaults, n = e.actions;
      e.autoScroll = C, C.now = function() {
        return e.now();
      }, n.phaselessTypes.autoscroll = !0, t.perAction.autoScroll = C.defaults;
    }, listeners: { "interactions:new": function(e) {
      e.interaction.autoScroll = null;
    }, "interactions:destroy": function(e) {
      e.interaction.autoScroll = null, C.stop(), C.interaction && (C.interaction = null);
    }, "interactions:stop": C.stop, "interactions:action-move": function(e) {
      return C.onInteractionMove(e);
    } } };
    Je.default = Zo;
    var _e = {};
    Object.defineProperty(_e, "__esModule", { value: !0 }), _e.warnOnce = function(e, t) {
      var n = !1;
      return function() {
        return n || (b.window.console.warn(t), n = !0), e.apply(this, arguments);
      };
    }, _e.copyAction = function(e, t) {
      return e.name = t.name, e.axis = t.axis, e.edges = t.edges, e;
    }, _e.sign = void 0, _e.sign = function(e) {
      return e >= 0 ? 1 : -1;
    };
    var qt = {};
    function Jo(e) {
      return v.default.bool(e) ? (this.options.styleCursor = e, this) : e === null ? (delete this.options.styleCursor, this) : this.options.styleCursor;
    }
    function Qo(e) {
      return v.default.func(e) ? (this.options.actionChecker = e, this) : e === null ? (delete this.options.actionChecker, this) : this.options.actionChecker;
    }
    Object.defineProperty(qt, "__esModule", { value: !0 }), qt.default = void 0;
    var ei = { id: "auto-start/interactableMethods", install: function(e) {
      var t = e.Interactable;
      t.prototype.getAction = function(n, r, o, i) {
        var s = function(a, l, u, f, d) {
          var h = a.getRect(f), g = { action: null, interactable: a, interaction: u, element: f, rect: h, buttons: l.buttons || { 0: 1, 1: 4, 3: 8, 4: 16 }[l.button] };
          return d.fire("auto-start:check", g), g.action;
        }(this, r, o, i, e);
        return this.options.actionChecker ? this.options.actionChecker(n, r, s, this, i, o) : s;
      }, t.prototype.ignoreFrom = (0, _e.warnOnce)(function(n) {
        return this._backCompatOption("ignoreFrom", n);
      }, "Interactable.ignoreFrom() has been deprecated. Use Interactble.draggable({ignoreFrom: newValue})."), t.prototype.allowFrom = (0, _e.warnOnce)(function(n) {
        return this._backCompatOption("allowFrom", n);
      }, "Interactable.allowFrom() has been deprecated. Use Interactble.draggable({allowFrom: newValue})."), t.prototype.actionChecker = Qo, t.prototype.styleCursor = Jo;
    } };
    qt.default = ei;
    var ft = {};
    function fr(e, t, n, r, o) {
      return t.testIgnoreAllow(t.options[e.name], n, r) && t.options[e.name].enabled && Vt(t, n, e, o) ? e : null;
    }
    function ti(e, t, n, r, o, i, s) {
      for (var a = 0, l = r.length; a < l; a++) {
        var u = r[a], f = o[a], d = u.getAction(t, n, e, f);
        if (d) {
          var h = fr(d, u, f, i, s);
          if (h)
            return { action: h, interactable: u, element: f };
        }
      }
      return { action: null, interactable: null, element: null };
    }
    function dr(e, t, n, r, o) {
      var i = [], s = [], a = r;
      function l(f) {
        i.push(f), s.push(a);
      }
      for (; v.default.element(a); ) {
        i = [], s = [], o.interactables.forEachMatch(a, l);
        var u = ti(e, t, n, i, s, r, o);
        if (u.action && !u.interactable.options[u.action.name].manualStart)
          return u;
        a = S.parentNode(a);
      }
      return { action: null, interactable: null, element: null };
    }
    function pr(e, t, n) {
      var r = t.action, o = t.interactable, i = t.element;
      r = r || { name: null }, e.interactable = o, e.element = i, (0, _e.copyAction)(e.prepared, r), e.rect = o && r.name ? o.getRect(i) : null, hr(e, n), n.fire("autoStart:prepared", { interaction: e });
    }
    function Vt(e, t, n, r) {
      var o = e.options, i = o[n.name].max, s = o[n.name].maxPerElement, a = r.autoStart.maxInteractions, l = 0, u = 0, f = 0;
      if (!(i && s && a))
        return !1;
      for (var d = 0; d < r.interactions.list.length; d++) {
        var h = r.interactions.list[d], g = h.prepared.name;
        if (h.interacting() && (++l >= a || h.interactable === e && ((u += g === n.name ? 1 : 0) >= i || h.element === t && (f++, g === n.name && f >= s))))
          return !1;
      }
      return a > 0;
    }
    function vr(e, t) {
      return v.default.number(e) ? (t.autoStart.maxInteractions = e, this) : t.autoStart.maxInteractions;
    }
    function jn(e, t, n) {
      var r = n.autoStart.cursorElement;
      r && r !== e && (r.style.cursor = ""), e.ownerDocument.documentElement.style.cursor = t, e.style.cursor = t, n.autoStart.cursorElement = t ? e : null;
    }
    function hr(e, t) {
      var n = e.interactable, r = e.element, o = e.prepared;
      if (e.pointerType === "mouse" && n && n.options.styleCursor) {
        var i = "";
        if (o.name) {
          var s = n.options[o.name].cursorChecker;
          i = v.default.func(s) ? s(o, n, r, e._interacting) : t.actions.map[o.name].getCursor(o);
        }
        jn(e.element, i || "", t);
      } else
        t.autoStart.cursorElement && jn(t.autoStart.cursorElement, "", t);
    }
    Object.defineProperty(ft, "__esModule", { value: !0 }), ft.default = void 0;
    var ni = { id: "auto-start/base", before: ["actions"], install: function(e) {
      var t = e.interactStatic, n = e.defaults;
      e.usePlugin(qt.default), n.base.actionChecker = null, n.base.styleCursor = !0, (0, P.default)(n.perAction, { manualStart: !1, max: 1 / 0, maxPerElement: 1, allowFrom: null, ignoreFrom: null, mouseButtons: 1 }), t.maxInteractions = function(r) {
        return vr(r, e);
      }, e.autoStart = { maxInteractions: 1 / 0, withinInteractionLimit: Vt, cursorElement: null };
    }, listeners: { "interactions:down": function(e, t) {
      var n = e.interaction, r = e.pointer, o = e.event, i = e.eventTarget;
      n.interacting() || pr(n, dr(n, r, o, i, t), t);
    }, "interactions:move": function(e, t) {
      (function(n, r) {
        var o = n.interaction, i = n.pointer, s = n.event, a = n.eventTarget;
        o.pointerType !== "mouse" || o.pointerIsDown || o.interacting() || pr(o, dr(o, i, s, a, r), r);
      })(e, t), function(n, r) {
        var o = n.interaction;
        if (o.pointerIsDown && !o.interacting() && o.pointerWasMoved && o.prepared.name) {
          r.fire("autoStart:before-start", n);
          var i = o.interactable, s = o.prepared.name;
          s && i && (i.options[s].manualStart || !Vt(i, o.element, o.prepared, r) ? o.stop() : (o.start(o.prepared, i, o.element), hr(o, r)));
        }
      }(e, t);
    }, "interactions:stop": function(e, t) {
      var n = e.interaction, r = n.interactable;
      r && r.options.styleCursor && jn(n.element, "", t);
    } }, maxInteractions: vr, withinInteractionLimit: Vt, validateAction: fr };
    ft.default = ni;
    var Ut = {};
    Object.defineProperty(Ut, "__esModule", { value: !0 }), Ut.default = void 0;
    var ri = { id: "auto-start/dragAxis", listeners: { "autoStart:before-start": function(e, t) {
      var n = e.interaction, r = e.eventTarget, o = e.dx, i = e.dy;
      if (n.prepared.name === "drag") {
        var s = Math.abs(o), a = Math.abs(i), l = n.interactable.options.drag, u = l.startAxis, f = s > a ? "x" : s < a ? "y" : "xy";
        if (n.prepared.axis = l.lockAxis === "start" ? f[0] : l.lockAxis, f !== "xy" && u !== "xy" && u !== f) {
          n.prepared.name = null;
          for (var d = r, h = function(p) {
            if (p !== n.interactable) {
              var x = n.interactable.options.drag;
              if (!x.manualStart && p.testIgnoreAllow(x, d, r)) {
                var _ = p.getAction(n.downPointer, n.downEvent, n, d);
                if (_ && _.name === "drag" && function(w, T) {
                  if (!T)
                    return !1;
                  var D = T.options.drag.startAxis;
                  return w === "xy" || D === "xy" || D === w;
                }(f, p) && ft.default.validateAction(_, p, d, r, t))
                  return p;
              }
            }
          }; v.default.element(d); ) {
            var g = t.interactables.forEachMatch(d, h);
            if (g) {
              n.prepared.name = "drag", n.interactable = g, n.element = d;
              break;
            }
            d = (0, S.parentNode)(d);
          }
        }
      }
    } } };
    Ut.default = ri;
    var Kt = {};
    function kn(e) {
      var t = e.prepared && e.prepared.name;
      if (!t)
        return null;
      var n = e.interactable.options;
      return n[t].hold || n[t].delay;
    }
    Object.defineProperty(Kt, "__esModule", { value: !0 }), Kt.default = void 0;
    var oi = { id: "auto-start/hold", install: function(e) {
      var t = e.defaults;
      e.usePlugin(ft.default), t.perAction.hold = 0, t.perAction.delay = 0;
    }, listeners: { "interactions:new": function(e) {
      e.interaction.autoStartHoldTimer = null;
    }, "autoStart:prepared": function(e) {
      var t = e.interaction, n = kn(t);
      n > 0 && (t.autoStartHoldTimer = setTimeout(function() {
        t.start(t.prepared, t.interactable, t.element);
      }, n));
    }, "interactions:move": function(e) {
      var t = e.interaction, n = e.duplicate;
      t.autoStartHoldTimer && t.pointerWasMoved && !n && (clearTimeout(t.autoStartHoldTimer), t.autoStartHoldTimer = null);
    }, "autoStart:before-start": function(e) {
      var t = e.interaction;
      kn(t) > 0 && (t.prepared.name = null);
    } }, getHoldDuration: kn };
    Kt.default = oi;
    var Zt = {};
    Object.defineProperty(Zt, "__esModule", { value: !0 }), Zt.default = void 0;
    var ii = { id: "auto-start", install: function(e) {
      e.usePlugin(ft.default), e.usePlugin(Kt.default), e.usePlugin(Ut.default);
    } };
    Zt.default = ii;
    var dt = {};
    function ai(e) {
      return /^(always|never|auto)$/.test(e) ? (this.options.preventDefault = e, this) : v.default.bool(e) ? (this.options.preventDefault = e ? "always" : "never", this) : this.options.preventDefault;
    }
    function si(e) {
      var t = e.interaction, n = e.event;
      t.interactable && t.interactable.checkAndPreventDefault(n);
    }
    function gr(e) {
      var t = e.Interactable;
      t.prototype.preventDefault = ai, t.prototype.checkAndPreventDefault = function(n) {
        return function(r, o, i) {
          var s = r.options.preventDefault;
          if (s !== "never")
            if (s !== "always") {
              if (o.events.supportsPassive && /^touch(start|move)$/.test(i.type)) {
                var a = (0, b.getWindow)(i.target).document, l = o.getDocOptions(a);
                if (!l || !l.events || l.events.passive !== !1)
                  return;
              }
              /^(mouse|pointer|touch)*(down|start)/i.test(i.type) || v.default.element(i.target) && (0, S.matchesSelector)(i.target, "input,select,textarea,[contenteditable=true],[contenteditable=true] *") || i.preventDefault();
            } else
              i.preventDefault();
        }(this, e, n);
      }, e.interactions.docEvents.push({ type: "dragstart", listener: function(n) {
        for (var r = 0; r < e.interactions.list.length; r++) {
          var o = e.interactions.list[r];
          if (o.element && (o.element === n.target || (0, S.nodeContains)(o.element, n.target)))
            return void o.interactable.checkAndPreventDefault(n);
        }
      } });
    }
    Object.defineProperty(dt, "__esModule", { value: !0 }), dt.install = gr, dt.default = void 0;
    var li = { id: "core/interactablePreventDefault", install: gr, listeners: ["down", "move", "up", "cancel"].reduce(function(e, t) {
      return e["interactions:".concat(t)] = si, e;
    }, {}) };
    dt.default = li;
    var In = {};
    Object.defineProperty(In, "__esModule", { value: !0 }), In.default = void 0, In.default = {};
    var Pt, zn = {};
    Object.defineProperty(zn, "__esModule", { value: !0 }), zn.default = void 0, function(e) {
      e.touchAction = "touchAction", e.boxSizing = "boxSizing", e.noListeners = "noListeners";
    }(Pt || (Pt = {})), Pt.touchAction, Pt.boxSizing, Pt.noListeners;
    var ui = { id: "dev-tools", install: function() {
    } };
    zn.default = ui;
    var rt = {};
    Object.defineProperty(rt, "__esModule", { value: !0 }), rt.default = function e(t) {
      var n = {};
      for (var r in t) {
        var o = t[r];
        v.default.plainObject(o) ? n[r] = e(o) : v.default.array(o) ? n[r] = I.from(o) : n[r] = o;
      }
      return n;
    };
    var ot = {};
    function yr(e, t) {
      return function(n) {
        if (Array.isArray(n))
          return n;
      }(e) || function(n, r) {
        if (typeof Symbol < "u" && Symbol.iterator in Object(n)) {
          var o = [], i = !0, s = !1, a = void 0;
          try {
            for (var l, u = n[Symbol.iterator](); !(i = (l = u.next()).done) && (o.push(l.value), !r || o.length !== r); i = !0)
              ;
          } catch (f) {
            s = !0, a = f;
          } finally {
            try {
              i || u.return == null || u.return();
            } finally {
              if (s)
                throw a;
            }
          }
          return o;
        }
      }(e, t) || function(n, r) {
        if (n) {
          if (typeof n == "string")
            return mr(n, r);
          var o = Object.prototype.toString.call(n).slice(8, -1);
          return o === "Object" && n.constructor && (o = n.constructor.name), o === "Map" || o === "Set" ? Array.from(n) : o === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(o) ? mr(n, r) : void 0;
        }
      }(e, t) || function() {
        throw new TypeError(`Invalid attempt to destructure non-iterable instance.
In order to be iterable, non-array objects must have a [Symbol.iterator]() method.`);
      }();
    }
    function mr(e, t) {
      (t == null || t > e.length) && (t = e.length);
      for (var n = 0, r = Array(t); n < t; n++)
        r[n] = e[n];
      return r;
    }
    function ci(e, t) {
      for (var n = 0; n < t.length; n++) {
        var r = t[n];
        r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), Object.defineProperty(e, r.key, r);
      }
    }
    function it(e, t, n) {
      return t in e ? Object.defineProperty(e, t, { value: n, enumerable: !0, configurable: !0, writable: !0 }) : e[t] = n, e;
    }
    Object.defineProperty(ot, "__esModule", { value: !0 }), ot.getRectOffset = br, ot.default = void 0;
    var fi = function() {
      function e(r) {
        (function(o, i) {
          if (!(o instanceof i))
            throw new TypeError("Cannot call a class as a function");
        })(this, e), it(this, "states", []), it(this, "startOffset", { left: 0, right: 0, top: 0, bottom: 0 }), it(this, "startDelta", void 0), it(this, "result", void 0), it(this, "endResult", void 0), it(this, "edges", void 0), it(this, "interaction", void 0), this.interaction = r, this.result = Jt();
      }
      var t, n;
      return t = e, (n = [{ key: "start", value: function(r, o) {
        var i = r.phase, s = this.interaction, a = function(u) {
          var f = u.interactable.options[u.prepared.name], d = f.modifiers;
          return d && d.length ? d : ["snap", "snapSize", "snapEdges", "restrict", "restrictEdges", "restrictSize"].map(function(h) {
            var g = f[h];
            return g && g.enabled && { options: g, methods: g._methods };
          }).filter(function(h) {
            return !!h;
          });
        }(s);
        this.prepareStates(a), this.edges = (0, P.default)({}, s.edges), this.startOffset = br(s.rect, o), this.startDelta = { x: 0, y: 0 };
        var l = this.fillArg({ phase: i, pageCoords: o, preEnd: !1 });
        return this.result = Jt(), this.startAll(l), this.result = this.setAll(l);
      } }, { key: "fillArg", value: function(r) {
        var o = this.interaction;
        return r.interaction = o, r.interactable = o.interactable, r.element = o.element, r.rect = r.rect || o.rect, r.edges = this.edges, r.startOffset = this.startOffset, r;
      } }, { key: "startAll", value: function(r) {
        for (var o = 0; o < this.states.length; o++) {
          var i = this.states[o];
          i.methods.start && (r.state = i, i.methods.start(r));
        }
      } }, { key: "setAll", value: function(r) {
        var o = r.phase, i = r.preEnd, s = r.skipModifiers, a = r.rect;
        r.coords = (0, P.default)({}, r.pageCoords), r.rect = (0, P.default)({}, a);
        for (var l = s ? this.states.slice(s) : this.states, u = Jt(r.coords, r.rect), f = 0; f < l.length; f++) {
          var d, h = l[f], g = h.options, p = (0, P.default)({}, r.coords), x = null;
          (d = h.methods) != null && d.set && this.shouldDo(g, i, o) && (r.state = h, x = h.methods.set(r), B.addEdges(this.interaction.edges, r.rect, { x: r.coords.x - p.x, y: r.coords.y - p.y })), u.eventProps.push(x);
        }
        u.delta.x = r.coords.x - r.pageCoords.x, u.delta.y = r.coords.y - r.pageCoords.y, u.rectDelta.left = r.rect.left - a.left, u.rectDelta.right = r.rect.right - a.right, u.rectDelta.top = r.rect.top - a.top, u.rectDelta.bottom = r.rect.bottom - a.bottom;
        var _ = this.result.coords, w = this.result.rect;
        if (_ && w) {
          var T = u.rect.left !== w.left || u.rect.right !== w.right || u.rect.top !== w.top || u.rect.bottom !== w.bottom;
          u.changed = T || _.x !== u.coords.x || _.y !== u.coords.y;
        }
        return u;
      } }, { key: "applyToInteraction", value: function(r) {
        var o = this.interaction, i = r.phase, s = o.coords.cur, a = o.coords.start, l = this.result, u = this.startDelta, f = l.delta;
        i === "start" && (0, P.default)(this.startDelta, l.delta);
        for (var d = 0; d < [[a, u], [s, f]].length; d++) {
          var h = yr([[a, u], [s, f]][d], 2), g = h[0], p = h[1];
          g.page.x += p.x, g.page.y += p.y, g.client.x += p.x, g.client.y += p.y;
        }
        var x = this.result.rectDelta, _ = r.rect || o.rect;
        _.left += x.left, _.right += x.right, _.top += x.top, _.bottom += x.bottom, _.width = _.right - _.left, _.height = _.bottom - _.top;
      } }, { key: "setAndApply", value: function(r) {
        var o = this.interaction, i = r.phase, s = r.preEnd, a = r.skipModifiers, l = this.setAll(this.fillArg({ preEnd: s, phase: i, pageCoords: r.modifiedCoords || o.coords.cur.page }));
        if (this.result = l, !l.changed && (!a || a < this.states.length) && o.interacting())
          return !1;
        if (r.modifiedCoords) {
          var u = o.coords.cur.page, f = { x: r.modifiedCoords.x - u.x, y: r.modifiedCoords.y - u.y };
          l.coords.x += f.x, l.coords.y += f.y, l.delta.x += f.x, l.delta.y += f.y;
        }
        this.applyToInteraction(r);
      } }, { key: "beforeEnd", value: function(r) {
        var o = r.interaction, i = r.event, s = this.states;
        if (s && s.length) {
          for (var a = !1, l = 0; l < s.length; l++) {
            var u = s[l];
            r.state = u;
            var f = u.options, d = u.methods, h = d.beforeEnd && d.beforeEnd(r);
            if (h)
              return this.endResult = h, !1;
            a = a || !a && this.shouldDo(f, !0, r.phase, !0);
          }
          a && o.move({ event: i, preEnd: !0 });
        }
      } }, { key: "stop", value: function(r) {
        var o = r.interaction;
        if (this.states && this.states.length) {
          var i = (0, P.default)({ states: this.states, interactable: o.interactable, element: o.element, rect: null }, r);
          this.fillArg(i);
          for (var s = 0; s < this.states.length; s++) {
            var a = this.states[s];
            i.state = a, a.methods.stop && a.methods.stop(i);
          }
          this.states = null, this.endResult = null;
        }
      } }, { key: "prepareStates", value: function(r) {
        this.states = [];
        for (var o = 0; o < r.length; o++) {
          var i = r[o], s = i.options, a = i.methods, l = i.name;
          this.states.push({ options: s, methods: a, index: o, name: l });
        }
        return this.states;
      } }, { key: "restoreInteractionCoords", value: function(r) {
        var o = r.interaction, i = o.coords, s = o.rect, a = o.modification;
        if (a.result) {
          for (var l = a.startDelta, u = a.result, f = u.delta, d = u.rectDelta, h = [[i.start, l], [i.cur, f]], g = 0; g < h.length; g++) {
            var p = yr(h[g], 2), x = p[0], _ = p[1];
            x.page.x -= _.x, x.page.y -= _.y, x.client.x -= _.x, x.client.y -= _.y;
          }
          s.left -= d.left, s.right -= d.right, s.top -= d.top, s.bottom -= d.bottom;
        }
      } }, { key: "shouldDo", value: function(r, o, i, s) {
        return !(!r || r.enabled === !1 || s && !r.endOnly || r.endOnly && !o || i === "start" && !r.setStart);
      } }, { key: "copyFrom", value: function(r) {
        this.startOffset = r.startOffset, this.startDelta = r.startDelta, this.edges = r.edges, this.states = r.states.map(function(o) {
          return (0, rt.default)(o);
        }), this.result = Jt((0, P.default)({}, r.result.coords), (0, P.default)({}, r.result.rect));
      } }, { key: "destroy", value: function() {
        for (var r in this)
          this[r] = null;
      } }]) && ci(t.prototype, n), e;
    }();
    function Jt(e, t) {
      return { rect: t, coords: e, delta: { x: 0, y: 0 }, rectDelta: { left: 0, right: 0, top: 0, bottom: 0 }, eventProps: [], changed: !0 };
    }
    function br(e, t) {
      return e ? { left: t.x - e.left, top: t.y - e.top, right: e.right - t.x, bottom: e.bottom - t.y } : { left: 0, top: 0, right: 0, bottom: 0 };
    }
    ot.default = fi;
    var fe = {};
    function Qt(e) {
      var t = e.iEvent, n = e.interaction.modification.result;
      n && (t.modifiers = n.eventProps);
    }
    Object.defineProperty(fe, "__esModule", { value: !0 }), fe.makeModifier = function(e, t) {
      var n = e.defaults, r = { start: e.start, set: e.set, beforeEnd: e.beforeEnd, stop: e.stop }, o = function(i) {
        var s = i || {};
        for (var a in s.enabled = s.enabled !== !1, n)
          a in s || (s[a] = n[a]);
        var l = { options: s, methods: r, name: t, enable: function() {
          return s.enabled = !0, l;
        }, disable: function() {
          return s.enabled = !1, l;
        } };
        return l;
      };
      return t && typeof t == "string" && (o._defaults = n, o._methods = r), o;
    }, fe.addEventModifiers = Qt, fe.default = void 0;
    var di = { id: "modifiers/base", before: ["actions"], install: function(e) {
      e.defaults.perAction.modifiers = [];
    }, listeners: { "interactions:new": function(e) {
      var t = e.interaction;
      t.modification = new ot.default(t);
    }, "interactions:before-action-start": function(e) {
      var t = e.interaction.modification;
      t.start(e, e.interaction.coords.start.page), e.interaction.edges = t.edges, t.applyToInteraction(e);
    }, "interactions:before-action-move": function(e) {
      return e.interaction.modification.setAndApply(e);
    }, "interactions:before-action-end": function(e) {
      return e.interaction.modification.beforeEnd(e);
    }, "interactions:action-start": Qt, "interactions:action-move": Qt, "interactions:action-end": Qt, "interactions:after-action-start": function(e) {
      return e.interaction.modification.restoreInteractionCoords(e);
    }, "interactions:after-action-move": function(e) {
      return e.interaction.modification.restoreInteractionCoords(e);
    }, "interactions:stop": function(e) {
      return e.interaction.modification.stop(e);
    } } };
    fe.default = di;
    var St = {};
    Object.defineProperty(St, "__esModule", { value: !0 }), St.defaults = void 0, St.defaults = { base: { preventDefault: "auto", deltaSource: "page" }, perAction: { enabled: !1, origin: { x: 0, y: 0 } }, actions: {} };
    var Et = {};
    function xr(e) {
      return (xr = typeof Symbol == "function" && typeof Symbol.iterator == "symbol" ? function(t) {
        return typeof t;
      } : function(t) {
        return t && typeof Symbol == "function" && t.constructor === Symbol && t !== Symbol.prototype ? "symbol" : typeof t;
      })(e);
    }
    function pi(e, t) {
      for (var n = 0; n < t.length; n++) {
        var r = t[n];
        r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), Object.defineProperty(e, r.key, r);
      }
    }
    function wr(e, t) {
      return (wr = Object.setPrototypeOf || function(n, r) {
        return n.__proto__ = r, n;
      })(e, t);
    }
    function vi(e, t) {
      return !t || xr(t) !== "object" && typeof t != "function" ? G(e) : t;
    }
    function G(e) {
      if (e === void 0)
        throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
      return e;
    }
    function Dn(e) {
      return (Dn = Object.setPrototypeOf ? Object.getPrototypeOf : function(t) {
        return t.__proto__ || Object.getPrototypeOf(t);
      })(e);
    }
    function V(e, t, n) {
      return t in e ? Object.defineProperty(e, t, { value: n, enumerable: !0, configurable: !0, writable: !0 }) : e[t] = n, e;
    }
    Object.defineProperty(Et, "__esModule", { value: !0 }), Et.InteractEvent = void 0;
    var _r = function(e) {
      (function(a, l) {
        if (typeof l != "function" && l !== null)
          throw new TypeError("Super expression must either be null or a function");
        a.prototype = Object.create(l && l.prototype, { constructor: { value: a, writable: !0, configurable: !0 } }), l && wr(a, l);
      })(s, e);
      var t, n, r, o, i = (r = s, o = function() {
        if (typeof Reflect > "u" || !Reflect.construct || Reflect.construct.sham)
          return !1;
        if (typeof Proxy == "function")
          return !0;
        try {
          return Boolean.prototype.valueOf.call(Reflect.construct(Boolean, [], function() {
          })), !0;
        } catch {
          return !1;
        }
      }(), function() {
        var a, l = Dn(r);
        if (o) {
          var u = Dn(this).constructor;
          a = Reflect.construct(l, arguments, u);
        } else
          a = l.apply(this, arguments);
        return vi(this, a);
      });
      function s(a, l, u, f, d, h, g) {
        var p;
        (function(ee, W) {
          if (!(ee instanceof W))
            throw new TypeError("Cannot call a class as a function");
        })(this, s), V(G(p = i.call(this, a)), "target", void 0), V(G(p), "currentTarget", void 0), V(G(p), "relatedTarget", null), V(G(p), "screenX", void 0), V(G(p), "screenY", void 0), V(G(p), "button", void 0), V(G(p), "buttons", void 0), V(G(p), "ctrlKey", void 0), V(G(p), "shiftKey", void 0), V(G(p), "altKey", void 0), V(G(p), "metaKey", void 0), V(G(p), "page", void 0), V(G(p), "client", void 0), V(G(p), "delta", void 0), V(G(p), "rect", void 0), V(G(p), "x0", void 0), V(G(p), "y0", void 0), V(G(p), "t0", void 0), V(G(p), "dt", void 0), V(G(p), "duration", void 0), V(G(p), "clientX0", void 0), V(G(p), "clientY0", void 0), V(G(p), "velocity", void 0), V(G(p), "speed", void 0), V(G(p), "swipe", void 0), V(G(p), "timeStamp", void 0), V(G(p), "axes", void 0), V(G(p), "preEnd", void 0), d = d || a.element;
        var x = a.interactable, _ = (x && x.options || St.defaults).deltaSource, w = (0, Te.default)(x, d, u), T = f === "start", D = f === "end", A = T ? G(p) : a.prevEvent, N = T ? a.coords.start : D ? { page: A.page, client: A.client, timeStamp: a.coords.cur.timeStamp } : a.coords.cur;
        return p.page = (0, P.default)({}, N.page), p.client = (0, P.default)({}, N.client), p.rect = (0, P.default)({}, a.rect), p.timeStamp = N.timeStamp, D || (p.page.x -= w.x, p.page.y -= w.y, p.client.x -= w.x, p.client.y -= w.y), p.ctrlKey = l.ctrlKey, p.altKey = l.altKey, p.shiftKey = l.shiftKey, p.metaKey = l.metaKey, p.button = l.button, p.buttons = l.buttons, p.target = d, p.currentTarget = d, p.preEnd = h, p.type = g || u + (f || ""), p.interactable = x, p.t0 = T ? a.pointers[a.pointers.length - 1].downTime : A.t0, p.x0 = a.coords.start.page.x - w.x, p.y0 = a.coords.start.page.y - w.y, p.clientX0 = a.coords.start.client.x - w.x, p.clientY0 = a.coords.start.client.y - w.y, p.delta = T || D ? { x: 0, y: 0 } : { x: p[_].x - A[_].x, y: p[_].y - A[_].y }, p.dt = a.coords.delta.timeStamp, p.duration = p.timeStamp - p.t0, p.velocity = (0, P.default)({}, a.coords.velocity[_]), p.speed = (0, De.default)(p.velocity.x, p.velocity.y), p.swipe = D || f === "inertiastart" ? p.getSwipe() : null, p;
      }
      return t = s, (n = [{ key: "getSwipe", value: function() {
        var a = this._interaction;
        if (a.prevEvent.speed < 600 || this.timeStamp - a.prevEvent.timeStamp > 150)
          return null;
        var l = 180 * Math.atan2(a.prevEvent.velocityY, a.prevEvent.velocityX) / Math.PI;
        l < 0 && (l += 360);
        var u = 112.5 <= l && l < 247.5, f = 202.5 <= l && l < 337.5;
        return { up: f, down: !f && 22.5 <= l && l < 157.5, left: u, right: !u && (292.5 <= l || l < 67.5), angle: l, speed: a.prevEvent.speed, velocity: { x: a.prevEvent.velocityX, y: a.prevEvent.velocityY } };
      } }, { key: "preventDefault", value: function() {
      } }, { key: "stopImmediatePropagation", value: function() {
        this.immediatePropagationStopped = this.propagationStopped = !0;
      } }, { key: "stopPropagation", value: function() {
        this.propagationStopped = !0;
      } }]) && pi(t.prototype, n), s;
    }(Ke.BaseEvent);
    Et.InteractEvent = _r, Object.defineProperties(_r.prototype, { pageX: { get: function() {
      return this.page.x;
    }, set: function(e) {
      this.page.x = e;
    } }, pageY: { get: function() {
      return this.page.y;
    }, set: function(e) {
      this.page.y = e;
    } }, clientX: { get: function() {
      return this.client.x;
    }, set: function(e) {
      this.client.x = e;
    } }, clientY: { get: function() {
      return this.client.y;
    }, set: function(e) {
      this.client.y = e;
    } }, dx: { get: function() {
      return this.delta.x;
    }, set: function(e) {
      this.delta.x = e;
    } }, dy: { get: function() {
      return this.delta.y;
    }, set: function(e) {
      this.delta.y = e;
    } }, velocityX: { get: function() {
      return this.velocity.x;
    }, set: function(e) {
      this.velocity.x = e;
    } }, velocityY: { get: function() {
      return this.velocity.y;
    }, set: function(e) {
      this.velocity.y = e;
    } } });
    var Mt = {};
    function Tt(e, t, n) {
      return t in e ? Object.defineProperty(e, t, { value: n, enumerable: !0, configurable: !0, writable: !0 }) : e[t] = n, e;
    }
    Object.defineProperty(Mt, "__esModule", { value: !0 }), Mt.PointerInfo = void 0, Mt.PointerInfo = function e(t, n, r, o, i) {
      (function(s, a) {
        if (!(s instanceof a))
          throw new TypeError("Cannot call a class as a function");
      })(this, e), Tt(this, "id", void 0), Tt(this, "pointer", void 0), Tt(this, "event", void 0), Tt(this, "downTime", void 0), Tt(this, "downTarget", void 0), this.id = t, this.pointer = n, this.event = r, this.downTime = o, this.downTarget = i;
    };
    var en, tn, me = {};
    function hi(e, t) {
      for (var n = 0; n < t.length; n++) {
        var r = t[n];
        r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), Object.defineProperty(e, r.key, r);
      }
    }
    function ne(e, t, n) {
      return t in e ? Object.defineProperty(e, t, { value: n, enumerable: !0, configurable: !0, writable: !0 }) : e[t] = n, e;
    }
    Object.defineProperty(me, "__esModule", { value: !0 }), Object.defineProperty(me, "PointerInfo", { enumerable: !0, get: function() {
      return Mt.PointerInfo;
    } }), me.default = me.Interaction = me._ProxyMethods = me._ProxyValues = void 0, me._ProxyValues = en, function(e) {
      e.interactable = "", e.element = "", e.prepared = "", e.pointerIsDown = "", e.pointerWasMoved = "", e._proxy = "";
    }(en || (me._ProxyValues = en = {})), me._ProxyMethods = tn, function(e) {
      e.start = "", e.move = "", e.end = "", e.stop = "", e.interacting = "";
    }(tn || (me._ProxyMethods = tn = {}));
    var gi = 0, Or = function() {
      function e(r) {
        var o = this, i = r.pointerType, s = r.scopeFire;
        (function(h, g) {
          if (!(h instanceof g))
            throw new TypeError("Cannot call a class as a function");
        })(this, e), ne(this, "interactable", null), ne(this, "element", null), ne(this, "rect", void 0), ne(this, "_rects", void 0), ne(this, "edges", void 0), ne(this, "_scopeFire", void 0), ne(this, "prepared", { name: null, axis: null, edges: null }), ne(this, "pointerType", void 0), ne(this, "pointers", []), ne(this, "downEvent", null), ne(this, "downPointer", {}), ne(this, "_latestPointer", { pointer: null, event: null, eventTarget: null }), ne(this, "prevEvent", null), ne(this, "pointerIsDown", !1), ne(this, "pointerWasMoved", !1), ne(this, "_interacting", !1), ne(this, "_ending", !1), ne(this, "_stopped", !0), ne(this, "_proxy", null), ne(this, "simulation", null), ne(this, "doMove", (0, _e.warnOnce)(function(h) {
          this.move(h);
        }, "The interaction.doMove() method has been renamed to interaction.move()")), ne(this, "coords", { start: E.newCoords(), prev: E.newCoords(), cur: E.newCoords(), delta: E.newCoords(), velocity: E.newCoords() }), ne(this, "_id", gi++), this._scopeFire = s, this.pointerType = i;
        var a = this;
        this._proxy = {};
        var l = function(h) {
          Object.defineProperty(o._proxy, h, { get: function() {
            return a[h];
          } });
        };
        for (var u in en)
          l(u);
        var f = function(h) {
          Object.defineProperty(o._proxy, h, { value: function() {
            return a[h].apply(a, arguments);
          } });
        };
        for (var d in tn)
          f(d);
        this._scopeFire("interactions:new", { interaction: this });
      }
      var t, n;
      return t = e, (n = [{ key: "pointerMoveTolerance", get: function() {
        return 1;
      } }, { key: "pointerDown", value: function(r, o, i) {
        var s = this.updatePointer(r, o, i, !0), a = this.pointers[s];
        this._scopeFire("interactions:down", { pointer: r, event: o, eventTarget: i, pointerIndex: s, pointerInfo: a, type: "down", interaction: this });
      } }, { key: "start", value: function(r, o, i) {
        return !(this.interacting() || !this.pointerIsDown || this.pointers.length < (r.name === "gesture" ? 2 : 1) || !o.options[r.name].enabled) && ((0, _e.copyAction)(this.prepared, r), this.interactable = o, this.element = i, this.rect = o.getRect(i), this.edges = this.prepared.edges ? (0, P.default)({}, this.prepared.edges) : { left: !0, right: !0, top: !0, bottom: !0 }, this._stopped = !1, this._interacting = this._doPhase({ interaction: this, event: this.downEvent, phase: "start" }) && !this._stopped, this._interacting);
      } }, { key: "pointerMove", value: function(r, o, i) {
        this.simulation || this.modification && this.modification.endResult || this.updatePointer(r, o, i, !1);
        var s, a, l = this.coords.cur.page.x === this.coords.prev.page.x && this.coords.cur.page.y === this.coords.prev.page.y && this.coords.cur.client.x === this.coords.prev.client.x && this.coords.cur.client.y === this.coords.prev.client.y;
        this.pointerIsDown && !this.pointerWasMoved && (s = this.coords.cur.client.x - this.coords.start.client.x, a = this.coords.cur.client.y - this.coords.start.client.y, this.pointerWasMoved = (0, De.default)(s, a) > this.pointerMoveTolerance);
        var u = this.getPointerIndex(r), f = { pointer: r, pointerIndex: u, pointerInfo: this.pointers[u], event: o, type: "move", eventTarget: i, dx: s, dy: a, duplicate: l, interaction: this };
        l || E.setCoordVelocity(this.coords.velocity, this.coords.delta), this._scopeFire("interactions:move", f), l || this.simulation || (this.interacting() && (f.type = null, this.move(f)), this.pointerWasMoved && E.copyCoords(this.coords.prev, this.coords.cur));
      } }, { key: "move", value: function(r) {
        r && r.event || E.setZeroCoords(this.coords.delta), (r = (0, P.default)({ pointer: this._latestPointer.pointer, event: this._latestPointer.event, eventTarget: this._latestPointer.eventTarget, interaction: this }, r || {})).phase = "move", this._doPhase(r);
      } }, { key: "pointerUp", value: function(r, o, i, s) {
        var a = this.getPointerIndex(r);
        a === -1 && (a = this.updatePointer(r, o, i, !1));
        var l = /cancel$/i.test(o.type) ? "cancel" : "up";
        this._scopeFire("interactions:".concat(l), { pointer: r, pointerIndex: a, pointerInfo: this.pointers[a], event: o, eventTarget: i, type: l, curEventTarget: s, interaction: this }), this.simulation || this.end(o), this.removePointer(r, o);
      } }, { key: "documentBlur", value: function(r) {
        this.end(r), this._scopeFire("interactions:blur", { event: r, type: "blur", interaction: this });
      } }, { key: "end", value: function(r) {
        var o;
        this._ending = !0, r = r || this._latestPointer.event, this.interacting() && (o = this._doPhase({ event: r, interaction: this, phase: "end" })), this._ending = !1, o === !0 && this.stop();
      } }, { key: "currentAction", value: function() {
        return this._interacting ? this.prepared.name : null;
      } }, { key: "interacting", value: function() {
        return this._interacting;
      } }, { key: "stop", value: function() {
        this._scopeFire("interactions:stop", { interaction: this }), this.interactable = this.element = null, this._interacting = !1, this._stopped = !0, this.prepared.name = this.prevEvent = null;
      } }, { key: "getPointerIndex", value: function(r) {
        var o = E.getPointerId(r);
        return this.pointerType === "mouse" || this.pointerType === "pen" ? this.pointers.length - 1 : I.findIndex(this.pointers, function(i) {
          return i.id === o;
        });
      } }, { key: "getPointerInfo", value: function(r) {
        return this.pointers[this.getPointerIndex(r)];
      } }, { key: "updatePointer", value: function(r, o, i, s) {
        var a = E.getPointerId(r), l = this.getPointerIndex(r), u = this.pointers[l];
        return s = s !== !1 && (s || /(down|start)$/i.test(o.type)), u ? u.pointer = r : (u = new Mt.PointerInfo(a, r, o, null, null), l = this.pointers.length, this.pointers.push(u)), E.setCoords(this.coords.cur, this.pointers.map(function(f) {
          return f.pointer;
        }), this._now()), E.setCoordDeltas(this.coords.delta, this.coords.prev, this.coords.cur), s && (this.pointerIsDown = !0, u.downTime = this.coords.cur.timeStamp, u.downTarget = i, E.pointerExtend(this.downPointer, r), this.interacting() || (E.copyCoords(this.coords.start, this.coords.cur), E.copyCoords(this.coords.prev, this.coords.cur), this.downEvent = o, this.pointerWasMoved = !1)), this._updateLatestPointer(r, o, i), this._scopeFire("interactions:update-pointer", { pointer: r, event: o, eventTarget: i, down: s, pointerInfo: u, pointerIndex: l, interaction: this }), l;
      } }, { key: "removePointer", value: function(r, o) {
        var i = this.getPointerIndex(r);
        if (i !== -1) {
          var s = this.pointers[i];
          this._scopeFire("interactions:remove-pointer", { pointer: r, event: o, eventTarget: null, pointerIndex: i, pointerInfo: s, interaction: this }), this.pointers.splice(i, 1), this.pointerIsDown = !1;
        }
      } }, { key: "_updateLatestPointer", value: function(r, o, i) {
        this._latestPointer.pointer = r, this._latestPointer.event = o, this._latestPointer.eventTarget = i;
      } }, { key: "destroy", value: function() {
        this._latestPointer.pointer = null, this._latestPointer.event = null, this._latestPointer.eventTarget = null;
      } }, { key: "_createPreparedEvent", value: function(r, o, i, s) {
        return new Et.InteractEvent(this, r, this.prepared.name, o, this.element, i, s);
      } }, { key: "_fireEvent", value: function(r) {
        this.interactable.fire(r), (!this.prevEvent || r.timeStamp >= this.prevEvent.timeStamp) && (this.prevEvent = r);
      } }, { key: "_doPhase", value: function(r) {
        var o = r.event, i = r.phase, s = r.preEnd, a = r.type, l = this.rect;
        if (l && i === "move" && (B.addEdges(this.edges, l, this.coords.delta[this.interactable.options.deltaSource]), l.width = l.right - l.left, l.height = l.bottom - l.top), this._scopeFire("interactions:before-action-".concat(i), r) === !1)
          return !1;
        var u = r.iEvent = this._createPreparedEvent(o, i, s, a);
        return this._scopeFire("interactions:action-".concat(i), r), i === "start" && (this.prevEvent = u), this._fireEvent(u), this._scopeFire("interactions:after-action-".concat(i), r), !0;
      } }, { key: "_now", value: function() {
        return Date.now();
      } }]) && hi(t.prototype, n), e;
    }();
    me.Interaction = Or;
    var yi = Or;
    me.default = yi;
    var at = {};
    function Pr(e) {
      e.pointerIsDown && (Cn(e.coords.cur, e.offset.total), e.offset.pending.x = 0, e.offset.pending.y = 0);
    }
    function Sr(e) {
      Rn(e.interaction);
    }
    function Rn(e) {
      if (!function(n) {
        return !(!n.offset.pending.x && !n.offset.pending.y);
      }(e))
        return !1;
      var t = e.offset.pending;
      return Cn(e.coords.cur, t), Cn(e.coords.delta, t), B.addEdges(e.edges, e.rect, t), t.x = 0, t.y = 0, !0;
    }
    function mi(e) {
      var t = e.x, n = e.y;
      this.offset.pending.x += t, this.offset.pending.y += n, this.offset.total.x += t, this.offset.total.y += n;
    }
    function Cn(e, t) {
      var n = e.page, r = e.client, o = t.x, i = t.y;
      n.x += o, n.y += i, r.x += o, r.y += i;
    }
    Object.defineProperty(at, "__esModule", { value: !0 }), at.addTotal = Pr, at.applyPending = Rn, at.default = void 0, me._ProxyMethods.offsetBy = "";
    var bi = { id: "offset", before: ["modifiers", "pointer-events", "actions", "inertia"], install: function(e) {
      e.Interaction.prototype.offsetBy = mi;
    }, listeners: { "interactions:new": function(e) {
      e.interaction.offset = { total: { x: 0, y: 0 }, pending: { x: 0, y: 0 } };
    }, "interactions:update-pointer": function(e) {
      return Pr(e.interaction);
    }, "interactions:before-action-start": Sr, "interactions:before-action-move": Sr, "interactions:before-action-end": function(e) {
      var t = e.interaction;
      if (Rn(t))
        return t.move({ offset: !0 }), t.end(), !1;
    }, "interactions:stop": function(e) {
      var t = e.interaction;
      t.offset.total.x = 0, t.offset.total.y = 0, t.offset.pending.x = 0, t.offset.pending.y = 0;
    } } };
    at.default = bi;
    var pt = {};
    function xi(e, t) {
      for (var n = 0; n < t.length; n++) {
        var r = t[n];
        r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), Object.defineProperty(e, r.key, r);
      }
    }
    function ce(e, t, n) {
      return t in e ? Object.defineProperty(e, t, { value: n, enumerable: !0, configurable: !0, writable: !0 }) : e[t] = n, e;
    }
    Object.defineProperty(pt, "__esModule", { value: !0 }), pt.default = pt.InertiaState = void 0;
    var Er = function() {
      function e(r) {
        (function(o, i) {
          if (!(o instanceof i))
            throw new TypeError("Cannot call a class as a function");
        })(this, e), ce(this, "active", !1), ce(this, "isModified", !1), ce(this, "smoothEnd", !1), ce(this, "allowResume", !1), ce(this, "modification", void 0), ce(this, "modifierCount", 0), ce(this, "modifierArg", void 0), ce(this, "startCoords", void 0), ce(this, "t0", 0), ce(this, "v0", 0), ce(this, "te", 0), ce(this, "targetOffset", void 0), ce(this, "modifiedOffset", void 0), ce(this, "currentOffset", void 0), ce(this, "lambda_v0", 0), ce(this, "one_ve_v0", 0), ce(this, "timeout", void 0), ce(this, "interaction", void 0), this.interaction = r;
      }
      var t, n;
      return t = e, (n = [{ key: "start", value: function(r) {
        var o = this.interaction, i = nn(o);
        if (!i || !i.enabled)
          return !1;
        var s = o.coords.velocity.client, a = (0, De.default)(s.x, s.y), l = this.modification || (this.modification = new ot.default(o));
        if (l.copyFrom(o.modification), this.t0 = o._now(), this.allowResume = i.allowResume, this.v0 = a, this.currentOffset = { x: 0, y: 0 }, this.startCoords = o.coords.cur.page, this.modifierArg = l.fillArg({ pageCoords: this.startCoords, preEnd: !0, phase: "inertiastart" }), this.t0 - o.coords.cur.timeStamp < 50 && a > i.minSpeed && a > i.endSpeed)
          this.startInertia();
        else {
          if (l.result = l.setAll(this.modifierArg), !l.result.changed)
            return !1;
          this.startSmoothEnd();
        }
        return o.modification.result.rect = null, o.offsetBy(this.targetOffset), o._doPhase({ interaction: o, event: r, phase: "inertiastart" }), o.offsetBy({ x: -this.targetOffset.x, y: -this.targetOffset.y }), o.modification.result.rect = null, this.active = !0, o.simulation = this, !0;
      } }, { key: "startInertia", value: function() {
        var r = this, o = this.interaction.coords.velocity.client, i = nn(this.interaction), s = i.resistance, a = -Math.log(i.endSpeed / this.v0) / s;
        this.targetOffset = { x: (o.x - a) / s, y: (o.y - a) / s }, this.te = a, this.lambda_v0 = s / this.v0, this.one_ve_v0 = 1 - i.endSpeed / this.v0;
        var l = this.modification, u = this.modifierArg;
        u.pageCoords = { x: this.startCoords.x + this.targetOffset.x, y: this.startCoords.y + this.targetOffset.y }, l.result = l.setAll(u), l.result.changed && (this.isModified = !0, this.modifiedOffset = { x: this.targetOffset.x + l.result.delta.x, y: this.targetOffset.y + l.result.delta.y }), this.onNextFrame(function() {
          return r.inertiaTick();
        });
      } }, { key: "startSmoothEnd", value: function() {
        var r = this;
        this.smoothEnd = !0, this.isModified = !0, this.targetOffset = { x: this.modification.result.delta.x, y: this.modification.result.delta.y }, this.onNextFrame(function() {
          return r.smoothEndTick();
        });
      } }, { key: "onNextFrame", value: function(r) {
        var o = this;
        this.timeout = je.default.request(function() {
          o.active && r();
        });
      } }, { key: "inertiaTick", value: function() {
        var r, o, i, s, a, l = this, u = this.interaction, f = nn(u).resistance, d = (u._now() - this.t0) / 1e3;
        if (d < this.te) {
          var h, g = 1 - (Math.exp(-f * d) - this.lambda_v0) / this.one_ve_v0;
          this.isModified ? (r = this.targetOffset.x, o = this.targetOffset.y, i = this.modifiedOffset.x, s = this.modifiedOffset.y, h = { x: Mr(a = g, 0, r, i), y: Mr(a, 0, o, s) }) : h = { x: this.targetOffset.x * g, y: this.targetOffset.y * g };
          var p = { x: h.x - this.currentOffset.x, y: h.y - this.currentOffset.y };
          this.currentOffset.x += p.x, this.currentOffset.y += p.y, u.offsetBy(p), u.move(), this.onNextFrame(function() {
            return l.inertiaTick();
          });
        } else
          u.offsetBy({ x: this.modifiedOffset.x - this.currentOffset.x, y: this.modifiedOffset.y - this.currentOffset.y }), this.end();
      } }, { key: "smoothEndTick", value: function() {
        var r = this, o = this.interaction, i = o._now() - this.t0, s = nn(o).smoothEndDuration;
        if (i < s) {
          var a = { x: Tr(i, 0, this.targetOffset.x, s), y: Tr(i, 0, this.targetOffset.y, s) }, l = { x: a.x - this.currentOffset.x, y: a.y - this.currentOffset.y };
          this.currentOffset.x += l.x, this.currentOffset.y += l.y, o.offsetBy(l), o.move({ skipModifiers: this.modifierCount }), this.onNextFrame(function() {
            return r.smoothEndTick();
          });
        } else
          o.offsetBy({ x: this.targetOffset.x - this.currentOffset.x, y: this.targetOffset.y - this.currentOffset.y }), this.end();
      } }, { key: "resume", value: function(r) {
        var o = r.pointer, i = r.event, s = r.eventTarget, a = this.interaction;
        a.offsetBy({ x: -this.currentOffset.x, y: -this.currentOffset.y }), a.updatePointer(o, i, s, !0), a._doPhase({ interaction: a, event: i, phase: "resume" }), (0, E.copyCoords)(a.coords.prev, a.coords.cur), this.stop();
      } }, { key: "end", value: function() {
        this.interaction.move(), this.interaction.end(), this.stop();
      } }, { key: "stop", value: function() {
        this.active = this.smoothEnd = !1, this.interaction.simulation = null, je.default.cancel(this.timeout);
      } }]) && xi(t.prototype, n), e;
    }();
    function nn(e) {
      var t = e.interactable, n = e.prepared;
      return t && t.options && n.name && t.options[n.name].inertia;
    }
    function Mr(e, t, n, r) {
      var o = 1 - e;
      return o * o * t + 2 * o * e * n + e * e * r;
    }
    function Tr(e, t, n, r) {
      return -n * (e /= r) * (e - 2) + t;
    }
    pt.InertiaState = Er;
    var wi = { id: "inertia", before: ["modifiers", "actions"], install: function(e) {
      var t = e.defaults;
      e.usePlugin(at.default), e.usePlugin(fe.default), e.actions.phases.inertiastart = !0, e.actions.phases.resume = !0, t.perAction.inertia = { enabled: !1, resistance: 10, minSpeed: 100, endSpeed: 10, allowResume: !0, smoothEndDuration: 300 };
    }, listeners: { "interactions:new": function(e) {
      var t = e.interaction;
      t.inertia = new Er(t);
    }, "interactions:before-action-end": function(e) {
      var t = e.interaction, n = e.event;
      return (!t._interacting || t.simulation || !t.inertia.start(n)) && null;
    }, "interactions:down": function(e) {
      var t = e.interaction, n = e.eventTarget, r = t.inertia;
      if (r.active)
        for (var o = n; v.default.element(o); ) {
          if (o === t.element) {
            r.resume(e);
            break;
          }
          o = S.parentNode(o);
        }
    }, "interactions:stop": function(e) {
      var t = e.interaction.inertia;
      t.active && t.stop();
    }, "interactions:before-action-resume": function(e) {
      var t = e.interaction.modification;
      t.stop(e), t.start(e, e.interaction.coords.cur.page), t.applyToInteraction(e);
    }, "interactions:before-action-inertiastart": function(e) {
      return e.interaction.modification.setAndApply(e);
    }, "interactions:action-resume": fe.addEventModifiers, "interactions:action-inertiastart": fe.addEventModifiers, "interactions:after-action-inertiastart": function(e) {
      return e.interaction.modification.restoreInteractionCoords(e);
    }, "interactions:after-action-resume": function(e) {
      return e.interaction.modification.restoreInteractionCoords(e);
    } } };
    pt.default = wi;
    var jt = {};
    function _i(e, t) {
      for (var n = 0; n < t.length; n++) {
        var r = t[n];
        r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), Object.defineProperty(e, r.key, r);
      }
    }
    function kt(e, t, n) {
      return t in e ? Object.defineProperty(e, t, { value: n, enumerable: !0, configurable: !0, writable: !0 }) : e[t] = n, e;
    }
    function jr(e, t) {
      for (var n = 0; n < t.length; n++) {
        var r = t[n];
        if (e.immediatePropagationStopped)
          break;
        r(e);
      }
    }
    Object.defineProperty(jt, "__esModule", { value: !0 }), jt.Eventable = void 0;
    var Oi = function() {
      function e(r) {
        (function(o, i) {
          if (!(o instanceof i))
            throw new TypeError("Cannot call a class as a function");
        })(this, e), kt(this, "options", void 0), kt(this, "types", {}), kt(this, "propagationStopped", !1), kt(this, "immediatePropagationStopped", !1), kt(this, "global", void 0), this.options = (0, P.default)({}, r || {});
      }
      var t, n;
      return t = e, (n = [{ key: "fire", value: function(r) {
        var o, i = this.global;
        (o = this.types[r.type]) && jr(r, o), !r.propagationStopped && i && (o = i[r.type]) && jr(r, o);
      } }, { key: "on", value: function(r, o) {
        var i = (0, ve.default)(r, o);
        for (r in i)
          this.types[r] = I.merge(this.types[r] || [], i[r]);
      } }, { key: "off", value: function(r, o) {
        var i = (0, ve.default)(r, o);
        for (r in i) {
          var s = this.types[r];
          if (s && s.length)
            for (var a = 0; a < i[r].length; a++) {
              var l = i[r][a], u = s.indexOf(l);
              u !== -1 && s.splice(u, 1);
            }
        }
      } }, { key: "getRect", value: function(r) {
        return null;
      } }]) && _i(t.prototype, n), e;
    }();
    jt.Eventable = Oi;
    var It = {};
    Object.defineProperty(It, "__esModule", { value: !0 }), It.default = function(e, t) {
      if (t.phaselessTypes[e])
        return !0;
      for (var n in t.map)
        if (e.indexOf(n) === 0 && e.substr(n.length) in t.phases)
          return !0;
      return !1;
    };
    var An = {};
    Object.defineProperty(An, "__esModule", { value: !0 }), An.createInteractStatic = function(e) {
      var t = function n(r, o) {
        var i = e.interactables.get(r, o);
        return i || ((i = e.interactables.new(r, o)).events.global = n.globalEvents), i;
      };
      return t.getPointerAverage = E.pointerAverage, t.getTouchBBox = E.touchBBox, t.getTouchDistance = E.touchDistance, t.getTouchAngle = E.touchAngle, t.getElementRect = S.getElementRect, t.getElementClientRect = S.getElementClientRect, t.matchesSelector = S.matchesSelector, t.closest = S.closest, t.globalEvents = {}, t.version = "1.10.11", t.scope = e, t.use = function(n, r) {
        return this.scope.usePlugin(n, r), this;
      }, t.isSet = function(n, r) {
        return !!this.scope.interactables.get(n, r && r.context);
      }, t.on = (0, _e.warnOnce)(function(n, r, o) {
        if (v.default.string(n) && n.search(" ") !== -1 && (n = n.trim().split(/ +/)), v.default.array(n)) {
          for (var i = 0; i < n.length; i++) {
            var s = n[i];
            this.on(s, r, o);
          }
          return this;
        }
        if (v.default.object(n)) {
          for (var a in n)
            this.on(a, n[a], r);
          return this;
        }
        return (0, It.default)(n, this.scope.actions) ? this.globalEvents[n] ? this.globalEvents[n].push(r) : this.globalEvents[n] = [r] : this.scope.events.add(this.scope.document, n, r, { options: o }), this;
      }, "The interact.on() method is being deprecated"), t.off = (0, _e.warnOnce)(function(n, r, o) {
        if (v.default.string(n) && n.search(" ") !== -1 && (n = n.trim().split(/ +/)), v.default.array(n)) {
          for (var i = 0; i < n.length; i++) {
            var s = n[i];
            this.off(s, r, o);
          }
          return this;
        }
        if (v.default.object(n)) {
          for (var a in n)
            this.off(a, n[a], r);
          return this;
        }
        var l;
        return (0, It.default)(n, this.scope.actions) ? n in this.globalEvents && (l = this.globalEvents[n].indexOf(r)) !== -1 && this.globalEvents[n].splice(l, 1) : this.scope.events.remove(this.scope.document, n, r, o), this;
      }, "The interact.off() method is being deprecated"), t.debug = function() {
        return this.scope;
      }, t.supportsTouch = function() {
        return z.default.supportsTouch;
      }, t.supportsPointerEvent = function() {
        return z.default.supportsPointerEvent;
      }, t.stop = function() {
        for (var n = 0; n < this.scope.interactions.list.length; n++)
          this.scope.interactions.list[n].stop();
        return this;
      }, t.pointerMoveTolerance = function(n) {
        return v.default.number(n) ? (this.scope.interactions.pointerMoveTolerance = n, this) : this.scope.interactions.pointerMoveTolerance;
      }, t.addDocument = function(n, r) {
        this.scope.addDocument(n, r);
      }, t.removeDocument = function(n) {
        this.scope.removeDocument(n);
      }, t;
    };
    var rn = {};
    function Pi(e, t) {
      for (var n = 0; n < t.length; n++) {
        var r = t[n];
        r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), Object.defineProperty(e, r.key, r);
      }
    }
    function $e(e, t, n) {
      return t in e ? Object.defineProperty(e, t, { value: n, enumerable: !0, configurable: !0, writable: !0 }) : e[t] = n, e;
    }
    Object.defineProperty(rn, "__esModule", { value: !0 }), rn.Interactable = void 0;
    var Si = function() {
      function e(r, o, i, s) {
        (function(a, l) {
          if (!(a instanceof l))
            throw new TypeError("Cannot call a class as a function");
        })(this, e), $e(this, "options", void 0), $e(this, "_actions", void 0), $e(this, "target", void 0), $e(this, "events", new jt.Eventable()), $e(this, "_context", void 0), $e(this, "_win", void 0), $e(this, "_doc", void 0), $e(this, "_scopeEvents", void 0), $e(this, "_rectChecker", void 0), this._actions = o.actions, this.target = r, this._context = o.context || i, this._win = (0, b.getWindow)((0, S.trySelector)(r) ? this._context : r), this._doc = this._win.document, this._scopeEvents = s, this.set(o);
      }
      var t, n;
      return t = e, (n = [{ key: "_defaults", get: function() {
        return { base: {}, perAction: {}, actions: {} };
      } }, { key: "setOnEvents", value: function(r, o) {
        return v.default.func(o.onstart) && this.on("".concat(r, "start"), o.onstart), v.default.func(o.onmove) && this.on("".concat(r, "move"), o.onmove), v.default.func(o.onend) && this.on("".concat(r, "end"), o.onend), v.default.func(o.oninertiastart) && this.on("".concat(r, "inertiastart"), o.oninertiastart), this;
      } }, { key: "updatePerActionListeners", value: function(r, o, i) {
        (v.default.array(o) || v.default.object(o)) && this.off(r, o), (v.default.array(i) || v.default.object(i)) && this.on(r, i);
      } }, { key: "setPerAction", value: function(r, o) {
        var i = this._defaults;
        for (var s in o) {
          var a = s, l = this.options[r], u = o[a];
          a === "listeners" && this.updatePerActionListeners(r, l.listeners, u), v.default.array(u) ? l[a] = I.from(u) : v.default.plainObject(u) ? (l[a] = (0, P.default)(l[a] || {}, (0, rt.default)(u)), v.default.object(i.perAction[a]) && "enabled" in i.perAction[a] && (l[a].enabled = u.enabled !== !1)) : v.default.bool(u) && v.default.object(i.perAction[a]) ? l[a].enabled = u : l[a] = u;
        }
      } }, { key: "getRect", value: function(r) {
        return r = r || (v.default.element(this.target) ? this.target : null), v.default.string(this.target) && (r = r || this._context.querySelector(this.target)), (0, S.getElementRect)(r);
      } }, { key: "rectChecker", value: function(r) {
        var o = this;
        return v.default.func(r) ? (this._rectChecker = r, this.getRect = function(i) {
          var s = (0, P.default)({}, o._rectChecker(i));
          return "width" in s || (s.width = s.right - s.left, s.height = s.bottom - s.top), s;
        }, this) : r === null ? (delete this.getRect, delete this._rectChecker, this) : this.getRect;
      } }, { key: "_backCompatOption", value: function(r, o) {
        if ((0, S.trySelector)(o) || v.default.object(o)) {
          for (var i in this.options[r] = o, this._actions.map)
            this.options[i][r] = o;
          return this;
        }
        return this.options[r];
      } }, { key: "origin", value: function(r) {
        return this._backCompatOption("origin", r);
      } }, { key: "deltaSource", value: function(r) {
        return r === "page" || r === "client" ? (this.options.deltaSource = r, this) : this.options.deltaSource;
      } }, { key: "context", value: function() {
        return this._context;
      } }, { key: "inContext", value: function(r) {
        return this._context === r.ownerDocument || (0, S.nodeContains)(this._context, r);
      } }, { key: "testIgnoreAllow", value: function(r, o, i) {
        return !this.testIgnore(r.ignoreFrom, o, i) && this.testAllow(r.allowFrom, o, i);
      } }, { key: "testAllow", value: function(r, o, i) {
        return !r || !!v.default.element(i) && (v.default.string(r) ? (0, S.matchesUpTo)(i, r, o) : !!v.default.element(r) && (0, S.nodeContains)(r, i));
      } }, { key: "testIgnore", value: function(r, o, i) {
        return !(!r || !v.default.element(i)) && (v.default.string(r) ? (0, S.matchesUpTo)(i, r, o) : !!v.default.element(r) && (0, S.nodeContains)(r, i));
      } }, { key: "fire", value: function(r) {
        return this.events.fire(r), this;
      } }, { key: "_onOff", value: function(r, o, i, s) {
        v.default.object(o) && !v.default.array(o) && (s = i, i = null);
        var a = r === "on" ? "add" : "remove", l = (0, ve.default)(o, i);
        for (var u in l) {
          u === "wheel" && (u = z.default.wheelEvent);
          for (var f = 0; f < l[u].length; f++) {
            var d = l[u][f];
            (0, It.default)(u, this._actions) ? this.events[r](u, d) : v.default.string(this.target) ? this._scopeEvents["".concat(a, "Delegate")](this.target, this._context, u, d, s) : this._scopeEvents[a](this.target, u, d, s);
          }
        }
        return this;
      } }, { key: "on", value: function(r, o, i) {
        return this._onOff("on", r, o, i);
      } }, { key: "off", value: function(r, o, i) {
        return this._onOff("off", r, o, i);
      } }, { key: "set", value: function(r) {
        var o = this._defaults;
        for (var i in v.default.object(r) || (r = {}), this.options = (0, rt.default)(o.base), this._actions.methodDict) {
          var s = i, a = this._actions.methodDict[s];
          this.options[s] = {}, this.setPerAction(s, (0, P.default)((0, P.default)({}, o.perAction), o.actions[s])), this[a](r[s]);
        }
        for (var l in r)
          v.default.func(this[l]) && this[l](r[l]);
        return this;
      } }, { key: "unset", value: function() {
        if (v.default.string(this.target))
          for (var r in this._scopeEvents.delegatedEvents)
            for (var o = this._scopeEvents.delegatedEvents[r], i = o.length - 1; i >= 0; i--) {
              var s = o[i], a = s.selector, l = s.context, u = s.listeners;
              a === this.target && l === this._context && o.splice(i, 1);
              for (var f = u.length - 1; f >= 0; f--)
                this._scopeEvents.removeDelegate(this.target, this._context, r, u[f][0], u[f][1]);
            }
        else
          this._scopeEvents.remove(this.target, "all");
      } }]) && Pi(t.prototype, n), e;
    }();
    rn.Interactable = Si;
    var on = {};
    function Ei(e, t) {
      for (var n = 0; n < t.length; n++) {
        var r = t[n];
        r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), Object.defineProperty(e, r.key, r);
      }
    }
    function Fn(e, t, n) {
      return t in e ? Object.defineProperty(e, t, { value: n, enumerable: !0, configurable: !0, writable: !0 }) : e[t] = n, e;
    }
    Object.defineProperty(on, "__esModule", { value: !0 }), on.InteractableSet = void 0;
    var Mi = function() {
      function e(r) {
        var o = this;
        (function(i, s) {
          if (!(i instanceof s))
            throw new TypeError("Cannot call a class as a function");
        })(this, e), Fn(this, "list", []), Fn(this, "selectorMap", {}), Fn(this, "scope", void 0), this.scope = r, r.addListeners({ "interactable:unset": function(i) {
          var s = i.interactable, a = s.target, l = s._context, u = v.default.string(a) ? o.selectorMap[a] : a[o.scope.id], f = I.findIndex(u, function(d) {
            return d.context === l;
          });
          u[f] && (u[f].context = null, u[f].interactable = null), u.splice(f, 1);
        } });
      }
      var t, n;
      return t = e, (n = [{ key: "new", value: function(r, o) {
        o = (0, P.default)(o || {}, { actions: this.scope.actions });
        var i = new this.scope.Interactable(r, o, this.scope.document, this.scope.events), s = { context: i._context, interactable: i };
        return this.scope.addDocument(i._doc), this.list.push(i), v.default.string(r) ? (this.selectorMap[r] || (this.selectorMap[r] = []), this.selectorMap[r].push(s)) : (i.target[this.scope.id] || Object.defineProperty(r, this.scope.id, { value: [], configurable: !0 }), r[this.scope.id].push(s)), this.scope.fire("interactable:new", { target: r, options: o, interactable: i, win: this.scope._win }), i;
      } }, { key: "get", value: function(r, o) {
        var i = o && o.context || this.scope.document, s = v.default.string(r), a = s ? this.selectorMap[r] : r[this.scope.id];
        if (!a)
          return null;
        var l = I.find(a, function(u) {
          return u.context === i && (s || u.interactable.inContext(r));
        });
        return l && l.interactable;
      } }, { key: "forEachMatch", value: function(r, o) {
        for (var i = 0; i < this.list.length; i++) {
          var s = this.list[i], a = void 0;
          if ((v.default.string(s.target) ? v.default.element(r) && S.matchesSelector(r, s.target) : r === s.target) && s.inContext(r) && (a = o(s)), a !== void 0)
            return a;
        }
      } }]) && Ei(t.prototype, n), e;
    }();
    on.InteractableSet = Mi;
    var an = {};
    function Ti(e, t) {
      for (var n = 0; n < t.length; n++) {
        var r = t[n];
        r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), Object.defineProperty(e, r.key, r);
      }
    }
    function Bn(e, t, n) {
      return t in e ? Object.defineProperty(e, t, { value: n, enumerable: !0, configurable: !0, writable: !0 }) : e[t] = n, e;
    }
    function Wn(e, t) {
      return function(n) {
        if (Array.isArray(n))
          return n;
      }(e) || function(n, r) {
        if (typeof Symbol < "u" && Symbol.iterator in Object(n)) {
          var o = [], i = !0, s = !1, a = void 0;
          try {
            for (var l, u = n[Symbol.iterator](); !(i = (l = u.next()).done) && (o.push(l.value), !r || o.length !== r); i = !0)
              ;
          } catch (f) {
            s = !0, a = f;
          } finally {
            try {
              i || u.return == null || u.return();
            } finally {
              if (s)
                throw a;
            }
          }
          return o;
        }
      }(e, t) || function(n, r) {
        if (n) {
          if (typeof n == "string")
            return kr(n, r);
          var o = Object.prototype.toString.call(n).slice(8, -1);
          return o === "Object" && n.constructor && (o = n.constructor.name), o === "Map" || o === "Set" ? Array.from(n) : o === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(o) ? kr(n, r) : void 0;
        }
      }(e, t) || function() {
        throw new TypeError(`Invalid attempt to destructure non-iterable instance.
In order to be iterable, non-array objects must have a [Symbol.iterator]() method.`);
      }();
    }
    function kr(e, t) {
      (t == null || t > e.length) && (t = e.length);
      for (var n = 0, r = Array(t); n < t; n++)
        r[n] = e[n];
      return r;
    }
    Object.defineProperty(an, "__esModule", { value: !0 }), an.default = void 0;
    var ji = function() {
      function e(r) {
        (function(o, i) {
          if (!(o instanceof i))
            throw new TypeError("Cannot call a class as a function");
        })(this, e), Bn(this, "currentTarget", void 0), Bn(this, "originalEvent", void 0), Bn(this, "type", void 0), this.originalEvent = r, (0, se.default)(this, r);
      }
      var t, n;
      return t = e, (n = [{ key: "preventOriginalDefault", value: function() {
        this.originalEvent.preventDefault();
      } }, { key: "stopPropagation", value: function() {
        this.originalEvent.stopPropagation();
      } }, { key: "stopImmediatePropagation", value: function() {
        this.originalEvent.stopImmediatePropagation();
      } }]) && Ti(t.prototype, n), e;
    }();
    function zt(e) {
      if (!v.default.object(e))
        return { capture: !!e, passive: !1 };
      var t = (0, P.default)({}, e);
      return t.capture = !!e.capture, t.passive = !!e.passive, t;
    }
    var ki = { id: "events", install: function(e) {
      var t, n = [], r = {}, o = [], i = { add: s, remove: a, addDelegate: function(f, d, h, g, p) {
        var x = zt(p);
        if (!r[h]) {
          r[h] = [];
          for (var _ = 0; _ < o.length; _++) {
            var w = o[_];
            s(w, h, l), s(w, h, u, !0);
          }
        }
        var T = r[h], D = I.find(T, function(A) {
          return A.selector === f && A.context === d;
        });
        D || (D = { selector: f, context: d, listeners: [] }, T.push(D)), D.listeners.push([g, x]);
      }, removeDelegate: function(f, d, h, g, p) {
        var x, _ = zt(p), w = r[h], T = !1;
        if (w)
          for (x = w.length - 1; x >= 0; x--) {
            var D = w[x];
            if (D.selector === f && D.context === d) {
              for (var A = D.listeners, N = A.length - 1; N >= 0; N--) {
                var ee = Wn(A[N], 2), W = ee[0], U = ee[1], Ee = U.capture, Ne = U.passive;
                if (W === g && Ee === _.capture && Ne === _.passive) {
                  A.splice(N, 1), A.length || (w.splice(x, 1), a(d, h, l), a(d, h, u, !0)), T = !0;
                  break;
                }
              }
              if (T)
                break;
            }
          }
      }, delegateListener: l, delegateUseCapture: u, delegatedEvents: r, documents: o, targets: n, supportsOptions: !1, supportsPassive: !1 };
      function s(f, d, h, g) {
        var p = zt(g), x = I.find(n, function(_) {
          return _.eventTarget === f;
        });
        x || (x = { eventTarget: f, events: {} }, n.push(x)), x.events[d] || (x.events[d] = []), f.addEventListener && !I.contains(x.events[d], h) && (f.addEventListener(d, h, i.supportsOptions ? p : p.capture), x.events[d].push(h));
      }
      function a(f, d, h, g) {
        var p = zt(g), x = I.findIndex(n, function(N) {
          return N.eventTarget === f;
        }), _ = n[x];
        if (_ && _.events)
          if (d !== "all") {
            var w = !1, T = _.events[d];
            if (T) {
              if (h === "all") {
                for (var D = T.length - 1; D >= 0; D--)
                  a(f, d, T[D], p);
                return;
              }
              for (var A = 0; A < T.length; A++)
                if (T[A] === h) {
                  f.removeEventListener(d, h, i.supportsOptions ? p : p.capture), T.splice(A, 1), T.length === 0 && (delete _.events[d], w = !0);
                  break;
                }
            }
            w && !Object.keys(_.events).length && n.splice(x, 1);
          } else
            for (d in _.events)
              _.events.hasOwnProperty(d) && a(f, d, "all");
      }
      function l(f, d) {
        for (var h = zt(d), g = new ji(f), p = r[f.type], x = Wn(E.getEventTargets(f), 1)[0], _ = x; v.default.element(_); ) {
          for (var w = 0; w < p.length; w++) {
            var T = p[w], D = T.selector, A = T.context;
            if (S.matchesSelector(_, D) && S.nodeContains(A, x) && S.nodeContains(A, _)) {
              var N = T.listeners;
              g.currentTarget = _;
              for (var ee = 0; ee < N.length; ee++) {
                var W = Wn(N[ee], 2), U = W[0], Ee = W[1], Ne = Ee.capture, Jn = Ee.passive;
                Ne === h.capture && Jn === h.passive && U(g);
              }
            }
          }
          _ = S.parentNode(_);
        }
      }
      function u(f) {
        return l(f, !0);
      }
      return (t = e.document) == null || t.createElement("div").addEventListener("test", null, { get capture() {
        return i.supportsOptions = !0;
      }, get passive() {
        return i.supportsPassive = !0;
      } }), e.events = i, i;
    } };
    an.default = ki;
    var sn = {};
    Object.defineProperty(sn, "__esModule", { value: !0 }), sn.default = void 0;
    var ln = { methodOrder: ["simulationResume", "mouseOrPen", "hasPointer", "idle"], search: function(e) {
      for (var t = 0; t < ln.methodOrder.length; t++) {
        var n;
        n = ln.methodOrder[t];
        var r = ln[n](e);
        if (r)
          return r;
      }
      return null;
    }, simulationResume: function(e) {
      var t = e.pointerType, n = e.eventType, r = e.eventTarget, o = e.scope;
      if (!/down|start/i.test(n))
        return null;
      for (var i = 0; i < o.interactions.list.length; i++) {
        var s = o.interactions.list[i], a = r;
        if (s.simulation && s.simulation.allowResume && s.pointerType === t)
          for (; a; ) {
            if (a === s.element)
              return s;
            a = S.parentNode(a);
          }
      }
      return null;
    }, mouseOrPen: function(e) {
      var t, n = e.pointerId, r = e.pointerType, o = e.eventType, i = e.scope;
      if (r !== "mouse" && r !== "pen")
        return null;
      for (var s = 0; s < i.interactions.list.length; s++) {
        var a = i.interactions.list[s];
        if (a.pointerType === r) {
          if (a.simulation && !Ir(a, n))
            continue;
          if (a.interacting())
            return a;
          t || (t = a);
        }
      }
      if (t)
        return t;
      for (var l = 0; l < i.interactions.list.length; l++) {
        var u = i.interactions.list[l];
        if (!(u.pointerType !== r || /down/i.test(o) && u.simulation))
          return u;
      }
      return null;
    }, hasPointer: function(e) {
      for (var t = e.pointerId, n = e.scope, r = 0; r < n.interactions.list.length; r++) {
        var o = n.interactions.list[r];
        if (Ir(o, t))
          return o;
      }
      return null;
    }, idle: function(e) {
      for (var t = e.pointerType, n = e.scope, r = 0; r < n.interactions.list.length; r++) {
        var o = n.interactions.list[r];
        if (o.pointers.length === 1) {
          var i = o.interactable;
          if (i && (!i.options.gesture || !i.options.gesture.enabled))
            continue;
        } else if (o.pointers.length >= 2)
          continue;
        if (!o.interacting() && t === o.pointerType)
          return o;
      }
      return null;
    } };
    function Ir(e, t) {
      return e.pointers.some(function(n) {
        return n.id === t;
      });
    }
    var Ii = ln;
    sn.default = Ii;
    var un = {};
    function zr(e) {
      return (zr = typeof Symbol == "function" && typeof Symbol.iterator == "symbol" ? function(t) {
        return typeof t;
      } : function(t) {
        return t && typeof Symbol == "function" && t.constructor === Symbol && t !== Symbol.prototype ? "symbol" : typeof t;
      })(e);
    }
    function Dr(e, t) {
      return function(n) {
        if (Array.isArray(n))
          return n;
      }(e) || function(n, r) {
        if (typeof Symbol < "u" && Symbol.iterator in Object(n)) {
          var o = [], i = !0, s = !1, a = void 0;
          try {
            for (var l, u = n[Symbol.iterator](); !(i = (l = u.next()).done) && (o.push(l.value), !r || o.length !== r); i = !0)
              ;
          } catch (f) {
            s = !0, a = f;
          } finally {
            try {
              i || u.return == null || u.return();
            } finally {
              if (s)
                throw a;
            }
          }
          return o;
        }
      }(e, t) || function(n, r) {
        if (n) {
          if (typeof n == "string")
            return Rr(n, r);
          var o = Object.prototype.toString.call(n).slice(8, -1);
          return o === "Object" && n.constructor && (o = n.constructor.name), o === "Map" || o === "Set" ? Array.from(n) : o === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(o) ? Rr(n, r) : void 0;
        }
      }(e, t) || function() {
        throw new TypeError(`Invalid attempt to destructure non-iterable instance.
In order to be iterable, non-array objects must have a [Symbol.iterator]() method.`);
      }();
    }
    function Rr(e, t) {
      (t == null || t > e.length) && (t = e.length);
      for (var n = 0, r = Array(t); n < t; n++)
        r[n] = e[n];
      return r;
    }
    function zi(e, t) {
      if (!(e instanceof t))
        throw new TypeError("Cannot call a class as a function");
    }
    function Di(e, t) {
      for (var n = 0; n < t.length; n++) {
        var r = t[n];
        r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), Object.defineProperty(e, r.key, r);
      }
    }
    function Cr(e, t) {
      return (Cr = Object.setPrototypeOf || function(n, r) {
        return n.__proto__ = r, n;
      })(e, t);
    }
    function Ri(e, t) {
      return !t || zr(t) !== "object" && typeof t != "function" ? function(n) {
        if (n === void 0)
          throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
        return n;
      }(e) : t;
    }
    function Xn(e) {
      return (Xn = Object.setPrototypeOf ? Object.getPrototypeOf : function(t) {
        return t.__proto__ || Object.getPrototypeOf(t);
      })(e);
    }
    Object.defineProperty(un, "__esModule", { value: !0 }), un.default = void 0;
    var Yn = ["pointerDown", "pointerMove", "pointerUp", "updatePointer", "removePointer", "windowBlur"];
    function Ar(e, t) {
      return function(n) {
        var r = t.interactions.list, o = E.getPointerType(n), i = Dr(E.getEventTargets(n), 2), s = i[0], a = i[1], l = [];
        if (/^touch/.test(n.type)) {
          t.prevTouchTime = t.now();
          for (var u = 0; u < n.changedTouches.length; u++) {
            var f = n.changedTouches[u], d = { pointer: f, pointerId: E.getPointerId(f), pointerType: o, eventType: n.type, eventTarget: s, curEventTarget: a, scope: t }, h = Fr(d);
            l.push([d.pointer, d.eventTarget, d.curEventTarget, h]);
          }
        } else {
          var g = !1;
          if (!z.default.supportsPointerEvent && /mouse/.test(n.type)) {
            for (var p = 0; p < r.length && !g; p++)
              g = r[p].pointerType !== "mouse" && r[p].pointerIsDown;
            g = g || t.now() - t.prevTouchTime < 500 || n.timeStamp === 0;
          }
          if (!g) {
            var x = { pointer: n, pointerId: E.getPointerId(n), pointerType: o, eventType: n.type, curEventTarget: a, eventTarget: s, scope: t }, _ = Fr(x);
            l.push([x.pointer, x.eventTarget, x.curEventTarget, _]);
          }
        }
        for (var w = 0; w < l.length; w++) {
          var T = Dr(l[w], 4), D = T[0], A = T[1], N = T[2];
          T[3][e](D, n, A, N);
        }
      };
    }
    function Fr(e) {
      var t = e.pointerType, n = e.scope, r = { interaction: sn.default.search(e), searchDetails: e };
      return n.fire("interactions:find", r), r.interaction || n.interactions.new({ pointerType: t });
    }
    function $n(e, t) {
      var n = e.doc, r = e.scope, o = e.options, i = r.interactions.docEvents, s = r.events, a = s[t];
      for (var l in r.browser.isIOS && !o.events && (o.events = { passive: !1 }), s.delegatedEvents)
        a(n, l, s.delegateListener), a(n, l, s.delegateUseCapture, !0);
      for (var u = o && o.events, f = 0; f < i.length; f++) {
        var d = i[f];
        a(n, d.type, d.listener, u);
      }
    }
    var Ci = { id: "core/interactions", install: function(e) {
      for (var t = {}, n = 0; n < Yn.length; n++) {
        var r = Yn[n];
        t[r] = Ar(r, e);
      }
      var o, i = z.default.pEventTypes;
      function s() {
        for (var a = 0; a < e.interactions.list.length; a++) {
          var l = e.interactions.list[a];
          if (l.pointerIsDown && l.pointerType === "touch" && !l._interacting)
            for (var u = function() {
              var d = l.pointers[f];
              e.documents.some(function(h) {
                var g = h.doc;
                return (0, S.nodeContains)(g, d.downTarget);
              }) || l.removePointer(d.pointer, d.event);
            }, f = 0; f < l.pointers.length; f++)
              u();
        }
      }
      (o = X.default.PointerEvent ? [{ type: i.down, listener: s }, { type: i.down, listener: t.pointerDown }, { type: i.move, listener: t.pointerMove }, { type: i.up, listener: t.pointerUp }, { type: i.cancel, listener: t.pointerUp }] : [{ type: "mousedown", listener: t.pointerDown }, { type: "mousemove", listener: t.pointerMove }, { type: "mouseup", listener: t.pointerUp }, { type: "touchstart", listener: s }, { type: "touchstart", listener: t.pointerDown }, { type: "touchmove", listener: t.pointerMove }, { type: "touchend", listener: t.pointerUp }, { type: "touchcancel", listener: t.pointerUp }]).push({ type: "blur", listener: function(a) {
        for (var l = 0; l < e.interactions.list.length; l++)
          e.interactions.list[l].documentBlur(a);
      } }), e.prevTouchTime = 0, e.Interaction = function(a) {
        (function(p, x) {
          if (typeof x != "function" && x !== null)
            throw new TypeError("Super expression must either be null or a function");
          p.prototype = Object.create(x && x.prototype, { constructor: { value: p, writable: !0, configurable: !0 } }), x && Cr(p, x);
        })(g, a);
        var l, u, f, d, h = (f = g, d = function() {
          if (typeof Reflect > "u" || !Reflect.construct || Reflect.construct.sham)
            return !1;
          if (typeof Proxy == "function")
            return !0;
          try {
            return Boolean.prototype.valueOf.call(Reflect.construct(Boolean, [], function() {
            })), !0;
          } catch {
            return !1;
          }
        }(), function() {
          var p, x = Xn(f);
          if (d) {
            var _ = Xn(this).constructor;
            p = Reflect.construct(x, arguments, _);
          } else
            p = x.apply(this, arguments);
          return Ri(this, p);
        });
        function g() {
          return zi(this, g), h.apply(this, arguments);
        }
        return l = g, (u = [{ key: "pointerMoveTolerance", get: function() {
          return e.interactions.pointerMoveTolerance;
        }, set: function(p) {
          e.interactions.pointerMoveTolerance = p;
        } }, { key: "_now", value: function() {
          return e.now();
        } }]) && Di(l.prototype, u), g;
      }(me.default), e.interactions = { list: [], new: function(a) {
        a.scopeFire = function(u, f) {
          return e.fire(u, f);
        };
        var l = new e.Interaction(a);
        return e.interactions.list.push(l), l;
      }, listeners: t, docEvents: o, pointerMoveTolerance: 1 }, e.usePlugin(dt.default);
    }, listeners: { "scope:add-document": function(e) {
      return $n(e, "add");
    }, "scope:remove-document": function(e) {
      return $n(e, "remove");
    }, "interactable:unset": function(e, t) {
      for (var n = e.interactable, r = t.interactions.list.length - 1; r >= 0; r--) {
        var o = t.interactions.list[r];
        o.interactable === n && (o.stop(), t.fire("interactions:destroy", { interaction: o }), o.destroy(), t.interactions.list.length > 2 && t.interactions.list.splice(r, 1));
      }
    } }, onDocSignal: $n, doOnInteractions: Ar, methodNames: Yn };
    un.default = Ci;
    var Dt = {};
    function Br(e) {
      return (Br = typeof Symbol == "function" && typeof Symbol.iterator == "symbol" ? function(t) {
        return typeof t;
      } : function(t) {
        return t && typeof Symbol == "function" && t.constructor === Symbol && t !== Symbol.prototype ? "symbol" : typeof t;
      })(e);
    }
    function Ln(e, t, n) {
      return (Ln = typeof Reflect < "u" && Reflect.get ? Reflect.get : function(r, o, i) {
        var s = function(l, u) {
          for (; !Object.prototype.hasOwnProperty.call(l, u) && (l = vt(l)) !== null; )
            ;
          return l;
        }(r, o);
        if (s) {
          var a = Object.getOwnPropertyDescriptor(s, o);
          return a.get ? a.get.call(i) : a.value;
        }
      })(e, t, n || e);
    }
    function Wr(e, t) {
      return (Wr = Object.setPrototypeOf || function(n, r) {
        return n.__proto__ = r, n;
      })(e, t);
    }
    function Ai(e, t) {
      return !t || Br(t) !== "object" && typeof t != "function" ? function(n) {
        if (n === void 0)
          throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
        return n;
      }(e) : t;
    }
    function vt(e) {
      return (vt = Object.setPrototypeOf ? Object.getPrototypeOf : function(t) {
        return t.__proto__ || Object.getPrototypeOf(t);
      })(e);
    }
    function Xr(e, t) {
      if (!(e instanceof t))
        throw new TypeError("Cannot call a class as a function");
    }
    function Yr(e, t) {
      for (var n = 0; n < t.length; n++) {
        var r = t[n];
        r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), Object.defineProperty(e, r.key, r);
      }
    }
    function $r(e, t, n) {
      return t && Yr(e.prototype, t), n && Yr(e, n), e;
    }
    function de(e, t, n) {
      return t in e ? Object.defineProperty(e, t, { value: n, enumerable: !0, configurable: !0, writable: !0 }) : e[t] = n, e;
    }
    Object.defineProperty(Dt, "__esModule", { value: !0 }), Dt.initScope = Lr, Dt.Scope = void 0;
    var Fi = function() {
      function e() {
        var t = this;
        Xr(this, e), de(this, "id", "__interact_scope_".concat(Math.floor(100 * Math.random()))), de(this, "isInitialized", !1), de(this, "listenerMaps", []), de(this, "browser", z.default), de(this, "defaults", (0, rt.default)(St.defaults)), de(this, "Eventable", jt.Eventable), de(this, "actions", { map: {}, phases: { start: !0, move: !0, end: !0 }, methodDict: {}, phaselessTypes: {} }), de(this, "interactStatic", (0, An.createInteractStatic)(this)), de(this, "InteractEvent", Et.InteractEvent), de(this, "Interactable", void 0), de(this, "interactables", new on.InteractableSet(this)), de(this, "_win", void 0), de(this, "document", void 0), de(this, "window", void 0), de(this, "documents", []), de(this, "_plugins", { list: [], map: {} }), de(this, "onWindowUnload", function(r) {
          return t.removeDocument(r.target);
        });
        var n = this;
        this.Interactable = function(r) {
          (function(l, u) {
            if (typeof u != "function" && u !== null)
              throw new TypeError("Super expression must either be null or a function");
            l.prototype = Object.create(u && u.prototype, { constructor: { value: l, writable: !0, configurable: !0 } }), u && Wr(l, u);
          })(a, r);
          var o, i, s = (o = a, i = function() {
            if (typeof Reflect > "u" || !Reflect.construct || Reflect.construct.sham)
              return !1;
            if (typeof Proxy == "function")
              return !0;
            try {
              return Boolean.prototype.valueOf.call(Reflect.construct(Boolean, [], function() {
              })), !0;
            } catch {
              return !1;
            }
          }(), function() {
            var l, u = vt(o);
            if (i) {
              var f = vt(this).constructor;
              l = Reflect.construct(u, arguments, f);
            } else
              l = u.apply(this, arguments);
            return Ai(this, l);
          });
          function a() {
            return Xr(this, a), s.apply(this, arguments);
          }
          return $r(a, [{ key: "_defaults", get: function() {
            return n.defaults;
          } }, { key: "set", value: function(l) {
            return Ln(vt(a.prototype), "set", this).call(this, l), n.fire("interactable:set", { options: l, interactable: this }), this;
          } }, { key: "unset", value: function() {
            Ln(vt(a.prototype), "unset", this).call(this), n.interactables.list.splice(n.interactables.list.indexOf(this), 1), n.fire("interactable:unset", { interactable: this });
          } }]), a;
        }(rn.Interactable);
      }
      return $r(e, [{ key: "addListeners", value: function(t, n) {
        this.listenerMaps.push({ id: n, map: t });
      } }, { key: "fire", value: function(t, n) {
        for (var r = 0; r < this.listenerMaps.length; r++) {
          var o = this.listenerMaps[r].map[t];
          if (o && o(n, this, t) === !1)
            return !1;
        }
      } }, { key: "init", value: function(t) {
        return this.isInitialized ? this : Lr(this, t);
      } }, { key: "pluginIsInstalled", value: function(t) {
        return this._plugins.map[t.id] || this._plugins.list.indexOf(t) !== -1;
      } }, { key: "usePlugin", value: function(t, n) {
        if (!this.isInitialized)
          return this;
        if (this.pluginIsInstalled(t))
          return this;
        if (t.id && (this._plugins.map[t.id] = t), this._plugins.list.push(t), t.install && t.install(this, n), t.listeners && t.before) {
          for (var r = 0, o = this.listenerMaps.length, i = t.before.reduce(function(a, l) {
            return a[l] = !0, a[Nr(l)] = !0, a;
          }, {}); r < o; r++) {
            var s = this.listenerMaps[r].id;
            if (i[s] || i[Nr(s)])
              break;
          }
          this.listenerMaps.splice(r, 0, { id: t.id, map: t.listeners });
        } else
          t.listeners && this.listenerMaps.push({ id: t.id, map: t.listeners });
        return this;
      } }, { key: "addDocument", value: function(t, n) {
        if (this.getDocIndex(t) !== -1)
          return !1;
        var r = b.getWindow(t);
        n = n ? (0, P.default)({}, n) : {}, this.documents.push({ doc: t, options: n }), this.events.documents.push(t), t !== this.document && this.events.add(r, "unload", this.onWindowUnload), this.fire("scope:add-document", { doc: t, window: r, scope: this, options: n });
      } }, { key: "removeDocument", value: function(t) {
        var n = this.getDocIndex(t), r = b.getWindow(t), o = this.documents[n].options;
        this.events.remove(r, "unload", this.onWindowUnload), this.documents.splice(n, 1), this.events.documents.splice(n, 1), this.fire("scope:remove-document", { doc: t, window: r, scope: this, options: o });
      } }, { key: "getDocIndex", value: function(t) {
        for (var n = 0; n < this.documents.length; n++)
          if (this.documents[n].doc === t)
            return n;
        return -1;
      } }, { key: "getDocOptions", value: function(t) {
        var n = this.getDocIndex(t);
        return n === -1 ? null : this.documents[n].options;
      } }, { key: "now", value: function() {
        return (this.window.Date || Date).now();
      } }]), e;
    }();
    function Lr(e, t) {
      return e.isInitialized = !0, v.default.window(t) && b.init(t), X.default.init(t), z.default.init(t), je.default.init(t), e.window = t, e.document = t.document, e.usePlugin(un.default), e.usePlugin(an.default), e;
    }
    function Nr(e) {
      return e && e.replace(/\/.*$/, "");
    }
    Dt.Scope = Fi;
    var he = {};
    Object.defineProperty(he, "__esModule", { value: !0 }), he.default = void 0;
    var Hr = new Dt.Scope(), Bi = Hr.interactStatic;
    he.default = Bi;
    var Wi = typeof globalThis < "u" ? globalThis : typeof window < "u" ? window : void 0;
    Hr.init(Wi);
    var cn = {};
    Object.defineProperty(cn, "__esModule", { value: !0 }), cn.default = void 0, cn.default = function() {
    };
    var fn = {};
    Object.defineProperty(fn, "__esModule", { value: !0 }), fn.default = void 0, fn.default = function() {
    };
    var dn = {};
    function Gr(e, t) {
      return function(n) {
        if (Array.isArray(n))
          return n;
      }(e) || function(n, r) {
        if (typeof Symbol < "u" && Symbol.iterator in Object(n)) {
          var o = [], i = !0, s = !1, a = void 0;
          try {
            for (var l, u = n[Symbol.iterator](); !(i = (l = u.next()).done) && (o.push(l.value), !r || o.length !== r); i = !0)
              ;
          } catch (f) {
            s = !0, a = f;
          } finally {
            try {
              i || u.return == null || u.return();
            } finally {
              if (s)
                throw a;
            }
          }
          return o;
        }
      }(e, t) || function(n, r) {
        if (n) {
          if (typeof n == "string")
            return qr(n, r);
          var o = Object.prototype.toString.call(n).slice(8, -1);
          return o === "Object" && n.constructor && (o = n.constructor.name), o === "Map" || o === "Set" ? Array.from(n) : o === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(o) ? qr(n, r) : void 0;
        }
      }(e, t) || function() {
        throw new TypeError(`Invalid attempt to destructure non-iterable instance.
In order to be iterable, non-array objects must have a [Symbol.iterator]() method.`);
      }();
    }
    function qr(e, t) {
      (t == null || t > e.length) && (t = e.length);
      for (var n = 0, r = Array(t); n < t; n++)
        r[n] = e[n];
      return r;
    }
    Object.defineProperty(dn, "__esModule", { value: !0 }), dn.default = void 0, dn.default = function(e) {
      var t = [["x", "y"], ["left", "top"], ["right", "bottom"], ["width", "height"]].filter(function(r) {
        var o = Gr(r, 2), i = o[0], s = o[1];
        return i in e || s in e;
      }), n = function(r, o) {
        for (var i = e.range, s = e.limits, a = s === void 0 ? { left: -1 / 0, right: 1 / 0, top: -1 / 0, bottom: 1 / 0 } : s, l = e.offset, u = l === void 0 ? { x: 0, y: 0 } : l, f = { range: i, grid: e, x: null, y: null }, d = 0; d < t.length; d++) {
          var h = Gr(t[d], 2), g = h[0], p = h[1], x = Math.round((r - u.x) / e[g]), _ = Math.round((o - u.y) / e[p]);
          f[g] = Math.max(a.left, Math.min(a.right, x * e[g] + u.x)), f[p] = Math.max(a.top, Math.min(a.bottom, _ * e[p] + u.y));
        }
        return f;
      };
      return n.grid = e, n.coordFields = t, n;
    };
    var Rt = {};
    Object.defineProperty(Rt, "__esModule", { value: !0 }), Object.defineProperty(Rt, "edgeTarget", { enumerable: !0, get: function() {
      return cn.default;
    } }), Object.defineProperty(Rt, "elements", { enumerable: !0, get: function() {
      return fn.default;
    } }), Object.defineProperty(Rt, "grid", { enumerable: !0, get: function() {
      return dn.default;
    } });
    var pn = {};
    Object.defineProperty(pn, "__esModule", { value: !0 }), pn.default = void 0;
    var Xi = { id: "snappers", install: function(e) {
      var t = e.interactStatic;
      t.snappers = (0, P.default)(t.snappers || {}, Rt), t.createSnapGrid = t.snappers.grid;
    } };
    pn.default = Xi;
    var ht = {};
    function Vr(e, t) {
      var n = Object.keys(e);
      if (Object.getOwnPropertySymbols) {
        var r = Object.getOwnPropertySymbols(e);
        t && (r = r.filter(function(o) {
          return Object.getOwnPropertyDescriptor(e, o).enumerable;
        })), n.push.apply(n, r);
      }
      return n;
    }
    function Nn(e) {
      for (var t = 1; t < arguments.length; t++) {
        var n = arguments[t] != null ? arguments[t] : {};
        t % 2 ? Vr(Object(n), !0).forEach(function(r) {
          Yi(e, r, n[r]);
        }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(e, Object.getOwnPropertyDescriptors(n)) : Vr(Object(n)).forEach(function(r) {
          Object.defineProperty(e, r, Object.getOwnPropertyDescriptor(n, r));
        });
      }
      return e;
    }
    function Yi(e, t, n) {
      return t in e ? Object.defineProperty(e, t, { value: n, enumerable: !0, configurable: !0, writable: !0 }) : e[t] = n, e;
    }
    Object.defineProperty(ht, "__esModule", { value: !0 }), ht.aspectRatio = ht.default = void 0;
    var Ur = { start: function(e) {
      var t = e.state, n = e.rect, r = e.edges, o = e.pageCoords, i = t.options.ratio, s = t.options, a = s.equalDelta, l = s.modifiers;
      i === "preserve" && (i = n.width / n.height), t.startCoords = (0, P.default)({}, o), t.startRect = (0, P.default)({}, n), t.ratio = i, t.equalDelta = a;
      var u = t.linkedEdges = { top: r.top || r.left && !r.bottom, left: r.left || r.top && !r.right, bottom: r.bottom || r.right && !r.top, right: r.right || r.bottom && !r.left };
      if (t.xIsPrimaryAxis = !(!r.left && !r.right), t.equalDelta)
        t.edgeSign = (u.left ? 1 : -1) * (u.top ? 1 : -1);
      else {
        var f = t.xIsPrimaryAxis ? u.top : u.left;
        t.edgeSign = f ? -1 : 1;
      }
      if ((0, P.default)(e.edges, u), l && l.length) {
        var d = new ot.default(e.interaction);
        d.copyFrom(e.interaction.modification), d.prepareStates(l), t.subModification = d, d.startAll(Nn({}, e));
      }
    }, set: function(e) {
      var t = e.state, n = e.rect, r = e.coords, o = (0, P.default)({}, r), i = t.equalDelta ? $i : Li;
      if (i(t, t.xIsPrimaryAxis, r, n), !t.subModification)
        return null;
      var s = (0, P.default)({}, n);
      (0, B.addEdges)(t.linkedEdges, s, { x: r.x - o.x, y: r.y - o.y });
      var a = t.subModification.setAll(Nn(Nn({}, e), {}, { rect: s, edges: t.linkedEdges, pageCoords: r, prevCoords: r, prevRect: s })), l = a.delta;
      return a.changed && (i(t, Math.abs(l.x) > Math.abs(l.y), a.coords, a.rect), (0, P.default)(r, a.coords)), a.eventProps;
    }, defaults: { ratio: "preserve", equalDelta: !1, modifiers: [], enabled: !1 } };
    function $i(e, t, n) {
      var r = e.startCoords, o = e.edgeSign;
      t ? n.y = r.y + (n.x - r.x) * o : n.x = r.x + (n.y - r.y) * o;
    }
    function Li(e, t, n, r) {
      var o = e.startRect, i = e.startCoords, s = e.ratio, a = e.edgeSign;
      if (t) {
        var l = r.width / s;
        n.y = i.y + (l - o.height) * a;
      } else {
        var u = r.height * s;
        n.x = i.x + (u - o.width) * a;
      }
    }
    ht.aspectRatio = Ur;
    var Ni = (0, fe.makeModifier)(Ur, "aspectRatio");
    ht.default = Ni;
    var st = {};
    Object.defineProperty(st, "__esModule", { value: !0 }), st.default = void 0;
    var Kr = function() {
    };
    Kr._defaults = {};
    var Hi = Kr;
    st.default = Hi;
    var Hn = {};
    Object.defineProperty(Hn, "__esModule", { value: !0 }), Object.defineProperty(Hn, "default", { enumerable: !0, get: function() {
      return st.default;
    } });
    var ye = {};
    function Gn(e, t, n) {
      return v.default.func(e) ? B.resolveRectLike(e, t.interactable, t.element, [n.x, n.y, t]) : B.resolveRectLike(e, t.interactable, t.element);
    }
    Object.defineProperty(ye, "__esModule", { value: !0 }), ye.getRestrictionRect = Gn, ye.restrict = ye.default = void 0;
    var Zr = { start: function(e) {
      var t = e.rect, n = e.startOffset, r = e.state, o = e.interaction, i = e.pageCoords, s = r.options, a = s.elementRect, l = (0, P.default)({ left: 0, top: 0, right: 0, bottom: 0 }, s.offset || {});
      if (t && a) {
        var u = Gn(s.restriction, o, i);
        if (u) {
          var f = u.right - u.left - t.width, d = u.bottom - u.top - t.height;
          f < 0 && (l.left += f, l.right += f), d < 0 && (l.top += d, l.bottom += d);
        }
        l.left += n.left - t.width * a.left, l.top += n.top - t.height * a.top, l.right += n.right - t.width * (1 - a.right), l.bottom += n.bottom - t.height * (1 - a.bottom);
      }
      r.offset = l;
    }, set: function(e) {
      var t = e.coords, n = e.interaction, r = e.state, o = r.options, i = r.offset, s = Gn(o.restriction, n, t);
      if (s) {
        var a = B.xywhToTlbr(s);
        t.x = Math.max(Math.min(a.right - i.right, t.x), a.left + i.left), t.y = Math.max(Math.min(a.bottom - i.bottom, t.y), a.top + i.top);
      }
    }, defaults: { restriction: null, elementRect: null, offset: null, endOnly: !1, enabled: !1 } };
    ye.restrict = Zr;
    var Gi = (0, fe.makeModifier)(Zr, "restrict");
    ye.default = Gi;
    var Ce = {};
    Object.defineProperty(Ce, "__esModule", { value: !0 }), Ce.restrictEdges = Ce.default = void 0;
    var Jr = { top: 1 / 0, left: 1 / 0, bottom: -1 / 0, right: -1 / 0 }, Qr = { top: -1 / 0, left: -1 / 0, bottom: 1 / 0, right: 1 / 0 };
    function eo(e, t) {
      for (var n = ["top", "left", "bottom", "right"], r = 0; r < n.length; r++) {
        var o = n[r];
        o in e || (e[o] = t[o]);
      }
      return e;
    }
    var to = { noInner: Jr, noOuter: Qr, start: function(e) {
      var t, n = e.interaction, r = e.startOffset, o = e.state, i = o.options;
      if (i) {
        var s = (0, ye.getRestrictionRect)(i.offset, n, n.coords.start.page);
        t = B.rectToXY(s);
      }
      t = t || { x: 0, y: 0 }, o.offset = { top: t.y + r.top, left: t.x + r.left, bottom: t.y - r.bottom, right: t.x - r.right };
    }, set: function(e) {
      var t = e.coords, n = e.edges, r = e.interaction, o = e.state, i = o.offset, s = o.options;
      if (n) {
        var a = (0, P.default)({}, t), l = (0, ye.getRestrictionRect)(s.inner, r, a) || {}, u = (0, ye.getRestrictionRect)(s.outer, r, a) || {};
        eo(l, Jr), eo(u, Qr), n.top ? t.y = Math.min(Math.max(u.top + i.top, a.y), l.top + i.top) : n.bottom && (t.y = Math.max(Math.min(u.bottom + i.bottom, a.y), l.bottom + i.bottom)), n.left ? t.x = Math.min(Math.max(u.left + i.left, a.x), l.left + i.left) : n.right && (t.x = Math.max(Math.min(u.right + i.right, a.x), l.right + i.right));
      }
    }, defaults: { inner: null, outer: null, offset: null, endOnly: !1, enabled: !1 } };
    Ce.restrictEdges = to;
    var qi = (0, fe.makeModifier)(to, "restrictEdges");
    Ce.default = qi;
    var gt = {};
    Object.defineProperty(gt, "__esModule", { value: !0 }), gt.restrictRect = gt.default = void 0;
    var Vi = (0, P.default)({ get elementRect() {
      return { top: 0, left: 0, bottom: 1, right: 1 };
    }, set elementRect(e) {
    } }, ye.restrict.defaults), no = { start: ye.restrict.start, set: ye.restrict.set, defaults: Vi };
    gt.restrictRect = no;
    var Ui = (0, fe.makeModifier)(no, "restrictRect");
    gt.default = Ui;
    var yt = {};
    Object.defineProperty(yt, "__esModule", { value: !0 }), yt.restrictSize = yt.default = void 0;
    var Ki = { width: -1 / 0, height: -1 / 0 }, Zi = { width: 1 / 0, height: 1 / 0 }, ro = { start: function(e) {
      return Ce.restrictEdges.start(e);
    }, set: function(e) {
      var t = e.interaction, n = e.state, r = e.rect, o = e.edges, i = n.options;
      if (o) {
        var s = B.tlbrToXywh((0, ye.getRestrictionRect)(i.min, t, e.coords)) || Ki, a = B.tlbrToXywh((0, ye.getRestrictionRect)(i.max, t, e.coords)) || Zi;
        n.options = { endOnly: i.endOnly, inner: (0, P.default)({}, Ce.restrictEdges.noInner), outer: (0, P.default)({}, Ce.restrictEdges.noOuter) }, o.top ? (n.options.inner.top = r.bottom - s.height, n.options.outer.top = r.bottom - a.height) : o.bottom && (n.options.inner.bottom = r.top + s.height, n.options.outer.bottom = r.top + a.height), o.left ? (n.options.inner.left = r.right - s.width, n.options.outer.left = r.right - a.width) : o.right && (n.options.inner.right = r.left + s.width, n.options.outer.right = r.left + a.width), Ce.restrictEdges.set(e), n.options = i;
      }
    }, defaults: { min: null, max: null, endOnly: !1, enabled: !1 } };
    yt.restrictSize = ro;
    var Ji = (0, fe.makeModifier)(ro, "restrictSize");
    yt.default = Ji;
    var qn = {};
    Object.defineProperty(qn, "__esModule", { value: !0 }), Object.defineProperty(qn, "default", { enumerable: !0, get: function() {
      return st.default;
    } });
    var Qe = {};
    Object.defineProperty(Qe, "__esModule", { value: !0 }), Qe.snap = Qe.default = void 0;
    var oo = { start: function(e) {
      var t, n = e.interaction, r = e.interactable, o = e.element, i = e.rect, s = e.state, a = e.startOffset, l = s.options, u = l.offsetWithOrigin ? function(h) {
        var g = h.interaction.element;
        return (0, B.rectToXY)((0, B.resolveRectLike)(h.state.options.origin, null, null, [g])) || (0, Te.default)(h.interactable, g, h.interaction.prepared.name);
      }(e) : { x: 0, y: 0 };
      if (l.offset === "startCoords")
        t = { x: n.coords.start.page.x, y: n.coords.start.page.y };
      else {
        var f = (0, B.resolveRectLike)(l.offset, r, o, [n]);
        (t = (0, B.rectToXY)(f) || { x: 0, y: 0 }).x += u.x, t.y += u.y;
      }
      var d = l.relativePoints;
      s.offsets = i && d && d.length ? d.map(function(h, g) {
        return { index: g, relativePoint: h, x: a.left - i.width * h.x + t.x, y: a.top - i.height * h.y + t.y };
      }) : [{ index: 0, relativePoint: null, x: t.x, y: t.y }];
    }, set: function(e) {
      var t = e.interaction, n = e.coords, r = e.state, o = r.options, i = r.offsets, s = (0, Te.default)(t.interactable, t.element, t.prepared.name), a = (0, P.default)({}, n), l = [];
      o.offsetWithOrigin || (a.x -= s.x, a.y -= s.y);
      for (var u = 0; u < i.length; u++)
        for (var f = i[u], d = a.x - f.x, h = a.y - f.y, g = 0, p = o.targets.length; g < p; g++) {
          var x, _ = o.targets[g];
          (x = v.default.func(_) ? _(d, h, t._proxy, f, g) : _) && l.push({ x: (v.default.number(x.x) ? x.x : d) + f.x, y: (v.default.number(x.y) ? x.y : h) + f.y, range: v.default.number(x.range) ? x.range : o.range, source: _, index: g, offset: f });
        }
      for (var w = { target: null, inRange: !1, distance: 0, range: 0, delta: { x: 0, y: 0 } }, T = 0; T < l.length; T++) {
        var D = l[T], A = D.range, N = D.x - a.x, ee = D.y - a.y, W = (0, De.default)(N, ee), U = W <= A;
        A === 1 / 0 && w.inRange && w.range !== 1 / 0 && (U = !1), w.target && !(U ? w.inRange && A !== 1 / 0 ? W / A < w.distance / w.range : A === 1 / 0 && w.range !== 1 / 0 || W < w.distance : !w.inRange && W < w.distance) || (w.target = D, w.distance = W, w.range = A, w.inRange = U, w.delta.x = N, w.delta.y = ee);
      }
      return w.inRange && (n.x = w.target.x, n.y = w.target.y), r.closest = w, w;
    }, defaults: { range: 1 / 0, targets: null, offset: null, offsetWithOrigin: !0, origin: null, relativePoints: null, endOnly: !1, enabled: !1 } };
    Qe.snap = oo;
    var Qi = (0, fe.makeModifier)(oo, "snap");
    Qe.default = Qi;
    var Le = {};
    function io(e, t) {
      (t == null || t > e.length) && (t = e.length);
      for (var n = 0, r = Array(t); n < t; n++)
        r[n] = e[n];
      return r;
    }
    Object.defineProperty(Le, "__esModule", { value: !0 }), Le.snapSize = Le.default = void 0;
    var ao = { start: function(e) {
      var t = e.state, n = e.edges, r = t.options;
      if (!n)
        return null;
      e.state = { options: { targets: null, relativePoints: [{ x: n.left ? 0 : 1, y: n.top ? 0 : 1 }], offset: r.offset || "self", origin: { x: 0, y: 0 }, range: r.range } }, t.targetFields = t.targetFields || [["width", "height"], ["x", "y"]], Qe.snap.start(e), t.offsets = e.state.offsets, e.state = t;
    }, set: function(e) {
      var t, n, r = e.interaction, o = e.state, i = e.coords, s = o.options, a = o.offsets, l = { x: i.x - a[0].x, y: i.y - a[0].y };
      o.options = (0, P.default)({}, s), o.options.targets = [];
      for (var u = 0; u < (s.targets || []).length; u++) {
        var f = (s.targets || [])[u], d = void 0;
        if (d = v.default.func(f) ? f(l.x, l.y, r) : f) {
          for (var h = 0; h < o.targetFields.length; h++) {
            var g = (t = o.targetFields[h], n = 2, function(w) {
              if (Array.isArray(w))
                return w;
            }(t) || function(w, T) {
              if (typeof Symbol < "u" && Symbol.iterator in Object(w)) {
                var D = [], A = !0, N = !1, ee = void 0;
                try {
                  for (var W, U = w[Symbol.iterator](); !(A = (W = U.next()).done) && (D.push(W.value), !T || D.length !== T); A = !0)
                    ;
                } catch (Ee) {
                  N = !0, ee = Ee;
                } finally {
                  try {
                    A || U.return == null || U.return();
                  } finally {
                    if (N)
                      throw ee;
                  }
                }
                return D;
              }
            }(t, n) || function(w, T) {
              if (w) {
                if (typeof w == "string")
                  return io(w, T);
                var D = Object.prototype.toString.call(w).slice(8, -1);
                return D === "Object" && w.constructor && (D = w.constructor.name), D === "Map" || D === "Set" ? Array.from(w) : D === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(D) ? io(w, T) : void 0;
              }
            }(t, n) || function() {
              throw new TypeError(`Invalid attempt to destructure non-iterable instance.
In order to be iterable, non-array objects must have a [Symbol.iterator]() method.`);
            }()), p = g[0], x = g[1];
            if (p in d || x in d) {
              d.x = d[p], d.y = d[x];
              break;
            }
          }
          o.options.targets.push(d);
        }
      }
      var _ = Qe.snap.set(e);
      return o.options = s, _;
    }, defaults: { range: 1 / 0, targets: null, offset: null, endOnly: !1, enabled: !1 } };
    Le.snapSize = ao;
    var ea = (0, fe.makeModifier)(ao, "snapSize");
    Le.default = ea;
    var mt = {};
    Object.defineProperty(mt, "__esModule", { value: !0 }), mt.snapEdges = mt.default = void 0;
    var so = { start: function(e) {
      var t = e.edges;
      return t ? (e.state.targetFields = e.state.targetFields || [[t.left ? "left" : "right", t.top ? "top" : "bottom"]], Le.snapSize.start(e)) : null;
    }, set: Le.snapSize.set, defaults: (0, P.default)((0, rt.default)(Le.snapSize.defaults), { targets: null, range: null, offset: { x: 0, y: 0 } }) };
    mt.snapEdges = so;
    var ta = (0, fe.makeModifier)(so, "snapEdges");
    mt.default = ta;
    var Vn = {};
    Object.defineProperty(Vn, "__esModule", { value: !0 }), Object.defineProperty(Vn, "default", { enumerable: !0, get: function() {
      return st.default;
    } });
    var Un = {};
    Object.defineProperty(Un, "__esModule", { value: !0 }), Object.defineProperty(Un, "default", { enumerable: !0, get: function() {
      return st.default;
    } });
    var bt = {};
    Object.defineProperty(bt, "__esModule", { value: !0 }), bt.default = void 0;
    var na = { aspectRatio: ht.default, restrictEdges: Ce.default, restrict: ye.default, restrictRect: gt.default, restrictSize: yt.default, snapEdges: mt.default, snap: Qe.default, snapSize: Le.default, spring: Vn.default, avoid: Hn.default, transform: Un.default, rubberband: qn.default };
    bt.default = na;
    var vn = {};
    Object.defineProperty(vn, "__esModule", { value: !0 }), vn.default = void 0;
    var ra = { id: "modifiers", install: function(e) {
      var t = e.interactStatic;
      for (var n in e.usePlugin(fe.default), e.usePlugin(pn.default), t.modifiers = bt.default, bt.default) {
        var r = bt.default[n], o = r._defaults, i = r._methods;
        o._methods = i, e.defaults.perAction[n] = o;
      }
    } };
    vn.default = ra;
    var lt = {};
    function lo(e) {
      return (lo = typeof Symbol == "function" && typeof Symbol.iterator == "symbol" ? function(t) {
        return typeof t;
      } : function(t) {
        return t && typeof Symbol == "function" && t.constructor === Symbol && t !== Symbol.prototype ? "symbol" : typeof t;
      })(e);
    }
    function oa(e, t) {
      for (var n = 0; n < t.length; n++) {
        var r = t[n];
        r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), Object.defineProperty(e, r.key, r);
      }
    }
    function uo(e, t) {
      return (uo = Object.setPrototypeOf || function(n, r) {
        return n.__proto__ = r, n;
      })(e, t);
    }
    function ia(e, t) {
      return !t || lo(t) !== "object" && typeof t != "function" ? be(e) : t;
    }
    function be(e) {
      if (e === void 0)
        throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
      return e;
    }
    function Kn(e) {
      return (Kn = Object.setPrototypeOf ? Object.getPrototypeOf : function(t) {
        return t.__proto__ || Object.getPrototypeOf(t);
      })(e);
    }
    function ke(e, t, n) {
      return t in e ? Object.defineProperty(e, t, { value: n, enumerable: !0, configurable: !0, writable: !0 }) : e[t] = n, e;
    }
    Object.defineProperty(lt, "__esModule", { value: !0 }), lt.PointerEvent = lt.default = void 0;
    var aa = function(e) {
      (function(a, l) {
        if (typeof l != "function" && l !== null)
          throw new TypeError("Super expression must either be null or a function");
        a.prototype = Object.create(l && l.prototype, { constructor: { value: a, writable: !0, configurable: !0 } }), l && uo(a, l);
      })(s, e);
      var t, n, r, o, i = (r = s, o = function() {
        if (typeof Reflect > "u" || !Reflect.construct || Reflect.construct.sham)
          return !1;
        if (typeof Proxy == "function")
          return !0;
        try {
          return Boolean.prototype.valueOf.call(Reflect.construct(Boolean, [], function() {
          })), !0;
        } catch {
          return !1;
        }
      }(), function() {
        var a, l = Kn(r);
        if (o) {
          var u = Kn(this).constructor;
          a = Reflect.construct(l, arguments, u);
        } else
          a = l.apply(this, arguments);
        return ia(this, a);
      });
      function s(a, l, u, f, d, h) {
        var g;
        if (function(_, w) {
          if (!(_ instanceof w))
            throw new TypeError("Cannot call a class as a function");
        }(this, s), ke(be(g = i.call(this, d)), "type", void 0), ke(be(g), "originalEvent", void 0), ke(be(g), "pointerId", void 0), ke(be(g), "pointerType", void 0), ke(be(g), "double", void 0), ke(be(g), "pageX", void 0), ke(be(g), "pageY", void 0), ke(be(g), "clientX", void 0), ke(be(g), "clientY", void 0), ke(be(g), "dt", void 0), ke(be(g), "eventable", void 0), E.pointerExtend(be(g), u), u !== l && E.pointerExtend(be(g), l), g.timeStamp = h, g.originalEvent = u, g.type = a, g.pointerId = E.getPointerId(l), g.pointerType = E.getPointerType(l), g.target = f, g.currentTarget = null, a === "tap") {
          var p = d.getPointerIndex(l);
          g.dt = g.timeStamp - d.pointers[p].downTime;
          var x = g.timeStamp - d.tapTime;
          g.double = !!(d.prevTap && d.prevTap.type !== "doubletap" && d.prevTap.target === g.target && x < 500);
        } else
          a === "doubletap" && (g.dt = l.timeStamp - d.tapTime);
        return g;
      }
      return t = s, (n = [{ key: "_subtractOrigin", value: function(a) {
        var l = a.x, u = a.y;
        return this.pageX -= l, this.pageY -= u, this.clientX -= l, this.clientY -= u, this;
      } }, { key: "_addOrigin", value: function(a) {
        var l = a.x, u = a.y;
        return this.pageX += l, this.pageY += u, this.clientX += l, this.clientY += u, this;
      } }, { key: "preventDefault", value: function() {
        this.originalEvent.preventDefault();
      } }]) && oa(t.prototype, n), s;
    }(Ke.BaseEvent);
    lt.PointerEvent = lt.default = aa;
    var Ct = {};
    Object.defineProperty(Ct, "__esModule", { value: !0 }), Ct.default = void 0;
    var hn = { id: "pointer-events/base", before: ["inertia", "modifiers", "auto-start", "actions"], install: function(e) {
      e.pointerEvents = hn, e.defaults.actions.pointerEvents = hn.defaults, (0, P.default)(e.actions.phaselessTypes, hn.types);
    }, listeners: { "interactions:new": function(e) {
      var t = e.interaction;
      t.prevTap = null, t.tapTime = 0;
    }, "interactions:update-pointer": function(e) {
      var t = e.down, n = e.pointerInfo;
      !t && n.hold || (n.hold = { duration: 1 / 0, timeout: null });
    }, "interactions:move": function(e, t) {
      var n = e.interaction, r = e.pointer, o = e.event, i = e.eventTarget;
      e.duplicate || n.pointerIsDown && !n.pointerWasMoved || (n.pointerIsDown && Zn(e), et({ interaction: n, pointer: r, event: o, eventTarget: i, type: "move" }, t));
    }, "interactions:down": function(e, t) {
      (function(n, r) {
        for (var o = n.interaction, i = n.pointer, s = n.event, a = n.eventTarget, l = n.pointerIndex, u = o.pointers[l].hold, f = S.getPath(a), d = { interaction: o, pointer: i, event: s, eventTarget: a, type: "hold", targets: [], path: f, node: null }, h = 0; h < f.length; h++) {
          var g = f[h];
          d.node = g, r.fire("pointerEvents:collect-targets", d);
        }
        if (d.targets.length) {
          for (var p = 1 / 0, x = 0; x < d.targets.length; x++) {
            var _ = d.targets[x].eventable.options.holdDuration;
            _ < p && (p = _);
          }
          u.duration = p, u.timeout = setTimeout(function() {
            et({ interaction: o, eventTarget: a, pointer: i, event: s, type: "hold" }, r);
          }, p);
        }
      })(e, t), et(e, t);
    }, "interactions:up": function(e, t) {
      Zn(e), et(e, t), function(n, r) {
        var o = n.interaction, i = n.pointer, s = n.event, a = n.eventTarget;
        o.pointerWasMoved || et({ interaction: o, eventTarget: a, pointer: i, event: s, type: "tap" }, r);
      }(e, t);
    }, "interactions:cancel": function(e, t) {
      Zn(e), et(e, t);
    } }, PointerEvent: lt.PointerEvent, fire: et, collectEventTargets: co, defaults: { holdDuration: 600, ignoreFrom: null, allowFrom: null, origin: { x: 0, y: 0 } }, types: { down: !0, move: !0, up: !0, cancel: !0, tap: !0, doubletap: !0, hold: !0 } };
    function et(e, t) {
      var n = e.interaction, r = e.pointer, o = e.event, i = e.eventTarget, s = e.type, a = e.targets, l = a === void 0 ? co(e, t) : a, u = new lt.PointerEvent(s, r, o, i, n, t.now());
      t.fire("pointerEvents:new", { pointerEvent: u });
      for (var f = { interaction: n, pointer: r, event: o, eventTarget: i, targets: l, type: s, pointerEvent: u }, d = 0; d < l.length; d++) {
        var h = l[d];
        for (var g in h.props || {})
          u[g] = h.props[g];
        var p = (0, Te.default)(h.eventable, h.node);
        if (u._subtractOrigin(p), u.eventable = h.eventable, u.currentTarget = h.node, h.eventable.fire(u), u._addOrigin(p), u.immediatePropagationStopped || u.propagationStopped && d + 1 < l.length && l[d + 1].node !== u.currentTarget)
          break;
      }
      if (t.fire("pointerEvents:fired", f), s === "tap") {
        var x = u.double ? et({ interaction: n, pointer: r, event: o, eventTarget: i, type: "doubletap" }, t) : u;
        n.prevTap = x, n.tapTime = x.timeStamp;
      }
      return u;
    }
    function co(e, t) {
      var n = e.interaction, r = e.pointer, o = e.event, i = e.eventTarget, s = e.type, a = n.getPointerIndex(r), l = n.pointers[a];
      if (s === "tap" && (n.pointerWasMoved || !l || l.downTarget !== i))
        return [];
      for (var u = S.getPath(i), f = { interaction: n, pointer: r, event: o, eventTarget: i, type: s, path: u, targets: [], node: null }, d = 0; d < u.length; d++) {
        var h = u[d];
        f.node = h, t.fire("pointerEvents:collect-targets", f);
      }
      return s === "hold" && (f.targets = f.targets.filter(function(g) {
        var p;
        return g.eventable.options.holdDuration === ((p = n.pointers[a]) == null ? void 0 : p.hold.duration);
      })), f.targets;
    }
    function Zn(e) {
      var t = e.interaction, n = e.pointerIndex, r = t.pointers[n].hold;
      r && r.timeout && (clearTimeout(r.timeout), r.timeout = null);
    }
    var sa = hn;
    Ct.default = sa;
    var gn = {};
    function la(e) {
      var t = e.interaction;
      t.holdIntervalHandle && (clearInterval(t.holdIntervalHandle), t.holdIntervalHandle = null);
    }
    Object.defineProperty(gn, "__esModule", { value: !0 }), gn.default = void 0;
    var ua = { id: "pointer-events/holdRepeat", install: function(e) {
      e.usePlugin(Ct.default);
      var t = e.pointerEvents;
      t.defaults.holdRepeatInterval = 0, t.types.holdrepeat = e.actions.phaselessTypes.holdrepeat = !0;
    }, listeners: ["move", "up", "cancel", "endall"].reduce(function(e, t) {
      return e["pointerEvents:".concat(t)] = la, e;
    }, { "pointerEvents:new": function(e) {
      var t = e.pointerEvent;
      t.type === "hold" && (t.count = (t.count || 0) + 1);
    }, "pointerEvents:fired": function(e, t) {
      var n = e.interaction, r = e.pointerEvent, o = e.eventTarget, i = e.targets;
      if (r.type === "hold" && i.length) {
        var s = i[0].eventable.options.holdRepeatInterval;
        s <= 0 || (n.holdIntervalHandle = setTimeout(function() {
          t.pointerEvents.fire({ interaction: n, eventTarget: o, type: "hold", pointer: r, event: r }, t);
        }, s));
      }
    } }) };
    gn.default = ua;
    var yn = {};
    function ca(e) {
      return (0, P.default)(this.events.options, e), this;
    }
    Object.defineProperty(yn, "__esModule", { value: !0 }), yn.default = void 0;
    var fa = { id: "pointer-events/interactableTargets", install: function(e) {
      var t = e.Interactable;
      t.prototype.pointerEvents = ca;
      var n = t.prototype._backCompatOption;
      t.prototype._backCompatOption = function(r, o) {
        var i = n.call(this, r, o);
        return i === this && (this.events.options[r] = o), i;
      };
    }, listeners: { "pointerEvents:collect-targets": function(e, t) {
      var n = e.targets, r = e.node, o = e.type, i = e.eventTarget;
      t.interactables.forEachMatch(r, function(s) {
        var a = s.events, l = a.options;
        a.types[o] && a.types[o].length && s.testIgnoreAllow(l, r, i) && n.push({ node: r, eventable: a, props: { interactable: s } });
      });
    }, "interactable:new": function(e) {
      var t = e.interactable;
      t.events.getRect = function(n) {
        return t.getRect(n);
      };
    }, "interactable:set": function(e, t) {
      var n = e.interactable, r = e.options;
      (0, P.default)(n.events.options, t.pointerEvents.defaults), (0, P.default)(n.events.options, r.pointerEvents || {});
    } } };
    yn.default = fa;
    var mn = {};
    Object.defineProperty(mn, "__esModule", { value: !0 }), mn.default = void 0;
    var da = { id: "pointer-events", install: function(e) {
      e.usePlugin(Ct), e.usePlugin(gn.default), e.usePlugin(yn.default);
    } };
    mn.default = da;
    var At = {};
    function fo(e) {
      var t = e.Interactable;
      e.actions.phases.reflow = !0, t.prototype.reflow = function(n) {
        return function(r, o, i) {
          for (var s = v.default.string(r.target) ? I.from(r._context.querySelectorAll(r.target)) : [r.target], a = i.window.Promise, l = a ? [] : null, u = function() {
            var d = s[f], h = r.getRect(d);
            if (!h)
              return "break";
            var g = I.find(i.interactions.list, function(T) {
              return T.interacting() && T.interactable === r && T.element === d && T.prepared.name === o.name;
            }), p = void 0;
            if (g)
              g.move(), l && (p = g._reflowPromise || new a(function(T) {
                g._reflowResolve = T;
              }));
            else {
              var x = (0, B.tlbrToXywh)(h), _ = { page: { x: x.x, y: x.y }, client: { x: x.x, y: x.y }, timeStamp: i.now() }, w = E.coordsToEvent(_);
              p = function(T, D, A, N, ee) {
                var W = T.interactions.new({ pointerType: "reflow" }), U = { interaction: W, event: ee, pointer: ee, eventTarget: A, phase: "reflow" };
                W.interactable = D, W.element = A, W.prevEvent = ee, W.updatePointer(ee, ee, A, !0), E.setZeroCoords(W.coords.delta), (0, _e.copyAction)(W.prepared, N), W._doPhase(U);
                var Ee = T.window.Promise, Ne = Ee ? new Ee(function(Jn) {
                  W._reflowResolve = Jn;
                }) : void 0;
                return W._reflowPromise = Ne, W.start(N, D, A), W._interacting ? (W.move(U), W.end(ee)) : (W.stop(), W._reflowResolve()), W.removePointer(ee, ee), Ne;
              }(i, r, d, o, w);
            }
            l && l.push(p);
          }, f = 0; f < s.length && u() !== "break"; f++)
            ;
          return l && a.all(l).then(function() {
            return r;
          });
        }(this, n, e);
      };
    }
    Object.defineProperty(At, "__esModule", { value: !0 }), At.install = fo, At.default = void 0;
    var pa = { id: "reflow", install: fo, listeners: { "interactions:stop": function(e, t) {
      var n = e.interaction;
      n.pointerType === "reflow" && (n._reflowResolve && n._reflowResolve(), I.remove(t.interactions.list, n));
    } } };
    At.default = pa;
    var Se = { exports: {} };
    function po(e) {
      return (po = typeof Symbol == "function" && typeof Symbol.iterator == "symbol" ? function(t) {
        return typeof t;
      } : function(t) {
        return t && typeof Symbol == "function" && t.constructor === Symbol && t !== Symbol.prototype ? "symbol" : typeof t;
      })(e);
    }
    Object.defineProperty(Se.exports, "__esModule", { value: !0 }), Se.exports.default = void 0, he.default.use(dt.default), he.default.use(at.default), he.default.use(mn.default), he.default.use(pt.default), he.default.use(vn.default), he.default.use(Zt.default), he.default.use(Ht.default), he.default.use(Je.default), he.default.use(At.default);
    var va = he.default;
    if (Se.exports.default = va, po(Se) === "object" && Se)
      try {
        Se.exports = he.default;
      } catch {
      }
    he.default.default = he.default, Se = Se.exports;
    var ut = { exports: {} };
    function vo(e) {
      return (vo = typeof Symbol == "function" && typeof Symbol.iterator == "symbol" ? function(t) {
        return typeof t;
      } : function(t) {
        return t && typeof Symbol == "function" && t.constructor === Symbol && t !== Symbol.prototype ? "symbol" : typeof t;
      })(e);
    }
    Object.defineProperty(ut.exports, "__esModule", { value: !0 }), ut.exports.default = void 0;
    var ha = Se.default;
    if (ut.exports.default = ha, vo(ut) === "object" && ut)
      try {
        ut.exports = Se.default;
      } catch {
      }
    return Se.default.default = Se.default, ut.exports;
  });
})(Eo);
const ho = /* @__PURE__ */ _a(Eo.exports);
function Mo(c) {
  return { all: c = c || /* @__PURE__ */ new Map(), on: function(y, m) {
    var b = c.get(y);
    b ? b.push(m) : c.set(y, [m]);
  }, off: function(y, m) {
    var b = c.get(y);
    b && (m ? b.splice(b.indexOf(m) >>> 0, 1) : c.set(y, []));
  }, emit: function(y, m) {
    var b = c.get(y);
    b && b.slice().map(function(k) {
      k(m);
    }), (b = c.get("*")) && b.slice().map(function(k) {
      k(y, m);
    });
  } };
}
function Oa(c) {
  let y = 0, m = 0;
  for (let b = 0, k = c.length; b < k; b++)
    m = c[b].y + c[b].h, m > y && (y = m);
  return y;
}
function Pa(c) {
  return JSON.parse(JSON.stringify(c));
}
function Qn(c) {
  const y = Array(c.length);
  for (let m = 0, b = c.length; m < b; m++)
    y[m] = Pa(c[m]);
  return y;
}
function To(c, y) {
  return !(c === y || c.x + c.w <= y.x || c.x >= y.x + y.w || c.y + c.h <= y.y || c.y >= y.y + y.h);
}
function Ft(c, y) {
  const m = ko(c), b = Io(c), k = Array(c.length);
  for (let M = 0, L = b.length; M < L; M++) {
    let v = b[M];
    v.static || (v = Sa(m, v, y), m.push(v)), k[c.indexOf(v)] = v, v.moved = !1;
  }
  return k;
}
function Sa(c, y, m) {
  if (m)
    for (; y.y > 0 && !wn(c, y); )
      y.y--;
  let b = {};
  for (; b = wn(c, y); )
    y.y = b.y + b.h;
  return y;
}
function Ea(c, y) {
  const m = ko(c);
  for (let b = 0, k = c.length; b < k; b++) {
    const M = c[b];
    if (M.x + M.w > y.cols && (M.x = y.cols - M.w), M.x < 0 && (M.x = 0, M.w = y.cols), !M.static)
      m.push(M);
    else
      for (; wn(m, M); )
        M.y++;
  }
  return c;
}
function go(c, y) {
  for (let m = 0, b = c.length; m < b; m++)
    if (c[m].i === y)
      return c[m];
}
function wn(c, y) {
  for (let m = 0, b = c.length; m < b; m++)
    if (To(c[m], y))
      return c[m];
}
function jo(c, y) {
  return c.filter((m) => To(m, y));
}
function ko(c) {
  return c.filter((y) => y.static);
}
function er(c, y, m, b, k, M = !1) {
  if (y.static)
    return c;
  const L = y.x, v = y.y, oe = b && y.y > b;
  typeof m == "number" && (y.x = m), typeof b == "number" && (y.y = b), y.moved = !0;
  let te = Io(c);
  oe && (te = te.reverse());
  const K = jo(te, y);
  if (M && K.length)
    return y.x = L, y.y = v, y.moved = !1, c;
  for (let ge = 0, le = K.length; ge < le; ge++) {
    const pe = K[ge];
    pe.moved || y.y > pe.y && y.y - pe.y > pe.h / 4 || (pe.static ? c = yo(c, pe, y, k) : c = yo(c, y, pe, k));
  }
  return c;
}
function yo(c, y, m, b) {
  if (b) {
    const M = {
      x: m.x,
      y: m.y,
      w: m.w,
      h: m.h,
      i: "-1"
    };
    if (M.y = Math.max(y.y - m.h, 0), !wn(c, M))
      return er(c, m, void 0, M.y, !1);
  }
  return er(c, m, void 0, m.y + 1, !1);
}
function Ma(c, y, m, b) {
  const k = `translate3d(${y}px,${c < 0 ? 0 : c}px, 0)`;
  return {
    transform: k,
    WebkitTransform: k,
    MozTransform: k,
    msTransform: k,
    OTransform: k,
    width: `${m}px`,
    height: `${b}px`,
    position: "absolute"
  };
}
function Ta(c, y, m, b) {
  const k = `translate3d(${y * -1}px,${c < 0 ? 0 : c}px, 0)`;
  return {
    transform: k,
    WebkitTransform: k,
    MozTransform: k,
    msTransform: k,
    OTransform: k,
    width: `${m}px`,
    height: `${b}px`,
    position: "absolute"
  };
}
function ja(c, y, m, b) {
  return {
    top: `${c}px`,
    left: `${y}px`,
    width: `${m}px`,
    height: `${b}px`,
    position: "absolute"
  };
}
function ka(c, y, m, b) {
  return {
    top: `${c}px`,
    right: `${y}px`,
    width: `${m}px`,
    height: `${b}px`,
    position: "absolute"
  };
}
function Io(c) {
  return [...c].sort((y, m) => y.y === m.y && y.x === m.x ? 0 : y.y > m.y || y.y === m.y && y.x > m.x ? 1 : -1);
}
function Ia(c, y = "Layout") {
  const m = ["x", "y", "w", "h"];
  if (!Array.isArray(c))
    throw new Error(`${y} must be an array!`);
  for (let b = 0, k = c.length; b < k; b++) {
    const M = c[b];
    for (let L = 0; L < m.length; L++)
      if (typeof M[m[L]] != "number")
        throw new Error(`VueGridLayout: ${y}[${b}].${m[L]} must be a number!`);
    if (M.i && M.i, M.static !== void 0 && typeof M.static != "boolean")
      throw new Error(`VueGridLayout: ${y}[${b}].static must be a boolean!`);
  }
}
Mo();
const tr = Symbol("eventBus"), za = Symbol("parentRoot"), zo = Symbol("isDraggable"), Do = Symbol("isResizable"), Ro = Symbol("rowHeight"), Co = Symbol("maxRows"), Ao = Symbol("colNum"), Fo = Symbol("containerWidth"), Bo = Symbol("margin"), Wo = Symbol("useCssTransforms"), Xo = Symbol("isMirrored");
function Da(c) {
  const y = c.target.offsetParent || document.body, m = y === document.body ? { left: 0, top: 0 } : y.getBoundingClientRect(), b = c.clientX + y.scrollLeft - m.left, k = c.clientY + y.scrollTop - m.top;
  return { x: b, y: k };
}
function mo(c) {
  return Da(c);
}
function Ra(c) {
  return typeof c == "number" && !isNaN(c);
}
function bo(c, y, m, b) {
  return Ra(c) ? {
    deltaX: m - c,
    deltaY: b - y,
    lastX: c,
    lastY: y,
    x: m,
    y: b
  } : {
    deltaX: 0,
    deltaY: 0,
    lastX: m,
    lastY: b,
    x: m,
    y: b
  };
}
const Ca = {
  autoSize: {
    type: Boolean,
    default: !0
  },
  colNum: {
    type: Number,
    default: 12
  },
  rowHeight: {
    type: Number,
    default: 10
  },
  maxRows: {
    type: Number,
    default: 1 / 0
  },
  margin: {
    type: Array,
    default: () => [10, 10]
  },
  isDraggable: {
    type: Boolean,
    default: !0
  },
  isResizable: {
    type: Boolean,
    default: !0
  },
  isMirrored: {
    type: Boolean,
    default: !1
  },
  useCssTransforms: {
    type: Boolean,
    default: !0
  },
  verticalCompact: {
    type: Boolean,
    default: !0
  },
  layout: {
    type: Array,
    required: !0
  },
  responsive: {
    type: Boolean,
    default: !1
  },
  responsiveLayouts: {
    type: Object,
    default: () => ({})
  },
  breakpoints: {
    type: Object,
    default: () => ({ lg: 1200, md: 996, sm: 768, xs: 480, xxs: 0 })
  },
  responsiveCols: {
    type: Object,
    default: () => ({ lg: 12, md: 10, sm: 6, xs: 4, xxs: 2 })
  },
  preventCollision: {
    type: Boolean,
    default: !1
  },
  buffer: {
    type: Number,
    default: 0
  }
}, Aa = {
  isDraggable: {
    type: Boolean,
    default: !1
  },
  isResizable: {
    type: Boolean,
    default: !1
  },
  static: {
    type: Boolean,
    default: !1
  },
  minH: {
    type: Number,
    default: 1
  },
  minW: {
    type: Number,
    default: 1
  },
  maxH: {
    type: Number,
    default: 1 / 0
  },
  maxW: {
    type: Number,
    default: 1 / 0
  },
  x: {
    type: Number,
    required: !0
  },
  y: {
    type: Number,
    required: !0
  },
  w: {
    type: Number,
    required: !0
  },
  h: {
    type: Number,
    required: !0
  },
  i: {
    type: [Number, String],
    required: !0
  },
  dragIgnoreFrom: {
    type: String,
    default: "a, button"
  },
  dragAllowFrom: {
    type: String,
    default: ""
  },
  resizeIgnoreFrom: {
    type: String,
    default: "a, button"
  }
}, Yo = Po({
  name: "GridItem",
  props: Aa,
  emits: ["container-resized", "resize", "resized", "move", "moved"],
  setup(c, {
    emit: y
  }) {
    const m = ze(tr), b = ze(Fo, $(100)), k = ze(Ro, $(10)), M = ze(Bo, $([10, 10])), L = ze(Co, $(1 / 0)), v = ze(Ao, $(12)), oe = ze(Wo, $(!0)), te = $(!1), K = $(!1), ge = $(0), le = $(0), pe = $(0), He = $(0), ue = $(c.x), we = $(c.y), X = $(c.w), Q = $(c.h), Oe = $(!1), Ge = bn({
      data: {
        width: "0px",
        height: "0px"
      }
    }), z = $(0), F = $(0), ie = $(0), S = $(0), Z = bn({
      data: {
        width: 0,
        height: 0
      }
    }), q = bn({
      data: {
        top: 0,
        left: 0
      }
    }), H = $(null), qe = $(null);
    let ae;
    function Pe() {
      if (ae = ae != null ? ae : ho(H.value), B.value && !c.static) {
        const O = {
          ignoreFrom: c.dragIgnoreFrom,
          allowFrom: c.dragAllowFrom
        };
        ae.draggable(O), te.value || (te.value = !0, ae.on("dragstart dragmove dragend", (Y) => {
          Wt(Y);
        }));
      } else
        ae.draggable({
          enabled: !1
        });
    }
    const P = $(!1), B = Me(c, "isDraggable").value ? Me(c, "isDraggable") : ze(zo, $(!0));
    tt(() => {
      xe(B, () => {
        Pe();
      }, {
        immediate: !0
      });
    });
    function Ae() {
      if (ae = ae != null ? ae : ho(H.value), B.value && !c.static) {
        const O = Re(0, 0, c.maxW, c.maxH), Y = Re(0, 0, c.minW, c.minH), I = {
          preserveAspectRatio: !0,
          edges: {
            left: se.value && `.${Ue.value.trim().replace(" ", ".")}`,
            right: se.value ? !1 : `.${Ue.value.trim().replace(" ", ".")}`,
            bottom: `.${Ue.value.trim().replace(" ", ".")}`,
            top: !1
          },
          ignoreFrom: c.resizeIgnoreFrom,
          restrictSize: {
            min: {
              height: Y.height,
              width: Y.width
            },
            max: {
              height: O.height,
              width: O.width
            }
          }
        };
        ae.resizable(I), K.value || (K.value = !0, ae.on("resizestart resizemove resizeend", (J) => {
          Ke(J);
        }));
      } else
        ae.resizable({
          enabled: !1
        });
    }
    const Ve = $(!1), Te = Me(c, "isResizable").value ? Me(c, "isResizable") : ze(Do, $(!0));
    tt(() => {
      xe(Te, () => {
        Ae();
      }, {
        immediate: !0
      });
    }), xe(() => c.static, () => {
      Pe(), Ae();
    });
    const ve = () => {
      c.x + c.w > v.value ? (ue.value = 0, X.value = c.w > v.value ? v.value : c.w) : (ue.value = c.x, X.value = c.w);
      const O = Re(ue.value, we.value, X.value, Q.value);
      P.value && (O.top = q.data.top, se.value ? O.right = q.data.left : O.left = q.data.left), Ve.value && (O.width = Z.data.width, O.height = Z.data.height);
      let Y = null;
      oe ? se.value ? Y = Ta(O.top, O.right, O.width, O.height) : Y = Ma(O.top, O.left, O.width, O.height) : se.value ? Y = ka(O.top, O.right, O.width, O.height) : Y = ja(O.top, O.left, O.width, O.height), Ge.data = Y;
    };
    So(() => {
      ve();
      const O = () => {
        ve();
      }, Y = () => {
        O();
      };
      m.on("compact", Y);
    });
    const wt = xt(() => Te.value && !c.static), De = ze(Xo, $(!1)), se = xt(() => De.value ? !Oe.value : Oe.value);
    xe(() => se.value, () => {
      Ae(), ve();
    }), xe([b, v], () => {
      Ae(), ve(), Yt();
    }), xe([() => c.minH, () => c.maxH, () => c.minW, () => c.maxW], () => {
      Ae();
    });
    const _t = xt(() => navigator.userAgent.toLowerCase().indexOf("android") !== -1), _n = xt(() => (B.value || Te.value) && !c.static), E = xt(() => ({
      "vue-grid-item": !0,
      "vue-resizable": wt.value,
      static: c.static,
      resizing: Ve.value,
      "vue-draggable-dragging": P.value,
      cssTransforms: oe.value,
      "render-rtl": se.value,
      "disable-userselect": P.value,
      "no-touch": _t.value && _n.value
    })), Ue = xt(() => se.value ? "vue-resizable-handle vue-rtl-resizable-handle" : "vue-resizable-handle"), Fe = () => (b.value - (M.value[0] || 10) * (v.value + 1)) / v.value, ct = (O, Y) => {
      const I = Fe();
      let J = Math.round((Y - M.value[0]) / (I + M.value[0])), R = Math.round((O - M.value[1]) / (k.value + M.value[1]));
      return J = Math.max(Math.min(J, v.value - X.value), 0), R = Math.max(Math.min(R, L.value - Q.value), 0), {
        x: J,
        y: R
      };
    }, Wt = (O) => {
      if (c.static || Ve.value)
        return;
      const Y = mo(O);
      if (Y === null)
        return;
      const {
        x: I,
        y: J
      } = Y, R = {
        top: 0,
        left: 0
      };
      switch (O.type) {
        case "dragstart": {
          pe.value = ue.value, He.value = we.value;
          const re = O.target.offsetParent.getBoundingClientRect(), Be = O.target.getBoundingClientRect();
          se.value ? R.left = (Be.right - re.right) * -1 : R.left = Be.left - re.left, R.top = Be.top - re.top, q.data = R, P.value = !0;
          break;
        }
        case "dragend": {
          if (!P.value)
            return;
          const re = O.target.offsetParent.getBoundingClientRect(), Be = O.target.getBoundingClientRect();
          se.value ? R.left = (Be.right - re.right) * -1 : R.left = Be.left - re.left, R.top = Be.top - re.top, q.data = {
            top: 0,
            left: 0
          }, P.value = !1;
          break;
        }
        case "dragmove": {
          const re = bo(z.value, F.value, I, J);
          se.value ? R.left = q.data.left - re.deltaX : R.left = q.data.left + re.deltaX, R.top = q.data.top + re.deltaY, q.data = R;
          break;
        }
      }
      let j = null;
      se.value, j = ct(R.top, R.left), z.value = I, F.value = J, (ue.value !== j.x || we.value !== j.y) && y("move", c.i, j.x, j.y), O.type === "dragend" && (pe.value !== ue.value || He.value !== we.value) && y("moved", c.i, j.x, j.y), m.emit("dragEvent", {
        eventType: O.type,
        i: c.i,
        x: j.x,
        y: j.y,
        h: Q.value,
        w: X.value
      });
    };
    function Re(O, Y, I, J, R = se.value) {
      const j = Fe();
      let re;
      return R ? re = {
        right: Math.round(j * O + (O + 1) * M.value[0]),
        top: Math.round(k.value * Y + (Y + 1) * M.value[1]),
        width: I === 1 / 0 ? I : Math.round(j * I + Math.max(0, I - 1) * M.value[0]),
        height: J === 1 / 0 ? J : Math.round(k.value * J + Math.max(0, J - 1) * M.value[1])
      } : re = {
        left: Math.round(j * O + (O + 1) * M.value[0]),
        top: Math.round(k.value * Y + (Y + 1) * M.value[1]),
        width: I === 1 / 0 ? I : Math.round(j * I + Math.max(0, I - 1) * M.value[0]),
        height: J === 1 / 0 ? J : Math.round(k.value * J + Math.max(0, J - 1) * M.value[1])
      }, re;
    }
    const Xt = (O, Y, I = !1) => {
      const J = Fe();
      let R = Math.round((Y + M.value[0]) / (J + M.value[0])), j = 0;
      return I ? j = Math.ceil((O + M.value[1]) / (k.value + M.value[1])) : j = Math.round((O + M.value[1]) / (k.value + M.value[1])), R = Math.max(Math.min(R, v.value - ue.value), 0), j = Math.max(Math.min(j, L.value - we.value), 0), {
        w: R,
        h: j
      };
    }, Ke = (O) => {
      if (c.static)
        return;
      const Y = mo(O);
      if (Y === null)
        return;
      const {
        x: I,
        y: J
      } = Y, R = {
        width: 0,
        height: 0
      };
      let j = null;
      switch (O.type) {
        case "resizestart": {
          ge.value = X.value, le.value = Q.value, j = Re(ue.value, we.value, X.value, Q.value), R.width = j.width, R.height = j.height, Z.data = R, Ve.value = !0;
          break;
        }
        case "resizemove": {
          const re = bo(ie.value, S.value, I, J);
          se.value ? R.width = Z.data.width - re.deltaX : R.width = Z.data.width + re.deltaX, R.height = Z.data.height + re.deltaY, Z.data = R;
          break;
        }
        case "resizeend": {
          j = Re(ue.value, we.value, X.value, Q.value), R.width = j.width, R.height = j.height, Z.data = {
            width: 0,
            height: 0
          }, Ve.value = !1;
          break;
        }
      }
      j = Xt(R.height, R.width), j.w < c.minW && (j.w = c.minW), j.w > c.maxW && (j.w = c.maxW), j.h < c.minH && (j.h = c.minH), j.h > c.maxH && (j.h = c.maxH), j.h < 1 && (j.h = 1), j.w < 1 && (j.w = 1), ie.value = I, S.value = J, (X.value !== j.w || Q.value !== j.h) && y("resize", c.i, j.h, j.w, R.height, R.width), O.type === "resizeend" && (ge.value !== X.value || le.value !== Q.value) && y("resized", c.i, j.h, j.w, R.height, R.width), m.emit("resizeEvent", {
        eventType: O.type,
        i: c.i,
        x: ue.value,
        y: we.value,
        h: j.h,
        w: j.w
      });
    }, Yt = () => {
      const O = {
        height: "0px",
        width: "0px"
      }, Y = ["width", "height"];
      for (const I of Y) {
        const R = Ge.data[I].match(/^(\d+)px$/);
        if (!R)
          return;
        O[I] = R[1];
      }
      y("container-resized", c.i, c.h, c.w, O.height, O.width);
    };
    return xe(
      k,
      () => {
        ve(), Yt();
      }
    ), xe(
      () => c.x,
      (O) => {
        ue.value = O, ve();
      }
    ), xe(
      () => c.y,
      (O) => {
        we.value = O, ve();
      }
    ), xe(
      () => c.h,
      (O) => {
        Q.value = O, ve();
      }
    ), xe(
      () => c.w,
      (O) => {
        X.value = O, ve();
      }
    ), {
      classObj: E,
      style: Ge,
      resizableHandleClass: Ue,
      resizableAndNotStatic: wt,
      itemContainer: H,
      handle: qe,
      dragging: q,
      calcXY: ct
    };
  },
  render() {
    var c, y;
    return xn("div", {
      ref: "itemContainer",
      class: this.classObj,
      style: this.style.data
    }, [(y = (c = this.$slots).default) == null ? void 0 : y.call(c), this.resizableAndNotStatic ? xn("span", {
      ref: "handle",
      class: this.resizableHandleClass
    }, null) : ""]);
  }
});
function Fa(c, y) {
  const m = $o(c);
  let b = m[0];
  for (let k = 1, M = m.length; k < M; k++) {
    const L = m[k];
    y > c[L] && (b = L);
  }
  return b;
}
function xo(c, y) {
  if (!y[c])
    throw new Error(`ResponsiveGridLayout: \`cols\` entry for breakpoint ${c} is missing!`);
  return y[c];
}
function Ba(c, y, m, b, k, M, L) {
  if (y[b])
    return Qn(y[b]);
  let v = c;
  const oe = $o(m), te = oe.slice(oe.indexOf(b));
  for (let K = 0, ge = te.length; K < ge; K++) {
    const le = te[K];
    if (y[le]) {
      v = y[le];
      break;
    }
  }
  return v = Qn(v || []), Ft(Ea(v, { cols: M }), L);
}
function $o(c) {
  return Object.keys(c).sort((m, b) => c[m] - c[b]);
}
function Wa(c) {
  return ga() ? (ya(c), !0) : !1;
}
const Bt = typeof window < "u";
function Xa(c) {
  var y;
  const m = ma(c);
  return (y = m == null ? void 0 : m.$el) != null ? y : m;
}
const Ya = Bt ? window : void 0;
Bt && window.document;
Bt && window.navigator;
Bt && window.location;
const nr = typeof globalThis < "u" ? globalThis : typeof window < "u" ? window : typeof global < "u" ? global : typeof self < "u" ? self : {}, rr = "__vueuse_ssr_handlers__";
nr[rr] = nr[rr] || {};
nr[rr];
var wo = Object.getOwnPropertySymbols, $a = Object.prototype.hasOwnProperty, La = Object.prototype.propertyIsEnumerable, Na = (c, y) => {
  var m = {};
  for (var b in c)
    $a.call(c, b) && y.indexOf(b) < 0 && (m[b] = c[b]);
  if (c != null && wo)
    for (var b of wo(c))
      y.indexOf(b) < 0 && La.call(c, b) && (m[b] = c[b]);
  return m;
};
function Ha(c, y, m = {}) {
  const b = m, { window: k = Ya } = b, M = Na(b, ["window"]);
  let L;
  const v = k && "ResizeObserver" in k, oe = () => {
    L && (L.disconnect(), L = void 0);
  }, te = xe(() => Xa(c), (ge) => {
    oe(), v && k && ge && (L = new ResizeObserver(y), L.observe(ge, M));
  }, { immediate: !0, flush: "post" }), K = () => {
    oe(), te();
  };
  return Wa(K), {
    isSupported: v,
    stop: K
  };
}
var _o, Oo;
Bt && (window == null ? void 0 : window.navigator) && ((_o = window == null ? void 0 : window.navigator) == null ? void 0 : _o.platform) && /iP(ad|hone|od)/.test((Oo = window == null ? void 0 : window.navigator) == null ? void 0 : Oo.platform);
const Ga = Po({
  name: "GridLayout",
  props: Ca,
  emits: ["layout-created", "layout-mounted", "layout-before-mount", "layout-updated", "layout-ready", "update:layout", "breakpoint-changed"],
  setup(c, {
    emit: y
  }) {
    let m = ze(tr);
    m || (m = Mo());
    const b = $(null);
    Ie(tr, m), Ie(za, b), Ie(zo, Me(c, "isDraggable")), Ie(Do, Me(c, "isResizable")), Ie(Xo, Me(c, "isMirrored")), Ie(Ro, Me(c, "rowHeight")), Ie(Co, Me(c, "maxRows")), Ie(Ao, Me(c, "colNum")), Ie(Bo, Me(c, "margin")), Ie(Wo, Me(c, "useCssTransforms"));
    const k = $(100);
    Ha(b, (z) => {
      const F = z[0];
      k.value = F.contentRect.width, le();
    }), Ie(Fo, k);
    const M = $({}), L = $(!1), v = bn({
      x: 0,
      y: 0,
      w: 0,
      h: 0,
      i: -1
    });
    let oe = {}, te = null, K;
    const ge = () => {
      if (!c.autoSize)
        return;
      const z = c.buffer || 0;
      return `${Oa(c.layout) * (c.rowHeight + c.margin[1]) + c.margin[1] + z}px`;
    }, le = () => {
      M.value = {
        height: ge()
      };
    }, pe = (z, F) => {
      const ie = z.filter((Z) => !F.some((q) => Z.i === q.i)), S = F.filter((Z) => !z.some((q) => Z.i === q.i));
      return ie.concat(S);
    }, He = () => {
      oe = Object.assign({}, c.responsiveLayouts);
    }, ue = () => {
      if (c.layout !== void 0) {
        if (c.layout.length !== K.length) {
          const z = pe(c.layout, K);
          z.length > 0 && (c.layout.length > K.length ? K = K.concat(z) : K = K.filter((F) => !z.some((ie) => F.i === ie.i))), He();
        }
        Ft(c.layout, c.verticalCompact), le(), y("layout-updated", c.layout);
      }
    };
    xe([() => c.layout.length, () => c.layout, () => c.margin], () => {
      ue();
    }), xe(() => c.responsive, () => {
      c.responsive || y("update:layout", K);
    });
    const we = () => {
      const z = Fa(c.breakpoints, k.value), F = xo(z, c.responsiveCols);
      te !== null && !oe[te] && (oe[te] = Qn(c.layout));
      const ie = Ba(K, oe, c.breakpoints, z, te, F, c.verticalCompact);
      oe[z] = ie, te !== z && y("breakpoint-changed", z, ie), y("update:layout", ie), te = z, m.emit("setColNum", xo(z, c.responsiveCols));
    }, X = (z, F, ie, S, Z, q) => {
      let H = go(c.layout, F);
      H == null && (H = {
        h: 0,
        w: 0,
        x: 0,
        y: 0,
        i: F
      });
      let qe = !1;
      if (c.preventCollision) {
        const ae = jo(c.layout, {
          ...H,
          w: q,
          h: Z
        }).filter((Pe) => Pe.i !== H.i);
        if (qe = ae.length > 0, qe) {
          let Pe = 1 / 0, P = 1 / 0;
          ae.forEach((B) => {
            B.x > H.x && (Pe = Math.min(Pe, B.x)), B.y > H.y && (P = Math.min(P, B.y));
          }), Number.isFinite(Pe) && (H.w = Pe - H.x), Number.isFinite(P) && (H.h = P - H.y);
        }
      }
      qe || (H.w = q, H.h = Z), z === "resizestart" || z === "resizemove" ? (v.i = F, v.x = ie, v.y = S, v.w = H.w, v.h = H.h, tt(() => {
        L.value = !0;
      })) : tt(() => {
        L.value = !1;
      }), c.responsive && we(), Ft(c.layout, c.verticalCompact), m.emit("compact"), le(), z === "resizeend" && y("layout-updated", c.layout);
    };
    function Q({
      eventType: z,
      i: F,
      x: ie,
      y: S,
      h: Z,
      w: q
    }) {
      X(z, F, ie, S, Z, q);
    }
    const Oe = (z, F, ie, S, Z, q) => {
      let H = go(c.layout, F);
      H == null && (H = {
        h: 0,
        w: 0,
        x: 0,
        y: 0,
        i: F
      }), z === "dragmove" || z === "dragstart" ? (v.i = F, v.x = H.x, v.y = H.y, v.w = q, v.h = Z, tt(() => {
        L.value = !0;
      })) : tt(() => {
        L.value = !1;
      }), er(c.layout, H, ie, S, !0, c.preventCollision), Ft(c.layout, c.verticalCompact), m.emit("compact"), le(), z === "dragend" && y("layout-updated", c.layout);
    }, Ge = ({
      eventType: z,
      i: F,
      x: ie,
      y: S,
      h: Z,
      w: q
    }) => {
      Oe(z, F, ie, S, Z, q);
    };
    return m.on("resizeEvent", Q), m.on("dragEvent", Ge), y("layout-created", c.layout), y("layout-before-mount", c.layout), ba(() => {
      m.off("resizeEvent", Q), m.off("dragEvent", Ge);
    }), So(() => {
      y("layout-mounted", c.layout), tt(() => {
        Ia(c.layout), K = c.layout, tt(() => {
          He(), Ft(c.layout, c.verticalCompact), y("layout-updated", c.layout), le();
        });
      });
    }), {
      mergedStyle: M,
      isDragging: L,
      placeholder: v,
      layoutContainer: b,
      dragEvent: Oe
    };
  },
  render() {
    var c, y;
    return xn("div", {
      ref: "layoutContainer",
      class: "vue-grid-layout",
      style: this.mergedStyle
    }, [(y = (c = this.$slots).default) == null ? void 0 : y.call(c), xa(xn(Yo, {
      ref: "gridItem",
      class: "vue-grid-placeholder",
      x: this.placeholder.x,
      y: this.placeholder.y,
      w: this.placeholder.w,
      h: this.placeholder.h,
      i: this.placeholder.i
    }, null), [[wa, this.isDragging]])]);
  }
}), Va = {
  install(c) {
    c.component("GridItem", Yo), c.component("GridLayout", Ga);
  }
};
export {
  Yo as GridItem,
  Ga as GridLayout,
  Va as default,
  tr as eventBusKey
};
