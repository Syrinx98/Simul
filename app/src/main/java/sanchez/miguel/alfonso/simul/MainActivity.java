package sanchez.miguel.alfonso.simul;

import android.os.Bundle;

public class MainActivity extends App {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}