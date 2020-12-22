package com.roncho.engine.templates.ui;

import com.roncho.engine.android.Logger;
import com.roncho.engine.gl.objects.UiObject;
import com.roncho.engine.helpers.Input;
import com.roncho.engine.structs.Texture2D;
import com.roncho.engine.structs.events.TouchEvent;
import com.roncho.engine.structs.events.VoidEvent;
import com.roncho.engine.structs.primitive.Color;
import com.roncho.engine.structs.primitive.Vector2;

public class UiButton extends UiObject {

    public final VoidEvent onClick;     // Called when the button is clicked
    public UiText text;

    public UiButton(Vector2 position){
        texture = Texture2D.load("pane.png");
        tint = Color.white();
        transform.position = position;

        onClick = new VoidEvent();
        onClick.add(() -> Logger.Log("You clicked me boss"));

        makeProgram(vertexShader, fragmentShader);

        Input.onTouch.add(this::touchListener);
    }


    public UiButton(Vector2 position, Vector2 scale){
        this(position);
        transform.scale = scale;
    }

    public UiButton(Vector2 position, Vector2 scale, String text, String font){
        this(position);
        transform.scale = scale;

        UiText uiText = new UiText(font, text);
        uiText.tint = Color.black();
        uiText.transform.scale = new Vector2(1 / scale.x, 1 / scale.y);
        uiText.transform.position = uiText.text.size.scale(-.5f);
        children.add(uiText);

        this.text = uiText;
    }

    public boolean hasText() { return text != null; }

    public void setText(String text){
        if(!hasText()) return;

        this.text.text.setText(text);
        recenterText();
    }

    public void recenterText(){
        if(!hasText()) return;

        text.transform.position = text.text.size.scale(-.5f);
    }

    private void touchListener(TouchEvent.TouchEventInfo info){
        if(info.used) return;

        if(info.position.x >= transform.minX() && info.position.x <= transform.maxX()
                && info.position.y >= transform.minY() && info.position.y <= transform.maxY()) {
            onClick.invoke();
            info.use();
        }
    }
}
