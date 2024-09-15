package geolocale.city.bj.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.arsy.maps_library.MapRipple;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

import geolocale.city.bj.R;
import geolocale.city.bj.submit.Problem;
import geolocale.city.bj.submit.ProblemAdapter;
import geolocale.city.bj.submit.Submit;
import geolocale.city.bj.tools.Cte;
import geolocale.city.bj.tools.User;

public class HomeFragment extends Fragment {


    private ShimmerFrameLayout shimmerFrameLayout;
    private LineChart line_chart;
    private LinearLayout submit, see_submissions;
    private TextView counter_all_solved, counter_all_sub;
    private RecyclerView recent_submissions;
    private Context ctx;
    private User user;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment ;
    private SwipeRefreshLayout swipe;
    private MapView mapView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ctx= getContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        View root = inflater.inflate(R.layout.fragment_accueil, container, false);

        initViews(root);
        return root;
    }

    private void initViews(final View view){
        shimmerFrameLayout=(ShimmerFrameLayout)view.findViewById(R.id.fbshimmer);
        line_chart= (LineChart)view.findViewById(R.id.chart);
        submit= (LinearLayout)view.findViewById(R.id.submit);
        see_submissions= (LinearLayout)view.findViewById(R.id.see_submissions);
        counter_all_solved= (TextView) view.findViewById(R.id.counter_all_solved);
        counter_all_sub= (TextView) view.findViewById(R.id.counter_all_submitted);
        recent_submissions= (RecyclerView) view.findViewById(R.id.recent_submissions);
        swipe= (SwipeRefreshLayout) view.findViewById(R.id.swipe);

        //mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);

        mapView = (MapView) view.findViewById(R.id.map);

        startLoading();
        startActions();

        getRecentProblem("problemes/", 0);
        getLocation();

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe.setRefreshing(false);
                initViews(view);
            }
        });
    }



    private void startActions(){
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence[] type_submit= {"Soumission simple",  "Soumission audio"};
                AlertDialog alertDialog= new AlertDialog.Builder(ctx)
                        .setItems(type_submit, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which==0)
                                    startActivity(new Intent(ctx, Submit.class));
                                else
                                    startActivity(new Intent(ctx, Submit.class)
                                    .putExtra("AUDIO", true));
                            }
                        }).create();
                alertDialog.show();
            }
        });
        see_submissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ctx, geolocale.city.bj.submit.SeeAllProblems.class));
            }
        });
    }

    private void startLoading(){
        shimmerFrameLayout.startShimmer();
        completeChart();
    }

    private List<Entry> dataValue(){
        List<Entry> dataVals = new ArrayList<Entry>();
        dataVals.add(new Entry(1,0));
        dataVals.add(new Entry(1,5));
        dataVals.add(new Entry(2,50));
        dataVals.add(new Entry(3,5));
        dataVals.add(new Entry(4,10));
        dataVals.add(new Entry(5,100));
        dataVals.add(new Entry(6,150));
        dataVals.add(new Entry(7,20));
        dataVals.add(new Entry(8,142));
        dataVals.add(new Entry(9,5));
        dataVals.add(new Entry(10,15));
        dataVals.add(new Entry(11,30));

        return dataVals;
    }

    boolean already_here=false;

    private void moveMap(double latitude, double longitude, String text) {

        LatLng latLng= new LatLng(latitude, longitude);
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(text)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))) ;
        /*if(!already_here)*/ {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
            already_here=true;
        }


    }


    private void completeChart(){
        LineDataSet linedataset= new LineDataSet(dataValue(), "Nombre de problème soumis");
        linedataset.disableDashedLine();
        ArrayList<ILineDataSet> dataSets= new ArrayList<>();
        dataSets.add(linedataset);

        LineData data= new LineData(dataSets);
        Description desc=new Description();
        desc.setText("Année 2020");
        line_chart.setDescription(desc);
        line_chart.setData(data);
        line_chart.invalidate();
        line_chart.setVisibility(View.GONE);

    }


    private void getRecentProblem(String sublink, final int type){
        shimmerFrameLayout.startShimmer();
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        recent_submissions.setVisibility(View.GONE);

        RequestQueue requestQueue=  Volley.newRequestQueue(ctx);
        String url= Cte.BASE_URL+"/"+sublink;
        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.GET
                , url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("RESPONSE", response.toString());
                try {
                    JSONArray datas= response.getJSONArray("data");
                    if(type==0)
                        completeRecycler(datas);
                    else {
                        if(Cte.isConnected(ctx)){
                            for (int i=0; i<datas.length(); i++){
                                JSONObject jo= datas.getJSONObject(i);
                                if(!jo.get("commune_id").toString().equals(user.getUser_communeId()+"")){
                                    datas.remove(i);
                                }
                            }
                            remplirMapsCommune(datas);
                        }else
                            remplirMapsCommune(datas);
                    }
                    counter_all_sub.setText(datas.length()+"");
                    counter_all_solved.setText("00");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                recent_submissions.setVisibility(View.GONE);
                shimmerFrameLayout.setVisibility(View.GONE);
                Toast.makeText(ctx, "Echec de la connexion... Veuillez rafraîchir la page..!", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    private void completeRecycler (JSONArray datas) throws JSONException {


        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        recent_submissions.setVisibility(View.VISIBLE);

        List<Problem> problemList= new ArrayList<>();
        ProblemAdapter problemAdapter = new ProblemAdapter(problemList);
        recent_submissions.setNestedScrollingEnabled(false);
        recent_submissions.setAdapter(problemAdapter);
        recent_submissions.setLayoutManager(new LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, true));

        for (int i = 0; i <datas.length() ; i++) {
            JSONObject object= datas.getJSONObject(datas.length()-i-1);
            if(object.getString("statut").equalsIgnoreCase("actif"))
            {
                Problem p =new Problem();
                p.setProblemData(object);
                problemList.add(p);
                problemAdapter.notifyDataSetChanged();
                if(i>=2)
                    break;
            }
        }

    }

    /*@Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getLocation();
    }*/

    @Override
    public void onStart() {
        super.onStart();
         /*recent_submissions.setVisibility(View.GONE);
       shimmerFrameLayout.setVisibility(View.GONE);
        if(recent_submissions.getVisibility()==View.GONE && shimmerFrameLayout.getVisibility()==View.GONE){
            mapFragment.getMapAsync(this);
            getRecentProblem("problemes/", 0);
        }*/
    }

    private void getRecentProblemLocation() {
        if(Cte.isConnected(ctx)) {
            user= new User(ctx);
            getRecentProblem("problemes/" , 1);
        }else{
            getRecentProblem("problemes/" , 1);
        }
    }

    private void getLocation(){
        if(checkPermissionGeoLocale()) {
            Toast.makeText(ctx, "Autorisez GoLocal à afficher des problèmes sur la carte Google MAP", Toast.LENGTH_SHORT).show();
            requestPermissionGeoLocale();
        }else{
            if(isLocationEnabled())
                getRecentProblemLocation();
            else{
                AlertDialog al= new AlertDialog.Builder(ctx)
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

    private boolean checkPermissionGeoLocale() {
        int fine_result = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarse_result = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION);
        return fine_result == PackageManager.PERMISSION_DENIED || coarse_result == PackageManager.PERMISSION_DENIED;
    }

    private boolean isLocationEnabled()  {
        LocationManager locationManager= (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        return ((locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                ||(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))));
    }

    private void requestPermissionGeoLocale() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 251);
    }

    private void remplirMapsCommune(JSONArray datas) throws JSONException {
        List<String > titles= new ArrayList<>();
        List<String > subTitles= new ArrayList<>();
        List<IGeoPoint > iGeoPoints= new ArrayList<>();
        for (int i = 0; i <datas.length() ; i++) {
            JSONObject object= datas.getJSONObject(i);
           if(object.getString("statut").equalsIgnoreCase("actif") ||
                    object.getString("statut").equalsIgnoreCase("en_cour") ||
                    object.getString("statut").equalsIgnoreCase("3") ||
                    object.getString("statut").equalsIgnoreCase("1")){

                final double latitude= Double.parseDouble(object.getString("latitude"));
                final double longitude= Double.parseDouble(object.getString("longitude"));
                String desc= object.getString("description");
                titles.add(getStatus(object.getString("statut")));
                subTitles.add(desc);
                iGeoPoints.add(new IGeoPoint() {
                    @Override
                    public int getLatitudeE6() {
                        return 0;
                    }

                    @Override
                    public int getLongitudeE6() {
                        return 0;
                    }

                    @Override
                    public double getLatitude() {
                        return latitude;
                    }

                    @Override
                    public double getLongitude() {
                        return longitude;
                    }
                });
            }
        }

        if( titles.size()>0){
            setupMap(iGeoPoints, titles, subTitles);
        }
    }


    private String getStatus(String status) {
        if(status.equalsIgnoreCase("1"))
            return "Résolu";
        else if(status.equalsIgnoreCase("3"))
            return "Actif";
        else
            return status;
    }

    private void setupMap(List<IGeoPoint> iGeoPoints, List<String> titles, List<String> subTitles) {

        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        IMapController mapController= mapView.getController();
        mapController.setCenter(new GeoPoint(iGeoPoints.get(0).getLatitude(), iGeoPoints.get(0).getLongitude()));
        mapController.setZoom(15.0);

        ArrayList<Marker> items= new ArrayList<>();
        for (int i = 0; i < iGeoPoints.size(); i++) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(iGeoPoints.get(i).getLatitude(), iGeoPoints.get(i).getLongitude()));
            marker.setIcon(getResources().getDrawable(R.drawable.marker));
            marker.setTitle(titles.get(i));
            marker.setSubDescription(subTitles.get(i)==null? "":subTitles.get(i));
            items.add(marker);
            mapView.getOverlays().add(marker);
        }



    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }
}
