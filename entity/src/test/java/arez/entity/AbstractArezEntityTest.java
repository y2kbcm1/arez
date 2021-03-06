package arez.entity;

import arez.Arez;
import arez.ArezTestUtil;
import arez.Observer;
import arez.ObserverError;
import arez.Procedure;
import arez.SafeFunction;
import arez.SafeProcedure;
import arez.integration.util.SpyEventRecorder;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.json.JSONException;
import org.realityforge.braincheck.BrainCheckTestUtil;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import static org.testng.Assert.*;

@SuppressWarnings( "Duplicates" )
public abstract class AbstractArezEntityTest
{
  private final ArrayList<String> _observerErrors = new ArrayList<>();
  private boolean _ignoreObserverErrors;
  private boolean _printObserverErrors;
  private String _currentMethod;

  final void setIgnoreObserverErrors( final boolean ignoreObserverErrors )
  {
    _ignoreObserverErrors = ignoreObserverErrors;
  }

  final void setPrintObserverErrors( final boolean printObserverErrors )
  {
    _printObserverErrors = printObserverErrors;
  }

  @BeforeMethod
  public void handleTestMethodName( Method method )
    throws Exception
  {
    _currentMethod = method.getName();
    BrainCheckTestUtil.resetConfig( false );
    ArezTestUtil.resetConfig( false );
    _ignoreObserverErrors = false;
    _printObserverErrors = true;
    _observerErrors.clear();
    Arez.context().addObserverErrorHandler( this::onObserverError );
  }

  private void onObserverError( @Nonnull final Observer observer,
                                @Nonnull final ObserverError error,
                                @Nullable final Throwable throwable )
  {
    final String message = "Observer: " + observer.getName() + " Error: " + error + " " + throwable;
    _observerErrors.add( message );
    if ( _printObserverErrors )
    {
      System.out.println( message );
    }
  }

  @AfterMethod
  protected void afterTest()
    throws Exception
  {
    BrainCheckTestUtil.resetConfig( true );
    ArezTestUtil.resetConfig( true );
    if ( !_ignoreObserverErrors && !_observerErrors.isEmpty() )
    {
      fail( "Unexpected Observer Errors: " + _observerErrors.stream().collect( Collectors.joining( "\n" ) ) );
    }
  }

  final void autorun( @Nonnull final Procedure procedure )
  {
    Arez.context().autorun( procedure );
  }

  final void safeAction( @Nonnull final SafeProcedure action )
  {
    Arez.context().safeAction( action );
  }

  final <T> T safeAction( @Nonnull final SafeFunction<T> action )
  {
    return Arez.context().safeAction( action );
  }

  @Nonnull
  final ArrayList<String> getObserverErrors()
  {
    return _observerErrors;
  }

  protected final void assertMatchesFixture( @Nonnull final SpyEventRecorder recorder )
    throws IOException, JSONException
  {
    recorder.assertMatchesFixture( fixtureDir().resolve( getFixtureFilename() ), outputFiles() );
  }

  protected static void observeADependency()
  {
    Arez.context().observable().reportObserved();
  }

  @Nonnull
  private String getFixtureFilename()
  {
    return getClass().getName().replace( ".", "/" ) + "." + _currentMethod + ".json";
  }

  @Nonnull
  private Path fixtureDir()
  {
    final String fixtureDir = System.getProperty( "arez.entity_fixture_dir" );
    assertNotNull( fixtureDir,
                   "Expected System.getProperty( \"arez.entity_fixture_dir\" ) to return fixture directory if arez.output_fixture_data=true" );

    return new File( fixtureDir ).toPath();
  }

  private boolean outputFiles()
  {
    return System.getProperty( "arez.output_fixture_data", "false" ).equals( "true" );
  }

}
