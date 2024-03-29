package com.group3.swengandroidapp.XMLRenderer;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.group3.swengandroidapp.AudioPlayer;
import com.group3.swengandroidapp.SImpLeGraphicsModule.GraphicModuleAndroid;

import junit.framework.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Presentation class for parsing into from the PWS
 * Draws the slides and the slide elements when drawnSlide is called
 */
public class Presentation extends XmlElement {
    private List<Slide> slides;
    private Map<String, String> meta;
    private Activity activity;
    private int currentSlide;
    private Handler handler;
    private LinearLayout linearLayout;
    public static boolean listenerEnable = true;

    public Presentation() {
        super(null);
        slides = new ArrayList<>();
        meta = new HashMap<>();

        //Sets default properties for children to inherit if they have no properties set
        setProperty("color","#000000");
        setProperty("fill","#FFFFFF");
        setProperty("font","sans-serif-medium");
        setProperty("textsize","12");
        setProperty("stroke","10");
        this.handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void addChild(XmlElement item) {
        if (item instanceof Slide) {
            addSlide((Slide) item);
        } else if (item instanceof Meta) {
            for (Map.Entry<String, String> e : item.getProperties().entrySet()) {
                addMeta(e.getKey(), e.getValue());
            }
        }
        else {
            super.addChild(item);
        }
    }

    public void addSlide(Slide slide) {
        slides.add(slide);
    }

    public Slide getSlide(int index) {
        return slides.get(index);
    }

    public void addMeta(String k, String v) {
        meta.put(k, v);
    }

    public String getMeta(String k) {
        return meta.get(k);
    }

    @Override
    public void draw(Activity activity) {
        this.activity = activity;
        drawSlide(currentSlide);
    }

    public void restart() {
        currentSlide = 0;
        drawSlide(currentSlide);
    }

    private void drawNextSlide() {
        currentSlide = Math.min(currentSlide + 1, slides.size() - 1);
        drawSlide(currentSlide);
    }

    private void drawPreviousSlide() {
        currentSlide = Math.max(currentSlide - 1, 0);
        drawSlide(currentSlide);
    }

    private void drawSlide(int index) {
        linearLayout = new LinearLayout(activity);
        linearLayout.setOnTouchListener(new OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(activity, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {

                    if (slides.get(index).getAdvert() == null && listenerEnable == true) {
                        Log.d("TEST", "onDoubleTap");
                        
                        AudioPlayer.touchSound();

                        handler.removeCallbacksAndMessages(null);
                        handler.post(Presentation.this::drawPreviousSlide);

                        return super.onDoubleTap(e);
                    }
                    else {
                        listenerEnable = true;
                        Log.d("listenerEnable", "TRUE");
                        return false;
                    }



                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    if (slides.get(index).getAdvert() == null && listenerEnable == true) {
                        AudioPlayer.touchSound();
                        Log.d("TEST", "Raw event: " + e.getAction() + ", (" + e.getRawX() + ", " + e.getRawY() + ")");
                        gestureDetector.onTouchEvent(e);

                        handler.removeCallbacksAndMessages(null);
                        handler.post(Presentation.this::drawNextSlide);
                        return true;
                    }
                    else {
                        listenerEnable = true;
                        Log.d("listenerEnable", "TRUE");
                        return false;
                    }

                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("TEST", "Raw event: " + event.getAction() + ", (" + event.getRawX() + ", " + event.getRawY() + ")");
                gestureDetector.onTouchEvent(event);
                return true;
            }

        });

        slides.get(index).draw(activity);

        if (index + 1 < slides.size()) {
            if (slides.get(index).getProperty("duration") != null) {
                handler.postDelayed(this::drawNextSlide, (Integer.valueOf(slides.get(index).getProperty("duration")) * 1000));
            }
        }
    }

    private void advanceOnTap() {
        handler.removeCallbacksAndMessages(null);
    }

    public LinearLayout getLayout() {
        return linearLayout;
    }
}
