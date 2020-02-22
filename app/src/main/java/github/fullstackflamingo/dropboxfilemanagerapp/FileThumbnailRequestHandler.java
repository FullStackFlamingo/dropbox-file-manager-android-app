package github.fullstackflamingo.dropboxfilemanagerapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ThumbnailFormat;
import com.dropbox.core.v2.files.ThumbnailSize;

import okio.Okio;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Example Picasso request handler that gets the thumbnail url for a dropbox path
 * Only handles urls like dropbox://dropbox/[path_to_file]
 */
public class FileThumbnailRequestHandler extends RequestHandler {

    public static final String SCHEME = "dropbox";
    public static final String HOST = "dropbox";
//    private static final String TAG = "FileThumbnailRequestHandler";
//    private static final int MAX_DISK_CACHE_SIZE = 200 * 1024 * 1024; // 200MB
    private static final String PICASSO_CACHE = "picasso-cache";

    private final ConnectivityManager conectivityManager;
    private final DbxClientV2 mDbxClient;
    private final File cacheDir;
    private final Context ctx;

    public FileThumbnailRequestHandler(Context context, DbxClientV2 dbxClient) {
        mDbxClient = dbxClient;
        ctx = context;
        conectivityManager =
                (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

        cacheDir = new File(context.getApplicationContext().getExternalCacheDir(), PICASSO_CACHE);
        if (!this.cacheDir.exists()) {
            this.cacheDir.mkdirs();
        }
    }

    /*public boolean isOnline() {
        boolean _isOnline = conectivityManager != null ? conectivityManager.getActiveNetworkInfo().isConnected() : false;
        Log.i(TAG, "online:" + _isOnline);
        return _isOnline;
    }*/

    public static Uri getLargeThumbnailFromFileMetaData(FileMetadata file) {
        return new Uri.Builder()
                .scheme(SCHEME)
                .authority(HOST)
                .path(file.getPathLower())
                .appendQueryParameter("thumb", "small")
                .build();
    }

    public static Uri getThumbnailFromFileMetaData(FileMetadata file) {
        return new Uri.Builder()
                .scheme(SCHEME)
                .authority(HOST)
                .path(file.getPathLower())
                .appendQueryParameter("thumb", "large")
                .build();
    }

    public static Uri getImageFileMetaData(FileMetadata file) {
        return new Uri.Builder()
                .scheme(SCHEME)
                .authority(HOST)
                .path(file.getPathLower())
                .build();
    }

    @Override
    public boolean canHandleRequest(Request data) {
        return SCHEME.equals(data.uri.getScheme()) && HOST.equals(data.uri.getHost());
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        try {
            Picasso.LoadedFrom loadedFrom = Picasso.LoadedFrom.DISK;
            String targetFilename = request.uri.getPath();
            targetFilename += request.uri.getQuery() != null ? request.uri.getQuery() : "";
            targetFilename = targetFilename.replaceAll("\\W+", "");
            File cachedFile = new File(cacheDir, targetFilename);

            if (!cachedFile.exists()) {
                OutputStream downloadOutputStream = ctx.getContentResolver().openOutputStream(Uri.fromFile(cachedFile));
                if (request.uri.getBooleanQueryParameter("thumb", false)) {
                    ThumbnailSize size = ThumbnailSize.W640H480;
                    if (request.uri.getQueryParameter("thumb") == "large") {
                        size = ThumbnailSize.W1024H768;
                    }
                    mDbxClient.files().getThumbnailBuilder(request.uri.getPath())
                            .withFormat(ThumbnailFormat.JPEG)
                            .withSize(size)
                            .download(downloadOutputStream);
                } else {
                    mDbxClient.files().downloadBuilder(request.uri.getPath()).download(downloadOutputStream);
                }
                loadedFrom = Picasso.LoadedFrom.NETWORK;
            }
            InputStream cachedFileStream = ctx
                    .getContentResolver().openInputStream(Uri.fromFile(cachedFile));

            return new Result(Okio.source(cachedFileStream), loadedFrom);
        } catch (DbxException e) {
            throw new IOException(e);
        }
    }
}
