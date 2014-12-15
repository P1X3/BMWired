package com.osovskiy.bmwired.fragment.plugins;

import android.app.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.osovskiy.bmwired.R;

import java.util.ArrayList;
import java.util.List;

public class PluginsFragment extends ListFragment
{
  private static final String TAG = PluginsFragment.class.getSimpleName();

  List<Plugin> pluginsList = new ArrayList<>();
  PluginListAdapter adapter;

  public PluginsFragment()
  {
    Log.d(TAG, "constructor");
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
    new PluginLoader(this).execute();
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
        new PluginLoader(this).execute();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  protected void updatePlugins(List<Plugin> plugins)
  {
    pluginsList = plugins;
    adapter.notifyDataSetChanged();
    Toast.makeText(getActivity(), plugins.size() + " plugin(s) found", Toast.LENGTH_SHORT).show();
  }
}
