package org.realityforge.arez.doc.examples.multi_zone;

import org.realityforge.arez.Arez;
import org.realityforge.arez.Zone;

public class Example
{
  public static void main( final String[] args )
  {
    // Create zone
    final Zone zone = Arez.createZone();
    // Activate the newly created zone and suspend the current zone if any
    zone.activate();

    // Create new component and ensure it is bound to current zone
    final MyComponent myComponent = new Arez_MyComponent();

    // Deactivate the zone and restore the previous if any
    zone.deactivate();
  }
}