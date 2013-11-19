(function($) {
    var reader;
    //var progress = document.querySelector('.percent');
    FileApi = {
        error: function(e) {
            switch (e.target.error.code) {
                case e.target.error.NOT_FOUND_ERR:
                    alert('File Not Found!');
                    break;
                case e.target.error.NOT_READABLE_ERR:
                    alert('File is not readable');
                    break;
                case e.target.error.ABORT_ERR:
                    break; // noop
                default:
                    alert('An error occurred reading this file.');
            }
        },
        progress: function(e) {
            alert('progress');
            /*if (e.lengthComputable) {
             var percentLoaded = Math.round((e.loaded / e.total) * 100);
             if (percentLoaded < 100) {
             progress.style.width = percentLoaded + '%';
             progress.textContent = percentLoaded + '%';
             }
             }*/
        },
        select: function(params) {

            var xhr = new XMLHttpRequest();
            this.ctrl = createThrobber(img);
            reader = new FileReader();
            xhr.upload.addEventListener("progress", function(e) {
                if (e.lengthComputable) {
                    var percentage = Math.round((e.loaded * 100) / e.total);
                    this.ctrl.update(percentage);
                }
            }, false);



            reader.open("POST", params.url);

            var f = new FormData();
            f.append("text", params.text);
            f.append("file", params.file);


            reader.send(f);
        }
    }
})($);