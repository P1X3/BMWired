package com.osovskiy.bmwired.fragment.plugins;

public class Plugin
{
  private String name;
  private String author;
  private String pkg;

  public Plugin(String name, String author, String pkg)
  {
    this.name = name;
    this.author = author;
    this.pkg = pkg;
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
}
