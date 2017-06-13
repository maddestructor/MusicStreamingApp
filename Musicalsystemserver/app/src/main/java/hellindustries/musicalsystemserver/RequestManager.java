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

            return handlePlayPause(session);

        } else if (uri.equalsIgnoreCase("/currentsong")){

            return createSuccessfulResponse(this.service.getCurrentSong());

        } else if (uri.toLowerCase().contains("songlist")){

            return handleSongListRequest(session);

        } else if (uri.toLowerCase().contains("seekto")){

            return handleSeekToRequest(session);

        }

        return super.serve(session);
    }

    private Response handlePlayPause(IHTTPSession session) {
        Song song;

        if(session.getHeaders().get(0).equalsIgnoreCase(MusicService.STREAMING_STRING)){
            service.setStreaming(MusicService.STREAMING_ON);
        } else if (session.getHeaders().get(0).equalsIgnoreCase(MusicService.STANDARD_STRING)){
            service.setStreaming(MusicService.STREAMING_OFF);
        } else {
            return createBadReqResponse("Vous devez spécifier le mode de lecture dans votre requête");
        }

        if(session.getParms().containsKey("searchByID")){
            int songId = Integer.parseInt(session.getParms().get("searchByID"));
            song = this.service.startNewSong(songId);
        } else {
            this.service.playPause();
            song = this.service.getCurrentSong();
        }

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(song);
        return new Response(json);
    }

    private Response handleSongListRequest(IHTTPSession session){
        String uri = session.getUri();

        Matcher m = regexMatcher("^\\/songlist\\/+(\\d{1,4})$", uri);

        if(m.find()){
            return createSuccessfulResponse(this.service.getSongByID(Integer.parseInt(m.group(1))));
        } else if (uri.equalsIgnoreCase("/songlist")){
            return createSuccessfulResponse(this.service.getSongList());
        } else {
            return createBadReqResponse("L'identifiant de chanson spécifié n'existe pas");
        }
    }

    private Response handleSeekToRequest(IHTTPSession session) {
        String uri = session.getUri();

        Matcher m = regexMatcher("^\\/seekto\\/+(\\d{1,3})$", uri);

        if(m.find()){
            this.service.startSongFromTime(Integer.parseInt(m.group(1)));
            return createSuccessfulResponse("I shall do what you say");
        } else {
            return createIServerErrResponse("Un problème est survenu dans la fonction SeekTo");
        }
    }

    private Matcher regexMatcher(String pattern, String toParse){
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(toParse);

        return m;
    }

    private void setStreamingType(String streamingType){

    }

    private Response createSuccessfulResponse(Object obj){
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(obj);
        return new Response(Response.Status.OK, "application/json", json);
    }

    private Response createBadReqResponse(String msg){

        return new Response(Response.Status.BAD_REQUEST, "message", msg);

    }

    private Response createIServerErrResponse(String msg){

        return new Response(Response.Status.INTERNAL_ERROR, "message", msg);

    }
}
