package com.example.autorun;

import arez.annotations.ArezComponent;
import arez.annotations.Autorun;
import arez.annotations.Priority;

@ArezComponent
public abstract class HighPriorityAutorunModel
{
  @Autorun( priority = Priority.HIGH )
  protected void doStuff()
  {
  }
}
