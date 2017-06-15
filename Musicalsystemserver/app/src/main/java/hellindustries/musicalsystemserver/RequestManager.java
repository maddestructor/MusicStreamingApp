package hellindustries.musicalsystemserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fi.iki.elonen.NanoHTTPD;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
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

        //on gère les différents appels sur le serveur
        if(uri.equalsIgnoreCase("/playpause")){

            return handlePlayPause(session);

        } else if (uri.equalsIgnoreCase("/currentsong")){

            return createSuccessfulResponse(this.service.getCurrentSong());

        } else if (uri.equalsIgnoreCase("/stop")){

            return handleStopRequest();

        } else if (uri.toLowerCase().contains("songlist")){

            return handleSongListRequest(session);

        } else if (uri.toLowerCase().contains("seekto")){

            return handleSeekToRequest(session);

        } else {
            return createBadReqResponse("Le chemin de votre requête n'existe pas dans l'api");
        }
    }

    private Response handlePlayPause(IHTTPSession session) {



        String playingType = session.getHeaders().get(MusicService.PLAY_TYPE);

        if(playingType != null){
            if(playingType.equalsIgnoreCase(MusicService.STREAMING_STRING)){
                service.setStreaming(MusicService.STREAMING_ON);
                return handleStreamingRequest(session);
            } else if (playingType.equalsIgnoreCase(MusicService.STANDARD_STRING)){
                service.setStreaming(MusicService.STREAMING_OFF);
                return handleStandardRequest(session);
            } else {
                return createBadReqResponse("Vous devez spécifier le mode de lecture dans votre requête");
            }
        } else {
            File songToPlay = service.getMusicFiles()[service.getCurrentSongId()];
            return serveFile(session, songToPlay, "audio/mpeg");
        }

    }

    private Response handleStreamingRequest(IHTTPSession session) {
        File songToPlay;

        if(session.getParms().containsKey("searchByID")){

            int songId = Integer.parseInt(session.getParms().get("searchByID"));
            songToPlay = service.getMusicFiles()[songId];
            service.setCurrentSongById(songId);

        } else {
            songToPlay = service.getMusicFiles()[service.getCurrentSongId()];
        }

        serveFile(session, songToPlay, "audio/mpeg");

        return createSuccessfulResponse(this.service.getCurrentSong());
    }

    private Response handleStandardRequest(IHTTPSession session) {
        Song song;

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

    private Response handleStopRequest() {
        Boolean songHasBeenStopped = this.service.stop();

        if (songHasBeenStopped){
            return createSuccessfulResponse("The song was successfully stopped");
        } else {
            return createSuccessfulResponse("No song was playing at the moment");
        }
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

    /**
     * CODE_EMPRUNTÉ!!!! Méthode qui crée une réponse d'acceptation pour l'envoi d'un
     * contenu partiel. Pour voir la source du code, veuillez vous référer au lien ci-dessous
     * @param status
     * @param mimeType
     * @param message
     * @return
     * @source https://stackoverflow.com/questions/19359304/how-to-serve-a-file-on-sdcard-using-nanohttpd-inside-android/19601432#19601432
     */
    private Response createAcceptPartialResponse(Response.Status status, String mimeType,
                                                 InputStream message){
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;

    }

    /**
     * CODE_EMPRUNTÉ!!!! Méthode qui crée une réponse d'acceptation pour l'envoi d'un
     * contenu partiel. Pour voir la source du code, veuillez vous référer au lien ci-dessous
     * @param status
     * @param mimeType
     * @param message
     * @return
     * @source https://stackoverflow.com/questions/19359304/how-to-serve-a-file-on-sdcard-using-nanohttpd-inside-android/19601432#19601432
     */
    private Response createAcceptPartialResponse(Response.Status status, String mimeType,
                                                 String message){
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;

    }

    /**
     * CODE_EMPRUNTÉ!!!!
     * Permet de servir des fichiers locaux (musique) en différent chunk donc
     * en lecture en transit.
     * @source https://stackoverflow.com/questions/19359304/how-to-serve-a-file-on-sdcard-using-nanohttpd-inside-android/19601432#19601432
     */
    private Response serveFile(IHTTPSession session, File file, String mime) {
        Response res;
        Map<String, String> header = session.getHeaders();

        try {
            // Calculate etag
            String etag = Integer.toHexString((file.getAbsolutePath()
                    + file.lastModified() + "" + file.length()).hashCode());

            // Support (simple) skipping:
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range
                                    .substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            // Change return code and add Content-Range header when skipping is
            // requested
            long fileLen = file.length();
            if (range != null && startFrom >= 0) {
                if (startFrom >= fileLen) {
                    res = createAcceptPartialResponse(Response.Status.RANGE_NOT_SATISFIABLE,
                            NanoHTTPD.MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }

                    final long dataLen = newLen;
                    FileInputStream fis = new FileInputStream(file) {
                        @Override
                        public int available() throws IOException {
                            return (int) dataLen;
                        }
                    };
                    fis.skip(startFrom);

                    res = createAcceptPartialResponse(Response.Status.PARTIAL_CONTENT, mime,
                            fis);
                    res.addHeader("Content-Length", "" + dataLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-"
                            + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else {
                if (etag.equals(header.get("if-none-match")))
                    res = createAcceptPartialResponse(Response.Status.NOT_MODIFIED, mime, "");
                else {
                    res = createAcceptPartialResponse(Response.Status.OK, mime,
                            new FileInputStream(file));
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }
        } catch (IOException ioe) {
            res = createAcceptPartialResponse(Response.Status.FORBIDDEN,
                    NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
        }

        return res;
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
