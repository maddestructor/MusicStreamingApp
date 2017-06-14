package hellindustries.musicalsystemserver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ServerActivity extends AppCompatActivity {

    private final static int REQUEST_EXTERNAL_STORAGE = 50;
    private Switch serviceToggle;
    private ListView songsListView;
    private List<String> songArray;
    private File[] musicFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        verifyStoragePermission();

        startService(new Intent(this, MusicService.class));

        serviceToggle = (Switch)findViewById(R.id.serviceToggle);
        songsListView = (ListView) findViewById(R.id.songsListView);
        musicFiles = new File(Environment.getExternalStorageDirectory().getPath() + "/Music/").listFiles();
        populateSongArray();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, songArray);
        songsListView.setAdapter(arrayAdapter);

        serviceToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    startService(new Intent(getApplicationContext(), MusicService.class));
                } else {
                    stopService(new Intent(getApplicationContext(), MusicService.class));
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, MusicService.class));
        super.onDestroy();
    }

    private void verifyStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    private void populateSongArray() {
        songArray = new ArrayList<>();
        for (int i = 0; i < musicFiles.length; i++) {

            // Get song uri
            Uri uri = Uri.parse(musicFiles[i].getPath());

            // Create song object
            Song song = this.createSongObject(i, uri);

            // Add song to list
            songArray.add(song.getTitle() + " - " + song.getArtist());

        }
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
