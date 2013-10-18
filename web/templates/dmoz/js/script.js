(function($) {
    
    $(document).ready(function() {
        /*$('article img').load(function() {
            
            var height = $(this).height();
            $('#resl').prepend(height+'<br>');
        });*/
        
        //$('img').last().load(function() {
        
            
        //Vote
        $('.vote_up').click(function(e) {
            e.preventDefault();
            
            var arr = $(this).attr('value').split('_');
            ajaxRating('top',(arr.length<2?'post':arr[1]),arr[0]);
        });
        
        $('.vote_down').click(function(e) {
            e.preventDefault();
            //if(confirm('Вы уверены в том, что хотите понизить рейтинг данного поста?')){
            var arr = $(this).attr('value').split('_');
            ajaxRating('down',(arr.length<2?'post':arr[1]),arr[0]);
        //}
        });
        
        $('.img__href.img__animated').click(function(e) {
            e.preventDefault();
            if(!$(this).hasClass("img__play")) {
                var src = $(this).find('img').attr('src');
                $(this).find('img').attr('src', src.replace(/middle_/g, ''));
                $(this).addClass('img__play');
                $(this).find('.list__info__img__hint').hide();
            } else {
                $(this).find('img').attr('src','http://yourmood.ru/photo_anekdot/middle_'+$(this).attr('value'));
                $(this).removeClass('img__play');
                $(this).find('.list__info__img__hint').show();
            }
        });
        
        $('.img__href').click(function(e) {
            
            });
        
        fieldlist = $('#content-list-field-main fieldset').length;
        $('#link-list-field').click(function(e) {
            e.preventDefault();
            fieldlist++;
            if(fieldlist<100) {
                $('#content-list-field').append('<fieldset id="list-'+fieldlist+'"><legend>Форма №'+fieldlist+' <a style=\"color:red;\" href=\"javascript:deleteItem('+fieldlist+')\">Удалить</a> &nbsp;</legend><div class="substrate"><div>Анекдот / Надпись над картинкой:<textarea name="text" rows="9" class="inp"></textarea></div><div style="text-align: left">Загрузить картинку?<br> <input type="file" name="photo"></div><div>Описание картинки (255 сим.):<input name="alt" style="width: 99%" class="inp"></div></div></fieldset>');
            }
        });
    });
    
    
    
    $.fn.addComment = function(options) {
        var defaults = {			
            q: 'AddComment',
            id: 0
        };
        options = $.extend(defaults, options);
        if(options.id==0) return;
        
        
        
        $(this).submit(function(event) {

            event.preventDefault(); 
        
            var $form = $(this),
            name = $form.find( 'input[name="name"]' ).val(),
            email = $form.find( 'input[name="email"]' ).val(),
            text = $form.find('textarea[name="text"]' ).val(),
            url = $form.attr('action');
            $form.find('input[name="button"]').attr("disabled", "disabled");

            $.post(url, {
                q: options.q,
                id: options.id,
                name: name,
                email: email,
                text: text
            },
            function(data) {
                
                //$("#message").html('<ul class="good">'+data.message+'</ul>');
                $("#message ul").removeClass('error').removeClass('good');
                $("#message ul").addClass(data.status);
                
                var pattern = /^(good)/i;
                if(pattern.test(data.status)) {
                                        
                    
                    if($('.getComment h2.navsections').length==0) {
                        $( ".getComment" ).html( '<h1>Комментарии (1)</h1>' );
                        $( ".getComment" ).append( data.message );
                    } else {
                        $( ".getComment" ).append( data.message );
                    }

                    $('.vote_up').click(function(e) {
                        e.preventDefault();
            
                        var arr = $(this).attr('value').split('_');
                        ajaxRating('top',(arr.length<2?'post':arr[1]),arr[0]);
                    });
        
                    $('.vote_down').click(function(e) {
                        e.preventDefault();
                        if(confirm('Вы уверены в том, что хотите понизить рейтинг данного поста?')){
                            var arr = $(this).attr('value').split('_');
                            ajaxRating('down',(arr.length<2?'post':arr[1]),arr[0]);
                        }
                    });
                    
                    $form.find('textarea[name="text"]').val('');
                    $form.find('input[name="name"],input[name="email"]').addClass("readonly").attr('readonly', true).removeAttr('name');
                    
                } else {
                    
                    $("#message").html('<ul class="error">'+data.message+'</ul>');
                    
                }
                
                
                
                
                $form.find('input[name="button"]').removeAttr("disabled");

            }, "json"
            );
        });
    }
    
    $(window).scroll(function() {
        if (!$('#hd').hasClass("fixed") && $(window).scrollTop() > 255) {
            $('#hd').css('opacity','0');
            $('#hd').animate({
                opacity: 1
            }, 500).addClass('fixed');
            $('#doc').css('margin-top','55px');
        } else if($('#hd').hasClass("fixed") && $(window).scrollTop() <= 55) {
            $('#hd').removeClass('fixed');
            $('#doc').css('margin-top','0px');
        }
    });

    $(window).scroll(function() {
        if ($(window).scrollTop() > 255) {
        //$("#scrollTop").show();
        } else {
        //$("#scrollTop").hide();
        }
    });
    
    var focus = true;
    $(document).ready(function() {
        /*$('#scrollTop').click(function () {
            $('body,html').animate({
                scrollTop: 0
            }, 0);
            return false; 
        });*/
        
        $('input, select, textarea').focus(function() {
            focus = false;
        });
        $('input, select, textarea').blur(function() {
            focus = true;
        });
        $(document).keyup(function (c) { 
            if(c.which == 82 && focus){
                $('body,html').animate({
                    scrollTop: 0
                }, 0);
            }
        });
    });
        
    $.fn.listScroll = function(options) {
        
            
        
        var defaults = {};
        options = $.extend(defaults, options);
            
        var b = [];
        
        $(".item__segment").each(function (c, d) {
            b[c] = [c==0 ? 0 : Math.ceil($(d).offset().top), $(d).height()];
        });
        $(document).keyup(function (c) {
            if ((87 == c.which || 83 == c.which) && focus) {
                for (var d = $(document).scrollTop(), a = 0; a < b.length && !(b[a][0] + b[a][1] > d+55); a++);
                a = 83 == c.which ? ++a : --a;
                0 > a && (a = 0);
                a >= b.length && (a = b.length - 1);
               
                $("body,html").animate({
                    scrollTop: b[a][0]-55
                }, 100);
                
            }
        });
        
    }
    
})($);

function deleteItem(id) {
    $('#list-'+id).remove();
    fieldlist--;
}

function ajaxRating(vote, action, id) {
    //var load = new Array('сек.','жопа','~','сек.','~','сек.','~','cиське','~');
    //$('#rating_'+id).html(load[randomNumber(0, load.length-1)]);
    //alert('/Service?q=rating&vote='+vote+'&action='+action+'&id='+id+'&lastVote='+$('#vote_'+id).html());
    
    $.ajax({
        url: '/Service?q=rating&vote='+vote+'&action='+action+'&id='+id+'&lastVote='+$('#vote_'+id).html(),
        dataType:'json',
        type: 'GET',
        timeout: 5000,
        cache: false,
        success: function(response,status) {
            if(status=="success") {
                $('#vote_'+id).removeClass('vote__error').removeClass('vote__good');
                $('#vote_'+id).addClass('vote__'+response.status);
                $('#vote_'+id).fadeOut(0);
                $('#vote_'+id).fadeIn(200).html(response.message);
            }
        },
        error: function() {
            $('#vote_'+id).addClass('vote__error');
            $('#vote_'+id).fadeOut(0);
            $('#vote_'+id).fadeIn(300).html("Упс!");
        }
    });
}

function log(out) {
    $('#out__error').prepend(out);
}

function confurmSubmit() {
    var name = $('#addAnekdot').find('input[name="name"]').val();
    return confirm((name!=''?name+', т':"Т")+'ы удалил с изображений, мусор: логотипы, адреса сайтов, рекламу?\nА категорию указал верно, белеать?');
}

// случайное число
function randomNumber(m, n) {
    m = parseInt(m);
    n = parseInt(n);
    return Math.floor(Math.random() * (n - m + 1)) + m;
}

