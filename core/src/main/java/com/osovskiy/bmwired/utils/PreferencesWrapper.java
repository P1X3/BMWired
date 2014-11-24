package com.osovskiy.bmwired.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

abstract class PreferencesWrapper
{
  protected SharedPreferences sharedPreferences;
  protected Context context;

  protected PreferencesWrapper(Context context)
  {
    this.context = context;
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
  }
}