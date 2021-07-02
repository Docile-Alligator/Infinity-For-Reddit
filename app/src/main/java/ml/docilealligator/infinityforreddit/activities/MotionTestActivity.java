package ml.docilealligator.infinityforreddit.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

import ml.docilealligator.infinityforreddit.R;

public class MotionTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motion_test);

        MotionLayout motionLayout = findViewById(R.id.motion_layout);
        motionLayout.addTransitionListener(new MotionLayout.TransitionListener() {
            @Override
            public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {
                Log.i("asdfasdf", "start " + (i == R.id.start) + " " + (i1 == R.id.end));
            }

            @Override
            public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) {
                Log.i("asdfasdf", "start " + (i == R.id.start) + " " + (i1 == R.id.end) + " " + v);
            }

            @Override
            public void onTransitionCompleted(MotionLayout motionLayout, int i) {
                Log.i("asdfasdf", "complete " + (i == R.id.start));
            }

            @Override
            public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {
                Log.i("asdfasdf", "trigger " + (i == R.id.start) + " " + v + " " + b);
            }
        });
    }
}