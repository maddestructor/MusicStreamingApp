package hellindustries.musicalsystemclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class PlayerActivity extends AppCompatActivity {

    ImageView playPauseBtn;
    ImageView nextBtn;
    ImageView previousBtn;
    ImageView shuffleBtn;
    ImageView repeatBtn;
    ImageView listBtn;
    ImageView albumImg;
    ImageView albumIcon;
    SeekBar seekbar;
    TextView songNameTxt;
    TextView artistTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getUIComponents();
        setOnClickListeners();

    }

    private void getUIComponents() {
        playPauseBtn = (ImageView) findViewById(R.id.playPauseBtn);
        nextBtn = (ImageView) findViewById(R.id.nextBtn);
        previousBtn = (ImageView) findViewById(R.id.previousBtn);
        shuffleBtn = (ImageView) findViewById(R.id.shuffleBtn);
        repeatBtn = (ImageView) findViewById(R.id.repeatBtn);
        listBtn = (ImageView) findViewById(R.id.listBtn);
        albumImg = (ImageView) findViewById(R.id.albumImg);
        albumIcon = (ImageView) findViewById(R.id.albumIcon);

        seekbar = (SeekBar) findViewById(R.id.seekBar);
        songNameTxt = (TextView) findViewById(R.id.songNameTxt);
        artistTxt = (TextView) findViewById(R.id.artistTxt);
    }

    private void setOnClickListeners() {
        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
