package geolocale.city.bj.login_signup;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import geolocale.city.bj.R;
import geolocale.city.bj.tools.Cte;


public class Login extends AppCompatActivity {

    private TextInputEditText contact, password;
    private Button login ;
    private TextView forgetPassword, goToSignIn;
    private ProgressDialog pdial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        pdial= new ProgressDialog(this);

        initViews();
        setActions();
    }

    private void initViews(){
        contact=(TextInputEditText)findViewById(R.id.contact);
        password=(TextInputEditText)findViewById(R.id.password);

        forgetPassword=(TextView)findViewById(R.id.textmidlle3);
        goToSignIn=(TextView)findViewById(R.id.textmidlle4);

        login=(Button)findViewById(R.id.validate);
    }

    private void setActions(){
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input_contact= contact.getText().toString().trim();
                String input_password= password.getText().toString().trim();

                if(input_contact.isEmpty()||input_password.isEmpty())
                    Toast.makeText(Login.this, "Tous les champs sont obligatoires...", Toast.LENGTH_SHORT).show();
                else{
                    requestLogin(input_contact, input_password);
                }
            }
        });

        goToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Signin.class));
            }
        });

        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(Cte.isConnected(this))
            onBackPressed();
    }


    private void requestLogin(String contact, final String password){
        pdial.setMessage("Connexion en cours...");
        pdial.setCanceledOnTouchOutside(false);
        pdial.setCancelable(false);
        pdial.show();

        RequestQueue requestQueue=  Volley.newRequestQueue(this);
        String url= Cte.BASE_URL+"/login";
        JSONObject entry = new JSONObject();
        try {
            entry.put("phone", contact);
            entry.put("password", password);
            entry.put("persistent",  "true");
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
                        Cte.ecrire(Login.this, "user.txt", data.toString());
                        String user_id= data.getInt("id")+"";
                        String lastname= data.getString("lastname");
                        String firstname= data.getString("firstname");
                        String phone= data.getString("phone");
                        String commune_id= data.getString("commune_id");
                        String genre= data.getString("genre");
                        String updated_at= data.getString("updated_at");
                        String created_at= data.getString("created_at");
                        String token="No token";
                        if(!data.getString("role").startsWith("user"))
                            token= response.getString("token");

                        String user_info= user_id+"\t"+lastname+"\t"+firstname+"\t"+phone+"\t"+commune_id+"\t"+genre+"\t"+updated_at+"\t"+created_at+"\t"+token+"\t"+password;
                        Cte.setUser_id(Login.this, user_id);
                        Cte.saveUserInfo(Login.this, user_info);

                        AlertDialog.Builder al=Cte.showSimpleDialog(Login.this, "La connexion a été faite avec succès !!\nNous sommes ravis de vous revoir "+firstname+".");
                        AlertDialog alertDialog= al.create();
                        alertDialog.show();
                        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                onBackPressed();
                            }
                        });
                    }else{
                        Cte.showSimpleDialog(Login.this, "Echec de la connexion...\nIl se peut que vous n'ayez pas saisi le bon MOT de PASSE ou le bon NUMERO de TELEPHONE: \n\n"+response.getString("message"))
                                .setTitle("Echec de la connexion")
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
                    Cte.showSimpleDialog(Login.this, "Le délai d'attente a expiré...\nVeuillez revoir votre connexion INTERNET et réessayer...")
                            .setTitle("Echec de la connexion")
                            .create()
                            .show();
                else
                    Cte.showSimpleDialog(Login.this, "Il se peut que vous n'ayez pas un compte GoLocal, que votre numéro ne soit jamais inscrit ou que vous n'ayez pas saisi le bon MOT de PASSE...\nVeuillez réessayer avec les bonnes informations...")
                            .setTitle("Echec de la connexion")
                            .create()
                            .show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

}
