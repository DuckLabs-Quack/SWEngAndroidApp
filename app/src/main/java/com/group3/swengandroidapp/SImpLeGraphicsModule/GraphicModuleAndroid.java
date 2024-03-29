package com.group3.swengandroidapp.SImpLeGraphicsModule;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.LinearLayout;

import com.group3.swengandroidapp.XMLRenderer.CanvasView;
import com.group3.swengandroidapp.XMLRenderer.Shape;
import com.group3.swengandroidapp.XMLRenderer.Slide;
import com.group3.swengandroidapp.XMLRenderer.XmlElement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shape class for drawing in the presentation for Android
 * Changes the contract code to work with Android
 */

public class GraphicModuleAndroid extends Shape{

    private Canvas canvas;
    private CanvasView canvasView;

    public GraphicModuleAndroid(XmlElement parent) {
        super(parent);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public CanvasView getCanvasView() {
        return canvasView;
    }

    public void setCanvasView(CanvasView canvasView) {
        this.canvasView = canvasView;
    }

    private void removeShape() {
        canvasView.removeShape(this);
    }

    @Override
    public void draw(Activity activity) {

        if (parent instanceof Slide) {

            canvasView.addShape(this);

            canvasView.onDraw(canvas);
            canvasView.setBackgroundColor(Color.TRANSPARENT);

            if(canvasView.getParent()==null) {
                LinearLayout layout = ((Slide) parent).getLayout();
                layout.addView(canvasView);
            }

        }

    }

}
