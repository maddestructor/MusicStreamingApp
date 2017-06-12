package hellindustries.musicalsystemserver;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;

import java.io.File;
import java.io.IOException;

public class MusicService extends Service {

    private static final int PORT = 9000;
    private MediaPlayer mediaPlayer;

    private Boolean isStreaming = false;
    private File[] musicFiles;
    private int currentSongIndex = 0;

    public MusicService() {
        super();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        musicFiles = new File(Environment.getExternalStorageDirectory().getPath() + "/Music/").listFiles();
        prepareMediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        RequestManager requestManager = new RequestManager(PORT, this);
        try {
            requestManager.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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
    }

    /**
     * Method that do play or do pause
     */
    public void playPause(){
        if(!mediaPlayer.isPlaying()){
            mediaPlayer.start();
        } else {
            mediaPlayer.pause();
        }
    }

    /**
     * Method to go to the previous song
     */
    public void doPrevious(){
        if(currentSongIndex > 1)
            currentSongIndex --;
        else
            currentSongIndex = musicFiles.length - 1;

        prepareMediaPlayer();
        playPause();
    }

    /**
     * Method to go to the next song
     */
    public void doNext(){
        if(currentSongIndex < musicFiles.length - 1)
            currentSongIndex++;
        else
            currentSongIndex = 0;

        prepareMediaPlayer();
        playPause();
    }

    public void sendSongList() {
    }

    public void sendCurrentSong() {
    }

    public void sendSongByID(int id) {
    }

    public void startSongFromTime(int time) {
    }

    public Boolean getStreaming() {
        return isStreaming;
    }

    public void setStreaming(Boolean streaming) {
        isStreaming = streaming;
    }
}
