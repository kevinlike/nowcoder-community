function like(btn,entityType,entityId,entityUserId,postId){
    $.post(
        CONTEXT_PATH+"/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        function(data){
            data=$.parseJSON(data);
            if(data.code==0){
                if(data.likeStatus==1)
                {
                    $(btn).children("b").text("已赞");
                }
                else if(data.likeStatus==0){
                    $(btn).children("b").text("赞");
                }
                $(btn).children("i").text(data.likeCount);
            }
            else{
                alert(data.msg);
            }
        }
    );
}