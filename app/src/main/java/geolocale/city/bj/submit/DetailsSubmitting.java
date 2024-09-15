package geolocale.city.bj.submit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import geolocale.city.bj.R;
import geolocale.city.bj.tools.Cte;
import geolocale.city.bj.tools.User;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class DetailsSubmitting extends AppCompatActivity {

    private CarouselView carouselView;
    private TextView title, localisation, see, desc, play;
    private EditText editText;
    private MaterialTextView show_from_name, show_from_time;
    private CircleImageView user_profile_pic;
    private ImageView btn_send_cmtaire;
    private RecyclerView recycleComment;
    private ShimmerFrameLayout shimmerFrameLayout;
    private LinearLayout audioLayout;
    private Chronometer recordTime;
    private String TITLE="", LOCALE="", DESC="", FROM_NAME="", TITRE="", FROM_TIME="", FILES, AUDIO, COMMUNE, ARRONDISSEMENT, PB_ID;
    
    private User user;
    
    private boolean isPlaying=false;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_submitting);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getDatas();
        initViews();
        loadCarousel(FILES);
        setActions();
        if (Cte.isConnected(this)){
            user= new User(this);
        }
    }

    private void getDatas(){
        TITLE=Objects.requireNonNull(getIntent().getExtras()).getString("TITLE", "Title");
        TITRE=Objects.requireNonNull(getIntent().getExtras()).getString("TITRE", "Titre");
        LOCALE= Objects.requireNonNull(getIntent().getExtras()).getString("LOCALISATION", "Commune, Arrondissement (lat0.0000, long0.000)");
        DESC=  Objects.requireNonNull(getIntent().getExtras()).getString("DESC", getResources().getString(R.string.problem_title));
        FROM_NAME= Objects.requireNonNull(getIntent().getExtras()).getString("FROM_NAME", "");
        FROM_TIME= Objects.requireNonNull(getIntent().getExtras()).getString("FROM_TIME", "");
        AUDIO= Objects.requireNonNull(getIntent().getExtras()).getString("AUDIO", "");
        FILES= Objects.requireNonNull(getIntent().getExtras()).getString("FILES", "");
        COMMUNE= Objects.requireNonNull(getIntent().getExtras()).getString("COMMUNE", "");
        ARRONDISSEMENT = Objects.requireNonNull(getIntent().getExtras()).getString("ARRONDISSEMENT", "");
        PB_ID = Objects.requireNonNull(getIntent().getExtras()).getString("ID", "");


    }


    private void getCommunes(){

        RequestQueue requestQueue=  Volley.newRequestQueue(this);
        String url= Cte.BASE_URL+"/communes";
        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.GET
                , url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray datas= response.getJSONArray("data");

                    for (int i = 0; i <datas.length() ; i++) {
                        JSONObject object= datas.getJSONObject(i);
                        if(COMMUNE.equals(object.get("id").toString())){
                            COMMUNE= object.get("name").toString();
                            break;
                        }
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
        });

        requestQueue.add(jsonObjectRequest);

    }


    private void getArrond( ){

        RequestQueue requestQueue=  Volley.newRequestQueue(this);
        String url= Cte.BASE_URL+"/arrondissements";
        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.GET
                , url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d("RESPONSE", response.toString());
                try {
                    JSONArray datas= response.getJSONArray("data");

                    for (int i = 0; i <datas.length() ; i++) {
                        JSONObject object= datas.getJSONObject(i);
                        if(ARRONDISSEMENT.equals(object.get("id").toString())){
                            ARRONDISSEMENT= object.get("name").toString();
                            localisation.setText(COMMUNE+", "+ARRONDISSEMENT+ " ("+LOCALE+")");
                            break;
                        }
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
        });

        requestQueue.add(jsonObjectRequest);

    }

    private void initViews() {
        commentAdapter = new CommentAdapter(problemList);
        title= (TextView)findViewById(R.id.title);
        localisation= (TextView)findViewById(R.id.localisation);
        desc= (TextView)findViewById(R.id.desc);
        see= (TextView)findViewById(R.id.see);
        editText= (EditText)findViewById(R.id.ed_cmtaire);

        show_from_name= (MaterialTextView)findViewById(R.id.show_name);

        show_from_time= (MaterialTextView)findViewById(R.id.show_time);

        btn_send_cmtaire=(ImageView)findViewById(R.id.btn_send);

        user_profile_pic=(CircleImageView)findViewById(R.id.btn_profil_pic);

        recycleComment=(RecyclerView)findViewById(R.id.recycleComment);

        shimmerFrameLayout=(ShimmerFrameLayout)findViewById(R.id.fbshimmer);

        carouselView= (CarouselView)findViewById(R.id.carousel);

        audioLayout= (LinearLayout)findViewById(R.id.audio);
        recordTime= (Chronometer) findViewById(R.id.record_timer);
        play= (TextView)findViewById(R.id.play);


        recycleComment.setNestedScrollingEnabled(false);
        recycleComment.setAdapter(commentAdapter);
        recycleComment.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));


    }

    private void loadCarousel(String FILES){
        shimmerFrameLayout.startShimmer();

        final String images[]= FILES.split(";");

        carouselView.setPageCount(images.length);

        carouselView.setImageListener(new ImageListener() {
            @Override
            public void setImageForPosition(int position, final ImageView imageView) {
                String lien=images[position].replaceAll("\\\\", "");
                Log.d("LINK", lien);
                lien= Html.fromHtml(lien).toString();
                Log.d("LINK", lien);
                Cte.showProfilePic(DetailsSubmitting.this, lien, imageView, R.drawable.common_google_signin_btn_icon_dark_focused);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        });


    }

    private void setActions() {
        see.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });
        title.setText(TITLE.length()<=4?"":TITLE);
        getSupportActionBar().setTitle(TITRE);
        desc.setText(DESC);
        if(AUDIO!=null && !AUDIO.isEmpty() && !AUDIO.equals("null")&& !AUDIO.equals(".")){
            if (!AUDIO.startsWith("http"))
                AUDIO="https://firebasestorage.googleapis.com/"+AUDIO;
            desc.setText("Problème audio de "+DESC);
            recordTime.setText(DESC);
            audioLayout.setVisibility(View.VISIBLE);
            playOrDownload();
        }
        show_from_name.setText(FROM_NAME);
        show_from_time.setText(FROM_TIME);
        localisation.setText(LOCALE);
        fromName();
        getCategorie(TITLE);
        getCommunes();
        getArrond();
        getComments(PB_ID);
        final int[] i = {0};
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (user==null ) {
                    btn_send_cmtaire.setEnabled(false);
                    if(i[0] ==0) {
                        Toast.makeText(DetailsSubmitting.this, "Veuillez vous connecter pour commenter le problème..!", Toast.LENGTH_SHORT).show();
                        i[0]++;
                    }
                }else {
                    if (s.toString().trim().isEmpty())
                        btn_send_cmtaire.setEnabled(false);
                    else
                        btn_send_cmtaire.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btn_send_cmtaire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user==null)
                    Toast.makeText(DetailsSubmitting.this, "Veuillez vous connecter pour commenter le problème..!", Toast.LENGTH_SHORT).show();
                if(editText.getText().toString().trim().isEmpty())
                    return;
                if(Cte.isConnected(DetailsSubmitting.this))
                    sendComment(editText.getText().toString().trim(), PB_ID, (new User(DetailsSubmitting.this).getCompleName()));
                else
                    sendComment(editText.getText().toString().trim(), PB_ID, "Anonymat");
                editText.setText("");

            }
        });
    }

    private void fromName(){
        if(getIntent().getExtras().getBoolean("ANONYMAT", true)){
            show_from_name.setText("Anonymat");
        }
    }

    private void getCategorie (final String categorie){
        RequestQueue requestQueue=  Volley.newRequestQueue(this);
        String url= Cte.BASE_URL+"/categories";
        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.GET
                , url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d("RESPONSE", response.toString());
                String result= "Non catégorisé", desc="description";
                try {
                    JSONArray datas= response.getJSONArray("data");
                    for (int i = 0; i < datas.length(); i++) {
                        JSONObject object= datas.getJSONObject(i);
                        if(object.getString("id").equals(categorie)) {
                            result =object.getString("title");
                            desc =object.getString("description");
                            break;
                        }
                    }
                    TITLE= result;
                    remplirCategorie(desc);


                } catch (JSONException e) {
                    e.printStackTrace();
                    remplirCategorie("");

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                TITLE= "Non catégorisé";
                remplirCategorie("");
            }
        });

        requestQueue.add (jsonObjectRequest);
    }

    private void remplirCategorie(String d) {
        if(TITLE== null || TITLE.equals("null"))
            TITLE= "Non catégorisé";
        title.setText(TITLE+""+(d.equals("")? "": ", "+d));
    }

    private void playOrDownload() {
        final String fileName= PB_ID+"_AUDIO";
        final String file=Cte.lire_le_fichier(DetailsSubmitting.this, fileName+".txt");
        if(!file.isEmpty()){
            play.setText("Lire");
        }

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(file.isEmpty()){
                    try {
                        download("audio", fileName, AUDIO);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    if(isPlaying)
                       stopAudio();
                    else
                        playAudio((file.startsWith("file:/")?file:"file://"+file));
                }
            }
        });
    }

    private int ID_PIC=5;

    private String NC_ID="GoLocal Download";

    public void download(final String type, final String fileName, String link) throws IOException {

        Log.d("link", link);

        StorageReference storageReference= FirebaseStorage.getInstance().getReferenceFromUrl(link);

        Toast.makeText(this, "Téléchargement en cours...", Toast.LENGTH_SHORT).show();

        File parent= new File(Environment.getExternalStorageDirectory()
                + "/GoLocale/Audio");
        if(!parent.exists())
            parent.mkdirs();
        String extension=".jpg";
        if(type.equalsIgnoreCase("audio"))
            extension=".mp3";

        final File file= new File(parent, "audio_"+fileName+""+extension);

        /*storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Cte.ecrire(DetailsSubmitting.this,fileName+".txt",file.getAbsolutePath());
                Cte.dismissProgressN(DetailsSubmitting.this, ID_PIC ,NC_ID);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                int pourcent=0;
                if(taskSnapshot.getTotalByteCount()!=0) {
                    pourcent = (int) (taskSnapshot.getBytesTransferred() * 100 / taskSnapshot.getTotalByteCount());
                }
                if(ID_PIC!=0)
                    Cte.showProgressNotif(DetailsSubmitting.this,
                            "Téléchargent de "+type+" ",
                            "Downloading...",
                            NC_ID,
                            ID_PIC,
                            pourcent,100
                    );
            }
        });*/
        final DownloadManager dm = (DownloadManager)  getSystemService(Context.DOWNLOAD_SERVICE);
        if(Uri.parse(link).toString().startsWith("com.") || !link.startsWith("http")){
            Toast.makeText(this, "Echec de téléchargement...", Toast.LENGTH_SHORT).show();
            return;
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link));
        request.setShowRunningNotification(true);
        request.setVisibleInDownloadsUi(false);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0230);
        }else {
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ) {
                // if(Environment.getExternalStorageDirectory().getUsableSpace()<(Environment.getExternalStorageDirectory().getTotalSpace()-1000)) {

                request.setDestinationUri(Uri.fromFile(file));

                long queue =0;
                try {
                    queue = dm.enqueue(request);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Echec de téléchargement..!", Toast.LENGTH_SHORT).show();
                }
                if(queue==0)
                    return;

                final long finalQueue = queue;
                BroadcastReceiver receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                            DownloadManager.Query query = new DownloadManager.Query();
                            query.setFilterById(finalQueue);

                            Cursor c = dm.query(query);
                            if (c.moveToFirst()) {
                                int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {

                                    String pa = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                                    String path = file.getPath();
                                    Cte.ecrire(DetailsSubmitting.this,fileName+".txt",pa);
                                    Log.d("PATH", pa+" \nVS=>  "+path+" \nVS=> "+file.getAbsolutePath());
                                    playOrDownload();
                                }
                            }


                        } else {
                            Toast.makeText(DetailsSubmitting.this, "Erreur lors du téléchargement de l'audio", Toast.LENGTH_SHORT).show();

                        }
                    }
                };
                registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            }else
                Toast.makeText(this, "Impossible d'écrire sur votre carte SD ! \nVeuillez vérifier la présence de la mémoire ou insérer une nouvelle carte SD", Toast.LENGTH_SHORT).show();
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

    public void playAudio(String fileToPlay) {
        Log.d("PATH", fileToPlay);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                play.setText("Arrêter la lecture");

                //Play the audio
                isPlaying = true;
                recordTime.setBase(SystemClock.elapsedRealtime());
                recordTime.start();
            }
        });

        try {
            mediaPlayer.setDataSource(fileToPlay);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopAudio();
            }
        });


    }

    private void stopAudio() {
        //Stop The Audio
        play.setText("Lire");
        recordTime.stop();
        isPlaying = false;
    }

    private void getLocation(){
        if(checkPermissionGeoLocale()) {
            Toast.makeText(this, "Autorisez GoLocal à afficher des problèmes sur la carte Google MAP", Toast.LENGTH_SHORT).show();
            requestPermissionGeoLocale();
        }else{
            if(isLocationEnabled())
                seeLocation();
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

    private void seeLocation() {
        String [] maps= LOCALE.split(";");
        startActivity(new Intent(DetailsSubmitting.this, MapsActivity.class)
                .putExtra("LATITUDE", Double.parseDouble(maps[0]))
                .putExtra("LONGITUDE",  Double.parseDouble(maps[1]))
                .putExtra("DETAILS",  DESC)
                .putExtra("VISIT", true));
    }

    private boolean checkPermissionGeoLocale() {
        int fine_result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarse_result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        return fine_result == PackageManager.PERMISSION_DENIED || coarse_result == PackageManager.PERMISSION_DENIED;
    }

    private boolean isLocationEnabled()  {
        LocationManager locationManager= (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return ((locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                ||(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))));
    }

    private void requestPermissionGeoLocale() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 251);
    }

    private void sendComment(String commentaire, String problem_id, String name){

        RequestQueue requestQueue=  Volley.newRequestQueue(this);
        String url= Cte.BASE_URL+"/comments";
        JSONObject entry = new JSONObject();
        try {
            entry.put("comment", commentaire);
            entry.put("user_id", user.getUser_id());
            entry.put("problem_id", problem_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(url, entry, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d("RESPONSE", response.toString());
                try {
                    String status= response.get("status").toString();
                    if(status.equals("success")){
                        JSONObject data= response.getJSONObject("data");
                        sendThisCmtaire(data);
                        Toast.makeText(DetailsSubmitting.this, "Votre commentaire a bien été envoyé !", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(DetailsSubmitting.this, "Echec de l'envoi du commentaire", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(DetailsSubmitting.this, "Echec de l'envoi du commentaire", Toast.LENGTH_SHORT).show();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> maps= new HashMap<String, String>();
                maps.put("token", user.getToken());
                return maps;
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
               // params.put("phone", user.getPhone());
                //params.put("password", user.getPassword());
                return params;
            }
        };
        requestQueue.add(jsonObjectRequest);

        /*try {
            entry.put("created_at", new SimpleDateFormat("dd/MMM/yyy", Locale.FRENCH).format(new Date()));
            sendThisCmtaire(entry);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    private void getComments(final String problem_id){
        shimmerFrameLayout.startShimmer();
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        recycleComment.setVisibility(View.GONE);

        RequestQueue requestQueue=  Volley.newRequestQueue(this);
        String url= Cte.BASE_URL+"/comments";
        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.GET
                , url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray datas= response.getJSONArray("data");
                    for (int i=0; i<datas.length(); i++){
                        JSONObject jo= datas.getJSONObject(i);
                        if(!jo.getString("problem_id").equals(problem_id)){
                            datas.remove(i);
                        }
                    }
                    completeRecycler(datas);

                } catch (JSONException e) {

                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                stopShimming();
                error.printStackTrace();
            }
        });

        requestQueue.add(jsonObjectRequest);

    }

    private void stopShimming(){

        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        recycleComment.setVisibility(View.VISIBLE);
    }


    private List<Problem> problemList= new ArrayList<>();
    private CommentAdapter commentAdapter;

    private void completeRecycler (JSONArray datas) throws JSONException {

        stopShimming();


        for (int i = 0; i <datas.length() ; i++) {
            JSONObject object= datas.getJSONObject(datas.length()-i-1);
             sendThisCmtaire(object);
        }

    }

    private void sendThisCmtaire(JSONObject object){

        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        recycleComment.setVisibility(View.VISIBLE);

        Problem p =new Problem();
        p.setProblemData(object);
        problemList.add(p);
        commentAdapter.notifyDataSetChanged();
    }






}
