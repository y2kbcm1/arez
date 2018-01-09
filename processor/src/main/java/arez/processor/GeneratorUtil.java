package arez.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import javax.annotation.Nonnull;

@SuppressWarnings( "Duplicates" )
final class GeneratorUtil
{
  static final ClassName NONNULL_CLASSNAME = ClassName.get( "javax.annotation", "Nonnull" );
  static final ClassName NULLABLE_CLASSNAME = ClassName.get( "javax.annotation", "Nullable" );
  static final ClassName INJECT_CLASSNAME = ClassName.get( "javax.inject", "Inject" );
  static final ClassName SINGLETON_CLASSNAME = ClassName.get( "javax.inject", "Singleton" );
  static final ClassName DAGGER_MODULE_CLASSNAME = ClassName.get( "dagger", "Module" );
  static final ClassName DAGGER_PROVIDES_CLASSNAME = ClassName.get( "dagger", "Provides" );
  static final ClassName GUARDS_CLASSNAME = ClassName.get( "org.realityforge.braincheck", "Guards" );
  static final ClassName AREZ_CLASSNAME = ClassName.get( "arez", "Arez" );
  static final ClassName AREZ_CONTEXT_CLASSNAME = ClassName.get( "arez", "ArezContext" );
  static final ClassName OBSERVABLE_CLASSNAME = ClassName.get( "arez", "Observable" );
  static final ClassName OBSERVER_CLASSNAME = ClassName.get( "arez", "Observer" );
  static final ClassName COMPUTED_VALUE_CLASSNAME = ClassName.get( "arez", "ComputedValue" );
  static final ClassName DISPOSABLE_CLASSNAME = ClassName.get( "arez", "Disposable" );
  static final ClassName COMPONENT_CLASSNAME = ClassName.get( "arez", "Component" );
  static final ClassName INJECTIBLE_CLASSNAME = ClassName.get( "arez.annotations", "Injectible" );
  static final ClassName NO_RESULT_EXCEPTION_CLASSNAME =
    ClassName.get( "arez.component", "NoResultException" );
  static final ClassName NO_SUCH_ENTITY_EXCEPTION_CLASSNAME =
    ClassName.get( "arez.component", "NoSuchEntityException" );

  static final String FIELD_PREFIX = "$$arez$$_";
  static final String CAUGHT_THROWABLE_NAME = FIELD_PREFIX + "e";
  static final String DISPOSED_FIELD_NAME = FIELD_PREFIX + "disposed";
  static final String DISPOSED_OBSERVABLE_FIELD_NAME = FIELD_PREFIX + "disposedObservable";
  static final String ON_DISPOSE_FIELD_NAME = FIELD_PREFIX + "onDispose";
  static final String SET_ON_DISPOSE_METHOD_NAME = FIELD_PREFIX + "setOnDispose";
  static final String ID_FIELD_NAME = FIELD_PREFIX + "id";
  static final String NAME_METHOD_NAME = FIELD_PREFIX + "name";
  static final String NEXT_ID_FIELD_NAME = FIELD_PREFIX + "nextId";
  static final String CONTEXT_FIELD_NAME = FIELD_PREFIX + "context";
  static final String COMPONENT_FIELD_NAME = FIELD_PREFIX + "component";
  static final String PRE_DISPOSE_METHOD_NAME = FIELD_PREFIX + "preDispose";

  private GeneratorUtil()
  {
  }

  static void generateNotDisposedInvariant( @Nonnull final ComponentDescriptor descriptor,
                                            @Nonnull final MethodSpec.Builder builder )
  {
    builder.addStatement( "$T.invariant( () -> !this.$N, () -> \"Method invoked on invalid " +
                          "component '\" + $N() + \"'\" )",
                          GUARDS_CLASSNAME,
                          DISPOSED_FIELD_NAME,
                          descriptor.getComponentNameMethodName() );
  }
}