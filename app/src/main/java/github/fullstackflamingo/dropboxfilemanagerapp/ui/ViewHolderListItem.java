package github.fullstackflamingo.dropboxfilemanagerapp.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import github.fullstackflamingo.dropboxfilemanagerapp.R;


public class ViewHolderListItem extends RecyclerView.ViewHolder {
    public int viewType;
    public View itemView = null;
    public TextView textView = null;
    public ImageView imageView = null;
//    public Button deleteButtonView = null;
    public ProgressBar spinnerView = null;
    public LIST_MODE listMode;


    public ViewHolderListItem(View item) {
        super(item);
        itemView = item;
        textView = item.findViewById(R.id.listitem__text);
        imageView = item.findViewById(R.id.listitem__image);
        spinnerView = item.findViewById(R.id.listitem__spinner);
//        deleteButtonView = item.findViewById(R.id.list_item_delete_button);
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public void setlistMode(LIST_MODE listMode) {
        this.listMode = listMode;
    }
}