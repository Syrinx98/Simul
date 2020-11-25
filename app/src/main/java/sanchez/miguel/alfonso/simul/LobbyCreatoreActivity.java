package sanchez.miguel.alfonso.simul;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class LobbyCreatoreActivity extends BaseActivity {

    TextView creator_nickname,destination_chosen;
    ImageView creator_img;

    RecyclerView persone_lobby;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        //Bindings
        creator_nickname = findViewById(R.id.creator_name);
        destination_chosen = findViewById(R.id.chosen_destination);
        creator_img = findViewById(R.id.creatore_immagine);
        persone_lobby = findViewById(R.id.lista_utenti_lobby);

        //Settaggio textview
        creator_nickname.setText(prefs.getString("nickname","Unknown"));
        destination_chosen.setText(prefs.getString("destinazione_room","Nessuna"));

        Picasso.get()
                .load(prefs.getString("immagine","-"))
                .transform(new CropCircleTransformation())
                .placeholder(R.drawable.round_images_placeholder)
                .error(R.drawable.unknown_user)
                .into(creator_img);

        //RoomsReference
        RoomsRef = FirebaseDatabase.getInstance().getReference().child("Rooms");
        prendi_user_id_attuale();



        int colonne = 2;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, colonne, GridLayoutManager.VERTICAL, false);
        persone_lobby.setLayoutManager(gridLayoutManager);

    }

    @Override
    protected void onStart() {
        super.onStart();

        Query query = RoomsRef.child(current_user_id).child("partecipanti").orderByChild("participant_name");

        FirebaseRecyclerOptions<LobbyQuery> options = new FirebaseRecyclerOptions.Builder<LobbyQuery>()
                .setQuery(query, LobbyQuery.class)
                .build();

        final FirebaseRecyclerAdapter<LobbyQuery, LobbyHolder> adapter = new FirebaseRecyclerAdapter<LobbyQuery, LobbyCreatoreActivity.LobbyHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull LobbyCreatoreActivity.LobbyHolder holder, int position, @NonNull LobbyQuery model) {
                Picasso.get()
                        .load(model.getParticipant_image())
                        .transform(new CropCircleTransformation())
                        .error(R.drawable.unknown_user)
                        .into(holder.immagine);

                holder.nome.setText(model.getParticipant_name());

                //robe per spaziare gli elementi (non so come caspita implementare qua con sto adapter)
                //https://www.youtube.com/watch?v=j6-Huxf1UVA&ab_channel=CodingSararea minuto 10:20 in poi circa

                if(model.getParticipant_state().equals("0")){
                    holder.immagine.setBackground(getResources().getDrawable(R.drawable.immagine_profilo_ring_verde));
                }
                else{
                    holder.immagine.setBackground(getResources().getDrawable(R.drawable.immagine_profilo_ring_rosso));
                }

            }

            @NonNull
            @Override
            public LobbyCreatoreActivity.LobbyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lobby_item_grid, parent, false);
                return new LobbyHolder(view);
            }
        };

        persone_lobby.setAdapter(adapter);
        adapter.startListening();
    }


    public static class LobbyHolder extends RecyclerView.ViewHolder {
        final TextView nome;
        final ImageView immagine;
        public LobbyHolder(@NonNull View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.lobby_grid_item_nick);
            immagine = itemView.findViewById(R.id.lobby_grid_item_img);
        }
    }


}