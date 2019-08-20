package com.example.pickfile.view.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.pickfile.R;
import com.example.pickfile.databinding.ActivityMainBinding;
import com.example.pickfile.utils.FileNamePath;
import com.example.pickfile.utils.GridSpacingItemDecoration;
import com.example.pickfile.utils.Permissions;
import com.example.pickfile.view.adapter.DocumentAdapter;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;
    String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private int RC_PERMISSIONS = 1;
    private static final int RC_PICK = 2, RC_CAPTURE = 3;
    private File mFile = null;
    private DocumentAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBinding();
        setupRecyclerView(binding.fileRecyclerView);
        setupRecyclerListener();
    }

    private void initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        adapter = new DocumentAdapter();
        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 10, true));
    }

    private void setupRecyclerListener() {
        adapter.setListener((document, position) -> {
            adapter.removeDocument(position);
            updateUI();
        });
    }


    public void onLayoutClick(View view) {
        if (Permissions.hasPermissions(this, PERMISSIONS)) {
            displayImageDialog();
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, RC_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean valid = true;
        if (requestCode == RC_PERMISSIONS && grantResults.length > 0) {
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED)
                    valid = true;
                else {
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                    valid = false;
                }
            }

            if (valid) {
                displayImageDialog();
            }
        }
    }


    private void displayImageDialog() {
        final CharSequence[] items = {"Capture Photo", "Gallery","Image Picker",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, (dialog, item) -> {

            if (items[item].equals("Capture Photo")) {
                capturePhoto();
            } else if (items[item].equals("Image Picker")) {
                showImagePickerScreen();
            }else if (items[item].equals("Gallery")) {
                showDocumentDialog();
            } else if (items[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


    private void showDocumentDialog() {
        Intent intent = new Intent();
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent = Intent.createChooser(intent, "Select image");
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            String[] mimetypes = {"image/*"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        }
        startActivityForResult(intent, RC_PICK);
    }


    private void capturePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mFile = FileNamePath.createTemporalFile(this);
        Uri mFileUri;
        if (Build.VERSION.SDK_INT < 24) {
            mFileUri = Uri.fromFile(mFile);
        } else {
            mFileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", mFile);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, RC_CAPTURE);
    }

    private void showImagePickerScreen() {
        Intent intent = new Intent();
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_PICK);
            intent = Intent.createChooser(intent, "Select Image");
        } else {
            intent.setAction(Intent.ACTION_PICK);
            String[] mimetypes = {"image/*"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, RC_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PICK && resultCode == Activity.RESULT_OK && data != null) {
            Uri mainPath = data.getData();
            if (mainPath != null) {
                Log.d(TAG,"uriPath "+mainPath);
                String path = FileNamePath.getPathFromUri(this, mainPath);
                Log.d(TAG,"path "+path);
                if (path != null) {
                    mFile = new File(path);
                    adapter.addDocument(mFile);
                }
            }

        } else if (requestCode == RC_CAPTURE && resultCode == Activity.RESULT_OK) {
            mFile = FileNamePath.reduceFileSize(mFile);
            adapter.addDocument(mFile);
        }


        updateUI();
    }

    private void updateUI(){
        binding.fileRecyclerView.setVisibility(adapter.getList().size()>0?View.VISIBLE:View.GONE);
        binding.emptyView.setVisibility(adapter.getList().size()<1?View.VISIBLE:View.GONE);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_add) {
            onLayoutClick(item.getActionView());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
