package com.greghaskins.spectrum;

interface Parent {

  void focus(Child child);

  boolean isIgnored();

  Parent getParent();

  default boolean isParentIgnored() {
    return getParent().isIgnored() || getParent().isParentIgnored();
  }

  Parent NONE = new Parent() {
    @Override
    public void focus(final Child child) {}

    @Override
    public boolean isIgnored() {
      return false;
    }

    @Override
    public Parent getParent() {
      return null;
    }

    @Override
    public boolean isParentIgnored() {
      return false;
    }
  };
}
