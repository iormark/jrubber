(function($) {
    $(document).ready(function() {

        $('.user__login').submit(function(e) {

        });


        $('.user__forms form').submit(function(e) {

            e.preventDefault();

            var x = new Date();

            var $form = $(this),
                    login = $form.find('input[name="login"]').val(),
                    email = $form.find('input[name="email"]').val(),
                    password = $form.find('input[name="password"]').val(),
                    timezone = (-x.getTimezoneOffset() / 60).toFixed(2),
                    url = $form.attr('action');

            $form.find('input[name="submit"]').attr("disabled", "disabled");

            $.post(url, {
                login: login,
                email: email,
                password: password,
                timezone: timezone
            },
            function(data) {

                $(".message").hide();
                var pattern = /^(good)/i;

                if (/^(good)/i.test(data.status)) {

                    $form.find(".message").fadeIn(500).html(data.message);

                } else if (/^(redirect)/i.test(data.status)) {

                    window.location.href = "/user/" + data.message;

                } else {

                    $form.find(".message").fadeIn(500).html(data.message);

                }

            }, "json").fail(function() {
                $(".message").hide();
                $form.find(".message").fadeIn(500).html("Простите пожалуйста, произошел сбой :(");
            });

            $form.find('input[name="submit"]').removeAttr("disabled");

        });
    });
})($);