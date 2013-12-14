/**
 * jRubber jQuery slider v2.0
 * http://jrubber.org
 *
 * Copyright 2011, Iordan Mark
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * 
 * Date: Wed May 1 18:53:34 2013 -0300
 */


(function($) {
    $.fn.jRubber = function(options) {
        var defaults = {
            // Режим перелистывания:
            // block - по одному блоку
            // page - постранично
            flipped: 'page',
            // Режим перелистывания блоков: 
            // 3 - scroll-слайдер
            // 4 - для блоков с разными картинкамиre
            effect: 3,
            // автоматическая прокрутка слайдера с задержкой по времени в м/с
            autoScroll: 0,
            //Скорость перелистывания блоков.
            speed: 250,
            //Вкл./Выкл. пейджер.
            pager: false,
            //Информационный текст в пейджере.
            pagerChSt: 'Page',
            pagerChEn: 'of',
            //Вкл./Выкл. навигацию.
            button: false,
            back: '',
            next: '',
            //размер одного блока
            widthBlock: 0,
            //Отступ между блоками
            edgeBlock: 3,
            //Позицианирование контейнера с блоками между кнопками навигации.
            contentLeft: 55,
            contentRight: 55


        };
        options = $.extend(defaults, options);

        var $this = $(this);
        var content = $this.find('.jr-cnt');
        var margin = options.edgeBlock;
        var speed = options.speed;

        // текущий размер контейнера блоков
        var widthСontainer = 0;
        var widthСontainerAll = 0;
        // номер страницы
        var pageId = 1;
        var pageLength = 0;
        var blockLength = $(content).find('li').length;


        // размер блока
        var widthBlock = options.widthBlock;//$(content).find('li').eq(0).width();
        var allowBlock = 0;
        var widthAll = 0;
        var blockId = 0;
        var distanceBetweenBlocks = 0;

        var autoScroll = options.autoScroll;
        var timerId = 0;
        var way = false;


        if (options.widthBlock > 0) {
            $(content).find('li').width(widthBlock);
        }

        if (options.button) {
            $this.find('.content').
            prepend('<a class="back-button" href="#Back"><span class="Back"><span>' + options.back + '</span></span></a>').
            append('<a class="next-button" href="#Next"><span class="Next"><span>' + options.next + '</span></span></a>');
            $(content).css('margin', '0 ' + options.contentLeft + 'px 0 ' + options.contentRight + 'px');
        }

        // пейджер, отображающий инфо. о страницах
        if (options.pager && options.flipped == 'page') {
            $this.find('.head').prepend('<div class="pager">' + options.pagerChSt + ' <span id="page-num"></span>' + options.pagerChEn + ' <span id="page-all"></span></div>');
        }




        switch (options.effect) {
            case 1:

                break
            case 2:

                break
            case 3:
                scroll();
                info();
                break
            case 4:
                $(content).prepend('<div class="jr-load">loading...</div>')
                break
        }

        if (options.effect != 4) {
            buttonView();
        }

        $(window).load(function() {
            switch (options.effect) {
                case 4:
                    scrollVarious();
                    $(content).find('.jr-load').remove();
                    info();
                    buttonView();
                    break
            }

            if (autoScroll > 0) {
                timerId = setTimeout(function() {
                    scrollPage('-');
                }, options.autoScroll);
            }

        });


        $(window).resize(function() {

            switch (options.effect) {
                case 1:

                    break
                case 2:

                    break
                case 3:
                    scroll();
                    reset();
                    break
                case 4:
                    scrollVarious();
                    reset();
                    break
            }

            info();

        });

        try {
            $(content).mousewheel(function(event, delta) {
                clearTimeout(timerId);

                var action = (delta == -1) ? '-' : '+';

                switch (options.effect) {
                    case 1:

                        break
                    case 2:

                        break
                    case 3:
                        scrollPage(action);
                        break
                    case 4:
                        scrollPage(action);
                        break
                }

                info();

                return false;
            });
        } catch (e) {
        }


        $this.find('.back-button').click(function(e) {
            
            e.preventDefault();
            clearTimeout(timerId);

            switch (options.effect) {
                case 1:

                    break
                case 2:

                    break
                case 3:
                    scrollPage('+');
                    break
                case 4:
                    scrollPage('+');
                    break
            }

            info();

        });


        $this.find('.next-button').click(function(e) {

            e.preventDefault();
            clearTimeout(timerId);

            switch (options.effect) {
                case 1:

                    break
                case 2:

                    break
                case 3:
                    scrollPage('-');
                    break
                case 4:
                    scrollPage('-');
                    break
            }

            info();
        });


        function scroll() {
            widthСontainer = $(content).width();

            allowBlock = Math.floor(widthСontainer / (widthBlock + margin));

            var widthAll = (widthBlock * allowBlock) + (margin * allowBlock);

            var MarginDynamic = Math.floor(((widthСontainer - widthAll) / (allowBlock * 2)) + (margin / 2));

            content.find('li').slice(0, blockLength).css("margin-right", MarginDynamic + "px")
            .css("margin-left", MarginDynamic + "px");

            pageLength = Math.ceil(blockLength / allowBlock);

            $(content).find('ul').css('width', ((widthBlock + MarginDynamic * 2) * blockLength));
            
            distanceBetweenBlocks = MarginDynamic;
        }


        function scrollVarious() {
            pageLength = 0;
            widthСontainer = $(content).width();
            allowBlock = 0;
            widthAll = 0;
            blockId = 0;
            widthСontainerAll = 0;
            var MarginDynamic = 0;

            $this.find('ul li').each(function(indx) {
                blockId++;
                allowBlock++;


                widthBlock = $(this).width();
                widthAll += (widthBlock + margin);
                widthСontainerAll += (widthBlock + margin);

                if (widthAll >= widthСontainer) {
                    pageLength++;

                    MarginDynamic = complete_replacement(widthСontainer,
                        widthAll - (widthBlock + margin),
                        allowBlock > 1 ? allowBlock - 1 : allowBlock,
                        blockId - 1, 'start');

                    widthСontainerAll += allowBlock * (MarginDynamic * 2);

                    allowBlock = 1;
                    widthAll = (widthBlock + margin);
                }

            });


            if (widthAll > 0) {
                pageLength++;
                MarginDynamic = complete_replacement(widthСontainer, widthAll, allowBlock, blockId + 1, 'end');
                widthСontainerAll += allowBlock * (MarginDynamic * 2);
            }

            $(content).find('ul').css('width', widthСontainerAll);

            return {};
        }


        function complete_replacement(widthСontainer, widthAll, allowBlock, blockId, act) {

            //alert(widthСontainer+"; "+allowBlock+"; "+widthAll +"; "+blockId );

            var MarginDynamic = Math.floor(((widthСontainer - widthAll) / (allowBlock * 2)) + (margin / 2));

            content.find('li').slice(act == 'start' ? blockId - allowBlock : blockId - (allowBlock + 1), blockId + 1).css("margin-right", MarginDynamic + "px")
            .css("margin-left", MarginDynamic + "px");

            return MarginDynamic;
        }


        function reset() {
            $(content).find('ul').css('left', '0px');

            pageId = 1;
            buttonView();
        }

        function info() {
            $this.find('#page-all').html((options.flipped=='page'?pageLength:blockLength) + ' ');
            $this.find('#page-num').html(pageId + ' ');
        }

        function buttonView() {
            if (
                (pageLength == pageId && options.flipped=='page') || 
                (blockLength == pageId+allowBlock-1 && options.flipped=='block')) {
                $this.find('.next-button').addClass('disable');
            } else {
                $this.find('.next-button').removeClass('disable');
            }

            if (pageId == 1) {
                $this.find('.back-button').addClass('disable');
            } else {
                $this.find('.back-button').removeClass('disable');
            }
        }

        function scrollPage(action) {
            
            if (
                (((pageLength > pageId && action == '-') || (pageId > 1 && action == '+')) && options.flipped == 'page') ||
                (((blockLength > pageId+allowBlock-1 && action == '-') || (pageId > 1 && action == '+')) && options.flipped == 'block')
                ) {
                var iterator = pageId-1;
                
                if (action == '+') {
                    iterator = pageId-2;
                    pageId--;
                    
                } else if (action == '-') {
                    pageId++;
                }

                if(options.flipped == 'page') {
                    $(content).find('ul').animate({
                        left: action + '=' + $(content).width()
                    }, options.speed);
                } else if(options.flipped == 'block') {
                    var li = $(content).find('li').eq(iterator);
                    var margin = (li.css("margin-left").replace(/px/, "")*1)+
                    (li.css("margin-right").replace(/px/, "")*1);
                    
                    $(content).find('ul').animate({
                        left: action + '=' + (li.width()+margin)
                    }, options.speed);
                }
            }

            buttonView(action);

            if (autoScroll > 0) {
                timerId = setTimeout(function() {
                    if (pageId == 1) {
                        way = false;
                        scrollPage('-');
                        return;
                    }
                    if (pageId > 1 && pageId < pageLength && !way) {
                        way = false;
                        scrollPage('-');
                        return;
                    }
                    if (pageId == pageLength || way) {
                        way = true;
                        scrollPage('+');
                    }
                }, options.autoScroll);
                info();
            }
        }

    }
})($);