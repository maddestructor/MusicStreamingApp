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
    private int currentSongId = 0;
    private Song currentSong;
    ArrayList<Song> songList;

    public final static String STREAMING_STRING = "streaming";
    public final static String STANDARD_STRING = "standard";
    public final static Boolean STREAMING_ON = true;
    public final static Boolean STREAMING_OFF = false;

    public MusicService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Get songList from Music device folder
        musicFiles = new File(Environment.getExternalStorageDirectory().getPath() + "/Music/").listFiles();

        populateSongList();

        // By default prepare the media player with first song at index 0
        prepareMediaPlayer(Uri.parse(musicFiles[currentSongId].getPath()));
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
    private void prepareMediaPlayer(Uri uri){
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getBaseContext(), uri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create song object
        this.currentSong = this.createSongObject(currentSongId, uri);
    }

    private void populateSongList(){
        songList = new ArrayList<>();
        for(int i = 0; i < musicFiles.length; i++){

            // Get song uri
            Uri uri = Uri.parse(musicFiles[i].getPath());

            // Create song object
            Song song = this.createSongObject(i, uri);

            // Add song to list
            songList.add(song);

        }
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

    public Song startNewSong(int songId){
        this.currentSongId = songId;
        Uri uri = Uri.parse(musicFiles[this.currentSongId].getPath());
        this.prepareMediaPlayer(uri);
        mediaPlayer.start();

        return currentSong;
    }

    public ArrayList<Song> getSongList() {
        return songList;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public Song getSongByID(int id) {
        return songList.get(id);
    }

    public void startSongFromTime(int time) {
    }

    public Boolean getStreaming() {
        return isStreaming;
    }

    public void setStreaming(Boolean streaming) {
        isStreaming = streaming;
    }

    private Song createSongObject(int id, Uri uri){

        // Set media metadate retriever
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(getBaseContext(), uri);

        // Create Song object
        Song song = new Song();
        song.setId(id + "");
        song.setTitle(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
        song.setArtist(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        song.setAlbum(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
        song.setDuration(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        return song;
    }
}
