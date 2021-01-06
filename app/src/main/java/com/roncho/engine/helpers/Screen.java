package com.roncho.engine.helpers;

import com.roncho.engine.structs.primitive.d2.Int2;

public final class Screen {

    public static Int2 screen;

    public static int width() { return screen.x; }
    public static int height() { return screen.y; }
}
