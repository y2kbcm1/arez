package com.example.inject;

import org.realityforge.arez.annotations.Action;
import org.realityforge.arez.annotations.ArezComponent;

@ArezComponent( inject = true )
public class DefaultCtorModel
{
  @Action
  public void myActionStuff()
  {
  }
}