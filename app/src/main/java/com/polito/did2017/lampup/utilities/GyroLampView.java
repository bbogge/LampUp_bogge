package com.polito.did2017.lampup.utilities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by marco on 14/06/2018.
 */

public class GyroLampView extends View {

    private Paint fillBrush, strokeBrush, widthFillBrush, widthStrokeBrush, holeBrush;
    private float angle;

    public GyroLampView(Context context) {
        super(context);
        initBrushes();
    }

    public GyroLampView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initBrushes();
    }

    public GyroLampView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initBrushes();
    }

    public void setAngle(float angle) {
        if (angle>=0 && angle<=180 && angle!=this.angle) {
            this.angle = angle;
            invalidate();
        }
    }

    public float getAngle() {
        return this.angle;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w=canvas.getWidth();
        float h=canvas.getHeight();
        float angulation;
        float holeOffsetL;
        float holeOffsetR;
        if(angle<=80) {
            angulation = (angle/160)+0.5f;
            holeOffsetL = (80-angle)/4.5f;
            holeOffsetR = 0;
        }
        else {
            angulation = 1.5f-(angle/160);
            holeOffsetL = 0;
            holeOffsetR = (80-angle)/4.5f;
        }
        float inside_delta = 150;
        float hole_margin = 20;
        float r1 = h*angulation;
        float r2 = r1*2/6;
        float r3 = r1*7/6;
        float r4 = (h-inside_delta-2*hole_margin)*angulation;
        float outer_margin = strokeBrush.getStrokeWidth()/2;
        float offset = (80-angle)/4.5f;

        //width
        canvas.drawOval(new RectF((w-r1)/2+offset, 0+outer_margin, (w+r1)/2+offset, h-outer_margin), widthStrokeBrush);   //outer circle
        canvas.drawOval(new RectF((w-r2)/2+offset, 0+outer_margin, (w+r2)/2+offset, h-outer_margin), widthFillBrush);   //vertical oval
        canvas.drawOval(new RectF((w-r3)/2+offset, inside_delta/2, (w+r3)/2+offset, h-(inside_delta/2)), widthFillBrush);   //horizontal oval

        //frontal view
        canvas.drawOval(new RectF((w-r1)/2, 0+outer_margin, (w+r1)/2, h-outer_margin), strokeBrush);   //outer circle
        canvas.drawOval(new RectF((w-r2)/2, 0+outer_margin, (w+r2)/2, h-outer_margin), fillBrush);   //vertical oval
        canvas.drawOval(new RectF((w-r3)/2, inside_delta/2, (w+r3)/2, h-(inside_delta/2)), fillBrush);   //horizontal oval

        //hole
        canvas.drawOval(new RectF((w-r4)/2, (inside_delta/2)+hole_margin, (w+r4)/2, h-(inside_delta/2)-hole_margin), widthFillBrush);   //hole width
        canvas.drawOval(new RectF((w-r4)/2+holeOffsetL, (inside_delta/2)+hole_margin, (w+r4)/2+holeOffsetR, h-(inside_delta/2)-hole_margin), holeBrush);   //hole
    }

    private void initBrushes() {

        fillBrush = new Paint();
        fillBrush.setColor(0xffb30000);

        strokeBrush = new Paint();
        strokeBrush.setColor(0xffb30000);
        strokeBrush.setStyle(Paint.Style.STROKE);
        strokeBrush.setStrokeWidth(30.0f);

        widthFillBrush = new Paint();
        widthFillBrush.setColor(0xffff0000);

        widthStrokeBrush = new Paint();
        widthStrokeBrush.setColor(0xffff0000);
        widthStrokeBrush.setStyle(Paint.Style.STROKE);
        widthStrokeBrush.setStrokeWidth(30.0f);

        holeBrush = new Paint();
        holeBrush.setColor(0xffffffff);
    }
}
