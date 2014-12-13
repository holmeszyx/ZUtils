package z.hol.utils.media;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;

public class MediaUtils {
	
	//**************提取自API2.2***********************
	//扫描新文件
    static class ClientProxy implements MediaScannerConnectionClient {
        final String[] mPaths;
        final String[] mMimeTypes;
        final OnScanCompletedListener mClient;
        MediaScannerConnection mConnection;
        int mNextPath;

        ClientProxy(String[] paths, String[] mimeTypes, OnScanCompletedListener client) {
            mPaths = paths;
            mMimeTypes = mimeTypes;
            mClient = client;
        }

        public void onMediaScannerConnected() {
            scanNextPath();
        }

        public void onScanCompleted(String path, Uri uri) {
            if (mClient != null) {
                mClient.onScanCompleted(path, uri);
            }
            scanNextPath();
        }

        void scanNextPath() {
            if (mNextPath >= mPaths.length) {
                mConnection.disconnect();
                return;
            }
            String mimeType = mMimeTypes != null ? mMimeTypes[mNextPath] : null;
            mConnection.scanFile(mPaths[mNextPath], mimeType);
            mNextPath++;
        }
    }
	
    /**
     * Interface for notifying clients of the result of scanning a
     * requested media file.
     */
    public interface OnScanCompletedListener {
        /**
         * Called to notify the client when the media scanner has finished
         * scanning a file.
         * @param path the path to the file that has been scanned.
         * @param uri the Uri for the file if the scanning operation succeeded
         * and the file was added to the media database, or null if scanning failed.
         */
        public void onScanCompleted(String path, Uri uri);
    }



    public static void scanFile(Context context, String[] paths, String[] mimeTypes,
            OnScanCompletedListener callback) {
        ClientProxy client = new ClientProxy(paths, mimeTypes, callback);
        MediaScannerConnection connection = new MediaScannerConnection(context, client);
        client.mConnection = connection;
        connection.connect();
    }
	//扫描新文件    
	//**************提取自API2.2***********************

}
