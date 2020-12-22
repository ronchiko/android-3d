package com.roncho.engine.structs.events;

import java.util.ArrayList;

public class Event<T> {

    public interface IFunctionReference<T> { void invoke(T in); }

    ArrayList<IFunctionReference<T>> listeners;

    public Event(){
        listeners = new ArrayList<>();
    }

    public void add(IFunctionReference<T> listener){
        listeners.add(listener);
    }

    public void remove(IFunctionReference<T> listener){
        listeners.remove(listener);
    }

    public void invoke(T value){
        for(IFunctionReference<T> listener : listeners) listener.invoke(value);
    }

    public void clear() {
        listeners.clear();
    }
}
