jQuery(document).ready(function() {
/* Краткие пояснения:
   1. myzoom - CSS-класс, задает внешний вид, рамку и т.п.
   2. 500 - скорость анимации, в миллисекундах
   3. width,height - ширина и высота картинки. 
*/
    //Клик, - плавно увеличить картинку
   
    	jQuery('.myzoom').click(function(e){
            e.preventDefault();
     	       jQuery(this).children('img.modal').stop(true,true).css("display", "block")
	      .animate({width:"630px",height:"400px",left:"-63px",top:"0"}, 500);
    	});
       
 
    
 
});