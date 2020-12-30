package sanchez.miguel.alfonso.simul;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.Locale;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class LobbyPartecipanteActivity extends BaseActivity implements LocationListener, SensorEventListener {

    TextView creator_nickname, destination_chosen, velocita_utente_textview,room_id_textview;
    ImageView creator_img;

    String creatore_lobby,room_id;
    RecyclerView persone_lobby;

    //variabili Stati
    FloatingActionButton segnala_stato_btn;
    MaterialButton partito_btn, arrivato_btn, pausa_rifornimenti_btn, traffico_btn, problemi_auto_bnt, emergenza_btn;
    Boolean cliccato = false;
    Animation rotate_open, rotate_close, from_bottom, to_bottom;
    MaterialCardView cardview_bottoni_stati;


    //Variabili per accelerometro e GPS
    SensorManager sensorManager;
    Sensor sensor;

    private double nCurrentSpeed = 0;

    LocationManager locationManager;
    Location locationGPS;
    Location locationNet;
    Location currentBestLocation;

    int easter = 0;

    ImageView icona_stato;


    private boolean utente_partito = true;
    private boolean utente_fermo = false;
    private CountDownTimer a;
    private Dialog warning_dialog;
    private boolean started_timer = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);


        creatore_lobby = prefs.getString("creatore_lobby", "nessuno");
        room_id = prefs.getString("room_id","00000000");

        //Bindings
        //(cardview)
        creator_img = findViewById(R.id.creatore_immagine);
        velocita_utente_textview = findViewById(R.id.velocita_utente_textview);
        creator_nickname = findViewById(R.id.creator_name);


        destination_chosen = findViewById(R.id.chosen_destination);
        persone_lobby = findViewById(R.id.lista_utenti_lobby);
        room_id_textview = findViewById(R.id.chosen_id);

        cardview_bottoni_stati = findViewById(R.id.cardview_bottoni_stati);
        segnala_stato_btn = findViewById(R.id.segnala_stato_btn);
        partito_btn = findViewById(R.id.partito_btn);
        arrivato_btn = findViewById(R.id.arrivato_btn);
        pausa_rifornimenti_btn = findViewById(R.id.pausa_rifornimenti_btn);
        traffico_btn = findViewById(R.id.traffico_btn);
        problemi_auto_bnt = findViewById(R.id.problemi_auto_btn);
        emergenza_btn = findViewById(R.id.emergenza_btn);


        rotate_open = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        rotate_close = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        from_bottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        to_bottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);

        //Settaggio textview
        creator_nickname.setText(prefs.getString("nickname", "Unknown"));
        destination_chosen.setText(prefs.getString("destinazione_room", "Nessuna"));
        room_id_textview.setText(room_id);

        Picasso.get()
                .load(prefs.getString("immagine", "-"))
                .transform(new CropCircleTransformation())
                .placeholder(R.drawable.unknown_user)
                .error(R.drawable.unknown_user)
                .into(creator_img);

        int colonne = 2;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, colonne, GridLayoutManager.VERTICAL, false);
        persone_lobby.setLayoutManager(gridLayoutManager);


        states_button_listener();


        initialize_accelerometer_and_gps();

        //Richiedo permessi per i messaggi
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            currentBestLocation = getLastBestLocation();
        }

        sms_permission();

    }

    private void sms_permission(){
        if (ContextCompat.checkSelfPermission(LobbyPartecipanteActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[] { Manifest.permission.SEND_SMS}, 1);
        }
    }

    private void states_button_listener(){


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
                send_state_to_room(creatore_lobby,current_user_id,PARTITO_STATE);
                onSegnalaStatoClicked();
            }
        });
        arrivato_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_state_to_room(creatore_lobby,current_user_id,ARRIVATO_STATE);
                onSegnalaStatoClicked();
            }
        });
        pausa_rifornimenti_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                easter++;
                if (easter == 10){
                    Toast.makeText(getApplicationContext(),"Pausa mmerda",Toast.LENGTH_SHORT).show();
                }
                send_state_to_room(creatore_lobby,current_user_id,RIFORNIMENTI_STATE);
                onSegnalaStatoClicked();
            }
        });
        traffico_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_state_to_room(creatore_lobby,current_user_id,TRAFFICO_STATE);
                onSegnalaStatoClicked();
            }
        });
        problemi_auto_bnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_state_to_room(creatore_lobby,current_user_id,PROBLEMI_AUTO_STATE);
                onSegnalaStatoClicked();
            }
        });
        emergenza_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_state_to_room(creatore_lobby,current_user_id,EMERGENZA_STATE);
                onSegnalaStatoClicked();
            }
        });
    }


    private void initialize_accelerometer_and_gps() {

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(LobbyPartecipanteActivity.this, sensor, sensorManager.SENSOR_DELAY_NORMAL);

        // check for gps permission
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            // start the program if the permission is granted
            doStuff();
        }

        this.updateSpeed(null);


    }

    private void onSegnalaStatoClicked(){

        //per side-effect
        states_assemble();
        cliccato = !cliccato;
    }

    private void states_assemble(){
        if(!cliccato){
            segnala_stato_btn.startAnimation(rotate_open);
            cardview_bottoni_stati.startAnimation(from_bottom);
            from_bottom.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    cardview_bottoni_stati.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    states_appear();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });


        }
        else{
            segnala_stato_btn.startAnimation(rotate_close);
            cardview_bottoni_stati.startAnimation(to_bottom);
            to_bottom.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    cardview_bottoni_stati.setVisibility(View.GONE);
                    states_disappear();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

        }
    }

    private void states_appear(){
        partito_btn.setVisibility(View.VISIBLE);
        partito_btn.setEnabled(true);
        arrivato_btn.setVisibility(View.VISIBLE);
        arrivato_btn.setEnabled(true);
        pausa_rifornimenti_btn.setVisibility(View.VISIBLE);
        pausa_rifornimenti_btn.setEnabled(true);
        traffico_btn.setVisibility(View.VISIBLE);
        traffico_btn.setEnabled(true);
        problemi_auto_bnt.setVisibility(View.VISIBLE);
        problemi_auto_bnt.setEnabled(true);
        emergenza_btn.setVisibility(View.VISIBLE);
        emergenza_btn.setEnabled(true);
    }

    private void states_disappear(){
        cardview_bottoni_stati.setVisibility(View.GONE);
        partito_btn.setVisibility(View.GONE);
        arrivato_btn.setVisibility(View.GONE);
        pausa_rifornimenti_btn.setVisibility(View.GONE);
        traffico_btn.setVisibility(View.GONE);
        problemi_auto_bnt.setVisibility(View.GONE);
        emergenza_btn.setVisibility(View.GONE);
        partito_btn.setEnabled(false);
        arrivato_btn.setEnabled(false);
        pausa_rifornimenti_btn.setEnabled(false);
        traffico_btn.setEnabled(false);
        problemi_auto_bnt.setEnabled(false);
        emergenza_btn.setEnabled(false);
    }


    @Override
    protected void onStart() {
        super.onStart();

        icona_stato = findViewById(R.id.icona_stato_lobby_creatore);
        DrawableCompat.setTint(
                DrawableCompat.wrap(icona_stato.getDrawable()),
                ContextCompat.getColor(getApplicationContext(), R.color.secondaryWhite)
        );

        Query query = RoomsRef.child(creatore_lobby).child("partecipanti").orderByChild("participant_name");

        FirebaseRecyclerOptions<LobbyQuery> options = new FirebaseRecyclerOptions.Builder<LobbyQuery>()
                .setQuery(query, LobbyQuery.class)
                .build();

        final FirebaseRecyclerAdapter<LobbyQuery, LobbyPartecipanteActivity.LobbyHolderPartecipante> adapter = new FirebaseRecyclerAdapter<LobbyQuery, LobbyPartecipanteActivity.LobbyHolderPartecipante>(options) {

            @Override
            protected void onBindViewHolder(@NonNull LobbyPartecipanteActivity.LobbyHolderPartecipante holder, int position, @NonNull LobbyQuery model) {

                if (model.getParticipant_name().equals(nickname)){

                    switch (model.getParticipant_state()){
                        case PARTITO_STATE:
                            creator_img.setBackground(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.immagine_profilo_ring_verde));
                            icona_stato.setImageDrawable(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.stati_ic_in_viaggio_dimensionabile));
                            break;

                        case ARRIVATO_STATE:
                            creator_img.setBackground(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.immagine_profilo_ring_verde));
                            icona_stato.setImageDrawable(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.stati_ic_arrivato_dimensionabile));
                            break;
                        case NON_PARTITO_STATE:
                            creator_img.setBackground(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.immagine_profilo_ring_giallo));
                            icona_stato.setImageDrawable(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.stati_ic_non_partito_dimensionabile));
                            break;
                        case RIFORNIMENTI_STATE:
                            creator_img.setBackground(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.immagine_profilo_ring_giallo));
                            icona_stato.setImageDrawable(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.stati_ic_rifornimenti_dimensionabile));
                            break;
                        case TRAFFICO_STATE:
                            creator_img.setBackground(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.immagine_profilo_ring_giallo));
                            icona_stato.setImageDrawable(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.stati_ic_traffico_dimensionabile));
                            break;
                        case PROBLEMI_AUTO_STATE:
                            creator_img.setBackground(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.immagine_profilo_ring_giallo));
                            icona_stato.setImageDrawable(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.stati_ic_problemi_auto_dimensionabile));
                            break;
                        case EMERGENZA_STATE:
                            creator_img.setBackground(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.immagine_profilo_ring_rosso));
                            icona_stato.setImageDrawable(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.stati_ic_emergenza_dimensionabile));
                            Toast.makeText(LobbyPartecipanteActivity.this,"Mando subito un allarme agli altri membri",Toast.LENGTH_LONG).show();
                            break;
                        case EMERGENZA_DETECTED:
                            creator_img.setBackground(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.immagine_profilo_ring_rosso));
                            icona_stato.setImageDrawable(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.stati_ic_incidente_dimensionabile));
                            Toast.makeText(LobbyPartecipanteActivity.this,"Attenzione!\n" + model.getParticipant_name() + "potrebbe essere in pericolo!",Toast.LENGTH_LONG).show();
                            break;
                    }

                    String vel = model.getParticipant_speed() + " km/h";

                    velocita_utente_textview.setText(vel);


                }
                Picasso.get()
                        .load(model.getParticipant_image())
                        .transform(new CropCircleTransformation())
                        .error(R.drawable.unknown_user)
                        .into(holder.immagine);

                holder.nome.setText(model.getParticipant_name());


                String vel = model.getParticipant_speed() + " km/h";
                holder.velocita.setText(vel);

                switch (model.getParticipant_state()){
                    case PARTITO_STATE:
                        holder.immagine.setBackground(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.immagine_profilo_ring_verde));
                        holder.stato_code.setText(PARTITO_STATE);
                        break;
                    case ARRIVATO_STATE:
                        holder.immagine.setBackground(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.immagine_profilo_ring_verde));
                        holder.stato_code.setText(ARRIVATO_STATE);
                        break;

                    case NON_PARTITO_STATE:
                        holder.immagine.setBackground(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.immagine_profilo_ring_giallo));
                        holder.stato_code.setText(NON_PARTITO_STATE);
                        break;
                    case RIFORNIMENTI_STATE:
                        holder.immagine.setBackground(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.immagine_profilo_ring_giallo));
                        holder.stato_code.setText(RIFORNIMENTI_STATE);
                        break;
                    case TRAFFICO_STATE:
                        holder.immagine.setBackground(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.immagine_profilo_ring_giallo));
                        holder.stato_code.setText(TRAFFICO_STATE);
                        break;
                    case PROBLEMI_AUTO_STATE:
                        holder.immagine.setBackground(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.immagine_profilo_ring_giallo));
                        holder.stato_code.setText(PROBLEMI_AUTO_STATE);
                        break;

                    case EMERGENZA_STATE:
                        holder.immagine.setBackground(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.immagine_profilo_ring_rosso));
                        Toast.makeText(LobbyPartecipanteActivity.this,"Emergenza lanciata agli altri membri della stanza",Toast.LENGTH_LONG).show();
                        holder.stato_code.setText(EMERGENZA_STATE);
                        break;

                    case EMERGENZA_DETECTED:
                        holder.immagine.setBackground(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.immagine_profilo_ring_rosso));
                        Toast.makeText(LobbyPartecipanteActivity.this,"Attenzione! \n" + model.getParticipant_name() + "potrebbe essere in pericolo!",Toast.LENGTH_LONG).show();
                        holder.stato_code.setText(EMERGENZA_DETECTED);
                        break;
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
        final TextView velocita;
        final TextView stato_code;

        public LobbyHolderPartecipante(@NonNull View itemView) {

            super(itemView);
            nome = itemView.findViewById(R.id.lobby_grid_item_nick);
            immagine = itemView.findViewById(R.id.lobby_grid_item_img);
            velocita = itemView.findViewById(R.id.velocita_attuale);
            stato_code = itemView.findViewById(R.id.codice_stato_attuale);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopup(nome.getText().toString(), immagine,immagine.getBackground(),velocita.getText().toString(),stato_code.getText().toString());
                }
            });
        }
    }

    private void showPopup(String nome, ImageView immagine, Drawable background,String velocita_attuale,String status_code) {

        Dialog stato_overlay = new Dialog(this);

        stato_overlay.setContentView(R.layout.card_stato_altrui_lobby);

        TextView temp_nome_card = stato_overlay.findViewById(R.id.partecipante_name);
        TextView velocita = stato_overlay.findViewById(R.id.velocita_lobby_partecipante);
        ImageView temp_immagine_card = stato_overlay.findViewById(R.id.partecipante_immagine);
        ImageView stato_lobby_partecipante = stato_overlay.findViewById(R.id.icona_stato_lobby_partecipante);

        switch (status_code) {
            case PARTITO_STATE:
                stato_lobby_partecipante.setImageDrawable(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.stati_ic_in_viaggio_dimensionabile));
                break;
            case ARRIVATO_STATE:
                stato_lobby_partecipante.setImageDrawable(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.stati_ic_arrivato_dimensionabile));
                break;
            case NON_PARTITO_STATE:
                stato_lobby_partecipante.setImageDrawable(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.stati_ic_non_partito_dimensionabile));
                break;
            case RIFORNIMENTI_STATE:
                stato_lobby_partecipante.setImageDrawable(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.stati_ic_rifornimenti_dimensionabile));
                break;
            case TRAFFICO_STATE:
                stato_lobby_partecipante.setImageDrawable(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.stati_ic_traffico_dimensionabile));
                break;
            case PROBLEMI_AUTO_STATE:
                stato_lobby_partecipante.setImageDrawable(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.stati_ic_problemi_auto_dimensionabile));
                break;
            case EMERGENZA_STATE:
                stato_lobby_partecipante.setImageDrawable(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.stati_ic_emergenza_dimensionabile));
                break;
            case EMERGENZA_DETECTED:
                stato_lobby_partecipante.setImageDrawable(ContextCompat.getDrawable(LobbyPartecipanteActivity.this,R.drawable.stati_ic_incidente_dimensionabile));
                break;
        }

        temp_nome_card.setText(nome);
        temp_immagine_card.setImageDrawable(immagine.getDrawable());
        temp_immagine_card.setBackground(background);
        velocita.setText(velocita_attuale);

        stato_overlay.show();
        stato_overlay.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        View v = stato_overlay.getWindow().getDecorView();
        v.setBackgroundResource(android.R.color.transparent);
    }


    //overrides delle interfacce implementate
    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location != null) {
            CLocation myLocation = new CLocation(location, this.useMetricUnits());
            this.updateSpeed(myLocation);

            currentBestLocation = location;

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @SuppressLint("MissingPermission")
    private void doStuff() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        Toast.makeText(this, "Accedo al GPS...", Toast.LENGTH_SHORT).show();
    }

    private void updateSpeed(CLocation location) {

        if (location != null) {
            location.setbUseMetricUnits(this.useMetricUnits());
            nCurrentSpeed = location.getSpeed();
            if (nCurrentSpeed != 0.){
                utente_partito = true;
                utente_fermo = false;
                started_timer = false;
            }
            else{
                utente_fermo = true;
            }


            if (utente_fermo && !started_timer){
                started_timer = true;

                a = new CountDownTimer(120000,60000) {
                    @Override
                    public void onTick(long millisUntilFinished) {}

                    @Override
                    public void onFinish() {
                        pop_stay_dialog();
                    }
                };

                a.start();
            }
        }
        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.ITALIAN, "%5.1f", nCurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        strCurrentSpeed = strCurrentSpeed.replace(" ", "0");
        if (this.useMetricUnits()) {

            prendi_user_id_attuale();
            update_current_speed_in_database(creatore_lobby, current_user_id, strCurrentSpeed);

            velocita_utente_textview.setText(strCurrentSpeed);
        } else {

            Log.v("LobbyPartecipanteActivity", "velocità corrente " + strCurrentSpeed);
        }
    }

    private boolean useMetricUnits() {
        return /*sw_metric.isChecked();*/ true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doStuff();
            } else {
                finish();
            }
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        DecimalFormat df2 = new DecimalFormat("#.##");
        double normalizzato = Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));

        Log.v("LobbyPartecipanteActivity", "valori accelerometro " + normalizzato);

        if (check_accelerometer_anomalies(normalizzato)) {

            if (check_velocity_anomalies(nCurrentSpeed)) {
                prendi_user_id_attuale();

                //la location potrebbe essere null
                if (currentBestLocation != null){
                    pop_alarm_possible_sinister(LobbyPartecipanteActivity.this, creatore_lobby, current_user_id,currentBestLocation);
                }
                else{
                    Toast.makeText(LobbyPartecipanteActivity.this,"Il GPS non è stato inizializzato correttamente,attendi qualche secondo o riavvia la stanza\n(controlla di aver attivato il GPS)",Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private Location getLastBestLocation() {
        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }

    private void pop_stay_dialog(){
        //Manda lo stato alla room
        send_state_to_room(creatore_lobby,current_user_id,TRAFFICO_STATE);
        warning_dialog = new Dialog(LobbyPartecipanteActivity.this);
        warning_dialog.setCancelable(true);
        warning_dialog.setContentView(R.layout.popup_controllo_pausa);
        warning_dialog.show();
        warning_dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        final View view = warning_dialog.getWindow().getDecorView();
        view.setBackgroundResource(android.R.color.transparent);

    }


}