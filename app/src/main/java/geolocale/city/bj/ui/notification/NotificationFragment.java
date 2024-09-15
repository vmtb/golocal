package geolocale.city.bj.ui.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;

import geolocale.city.bj.R;
import geolocale.city.bj.login_signup.Login;
import geolocale.city.bj.submit.DetailsSubmitting;
import geolocale.city.bj.tools.Cte;

public class NotificationFragment extends Fragment {

    private ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView notifsListsDisp;
    private Context ctx;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_notification, container, false);
        ctx= getContext();
        if(Cte.isConnected(ctx))
            initViews(root);




        return root;
    }


    private void initViews(View view){
        shimmerFrameLayout=(ShimmerFrameLayout)view.findViewById(R.id.fbshimmer);
        notifsListsDisp=(RecyclerView)view.findViewById(R.id.recycler_notif);
        startLoading();
    }

    private void startLoading(){
        shimmerFrameLayout.startShimmer();

    }
}
