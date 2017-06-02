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
import cz.msebera.android.httpclient.impl.entity.StrictContentLengthStrategy;

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
            URI uri = new URI("http://192.168.43.1:9000/playpause");
            HttpGet get = new HttpGet(uri);

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
