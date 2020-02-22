package github.fullstackflamingo.dropboxfilemanagerapp;

import android.content.Context;
import android.widget.ImageView;

import com.dropbox.core.v2.files.FileMetadata;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

/**
 * Singleton instance of Picasso pre-configured
 */
public class PicassoClient {
    private static Picasso picasso;

    public static Picasso init(Context context) throws Exception {
        if (picasso == null) {
            // Configure picasso to know about special thumbnail requests
            picasso = new Picasso.Builder(context)
                    .downloader(new OkHttp3Downloader(context, Integer.MAX_VALUE))
                    .addRequestHandler(new FileThumbnailRequestHandler(context, DropboxGlobal.getClient(context)))
                    .build();
            /*  picasso.setIndicatorsEnabled(true);
            picasso.setLoggingEnabled(true); */
            Picasso.setSingletonInstance(picasso);
        }
        return picasso;
    }


    public static Picasso getPicasso(Context context) throws Exception {
        if (picasso == null) return PicassoClient.init(context);
        return picasso;
    }

    public static void loadDropboxImageFileIntoListView(Context context, FileMetadata md, ImageView imageView, com.squareup.picasso.Callback cb) throws Exception {
        if (picasso == null) PicassoClient.init(context);
        picasso.load(FileThumbnailRequestHandler.getImageFileMetaData(md)).into(imageView, cb);
    }

    public static void loadDropboxThumbnailIntoListView(Context context, FileMetadata md, ImageView imageView, com.squareup.picasso.Callback cb) throws Exception {
        if (picasso == null) PicassoClient.init(context);
        picasso.load(FileThumbnailRequestHandler.getThumbnailFromFileMetaData(md)).into(imageView, cb);
    }
    public static void loadDropboxLargeThumbnailIntoView(Context context, FileMetadata md, ImageView imageView, com.squareup.picasso.Callback cb) throws Exception {
        if (picasso == null) PicassoClient.init(context);
        picasso.load(FileThumbnailRequestHandler.getLargeThumbnailFromFileMetaData(md)).into(imageView, cb);
    }
}