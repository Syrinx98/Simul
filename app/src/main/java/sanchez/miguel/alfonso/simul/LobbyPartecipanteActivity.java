package sanchez.miguel.alfonso.simul;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class LobbyPartecipanteActivity extends BaseActivity {

    String creatore_lobby;
    RecyclerView persone_lobby;

    Dialog stato_overlay;

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

        final FirebaseRecyclerAdapter<LobbyQuery, LobbyPartecipanteActivity.LobbyHolderPartecipante> adapter = new FirebaseRecyclerAdapter<LobbyQuery, LobbyPartecipanteActivity.LobbyHolderPartecipante>(options) {

            @Override
            protected void onBindViewHolder(@NonNull LobbyPartecipanteActivity.LobbyHolderPartecipante holder, int position, @NonNull LobbyQuery model) {

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
            public LobbyPartecipanteActivity.LobbyHolderPartecipante onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lobby_item_grid, parent, false);
                return new LobbyPartecipanteActivity.LobbyHolderPartecipante(view);
            }
        };

        persone_lobby.setAdapter(adapter);
        adapter.startListening();
    }

    public class LobbyHolderPartecipante extends RecyclerView.ViewHolder {

        final TextView nome;
        final ImageView immagine;

        public LobbyHolderPartecipante(@NonNull View itemView) {

            super(itemView);
            nome = itemView.findViewById(R.id.lobby_grid_item_nick);
            immagine = itemView.findViewById(R.id.lobby_grid_item_img);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopup(nome.getText().toString(),immagine);
                }
            });
        }
    }

    private void showPopup(String nome, ImageView immagine){

        stato_overlay = new Dialog(this);

        stato_overlay.setContentView(R.layout.card_stato_altrui_lobby);

        TextView nome_card = stato_overlay.findViewById(R.id.partecipante_name);
        nome_card.setText(nome);

        ImageView immagine_card = stato_overlay.findViewById(R.id.partecipante_immagine);
        immagine_card.setImageDrawable(immagine.getDrawable());

        stato_overlay.show();
        stato_overlay.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        View v = stato_overlay.getWindow().getDecorView();
        v.setBackgroundResource(android.R.color.transparent);
    }
}