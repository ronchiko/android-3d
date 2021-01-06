package com.roncho.engine.structs.events;

import android.view.MotionEvent;

import com.roncho.engine.helpers.Screen;
import com.roncho.engine.structs.primitive.d2.Vector2;

public class TouchEvent extends Event<TouchEvent.TouchEventInfo> {

    public static class TouchEventInfo {
        public final MotionEvent event;
        public final Vector2 position;

        public boolean used = false;

        public TouchEventInfo(MotionEvent event){
            this.event = event;
            position = new Vector2(event.getX() / Screen.width() * 2 - 1, event.getY() / Screen.height() * 2 - 1);
        }

        public void use(){
            used = true;
        }
    }
}
