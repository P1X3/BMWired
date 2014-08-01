package com.osovskiy.bmwinterface;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Administrator on 8/1/2014.
 */
public class InterfaceSettings extends PreferenceActivity
{
  @Override
  protected void onPostCreate(Bundle savedInstanceState)
  {
    super.onPostCreate(savedInstanceState);

    //addPreferencesFromResource(R.xml.preferences);
  }
}
