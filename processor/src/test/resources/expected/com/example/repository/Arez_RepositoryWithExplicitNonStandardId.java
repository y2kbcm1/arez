package com.example.repository;

import arez.Arez;
import arez.ArezContext;
import arez.Component;
import arez.Disposable;
import arez.Observable;
import arez.component.Identifiable;
import java.util.Objects;
import javax.annotation.Generated;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.realityforge.braincheck.Guards;

@Generated("arez.processor.ArezProcessor")
final class Arez_RepositoryWithExplicitNonStandardId extends RepositoryWithExplicitNonStandardId implements Disposable, Identifiable<Integer> {
  private byte $$arezi$$_state;

  @Nullable
  private final ArezContext $$arezi$$_context;

  private final Component $$arezi$$_component;

  private final Observable<Boolean> $$arezi$$_disposedObservable;

  @Nonnull
  private final Observable<String> $$arez$$_name;

  Arez_RepositoryWithExplicitNonStandardId(@Nonnull final String packageName,
      @Nonnull final String name) {
    super(packageName,name);
    this.$$arezi$$_context = Arez.areZonesEnabled() ? Arez.context() : null;
    this.$$arezi$$_state = 1;
    this.$$arezi$$_component = Arez.areNativeComponentsEnabled() ? $$arezi$$_context().createComponent( "RepositoryWithExplicitNonStandardId", object_id(), $$arezi$$_name(), null, null ) : null;
    this.$$arezi$$_disposedObservable = $$arezi$$_context().createObservable( Arez.areNativeComponentsEnabled() ? this.$$arezi$$_component : null, Arez.areNamesEnabled() ? $$arezi$$_name() + ".isDisposed" : null, Arez.arePropertyIntrospectorsEnabled() ? () -> this.$$arezi$$_state >= 0 : null, null );
    this.$$arez$$_name = $$arezi$$_context().createObservable( Arez.areNativeComponentsEnabled() ? this.$$arezi$$_component : null, Arez.areNamesEnabled() ? $$arezi$$_name() + ".name" : null, Arez.arePropertyIntrospectorsEnabled() ? () -> super.getName() : null, Arez.arePropertyIntrospectorsEnabled() ? v -> super.setName( v ) : null );
    if ( Arez.areNativeComponentsEnabled() ) {
      this.$$arezi$$_component.complete();
    }
    this.$$arezi$$_state = 2;
    this.$$arezi$$_state = 3;
  }

  final ArezContext $$arezi$$_context() {
    if ( Arez.shouldCheckApiInvariants() ) {
      Guards.apiInvariant( () -> this.$$arezi$$_state == 0, () -> "Method invoked on uninitialized component named '" + $$arezi$$_name() + "'" );
    }
    return Arez.areZonesEnabled() ? this.$$arezi$$_context : Arez.context();
  }

  @Override
  @Nonnull
  public final Integer getArezId() {
    return object_id();
  }

  String $$arezi$$_name() {
    if ( Arez.shouldCheckApiInvariants() ) {
      Guards.apiInvariant( () -> this.$$arezi$$_state == 0, () -> "Method invoked on uninitialized component named '" + $$arezi$$_name() + "'" );
    }
    return "RepositoryWithExplicitNonStandardId." + object_id();
  }

  @Override
  public boolean isDisposed() {
    if ( $$arezi$$_context().isTransactionActive() && !this.$$arezi$$_disposedObservable.isDisposed() )  {
      this.$$arezi$$_disposedObservable.reportObserved();
    }
    return this.$$arezi$$_state < 0;
  }

  @Override
  public void dispose() {
    if ( !isDisposed() ) {
      this.$$arezi$$_state = -2;
      if ( Arez.areNativeComponentsEnabled() ) {
        this.$$arezi$$_component.dispose();
      } else {
        $$arezi$$_context().safeAction( Arez.areNamesEnabled() ? $$arezi$$_name() + ".dispose" : null, () -> { {
          this.$$arezi$$_disposedObservable.dispose();
          this.$$arez$$_name.dispose();
        } } );
      }
      this.$$arezi$$_state = -1;
    }
  }

  @Nonnull
  @Override
  public String getName() {
    if ( Arez.shouldCheckApiInvariants() ) {
      Guards.apiInvariant( () -> this.$$arezi$$_state >= 2, () -> "Method invoked on dispos" + (this.$$arezi$$_state == -2 ? "ing" : "ed" ) + " component named '" + $$arezi$$_name() + "'" );
    }
    this.$$arez$$_name.reportObserved();
    return super.getName();
  }

  @Override
  public void setName(@Nonnull final String name) {
    if ( Arez.shouldCheckApiInvariants() ) {
      Guards.apiInvariant( () -> this.$$arezi$$_state >= 2, () -> "Method invoked on dispos" + (this.$$arezi$$_state == -2 ? "ing" : "ed" ) + " component named '" + $$arezi$$_name() + "'" );
    }
    if ( !Objects.equals( name, super.getName() ) ) {
      this.$$arez$$_name.preReportChanged();
      super.setName(name);
      this.$$arez$$_name.reportChanged();
    }
  }

  @Override
  public final int hashCode() {
    return Integer.hashCode( object_id() );
  }

  @Override
  public final boolean equals(final Object o) {
    if ( this == o ) {
      return true;
    } else if ( null == o || !(o instanceof Arez_RepositoryWithExplicitNonStandardId) ) {
      return false;
    } else {
      final Arez_RepositoryWithExplicitNonStandardId that = (Arez_RepositoryWithExplicitNonStandardId) o;;
      return object_id() == that.object_id();
    }
  }

  @Override
  public final String toString() {
    if ( Arez.areNamesEnabled() ) {
      return "ArezComponent[" + $$arezi$$_name() + "]";
    } else {
      return super.toString();
    }
  }
}