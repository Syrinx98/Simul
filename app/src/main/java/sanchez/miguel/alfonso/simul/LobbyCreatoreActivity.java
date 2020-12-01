package sanchez.miguel.alfonso.simul;

import android.app.Dialog;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class LobbyCreatoreActivity extends BaseActivity {

    TextView creator_nickname,destination_chosen;
    ImageView creator_img;

    RecyclerView persone_lobby;

    static Dialog stato_overlay;

    //variabili FAB
    FloatingActionButton segnala_stato_btn;
    ExtendedFloatingActionButton partito_btn, arrivato_btn, pausa_rifornimenti_btn, traffico_btn, problemi_auto_bnt, emergenza_btn;
    Boolean cliccato = false;
    Animation rotate_open, rotate_close, from_bottom, to_bottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        //Bindings
        creator_nickname = findViewById(R.id.creator_name);
        destination_chosen = findViewById(R.id.chosen_destination);
        creator_img = findViewById(R.id.creatore_immagine);
        persone_lobby = findViewById(R.id.lista_utenti_lobby);

        segnala_stato_btn = findViewById(R.id.segnala_stato_btn);
        partito_btn = findViewById(R.id.partito_btn);
        arrivato_btn = findViewById(R.id.arrivato_btn);
        pausa_rifornimenti_btn = findViewById(R.id.pausa_rifornimenti_btn);
        traffico_btn = findViewById(R.id.traffico_btn);
        problemi_auto_bnt  = findViewById(R.id.problemi_auto_btn);
        emergenza_btn = findViewById(R.id.emergenza_btn);

        rotate_open = AnimationUtils.loadAnimation(this,R.anim.rotate_open_anim);
        rotate_close = AnimationUtils.loadAnimation(this,R.anim.rotate_close_anim);
        from_bottom = AnimationUtils.loadAnimation(this,R.anim.from_bottom_anim);
        to_bottom = AnimationUtils.loadAnimation(this,R.anim.to_bottom_anim);

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

        stato_overlay = new Dialog(this);

        //settaggio listeners bottoni FAB segnala stato
        segnala_stato_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSegnalaStatoClicked();
            }
        });
        partito_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Partito",Toast.LENGTH_SHORT).show();
            }
        });
        arrivato_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Arrivato",Toast.LENGTH_SHORT).show();
            }
        });
        pausa_rifornimenti_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Pausa rifornimenti",Toast.LENGTH_SHORT).show();
            }
        });
        traffico_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Problemi traffico",Toast.LENGTH_SHORT).show();
            }
        });
        problemi_auto_bnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Problemi auto",Toast.LENGTH_SHORT).show();
            }
        });
        emergenza_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Emergenza !!!",Toast.LENGTH_SHORT).show();
            }
        });

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

                if(model.getParticipant_state().equals("0")){
                    holder.immagine.setBackground(getResources().getDrawable(R.drawable.immagine_profilo_ring_verde));
                }
                else{
                    holder.immagine.setBackground(getResources().getDrawable(R.drawable.immagine_profilo_ring_rosso));
                }
                //TO-DO: aggiungere ramo per cerchio giallo (presente e pronto all'uso), verificare che il database lo permetta prima

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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopup(nome.getText().toString(),immagine);
                }
            });
        }
    }

    static private void showPopup(String nome, ImageView immagine){

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


    private void onSegnalaStatoClicked(){
        setVisibilityStati(cliccato);
        setAnimationStati(cliccato);
        cliccato = !cliccato;
    }

    private void setVisibilityStati(Boolean cliccato){
        if(!cliccato){
            partito_btn.setVisibility(View.VISIBLE);
            arrivato_btn.setVisibility(View.VISIBLE);
            pausa_rifornimenti_btn.setVisibility(View.VISIBLE);
            traffico_btn.setVisibility(View.VISIBLE);
            problemi_auto_bnt.setVisibility(View.VISIBLE);
            emergenza_btn.setVisibility(View.VISIBLE);
        }
        else{
            partito_btn.setVisibility(View.INVISIBLE);
            arrivato_btn.setVisibility(View.INVISIBLE);
            pausa_rifornimenti_btn.setVisibility(View.INVISIBLE);
            traffico_btn.setVisibility(View.INVISIBLE);
            problemi_auto_bnt.setVisibility(View.INVISIBLE);
            emergenza_btn.setVisibility(View.INVISIBLE);
        }
    }

    private void setAnimationStati(Boolean cliccato){
        if(!cliccato){
            segnala_stato_btn.startAnimation(rotate_open);
            partito_btn.startAnimation(from_bottom);
            arrivato_btn.startAnimation(from_bottom);
            pausa_rifornimenti_btn.startAnimation(from_bottom);
            traffico_btn.startAnimation(from_bottom);
            problemi_auto_bnt.startAnimation(from_bottom);
            emergenza_btn.startAnimation(from_bottom);
        }
        else{
            segnala_stato_btn.startAnimation(rotate_close);
            partito_btn.startAnimation(to_bottom);
            arrivato_btn.startAnimation(to_bottom);
            pausa_rifornimenti_btn.startAnimation(to_bottom);
            traffico_btn.startAnimation(to_bottom);
            problemi_auto_bnt.startAnimation(to_bottom);
            emergenza_btn.startAnimation(to_bottom);
        }
    }

}