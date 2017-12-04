package com.lgv.ext;

/**
 * den
 * Creation date: (15.05.2006)
 */
public class PageInfo implements java.io.Serializable, Cloneable {
	public int nPage;
	public int dWidth;
	public int dHeight;
	public int nError;

	//c means const
	/*
	public final static String cnPage = "nPage";
	public final static String cdWidth = "dWidth";
	public final static String cdHeight = "dHeight";
	public final static String cnError = "nError";
	*/

	public PageInfo() {
		super();
	}
	public PageInfo(int anPage, int adWidth, int adHeight) {
		this();
		nPage = anPage;
		dWidth = adWidth;
		dHeight = adHeight;
		nError = 0;
	}
	public PageInfo(PageInfo frvPI) {
		this();
		nPage = frvPI.nPage;
		dWidth = frvPI.dWidth;
		dHeight = frvPI.dHeight;
		nError = frvPI.nError;
	}
	/**
	 * den
	 * Creation date: (15.05.2006)
	 * @return java.lang.Object
	 */
	public Object clone() {
		return new PageInfo(this);
	}
	/**
	 * den
	 * Creation date: (15.05.2006)
	 * @param in java.io.ObjectInputStream
	 * @exception java.io.IOException The exception description.
	 */
	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException 
	{
		nPage = in.readInt();
		dWidth = in.readInt();
		dHeight = in.readInt();
		nError = in.readInt();
	}
	/**
	 * den
	 * Creation date: (15.05.2006)
	 * @param out java.io.ObjectOutputStream
	 * @exception java.io.IOException The exception description.
	 */
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException 
	{
		out.writeInt(nPage);
		out.writeInt(dWidth);
		out.writeInt(dHeight);
		out.writeInt(nError);
	}
}
