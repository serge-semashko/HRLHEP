1)Unzip converter.zip to webapps dir

2)Change parameters in web.xml:
a) for "<servlet-name>conv</servlet-name>":
dllPath (...webapps\\converter\\dll\\Inso\\doc2image.exe)  //pdf2raster old
dllPdfPath (...webapps\\converter\\dll\\Inso2\\doc2image.exe)//any2pdf old
dllPdf2RasterPath (...webapps\\converter\\dll\\\\PDF2Raster)//pdf2raster main folder new
dllLogPath (...webapps\\converter\\dll\\\\PDF2Raster\\logs) //It is dll log path and it can be set the same dir as for dll
defaultOutDir - path to temp dir
b) for <servlet-name>getFile</servlet-name>:
defaultOutDir - the same as for a.