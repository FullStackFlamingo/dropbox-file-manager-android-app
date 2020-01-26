package github.fullstackflamingo.dropboxfilemanagerapp;

import android.content.Context;
import android.webkit.MimeTypeMap;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;

public class DropboxGlobal {
    private static DbxClientV2 _dbxClient;

    public static DbxClientV2 getClient(Context ctx) throws DbxException {
//        AppPreferences.init(ctx);
//        final String ACCESS_TOKEN = AppPreferences.instance.getString("DbxAccessToken", null);
        String ACCESS_TOKEN = ctx.getString(R.string.DB_ACCESS_TOKEN);
        if (ACCESS_TOKEN == null) throw new DbxException("No Dropbox Access Token");

        if (_dbxClient == null) {
            DbxRequestConfig DbxLocationClientConfig = DbxRequestConfig.newBuilder("DBClient").build();
            _dbxClient = new DbxClientV2(DbxLocationClientConfig, ACCESS_TOKEN);
        }
        return _dbxClient;
    }

    public static boolean isMetaDataAnImage(Metadata md) {
        if (md instanceof FileMetadata == false) return false;

        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = md.getName().substring(md.getName().indexOf(".") + 1);
        String type = mime.getMimeTypeFromExtension(ext);
        return type != null && type.startsWith("image/");
    }

}
