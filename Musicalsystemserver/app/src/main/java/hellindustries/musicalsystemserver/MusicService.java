package hellindustries.musicalsystemserver;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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

    public void startNewSong(int songId){
        this.currentSongIndex = songId;
        this.prepareMediaPlayer();
        mediaPlayer.start();
    }

    public ArrayList<Song> getSongList() {
        ArrayList<Song> songs = new ArrayList<>();
        for(int i = 0; i < musicFiles.length; i++){

            // Get song infos
            Uri uri = Uri.parse(musicFiles[i].getPath());
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(getBaseContext(), uri);

            // Create Song object
            Song song = new Song();
            song.setId(i + "");
            song.setTitle(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            song.setArtist(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            song.setAlbum(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            song.setDuration(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

            // Add song to list
            songs.add(song);

        }
        return songs;
    }

    public void sendCurrentSong() {
    }

    public void sendSongByID(int id) {
    }

    public void startSongFromTime(int time) {
    }
}
