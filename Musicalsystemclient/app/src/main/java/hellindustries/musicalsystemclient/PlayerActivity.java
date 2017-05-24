package hellindustries.musicalsystemclient;

import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
    TextView songTimeTxt;
    TextView currentTimeTxt;

    private MediaPlayer mediaPlayer;
    private int currentSongIndex = 0;
    private File[] musicFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getUIComponents();
        setOnClickListeners();

        // Initiate songs list
        musicFiles = new File(Environment.getExternalStorageDirectory().getPath() + "/Music/").listFiles();

        // Initiate MediaPlayer
        prepareMediaPlayer();

        // Seek song when we move seekbar
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mediaPlayer.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // For seekbar updates during play
        final Handler handler = new Handler();
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int currentTime = mediaPlayer.getCurrentPosition();
                seekbar.setProgress(currentTime);
                currentTimeTxt.setText(millisToStringTimer(currentTime));
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
        songTimeTxt = (TextView) findViewById(R.id.songTimeTxt);
        currentTimeTxt = (TextView) findViewById(R.id.currentTimeTxt);
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
                doNext();
            }
        });
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doPrevious();
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

    /**
     * Method to go to the next song
     */
    private void doNext(){
        if(currentSongIndex < musicFiles.length - 1)
            currentSongIndex++;
        else
            currentSongIndex = 0;

        prepareMediaPlayer();
        playPause();
    }

    /**
     * Method to go to the previous song
     */
    private void doPrevious(){
        if(currentSongIndex > 1)
            currentSongIndex --;
        else
            currentSongIndex = musicFiles.length - 1;

        prepareMediaPlayer();
        playPause();
    }

    /**
     * Method that format timers
     * @param millis the time in milliseconds to format
     * @return a formatted string
     */
    private String millisToStringTimer(int millis){
        int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(millis);
        int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes));
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Method that prepares a new mediaplayer with current song (with index)
     * and updates song infos
     */
    private void prepareMediaPlayer(){
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Uri uri = Uri.parse(musicFiles[currentSongIndex].getPath());
        try {
            mediaPlayer.setDataSource(getBaseContext(), uri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Update song info band
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(getBaseContext(), uri);
        songNameTxt.setText(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
        artistTxt.setText(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));

        // Update time
        int time = mediaPlayer.getDuration();
        songTimeTxt.setText(millisToStringTimer(time));

        // Update seekbar
        seekbar.setMax(mediaPlayer.getDuration());
    }
}
