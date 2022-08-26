$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	//获取标题和内容
	var title=$("#recipient-name").val();
	var content=$("#message-text").val();
	//发送异步请求
	$.post(
		CONTEXT_PATH+"/discuss/add",
		{"title":title,"content":content},
		function(data){
			data=$.parseJSON(data);
			//在提示框显示返回的消息
			$("#hintBody").text(data.msg);
			//显示提示框2秒后自动隐藏
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//刷新页面
				if(data.code==0){
					window.location.reload();
				}
			}, 2000);
		}
	)

	
}