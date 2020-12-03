package com.roncho.engine.structs;

public abstract class ComponentBase {

    public ComponentBase() {}

    public abstract void onUpdate();
    public abstract void onStart();
    public abstract void onDestroy();
}
