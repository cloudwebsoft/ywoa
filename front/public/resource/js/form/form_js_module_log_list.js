function restore(id) {
    myConfirm('提示', '您确定要恢复么？', function() {
        var params = {id: id};
        ajaxPost('/visual/restore', params).then((data) => {
            myMsg(data.msg);
        });
    });
}