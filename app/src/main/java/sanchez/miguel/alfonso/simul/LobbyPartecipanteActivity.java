package sanchez.miguel.alfonso.simul;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class LobbyPartecipanteActivity extends BaseActivity {

    String creatore_lobby;
    RecyclerView persone_lobby;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partecipante_lobby);

        creatore_lobby = prefs.getString("creatore_lobby","nessuno");

        //Bindings
        persone_lobby = findViewById(R.id.recyclerview_lobby_partecipante);

        int colonne = 2;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, colonne, GridLayoutManager.VERTICAL, false);
        persone_lobby.setLayoutManager(gridLayoutManager);

    }


    @Override
    protected void onStart() {
        super.onStart();

        Query query = RoomsRef.child(creatore_lobby).child("partecipanti").orderByChild("participant_name");

        FirebaseRecyclerOptions<LobbyQuery> options = new FirebaseRecyclerOptions.Builder<LobbyQuery>()
                .setQuery(query, LobbyQuery.class)
                .build();

        final FirebaseRecyclerAdapter<LobbyQuery, LobbyCreatoreActivity.LobbyHolder> adapter = new FirebaseRecyclerAdapter<LobbyQuery, LobbyCreatoreActivity.LobbyHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull LobbyCreatoreActivity.LobbyHolder holder, int position, @NonNull LobbyQuery model) {
                Picasso.get()
                        .load(model.getParticipant_image())
                        .transform(new CropCircleTransformation())
                        .error(R.drawable.unknown_user)
                        .into(holder.immagine);

                holder.nome.setText(model.getParticipant_name());

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
                return new LobbyCreatoreActivity.LobbyHolder(view);
            }
        };

        persone_lobby.setAdapter(adapter);
        adapter.startListening();
    }
}