(function ($) {
    if (!this.bowlder) {
        Object.create || (Object.create = function (e) {
            var t = function () {
            };
            return t.prototype = e, new t
        });
        var x = this.bowlder = function (e, t) {
            return e && g(e) ? h(e) ? new ne(/^\s*</.test(e) ? S.create(e).childNodes : l.cssQuery(e, t)) : O(e) || g(e.nodeType) || oe(e) ? new ne(e) : (T(e) && x.ready(e), e) : new ne([])
        };
        this.$$ || (this.$$ = x), x.ver = "0.9", x.cb = {counter: 0};
        var r = "object" == typeof performance ? performance.timing.connectStart : +new Date, i = {connectStart: r}, o = {};
        x.bench = {
            mark: function (e) {
                var t = +new Date;
                if (e) {
                    o[e] || (o[e] = ee.defer());
                    var n = {duration: t - i.connectStart, interval: t - r, stamp: t};
                    setTimeout(function () {
                        n.lag = (+new Date - t) / 100, o[e].resolve(X(n, i), !0)
                    }, 100)
                }
                return r = t, o[e]
            }, get: function (e) {
                return e ? (o[e] || (o[e] = ee.defer()), o[e].promise) : x.ready().then(function () {
                    return x.ready(x.rootWidget.find())
                }).then(function () {
                    var n = {};
                    return _(o, function (e, t) {
                        n[t] = e.promise
                    }), ee.all(n)
                })
            }
        };
        var t = 0, n = Object.prototype.toString, u = [].slice;
        Function.prototype.bind && "function" == typeof x.bind(this) || (Function.prototype.bind = function () {
            if (!arguments.length) return this;
            var e = this, t = u.call(arguments), n = t.shift();
            return function () {
                return e.apply(n, t.concat(u.call(arguments)))
            }
        });
        var _ = x.each = function (e, t, n) {
            var r, i = typeof e;
            if (e) if ("string" == i && (e = l.incArray(e)), "function" == i) for (r in e) "prototype" == r || "length" == r || "name" == r || e.hasOwnProperty && !e.hasOwnProperty(r) || t.call(n, e[r], r); else if (le(e)) for (r = 0; r < e.length; r++) g(e[r]) && t.call(n, e[r], r); else if ("object" == i) for (r in e) e.hasOwnProperty(r) && t.call(n, e[r], r);
            return e
        }, g = x.isDefined = function (e) {
            return void 0 !== e
        }, d = x.isObject = function (e) {
            return null != e && "[object Object]" === n.call(e) && !g(e.nodeType)
        }, p = x.isNumber = function (e) {
            return "number" == typeof e && !isNaN(e)
        }, T = x.isFunction = function (e) {
            return "[object Function]" === n.call(e)
        }, O = x.isArray = function (e) {
            return "[object Array]" === n.call(e)
        }, h = x.isString = function (e) {
            return "[object String]" === n.call(e)
        };
        _("File RegExp Boolean".split(/ /), function (t) {
            x["is" + t] = function (e) {
                return n.call(e) === "[object " + t + "]"
            }
        }), x.async = function (e) {
            var r = $q.defer(), i = e.apply(null, u.call(arguments, 1)), o = function (e) {
                var t = i.next(e);
                if (t.done) r.resolve(t.value); else {
                    var n = t.value;
                    T(n) ? n(o) : (d(n) && T(n.then) || (n = x.Promise.resolve(n)), n.then(o))
                }
            };
            return o(), r.promise
        };
        var a = {};
        _("fillOpacity fontWeight lineHeight opacity orphans widows zIndex zoom".split(/ /), function (e) {
            a[e] = !0
        });
        var A = function (e) {
            return h(e) ? e.toLowerCase() : e
        }, v = function (e) {
            return h(e) ? e.toUpperCase() : e
        }, s = {};
        _("selected checked disabled readOnly readonly required open autofocus controls autoplay compact loop defer multiple".split(" "), function (e) {
            s[e] = 1
        });
        var c = {
            class: function (e, t) {
                "className" in e ? e.className = t || "" : e.setAttribute("class", t)
            }, for: function (e, t) {
                "htmlFor" in e ? e.htmlFor = t : e.setAttribute("for", t)
            }, style: function (e, t) {
                e.style ? e.style.cssText = t : e.setAttribute("style", t)
            }, value: function (e, t) {
                e.value = null != t ? t : ""
            }
        }, f = A(navigator.userAgent), y = parseInt((/msie (\d+)/.exec(f) || [])[1], 10);
        isNaN(y) && (y = parseInt((/trident\/.*; rv:(\d+)/.exec(f) || [])[1], 10));
        var N = window.requestAnimationFrame || window.webkitRequestAnimationFrame || window.mozRequestAnimationFrame || window.oRequestAnimationFrame || window.msRequestAnimationFrame || function (e) {
            window.setTimeout(e, 30)
        }, l = x.utils = {
            msie: y, rAF: N, camelCase: pe, cssQuery: function (e, t) {
                if (!x.needPolyfill) {
                    if (t = t || document, !g(t.nodeType) && t[0] && (t = t[0]), !g(t.nodeType) || !h(e)) return [];
                    var n, r = t != document;
                    r ? (e = e.replace(/(^\s*|,\s*)/g, "$1#__bowlder__ "), n = t.id, t.id = "__bowlder__") : e = e.replace(/(^\s*|,\s*)>/g, "$1body>");
                    var i = u.call(t.querySelectorAll(e));
                    return r && (n ? t.id = n : t.removeAttribute("id")), i
                }
                alert("polyfill's not ready.")
            }, incArray: function (e, t, n) {
                var r = [], i = "push";
                if (h(e)) {
                    var o = e.split(".."), a = o.length;
                    e = parseInt(o[0], 10), t = parseInt(o[a - 1], 10), n = Math.abs(3 == a ? parseInt(o[1], 10) : 1)
                }
                if (!isNaN(n) && n || (n = 1), isNaN(e) || isNaN(t)) return r;
                t < e && (i = e, e = t, t = i, i = "unshift");
                for (var s = e; s <= t; s += n) r[i](s);
                return r
            }
        }, m = /[;\s]+/, b = {}, w = {}, E = {}, j = {}, k = {tfoot: "table", thead: "table", tbody: "table", tr: "tbody", th: "tr", td: "tr", option: "select"}, S = x.dom = {
            _fixe: function (t) {
                var e = ["clientX", "clientY", "pageX", "pageY"];
                if (t.touches) switch (t.type) {
                    case"touchstart":
                        _(e, function (e) {
                            t[e] = t.touches[0][e]
                        });
                        break;
                    case"touchend":
                    case"touchmove":
                        _(e, function (e) {
                            t[e] = t.changedTouches[0][e]
                        })
                }
                return t
            }, _on: function (e, t, n, r) {
                e.addEventListener(t, n, r || !1)
            }, _off: function (e, t, n, r) {
                e.removeEventListener(t, n, r || !1)
            }, _idname: "_b$id", _nodeId: function (e) {
                return e[S._idname] || (e[S._idname] = ++t)
            }, addClass: function (e, t) {
                P.add(e, t)
            }, toggleClass: function (e, t) {
                P.toggle(e, t)
            }, removeClass: function (e, t) {
                P.remove(e, t)
            }, hasClass: function (e, t) {
                return P.contains(e, t)
            }, hasRole: function (e, t) {
                return !!(e && e.getAttribute && t) && -1 < (e.getAttribute("ne-role") || "").split(/\s+/).indexOf(t)
            }, parent: function (e, t, n) {
                var r;
                if (t) for (var i = l.cssQuery(t); e.parentNode && 1 == e.parentNode.nodeType;) {
                    if (e = e.parentNode, ~i.indexOf(e)) {
                        r = e;
                        break
                    }
                    if (!n) break
                } else r = e.parentNode;
                return r
            }, delegate: function (r, n, i, o) {
                if (d(i)) _(i, function (e, t) {
                    S.delegate(r, n, t, e)
                }); else if (T(i)) S.bind(r, n, i); else if (h(i)) {
                    var e = S._nodeId(r), a = w[e] || (w[e] = {});
                    _(n.split(m), function (e) {
                        if (e) {
                            var t = a[e];
                            if (!t) {
                                t = a[e] = {};
                                var n = ue(function (e) {
                                    (function (n, e) {
                                        for (var r, i = n.target, o = this, a = {}, t = function (e, t) {
                                            a[t] || (a[t] = l.cssQuery(t, o)), -1 < a[t].indexOf(i) && _(e, function (e) {
                                                n.currentTarget = i, !1 !== r && (r = e.call(i, n))
                                            })
                                        }; i && i != o;) {
                                            if (_(e, t), !1 === r || -1 === r) {
                                                n.preventDefault(), -1 === r && n.stopPropagation();
                                                break
                                            }
                                            i = i.parentNode
                                        }
                                    }).call(r, e, t)
                                }, y < 9 ? r : "");
                                S._on(r, e, n)
                            }
                            if (t[i]) {
                                if (-1 < t[i].indexOf(o)) return
                            } else t[i] = [];
                            t[i].push(o)
                        }
                    })
                } else T(o) && S.bind(r, n, o)
            }, undelegate: function (n, r, i, o) {
                if (d(i)) _(i, function (e, t) {
                    S.undelegate(n, r, t, e)
                }); else if (h(i)) {
                    var e = S._nodeId(n), a = w[e] || (w[e] = {});
                    _(r.split(m), function (e) {
                        if (e) {
                            var t = a[e];
                            if (t[i]) {
                                for (var n = 0; n < t[i].length; n++) if (t[i][n] == o) {
                                    t[i].splice(n, 1);
                                    break
                                }
                                0 === t[i].length && delete t[i]
                            }
                        }
                    })
                } else T(i) ? S.unbind(n, r, i) : T(o) && S.unbind(n, r, o)
            }, bind: function (r, e, t, i) {
                if (T(t)) {
                    var o = S._nodeId(r);
                    t = ue(t, y < 9 ? r : "");
                    var a = b[o] || (b[o] = {});
                    _(e.split(m), function (n) {
                        if (n = h(n) && n.split(".")[0]) {
                            if (a[n]) {
                                if (-1 < a[n].indexOf(t)) return
                            } else a[n] = [];
                            if (a[n].push(t), S._on(r, n, t, i), E[n]) {
                                var e = j[o] || (j[o] = {});
                                e[n] || (e[n] = E[n].call(r, function (e, t) {
                                    S.trigger(t || r, n, !!t, e)
                                }) || !0)
                            }
                        }
                    })
                }
            }, unbind: function (n, e, r, i) {
                var t = n[S._idname];
                if (t && h(e)) {
                    var o = b[t];
                    o && (r ? (r = ue(r, y < 9 ? n : ""), _(e.split(m), function (e) {
                        if ((e = h(e) && e.split(".")[0]) && O(o[e])) {
                            S._off(n, e, r, i);
                            for (var t = 0; t < o[e].length; t++) o[e][t] == r && o[e].splice(t--, 1)
                        }
                    })) : _(e.split(m), function (t) {
                        (t = h(t) && t.split(".")[0]) && (_(o[t], function (e) {
                            S._off(n, t, e, i)
                        }), delete o[t])
                    }))
                }
            }, before: function (e, t) {
                return h(e) && (e = S.create(e)), t && t.parentNode && t.parentNode.insertBefore(e, t), e
            }, after: function (e, t) {
                if (h(e) && (e = S.create(e)), t && t.parentNode) {
                    var n = t.parentNode, r = t.nextSibling;
                    r ? n.insertBefore(e, r) : n.appendChild(e)
                }
                return e
            }, replace: function (e, t) {
                return h(e) && (e = S.create(e)), t && t.parentNode && t.parentNode.replaceChild(e, t), e
            }, trigger: function (e, t, n, r) {
                var i = document.createEvent("MouseEvents");
                i.initEvent(t, !g(n) || !!n, !1), d(r) && x.extend(i, r), e.dispatchEvent(i)
            }, show: function (e) {
                e.style.display = e.olddisplay || "", "none" == ae(e, "display") && (e.style.display = "block")
            }, hide: function (e) {
                var t = e.olddisplay || ae(e, "display");
                e.olddisplay = "none" == t || "block" == t ? "" : t, e.style.display = "none"
            }, toggle: function (e) {
                S["none" == S.css(e, "display") ? "show" : "hide"](e)
            }, css: function (n, e, t) {
                if (h(e)) {
                    var r = pe(e);
                    if (-1 < e.indexOf(":")) n.style.cssText += ";" + e; else {
                        if (!g(t)) {
                            if (he(r)) t = ae(n, r); else {
                                if (/scroll/.test(e) && (oe(n) || 9 == n.nodeType)) return document.body[e] || document.documentElement[e];
                                9 == n.nodeType && (n = n.documentElement), t = n[r]
                            }
                            return g(t) ? t : ""
                        }
                        se(n, r, t)
                    }
                } else if (d(e)) {
                    var i, o = {};
                    if (_(e, function (e, t) {
                        if (G.test(t)) return !isNaN(e) && e && (0 === t.indexOf("translate") || 0 === t.indexOf("persipective") ? e += "px" : 0 === t.indexOf("rotate") && (e += "deg")), i = !0, void (o[t] = t + "(" + e + ")");
                        he(t = pe(t)) ? se(n, t, e) : n[t] = e
                    }), i) {
                        var a, s = n.style[pe(F)];
                        if (s) for (var u = /(\S+)\s*(\(.*?\))/g; null != (a = u.exec(s));) {
                            var c = a[1];
                            g(o[c]) || (o[c] = c + a[2])
                        }
                        var f = [];
                        _(o, function (e) {
                            f.push(e)
                        }), n.style[pe(F)] = f.join(" ")
                    }
                }
            }, val: function (e, t) {
                if ("input" == A(e.tagName)) {
                    var n = A(e.getAttribute("type"));
                    if ("checkbox" == n) return S.attr(e, "checked", t);
                    if ("radio" == n) return g(t) ? e.value = t : e.value
                }
                var r = x.widget(e);
                return r ? r.val(t) : g(t) ? void (e.value = t) : e.value
            }, stop: function (e) {
                e.startTime = 0
            }, pause: function (e) {
                var t = e._pause;
                t && (e._pause = null, t())
            }, animate: function (i, n, r, e, t, o) {
                if (i && n) {
                    if (T(n)) return n(i).play(r, e).then(t);
                    T(r) && (t = r, r = e = $), T(e) && (t = e, e = $), r = "number" == typeof r ? r : V.speeds[r] || V.speeds.normal, o && (o = parseFloat(o) / 1e3);
                    var a = x.conf("easeFns"), s = a[e] || a.linear;
                    1 < r && S.pause(i);
                    var u, c, f, l = {}, d = {}, p = new Date, h = !1, v = !1, m = !1;
                    if (r === $ && (r = V.speeds.normal), o === $ && (o = 0), "string" == typeof n) m = !0, l[Q] = n, l[J] = r / 1e3 + "s", l[W] = o + "s", l[z] = e || "linear"; else {
                        var g = [];
                        _(n, function (e, t) {
                            V.off || r <= 1 || !he(pe(t)) || O(e) ? (v = !0, d[t] = O(e) || r <= 1 ? e : [S.css(i, t), e]) : (m = !0, l[t] = e, G.test(t) ? f = !0 : g.push(A(t.replace(/^ms([A-Z])/, "-ms-$1").replace(/(^|[a-z])([A-Z])/g, "$1-$2"))))
                        }), m && (f && g.push(F), l[q] = g.join(", "), l[H] = r / 1e3 + "s", l[U] = o + "s", l[B] = e || "linear")
                    }
                    var y = function (n) {
                        var r = {};
                        _(d, function (e, t) {
                            r[t] = Y(e, s(n))
                        }), S.css(i, r)
                    }, b = function () {
                        h || (h = !0, V.off || S.css(i, K), t && t.call(i), i._pause = null)
                    };
                    if (0 <= r && r <= 1) y(r); else {
                        if (v) {
                            var w = function () {
                                var e = new Date - p;
                                e < r ? (y(e / r), u || N(w)) : (y(1), b())
                            };
                            w()
                        } else c = setTimeout(function () {
                            b()
                        }, r + 10);
                        m && (i.clientLeft, S.css(i, l)), i._pause = function () {
                            if (u = !0, !V.off) {
                                var e = new Date - p, t = Math.min(e / r, 1);
                                S.css(i, K), S.animate(i, n, t)
                            }
                            c && clearTimeout(c)
                        }
                    }
                }
            }, fadeToggle: function (e) {
                return S["none" == e.style.display ? "fadeIn" : "fadeOut"].call(S, arguments)
            }, fadeIn: function (e, t, n, r) {
                if (!S.data(e, "_b$fadeIn")) {
                    S.data(e, "_b$fadeIn", 1);
                    var i = ae(e, "opacity");
                    T(t) && (r = t, t = null), T(n) && (r = n, n = null), S.animate(e, {opacity: "show"}, t, n, function () {
                        S.data(e, "_b$fadeIn", 0), se(e, "opacity", i), T(r) && r()
                    })
                }
            }, fadeOut: function (e, t, n, r) {
                if (!S.data(e, "_b$fadeOut")) {
                    S.data(e, "_b$fadeOut", 1);
                    var i = ae(e, "opacity");
                    T(t) && (r = t, t = null), T(n) && (r = n, n = null), S.animate(e, {opacity: "hide"}, t, n, function () {
                        S.data(e, "_b$fadeOut", 0), se(e, "opacity", i), S.hide(e), T(r) && r()
                    })
                }
            }, slideToggle: function (e) {
                S["none" == e.style.display ? "slideDown" : "slideUp"].call(S, arguments)
            }, slideUp: function (e, t, n, r) {
                if (!S.data(e, "_b$fadeUp")) {
                    S.data(e, "_b$fadeUp", 1);
                    var i = {height: e.style.height, paddingTop: e.style.paddingTop, paddingBottom: e.style.paddingBottom}, o = {overflow: ["hidden", e.style.overflow], height: "hide", paddingTop: 0, paddingBottom: 0};
                    T(t) && (r = t, t = null), T(n) && (r = n, n = null), S.animate(e, o, t, n, function () {
                        S.data(e, "_b$fadeUp", 0), S.hide(e), S.css(e, i), T(r) && r()
                    })
                }
            }, slideDown: function (e, t, n, r) {
                if (!S.data(e, "_b$fadeDown")) {
                    S.data(e, "_b$fadeDown", 1);
                    var i = {overflow: e.style.overflow, height: e.style.height, paddingTop: e.style.paddingTop, paddingBottom: e.style.paddingBottom}, o = {height: "show", paddingTop: ae(e, "paddingTop"), paddingBottom: ae(e, "paddingBottom")};
                    T(t) && (r = t, t = null), T(n) && (r = n, n = null), S.css(e, {overflow: "hidden", paddingTop: 0, paddingBottom: 0}), S.animate(e, o, t, n, function () {
                        S.data(e, "_b$fadeDown", 0), S.css(e, i), T(r) && r()
                    })
                }
            }, data: function (e, t, n) {
                if (!e || !1 !== t) return g(t) ? !g(n) && h(t) ? e && ve(e)[t] : e ? d(t) ? X(ve(e), t) : ve(e)[t] = n : void 0 : e && ve(e);
                e[S._idname] && delete ve[e[S._idname]]
            }, attr: function (n, e, t) {
                if (!n || !n.nodeType) return null;
                if (e = A(e), s[e]) {
                    if (!g(t)) return !!n[e];
                    h(t) && "false" == t && (t = !1), n[e] = !!t, t ? n.setAttribute(e, e) : n.removeAttribute(e)
                } else if (g(t)) c[e] ? c[e](n, t) : n.setAttribute(e, t); else {
                    if (!d(e)) return n.getAttribute(e, 2);
                    _(e, function (e, t) {
                        S.attr(n, t, e)
                    })
                }
            }, _create: function (e, t) {
                var n = document.createElement(e);
                return n.innerHTML = t, n
            }, create: function (e, t) {
                if (!t && /^\w+$/.test(e)) return document.createElement(e);
                var n;
                /<(\w+)/.test(e) ? n = S._create(k[A(RegExp.$1)] || "div", e) : (n = L).innerHTML = e;
                for (var r = document.createDocumentFragment(), i = n.firstChild; i;) r.appendChild(i), i = n.firstChild;
                return r
            }, remove: function (e) {
                e.parentNode && e.parentNode.removeChild(e)
            }, offset: function (e, t) {
                var n = {left: 0, top: 0}, r = document.body;
                if (e && !e.nodeType && (e = e[0]), !e || 1 !== e.nodeType) return n;
                if (n.left = e.offsetLeft, n.top = e.offsetTop, !0 !== t) for (; e;) {
                    if ("fixed" == S.css(e, "position")) {
                        var i = e.offsetParent || r;
                        n.left += i != r ? i.scrollLeft : i.scrollLeft || document.documentElement.scrollLeft, n.top += i != r ? i.scrollTop : i.scrollTop || document.documentElement.scrollTop
                    }
                    if (!(e = e.offsetParent) || e == t) break;
                    n.left += e.offsetLeft, n.top += e.offsetTop
                }
                return n
            }
        };
        if (S.pos = S.offset, y <= 9) {
            var D = {tr: ["<table><tbody><tr>", "</tr></tbody></table>", 3], tbody: ["<table><tbody>", "</tbody></table>", 2], thead: ["<table><thead>", "</thead></table>", 2], table: ["<table>", "</table>", 1], select: ["<select>", "</select>", 1]};
            S._create = function (e, t) {
                var n = D[e], r = document.createElement(n ? "div" : e);
                if (r.innerHTML = (n ? n[0] : "") + t + (n ? n[1] : ""), n) for (var i = 0; i < n[2]; i++) r = r.firstChild;
                return r
            }
        }
        var C = /(.*?)([smhd])$/, R = {s: 1, m: 60, h: 3600, d: 86400};
        x.cookie = {
            get: function (e) {
                for (var t = document.cookie.split(/;\s*/), n = 0; n < t.length; n++) {
                    var r = t[n].split("=");
                    if (e == r[0]) try {
                        return decodeURIComponent(r[1])
                    } catch (e) {
                        return ""
                    }
                }
                return ""
            }, remove: function (e, t, n) {
                document.cookie = e + "=1; path=" + (n || "/") + (t ? "; domain=" + t : "") + ";expires=Fri, 02-Jan-1970 00:00:00 GMT"
            }, set: function (e, t, n, r, i, o, a) {
                h(n) && C.test(n) && (n = RegExp.$1, a = RegExp.$2), n = 1e3 * (parseFloat(n) || 365) * (R[a] || R.d), document.cookie = e + "=" + encodeURIComponent(t) + (n ? "; expires=" + new Date(+new Date + n).toGMTString() : "") + (r ? "; domain=" + r : "") + "; path=" + (i || "/") + (o ? "; secure" : "")
            }
        }, x.event = function (e, t) {
            if (!t) return !!E[e];
            E[e] = t
        }, x.param = function (e) {
            var n = [];
            return _(e, function (e, t) {
                null != e && (O(e) || (e = [e]), _(e, function (e) {
                    d(e) && (e = JSON.stringify(e)), n.push(ce(t) + "=" + ce(e))
                }))
            }), n.join("&")
        };
        var I = l.buildUrl = function (e, t) {
            return e + ((t = x.param(t)) ? (-1 == e.indexOf("?") ? "?" : "&") + t : "")
        }, L = document.createElement("div"), P = x.classList = L.classList ? {
            contains: function (e, t) {
                return !(!e.classList || !e.classList.contains(t))
            }, add: function (t, e) {
                t.classList && e && _(e.toString().split(/\s+/), function (e) {
                    e && t.classList.add(e)
                })
            }, remove: function (t, e) {
                t.classList && e && _(e.toString().split(/\s+/), function (e) {
                    e && t.classList.remove(e)
                })
            }
        } : {
            check: function (e, t) {
                return 1 === e.nodeType && "string" == typeof e.className && "object" != typeof t && null != t
            }, contains: function (e, t) {
                return this.check(e, t) && new RegExp("\\b" + t + "\\b").test(e.className)
            }, add: function (e, t) {
                this.check(e, t) && !this.contains(e, t) && (e.className = e.className.replace(/\s*$/, " " + t))
            }, remove: function (e, t) {
                this.check(e, t) && this.contains(e, t) && (e.className = e.className.replace(new RegExp("\\b" + t + "\\b\\s*", "g"), ""))
            }
        };
        P.batch = function (e, t, n) {
            if (1 == e.nodeType) {
                var r, i = e.className.split(/\s+/);
                h(t) && (t = t.split(/\s+/)), h(n) && (n = n.split(/\s+/)), _(n, function (e) {
                    if (e) for (; -1 < (r = i.indexOf(e));) i.splice(r, 1)
                }), _(t, function (e) {
                    e && -1 == i.indexOf(e) && i.push(e)
                }), e.className = i.join(" ")
            }
        }, P.toggle = function (e, t) {
            this.contains(e, t) ? this.remove(e, t) : this.add(e, t)
        };
        var M, F, q, H, B, U, Q, J, z, W, X = x.extend = function () {
            var t = 1 <= arguments.length ? u.call(arguments, 0) : [], n = !1;
            "boolean" == typeof t[0] && (n = t.shift());
            var r, i = t.shift();
            if (i && "object" == typeof i) for (; t.length;) if ((r = t.shift()) && r !== i) {
                if (T(r.then) && T(r.catch)) return r.then(function (e) {
                    return t.unshift(T(r.success) ? e.data : e), t.unshift(i), t.unshift(n), X.apply(this, t)
                });
                _(r, function (e, t) {
                    n && d(e) ? X(n, i.hasOwnProperty(t) ? i[t] : i[t] = {}, e) : i[t] = n && O(e) ? u.call(e, 0) : e
                })
            }
            return i
        }, G = /^((translate|rotate|scale)(X|Y|Z|3d)?|matrix(3d)?|perspective|skew(X|Y)?)$/i, K = {}, V = (_(["", "-webkit-", "-o-", "-ms-"], function (e) {
            !g(M) && g(L.style[pe(e + "transition-property")]) && (M = e, l.supportCSS3 = !0)
        }), F = M + "transform", K[q = M + "transition-property"] = K[H = M + "transition-duration"] = K[U = M + "transition-delay"] = K[B = M + "transition-timing-function"] = K[Q = M + "animation-name"] = K[J = M + "animation-duration"] = K[W = M + "animation-delay"] = K[z = M + "animation-timing-function"] = "", {
            off: !l.supportCSS3,
            speeds: {normal: 300, fast: 200, slow: 600},
            cssPrefix: M
        }), Y = l.interpolate = function (e, r) {
            if (!O(e)) return e;
            var t = e.length;
            if (0 === r || 1 == r) return e[r * (t - 1)];
            var n = Math.floor(r * (t - 1)), i = Math.min(n + 1, t - 1), o = e[n], a = e[i];
            if (!/\d/.test(a.toString())) return o;
            r = r * (t - 1) - n;
            var s = o.toString().match(/[\-\.\d]+/g) || [], u = 0;
            return a.toString().replace(/[\-\.\d]+/g, function (e) {
                var t = parseFloat(s[u++]) || 0, n = t * (1 - r) + e * r;
                return 10 < Math.abs(e - t) ? Math.round(n) : n
            })
        }, Z = {};
        x.on = function (e, t, n) {
            var r = this, i = d(e) ? e : {}, o = r.hasOwnProperty("$msg") ? r.$msg : Z;
            return h(e) && _(e.split(/\s+/), function (e) {
                i[e] = t
            }), _(i, function (e, t) {
                o[t] || (o[t] = []), o[t].push(e), n && o[t].cache && e.apply(r, o[t].cache)
            }), r
        }, x.once = function (e, t, n) {
            return d(e) ? _(e, function (e) {
                T(e) && (e.once = !0)
            }) : T(t) && (t.once = !0), x.on.call(this, e, t, n)
        }, x.off = function (e, i) {
            var n = this;
            if (d(e)) return _(e, function (e, t) {
                x.off.call(n, e, t)
            }), this;
            var o = this.hasOwnProperty("$msg") ? this.$msg : Z;
            return o && h(e) && _(e.split(/\s+/), function (e) {
                var t = o[e];
                if (t) if (i) {
                    for (var n = 0, r = t.length; n < r; n++) if (t[n] == i) {
                        t.splice(n, 1);
                        break
                    }
                } else delete o[e]
            }), this
        }, x.emit = function () {
            var i = 1 <= arguments.length ? u.call(arguments, 0) : [], e = i.shift();
            if (h(e)) {
                var o, a = this, s = a.hasOwnProperty("$msg") ? a.$msg : Z;
                s && _(e.split(/\s+/), function (e) {
                    var t = s[e] || (s[e] = []);
                    t.cache = i;
                    for (var n = 0, r = t.length; n < r && ((o = t[n]).once && t.splice(n, 1), !1 !== o.apply(a, i)); n++) ;
                })
            }
        };
        var ee = x.$q = x.q = function () {
            var c = function () {
                var s, u = [], t = {
                    resolve: function (e, t) {
                        if (u) {
                            var n = u;
                            u = $, s = o(e), _(n, function (e) {
                                s.then(e[0], e[1], e[2])
                            })
                        } else t && (s = o(e))
                    }, reject: function (e) {
                        t.resolve(n(e))
                    }, notify: function (t) {
                        u && _(u, function (e) {
                            e[2](t)
                        })
                    }, promise: {
                        then: function (t, n, r) {
                            var i = c(), e = function (e) {
                                try {
                                    i.resolve((T(t) ? t : f)(e))
                                } catch (e) {
                                    i.reject(e), de(e)
                                }
                            }, o = function (e) {
                                try {
                                    i.resolve((T(n) ? n : l)(e))
                                } catch (e) {
                                    i.reject(e), de(e)
                                }
                            }, a = function (e) {
                                try {
                                    i.notify((T(r) ? r : f)(e))
                                } catch (e) {
                                    de(e)
                                }
                            };
                            return u ? u.push([e, o, a]) : s.then(e, o, a), i.promise
                        }, catch: function (e) {
                            return this.then(null, e)
                        }, finally: function (r) {
                            function i(n, r) {
                                return new te(function (e, t) {
                                    r ? e(n) : t(n)
                                })
                            }

                            function t(e, t) {
                                var n = null;
                                try {
                                    n = (r || f)()
                                } catch (e) {
                                    return i(e, !1)
                                }
                                return n && T(n.then) ? n.then(function () {
                                    return i(e, t)
                                }, function (e) {
                                    return i(e, !1)
                                }) : i(e, t)
                            }

                            return this.then(function (e) {
                                return t(e, !0)
                            }, function (e) {
                                return t(e, !1)
                            })
                        }
                    }
                };
                return t
            }, n = function (r) {
                return {
                    then: function (e, t) {
                        var n = c();
                        try {
                            n.resolve((T(t) ? t : l)(r))
                        } catch (e) {
                            n.reject(e)
                        }
                        return n.promise
                    }
                }
            };

            function o(n) {
                return n && T(n.then) ? n : {
                    then: function (e) {
                        var t = c();
                        return t.resolve(e(n)), t.promise
                    }
                }
            }

            function f(e) {
                return e
            }

            function l(e) {
                return n(e)
            }

            return {
                defer: c, reject: n, all: function (e) {
                    var n = c(), r = 0, i = O(e) ? [] : {};
                    return _(e, function () {
                        r++
                    }), _(e, function (e, t) {
                        o(e).then(function (e) {
                            i.hasOwnProperty(t) || (i[t] = e, --r || n.resolve(i))
                        }, function (e) {
                            i.hasOwnProperty(t) || n.reject(e)
                        })
                    }), 0 === r && n.resolve(i), n.promise
                }, race: function (e) {
                    var t = c(), n = 0;
                    return _(e, function () {
                        n++
                    }), _(e, function (e) {
                        o(e).then(function (e) {
                            t.resolve(e)
                        }, function (e) {
                            0 == --n && t.reject(e)
                        })
                    }), 0 === n && t.resolve(), t.promise
                }, ref: o, never: {
                    then: function () {
                    }
                }
            }
        }(), te = x.Promise = function (e) {
            var t = ee.defer();
            return T(e) && e(t.resolve, t.reject), t.promise
        };
        te.all = ee.all, te.race = ee.race, te.reject = ee.reject, te.resolve = function (e) {
            var t = ee.defer();
            return t.resolve(e), t.promise
        }, x.ajax = function () {
            var t = /^\s*(\[|\{[^\{])/, n = /[\}\]]\s*$/, e = {"Content-Type": "application/json;charset=utf-8"}, a = f.defaults = {
                transformResponse: [function (e) {
                    return h(e) && t.test(e) && n.test(e) && (e = JSON.parse(e, !0)), e
                }], transformRequest: [function (e) {
                    return !O(e) && !d(e) || x.isFile(e) ? e : JSON.stringify(e)
                }], headers: {common: {Accept: "application/json, text/plain, */*"}, post: X({}, e), put: X({}, e), patch: X({}, e)}
            }, c = function (v) {
                return function (e, t, n, r, i, o, a) {
                    var s, u, c = oe(this) ? this : window;
                    if ("REQUIRE" == e) m(t, function (e) {
                        h(r, e || 200)
                    }, i); else if ("JSONP" == e) {
                        var f = "_" + (v.counter++).toString(36), l = v[f] = function (e) {
                            l.datas || (l.datas = []), l.datas.push(e)
                        };
                        if (/callback=(\w+)/.test(t)) {
                            var d = RegExp.$1;
                            "CALLBACK" != d && (l = c[d] || (c[d] = v[f]))
                        }
                        m(t.replace("CALLBACK", "bowlder.cb." + f), function () {
                            var e = l.datas && l.datas.shift();
                            g(e) ? h(r, 200, e) : h(r, s || -2), delete v[f]
                        }, i)
                    } else {
                        var p = -1 != t.indexOf("//") && -1 == t.indexOf(location.host + "/");
                        (u = function (e) {
                            return y <= 6 ? new this.ActiveXObject("Microsoft.XMLHTTP") : y < 10 && "PATCH" === e ? new this.XDomainRequest : new this.XMLHttpRequest
                        }.call(c, p ? "PATCH" : e)).open(e, t, !0), _(i, function (e, t) {
                            "withCredentials" == t ? u.withCredentials = e : g(e) && u.setRequestHeader && u.setRequestHeader(t, e)
                        }), y < 10 && p ? (u.onload = function () {
                            h(r, 200, -1 !== s ? u.response || u.responseText : null, null)
                        }, u.onerror = function () {
                            h(r, u.status || -1, u.error, null)
                        }) : u.onreadystatechange = function () {
                            if (u && 4 == u.readyState) {
                                var e = null, t = null;
                                -1 !== s && (t = u.response || u.responseText, u.getAllResponseHeaders && (e = u.getAllResponseHeaders())), h(r, s || u.status, t, e)
                            }
                        }, a && (u.responseType = a), u.send(n || null)
                    }

                    function h(e, t, n, r) {
                        u = null, e(t = 1223 == (t = 0 === t ? n ? 200 : 404 : t) ? 204 : t, n, r)
                    }

                    o.then(function () {
                        s = -1, u ? u.abort() : h(r, s)
                    })
                };

                function m(e, t, n) {
                    var r = y < 9 ? document.getElementsByTagName("head")[0] : document.body || document.head || document.getElementsByTagName("head")[0] || document.documentElement, i = document.createElement("script");

                    function o(e) {
                        t && t(p(e) ? e : 200), i.onreadystatechange = i.onload = i.onerror = t = null;
                        try {
                            r.removeChild(i)
                        } catch (e) {
                        }
                    }

                    y < 9 && (i.onreadystatechange = function () {
                        /loaded|complete/.test(i.readyState) && o()
                    }), i.onload = o, i.onerror = function () {
                        o(400)
                    }, i.charset = n && n.charset || "utf-8", i.src = e, r.appendChild(i)
                }
            }(x.cb), s = ["GET", "REQUIRE", "JSONP"];

            function f(e) {
                var n = {transformRequest: a.transformRequest, transformResponse: a.transformResponse}, t = a.headers, r = X({}, t.common, t[A(e.method)], e.headers);
                X(n, e), n.headers = r, n.method = v(n.method) || "GET", n.url = I(n.url, n.params);
                var i = function (n) {
                    r = n.headers;
                    var e = "";
                    return -1 < s.indexOf(n.method) && d(n.data) ? n.url = I(n.url, n.data) : e = !1 === n.processData ? n.data : o(n.data, fe(r), n.transformRequest), _(r, function (e, t) {
                        "content-type" === A(t) && (g(n.data) && r[t] || delete r[t])
                    }), function (o, e, t) {
                        var n = o.url;
                        f.pendingRequests.push(o);
                        var a, s = ee.defer(), r = new te(function (r, i) {
                            c.call(o.win, o.method, n, e, function (e, t, n) {
                                a && clearTimeout(a), (l(e = Math.max(e, 0)) ? r : i)({data: t, status: e, headers: fe(n), config: o})
                            }, t, s.promise, o.responseType)
                        });

                        function i() {
                            var e = f.pendingRequests.indexOf(o);
                            -1 !== e && f.pendingRequests.splice(e, 1)
                        }

                        return r.then(i, i), (r = r.then(u, u)).abort = function () {
                            s.resolve()
                        }, T(o.beforeSend) && !1 === o.beforeSend(r, o) && r.abort(), 0 < o.timeout && (a = setTimeout(r.abort, o.timeout)), r
                    }(n, e, r)
                }(n);
                return i.success = function (t) {
                    return i.then(function (e) {
                        t(e.data, e.status, e.headers, n)
                    }), i
                }, i.error = function (t) {
                    return i.then(null, function (e) {
                        t(e.data, e.status, e.headers, n)
                    }), i
                }, i;

                function o(t, n, e) {
                    return T(e) ? e(t, n) : (_(e, function (e) {
                        t = e(t, n)
                    }), t)
                }

                function u(e) {
                    var t = X(e, {data: o(e.data, e.headers, n.transformResponse)});
                    return l(e.status) ? t : ee.reject(t)
                }
            }

            function l(e) {
                return 200 <= e && e < 300
            }

            return f.pendingRequests = [], _(["get", "delete", "head", "jsonp", "require"], function (n) {
                f[n] = function (e, t) {
                    return f(X(d(t) ? t : h(t) ? {headers: {charset: t}} : {}, {win: this, method: n, url: e}))
                }
            }), _(["post", "put"], function (r) {
                f[r] = function (e, t, n) {
                    return f(X(d(n) ? n : {}, {win: this, method: r, url: e, data: t}))
                }
            }), f
        }();
        var ne = function (e) {
            e ? 1 !== e.nodeType && le(e) || (e = [e]) : e = [];
            for (var t = this.length = e.length, n = 0; n < t; n++) this[n] = e[n]
        };
        x.fn = ne.prototype = {
            add: function (e) {
                var t = this;
                return T(e.each) ? e.each(function () {
                    t.add(this)
                }) : t[t.length++] = e, t
            }, eq: function (e) {
                return this.length <= 1 ? this : new ne(this[e])
            }, filter: function (t) {
                var n = [];
                if (h(t)) {
                    var r = l.cssQuery(t);
                    t = function (e) {
                        return ~r.indexOf(e)
                    }
                }
                return T(t) && this.each(function (e) {
                    t(e) && n.push(e)
                }), new ne(n)
            }, each: function (e, t) {
                for (var n = 0, r = this.length; n < r; n++) e.call(t || this[n], this[n], n);
                return this
            }, parent: function (t) {
                var n = [];
                return this.each(function () {
                    var e = S.parent(this, t);
                    e && -1 == n.indexOf(e) && n.push(e)
                }), x(n)
            }, closest: function (t) {
                var n = [];
                return t && this.each(function () {
                    var e = S.parent(this, t, !0);
                    e && -1 == n.indexOf(e) && n.push(e)
                }), x(n)
            }, children: function () {
                var t = [];
                return this.each(function () {
                    _(this.children, function (e) {
                        t.push(e)
                    })
                }), x(t)
            }, html: function (e) {
                return g(e) ? this.each(function () {
                    this.innerHTML = e
                }) : this[0] ? this[0].innerHTML : ""
            }, text: function (e) {
                var t = y < 9 ? "innerText" : "textContent";
                return g(e) ? this.each(function () {
                    this[t] = e
                }) : this[0] ? this[0][t] : ""
            }, hasClass: function (e) {
                var t = !1;
                return this.each(function () {
                    t = t || P.contains(this, e)
                }), t
            }, append: function (e, t) {
                var n = this;
                if (h(e)) e = S.create(e); else if (le(e)) return _(u.call(e), function (e) {
                    n.append(e, t)
                }), n;
                return this.each(function () {
                    this.appendChild(t ? e.cloneNode(!0) : e)
                })
            }, prepend: function (e, t) {
                var n = this;
                if (h(e)) e = S.create(e); else if (le(e)) return _(u.call(e), function (e) {
                    n.prepend(e, t)
                }), n;
                return this.each(function () {
                    this.insertBefore(t ? e.cloneNode(!0) : e, this.firstChild)
                })
            }, appendTo: function (e) {
                return h(e) ? e = l.cssQuery(e)[0] : !e.nodeType && e[0] && (e = e[0]), e && 1 === e.nodeType ? this.each(function () {
                    e.appendChild(this)
                }) : this
            }, prependTo: function (e) {
                return h(e) ? e = l.cssQuery(e)[0] : !e.nodeType && e[0] && (e = e[0]), e && 1 === e.nodeType ? this.each(function () {
                    e.insertBefore(this, e.firstChild)
                }) : this
            }, attr: function (t, n) {
                return g(n) || d(t) ? this.each(function (e) {
                    S.attr(e, t, n)
                }) : this[0] && S.attr(this[0], t)
            }, offset: function (e) {
                return S.offset(this[0], e)
            }, removeAttr: function (e) {
                return this.each(function () {
                    this.removeAttribute(e)
                })
            }, prop: function (e, t) {
                return g(t) ? this.each(function () {
                    this[e] = t
                }) : this[0] && this[0][e]
            }, data: function (t, n) {
                return g(n) || d(t) ? this.each(function (e) {
                    S.data(e, t, n)
                }) : this[0] && S.data(this[0], t)
            }, removeData: function (e) {
                return this.each(function () {
                    delete ve(this)[e]
                })
            }, val: function (t) {
                var e = this[0];
                return e ? g(t) ? _(this, function (e) {
                    S.val(e, t)
                }) : S.val(e) : null
            }, css: function (e, t) {
                var n = this[0];
                if (!n) return d(e) || g(t) ? this : null;
                var r = S.css(n, e, t);
                if (g(r)) return r;
                for (var i = 1; i < this.length; i++) S.css(this[i], e, t);
                return this
            }, find: function (t) {
                var n = [];
                return _(this, function (e) {
                    _(l.cssQuery(t, e), function (e) {
                        -1 == n.indexOf(e) && n.push(e)
                    })
                }), x(n)
            }
        }, _(["on", "off", "emit"], function (r) {
            x.fn["$" + r] = function () {
                var n = 1 <= arguments.length ? u.call(arguments, 0) : [];
                return this.each(function () {
                    var e = x.widget(this), t = S.plugin(this);
                    e && e[r].apply(e, n), t && t.then(function (e) {
                        T(e["$" + r]) && e["$" + r].apply(e, n)
                    })
                })
            }
        }), _(["addClass", "removeClass", "toggleClass", "delegate", "undelegate", "bind", "unbind", "remove", "show", "hide", "toggle", "trigger", "animate", "stop", "pause", "fadeIn", "fadeOut", "fadeToggle", "slideUp", "slideDown", "slideToggle"], function (t) {
            x.fn[t] = function () {
                var e = 1 <= arguments.length ? u.call(arguments, 0) : [];
                return this.each(function () {
                    S[t].apply(S, [this].concat(e))
                })
            }
        }), _(["focus", "blur", "submit"], function (e) {
            x.fn[e] = function () {
                return this.each(function () {
                    this[e]()
                })
            }
        }), _(["width", "height", "scrollLeft", "scrollTop"], function (n) {
            x.fn[n] = function (e) {
                var t = this.css(n, e);
                return g(e) || (t = parseInt(t, 10) || 0), t
            }
        }), _(["before", "after"], function (n) {
            x.fn[n] = function (e, t) {
                return !e.nodeType && e[0] && (e = e[0]), 1 !== e.nodeType || (t ? this.each(function () {
                    S[n](e.cloneNode(!0), this)
                }) : S[n](e, this[0])), this
            }, x.fn[pe("insert-" + n)] = function (e) {
                return !e.nodeType && e[0] && (e = e[0]), 1 !== e.nodeType ? this : this.each(function () {
                    S[n](this, e)
                })
            }
        }), x.fn.on = x.fn.delegate, x.fn.off = x.fn.undelegate;
        var e, re = ee.defer();
        if (x.ready = function (e, t) {
            var n = re.promise;
            if (T(e)) t = e; else {
                if (!O(e)) return x.rootWidget.defer.promise;
                var r = [];
                _(e, function (e) {
                    r.push(e.ready())
                }), n = ee.all(r)
            }
            return T(t) && n.then(function (e) {
                try {
                    t.call(document, e)
                } catch (e) {
                    de(e)
                }
            }), n
        }, y && !this.addEventListener && (x.needPolyfill = !0, document.write('<script src="http://img2.cache.netease.com/f2e/modules/polyfill.js"><\/script>')), "complete" !== document.readyState) {
            if (document.addEventListener) document.addEventListener("DOMContentLoaded", me, !1), window.addEventListener("load", me, !1); else if (document.attachEvent) {
                var ie = function () {
                    "complete" == document.readyState && me()
                };
                document.attachEvent("onreadystatechange", ie), window.attachEvent("onload", ie)
            }
        } else setTimeout(me)
    }

    function oe(e) {
        return e && e.document && e.window == e
    }

    function ae(e, t) {
        if (oe(e)) {
            if (e = e.document.documentElement, /^(width|height)$/.test(t)) return e[pe("client-" + t)] || this[pe("inner-" + t)]
        } else 9 == e.nodeType && (e = e.documentElement);
        if (y && /^(width|height)$/.test(t)) return e[pe("offset-" + t)];
        if (y < 9 && "opacity" == t) {
            var n = ae(e, "filter") || "";
            return /opacity=(\d+)/.test(n) ? RegExp.$1 : 1
        }
        if (G.test(t)) {
            var r = pe(F), i = e.style[r];
            return i && new RegExp(t + "\\s*\\((.*?)\\)").test(i) ? RegExp.$1 : ae(e, r)
        }
        return e.currentStyle ? e.currentStyle[t] || "" : window.getComputedStyle ? window.getComputedStyle(e, null)[t] : ""
    }

    function se(e, t, n) {
        if ((he(t) || he(t = pe(t))) && g(n) && (!isNaN(n) || "number" != typeof n)) {
            if (y < 9 && "opacity" == t && p(n)) {
                var r = ae(e, "filter") || "", i = /alpha\([^\)]*\)/;
                return n = "alpha(opacity=" + Math.round(100 * parseFloat(isNaN(n) ? 1 : n)) + ")", void (e.style.filter = i.test(r) ? r.replace(i, n) : n)
            }
            if ("transform" == t && (t = pe(F)), G.test(t)) return !isNaN(n) && n && (0 === t.indexOf("translate") ? n += "px" : 0 === t.indexOf("rotate") && (n += "deg")), void (e.style[pe(F)] = t + "(" + n + ")");
            if ("show" == n) {
                if ("none" != ae(e, "display")) return;
                S.show(e), n = ae(e, t), se(e, H, "0s"), se(e, t, 0), e.offsetWidth, se(e, H, "")
            } else "hide" == n && (n = 0, e.style[t] = ae(e, t), e.offsetWidth);
            isNaN(n) || !n || a[t] || (n += "px");
            try {
                e.style[t] = n
            } catch (e) {
                de(e)
            }
        }
    }

    function ue(n, r) {
        var e = "b$Event" + (r[S._idname] || "");
        return n[e] = n[e] || function (e) {
            var t = n.call(r || this, S._fixe(e));
            return !1 !== t && -1 !== t || e.preventDefault(), -1 === t && e.stopPropagation(), t
        }
    }

    function ce(e) {
        return encodeURIComponent(e).replace(/%3A/gi, ":").replace(/%24/g, "$").replace(/%2C/gi, ",")
    }

    function fe(a) {
        var s = d(a) ? a : $;
        return function (e) {
            var t, n, r, i, o;
            return s || (o = {}, (t = a) && _(t.split("\n"), function (e) {
                i = e.indexOf(":"), n = A(e.substr(0, i).trim()), r = e.substr(i + 1).trim(), n && (o[n] ? o[n] += ", " + r : o[n] = r)
            }), s = o), e ? s[A(e)] || null : s
        }
    }

    function le(e) {
        if (null == e || oe(e)) return !1;
        var t = e.length;
        return !(1 !== e.nodeType || !t) || (h(e) || O(e) || 0 === t || "number" == typeof t && 0 < t && t - 1 in e)
    }

    function de(e) {
        if ("undefined" != typeof console && g(console.error)) {
            if (!/firefox/i.test(f) && e instanceof Error) if (e.stack) {
                var t = e.stack;
                e = e.message && -1 === t.indexOf(e.message) ? "Error: " + e.message + "\n" + t : t
            } else e.sourceURL && (e = e.message + "\n" + e.sourceURL + ":" + e.line);
            console.error(e)
        }
    }

    function pe(e) {
        return e.replace(/-([a-z])/g, function (e, t) {
            return v(t)
        }).replace(/^Ms([A-Z])/, "ms$1")
    }

    function he(e) {
        return "opacity" == e || G.test(e) || g(L.style[e])
    }

    function ve(e) {
        var t = S._nodeId(e);
        return ve[t] || (ve[t] = {$on: x.on, $off: x.off, $emit: x.emit, $msg: {}})
    }

    function me() {
        if (!e) {
            if (!document.body) return void setTimeout(me, 13);
            if (x.needPolyfill) return;
            e = !0, re.resolve()
        }
    }
}).call(this), function (w, i, $) {
    if (!w.expr) {
        var u, o = {}, s = this, c = function (e) {
                return k(e) ? e.toLowerCase() : e
            }, t = /(^|\s|[\(,;])([\w\.\[\]]+)\?(?=$|\s*[\),;])/g, a = function (e) {
                return e.replace(t, "$1(typeof $2=='undefined'?null:$2)")
            }, _ = w.expr = function (t, e, n, r) {
                try {
                    o[t] || (o[t] = i(a(t)))
                } catch (e) {
                    throw"invalid expression: " + a(t) + "\n" + e
                }
                try {
                    r = o[t](e || window)
                } catch (e) {
                    if (n || u) throw e
                }
                return r
            }, f = /\s*;\s*/, T = /(\\?)\{\!?\{(.+?)\}\}/, n = /[;\s]+/, O = w.utils, v = O.rAF, A = w.dom, l = O.msie, N = w.extend, x = [].slice, E = w.each, j = w.isObject, d = w.isNumber, k = w.isString, S = w.isArray, D = w.isFunction, m = w.q, p = w.classList, C = w.isDefined, h = w.ajax, g = w.Promise,
            r = {"'": "'", "\\": "\\", "\r": "r", "\n": "n", "\t": "t", "\u2028": "u2028", "\u2029": "u2029"}, y = function (e, t) {
                if (S(e)) {
                    var n = e.indexOf(t);
                    ~n && e.splice(n, 1)
                }
            };
        w.throttle = function (n, r, i) {
            if (!r) return n;
            var o, a, s, u = null, c = 0, f = function () {
                c = !1 === i ? 0 : +new Date, u = null, s = n.apply(o, a), u || (o = a = null)
            };
            return function () {
                var e = +new Date;
                c || !1 !== i || (c = e);
                var t = r - (e - c);
                return o = this, a = arguments, t <= 0 || r < t ? (u && (clearTimeout(u), u = null), c = e, s = n.apply(o, a), u || (o = a = null)) : u || (u = setTimeout(f, t)), s
            }
        }, w.debounce = function (t, n, r) {
            if (!n) return t;
            var i, o, a, s, u, c = function () {
                var e = +new Date - s;
                e < n && 0 <= e ? i = setTimeout(c, n - e) : (i = null, r || (u = t.apply(a, o), i || (a = o = null)))
            };
            return function () {
                a = this, o = arguments, s = +new Date;
                var e = r && !i;
                return i || (i = setTimeout(c, n)), e && (u = t.apply(a, o), a = o = null), u
            }
        };
        var b = {
            _map: {}, add: function (e, t) {
                var n = b._map, r = n[e] || (n[e] = []);
                t && -1 == r.indexOf(t) && r.push(t)
            }, remove: function (e, t) {
                var n = b._map, r = n[e];
                if (S(r)) if (t) {
                    var i = r.indexOf(t);
                    -1 != i && r.splice(i, 1)
                } else delete n[e]
            }
        }, R = function (t) {
            return t ? new g(function (e) {
                setTimeout(e, t)
            }) : m.ref()
        };
        w.fx = function (n) {
            return n.start || (n.start = null), n.end || (n.end = null), function (e, t) {
                return N(new ye(e), n, t)
            }
        }, w.fx.add = b.add, w.fx.remove = b.remove, w.fx.play = function (e) {
            var r = [], t = b._map[e];
            return t && t.length ? (E(t, function (e, t) {
                var n = D(e.delay) ? e.delay(t) : e.delay;
                r.push(R(n).then(function () {
                    return e.play()
                }))
            }), m.all(r).then(function () {
                w.fx.play(e + ".then")
            })) : m.ref()
        };
        var I = {};
        w.fx.repeat = function (e, t, n) {
            var r = [], i = b._map[e];
            !i.length || d(t) && 0 == t-- || (I[e] = +new Date, E(i, function (e, t) {
                r.push(e.play())
            }), m.all(r).then(function () {
                I[e] && (!n && +new Date - I[e] < 25 && (n = 25), R(n).then(function () {
                    w.fx.repeat(e, t, n)
                }))
            }))
        }, E(["pause", "stop"], function (t) {
            w.fx[t] = function (e) {
                delete I[e], E(b._map[e], function (e) {
                    e[t]()
                })
            }
        }), w.fx.step = function (e, n) {
            "number" != typeof n ? n = 0 : n < 0 ? n = 0 : 1 < n && (n = 1), E(b._map[e], function (e) {
                var t;
                D(e.step) && (t = e.fix().step(n, e.$el)), (j(t) || j(e.to)) && e.$el.animate(N(t || {}, e.to), n, e.ease), e.progress = n
            })
        }, ye.prototype = {
            fix: function () {
                if (S(this.step)) {
                    var s = this.step, u = s.length, c = [];
                    E(s, function (e) {
                        c.push(parseFloat(e.ratio) || 1 / u), delete e.ratio
                    }), this.step = function (e) {
                        for (var t, n = 0, r = {}, i = 0; i < u; i++) {
                            n += c[i], t = i == u - 1 || e <= n;
                            var o = s[i], a = t ? (e - n + c[i]) / c[i] : 1;
                            if (E(o, function (e, t) {
                                r[t] = O.interpolate(e, a)
                            }), t) return r
                        }
                    }
                }
                return this
            }, play: function (e) {
                this.fix().pause(), e = (e || this.duration) * (1 - this.progress);
                var t, n, r, i, o, a, s, u, c, f, l = this, d = l.$el, p = m.defer(), h = function () {
                    l._endTimeout = l._updator = null, D(l.end) && l.end(d), j(l.reset) && d.css(l.reset), l.progress = 0, p.resolve()
                };
                return D(l.start) ? (l.start(d), l._endTimeout = setTimeout(h, e + 10)) : !D(l.step) && j(l.to) ? l._updator = d.animate(l.to, e, l.ease, h) : (D(l.step) || j(l.to)) && (l._updator = (o = (t = l).progress || 0, a = t.duration, s = +new Date, u = m.defer(), c = le[t.ease] || le.linear, (f = function () {
                    var e;
                    r = c(Math.min((+new Date - s) / a, 1)), !n && r < 1 && v(f), i = o * (1 - r) + r, D(t.step) && (e = t.step(i, t.$el)), (j(t.to) || j(e)) && t.$el.animate(N(e || {}, t.to), i), 1 == r && u.resolve()
                })(), u.promise.pause = function () {
                    n = !0, t.progress = i
                }, u.promise), l._updator.then(h)), p.promise
            }, repeat: function (e, t) {
                var n = this;
                d(e) && 0 == e-- || (n._repeating = +new Date, n.play().then(function () {
                    n._repeating && (!t && +new Date - n._repeating < 25 && (t = 25), R(t).then(function () {
                        n.repeat(e, t)
                    }))
                }))
            }, pause: function () {
                var e = this._updator, t = this._endTimeout;
                e && D(e.pause) && e.pause(), t && clearTimeout(t), this._updator = this._endTimeout = this._repeating = null
            }, stop: function () {
                this.pause(), this.progress = 0
            }, start: function (e) {
                var t = this.prefix;
                e.removeClass(t + "-end"), e.addClass(t + "-init"), e[0].offsetWidth, e.addClass(t + "-ing")
            }, end: function (e) {
                var t = this.prefix;
                e.removeClass(t + "-ing"), e.addClass(t + "-end")
            }
        };
        var L = /\\|'|\r|\n|\t|\u2028|\u2029/g, P = function (e) {
            return "\\" + r[e]
        }, M = w.template = {
            replace: function (t, e, n, a, s) {
                if (!C(e)) return t;
                S(e) || (e = [e]);
                var r = [];
                return E(e, function (e) {
                    var o;
                    r.push((o = e, t.replace(n || /\{\!?\{([^}]+)\}\}/g, function (e, t) {
                        if (s && !s.test(e)) return e;
                        var n, r, i = _(t, o, u);
                        return null != i ? i : (r = e, null != (n = a) ? n : "!" == r.substr(1, 1) ? "" : r)
                    })))
                }), r.join("")
            }, parse: function (i, e) {
                var o = 0, a = "var __t,__p='',__j=Array.prototype.join,print=function(){__p+=__j.call(arguments,'');};\nwith(obj||{}){__p+='";
                i.replace(/<%=([\s\S]+?)%>|<%([\s\S]+?)%>|$/g, function (e, t, n, r) {
                    return a += i.slice(o, r).replace(L, P), t ? a += ~t.indexOf("(") ? "'+\n(typeof (__t=" + t + ") =='undefined'||__t==null?'':__t)+'" : "'+\n(typeof (" + t + ") =='undefined'||(__t=(" + t + "))==null?'':__t)+'" : n && (a += "';\n" + n + "\n__p+='"), o = r + e.length, e
                }), a += "';\n}return __p;";
                var t = new Function("obj", a);
                return e ? t(e) : t
            }
        }, F = function (e) {
            return e && k(e.tagName) ? e.tagName.toLowerCase() : ""
        }, q = {
            $on: w.on, $off: w.off, $once: w.once, $emit: w.emit, $extend: function () {
                var e = !0, t = 1 <= arguments.length ? x.call(arguments, 0) : [];
                "boolean" == typeof t[0] && (e = t.shift());
                return N.apply(this, [e, this].concat(t))
            }
        };
        A.plugin = function (e, t, n) {
            return k(t) || j(t) ? Te(e, t, n) : A.data(e, "_b$pluginPromise")
        }, w.fn.plugin = function (t, n) {
            var r = m.defer(), e = this, i = [];
            return w.ready(function () {
                E(e, function (e) {
                    i.push(A.plugin(e, t, n))
                }), m.all(i).then(function (e) {
                    r.resolve(e)
                })
            }), r.promise
        };
        var H = {
            "ne-role": function (e, t, n) {
                var r = this.__roles;
                E(n.split(/\s+/), function (e) {
                    e && (r[e] || (r[e] = []), r[e].push(t))
                })
            }, "ne-model": function (e, t, n, r) {
                var i = this.models, o = i.add(t, n.replace(/^\s*{{(.*?)}}\s*$/, "$1"), e);
                r.push(function () {
                    i.remove(o)
                })
            }, "ne-if": function (t, n, e, r) {
                var i = this;
                if (A.data(n, "_b$ifed", !0), k(e)) {
                    if (e = e.replace(/^\s*{{(.*?)}}\s*$/, "$1"), "script" == F(n)) return H["ne-repeat"].call(i, t, n, "", r, !0);
                    var o = document.createTextNode(""), a = !0, s = !1;
                    A.after(o, n), S(n["ne-destroy"]) && n["ne-destroy"].push(function () {
                        A.remove(o)
                    });
                    var u = function (e) {
                        a && !e ? (A.remove(n), a = !1) : !a && e && (A.before(n, o), a = !0), a && !s && (s = !0, i.$refresh2 = !0, i.wander(n, t, !0), i.compile(n))
                    };
                    return r.push(i.views.add({fn: u, model: e, scope: t})), u(t.$parse(e)), o
                }
            }, "ne-html": function (n, r, e, t) {
                var i = this;
                if (e) {
                    t.push(i.views.add({
                        fn: function (e, t) {
                            C(e) || (e = ""), l <= 9 && /tr|thead|tbody|tfoot/.test(F(r)) ? (E(r.children, function (e) {
                                r.removeChild(e)
                            }), r.appendChild(A.create(e))) : r.innerHTML = e, e && ~e.toString().indexOf("<") && (i.wander(r, n), i.compile(r), i.$refresh2 = !0)
                        }, model: e, scope: n, debug: w.debug && ~e.indexOf("(")
                    }))
                }
            }, "ne-text": function (e, n, t, r) {
                if (t) {
                    r.push(this.views.add({
                        fn: function (e, t) {
                            C(e) || (e = ""), n[l < 9 ? "innerText" : "textContent"] = e
                        }, model: t, scope: e, debug: w.debug && ~t.indexOf("(")
                    }))
                }
            }, "ne-state-extend": function (e, r, t, n) {
                var i = this.scope;
                if (t) {
                    r.removeAttribute("ne-state-extend");
                    var o = function (e, t) {
                        var n = w.widget(r);
                        e && n && n.prepared(function () {
                            N(!0, n.scope.state, e)
                        })
                    };
                    i.$watch(t, o, e, !0), n && n.push(function () {
                        i.$unwatch(t, o, e)
                    })
                }
            }, "ne-on": function (e, t, n, r) {
                var i, o = this.scope, a = w.widget(t);
                if (n && a) for (; null !== (i = K.exec(n));) {
                    var s = i[1], u = function (t) {
                        return function () {
                            var e = _(t, o, !0);
                            D(e) && e.apply(o, arguments)
                        }
                    }(i[2]);
                    a.on(s, u), r.push(function () {
                        a.off(s, u)
                    })
                }
            }, "ne-extend": function (e, r, t, n) {
                var i = this.scope;
                if (t && /^\s*{{(.*?)}}\s*$/.test(t)) {
                    r.removeAttribute("ne-extend");
                    var o = function (e, t) {
                        var n = w.widget(r);
                        n && n.extend(e)
                    };
                    i.$watch(t, o, e, !0), n && n.push(function () {
                        i.$unwatch(t, o, e)
                    })
                }
            }, "ne-options": function (e, n, t, r) {
                var i = this, o = i.scope;
                if (n.removeAttribute("ne-options"), n.options) {
                    var a = n.options.length, s = function (e) {
                        for (; a < n.options.length;) n.remove(a);
                        var r, t, i;
                        r = n, i = S(t = e), E(t, function (e, t) {
                            i && (j(e) ? (t = e.value, e = C(e.label) ? e.label : e.value) : t = e);
                            var n = new Option(e, t);
                            r.options.add(n)
                        })
                    };
                    o.$watch(t, s, e, !0), r && r.push(function () {
                        o.$unwatch(t, s, e)
                    })
                }
            }, "ne-foreach": function (e, t, n, r) {
                return t.removeAttribute("ne-foreach"), H["ne-repeat"].call(this, e, t, n, r, !0)
            }, "ne-recurse": function (e, t, n, r) {
                if (e.hasOwnProperty("$recurse")) {
                    var i = e.$recurse;
                    if (n && -1 != n.indexOf(i.key + ".")) {
                        var o = document.createTextNode("");
                        A.replace(o, t);
                        var a = {node: [o], type: "repeat", key: i.key, attr: i.attr, isJoin: i.isJoin, model: n, scope: e};
                        return r.push(this.views.add(a)), r.subnode || (r.subnode = []), o
                    }
                }
            }, "ne-repeat": function (e, t, n, r, i) {
                var o = "script" == F(t);
                if (!i && !n) return !1;
                var a, s = document.createTextNode("");
                if (S(t)) {
                    A.before(s, t[0]);
                    var u = document.createDocumentFragment();
                    E(t, function (e) {
                        u.appendChild(e)
                    }), t = u
                } else t.removeAttribute("ne-repeat");
                if (o) {
                    var c = M.parse(t.innerHTML.trim());
                    a = function () {
                        var e = "";
                        try {
                            e = c.apply(this, arguments)
                        } catch (e) {
                            console.error(e)
                        }
                        return e
                    }
                } else {
                    if (i) throw"ne-foreach should be used in script.";
                    A.before(s, t);
                    var f = document.createElement("div");
                    f.appendChild(t), a = f.innerHTML.replace(/&amp;/g, "&"), A.remove(t)
                }
                s.parentNode || A.replace(s, t);
                var l = {node: [s], attr: a, scope: e, isJoin: i, type: "repeat", destroys: r};
                if (o) {
                    var d = t.getAttribute("ne-if");
                    k(d) && (l.cond = d.replace(/^\s*{{(.*?)}}\s*$/, "$1"))
                }
                /^\s*(\S+)\s+in\s+(.*)/.test(n) ? N(l, {key: RegExp.$1, model: RegExp.$2}) : l.model = n;
                var p = this.views.add(l);
                return r.push(p), A.data(t, "_b$selfcide", function () {
                    p(), E(l.node, function (e) {
                        E(e, A.remove)
                    })
                }), r.subnode || (r.subnode = []), s
            }, "ne-repeat-start": function (e, t, n, r) {
                var i = !1, o = [t];
                for (t.removeAttribute("ne-repeat-start"); t = t.nextSibling;) if (o.push(t), t.getAttribute && k(t.getAttribute("ne-repeat-end"))) {
                    t.removeAttribute("ne-repeat-end"), i = !0;
                    break
                }
                return !i || H["ne-repeat"].call(this, e, o, n, r)
            }, "ne-fx": function (e, t, n, r) {
                var i, o = {};
                if (n) {
                    for (var a = w(t); null !== (i = K.exec(n));) {
                        var s, u = i[2].trim();
                        if (/^(\d+)(\S*)/.test(u)) (s = new ye(t)).duration = parseInt(RegExp.$1), RegExp.$2 && (s.prefix = RegExp.$2); else {
                            var c = u.split("@"), f = parseInt(c[1]), l = e.$parse(c[0]);
                            j(l) && (l = w.fx(l)), s = D(l) && l(a), d(f) && (s.duration = f)
                        }
                        s && (o[i[1]] = s)
                    }
                    E(o, function (e, t) {
                        b.add(t, e)
                    }), r.push(function () {
                        E(o, function (e, t) {
                            b.remove(t, e)
                        })
                    })
                }
            }
        }, B = {};
        w.directive = function (e, t, r) {
            if (!D(t) && D(r) && (t = function (e, t, n) {
                return e.$view(n, r.bind(e, t))
            }), !e || !D(t)) return B[e];
            B[e] = t
        }, w.map = function (e, n, r) {
            if (!e) return e;
            var i = S(e) ? [] : {};
            return E(e, function (e, t) {
                i[t] = D(n) ? n.call(r, e, t, i) : e
            }), i
        };
        var U = w.any = function (e, t) {
            return !E(e, function (e) {
                return !t(e)
            }, !0)
        };
        w.all = function (e, t) {
            return E(e, function (e) {
                return t(e)
            }, !0)
        };
        w.filter = function (e, r, i) {
            if (!e) return e;
            var o = S(e), a = o ? [] : {};
            return E(e, function (n, e) {
                if (D(r)) {
                    if (!r.call(i, n, e, a)) return
                } else if (r) {
                    var t = !1;
                    if (k(n) ? t = Oe(n, r) : j(n) && (t = j(r) ? w.all(r, function (e, t) {
                        return e === n[t]
                    }) : U(n, function (e) {
                        return Oe(e, r)
                    })), !t) return
                }
                o ? a.push(n) : a[e] = n
            }), a
        }, Ne.prototype = N(Object.create(q), {
            $reverse: function (e) {
                return N([], e).reverse()
            }, $sort: function (e, r, i) {
                var t = D(r) ? r : function (e, t) {
                    var n = i ? -1 : 1;
                    return e[r] < t[r] ? -n : n
                };
                return e.sort(t)
            }, $filter: function (e, t) {
                return S(e) && t ? w.filter(e, t) : e
            }, $watch: function (e, t, n, r) {
                e = e.replace(/^\s*{{([^}]*?)}}\s*$/, "$1");
                var i = this.$widget ? this.$widget.scope : this;
                if (i.hasOwnProperty("$$watches") || (i.$$watches = []), S(this.$$watches) && D(t)) {
                    var o = {fn: t, expr: e, scope: n || this};
                    this.$$watches.push(o), r && Ae.call(n, o, e)
                }
            }, $unwatch: function (e, t, n) {
                var r;
                for (e = e.replace(/^\s*{{([^}]*?)}}\s*$/, "$1"), r = 0; r < this.$$watches.length; r++) {
                    var i = this.$$watches[r];
                    i.expr != e || i.scope != n || t && i.fn != t || this.$$watches.splice(r--, 1)
                }
            }, $parse: function (e, t) {
                return t = t || this, -1 < e.indexOf("{{") ? M.replace(e, t, /{{([^}]+)}}/g, "") : _(e, t)
            }, $cancel: function () {
                return !1
            }, $refresh: function (e) {
                var t = this, n = t.$widget;
                t.hasOwnProperty("$refreshing") && t.$refreshing ? n.$refresh2 = !0 : (t.$refreshing = 1, d(e) ? setTimeout(function () {
                    t.$refresh()
                }, e) : (t.hasOwnProperty("$$watches") && E(t.$$watches, function (e) {
                    Ae.call(e.scope || t, e, e.expr)
                }), n.models && n.models.items.length && (n.views && n.views.refresh("repeat"), n.models.refresh()), n.views && n.views.refresh(), E(n.children, function (e) {
                    e.isReady && e.refresh()
                }), t.$refreshing = 0, n.$refresh2 ? (n.$refreshed = !0, n.$refresh2 = !1, t.$refresh(), n.isReady && n.updateRoles()) : n.$refreshed && (n.$refreshed = !1, n.emit("refreshed"))))
            }
        });
        var e, Q = document.createElement("a"), J = (e = window.location.href, Q.setAttribute("href", e), {href: Q.href, host: Q.host, pathname: "/" === Q.pathname.charAt(0) ? Q.pathname : "/" + Q.pathname}), z = /(^|\/)\w[^\/;,]*?\/\.\.\//, W = /^\.\.\/(.*)/, X = /^\-?([1-9][0-9]*|0)(\.[0-9]+)?$/, G = /[^\/]+?\/?$/,
            K = /([^;\s]+?)\s*[=:]\s*([^;]*)/g, V = /(?:^|\.)(.+?)(?=\[|\.|$|\()|\[(['"]?)(.+?)\2\]/g, Y = {}, Z = {}, ee = 1;
        ke.create = function (e, t, n) {
            var r;
            if (e) {
                var i = e.getAttribute("ne-id"), o = e[A._idname];
                o && Z[o] ? r = Z[o] : i && Z["#" + i] ? r = Z["#" + i] : (r = new ke, i && (Z["#" + i] = r));
                var a = "$$" + (n || e != document.documentElement ? ee++ : 0);
                e["ne-wguid"] = r.guid = a, (Z[a] = r).$root = w(e), t && (r.parent = t).children.push(r), n && (Z[n] || (Z[n] = []), Z[n].push(r))
            } else (r = new ke).children = [], r.scope = w.rootScope, (r.scope.$widget = r).views = new Ue(r), r.models = new Me(r), r.update = w.rootScope.$update = r.models.update.bind(r.models);
            return r.__roles = {}, r.roles = {}, r.constructor = ke, r
        }, ke.shortName = function (e) {
            return e.replace(/.*\//, "").replace(/\..*/, "")
        };
        var te = /^(click|load|dblclick|contextmenu|key\w+|mouse\w+|touch\w+)/, ne = {};
        E(["submit", "load", "change", "focus", "blur", "mouseenter", "mouseleave"], function (e) {
            ne["ne-" + e] = 1
        }), ke.prototype = {
            _assure: function (e) {
                if (!this.isReady) throw"widget is not ready";
                return e.call(this)
            }, lazy: function (e) {
                this._lazyPromise = new g(e)
            }, load: function (t, e, n) {
                var r = this;
                if (e && k(t)) {
                    if (e.nodeType) {
                        if (e.parentNode && !we(e)) throw"widget cannot be loaded on existed tree";
                        return De(t, e, r, n)
                    }
                    if (e.length) {
                        var i = [];
                        return E(e, function (e) {
                            i.push(De(t, e, r, n))
                        }), i
                    }
                }
                return null
            }, val: function (e) {
                if (!C(e)) return this.scope && this.scope.hasOwnProperty("value") ? this.scope.value : $;
                this.set("value", e)
            }, get: function (e) {
                return this._assure(function () {
                    return this.scope[e]
                })
            }, set: function (t, n) {
                var r = 1 <= arguments.length ? x.call(arguments, 0) : [];
                return this.ready(function () {
                    if (k(t)) this.scope[t] = n; else {
                        var e = !0;
                        "boolean" == typeof r[0] && (e = r.shift()), N.apply(this, [e, this.scope].concat(r))
                    }
                    this.scope.$refresh()
                })
            }, setState: function () {
                var e = 1 <= arguments.length ? x.call(arguments, 0) : [], t = !0;
                return "boolean" == typeof e[0] && (t = e.shift()), this.ready(function () {
                    N.apply(this, [t, this.scope.state].concat(e)), this.scope.$refresh()
                })
            }, roleDelegate: function (e, t) {
                var a = this;
                if (!a.$root) return a;
                var s = a.$root[0], u = a.__roleDelegate || (a.__roleDelegate = {});
                return j(t) && (k(e) && (e = e.trim().split(n)), E(e, function (o) {
                    if (!u[o]) {
                        u[o] = [];
                        var e = function (n) {
                            for (var t = n.target, e = t; e && e != s;) e.getAttribute("ne-module") && (t = e.parentNode), e = e.parentNode;
                            if (e) {
                                for (var r = []; t;) {
                                    var i = (t.getAttribute("ne-role") || "").split(/\s+/);
                                    if (E(u[o], function (e) {
                                        ~i.indexOf(e.role) && r.push({target: t, fn: e.fn})
                                    }), t == s) break;
                                    t = t.parentNode
                                }
                                r.length && a.update(function () {
                                    for (var e, t = 0; t < r.length; t++) if (!1 === (e = r[t].fn.call(r[t].target, n)) || -1 === e) {
                                        n.preventDefault(), -1 === e && n.stopPropagation();
                                        break
                                    }
                                    return e
                                }), r = null
                            }
                            return !0
                        };
                        a.$root.bind(o, e), a.ready(function () {
                            s["ne-destroy"] && s["ne-destroy"].push(function () {
                                A.unbind(s, o, e)
                            })
                        })
                    }
                    E(t, function (e, t) {
                        t && u[o].push({role: t, fn: e})
                    })
                })), a
            }, updateRoles: function (e) {
                var t = this;
                if (!t.$root) return t;
                var n = t.__rolecbs || (t.__rolecbs = []);
                if (D(e)) return n.push(e), t;
                if (t.isReady) {
                    var r = t.__roles = {}, i = t.$root[0];
                    for (var o in $e(i, function (t) {
                        var e = t.getAttribute("ne-role");
                        return e && E(e.split(/\s+/), function (e) {
                            e && (r[e] || (r[e] = []), r[e].push(t))
                        }), (t == i || !k(t.getAttribute("ne-module"))) && t
                    }), t.roles) delete t.roles[o];
                    for (var a in t.__roles) t.roles[a] = w(t.__roles[a])
                }
                return E(n, function (e) {
                    e(t.roles)
                }), t
            }, compile: function (e, t) {
                var n = this, r = n.$root && n.$root[0];
                if (!t && r && (t = r["ne-destroy"]), !(e = e || r) || e.nodeType) return Se(e, n, t);
                if (e.length) {
                    var i = [];
                    return E(e, function (e) {
                        i = i.concat(Se(e, n, t))
                    }), i
                }
                return []
            }, prepared: function (e) {
                var t = this._preparedDefer ? this._preparedDefer.promise : m.never;
                return D(e) ? (t.then(e.bind(this)), this) : t
            }, replaceWith: function (e, t, n) {
                if (k(e)) {
                    var r = this.parent, i = this.$root;
                    this.destroy(n), De(e, i[0], r, t)
                }
            }, destroy: function (t) {
                var n = this, e = n.scope, r = n.$root[0];
                Le(r), C(t) && !t || (r.innerHTML = "");
                var i = e.$moduleid;
                y(Z[i], n), y(n.parent.children, n), e.$msg = null, e.hasOwnProperty("destroy") && D(e.destroy) && e.destroy(n), E(n.children, function (e) {
                    e.destroy(t)
                }), E(n, function (e, t) {
                    delete n[t]
                })
            }, ready: function (e) {
                var t = this._readyDefer ? this._readyDefer.promise : m.never;
                return D(e) ? (t.then(e.bind(this)), this) : t
            }, extend: function () {
                var e = this, t = !0, n = 1 <= arguments.length ? x.call(arguments, 0) : [];
                return "boolean" == typeof n[0] && (t = n.shift()), E(n, function (e, t) {
                    k(e) && (n[t] = ce.getExport(e) || null)
                }), e.prepared(function () {
                    N.apply(e, [t, e.scope].concat(n)), e.isReady && e.refresh()
                }), e
            }, find: function (e) {
                if (e) return w.widget(e, this);
                var t = [];
                return E(this.children, function (e) {
                    t.push(e), E(e.find(), function (e) {
                        t.push(e)
                    })
                }), t
            }, isChildOf: function (e) {
                for (var t = this.parent; t;) {
                    if (t == e) return !0;
                    t = t.parent
                }
                return !1
            }, render: function () {
                var t, n = this;
                if (n.$root) {
                    var r = n.$root[0], i = n.scope, o = [], a = [];
                    n.children = [];
                    var e = r.getAttribute("ne-extend") || "";
                    if (!/^\s*{{(.*?)}}\s*$/.test(e)) {
                        var s = e.split(f);
                        E(s, function (e) {
                            e && (e = je(e), a.push(e), o.push(ce.makeDefer(e).promise))
                        })
                    }
                    m.all(o).then(function () {
                        o.length && E(a, function (e) {
                            N(!0, i, ce.getExport(e))
                        });
                        var e = function () {
                            t || (t = !0, setTimeout(function () {
                                n.scope && (i.hasOwnProperty("init") && D(i.init) && i.init(n), n.refresh(), n.updateRoles(), n._readyDefer.resolve(n))
                            }))
                        };
                        (n._lazyPromise || m.ref()).then(function () {
                            n.wander(), n.compile(r), n.parent.ready(e), setTimeout(e, 1e3)
                        })
                    })
                }
            }, refresh: function () {
                return this.scope && this.scope.$refresh(), this
            }, wander: function (e, l, s, d) {
                var p = this;
                if (!e && p.parent && "$$0" == p.parent.guid && !C(s) && (s = !0), (e = e || p.$root) && !e.nodeType && (e = e[0]), !e) return p;
                var h = p.$root ? p.$root[0] : e;
                l = l || p.scope;
                var v = p.views, m = ["ne-recurse", "ne-repeat-start", "ne-repeat", "ne-foreach", "ne-options", "ne-role", "ne-model", "ne-html", "ne-text", "ne-state-extend", "ne-extend", "ne-fx", "ne-on"];
                S(e["ne-destroy"]) ? (e != h || p.hasOwnProperty("find")) && (E(e["ne-destroy"], function (e) {
                    e()
                }), e["ne-destroy"].splice(0)) : e == h && (e["ne-destroy"] = []);
                for (var g, u = e; u.parentNode && u != h && !u["ne-destroy"];) u = u.parentNode;
                var y = u["ne-destroy"];
                if (!y) return p;
                g = u == e ? (E(A.data(u, "_b$dlDestroy"), function (e) {
                    D(e) && e()
                }), A.data(u, "_b$dlDestroy", {})) : A.data(u, "_b$dlDestroy") || A.data(u, "_b$dlDestroy", {}), w("script[ne-macro]", e).each(function (e) {
                    var t = e.innerHTML, n = e.getAttribute("ne-macro"), a = [], r = "";
                    /(.*?)\s*\(\s*(.*?)\s*\)/.test(n) && (n = RegExp.$1, a = RegExp.$2.split(/\s*,\s*/)), E(a, function (e, t) {
                        var n = e.split(/\s*=\s*/);
                        n[1] && (a[t] = n[0], r += "if(" + n[0] + "==null)" + e + ";")
                    }), r && (t = "<%" + r + "%>" + t);
                    var s = M.parse(t);
                    qe(l, n, function () {
                        var n = Object.create(this), r = arguments.length ? x.call(arguments, 0) : [], i = this.__scopes;
                        E(a, function (e, t) {
                            e && (n[e] = C(r[t]) ? r[t] : null)
                        });
                        var o = !1, e = s(n).replace(/%(\w+)%/g, function (e, t) {
                            return ~a.indexOf(t) ? (o = !0, "__scopes[" + i.length + "]." + t) : e
                        });
                        return o && (i[i.length] = n), e
                    }), A.remove(e)
                }), Se.inited || (p.$last = !1);
                var b = 0 == arguments.length && p.$last;
                $e(e, function (i) {
                    var e, o = xe(i);
                    if ("$$0" == p.guid && k(o["ne-module"]) && i != h) return !1;
                    if (b) return b == i && (b = $), i;
                    if (p.$last = !Se.inited && i, k(o["ne-if"]) && !A.data(i, "_b$ifed")) return H["ne-if"].call(p, l, i, o["ne-if"], y);
                    for (var t = function (e, t, n) {
                        var r = e.call(p, n || l, i, t, y);
                        if (!1 === r) return !1;
                        if (D(r)) y.push(r); else if (r) {
                            if (1 != (i = r).nodeType) return i;
                            i.removeAttribute(a), o = xe(i)
                        }
                    }, n = 0, r = m.length; n < r; n++) {
                        var a = m[n], s = o[a];
                        if (k(s) && D(H[a]) && (e = t(H[a], s, "ne-model" == a ? d : ""), C(e))) return e
                    }
                    for (var u in o) if (!H[u]) {
                        var c = o[u], f = u.substr(3);
                        ne[u] || w.event(f) ? k(c) && y.push(Ce(i, f, c, p, l)) : te.test(f) ? g[f] || (g[f] = 1) : /ne-(href|for|src|title|disabled|checked|selected|read[oO]nly|required)/.test(u) ? (u = RegExp.$1, c && (/{{.+?}}/.test(c) ? y.push(v.add({
                            node: i,
                            model: c,
                            attr: u,
                            scope: l
                        }, !0)) : A.attr(i, u, c))) : "ne-" == u.substr(0, 3) && D(v[u]) ? (e = v[u](i, c, l), w.debug || i.removeAttribute(u), D(e) && y.push(e)) : D(B[u]) ? t(B[u], c) : "ne-cloak" == u ? i.removeAttribute(u) : !H[u] && "ne-module" != u && /^{{.+?}}$/.test(c) && y.push(v.add({
                            node: i,
                            model: c,
                            attr: u,
                            scope: l
                        }, !0))
                    }
                    return !k(o["ne-module"]) && o["ne-plugin"] && Te(i, o["ne-plugin"]), (!k(o["ne-module"]) || i == h) && i
                }, function (e) {
                    p.$last = !Se.inited && e;
                    var t = e.nodeValue;
                    T.test(t) && ("\\" == RegExp.$1 ? e.nodeValue = t.replace(/\\(\{\!?\{.*?\}\})/g, "$1") : y.push(v.add({node: e, model: t, scope: l}, !0)))
                }, s), E(p.__roles, function (e, t) {
                    p.roles[t] = w(e)
                }), E(g, function (e, a) {
                    if (1 === e) {
                        var t = function (n) {
                            for (var e = n.target, t = e, r = !0; t && t != u;) (t["ne-destroy"] || A.data(t, "_b$dlDestroy")) && (e = t["ne-wguid"] ? t : t.parentNode), t = t.parentNode;
                            if (t) {
                                for (var i = []; e;) {
                                    var o = e.getAttribute("ne-" + a);
                                    if (o && i.push({target: e, evt: o}), e == u) break;
                                    if (e = e.parentNode, !s && l == p.scope && e == u) break
                                }
                                i.length && p.update(function () {
                                    var e;
                                    l.$event = n;
                                    for (var t = 0; t < i.length; t++) if (n.currentTarget = l.$target = i[t].target, !1 === (e = _(i[t].evt, l, !0)) || -1 === e) return n.preventDefault(), -1 === e && n.stopPropagation(), void (r = !1)
                                }), i = null
                            }
                            return r
                        };
                        A.bind(u, a, t), y.push(g[a] = function () {
                            A.unbind(u, a, t)
                        })
                    }
                })
            }
        }, E(["on", "off", "emit", "watch", "unwatch"], function (n) {
            ke.prototype[n] = function () {
                var t = x.call(arguments);
                return this["emit" != n ? "prepared" : "ready"](function () {
                    var e = this.scope;
                    e && e["$" + n].apply(e, t)
                })
            }
        });
        var re = O.getParams = function (e, t) {
            if (j(t) || (t = {}), k(e)) for (var n, i, o, r, a; null !== (n = K.exec(e));) r = n[1], "false" == (a = n[2].trim()) ? a = !1 : "true" == a ? a = !0 : X.test(a) && (a = parseFloat(a)), i = null, o = t, r.trim().replace(V, function (e, t, n, r) {
                return i && (o[i] || (o[i] = {}), o = o[i]), i = t || r, ""
            }), o[i] = a;
            return t
        }, ie = {
            _replace: function (e, t) {
                for (var n, r = 0, i = [], o = function (e) {
                    S(e) ? i[r] = e : (i[r] || (i[r] = []), i[r].push(e))
                }, a = e.pop(), s = a.parentNode, u = (document.createElement("div"), document.createDocumentFragment()), c = [u], f = [], l = []; r < t.length; r++) if (t[r]) {
                    var d, p = S(t[r]) ? t[r].reserve : 0;
                    if (n && p - n != 1 && (f.push(d), u = document.createDocumentFragment(), c.push(u)), n = p, S(t[r])) E(t[r], function (e) {
                        p ? (f.length || f.push(e), d = e) : u.appendChild(e)
                    }), o(t[r]), l.push(t[r]); else {
                        var h = A.create(t[r], !0);
                        E(h.childNodes, o), u.appendChild(h)
                    }
                }
                return r--, E(e, function (e) {
                    -1 == l.indexOf(e) && E(e, function (e) {
                        Le(e), e.parentNode == s && s.removeChild(e)
                    })
                }), e = null, i.push(a), i.fragments = c, i.poles = f, i
            }, repeat: function (u, c, e) {
                var f, t, l, i = u.attr, d = u.key, o = D(i), p = u.isJoin, a = u.scope || c.scope, h = u.model, v = [], n = !k(u.cond) || _(u.cond, a);
                if (a.__scopes = [], h) {
                    l = n ? function (e, t) {
                        var n = e.split(".."), r = n.length;
                        if (1 == n.length) return _(e, t);
                        var i = Pe(n[0], t), o = Pe(n[r - 1], t), a = Math.abs(3 == r ? Pe(n[1], t) : 1);
                        return O.incArray(i, o, a)
                    }(h, a) : [], S(l) || (l = null != l ? [l] : []), f = l.length;
                    var s = !u.repeatScopes || u.arrLen != f, m = d ? null : u.repeatNoKeys || (u.repeatNoKeys = []);
                    if (u.arrLen = f, d && !s) {
                        for (var r = 0; r < f; r++) {
                            var g = u.repeatScopes[r];
                            if ((g ? g[d] : null) !== (C(l[r]) ? l[r] : null)) {
                                s = !0;
                                break
                            }
                        }
                        if (!o && !s) return !1
                    }
                    var y = u.repeatScopes || [];
                    if (u.repeatScopes = [], E(l, function (e, t) {
                        if (null != e) {
                            var n = Object.create(a);
                            n.__scopes = [], d ? (n[d] = e, n.$recurse = {key: d, attr: i, isJoin: p}) : N(!0, n, j(e) ? e : {__val: e}), N(n, {__len: f, __i: t});
                            var r = o ? i(n) : d ? i : M.replace(i, n);
                            y[t] && y[t].hasOwnProperty("b$html") && r == y[t].b$html || (s = !0), u.repeatScopes[t] = n, v[t] = r
                        }
                    }), !s) return u.repeatScopes = y, d || E(l, function (e, t) {
                        N(u.repeatScopes[t], e)
                    }), !1;
                    var b = -1;
                    E(l, function (e, t) {
                        if (null != e) {
                            var n, r, i = v[t];
                            if (!p) {
                                var o = d ? null : function (e, t, n, r) {
                                    if (j(e)) for (var i = 0; i < t.length; i++) if (t[i].item === e && -1 == r.indexOf(t[i].scope)) {
                                        if (t[i].string == n) return t[i];
                                        t.splice(i, 1);
                                        break
                                    }
                                    return null
                                }(e, m, i, u.repeatScopes), a = d ? function (e, t, n, r) {
                                    for (var i = 0; i < t.length; i++) if (t[i] && t[i][n] === e && -1 == r.indexOf(t[i])) return t[i];
                                    return null
                                }(e, y, d, u.repeatScopes) : o ? o.scope : null, s = d ? a && a.hasOwnProperty("b$node") ? a.b$node : null : o ? o.node : null;
                                a && ((r = (n = s) && n.length) && E(n, function (e) {
                                    e.parentNode || (r = !1)
                                }), r) && i == a.b$html && (-1 == b || b + 1 <= a.__i ? (b = a.__i, s.reserve = t + 1) : s.reserve = !1, -1 == v.indexOf(s) && (v[t] = s, u.repeatScopes[t] = a, d || N(u.repeatScopes[t], e)))
                            }
                            N(u.repeatScopes[t], {__len: f, __i: t, b$html: i})
                        }
                    }), y = null
                } else {
                    if (t = n ? i(a) : "", C(u._value) && t == u._value) return !1;
                    u._value = t, v.push(t)
                }
                if (p && (v = [v.join("")]), u.node = this._replace(u.node, v), f || !h) {
                    var w = e && e.subnode;
                    w && w.splice(0), E(u.node, function (e, r) {
                        var i, t, n, o, a, s;
                        e && e.length && (c.$refresh2 = !0, h && !p && (i = u.repeatScopes[r]) && (d && S(e) && e.length ? i.b$node = e : k(v[r]) && (t = l[r], n = m, o = e, a = i, s = v[r], j(t) && n.push({node: o, item: t, string: s, scope: a}))), E(e, function (e) {
                            if (!e["ne-destroy"]) {
                                if (3 == e.nodeType) {
                                    var t = e.nodeValue;
                                    T.test(t) && (e["ne-destroy"] = [c.views.add({node: e, model: t, scope: i})])
                                } else {
                                    if (1 != e.nodeType) return;
                                    var n = e["ne-destroy"] = [];
                                    c.wander(e, i, !0, !d && l && j(l[r]) ? l[r] : null), A.data(e, "_b$ifed") || c.compile(e, n)
                                }
                                w && w.push(e)
                            }
                        }))
                    })
                }
                var $ = u.node.fragments, x = u.node.poles;
                return E($, function (e, t) {
                    e.childNodes.length && A[0 == t ? "before" : "after"](e, x[t] || u.node[u.node.length - 1])
                }), u.node.frags = u.node.poles = null, !1
            }
        };
        Me.prototype = {
            add: function (e, t, n) {
                var r = this.widget;
                n = n || r.scope, t = t.trim();
                var i = {node: e, scope: n};
                "*" == t.substr(-1) && (i.array = !0, t = t.substr(0, t.length - 1)), i.model = t, this.items.push(i);
                var o = w.widget(e), a = r.$root[0];
                if (o) o.watch("value", r.update), a["ne-destroy"].push(function () {
                    o.unwatch("value", r.update)
                }); else {
                    var s = "change";
                    /input|textarea/.test(c(e.tagName)) && (s += " input"), A.bind(e, s, r.update), a["ne-destroy"].push(function () {
                        A.unbind(e, s, r.update)
                    })
                }
                return i
            }, remove: function (e) {
                for (var t = 0, n = this.items.length; t < n; t++) if (this.items[t] == e) {
                    this.items.splice(t, 1);
                    break
                }
            }, update: function (e, t) {
                var n = this, r = n.widget, i = r.scope, o = r;
                i && i.$refreshing ? setTimeout(function () {
                    n.update(e, t)
                }, 50) : (E(n.items, function (e) {
                    var t = Fe(e);
                    if (t) for (; o.parent && C(o.scope[t]) && !o.scope.hasOwnProperty(t);) o = o.parent
                }), E(r.children, function (e) {
                    e.update && e.update(null, !1)
                }), D(e) && (t = e.apply(r.scope)), t && D(t.then) ? t.then(function () {
                    o.refresh()
                }) : !1 !== t && o.refresh())
            }, refresh: function () {
                E(this.items, Be)
            }
        }, Ue.prototype = {
            add: function (t, e) {
                var n = this.items;
                return t && k(t.model) && (t.model = t.model.replace(/^\s*{{([^}]*?)}}\s*$/, "$1")), n.push(t), e && this.refresh(t), function () {
                    var e = n.indexOf(t);
                    -1 < e && n.splice(e, 1)
                }
            }, "ne-show": function (t, e, n) {
                return this.widget.views.add({
                    fn: function (e) {
                        A[e ? "show" : "hide"](t)
                    }, model: e, scope: n
                })
            }, "ne-hide": function (t, e, n) {
                return this.widget.views.add({
                    fn: function (e) {
                        A[e ? "hide" : "show"](t)
                    }, model: e, scope: n
                })
            }, "ne-visible": function (t, e, n) {
                return this.widget.views.add({
                    fn: function (e) {
                        t.style.visibility = e ? "visible" : "hidden"
                    }, model: e, scope: n
                }, !0)
            }, "ne-hidden": function (t, e, n) {
                return this.widget.views.add({
                    fn: function (e) {
                        t.style.visibility = e ? "hidden" : "visible"
                    }, model: e, scope: n
                }, !0)
            }, "ne-value": function (t, e, n) {
                return this.widget.views.add({
                    fn: function (e) {
                        A.val(t, e)
                    }, model: e, scope: n
                }, !0)
            }, "ne-class": function (n, e, t) {
                if (k(e) && e.trim()) {
                    var r = [];
                    if (e = e.replace(/[\w\-]*{{.+?}}[\w\-]*/g, function (e) {
                        return r.push(e), ""
                    }), p.batch(n, e), e = r.join(" ")) return this.widget.views.add({
                        fn: function (e, t) {
                            p.batch(n, e, t)
                        }, model: e, scope: t
                    })
                }
            }, "ne-style": function (n, e, t) {
                if (k(e)) {
                    if (!(e = e.trim())) return null;
                    if (/{{(.+?)}}/.test(e)) return this.widget.views.add({
                        fn: function (e, t) {
                            n.style.cssText = n.style.cssText.replace(";" + t, "") + ";" + e
                        }, model: e, scope: t
                    });
                    A.css(n, e)
                }
            }, refresh: function (e) {
                var t = this, n = t.scope;
                if (k(e)) for (var r = 0; r < t.items.length; r++) t.items[r].type == e && t.refresh(t.items[r]); else if (e) {
                    var i = e.type, o = e.node, a = t.widget;
                    if (ie[i]) ie[i](e, a, e.destroys); else {
                        var s = e.scope || n, u = ~e.model.indexOf("}}") ? M.replace(e.model, s) : _(e.model, s, e.debug), c = be(u);
                        if (!e.inited || c !== e._valueStr) {
                            if (e.inited = !0, a.$refreshed = !0, D(e.fn)) e.fn(u, e._value); else if (o && o.parentNode) {
                                var f = o.nodeType;
                                3 == f ? o.nodeValue = u : 1 == f && e.attr && (u === e.model ? o.removeAttribute(e.attr) : A.attr(o, e.attr, u))
                            }
                            e._value = u, e._valueStr = c
                        }
                    }
                } else for (r = 0; r < t.items.length; r++) t.refresh(t.items[r])
            }
        };
        var oe = [], ae = {};
        w.define = function (e, t, n) {
            k(e) || (n = t, t = e, e = null), S(t) && n || (n = t, t = []);
            var r = {fn: n, deps: t};
            return e ? k(r.fn) || ce._defers[e] ? ce.postDefine(e, r) : ae[e] = r : (oe.push(r), w.define.amd = !1), r
        }, w.define.amd = {jQuery: !0}, this.define || (this.define = w.define);
        var se = this.define.skin = function (e, t) {
            j(e) ? E(e, function (e, t) {
                se(t, e)
            }) : ue.promises[je(e)] = m.ref(t)
        }, ue = {
            promises: {}, load: function (n, e) {
                var s = document.head || document.getElementsByTagName("head")[0] || document.documentElement, t = n.replace(/\?.*/, "").replace(/^https?:\/\/.*?\//, "/"), r = this.promises[n] || this.promises[t];
                return r || (r = this.promises[n] = h.get(n).then(function (e) {
                    var t = document.createElement("div"), a = e.data, i = n.replace(/[^\/]+$/, ""), o = -1 == n.indexOf("//") ? "/" : n.replace(/(\/\/.*?\/).*/, "$1");
                    return a = a.replace(/(\s(href|src|ne-module|ne-extend|ne-plugin)=["'])@(\/)?/g, function (e, t, n, r) {
                        return t + (r ? o : i)
                    }), k(w.debug) && (a = a.replace(/(<link [^>]*?href=["'])\//, "$1" + w.debug + "/")), t.innerHTML = (l < 9 ? "<input />" : "") + a, l < 9 && t.removeChild(t.firstChild), new g(function (r) {
                        var i = 0, o = ce._loadedlink;
                        E(O.cssQuery("link, style", t), function (e) {
                            if ("link" == F(e)) {
                                i++;
                                var t = je(e.getAttribute("href")), n = function () {
                                    0 == --i && r(a)
                                };
                                o[t] ? (A.remove(e), e = o[t], setTimeout(n, 100)) : ((o[t] = e).onload = e.onreadystatechange = function () {
                                    e.readyState && "complete" != e.readyState || (e.onload = e.onreadystatechange = null, setTimeout(n, 50))
                                }, setTimeout(function () {
                                    e.onload && e.onload()
                                }, 2e3))
                            }
                            s.appendChild(e)
                        }), a = t.innerHTML, t = null, 0 === i && r(a)
                    })
                })), r
            }
        }, ce = {
            _fns: {}, _exports: {}, _loadedlink: {}, _defers: {}, _promises: {}, makeDefer: function (e, t, n) {
                var r = ce._defers, i = e.split("@");
                e = i[0];
                var o = i[1] || "utf-8";
                if (r[e]) return r[e];
                var a = r[e] = m.defer(), s = a.promise;
                if (n || (s.deploy = function (t, n) {
                    var r = 3 <= arguments.length ? x.call(arguments, 2) : [];
                    return s.then(function (e) {
                        D(e.deploy) && (t = e.deploy(t, n, r))
                    }), t
                }), !t) {
                    var u = ce._exports;
                    u[e] ? a.resolve(u[e]) : ae[e] ? (ce.postDefine(e, ae[e]), delete ae[e]) : "%" != e.substr(0, 1) && h.require(e, {charset: o}).success(function () {
                        ce.postDefine(e, null, n)
                    }).error(function () {
                        throw ce.postDefine(e, {
                            fn: function () {
                            }
                        }, n), e + " load error." + (Y[e] ? "\n  => " + Y[e] : "")
                    })
                }
                return a
            }, get: function (e) {
                return e ? (e = je(e), ce.makeDefer(e).promise) : m.ref()
            }, getExport: function (e) {
                var t, n = ce._exports, r = ce._fns;
                e = e.replace(/(\w)\@.*/, "$1"), /^(plugin)\!(.*)/.test(e) && (t = RegExp.$1, e = RegExp.$2), e = je(e), ae[e] && (ce.postDefine(e, ae[e]), delete ae[e]);
                var i = r[e];
                if ("plugin" == t) return i;
                if (!n[e] && i && D(i.fn)) {
                    var o = _e();
                    d(i.depInject.exportIdx) && (i.depInject[i.depInject.exportIdx] = o);
                    var a = i.fn.apply(o, i.depInject);
                    n[e] = C(a) ? a : o
                }
                return n[e]
            }, createLink: function (e) {
                var t = document.head || document.getElementsByTagName("head")[0] || document.documentElement, n = ce._loadedlink[e];
                n || ((n = ce._loadedlink[e] = document.createElement("link")).href = e, n.rel = "stylesheet"), t.appendChild(n)
            }, depPromise: function (n, t) {
                var r = this, e = r._promises, i = m.ref();
                if (/^text\!/.test(n)) {
                    var o = (n = je(n, t)).replace(/.*\!/, ""), a = r._exports;
                    return a[n] ? m.ref(a[n]) : ((i = e[n]) || (i = e[n] = new g(function (t) {
                        h.get(o).success(function (e) {
                            a[n] = e, t(e)
                        })
                    })), i)
                }
                var s = "!" == n.substr(0, 1);
                return E(n.split(f), function (e) {
                    i = i.then(function () {
                        return r.makeDefer(je(e.replace(/.*\!/, ""), t), !1, s).promise
                    })
                }), i
            }, postDefine: function (t, r, e) {
                w.define.amd = {jQuery: !0};
                var n = ce._exports, i = s.module, o = ce.makeDefer(t, !0);
                if (!o.def || r || oe.length) {
                    var a = [];
                    if (!e) {
                        if (r || (i && i.exports ? (r = {fn: i.exports}, delete i.exports) : r = oe.shift()), !r) throw"define not found for " + t;
                        if (k(r.fn)) return void (n["text!" + t] = r.fn);
                        if (!D(r.fn)) return n[t] = r.fn || {}, void o.resolve(r)
                    }
                    (r && r.deps ? ge : m.ref()).then(function () {
                        e || (r.name = t, S(r.deps) && Ee(r.deps, t), a.push(Qe(r.deps).then(function (e) {
                            r.depInject = e, r._deps = r.deps, delete r.deps, ce._fns[t] = r
                        })), r.deploy = function (e, t, n) {
                            return (e._lazyPromise || m.ref()).then(function () {
                                ce.instantiate(r, e, n)
                            }), e
                        }), m.all(a).then(function () {
                            o.def = r, o.resolve(r, !0)
                        })
                    })
                }
            }, instantiate: function (e, r, t) {
                if (r._preparedDefer) {
                    var i = r.parent.scope, n = r.scope, o = r.models = new Me(r);
                    r.update = n.$update = o.update.bind(o), r.views = new Ue(r), n.$widget = r, n.$root = r.$root;
                    var a = r.$root[0], s = function () {
                        Ie(n, a.getAttribute("ne-state")), r._preparedDefer.resolve(r)
                    };
                    r.prepared(function () {
                        var n = r.parent.$root && r.parent.$root[0]["ne-destroy"];
                        E(["ne-state-extend", "ne-extend"], function (e) {
                            var t = a.getAttribute(e);
                            t && H[e].call(r.parent, i, a, t, n)
                        })
                    }), e ? (n.$moduleid = e.name, d(e.depInject.exportIdx) && (e.depInject[e.depInject.exportIdx] = n), e.fn.apply(n, e.depInject), s(), t && (S(t) || (t = [t]), E(t, function (e) {
                        N(!0, n, e)
                    })), Re(e.name, r)) : s()
                }
            }
        }, fe = {}, le = {
            linear: function (e) {
                return e
            }
        }, de = {alias: fe, easeFns: le};
        le["ease-out"] = le.ease = function (e) {
            return Math.sqrt(e)
        }, le["ease-in"] = function (e) {
            return e * e
        }, w.conf = function (e) {
            if (k(e)) return de[e];
            N(!0, de, e)
        }, w.run = function (e) {
            return S(e) && Ee(e), k(e) ? ce.get(e).then(function (e) {
                if (e && e.fn) {
                    if (D(e.fn)) {
                        var t = _e();
                        d(e.depInject.exportIdx) && (e.depInject[e.depInject.exportIdx] = t);
                        var n = e.fn.apply(t, e.depInject);
                        return n && "object" == typeof n ? n : t
                    }
                    return e.fn
                }
                return e
            }) : Qe.apply(w, arguments)
        }, w.defined = ce.getExport, w.widget = function (e, t) {
            if (k(e)) "#" == e.substr(0, 1) ? Z[e] || (Z[e] = new ke) : e = je(e); else if (e) {
                if (e.getAttribute || (e = e[0]), !e || !e.getAttribute) return null;
                var n = e;
                if (!(e = n["ne-wguid"]) && k(n.getAttribute("ne-module"))) {
                    var r = n.getAttribute("ne-id"), i = A._nodeId(n), o = Z[i] = r && Z["#" + r] || Z[i] || new ke;
                    return r && (Z["#" + r] = o), o
                }
            }
            var a = Z[e] || null, s = [];
            return S(a) ? (E(a, function (e) {
                t && !e.isChildOf(t) || s.push(e)
            }), s) : t && a && !a.isChildOf(t) ? null : a
        }, w.rootScope = new Ne;
        var pe = w.rootWidget = ke.create();
        pe._preparedDefer.resolve(pe), pe._readyDefer.resolve(pe), pe.defer = m.defer();
        var he = document.body && document.body.getAttribute("ne-alias");
        if (!he) {
            var ve = document.getElementsByTagName("script"), me = ve[ve.length - 1];
            s.addEventListener || !me || me.getAttribute("ne-alias") || (me = ve[ve.length - 2]), he = me && me.getAttribute("ne-alias")
        }
        var ge = w.run(he).then(function (e) {
            e && N(!0, fe, e)
        });
        he && (pe.compile = function () {
            ge.then(Se)
        }), (window.NTES || w).ready(function () {
            ge.then(function () {
                Se(), Se.inited = !0, pe.defer.resolve()
            })
        }), define("require", function () {
            return w.defined
        })
    }

    function ye(e) {
        this.$el = w(e), this.duration = 300, this.progress = 0, this.prefix = "ne-anim"
    }

    function be(e) {
        return "object" == typeof e && e && !e.alert ? JSON.stringify(e) : e
    }

    function we(e) {
        return 1 == e.nodeType && "" === e.innerHTML.replace(/<!--[\s\S]*?-->/g, "").replace(/^\s+/, "")
    }

    function $e(e, t, n, r) {
        if (1 !== e.nodeType) return e;
        if (r && !(e = t(e))) return !1;
        for (var i, o = e.firstChild; o;) {
            var a = o.nodeType, s = o.nextSibling;
            1 == a && t ? (i = t(o)) && ("script" != F(i) && $e(i, t, n), s = i.nextSibling) : 3 == a && n && n(o), o = s
        }
        return e
    }

    function xe(e) {
        var t, n, r, i = {}, o = e.outerHTML;
        if (o) for (var a = 0, s = /((\S+?)=(['"])(.*?)\3|[^>\s]+)\s*(\/?\s*>)?/g; t = s.exec(o);) {
            if (n = t[2], r = t[4], !n) {
                var u = t[1].split("=");
                n = u[0], r = u[1] || ""
            }
            if (a++ && (i[n] = r.replace(/&amp;/g, "&").replace(/&gt;/g, ">").replace(/&lt;/g, "<")), t[5]) break
        } else if (e.attributes) for (var c = 0, f = e.attributes.length; c < f; c++) {
            var l = e.attributes[c];
            i[l.name] = l.value
        }
        return i
    }

    function _e() {
        var e = Object.create(q);
        return e.$msg = {}, e
    }

    function Te(n, e, r) {
        var i = [];
        if (e = e || n.getAttribute("ne-plugin"), j(e)) {
            var t = _e(), o = e;
            d(o.depInject.exportIdx) && (o.depInject[o.depInject.exportIdx] = t), o.fn.apply(t, o.depInject), N(!0, Ie(t, n.getAttribute("ne-plugin-state")), r), t.init && t.init(w(n)), i.push(m.ref(t))
        } else k(e) && E(e.trim().split(f), function (e) {
            if (e = je(e), /\.css$/i.test(e)) ce.createLink(e); else {
                var t = ce.makeDefer(e).promise;
                i.push(t.then(function (e) {
                    var t = _e();
                    return d(e.depInject.exportIdx) && (e.depInject[e.depInject.exportIdx] = t), e.fn.apply(t, e.depInject), N(!0, Ie(t, n.getAttribute("ne-plugin-state")), r), D(t.init) && t.init(w(n)), t
                }))
            }
        });
        return A.data(n, "_b$pluginPromise", 1 == i.length ? i[0] : m.all(i))
    }

    function Oe(e, t, n) {
        return w.isRegExp(t) ? t.test(e) : n ? e === t : e.indexOf && -1 < e.indexOf(t)
    }

    function Ae(e, t) {
        var n = this.$parse(t), r = e.cache, i = be(n);
        e.inited && i === r || (e.cache = i, e._value = n, e.inited = !0, e.fn && e.fn(n, r))
    }

    function Ne() {
    }

    function Ee(r, i) {
        E(r, function (e, t) {
            var n = [];
            E(e.split(f), function (e) {
                e = -1 == e.indexOf("//") ? je(e, i) : e, n.push(e), Y[e] = i
            }), r[t] = n.join(";")
        })
    }

    function je(e, t) {
        var n = "";
        if (t && 0 === t.indexOf("%") && (t = ""), /^([a-z]*\!)(\S+)/.test(e) && (n = RegExp.$1, e = RegExp.$2), "%" == e.substr(0, 1)) return n + e;
        if (fe[e]) e = fe[e]; else for (var r in fe) if (/\/$/.test(r) && 0 === e.indexOf(r)) {
            e = e.replace(r, fe[r]);
            break
        }
        if ("exports" == e || ce._defers[e] || ae[e]) return n + e;
        if (!/^(\/\/|http)/.test(e)) if (0 !== e.indexOf("/")) {
            var i = t || J.pathname;
            for (/\/[^\/]+$/.test((t || J.href || "").replace(/[#\?].*/, "")) && (i = i.replace(G, "")); W.test(e);) e = RegExp.$1, i = i.replace(G, "");
            e = i + e
        } else if (k(t) && /(.*\/\/\S+?)\//.test(t)) {
            e = RegExp.$1 + e
        }
        return k(w.debug) && !/cache.netease|163.com|126.net/.test(e) && (e = e.replace(/https?:\/\/.*?\//, "/").replace(/^\//, w.debug + "/")), /\/\w+$/.test(e) && (e += ".js"), n + function (e) {
            var t = e.replace(/\/\.\//g, "/");
            for (; (e = t.replace(z, "$1")) && e != t;) t = e;
            return t
        }(e)
    }

    function ke() {
        this._readyDefer = m.defer(), this._preparedDefer = m.defer(), this._readyDefer.promise.then(function (e) {
            e.isReady = !0
        })
    }

    function Se(e, n, r) {
        if (!w.debug && /\bdebug=(\S+?)($|&|#)/.test(location.href)) {
            var t = RegExp.$1;
            if ("noCompile" == t) return;
            "strict" == t && (u = !0), w.debug = "http" != t.substr(0, 4) || t
        }
        var i = [];
        n || (n = pe), e ? !e["ne-wguid"] && k(e.getAttribute("ne-module")) ? i.push(e) : $e(e, function (e) {
            var t = e.getAttribute("ne-benchmark");
            return k(t) && (e.removeAttribute("ne-benchmark"), (w.widget(e) || w).ready(function () {
                w.bench.mark(t)
            })), k(e.getAttribute("ne-module")) ? (i.push(e), !1) : e
        }) : i.push(document.documentElement);
        var o = [];
        return E(i, function (e) {
            var t;
            e["ne-wguid"] ? (t = w.widget(e), Se.inited || (t.compile(), t.$last && t.$last.nextSibling && t.wander())) : t = De(e.getAttribute("ne-module") || "", e, n), o.push(t), r && r.push(function () {
                t.destroy()
            })
        }), o
    }

    function De(e, t, n, r) {
        if (!t || !t.nodeType) return null;
        t == document.documentElement || k(t.getAttribute("ne-module")) || t.setAttribute("ne-module", ""), e = e.replace(/^\s+|\s+$/g, "");
        var i = !1;
        if (/(.*)\*$/.test(e) && (e = RegExp.$1, i = !0), e) {
            if ("@" == e.substr(0, 1) && n && n.scope) {
                var o = n.scope.$moduleid;
                e = o && "%" != o.substr(0, 1) ? o.replace(/[^\/]*?$/, "") + e.substr(1) : e.substr(1)
            }
            e = je(e)
        }
        var a = ke.create(t, i ? pe : n, e);
        return a.isolate = i, a.scope = a.isolate ? new Ne : Object.create(n.scope), a.scope.$msg = {}, w.isObject(r) && a.extend(r), Te(t).then(function () {
            e ? /\.html?$/.test(e) ? (ce.instantiate(null, a), Re(e, a)) : ce.get(e).deploy(a, n) : (ce.instantiate(null, a), a.render())
        }), a
    }

    function Ce(e, t, n, r, i) {
        var o = function (t) {
            (i.$event = t).currentTarget = i.$target = e, n && r.update(function () {
                var e = _(n, i, !0);
                !1 !== e && -1 !== e || (t.preventDefault(), -1 === e && t.stopPropagation())
            })
        };
        if ("change" == t) {
            var a = w.widget(e);
            if (a) return a.watch("value", o), function () {
                a.unwatch("value", o)
            }
        }
        return e.getAttribute("ne-model") && A.unbind(e, t, r.update), A.bind(e, t, o), function () {
            A.unbind(e, t, o)
        }
    }

    function Re(e, t) {
        var n = t.$root[0], r = t.scope, i = k(n.getAttribute("ne-transclude"));
        if ((i || we(n)) && (t._empty = !0, i && (n.transclude = n.innerHTML)), t._empty) {
            var o = re(n.getAttribute("ne-props")), a = function (e) {
                if (n.innerHTML = (l < 9 ? '<input style="display:none"/>' : "") + M.replace(e, {props: o, transclude: n.transclude || ""}, null, null, /props\.|transclude/), l < 9 && n.removeChild(n.firstChild), /<script/i.test(e)) {
                    var i = m.ref();
                    w("script", n).each(function (e) {
                        var t = c(e.getAttribute("type"));
                        if (!t || "text/javascript" == t) {
                            var n = e.getAttribute("src");
                            if (n) i = i.then(function () {
                                return h.require(n)
                            }); else {
                                var r = document.createElement("script");
                                r.innerHTML = e.innerHTML, i = i.then(function () {
                                    return A.replace(r, e)
                                })
                            }
                        }
                    })
                }
                t.render()
            };
            if (r.hasOwnProperty("html")) a(r.html); else {
                var s = o.skin;
                e = /\//.test(s) ? je(s) : e.replace(/(\.[^\.]*)?$/, (s ? "." + s : "") + ".html") + (l ? "?" + +new Date : ""), delete o.skin, ue.load(O.buildUrl(e, o), n).then(a)
            }
        } else t.render()
    }

    function Ie(e, t) {
        return e.hasOwnProperty("state") || (e.state = {}), re(t, e.state), e
    }

    function Le(e) {
        try {
            var t = A.data("_b$selfcide");
            D(t) && t();
            var n = e["ne-destroy"];
            n && (delete e["ne-destroy"], E(n, function (e) {
                e()
            }), E(n.subnode, function (e) {
                Le(e)
            })), A.data(e, !1)
        } catch (e) {
        }
    }

    function Pe(e, t) {
        var n = parseInt(e, 10);
        return isNaN(n) ? parseInt(_(e, t), 10) : n
    }

    function Me(e) {
        this.widget = e, this.items = [], this.cursor = 0
    }

    function Fe(e, t) {
        var n = e.node, r = A.val(n);
        if (C(t) || (t = He(e.model, e.scope)), "input" != c(n.tagName) || "radio" != c(n.getAttribute("type")) || !1 !== n.checked) return C(r) && (e.inited = !0, r !== t) ? qe(e.scope, e.model, r, e.array && A.attr(n, "value")) : void 0
    }

    function qe(e, t, n, r) {
        var i, o = null, a = e;
        if (t.replace(V, function (e, t, n, r) {
            return o && (C(a[o]) || (a[o] = {}), a = a[o]), o = t || r, i || (i = o), ""
        }), k(r)) {
            S(a[o]) || (a[o] = []);
            var s = a[o].indexOf(r);
            n && -1 == s ? a[o].push(r) : n || -1 == s || a[o].splice(s, 1)
        } else a[o] = n;
        return i
    }

    function He(e, t) {
        var i = null, o = t;
        return e.replace(V, function (e, t, n, r) {
            return i && (o[i] || (o[i] = {}), o = o[i]), i = t || r, ""
        }), o[i]
    }

    function Be(e) {
        var t = e.node, n = He(e.model, e.scope), r = "input" == c(t.tagName) && "radio" == c(t.getAttribute("type")), i = r ? t.checked : A.val(t);
        e.array && (n = S(n) && -1 != n.indexOf(A.attr(t, "value"))), n !== i && (!C(n) && e.inited && (n = ""), C(n) && (r ? A.attr(t, "checked", n === t.value) : A.val(t, n)), Fe(e, n))
    }

    function Ue(e) {
        this.widget = e, this.scope = e.scope, this.items = []
    }

    function Qe(e, t) {
        var n = [], r = ce._fns, i = [];
        e = D(e) ? (t = e, []) : e || [];
        for (var o = 0; o < e.length; o++) /\.css$/i.test(e[o]) ? (ce.createLink(e[o]), e.splice(o--, 1)) : e[o] ? "!" == e[o].substr(0, 1) && (n.push(ce.depPromise(e[o])), e.splice(o--, 1)) : e.splice(o--, 1);
        return E(e, function (e, t) {
            "exports" == e ? i.exportIdx = t : r[e] || n.push(ce.depPromise(e))
        }), m.all(n).then(function () {
            return e && E(e, function (e) {
                i.push("exports" == e ? null : ce.getExport(e))
            }), D(t) && t.apply(this, i), i
        })
    }
}.call(this, this.bowlder, function (e) {
    return new Function("obj", "with(obj)return " + e)
});