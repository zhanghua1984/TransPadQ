package cn.transpad.transpadui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;

import cn.transpad.transpadui.R;


public class ColorBallProgressView extends RelativeLayout {

	// Animation blueCubeAnim;
	// View blueCubeView;
	//
	// Animation orangeCubeAnim;
	// View orangeCubeView;
	// View greenCubeView;

	// ShapeHolder blueCube;
	// ShapeHolder orangeCube;
	// ShapeHolder greenCube;

	// AnimatorSet mAnimSet;
	// boolean mAnimStart;

	// ObjectAnimator xblueCube, yblueCube, alphablueCube, xOrangeCube,
	// yOrangeCube, alphaOrangeCube, xGreenCube, yGreenCube, alphaGreenCube;

	private float[] xyblueCube = new float[] { 1f, 1.3f, 1f, 1f, 1f, };
	private float[] alphablueCube = new float[] { 1f, 1f, 0.3f, 0.3f, 1f, 1f,
			1f };
	private float[] xyOrangeCube = new float[] { 1f, 1f, 1.3f, 1f, 1f };
	private float[] alphaOrangeCube = new float[] { 1f, 0.3f, 1f, 0.3f, 0.3f,
			1f, 1f };
	private float[] xyGreenCube = new float[] { 1f, 1f, 1f, 1.3f, 1f };
	private float[] alphaGreenCube = new float[] { 1f, 0.3f, 0.3f, 1f, 0.3f,
			0.3f, 1f };
	private Handler mHandler;

	View blueCubeView;
	View orangeCubeView;
	View greenCubeView;

	public ColorBallProgressView(Context context) {
		this(context, null);
	}

	public ColorBallProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mHandler = new Handler(Looper.getMainLooper());
		boolean isDialogStyle = false;

		// int setRepeatCount = 5;
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.ColorBallProgressView, 0, 0);

		// try {
		isDialogStyle = a.getBoolean(
				R.styleable.ColorBallProgressView_dialogStyle, true);
		//
		// Log.i("ColorBallProgressView", "");
		//
		// if (isDialogStyle) {
		// setRepeatCount = ObjectAnimator.INFINITE;
		// }
		//
		// } finally {
		// a.recycle();
		// }

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.color_cube_progress, this, true);

		blueCubeView = (View) findViewById(R.id.blue_cube);
		orangeCubeView = (View) findViewById(R.id.orange_cube);
		greenCubeView = (View) findViewById(R.id.green_cube);

		// ObjectAnimator xblueCube = ObjectAnimator.ofFloat(blueCubeView,
		// "scaleX", 1f, 1.3f, 1f, 1f, 1f, 1f, 1f).setDuration(2200);
		// xblueCube.setRepeatCount(setRepeatCount);
		// xblueCube.setRepeatMode(ObjectAnimator.RESTART);
		//
		// ObjectAnimator yblueCube = ObjectAnimator.ofFloat(blueCubeView,
		// "scaleY", 1f, 1.3f, 1f, 1f, 1f, 1f, 1f).setDuration(2200);
		// yblueCube.setRepeatCount(setRepeatCount);
		// yblueCube.setRepeatMode(ObjectAnimator.RESTART);
		//
		// ObjectAnimator alphablueCube = ObjectAnimator.ofFloat(blueCubeView,
		// "alpha", 1f, 1f, 0.3f, 0.3f, 1f, 1f, 1f).setDuration(2200);
		// alphablueCube.setRepeatCount(setRepeatCount);
		// alphablueCube.setRepeatMode(ObjectAnimator.RESTART);
		//
		// ObjectAnimator xOrangeCube = ObjectAnimator.ofFloat(orangeCubeView,
		// "scaleX", 1f, 1f, 1.3f, 1f, 1f, 1f, 1f).setDuration(2200);
		// xOrangeCube.setRepeatCount(setRepeatCount);
		// xOrangeCube.setRepeatMode(ObjectAnimator.RESTART);
		//
		// ObjectAnimator yOrangeCube = ObjectAnimator.ofFloat(orangeCubeView,
		// "scaleY", 1f, 1f, 1.3f, 1f, 1f, 1f, 1f).setDuration(2200);
		// yOrangeCube.setRepeatCount(setRepeatCount);
		// yOrangeCube.setRepeatMode(ObjectAnimator.RESTART);
		//
		// ObjectAnimator alphaOrangeCube =
		// ObjectAnimator.ofFloat(orangeCubeView, "alpha", 1f, 0.3f, 1f, 0.3f,
		// 0.3f, 1f, 1f).setDuration(2200);
		// alphaOrangeCube.setRepeatCount(setRepeatCount);
		// alphaOrangeCube.setRepeatMode(ObjectAnimator.RESTART);
		//
		// ObjectAnimator xGreenCube = ObjectAnimator.ofFloat(greenCubeView,
		// "scaleX", 1f, 1f, 1f, 1.3f, 1f, 1f, 1f).setDuration(2200);
		// xGreenCube.setRepeatCount(setRepeatCount);
		// xGreenCube.setRepeatMode(ObjectAnimator.RESTART);
		//
		// ObjectAnimator yGreenCube = ObjectAnimator.ofFloat(greenCubeView,
		// "scaleY", 1f, 1f, 1f, 1.3f, 1f, 1f, 1f).setDuration(2200);
		// yGreenCube.setRepeatCount(setRepeatCount);
		// yGreenCube.setRepeatMode(ObjectAnimator.RESTART);
		//
		// ObjectAnimator alphaGreenCube = ObjectAnimator.ofFloat(greenCubeView,
		// "alpha", 1f, 0.3f, 0.3f, 1f, 0.3f, 0.3f, 1f).setDuration(2200);
		// alphaGreenCube.setRepeatCount(setRepeatCount);
		// alphaGreenCube.setRepeatMode(ObjectAnimator.RESTART);
		//
		// mAnimSet = new AnimatorSet();
		// mAnimSet.playTogether(xblueCube, yblueCube, alphablueCube,
		// xOrangeCube, yOrangeCube, alphaOrangeCube, xGreenCube, yGreenCube,
		// alphaGreenCube);

		if (isDialogStyle) {
			// mAnimSet.start();
			startAnim();
		}

		//
		// blueCubeAnim = AnimationUtils.loadAnimation(context,
		// R.anim.blue_cube_anim);
		//
		// blueCubeAnim.setAnimationListener(new AnimationListener() {
		//
		// @Override
		// public void onAnimationStart(Animation animation) {
		// }
		//
		// @Override
		// public void onAnimationRepeat(Animation animation) {
		// }
		//
		// @Override
		// public void onAnimationEnd(Animation animation) {
		// blueCubeView.startAnimation(blueCubeAnim);
		//
		// }
		// });
		//
		//
		// blueCubeView.startAnimation(blueCubeAnim);
		//
		// orangeCubeView = (ImageView)findViewById(R.id.orange_cube);
		//
		// orangeCubeAnim = AnimationUtils.loadAnimation(context,
		// R.anim.orange_cube_anim);
		//
		// orangeCubeAnim.setAnimationListener(new AnimationListener() {
		//
		// @Override
		// public void onAnimationStart(Animation animation) {
		// }
		//
		// @Override
		// public void onAnimationRepeat(Animation animation) {
		// }
		//
		// @Override
		// public void onAnimationEnd(Animation animation) {
		// orangeCubeView.startAnimation(orangeCubeAnim);
		//
		// }
		// });
		//
		// orangeCubeView.startAnimation(orangeCubeAnim);

	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {

		//Log.v("ColorBallProgressView", "onVisibilityChanged: " + visibility);
		super.onVisibilityChanged(changedView, visibility);
		if (visibility == VISIBLE) {
			startAnim();
		} else {
			stopAnim();
		}
	}

	private int animIndex = 0;

	Runnable animRun = new Runnable() {
		@Override
		public void run() {
			if (animIndex >= xyblueCube.length - 1) {
				animIndex = 0;
			}
			// ���Ŷ����Ĳ���
			float startXy = xyblueCube[animIndex];
			float endXy = xyblueCube[animIndex + 1];
			if (startXy != endXy) {
				if (startXy > endXy) {
					startXy = 1;
					endXy = 1 / startXy;
				}
				ScaleAnimation anim = new ScaleAnimation(startXy, endXy,
						startXy, endXy, blueCubeView.getWidth() / 2,
						blueCubeView.getWidth() / 2);
				anim.setDuration(310);
				blueCubeView.startAnimation(anim);
			}

			startXy = xyOrangeCube[animIndex];
			endXy = xyOrangeCube[animIndex + 1];
			if (startXy != endXy) {
				if (startXy > endXy) {
					startXy = 1;
					endXy = 1 / startXy;
				}
				ScaleAnimation anim = new ScaleAnimation(startXy, endXy,
						startXy, endXy, orangeCubeView.getWidth() / 2,
						orangeCubeView.getWidth() / 2);
				anim.setDuration(310);
				orangeCubeView.startAnimation(anim);
			}

			startXy = xyGreenCube[animIndex];
			endXy = xyGreenCube[animIndex + 1];
			if (startXy != endXy) {
				if (startXy > endXy) {
					startXy = 1;
					endXy = 1 / startXy;
				}
				ScaleAnimation anim = new ScaleAnimation(startXy, endXy,
						startXy, endXy, greenCubeView.getWidth() / 2,
						greenCubeView.getWidth() / 2);
				anim.setDuration(310);
				greenCubeView.startAnimation(anim);
			}

			// ͸���ȶ�������

			// float startAlpha = alphablueCube[animIndex];
			// float endAlpha = alphablueCube[animIndex + 1];
			// if (startAlpha != endAlpha) {
			// if (startAlpha < endAlpha) {
			// endAlpha = endAlpha / startAlpha;
			// startAlpha = 1;
			// }
			// AlphaAnimation anim = new AlphaAnimation(startAlpha,
			// endAlpha);
			// anim.setDuration(310);
			// anim.setFillEnabled(false);
			// anim.setFillAfter(true);
			// blueCubeView.startAnimation(anim);
			// }
			//
			// startAlpha = alphaOrangeCube[animIndex];
			// endAlpha = alphaOrangeCube[animIndex + 1];
			// if (startAlpha != endAlpha) {
			// if (startAlpha < endAlpha) {
			// endAlpha = endAlpha / startAlpha;
			// startAlpha = 1;
			// }
			// AlphaAnimation anim = new AlphaAnimation(startAlpha,
			// endAlpha);
			// anim.setDuration(310);
			// orangeCubeView.startAnimation(anim);
			// }
			//
			// startAlpha = alphaGreenCube[animIndex];
			// endAlpha = alphaGreenCube[animIndex + 1];
			// if (startAlpha != endAlpha) {
			// if (startAlpha < endAlpha) {
			// endAlpha = endAlpha / startAlpha;
			// startAlpha = 1;
			// }
			// AlphaAnimation anim = new AlphaAnimation(startAlpha,
			// endAlpha);
			// anim.setDuration(310);
			// greenCubeView.startAnimation(anim);
			// }
			animIndex++;
			mHandler.postDelayed(this, 310);
		}
	};

	private void startAnim() {
		mHandler.removeCallbacks(animRun);
		mHandler.post(animRun);
	}

	private void stopAnim() {
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
		}
	}

	// public void stop() {
	// if (mAnimSet != null) {
	// mAnimSet.cancel();
	// }
	//
	// }

	// private void creatCube() {
	//
	//
	// ShapeHolder blueCube = new ShapeHolder((ShapeDrawable)
	// getContext().getResources().getDrawable(R.drawable.blue_cube));
	//
	// blueCube.setX(dp2px(10));
	// blueCube.setY(dp2px(25));
	//
	//
	// ShapeHolder orangeCube = new ShapeHolder((ShapeDrawable)
	// getContext().getResources().getDrawable(R.drawable.orange_cube));
	// blueCube.setX(dp2px(25));
	// blueCube.setY(dp2px(25));
	//
	// ShapeHolder greenCube = new ShapeHolder((ShapeDrawable)
	// getContext().getResources().getDrawable(R.drawable.green_cube));
	//
	// blueCube.setX(dp2px(40));
	// blueCube.setY(dp2px(25));
	//
	// }

	// @Override
	// protected void onDraw(Canvas canvas) {
	// canvas.save();
	// canvas.translate(ball.getX(), ball.getY());
	// ball.getShape().draw(canvas);
	// canvas.restore();
	//
	//
	//
	//
	// }

	//
	// public float getDensity() {
	// DisplayMetrics dm = new DisplayMetrics();
	// dm = getContext().getResources().getDisplayMetrics();
	// return dm.density;
	// }
	//
	// /**
	// * dpתpx
	// *
	// * @param dpValue
	// * dp
	// * @return int px
	// * @throws
	// */
	// public int dp2px(float dpValue) {
	// return (int) (dpValue * getDensity() + 0.5f);
	// }

	// public ColorBallProgressView(Context context, AttributeSet attrs,
	// int defStyleAttr) {
	// super(context, attrs, defStyleAttr);
	//
	// Log.i("ColorBallProgressView", "ColorBallProgressView: Start");
	//
	// Resources res = context.getResources();
	// Bitmap bitmap = BitmapFactory.decodeResource(res,
	// R.drawable.color_ball_loading);
	//
	// // Bitmap bitmap =
	// // context.getResources().getDrawable(R.drawable.loading).
	//
	// mDrawable = new ColorBallProgressDrawable(bitmap, 10);
	// mDrawable.setCallback(this);
	//
	// Log.i("ColorBallProgressView", "ColorBallProgressView: End");
	// }

	// @Override
	// protected void onVisibilityChanged(View changedView, int visibility) {
	//
	// Log.i("ColorBallProgressView", "onVisibilityChanged: " + visibility);
	// super.onVisibilityChanged(changedView, visibility);
	// if (visibility == VISIBLE) {
	// if (mDrawable != null)
	// mDrawable.start();
	// } else {
	// if (mDrawable != null)
	// mDrawable.stop();
	// }
	// }
	//
	// @Override
	// protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	// super.onSizeChanged(w, h, oldw, oldh);
	// mDrawable.setBounds(0, 0, w, h);
	// }
	//
	// @Override
	// public void draw(Canvas canvas) {
	// super.draw(canvas);
	// mDrawable.draw(canvas);
	// }
	//
	// @Override
	// protected boolean verifyDrawable(Drawable who) {
	// return who == mDrawable || super.verifyDrawable(who);
	// }
}