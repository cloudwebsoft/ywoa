import { defHttp } from '/@/utils/http/axios';
import setting from '/@/settings/projectSetting';
import { useMessage } from '/@/hooks/web/useMessage';
const { createConfirm, createMessage } = useMessage();
import { ContentTypeEnum } from '/@/enums/httpEnum';
import { h } from 'vue';
import { getShowImg, getShowImgInJar } from '/@/api/system/system';
import { bufToUrl } from '/@/utils/file/base64Conver';
import { useUserStore } from '/@/store/modules/user';

export function getUrlParam(paraName: string, url: any = document.location.href) {
  const urls = url;
  const arrObj = urls.split('?');

  if (arrObj.length > 1) {
    const arrPara = arrObj[1].split('&');
    let arr;

    for (let i = 0; i < arrPara.length; i++) {
      arr = arrPara[i].split('=');

      if (arr != null && arr[0] == paraName) {
        return arr[1];
      }
    }
    return '';
  } else {
    return '';
  }
}

// 清除所有的timeout，包括interval
export function clearTimeoutAll() {
  const highestTimeoutId = setTimeout(';');
  for (let i = 0; i < highestTimeoutId; i++) {
    clearTimeout(i);
  }
}

export function createCss(css, srcId = '-srcCss') {
  const element = document.createElement('style');
  element.innerHTML = css;
  element.setAttribute('id', `${600}${srcId}`);
  document.head.appendChild(element);
}

// -----------------------------------------------------处理头部文件开始-------------------------------------------------------------
// AJAX加载的javascript无效，需作处理
/**
 * 从html中过滤出JS，并运行
 * @param html
 * @param srcId
 * @param formObj 使JS中的ReplaceCtlWithValue之类的方法作用于formObj指定的form中，如在嵌套表格中打开processShowDrawer.vue时
 * 以免与页面中已有的florForm冲突
 * @returns
 */
export const filterJS = async (html, srcId = '-src', formObj: any = null, callback) => {
  // let oldFormObj = null;
  if (formObj) {
    // oldFormObj = getCurrentFormObj();
    // console.log('oldFormObj name', oldFormObj);
    setCurrentFormObj(formObj);
  }
  let formId = '';
  if (formObj) {
    formId = formObj.getAttribute('name')
      ? formObj.getAttribute('name')
      : formObj.getAttribute('id');
  }
  const obj = document.createElement('div');
  obj.innerHTML = html;
  const _parseScripts = function () {
    const s = obj.getElementsByTagName('script');
    // For browsers which discard scripts when inserting innerHTML, extract the scripts using a RegExp
    if (s.length == 0) {
      // 有的时候在html中的script识别不出来，需用正则表达式解析，如: BasicTreeSelectCtl在convertToHTMLCtlForQuery生成查询字符串时就识别不出来
      const re =
        /(?:<script.*(?:src=[\"\'](.*)[\"\']).*>.*<\/script>)|(?:<script.*>([\S\s]*?)<\/script>)/gi; // assumes HTML well formed and then loop through it.
      let match;
      while ((match = re.exec(html))) {
        if (typeof callback === 'function') {
          // console.log('filterJS visible1 ---- ' + callback());
          if (!callback()) {
            console.log('filterJS visible1 ' + callback());
            break;
          }
        }
        const s0 = document.createElement('script');
        console.log('s001', s0);
        if (match[1]) {
          s0.src = match[1];
          s0.id = `${300}${srcId}`;
        } else if (match[2]) {
          s0.text = match[2];
          s0.id = `${300}${srcId}`;
        }
        document.body.appendChild(s0);
      }
    } else {
      for (let i = 0; i < s.length; i++) {
        if (typeof callback === 'function') {
          // console.log('filterJS visible2 ---- ' + callback());
          if (!callback()) {
            console.log('filterJS visible2 ' + callback());
            break;
          }
        }
        // console.log('_parseScripts s[' + i + '].text', s[i].text);
        const s0 = document.createElement('script');
        if (s[i].text) {
          s0.text = s[i].text;
          s0.id = `${500}${srcId}`;
        } else {
          s0.src = s[i].src;
          s0.id = `${500}${srcId}`;
        }
        // console.log('s0', s0);
        document.body.appendChild(s0);
      }
    }
    // 恢复原来的formObj
    /* if (oldFormObj != null) {
      setCurrentFormObj(oldFormObj);
    } */
    // 注意必须要销毁currentFormObj，因为表单form在抽屉关闭时，仅管销毁了，但还在内存中，如果不置为null，则通过getCurForm时取到的是驻留内存的form，会导致赋值操作看起来无效，因为操作的是内存中的form中的元素。
    destroyCurrentFormObj();
  };
  return new Promise((resolve) => {
    setTimeout(() => {
      console.log('filterJS formId', formId);
      if (formId != '') {
        const obj = o(formId);
        if (obj) {
          const isclosed = obj.getAttribute('isclosed');
          if (isclosed == null) {
            _parseScripts();
          } else {
            console.log('form ' + formId + 'is closed');
          }
        } else {
          console.warn('filterJS formObj ' + formId + ' is not exist');
        }
      } else {
        console.warn('filterJS param formObj is not set');

        _parseScripts();
      }

      resolve(true);
    }, 50);

    // setTimeout(() => {
    //   _parseScripts();
    //   resolve(true);
    // }, 50);
  });
};

export function removeScript(srcId = '-src') {
  /**
   * 删除 script 文件
   * @param
   */
  const scripts = document.getElementsByTagName('script');
  if (scripts && scripts.length > 0) {
    // 会出现删不干净的问题
    // for (let i = 0; i < scripts.length; i++) {
    //   if (scripts[i] && scripts[i].id && scripts[i].id.indexOf(srcId) != -1) {
    //     console.log('removeScript id', scripts[i].id + ' length=' + scripts.length);
    //     console.log('removeScript', scripts[i]);
    //     scripts[i].parentNode.removeChild(scripts[i]);
    //   }
    // }

    for (let i = scripts.length - 1; i >= 0; i--) {
      if (scripts[i] && scripts[i].id && scripts[i].id.indexOf(srcId) != -1) {
        // console.log('removeScript id', scripts[i].id + ' length=' + scripts.length);
        // console.log('removeScript', scripts[i]);
        scripts[i].parentNode.removeChild(scripts[i]);
      }
    }
  }
}
export function removeLink(srcId = '-srcCss') {
  /**
   * 删除 link 文件
   * @param
   */
  const links = document.getElementsByTagName('link');
  if (links && links.length > 0) {
    // for (let i = 0; i < links.length; i++) {
    //   if (links[i] && links[i].id) {
    //     // 删除在process***.vue文件中引入的assets/css/css.css
    //     if (links[i].href.indexOf('/assets/css/css') != -1) {
    //       console.log('removeLink', links[i]);
    //       links[i].parentNode.removeChild(links[i]);
    //     }
    //   }
    // }

    for (let i = links.length - 1; i >= 0; i--) {
      if (links[i] && links[i].id) {
        // 删除在process***.vue文件中引入的assets/css/css.css
        if (links[i].href.indexOf('/assets/css/css') != -1) {
          console.log('removeLink', links[i]);
          links[i].parentNode.removeChild(links[i]);
        }
      }
    }
  }

  // 删除pageCss
  const styles = document.getElementsByTagName('style');
  if (styles && styles.length > 0) {
    // for (let i = 0; i < styles.length; i++) {
    //   if (styles[i] && styles[i].id) {
    //     if (styles[i].id.indexOf(srcId)) {
    //       console.log('removeLink style', styles[i]);
    //       styles[i].parentNode.removeChild(styles[i]);
    //     }
    //   }
    // }

    for (let i = styles.length - 1; i >= 0; i--) {
      if (styles[i] && styles[i].id) {
        if (styles[i].id.indexOf(srcId)) {
          console.log('removeLink style', styles[i]);
          styles[i].parentNode.removeChild(styles[i]);
        }
      }
    }
  }
}
// -----------------------------------------------------处理头部文件结束-------------------------------------------------------------

export const ajaxGet = (myUrl: any, params?: any, others = {}) =>
  defHttp.get<any>(
    { url: `${setting.interfacePrefix}` + myUrl, params, ...others },
    {
      isTransformResponse: false,
    },
  );

export const ajaxGetWithNoErrMsg = (myUrl: any, params?: any, others = {}) =>
  defHttp.get<any>(
    { url: `${setting.interfacePrefix}` + myUrl, params, ...others },
    {
      isTransformResponse: false,
      errorMessageMode: 'none',
    },
  );

export const ajaxPost = async (myUrl: any, params?: any, others = {}) =>
  await defHttp.post<any>(
    { url: `${setting.interfacePrefix}` + myUrl, params, ...others },
    {
      isTransformResponse: false,
    },
  );

export const ajaxGetJS = async (myUrl: any, params?: any) => {
  // console.log('ajaxGetJS start:' + myUrl);
  try {
    let data = await ajaxGetWithNoErrMsg(myUrl, params);
    if (data.indexOf('<script') === -1) {
      data = '<script>' + data + '</script>';
    }
    await filterJS(data);
  } catch (e) {
    console.warn(e);
  }
  // console.log('ajaxGetJS end');
};

export const ajaxPostJson = (myUrl: any, params?: any) =>
  defHttp.post<any>({
    url: `${setting.interfacePrefix}` + myUrl,
    params,
    headers: { 'Content-Type': ContentTypeEnum.JSON },
  });

export const ajaxGetJson = (myUrl: any, params?: any) =>
  defHttp.get<any>({
    url: `${setting.interfacePrefix}` + myUrl,
    params,
    headers: { 'Content-Type': ContentTypeEnum.JSON },
  });

export const myConfirm = (title: string, msg: string, callBack: any, params: object) => {
  createConfirm({
    iconType: 'info',
    title: () => h('span', title),
    content: () => h('span', msg),
    maskClosable: false,
    onOk: async () => {
      callBack(params);
    },
  });
};

export const myMsg = (msg: string, type: string) => {
  switch (type) {
    case 'warning':
      createMessage.warning(msg);
      break;
    case 'warn':
      createMessage.warn(msg);
      break;
    case 'error':
      createMessage.error(msg);
      break;
    default:
      createMessage.success(msg);
  }
};

export const colorToRGB = (color, opacity) => {
  let color1, color2, color3;
  color = '' + color;
  if (typeof color !== 'string') return '';

  if (color.toLowerCase().startsWith('rgba')) {
    return color;
  } else if (color.toLowerCase().startsWith('rgb(')) {
    const p = color.indexOf('(');
    const q = color.indexOf(')');
    if (p != -1 && q != -1) {
      const str = color.substring(p + 1, q);
      const ary = str.split(',');
      if (ary.length === 4) {
        return color;
      } else if (ary.length === 3) {
        return 'rgba(' + str + ',' + opacity + ')';
      } else {
        return color;
      }
    } else {
      return color;
    }
  }

  if (color.charAt(0) == '#') {
    color = color.substring(1);
  }
  if (color.length == 3) {
    color = color[0] + color[0] + color[1] + color[1] + color[2] + color[2];
  }
  if (/^[0-9a-fA-F]{6}$/.test(color)) {
    color1 = parseInt(color.substr(0, 2), 16);
    color2 = parseInt(color.substr(2, 2), 16);
    color3 = parseInt(color.substr(4, 2), 16);
    if (!opacity) {
      return 'rgb(' + color1 + ',' + color2 + ',' + color3 + ')';
    } else {
      return 'rgb(' + color1 + ',' + color2 + ',' + color3 + ',' + opacity + ')';
    }
  }
};

// 获取表单中的图片
export const loadImg = (formId) => {
  let $formObj;
  if (formId.indexOf('.') == 0) {
    $formObj = $(formId);
  } else {
    const obj = o(formId);
    if (!obj) {
      console.warn('未找到 ' + formId);
      return;
    }
    $formObj = $(obj);
  }

  $formObj.find('img').each(function () {
    let imgPath: any = $(this).attr('src');
    console.log('imgPath', imgPath);
    if (imgPath.indexOf('path=') != -1) {
      imgPath = imgPath.substring(imgPath.lastIndexOf('=') + 1);
      // 图标宏控件，亮灯 http://localhost:8086/***/showImgInJar.do?path=/static/images/symbol/lamp_2.png
      if (imgPath.indexOf('static/') != -1) {
        getShowImgInJar({ path: imgPath }).then((res2) => {
          $(this).attr('src', bufToUrl(res2));
        });
        return;
      }
    }
    if (
      imgPath.indexOf('/resource') == -1 &&
      !imgPath.startsWith('http') &&
      !imgPath.startsWith('blob:') &&
      !imgPath.startsWith('data:')
    ) {
      getShowImg({ path: imgPath }).then((res: any) => {
        $(this).attr('src', bufToUrl(res));
      });
    }
  });
};

export const loadImgInJar = async (imgPath) => {
  if (imgPath.indexOf('path=') != -1) {
    imgPath = imgPath.substring(imgPath.lastIndexOf('=') + 1);
    // 图标宏控件，亮灯 http://localhost:8086/***/showImgInJar.do?path=/static/images/symbol/lamp_2.png
    if (imgPath.indexOf('static/') != -1) {
      console.log('loadImgInJar', imgPath);
      const res2 = await getShowImgInJar({ path: imgPath });
      return bufToUrl(res2);
    } else {
      return null;
    }
  }
};

// export const loadImgByJQueryObj = ($obj) => {
//   $obj.find('img').each(function () {
//     let imgPath: any = $(this).attr('src');
//     console.log('imgPath', imgPath);
//     if (imgPath.indexOf('path=') != -1) {
//       imgPath = imgPath.substring(imgPath.lastIndexOf('=') + 1);
//       // 图标宏控件，亮灯 http://localhost:8086/***/showImgInJar.do?path=/static/images/symbol/lamp_2.png
//       if (imgPath.indexOf('static/') != -1) {
//         getShowImgInJar({ path: imgPath }).then((res2) => {
//           $(this).attr('src', bufToUrl(res2));
//         });
//         return;
//       }
//     }
//     if (
//       imgPath.indexOf('/resource') == -1 &&
//       !imgPath.startsWith('http') &&
//       !imgPath.startsWith('blob:') &&
//       !imgPath.startsWith('data:')
//     ) {
//       getShowImg({ path: imgPath }).then((res: any) => {
//         $(this).attr('src', bufToUrl(res));
//       });
//     }
//   });
// };

// 获取表单中路径为
export const initFormCtl = (formId) => {
  let formObj;
  if (typeof formId == 'string') {
    formObj = o(formId);
  } else {
    formObj = formId;
  }

  if (!formObj) {
    console.warn('form: ' + formId + ' 不存在');
    return;
  }
  $(formObj)
    .find('input')
    .each(function () {
      if ($(this).attr('kind') == 'DATE' || $(this).attr('kind') == 'DATE_TIME') {
        $(this).attr('autocomplete', 'off');
      }
    });

  // 替换按钮的样式
  const btns = formObj.getElementsByTagName('button');
  for (let i = 0; i < btns.length; i++) {
    btns[i].className = 'ant-btn ant-btn-primary ant-btn-sm form-btn';
  }

  const inputBtns = formObj.getElementsByTagName('input');
  for (let i = 0; i < inputBtns.length; i++) {
    if (inputBtns[i].getAttribute('type') === 'button') {
      // 注意不能有ant-btn，否则按钮会消失
      // inputBtns[i].className = 'ant-btn ant-btn-primary ant-btn-sm form-btn';
      inputBtns[i].className = 'ant-btn-primary ant-btn-sm form-btn';
    }
  }

  // 初始化提示
  $(formObj)
    .find('input,select,textarea')
    .each(function () {
      let tip = '';
      if ($(this).attr('type') == 'radio') {
        tip = $(this).parent().attr('tip');
      } else {
        tip = $(this).attr('tip');
      }
      //console.log('tip', tip);
      if (null != tip && '' != tip) {
        $(this).poshytip({
          content: function () {
            return tip;
          },
          className: 'tip-yellowsimple',
          alignTo: 'target',
          alignX: 'center',
          offsetY: 5,
          allowTipHover: true,
        });
      }

      // 判断是否为隐藏元素，初始化控件宽度及样式
      const $obj = $(this);
      // is(':hidden')为隐藏的元素，但是有些根据显示规则会有变化，故也需初始化
      // if (!$obj.is(':hidden') && !$obj.prop('disable')) {
      if (!$obj.prop('disable')) {
        if (
          (this.type != 'hidden' &&
            this.type != 'checkbox' &&
            this.type != 'radio' &&
            this.type != 'button' &&
            this.tagName == 'INPUT') ||
          this.tagName == 'SELECT' ||
          this.tagName == 'TEXTAREA'
        ) {
          if (!this.style.width) {
            if (
              this.className.indexOf('ueditor') == -1 &&
              this.className.indexOf('opinionTextarea') == -1
            ) {
              this.style.width = '150px';
            }
          }
          $obj.addClass('ant-input-affix-wrapper');
          $obj
            .focus(function () {
              $(this).addClass('ant-input-affix-wrapper-focus');
            })
            .blur(function () {
              $(this).removeClass('ant-input-affix-wrapper-focus');
            });
        }
      }
    });
};

/* 判断各个浏览器版本号 */
export function matchVesion(): Recordable {
  const userAgent = navigator.userAgent;
  const rMsie = /(msie\s|trident.*rv:)([\w.]+)/;
  const rEdge = /(edg)\/([\w.]+)/;
  const rFirefox = /(firefox)\/([\w.]+)/;
  const rOpera = /(opera).+version\/([\w.]+)/;
  const rChrome = /(chrome)\/([\w.]+)/;
  const rSafari = /version\/([\w.]+).*(safari)/;
  const ua = userAgent.toLowerCase();
  const match = rMsie.exec(ua);
  if (match !== null) {
    return { browser: 'IE', version: match[2] || '0' };
  }
  const rEmatch = rEdge.exec(ua);
  if (rEmatch !== null) {
    return { browser: 'Edge', version: rEmatch[2] || '0' };
  }
  const rFmatch = rFirefox.exec(ua);
  if (rFmatch !== null) {
    return { browser: rFmatch[1] || '', version: rFmatch[2] || '0' };
  }
  const rOmatch = rOpera.exec(ua);
  if (rOmatch !== null) {
    return { browser: rOmatch[1] || '', version: rOmatch[2] || '0' };
  }
  const rCmatch = rChrome.exec(ua);
  if (rCmatch !== null) {
    return { browser: rCmatch[1] || '', version: rCmatch[2] || '0' };
  }
  const rSmatch = rSafari.exec(ua);
  if (rSmatch !== null) {
    return { browser: rSmatch[2] || '', version: rSmatch[1] || '0' };
  }
  if (match !== null) {
    return { browser: '', version: '0' };
  }
  return {};
}

export const o = (s) => {
  const e = document.getElementById(s);
  if (e != null) return e;
  const els = document.getElementsByName(s);
  if (els.length == 0) return null;
  else return els[0];
};

export const getServerInfo = () => {
  const userStore = useUserStore();
  return userStore.getServerInfo;
};

export const isImage = (path) => {
  const p = path.lastIndexOf('.');
  path = path.substring(p + 1);
  return path == 'jpg' || path == 'png' || path == 'gif' || path == 'jpeg' || path == 'bmp';
};
