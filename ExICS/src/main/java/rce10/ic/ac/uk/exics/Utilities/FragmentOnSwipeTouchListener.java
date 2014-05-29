package rce10.ic.ac.uk.exics.Utilities;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import rce10.ic.ac.uk.exics.Interfaces.ExICS_Main_Fragment_Interface;

/**
 * Created by Rich on 28/05/2014.
 */
public class FragmentOnSwipeTouchListener implements View.OnTouchListener {
    private static final String TAG = FragmentOnSwipeTouchListener.class.getName();
    private final GestureDetector gestureDetector;
    private final ExICS_Main_Fragment_Interface mCallbacks;

    public FragmentOnSwipeTouchListener(Context context, ExICS_Main_Fragment_Interface callbacks) {
        gestureDetector = new GestureDetector(context, new GestureListener());
        mCallbacks = callbacks;
    }

    public void onSwipeLeft() {
    }

    public void onSwipeRight() {
    }

    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.i(TAG, "OnSwipeTouchListener Fling");
            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (distanceX > 0) mCallbacks.onFragmentSwipedRight();
                else mCallbacks.onFragmentSwipedLeft();
                return true;
            }
            return false;
        }
    }
}
