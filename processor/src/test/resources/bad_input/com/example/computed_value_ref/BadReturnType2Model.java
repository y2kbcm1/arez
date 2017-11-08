package com.example.computed_value_ref;

import javax.annotation.Nonnull;
import org.realityforge.arez.ComputedValue;
import org.realityforge.arez.annotations.ArezComponent;
import org.realityforge.arez.annotations.Computed;
import org.realityforge.arez.annotations.ComputedValueRef;

@ArezComponent
public class BadReturnType2Model
{
  @Computed
  public long getTime()
  {
    return 0;
  }

  @Nonnull
  @ComputedValueRef
  public ComputedValue<?> getTimeComputedValue()
  {
    throw new IllegalStateException();
  }
}