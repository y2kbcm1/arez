package com.example.repository;

import javax.annotation.Nonnull;
import org.realityforge.arez.annotations.ArezComponent;
import org.realityforge.arez.annotations.Injectible;
import org.realityforge.arez.annotations.Observable;
import org.realityforge.arez.annotations.Repository;

@Repository( dagger = Injectible.FALSE )
@ArezComponent
public class DaggerDisabledRepository
{
  @Nonnull
  private String _name;

  DaggerDisabledRepository( @Nonnull final String name )
  {
    _name = name;
  }

  @Observable
  @Nonnull
  public String getName()
  {
    return _name;
  }

  public void setName( @Nonnull final String name )
  {
    _name = name;
  }
}