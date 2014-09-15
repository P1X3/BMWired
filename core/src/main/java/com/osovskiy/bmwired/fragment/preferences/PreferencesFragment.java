package com.osovskiy.bmwired.fragment.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.osovskiy.bmwired.R;

public class PreferencesFragment extends PreferenceFragment
{
  SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener()
  {
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
      setupPreferences(sharedPreferences);
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
    setupPreferences(getPreferenceManager().getSharedPreferences());
  }

  @Override
  public void onResume()
  {
    super.onResume();
    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
  }

  @Override
  public void onPause()
  {
    getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    super.onPause();
  }

  private void setupPreferences(SharedPreferences sharedPreferences)
  {
    if (sharedPreferences.getBoolean("bluetooth_interface", false))
    {
      PreferenceScreen preferenceScreen = getPreferenceScreen();
      preferenceScreen.findPreference("bluetooth_mac").setEnabled(true);
      preferenceScreen.findPreference("bluetooth_attempts").setEnabled(true);
      preferenceScreen.findPreference("bluetooth_delay").setEnabled(true);
    }
    else
    {
      PreferenceScreen preferenceScreen = getPreferenceScreen();
      preferenceScreen.findPreference("bluetooth_mac").setEnabled(false);
      preferenceScreen.findPreference("bluetooth_attempts").setEnabled(false);
      preferenceScreen.findPreference("bluetooth_delay").setEnabled(false);
    }
  }
}
