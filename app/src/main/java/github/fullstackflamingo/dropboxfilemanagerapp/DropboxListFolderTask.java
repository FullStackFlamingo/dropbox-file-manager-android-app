package github.fullstackflamingo.dropboxfilemanagerapp;


import android.content.Context;
import android.os.AsyncTask;
import com.dropbox.core.v2.files.ListFolderResult;

class DropboxListFolderTask extends AsyncTask<String, Void, ListFolderResult> {
    private final String TAG = "DropboxListFolderTask";
    private final Callback mCallback;
    private final Context  ctx;
    private Exception mException;

    public DropboxListFolderTask(Context ctx, Callback callback) throws Exception {
        mCallback = callback;
        this.ctx = ctx;
    }

    public interface Callback {
        void onComplete(ListFolderResult result);

        void onError(Exception e);
    }

    @Override
    protected void onPostExecute(ListFolderResult result) {
        super.onPostExecute(result);

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onComplete(result);
        }
    }

    @Override
    protected ListFolderResult doInBackground(String... params) {
        try {
            return DropboxGlobal.getClient(ctx).files().listFolder(params[0]);
        } catch (Exception e) {
            mException = e;
        }

        return null;
    }
}