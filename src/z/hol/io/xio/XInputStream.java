package z.hol.io.xio;

import java.io.IOException;
import java.io.InputStream;

public class XInputStream extends InputStream{
	private static final byte KEY = 0x73;
	
	private int mPos = 2;
	private InputStream mIn;
	
	public XInputStream(InputStream in){
		mIn = in;
	}
	

	@Override
	public int read() throws IOException {
		// TODO Auto-generated method stub
		int b = mIn.read();
		if (b == -1) return -1;
		int p = (mPos * mPos * mPos) >> 1;
		int f = (p & 0x000000FF) ^ KEY ^ b;
		mPos ++;
		return f;
	}
	
	@Override
	public int available() throws IOException {
		// TODO Auto-generated method stub
		//return super.available();
		if (mIn != null){
			return mIn.available();
		}
		return 0;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		super.close();
		mIn.close();
	}
}
