package sanchez.miguel.alfonso.simul;

import android.content.Intent;
import androidx.fragment.app.Fragment;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Objects;
import static sanchez.miguel.alfonso.simul.BaseActivity.User;


abstract class BaseFragment extends Fragment {
    public static FirebaseAuth mAuth;
    protected GoogleSignInOptions gso;
    //Intent del sign in (come avviare un activity)
    protected int RC_SIGN_IN;
    protected Intent signInIntent;
    public static String current_user_id;


    protected void prendi_user_id_attuale() {
        mAuth = FirebaseAuth.getInstance();
        User = mAuth.getCurrentUser();
        current_user_id = Objects.requireNonNull(Objects.requireNonNull(User).getUid());
    }


}
