package app.com.example.kiran.sunshine;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

//AppCompatActivity
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener

{
    private static final String KEY_EDIT_TEXT_PREFERENCE = "LOCATION";
    private static final String KEY_LIST_View_PREFERENCE = "UNITS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.xml);
        // Get the Units from the default Shared preference
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);

        updatePreference(getString(R.string.pref_Location_key));
        updatePreference(getString(R.string.pref_units_key));
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        updatePreference(key);
    }

    private void updatePreference(String key){
        Preference preference = findPreference(key);
        if (key.equals(KEY_EDIT_TEXT_PREFERENCE)){

            if (preference instanceof EditTextPreference){
                EditTextPreference editTextPreference =  (EditTextPreference)preference;
                if (editTextPreference.getText().trim().length() > 0){
                    editTextPreference.setSummary( editTextPreference.getText());
                }else{
                    editTextPreference.setSummary(getString(R.string.pref_Location_default));
                }
            }

        }
        else if(preference instanceof ListPreference)
        {
            ListPreference listPreference = (ListPreference)preference;
            if (listPreference.getEntry().length() > 0) {
                listPreference.setSummary(listPreference.getEntry());
            }
            else
            {
                listPreference.setSummary(R.string.pref_units_label_metric);
            }
        }
    }
}
