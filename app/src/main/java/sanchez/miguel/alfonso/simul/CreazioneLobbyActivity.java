package sanchez.miguel.alfonso.simul;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.FirebaseDatabase;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Objects;

public class CreazioneLobbyActivity extends BaseActivity{
    private String id;
    private final int LUNGHEZZA_ID = 8;

    TextView lobby_id_textview;
    TextInputEditText destinazione_input_text;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creazione_lobby);

        //La probabilità di collisione è determinata dalla seguente equazione
        //n^2 / (2 * (q)^x )
        //n = numero di valori generati (sempre a 1)
        //q = lunghezza dell'alfabeto usato (sempre 36)
        //x = lunghezza dell'id generato (sempre 8)
        //Con i nostri dati la possibilità è 1 su (5.6 * 10^12) --> (1 / 5.600.000.000.000)
        id = generate_id();

        //Bindings
        lobby_id_textview = findViewById(R.id.chosen_destination);
        destinazione_input_text = findViewById(R.id.destinazione_input_text);

        //Settaggio informazioni a runtime
        lobby_id_textview.setText(id);

        //Listeners
        findViewById(R.id.crea_lobby_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check_destination();
            }
        });


    }


    private String generate_id(){
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(LUNGHEZZA_ID);
        for(int i = 0; i < LUNGHEZZA_ID; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    private void check_destination(){
        int initial_length = Objects.requireNonNull(destinazione_input_text.getText()).toString().length();
        final String clean_destination = Objects.requireNonNull(destinazione_input_text.getText()).toString().trim();
        if (clean_destination.length() != 0){
            crea_stanza();
        }
        else{
            if (initial_length != 0){
                Toast.makeText(CreazioneLobbyActivity.this,"Furbetto!\nNon puoi mettere spazi ;)", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(CreazioneLobbyActivity.this,"Inserisci una destinazione", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private  void crea_stanza(){

        public_progressdialog = ProgressDialog.show(CreazioneLobbyActivity.this, null,null, false, false );
        Objects.requireNonNull(public_progressdialog.getWindow()).setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ));
        public_progressdialog.setContentView(R.layout.dialog_bar);

        RoomsRef = FirebaseDatabase.getInstance().getReference().child("Rooms");
        link_immmagine_dentro_db = prefs.getString("immagine","-");
        nickname = prefs.getString("nickname","none");
        String destinazione = Objects.requireNonNull(destinazione_input_text.getText()).toString().trim();
        editor.putString("destinazione_room",destinazione);
        editor.apply();

        HashMap<String, Object> roomMap = new HashMap<>();
        roomMap.put("room_id",id);
        roomMap.put("creatore", nickname);
        roomMap.put("immagine_creatore",link_immmagine_dentro_db);
        roomMap.put("destinazione",destinazione);

        prendi_user_id_attuale();

        //IMPORTANTE La creo con l'user id attuale perchè l'utente deve poter creare solo una stanza alla volta.
        RoomsRef.child(current_user_id).updateChildren(roomMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                aggiungi_creatore_ai_partecipanti();
            }
        });
    }

    private void aggiungi_creatore_ai_partecipanti(){
        HashMap<String, Object> creator_as_participant_map = new HashMap<>();
        creator_as_participant_map.put("participant_name", nickname);
        creator_as_participant_map.put("participant_image",link_immmagine_dentro_db);
        //TODO DA DECIDERE COSA METTERE NELLO STATO
        creator_as_participant_map.put("participant_state","0");
        RoomsRef.child(current_user_id).child("partecipanti").child(current_user_id).updateChildren(creator_as_participant_map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                public_progressdialog.dismiss();
                startActivity(new Intent(CreazioneLobbyActivity.this, LobbyCreatoreActivity.class));
            }
        });
    }
}
