package hellindustries.musicalsystemserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;

public class MusicService extends Service {

    RequestManager requestManager;
    private static final int PORT = 9000;

    public MusicService() {
        super();
    }


    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        RequestManager requestManager = new RequestManager(PORT);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
