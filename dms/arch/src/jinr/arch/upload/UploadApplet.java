package jinr.arch.upload;

import java.awt.Font;
import java.awt.List;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
// import netscape.javascript.*;

public class UploadApplet extends JApplet
{

	public int id;
//	public String begin_folder;
	public Container cont;
	public JFileChooser chooser=null;
	GetObjects getobjects = null;
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private JTextField txt = new JTextField();
	private JButton butOpen = new JButton();
	private JButton butDel = new JButton();
	private JButton butSend = new JButton();
	private List lst = new List( 5 );
//	private JLabel lab_progress = new JLabel();

	public UploadApplet ()
	{
		try
		{
			Init();

		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	private void sendFiles ()
	{
	  Socket socket;
	  ObjectOutputStream output;
	  ObjectInputStream input;
		try
		{
			URL u = getCodeBase();
			String host = u.getHost();
			int port = Integer.parseInt( getParameter( "port" ) );
			socket = new Socket( host, port );
			output = new ObjectOutputStream( socket.getOutputStream() );
			output.writeInt( getobjects.sendobj_mass.size() );
			//JOptionPane.showMessageDialog(null, getobjects.sendobj_mass.size());
			//JOptionPane.showMessageDialog(null, getobjects.sendobj_mass.capacity());
			for( int i = 0; i < getobjects.sendobj_mass.size(); i++ )
			{
//				lab_progress.setText( "" );
				System.out.print( i + ": sending... " );
				output.writeObject( getobjects.sendobj_mass.get( i ) );
				System.out.println( i + " - OK" );
//				lab_progress.setText( "Пересылка " + ( ( SendObj ) ( getobjects.sendobj_mass.get( i ) ) ).name );
				output.flush();
			}
			butSend.setEnabled( false );
			lst.removeAll();
		  getobjects.sendobj_mass.removeAllElements();

		  input = new ObjectInputStream(socket.getInputStream());    
		  int numLoad = input.readInt();
		  //      lab_progress.setText( "" );
//		        netscape.javascript.JSObject win = ( JSObject ) JSObject.getWindow( this );
		  //      System.out.println( ": win=" + getobjects.lastPath );
//		        win.eval( "uploadDone('" + getobjects.lastPath + "');" );
		  input.close();
		  //output.close();
		  socket.close();
//		  JOptionPane.showMessageDialog(null, "Загружено файлов: " + numLoad);
		}
		catch( Exception e )
		{
			JOptionPane.showMessageDialog( null, "Ошибка пересылки файлов", "Error", JOptionPane.ERROR_MESSAGE );
			e.printStackTrace(System.out);
		}
	}


	public void start ()
	{
		butSend.addActionListener( new ActionListener()
				{
					public void actionPerformed ( ActionEvent event )
					{
						sendFiles();
					}

				} );

		butOpen.addActionListener( new ActionListener()
				{
					public void actionPerformed ( ActionEvent event )
					{
//						if (chooser == null) initChooser();
						id = Integer.parseInt( getParameter( "id" ) );
						getobjects.OpenFile( );
						if( lst.getItemCount() > 0 )
						{
							butSend.setEnabled( true );
						  getobjects.ReadFiles( id );
						}
					}

				} );

		butDel.addActionListener( new ActionListener()
				{
					public void actionPerformed ( ActionEvent event )
					{
						if( lst.getSelectedIndex() == -1 )
						{
							JOptionPane.showMessageDialog( null, "Щелкните мышкой на файле, который хотите удалить из списка загрузки." );
							return;
						}
						for( int i = 0; i < lst.getItemCount(); i++ )
						{
							if( lst.isIndexSelected( i ) )
							{
								lst.remove( i );
								getobjects.sendobj_mass.remove( i );
								//JOptionPane.showMessageDialog(null, getobjects.sendobj_mass.size());
							}
						}
						if( lst.getItemCount() < 1 )
						{
							butSend.setEnabled( false );
						}

					}

				} );
	  initChooser();

}

private void initChooser()
{
	chooser = new JFileChooser( getParameter( "begin_folder" ) );
	chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
	chooser.setMultiSelectionEnabled( true );

	getobjects = new GetObjects( chooser, lst );
}	
public void openFileChooser() 
{
	System.out.println( "============= openFileChooser ==============" );
// if (chooser == null)		initChooser();
	butOpen.doClick();

}

	private void Init () throws Exception
	{
	  System.out.println( "============= INIT ==============" );

		this.setBounds( new Rectangle( 1, 1, 800, 120 ) );
		this.getContentPane().setLayout( null );
		jPanel1.setBounds( new Rectangle( 0, 20, 10, 380 ) );
		jPanel2.setBounds( new Rectangle( 5, 5, 795, 390 ) );
		jPanel2.setLayout( null );
		txt.setText( "Выбранные файлы:" );
		txt.setFont( new Font( "Dialog", 1, 14 ) );
		txt.setBounds( new Rectangle( 1, 10, 155, 20 ) );
		txt.setEditable( false );
//		lab_progress.setBounds( new Rectangle( 5, 132, 475, 20 ) );
		// lab_progress.setText("Пересылается ");
		butOpen.setText( "Выбрать файлы" );
		butOpen.setBounds( new Rectangle( 650, 10, 145, 22 ) );
		butOpen.setFont( new Font( "Dialog", 1, 12 ) );
		butDel.setText( "Удалить из списка" );
		butDel.setBounds( new Rectangle( 650, 40, 145, 22 ) );
		butDel.setFont( new Font( "Dialog", 1, 12 ) );
		butSend.setText( "Загрузить >>" );
		butSend.setBounds( new Rectangle( 650, 86, 145, 22 ) );
		butSend.setFont( new Font( "Dialog", 1, 12 ) );
		butSend.setEnabled( false );
		lst.setBounds( new Rectangle( 160, 10, 480, 100 ) );
		jPanel2.add( lst, null );
		jPanel2.add( butSend, null );
		jPanel2.add( butDel, null );
		jPanel2.add( butOpen, null );
		jPanel2.add( txt, null );
//		jPanel2.add( lab_progress, null );
		this.getContentPane().add( jPanel2, null );
		this.getContentPane().add( jPanel1, BorderLayout.WEST );

	}

}
