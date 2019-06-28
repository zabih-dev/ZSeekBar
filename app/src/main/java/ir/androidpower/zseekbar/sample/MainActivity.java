package ir.androidpower.zseekbar.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import zHelper.view.ZSeekBar;


public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);


    final TextView test_progress = findViewById(R.id.test_progress2);
    final ZSeekBar test_zSeekBar = findViewById(R.id.test_zSeekBar2);


    test_zSeekBar.setOnZArcSeekBarChangeListener(new ZSeekBar.OnZArcSeekBarChangeListener() {
      @Override
      public void onProgressChanged(ZSeekBar circleProgress, float progress, boolean fromUser) {
        test_progress.setText((int) progress + "");
      }


      @Override
      public void onStartTrackingTouch(ZSeekBar circleProgress) {

      }


      @Override
      public void onStopTrackingTouch(ZSeekBar circleProgress) {

      }
    });
  }
}
