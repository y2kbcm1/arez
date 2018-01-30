package com.example.repository;

import arez.annotations.Action;
import arez.annotations.ArezComponent;
import arez.annotations.Feature;
import arez.component.AbstractRepository;
import javax.annotation.Generated;
import javax.annotation.Nonnull;
import javax.inject.Singleton;

@Generated("arez.processor.ArezProcessor")
@ArezComponent(
    nameIncludesId = false,
    inject = Feature.ENABLE
)
@Singleton
public abstract class InjectEnabledRepositoryRepository extends AbstractRepository<Long, InjectEnabledRepository, InjectEnabledRepositoryRepository> {
  InjectEnabledRepositoryRepository() {
  }

  @Nonnull
  public static InjectEnabledRepositoryRepository newRepository() {
    return new Arez_InjectEnabledRepositoryRepository();
  }

  @Action(
      name = "create_name"
  )
  @Nonnull
  InjectEnabledRepository create(@Nonnull final String name) {
    final Arez_InjectEnabledRepository entity = new Arez_InjectEnabledRepository(name);
    entity.$$arez$$_setOnDispose( e -> destroy( e ) );
    registerEntity( entity );
    return entity;
  }

  @Override
  protected void preDisposeEntity(@Nonnull final InjectEnabledRepository entity) {
    ((Arez_InjectEnabledRepository) entity).$$arez$$_setOnDispose( null );
  }
}
