package z.hol.net.download;

import java.io.IOException;

public class DownloadTest {

	public static void main (String[] args) throws IOException{
		String url = "http://docs.python.org/archives/python-2.7.3-docs-html.tar.bz2";
		MultiThreadDownload download = new MultiThreadDownload(url);
		download.startDownload();
		System.in.read();
	}
}
