package cn.transpad.transpadui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class StrokeLineView extends View {
	@Override
	protected void onDraw(Canvas canvas) {
		float width = getWidth();
		float height = getHeight();
		int lineWidth = 10; // �߿�ʮ������
		int grayColor = Color.GRAY;
		Paint mLinePaint = new Paint();
		mLinePaint.setStyle(Paint.Style.STROKE);

		mLinePaint.setStrokeWidth(lineWidth);

		mLinePaint.setStrokeWidth(10);

		// ������
		mLinePaint.setColor(grayColor);
		mLinePaint.setAntiAlias(true);


		// �����ܵı߿� ע������� lineWidth/2 ���ӵĻ����ܵ��߿��ܲ�һ���
		canvas.drawLine(0f, 0 + lineWidth / 2, width, 0 + lineWidth / 2,
				mLinePaint);

		super.onDraw(canvas);

	}

	public StrokeLineView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

}
