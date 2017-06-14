package hellindustries.musicalsystemclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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

import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;

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

    AsyncHttpClient asyncHttpClient;

    private ArrayList<Song> songs;
    private int currentSongIndex = 0;
    private boolean isPlaying = false;

    public final static String STREAMING_STRING = "streaming";
    public final static String STANDARD_STRING = "standard";
    public final static String PLAY_TYPE = "playingType";
    private final String BASIC_GET_URI = "http://192.168.2.189:9000/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getUIComponents();
        setOnClickListeners();

        asyncHttpClient = new AsyncHttpClient();

        // Get songs list from server
        songs = new ArrayList<>();
        this.getSongs();

        // Seek song when we move seekbar
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
                        songs.add(song);
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

                Song song = new Gson().fromJson(responseBody.toString(), Song.class);
                updateSongInfos(song);
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
        if(currentSongIndex > 1)
            currentSongIndex --;
        else
            currentSongIndex = songs.size() - 1;

        startNewSong();
    }

    /**
     * Method to go to the next song
     */
    private void doNext(){
        if(currentSongIndex < songs.size() - 1)
            currentSongIndex++;
        else
            currentSongIndex = 0;

        startNewSong();
    }

    private void startNewSong(){
        RequestParams params = new RequestParams("searchByID", currentSongIndex);
        asyncHttpClient.get(BASIC_GET_URI + "playpause", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject responseBody) {
                isPlaying = !isPlaying;
                updatePlayPauseBtn();

                Song song = new Gson().fromJson(responseBody.toString(), Song.class);
                updateSongInfos(song);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

            }
        });
    }

    private void updateSongInfos(Song song){
        this.songNameTxt.setText(song.getTitle());
        this.artistTxt.setText(song.getArtist());
        this.songTimeTxt.setText(this.millisToStringTimer(Integer.parseInt(song.getDuration())));
    }

    private void updatePlayPauseBtn(){
        if(isPlaying)
            playPauseBtn.setImageResource(R.drawable.ic_pause_black_24dp);
        else
            playPauseBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
    }
}
