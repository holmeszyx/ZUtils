package z.hol.io.xio;

import java.io.IOException;
import java.io.OutputStream;

public class XOutputStream extends OutputStream{
	private static final byte KEY = 0x73;

	private int mPos = 2;
	
	private OutputStream mOut;
	
	public XOutputStream(OutputStream out){
		mOut = out;
	}
	
	@Override
	public void write(int b) throws IOException {
		// TODO Auto-generated method stub
		int p = (mPos * mPos * mPos) >> 1;
		int f = (p & 0x000000FF) ^ KEY ^ b;
		mOut.write(f);
		mPos ++;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		super.close();
		mOut.close();
	}
}
