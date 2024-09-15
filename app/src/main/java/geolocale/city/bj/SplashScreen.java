package geolocale.city.bj;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import geolocale.city.bj.tools.Cte;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        Thread th= new Thread(){
            @Override
            public void run() {
                try {
                    sleep(2700);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if(!Cte.isFistVisit(SplashScreen.this)){
                        startActivity(new Intent(SplashScreen.this, MainActivity.class));
                    }else{
                        startActivity(new Intent(SplashScreen.this, WelcomePage.class));
                    }
                    finish();
                }
            }
        };

        th.start();
    }
}
