package com.roncho.engine.gl.text;

import android.graphics.Typeface;

public class FontInfo {

    private Typeface font;
    private String emptyChars;

    public FontInfo(Typeface font, String emptyChars){
        this.font = font;
        this.emptyChars = emptyChars;
    }

    public Typeface getFont() { return font; }
    public String getEmptyChars() { return emptyChars; }
}
