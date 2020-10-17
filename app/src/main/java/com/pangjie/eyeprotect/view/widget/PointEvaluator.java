package com.pangjie.eyeprotect.view.widget;

import android.animation.TypeEvaluator;

//实现TypeEvaluator接口
public class PointEvaluator implements TypeEvaluator {

    //重写evaluate()方法
    @Override
    public Object evaluate(float fraction, Object startValue, Object endValue) {

        //始末值强转为Point对象
        CirclePoint startPoint = (CirclePoint) startValue;
        CirclePoint endPoint = (CirclePoint) endValue;

        //通过fraction计算当前动画的坐标值x,y
        float x = startPoint.getP().X + fraction * (endPoint.getP().X - startPoint.getP().X);
        float y = startPoint.getP().Y + fraction * (endPoint.getP().Y - startPoint.getP().Y);

        //返回以上述x,y组装的新的Point对象
        CirclePoint point = new CirclePoint(new P(x,y,startPoint.getP().radius));
        return point;
    }
}