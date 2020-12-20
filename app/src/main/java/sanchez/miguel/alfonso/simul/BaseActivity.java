package sanchez.miguel.alfonso.simul;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

abstract class BaseActivity extends AppCompatActivity {

    //Google singIn release key presa dall'api console
    public static final String web_client_for_google_sign_in = "436100610708-gfrks3cgfganfa8tgj3ic54us9kempjm.apps.googleusercontent.com";

    //Inizializzo questi valori nell'attività di login prima del controllo

    public static SharedPreferences prefs;
    public static SharedPreferences.Editor editor;
    public static FirebaseAuth mAuth;
    public static FirebaseUser User;

    //Informazioni Utente
    public static String current_user_id;
    public static String data_attuale, ora_attuale, data_e_ora;
    public static String data_creazione_account;
    public static String link_immmagine_dentro_db;
    public static String nickname;
    public static String email;
    public static String angel_name;
    public static String angel_reference;

    //Variabili per il client in locale di google sing in
    public GoogleSignInOptions gso;
    public static GoogleSignInClient mGoogleSignInClient;
    //Intent del sign in (come avviare un activity)
    public int RC_SIGN_IN;
    public Intent signInIntent;

    //Oggetti pubblici
    public static ProgressDialog public_progressdialog;
    public static Dialog dialog;

    //Database References : storage
    public static StorageReference image_storage, immagine_utente;

    //Database References : realtime database
    public static DatabaseReference NicknameRef;
    public static DatabaseReference UsersRef;
    public static DatabaseReference CurrentUserRef;
    public static DatabaseReference RoomsRef;

    //Variabili necessarie per i metodi pubblici
    private int registration_three_way_check;


    //valori soglia accelerometro e velocità
    public static final double SOGLIA_ACCELEROMETRO = 50.0;
    public static final double SOGLIA_VELOCITA_KM_H = 0;
    public static final int ALARM_COUNTDOWN_MAX_TIME = 10000;

    private boolean dialog_is_already_opened = false;

    CountDownTimer alarm_countdown;

    boolean check_accelerometer_anomalies(double val){
        return val >= SOGLIA_ACCELEROMETRO;
    }

    boolean check_velocity_anomalies(double val){
        return val >= SOGLIA_VELOCITA_KM_H;
    }


    protected void prendi_user_id_attuale() {
        mAuth = FirebaseAuth.getInstance();
        User = mAuth.getCurrentUser();
        current_user_id = Objects.requireNonNull(Objects.requireNonNull(User).getUid());
    }

    //controllo se ho un utente connesso attualmente
    protected boolean check_connection() {
        return (mAuth.getCurrentUser() != null);
    }

    //controllo se mi sono già loggato in precedenza
    protected boolean check_login() {
        return prefs.getBoolean("hasLogin", false);
    }

    //controllo se mi sono già registrato in precedenza
    protected boolean check_registration() {
        return prefs.getBoolean("hasRegistration", false);
    }


    protected void editext_listener(final Context context, final EditText nick_editext) {
        nick_editext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (nick_check(context,nick_editext)) {
                    check_nick_existance(context,nick_editext);
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    protected void initilize_google_variables(){
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(web_client_for_google_sign_in)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
        RC_SIGN_IN = 1;
        signInIntent = mGoogleSignInClient.getSignInIntent();
    }

    protected boolean nick_check(Context context, EditText NickText){
        final String nickname = NickText.getText().toString();
        if (TextUtils.isEmpty(nickname)){
            NickText.setBackgroundResource(R.drawable.fish_red_stroke);
            return false;
        }
        else if (nickname.length() < 3){
            NickText.setBackgroundResource(R.drawable.fish_red_stroke);
            return false;
        }
        else if (nickname.contains(" ")){
            Toast.makeText(context, "Il nickname non può contenere spazi", Toast.LENGTH_SHORT).show();
            NickText.setBackgroundResource(R.drawable.fish_red_stroke);
            return false;
        }
        else {
            NickText.setBackgroundResource(R.drawable.fish_green_stroke);
            return true;
        }
    }


    protected void check_nick_existance(final Context context, final EditText nick_editext) {
        if (check_connection()) {
            final String nickname = nick_editext.getText().toString();
            NicknameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild(nickname)) {
                            if (!TextUtils.isEmpty(nickname)) {
                                Toast.makeText(context, "Il nickname scelto è già presente", Toast.LENGTH_SHORT).show();
                                //Todo : caso in cui il nickname vada bene, modificare per conformarlo a quello che hai inserito tu @Martin
                                //Basta che modifichi i file drawable
                                nick_editext.setBackgroundResource(R.drawable.fish_red_stroke);
                            }
                        } else {
                            //Todo : caso in cui il nickname vada bene, modificare per conformarlo a quello che hai inserito tu @Martin
                            //Basta che modifichi i file drawable 
                            nick_editext.setBackgroundResource(R.drawable.fish_green_stroke);
                        }
                    }
                    NicknameRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        } else {
            Toast.makeText(context, "Connessione persa", Toast.LENGTH_SHORT).show();
        }
    }

    protected void prendi_data_e_ora_attuale() {
        prendi_data_attuale();
        prendi_ora_attuale();
        data_e_ora = data_attuale + " " + ora_attuale;
    }

    protected void prendi_data_attuale() {
        Calendar calldate = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat currdate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        data_attuale = currdate.format(calldate.getTime());
    }

    protected void prendi_ora_attuale() {
        Calendar calldate = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat currtime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        ora_attuale = currtime.format(calldate.getTime());
    }

    protected void metto_immagine_in_database(final Context context, Uri local_uri, final String mode,final String nickname) {

        registration_three_way_check = 0;
        prendi_user_id_attuale();
        prendi_data_e_ora_attuale();

        public_progressdialog = new ProgressDialog(context);
        public_progressdialog.show();
        public_progressdialog.setCanceledOnTouchOutside(false);
        public_progressdialog.setTitle("Verificando i tuoi dati...");

        immagine_utente = image_storage.child(current_user_id + ".jpg");
        immagine_utente.putFile(Objects.requireNonNull(local_uri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                public_progressdialog.dismiss();
                immagine_utente.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        link_immmagine_dentro_db = uri.toString();
                        if (mode.equals("registrazione")){
                            metto_altri_dati(context,nickname);
                        }
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "sis " + e.getMessage(), Toast.LENGTH_SHORT).show();
                public_progressdialog.dismiss();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                //displaying the upload progress
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                public_progressdialog.setMessage("Caricamento " + ((int) progress) + "%...");
            }
        });


    }


    private void metto_altri_dati(final Context context,final String nickname){

        prendi_data_e_ora_attuale();
        prendi_user_id_attuale();

        HashMap<String, Object> userMap = new HashMap<>();

        registration_three_way_check = registration_three_way_check + 1;
        userMap.put("uid", current_user_id);
        userMap.put("data", data_attuale);
        userMap.put("ora", ora_attuale);
        userMap.put("nickname", nickname);
        userMap.put("email",prefs.getString("email","Mail non trovata"));
        userMap.put("user_image",link_immmagine_dentro_db);
        //L'utente lo imposterà dopo, questo ci da la possibilità di vedere quanti effettivamente usano la funzione
        userMap.put("guardian_angel_name","Non ancora impostato");
        userMap.put("guardian_angel_reference","Non ancora impostato");


        //Ora creo un figlio di Users, il figlio sarà l'id univoco
        CurrentUserRef = UsersRef.child(current_user_id);
        CurrentUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    registration_three_way_check = registration_three_way_check + 1;
                    editor.putBoolean("hasRegistration", true);
                    editor.apply();
                } else {
                    String message = Objects.requireNonNull(task.getException()).getMessage();
                    Toast.makeText(context, "Errore: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
        //Metto info in Nicknames
        NicknameRef.child(nickname).setValue(0).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    registration_three_way_check = registration_three_way_check + 1;
                    if (registration_three_way_check == 3){
                        Toast.makeText(context, "Account creato correttamente", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else {
                        String message = Objects.requireNonNull(task.getException()).getMessage();
                        Toast.makeText(context, "Errore: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    protected void update_current_speed_in_database(String uid_creatore,String uid_utente,String speed){

        RoomsRef = FirebaseDatabase.getInstance().getReference().child("Rooms");

        RoomsRef.child(uid_creatore).child("partecipanti").child(uid_utente).child("participant_speed").setValue(speed).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }

    protected void pop_alarm_possible_sinister(Context context, final String uid_creatore, final String uid_utente, final Location current_location){

        //controllo se il dialog non sia già aperto
        if (!dialog_is_already_opened){

            dialog_is_already_opened = true;
            dialog = new Dialog(context);
            dialog.setCancelable(false);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.sinister_alarm_popup_layout);
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


            if (!(context instanceof Activity && ((Activity) context).isFinishing())) {
                dialog.show();
            }

            // set the custom dialog components - text, image and button
            ImageButton confirm =  dialog.findViewById(R.id.imfinethanks);
            TextView t = dialog.findViewById(R.id.alarm_popup_message);
            final TextView time = dialog.findViewById(R.id.countdown_time);

            t.setText("Hey!\nTutto ok?");
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    close_dialog();
                }
            });

            //Inizio il countdown
            alarm_countdown = new CountDownTimer(ALARM_COUNTDOWN_MAX_TIME,500) {
                @Override
                public void onTick(long millisUntilFinished) {
                    String local_time = (millisUntilFinished / 1000) + "";
                    time.setText(local_time);
                }

                @Override
                public void onFinish() {
                    close_dialog();
                    String location_format = "http://maps.google.com/?q=" + current_location.getLatitude() + "," + current_location.getLongitude();
                    String longitude = current_location.getLongitude() + "";
                    String latitude = current_location.getLatitude() + "";
                    String altitude = current_location.getAltitude() + "";
                    String speed = current_location.getSpeed() + "";
                    String time = current_location.getTime() + "";
                    Log.v("CIAO","longitude "+ longitude + "\nlatitude " + latitude + "\naltitude " + altitude + "\nspeed " + speed + "\nora " + time);

                    String message = "Simul Guardian Angel System\nIncidente rilevato a questa posizione\n" + "Longitudine "+ longitude + "\nLatitudine " + latitude + "\nAltitudine " + altitude + "\nVelocità " + speed + "\nLINK\n" + location_format;
                    send_sms(prefs.getString("angel_reference","Non ancora impostato"),message);
                    send_alarm_to_room(uid_creatore,uid_utente);
                }
            };


            alarm_countdown.start();
        }
    }

    private void close_dialog(){
        dialog_is_already_opened = false;
        dialog.dismiss();
        alarm_countdown.cancel();
    }

    private void send_alarm_to_room(String uid_creatore,String uid_utente){
        RoomsRef = FirebaseDatabase.getInstance().getReference().child("Rooms");

        RoomsRef.child(uid_creatore).child("partecipanti").child(uid_utente).child("participant_state").setValue("1").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }


    public void send_sms(String number,String message){

        SmsManager mySmsManager = SmsManager.getDefault();

        ArrayList<String> parts = mySmsManager.divideMessage(message);
        mySmsManager.sendMultipartTextMessage(number,null, parts, null, null);
    }



}















