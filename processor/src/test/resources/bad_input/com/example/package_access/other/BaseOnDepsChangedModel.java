package com.example.package_access.other;

import arez.annotations.OnDepsChanged;
import arez.annotations.Track;

public abstract class BaseOnDepsChangedModel
{
  @Track
  public void render( final long time, float someOtherParameter )
  {
  }

  @OnDepsChanged
  void onRenderDepsChanged()
  {
  }
}
