package sanchez.miguel.alfonso.simul;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static sanchez.miguel.alfonso.simul.BaseActivity.CurrentUserRef;
import static sanchez.miguel.alfonso.simul.BaseActivity.NicknameRef;
import static sanchez.miguel.alfonso.simul.BaseActivity.UsersRef;
import static sanchez.miguel.alfonso.simul.BaseActivity.dialog;
import static sanchez.miguel.alfonso.simul.BaseActivity.editor;
import static sanchez.miguel.alfonso.simul.BaseActivity.mGoogleSignInClient;
import static sanchez.miguel.alfonso.simul.BaseActivity.nickname;
import static sanchez.miguel.alfonso.simul.BaseActivity.prefs;
import static sanchez.miguel.alfonso.simul.BaseActivity.web_client_for_google_sign_in;


public class Profilo extends BaseFragment {

    public Profilo() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        prefs = requireActivity().getSharedPreferences("MY_PREFS_NAME", MODE_PRIVATE);
        editor = prefs.edit();
        editor.apply();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
        //Prendo auth corrente
        mAuth = FirebaseAuth.getInstance();

        prendi_user_id_attuale();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profilo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

            //listeners per i tasti
            view.findViewById(R.id.tasto_logout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sloggami(getContext());
                }
            });

            view.findViewById(R.id.tasto_delete_account).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    delete_account(getContext());
                }
            });

        super.onViewCreated(view, savedInstanceState);
    }


    protected void initilize_google_variables(){
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(web_client_for_google_sign_in)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
        RC_SIGN_IN = 1;
        signInIntent = mGoogleSignInClient.getSignInIntent();
    }


    protected void sloggami(Context context) {
        initilize_google_variables();
        if (mGoogleSignInClient != null){
            mGoogleSignInClient.signOut();
        }
        if (mAuth != null){
            mAuth.signOut();
        }
        FirebaseUser user = Objects.requireNonNull(mAuth).getCurrentUser();
        if (user == null) {
            //Aggiorno SharedPreferences
            editor.putBoolean("hasLogin", false);
            editor.putBoolean("CONNECTION", false);
            editor.putBoolean("hasRegistration", false);
            editor.apply();

            Toast.makeText(context, "Sloggato correttamente", Toast.LENGTH_SHORT).show();

            requireActivity().finish();
        } else {
            Toast.makeText(context, "Non sono riuscito a sloggarmi", Toast.LENGTH_SHORT).show();
        }
    }

    protected void delete_account(final Context context){
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_layout);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        // set the custom dialog components - text, image and button
        ImageButton confirm =  dialog.findViewById(R.id.confirm_delete_calibration);
        TextView t = dialog.findViewById(R.id.calibration_name_calibration_lol);

        t.setText("Sicuro di voler eliminare il tuo account?\ni tuoi dati online andranno persi");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete_account_from_database(context);
            }
        });

        ImageButton no =  dialog.findViewById(R.id.no_delete_calibration);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void delete_account_from_database(final Context context){
        //riprendo lo user id, in caso di crash le variabili statiche si azzerano
        prendi_user_id_attuale();
        CurrentUserRef = UsersRef.child(current_user_id);

        CurrentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                snapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        remove_nickaname_from_database(context);
                    }
                });

                CurrentUserRef.removeEventListener(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                CurrentUserRef.removeEventListener(this);
            }
        });

        dialog.dismiss();
    }

    private void remove_nickaname_from_database(final Context context){
        NicknameRef.child(nickname).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    snapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            sloggami(context);
                            dialog.dismiss();
                        }
                    });
                }
                NicknameRef.child(nickname).removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                NicknameRef.child(nickname).removeEventListener(this);
            }
        });
    }

}