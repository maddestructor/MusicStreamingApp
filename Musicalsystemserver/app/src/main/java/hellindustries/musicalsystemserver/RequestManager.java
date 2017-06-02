package hellindustries.musicalsystemserver;

import fi.iki.elonen.NanoHTTPD;

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
        switch (session.getUri()){
            case "/playpause":
                this.service.playPause();
                break;
            case "/previous":
                this.service.doPrevious();
                break;
            case "/next":
                this.service.doNext();
                break;
        }

        return super.serve(session);
    }
}
