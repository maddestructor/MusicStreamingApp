package hellindustries.musicalsystemclient;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;

/**
 * Created by Jonathan on 2017-05-25.
 */

public class ClientPlayingTask extends AsyncTask {

    public final static String PLAY_COMMAND = "play";
    @Override
    protected Object doInBackground(Object[] params) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
//
            URIBuilder uriBuilder = new URIBuilder("http://192.168.43.1:9000");
            uriBuilder.setParameter("commandType", PLAY_COMMAND);
            HttpGet get = new HttpGet(uriBuilder.build());

            CloseableHttpResponse response = httpClient.execute(get);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
