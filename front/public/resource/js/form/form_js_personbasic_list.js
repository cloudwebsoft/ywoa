// 打开详情页示例
function clickIt(id) {
  var dept = o("dept").value;
  openWinModuleShow('personbasic', id, '', {'dept': dept});
}

// 通过静态路由打开列表页示例
function jump(xmId) {
  // 打开一个新的选项卡
  goTo('/smartModulePage', {
    moduleCode: 'personbasic',
    xm: xmId,
  })
}