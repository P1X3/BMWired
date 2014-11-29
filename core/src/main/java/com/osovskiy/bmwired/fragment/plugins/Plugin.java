package com.osovskiy.bmwired.fragment.plugins;

import android.graphics.drawable.Drawable;

public class Plugin
{
  private String name;
  private String author;
  private String pkg;
  private Drawable icon;

  public Plugin(String name, String author, String pkg, Drawable icon)
  {
    this.name = name;
    this.author = author;
    this.pkg = pkg;
    this.icon = icon;
  }

  public String getName()
  {
    return name;
  }

  public String getAuthor()
  {
    return author;
  }

  public String getPackage()
  {
    return pkg;
  }

  public Drawable getIcon()
  {
    return icon;
  }
}
