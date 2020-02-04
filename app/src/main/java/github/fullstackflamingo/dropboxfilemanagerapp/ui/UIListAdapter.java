package github.fullstackflamingo.dropboxfilemanagerapp.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.squareup.picasso.Picasso;

import github.fullstackflamingo.dropboxfilemanagerapp.DropboxGlobal;
import github.fullstackflamingo.dropboxfilemanagerapp.PicassoClient;
import github.fullstackflamingo.dropboxfilemanagerapp.R;

public class UIListAdapter extends RecyclerView.Adapter<ViewHolderListItem> {
    private static final String TAG = "UIListAdapter";
    private static final int VIEW_TYPE_FOLDER = 0;
    private static final int VIEW_TYPE_FILE = 1;
    private static final int VIEW_TYPE_IMAGE = 2;

    private ListItem[] dataset;
    private Callback listItemCallback;
    private Context ctx;
    private LIST_MODE listMode = LIST_MODE.LIST;

    public void setListMode(LIST_MODE lm) {
        listMode = lm;
    }

    public interface Callback {
        void onClick(String newPath);

        void onClick(FileMetadata md);
    }

    public static class ListItem {
        Metadata md = null;

        public ListItem(Metadata md) {
            this.md = md;
        }
    }


    public UIListAdapter(Context ctx, Callback listItemCallback) {
        this.listItemCallback = listItemCallback;
        this.ctx = ctx;
    }

    public void updateDataset(ListItem[] result) {
        dataset = result;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolderListItem onCreateViewHolder(ViewGroup parent,
                                                 int viewType) {
        int layout;
        switch (viewType) {
            case VIEW_TYPE_IMAGE:
                layout = listMode == LIST_MODE.LIST ? R.layout.list_item_image : R.layout.card_list_item_image;
                break;
            case VIEW_TYPE_FILE:
                layout = listMode == LIST_MODE.LIST ? R.layout.list_item_file : R.layout.card_list_item_file;
                break;
            case VIEW_TYPE_FOLDER:
            default:
                layout = listMode == LIST_MODE.LIST ? R.layout.list_item_folder : R.layout.card_list_item_folder;
        }
        ViewHolderListItem vh = new ViewHolderListItem(LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false));
        vh.setViewType(viewType);
        vh.setlistMode(listMode);
        return vh;
    }

    @Override
    public int getItemViewType(int position) {
        final Metadata md = dataset[position].md;
        if (DropboxGlobal.isMetaDataAnImage(md)) return VIEW_TYPE_IMAGE;
        if (DropboxGlobal.isMetaDataAFile(md)) return VIEW_TYPE_FILE;
        return VIEW_TYPE_FOLDER;
    }

    @Override
    public void onBindViewHolder(final ViewHolderListItem holder, int position) {
        final Metadata md = dataset[position].md;

        holder.textView.setText(md.getName());
        if (holder.viewType == VIEW_TYPE_IMAGE) {
            com.squareup.picasso.Callback cb = new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    holder.spinnerView.setVisibility(View.INVISIBLE);
                    holder.imageView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(Exception e) {
                    holder.spinnerView.setVisibility(View.INVISIBLE);
                    holder.imageView.setVisibility(View.VISIBLE);
                    Picasso.get().cancelRequest(holder.imageView);
                }
            };
            try {
                if (holder.listMode == LIST_MODE.CARD) {
                    PicassoClient.loadDropboxImageFileIntoListView(ctx, (FileMetadata) md, holder.imageView, cb);
                } else {
                    PicassoClient.loadDropboxThumbnailIntoListView(ctx, (FileMetadata) md, holder.imageView, cb);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (holder.viewType == VIEW_TYPE_FOLDER) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    listItemCallback.onClick(md.getPathDisplay());
                }
            });
        }


    }

    @Override
    public void onViewRecycled(@NonNull ViewHolderListItem holder) {
        super.onViewRecycled(holder);
        if (holder.viewType == VIEW_TYPE_IMAGE) {
            Picasso.get().cancelRequest(holder.imageView);
            holder.spinnerView.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        if (dataset != null) return dataset.length;
        return 0;
    }
}