package github.fullstackflamingo.dropboxfilemanagerapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ViewGroup loadingSpinner;
    private RecyclerView recyclerView;
    private FolderFileListAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private ActionBar actionBar;

    private Boolean loading = false;

    private String currentPath = "";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainlayout);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        loadingSpinner = findViewById(R.id.folder_file_list_item_spinner);

        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayout.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        // specify an adapter (see also next example)
        mAdapter = new FolderFileListAdapter(this, new FolderFileListAdapter.Callback() {
            @Override
            public void onClick(String newPath) {
                if (loading) return;
                getDBxList(newPath);
            }

            @Override
            public void onClick(FileMetadata md) {
                if (loading) return;
            }
        });
        recyclerView.setAdapter(mAdapter);

        getDBxList();
    }

    private void getDBxList() {
        getDBxList(currentPath);
    }

    @NonNull
    private void getDBxList(String newPath) {
        if (loading) return;
        final String pathToLoad = newPath != null ? newPath : currentPath;
        loading = true;
        loadingSpinner.setVisibility(View.VISIBLE);
        try {
            new DropboxListFolderTask(this, new DropboxListFolderTask.Callback() {
                @Override
                public void onComplete(ListFolderResult result) {
                    loading = false;
                    loadingSpinner.setVisibility(View.INVISIBLE);
                    currentPath = pathToLoad;
                    actionBar.setDisplayHomeAsUpEnabled(currentPath != "");
                    // results list to ListItem array
                    List<Metadata> list = result.getEntries();
                    FolderFileListAdapter.ListItem[] datasetArray = new FolderFileListAdapter.ListItem[list.size()];
                    //for(int i = list.size() - 1; i >= 0; i--) {
                    for (int i = 0; i < list.size(); i += 1) {
                        datasetArray[list.size() - 1 - i] = new FolderFileListAdapter.ListItem(list.get(i));
                    }
                    mAdapter.updateDataset(datasetArray);
                }

                @Override
                public void onError(Exception e) {
                    loading = false;
                    loadingSpinner.setVisibility(View.INVISIBLE);
                    e.printStackTrace();

                }
            }).execute(pathToLoad);
        } catch (Exception e) {
            e.printStackTrace();
            loading = false;
            loadingSpinner.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (currentPath == "") {
            super.onBackPressed();
            return;
        }
        currentPath = "";
        getDBxList();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                getDBxList();
                return true;
            case R.id.switch_to_list:
                mAdapter.setListMode(FolderFileListAdapter.LIST_MODE.LIST);
                return true;
            case R.id.switch_to_card:
                mAdapter.setListMode(FolderFileListAdapter.LIST_MODE.CARD);
                return true;
            /*case R.id.settings:
                // TODO: Open dialog to set encrypted Dropbox access token via AppPreferences
                // https://developer.android.com/guide/topics/ui/dialogs#CustomLayout
                return true;*/
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


}