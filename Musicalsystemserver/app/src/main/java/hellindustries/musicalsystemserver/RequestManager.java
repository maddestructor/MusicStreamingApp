package hellindustries.musicalsystemserver;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by Jonathan on 2017-05-25.
 */

public class RequestManager extends NanoHTTPD {
    public RequestManager(int port) {
        super(port);
    }

    public RequestManager(String hostname, int port) {
        super(hostname, port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        return super.serve(session);
    }
}
