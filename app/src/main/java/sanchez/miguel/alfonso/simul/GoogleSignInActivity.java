package sanchez.miguel.alfonso.simul;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class GoogleSignInActivity extends App {
    public ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Inizializzo variabili globali fondamentali
        //SharedPreferences per memorizzare i dati nella memoria interna anche quando l'app chiude
        prefs = getSharedPreferences("MY_PREFS_NAME", MODE_PRIVATE);
        editor = prefs.edit();
        //Inizializzo variabili per autenticazione
        mAuth = FirebaseAuth.getInstance();
        User = mAuth.getCurrentUser();

        //Inizializzo reference ai nodi del database
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        //Prima ancora di avviare l'activity e settare il layout controllo se sono già registrato.
        if (check_connection() && check_login() && check_registration()) {

            prendi_user_id_attuale();
            startActivity(new Intent(GoogleSignInActivity.this, MainActivity.class));
        }
        //Non sono registrato o loggato e quindi setuppo l'activity.
        else{
            //Variabili per il GoogleSignIn
            gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(web_client_for_google_sign_in)
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
            RC_SIGN_IN = 1;
            signInIntent = mGoogleSignInClient.getSignInIntent();

            setContentView(R.layout.activity_main);

            //Todo Martin : iconcina di google a sinistra all'interno del bottone, c'è un comando apposta in xml, drawableleft e aggiungi drawable fatto apposta, io so già come fare ma non voglio toglierti lavoro in modo che tu possa imparare a fare anche queste cose ;-)
            findViewById(R.id.google_register_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try_google_sign_in();
                }
            });
        }

    }

    //starto l'activity sopra l'activity principale, se mi da il requestcode chiesto allora avvio il login con google.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }




    //Metodo all'onclick del bottone
    protected void try_google_sign_in() {
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //gestisco il risultato dell'activity result
    protected void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount acc = completedTask.getResult(ApiException.class);
            FirebaseGoogleAuth(acc);
        } catch (ApiException e) {
            Toast.makeText(GoogleSignInActivity.this, "ApiException, bad configurazione (controlla l'id del sign in)", Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuth(null);
        }
    }


    protected void FirebaseGoogleAuth(GoogleSignInAccount acct) {
        if (acct != null) {
            AuthCredential authCredential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            progressDialog = new ProgressDialog(GoogleSignInActivity.this, R.style.MyAlertDialogStyle);
            progressDialog.setTitle("Solo un secondo!");
            progressDialog.setMessage("Stiamo cercando di connetterci ai nostri server...");
            progressDialog.show();
            progressDialog.setCancelable(false);

            mAuth.signInWithCredential(authCredential).addOnCompleteListener(GoogleSignInActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        //Necessario creare qui un firebaseuser poichè è dopo la connessione
                        final FirebaseUser utente_dopo_connessione = mAuth.getCurrentUser();
                        //Prendo stringa Uid dello user corrente
                        final String user_id = Objects.requireNonNull(utente_dopo_connessione).getUid();
                        updateUI(utente_dopo_connessione);
                        progressDialog.setTitle("Connesso!");
                        progressDialog.setMessage("Ora controlliamo se sei nei nostri database...");


                        UsersRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(user_id)) {
                                    Log.i("UTENTE", "Utente trovato, vado nel activity_main_container");
                                    //Se ho già dati nel db allora vado in MainContainerActivity altrimenti vado nell'attività dove richiedo i dati
                                    progressDialog.dismiss();
                                    editor.putBoolean("hasRegistration", true);
                                    editor.apply();
                                    startActivity(new Intent(GoogleSignInActivity.this, MainActivity.class));
                                    UsersRef.removeEventListener(this);
                                } else {
                                    Log.i("UTENTE", "Utente  non trovato, vado in setup");
                                    //Vado nell'attività di setup
                                    startActivity(new Intent(GoogleSignInActivity.this, RegisterActivity.class));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });

                    } else {
                        updateUI(null);
                        progressDialog.dismiss();
                        //-//
                        Log.i("CREDENZIALI ACCESSO", "Connessione a firebase non riuscita");
                        Toast.makeText(GoogleSignInActivity.this, "Connessione non riuscita Internet non disponibile", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {

            Log.i("CREDENZIALI ACCESSO", "Tentativo annullato dall'utente");
            Toast.makeText(GoogleSignInActivity.this, "Tentativo annullato dall' utente", Toast.LENGTH_SHORT).show();

        }
    }


    private void updateUI(FirebaseUser fUser) {
        if (fUser != null) {
            //Prendo nome da database
            String personEmail = fUser.getEmail();
            String personuid = fUser.getUid();

            //Registro l'avvenuta connessione al server sulle Sharedpreferences
            editor.putString("email", personEmail);
            editor.putString("uid", personuid);
            editor.putBoolean("CONNECTION", true);
            editor.putBoolean("hasLogin", true);
            editor.apply();

        } else {
            //Caso internet non disponibile, refresho Sharedpreferences
            editor.putBoolean("CONNECTION", false);
            editor.putBoolean("hasLogin", false);
            editor.apply();
            Log.i("STATO CONNESSIONE", "" + prefs.getBoolean("CONNECTION", false));
            Log.i("STATO LOGIN", "" + prefs.getBoolean("hasLogin", false));

        }
    }

}