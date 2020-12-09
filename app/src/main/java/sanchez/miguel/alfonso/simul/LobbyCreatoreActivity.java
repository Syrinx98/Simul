package sanchez.miguel.alfonso.simul;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
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

import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.Locale;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class LobbyCreatoreActivity extends BaseActivity implements LocationListener, SensorEventListener {

    String room_id;

    TextView creator_nickname,destination_chosen,room_id_textview,velocita_utente_textview;
    ImageView creator_img;

    RecyclerView persone_lobby;

    Dialog stato_overlay;

    //variabili FAB
    FloatingActionButton segnala_stato_btn;
    ExtendedFloatingActionButton partito_btn, arrivato_btn, pausa_rifornimenti_btn, traffico_btn, problemi_auto_bnt, emergenza_btn;
    Boolean cliccato = false;
    Animation rotate_open, rotate_close, from_bottom, to_bottom;

    //Variabili per accelerometro e GPS

    SensorManager sensorManager;
    Sensor sensor;

    private double nCurrentSpeed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        room_id = prefs.getString("room_id","00000000");

        //Bindings
        //(cardview)
        creator_img = findViewById(R.id.creatore_immagine);
        velocita_utente_textview = findViewById(R.id.velocita_utente_textview);
        creator_nickname = findViewById(R.id.creator_name);

        //Resto bindings
        room_id_textview = findViewById(R.id.chosen_id);
        destination_chosen = findViewById(R.id.chosen_destination);
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
        room_id_textview.setText(room_id);
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


        initialize_accelerometer_and_gps();

    }


    private void initialize_accelerometer_and_gps(){

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(LobbyCreatoreActivity.this,sensor,sensorManager.SENSOR_DELAY_NORMAL);
        // ---

        // check for gps permission
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        }
        else{
            // start the program if the permission is granted
            doStuff();
        }

        this.updateSpeed(null);

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


                if (!model.getParticipant_name().equals(nickname)){
                    Picasso.get()
                            .load(model.getParticipant_image())
                            .transform(new CropCircleTransformation())
                            .error(R.drawable.unknown_user)
                            .into(holder.immagine);

                    holder.nome.setText(model.getParticipant_name());

                    if(model.getParticipant_state().equals("0") ){
                        holder.immagine.setBackground(getResources().getDrawable(R.drawable.immagine_profilo_ring_verde));
                    }
                    else {
                        holder.immagine.setBackground(getResources().getDrawable(R.drawable.immagine_profilo_ring_rosso));
                        Toast.makeText(LobbyCreatoreActivity.this,"Attenzione!\n" + model.getParticipant_name() + "potrebbe essere in pericolo!",Toast.LENGTH_LONG).show();
                    }


                }
                else{

                    RecyclerView.LayoutParams param = (RecyclerView.LayoutParams)holder.itemView.getLayoutParams();
                    param.height = 0;
                    param.width = LinearLayout.LayoutParams.MATCH_PARENT;
                    holder.itemView.setVisibility(View.VISIBLE);

                    if(model.getParticipant_state().equals("0") ){
                        creator_img.setBackground(getResources().getDrawable(R.drawable.immagine_profilo_ring_verde));
                    }
                    else {
                        creator_img.setBackground(getResources().getDrawable(R.drawable.immagine_profilo_ring_rosso));
                        Toast.makeText(LobbyCreatoreActivity.this,"Intervengo subito!",Toast.LENGTH_LONG).show();
                    }
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


    public class LobbyHolder extends RecyclerView.ViewHolder {

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

    private void showPopup(String nome, ImageView immagine){

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


    //overrides delle interfacce implementate
    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(location != null){
            CLocation myLocation = new CLocation(location, this.useMetricUnits());
            this.updateSpeed(myLocation);
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
    private void doStuff(){
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if(locationManager != null){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        Toast.makeText(this, "Accedo al GPS...", Toast.LENGTH_SHORT).show();
    }






    private void updateSpeed(CLocation location){

        if(location != null){
            location.setbUseMetricUnits(this.useMetricUnits());
            nCurrentSpeed = location.getSpeed();
        }
        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.ITALIAN, "%5.1f", nCurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        strCurrentSpeed = strCurrentSpeed.replace(" ", "0");
        if(this.useMetricUnits()){

            prendi_user_id_attuale();
            update_current_speed_in_database(current_user_id,current_user_id,strCurrentSpeed);
            velocita_utente_textview.setText(strCurrentSpeed + "km/h");
        }
        else{

            //caso miglia orarie, ma buttiamo su solo miles per hour e la visualizzazione avverrà a seconda di che metric units starò usando
            //todo guardare questa cosa
        }
    }

    private boolean useMetricUnits(){
        return /*sw_metric.isChecked();*/ true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1000){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                doStuff();
            }
            else{
                finish();
            }
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        DecimalFormat df2 = new DecimalFormat("#.##");
        double normalizzato = Math.sqrt(Math.pow(event.values[0],2)+Math.pow(event.values[1],2)+Math.pow(event.values[2],2));
        //todo check su normalizzato, teoricamente a valori normali è 9.81, valutare quante accelerazioni di gravità di servono
        //todo per il trigger

        if(check_accelerometer_anomalies(normalizzato)){

            if (check_velocity_anomalies(nCurrentSpeed)){
                prendi_user_id_attuale();
                pop_alarm_possible_sinister(LobbyCreatoreActivity.this,current_user_id,current_user_id);
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}