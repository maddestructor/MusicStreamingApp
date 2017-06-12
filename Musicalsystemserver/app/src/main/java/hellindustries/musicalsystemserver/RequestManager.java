package hellindustries.musicalsystemserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fi.iki.elonen.NanoHTTPD;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jonathan on 2017-05-25.
 */

public class RequestManager extends NanoHTTPD {

    private MusicService service;

    public RequestManager(int port, MusicService service) {
        super(port);
        this.service = service;
    }

    public RequestManager(String hostname, int port) {
        super(hostname, port);
    }

    @Override
    public Response serve(IHTTPSession session) {

        String uri = session.getUri();

        if(uri.equalsIgnoreCase("/playpause")){

            Song song;
            if(session.getParms().containsKey("searchByID")){
                int songId = Integer.parseInt(session.getParms().get("searchByID"));
                song = this.service.startNewSong(songId);
            } else {
                this.service.playPause();
                song = this.service.sendCurrentSong();
            }

            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(song);
            return new Response(json);

        } else if (uri.equalsIgnoreCase("/currentsong")){

            this.service.sendCurrentSong();

        } else if (uri.toLowerCase().contains("songlist")){

            Matcher m = regexMatcher("^\\/songlist\\/+(\\d{1,4})$", uri);

            if(m.find()){
                this.service.sendSongByID(Integer.parseInt(m.group(1)));
            } else if (uri.equalsIgnoreCase("/songlist")){
                Gson gson = new GsonBuilder().create();
                String json = gson.toJson(this.service.getSongList());
                return new Response(json);
            } else {
                //should return an http error
            }

        } else if (uri.toLowerCase().contains("seekto")){

            Matcher m = regexMatcher("^\\/seekto\\/+(\\d{1,3})$", uri);

            if(m.find()){
                this.service.startSongFromTime(Integer.parseInt(m.group(1)));
            } else {
                //should return an http error
            }

        }

        return super.serve(session);
    }

    private Matcher regexMatcher(String pattern, String toParse){
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(toParse);

        return m;
    }

    private void setStreamingType(String streamingType){

    }
}
