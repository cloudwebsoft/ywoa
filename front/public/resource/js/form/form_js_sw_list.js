// 处理公文
function launchFlow(flowId) {
  goTo('/flow/launch', {
    op: 'handlePaper',
    paperFlowId: flowId,
  })
}

// 抄送
function distribute(flowId) {
  openDistributeModal(flowId);
}