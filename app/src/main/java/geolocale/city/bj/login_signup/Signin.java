package geolocale.city.bj.login_signup;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import geolocale.city.bj.R;
import geolocale.city.bj.tools.Cte;

public class Signin extends AppCompatActivity {

    private TextInputEditText name, surname, phone, createPassWord, confirmPassWord;
    private Spinner commune, sexe, arrond;
    private Button signin;
    private TextView goToLogin;
    private ProgressDialog pdial;
    private int commune_index=0;
    private String [] communes ={"Commune", "Avrankou", "Toffo", "Banikora", "Bohicon"};
    private int [] communes_index ={0, 1,2, 3, 4};
    private int arrond_index=0;
    private String [] genres ={"M", "F"};
    private String genre= genres[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        pdial= new ProgressDialog(this);
        initViews();
        setActions();
        getCommunes();
    }


    private void initViews(){
        name=(TextInputEditText)findViewById(R.id.name);
        surname=(TextInputEditText)findViewById(R.id.surname);
        phone=(TextInputEditText)findViewById(R.id.phone);
        commune=(Spinner)findViewById(R.id.email);
        arrond=(Spinner)findViewById(R.id.arrond);
        sexe=(Spinner)findViewById(R.id.sexe);
        createPassWord=(TextInputEditText)findViewById(R.id.createPassWord);
        confirmPassWord=(TextInputEditText)findViewById(R.id.confirmPassWord);

        signin=(Button)findViewById(R.id.validate);

        goToLogin= (TextView)findViewById(R.id.textmidlle3);

    }

    private void setActions(){
        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input_name= name.getText().toString().trim();
                String input_surname= surname.getText().toString().trim();
                String input_phone= phone.getText().toString().trim();
                String input_password= createPassWord.getText().toString().trim();

                if(input_name.isEmpty()||input_surname.isEmpty() || commune_index<=0 || arrond_index <=0||input_password.isEmpty() ||input_phone.isEmpty())
                    Toast.makeText(Signin.this, "Tous les champs sont obligatoires !", Toast.LENGTH_SHORT).show();
                else if(!input_password.equals(confirmPassWord.getText().toString()))
                    Toast.makeText(Signin.this, "Les mots de passe ne correspondent pas ...!", Toast.LENGTH_SHORT).show();
                else{
                    requestSignup(input_name,
                            input_surname,
                            input_phone,
                            input_password,
                            genre,
                            "user",
                            commune_index);
                }

            }
        });

        setSpinnerAction();

        ArrayAdapter aa_genre= new ArrayAdapter(this, android.R.layout.simple_spinner_item, genres);
        aa_genre.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sexe.setAdapter(aa_genre);
        sexe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                genre=genres[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void setSpinnerAction(){

        ArrayAdapter aa= new ArrayAdapter(this, android.R.layout.simple_spinner_item, communes);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        commune.setAdapter(aa);
        commune.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                commune_index=communes_index[position];
                getArrond(commune_index);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
                    communes= new String[datas.length()+1];
                    communes[0]="Choisissez votre commune";

                    communes_index= new int[datas.length()+1];
                    communes_index[0]= 1;
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

            }
        });

        requestQueue.add(jsonObjectRequest);

    }

    private void requestSignup(String nom, String prenom, final String contact, final String password, String genre, final String rule, int commune_id){
        pdial.setMessage("Inscription en cours...");
        pdial.setCanceledOnTouchOutside(false);
        pdial.setCancelable(false);
        pdial.show();

        RequestQueue requestQueue=  Volley.newRequestQueue(this);
        String url= Cte.BASE_URL+"/register";
        JSONObject entry = new JSONObject();
        try {
            entry.put("firstname", prenom);
            entry.put("lastname", nom);
            entry.put("phone", contact);
            entry.put("commune_id", commune_id);
            //entry.put("arrondissement_id", arrond_index);
            entry.put("password", password);
            entry.put("genre", genre);
            entry.put("role", rule);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(url, entry, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                pdial.dismiss();
                Log.d("RESPONSE", response.toString());
                try {
                    String status= response.get("status").toString();
                    if(status.equals("success")){
                        JSONObject data= response.getJSONObject("data");
                        Cte.ecrire(Signin.this, "user.txt", data.toString());
                        String user_id= data.getInt("id")+"";
                        String lastname= data.getString("lastname");
                        String firstname= data.getString("firstname");
                        String phone= data.getString("phone");
                        String commune_id= data.getString("commune_id");
                        String genre= data.getString("genre");
                        String updated_at= data.getString("updated_at");
                        String created_at= data.getString("created_at");
                        String token="No token";
                        if(!rule.startsWith("user"))
                            token= response.getString("token");

                        String user_info= user_id+"\t"+lastname+"\t"+firstname+"\t"+phone+"\t"+commune_id+"\t"+genre+"\t"+updated_at+"\t"+created_at+"\t"+token+"\t"+password;
                        Cte.setUser_id(Signin.this, user_id);


                        Cte.saveUserInfo(Signin.this, user_info);

                        AlertDialog.Builder al=Cte.showSimpleDialog(Signin.this, "L'inscription a été faite avec succès !! Bienvenue sur GoLocal "+firstname);
                        AlertDialog alertDialog= al.create();
                        alertDialog.show();
                        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                onBackPressed();
                            }
                        });
                    }else{
                        Cte.showSimpleDialog(Signin.this, "L'inscription s'est faite avec succès mais un problème a survenu pendant la configuration...\nVeuillez vous connecter vous GoLocal pour terminer la configuration !")
                                .setTitle("ALERTE")
                                .create()
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pdial.dismiss();
                error.printStackTrace();
                if(error.getMessage()!=null && error.getMessage().toLowerCase().contains("timeout"))
                    Cte.showSimpleDialog(Signin.this, "Le délai d'attente a expiré...\nVeuillez revoir votre connexion INTERNET et réessayer...")
                            .setTitle("Echec de l'inscription")
                            .create()
                            .show();
                else
                    Cte.showSimpleDialog(Signin.this, "Il se peut que vous ayez déjà un compte GoLocal ou que votre numéro soit déjà utilisé pour l'inscription...\nVous pouvez essayer de vous connecter")
                            .setTitle("Echec de l'inscription")
                            .create()
                        .show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }


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



}

/*
D/RESPONSE: {"status":"success","login":true,"token":"37|hCLtzMdqf9VidXMRfL8QWsrLMOCUinXfXVZ1CkgR",
        "data":{"id":20,"firstname":"Marcos","lastname":"VITOULEY","phone":"61857795",
        "email":null,"email_verified_at":null,"genre":"M   " +
        "" +
        ","rule":"users","arrondissement":null,"commune_id":5,"created_at":"2021-01-07T22:53:43.000000Z"," +
        ""updated_at":"2021-01-07T22:53:43.000000Z"" +
        "}}*/