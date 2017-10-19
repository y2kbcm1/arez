package com.example.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Generated;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.realityforge.arez.Disposable;
import org.realityforge.arez.Observable;
import org.realityforge.arez.annotations.Action;
import org.realityforge.arez.annotations.ArezComponent;
import org.realityforge.arez.annotations.ObservableRef;
import org.realityforge.arez.annotations.PreDispose;
import org.realityforge.arez.component.NoResultException;
import org.realityforge.braincheck.Guards;

@Generated("org.realityforge.arez.processor.ArezProcessor")
@ArezComponent(
    singleton = true
)
public class RepositoryWithProtectedConstructorRepository implements RepositoryWithProtectedConstructorBaseRepositoryExtension {
  private static final boolean $$arez$$_IMMUTABLE_RESULTS = "true".equals( System.getProperty( "arez.repositories_return_immutables", String.valueOf( System.getProperty( "arez.environment", "production" ).equals( "development" ) ) ) );
  ;

  private final HashMap<Long, RepositoryWithProtectedConstructor> $$arez$$_entities = new HashMap<>();
  ;

  private final Collection<RepositoryWithProtectedConstructor> $$arez$$_entityList = Collections.unmodifiableCollection( $$arez$$_entities.values() );
  ;

  RepositoryWithProtectedConstructorRepository() {
  }

  @Nonnull
  public static RepositoryWithProtectedConstructorRepository newRepository() {
    return new Arez_RepositoryWithProtectedConstructorRepository();
  }

  @Action(
      name = "create_name"
  )
  @Nonnull
  protected RepositoryWithProtectedConstructor create(@Nonnull final String name) {
    final Arez_RepositoryWithProtectedConstructor entity = new Arez_RepositoryWithProtectedConstructor(name);
    $$arez$$_entities.put( entity.$$arez$$_id(), entity );
    getEntitiesObservable().reportChanged();
    return entity;
  }

  @PreDispose
  final void preDispose() {
    $$arez$$_entityList.forEach( e -> Disposable.dispose( e ) );
    $$arez$$_entities.clear();
    getEntitiesObservable().reportChanged();
  }

  public boolean contains(@Nonnull final RepositoryWithProtectedConstructor entity) {
    getEntitiesObservable().reportObserved();
    return entity instanceof Arez_RepositoryWithProtectedConstructor && $$arez$$_entities.containsKey( ((Arez_RepositoryWithProtectedConstructor) entity).$$arez$$_id() );
  }

  @Action
  public void destroy(@Nonnull final RepositoryWithProtectedConstructor entity) {
    assert null != entity;
    if ( entity instanceof Arez_RepositoryWithProtectedConstructor && null != $$arez$$_entities.remove( ((Arez_RepositoryWithProtectedConstructor) entity).$$arez$$_id() ) ) {
      Disposable.dispose( entity );
      getEntitiesObservable().reportChanged();
    } else {
      Guards.fail( () -> "Called destroy() passing an entity that was not in the repository. Entity: " + entity );
    }
  }

  @ObservableRef
  Observable getEntitiesObservable() {
    throw new IllegalStateException();
  }

  /**
   * Return the raw collection of entities in the repository.
   * This collection should not be exposed to the user but may be used be repository extensions when
   * they define custom queries. NOTE: use of this method marks the list as observed.
   */
  @org.realityforge.arez.annotations.Observable(
      expectSetter = false
  )
  @Nonnull
  protected Collection<RepositoryWithProtectedConstructor> entities() {
    return $$arez$$_entityList;
  }

  /**
   * If config option enabled, wrap the specified list in an immutable list and return it.
   * This method should be called by repository extensions when returning list results when not using {@link toList(List)}.
   */
  @Nonnull
  protected final List<RepositoryWithProtectedConstructor> wrap(@Nonnull final List<RepositoryWithProtectedConstructor> list) {
    return $$arez$$_IMMUTABLE_RESULTS ? Collections.unmodifiableList( list ) : list;
  }

  /**
   * Convert specified stream to a list, wrapping as an immutable list if required.
   * This method should be called by repository extensions when returning list results.
   */
  @Nonnull
  protected final List<RepositoryWithProtectedConstructor> toList(@Nonnull final Stream<RepositoryWithProtectedConstructor> stream) {
    return wrap( stream.collect( Collectors.toList() ) );
  }

  @Nonnull
  public final List<RepositoryWithProtectedConstructor> findAll() {
    return toList( entities().stream() );
  }

  @Nonnull
  public final List<RepositoryWithProtectedConstructor> findAll(@Nonnull final Comparator<RepositoryWithProtectedConstructor> sorter) {
    return toList( entities().stream().sorted( sorter ) );
  }

  @Nonnull
  public final List<RepositoryWithProtectedConstructor> findAllByQuery(@Nonnull final Predicate<RepositoryWithProtectedConstructor> query) {
    return toList( entities().stream().filter( query ) );
  }

  @Nonnull
  public final List<RepositoryWithProtectedConstructor> findAllByQuery(@Nonnull final Predicate<RepositoryWithProtectedConstructor> query, @Nonnull final Comparator<RepositoryWithProtectedConstructor> sorter) {
    return toList( entities().stream().filter( query ).sorted( sorter ) );
  }

  @Nullable
  public final RepositoryWithProtectedConstructor findByQuery(@Nonnull final Predicate<RepositoryWithProtectedConstructor> query) {
    return entities().stream().filter( query ).findFirst().orElse( null );
  }

  @Nonnull
  public final RepositoryWithProtectedConstructor getByQuery(@Nonnull final Predicate<RepositoryWithProtectedConstructor> query) {
    final RepositoryWithProtectedConstructor entity = findByQuery( query );
    if ( null == entity ) {
      throw new NoResultException();
    }
    return entity;
  }

  @Override
  @Nonnull
  public final RepositoryWithProtectedConstructorRepository self() {
    return this;
  }
}