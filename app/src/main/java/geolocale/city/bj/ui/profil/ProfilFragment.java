package geolocale.city.bj.ui.profil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;
import geolocale.city.bj.R;
import geolocale.city.bj.login_signup.Login;
import geolocale.city.bj.tools.Cte;
import geolocale.city.bj.tools.User;

public class ProfilFragment extends Fragment {

    private MaterialTextView user_name, email, contact, sexe, age, commune, arrond;
    private TextView count_submitted, count_solved;
    private Switch allNotif, vibreur;
    private Button update;
    private CircleImageView profile_pic;
    private Context ctx;
    private User user;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profil, container, false);
        ctx= getContext();
        if(Cte.isConnected(ctx))
            initViews(root);
        else{
            startActivity(new Intent(ctx, Login.class)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }
        return root;
    }

    private void  initViews(View view){
        user_name=(MaterialTextView)view.findViewById(R.id.show_name);
        email=(MaterialTextView)view.findViewById(R.id.email);
        contact=(MaterialTextView)view.findViewById(R.id.contact);
        sexe=(MaterialTextView)view.findViewById(R.id.sexe);
        commune=(MaterialTextView)view.findViewById(R.id.commune);
        arrond=(MaterialTextView)view.findViewById(R.id.arrond);

        age=(MaterialTextView)view.findViewById(R.id.age);

        count_submitted=(TextView)view.findViewById(R.id.counter_all_submitted);
        count_solved=(TextView)view.findViewById(R.id.counter_all_solved);

        allNotif=(Switch)view.findViewById(R.id.allNotif);
        vibreur=(Switch)view.findViewById(R.id.vibreur);

        update= (Button)view.findViewById(R.id.update);

        profile_pic=(CircleImageView)view.findViewById(R.id.profile_pic);

        user= new User(ctx);
        setActions();
    }

    private void setActions(){
        user_name.setText("   "+user.getFirstname()+" "+user.getLastname());
        email.setText("    Email: -");
        contact.setText("    Contact: "+user.getPhone());
        sexe.setText("    Genre: "+user.getGenre());
        arrond.setText("    Arrondissement: -");
        commune.setText("    Commune: "+user.getUser_communeId());
        getCommunes(user.getUser_communeId());
        age.setText("    Tranche d'âge: -");

        getProblem( user.getUser_id());
    }



    private void getCommunes(final String com){

        RequestQueue requestQueue=  Volley.newRequestQueue(ctx);
        String url= Cte.BASE_URL+"/communes";
        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.GET
                , url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray datas= response.getJSONArray("data");

                    for (int i = 0; i <datas.length() ; i++) {
                        JSONObject object= datas.getJSONObject(i);
                        if(com.equals(object.get("id").toString())){
                            commune.setText("    Commune: "+object.get("name").toString());
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


    private void getProblem(final String id){

        Log.d("RESPONSE", id);
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
                    count_submitted.setText(f.length()+"");
                    count_solved.setText("00");
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

}
