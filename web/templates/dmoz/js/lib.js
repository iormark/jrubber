(function($) {
    var interval;
    $('#share').hover(function(e) {
        e.preventDefault();
        
        $('#share ul').show();
        clearInterval(interval);
    },
    function () {
        interval=setTimeout(
            function() {
                $('#share ul').hide();
            }, 300);
    });
    
    $('#share ul li > a').click(function(e) {

        var service = $(this).attr('class');       
        var title = $('head > title').text();
        var img = typeof($('[itemprop="contentUrl"]').attr('content'))!='undefined' ? $('[itemprop="contentUrl"]').attr('content') : 'http://yourmood.ru/yourmood.gif';
        var url = window.location.href;
        var text = typeof($('.post_info .text').text())!='undefined' ? $('.post_info .text').text() : '';
        
        var winUrl = '';
        if(service == 'vkontakte') {
            winUrl  = 'http://vk.com/share.php?';
            winUrl += 'url=' + encodeURIComponent(url);
            winUrl += '&title=' + encodeURIComponent(title);
            winUrl += '&description=' + encodeURIComponent(text.substr(0,500));
            winUrl += '&image=' + encodeURIComponent(img);
            winUrl += '&noparse=true';
        }
        else if (service == 'facebook') {
            winUrl  = 'http://www.facebook.com/sharer.php?s=100';
            winUrl += '&p[title]=' + encodeURIComponent(title);
            winUrl += '&p[summary]=' + encodeURIComponent(text.substr(0,1000));
            winUrl += '&p[url]=' + encodeURIComponent(url);
            winUrl += '&p[images][0]=' + encodeURIComponent(img);
        }
        else if (service == "twitter") {
            winUrl  = 'http://twitter.com/share?';
            winUrl += 'text=' + encodeURIComponent(text!=''?text:title.substr(0,100)+'...\n');
            winUrl += '&url=' + encodeURIComponent(url);
            winUrl += '&counturl=' + encodeURIComponent(url);
        }
        else if (service == 'gplus') {
            winUrl  = 'https://plus.google.com/share?';
            winUrl += 'url=' + encodeURIComponent(url);
        }
        else if (service == 'odnoklassniki') {
            winUrl  = 'http://www.odnoklassniki.ru/dk?st.cmd=addShare&st.s=1';
            winUrl += '&st.comments=' + encodeURIComponent(text.substr(0,400));
            winUrl += '&st._surl=' + encodeURIComponent(url);
        }
        else if (service == 'yaru') {
            winUrl  = 'http://my.ya.ru/posts_add_link.xml';
            winUrl += '?URL=' + encodeURIComponent(url);
            winUrl += '&title=' + encodeURIComponent(title);
            winUrl += '&body=' + encodeURIComponent(text.substr(0,1000));
        } 
        else if (service == 'jj') {
            winUrl  = 'http://www.livejournal.com/update.bml';
            winUrl += '?subject=' + encodeURIComponent(title);
            winUrl += '&event=' + encodeURIComponent('<img src="'+img+'" style="float:left;matgim:0 10px 10px 0">'+text.substr(0,1000)+'<br><a href="'+url+'" target="_blank">'+url+'</a>');
            winUrl += '&prop_taglist=поиск книг, купить книгу, заказ книг, книгоед';
        }
        
        if (service == 'yaru' || service == 'jj'){
            $(this).attr('href', winUrl);
            
        } else {
            e.preventDefault();
            
            window.open(winUrl,'','left='+((screen.width-800)/2)+',top='+((screen.height-600)/2)+',toolbar=0,status=0,width=626,height=536');
        }
        
    });
})($);