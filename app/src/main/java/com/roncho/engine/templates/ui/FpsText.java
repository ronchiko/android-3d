package com.roncho.engine.templates.ui;

import com.roncho.engine.helpers.Time;
import com.roncho.engine.structs.primitive.Vector2;

/** Shows FPS */
public class FpsText extends UiText {

    private class RecordComponent extends Component {
        @Override
        public void onUpdate() {
            text.setText("Fps: " + (int)(1 / Time.deltaTime()));
        }
        @Override
        public void onStart() {

        }
        @Override
        public void onDestroy() {

        }
    }

    public FpsText(String font, Vector2 position) {
        super(font, "");

        enable(F_DONT_CALCULATE_SIZE);

        addComponent(new RecordComponent());

        transform.position = position;
    }
}
