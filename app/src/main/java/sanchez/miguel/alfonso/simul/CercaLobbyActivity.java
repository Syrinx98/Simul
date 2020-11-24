package sanchez.miguel.alfonso.simul;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
                public_progressdialog.dismiss();
                if (!trovato){
                    Toast.makeText(CercaLobbyActivity.this,"La room è piena o non esiste", Toast.LENGTH_SHORT).show();
                }
                else{
                    startActivity(new Intent(CercaLobbyActivity.this, PartecipanteLobbyActivity.class));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}