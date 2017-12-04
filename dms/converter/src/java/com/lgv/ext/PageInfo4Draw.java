package com.lgv.ext;

public class PageInfo4Draw implements java.io.Serializable, Cloneable{
	public int x = 0;
	public int y = 0;
	public int dWidth = 0;
	public int dHeight = 0;
	public int nPage = 0;
	public int flags = 0x100;
	public int rotate = 0;
	public double zoom = 1.0;
	public double kPdf = 0.;
	public PageInfo4Draw() {
		super();
	}
	public PageInfo4Draw(int _nPage, int _x, int _y, int _dWidth,int _dHeight) {
		this();
		nPage = _nPage;
		dWidth = _dWidth;
		dHeight = _dHeight;
		x = _x;
		y=_y;
	}
	public PageInfo4Draw(PageInfo4Draw frvPI) {
		this();
		nPage = frvPI.nPage;
		dWidth = frvPI.dWidth;
		dHeight = frvPI.dHeight;
		x = frvPI.x;
		y=frvPI.y;
		kPdf = frvPI.kPdf;
	}
	public Object clone() {
		return new PageInfo4Draw(this);
	}
	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException 
	{
		nPage = in.readInt();
		x = in.readInt();
		y = in.readInt();
		dWidth = in.readInt();
		dHeight = in.readInt();
		flags = in.readInt();
		rotate = in.readInt();
		zoom = in.readDouble();
		kPdf = in.readDouble();
		
	}
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException 
	{
		out.writeInt(nPage);
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(dWidth);
		out.writeInt(dHeight);
		out.writeInt(flags);
		out.writeInt(rotate);
		out.writeDouble(zoom);
		out.writeDouble(kPdf);
	}
}
