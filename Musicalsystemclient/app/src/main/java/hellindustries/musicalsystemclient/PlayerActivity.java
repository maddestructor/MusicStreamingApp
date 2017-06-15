package hellindustries.musicalsystemclient;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;

public class PlayerActivity extends AppCompatActivity {

    ImageView playPauseBtn;
    ImageView nextBtn;
    ImageView previousBtn;
    ImageView shuffleBtn;
    ImageView repeatBtn;
    ImageView listBtn;
    ImageView settingsBtn;
    ImageButton stopBtn;
    ImageView albumImg;
    ImageView albumIcon;
    SeekBar seekbar;
    TextView songNameTxt;
    TextView artistTxt;
    TextView songTimeTxt;
    TextView currentTimeTxt;
    ProgressBar spinner;

    AsyncHttpClient asyncHttpClient;

    private ArrayList<Song> songList;
    private Song currentSong;
    private int currentSongIndex = 0;
    private boolean isPlaying = false;
    private boolean isShuffled = false;
    private boolean isRepeating = false;
    private boolean isStreaming = false;
    private MediaPlayer mediaPlayer;
    private int currentTime = 0;
    private Handler handler;
    private Runnable updateProgressionRunnable;
    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;

    private String ip;
    private String port;
    private String basicGetUri;

    public final static String STREAMING_STRING = "streaming";
    public final static String STANDARD_STRING = "standard";
    public final static String PLAY_TYPE = "playingType";
    private final String PREF_IP = "pref_ip";
    private final String PREF_PORT = "pref_port";
    private final String PREF_STREAMING = "pref_streaming";
    private final int ONE_SECOND_IN_MILLIS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Initiate UI
        getUIComponents();
        setOnClickListeners();

        // Disable seekbar from being dragged
        seekbar.setEnabled(false);

        // Initiate some variables
        asyncHttpClient = new AsyncHttpClient();
        songList = new ArrayList<>();
        handler = new Handler();
        updateProgressionRunnable = new Runnable() {
            @Override
            public void run() {
                updateProgression();
            }
        };

        // Preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        assignPreferencesValues();
        setPreferencesListener();
    }

    private void getUIComponents() {
        playPauseBtn = (ImageView) findViewById(R.id.playPauseBtn);
        nextBtn = (ImageView) findViewById(R.id.nextBtn);
        previousBtn = (ImageView) findViewById(R.id.previousBtn);
        shuffleBtn = (ImageView) findViewById(R.id.shuffleBtn);
        repeatBtn = (ImageView) findViewById(R.id.repeatBtn);
        listBtn = (ImageView) findViewById(R.id.listBtn);
        stopBtn = (ImageButton) findViewById(R.id.stopBtn);
        albumImg = (ImageView) findViewById(R.id.albumImg);
        albumIcon = (ImageView) findViewById(R.id.albumIcon);
        settingsBtn = (ImageView) findViewById(R.id.settingsBtn);
        spinner = (ProgressBar) findViewById(R.id.spinner);
        spinner.setVisibility(View.GONE);
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
                doPlayPause();
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
                doShuffle();
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRepeat();
            }
        });
        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doStop();
            }
        });
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(PlayerActivity.this, MuscialSystemClientPreferenceActivity.class);
                startActivityForResult(intent, 0);
            }
        });
    }

    private void assignPreferencesValues() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ip = sharedPreferences.getString(PREF_IP, getResources().getString(R.string.pref_ip_default));
        port = sharedPreferences.getString(PREF_PORT, getResources().getString(R.string.pref_port_default));
        isStreaming = sharedPreferences.getBoolean(PREF_STREAMING, false);
        updateBasicGetUri();
    }

    private void setPreferencesListener(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                switch (key){
                    case PREF_IP:
                        ip = sharedPreferences.getString(key, getResources().getString(R.string.pref_ip_default));
                        updateBasicGetUri();
                        break;
                    case PREF_PORT:
                        port = sharedPreferences.getString(key, getResources().getString(R.string.pref_port_default));
                        updateBasicGetUri();
                        break;
                    case PREF_STREAMING:
                        isStreaming = sharedPreferences.getBoolean(key, false);
                        doStop();
                        break;
                }
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(prefsListener);
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


    private void getSongs(){
        asyncHttpClient.get(basicGetUri + "songList", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                for(int i = 0; i < response.length(); i++){
                    try {
                        Song song = new Gson().fromJson(response.getString(i), Song.class);
                        songList.add(song);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

            }
        });
    }

    /**
     * Method that do play or do pause
     */
    private void doPlayPause(){
        if(isStreaming){
            if(mediaPlayer == null){
                streamRequest();
            } else {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    isPlaying = false;
                } else {
                    mediaPlayer.start();
                    isPlaying = true;
                }
                updatePlayPauseBtn();
                playPauseProgression();
            }
        } else {
            standardRequest();
        }
    }

    private void standardRequest() {
        asyncHttpClient.addHeader(PLAY_TYPE, STANDARD_STRING);
        asyncHttpClient.get(basicGetUri + "playpause", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject responseBody) {
                isPlaying = !isPlaying;
                updatePlayPauseBtn();

                currentSong = new Gson().fromJson(responseBody.toString(), Song.class);
                playPauseProgression();
                updateSongInfos();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

            }
        });
    }

    private void standardRequest(int songId) {
        RequestParams params = new RequestParams("searchByID", songId);
        asyncHttpClient.get(basicGetUri + "playpause", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject responseBody) {
                currentTime = 0;

                isPlaying = true;
                updatePlayPauseBtn();

                currentSong = new Gson().fromJson(responseBody.toString(), Song.class);
                playPauseProgression();
                updateSongInfos();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

            }
        });
    }

    /**
     * Allow us to request a stream for the current song on the server
     */
    private void streamRequest() {
        //We tell the server we want to start a stream and he will
        asyncHttpClient.addHeader(PLAY_TYPE, STREAMING_STRING);
        asyncHttpClient.get(basicGetUri + "playpause", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject responseBody) {

                currentSong = new Gson().fromJson(responseBody.toString(), Song.class);
                playPauseProgression();
                updateSongInfos();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

            }
        });
        prepareMediaPlayer(basicGetUri + "playpause");
        //He will then connect to the stream by itself
        //We use the same URL we started the stream on and give to the mediaPlayer
    }

    /**
     * Allow us to request a stream for a specific song
     * @param songId
     */
    private void streamRequest(int songId) {
        //We tell the server we want to start a stream for a specific song
        asyncHttpClient.addHeader(PLAY_TYPE, STREAMING_STRING);
        RequestParams params = new RequestParams("searchByID", songId);
        asyncHttpClient.get(basicGetUri + "playpause", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject responseBody) {
                currentTime = 0;

                isPlaying = true;
                updatePlayPauseBtn();

                currentSong = new Gson().fromJson(responseBody.toString(), Song.class);
                playPauseProgression();
                updateSongInfos();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

            }
        });
        //We use the same URL we started the stream on and give to the mediaPlayer
        //He will then connect to the stream by itself
        prepareMediaPlayer(basicGetUri + "playpause");
    }

    /**
     * Method to go to the previous song
     */
    private void doPrevious(){
        if(currentSongIndex > 0)
            currentSongIndex --;
        else
            currentSongIndex = songList.size() - 1;

        startNewSong();
    }

    /**
     * Method to go to the next song
     */
    private void doNext(){
        if(currentSongIndex < songList.size() - 1)
            currentSongIndex++;
        else
            currentSongIndex = 0;

        startNewSong();
    }

    private void doStop() {
        asyncHttpClient.get(basicGetUri + "stop", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                resetUI();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private void resetUI(){
        // Update play pause state
        isPlaying = false;
        updatePlayPauseBtn();
        playPauseProgression();

        // Reset current time and seekbar
        currentTime = 0;
        currentTimeTxt.setText(millisToStringTimer(0));
        seekbar.setProgress(0);
    }


    private void startNewSong(){
        int songId = Integer.parseInt(songList.get(currentSongIndex).getId());
        if(isStreaming) {
            streamRequest(songId);
        } else {
            standardRequest(songId);
        }
    }

    private void doShuffle(){
        if(isShuffled){
            shuffleBtn.setColorFilter(ContextCompat.getColor(getBaseContext(), R.color.disabledElementColor));
            Collections.sort(songList);
        } else {
            shuffleBtn.setColorFilter(ContextCompat.getColor(getBaseContext(), R.color.colorAccent));
            Collections.shuffle(songList);
        }

        // To be sure we start at index 0 when we do next
        currentSongIndex = -1;
        isShuffled = !isShuffled;
    }

    private void doRepeat(){
        if(isRepeating)
            repeatBtn.setColorFilter(ContextCompat.getColor(getBaseContext(), R.color.disabledElementColor));
        else
            repeatBtn.setColorFilter(ContextCompat.getColor(getBaseContext(), R.color.colorAccent));

        isRepeating = !isRepeating;
    }

    /**
     * Updaters section
     */

    private void updateBasicGetUri(){
        basicGetUri = "http://" + ip + ":" + port + "/";
        this.getSongs();
    }

    private void updateSongInfos(){
        this.songNameTxt.setText(currentSong.getTitle());
        this.artistTxt.setText(currentSong.getArtist());
        this.songTimeTxt.setText(this.millisToStringTimer(Integer.parseInt(currentSong.getDuration())));

        this.seekbar.setMax(Integer.parseInt(currentSong.getDuration()));
    }

    private void updatePlayPauseBtn(){
        if(isPlaying)
            playPauseBtn.setImageResource(R.drawable.ic_pause_black_24dp);
        else
            playPauseBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
    }

    private void playPauseProgression(){
        if(isPlaying)
            handler.postDelayed(updateProgressionRunnable, 1);
        else
            handler.removeCallbacks(updateProgressionRunnable);
    }

    private void updateProgression(){
        handler.removeCallbacks(updateProgressionRunnable);

        seekbar.setProgress(currentTime);
        currentTimeTxt.setText(this.millisToStringTimer(currentTime));

        currentTime += ONE_SECOND_IN_MILLIS;


        handler.postDelayed(updateProgressionRunnable, ONE_SECOND_IN_MILLIS);
    }

    private void prepareMediaPlayer(String url){
        spinner.setVisibility(View.VISIBLE);

        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        setMediaPlayerListeners(mediaPlayer);
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setMediaPlayerListeners(MediaPlayer mediaPlayer) {
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                spinner.setVisibility(View.GONE);
                mp.start();
                isPlaying = true;
                updatePlayPauseBtn();
                playPauseProgression();

            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(isRepeating)
                    currentSongIndex --;

                doNext();
            }
        });
        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {

            }
        });
    }

}
