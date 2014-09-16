package com.osovskiy.bmwired.fragment.plugins;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.osovskiy.bmwired.R;
import com.osovskiy.bmwired.lib.Utils;

import java.util.ArrayList;
import java.util.List;

public class PluginsFragment extends ListFragment
{
  List<Plugin> pluginsList = new ArrayList<>();
  PluginListAdapter adapter;

  public PluginsFragment()
  {

  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
  {
    inflater.inflate(R.menu.main_activity_plugins, menu);
  }

  @Override
  public void onAttach(Activity activity)
  {
    super.onAttach(activity);
    new LoadPluginsTask().execute();
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    setHasOptionsMenu(true);

    adapter = new PluginListAdapter(getActivity(), R.layout.list_plugin_item, pluginsList);
    setListAdapter(adapter);

    return super.onCreateView(inflater, container, savedInstanceState);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id)
  {
    super.onListItemClick(l, v, position, id);
    Plugin plugin = pluginsList.get(position);
    PackageManager pm = getActivity().getPackageManager();
    Intent intent = pm.getLaunchIntentForPackage(plugin.getPackage());
    startActivity(intent);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch ( item.getItemId() )
    {
      case R.id.actions_refresh:
        new LoadPluginsTask().execute();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }

  }

  private class LoadPluginsTask extends AsyncTask<Void, Void, Void>
  {
    @Override
    protected Void doInBackground(Void... params)
    {
      PackageManager pm = getActivity().getPackageManager();
      List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

      pluginsList.clear();

      for ( ApplicationInfo packageInfo : packages )
      {
        boolean receiveGranted = (PackageManager.PERMISSION_GRANTED == pm.checkPermission(Utils.PERMISSION_RECEIVE_MESSAGE, packageInfo.packageName));
        boolean sendGranted = (PackageManager.PERMISSION_GRANTED == pm.checkPermission(Utils.PERMISSION_SEND_MESSAGE, packageInfo.packageName));

        if ( receiveGranted || sendGranted )
        {
          String author = "empty";
          if ( packageInfo.metaData != null )
            author = packageInfo.metaData.getString("plugin_author");

          pluginsList.add(new Plugin(String.valueOf(packageInfo.loadLabel(pm)), author, packageInfo.packageName));
        }
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
      adapter.notifyDataSetChanged();
    }
  }
}
