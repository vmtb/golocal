package geolocale.city.bj.gallery;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.FileDescriptorBitmapDecoder;
import com.bumptech.glide.load.resource.bitmap.VideoBitmapDecoder;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import geolocale.city.bj.R;


public class BottomGallery extends BottomSheetDialogFragment implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private View root;

    public static closeBottomDialog closeBottomDialog;
    private Context context;
    private ImageView close, check;
    private GridView gridView;
    private Spinner spinner;
    private List<String> folders_paths= new ArrayList<>();
    private List<File> files_paths= new ArrayList<>();
    private List<String> names_lists= new ArrayList<>();
    private List<File> thumbs_lists= new ArrayList<>();
    private ProgressBar pbar;
    private Context parentContext;
    private List<File> selectedFile =new ArrayList<>();
    private List<File> selectedThumbs =new ArrayList<>();
    private boolean single_select_type=true;
    private boolean isImageType= true;

    public void setCloseBottomDialog(BottomGallery.closeBottomDialog closeBottomDialog) {
        BottomGallery.closeBottomDialog = closeBottomDialog;
    }

    public BottomGallery(Context parentContext, boolean single_select_type) {
        this.parentContext = parentContext;
        this.single_select_type= single_select_type;
    }
    public BottomGallery(Context parentContext, boolean single_select_type, boolean isImageType) {
        this.parentContext = parentContext;
        this.single_select_type= single_select_type;
        this.isImageType=isImageType;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v=inflater.inflate(R.layout.layout_gallery,container,false);
        context= getContext();

        initComponents(v);
        getLoaderManager().initLoader(0, null, BottomGallery.this);

        return v;
    }

    private void initComponents(View v){
        root= v.findViewById(R.id.root);
        close= (ImageView)v.findViewById(R.id.close);
        check= (ImageView)v.findViewById(R.id.check);
        gridView=(GridView)v.findViewById(R.id.gridView);
        spinner=(Spinner) v.findViewById(R.id.spinner);
        pbar=(ProgressBar) v.findViewById(R.id.pbar);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeBottomDialog.onCloseListener();
            }
        });

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedFile.size()<1)
                    Toast.makeText(context, "Aucun fichier sélectionné", Toast.LENGTH_SHORT).show();
                else{
                    closeBottomDialog.onSelectFinished(selectedFile, selectedThumbs, single_select_type, isImageType);
                    closeBottomDialog.onCloseListener();
                }
            }
        });
    }

    private class ImageFileFilter implements FileFilter{
        boolean folder_only=false;

        public ImageFileFilter(boolean folder_only) {
            this.folder_only = folder_only;
        }

        @Override
        public boolean accept(File pathname) {
            if(pathname.isDirectory())
                return true;
            else if (isImageFile(pathname.getAbsolutePath()))
                return true;
            return false;
        }
    }

    private boolean isImageFile(String path){
        if(path.toLowerCase().endsWith(".jpg")|| path.toLowerCase().endsWith(".png"))
            return true;

        return false;
    }

    private void loadFolders() {

        names_lists.add("Gallerie d'image");
        for (int i = 0; i < folders_paths.size(); i++) {
            names_lists.add(new File(folders_paths.get(i)).getName());
        }
        ArrayAdapter arr = new ArrayAdapter(context, android.R.layout.simple_list_item_1, names_lists);
        arr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arr);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==0)
                    loadImagesWith(files_paths, isImageType?null:thumbs_lists);
                else{
                    List<File> files_img_paths= filterFiles(files_paths, folders_paths.get(position-1), null);
                    List<File> files_thumbs= filterFiles(files_paths, folders_paths.get(position-1), thumbs_lists);
                    loadImagesWith(files_img_paths, files_thumbs);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void loadImagesWith(final List<File> files_img_paths, final List<File> thumbs_lists) {

        List<GridViewItem> gridViewItems= new ArrayList<>();
        for (int i = 0; i < files_img_paths.size(); i++) {
            GridViewItem gv= new GridViewItem();
            gv.setFile(files_img_paths.get(i));
            gridViewItems.add(gv);
        }

        GridViewAdapter gridViewAdapter= new GridViewAdapter(context, gridViewItems);
        ArrayAdapter aa= new ArrayAdapter(context, android.R.layout.simple_list_item_1, files_img_paths);
        gridView.setAdapter(gridViewAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*if (!selectedFile.contains(files_img_paths.get(position))) */{
                    if(!single_select_type){
                        if (selectedFile.contains(files_img_paths.get(position))) {
                            view.setBackground(null);
                            selectedFile.remove(files_img_paths.get(position));
                            if(thumbs_lists!=null)
                                selectedThumbs.remove(thumbs_lists.get(position));
                        } else {
                            //view.setBackgroundColor(getResources().getColor(R.color.selectedColor));
                            selectedFile.add(files_img_paths.get(position));
                            if(thumbs_lists!=null)
                                selectedThumbs.add(thumbs_lists.get(position));
                        }
                    }else{
                        selectedFile.add(files_img_paths.get(position));
                        if(thumbs_lists!=null)
                            selectedThumbs.add(thumbs_lists.get(position));
                        closeBottomDialog.onSelectFinished(selectedFile, selectedFile, true, isImageType);
                    }
                }
            }
        });
        if(!single_select_type) {
            check.setVisibility(View.VISIBLE);
            gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (selectedFile.contains(files_img_paths.get(position))) {
                        view.setBackground(null);
                        selectedFile.remove(files_img_paths.get(position));
                        if(thumbs_lists!=null)
                            selectedThumbs.remove(thumbs_lists.get(position));
                    } else {
                       // view.setBackgroundColor(getResources().getColor(R.color.selectedColor));
                        selectedFile.add(files_img_paths.get(position));
                        if(thumbs_lists!=null)
                            selectedThumbs.remove(thumbs_lists.get(position));
                    }
                    return false;
                }
            });
        }else{
            check.setVisibility(View.INVISIBLE);
        }
    }

    private List<File> filterFiles(List<File> files_paths, String s, List<File> thumbs_lists) {
        List<File> fileFiltered= new ArrayList<>();
        int i=0;
        for (File file:files_paths ) {
            if(file.getAbsolutePath().startsWith(s)) {
                fileFiltered.add(file);
            }
            i++;
        }
        return fileFiltered;
    }

    private void getAllImagesPaths(Cursor cursor){

        try {
            if(cursor!=null)
                cursor.moveToFirst();
            else
                Log.d("Cursor null", "Cursor null");
            while(cursor.moveToNext()){
                String name="", folder="", datapaths="", folderpath="", thumb_path="";
                if(isImageType){
                    name=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                    folder=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    datapaths=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    folderpath= datapaths.substring(0, datapaths.indexOf(folder+"/"));
                }else{
                    name=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                    folder=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                    datapaths=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    thumb_path=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
                    folderpath= datapaths.substring(0, datapaths.indexOf(folder+"/"));
                    thumbs_lists.add(new File(thumb_path));
                }
                folderpath= folderpath+folder+"/";
                if(!folders_paths.contains(folderpath))
                    folders_paths.add(folderpath);
                files_paths.add(new File(datapaths));

            }
            cursor.close();
            pbar.setVisibility(View.INVISIBLE);
            loadFolders();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Error_display_img", e.getMessage()+"");
            pbar.setVisibility(View.INVISIBLE);
        }

    }

    private class GridViewAdapter extends BaseAdapter {

        List<GridViewItem> items;
        LayoutInflater inflater;
        Context ctx;
        public GridViewAdapter(Context ctx, List<GridViewItem> items) {
            this.items = items;
            inflater= (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.ctx=ctx;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //if(convertView==null)
            convertView= inflater.inflate(R.layout.item_grid_img, null);
            ImageView img= (ImageView)convertView.findViewById(R.id.imageView);
            //img.setImageURI(Uri.fromFile(items.get(position).getFile()));
            if(isImageType)
                Picasso.with(ctx).load(items.get(position).getFile()).fit().centerCrop().into(img);
            else {
                BitmapPool bitmapPool = Glide.get(ctx).getBitmapPool();
                VideoBitmapDecoder videoBitmapDecoder = new VideoBitmapDecoder(3000000);
                FileDescriptorBitmapDecoder fileDescriptorBitmapDecoder = new FileDescriptorBitmapDecoder(videoBitmapDecoder
                        , bitmapPool, DecodeFormat.PREFER_ARGB_8888);
                try {
                    Glide.with(ctx).load(Uri.fromFile(items.get(position).getFile()))
                            .asBitmap()
                            .videoDecoder(fileDescriptorBitmapDecoder)
                            .centerCrop()
                            .into(img);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return convertView;
        }
    }


    private class GridViewItem{
        File file;

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Uri allimgUri=MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection={MediaStore.Images.Media.DATA,MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID};
        if(!isImageType){
            allimgUri=MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            projection=new String[]{MediaStore.Video.Media.DATA,MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.BUCKET_ID, MediaStore.Video.Thumbnails.DATA};
        }
        return new CursorLoader(parentContext.getApplicationContext(), allimgUri, projection, null,null, "date_modified DESC");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        getAllImagesPaths(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    public interface closeBottomDialog{
        void onCloseListener();
        void onSelectFinished(List<File> selectedFile, List<File> fileSelected, boolean singleSelected, boolean imageType);
    }


}
