package sanchez.miguel.alfonso.simul;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Objects;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class MainActivity extends App {

    private boolean connessione_scadente_comunicata;
    private String welcome_text = "";
    private int TEMPO_DI_ATTESA_CONNESSIONE = 7000;

    //Views
    ImageView user_img;
    TextView email_textview,nickname_textview,date_textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        user_img = findViewById(R.id.user_photo);
        email_textview = findViewById(R.id.email);
        nickname_textview = findViewById(R.id.nickname);
        date_textview = findViewById(R.id.creation_date);

        //Scarico le informazioni dell'utente
        download_information(MainActivity.this);

        //setto listeners su logout e su delete account
        listeners_logout_and_delete_account();

    }
    protected void listeners_logout_and_delete_account(){
        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sloggami(MainActivity.this);
            }
        });

        findViewById(R.id.delete_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete_account(MainActivity.this);
            }
        });
    }

    //se premo il pulsante per tornare indietro, esce dall'app.
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    protected void download_information(final Context context){
        prendi_ora_attuale();
        prendi_user_id_attuale();
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay >= 4 && timeOfDay < 13){
            welcome_text = "Buongiorno ";
        }else if(timeOfDay >= 13 && timeOfDay < 17){
            welcome_text = "Buonpomeriggio ";
        }else if(timeOfDay >= 17){
            welcome_text = "Buonasera ";
        }
        else if (timeOfDay >= 1){
            welcome_text = "Ciao!\nè tardi perciò cercherò di fare piano ";
        }

        final CountDownTimer connection_countdowntimer = new CountDownTimer(TEMPO_DI_ATTESA_CONNESSIONE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                Toast.makeText(context,"Sembra che la connessione a internet sia assente o molto scadente!",Toast.LENGTH_SHORT).show();
                connessione_scadente_comunicata = true;
            }
        };
        connection_countdowntimer.start();

        final ProgressDialog ciao = ProgressDialog.show(context, null,null, false, false );
        Objects.requireNonNull(ciao.getWindow()).setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ));
        ciao.setContentView(R.layout.dialog_bar);
        UsersRef.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    current_user_id = Objects.requireNonNull(dataSnapshot.child("uid").getValue()).toString();
                    link_immmagine_dentro_db = Objects.requireNonNull(dataSnapshot.child("user_image").getValue()).toString();
                    data_creazione_account = Objects.requireNonNull(dataSnapshot.child("data").getValue()).toString();
                    nickname = Objects.requireNonNull(dataSnapshot.child("nickname").getValue()).toString();
                    email = Objects.requireNonNull(dataSnapshot.child("email").getValue()).toString();

                    editor.putString("uid",current_user_id);
                    editor.putString("immagine",link_immmagine_dentro_db);
                    editor.putString("data_creazione",data_creazione_account);
                    editor.putString("nickname",nickname);
                    editor.putString("email",email);
                    editor.apply();

                    //todo attenzione a questo passaggio, una volta che ho scaricato i dati li passo alla funzione per l'update dell'ui, scrivo a schermo quello che ho scaricato
                    update_ui_user_information();

                    connection_countdowntimer.cancel();
                    ciao.dismiss();
                    if (connessione_scadente_comunicata){
                        Toast.makeText(context,"Connessione ristabilita! Ben tornato " + nickname,Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(context,welcome_text + nickname,Toast.LENGTH_SHORT).show();
                    }
                }
                UsersRef.child(current_user_id).removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void update_ui_user_information(){
        Picasso.get()
                .load(link_immmagine_dentro_db)
                .transform(new CropCircleTransformation())
                .placeholder(R.drawable.round_images_placeholder)
                .error(R.drawable.unknown_user)
                .into(user_img);
        user_img.setBackground(getResources().getDrawable(R.drawable.round_images_background));

        email_textview.setText(email);
        date_textview.setText("Creato il\n" + data_creazione_account);
        nickname_textview.setText(nickname);


    }
}