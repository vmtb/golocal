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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import geolocale.city.bj.R;
import geolocale.city.bj.tools.Cte;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.viewHolder> {

    private List<Problem> problemList;
    private Context ctx;

    public CommentAdapter(List<Problem> problemList) {
        this.problemList = problemList;
    }

    @NonNull
    @Override
    public CommentAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ctx= parent.getContext();
        View view= LayoutInflater.from(ctx).inflate(R.layout.item_commentaire, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.viewHolder holder, int position) {
        Problem p= problemList.get(position);
        final JSONObject pdata= p.getProblemData();
        Log.d("PROBLEM", pdata.toString());
        try {
            holder.statut.setText(pdata.getString("name"));
            holder.dateTime.setText(pdata.getString("created_at"));
            holder.title.setText(pdata.getString("comments"));
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

            title= (TextView)itemView.findViewById(R.id.tv_cmtaire);
            statut= (TextView)itemView.findViewById(R.id.name);
            dateTime= (TextView)itemView.findViewById(R.id.time);

            //photo= (ImageView)itemView.findViewById(R.id.img);
        }
    }
}
