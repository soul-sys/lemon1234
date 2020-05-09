jQuery(document).ready(function($) {	
	if($("meta[name=toTop]").attr("content")=="true"){
		$("<div id='toTop'><img src='/static/img/toTop.png'></div>").appendTo('body');
		$("#toTop").css({
			width: '50px',
			height: '50px',
			bottom:'50px',
			right:'50px',
			position:'fixed',
			cursor:'pointer',
			zIndex:'999999',
		});
		if($(this).scrollTop()==0){
				$("#toTop").hide();
			}
		$(window).scroll(function(event) {
			if($(this).scrollTop()==0){
				$("#toTop").hide();
			}
			if($(this).scrollTop()!=0){
				$("#toTop").show();
			}
		});	
			$("#toTop").click(function(event) {
				$("html,body").animate({
					scrollTop:"0px"},
					666
					)
			});
		}
});
