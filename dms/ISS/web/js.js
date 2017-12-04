$(document).ready(function() {
if($('#excbutt').size()>0){
    $('#menutoggle').html($('#menutoggle').html()+$('#excbutt').html());
    $('#excbutt').remove();
   
}

    $("a.iss-version").fancybox({
				
				'height'			: '75%',
				'autoScale'			: false,
				'transitionIn'		: 'none',
				'transitionOut'		: 'none',
				'type'				: 'iframe'
			});
    		

var fsmustbeopened = false;
$( "#fs" )
 .hover( function(event) {
    fsmustbeopened = true;
//    console.log(event);
    $(".fontsize").css('display', 'inline-block');
    //console.log(fsmustbeopened);
}
,
function(event) {
    fsmustbeopened = false;
    setTimeout(
        function () { 
            if (!fsmustbeopened){
                //console.log("kkk"+fsmustbeopened);
                $(".fontsize:not(.checked)").hide("slow");
            }
        }
    , 1000);
});



//$( "#fs" )
// .on( "mouseenter", function(event) {
//    console.log(event);
//    $(".fontsize").css('display', 'inline-block');
//})
//.on( "mouseleave", function(event) {
//    console.log(event);
//$(".fontsize:not(.checked)").delay(1000).hide("slow");
//});


//.mouseleave(function() {
//$(".fontsize:not(.checked)").delay(1000).hide("slow");
//})
//.mouseenter(function() {
// $(".fontsize").css('display', 'inline-block');
//});


    var menu = $.cookie('menu');
    if(menu==null) menu = 1;
    //размер шрифта
    var fs = $.cookie('fs');
//     console.log('fs:'+fs);
    if(!fs) fs = 'fs1';
//    console.log('fs:'+fs);
    setFS(fs);
    //$().UItoTop({ easingType: 'easeOutQuart' });
    if($('#menu').height()<$('#mid_conteiner').height())$('#menu').height($('#mid_conteiner').height());// меню на всю высоту.
        //PIROBOX
    $.piroBox_ext({piro_speed :700, bg_alpha : 0.6, piro_scroll : true, piro_drag :true, piro_nav_pos: 'top'});
    $.each($("select.chosen"),function() {$(this).chosen();});
    $.each($("select.chosen-nosearch"),function() {$(this).chosen({disable_search: true});});

//$('#scroll-pane').jScrollPane();
$(function() {
    $("#scroll-pane").mousewheel(function(event, delta){
//    console.log('e.d'+delta);
    if($("#scroll-pane").width()<$("#qtable").width()){
//        console.log($("#scroll-pane").scrollLeft());
   this.scrollLeft -= (delta * 30);     
  event.preventDefault();
   }
    });
});


function openMenu(){
    $("#menu").removeClass('menu-closed');
    $("#menu").addClass('menu-opened');
    $("#content").addClass('normal');
    $("#toggle").removeClass('img-menu-opened');    
    $("#toggle").addClass('img-menu-closed');    
    $(".tiptip").tipTip({fadeOut: 1}); 
    $.cookie('menu',1);     
    menu = 1;
}

function closeMenu()
{
      $("#menu").removeClass('menu-opened');
      $("#menu").addClass('menu-closed');
      $("#content").addClass('full');
      $("#toggle").addClass('img-menu-opened');    
      $("#toggle").removeClass('img-menu-closed');   
      $(".tiptip").tipTip({fadeOut: 1}); 
      $.cookie('menu',0);    
      menu = 0;
}

function toggleMenu(){
    if(menu == 0){
        openMenu();
    }else{
        closeMenu();
    }

}

$("#toggle").click(function (event) {
    toggleMenu();
    event.preventDefault();
});   
//кнопка выполнения запроса
$("#execute").click(function ( event ) {
    event.preventDefault();
    //установка значений по умолчанию
    $( "input[defaultvalue]" ).each(function() {
        if($( this ).attr( "value" ) == ''){
            $( this ).attr( "value" ,$( this ).attr( "defaultvalue" ))
        }
    });
    quickSelect('Page',1);
    $('#theform').submit();
    
    showBusy();    
});

//$("#scrolltop").click(function (event) {
//    $(document).scrollTop();
//});   

$(window).scroll(function () {
//console.log($(window).scrollTop()+':'+$('.header').height())
    if ($(window).scrollTop()  > 200) {
        $('#scrolltop').show();
    } else {
        $('#scrolltop').hide();
    }
});


$(".tiptip").tipTip(); 

//alert("qt:"+$("#qtable").width());
//alert("sp:"+$("#scroll-pane").innerWidth());
//$("#qtable").width($("#scroll-pane").width());
if($("#execute").size()==0){
    openMenu();
}
if($.cookie('menu')==0){
    closeMenu();
}else{
    openMenu();
}
if($("#scroll-pane").width()<$("#qtable").width()){
closeMenu();
}

});




function showLoader(){
$('#loader').css('display','block');
$('#loader').css('z-index','9999');
$('#loader').css('height', $(document).height())
$('#loader').css('width', $(document).width()) 
$('#loaderimg').css('left', ($(document).width()/2)-($('.loaderimg').width())) 
}







function setPage(pageno){
        
          var elem = document.getElementsByName("Page")[0];
          elem.value=pageno;
          document.theform.submit();
          showBusy();
          //showLoader();
}

function showBusy(){
    $("#busy").show();
    $("#exe").hide();
    $("#execute").attr("disabled","disabled");
}



function setFS(className){
    $("body").removeClass();
    $("body").addClass("body-"+className);
    $(".fontsize").removeClass("checked");
    $("#"+className).addClass("checked");
    $.cookie('fs',className);
}

function showFSBar(){
    $(".fontsize").css('display', 'inline-block');
}
function hideFSBar(){
    
    $(".fontsize:not(.checked)").delay(1000).hide("slow");
}
function doAlert(){
alert('');
}

function highlightRow(el){
$(el).addClass("row-highlight");
}
function shadeRow(el){
$(el).removeClass("row-highlight");
}
function markRow(el){
$(el).toggleClass("row-marked");
}
function showRow(el){
    $(el).addClass("row-highlight");
    s="<table cellpadding='5' colspacing = '5'>";
    hrows = $('#tab-header-row').children()
    vrows = $(el).children()
    for(i = 0; i < hrows.length; i++){
        s = s+"<tr><th align='right'>"+$(hrows[i]).html()+'</th><td>'+$(vrows[i]).html()+'</td></tr>';
    }
    s+='</table>';
    
    $.fancybox({
    
    'titlePosition': 	'outside',
    'autoScale': true,
    'transitionIn': 'none',
    'transitionOut': 'none',
    'content':s   
});
}
function quickSelect(name, value) {
try{ var elem = document.getElementsByName(name)[0];
          if (elem.type =="select-one"){
          for( i=0; i<elem.length; i++ ){
              //console.log(elem[i].value.replace(/"/g, "&quot;")+" : "+value);
            if(elem[i].value.replace(/"/g, "&quot;") == value)
            elem[i].selected = "selected";
          }
          }
          if(elem.type == "radio"){
            elem = document.getElementsByName(name);
            for (i = 0; i<elem.length; i++){
                if(elem[i].value==value) elem[i].checked="checked";
            }
            
          }
    }catch(e){
    //TODO: на jquery;
    }      
}
