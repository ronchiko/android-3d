package com.roncho.engine.structs.events;

import java.util.ArrayList;

public class VoidEvent {

    public interface IVoidFunction { void invoke(); }

    private final ArrayList<IVoidFunction> listeners;

    public VoidEvent(){
        listeners = new ArrayList<>();
    }

    public void add(IVoidFunction listener){
        listeners.add(listener);
    }

    public void remove(IVoidFunction listener){
        listeners.remove(listener);
    }

    public void invoke(){
        for(IVoidFunction listener : listeners) listener.invoke();
    }

    public void clear() {
        listeners.clear();
    }
}
