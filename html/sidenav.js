function openNav() {
    document.getElementById("SIDENAV").style.width = "250px";
	document.getElementById("LOGO").style.marginRight = "250px";
	document.body.style.marginLeft="250px";
	document.getElementById("MENU").onclick=function(){closeNav();}
}

function closeNav() {
    document.getElementById("SIDENAV").style.width = "0px";
	document.body.style.marginLeft="0px";
	document.getElementById("LOGO").style.marginRight = "0px";
	document.getElementById("MENU").onclick=function(){openNav();}
}

