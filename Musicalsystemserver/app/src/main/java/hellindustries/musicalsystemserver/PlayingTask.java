package hellindustries.musicalsystemserver;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

/**
 * Created by mathieu on 5/25/17.
 */

public class PlayingTask extends AsyncTask {

    private Context context;

    public PlayingTask(Context context){
        super();
        this.context = context;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        MusicService service = new MusicService();
        Intent serviceIntent = new Intent(context, MusicService.class);
        service.startService(serviceIntent);

        return null;
    }
}
