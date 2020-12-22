package com.roncho.engine.structs.events;

import com.roncho.engine.structs.primitive.Vector2;

import java.util.ArrayList;

public class TouchEvent {

    public interface IFunctionReference { void invoke(Vector2 in); }

    private ArrayList<IFunctionReference> listeners;

    public TouchEvent(){
        listeners = new ArrayList<>();
    }

    public void add(IFunctionReference listener){
        listeners.add(listener);
    }

    public void remove(IFunctionReference listener){
        listeners.remove(listener);
    }

    public void invoke(Vector2 value){
        for(IFunctionReference listener : listeners) listener.invoke(value);
    }

    public void clear() {
        listeners.clear();
    }
}
