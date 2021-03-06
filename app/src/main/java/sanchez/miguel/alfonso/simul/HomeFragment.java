package sanchez.miguel.alfonso.simul;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;

import static sanchez.miguel.alfonso.simul.BaseActivity.nickname;
import static sanchez.miguel.alfonso.simul.BaseActivity.prefs;

public class HomeFragment extends Fragment {

    private TextView nome;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        nome = v.findViewById(R.id.home_text_name);
        nickname = prefs.getString("nickname","");
        nome.setText(nickname);

        v.findViewById(R.id.goto_creazionelobby).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), CreazioneLobbyActivity.class));

            }
        });
        v.findViewById(R.id.goto_partecipalobby).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), CercaLobbyActivity.class));

            }
        });

        return v;
    }

}