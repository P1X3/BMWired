package com.osovskiy.bmwired.fragment.plugins;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.osovskiy.bmwired.R;
import com.osovskiy.bmwired.lib.Utils;

import java.util.ArrayList;
import java.util.List;

class PluginLoader extends AsyncTask<Void, Void, List<Plugin>>
{
  private PluginsFragment pluginsFragment;

  public PluginLoader(PluginsFragment pluginsFragment)
  {
    this.pluginsFragment = pluginsFragment;
  }

  @Override
  protected List<Plugin> doInBackground(Void... params)
  {
    List<Plugin> plugins = new ArrayList<>();
    PackageManager pm = pluginsFragment.getActivity().getPackageManager();
    List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

    Drawable defaultDrawable = pluginsFragment.getResources().getDrawable(R.drawable.ic_launcher);
    for ( ApplicationInfo packageInfo : packages )
    {
      boolean receiveGranted = (PackageManager.PERMISSION_GRANTED == pm.checkPermission(Utils.PERMISSION_RECEIVE_MESSAGE, packageInfo.packageName));
      boolean sendGranted = (PackageManager.PERMISSION_GRANTED == pm.checkPermission(Utils.PERMISSION_SEND_MESSAGE, packageInfo.packageName));

      if ( receiveGranted || sendGranted )
      {
        String author = "empty";
        if ( packageInfo.metaData != null )
          author = packageInfo.metaData.getString("plugin_author");

        Drawable pluginIcon = defaultDrawable;

        try
        {
          pluginIcon = pm.getApplicationIcon(packageInfo.packageName);
        }
        catch (PackageManager.NameNotFoundException e)
        {
          e.printStackTrace();
        }

        plugins.add(new Plugin(String.valueOf(packageInfo.loadLabel(pm)), author, packageInfo.packageName, pluginIcon));
      }
    }
    return plugins;
  }

  @Override
  protected void onPostExecute(final List<Plugin> plugins)
  {
    pluginsFragment.getActivity().runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        pluginsFragment.updatePlugins(plugins);
      }
    });
  }
}
