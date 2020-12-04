package sanchez.miguel.alfonso.simul;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.Locale;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class LobbyPartecipanteActivity extends BaseActivity implements LocationListener, SensorEventListener {

    String creatore_lobby;
    RecyclerView persone_lobby;

    Dialog stato_overlay;
    //Variabili per accelerometro e GPS
    SensorManager sensorManager;
    Sensor sensor;

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


        initialize_accelerometer_and_gps();

    }


    private void initialize_accelerometer_and_gps(){

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(LobbyPartecipanteActivity.this,sensor,sensorManager.SENSOR_DELAY_NORMAL);
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
        Toast.makeText(this, "Waiting for GPS connection...", Toast.LENGTH_SHORT).show();
    }

    private void updateSpeed(CLocation location){
        float nCurrentSpeed = 0;
        if(location != null){
            location.setbUseMetricUnits(this.useMetricUnits());
            nCurrentSpeed = location.getSpeed();
        }
        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.ITALIAN, "%5.1f", nCurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        strCurrentSpeed = strCurrentSpeed.replace(" ", "0");
        if(this.useMetricUnits()){

            //todo aggiornare dati nel database
            String current_speed = strCurrentSpeed;

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

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}