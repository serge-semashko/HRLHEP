
//======= DIV manipulation functions ===========

var dww = 8;
var currDiv = null;
var currHd=0;
var maxHd = 300;

function showDiv(divId, h, slow)
{ currDiv = document.getElementById(divId);
  currHd = currHeight(currDiv);
  currDiv.style.display = "block";
  if (slow)
  { dww = h / 10;
	maxHd = h;
    exploreDiv();  
  }
  else if (h >= 0)
	currDiv.style.height = h;
}

function hideDiv(divId)
{ currDiv = document.getElementById(divId);
  currHd = currHeight(currDiv);
  dww = -currHd / 5;
  maxHd = currHd;
  exploreDiv();
}

function showHideDiv(divId, h)
{ currDiv = document.getElementById(divId);
  currHd = currHeight(currDiv );
  currDiv.style.display = "block";
  if (currHd < h) 
  { dww = h / 10;
    maxHd = h;
  }
  else 
  { dww = -h/5;
    maxHd = currHd;
  }
  exploreDiv();
  return (dww > 0);
}

function exploreDiv()
{ currHd += dww;
  if (currHd >= maxHd) currHd = maxHd;
  if (currHd < 0.) currHd = 0.;
  currDiv.style.height = currHd + "px";
// alert (currHd);
  if (currHd > 0. && currHd < maxHd)
    setTimeout("exploreDiv();",20);
  if (currHd == 0.)
	currDiv.style.display = "none";
}

//======= Frame manipulation functions ===========
var currFrame = null;
var currH=0.;
var maxH = 300.;
var minH = 0.;
var currURL = "";


function loadFrame(frameId, h, url, steps)
{ window.scrollTo(0,0);
  if (h >0)
	showDiv('svsDiv', 16);
  else
	showDiv('svsDiv', -1);
  getCurrFrame(frameId);
  if (typeof url == "string") 
  { currURL = url;
  	currFrame.src = currURL;
  }
  setFrameHeight(frameId, h, true, steps)
}

function hideFrame(frameId, steps)
{ var h = maxH; 
  setFrameHeight(frameId, 0, true, steps);
  maxH = h;
}

function setFrameHeight(frameId, h, doDecrease, steps)
{ getCurrFrame(frameId);
	if (currH > h && !doDecrease) return;
  var n = (steps)? steps:10;
  if (h < 0) h = currH;
  maxH = h; 
  minH = h;
  if (n > 1)
  { dww = (maxH-currH) / n;
//	alert (currH +":"+maxH+":"+dww);
  	exploreFrame();
  }
  else
  {	currFrame.style.height = maxH + "px";
  	setFrameButtons(false);
  }
}


function getCurrFrame(frameId)
{ var f = document.getElementById(frameId);
  currFrame = f;
  currH = currHeight(f);
}

function setFrameSrc(frameId, url)
{ var f = document.getElementById(frameId);
  currFrame = f;
  currH = currHeight(f);
  if (typeof url == "string") 
  { currURL = url;
  	currFrame.src = currURL;
  }
}

function showHideFrame(frameId, h)
{ getCurrFrame(frameId);
  setFrameButtons(true);
  if (currH < h) 
  { dww = h / 10;
    maxH = h;
  }
  else 
  { dww = -h/15;
    maxH = currH;
    minH=0.;
  }
  exploreFrame();  
}

function setFrameButtons(invert)
{ var visible = (currHeight(currFrame) > 0.);
  if (invert) visible = !visible;
  try
  {  if (visible)
    { document.getElementById("svsframe_max").style.display="none";
      document.getElementById("svsframe_min").style.display="inline";
    }
    else
    { document.getElementById("svsframe_max").style.display="inline";
      document.getElementById("svsframe_min").style.display="none";
    }
  }
  catch (e) {}
}

function closeFrame(frameId, divId)
{ getCurrFrame(frameId);
  currFrame.src = "";
  currFrame.style.height = 0 + "px";
  currH = 0;
  if (divId) hideDiv(divId);
}

function currHeight(f)
{ var s = f.style.height;
  if (s.length < 3) s="0px";
  var i = s.indexOf("px");
  return parseInt(s.substring(0,i));
}

function exploreFrame()
{ currH += dww;
  if (currH > maxH && dww > 0.) currH = maxH;
  if (currH < minH && dww < 0.) currH = minH;
  if (currH < 0.) currH = 0.;
  currFrame.style.height = currH + "px";

  if ((dww > 0. && currH < maxH) 
   || (dww < 0. && currH > minH))
    setTimeout("exploreFrame();",30);
  else
  	setFrameButtons(false);
}

