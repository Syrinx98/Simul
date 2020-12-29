package sanchez.miguel.alfonso.simul;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static android.content.Context.MODE_PRIVATE;
import static sanchez.miguel.alfonso.simul.BaseActivity.CurrentUserRef;
import static sanchez.miguel.alfonso.simul.BaseActivity.NicknameRef;
import static sanchez.miguel.alfonso.simul.BaseActivity.UsersRef;
import static sanchez.miguel.alfonso.simul.BaseActivity.data_creazione_account;
import static sanchez.miguel.alfonso.simul.BaseActivity.dialog;
import static sanchez.miguel.alfonso.simul.BaseActivity.editor;
import static sanchez.miguel.alfonso.simul.BaseActivity.email;
import static sanchez.miguel.alfonso.simul.BaseActivity.link_immmagine_dentro_db;
import static sanchez.miguel.alfonso.simul.BaseActivity.mGoogleSignInClient;
import static sanchez.miguel.alfonso.simul.BaseActivity.nickname;
import static sanchez.miguel.alfonso.simul.BaseActivity.prefs;
import static sanchez.miguel.alfonso.simul.BaseActivity.public_progressdialog;
import static sanchez.miguel.alfonso.simul.BaseActivity.web_client_for_google_sign_in;


public class ProfiloFragment extends BaseFragment {

    private ImageView user_img;
    private TextView email_textview;
    private TextView date_textview;
    private TextView nickname_textview;
    private MaterialCardView profilo_crea_angelo;
    private Dialog crea_angelo_dialog;
    private TextInputEditText angelo_reference;
    private TextInputEditText angelo_nome;

    public ProfiloFragment() {
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


        user_img = view.findViewById(R.id.user_photo);
        email_textview = view.findViewById(R.id.email);
        date_textview = view.findViewById(R.id.creation_date);
        nickname_textview = view.findViewById(R.id.nickname);
        profilo_crea_angelo = view.findViewById(R.id.profilo_crea_angelo);
        crea_angelo_dialog = new Dialog(view.getContext());

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

        profilo_crea_angelo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupAngelo();
            }
        });

        //Update delle informazioni, posso chiamarlo perchè per essere arrivato qua i dati devono essere stati scaricati
        update_ui_user_information();

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
        MaterialCardView confirm =  dialog.findViewById(R.id.imfinethanks);
        TextView t = dialog.findViewById(R.id.alarm_popup_message);

        t.setText("Sicuro di voler eliminare il tuo account?\ni tuoi dati online andranno persi");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete_account_from_database(context);
            }
        });

        MaterialCardView no =  dialog.findViewById(R.id.no_delete_calibration);
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
        NicknameRef = FirebaseDatabase.getInstance().getReference().child("Nicknames");
        nickname = prefs.getString("nickname","none");
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

    private void update_ui_user_information(){
        link_immmagine_dentro_db = prefs.getString("immagine","-");
        email = prefs.getString("email","sas@ses.com");
        data_creazione_account = prefs.getString("data_creazione","01/01/1900");

        String resultDate = convertStringDateToAnotherStringDate(data_creazione_account, "dd-MM-yyyy", "dd MMMM yyyy");

        Picasso.get()
                .load(link_immmagine_dentro_db)
                .transform(new CropCircleTransformation())
                .placeholder(R.drawable.unknown_user)
                .error(R.drawable.unknown_user)
                .into(user_img);

        email_textview.setText(email);
        date_textview.setText("Account creato il " + resultDate);
        nickname_textview.setText(nickname);
    }

    public String convertStringDateToAnotherStringDate(String stringdate, String stringdateformat, String returndateformat){

        try {
            Date date = new SimpleDateFormat(stringdateformat).parse(stringdate);
            String returndate = new SimpleDateFormat(returndateformat, Locale.ITALIAN).format(date);
            return returndate;
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }

    }

    private void showPopupAngelo(){

        crea_angelo_dialog.setContentView(R.layout.dialog_modifica_dati_angelo);
        crea_angelo_dialog.show();
        crea_angelo_dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        final View view = crea_angelo_dialog.getWindow().getDecorView();
        view.setBackgroundResource(android.R.color.transparent);

        //Bindings
        angelo_nome = view.findViewById(R.id.angelo_nome);
        angelo_reference = view.findViewById(R.id.angelo_numero);

        //setto il name e la reference già esistenti
        angelo_nome.setText(prefs.getString("angel_name","Non ancora impostato"));
        angelo_reference.setText(prefs.getString("angel_reference","Non ancora impostato"));

        view.findViewById(R.id.btn_conferma_dati_angelo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Getto
                String angel_name = angelo_nome.getText().toString();
                String angel_reference = angelo_reference.getText().toString();
                //Trimmo
                angel_name = Objects.requireNonNull(angel_name.trim());
                angel_reference = Objects.requireNonNull(angel_reference.trim());
                //Provo
                if (check_angel_name_and_reference(angel_name,angel_reference)){
                    set_angel_in_database(angel_name,angel_reference);
                }
            }
        });
    }

    private boolean check_angel_name_and_reference(String angel_name,String angel_reference){
        if (check_angel_name(angel_name)){
            Toast.makeText(getContext(),"Il nome è troppo corto",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (check_angel_reference(angel_reference)){
            Toast.makeText(getContext(),"Il numero di telefono è troppo corto",Toast.LENGTH_SHORT).show();
            return false;
        }
        else{
            return true;
        }
    }

    private boolean check_angel_name(String angel_name){
        return angel_name.length() < 3;
    }


    private boolean check_angel_reference(String angel_reference){
        return angel_reference.length() < 8 ;
    }


    private void set_angel_in_database(final String angel_name,final String angel_reference){
        //Spawn progressdialog
        public_progressdialog = ProgressDialog.show(getActivity(), null,null, false, false );
        Objects.requireNonNull(public_progressdialog.getWindow()).setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ));
        public_progressdialog.setContentView(R.layout.dialog_bar);


        prendi_user_id_attuale();
        CurrentUserRef = UsersRef.child(current_user_id);
        HashMap<String, Object> angelMap = new HashMap<>();
        angelMap.put("guardian_angel_name",angel_name);
        angelMap.put("guardian_angel_reference",angel_reference);

        CurrentUserRef.updateChildren(angelMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                //Aggiorno in sharedpreferences
                editor.putString("angel_name",angel_name);
                editor.putString("angel_reference",angel_reference);
                editor.apply();

                Toast.makeText(getContext(),"Il tuo angelo custode ora ti protegge in caso di incidente!",Toast.LENGTH_SHORT).show();
                public_progressdialog.dismiss();
                crea_angelo_dialog.dismiss();
            }
        });

    }

}