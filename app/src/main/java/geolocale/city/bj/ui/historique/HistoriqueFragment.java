package geolocale.city.bj.ui.historique;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import geolocale.city.bj.login_signup.Login;
import geolocale.city.bj.submit.Problem;
import geolocale.city.bj.submit.ProblemAdapter;
import geolocale.city.bj.tools.Cte;
import geolocale.city.bj.tools.User;

public class HistoriqueFragment extends Fragment {

    private ShimmerFrameLayout shimmerFrameLayout;

    private RecyclerView histListsDisp;
    private Context ctx;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_historique, container, false);
        ctx= getContext();
        if(Cte.isConnected(ctx))
            initViews(root);
        else{
            startActivity(new Intent(ctx, Login.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }

        return root;
    }


    private void initViews(View view){
        shimmerFrameLayout=(ShimmerFrameLayout)view.findViewById(R.id.fbshimmer);
        histListsDisp =(RecyclerView)view.findViewById(R.id.recyclerHist);

        startLoading();
        User user= new User(ctx);
        getProblem("", user.getUser_id(), 0);
    }


    private void getProblem(final String type, final String id, final int position){
        shimmerFrameLayout.startShimmer();
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        histListsDisp.setVisibility(View.GONE);

        RequestQueue requestQueue=  Volley.newRequestQueue(ctx);
        String url= Cte.BASE_URL+"/problemes";
        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.GET
                , url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("RESPONSE", response.toString());

                try {
                    JSONArray datas= response.getJSONArray("data");
                    JSONArray f=new JSONArray();
                    for (int i=0; i<datas.length(); i++){
                        JSONObject jo= datas.getJSONObject(i);

                        if(jo.get("user_id").toString().equals(id)) {
                            f.put(jo);
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
                histListsDisp.setVisibility(View.GONE);
                shimmerFrameLayout.setVisibility(View.GONE);
                Toast.makeText(ctx, "Echec de la connexion... Veuillez rafraichir la page..!", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    private void completeRecycler (JSONArray datas, int type) throws JSONException {


        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        histListsDisp.setVisibility(View.VISIBLE);

        List<Problem> problemList= new ArrayList<>();
        ProblemAdapter problemAdapter = new ProblemAdapter(problemList);
        histListsDisp.setNestedScrollingEnabled(false);
        histListsDisp.setAdapter(problemAdapter);
        histListsDisp.setLayoutManager(new LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, true));

        for (int i = 0; i <datas.length() ; i++) {
            JSONObject object= datas.getJSONObject(i);
            Problem p =new Problem();
            p.setProblemData(object);
            problemList.add(p);
            problemAdapter.notifyDataSetChanged();
        }

    }



    private void startLoading(){
        shimmerFrameLayout.startShimmer();
    }
}
