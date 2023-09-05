


/**
 * 触发 window.resize
 */
export function triggerWindowResizeEvent() {
    let event = document.createEvent('HTMLEvents')
    event.initEvent('resize', true, true)
    event.eventType = 'message'
    window.dispatchEvent(event)
}

/**
 * 过滤对象中为空的属性
 * @param obj
 * @returns {*}
 */
export function filterObj(obj) {
    if (!(typeof obj == 'object')) {
        return;
    }

    for (var key in obj) {
        if (obj.hasOwnProperty(key)
            && (obj[key] == null || obj[key] == undefined || obj[key] === '')) {
            delete obj[key];
        }
    }
    return obj;
}

/**
 * 时间格式化
 * @param value
 * @param fmt
 * @returns {*}
 */
export function formatDate(value, fmt) {
    var regPos = /^\d+(\.\d+)?$/;
    if (regPos.test(value)) {
        //如果是数字
        let getDate = new Date(value);
        let o = {
            'M+': getDate.getMonth() + 1,
            'd+': getDate.getDate(),
            'h+': getDate.getHours(),
            'm+': getDate.getMinutes(),
            's+': getDate.getSeconds(),
            'q+': Math.floor((getDate.getMonth() + 3) / 3),
            'S': getDate.getMilliseconds()
        };
        if (/(y+)/.test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (getDate.getFullYear() + '').substr(4 - RegExp.$1.length))
        }
        for (let k in o) {
            if (new RegExp('(' + k + ')').test(fmt)) {
                fmt = fmt.replace(RegExp.$1, (RegExp.$1.length === 1) ? (o[k]) : (('00' + o[k]).substr(('' + o[k]).length)))
            }
        }
        return fmt;
    } else {
        //TODO
        value = value.trim();
        return value.substr(0, fmt.length);
    }
}


/**
 * 深度克隆对象、数组
 * @param obj 被克隆的对象
 * @return 克隆后的对象
 */
export function cloneObject(obj) {
    return JSON.parse(JSON.stringify(obj))
}

/**
 * 随机生成数字
 *
 * 示例：生成长度为 12 的随机数：randomNumber(12)
 * 示例：生成 3~23 之间的随机数：randomNumber(3, 23)
 *
 * @param1 最小值 | 长度
 * @param2 最大值
 * @return int 生成后的数字
 */
export function randomNumber() {
    // 生成 最小值 到 最大值 区间的随机数
    const random = (min, max) => {
        return Math.floor(Math.random() * (max - min + 1) + min)
    }
    if (arguments.length === 1) {
        let [length] = arguments
        // 生成指定长度的随机数字，首位一定不是 0
        let nums = [...Array(length).keys()].map((i) => (i > 0 ? random(0, 9) : random(1, 9)))
        return parseInt(nums.join(''))
    } else if (arguments.length >= 2) {
        let [min, max] = arguments
        return random(min, max)
    } else {
        return Number.NaN
    }
}

/**
 * 随机生成字符串
 * @param length 字符串的长度
 * @param chats 可选字符串区间（只会生成传入的字符串中的字符）
 * @return string 生成的字符串
 */
export function randomString(length, chats) {
    if (!length) length = 1
    if (!chats) chats = '0123456789qwertyuioplkjhgfdsazxcvbnm'
    let str = ''
    for (let i = 0; i < length; i++) {
        let num = randomNumber(0, chats.length - 1)
        str += chats[num]
    }
    return str
}

/**
 * 随机生成uuid
 * @return string 生成的uuid
 */
export function randomUUID(leg = 32) {
    let chats = '0123456789abcdef'
    return randomString(leg, chats)
}

/**
 * 下划线转驼峰
 * @param string
 * @returns {*}
 */
export function underLine2CamelCase(string) {
    return string.replace(/_([a-z])/g, function (all, letter) {
        return letter.toUpperCase();
    });
}

/**
 * 判断是否显示办理按钮
 * @param bpmStatus
 * @returns {*}
 */
export function showDealBtn(bpmStatus) {
    if (bpmStatus != "1" && bpmStatus != "3" && bpmStatus != "4") {
        return true;
    }
    return false;
}

/**
 * 增强CSS，可以在页面上输出全局css
 * @param css 要增强的css
 * @param id style标签的id，可以用来清除旧样式
 */
export function cssExpand(css, id) {
    let style = document.createElement('style')
    style.type = "text/css"
    style.innerHTML = `@charset "UTF-8"; ${css}`
    // 清除旧样式
    if (id) {
        let $style = document.getElementById(id)
        if ($style != null) $style.outerHTML = ''
        style.id = id
    }
    // 应用新样式
    document.head.appendChild(style)
}


/**
 * 根据组件名获取父级
 * @author ShowMaker
 * @param vm
 * @param name
 * @returns {Vue | null|null|Vue}
 */
export function getVmParentByName(vm, name) {
    let parent = vm.$parent
    if (parent && parent.$options) {
        if (parent.$options.name === name) {
            return parent
        } else {
            let res = getVmParentByName(parent, name)
            if (res) {
                return res
            }
        }
    }
    return null
}

//序列化 样例{a:1:b:2} return a=1&b=2
export function serialize(obj) {
    var query = "",
        name,
        value,
        subName,
        innerObj,
        i,
        p;

    for (name in obj) {
        value = obj[name];

        if (value instanceof Array) {
            for (i = 0; i < value.length; ++i) {
                if (value[i] instanceof Object || value[i] instanceof Array) {
                    innerObj = {};
                    innerObj[name + "[" + i + "]"] = value[i];
                    (p = serialize(innerObj)) && (query += p + "&");
                } else if (value[i] !== undefined && value[i] !== null) {
                    query +=
                        encodeURIComponent(name) +
                        "=" +
                        encodeURIComponent(value[i]) +
                        "&";
                }
            }
        } else if (value instanceof Object) {
            for (subName in value) {
                innerObj = {};
                innerObj[name + "." + subName] = value[subName];
                (p = serialize(innerObj)) && (query += p + "&");
            }
        } else if (value !== undefined && value !== null) {
            query +=
                encodeURIComponent(name) + "=" + encodeURIComponent(value) + "&";
        }
    }

    return query.length ? query.substr(0, query.length - 1) : query;
}