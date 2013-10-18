(function($) {
    
    $(document).ready(function() {
        
        $('.ajax-rating-top').click(function(e) {
            e.preventDefault();
            ajaxRating('top','humor_meta',$(this).attr('value'));
        });
        
        $('.ajax-rating-down').click(function(e) {
            e.preventDefault();
            if(confirm('Вы уверены в том, что хотите понизить рейтинг данного поста?')){
                //if(confirm('Администрация сайта не несет ответственность за ваше деяние!'))
                ajaxRating('down','humor_meta',$(this).attr('value'));
            }
        });
        
        
        $('#link-list-field').click(function(e) {
            e.preventDefault();
            $('#content-list-field').append('<fieldset id=\"list-" + i + "\"><p>Анекдот / Надпись над картинкой:<br><textarea name="text" rows="9" style="width: 100%"></textarea></p><p style="text-align: left">Загрузить картинку?<input type="file" name="photo"></p><p>Описание картинки:<br><textarea name="alt" rows="3" style="width: 100%"></textarea></p></fieldset>');
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
                
                $("#message").removeClass('error').removeClass('good');
                $("#message").addClass(data.status);
                $("#message").html(data.message);
                
                var pattern = /^(good)/i;
                if(pattern.test(data.status)) {
                    $form.find('textarea[name="text"]').val('');
                    $form.find('input[name="name"],input[name="email"]').addClass("readonly").attr('readonly', true).removeAttr('name');
                }
                
                $form.find('input[name="button"]').removeAttr("disabled");

            }, "json"
            );
        });
    }
    
    
    
})($);

function deleteItem(id) {
    $('#list-'+id).remove();
}

function ajaxRating(score, action, id) {
    var load = new Array('лох','жопа','сука','чмо','соси','геморрой','соси','cиськи','жопа')
    $('#rating_'+id).html(load[randomNumber(0, load.length-1)]);
    $.ajax({
        url: '/Service?q=rating&score='+score+'&action='+action+'&id='+id,
        dataType:'html',
        type: 'GET',
        timeout: 5000,
        cache: false,
        success: function(response,status) {
            if(status=="success") {
                $('#rating_'+id).html(response);
            }
        },
        error: function() {
            $('#rating_'+id).html("Упс! Что то сломалось. Уже чиним.");
        }
    });
}

// случайное число
function randomNumber(m, n) {
    m = parseInt(m);
    n = parseInt(n);
    return Math.floor(Math.random() * (n - m + 1)) + m;
}