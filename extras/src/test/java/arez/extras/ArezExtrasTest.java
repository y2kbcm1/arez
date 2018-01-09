package arez.extras;

import arez.Arez;
import arez.Disposable;
import arez.Observable;
import arez.SafeFunction;
import arez.SafeProcedure;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.realityforge.guiceyloops.shared.ValueUtil;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ArezExtrasTest
  extends AbstractArezExtrasTest
{
  @Test
  public void when()
    throws Throwable
  {
    final AtomicInteger conditionRun = new AtomicInteger();
    final AtomicInteger effectRun = new AtomicInteger();

    final String name = ValueUtil.randomString();
    final SafeFunction<Boolean> condition = () -> {
      conditionRun.incrementAndGet();
      return false;
    };
    final SafeProcedure procedure = effectRun::incrementAndGet;

    final Disposable node = ArezExtras.when( name, true, condition, procedure );

    assertTrue( node instanceof Watcher );
    final Watcher watcher = (Watcher) node;
    assertEquals( watcher.getName(), name );
    assertEquals( conditionRun.get(), 1 );
    assertEquals( effectRun.get(), 0 );
  }

  @Test
  public void when_minimalParameters()
    throws Throwable
  {
    final Observable observable = Arez.context().createObservable();

    final AtomicBoolean result = new AtomicBoolean();

    final AtomicInteger conditionRun = new AtomicInteger();
    final AtomicInteger effectRun = new AtomicInteger();

    final SafeFunction<Boolean> condition = () -> {
      conditionRun.incrementAndGet();
      observable.reportObserved();
      return result.get();
    };
    final SafeProcedure procedure = effectRun::incrementAndGet;

    final Disposable node = ArezExtras.when( condition, procedure );

    assertTrue( node instanceof Watcher );
    final Watcher watcher = (Watcher) node;
    assertEquals( watcher.getName(), "When@2", "The name has @2 as one other Arez entity created (Observable)" );
    assertEquals( conditionRun.get(), 1 );
    assertEquals( effectRun.get(), 0 );
  }
}