package sanchez.miguel.alfonso.simul;

import androidx.annotation.NonNull;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class CercaLobbyActivity extends BaseActivity {

    TextInputEditText cerca_edit_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cerca_lobby);

        //Bindings
        cerca_edit_text = findViewById(R.id.cerca_lobby_input_text);

        cerca_edit_text.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        editext_listener();

        //Resetto il creatore della lobby nella sharedpreferences per evitare bug
        editor.putString("creatore_lobby","nessuno");
        editor.apply();
    }


    private void editext_listener() {
        cerca_edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String upper = s.toString();
                String cleaned_string = Objects.requireNonNull(upper.trim());
                if (check_id_string(cleaned_string)){
                    public_progressdialog = ProgressDialog.show(CercaLobbyActivity.this, null,null, false, false );
                    Objects.requireNonNull(public_progressdialog.getWindow()).setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ));
                    public_progressdialog.setContentView(R.layout.dialog_bar);

                    check_id_existance(cleaned_string);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }


    //Ritorna la lunghezza della stringa pulita
    private boolean check_id_string(String s){
        return s.length() == 8;
    }

    private void check_id_existance(final String s){
        RoomsRef = FirebaseDatabase.getInstance().getReference().child("Rooms");
        Log.v("CercaLobby",s);

        RoomsRef.orderByChild("room_id").equalTo(s).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean trovato = false;
                for(DataSnapshot data: snapshot.getChildren()){
                    String key=data.getKey();
                    Log.v("CercaLobby", "creatore : " + key);
                    editor.putString("creatore_lobby",key);
                    editor.apply();
                    trovato = true;

                }

                if (!trovato){
                    Toast.makeText(CercaLobbyActivity.this,"La room è piena o non esiste", Toast.LENGTH_SHORT).show();
                }
                else{
                    add_participant();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void add_participant(){

        //Preparo dati per la query
        link_immmagine_dentro_db = prefs.getString("immagine","-");
        nickname = prefs.getString("nickname","none");
        prendi_user_id_attuale();
        String creatore_lobby = prefs.getString("creatore_lobby","nessuno");

        //Creo Hashmap informazioni utente
        HashMap<String, Object> participant_map = new HashMap<>();
        participant_map.put("participant_name", nickname);
        participant_map.put("participant_image",link_immmagine_dentro_db);
        participant_map.put("participant_state","0");

        RoomsRef.child(creatore_lobby).child("partecipanti").child(current_user_id).updateChildren(participant_map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                public_progressdialog.dismiss();
                startActivity(new Intent(CercaLobbyActivity.this, LobbyPartecipanteActivity.class));
            }
        });
    }

}