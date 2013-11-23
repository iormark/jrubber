$(document).ready(function() {
    // Info
    var fieldInfo = $("#add__info");
    // Консоль
    var console = $("#console");

    // ul-список, содержащий миниатюрки выбранных файлов
    var fileList = $('#file_list');

    //Adding a new field
    var fieldAddText = $('#filed__add--text');
    var fieldAddImage = $('#filed__add--image');
    var fieldAddVideo = $('#filed__add--video');

    //Quick tips tag
    var autocomplete = $('#filed__autocomplete');


    // Status change form
    var create = false;


    var fileCount = 0;
    var itemCount = fileList.find('li').length;
    var updateCount = 0;

    // Buttom upload disabled
    $('#upload').attr('disabled', 'disabled');

    // Drag events
    $(fileList).sortable({
        zIndex: 999,
        axis: 'y',
        cursor: 'move',
        opacity: 1,
        tolerance: 'intersect',
        update: function() {
            updateInfo();
        }
    });
    //$(fileList).disableSelection();


    //var drop = new Draggable(fileList.find('li'));
    //fileList.find('li').each(function() {
    //  drop.handleAdd(this);
    //});

    // Info
    info();
    function info() {
        $(fieldInfo).html('<span>Всего: ' + itemCount + '</span>');
    }
    
    function updateInfo() {
        var i = 0;
        fileList.find('li').each(function() {
            $(this).find('input[name="sort"]').val(i++);
        });
        fileList.find('li').removeClass('read').addClass('update');
        create = true;
        $('#upload').removeAttr('disabled');
    }


    // Вывод в консоль
    function log(str) {
        //$(logout).add('<ul/>').addClass('error').html(str);
        $('<div/>').html(str).prependTo(console);
    }

    // Progress item bar
    function Progress(obj, percents) {
        $(obj).find('.percent').css('width', percents + '%').text(percents + '%');
    }

    $(window).unload(function() {
        if (create) {
            alert("Вы не сохранили изменения! Уверенны что хотите покинуть эту страницу?");
        }
    });


    $(document).on('change', '.file_input', function(e) {
        //alert($(this).parent().html())
        displayFiles(this);
    });


    $(document).on('change', '#file_list textarea, #file_list input[name="video"], #file_list input[name="sort"]', function(e) {
        var parent = $(this).parent();
        if ($(this).val().trim() === '' && $(this).attr('name') !== 'text') {
            parent.removeClass('update').addClass('read');
        } else {
            parent.removeClass('read').addClass('update');
        }
        $('#upload').removeAttr('disabled');
    });
    $(document).on('change', '#add__post input[name="title"], #add__post input[name="tags"]', function(e) {
        if ($('#add__post input[name="post"]').val() > 0) {
            create = true;
            $('#upload').removeAttr('disabled');
        }
    });

    // Adding a new field

    fieldAddText.click(function() {
        var li = $('<li/>').appendTo(fileList);
        $('<textarea/>').addClass('text').attr('name', 'text').appendTo(li).autosize();
        $('<input/>').attr('type', 'hidden').attr('name', 'id').attr('value', 0).appendTo(li);
        $('<input/>').attr('type', 'hidden').attr('name', 'sort').attr('value', itemCount).appendTo(li);
        itemCount++;
        info();
    });
    fieldAddImage.click(function() {
        var li = $('<li/>').appendTo(fileList);
        $('<input/>').attr('type', 'file').attr('class', 'file_input').attr('multiple', '').appendTo(li);
        $('<input/>').attr('type', 'hidden').attr('name', 'id').attr('value', 0).appendTo(li);
        $('<input/>').attr('type', 'hidden').attr('name', 'sort').attr('value', itemCount).appendTo(li);
        itemCount++;
        info();
    });
    fieldAddVideo.click(function() {
        var li = $('<li/>').appendTo(fileList);
        $('<div>').html('Видео (Coub): URL (без http\'s), пример: <code>www.youtube.com/video...</code>').appendTo(li);
        $('<input/>').attr('type', 'text').attr('name', 'video').attr('value', '').attr('id', 'filed__video').appendTo(li);
        $('<input/>').attr('type', 'hidden').attr('name', 'id').attr('value', 0).appendTo(li);
        $('<input/>').attr('type', 'hidden').attr('name', 'sort').attr('value', itemCount).appendTo(li);
        //$(this).html('&minus; Удалить видео');
        //$(this).attr('disabled', 'disabled');
        itemCount++;
        info();
    });

    $(document).on('click', '.item-delete', function(e) {
        if (!confirm("Это действие невозможно будет отменить! Вы уверены что хотите удалить этот элемент?")) {
            return;
        }

        var item = $(this).parent();

        // Delete item
        $.post('/svc/FileUpload', {
            url: '/svc/FileUpload',
            q: 'item-delete',
            item: $(item).find('input[name="id"]').val(),
        }, function(response) {
            if (response.status === 'ok' && response.post >= 0) {
                $(item).remove();
                itemCount--;
                if ($(item).hasClass('update')) {
                    updateCount--;
                }
                info();
                updateInfo();
            } else {
                alert(response.message);
            }
        }, "json").fail(function() {
            log("Произошла ошибка при удалении! Пожалуйста, повторите попытку, \nпростите...");
        });
    });


    function displayFiles(obj) {

        var fileType = /image.*/;
        var num = 0;

        $.each(obj.files, function(i, file) {

            // Отсеиваем не картинки
            if (!file.type.match(fileType)) {
                log('Файл отсеян: `' + file.name + '` (тип ' + file.type + ')');
                return true;
            }

            var li;
            var theFile;
            if (num == 0) {
                li = $(obj).parent();
                li.removeClass('read').addClass('update').addClass('file');
                theFile = li.find('img');

                if ($(theFile).length == 0) {
                    $('<div/>').addClass('file_name').text(file.name).appendTo(li);
                    theFile = $('<img/>').appendTo(li);
                    var progress = $('<div/>').addClass('progress').appendTo(li);
                    $('<div/>').addClass('percent').css('width', 0).text('0%').appendTo(progress);
                } else {
                    li.find('.file_name').text(file.name);
                    li.find('.percent').css('width', 0).text('0%');
                }


            } else {
                li = $('<li/>').addClass('file').appendTo(fileList);
                $('<input/>').attr('type', 'file').attr('class', 'file_input').attr('multiple', '').appendTo(li);
                $('<div/>').text(file.name).appendTo(li);
                theFile = $('<img/>').appendTo(li);
                $('<input/>').attr('type', 'hidden').attr('name', 'id').attr('value', 0).appendTo(li);
                $('<input/>').attr('type', 'hidden').attr('name', 'sort').attr('value', itemCount).appendTo(li);
                var progress = $('<div/>').addClass('progress').appendTo(li);
                $('<div/>').addClass('percent').css('width', 0).text('0%').appendTo(progress);
                itemCount++;
            }

            li.get(0).file = file;

            num++;
            fileCount++;


            var reader = new FileReader();
            reader.onload = (function(theFile) {
                return function(e) {
                    theFile.attr('src', e.target.result);
                    theFile.css('width', 150);
                    log('Картинка добавлена: `' + file.name + '` (' + Math.round(file.size / 1024) + ' Кб)');
                    $('#upload').removeAttr('disabled');
                    info();
                };
            })(theFile);
            reader.readAsDataURL(file);
        });
    }


    $("#upload").click(function() {
        log('Секундочку...');
        $('#upload').attr('disabled', 'disabled');

        if (create) {
            createPost();
        }


        fileList.find('li').each(function() {
            var item = this;
            // Keeps out of the loaded
            if ($(item).hasClass('read')) {
                $('#upload').removeAttr('disabled');
                return true;
            }

            var id = $(item).find('input[name="id"]');
            var isFile = null;
            if (typeof(item.file) != 'undefined') {
                isFile = item.file;
            } else if ($(item).find('.percent').text() == '100%') {
                isFile = true;
            } else {
                isFile = false;
            }

            // Create or overwrite item
            var params = {
                url: '/svc/FileUpload',
                q: 'item',
                title: $("#add__post input[name='title']").val(),
                tags: $("#add__post input[name='tags']").val(),
                key: $("#add__post input[name='key']").val(),
                text: typeof($(item).find('textarea').val()) != 'undefined' ? $(item).find('textarea').val() : null,
                video: (typeof($(item).find('input[name="video"]').val()) != 'undefined' ? $(item).find('input[name="video"]').val() : null),
                sort: $(item).find('input[name="sort"]').val(),
                file: isFile,
                item: (typeof(id.val()) != 'undefined' ? id.val() : null),
                post: $("#add__post input[name='post']").val()
            };

            //alert(JSON.stringify(params));
            new Uploads(params, {
                onprogress: function(percents) {
                    //log(percents);
                    Progress(item, percents)
                },
                oncomplete: function(response) {

                    if (response.status === 'ok' && response.item >= 0) {
                        updateCount++;
                        $(item).removeClass('update').addClass('read');
                        $(id).val(response.item);
                        $(item).find('.error').remove();
                        createPost();
                    } else {
                        if ($(item).find('.percent').length > 0) {
                            $(item).find('.percent').css('width', 0).text('0%');
                        }

                        if ($(item).find('.error').length === 0) {
                            $('<div/>').addClass('error').html(response.message).appendTo(item);
                        } else {
                            $(item).find('.error').html(response.message);
                        }

                        $('#upload').removeAttr('disabled');
                    }
                }
            });
        });

    });


    function createPost() {
        //alert((((itemCount - updateCount) + updateCount === itemCount) || create));
        if (!fileList.find('li').hasClass('update') &&
                (((itemCount - updateCount) + updateCount === itemCount) || create)) {

            log('Сохраняем...');
            $('#upload').attr('disabled', 'disabled');

            // Create or overwrite post
            $.post('/svc/FileUpload', {
                url: '/svc/FileUpload',
                q: 'create',
                title: $("#add__post input[name='title']").val(),
                tags: $("#add__post input[name='tags']").val(),
                key: $("#add__post input[name='key']").val(),
                post: $("#add__post input[name='post']").val()
            },
            function(response) {

                if (response.status === 'ok' && response.post >= 0) {
                    $("#add__post input[name='post']").val(response.post);
                    create = false;
                    location.href = '/post?id=' + response.post;
                    //location.href = '/add?id=' + response.post;
                } else {
                    $('#upload').removeAttr('disabled');
                    alert(response.message);
                }
            }, "json").fail(function() {
                $('#upload').removeAttr('disabled');
                log("Произошла ошибка! Пожалуйста, повторите попытку, \nпростите...");
            });
        } else {
            $('#upload').removeAttr('disabled');
        }
    }




    function Uploads(params, callback) {
        var percents = 0;
        var xhr = new XMLHttpRequest();

        xhr.upload.addEventListener("progress", function(e) {
            if (e.lengthComputable) {
                percents = Math.round((e.loaded / e.total) * 100);
                if (callback.onprogress instanceof Function) {
                    callback.onprogress(percents);
                }
            }
        }, false);

        xhr.upload.addEventListener("load", function(e) {
            percents = 100;
        }, false);

        xhr.upload.addEventListener("error", function() {
            alert('Error uploading on server');
        }, false);

        xhr.onreadystatechange = function() {
            var callbackDefined = callback.oncomplete instanceof Function;
            if (this.readyState == 4) {
                if (this.status == 200) {
                    if (callbackDefined) {
                        //alert(this.responseText);
                        callback.oncomplete(JSON.parse(this.responseText));
                    }
                } else {
                    alert('HTTP response code is not OK (' + this.status + ')');
                    if (callbackDefined) {
                        callback.oncomplete(false);
                    }
                }
            }
        };

        xhr.open("POST", params.url);
        var f = new FormData();
        for (var key in params) {
            f.append(key, params[key]);
        }

        xhr.send(f);
    }


    $("#filed__tags").keyup(function() {
        $.getJSON(('/svc/FileUpload?q=autocomplete&tags=' + $(this).val()), function(data) {
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
  