$(document).ready(function() {


    // Консоль
    var logout = $("#add__logout");
    var console = $("#console");

    // Инфа о выбранных файлах
    var countInfo = $("#info-count");
    var sizeInfo = $("#info-size");

    // Стандарный input для файлов
    var fileInput = $('#file-field');

    // ul-список, содержащий миниатюрки выбранных файлов
    var imgList = $('ul#img-list');

    // Контейнер, куда можно помещать файлы методом drag and drop
    var dropBox = $('#img-container');

    var autocomplete = $('#filed__autocomplete');

    // Счетчик всех выбранных файлов и их размера
    var imgCount = 0;
    var downCount = 0;
    var imgSize = 0;
    var imgCountFast = 0;


    ////////////////////////////////////////////////////////////////////////////


    // Вывод в консоль
    function log(str) {
        //$(logout).add('<ul/>').addClass('error').html(str);
        $('<div/>').html(str).prependTo(console);

    }

    // Вывод инфы о выбранных
    function updateInfo() {
        countInfo.text((imgCount == 0) ? 'Изображений не выбрано' : ('Изображений выбрано: ' + imgCount));
        sizeInfo.text(Math.round(imgSize / 1024));
    }

    // Обновление progress bar'а
    function updateProgress(bar, value) {
        var width = bar.width();
        var bgrValue = -width + (value * (width / 100));
        bar.attr('rel', value).css('background-position', bgrValue + 'px center').text(value + '%');
    }

    function updateDown() {
        downCount = $(imgList.find('li.down')).length;
    }



    // Отображение выбраных файлов и создание миниатюр
    function displayFiles(files) {
        var imageType = /image.*/;
        var num = 0;

        $.each(files, function(i, file) {

            // Отсеиваем не картинки
            if (!file.type.match(imageType)) {
                log('Файл отсеян: `' + file.name + '` (тип ' + file.type + ')');
                return true;
            }

            num++;



            // Создаем элемент li и помещаем в него название, миниатюру и progress bar,
            // а также создаем ему свойство file, куда помещаем объект File (при загрузке понадобится)
            var img;
            var li;
            if (imgCountFast == 0) {
                li = imgList.find("li").eq(0);
            } else {
                li = $('<li/>').appendTo(imgList);
                $('<textarea/>').addClass('text').attr('name', 'text').appendTo(li);
            }

            $('<input/>').attr('type', 'hidden').attr('name', 'sort').attr('value', imgCountFast).appendTo(li);
            $('<div/>').text(file.name).appendTo(li);
            img = $('<img/>').appendTo(li);
            $('<div/>').addClass('progress').attr('rel', '0').text('0%').appendTo(li);
            $('<div/>').appendTo(li).append('<span>Не добавлять</span>').
                    addClass('close').
                    click(function() {
                $(li).remove();
                imgCount--;
                imgSize -= file.size;
                updateInfo();
            });
            $('<div/>').addClass('out').appendTo(li);
            li.get(0).file = file;

            imgCountFast++;

            // Создаем объект FileReader и по завершении чтения файла, отображаем миниатюру и обновляем
            // инфу обо всех файлах
            var reader = new FileReader();
            reader.onload = (function(aImg) {
                return function(e) {
                    aImg.attr('src', e.target.result);
                    aImg.css('width', 200);
                    log('Картинка добавлена: `' + file.name + '` (' + Math.round(file.size / 1024) + ' Кб)');
                    imgCount++;
                    imgSize += file.size;
                    updateInfo();
                };
            })(img);

            reader.readAsDataURL(file);
        });
    }


    ////////////////////////////////////////////////////////////////////////////


    // Обработка события выбора файлов через стандартный input
    // (при вызове обработчика в свойстве files элемента input содержится объект FileList,
    //  содержащий выбранные файлы)
    fileInput.bind({
        change: function() {
            log(this.files.length + " файл(ов) выбрано через поле выбора");
            displayFiles(this.files);
        }
    });


    // Обработка событий drag and drop при перетаскивании файлов на элемент dropBox
    // (когда файлы бросят на принимающий элемент событию drop передается объект Event,
    //  который содержит информацию о файлах в свойстве dataTransfer.files. В jQuery "оригинал"
    //  объекта-события передается в св-ве originalEvent)
    dropBox.bind({
        dragenter: function() {
            $(this).addClass('highlighted');
            return false;
        },
        dragover: function() {
            return false;
        },
        dragleave: function() {
            $(this).removeClass('highlighted');
            return false;
        },
        drop: function(e) {
            var dt = e.originalEvent.dataTransfer;
            log(dt.files.length + " файл(ов) выбрано через drag'n'drop");
            displayFiles(dt.files);
            return false;
        }
    });


    $("#upload-all").click(function() {
        //$('#upload-all').attr('disabled', 'disabled');
        log('Проверяем');

        $.post("/svc/FileUpload2", {
            q: "header",
            name: $("#add__post input[name='name']").val(),
            email: $("#add__post input[name='email']").val(),
            title: $("#add__post input[name='title']").val(),
            tags: $("#add__post input[name='tags']").val()
        },
        function(response) {
            if (response.status == 'ok') {
                uploaderFile(response);
            } else {
                //$('#upload-all').removeAttr('disabled');
                log(response.message);
            }
        }, "json").fail(function() {
            $('#upload-all').removeAttr('disabled');
            log("Произошла ошибка! Пожалуйста, повторите попытку...");
        });
    });

    function uploaderFile() {

        imgList.find('li').each(function() {

            var uploadItem = this;
            var pBar = $(uploadItem).find('.progress');

            if (!uploadItem.file) {

                $.post("/svc/FileUpload2", {
                    q: "article",
                    name: $("#add__post input[name='name']").val(),
                    email: $("#add__post input[name='email']").val(),
                    title: $("#add__post input[name='title']").val(),
                    tags: $("#add__post input[name='tags']").val(),
                    text: $(uploadItem).find('textarea').val(),
                    video: $("#add__post input[name='video']").val(),
                    key: $("#add__post input[name='key']").val()
                },
                function(response) {
                    if (response.status == 'ok') {
                        log(response.message);
                        messageEstablishment();
                    } else {
                        //$('#upload-all').removeAttr('disabled');
                        log(response.message);
                    }
                }, "json").fail(function() {
                    $('#upload-all').removeAttr('disabled');
                    log("Произошла ошибка! Пожалуйста, повторите попытку, \nпростите...");
                });
                return false;
            } else {

                log('Загружаем...');
                var create = createPost();
                if (!create)
                    return false;

                // Отсеиваем загруженное
                if ($(uploadItem).hasClass('down')) {
                    log('Файл отсеян: `' + uploadItem.file.name + '` (тип ' + uploadItem.file.type + ')');
                    return true;
                }

                //alert("file");
                new uploaderObject({
                    name: $("#add__post input[name='name']").val(),
                    email: $("#add__post input[name='email']").val(),
                    title: $("#add__post input[name='title']").val(),
                    tags: $("#add__post input[name='tags']").val(),
                    key: $("#add__post input[name='key']").val(),
                    text: $(uploadItem).find('textarea').val(),
                    video: $("#add__post input[name='video']").val(),
                    sort: $(uploadItem).find('input[name="sort"]').val(),
                    file: uploadItem.file,
                    url: '/svc/FileUpload2',
                    onprogress: function(percents) {
                        updateProgress(pBar, percents);
                    },
                    oncomplete: function(done, data) {
                        if (done) {
                            updateProgress(pBar, 100);

                            $(logout).remove('.error');

                            if (data.status == 'ok') {
                                $(uploadItem).addClass('down');
                            } else if (data.status == 'error') {
                                //$(uploadItem).addClass('down__error');
                                updateProgress(pBar, 0);
                                //$('<ul/>').html(data.message).addClass('error').appendTo($(uploadItem).find('.out'));
                                log('Ошибка: `' + uploadItem.file.name + '` (' + data.message + ')');
                                //$('#upload-all').removeAttr('disabled');
                            }

                            var create = createPost();
                            if (!create)
                                return false;

                        } else {
                            $('#upload-all').removeAttr('disabled');
                            log('Ошибка: ' + this.lastError.text);
                        }
                    }
                });

            }
        });
    }


    function createPost() {
        updateDown();
        if (downCount == imgCount) {
            log('Проверяем...');

            $.post("/svc/FileUpload2", {
                q: "create",
                name: $("#add__post input[name='name']").val(),
                email: $("#add__post input[name='email']").val(),
                title: $("#add__post input[name='title']").val(),
                video: $("#add__post input[name='video']").val(),
                tags: $("#add__post input[name='tags']").val(),
                key: $("#add__post input[name='key']").val()
            },
            function(response) {
                if (response.status == "ok") {
                    log(response.message);
                    messageEstablishment();
                } else {
                    log(response.message);
                }
            }, "json").fail(function() {
                $('#upload-all').removeAttr('disabled');
                log("Произошла ошибка! Пожалуйста, повторите попытку, \nпростите...");
            });


            return false;
        } else {
            return true;
        }
    }

    function messageEstablishment() {
        var obj = $('#add__post');
        obj.html('');
        $('<h1/>').text('Спасибо, пост отправлен').appendTo(obj);
        $('<div/>').html('<a href="/">Ваша новость в разделе новое.</a>').appendTo(obj);

        $('body,html').animate({
            scrollTop: 0
        }, 0);
    }



    // Проверка поддержки File API в браузере
    if (window.FileReader == null) {
        log('Ваш браузер не поддерживает File API!');
    }

    $("#filed__tags").keyup(function() {
        $.getJSON(('/svc/FileUpload2?q=autocomplete&tags=' + $(this).val()), function(data) {
            autocomplete.html('');
            $.each(data[1], function(key, val) {
                $('<div/>').html(val).appendTo(autocomplete).click(function() {
                    setTag($(this).find('.tag').text());
                });
            });
        }).fail(function() {

        })
    });

    function setTag(tag) {
        var tagArray = $("#filed__tags").val().split(',');
        tagArray[tagArray.length - 1] = ' ' + tag;
        tag = unique(tagArray).toString() + ', ';
        $("#filed__tags").val(tag);
    }

    function unique(arr) {
        var obj = {};
        for (var i = 0; i < arr.length; i++) {
            var str = arr[i];
            obj[str] = true;
        }
        return Object.keys(obj);
    }


});

