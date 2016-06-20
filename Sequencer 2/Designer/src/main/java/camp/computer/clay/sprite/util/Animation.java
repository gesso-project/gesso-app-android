package camp.computer.clay.sprite.util;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.mobeta.android.sequencer.R;

import camp.computer.clay.designer.ApplicationView;
import camp.computer.clay.designer.MapView;

public class Animation {

    // Based on: http://stackoverflow.com/questions/10276251/how-to-animate-a-view-with-translate-animation-in-android
    public void moveToPoint (final View view, final Point destinationPoint, int translateDuration)
    {
        FrameLayout root = (FrameLayout) ApplicationView.getApplicationView().findViewById(R.id.application_view);
        DisplayMetrics dm = new DisplayMetrics();
        ApplicationView.getApplicationView().getWindowManager().getDefaultDisplay().getMetrics( dm );
        int statusBarOffset = dm.heightPixels - root.getMeasuredHeight();

        int originalPos[] = new int[2];
        view.getLocationOnScreen( originalPos );

        /*
        int xDest = dm.widthPixels/2;
        xDest -= (view.getMeasuredWidth()/2);
        int yDest = dm.heightPixels/2 - (view.getMeasuredHeight()/2) - statusBarOffset;
        */

        int xDest = destinationPoint.x;
        int yDest = destinationPoint.y;


        final int amountToMoveRight = xDest - originalPos[0] - (int) (view.getWidth() / 2.0f);
        final int amountToMoveDown = yDest - originalPos[1] - (int) (view.getHeight() / 2.0f);

        TranslateAnimation animation = new TranslateAnimation(0, amountToMoveRight, 0, amountToMoveDown);
        animation.setDuration(translateDuration);
        // animation.setFillAfter(true);

        animation.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            @Override
            public void onAnimationStart(android.view.animation.Animation animation) {

            }

            @Override
            public void onAnimationEnd(android.view.animation.Animation animation) {
//                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
//                params.topMargin += amountToMoveDown;
//                params.leftMargin += amountToMoveRight;
//                view.setLayoutParams(params);

                // Get button holder
                RelativeLayout relativeLayout = (RelativeLayout) ApplicationView.getApplicationView().findViewById(R.id.context_button_holder);

                // Get screen width and height of the device
                DisplayMetrics metrics = new DisplayMetrics();
                ApplicationView.getApplicationView().getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int screenWidth = metrics.widthPixels;
                int screenHeight = metrics.heightPixels;

                // Get button width and height
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams();
                int buttonWidth = relativeLayout.getWidth();
                int buttonHeight = relativeLayout.getHeight();

                // Reposition button
//                params.rightMargin = screenWidth - (int) event.getRawX() - (int) (buttonWidth / 2.0f);
//                params.bottomMargin = screenHeight - (int) event.getRawY() - (int) (buttonHeight / 2.0f);

                params.bottomMargin = screenWidth - (int) destinationPoint.x - (int) (buttonWidth / 2.0f); // amountToMoveDown;
                params.rightMargin = screenHeight - (int) destinationPoint.y - (int) (buttonHeight / 2.0f); // amountToMoveRight;
                view.setLayoutParams(params);

//                relativeLayout.requestLayout();
//                relativeLayout.invalidate();
            }

            @Override
            public void onAnimationRepeat(android.view.animation.Animation animation) {

            }
        });


        view.startAnimation (animation);
    }

    public interface OnScaleCallback {
        public void onScale (float currentScale);
    }

    public static void scaleValue (final float startScale, final float targetScale, int duration, final OnScaleCallback onScaleCallback) {
//        FrameLayout root = (FrameLayout) ApplicationView.getApplicationView().findViewById(R.id.application_view);
//        DisplayMetrics dm = new DisplayMetrics();
//        ApplicationView.getApplicationView().getWindowManager().getDefaultDisplay().getMetrics( dm );
//        int statusBarOffset = dm.heightPixels - root.getMeasuredHeight();

//        int originalPos[] = new int[2];
//        view.getLocationOnScreen( originalPos );

        /*
        int xDest = dm.widthPixels/2;
        xDest -= (view.getMeasuredWidth()/2);
        int yDest = dm.heightPixels/2 - (view.getMeasuredHeight()/2) - statusBarOffset;
        */

//        int xDest = targetScale.x;
//        int yDest = targetScale.y;

//        float startScale = ((MapView) view).getScale();

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(startScale, targetScale);
        valueAnimator.setDuration(duration);
        // valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (onScaleCallback != null) {
                    onScaleCallback.onScale((float) animation.getAnimatedValue());
                }
            }
        });

        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
//                ((MapView) view).scale = targetScale;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        valueAnimator.start();
    }

}
