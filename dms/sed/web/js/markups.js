
//переменные для определения координаты курсора
var mouseX, mouseY;

// координаты и размеры image (выставляются в getImgMetrics();)
var page_x, page_y, page_w, page_h;

// ID активной страницы. Ставится в show_page.cfg
var active_page_id;

// Массив ID страниц, отображенных в thumbnails. Заполняется в show_thumbnails.cfg
var page_list;

// Режим добавления маркапа - включен обработчик на #page_content
var addCommentMode = false;

/**
 * Отображение страницы с заданным номером
 * Ajax вызов модуля viewer/show_page, выделение соответствующего thumbnail-а
 * @param page_id - ID страницы, которую нужно показать
 */

var showPage = function (page_id) {
    active_page_id = page_id;
    AjaxCall("page_content", "c=viewer/show_page&active_page_id=" + page_id);

    $(".thumb_div").removeClass("active_page");
    $("#thmb_" + page_id).addClass("active_page");
    $("#dialog").hide();
};

// ============== Добавление / редактирование / удаление маркапа ==============
/**
 * Клик на Comment кнопке 
 */
var commentButtonClick = function () {

    var cookieHelper = getCookie("helperWindow");
    console.log("commentButtonClick..." + cookieHelper + "; addCommentMode=" + addCommentMode);
    //Заносим в диалоговое окно текст подсказки и показываем ее.
    // выполняется, если юзер не ставил галку "не показывать"
    // или при повторном клике на кнопку "добавить"
    if (!cookieHelper || cookieHelper == "false" || addCommentMode) {
        $("#popupCont").html($("#comment_help").html());
        $("#dialog_title").html("Добавление комментария");

        //Ставим координаты окна
        $("#dialog").css({"left": (mouseX - 350), "top": (mouseY + 40), "width": "400px"});
        //показываем диалоговое окно
        $("#dialog").show(200);
        if (cookieHelper == "true")
            $('#doNotShow').prop('checked', true);
    }
    setAddCommentMode(true);
};

/**
 * Установка-сброс режима ввода замечания
 * 
 * Ставит / снимает:
 * - обработчик mouse click на текст (#page_content => showNewMarkupForm();)
 * - флаг addCommentMode (true / false)
 * - спец. вид курсора на странице
 * 
 * @param {type} set - true - установка режима добавления замечания 
 */
var setAddCommentMode=function(set){
  $("#page_content").unbind();
  if(set) {
    $("#page_content").click(showNewMarkupForm);
    // Ставим курсор и флаг
    $("#content, .control").addClass("enter_mode");
    addCommentMode = true;   
  } else {
    $("#content, .control").removeClass("enter_mode");
    addCommentMode = false;
    showNewCommentIcon(false);
  }  
}

/**
 * Клик на Comment кнопке "Отмена" - просто убираем диалог 
 * сброс "тулбар disabled" и обработчика клика на странице текста
 */
var cancelButtonClick = function () {
    $("#dialog").hide();
    //сброс обработчика на странице текста
    $("#page_content").unbind();
    setAddCommentMode(false);
};


/**
 * Отображение формы для ввода нового комментария
 * В окно из шаблона #comment_edit_form заносится пустая форма для ввода комментария
 * Окно позиционируется около координат мыши
 */
var showNewMarkupForm = function () {

  // скрываем диалоговое окгно
    $("#dialog").hide();
    // определяем параметры изображения страницы
    getImgMetrics("main_page");
    // Отображаем форму ввода 
    $("#dialog_title").html("Добавление замечания");
    AjaxCall("popupCont", "c=viewer/markup_form", true);
    // ставим координаты окна
    $("#dialog").css({"left": (mouseX - 200), "top": (mouseY + 27), "width": 2 * page_w / 3});

    textAreaResize();
    showNewCommentIcon(true);
};

/**
 * 
 * @param {type} show
 */
var showNewCommentIcon = function (show) {
    if (show) {
        $("#mkp_icon_new").css({"left": mouseX, "top": (mouseY - 5)});
        $("#mkp_icon_new").show(300);
    } else
        $("#mkp_icon_new").hide();
}

/**
 * Показывает форму редактирования markup'а  
 * Форма содержит данные markup'а с кнопками "редактировать" "закрыть"
 * Окно позиционируется в координаты markup'а
 */
var showMarkupForm = function (markup_id, x_coordinate, y_coordinate, page_id) {

    getImgMetrics("main_page");
    if (page_id != active_page_id) {
        showPage(page_id);
    }

    $("#dialog_title").html("Просмотр замечания");
    $("#popupCont").html("<br><br><center>Загрузка...</center><br><br>");
    AjaxCall("popupCont", "c=viewer/markup_form&markup_id=" + markup_id, true);

    console.log("showMarkupForm: top=" + y_coordinate + "; left=" + x_coordinate);
    // Коррекция координат, если они пришли из списка (относительные)
    if (x_coordinate < 2 || y_coordinate < 2) {
        x_coordinate = x_coordinate * page_w;
        y_coordinate = y_coordinate * page_h;
    }
    // ставим координаты окна
    $("#dialog").css({"left": (page_x + x_coordinate-200), "top": (page_y + y_coordinate + 5), "width": 2 * page_w / 3});
};

function scrollToView(element){
    var offset = element.offset().top;
    var visible_area_start = $(window).scrollTop();
    var visible_area_end = visible_area_start + window.innerHeight;

    if( offset < visible_area_start || offset + 200 > visible_area_end) {
         // Not in view so scroll to it
         $('html,body').animate({scrollTop: offset - window.innerHeight/3}, 1000);
         return false;
    }
    return true;
}

/**
 * Клик на кнопке "Сохранить" формы ввода комментария
 * 
 */
var saveMarkup = function () {
    $("#dialog").hide();
    showNewCommentIcon(false);
    //Сброс "тулбар disabled"
    // $("#toolbar").removeClass("tb_disabled");
    AjaxCall("popupCont", "c=viewer/registerMarkup", true, "commonForm");
};

/**
 * Занесение в markup_form форму относительных координат нового markup-a
 */
var setMarkupPosition = function () {
    var x = (mouseX - page_x) / page_w;
    var y = (mouseY - page_y + 17) / page_h;
    $("input[name=x_coordinate]").val(x);
    $("input[name=y_coordinate]").val(y);
};


//================= Функции отрисовки иконок комментариев ===================
/*
 * Определение координат верхнего левого угла и размеров image.
 *
 * @param {type} container_id - ID контейнера, в который загружена image
 * 
 * Устанавливаются глобальные переменные:
 * page_x, page_y, page_w и page_h
 * @returns {String}
 * &x=[x угла]&y=[y угла]&w=[ширина]&h=[высота]
 */
var getImgMetrics = function (container_id) {
    var img = $('#' + container_id).first();
    var pos = img.position();
    page_x = pos.left;
    page_y = pos.top;
    page_w = img.width();
    page_h = img.height();
    var s = "&x=" + pos.left + "&y=" + pos.top + "&w=" + img.width() + "&h=" + img.height();
    console.log("getImgMetrics('" + container_id + "'):" + s);
    return s;
}

/**
 * Вывод иконок маркапов на страницу 
 * AJAX-вызов модуля viewer/show_markup_icons, которому передаются 
 * ID страницы, положение и размеры изображения, признак thmb,
 * а также все данные формы для фильтрации по автору.
 * 
 * @param {type} page_id - ID страницы
 * @param {type} thmb = Y - выводить маленькие иконки в контейнер "page_" + page_id + "_mkps"
 * N - большие иконки, с кликом, в main_page_mkps.
 * Если thmb не задан, то обновить маркапы на основной странице и на thumbnails
 * 
 */

var showMarkupIcons = function (page_id, thmb) {
    console.log("showMarkupIcons: page_id=" + page_id + "; thmb=" + thmb);
    if (thmb) {
        var container_id = "main_page";
        if (thmb === "Y")
            container_id = "page_" + page_id;
        AjaxCall(container_id + "_mkps"
                , "c=viewer/show_markup_icons&page_id=" + page_id
                + getImgMetrics(container_id) + "&thmb=" + thmb, true, "commonForm");
    } else {
        AjaxCall("main_page_mkps"
                , "c=viewer/show_markup_icons&page_id=" + page_id
                + getImgMetrics("main_page") + "&thmb=N", true, "commonForm");
        AjaxCall("page_" + page_id + "_mkps"
                , "c=viewer/show_markup_icons&page_id=" + page_id
                + getImgMetrics("page_" + page_id) + "&thmb=Y", true, "commonForm");
    }
}

/**
 * Вывод иконок маркапов на всех страницах (на главной и на thumbnails)
 * Цикл по списку ID страниц и вызов showMarkupIcons();
 * Списко страниц формируется в show_thumbnails.cfg
 */

var showAllMarkupIcons = function () {
    showMarkupIcons(active_page_id, "N");
    for (var i = 0; i < page_list.length; i++) {
        showMarkupIcons(page_list[i], "Y");
    }
}

//
//=================  Работа со списком маркапов ==================

/**
 * Сортировка списка маркапов по заданному полю
 * @param {type} field
 * @returns {undefined}
 */

var sortMarkupList = function (field) {
    AjaxCall("page_comments", "c=viewer/show_markup_list&srt_field=" + field, true, "commonForm");
}

/**
 * Фильтрует список маркапов по автору
 */
var filter_author = function (author_id) {
    console.log("markups_author: " + author_id);
    $('input[name="author_id"]').val(author_id);
    AjaxCall("page_comments", "c=viewer/show_markup_list", true, "commonForm");

    showAllMarkupIcons();
}

/**
 * Переключатель, который скрывает/показывает весь текст маркапа
 */
var toggleTextMarkup = function (markup_nr) {
    var textMarkup = document.getElementById("textMarkup_" + markup_nr);
    var textMarkupHeigth = textMarkup.offsetHeight;
    console.log("markupNr_" + markup_nr);
    console.log("textMarkupHeigth= " + textMarkupHeigth);
    //$("#textMarkup_"+markup_nr).css("background-color", "red");

    if (parseInt(textMarkupHeigth) > 72) {
        $("#textMarkup_" + markup_nr).css("overflow", "hidden");
        $("#textMarkup_" + markup_nr).css("text-overflow", "ellipsis");
        $("#textMarkup_" + markup_nr).css("height", "35px");
        $("#toggle_" + markup_nr).removeClass("fa-caret-down").addClass("fa-caret-up");
    } else {
        $("#textMarkup_" + markup_nr).css("overflow", "");
        $("#textMarkup_" + markup_nr).css("text-overflow", "");
        $("#textMarkup_" + markup_nr).css("height", "auto");
        $("#toggle_" + markup_nr).removeClass("fa-caret-up").addClass("fa-caret-down");
    }
}

//=========================================================
/**
 * Отображение окна для ввода обратной связи от юзера
 */
var showErrorReportForm = function () {
    console.log("showErrorReportForm...");
    // заносим в pop-up окно форму ввода из шаблона
    $("#popupCont").html($("#error_report_form").html());
    $("#dialog_title").html("Отправка информации о проблеме");
    $("#dialog").css({"left": (mouseX - 350), "top": (mouseY + 20), "width": ""});
    // показываем диалоговое окно
    $("#dialog").show(200);
    setStandardEvents();
    textAreaResize();
};

// ===============================================================
// ===============================================================

/**
 * (document ).ready
 */
$(document).ready(function () {

    //   $('textarea.editor').keypress(delayedResize);   

    //   $("#dialog").draggable({handle: "#dialog_handler"});   

    /**
     * Обработка MouseMove - единая для всех
     * заносит координаты мыши в mouseX и mouseY.
     */
    $(document).mousemove(function (e) {
        mouseX = e.pageX;
        mouseY = e.pageY;
        //       var messagetoSend = document.getElementById('markupTextBlock').value.replace(/\n/g, "<br />");
    });
});