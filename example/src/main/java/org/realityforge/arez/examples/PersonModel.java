package org.realityforge.arez.examples;

import javax.annotation.Nonnull;
import org.realityforge.arez.annotations.Computed;
import org.realityforge.arez.annotations.Container;
import org.realityforge.arez.annotations.Observable;

@Container( name = "Person", singleton = true )
public class PersonModel
{
  @Nonnull
  private String _firstName;
  @Nonnull
  private String _lastName;

  public PersonModel( @Nonnull final String firstName, @Nonnull final String lastName )
  {
    _firstName = firstName;
    _lastName = lastName;
  }

  @Observable
  @Nonnull
  public String getFirstName()
  {
    return _firstName;
  }

  @Observable
  public void setFirstName( @Nonnull final String firstName )
  {
    _firstName = firstName;
  }

  @Observable
  @Nonnull
  public String getLastName()
  {
    return _lastName;
  }

  @Observable
  public void setLastName( @Nonnull final String lastName )
  {
    _lastName = lastName;
  }

  @Computed
  @Nonnull
  public String getFullName()
  {
    return getFirstName() + " " + getLastName();
  }
}
