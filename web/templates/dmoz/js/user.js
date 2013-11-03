(function($) {
    $(document).ready(function() {

        $('.user__block .menu a, #user__password').click(function(e) {
            var anchor = $(this).attr('href').replace("#", "");
            $('.user__block .user__forms').hide();
            $('.user__block .user__' + anchor).show();
            $('.user__block .menu li').removeClass('active').addClass('noactive');
            $(this).parent('li').addClass('active').removeClass('noactive');
            e.preventDefault();
        });


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
            Edit.reply_selected('comment', $(this).attr('value'));
        });


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
                form.find('input[type="submit"]').removeAttr("disabled");
            });

            request.fail(function() {
                log('Простите пожалуйста, произошел сбой :(', form.find(".message"));
            });

        },
        create: function(action) {
            var form = $('#' + action + '__add');
            var get = $('#' + action + '__get');
            var fields = {};
            var x = new Date();


            form.find('.form__edit').find('select, input, textarea').
                    each(function(i, field) {
                fields[field.name] = field.value;
            });

            fields['timezone'] = (-x.getTimezoneOffset() / 60).toFixed(2);
            fields['dataType'] = 'html';

            var login = form.find('.reply_to a').text();
            var text = $.trim(fields['text']);


            if (text == '' || text == login + ',') {
                form.find('textarea[name="text"]').focus();
                return false;
            } else if (text.length > 2000) {
                log('Длина комментария 2000 символов максимум.', form.find(".message"));
                form.find('textarea[name="text"]').focus();
                return false;
            }

            var request = $.ajax({
                type: 'post',
                url: '/svc/edit?q=' + action,
                data: fields,
                dataType: 'html',
            });

            request.done(function(data) {
                get.append(data);
                form.find('textarea[name="text"]').val('').height(38);
                form.find('.reply_to').text('');
            });

            request.fail(function() {
                log('Простите пожалуйста, произошел сбой :(', form.find(".message"));
            });

        },
        reply: function(action, id) {
            var form = $('#' + action + '_' + id);
            var add = $('#' + action + '__add');


            var login = form.find('.login a').text();
            var textarea = add.find('textarea[name="text"]');
            if (textarea.val() == '') {
                textarea.focus().val(login + ', ');
            }

            add.find('input[name="button"]').val('Ответ для');
            add.find('.reply_to').html('<a href="/' + login + '" class="reply_selected__button" value="' + id + '">' + login + '</a>');
            $('<input/>').attr('type', 'hidden').attr('name', 'parent').attr('value', id).appendTo(add.find('.form__edit'));

            $('body,html').animate({
                scrollTop: add.offset().top - ($(window).height() / 2)
            }, 100);

        },
        reply_selected: function(action, id) {

            var form = $('#' + action + '_' + id);
            form.css('opacity', '0')
            $(form).animate({opacity: "1"}, 500);

        }
    }
})($);