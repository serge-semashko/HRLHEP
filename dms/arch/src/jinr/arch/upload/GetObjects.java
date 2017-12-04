package jinr.arch.upload;

import java.awt.Font;
import java.awt.List;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;


public class GetObjects
{

	File[] f_name;
	SendObj sendobj;
	//public SendObj [] sendobj_mass;
	public Vector sendobj_mass;
	byte[] b_code;
	JFileChooser chooser;
	java.awt.List lst;
	public String lastPath = "c:";

	GetObjects ( JFileChooser ch, java.awt.List l )
	{
		f_name = null;
		sendobj = null;
		sendobj_mass = new Vector();
		b_code = null;
		chooser = ch;
		lst = l;
	}

	public void OpenFile ( )
	{
		int result = chooser.showDialog( null, "Выбрать");
// int result = chooser.showOpenDialog( null );
//	  int result = chooser.showOpenDialog( chooser );
		if( result == JFileChooser.CANCEL_OPTION )
		{
			return;
		}
		f_name = chooser.getSelectedFiles();

		if( f_name == null )
		{
			JOptionPane.showMessageDialog( null, "Выберите файлы" );
		}
		else
		{
			try
			{
				if( lst.getItemCount() < 1 )
				{
					for( int t = 0; t < f_name.length; t++ )
					{
						lst.add( f_name[t].getPath() );
					}
					return;
				}
				for( int j = 0; j < f_name.length; j++ )
				{
					boolean flag = false;
					for( int k = 0; k < lst.getItemCount(); k++ )
					{
						if( lst.getItem( k ).compareTo( f_name[j].getPath() ) == 0 )
						{
							flag = true;
							break;
						}
					}
					if( !flag )
					{
						lst.add( f_name[j].getPath() );
					}
					else
					{
						JOptionPane.showMessageDialog( null, f_name[j].getPath() + "\n\r уже есть в списке" );
						f_name[j] = null;
					}
				}

				//chooser.setCurrentDirectory(f_name[0]);
			}
			catch( Exception e )
			{
				JOptionPane.showMessageDialog( null, "Error" );
			}
		}
	}

	public void ReadFiles ( int id )
	{
		//sendobj_mass = new Vector(f_name.length); 
		//chooser.setCurrentDirectory(f_name[0]);
		for( int i = 0; i < f_name.length; i++ )
		{
			try
			{
				if( f_name[i] == null )
				{
					continue;
				}
				FileInputStream f_in = new FileInputStream( f_name[i] );
				int size = f_in.available();
				System.out.println( i + ": File: " + f_name[i] + "; size=" + size );
				b_code = new byte[size];
				f_in.read( b_code );
				sendobj = new SendObj( b_code, id, i + 1, f_name[i].getPath() );
				sendobj_mass.add( sendobj );
				lastPath = f_name[i].getPath();
				int ii = lastPath.lastIndexOf( "\\" );
				lastPath = lastPath.substring( 0, ii + 1 ).replace( '\\', '/' );
				//JOptionPane.showMessageDialog(null, new String(sendobj_mass.get(i).toString()));
				//JOptionPane.showMessageDialog(null, sendobj_mass.size());
			}
			catch( Exception e )
			{
				e.printStackTrace( System.out );
				JOptionPane.showMessageDialog( null, "Файл не может быть прочитан!" );
			}
		}
	}

}

/*
 * 
 * 		int result = chooser.showDialog( null, "Выбрать");

============= openFileChooser ==============
java.security.AccessControlException: access denied (java.lang.RuntimePermission modifyThread)
	at java.security.AccessControlContext.checkPermission(Unknown Source)
	at java.security.AccessController.checkPermission(Unknown Source)
	at java.lang.SecurityManager.checkPermission(Unknown Source)
	at sun.applet.AppletSecurity.checkAccess(Unknown Source)
	at java.lang.Thread.checkAccess(Unknown Source)
	at java.lang.Thread.interrupt(Unknown Source)
	at javax.swing.plaf.basic.BasicDirectoryModel.validateFileCache(Unknown Source)
	at sun.swing.FilePane.rescanCurrentDirectory(Unknown Source)
	at javax.swing.plaf.metal.MetalFileChooserUI.rescanCurrentDirectory(Unknown Source)
	at javax.swing.JFileChooser.rescanCurrentDirectory(Unknown Source)
	at javax.swing.JFileChooser.showDialog(Unknown Source)
	at jinr.arch.upload.GetObjects.OpenFile(GetObjects.java:56)
	at jinr.arch.upload.UploadApplet$2.actionPerformed(UploadApplet.java:112)
	at javax.swing.AbstractButton.fireActionPerformed(Unknown Source)
	at javax.swing.AbstractButton$Handler.actionPerformed(Unknown Source)
	at javax.swing.DefaultButtonModel.fireActionPerformed(Unknown Source)
	at javax.swing.DefaultButtonModel.setPressed(Unknown Source)
	at javax.swing.AbstractButton.doClick(Unknown Source)
	at javax.swing.AbstractButton.doClick(Unknown Source)
	at jinr.arch.upload.UploadApplet.openFileChooser(UploadApplet.java:164)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
	at java.lang.reflect.Method.invoke(Unknown Source)
	at sun.plugin.javascript.invoke.JSInvoke.invoke(Unknown Source)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
	at java.lang.reflect.Method.invoke(Unknown Source)
	at sun.plugin.javascript.JSClassLoader.invoke(Unknown Source)
	at sun.plugin.com.MethodDispatcher.invoke(Unknown Source)
	at sun.plugin.com.DispatchImpl.invokeImpl(Unknown Source)
	at sun.plugin.com.DispatchImpl$1.run(Unknown Source)
	at java.security.AccessController.doPrivileged(Native Method)
	at sun.plugin.com.DispatchImpl.invoke(Unknown Source)
java.lang.Exception: java.security.AccessControlException: access denied (java.lang.RuntimePermission modifyThread)
	at sun.plugin.com.DispatchImpl.invokeImpl(Unknown Source)
	at sun.plugin.com.DispatchImpl$1.run(Unknown Source)
	at java.security.AccessController.doPrivileged(Native Method)
	at sun.plugin.com.DispatchImpl.invoke(Unknown Source)
0: File: C:\apps\adb\addon\DATA\dblink.xls; size=346112
0: sending... 0 - OK
*/