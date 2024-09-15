package geolocale.city.bj.submit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import geolocale.city.bj.R;
import geolocale.city.bj.tools.Cte;
import geolocale.city.bj.tools.User;

public class SeeAllProblems extends AppCompatActivity {

    private Spinner spinner;
    private ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView recycleAllProblem;
    private String tris[]= {"Tous les problèmes", "Problèmes de ma commune"};
    private Switch onlySolved;
    private JSONArray myComData, recyleData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_all_problems);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Problèmes soumis");
        initViews();
        setActions();
    }

    private void initViews(){
        spinner= (Spinner)findViewById(R.id.spinner);
        onlySolved= (Switch)findViewById(R.id.solved);
        shimmerFrameLayout =(ShimmerFrameLayout)findViewById(R.id.fbshimmer);
        recycleAllProblem= (RecyclerView)findViewById(R.id.recyclerAllP);
    }



    private void setActions(){
        completeSpinner();
    }

    private void completeSpinner(){
        ArrayAdapter arrayAdapter= new ArrayAdapter(this, android.R.layout.simple_spinner_item, tris);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position ==1 ){
                    if(myComData==null) {
                        if(Cte.isConnected(SeeAllProblems.this)) {
                            User user= new User(SeeAllProblems.this);
                            getProblem("commune", user.getUser_communeId(), 0);
                        }else
                            Toast.makeText(SeeAllProblems.this, "Veuillez d'abord vous connecter...", Toast.LENGTH_SHORT).show();
                    }else {
                        try {
                            completeRecycler(myComData, 0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }else {
                    if(recyleData==null)
                        getProblem("", "", 1);
                    else {
                        try {
                            completeRecycler(recyleData, 1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }else
            return super.onOptionsItemSelected(item);
    }

    private void getProblem(final String type, final String id, final int position){
        shimmerFrameLayout.startShimmer();
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        recycleAllProblem.setVisibility(View.GONE);

        RequestQueue requestQueue=  Volley.newRequestQueue(this);
        String url= Cte.BASE_URL+"/problemes";
        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.GET
                , url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("RESPONSE", response.toString());

                try {
                    JSONArray datas= response.getJSONArray("data");
                    if(!type.isEmpty()){
                        for (int i=0; i<datas.length(); i++){
                            JSONObject jo= datas.getJSONObject(i);
                            if(!jo.get("commune_id").toString().equals(id+"")){
                                datas.remove(i);
                            }
                        }
                    }
                    JSONArray f= new JSONArray();
                    for (int i = 0; i <datas.length(); i++) {
                        JSONObject object= datas.getJSONObject(i);
                         if(Cte.statutAffichable(object)){
                             f.put(object);
                         }
                    }
                    completeRecycler(f, position);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                recycleAllProblem.setVisibility(View.GONE);
                shimmerFrameLayout.setVisibility(View.GONE);
                Toast.makeText(SeeAllProblems.this, "Echec de la connexion... Veuillez rafraîchir la page..!", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    private void completeRecycler (JSONArray datas, int type) throws JSONException {
        if (type==0)
            myComData = datas;
        else
            recyleData = datas;
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        recycleAllProblem.setVisibility(View.VISIBLE);

        List<Problem> problemList= new ArrayList<>();
        ProblemAdapter problemAdapter = new ProblemAdapter(problemList);
        recycleAllProblem.setNestedScrollingEnabled(false);
        recycleAllProblem.setAdapter(problemAdapter);
        recycleAllProblem.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));

        for (int i = 0; i <datas.length() ; i++) {
            JSONObject object= datas.getJSONObject(i);
            //if(object.getString("statut").equalsIgnoreCase("actif"))
            {
                Problem p =new Problem();
                p.setProblemData(object);
                problemList.add(p);
                problemAdapter.notifyDataSetChanged();
            }
        }


    }



}
