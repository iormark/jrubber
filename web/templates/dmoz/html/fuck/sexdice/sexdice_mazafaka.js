(function($) {
    $.fn.jSexdice = function(options) {
        var defaults = {			

        };
        
    
        options = $.extend(defaults, options);
        
        var $this = $(this);
        var girl = Math.floor(Math.random()*6);
        var pix = 100; //колличество пикселов
        var speed = 300; //скорость
        var level = 1;
        var status = false;
        var dickIter = -1;
        var click = 0;
        var Star = new Array();
        var AllDick = 0;
        var BestTime = 0;
        var RealTime = 0;
        
        /*
         * Настройка AJAX
         */
        $.ajaxSetup({
            url: '/SexDice', 
            type: 'get', 
            dataType: 'html',
            timeout: 7000,
            cache: false
        });
        
        $.ajax({
            data: 'q=setSession&girl='+girl+'&level='+level,
            success: server.onSuccess,
            error: server.onError
        });
        
        simple_timer(0, $('#real-time'), false);
        
        
        $.ajax({
            data: 'q=getTime',
            success: function(response, status) {
                BestTime = response;                
                simple_timer(BestTime, $('#best-time'), true);
            },
            error: server.onError
        });
        
        
        $('html').mouseover(function(e) {
            doReboot();
        });
        
        $(window).resize(function() {
            location.reload();
        });
        
        $this.find('.dick').mouseover(function(e) {
            
            dickIter = $this.find('.dick').index(this);
                        
            
            var x = 0, y = 0, w = 0, h = 0, 
            top = Math.floor($(this).offset().top), 
            left = Math.floor($(this).offset().left);
            
            x = Math.floor(e.pageX - left);
            y = Math.floor(e.pageY - top);
            w = $(this).width();
            h = $(this).height();
            
            
            
            //if(status == false) {
            //$('#level').html("x="+x+";y="+y+"; left="+left+"; top="+top+"; w="+w+"; h="+h+"; speed="+speed);
            
            if(x<w && (y>=0 && y<20)) {
                Slide(top,0,'top');
            } else 
            if(x<w && (y<w+3 && y>w-20)) {
                Slide(top,0,'bottom');
            } else 
            if(y<w && (x>=0 && x<20)) {
                Slide(0,left,'left');
            } else 
            if(y<w && (x<=w+2 && x>w-20)) {
                Slide(0,left,'right');
                
            }
        //}
        
            
            
        });
        
        function Slide(x, y, action) {
            
            var dick = $("#dick_"+dickIter);
            

            if((action=='top' && level<4) || (level==4 && action=='bottom')) {
                $(dick).animate({ 
                    top: (x+pix)+'px'
                }, speed );
            }
            else
            if((action=='bottom' && level<4) || (level==4 && action=='top')) {
                $(dick).animate({ 
                    top: (x-pix)+'px'
                }, speed );
            }
            else
            if((action=='left' && level<4) || (level==4 && action=='right')) {
                $(dick).animate({ 
                    left: (y+pix)+'px'
                }, speed );
            }
            else
            if((action=='right' && level<4) || (level==4 && action=='left')) {
                $(dick).animate({ 
                    left: (y-pix)+'px'
                }, speed );
            }
        }
        
        
        
        $this.find('.dick').click(function(e) {

            
            $(this).animate({ 
                width: "1px",
                height: "1px",
                fontSize: "0.1em",
                BorderRadius:"50%",
                opacity: 'hide'
            }, 80 );
            
            AllDick++;

            doReboot();

        });
        
        $this.find('#anew').click(function(e) {
            
            
            girl = Math.floor(Math.random()*6);
            $('#res').html();
            $('#bkg').show();
                
            setTimeout(function(){
                Reboot()
            },1000);
            
            level = 1;
            
            e.preventDefault();
        });
        
        
        
        $this.find('#kartinka_vid').click(function(e) {
            $this.find('#kartinka').slideToggle('normal');
            e.preventDefault();
        });
        
        function doReboot() {
            if(/*$this.find('.dick:visible').size()==1 && */AllDick>=3) {
                AllDick = 0;
                Star[level-1] = click;

                
                level += 1;
                pix += 20;
                speed -= 80;
                
                if(level == 4) {
                    speed = 50;
                } else if (level == 5) {
                    Reboot();
                    //$('body').css('background', 'url(/templates/default/html/fuck/sexdice/kartinki/'+girl+'/'+level+'.jpg) center -9% no-repeat');
                    $.ajax({
                        data: 'q=setSession&girl='+girl+'&level='+level,
                        success: server.onSuccess,
                        error: server.onError
                    });
                    return;
                }


                $('#bkg').show();
                
                setTimeout(function(){
                    Reboot()
                },1000);
            }
        }
        
        function Reboot() {
            click = 0;
            
            if(level==1) {
                $('#level').html('Уровень: первый');
            } else if(level==2) {
                $('#level').html('Уровень: <span style="color:#42529C">второй</span>');
            } else if(level==3) {
                $('#level').html('Уровень: <span style="color:green">третий</span>');
            } else if(level==4) {
                $('#level').html('Уровень: <span style="color:#F30">четвёртый</span>');
            } else if(level==5) {
                $('#level').html('<span style="color:#F30">Конец игры :)</span>');
                
                
                if(RealTime < BestTime || BestTime == 0) {
                    $.ajax({
                        data: 'q=setTime&besttime='+RealTime,
                        success: function(response, status) {
                            BestTime = response;                
                            simple_timer(BestTime, $('#best-time'), true);
                        },
                        error: server.onError
                    });
                }
                
            }
            
            
            
            $('#bkg').hide();
            
            if(level<5) {
                $this.find('.dick').show();
                $this.find('.dick').
                css('width','100px').
                css('height','100px').
                css('border-radius','10%').
                css('font-size','80px').css('top','45%');
            
            
                $this.find('#dick_0').css('left','33%');
                $this.find('#dick_1').css('left','43%');
                $this.find('#dick_2').css('left','53%');
            
            }
            
            $.ajax({
                data: 'q=setSession&girl='+girl+'&level='+level,
                success: server.onSuccess,
                error: server.onError
            });
            
        //$('body').css('background', 'url(/templates/default/html/fuck/sexdice/kartinki/'+girl+'/'+level+'.jpg) right 10px no-repeat');
            
        }
        
        $('html').click(function(e) {
            click++;
            //$('#act').html(click);
            
            if(click==130) {
                $('#level').html('<span style="color:#F30">Нервишки еще не сдают?:D</span>');
            }
            if(click==210) {
                $('#level').html('<span style="color:green">Сделай глубокий вдох)</span>');
            }
            if(click==310) {
                $('#level').html('<span style="color:#42529C">Молодцом)</span>');
            }
        });
        
        
        function simple_timer(sec, block, parse) {
            
            var time    = sec;
             
            var hour    = parseInt(time / 3600);
            if ( hour < 1 ) hour = 0;
            time = parseInt(time - hour * 3600);
            if ( hour < 10 ) hour = '0'+hour;
 
            var minutes = parseInt(time / 60);
            if ( minutes < 1 ) minutes = 0;
            time = parseInt(time - minutes * 60);
            if ( minutes < 10 ) minutes = '0'+minutes;
 
            var seconds = time;
            if ( seconds < 10 ) seconds = '0'+seconds;
 
            block.html(hour+':'+minutes+':'+seconds);
            
            
            RealTime = sec;
            
 
            if(level < 5 && !parse) { 
                sec++;
 
                setTimeout(function(){
                    simple_timer(sec, block, parse);
                }, 1000);
            } else {
                
            }
        }
        
        
        function parse_timer(sec) {
        
        }
        
    }
    
    
    
    var server = {
        onSuccess: function(data) {
            var rand = Math.floor(Math.random()*99999999);
            $('body').css('background', 'url(/SexDice?q=photo&r='+rand+') right 10px no-repeat'); 
        },
            
        onError: function(data) {
            alert("Error!");
        }
    }
})($);

