package geolocale.city.bj.tools;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import geolocale.city.bj.R;
import geolocale.city.bj.login_signup.Login;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

//Constantes
public class Cte {


    private static final DecelerateInterpolator DECELERATE_INTERPOLATOR= new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR=new AccelerateInterpolator();
    public static String user_information_file="USER_INFORMATION.vv";
    private static String user_id_file="user_id.vv";
    public static String visit_user_id="VISIT_USER_ID";
    private static String FISRT_VISIT="first_visit.vv";
    private static String LOCATION = "current_problem_location.vv";
    public static String BASE_URL="http://api.golocal.bj/api";

    //Show snackBar short_time
    public static void showSnackBar(View view, String message){
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    //Write in file
    public static void ecrire(Context ctx, String fichier, String texte){
        try {
            FileOutputStream fil= ctx.openFileOutput(fichier, MODE_PRIVATE);
            fil.write(texte.getBytes());
            fil.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Read in file
    public static String lire_le_fichier(Context ctx, String fichier){
        String texto="";
        try {
            FileInputStream fil_i=ctx.openFileInput(fichier);  // new FileInputStream(fichier);
            byte[] buffer= new byte[fil_i.available()];
            fil_i.read(buffer);
            texto= new String(buffer);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return texto;
    }

    //Check if first visit
    public static boolean isFistVisit(Context ctx){
        return lire_le_fichier(ctx, FISRT_VISIT).equals("");
    }

    //Set fisrt visit
    public static void setFisrtVisit(Context ctx, boolean enableFisrtVisit){
        if(enableFisrtVisit)
            ecrire(ctx, FISRT_VISIT, "");
        else
            ecrire(ctx, FISRT_VISIT, "True");
    }

    //Save currentProblemLocation
    public static void saveCurrentProblemLocation(Context ctx, String text){
        ecrire(ctx, LOCATION, text);
    }

    //Get Cuurent proclemLocation
    public static String getCurrentProblemLocation(Context ctx){
        return lire_le_fichier(ctx, LOCATION);
    }

    //Check if user is connected
    public static boolean isConnected(Context ctx){
        return !getUser_id(ctx).isEmpty();
    }


    public static String getUser_id(Context ctx){
        return lire_le_fichier(ctx, user_id_file);
    }

    public static void setUser_id(Context ctx, String user_id){ecrire(ctx, user_id_file, user_id);}

    public static void saveUserInfo(Context ctx, String user_info){
        ecrire(ctx, user_information_file, user_info);
    }

    public static String[] retrieveUserInfo(Context ctx){
        return lire_le_fichier(ctx, user_information_file).split("\t");
    }

    public static AlertDialog.Builder showSimpleDialog(Context ctx, String texte){
        AlertDialog.Builder materialAlertDialogBuilder= new AlertDialog.Builder(ctx)
                .setMessage(texte)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return materialAlertDialogBuilder;
    }

    public static String getCurrentFormatDate(){
        SimpleDateFormat sdf= new SimpleDateFormat("dd MMM yyyy", Locale.FRENCH);
        return sdf.format(new Date());
    }

    public static String getFormatDateTime(long time){
        SimpleDateFormat sdf= new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.FRANCE);
        return sdf.format(new Date(time));
    }

    public static String getCurrentFormatDate(long time){
        SimpleDateFormat sdf= new SimpleDateFormat("dd MMM yyyy", Locale.FRANCE);
        return sdf.format(new Date(time));
    }

    public static void showNotif (Context ctx, String title, String caption, int ID_NOTIF, String NC_ID_, Intent chatIntent){

        PendingIntent pendingIntent= PendingIntent.getActivity(ctx, 1, chatIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager nm= (NotificationManager)ctx.getSystemService(NOTIFICATION_SERVICE);
        createDefChan(nm, NC_ID_);

        Bitmap bitmap= BitmapFactory.decodeResource(ctx.getResources(), R.drawable.logo);
        NotificationCompat.Builder builder= new NotificationCompat.Builder(ctx)
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(bitmap)
                .setContentTitle(title)
                .setContentText(caption)
                .setContentIntent(pendingIntent)
                .setPriority(1)
                .setChannelId(NC_ID_);
        nm.notify(ID_NOTIF, builder.build());
    }

    private static void createDefChan(NotificationManager nm, String nc_id_) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel= new NotificationChannel(nc_id_, "GoLocale", NotificationManager.IMPORTANCE_MAX);
            notificationChannel.setDescription("GoLocal Notification");
            notificationChannel.enableLights(true);
            //notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            //notificationChannel.enableVibration(true);
            notificationChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null);
            nm.createNotificationChannel(notificationChannel);
        }
    }

    public static void showProgressNotif (Context ctx, String title, String caption, String NC_ID, int ID_NOTIF,  long current, long total){
        int percentage=0;
        if(total>0)
            percentage=(int)(100*current/total);
        NotificationCompat.Builder builder= new NotificationCompat.Builder(ctx)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setChannelId(NC_ID)
                .setContentText(caption)
                .setProgress(100, percentage, false)
                .setAutoCancel(false);
        NotificationManager nm= (NotificationManager)ctx.getSystemService(NOTIFICATION_SERVICE);
        createDefChan(nm, NC_ID);
        nm.notify( ID_NOTIF, builder.build() );
    }

    public static void dismissProgressN(Context ctx, int ID_NOTIF, String NC_ID){
        NotificationManager nm= (NotificationManager)ctx.getSystemService(NOTIFICATION_SERVICE);
        createDefChan(nm, NC_ID);
        nm.cancel(ID_NOTIF);
    }

    public static void showProfilePic(final Context ctx, String link, final ImageView img, int placeholder){
        if(placeholder==0)
            placeholder= R.drawable.default_img_problem;
        final int finalPlaceholder = placeholder;
        if(link==null)
            return;
        if(link.startsWith("http")) {
            final String finalLink = link;
            Picasso.with(ctx).load(link).placeholder(placeholder).into(img, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(finalLink).placeholder(finalPlaceholder).into(img);
                }
            });
        }else{
            //Picasso.with(ctx).load(new File(link)).placeholder(placeholder).into(img);
            link="https://firebasestorage.googleapis.com/"+link;
            showProfilePic(ctx, link, img, placeholder);
        }
    }

    public static void animateView(View view){
        AnimatorSet animatorSet =new AnimatorSet();

        ObjectAnimator scaleDownX=ObjectAnimator.ofFloat(view, "ScaleX", 0f, 1f);
        scaleDownX.setDuration(250);
        scaleDownX.setInterpolator(ACCELERATE_INTERPOLATOR);

        ObjectAnimator scaleDownY=ObjectAnimator.ofFloat(view, "ScaleY", 0f, 1f);
        scaleDownY.setDuration(250);
        scaleDownY.setInterpolator(DECELERATE_INTERPOLATOR);

        animatorSet.playTogether(scaleDownY, scaleDownX);
        animatorSet.start();
    }

    public static boolean statutAffichable(JSONObject object) throws JSONException {
       return (object.getString("statut").equalsIgnoreCase("actif") ||
                object.getString("statut").equalsIgnoreCase("resolu") ||
                object.getString("statut").equalsIgnoreCase("3") ||
                object.getString("statut").equalsIgnoreCase("1")||
                object.getString("statut").equalsIgnoreCase("non_affecté") ||
                object.getString("statut").equalsIgnoreCase("2"));
    }
}
