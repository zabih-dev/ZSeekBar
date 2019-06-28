package zHelper.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ir.androidpower.zseekbar.R;


public class ZSeekBar extends View {

  public static final int GRAVITY_NONE = 0;
  public static final int GRAVITY_TOP = 1;
  public static final int GRAVITY_BOTTOM = 2;
  //
  public static final int SEEK_MODE_CIRCLE = 0;
  public static final int SEEK_MODE_ARC = 1;
  public static final int SEEK_MODE_LINE = 2;

  protected final int[] defaultColorList = {0, 0};
  //
  protected Paint progressPaint = new Paint();
  protected Paint progressBackgroundPaint = new Paint();
  protected Paint thumbPaint = new Paint();
  protected RectF ovalRectF = new RectF();
  protected float progressWidth;
  protected float progressBackgroundWidth;
  protected float thumbDrawableWidth;
  protected int thumbTintColor;
  protected @Nullable Drawable thumbDrawable;
  //for enable semi circle set gravity top , bottom otherwise circle
  protected int semiCircleGravity;
  protected float progress;
  protected float progressSweepAngle;
  protected float minProgress = 0;
  protected float maxProgress;
  protected float startAngle;
  protected float sweepAngle;
  protected boolean isClockwise;
  protected boolean isUserSeekable = true;
  protected boolean isRoundEdges;
  protected int progressBackgroundColor;
  protected @Nullable int[] progressColorList;
  protected @Nullable int[] reversedProgressColorList;
  protected @Nullable OnZArcSeekBarChangeListener onZArcSeekBarChangeListener;
  protected boolean isUserTracking;
  protected boolean isFloatingThumbColor;
  protected boolean isTouchInsideToSeeking;
  protected float touchIgnoreRadius;
  protected float radius;

  //
  protected boolean isAllowToSeekingOnTouch = true;
  protected boolean isMustTouchThumbToSeeking = false;
  //
  protected float mTranslateX;
  protected float mTranslateY;
  protected final int INVALID_PROGRESS_VALUE = -1;
  //
  protected float thumbCX;
  protected float thumbCY;

  //
  protected int seekMode;
  protected float arc_dx;
  protected float arc_dy;
  protected float arc_realWidth;
  protected float arc_realHeight;
  protected float arc_alphaRad;

  protected float arc_circleCenterX;
  protected float arc_circleCenterY;

  protected final double pi = Math.PI;
  protected final float zero = 0.0001F;



  public interface OnZArcSeekBarChangeListener {
    void onProgressChanged(ZSeekBar circleProgress, float progress, boolean fromUser);

    void onStartTrackingTouch(ZSeekBar circleProgress);

    void onStopTrackingTouch(ZSeekBar circleProgress);
  }


  public ZSeekBar(Context context) {
    this(context, null);
  }


  public ZSeekBar(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.zSeekBarStyle);
  }


  public ZSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    final int accentColor = getResources().getColor(R.color.colorAccent);

    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ZSeekBar, defStyleAttr, 0);

    startAngle = typedArray.getFloat(R.styleable.ZSeekBar_zStartAngle, 0);
    sweepAngle = typedArray.getFloat(R.styleable.ZSeekBar_zSweepAngle, 360);
    progress = typedArray.getFloat(R.styleable.ZSeekBar_zProgress, 0);
    maxProgress = typedArray.getFloat(R.styleable.ZSeekBar_zMaxProgress, 100);
    semiCircleGravity = typedArray.getInt(R.styleable.ZSeekBar_zSemiCircleGravity, GRAVITY_NONE);
    seekMode = typedArray.getInt(R.styleable.ZSeekBar_zSeekMode, SEEK_MODE_CIRCLE);
    isClockwise = typedArray.getBoolean(R.styleable.ZSeekBar_zIsClockwise, true);
    isRoundEdges = typedArray.getBoolean(R.styleable.ZSeekBar_zIsRoundEdges, true);
    isFloatingThumbColor = typedArray.getBoolean(R.styleable.ZSeekBar_zIsFloatingThumbColor, true);
    progressWidth = typedArray.getDimension(R.styleable.ZSeekBar_zProgressWidth, 8);
    progressBackgroundWidth = typedArray.getDimension(R.styleable.ZSeekBar_zProgressBackgroundWidth, 4);
    //
    thumbDrawable = typedArray.getDrawable(R.styleable.ZSeekBar_zThumb);
    int thumbDefaultColor = thumbDrawable == null ? accentColor : 0;
    thumbTintColor = typedArray.getColor(R.styleable.ZSeekBar_zThumbTintColor, thumbDefaultColor);
    float thumbWidthDef = thumbDrawable != null ? -1 : progressWidth * 2.5f;
    thumbDrawableWidth = typedArray.getDimension(R.styleable.ZSeekBar_zThumbWidth, thumbWidthDef);
    //
    progressBackgroundColor = typedArray.getColor(R.styleable.ZSeekBar_zProgressBackgroundColor, Color.GRAY);

    if (thumbDrawableWidth < -1) thumbDrawableWidth = -1;

    int colorStart = typedArray.getColor(R.styleable.ZSeekBar_zProgressColor, accentColor);
    int colorCenter = typedArray.getColor(R.styleable.ZSeekBar_zProgressCenterColor, -1);
    int colorEnd = typedArray.getColor(R.styleable.ZSeekBar_zProgressEndColor, -1);
    //
    String colorList = typedArray.getString(R.styleable.ZSeekBar_zProgressColorList);


    if (colorList != null) {
      String[] strings = colorList.split(",");

      for (int i = 0; i < strings.length; i++) {
        if (progressColorList == null) progressColorList = new int[strings.length];
        String string = strings[i].trim();
        if (string.startsWith("#") && (string.length() == 7 || string.length() == 9)) {
          progressColorList[i] = getColor(string);
        }
      }
    } else {
      List<Integer> tempList = new ArrayList();
      for (int i = 0; i < 3; i++) {
        if (i == 0 && colorStart != -1) tempList.add(colorStart);
        else if (i == 1 && colorCenter != -1) tempList.add(colorCenter);
        else if (i == 2 && colorEnd != -1) tempList.add(colorEnd);
      }

      progressColorList = new int[tempList.size()];

      for (int i = 0; i < tempList.size(); i++) {
        progressColorList[i] = tempList.get(i);
      }
    }


    typedArray.recycle();
  }


  protected int getColor(String string) {
    return Color.parseColor(string);
  }


  public void setOnZArcSeekBarChangeListener(@Nullable OnZArcSeekBarChangeListener listener) {
    this.onZArcSeekBarChangeListener = listener;
  }


  public void setProgress(float progress) {
    this.progress = progress;
    postInvalidate();
  }


  public void setMaxProgress(float maxProgress) {
    this.maxProgress = maxProgress;
    postInvalidate();
  }


  public void setStartAngle(float startAngle) {
    this.startAngle = startAngle;
    postInvalidate();
  }


  public void setSweepAngle(float sweepAngle) {
    this.sweepAngle = checkBound(sweepAngle, 0, 360);
    postInvalidate();
  }


  public void setTouchInSide(boolean isEnabled) {
    isTouchInsideToSeeking = isEnabled;
    if (isTouchInsideToSeeking) {
      touchIgnoreRadius = radius / 4;
    } else {
      int thumbHalfWidth = getThumbWidth() / 2;
      int thumbHalfHeight = getThumbHeight() / 2;
      // Don't use the exact radius makes interaction too tricky
      touchIgnoreRadius = radius - Math.min(thumbHalfWidth, thumbHalfHeight);
    }
  }


  public void setSemiCircleGravity(int semiCircleGravity) {
    this.semiCircleGravity = semiCircleGravity;
    postInvalidate();
  }


  public void setSeekMode(int seekMode) {
    this.seekMode = seekMode;
    postInvalidate();
  }


  public void setClockwise(boolean clockwise) {
    isClockwise = clockwise;
    postInvalidate();
  }


  public void setProgressWidth(float progressWidth) {
    this.progressWidth = progressWidth;
    postInvalidate();
  }


  public void setProgressBackgroundWidth(float progressBackgroundWidth) {
    this.progressBackgroundWidth = progressBackgroundWidth;
    postInvalidate();
  }


  public void setThumbDrawableWidth(float thumbDrawableWidth) {
    this.thumbDrawableWidth = thumbDrawableWidth;
    postInvalidate();
  }


  public void setThumbTintColor(int thumbTintColor) {
    this.thumbTintColor = thumbTintColor;
    postInvalidate();
  }


  public void setThumbDrawable(@Nullable Drawable thumbDrawable) {
    this.thumbDrawable = thumbDrawable;
    postInvalidate();
  }


  public void setRoundEdges(boolean roundEdges) {
    isRoundEdges = roundEdges;
    postInvalidate();
  }


  public void setProgressBackgroundColor(int progressBackgroundColor) {
    this.progressBackgroundColor = progressBackgroundColor;
    postInvalidate();
  }


  public void setFloatingThumbColor(boolean floatingThumbColor) {
    isFloatingThumbColor = floatingThumbColor;
    postInvalidate();
  }


  public void setMustTouchThumbToSeeking(boolean mustTouchThumbToSeeking) {
    isMustTouchThumbToSeeking = mustTouchThumbToSeeking;
    postInvalidate();
  }


  protected int[] getProgressColorList() {
    if (progressColorList == null || progressColorList.length == 0) {
      progressColorList = defaultColorList;
    }
    //must >= 2
    if (progressColorList != null && progressColorList.length < 2) {
      int color = progressColorList[0];
      progressColorList = new int[2];
      progressColorList[0] = color;
      progressColorList[1] = color;
    }

    if (reversedProgressColorList == null) {
      reversedProgressColorList = new int[progressColorList.length];
      List<Integer> tempList = new ArrayList<>();

      for (int i : progressColorList) {
        tempList.add(i);
      }
      Collections.reverse(tempList);

      for (int i = 0; i < tempList.size(); i++) {
        reversedProgressColorList[i] = tempList.get(i);
      }
    }

    if (semiCircleGravity == GRAVITY_TOP) {
      if (isClockwise) return reversedProgressColorList;
      else return progressColorList;
    }

    if (!isClockwise) {
      return reversedProgressColorList;
    } else {
      return progressColorList;
    }
  }


  protected float getBound(float value, float min, float max) {
    if (value > max) return max;
    if (value < min) return min;
    return value;
  }


  protected float checkBound(float value, float minValue, float maxValue) {
    if (value > maxValue) {
      value -= maxValue;

      if (value > maxValue) {
        return checkBound(value, minValue, maxValue);
      }
    }

    if (value < minValue) {
      value += maxValue;

      if (value < minValue) {
        return checkBound(value, minValue, maxValue);
      }
    }

    return value;
  }


  protected boolean isStartAngle180() {
    return
      (semiCircleGravity == GRAVITY_BOTTOM && isClockwise) ||
        (semiCircleGravity == GRAVITY_TOP && !isClockwise);
  }


  protected int getThumbWidth() {
    thumbDrawableWidth = Math.max(-1, thumbDrawableWidth);
    if (thumbDrawable != null && thumbDrawableWidth == -1) {
      return thumbDrawable.getIntrinsicWidth();
    }
    if (thumbDrawableWidth < -1) return -1;
    return (int) thumbDrawableWidth;
  }


  protected int getThumbHeight() {
    thumbDrawableWidth = Math.max(-1, thumbDrawableWidth);
    if (thumbDrawable != null && thumbDrawableWidth == -1) {
      return thumbDrawable.getIntrinsicHeight();
    }
    if (thumbDrawableWidth < -1) return -1;
    return (int) thumbDrawableWidth;
  }


  protected void onTouchUpdateDraw(MotionEvent event) {

    boolean ignoreTouch = ignoreTouch(event.getX(), event.getY());
    if (ignoreTouch) {
      return;
    }
//    setPressed(true);
    float progress;

    if (isArcMode() || isLineMode()) {
      progress = getProgressFromClick(event.getX(), event.getY(), getThumbHeight());

      if (isLineMode() && !isClockwise) {
        progress = maxProgress - progress;
      }
    } else {
      double mTouchAngle = getTouchDegrees(event.getX(), event.getY());
      progress = getProgressByAngle(mTouchAngle);
    }

    if (progress != INVALID_PROGRESS_VALUE) {
      this.progress = Math.round(progress);
      postInvalidate();
    }
  }


  protected boolean ignoreTouch(float xPos, float yPos) {
    boolean ignore = false;
    float x = xPos - mTranslateX;
    float y = yPos - mTranslateY;

    float touchRadius = (float) Math.sqrt(((x * x) + (y * y)));
    if (touchRadius < touchIgnoreRadius) {
      ignore = true;
    }
    return ignore;
  }


  protected double getTouchDegrees(float xPos, float yPos) {
    boolean isSemiCircle = isSemiCircle();
    float x = xPos - mTranslateX;
    float y = yPos - mTranslateY;

    if (isSemiCircle) {
      if (semiCircleGravity == GRAVITY_BOTTOM) {
        y -= mTranslateY;
      } else y += mTranslateY;

      if (semiCircleGravity == GRAVITY_BOTTOM && isClockwise) {
        x = -x;
      }

      if (semiCircleGravity == GRAVITY_TOP && !isClockwise) {
        x = -x;
      }
    }


    if (semiCircleGravity != GRAVITY_TOP) {
      y = (isClockwise && !isSemiCircle) ? y : -y;
    }

    // convert to arc Angle
    double offset = 0;
    double angle = Math.toDegrees(Math.atan2(y, x) + offset);
    if (angle < 0) {
      angle = 360 + angle;
    }

    float startAngle2 = startAngle;

    if (isSemiCircle) {
      startAngle2 = 0;
    }

    angle -= startAngle2;

    if (!isSemiCircle) {
      angle = checkBound((float) angle, 0, 360);
    }

    return angle;
  }


  protected float getProgressByAngle(double angle) {
    float sweepAngle1 = sweepAngle;
    if (isSemiCircle()) {
      sweepAngle1 = getBound(sweepAngle1, 0, 180);
    }
    float range = getRang();
    float touchProgress = (float) ((angle * range) / sweepAngle1);

    touchProgress = (touchProgress < 0) ? INVALID_PROGRESS_VALUE : touchProgress;
    touchProgress = (touchProgress > maxProgress) ? INVALID_PROGRESS_VALUE : touchProgress;
    return touchProgress;
  }


  protected float getRang() {
    return maxProgress - minProgress;
  }


  protected float getProgressFromClick(float x, float y, int thumbHeight) {
    if (isMustTouchThumbToSeeking && y > arc_realHeight + arc_dy * 2) return INVALID_PROGRESS_VALUE;
    double xx = arc_circleCenterX - x;
    double yy = arc_circleCenterY - y;
    double distToCircleCenter = Math.sqrt(Math.pow(xx, 2.0) + Math.pow(yy, 2.0));//
    if (isMustTouchThumbToSeeking && Math.abs(distToCircleCenter - radius) > thumbHeight)
      return INVALID_PROGRESS_VALUE;
    float innerWidthHalf = arc_realWidth / 2f;
    float xFromCenter = getBound(x - arc_circleCenterX, -innerWidthHalf, innerWidthHalf);
    float touchAngle = (float) (Math.acos(xFromCenter / radius) + arc_alphaRad - pi / 2);
    float angleToMax = (float) (1.0 - touchAngle / (2 * arc_alphaRad));
    return getBound(((maxProgress + 1) * angleToMax), 0, maxProgress);
  }


  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (getThumbWidth() <= 0 || !isUserSeekable || !isEnabled()) {
      return false;
    }
    float x = event.getX();
    float y = event.getY();

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        isUserTracking = true;
        if (onZArcSeekBarChangeListener != null)
          onZArcSeekBarChangeListener.onStartTrackingTouch(this);
        if (isMustTouchThumbToSeeking) {
          float halfWidth = getThumbWidth() / 2f;
          float minThumbX = thumbCX - halfWidth;
          float maxThumbX = thumbCX + halfWidth;
          float minThumbY = thumbCY - halfWidth;
          float maxThumbY = thumbCY + halfWidth;
          isAllowToSeekingOnTouch = x >= minThumbX && x <= maxThumbX && y >= minThumbY && y <= maxThumbY;
        } else {
          isAllowToSeekingOnTouch = true;
        }
        if (isAllowToSeekingOnTouch) {
          setPressed(true);
          onTouchUpdateDraw(event);
        }
        break;
      case MotionEvent.ACTION_MOVE:
        if (isAllowToSeekingOnTouch) {
          setPressed(true);
          onTouchUpdateDraw(event);
        }
        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        setPressed(false);
        isUserTracking = false;
        if (onZArcSeekBarChangeListener != null)
          onZArcSeekBarChangeListener.onStopTrackingTouch(this);
        break;
    }
    return true;
  }


  protected boolean isSemiCircle() {
    return semiCircleGravity != GRAVITY_NONE;
  }


  protected boolean isCircleMode() {
    return seekMode == SEEK_MODE_CIRCLE;
  }


  protected boolean isArcMode() {
    return seekMode == SEEK_MODE_ARC;
  }


  protected boolean isLineMode() {
    return seekMode == SEEK_MODE_LINE;
  }


  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    boolean isSemiCircle = isSemiCircle();

    float width = (float) getWidth();
    float height = (float) getHeight();
    float height0 = height;
    //
    if (isArcMode() || isLineMode()) {
      float dx0 = Math.max(getThumbWidth() / 2f, this.progressWidth) + 2;
      float dy0 = Math.max(getThumbHeight() / 2f, this.progressWidth) + 2;

      arc_realWidth = width - 2 * dx0 - getPaddingLeft() - getPaddingRight();
      arc_realHeight = Math.min(height - 2 * dy0 - getPaddingTop() - getPaddingBottom(), arc_realWidth / 2);

      if (isLineMode()) arc_realHeight = 1;

      arc_dx = dx0 + getPaddingLeft();
      arc_dy = dy0 + getPaddingTop();
      width = arc_realWidth;
      height = arc_realHeight;
      radius = height / 2 + (width * width / 8 / height);

      arc_circleCenterX = width / 2 + arc_dx;
      arc_circleCenterY = radius + arc_dy * 2;

      double xxx = radius - height;
      arc_alphaRad = getBound((float) Math.acos(xxx / radius), zero, (float) (2 * pi));

      float top = arc_circleCenterY - radius;
      float bottom = arc_circleCenterY + radius;
      float offsetCY = (height0 / 2) - (2 * arc_dy);//offset to centerY

      if (isLineMode()) {
        top += offsetCY;
        bottom += offsetCY;
      }


      ovalRectF.set(arc_circleCenterX - radius, top, arc_circleCenterX + radius, bottom);

      float sValue = (float) (270 - Math.toDegrees(arc_alphaRad));
      startAngle = getBound(sValue, 180F, 360F);

      float swValue = (float) Math.toDegrees(2 * arc_alphaRad);
      sweepAngle = getBound(swValue, zero, 180F);
      float pValue = (progress / maxProgress) * 2 * arc_alphaRad;
      float progressSweepRad = maxProgress == 0 ?
        zero : getBound(pValue, zero, (float) (2 * pi));
      progressSweepAngle = progressSweepRad / 2 / (float) pi * 360F;

      double rad = (arc_alphaRad + pi / 2 - progressSweepRad);
      thumbCX = (float) (radius * Math.cos(rad) + arc_circleCenterX);
      thumbCY = (float) (-radius * Math.sin(rad) + arc_circleCenterY);

      if (isLineMode()) {
        thumbCY += offsetCY;
      }
    } else {
      float minValue = Math.min(width, height);
      float maxPadding = Math.max(progressWidth, getThumbWidth());
      minValue -= (maxPadding + getPaddingLeft() + getPaddingRight());
      radius = minValue / 2;
    }


    float centerX = width / 2;
    float centerY = height / 2;

    //
    mTranslateX = width * 0.5f;
    mTranslateY = height * 0.5f;


    //
    float sweepAngle1 = sweepAngle;

    if (isSemiCircle) {
      sweepAngle1 = getBound(sweepAngle1, 0, 180);
    }

    progress = checkBound(progress, minProgress, maxProgress);
    //*
    if (isCircleMode()) {
      progressSweepAngle = (progress / maxProgress) * sweepAngle1;
    }


    float startAngle2 = startAngle;
    //
    float top = centerY - radius;
    float bottom = centerY + radius;
    float left = centerX - radius;
    float right = centerX + radius;


    if (isSemiCircle && isCircleMode()) {
      startAngle2 = isStartAngle180() ? 180 : 0;

      if (semiCircleGravity == GRAVITY_BOTTOM || semiCircleGravity == GRAVITY_TOP) {
        top = semiCircleGravity == GRAVITY_BOTTOM ? height - radius : 0 - radius;
        bottom = semiCircleGravity == GRAVITY_BOTTOM ? height + radius : 0 + radius;
      }
    }


    int[] colorList = getProgressColorList();
    Shader shader;
    if (isSemiCircle || isArcMode() || isLineMode()) {
      float x0 = isSemiCircle ? left : arc_dx;
      float x1 = isSemiCircle ? right : arc_realWidth;
      shader = new LinearGradient(x0, 0, x1, 0, colorList, null, Shader.TileMode.CLAMP);
    } else {
      shader = new SweepGradient(centerX, centerY, colorList, null);
    }


    progressPaint.setStrokeWidth(progressWidth);
    progressPaint.setStyle(Paint.Style.STROKE);
    progressPaint.setAntiAlias(true);
    progressPaint.setShader(shader);
    progressPaint.setStrokeCap(isRoundEdges ? Paint.Cap.ROUND : Paint.Cap.SQUARE);
    //
    progressBackgroundPaint.setStrokeWidth(progressBackgroundWidth);
    progressBackgroundPaint.setStyle(Paint.Style.STROKE);
    progressBackgroundPaint.setAntiAlias(true);
    progressBackgroundPaint.setColor(progressBackgroundColor);
    progressBackgroundPaint.setStrokeCap(isRoundEdges ? Paint.Cap.ROUND : Paint.Cap.SQUARE);


    if (isCircleMode()) {
      ovalRectF.set(left, top, right, bottom);
    }

    if (!isClockwise) {
      float sx = 1;
      float sy = -1;
      if (isLineMode()) {
        sx = -1;
        sy = 1;
      }
      if (isArcMode()) {
        sx = 1;
        sy = 1;
      }
      canvas.scale(sx, sy, ovalRectF.centerX(), ovalRectF.centerY());
    }


    canvas.drawArc(ovalRectF, startAngle2, sweepAngle1, false, progressBackgroundPaint);
    canvas.drawArc(ovalRectF, startAngle2, progressSweepAngle, false, progressPaint);


    /*draw thumb paint or drawable*/
    float sweepAngle3 = progressSweepAngle + startAngle2;
    //get radian form degree
    float thumbRad = (float) (sweepAngle3 * Math.PI / 180f);

    if (isCircleMode()) {
      thumbCX = (float) (radius * Math.cos(thumbRad) + centerX);
      thumbCY = (float) (radius * Math.sin(thumbRad) + bottom - radius);
    }

    if (thumbDrawable != null) {
      int thumbHalfWidth = getThumbWidth() / 2;
      int thumbHalfHeight = getThumbHeight() / 2;

      thumbDrawable.setBounds((int) thumbCX - thumbHalfWidth, (int) thumbCY - thumbHalfHeight, (int) thumbCX + thumbHalfWidth, (int) thumbCY + thumbHalfHeight);
      if (thumbTintColor != 0) {
        thumbDrawable.setColorFilter(thumbTintColor, PorterDuff.Mode.SRC_ATOP);
      }
      thumbDrawable.draw(canvas);
    } else {
      onDrawThumbPaint(canvas, shader);
    }


    if (onZArcSeekBarChangeListener != null) {
      onZArcSeekBarChangeListener.onProgressChanged(this, progress, isUserTracking);
    }
  }


  protected void onDrawThumbPaint(Canvas canvas, Shader shader) {
    thumbPaint.setColor(thumbTintColor);
    thumbPaint.setStyle(Paint.Style.FILL);
    thumbPaint.setAntiAlias(true);
    if (isFloatingThumbColor) {
      thumbPaint.setShader(shader);
    }
    canvas.drawCircle(thumbCX, thumbCY, thumbDrawableWidth / 2, thumbPaint);
  }
}
