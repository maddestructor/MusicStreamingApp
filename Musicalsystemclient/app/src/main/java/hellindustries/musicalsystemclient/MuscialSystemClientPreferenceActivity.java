package hellindustries.musicalsystemclient;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by jonat on 2017-06-15.
 */

public class MuscialSystemClientPreferenceActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new MuscialSystemClientPreferenceFragment()).commit();
    }

}
