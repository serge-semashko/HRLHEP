package myiss;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;


public class Html2Text extends HTMLEditorKit.ParserCallback {
 StringBuffer s;

 public Html2Text() {
    
 }

 public void parse(Reader in) throws IOException {
   s = new StringBuffer();
   ParserDelegator delegator = new ParserDelegator();
   // the third parameter is TRUE to ignore charset directive
   delegator.parse(in, this, Boolean.TRUE);
 }

 public void handleText(char[] text, int pos) {
   s.append(text);
 }

 public String getText() {
   return s.toString();
 }

 public static void main (String[] args) {
   try {
     // the HTML to convert
     StringReader in = new StringReader("<i>dfkdsjfl</i><a href=LLL>sdkjsld</a>");
     Html2Text parser = new Html2Text();
     parser.parse(in);
     in.close();
     System.out.println(parser.getText());
   }
   catch (Exception e) {
     e.printStackTrace();
   }
 }
}
