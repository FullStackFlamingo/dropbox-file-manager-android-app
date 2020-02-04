package github.fullstackflamingo.dropboxfilemanagerapp;

import android.net.Uri;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ThumbnailFormat;
import com.dropbox.core.v2.files.ThumbnailSize;

import okio.Okio;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;

/**
 * Example Picasso request handler that gets the thumbnail url for a dropbox path
 * Only handles urls like dropbox://dropbox/[path_to_file]
 */
public class FileThumbnailRequestHandler extends RequestHandler {

    public static final String SCHEME = "dropbox";
    public static final String HOST = "dropbox";

    private final DbxClientV2 mDbxClient;

    public FileThumbnailRequestHandler(DbxClientV2 dbxClient) {
        mDbxClient = dbxClient;
    }

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
            DbxDownloader<FileMetadata> downloader;
            if (request.uri.getBooleanQueryParameter("thumb", false)) {
                ThumbnailSize size = ThumbnailSize.W640H480;
                if (request.uri.getQueryParameter("thumb") == "large") {
                    size = ThumbnailSize.W1024H768;
                }

                downloader = mDbxClient.files().getThumbnailBuilder(request.uri.getPath())
                        .withFormat(ThumbnailFormat.JPEG)
                        .withSize(size)
                        .start();
            } else {
                downloader = mDbxClient.files().downloadBuilder(request.uri.getPath())
                        .start();
            }


            return new Result(Okio.source(downloader.getInputStream()), Picasso.LoadedFrom.NETWORK);
        } catch (DbxException e) {
            throw new IOException(e);
        }
    }
}
