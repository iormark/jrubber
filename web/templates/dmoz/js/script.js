(function($) {

    $(document).ready(function() {
        /*$('article img').load(function() {
         
         var height = $(this).height();
         $('#resl').prepend(height+'<br>');
         });*/

        //$('img').last().load(function() {



        $('textarea').autosize();
        //$('textarea').elastic();


        //Vote
        $(document).on('click', '.vote_up', function(e) {
            e.preventDefault();

            var arr = $(this).attr('value').split('_');
            ajaxRating('top', (arr.length < 2 ? 'post' : arr[1]), arr[0]);
        });

        $(document).on('click', '.vote_down', function(e) {
            e.preventDefault();
            //if(confirm('Вы уверены в том, что хотите понизить рейтинг данного поста?')){
            var arr = $(this).attr('value').split('_');
            ajaxRating('down', (arr.length < 2 ? 'post' : arr[1]), arr[0]);
            //}
        });


        $('.img__href.img__animated').click(function(e) {
            e.preventDefault();
            if (!$(this).hasClass("img__play")) {
                var src = $(this).find('img').attr('src');
                $(this).find('img').attr('src', src.replace(/middle_/g, ''));
                $(this).addClass('img__play');
                $(this).find('.list__info__img__hint').hide();
            } else {
                var arr = $(this).attr('value').split(':');
                $(this).find('img').attr('src', 'http://yourmood.ru' + arr[0] + '/middle_' + arr[1]);
                $(this).removeClass('img__play');
                $(this).find('.list__info__img__hint').show();
            }
        });



    });



    $.fn.addComment = function(options) {
        var defaults = {
            q: 'AddComment',
            id: 0
        };
        options = $.extend(defaults, options);
        if (options.id == 0)
            return;



        $(this).submit(function(event) {

            event.preventDefault();

            var $form = $(this),
                    //name = $form.find('input[name="name"]').val(),
                    //email = $form.find('input[name="email"]').val(),
                    text = $form.find('textarea[name="text"]').val(),
                    url = $form.attr('action');
            $form.find('input[name="button"]').attr("disabled", "disabled");

            $.post(url, {
                q: options.q,
                id: options.id,
                //name: name,
                //email: email,
                text: text
            },
            function(data) {

                //$("#message").html('<ul class="good">'+data.message+'</ul>');
                $("#message ul").removeClass('error').removeClass('good');
                $("#message ul").addClass(data.status);


                var pattern = /^(good)/i;
                if (pattern.test(data.status)) {


                    if ($('.getComment h2.navsections').length == 0) {
                        //$(".getComment").html('<h1>Комментарии (1)</h1>');
                        $(".getComment").append(data.message);
                    } else {
                        $(".getComment").append(data.message);
                    }

                    $('.vote_up').click(function(e) {
                        e.preventDefault();
                        var arr = $(this).attr('value').split('_');
                        ajaxRating('top', (arr.length < 2 ? 'post' : arr[1]), arr[0]);
                    });

                    $('.vote_down').click(function(e) {
                        e.preventDefault();
                        if (confirm('Вы уверены в том, что хотите понизить рейтинг данного поста?')) {
                            var arr = $(this).attr('value').split('_');
                            ajaxRating('down', (arr.length < 2 ? 'post' : arr[1]), arr[0]);
                        }
                    });

                    $form.find('textarea[name="text"]').val('');
                    //$form.find('input[name="name"],input[name="email"]').addClass("readonly").attr('readonly', true).removeAttr('name');

                } else {

                    $("#message").html('<ul class="error">' + data.message + '</ul>');

                }

                $form.find('input[name="button"]').removeAttr("disabled");

            }, "json");
        });
    }

    $(window).scroll(function() {
        /*if (!$('#hd').hasClass("fixed") && $(window).scrollTop() > 39) {
         //$('#hd').css('opacity', '0');
         $('#docs').css('margin-top', '39px');
         $('#hd').addClass('fixed');
         
         } else if ($('#hd').hasClass("fixed") && $(window).scrollTop() <= 39) {
         
         $('#docs').css('margin-top', '0px');
         $('#hd').removeClass('fixed');
         }*/
    });

    var focus = true;
    $(document).ready(function() {
        /*$('#scrollTop').click(function () {
         $('body,html').animate({
         scrollTop: 0
         }, 0);
         return false; 
         });*/

        $(document).on('focus', 'input, select, textarea', function(e) {
            focus = false;
        });
        $(document).on('blur', 'input, select, textarea', function(e) {
            focus = true;
        });



        $(document).keyup(function(c) {
            if (c.which == 82 && focus) {
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

        $(".item__segment").each(function(c, d) {
            b[c] = [c == 0 ? 0 : Math.ceil($(d).offset().top), $(d).height()];
        });
        $(document).keyup(function(c) {
            if ((87 == c.which || 83 == c.which) && focus) {
                for (var d = $(document).scrollTop(), a = 0; a < b.length && !(b[a][0] + b[a][1] > d + 55); a++)
                    ;
                a = 83 == c.which ? ++a : --a;
                0 > a && (a = 0);
                a >= b.length && (a = b.length - 1);

                $("body,html").animate({
                    scrollTop: b[a][0] - 55
                }, 100);

            }
        });
    }

})($);

function deleteItem(id) {
    $('#list-' + id).remove();
    fieldlist--;
}

function ajaxRating(vote, action, id) {
    //var load = new Array('сек.','жопа','~','сек.','~','сек.','~','cиське','~');
    //$('#rating_'+id).html(load[randomNumber(0, load.length-1)]);
    //alert('/svc/Service?q=rating&vote=' + vote + '&action=' + action + '&id=' + id + '&lastVote=' + $('#vote_' + id).html());

    $.ajax({
        url: '/svc/Service?q=rating&vote=' + vote + '&action=' + action + '&id=' + id + '&lastVote=' + $('#vote_' + id).html(),
        dataType: 'json',
        type: 'GET',
        timeout: 5000,
        cache: false,
        success: function(response, status) {
            if (status == "success") {
                $('#vote_' + id).removeClass('vote__error').removeClass('vote__good');
                $('#vote_' + id).addClass('vote__' + response.status);
                $('#vote_' + id).fadeOut(0);
                $('#vote_' + id).fadeIn(200).html(response.message);
            }
        },
        error: function() {
            $('#vote_' + id).addClass('vote__error');
            $('#vote_' + id).fadeOut(0);
            $('#vote_' + id).fadeIn(300).html("Упс!");
        }
    });
}

function log(out, obj) {
    obj = obj != null ? obj : '#out__error';
    $(obj).html(out).fadeIn(500);
    setTimeout(function() {
        $(obj).fadeOut(500);
    }, 4000);

    return false;
}

function confurmSubmit() {
    var name = $('#addAnekdot').find('input[name="name"]').val();
    return confirm((name != '' ? name + ', т' : "Т") + 'ы удалил с изображений, мусор: логотипы, адреса сайтов, рекламу?\nА категорию указал верно, белеать?');
}

// случайное число
function randomNumber(m, n) {
    m = parseInt(m);
    n = parseInt(n);
    return Math.floor(Math.random() * (n - m + 1)) + m;
}



(function(e) {
    "function" == typeof define && define.amd ? define(["jquery"], e) : e(window.jQuery || window.$)
})(function(e) {
    var t, o = {className: "autosizejs", append: "", callback: !1, resizeDelay: 10}, i = '<textarea tabindex="-1" style="position:absolute; top:-999px; left:0; right:auto; bottom:auto; border:0; padding: 0; -moz-box-sizing:content-box; -webkit-box-sizing:content-box; box-sizing:content-box; word-wrap:break-word; height:0 !important; min-height:0 !important; overflow:hidden; transition:none; -webkit-transition:none; -moz-transition:none;"/>', n = ["fontFamily", "fontSize", "fontWeight", "fontStyle", "letterSpacing", "textTransform", "wordSpacing", "textIndent"], s = e(i).data("autosize", !0)[0];
    s.style.lineHeight = "99px", "99px" === e(s).css("lineHeight") && n.push("lineHeight"), s.style.lineHeight = "", e.fn.autosize = function(i) {
        return this.length ? (i = e.extend({}, o, i || {}), s.parentNode !== document.body && e(document.body).append(s), this.each(function() {
            function o() {
                var t, o;
                "getComputedStyle"in window ? (t = window.getComputedStyle(h, null), o = h.getBoundingClientRect().width, e.each(["paddingLeft", "paddingRight", "borderLeftWidth", "borderRightWidth"], function(e, i) {
                    o -= parseInt(t[i], 10)
                }), s.style.width = o + "px") : s.style.width = Math.max(p.width(), 0) + "px"
            }
            function a() {
                var a = {};
                if (t = h, s.className = i.className, d = parseInt(p.css("maxHeight"), 10), e.each(n, function(e, t) {
                    a[t] = p.css(t)
                }), e(s).css(a), o(), window.chrome) {
                    var r = h.style.width;
                    h.style.width = "0px", h.offsetWidth, h.style.width = r
                }
            }
            function r() {
                var e, n;
                t !== h ? a() : o(), s.value = h.value + i.append, s.style.overflowY = h.style.overflowY, n = parseInt(h.style.height, 10), s.scrollTop = 0, s.scrollTop = 9e4, e = s.scrollTop, d && e > d ? (h.style.overflowY = "scroll", e = d) : (h.style.overflowY = "hidden", c > e && (e = c)), e += f, n !== e && (h.style.height = e + "px", w && i.callback.call(h, h))
            }
            function l() {
                clearTimeout(u), u = setTimeout(function() {
                    var e = p.width();
                    e !== g && (g = e, r())
                }, parseInt(i.resizeDelay, 10))
            }
            var d, c, u, h = this, p = e(h), f = 0, w = e.isFunction(i.callback), z = {height: h.style.height, overflow: h.style.overflow, overflowY: h.style.overflowY, wordWrap: h.style.wordWrap, resize: h.style.resize}, g = p.width();
            p.data("autosize") || (p.data("autosize", !0), ("border-box" === p.css("box-sizing") || "border-box" === p.css("-moz-box-sizing") || "border-box" === p.css("-webkit-box-sizing")) && (f = p.outerHeight() - p.height()), c = Math.max(parseInt(p.css("minHeight"), 10) - f || 0, p.height()), p.css({overflow: "hidden", overflowY: "hidden", wordWrap: "break-word", resize: "none" === p.css("resize") || "vertical" === p.css("resize") ? "none" : "horizontal"}), "onpropertychange"in h ? "oninput"in h ? p.on("input.autosize keyup.autosize", r) : p.on("propertychange.autosize", function() {
                "value" === event.propertyName && r()
            }) : p.on("input.autosize", r), i.resizeDelay !== !1 && e(window).on("resize.autosize", l), p.on("autosize.resize", r), p.on("autosize.resizeIncludeStyle", function() {
                t = null, r()
            }), p.on("autosize.destroy", function() {
                t = null, clearTimeout(u), e(window).off("resize", l), p.off("autosize").off(".autosize").css(z).removeData("autosize")
            }), r())
        })) : this
    }
});