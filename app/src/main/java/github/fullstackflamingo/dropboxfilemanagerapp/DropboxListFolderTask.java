package github.fullstackflamingo.dropboxfilemanagerapp;


import android.content.Context;
import android.os.AsyncTask;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
            // initial 2000 entries
            ListFolderResult result = DropboxGlobal.getClient(ctx).files().listFolder(params[0]);

            // while loop to collect more results if there are more than 2000 results
            while (true) {
                if (!result.getHasMore()) {
                    break;
                }
                ListFolderResult moreResults = DropboxGlobal.getClient(ctx).files().listFolderContinue(result.getCursor());
                // merge results with new results and loop
                List<Metadata> newList = new ArrayList<Metadata>(result.getEntries());
                newList.addAll(moreResults.getEntries());
                result = new ListFolderResult(newList, moreResults.getCursor(), moreResults.getHasMore());
            }

            // sort reverse alphabetical to show newest datestamp-named files first
            result.getEntries().sort(new Comparator<Metadata>() {
                @Override
                public int compare(Metadata o1, Metadata o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            return result;
        } catch (Exception e) {
            mException = e;
        }

        return null;
    }
}