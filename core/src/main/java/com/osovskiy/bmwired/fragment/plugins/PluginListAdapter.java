package com.osovskiy.bmwired.fragment.plugins;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.osovskiy.bmwired.R;

import java.util.List;

public class PluginListAdapter extends ArrayAdapter<Plugin>
{
  Context context;
  int resource;
  List<Plugin> objects;

  public PluginListAdapter(Context context, int resource, List<Plugin> objects)
  {
    super(context, resource, objects);
    this.context = context;
    this.resource = resource;
    this.objects = objects;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent)
  {
    if ( convertView == null )
    {
      LayoutInflater inflater = ( (Activity) context ).getLayoutInflater();
      convertView = inflater.inflate(resource, parent, false);
    }

    Plugin plugin = objects.get(position);

    ( (TextView) convertView.findViewById(R.id.textPluginName) ).setText(plugin.getName());
    ( (TextView) convertView.findViewById(R.id.textPluginAuthor) ).setText(plugin.getAuthor());
    ( (TextView) convertView.findViewById(R.id.textPluginPackage) ).setText(plugin.getPackage());

    return convertView;
  }
}
