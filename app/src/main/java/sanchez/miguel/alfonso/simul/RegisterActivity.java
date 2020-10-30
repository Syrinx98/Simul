package sanchez.miguel.alfonso.simul;

import androidx.annotation.Nullable;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import java.util.Objects;

public class RegisterActivity extends App {

    private ImageView user_img;
    private EditText nick_editext;
    private CheckBox privacy_checkbox;
    private Button register_button;

    //Code per callback nel Activityresult
    private static final int  GALLERY_PICK = 1;
    private Uri user_image;

    //variabili per controlli
    private boolean immagine_messa = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //Questa è la mail che ho scelto, se non faccio signout non mi chiederà più di scegliere.
        mGoogleSignInClient.signOut();


        //Bindings
        nick_editext = findViewById(R.id.nick);
        user_img = findViewById(R.id.user_image_input);
        privacy_checkbox = findViewById(R.id.checkBox);
        register_button = findViewById(R.id.register_button);

        //Database References : storage
        image_storage = FirebaseStorage.getInstance().getReference().child("Profile_images");

        //Database References : realtime database
        NicknameRef = FirebaseDatabase.getInstance().getReference().child("Nicknames");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        register_listener();
        image_listener();
        editext_listener(RegisterActivity.this,nick_editext);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null){
            Uri image_uri = data.getData();

            CropImage.activity(image_uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK){
                user_image = Objects.requireNonNull(result).getUri();
                user_img.setPadding(4,4,4,4);
                Picasso.get()
                        .load(user_image)
                        .transform(new CropCircleTransformation())
                        .error(R.drawable.unknown_user)
                        .into(user_img);
                user_img.setBackground(getResources().getDrawable(R.drawable.round_images_background));
                immagine_messa = true;
            }
        }

    }

    //se premo il pulsante per tornare indietro, esce dall'app.
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }


    private void image_listener(){
        user_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_PICK);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_PICK);
            }
        });
    }

    private void register_listener(){
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check_and_upload_data(RegisterActivity.this,nick_editext);
            }
        });
    }

    void check_and_upload_data(final Context context, EditText nick_editext) {

        if (!nick_check(context,nick_editext)) {
            Toast.makeText(context, "Inserisci un nickname valido", Toast.LENGTH_SHORT).show();
        }
        else if(!privacy_checkbox.isChecked()){
            Toast.makeText(context, "Perfavore accetta i nostri termini di Privacy", Toast.LENGTH_SHORT).show();
        }
        else{
            //Controllo connessione
            if (check_connection()) {
                if (!immagine_messa){
                    user_image = (new Uri.Builder())
                            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                            .authority(getResources().getResourcePackageName(R.drawable.unknown_user))
                            .appendPath(getResources().getResourceTypeName(R.drawable.unknown_user))
                            .appendPath(getResources().getResourceEntryName(R.drawable.unknown_user))
                            .build();

                }
                metto_immagine_in_database(context,user_image,"registrazione",nick_editext.getText().toString());
            } else {
                //Eccezione se l'utente perde la connessione durante la fase di registrazione
                Toast.makeText(context, "Connessione persa o inesistente", Toast.LENGTH_SHORT).show();
            }
        }
    }











}