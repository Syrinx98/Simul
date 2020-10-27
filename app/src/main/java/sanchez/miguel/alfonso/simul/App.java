package sanchez.miguel.alfonso.simul;

import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.Objects;

abstract class App extends AppCompatActivity {

    //Google singIn release key presa dall'api console
    public static final String web_client_for_google_sign_in = "436100610708-gfrks3cgfganfa8tgj3ic54us9kempjm.apps.googleusercontent.com";

    //Inizializzo questi valori nell'attività di login prima del controllo
    //Todo verificare la correttezza dell'architettura.
    public static SharedPreferences prefs;
    public static SharedPreferences.Editor editor;
    public static FirebaseAuth mAuth;
    public static FirebaseUser User;
    public static String current_user_id;

    //Nodi database
    public static DatabaseReference UsersRef;

    //Variabili per il client in locale di google sing in
    public GoogleSignInOptions gso;
    public GoogleSignInClient mGoogleSignInClient;
    //Intent del sign in (come avviare un activity)
    public int RC_SIGN_IN;
    public Intent signInIntent;


    protected void prendi_user_id_attuale() {
        mAuth = FirebaseAuth.getInstance();
        User = mAuth.getCurrentUser();
        current_user_id = Objects.requireNonNull(Objects.requireNonNull(User).getUid());
    }

}
