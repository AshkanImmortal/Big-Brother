package com.aetava.bigbrother;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

public class BigBrotherCameraPreview extends SurfaceView {

    public static final String TAG = BigBrotherCameraPreview.class.getSimpleName();

    private static final int INVALID_POINTER_ID = -1;

    private static final int ZOOM_OUT = 0;
    private static final int ZOOM_IN = 1;
    private static final int ZOOM_DELTA = 1;

    private static final int FOCUS_SQR_SIZE = 100;
    private static final int FOCUS_MAX_BOUND = 1000;
    private static final int FOCUS_MIN_BOUND = -FOCUS_MAX_BOUND;
    private Camera camera;

    private float lastTouchX;
    private float lastTouchY;

    // For scaling
    private int maxZoom;
    private int scaleFactor = 1;
    private ScaleGestureDetector scaleDetector;

    // For focus
    private boolean isFocus;
    private Camera.Area focusArea;
    private ArrayList<Camera.Area> focusAreas;

    public BigBrotherCameraPreview(Context context) {
        super(context);
        init(context);
    }

    public BigBrotherCameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BigBrotherCameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        focusArea = new Camera.Area(new Rect(), FOCUS_MAX_BOUND);
        focusAreas = new ArrayList<>();
        focusAreas.add(focusArea);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        setMeasuredDimension(width, height);
    }

    public int getViewWidth() {
        return getWidth();
    }

    public int getViewHeight() {
        return getHeight();
    }

    public void setCamera(Camera camera) {
        this.camera = camera;

        if (camera != null) {
            Camera.Parameters params = camera.getParameters();
            boolean mIsZoomSupported = params.isZoomSupported();
            if (mIsZoomSupported) {
                maxZoom = params.getMaxZoom();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                int mActivePointerId = INVALID_POINTER_ID;
            {
                isFocus = true;

                lastTouchX = event.getX();
                lastTouchY = event.getY();

                mActivePointerId = event.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (isFocus) {
                    handleFocus(camera.getParameters());
                }
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                camera.cancelAutoFocus();
                isFocus = false;
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }
        }

        return true;
    }

    private void handleZoom(Camera.Parameters params) {
        int zoom = params.getZoom();
        if (scaleFactor == ZOOM_IN) {
            if (zoom < maxZoom) zoom += ZOOM_DELTA;
        } else if (scaleFactor == ZOOM_OUT) {
            if (zoom > 0) zoom -= ZOOM_DELTA;
        }
        params.setZoom(zoom);
        camera.setParameters(params);
    }

    private void handleFocus(Camera.Parameters params) {
        float x = lastTouchX;
        float y = lastTouchY;

        if (!setFocusBound(x, y)) return;

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null
                && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            Log.d(TAG, focusAreas.size() + "");
            params.setFocusAreas(focusAreas);
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            camera.setParameters(params);
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    // Callback when the auto focus completes
                }
            });
        }
    }

    private boolean setFocusBound(float x, float y) {
        int left = (int) (x - FOCUS_SQR_SIZE / 2);
        int right = (int) (x + FOCUS_SQR_SIZE / 2);
        int top = (int) (y - FOCUS_SQR_SIZE / 2);
        int bottom = (int) (y + FOCUS_SQR_SIZE / 2);

        if (FOCUS_MIN_BOUND > left || left > FOCUS_MAX_BOUND) return false;
        if (FOCUS_MIN_BOUND > right || right > FOCUS_MAX_BOUND) return false;
        if (FOCUS_MIN_BOUND > top || top > FOCUS_MAX_BOUND) return false;
        if (FOCUS_MIN_BOUND > bottom || bottom > FOCUS_MAX_BOUND) return false;

        focusArea.rect.set(left, top, right, bottom);

        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor = (int) detector.getScaleFactor();
            handleZoom(camera.getParameters());
            return true;
        }
    }
}
