package org.realityforge.arez.processor;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.ExecutableElement;
import org.realityforge.arez.annotations.Computed;
import org.realityforge.arez.annotations.OnActivate;
import org.realityforge.arez.annotations.OnDeactivate;
import org.realityforge.arez.annotations.OnDispose;
import org.realityforge.arez.annotations.OnStale;

/**
 * The class that represents the parsed state of @Computed methods on a @Container annotated class.
 */
final class ComputedDescriptor
{
  @Nonnull
  private final String _name;
  @Nullable
  private ExecutableElement _computed;
  @Nullable
  private ExecutableElement _onActivate;
  @Nullable
  private ExecutableElement _onDeactivate;
  @Nullable
  private ExecutableElement _onStale;
  @Nullable
  private ExecutableElement _onDispose;

  ComputedDescriptor( @Nonnull final String name )
  {
    _name = Objects.requireNonNull( name );
  }

  @Nonnull
  String getName()
  {
    return _name;
  }

  @Nonnull
  ExecutableElement getComputed()
  {
    return Objects.requireNonNull( _computed );
  }

  @Nullable
  ExecutableElement getOnActivate()
  {
    return _onActivate;
  }

  @Nullable
  ExecutableElement getOnDeactivate()
  {
    return _onDeactivate;
  }

  @Nullable
  ExecutableElement getOnStale()
  {
    return _onStale;
  }

  @Nullable
  ExecutableElement getOnDispose()
  {
    return _onDispose;
  }

  void setComputed( @Nonnull final ExecutableElement computed )
    throws ArezProcessorException
  {
    MethodChecks.mustNotBeStatic( Computed.class, computed );
    MethodChecks.mustNotBeAbstract( Computed.class, computed );
    MethodChecks.mustNotBePrivate( Computed.class, computed );
    MethodChecks.mustNotBeFinal( Computed.class, computed );
    MethodChecks.mustNotHaveAnyParameters( Computed.class, computed );
    MethodChecks.mustReturnAValue( Computed.class, computed );
    MethodChecks.mustNotThrowAnyExceptions( Computed.class, computed );

    if ( null != _computed )
    {
      throw new ArezProcessorException( "Method annotated with @Computed specified name " + getName() +
                                        " that duplicates computed defined by method " +
                                        _computed.getSimpleName(), computed );
    }
    else
    {
      _computed = Objects.requireNonNull( computed );
    }
  }

  void setOnActivate( @Nonnull final ExecutableElement onActivate )
    throws ArezProcessorException
  {
    MethodChecks.mustNotBeStatic( OnActivate.class, onActivate );
    MethodChecks.mustNotBeAbstract( OnActivate.class, onActivate );
    MethodChecks.mustNotBePrivate( OnActivate.class, onActivate );
    MethodChecks.mustNotHaveAnyParameters( OnActivate.class, onActivate );
    MethodChecks.mustNotReturnAnyValue( OnActivate.class, onActivate );
    MethodChecks.mustNotThrowAnyExceptions( OnActivate.class, onActivate );

    if ( null != _onActivate )
    {
      throw new ArezProcessorException( "@OnActivate target duplicates existing method named " +
                                        _onActivate.getSimpleName(),
                                        onActivate );
    }
    else
    {
      _onActivate = Objects.requireNonNull( onActivate );
    }
  }

  void setOnDeactivate( @Nonnull final ExecutableElement onDeactivate )
    throws ArezProcessorException
  {
    MethodChecks.mustNotBeStatic( OnDeactivate.class, onDeactivate );
    MethodChecks.mustNotBeAbstract( OnDeactivate.class, onDeactivate );
    MethodChecks.mustNotBePrivate( OnDeactivate.class, onDeactivate );
    MethodChecks.mustNotHaveAnyParameters( OnDeactivate.class, onDeactivate );
    MethodChecks.mustNotReturnAnyValue( OnDeactivate.class, onDeactivate );
    MethodChecks.mustNotThrowAnyExceptions( OnDeactivate.class, onDeactivate );
    if ( null != _onDeactivate )
    {
      throw new ArezProcessorException( "@OnDeactivate target duplicates existing method named " +
                                        _onDeactivate.getSimpleName(),
                                        onDeactivate );
    }
    else
    {
      _onDeactivate = Objects.requireNonNull( onDeactivate );
    }
  }

  void setOnStale( @Nonnull final ExecutableElement onStale )
    throws ArezProcessorException
  {
    MethodChecks.mustNotBeStatic( OnStale.class, onStale );
    MethodChecks.mustNotBeAbstract( OnStale.class, onStale );
    MethodChecks.mustNotBePrivate( OnStale.class, onStale );
    MethodChecks.mustNotHaveAnyParameters( OnStale.class, onStale );
    MethodChecks.mustNotReturnAnyValue( OnStale.class, onStale );
    MethodChecks.mustNotThrowAnyExceptions( OnStale.class, onStale );
    if ( null != _onStale )
    {
      throw new ArezProcessorException( "@OnStale target duplicates existing method named " +
                                        _onStale.getSimpleName(),
                                        onStale );
    }
    else
    {
      _onStale = Objects.requireNonNull( onStale );
    }
  }

  void setOnDispose( @Nonnull final ExecutableElement onDispose )
    throws ArezProcessorException
  {
    MethodChecks.mustNotBeStatic( OnDispose.class, onDispose );
    MethodChecks.mustNotBeAbstract( OnDispose.class, onDispose );
    MethodChecks.mustNotBePrivate( OnDispose.class, onDispose );
    MethodChecks.mustNotHaveAnyParameters( OnDispose.class, onDispose );
    MethodChecks.mustNotReturnAnyValue( OnDispose.class, onDispose );
    MethodChecks.mustNotThrowAnyExceptions( OnDispose.class, onDispose );

    if ( null != _onDispose )
    {
      throw new ArezProcessorException( "@OnDispose target duplicates existing method named " +
                                        _onDispose.getSimpleName(), onDispose );
    }
    else
    {
      _onDispose = Objects.requireNonNull( onDispose );
    }
  }

  void validate()
    throws ArezProcessorException
  {
    if ( null == _computed )
    {
      if ( null != getOnActivate() )
      {
        throw new ArezProcessorException( "@OnActivate exists but there is no corresponding @Computed",
                                          getOnActivate() );
      }
      else if ( null != getOnDeactivate() )
      {
        throw new ArezProcessorException( "@OnDeactivate exists but there is no corresponding @Computed",
                                          getOnDeactivate() );
      }
      else if ( null != getOnDispose() )
      {
        throw new ArezProcessorException( "@OnDispose exists but there is no corresponding @Computed",
                                          getOnDispose() );
      }
      else
      {
        final ExecutableElement onStale = getOnStale();
        assert null != onStale;
        throw new ArezProcessorException( "@OnStale exists but there is no corresponding @Computed", onStale );
      }
    }
  }
}
