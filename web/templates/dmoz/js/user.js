(function($) {
    $.fn.multipart = function(options) {
        var form = this;
        var fileList = $('#edit__file_list');
        var fileInput = form.find('input[type="file"]');
        var submit = form.find('input[type="submit"]')
        var message = form.find(".message");
        var fileSize = 0;

        $(fileInput).change(function(e) {
            var fileType = /image.*/;

            $.each(this.files, function(i, file) {

                // Отсеиваем не картинки
                if (!file.type.match(fileType)) {
                    alert('Файл отсеян: `' + file.name + '` (тип ' + file.type + ')');
                    return true;
                }

                var li = fileList.addClass('file');
                var theFile = $('<img/>');
                li.html(theFile);
                li.get(0).file = file;

                var reader = new FileReader();
                reader.onload = (function(theFile) {
                    return function(e) {
                        theFile.attr('src', e.target.result);
                        theFile.css('width', 100);
                        fileSize += file.size;
                        
                        if ((file.size / (1024*1024)) > 5) {
                            alert("Ваш файл превышает допустимый размер 5 мб.");
                        }
                    };
                })(theFile);
                reader.readAsDataURL(file);

            });
        });

        $(form).submit(function(e) {
            e.preventDefault();
            var fields = {};
            submit.attr("disabled", "disabled");

            fields['url'] = '/svc/EditProfile';
            form.find('select, input, textarea').each(function(i, field) {
                fields[field.name] = field.value;
            });

            fields['file'] = fileList.get(0).file;

            new Uploads(fields, {
                onprogress: function(percents) {

                },
                oncomplete: function(response) {

                    message.hide();
                    if (/^(ok)/i.test(response.status)) {
                        message.fadeIn(500).html(response.message);
                    } else if (/^(redirect)/i.test(response.status)) {
                        window.location.href = response.message;
                    } else {
                        message.fadeIn(500).html(response.message);
                    }
                    submit.removeAttr("disabled");
                }
            });
        });
    }



    $(document).ready(function() {

        $('.user__block .menu a, #user__password').click(function(e) {
            var anchor = $(this).attr('href').replace("#", "");
            $('.user__block .user__forms').hide();
            $('.user__block .user__' + anchor).show();
            $('.user__block .menu li').removeClass('active').addClass('noactive');
            $(this).parent('li').addClass('active').removeClass('noactive');
            e.preventDefault();
        });


        /**
         * Standard form submission
         */
        $('form.wagon').submit(function(e) {
            e.preventDefault();

            var fields = {};
            var form = $(this);
            var x = new Date();

            form.find('input[type="submit"]').attr("disabled", "disabled");

            form.find('select, input, textarea').each(function(i, field) {
                fields[field.name] = field.value;
            });

            fields['timezone'] = (-x.getTimezoneOffset() / 60).toFixed(2);

            var request = $.ajax({
                type: form.attr('method'),
                url: form.attr('action'),
                data: fields,
                dataType: "json",
            });

            request.done(function(data) {

                form.find(".message").hide();
                if (/^(good)/i.test(data.status)) {

                    form.find(".message").fadeIn(500).html(data.message);

                } else if (/^(redirect)/i.test(data.status)) {

                    window.location.href = data.message;

                } else {

                    form.find(".message").fadeIn(500).html(data.message);

                }
                form.find('input[type="submit"]').removeAttr("disabled");
            });

            request.fail(function() {
                form.find(".message").hide();
                form.find(".message").fadeIn(500).html("Простите пожалуйста, произошел сбой :(");
                form.find('input[type="submit"]').removeAttr("disabled");
            });

        });


        $(document).on('click', '.reply__button', function(e) {
            Edit.reply('comment', $(this).attr('value'));
        });
        $(document).on('click', '.edit__button', function(e) {
            Edit.edit('comment', $(this).attr('value'));
        });
        $(document).on('click', '.delete__button', function(e) {
            Edit.delete('comment', $(this).attr('value'));
        });
        $(document).on('click', '.save__button', function(e) {
            Edit.save('comment', $(this).attr('value'));
        });
        $(document).on('click', '.cancel__button', function(e) {
            Edit.cancel('comment', $(this).attr('value'), true);
        });
        $(document).on('click', '.reply_selected__button', function(e) {
            e.preventDefault();
            Edit.anchor_selected(this);
            //Edit.reply_selected('comment', $(this).attr('value'));
        });

        Edit.anchor_selected(this);
    });


    function escapeHtml(unsafe) {
        return unsafe
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/\r\n|\r|\n/g, "<br>")
    }

    function uncapeHtml(safe) {
        return safe
                .replace(/<br>/g, "\r\n")
                .replace(/&lt;/g, "<")
                .replace(/&gt;/g, ">");
    }


    Edit = {
        /*
         * Епемся с комментариями.
         */

        edit: function(action, id) {
            var form = $('#' + action + '_' + id);
            var fv = form.find('.' + action + '__value');
            var fe = form.find('.' + action + '__edit');
            fe.show();
            var val = fv.find('.' + action + '__text').html();
            fe.find('textarea[name="text"]').val(uncapeHtml(val)).trigger('autosize.resize');
            $(fe.find('textarea[name="text"]')).autosize();
            fv.hide();
        },
        cancel: function(action, id, reset) {
            var form = $('#' + action + '_' + id);
            var fv = form.find('.' + action + '__value');
            var fe = form.find('.' + action + '__edit');
            if (!reset) {
                var val = fe.find('textarea[name="text"]').val();
                fv.find('.' + action + '__text').html(escapeHtml(val));
            }
            fv.show();
            fe.hide();
        },
        delete: function(action, id) {
            if (!confirm("Комментарий нельзя будет восстановить! Вы уверены?")) {
                return false;
            }

            var form = $('#' + action + '_' + id);

            var request = $.ajax({
                type: 'post',
                url: '/svc/edit?q=' + action + '_delete',
                data: {id: id},
                dataType: "json",
            });

            request.done(function(data) {

                if (/^(replace)/i.test(data.status)) {

                    form.html(data.message);

                } else {

                    form.find(".message").fadeIn(500).html(data.message);

                }
            });
        },
        save: function(action, id) {
            var form = $('#' + action + '_' + id);
            var fields = {};
            var x = new Date();

            form.find('input[type="button"]').attr("disabled", "disabled");

            form.find('.form__edit').find('select, input, textarea').
                    each(function(i, field) {
                fields[field.name] = field.value;
            });

            fields['timezone'] = (-x.getTimezoneOffset() / 60).toFixed(2);

            var request = $.ajax({
                type: 'post',
                url: '/svc/edit?q=' + action,
                data: fields,
                dataType: "json",
            });

            request.done(function(data) {

                form.find(".message").hide();
                if (/^(good)/i.test(data.status)) {

                    Edit.cancel(action, id, false);

                } else if (/^(redirect)/i.test(data.status)) {

                    window.location.href = data.message;

                } else {

                    form.find(".message").fadeIn(500).html(data.message);

                }
                form.find('input[type="button"]').removeAttr("disabled");
            });

            request.fail(function() {
                log('Простите пожалуйста, произошел сбой :(', form.find(".message"));
            });

        },
        create: function(action) {
            var form = $('#add-' + action + '');
            var get = $('#' + action + '__get');
            var fields = {};
            var x = new Date();

            form.find('input[type="button"]').attr("disabled", "disabled");

            form.find('.form__edit').find('select, input, textarea').
                    each(function(i, field) {
                fields[field.name] = field.value;
            });

            fields['timezone'] = (-x.getTimezoneOffset() / 60).toFixed(2);
            fields['dataType'] = 'html';

            var login = form.find('.reply_to a').text();
            var text = $.trim(fields['text']);


            if (text === '' || text === login + ',') {
                form.find('input[type="button"]').removeAttr("disabled");
                form.find('textarea[name="text"]').focus();
                return false;
            } else if (text.length > 2000) {
                log('Длина комментария 2000 символов максимум.', form.find(".message"));
                form.find('textarea[name="text"]').focus();
                form.find('input[type="button"]').removeAttr("disabled");
                return false;
            }

            var request = $.ajax({
                type: 'post',
                url: '/svc/edit?q=' + action,
                data: fields,
                dataType: 'html'
            });

            request.done(function(data) {
                get.append(data);
                form.find('textarea[name="text"]').val('').height(38);
                form.find('.reply_to').text('');
                form.find('input[type="button"]').removeAttr("disabled");
            });

            request.fail(function() {
                log('Простите пожалуйста, произошел сбой :(', form.find(".message"));
                form.find('input[type="button"]').removeAttr("disabled");
            });

        },
        reply: function(action, id) {
            var form = $('#' + action + '_' + id);
            var add = $('#add-' + action + '');


            var login = form.find('.users a.login').first().text();
            var textarea = add.find('textarea[name="text"]');
            if (textarea.val() === '') {
                textarea.focus().val(login + ', ');
            }

            add.find('input[name="button"]').val('Ответ для');
            add.find('.reply_to').html('<a href="#comment_' + id + '" class="reply_selected__button" value="' + id + '">' + login + '</a>');
            $('<input/>').attr('type', 'hidden').attr('name', 'parent').attr('value', id).appendTo(add.find('.form__edit'));

            $('body,html').animate({
                scrollTop: add.offset().top - ($(window).height() / 2)
            }, 100);

        },
        reply_selected: function(action, id) {

            var form = $('#' + action + '_' + id);
            form.css('opacity', '0');
            $(form).animate({opacity: "1"}, 500);

        },
        anchor_selected: function(obj) {

            if (typeof($(obj).attr('href')) !== 'undefined') {
                var url = (document.location.href).split('#');
                document.location.href = url[0] + '' + $(obj).attr('href');
            }

            var selected = $(window.location.hash);

            if (window.location.hash === '') {
                selected = $($(obj).attr('href'));
            }
            
            

            if (typeof(selected.offset()) !== 'undefined') {
                var height = $(window.location.hash).height();
                //alert(height);
                $('body,html').animate({
                    scrollTop: selected.offset().top - (($(window).height() / 2)-(height/2))
                }, 100);
            }
            
            var hash = (window.location.hash).split('_');

            if (hash[0] === '#comment' || hash[0] === '#high') {
                var selected = $(window.location.hash);
                selected.css('opacity', '.1');
                $(selected).animate({opacity: "1"}, 4000);

            }
        }
    }

})($);