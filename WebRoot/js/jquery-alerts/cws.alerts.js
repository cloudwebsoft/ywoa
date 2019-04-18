function jAlert_Redirect(msg, title, url) {
	if (!document.body)
		document.write("<body></body>");
	jAlert(msg, title, function() {
			window.location.href = url;
		});
}

function jAlert_Back(msg, title) {
	if (!document.body)
		document.write("<body></body>");
	jAlert(msg, title, function() {
			window.history.back();
		});				
}