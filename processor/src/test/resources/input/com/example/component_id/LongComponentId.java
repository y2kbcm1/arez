package com.example.component_id;

import arez.annotations.ArezComponent;
import arez.annotations.ComponentId;

@ArezComponent( allowEmpty = true )
public abstract class LongComponentId
{
  @ComponentId
  public final long getId()
  {
    return 0;
  }
}
