package hellindustries.musicalsystemclient;

import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    ImageButton stopBtn;
    ImageView albumImg;
    ImageView albumIcon;
    SeekBar seekbar;
    TextView songNameTxt;
    TextView artistTxt;
    TextView songTimeTxt;
    TextView currentTimeTxt;

    AsyncHttpClient asyncHttpClient;

    private ArrayList<Song> songList;
    private Song currentSong;
    private int currentSongIndex = 0;
    private boolean isPlaying = false;
    private boolean isShuffled = false;
    private boolean isRepeating = false;
    private int currentTime = 0;
    private Handler handler;
    private Runnable updateProgressionRunnable;


    public final static String STREAMING_STRING = "streaming";
    public final static String STANDARD_STRING = "standard";
    public final static String PLAY_TYPE = "playingType";
    private final int ONE_SECOND_IN_MILLIS = 1000;
    private final String BASIC_GET_URI = "http://192.168.43.1:9000/";

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
        handler = new Handler();
        updateProgressionRunnable = new Runnable() {
            @Override
            public void run() {
                updateProgression();
            }
        };

        // Get songs list from server
        songList = new ArrayList<>();
        this.getSongs();
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
        asyncHttpClient.get(BASIC_GET_URI + "songList", new JsonHttpResponseHandler() {
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
        asyncHttpClient.addHeader(PLAY_TYPE, STANDARD_STRING);
        asyncHttpClient.get(BASIC_GET_URI + "playpause", new JsonHttpResponseHandler() {
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
        asyncHttpClient.get(BASIC_GET_URI + "stop", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Update play pause state
                isPlaying = false;
                updatePlayPauseBtn();
                playPauseProgression();

                // Reset current time and seekbar
                currentTime = 0;
                currentTimeTxt.setText(millisToStringTimer(0));
                seekbar.setProgress(0);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }


    private void startNewSong(){
        int songId = Integer.parseInt(songList.get(currentSongIndex).getId());
        RequestParams params = new RequestParams("searchByID", songId);
        asyncHttpClient.get(BASIC_GET_URI + "playpause", params, new JsonHttpResponseHandler() {
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

        // If the song is finished
        if(currentTime >= Integer.parseInt(currentSong.getDuration())){
            handler.removeCallbacks(updateProgressionRunnable);
            
            if(isRepeating)
                currentSongIndex --;

            doNext();
        }

        handler.postDelayed(updateProgressionRunnable, ONE_SECOND_IN_MILLIS);
    }
}
