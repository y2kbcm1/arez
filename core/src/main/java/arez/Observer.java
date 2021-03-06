package arez;

import arez.spy.ComputeCompletedEvent;
import arez.spy.ComputeStartedEvent;
import arez.spy.ObserverDisposedEvent;
import arez.spy.ReactionCompletedEvent;
import arez.spy.ReactionStartedEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import static org.realityforge.braincheck.Guards.*;

/**
 * A node within Arez that is notified of changes in 0 or more Observables.
 */
public final class Observer
  extends Node
{
  /**
   * The component that this observer is contained within.
   * This should only be set if {@link Arez#areNativeComponentsEnabled()} is true but may also be null if
   * the observer is a "top-level" observer.
   */
  @Nullable
  private final Component _component;
  /**
   * The reference to the ComputedValue if this observer is a derivation.
   */
  @Nullable
  private final ComputedValue<?> _computedValue;
  /**
   * Hook action called when the Observer changes from the INACTIVE state to any other state.
   */
  @Nullable
  private Procedure _onActivate;
  /**
   * Hook action called when the Observer changes to the INACTIVE state to any other state.
   */
  @Nullable
  private Procedure _onDeactivate;
  /**
   * Hook action called when the Observer changes from the UP_TO_DATE state to STALE or POSSIBLY_STALE.
   */
  @Nullable
  private Procedure _onStale;
  /**
   * Hook action called when the Observer is disposed.
   */
  @Nullable
  private Procedure _onDispose;
  /**
   * The stalest state of the associated observables that are also derivations.
   */
  @Nonnull
  private ObserverState _state = ObserverState.INACTIVE;
  /**
   * The observables that this observer receives notifications from.
   * These are the dependencies within the dependency graph and will
   * typically correspond to the observables that were accessed in last
   * transaction that this observer was tracking.
   *
   * This list should contain no duplicates.
   */
  @Nonnull
  private ArrayList<Observable<?>> _dependencies = new ArrayList<>();
  /**
   * Flag indicating whether this observer has been scheduled.
   * Should always be false unless _reaction is non-null.
   */
  private boolean _scheduled;
  /**
   * The transaction mode in which the observer executes.
   */
  @Nullable
  private final TransactionMode _mode;
  /**
   * The code responsible for responding to changes if any.
   */
  @Nonnull
  private final Reaction _reaction;
  /**
   * The memoized observable value created by observer if any.
   */
  @Nullable
  private final Observable<?> _derivedValue;
  /**
   * The priority of the observer.
   */
  @Nonnull
  private final Priority _priority;
  /**
   * Flag set to true if Observer can be passed as tracker into one of the transaction methods.
   */
  private final boolean _canTrackExplicitly;
  /**
   * Flag set to true if the Observer is allowed to observe {@link ComputedValue} instances with a lower priority.
   */
  private final boolean _observeLowerPriorityDependencies;
  /**
   * Flag set to true after Observer has been disposed.
   */
  private boolean _disposed;
  /**
   * Flag set to true when disposing observer.
   */
  private boolean _disposing;

  Observer( @Nullable final ArezContext context,
            @Nullable final Component component,
            @Nullable final String name,
            @Nullable final ComputedValue<?> computedValue,
            @Nullable final TransactionMode mode,
            @Nonnull final Reaction reaction,
            @Nonnull final Priority priority,
            final boolean canTrackExplicitly,
            final boolean observeLowerPriorityDependencies )
  {
    super( context, name );
    if ( Arez.shouldCheckInvariants() )
    {
      if ( Arez.shouldEnforceTransactionType() )
      {
        if ( TransactionMode.READ_WRITE_OWNED == mode )
        {
          invariant( () -> null != computedValue,
                     () -> "Arez-0079: Attempted to construct an observer named '" + getName() + "' with " +
                           "READ_WRITE_OWNED transaction mode but no ComputedValue." );
          assert null != computedValue;
          invariant( () -> !canTrackExplicitly,
                     () -> "Arez-0080: Attempted to construct an ComputedValue '" + getName() + "' that could " +
                           "track explicitly." );
        }
        else if ( null != computedValue )
        {
          fail( () -> "Arez-0081: Attempted to construct an observer named '" + getName() + "' with " + mode +
                      " transaction mode and a ComputedValue." );
        }
      }
      else
      {
        invariant( () -> null == mode,
                   () -> "Arez-0082: Observer named '" + getName() + "' specified mode '" + mode + "' when " +
                         "Arez.enforceTransactionType() is false." );
        assert null == mode;
      }
      invariant( () -> Arez.areNativeComponentsEnabled() || null == component,
                 () -> "Arez-0083: Observer named '" + getName() + "' has component specified but " +
                       "Arez.areNativeComponentsEnabled() is false." );
      invariant( () -> Priority.LOWEST != priority || !observeLowerPriorityDependencies,
                 () -> "Arez-0184: Observer named '" + getName() + "' has LOWEST priority but has passed " +
                       "observeLowerPriorityDependencies = true which should be false as no lower priority." );
    }
    assert null == computedValue || !Arez.areNamesEnabled() || computedValue.getName().equals( name );
    _component = Arez.areNativeComponentsEnabled() ? component : null;
    _computedValue = computedValue;
    _mode = Arez.shouldEnforceTransactionType() ? Objects.requireNonNull( mode ) : null;
    _reaction = Objects.requireNonNull( reaction );
    _priority = Objects.requireNonNull( priority );
    _canTrackExplicitly = canTrackExplicitly;
    _observeLowerPriorityDependencies = Arez.shouldCheckInvariants() && observeLowerPriorityDependencies;
    if ( null != _computedValue )
    {
      _derivedValue =
        new Observable<>( context,
                          null,
                          name,
                          this,
                          Arez.arePropertyIntrospectorsEnabled() ? _computedValue::getValue : null,
                          null );
    }
    else
    {
      _derivedValue = null;
      if ( null != _component )
      {
        _component.addObserver( this );
      }
      else if ( Arez.areRegistriesEnabled() )
      {
        getContext().registerObserver( this );
      }
    }
  }

  @Nonnull
  Observable<?> getDerivedValue()
  {
    if ( Arez.shouldCheckInvariants() )
    {
      invariant( this::isNotDisposed,
                 () -> "Arez-0084: Attempted to invoke getDerivedValue on disposed observer " +
                       "named '" + getName() + "'." );
      invariant( this::isDerivation,
                 () -> "Arez-0085: Attempted to invoke getDerivedValue on observer named '" +
                       getName() + "' when is not a computed observer." );
    }
    assert null != _derivedValue;
    return _derivedValue;
  }

  @Nonnull
  Priority getPriority()
  {
    return _priority;
  }

  boolean canTrackExplicitly()
  {
    return _canTrackExplicitly;
  }

  boolean canObserveLowerPriorityDependencies()
  {
    return _observeLowerPriorityDependencies;
  }

  boolean isDerivation()
  {
    /*
     * We do not use "null != _derivedValue" as it is called from constructor of observable
     * prior to assigning it to _derivedValue. However it is only called if Arez.shouldEnforceTransactionType()
     * so we can use "null != _derivedValue" when not enabled.
     */
    return Arez.shouldEnforceTransactionType() ? TransactionMode.READ_WRITE_OWNED == getMode() : null != _derivedValue;
  }

  /**
   * Make the Observer INACTIVE and release any resources associated with observer.
   * The applications should NOT interact with the Observer after it has been disposed.
   */
  @Override
  public void dispose()
  {
    if ( !_disposed && !_disposing )
    {
      _disposing = true;
      runHook( getOnDispose(), ObserverError.ON_DISPOSE_ERROR );
      getContext().safeAction( Arez.areNamesEnabled() ? getName() + ".dispose" : null,
                               true,
                               false,
                               this::performDispose );
      if ( !isDerivation() )
      {
        if ( willPropagateSpyEvents() )
        {
          reportSpyEvent( new ObserverDisposedEvent( new ObserverInfoImpl( getContext().getSpy(), this ) ) );
        }
        if ( null != _component )
        {
          _component.removeObserver( this );
        }
        else if ( Arez.areRegistriesEnabled() )
        {
          getContext().deregisterObserver( this );
        }
      }
      if ( null != _computedValue )
      {
        _computedValue.dispose();
      }
      if ( null != _derivedValue )
      {
        _derivedValue.dispose();
      }
      _disposing = false;
    }
  }

  private void performDispose()
  {
    getContext().getTransaction().reportDispose( this );
    markDependenciesLeastStaleObserverAsUpToDate();
    clearDependencies();
    setDisposed( true );
    setState( ObserverState.INACTIVE );
  }

  void setDisposed( final boolean disposed )
  {
    _disposed = disposed;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDisposed()
  {
    return _disposed;
  }

  /**
   * Return true during invocation of dispose, false otherwise.
   *
   * @return true during invocation of dispose, false otherwise.
   */
  boolean isDisposing()
  {
    return _disposing;
  }

  /**
   * Return the state of the observer.
   *
   * @return the state of the observer.
   */
  @Nonnull
  ObserverState getState()
  {
    return _state;
  }

  /**
   * Return the transaction mode in which the observer executes.
   *
   * @return the transaction mode in which the observer executes.
   */
  @Nullable
  TransactionMode getMode()
  {
    assert Arez.shouldEnforceTransactionType();
    return _mode;
  }

  /**
   * Return the reaction.
   *
   * @return the reaction.
   */
  @Nonnull
  Reaction getReaction()
  {
    return _reaction;
  }

  /**
   * Return true if the observer is active.
   * Being "active" means that the state of the observer is not {@link ObserverState#INACTIVE}.
   *
   * <p>An inactive observer has no dependencies and depending on the type of observer may
   * have other consequences. i.e. An inactive observer will never be scheduled even if it has a
   * reaction.</p>
   *
   * @return true if the Observer is active.
   */
  boolean isActive()
  {
    return ObserverState.INACTIVE != getState();
  }

  /**
   * Return true if the observer is not active.
   * The inverse of {@link #isActive()}
   *
   * @return true if the Observer is inactive.
   */
  boolean isInactive()
  {
    return !isActive();
  }

  /**
   * Set the state of the observer.
   * Call the hook actions for relevant state change.
   * This is equivalent to passing true in <code>schedule</code> parameter to {@link #setState(ObserverState, boolean)}
   *
   * @param state the new state of the observer.
   */
  void setState( @Nonnull final ObserverState state )
  {
    setState( state, true );
  }

  /**
   * Set the state of the observer.
   * Call the hook actions for relevant state change.
   *
   * @param state    the new state of the observer.
   * @param schedule true if a state transition can cause observer to reschedule, false otherwise.
   */
  void setState( @Nonnull final ObserverState state, final boolean schedule )
  {
    if ( Arez.shouldCheckInvariants() )
    {
      invariant( () -> getContext().isTransactionActive(),
                 () -> "Arez-0086: Attempt to invoke setState on observer named '" + getName() + "' when there is " +
                       "no active transaction." );
      invariantState();
    }
    if ( !state.equals( _state ) )
    {
      final ObserverState originalState = _state;
      _state = state;
      if ( null == _derivedValue && ObserverState.STALE == state )
      {
        runHook( getOnStale(), ObserverError.ON_STALE_ERROR );
        if ( schedule )
        {
          schedule();
        }
      }
      else if ( null != _derivedValue &&
                ObserverState.UP_TO_DATE == originalState &&
                ( ObserverState.STALE == state || ObserverState.POSSIBLY_STALE == state ) )
      {
        // Have to check _derivedValue here as isDerivation() will be true
        // during construction, prior to _derivedValue being set.
        _derivedValue.reportPossiblyChanged();
        runHook( getOnStale(), ObserverError.ON_STALE_ERROR );
        if ( schedule )
        {
          schedule();
        }
      }
      else if ( ObserverState.INACTIVE == _state )
      {
        runHook( getOnDeactivate(), ObserverError.ON_DEACTIVATE_ERROR );
        if ( isDerivation() )
        {
          getComputedValue().setValue( null );
        }
        clearDependencies();
      }
      else if ( ObserverState.INACTIVE == originalState )
      {
        if ( Arez.shouldCheckInvariants() )
        {
          invariant( this::isNotDisposed,
                     () -> "Arez-0087: Attempted to activate disposed observer named '" + getName() + "'." );
        }
        runHook( getOnActivate(), ObserverError.ON_ACTIVATE_ERROR );
      }
      if ( Arez.shouldCheckInvariants() )
      {
        invariantState();
      }
    }
  }

  /**
   * Run the supplied hook if non null.
   *
   * @param hook the hook to run.
   */
  void runHook( @Nullable final Procedure hook, @Nonnull final ObserverError error )
  {
    if ( null != hook )
    {
      try
      {
        hook.call();
      }
      catch ( final Throwable t )
      {
        getContext().reportObserverError( this, error, t );
      }
    }
  }

  /**
   * Set the onActivate hook.
   *
   * @param onActivate the hook.
   */
  void setOnActivate( @Nullable final Procedure onActivate )
  {
    _onActivate = onActivate;
  }

  /**
   * Return the onActivate hook.
   *
   * @return the onActivate hook.
   */
  @Nullable
  Procedure getOnActivate()
  {
    return _onActivate;
  }

  /**
   * Set the onDeactivate hook.
   *
   * @param onDeactivate the hook.
   */
  void setOnDeactivate( @Nullable final Procedure onDeactivate )
  {
    _onDeactivate = onDeactivate;
  }

  /**
   * Return the onDeactivate hook.
   *
   * @return the onDeactivate hook.
   */
  @Nullable
  Procedure getOnDeactivate()
  {
    return _onDeactivate;
  }

  /**
   * Set the onStale hook.
   *
   * @param onStale the hook.
   */
  void setOnStale( @Nullable final Procedure onStale )
  {
    _onStale = onStale;
  }

  /**
   * Return the onStale hook.
   *
   * @return the onStale hook.
   */
  @Nullable
  Procedure getOnStale()
  {
    return _onStale;
  }

  /**
   * Set the onDispose hook.
   *
   * @param onDispose the hook.
   */
  void setOnDispose( @Nullable final Procedure onDispose )
  {
    _onDispose = onDispose;
  }

  /**
   * Return the onDispose hook.
   *
   * @return the onDispose hook.
   */
  @Nullable
  Procedure getOnDispose()
  {
    return _onDispose;
  }

  /**
   * Remove all dependencies, removing this observer from all dependencies in the process.
   */
  void clearDependencies()
  {
    getDependencies().forEach( dependency -> {
      dependency.removeObserver( this );
      if ( !dependency.hasObservers() )
      {
        dependency.setLeastStaleObserverState( ObserverState.UP_TO_DATE );
      }
    } );
    getDependencies().clear();
  }

  /**
   * Return true if this observer has a pending reaction.
   *
   * @return true if this observer has a pending reaction.
   */
  boolean isScheduled()
  {
    return _scheduled;
  }

  /**
   * Clear the scheduled flag. This is called when the observer's reaction is executed so it can be scheduled again.
   */
  void clearScheduledFlag()
  {
    _scheduled = false;
  }

  /**
   * Set the scheduled flag. This is called when the observer is schedule so it can not be scheduled again until it has run.
   */
  void setScheduledFlag()
  {
    _scheduled = true;
  }

  /**
   * Schedule this observer if it does not already have a reaction pending.
   */
  void schedule()
  {
    if ( isNotDisposed() )
    {
      if ( Arez.shouldCheckInvariants() )
      {
        invariant( this::isActive,
                   () -> "Arez-0088: Observer named '" + getName() + "' is not active but an attempt has been made " +
                         "to schedule observer." );
      }
      if ( !isScheduled() )
      {
        getContext().scheduleReaction( this );
      }
    }
  }

  /**
   * Run the reaction in a transaction with the name and mode defined
   * by the observer. If the reaction throws an exception, the exception is reported
   * to the context global ObserverErrorHandlers
   */
  void invokeReaction()
  {
    if ( isNotDisposed() )
    {
      final long start;
      if ( willPropagateSpyEvents() )
      {
        start = System.currentTimeMillis();
        if ( isDerivation() )
        {
          reportSpyEvent( new ComputeStartedEvent( new ComputedValueInfoImpl( getSpy(), getComputedValue() ) ) );
        }
        else
        {
          reportSpyEvent( new ReactionStartedEvent( new ObserverInfoImpl( getSpy(), this ) ) );
        }
      }
      else
      {
        start = 0;
      }
      try
      {
        // ComputedValues may have calculated their values and thus be up to date so no need to recalculate.
        if ( ObserverState.UP_TO_DATE != getState() )
        {
          getReaction().react( this );
        }
      }
      catch ( final Throwable t )
      {
        getContext().reportObserverError( this, ObserverError.REACTION_ERROR, t );
      }
      if ( willPropagateSpyEvents() )
      {
        final long duration = System.currentTimeMillis() - start;
        if ( isDerivation() )
        {
          reportSpyEvent( new ComputeCompletedEvent( new ComputedValueInfoImpl( getSpy(), getComputedValue() ),
                                                     duration ) );
        }
        else
        {
          reportSpyEvent( new ReactionCompletedEvent( new ObserverInfoImpl( getSpy(), this ), duration ) );
        }
      }
    }
  }

  /**
   * Utility to mark all dependencies least stale observer as up to date.
   * Used when the Observer is determined to be up todate.
   */
  void markDependenciesLeastStaleObserverAsUpToDate()
  {
    for ( final Observable dependency : getDependencies() )
    {
      dependency.setLeastStaleObserverState( ObserverState.UP_TO_DATE );
    }
  }

  /**
   * Determine if any dependency of the Observer has actually changed.
   * If the state is POSSIBLY_STALE then recalculate any ComputedValue dependencies.
   * If any ComputedValue dependencies actually changed then the STALE state will
   * be propagated.
   *
   * <p>By iterating over the dependencies in the same order that they were reported and
   * stopping on the first change, all the recalculations are only called for ComputedValues
   * that will be tracked by derivation. That is because we assume that if the first N
   * dependencies of the derivation doesn't change then the derivation should run the same way
   * up until accessing N-th dependency.</p>
   *
   * @return true if the Observer should be recomputed.
   */
  boolean shouldCompute()
  {
    switch ( getState() )
    {
      case UP_TO_DATE:
        return false;
      case INACTIVE:
      case STALE:
        return true;
      case POSSIBLY_STALE:
      {
        for ( final Observable observable : getDependencies() )
        {
          if ( observable.hasOwner() )
          {
            final Observer owner = observable.getOwner();
            final ComputedValue computedValue = owner.getComputedValue();
            try
            {
              computedValue.get();
            }
            catch ( final Throwable ignored )
            {
            }
            // Call to get() will update this state if ComputedValue changed
            if ( ObserverState.STALE == getState() )
            {
              return true;
            }
          }
        }
      }
    }
    /*
     * This results in POSSIBLY_STALE returning to UP_TO_DATE
     */
    markDependenciesLeastStaleObserverAsUpToDate();
    return false;
  }

  /**
   * Return the dependencies.
   *
   * @return the dependencies.
   */
  @Nonnull
  ArrayList<Observable<?>> getDependencies()
  {
    return _dependencies;
  }

  /**
   * Replace the current set of dependencies with supplied dependencies.
   * This should be the only mechanism via which the dependencies are updated.
   *
   * @param dependencies the new set of dependencies.
   */
  void replaceDependencies( @Nonnull final ArrayList<Observable<?>> dependencies )
  {
    if ( Arez.shouldCheckInvariants() )
    {
      invariantDependenciesUnique( "Pre replaceDependencies" );
    }
    _dependencies = Objects.requireNonNull( dependencies );
    if ( Arez.shouldCheckInvariants() )
    {
      invariantDependenciesUnique( "Post replaceDependencies" );
      invariantDependenciesBackLink( "Post replaceDependencies" );
      invariantDependenciesNotDisposed();
    }
  }

  /**
   * Ensure the dependencies list contain no duplicates.
   * Should be optimized away if invariant checking is disabled.
   *
   * @param context some useful debugging context used in invariant checks.
   */
  void invariantDependenciesUnique( @Nonnull final String context )
  {
    if ( Arez.shouldCheckInvariants() )
    {
      invariant( () -> getDependencies().size() == new HashSet<>( getDependencies() ).size(),
                 () -> "Arez-0089: " + context + ": The set of dependencies in observer named '" +
                       getName() + "' is not unique. Current list: '" +
                       getDependencies().stream().map( Node::getName ).collect( Collectors.toList() ) + "'." );
    }
  }

  /**
   * Ensure all dependencies contain this observer in the list of observers.
   * Should be optimized away if invariant checking is disabled.
   *
   * @param context some useful debugging context used in invariant checks.
   */
  void invariantDependenciesBackLink( @Nonnull final String context )
  {
    if ( Arez.shouldCheckInvariants() )
    {
      getDependencies().forEach( observable ->
                                   invariant( () -> observable.getObservers().contains( this ),
                                              () -> "Arez-0090: " + context + ": Observer named '" + getName() +
                                                    "' has dependency observable named '" + observable.getName() +
                                                    "' which does not contain the observer in the list of " +
                                                    "observers." ) );
      invariantDerivationState();
    }
  }

  /**
   * Ensure all dependencies are not disposed.
   */
  void invariantDependenciesNotDisposed()
  {
    if ( Arez.shouldCheckInvariants() )
    {
      getDependencies().forEach( observable ->
                                   invariant( observable::isNotDisposed,
                                              () -> "Arez-0091: Observer named '" + getName() + "' has dependency " +
                                                    "observable named '" + observable.getName() +
                                                    "' which is disposed." ) );
      invariantDerivationState();
    }
  }

  /**
   * Ensure that state field and other fields of the Observer are consistent.
   */
  void invariantState()
  {
    if ( Arez.shouldCheckInvariants() )
    {
      if ( isInactive() )
      {
        invariant( () -> getDependencies().isEmpty(),
                   () -> "Arez-0092: Observer named '" + getName() + "' is inactive but still has dependencies: " +
                         getDependencies().stream().map( Node::getName ).collect( Collectors.toList() ) + "." );
      }
      if ( isDerivation() && isNotDisposed() )
      {
        invariant( () -> Objects.equals( getDerivedValue().hasOwner() ? getDerivedValue().getOwner() : null, this ),
                   () -> "Arez-0093: Observer named '" + getName() + "' has a derived value that does not " +
                         "link back to observer." );
      }
    }
  }

  void invariantDerivationState()
  {
    if ( Arez.shouldCheckInvariants() )
    {
      if ( isDerivation() && isActive() && isNotDisposed() )
      {
        invariant( () -> !getDerivedValue().getObservers().isEmpty() ||
                         Objects.equals( getContext().getTransaction().getTracker(), this ),
                   () -> "Arez-0094: Observer named '" + getName() + "' is a derivation and active but the " +
                         "derived value has no observers." );
      }
    }
  }

  /**
   * Return the ComputedValue for Observer.
   * This should not be called if observer is not a derivation and will generate an invariant failure
   * if invariants are enabled.
   *
   * @return the associated ComputedValue.
   */
  @Nonnull
  ComputedValue<?> getComputedValue()
  {
    if ( Arez.shouldCheckInvariants() )
    {
      invariant( this::isDerivation,
                 () -> "Arez-0095: Attempted to invoke getComputedValue on observer named '" + getName() + "' when " +
                       "is not a computed observer." );
    }
    assert null != _computedValue;
    return _computedValue;
  }

  @Nullable
  Component getComponent()
  {
    return _component;
  }

  void markAsScheduled()
  {
    _scheduled = true;
  }
}
