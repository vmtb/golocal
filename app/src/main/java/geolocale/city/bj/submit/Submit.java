package geolocale.city.bj.submit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import geolocale.city.bj.R;
import geolocale.city.bj.gallery.BottomGallery;
import geolocale.city.bj.login_signup.Signin;
import geolocale.city.bj.tools.Cte;
import geolocale.city.bj.tools.ImageCompress;
import geolocale.city.bj.tools.User;

public class Submit extends AppCompatActivity {

    private TextInputEditText localisation, title, desc;
    private TextInputLayout descLayout;
    private TextView add1, add2, changeLoc;
    private ImageView img1, img2;
    private LinearLayout card1, card2;
    private Spinner commune, arrond, category;
    private Button submit;
    private FusedLocationProviderClient client;
    private double longitude=35, latitude=-15;
    private ProgressDialog pdial;
    private CheckBox anonymate;

    private LinearLayout nvLayout;
    private RelativeLayout headnvLayout;
    private Chronometer recordTime;
    private ImageButton recordBtn;
    private TextView play, delete;


    private boolean isRecording = false, isAudioType=false;

    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;

    private MediaRecorder mediaRecorder;
    private String recordFile;

    private LocationManager locationManager;

    private String link_image="", link_audio="";

    private int commune_index=0, arrond_index=0,category_id=0;
    private String commune_select="";
    private String [] communes ={"Commune", "Avrankou", "Toffo", "Banikora", "Abomey"};
    private int [] communes_index ={0, 1,2, 3, 4};

    private List<String> categories_list= new ArrayList<>();

    private File[] imageFiles= new File[2];

    private String user_name="Anonymat", token="", phone="", password="", user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_submit);
        pdial= new ProgressDialog(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Nouvelle soumission");
        locationManager= (LocationManager)getSystemService(LOCATION_SERVICE);
        initViews();
        setActions();
        getCommunes();
        getCategories();
    }

    private void getCategories() {
        categories_list.add("Catégorie");
        RequestQueue requestQueue=  Volley.newRequestQueue(this);
        String url= Cte.BASE_URL+"/categories/";
        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.GET
                , url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray datas= response.getJSONArray("data");
                    for (int i = 0; i <datas.length() ; i++) {
                        JSONObject object= datas.getJSONObject(i);
                        categories_list.add(object.get("title").toString());
                    }
                    setSpinnerCatAction();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                setSpinnerCatAction();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    private void setSpinnerCatAction(){
        String [] categories=new String[categories_list.size()] ;
        for (int i = 0; i < categories_list.size(); i++) {
            categories[i]= categories_list.get(i);
        }
        ArrayAdapter aa= new ArrayAdapter(this, android.R.layout.simple_spinner_item, categories);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(aa);
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category_id=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void initViews(){
        add1=(TextView)findViewById(R.id.add1);
        add2=(TextView)findViewById(R.id.add2);
        changeLoc=(TextView)findViewById(R.id.changeLoc);
        localisation=(TextInputEditText)findViewById(R.id.localisation);
        title=(TextInputEditText)findViewById(R.id.title);
        desc=(TextInputEditText)findViewById(R.id.desc);
        submit=(Button)findViewById(R.id.validate);
        img1= (ImageView)findViewById(R.id.img1);
        img2= (ImageView)findViewById(R.id.img2);
        card1= (LinearLayout)findViewById(R.id.card_add1);
        card2= (LinearLayout)findViewById(R.id.card_add2);
        anonymate=(CheckBox)findViewById(R.id.anonymatCB);
        commune=(Spinner)findViewById(R.id.commune);
        arrond=(Spinner)findViewById(R.id.arrond);
        category=(Spinner)findViewById(R.id.category);

        headnvLayout=(RelativeLayout)findViewById(R.id.headnvLayout);
        descLayout=(TextInputLayout)findViewById(R.id.descLayout);
        nvLayout=(LinearLayout)findViewById(R.id.nvLayout);
        recordBtn=(ImageButton)findViewById(R.id.record_btn);
        recordTime=(Chronometer)findViewById(R.id.record_timer);
        play=(TextView)findViewById(R.id.play);
        delete=(TextView)findViewById(R.id.delete);

        if (getIntent()!=null && getIntent().getExtras()!=null  && getIntent().getExtras().getBoolean("AUDIO", false)){
            descLayout.setVisibility(View.GONE);
            isAudioType=true;
        }else{
            headnvLayout.setVisibility(View.GONE);
            nvLayout.setVisibility(View.GONE);
        }
    }

    private void setActions() {
        getLocation();
        /*TODO: IF USER ISN'T AUTHENTIFIED, HE IS ANONYMATE*/
        if (!Cte.isConnected(this))
            anonymate.setEnabled(false);
        else{
            User user= new User(this);
            user_name= user.getFirstname()+" "+user.getLastname();
            user_id= user.getUser_id();
            token=user.getToken();
            phone=user.getPhone();
            password=user.getPassword();
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text_title= title.getText().toString().trim();
                String text_desc= desc.getText().toString().trim();
                if(commune_index<=0){
                    Toast.makeText(Submit.this, "Vous devez sélectionner la commune du problème..!", Toast.LENGTH_SHORT).show();
                    return;
                }/*else if(!localisation.getText().toString().toLowerCase().contains(commune_select.toLowerCase())){
                    Cte.showSimpleDialog(Submit.this, "Désolé, la zone que vous avez choisie sur la carte ne correspond pas à la commune sélectionée !")
                            .create()
                            .show();
                    return;
                }*/else if(arrond_index<=0){
                    Toast.makeText(Submit.this, "Vous devez sélectionner un arrondissement...!", Toast.LENGTH_SHORT).show();
                    return;
                }else if(imageFiles[0]==null){
                    Toast.makeText(Submit.this, "Vous devez sélectionner au moins une image", Toast.LENGTH_SHORT).show();
                    return;
                }else if(category_id<=0){
                    Toast.makeText(Submit.this, "Vous devez sélectionner une catégorie", Toast.LENGTH_SHORT).show();
                    return;
                }else if(text_title.isEmpty()){
                    Toast.makeText(Submit.this, "Vous devez donner un titre au problème", Toast.LENGTH_SHORT).show();
                    return;

                }

                if(isAudioType){
                    if(recordFile==null || recordFile.isEmpty())
                        Toast.makeText(Submit.this, "Vous devez faire un enregistrement audio du problème", Toast.LENGTH_SHORT).show();
                    /*else if (text_title.isEmpty())
                        Toast.makeText(Submit.this, "Vous devez remplir le titre...", Toast.LENGTH_SHORT).show();
                    */else
                        uploadFile(imageFiles, "IMAGE", new File(recordFile),1,imageFiles[1]==null?1:1, recordTime.getText().toString(), category_id, text_title);

                }if(/*text_title.isEmpty()||*/text_desc.isEmpty())
                        Toast.makeText(Submit.this, "Certains champs ne sont pas remplis", Toast.LENGTH_SHORT).show();
                else
                    uploadFile(imageFiles, "IMAGE", null,1,imageFiles[1]==null?1:1, text_desc, category_id,text_title );
            }
        });

        changeLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Submit.this, MapsActivity.class)
                    .putExtra("LATITUDE", latitude)
                    .putExtra("LONGITUDE", longitude));
            }
        });

        add1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkPermissionGallerie()) {
                    final BottomGallery bottomGallery = new BottomGallery(Submit.this, true);
                    bottomGallery.show(getSupportFragmentManager(), "");
                    bottomGallery.setCloseBottomDialog(new BottomGallery.closeBottomDialog() {
                        @Override
                        public void onCloseListener() {

                        }

                        @Override
                        public void onSelectFinished(List<File> selectedFile, List<File> fileSelected, boolean singleSelected, boolean imageType) {
                            if (selectedFile.size() >= 1) {
                                card2.setVisibility(View.VISIBLE);
                                File file= new File(
                                        new ImageCompress(Submit.this,"Submit")
                                                .compressImage(
                                                        selectedFile.get(0).getAbsolutePath()
                                                ));
                                imageFiles[0]=file;
                                img1.setImageURI(Uri.fromFile(file));
                                bottomGallery.dismiss();
                            } else {
                                card2.setVisibility(View.GONE);
                            }
                        }
                    });
                }else
                    requestPermissionExternalCard();
            }
        });

        add2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomGallery bottomGallery= new BottomGallery(Submit.this,true);
                bottomGallery.show(getSupportFragmentManager(),"");
                bottomGallery.setCloseBottomDialog(new BottomGallery.closeBottomDialog() {
                    @Override
                    public void onCloseListener() {

                    }

                    @Override
                    public void onSelectFinished(List<File> selectedFile, List<File> fileSelected, boolean singleSelected, boolean imageType) {
                        if(selectedFile.size()>=1){
                            File file= new File(
                                new ImageCompress(Submit.this,"Submit")
                                        .compressImage(
                                                selectedFile.get(0).getAbsolutePath()
                                        ));
                            imageFiles[1]=file;
                            img2.setImageURI(Uri.fromFile(file));
                            bottomGallery.dismiss();
                        }
                    }
                });
            }
        });

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRecording) {
                    //Stop Recording
                    stopRecording();

                    // Change button image and set Recording state to false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_stopped, null));
                    }
                    isRecording = false;
                } else {
                    //Check permission to record audio
                    if(!checkPermissionRecording()) {
                        //Start Recording
                        startRecording();

                        // Change button image and set Recording state to false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_recording, null));
                        }
                        isRecording = true;
                    }else
                        requestPermissionRecording();
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play.setVisibility(View.GONE);
                delete.setVisibility(View.GONE);
                recordFile=null;
                recordTime.setText("00:00");
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying)
                    stopAudio();
                else
                    playAudio(new File(recordFile));
            }
        });
    }


    private void stopRecording() {
        //Stop Timer, very obvious
        recordTime.stop();

        //Change text on page to file saved
        play.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);

        //Stop media recorder and set it to null for further use to record new audio
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    private void startRecording() {
        //Start timer from 0
        recordTime.setBase(SystemClock.elapsedRealtime());
        recordTime.start();

        //Get app external directory path
        String recordPath = getExternalFilesDir("/").getAbsolutePath();

        //Get current date and time
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.FRENCH);
        Date now = new Date();

        //initialize filename variable with date and time at the end to ensure the new file wont overwrite previous file
        recordFile = "Recording_" + formatter.format(now) + ".amr";

        recordFile= recordPath + "/" + recordFile;

        //Setup Media Recorder for recording
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Start Recording
        mediaRecorder.start();
    }



    public void playAudio(File fileToPlay) {

        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

       play.setText("Arrêter la lecture");

        //Play the audio
        isPlaying = true;

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopAudio();
            }
        });

        recordTime.setBase(SystemClock.elapsedRealtime());
        recordTime.start();

    }

    private void stopAudio() {
        //Stop The Audio
        play.setText("Lire");
        recordTime.stop();
        isPlaying = false;
    }
    
    private void getLocation(){
        if(checkPermissionGeoLocale()) {
            Toast.makeText(this, "Autorisez GoLocal à accéder à votre position", Toast.LENGTH_SHORT).show();
            requestPermissionGeoLocale();
        }else{
            if(isLocationEnabled())
                getCurrentLocation();
            else{
                AlertDialog al= new AlertDialog.Builder(this)
                        .setMessage("Veuillez activer votre localisation")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        }).create();
                al.setCanceledOnTouchOutside(false);
                al.setCancelable(false);
                al.show();

            }
        }
    }

    private void getCurrentLocation() {
        /*MapView map= new MapView(this);
        MyLocationNewOverlay myLocationNewOverlay= new MyLocationNewOverlay(new GpsMyLocationProvider(this),map );
        myLocationNewOverlay.enableMyLocation();
        GeoPoint ig= myLocationNewOverlay.getMyLocation();
        localisation.setText("En cours...");
        latitude=ig.getLatitude();
        longitude=ig.getLongitude();*/
        client= LocationServices.getFusedLocationProviderClient(this);
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null){
                    latitude=location.getLatitude();
                    longitude=location.getLongitude();
        changeLocatioText();
                }
            }
        });
    }

    private void changeLocatioText() {
        Geocoder geocoder = new Geocoder(Submit.this);
        List<Address> adress = null;
        try {
            adress = geocoder.getFromLocation(latitude, longitude, 2);
            String str = adress.get(0).getLocality();
            String st = adress.get(0).getCountryName();
            localisation.setText(st+", "+str+" ("+latitude+ ", "+longitude+")");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(checkPermissionGeoLocale()) {
            Toast.makeText(this, "Autorisez GoLocal à accéder à votre position", Toast.LENGTH_SHORT).show();
            requestPermissionGeoLocale();
        }else{
            if(!Cte.getCurrentProblemLocation(this).equals("")){
                String datas[]= Cte.getCurrentProblemLocation(this).split("\t");
                assert datas.length >= 2 ;
                latitude= Double.parseDouble(datas[0]);
                longitude= Double.parseDouble(datas[1]);
                localisation.setText(datas[2] +" ("+latitude+", "+longitude+")");
                changeLocatioText();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }else
            return super.onOptionsItemSelected(item);
    }

    private boolean checkPermissionRecording() {
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return record_audio_result == PackageManager.PERMISSION_DENIED;
    }
    private void requestPermissionRecording() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.RECORD_AUDIO
        }, 252);
    }

    private void requestPermissionExternalCard() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, 250);
    }
    private boolean checkPermissionGallerie() {
        int write_external_strorage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read_external_strorage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return write_external_strorage_result == PackageManager.PERMISSION_DENIED || read_external_strorage_result == PackageManager.PERMISSION_DENIED;
    }

    private boolean checkPermissionGeoLocale() {
        int fine_result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarse_result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        return fine_result == PackageManager.PERMISSION_DENIED || coarse_result == PackageManager.PERMISSION_DENIED;
    }

    private boolean isLocationEnabled()  {
        return ((locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                ||(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))));
    }


    private void requestPermissionGeoLocale() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 251);
    }


    private void setSpinnerAction(){

        ArrayAdapter aa= new ArrayAdapter(this, android.R.layout.simple_spinner_item, communes);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        commune.setAdapter(aa);
        commune.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                commune_index=communes_index[position];
                commune_select=communes[position];
                arrond.setAdapter(null);
                getArrond(commune_index);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void getCommunes(){

        RequestQueue requestQueue=  Volley.newRequestQueue(this);
        String url= Cte.BASE_URL+"/communes/";
        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.GET
                , url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray datas= response.getJSONArray("data");
                    communes= new String[datas.length()+1];
                    communes[0]="Commune du problème";

                    communes_index= new int[datas.length()+1];
                    communes_index[0]= 0;
                    for (int i = 0; i <datas.length() ; i++) {
                        JSONObject object= datas.getJSONObject(i);
                        communes_index[i+1]=Integer.parseInt(object.get("id").toString());
                        communes[i+1]=object.get("name").toString();
                    }
                    setSpinnerAction();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                setSpinnerAction();
            }
        });

        requestQueue.add(jsonObjectRequest);

    }

    private JSONArray arrondArray ;
    private void getArrond(final int commune_id){

        /*if(arrondArray!=null) {
            try {
                careArrond(arrondArray, commune_id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ;
        }*/


        RequestQueue requestQueue=  Volley.newRequestQueue(this);
        String url= Cte.BASE_URL+"/arrondissements";
        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.GET
                , url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d("RESPONSE", response.toString());
                try {
                    JSONArray datas= response.getJSONArray("data");
                    for (int i=0; i<datas.length(); i++){
                        JSONObject jo= datas.getJSONObject(i);
                        if(!jo.get("commune_id").toString().equals(commune_id+"")){
                            datas.remove(i);
                        }
                    }
                    arrondArray=datas ;

                    careArrond(datas, commune_id);

                } catch (JSONException e) {
                    e.printStackTrace();
                    //createArrond(commune_id, "Standard");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        requestQueue.add(jsonObjectRequest);

    }

    private void careArrond(JSONArray datas, int commune_id ) throws JSONException {

        List<String> arrondList= new ArrayList<>();
        List<String> arrond_idsList= new ArrayList<>();
        for (int i = 0; i <datas.length() ; i++) {
            JSONObject object= datas.getJSONObject(i);
            if(object.getString("commune_id").equals(commune_id+"")){
                arrondList.add(object.get("name").toString());
                arrond_idsList.add(object.get("id").toString());
            }
        }

        String [] arrond= new String[arrondList.size()];
        int [] arrond_ids= new int[arrondList.size()];

        for (int i = 0; i < arrondList.size(); i++) {
            arrond_ids[i]=Integer.parseInt(arrond_idsList.get(i));
            arrond[i]=arrondList.get(i);
        }

        if(arrond.length >=1){
            setArrondSpinner(arrond, arrond_ids);
        }else if (commune_id!=0){
            createArrond(commune_id, "Standard");
        }
    }

    private void setArrondSpinner(String [] arronds, final int[] arrond_ids){
        ArrayAdapter aa= new ArrayAdapter(this, android.R.layout.simple_spinner_item, arronds);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrond.setAdapter(aa);
        arrond.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                arrond_index= arrond_ids[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void createArrond(final int commune_id, String nom){

        RequestQueue requestQueue=  Volley.newRequestQueue(this);
        String url= Cte.BASE_URL+"/arrondissements";
        JSONObject entry = new JSONObject();
        try {
            entry.put("name", nom);
            entry.put("commune_id", commune_id+"");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("DATAS", entry.toString());

        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(url, entry, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d("RESPONSE", response.toString());
                try {
                    String status= response.get("status").toString();
                    if(status.equals("success")){
                        getArrond(commune_id);
                    }else{
                        /*TODO ACtion*/
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> maps= new HashMap<String, String>();
                maps.put("token", token);
                return maps;
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("phone", phone);
                params.put("password", password);
                return params;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    private void uploadFile(final File[] files,
                            String type,
                            final File audio_file,
                            final int current_file,
                            final int files_size,
                            final String desc, final int category_id, final String text_title){
        pdial.setCancelable(false);
        pdial.setCanceledOnTouchOutside(false);
        pdial.setMessage("Envoi de l'"+type.toLowerCase()+" ("+current_file+"/"+files_size+")");
        pdial.show();
        StorageReference dataStoreRef= FirebaseStorage.getInstance().getReference().child("GoLocal_"+type);
        String fileName=current_file+"_"+System.currentTimeMillis()+""+(new Random().nextInt(200))+".jpg";
        File file=files[current_file-1];
        Log.d("SIZE_OF_IMAGES",link_image.split(";").length+"" );
        if(type.equals("AUDIO")/*|| (current_file==files_size && link_image.split(";").length>files_size)*/) {
            fileName = System.currentTimeMillis() + "" + (new Random().nextInt(200)) + ".mp3";
            file=audio_file;
            type="AUDIO";
        }


        final StorageReference fileRef= dataStoreRef.child(fileName);

        final String finalType = type;
        fileRef.putFile(Uri.fromFile(file)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String path= uri.toString();
                            if(!finalType.equals("AUDIO")){
                                link_image=link_image+""+path+";";
                                if(current_file<files_size){
                                    uploadFile(files, finalType, audio_file, current_file+1, files_size, desc, category_id, text_title);
                                }else {
                                    if(isAudioType){
                                        uploadFile(files, "AUDIO", audio_file, 1, 1, desc, category_id, text_title);
                                    }else{
                                        makeSubmissions(desc,
                                                link_audio,
                                                link_image,
                                                user_name,
                                                anonymate.isChecked(),
                                                (float)latitude,
                                                (float) longitude,
                                                commune_index, category_id, arrond_index, text_title);
                                    }
                                }
                            }else {
                                link_audio=path;
                                makeSubmissions(desc,
                                        link_audio,
                                        link_image,
                                        user_name,
                                        anonymate.isChecked(),
                                        (float)latitude,
                                        (float) longitude,
                                        commune_index, category_id, arrond_index , text_title);
                            }

                        }
                    });
                }else{
                    pdial.dismiss();
                    Cte.showSimpleDialog(Submit.this,
                            "Echec de l'envoi du fichier\nVeuillez vérifier votre connexion et réessayer...")
                            .create()
                            .show();
                }
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int percent = (int) (taskSnapshot.getBytesTransferred()*100/taskSnapshot.getTotalByteCount());
                pdial.setMessage("Envoi de l'"+finalType.toLowerCase()+" ("+current_file+"/"+files_size+") \n"+percent+"% effectué(s)");
            }
        });



    }

    private void makeSubmissions(String desc,
                                 String audio,
                                 String image,
                                 String user_name,
                                 boolean anonymate,
                                 float latitude,
                                 float longitude,
                                 int commune_id,
                                 int category_id,
                                 int arrondissement_id,
                                 String text_title) {
        image=image.substring(0, image.length()-1);
        pdial.setMessage("Soumission en cours...");

        RequestQueue requestQueue=  Volley.newRequestQueue(this);
        String url= Cte.BASE_URL+"/problemes";
        JSONObject entry = new JSONObject();
        try {
            //entry.put("code", "automatique");
            entry.put("description", desc);
            entry.put("audio", audio.trim().isEmpty()?".":audio);
            entry.put("image", image);
            entry.put("user_id", user_id);
            entry.put("status", "0");
            entry.put("anonymes", anonymate);
            entry.put("longitude", longitude+"");
            entry.put("latitude", latitude+"");
            entry.put("commune_id", commune_id+"");
            entry.put("category_id", category_id+"");
            entry.put("arrondissement_id", arrondissement_id+"");
            entry.put("titre", text_title+"");
            //entry.put("category_id", "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("DATAS", entry.toString());

        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(url, entry, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                pdial.dismiss();
                Log.d("RESPONSE", response.toString());
                try {
                    String status= response.getJSONObject("data").get("id").toString();
                    if(!status.equals("")){

                        AlertDialog.Builder al=Cte.showSimpleDialog(Submit.this, "Votre soumission a été faite avec succès !!\n\nElle est en attente de validation et sera publiée par l'administrateur.");
                        AlertDialog alertDialog= al.create();
                        alertDialog.show();
                        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                onBackPressed();
                            }
                        });
                    }else{
                        Cte.showSimpleDialog(Submit.this, "Echec dela soumission...");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Cte.showSimpleDialog(Submit.this, "Echec dela soumission...");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pdial.dismiss();
                error.printStackTrace();
                Cte.showSimpleDialog(Submit.this, "Une erreur s'est produite pendant la soumission...\nVeuillez revoir votre connexion INTERNET et réessayer");
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

}
