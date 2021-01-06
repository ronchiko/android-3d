package com.roncho.engine;

import com.roncho.engine.android.WorldRenderer;
import com.roncho.engine.gl.Shader;
import com.roncho.engine.gl.objects.UiObject;
import com.roncho.engine.gl.objects.WorldObject;

public class World {

    private final WorldRenderer renderer;

    public World(WorldRenderer renderer){
        this.renderer = renderer;
    }

    public void register(WorldObject object) {
        renderer.register(object, null, null);
    }
    public void register(WorldObject object, Shader vertex, Shader fragment) {
        renderer.register(object, vertex, fragment);
    }
    public void register(UiObject ui){
        renderer.register(ui, null, null);
    }
}
