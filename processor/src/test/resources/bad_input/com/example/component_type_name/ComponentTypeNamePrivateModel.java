package com.example.component_type_name;

import arez.annotations.Action;
import arez.annotations.ArezComponent;
import arez.annotations.ComponentTypeNameRef;

@ArezComponent
public abstract class ComponentTypeNamePrivateModel
{
  @Action
  void myAction()
  {
  }

  @ComponentTypeNameRef
  private String getTypeName()
  {
    return null;
  }
}
