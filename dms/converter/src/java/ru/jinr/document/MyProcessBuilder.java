package ru.jinr.document;

import java.io.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;

/**
 * Insert the type's description here.
 * Creation date: (07.10.2009 18:44:28)
 * @author: Administrator
 */
public class MyProcessBuilder {
	  ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
	  ByteArrayOutputStream baosErr = new ByteArrayOutputStream(); 
	  final int TIMEOUT = 30;

	public class Spooller implements Runnable
	{
	  final static int BINARY = 1;
	  final static int CHARACTER = 2;
	  
	  private InputStream in;
	  private OutputStream out;
	  private Reader reader;
	  private Writer writer;
	  private int type;
	  private int bufferSize = 16 * 1024;

	  public Spooller(InputStream in, OutputStream out)
	  {
	    if(in == null || out == null)
	    {
	      throw new NullPointerException();
	    }

	    this.in = in;
	    this.out = out;
	    type = BINARY;
	  }

	  public Spooller(Reader reader, Writer writer)
	  {
	    if(reader == null || writer == null)
	    {
	      throw new NullPointerException();
	    }

	    this.reader = reader;
	    this.writer = writer;
	    type = CHARACTER;
	  }

	  public int getBufferSize()
	  {
	    return bufferSize;
	  }

	  public void setBufferSize(int bufferSize)
	  {
	    this.bufferSize = bufferSize;
	  }

	  public int getType()
	  {
	    return type;
	  }

	  public void run()
	  {
	    switch(type)
	    {
	      case BINARY:
	        spoolBinary();
	        break;

	      case CHARACTER:
	        spoolCharacter();
	        break;
	    }
	  }

	  private void spoolBinary()
	  {
	    try
	    {
	      byte[] buffer = new byte[bufferSize];
	      int read;
	      while((read = in.read(buffer)) != -1)
	      {
	        out.write(buffer, 0, read);
	      }
	    }
	    catch(Exception e)
	    {
	      e.printStackTrace();
	    }
	    finally
	    {
	    	try {
	    		in.close();
			} catch (IOException e) {
			}
	    	try {
	    		out.close();
			} catch (IOException e) {
			}
	    }
	  }

	  public void spool()
	  {
	    Thread spoolThread = new Thread(this, "Spool thread");
	    spoolThread.start();
	  }

	  private void spoolCharacter()
	  {
	    try
	    {
	      char[] buffer = new char[bufferSize];
	      int read;
	      while((read = reader.read(buffer)) != -1)
	      {
	        writer.write(buffer, 0, read);
	      }
	    }
	    catch(Exception e)
	    {
	      e.printStackTrace();
	    }
	    finally
	    {
	    	try {
				reader.close();
			} catch (IOException e) {
			}
	    	try {
				writer.close();
			} catch (IOException e) {
			}
	    }
	  }


	}//AE]	
	
/**
 * MyProcessBuilder constructor comment.
 */
public MyProcessBuilder() {
	super();
}
public boolean startProcess(String startString,File workDir){
	return startProcess(startString, workDir, true, TIMEOUT);
}
//AE 12.01.10 added needWait parameter
public boolean startProcess(String startString,File workDir,boolean needWait,int timeMinute){
    ProcessBuilder processBuilder = new ProcessBuilder(new String[]{"cmd.exe", "/C","\""+startString+"\""});
    processBuilder.directory(workDir);

    //System.out.println("GS_LIB=" + System.getenv("GS_LIB"));
//    System.out.println("startString=" + startString);
    Process process;
	try {
		process = processBuilder.start();
	    new Spooller(new InputStreamReader(process.getInputStream(), "Cp866"), new OutputStreamWriter(baosOut, "Cp866")).spool();
	    new Spooller(new InputStreamReader(process.getErrorStream(), "Cp866"), new OutputStreamWriter(baosErr, "Cp866")).spool();// new OutputStreamWriter(System.err, "Cp866")).spool();
	    if(needWait)
	    	return waitFor(process,timeMinute,TimeUnit.MINUTES);
	    return true;
	} catch (Exception e1) {
		e1.printStackTrace();
	}
	return false;
}

public String getInfo(){
	String st="";
	if(baosOut==null || baosOut.toByteArray().length==0)
		return st;
	String stt = "";
	try{
		stt = new String(baosOut.toByteArray());
	}
	catch(Exception ex){
		st+="5";
	}
	st+=stt;
	return st;
}
public String getError(){
	String st="";
	if(baosErr==null || baosErr.toByteArray().length==0)
		return st;
	String stt = "";
	try{
		stt = new String(baosErr.toByteArray());
	}
	catch(Exception ex){
		st+="5";
	}
	st+=stt;
	return st;
}
//public boolean startProcess(String startString, boolean needWait) {
//    ProcessBuilder processBuilder = new ProcessBuilder(new String[]{"cmd.exe", "/C","\""+startString+"\""});
//	//processBuilder.redirectErrorStream(true);
//    try {
//    	Process process = processBuilder.start();
//    	if(needWait)
//    		process.waitFor();
//	} catch (Exception e1) {
//		e1.printStackTrace();
//		return false;
//	}
//    return true;
//}

private boolean waitFor(Process process,long timeout, TimeUnit unit)
        throws InterruptedException
    {
        long startTime = System.nanoTime();
        long rem = unit.toNanos(timeout);

        do {
            try {
            	process.exitValue();
                return true;
            } catch(IllegalThreadStateException ex) {
                if (rem > 0)
                    Thread.sleep(
                        Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
            }
            rem = unit.toNanos(timeout) - (System.nanoTime() - startTime);
        } while (rem > 0);
        return false;
    }

}
