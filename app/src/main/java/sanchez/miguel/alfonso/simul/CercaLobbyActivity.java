package sanchez.miguel.alfonso.simul;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.material.textfield.TextInputEditText;

public class CercaLobbyActivity extends AppCompatActivity {

    TextInputEditText cerca_edit_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cerca_lobby);
    }
}