package hellindustries.musicalsystemclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

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
    private final String BASIC_GET_URI = "http://192.168.43.1:9000/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getUIComponents();
        setOnClickListeners();

        asyncHttpClient = new AsyncHttpClient();

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

    /**
     * Method that do play or do pause
     */
    private void doPlayPause(){
        asyncHttpClient.get(BASIC_GET_URI + "playpause", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    /**
     * Method to go to the previous song
     */
    private void doPrevious(){
        asyncHttpClient.get(BASIC_GET_URI + "previous", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    /**
     * Method to go to the next song
     */
    public void doNext(){
        asyncHttpClient.get(BASIC_GET_URI + "next", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }
}
