package geolocale.city.bj.submit;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import geolocale.city.bj.R;
import geolocale.city.bj.tools.Cte;

public class ProblemAdapter extends RecyclerView.Adapter<ProblemAdapter.viewHolder> {

    private List<Problem> problemList;
    private Context ctx;

    public ProblemAdapter(List<Problem> problemList) {
        this.problemList = problemList;
    }

    @NonNull
    @Override
    public ProblemAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ctx= parent.getContext();
        View view= LayoutInflater.from(ctx).inflate(R.layout.item_view_problem, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProblemAdapter.viewHolder holder, int position) {
        Problem p= problemList.get(position);
        final JSONObject pdata= p.getProblemData();
        Log.d("PROBLEM", pdata.toString());
        try {
            String audio= pdata.getString("audio");
            final String categorie = "Non catégorisé";
            final String commune = pdata.getInt("commune_id")+"";
            final String arrondissement = pdata.getInt("arrondissement_id")+"";
            if(audio.isEmpty()){
                holder.title.setText("Problème audio "+
                        pdata.getString("titre")+
                        "\nCatégorie: "+pdata.getJSONObject("category").get("title")+"\tCommune: "+pdata.getJSONObject("commune").get("name")
                );
            }else
                holder.title.setText(
                        pdata.getString("titre")+
                                "\nCatégorie: "+pdata.getJSONObject("category").get("title")+"\tCommune: "+pdata.getJSONObject("commune").get("name")
                );
            String statut=pdata.getString("statut");
            holder.statut.setText((statut.isEmpty() || statut.equals("null"))?
                    "En cours de validation...":
                    pdata.getString("statut")
            );

            holder.dateTime.setText(pdata.getString("created_at"));

            String link= pdata.getString("image").split(";")[0];

            Cte.showProfilePic(ctx, link, holder.photo, 0);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        boolean anonymes=pdata.getInt("anonymes")==1;
                        String audio=pdata.getString("audio");
                        Intent intent= new Intent(ctx, DetailsSubmitting.class)
                                .putExtra("TITLE",  pdata.getString("category_id"))
                                .putExtra("TITRE",  pdata.getString("titre"))
                                .putExtra("LOCALISATION", pdata.getString("latitude")+";"+pdata.getString("longitude"))
                                .putExtra("DESC", pdata.getString("description"))
                                .putExtra("FROM_NAME",anonymes?"Anonymat":pdata.getJSONObject("user").getString("firstname")+" "+ pdata.getJSONObject("user").getString("lastname"))
                                .putExtra("FROM_TIME", pdata.getString("created_at"))
                                .putExtra("COMMUNE", commune)
                                .putExtra("ARRONDISSEMENT", arrondissement)
                                .putExtra("AUDIO", audio)
                                .putExtra("ANONYMAT", anonymes)
                                .putExtra("FILES", pdata.getString("image"))
                                .putExtra("ID", pdata.getString("id"));
                        ctx.startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        return problemList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        private TextView title, statut, dateTime;
        private ImageView photo;

        public viewHolder(@NonNull View itemView) {

            super(itemView);

            title= (TextView)itemView.findViewById(R.id.title);
            statut= (TextView)itemView.findViewById(R.id.statut);
            dateTime= (TextView)itemView.findViewById(R.id.date);

            photo= (ImageView)itemView.findViewById(R.id.img);
        }
    }
}
