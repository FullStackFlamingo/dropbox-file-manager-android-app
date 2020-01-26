package github.fullstackflamingo.dropboxfilemanagerapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.squareup.picasso.Picasso;

public class FolderFileListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "FolderFileListAdapter";
    private ListItem[] dataset;
    private Callback listItemCallback;
    private Context ctx;

    public enum LIST_MODE {
        CARD,
        LIST
    }

    private LIST_MODE listMode = LIST_MODE.CARD;

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

    public static class ListItemHolder extends RecyclerView.ViewHolder {
        public View itemView;
        public TextView textView;
        public ImageView imageView;
        public ProgressBar spinnerView;

        public ListItemHolder(View item) {
            super(item);
            itemView = item;
            textView = item.findViewById(R.id.folder_file_list_item_text);
            imageView = item.findViewById(R.id.folder_file_list_item_image);
            spinnerView = item.findViewById(R.id.folder_file_list_item_spinner);
        }
    }

    public FolderFileListAdapter(Context ctx, Callback listItemCallback) {
        this.listItemCallback = listItemCallback;
        this.ctx = ctx;
    }

    public void updateDataset(ListItem[] result) {
        dataset = result;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        switch (listMode) {
            case LIST:
            case CARD:
            default:
                return new ListItemHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.imagelistitem, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder _holder, int position) {
        final Metadata md = dataset[position].md;

        if (_holder instanceof ListItemHolder) {
            final ListItemHolder holder = (ListItemHolder) _holder;
            holder.textView.setText(md.getName());
            if (md instanceof FileMetadata) {
                if (DropboxGlobal.isMetaDataAnImage(md)) {
                    try {
                        PicassoClient.loadDropboxImageIntoListView(ctx, (FileMetadata) md, holder.imageView, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                holder.spinnerView.setVisibility(View.INVISIBLE);
                                holder.imageView.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().cancelRequest(holder.imageView);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (md instanceof FileMetadata) {
                        listItemCallback.onClick((FileMetadata) md);
                    } else {
                        listItemCallback.onClick(md.getPathDisplay());
                    }
                }
            });
        }

    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder _holder) {
        super.onViewRecycled(_holder);
        if (_holder instanceof ListItemHolder) {
            ListItemHolder holder = (ListItemHolder) _holder;
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