package com.example.tracked;

import arez.annotations.ArezComponent;
import arez.annotations.OnDepsChanged;
import arez.annotations.Priority;
import arez.annotations.Track;

@ArezComponent
public abstract class HighPriorityTrackedModel
{
  @Track( priority = Priority.HIGH )
  public void render( final long time, float someOtherParameter )
  {
  }

  @OnDepsChanged
  public void onRenderDepsChanged()
  {
  }
}
