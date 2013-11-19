




function Draggable(cols) {
    var dragSrcEl;

    this.handleDragStart = function(e) {
        this.style.opacity = '0.9';
        e.dataTransfer.effectAllowed = 'move';
        e.dataTransfer.setData('text/html', this.innerHTML);
        dragSrcEl = this;
        this.classList.add('moving');
    }
    this.handleDragOver = function(e) {
        
        if (e.preventDefault) {
            e.preventDefault();
        }

        e.dataTransfer.dropEffect = 'move';
        return false;
    }
    this.handleDragEnter = function(e) {
        this.classList.add('over');
    }
    this.handleDragLeave = function(e) {
        this.classList.remove('over');
    }
    this.handleDrop = function(e) {

        if (e.stopPropagation) {
            e.stopPropagation();
        }

        if (dragSrcEl != this) {
            dragSrcEl.innerHTML = this.innerHTML;
            this.innerHTML = e.dataTransfer.getData('text/html');
        }

        return false;
    }
    this.handleDragEnd = function(e) {
        this.style.opacity = '1';
        var i = 0;
        [].forEach.call(cols, function(col) {
            col.classList.remove('over');
            col.classList.remove('moving');
            col.classList.remove('read');
            col.classList.add('update');
            col.querySelector('[name="sort"]').value = i++;
        });
    }
    this.handleAdd = function(col) {
        col.setAttribute('draggable', 'true');
        col.addEventListener('dragstart', this.handleDragStart, false);
        col.addEventListener('dragenter', this.handleDragEnter, false);
        col.addEventListener('dragover', this.handleDragOver, false);
        col.addEventListener('dragleave', this.handleDragLeave, false);
        col.addEventListener('drop', this.handleDrop, false);
        col.addEventListener('dragend', this.handleDragEnd, false);
    }
}