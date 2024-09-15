package geolocale.city.bj;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import geolocale.city.bj.submit.Submit;
import geolocale.city.bj.tools.Cte;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               submissionRequest(MainActivity.this);
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        navigationOptions(navigationView);
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_accueil,
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    private void submissionRequest(final Context ctx) {
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

    private void navigationOptions(NavigationView navigationView) {
        navigationView.getMenu().getItem(navigationView.getMenu().size()-1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AlertDialog dialog= Cte.showSimpleDialog(MainActivity.this, "Vous serez redirigé sur la page web du projet GoLocal")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri uri = Uri.parse(getResources().getString(R.string.linkProject));
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        }).create();
                dialog.show();

                return false;
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(Cte.isConnected(this)){
            String [] infos= Cte.retrieveUserInfo(this);
            TextView tv_user_name= (TextView)navigationView.getHeaderView(0).findViewById(R.id._name);
            if(tv_user_name!=null)
                tv_user_name.setText(infos.length>=3?infos[1]+" "+infos[2]: "Anonymat");
        }
    }
}
