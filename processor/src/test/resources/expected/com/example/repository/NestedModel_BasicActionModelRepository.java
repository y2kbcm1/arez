package com.example.repository;

import arez.annotations.Action;
import arez.annotations.ArezComponent;
import arez.component.AbstractRepository;
import javax.annotation.Generated;
import javax.annotation.Nonnull;
import javax.inject.Singleton;

@Generated("arez.processor.ArezProcessor")
@ArezComponent(
    nameIncludesId = false
)
@Singleton
public abstract class NestedModel_BasicActionModelRepository extends AbstractRepository<Long, NestedModel.BasicActionModel, NestedModel_BasicActionModelRepository> {
  NestedModel_BasicActionModelRepository() {
  }

  @Nonnull
  public static NestedModel_BasicActionModelRepository newRepository() {
    return new Arez_NestedModel_BasicActionModelRepository();
  }

  @Action(
      name = "create"
  )
  @Nonnull
  public NestedModel.BasicActionModel create() {
    final NestedModel_Arez_BasicActionModel entity = new NestedModel_Arez_BasicActionModel();
    entity.$$arez$$_setOnDispose( e -> destroy( e ) );
    registerEntity( entity );
    return entity;
  }

  @Override
  protected void preDisposeEntity(@Nonnull final NestedModel.BasicActionModel entity) {
    ((NestedModel_Arez_BasicActionModel) entity).$$arez$$_setOnDispose( null );
  }
}
