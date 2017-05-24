package hellindustries.musicalsystemclient;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

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

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getUIComponents();
        setOnClickListeners();

        // Initiate MediaPlayer
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/Music/Liberee delivree.mp3");
        try {
            mediaPlayer.setDataSource(getBaseContext(), uri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // For seekbar updates during play
        final Handler handler = new Handler();
        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(mediaPlayer.getDuration());
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int currentTime = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(currentTime);
                handler.postDelayed(this, 1000);
            }
        });

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
                playPause();
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

    /**
     * Method that do play or do pause
     */
    private void playPause(){
        Log.d("CLICK", "playPauseBtn clicked");

        if(!mediaPlayer.isPlaying()){
            mediaPlayer.start();
            playPauseBtn.setImageResource(R.drawable.ic_pause_black_24dp);
        } else {
            mediaPlayer.pause();
            playPauseBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
    }
}
