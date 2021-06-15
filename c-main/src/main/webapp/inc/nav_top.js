window.onload=function(){
   var menu_id=0,menu=document.getElementById("navItems");
   if(!menu) return;
   for(var i=0; i<menu.childNodes.length;i++){
      if(menu.childNodes[i].tagName!="A")
         continue;
      if(menu_id==0)
         menu.childNodes[i].className="highlight";
      menu.childNodes[i].onclick=function(){
         var menu=document.getElementById("navItems");
         for(var i=0; i<menu.childNodes.length;i++)
         {
            if(menu.childNodes[i].tagName!="A")
               continue;
            menu.childNodes[i].className="";
         }
         this.className="highlight";
      }
      menu_id++;
   }
};