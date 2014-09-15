package com.osovskiy.bmwired.lib;

public class CallbackRegister
{

  public void register(RegistrationType registrationType, Callback callback)
  {

  }

  public void register(RegistrationType registrationType, int specific, Callback callback)
  {

  }

  public enum RegistrationType
  {
    Once,
    Specific,
    Continious
  }

  public interface Callback
  {
    void handleMessage(BusMessage message);
  }
}
