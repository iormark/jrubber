$(document).ready(function() {

    var draggable = $('.draggable [draggable=true]');

    draggable.each(function() {
        $(this).on('dragstart', handleDragStart);
        $(this).on('dragenter', handleDragEnter);
        $(this).on('dragover', handleDragOver);
        $(this).on('dragleave', handleDragLeave);
        $(this).on('drop', handleDrop);
        $(this).on('dragend', handleDragEnd);
    });

    var dragSrcEl = null;

    function handleDragStart(e) {
        $(this).css('opacity', .4);

        dragSrcEl = this;

        e.dataTransfer.effectAllowed = 'move';
        e.dataTransfer.setData('text/html', $(this).html());
    }

    function handleDragOver(e) {
        if (e.preventDefault) {
            e.preventDefault();
        }

        e.dataTransfer.dropEffect = 'move';

        return false;
    }

    function handleDragEnter(e) {
        $(this).addClass('over');
    }

    function handleDragLeave(e) {
        $(this).removeClass('over');
    }

    function handleDrop(e) {
        if (e.stopPropagation) {
            e.stopPropagation(); 
        }

        if (dragSrcEl != this) {
            
            
            $(dragSrcEl).html($(this).html());
            alert(e.dataTransfer.getData('text/html'));
            $(this).html(e.dataTransfer.getData('text/html'));
        }

        return false;
    }

    function handleDragEnd(e) {
        draggable.each(function() {
            $(this).removeClass('over');
        });
    }

});