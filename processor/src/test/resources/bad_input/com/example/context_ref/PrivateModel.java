package com.example.context_ref;

import arez.ArezContext;
import arez.annotations.ArezComponent;
import arez.annotations.ContextRef;

@ArezComponent( allowEmpty = true )
abstract class PrivateModel
{
  @ContextRef
  private ArezContext getContext()
  {
    throw new IllegalStateException();
  }
}
