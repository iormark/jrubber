$(document).ready(function() {


    // Консоль
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

    // Счетчик всех выбранных файлов и их размера
    var imgCount = 0;
    var imgSize = 0;
    var imgCountFast = 0;


    ////////////////////////////////////////////////////////////////////////////


    // Вывод в консоль
    function log(str) {
        $(console).html(str);
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

            $('<div/>').text(file.name).appendTo(li);
            img = $('<img/>').appendTo(li);
            $('<div/>').addClass('progress').attr('rel', '0').text('0%').appendTo(li);
            li.get(0).file = file;

            imgCountFast++;

            // Создаем объект FileReader и по завершении чтения файла, отображаем миниатюру и обновляем
            // инфу обо всех файлах
            var reader = new FileReader();
            reader.onload = (function(aImg) {
                return function(e) {
                    aImg.attr('src', e.target.result);
                    aImg.attr('width', 150);
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


    // Обаботка события нажатия на кнопку "Загрузить". Проходим по всем миниатюрам из списка,
    // читаем у каждой свойство file (добавленное при создании) и начинаем загрузку, создавая
    // экземпляры объекта uploaderObject. По мере загрузки, обновляем показания progress bar,
    // через обработчик onprogress, по завершении выводим информацию
    $("#upload-all").click(function() {
        uploaderFile();
    });

    function uploaderFile() {

        imgList.find('li').each(function() {

            var uploadItem = this;
            var pBar = $(uploadItem).find('.progress');
            log('Начинаем загрузку...');

            if (!uploadItem.file) {

                $.post("/FileUpload", {
                    q: "article",
                    name: $("#add input[name='name']").val(),
                    email: $("#add input[name='email']").val(),
                    title: $("#add input[name='title']").val(),
                    text: $(uploadItem).find('textarea').val()
                },
                function(response) {
                    if (response.status === "ok") {
                        log(response.message);
                    } else {
                        log(response.message);
                    }
                }, "json").fail(function() {
                    log("Произошла ошибка! Пожалуйста, повторите попытку, \nпростите...");
                });
                return false;
            } else {

                new uploaderObject({
                    id: params.id,
                    text: $(uploadItem).find('textarea').val(),
                    fieldText: 'text',
                    file: uploadItem.file,
                    url: '/FileUpload',
                    fieldName: 'file',
                    onprogress: function(percents) {
                        updateProgress(pBar, percents);
                    },
                    oncomplete: function(done, data) {
                        if (done) {
                            updateProgress(pBar, 100);
                            log('Загружен:<br/>*****<br/>' + data + '<br/>*****');
                        } else {
                            log('Ошибка:<br/>' + this.lastError.text);
                        }
                    }
                });
            }
        });

    }


    // Проверка поддержки File API в браузере
    if (window.FileReader == null) {
        log('Ваш браузер не поддерживает File API!');
    }


});