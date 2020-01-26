package github.fullstackflamingo.dropboxfilemanagerapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import com.dropbox.core.v2.files.FileMetadata;

import java.io.ByteArrayOutputStream;
import java.io.File;


class DropboxDownloadFileTask extends AsyncTask<FileMetadata, Void, Boolean> {
    private final String TAG = "DropboxDownloadJSONFileTask";
    private final Callback mCallback;
    private final Context ctx;
    private Exception mException;

    public interface Callback {
        void onComplete(Boolean result);
        void onError(Exception e);
    }

    DropboxDownloadFileTask(Context ctx, Callback callback) throws Exception {
        this.ctx = ctx;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onComplete(result);
        }
    }

    @Override
    protected Boolean doInBackground(FileMetadata... params) {
        FileMetadata metadata = params[0];
        try {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);

            // Make sure the Downloads directory exists.
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    mException = new RuntimeException("Unable to create directory: " + path);
                }
            } else if (!path.isDirectory()) {
                mException = new IllegalStateException("Download path is not a directory: " + path);
                return null;
            }

            // Download the file.
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                DropboxGlobal.getClient(ctx).files().download(metadata.getPathLower(), metadata.getRev())
                        .download(outputStream);

            File file = new File(path, metadata.getName());

            // Tell android about the file
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(file));
            ctx.sendBroadcast(intent);

        } catch (Exception e) {
            mException = e;
        }

        return null;
    }
}
