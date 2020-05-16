package com.beacat.calendar.ladycal;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ViewFlipper;

/**
 * Activity that shows a one time tutorial with the base functionality of the app
 */

public class TutorialActivity extends AppCompatActivity implements View.OnClickListener {

    private ViewFlipper flipper;
    Button btn;
    FrameLayout f1;
    FrameLayout f2;
    FrameLayout f3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tutorial);

        flipper = (ViewFlipper)findViewById(R.id.view_flipper);
        btn = (Button)findViewById(R.id.tutorial_btn);
        btn.setOnClickListener(this);

        f1 = (FrameLayout)findViewById(R.id.b1_frame);
        f2 = (FrameLayout)findViewById(R.id.b2_frame);
        f3 = (FrameLayout)findViewById(R.id.b3_frame);
        f2.setVisibility(View.VISIBLE); // start period image
    }

    @Override
    public void onClick(View v) {
        switch(flipper.getDisplayedChild()){
            case 0:
                f2.setVisibility(View.INVISIBLE);
                flipper.showNext();
                f3.setVisibility(View.VISIBLE); // med image
                break;
            case 1:
                f3.setVisibility(View.INVISIBLE);
                flipper.showNext();
                f1.setVisibility(View.VISIBLE); // refresh image
                btn.setText("DONE");
                break;
            case 2:
                finish();
                break;
        }
    }
}
