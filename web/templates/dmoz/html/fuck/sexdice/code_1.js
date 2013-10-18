var pix = 10; //колличество пикселов
var spid = 40; //скорость
var level = 1; //уровень
var box = Array();
var color = Array();
var play = true;

var level = 1; //уровень
var img_foto = 0; //номер девушки

function Choice(i) {
    img_foto = i;
    document.getElementById("menu").style.display = "none";
    document.getElementById("images").innerHTML = "<img src='/templates/default/html/fuck/sexdice/data/src/src1/src/src"+img_foto+"/1.jpg'>";
}

function Over(e,i) {
    
    e = e || event;
    box = document.getElementById("box"+i);
    var x = 0, y = 0, w = 0, h = 0, top = box.offsetTop, left = box.offsetLeft;
    x = e.pageX - left;
    y = e.pageY - top;
    w = box.offsetWidth;
    h = box.offsetHeight;


    if(x<w && (y>=0&&y<20)) {
        //box.innerHTML = "Сверху";
        Go(0,top,0,0,i);
        pause = false;
    }
    if(x<w && (y<100&&y>80)) {
        //box.innerHTML = "Снизу";
        Go(0,top,0,1,i);
        pause = false;
    }
    if(y<w && (x>=0&&x<20)) {
        //box.innerHTML = "Слева";
        Go(0,0,left,2,i);
        pause = false;
    }
    if(y<w && (x<=w&&x>w-20)) {
        //box.innerHTML = "Справа";
        Go(0,0,left,3,i);
        pause = false;
    }

//box.innerHTML = x+":"+y;
}
function Go(inc,x,y,act,i) {
    box = document.getElementById("box"+i);
    inc++;
    if(level == 5) {
      if(act==1) {
        x+=pix;
        box.style.top=x+"px";
      }
      if(act==0) {
        x-=pix;
        box.style.top=x+"px";
       }
      if(act==3) {
        y+=pix;
        box.style.left=y+"px";
      }
      if(act==2) {
        y-=pix;
        box.style.left=y+"px";
      }
    } else {

       if(act==0) {
        x+=pix;
        box.style.top=x+"px";
      }
      if(act==1) {
        x-=pix;
        box.style.top=x+"px";
      }
      if(act==2) {
        y+=pix;
        box.style.left=y+"px";
      }
      if(act==3) {
        y-=pix;
        box.style.left=y+"px";
      }
    }
    if(inc <= 20) {
        setTimeout("Go("+inc+","+x+","+y+","+act+","+i+")", spid);
    }
}

function Bom(i) {
    var images = document.getElementById("images");
    var status = document.getElementById("status");
    var box = document.getElementById("box"+i);
    box.style.backgroundColor = "#fff";
    box.style.width = "0px";
    box.style.height = "0px";
    box.innerHTML = "<img width='140px' src='boom.png'>";
    setTimeout("del("+i+")", 1000);
    
    if(i == 0) {
        box[0] = 1;
    }
    if(i == 1) {
        box[1] = i;
    }
    if(i == 2) {
        box[2] = i;
    }
	
    if(box[0] == 1 && box[1]== 1 && box[2] == 2) {
        level += 1;

        if(level ==2) {
            images.innerHTML = "<img src='data/src/src1/src/src"+img_foto+"/2.jpg'>";
        }
        if(level ==3) {
            images.innerHTML = "<img src='data/src/src1/src/src"+img_foto+"/3.jpg'>";
        }
        if(level ==4) {
            images.innerHTML = "<img src='data/src/src1/src/src"+img_foto+"/4.jpg'>";
        }
        if(level ==5) {
            images.innerHTML = "<img src='data/src/src1/src/src"+img_foto+"/5.jpg'>";
            alert("Ты попал на главный уровень. Будь очень внимателен, накажи её!");
        }

        if(level ==6) {
            images.innerHTML = "<img src='data/src/src1/src/src6/h2.jpg'>";
            alert("Ты самый озабоченный на этой неделе");
            window.location.reload();
        } else {
            if(level == 4) {
                pix = 7;
                Drebez(0);
                Drebez(1);
                Drebez(2);
                spid = 0;
            } if(level == 5) {
                pix = 10;
                spid = 3;
            } 
            else spid -= 15;
            document.getElementById("blockir").style.display = "block";
            setTimeout("Clear()", 1200);
            status.innerHTML = "Уровень "+level;
        }
    }
}


function del(i) {
   var box = document.getElementById("box"+i);
   box.style.display = "none";
}

function Clear() {
    document.getElementById("blockir").style.display = "none";
	
    color[0] = '#fdeaa8';
    color[1] = '#cc8899';
    color[2] = '#addfad';
    for(i=0; i<3; i++) {
        document.getElementById("box"+i).style.backgroundColor = color[i];
        document.getElementById("box"+i).style.top = (130*i)+"px";
        document.getElementById("box"+i).style.left = 200+"px";
		document.getElementById("box"+i).innerHTML = "";
		document.getElementById("box"+i).style.display = "block";
		document.getElementById("box"+i).style.width = "100px";
	    document.getElementById("box"+i).style.height = "100px";
        box[i] = 0;
    }
}


inc = 0;
function Drebez(i) {
    box = document.getElementById("box"+i);
    inc++;
    var st = 0;
    if((1&inc) == 0) {
        st = 20;
    } else {
        st -= 20;
    }
    var top = box.offsetTop, left = box.offsetLeft;
    box.style.top = (top+st)+"px";

    if(play == true && i==0) setTimeout("Drebez("+i+")", 100);
    if(play == true && i==1) setTimeout("Drebez("+i+")", 100);
    if(play == true && i==2) setTimeout("Drebez("+i+")", 100);
}

function show() {
    var images = document.getElementById("images");
    if(images.style.display == "block")
        images.style.display = "none"; else images.style.display = "block";
}

function instruction() {
    var inst = document.getElementById("inst");
    if(inst.style.display == "block")
        inst.style.display = "none"; else inst.style.display = "block";
}
