var lastcompletition=-1;
function update() {
  var completition = Math.min(  $(document).scrollTop()/2,100);
if(completition>0)
{
  completition=100;
}
  if($(document).scrollTop()>300)
{
$(".shadow").addClass("opaque");
}else{
  $(".shadow").removeClass("opaque");
}
if(completition!=lastcompletition)
{
    lastcompletition=completition;


    $( ".logo").css("top",16-completition/7+"vh");
    $( ".logo").css("height",36-completition/3.333+"vh");
    $( ".logo").css("width",80-Math.sin(completition/200*3.14)*60+"vw");



}
}
  var subs;
function getmy(str)
{

  if(subs.search(str+":")>=0)
  {
  return  subs.split(str+":")[1].split('\n')[0];
}else {
  return "";
}

}
function fillgallery()
{


  var  folder="!zawartosc/galeria";

  $.ajax({
          url : folder+"/nazwy.txt",
          dataType: "text",
          success : function (data) {
            subs=data;
          }
        });


  $.ajax({
      url : folder,
      success: function (data) {
          $(data).find("a").attr("href", function (i, val) {
              if( val.match(/\.(jpe?g|png|gif)$/) ) {
                  $(".gallery").append( '<div class="gal_img_group"><img src="'+ folder+"\\" + val +'"><div>'+getmy(val)+"</div></div>" );

              }
          });
      }
  });
}
function fillmenu(){
  $.ajax({
          url : "!zawartosc/menu.txt",
          dataType: "text",
          success : function (data) {
            var items = data.split("\n");
            var html="<div>";
            $.each(items, function( index, value ) {
              if(value.charAt(0)!="#")
              {
                if(value.charAt(0)=="@")
                {

                   html+=('</div><div class="menu_row_type">'+value.replace("@", "")+'')

                }else if(value.length>1) {
                  var item = value.split("$");

                  if(item[1]==undefined)
                  {
                    item[1]="?";
                  }
                  if(item[2]==undefined)
                  {
                    item[2]="";
                  }

                  html+=('<div class="menu_row_item">'+item[0]+'<span class="menu_row_price">'+item[1]+'</span><span class="menu_row_sub">'+item[2]+'</span></div>')
                }
              }
                });
                html+="</div>";
                $(".menu_row").append(html);
              }
      });
}
$(document).ready(function(){
  update();
  fillmenu();
fillgallery();
$(".divider:even").addClass("odddivider");

$(".divider:odd").addClass("evendivider");
  $( ".fb_fg, .fb_bg, .fb_letter, .fb_hover" ).hover(function(){
    $( ".fb_fg").css("transform","scale("+1+")")
    $( ".fb_bg").css("transform","scale("+0+")")

    },function(){
  $( ".fb_fg").css("transform","scale("+0+")")
  $( ".fb_bg").css("transform","scale("+1+")")

  });

    $( ".inst_fg, .inst_bg, .inst_logo, .inst_hover" ).hover(function(){
      $( ".inst_bg").css("transform","scale("+0+")")

      $( ".inst_fg").css("transform","scale("+1+")")
      },function(){
        $( ".inst_bg").css("transform","scale("+1+")")

    $( ".inst_fg").css("transform","scale("+0+")")
    });
  $( ".menu_hamburger, .menu_button" ).on( "click", function( event ) {
    $( ".bar").toggleClass("isx");
    $( ".menu").toggleClass("isxmenu");
    $( ".logo").toggleClass("fullsizelogo");
  });
    $( ".menu_button" ).on( "click", function( event ) {
    var mytext=$.trim($(this).html());
    $( ".divider" ).each(function( index ) {
      var thistext=$.trim($(this).html());
if(thistext===mytext && !($(".menu").hasClass("isxmenu") && ($( window ).width()<900) ))
{
var pos=$(this).offset().top-100;
$('html, body').animate({
          scrollTop: pos
        }, 500);


}
});
    });
  $(document).scroll(function(){

    update();

  });
});
