package com.osovskiy.bmwinterface.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.osovskiy.bmwinterface.R;

/**
 * Created by Administrator on 8/22/2014.
 */
public class Preferences extends PreferenceFragment
{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.preferences);
  }
}
