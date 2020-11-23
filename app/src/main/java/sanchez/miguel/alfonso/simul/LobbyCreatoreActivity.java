package sanchez.miguel.alfonso.simul;


import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class LobbyCreatoreActivity extends BaseActivity {

    TextView creator_nickname,destination_chosen;
    ImageView creator_img;

    RecyclerView persone_lobby;
    List<String> nomi;
    List<Integer> immagini;
    Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        //Bindings
        creator_nickname = findViewById(R.id.creator_name);
        destination_chosen = findViewById(R.id.chosen_destination);
        creator_img = findViewById(R.id.creatore_immagine);

        //Settaggio textview
        creator_nickname.setText(prefs.getString("nickname","Unknown"));
        destination_chosen.setText(prefs.getString("destinazione_room","Nessuna"));

        Picasso.get()
                .load(prefs.getString("immagine","-"))
                .transform(new CropCircleTransformation())
                .placeholder(R.drawable.round_images_placeholder)
                .error(R.drawable.unknown_user)
                .into(creator_img);

        //Bindings per recycler grid view
        persone_lobby = findViewById(R.id.lista_utenti_lobby);
        nomi = new ArrayList<>();
        immagini = new ArrayList<>();

        adapter = new Adapter(this, nomi, immagini);

        int colonne = 2;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, colonne, GridLayoutManager.VERTICAL, false);
        persone_lobby.setLayoutManager(gridLayoutManager);
        persone_lobby.setAdapter(adapter);

        aggiungiPersone();

    }

    public void aggiungiPersone(){
        //aggiungere dinamicamente le persone nelle Liste
    }
}