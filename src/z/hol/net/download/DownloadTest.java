package z.hol.net.download;

import java.io.IOException;

public class DownloadTest {

	public static void main (String[] args) throws IOException{
		long length = MultiThreadDownload.getUrlContentLength("http://api.hulutan.net/api/gamedown/166?id=166&sw=480&sh=800&imei=A100000D9C2738&imsi=3101200000009331&md=PC36100&pf=android&ver=1.0.4&verint=4");
		System.out.println(length);
//		String url = "http://docs.python.org/archives/python-2.7.3-docs-html.tar.bz2";
//		MultiThreadDownload download = new MultiThreadDownload(url);
//		download.startDownload();
//		System.in.read();
	}
}
