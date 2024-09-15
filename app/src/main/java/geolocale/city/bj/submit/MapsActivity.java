package geolocale.city.bj.submit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.arsy.maps_library.MapRipple;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import geolocale.city.bj.R;
import geolocale.city.bj.tools.Cte;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    private EditText edit_search;
    private ImageView search;
    private Button validate,cancel, select_drag;
    private MapRipple mapRipple;
    private String global_title; double global_latitude,global_longitude;
    private ProgressDialog pdial;
    private boolean seeing= false;
    private MapView mapView;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx= getApplicationContext();
        //Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_maps);
        pdial= new ProgressDialog(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        //mapView= (MapView)findViewById(R.id.map);
        edit_search=(EditText)findViewById(R.id.editsearch);
        search=(ImageView)findViewById(R.id.search);
        validate=(Button)findViewById(R.id.validate);
        select_drag=(Button)findViewById(R.id.select_drag);
        cancel=(Button)findViewById(R.id.cancel);
        global_latitude= getIntent().getDoubleExtra("LATITUDE", 0);
        global_longitude= getIntent().getDoubleExtra("LONGITUDE", 0);;
        mapFragment.getMapAsync(this);


        setActions();
    }
/*

    private void setupMap(double latitude, double longitude, String title, String subTitle) {
        mapView.getOverlays().clear();
        global_title= title==null?"Lieu du problème":title;
        global_longitude= longitude;
        global_latitude= latitude;

        GeoPoint gp= new GeoPoint(global_latitude, global_longitude);
        if(global_latitude==0){
            MyLocationNewOverlay myLocationNewOverlay= new MyLocationNewOverlay(new GpsMyLocationProvider(this),mapView );
            myLocationNewOverlay.enableMyLocation();
            gp= myLocationNewOverlay.getMyLocation();
        }

        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.setBuiltInZoomControls(true);
        IGeoPoint startPoint= new IGeoPoint() {
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
                return global_latitude;
            }

            @Override
            public double getLongitude() {
                return global_longitude;
            }
        };
        IMapController mapController= mapView.getController();
        mapController.setCenter(gp);
        mapController.setZoom(20.8);

        //ArrayList<OverlayItem> items= new ArrayList<>();
        Marker marker = new Marker(mapView);
        marker.setPosition(gp);
        marker.setIcon(getResources().getDrawable(R.drawable.marker));
        marker.setTitle(title);
        marker.setSubDescription(subTitle==null? "":subTitle);
        mapView.getOverlays().add(marker);

        pdial.dismiss();
        select_drag.setVisibility(View.GONE);

        if(!seeing){

           marker.setDraggable(true);
           marker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
               @Override
               public void onMarkerDrag(Marker marker) {

               }

               @Override
               public void onMarkerDragEnd(final Marker marker) {
                   select_drag.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           double latitude = marker.getPosition().getLatitude();
                           double longitude = marker.getPosition().getLongitude();

                           //Moving the map
                           Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                           List<Address>adress = null;
                           pdial.setMessage("Veuillez patienter...");
                           pdial.show();
                           try {
                               adress = geocoder.getFromLocation(latitude, longitude,1);
                               if(adress.size()>=1) {
                                   setupMap(latitude, longitude,adress.get(0).getAddressLine(0), adress.get(0).getAdminArea());
                               }
                           } catch (IOException e) {
                               e.printStackTrace();
                           }
                       }
                   });
               }

               @Override
               public void onMarkerDragStart(Marker marker) {
                   select_drag.setVisibility(View.VISIBLE);
               }
           });
        }


    }
*/

    private void setActions(){
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String edit= edit_search.getText().toString().trim();
                if(!edit.isEmpty()){
                    pdial.setMessage("Recherche en cours...");
                    pdial.show();
                    pdial.setCanceledOnTouchOutside(false);
                    searchFomAdress(edit);
                    edit_search.setText("");
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data= global_latitude+"\t"+global_longitude+"\t"+global_title;
                Cte.saveCurrentProblemLocation(MapsActivity.this, data);
                onBackPressed();
            }
        });
    }

    private void searchFomAdress(String edit) {
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        List<Address> adress = null;
        try {
            adress = geocoder.getFromLocationName(edit+", Bénin", 2);
            if(adress.size()>=1) {
                double latitude= adress.get(0).getLatitude();
                double longitude= adress.get(0).getLongitude();


                Log.d("COORDINATE", latitude+" # "+ longitude);
                String str = adress.get(0).getLocality();
                String st = adress.get(0).getCountryName();
                

                moveMap(latitude, longitude, adress.get(0).getAddressLine(0)+'-'+ adress.get(0).getLocality());

            }else{
                pdial.dismiss();
                Toast.makeText(this, "Localité introuvable au Bénin..!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            pdial.dismiss();
            Toast.makeText(this, "Service non disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void moveMap(double latitude, double longitude, String text){
        global_title= text==null?"Lieu du problème":text;
        global_longitude= longitude;
        global_latitude= latitude;
        LatLng latLng= new LatLng(global_latitude, global_longitude);
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
        .position(latLng)
        .draggable(!seeing)
        .title(global_title)
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))) ;

        if(!seeing) {
            mMap.setOnMarkerDragListener(this);
            mapRipple = new MapRipple(mMap, latLng, this);
            mapRipple.withNumberOfRipples(2);
            mapRipple.withDistance(1000);
            mapRipple.withRippleDuration(5000);
            mapRipple.withTransparency(0.5f);
            mapRipple.startRippleMapAnimation();
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
        pdial.dismiss();
        select_drag.setVisibility(View.GONE);

    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(getIntent().getExtras()!=null && getIntent().getExtras().getBoolean("VISIT", false)){
            validate.setVisibility(View.GONE);
            edit_search.setVisibility(View.GONE);
            search.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
            select_drag.setVisibility(View.GONE);
            seeing=true;
            //setupMap(global_latitude, global_longitude, null, getIntent().getExtras().getString("DETAILS", null));
            moveMap(global_latitude, global_longitude,  getIntent().getExtras().getString("DETAILS", null));
        }else {
            moveMap(global_latitude, global_longitude,  null);
        }

        //moveMap(global_latitude, global_longitude, null);
    }



    /*DRAG*/


    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.clear();
        //Adding a new marker to the current pressed position
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true));

        double latitude = latLng.latitude;
        double longitude = latLng.longitude;
    }

/*    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }*/

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMarkerDragStart(com.google.android.gms.maps.model.Marker marker) {
        select_drag.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMarkerDrag(com.google.android.gms.maps.model.Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(final com.google.android.gms.maps.model.Marker marker) {
        select_drag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double latitude = marker.getPosition().latitude;
                double longitude = marker.getPosition().longitude;

                //Moving the map
                Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                List<Address>adress = null;
                pdial.setMessage("Veuillez patienter...");
                pdial.show();
                try {
                    adress = geocoder.getFromLocation(latitude, longitude,1);
                    if(adress.size()>=1) {
                        moveMap(latitude, longitude,adress.get(0).getAddressLine(0)+'-'+ adress.get(0).getLocality());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    pdial.dismiss();
                    Toast.makeText(MapsActivity.this, "Service non disponible", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
