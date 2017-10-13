---
title: Frequently Asked Questions
category: General
order: 1
toc: true
---

### Application Development

#### Why are @Autorun methods not being re-run when observable properties?

Arez only re-runs `@Autorun` methods if it is told that an observable property that is a dependency of the
`@Autorun` method is changed. Assuming you are using classes annotated with `@ArezComponent` then this means
that the code must:

* mutate the property using the setter method to mark the property as changed. The generated code ultimately calls
  the [Observable.reportChanged()]({% api_url Observable reportChanged() %}) method to mark the property as changed.
* access the property using the getter method.. The generated code ultimately calls the
  [Observable.reportObserved()]({% api_url Observable reportObserved() %}) method to mark the property as observed.
  This will add it as a dependency to the containing `@Autorun` method.

Typically this problem arises when you mutate field directly within the same class. Consider this problematic code
snippet:

```java
  @Observable
  public int getRemainingRides()
  {
    return _remainingRides;
  }

  public void setRemainingRides( int remainingRides )
  {
    _remainingRides = remainingRides;
  }

  @Action
  public void rideTrain()
  {
    _remainingRides = _remainingRides - 1;
  }
```

Compare it to this code that correctly notifies observers that the `remainingRides` property has updated. The only
difference is how the state is mutated within `rideTrain()` method.

```java
  @Observable
  public int getRemainingRides()
  {
    return _remainingRides;
  }

  public void setRemainingRides( int remainingRides )
  {
    _remainingRides = remainingRides;
  }

  @Action
  public void rideTrain()
  {
    setRemainingRides( getRemainingRides() - 1 );
  }
```

### Library Design

#### Why do the change events/notifications not include a description of the change?

In many state management frameworks, notifications of change are accompanied by a description
of change but not so in Arez. For example in Arez the [Observable.reportChanged()]({% api_url Observable reportChanged() %})
method accepts no change description, the [ObservableChangedEvent]({% api_url spy.ObservableChangedEvent %}) class
has no description of a change and there is no way for an [Observer]({% api_url Observer %}) to receive changes.

The reason is that change descriptions seem to be used as an optimization strategy needed in very specific
scenarios. For example, consider the scenario where you are representing an `Employee` as a react component
and you maintain a list of `Employee` instances that are candidates for a particular allowance. Calculating the
applicability of an allowance for a particular `Employee` is an expensive operation. In Arez you are forced to
regenerate the list of allowance candidates each time the set of `Employee` instances changes. In other
systems you could just listen for an event such as `EntityAdded(Employee)`, calculate the applicability for that
specific instance and insert them into the allowance candidates if appropriate. In that scenario the event
listen approach can be better optimized.

However building this in Arez with judicious use of [@Computed]({% api_url annotations.Computed %}) annotated
methods can achieve acceptable performance, at least in our tests of up to ~8000 entities in the original `Employee`
set. It is possible that in the future, change messages will be added back into Arez but will only occur when
it is determined that the implementation and performance costs for other scenarios is worth the tradeoff.

This decision was not made lightly and the original Arez implementation included change events such as
`AtomicChange(FromValue, ToValue)`, `MapAdd(Key, Value)`, `Disposed()`, `UnspecifiedChange()` etc. These events
had to be queued on the `Observer` in the order they were generated and explicitly consumed by the `Observer`
during the reaction phase. This added some complexity to the Arez implementation. The author of Arez has also
previously implemented two other state management frameworks using this technique and considers the complexity
for downstream consumers to be the greatest problem with this strategy.

#### Why is there not a @Memoize annotation for caching method calls?

[Memoization](https://en.wikipedia.org/wiki/Memoization) of expensive method calls seems like a good fit for
the Arez library and yet there is no `@Memoize` annotation that can be applied to `@ArezComponent` annotated
classes. There is no real reason why this feature is lacking other than a lack of time to implement it. An
initial spike implementation created a [ComputedValue]({% api_url ComputedValue %}) instance for each
combination of unique parameters in method and seemed to work effectively. There was some complexity in the
implementation as testing for parameter equality needed to be configurable for each parameter and deactivation
typically triggered a dispose after a delay. Until this feature is implemented, it is reasonable easy to
implemented this with a `@Computed` annotation.