package com.example.gallery;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private static final int REQUEST_PERMISSIONS=1234;

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int PERMISSION_COUNT = 2;

    @SuppressLint("NewApi")
    private boolean arePermissionDenied() {
        for(int i=0;i< PERMISSION_COUNT;i++) {
            if(checkSelfPermission(PERMISSIONS[i])!= PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode==REQUEST_PERMISSIONS && grantResults.length>0){
            if(arePermissionDenied()){
                ((ActivityManager) Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
                recreate();
            }
            else {
                onResume();
            }
        }
    }

    private List<String> filesList;

    private void addImagesFrom(String dirPath){
        final File imagesDir = new File(dirPath);
        final File[] files = imagesDir.listFiles();

        for (File file : files) {
            final String path = file.getAbsolutePath();
            if (path.endsWith(".jpg") || path.endsWith(".png") || path.endsWith(".jpeg")) {
                filesList.add(path);
            }
        }
    }

    private boolean isGalleryInitialized;

    @Override
    protected void onResume(){
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && arePermissionDenied()){
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
            return;
        }
        //Initialize our app
        if(!isGalleryInitialized){
            filesList = new ArrayList<>();
            addImagesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)));
            addImagesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
            addImagesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)));

            final ListView listView=findViewById(R.id.listView);
            final GalleryAdaptor galleryAdaptor =new GalleryAdaptor();

            galleryAdaptor.setData(filesList);
            listView.setAdapter(galleryAdaptor);

            final TextView imageName = findViewById(R.id.imageName);
            final View topBar = findViewById(R.id.topBar);

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    imageName.setVisibility(View.VISIBLE);
                    imageName.setText(filesList.get(position).substring(filesList.get(position).lastIndexOf('/')+1));
                    topBar.setVisibility(View.VISIBLE);
                    return false;
                }
            });

            isGalleryInitialized=true;
        }
    }

    final class GalleryAdaptor extends BaseAdapter {

        private List<String> data= new ArrayList<>();

        void setData(List<String> data){
            if(this.data.size()>0){
                data.clear();
            }
            this.data.addAll(data);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ImageView imageView;
            if(convertView==null){
                imageView= (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent, false);
            }
            else {
                imageView = (ImageView) convertView;
            }
            Glide.with(MainActivity.this).load(data.get(position)).centerInside().into(imageView);
            return imageView;
        }
    }
}
