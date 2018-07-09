package com.example.sgr.setpolytopoly;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Arrays;

/**
 * Data：2018/7/5/005-18:20
 * By  沈国荣
 * Description:
 */
public class CurtainView extends View {
    private static final String TAG = "SetPolyToPoly";

    private int triggerRadius = 180;    // 触发半径为180px

    private Bitmap mBitmap;             // 要绘制的图片
    private Matrix mPolyMatrix,mCollectMatrix,mTransMatrix;         // 测试setPolyToPoly用的Matrix
    private Point2 besidePointLine = null;
    private Point2 orgBesidePointLine = null;
    private Point2 besidePoint = null;
    private float[] src = new float[8];
    private float[] dst = new float[8];
    private Point2 downPoint = null;
    private Paint pointPaint;
    private Point2 orgBesidePoint = null;
    private Point2[] pointArr;//图形四点的坐标
    private Point2[] pointPaintArr;//画路径的四点坐标
    private Path path = new Path();
    private Paint paintEdge;  // 绘制图片的边框的画笔
    private int position=-1;//记录需要变动的点的位置
    int mode;
    private Point2 pianyi=null;//偏移量
    // 资源缩放图片的位图
    private Bitmap srcIcon;
    private Rect rect;
    private Matrix moveMatrix = new Matrix();
    private Matrix downMatrix = new Matrix();

    public CurtainView(Context context) {
        this(context, null);
    }

    public CurtainView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CurtainView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initBitmapAndMatrix();
    }

    private void initBitmapAndMatrix() {
        mBitmap = BitmapFactory.decodeResource(getResources(),
                R.mipmap.timg);
        srcIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_mirror);
        float[] temp = {0, 0,                                    // 左上
                mBitmap.getWidth(), 0,                          // 右上
                mBitmap.getWidth(), mBitmap.getHeight(),        // 右下
                0, mBitmap.getHeight()};                        // 左下
        src = temp.clone();
        dst = temp.clone();
        rect = new Rect();
        Point2 p0 = new Point2(0, 0);
        Point2 p1 = new Point2(0+mBitmap.getWidth(),0);
        Point2 p2 = new Point2(  0+mBitmap.getWidth(), 0+mBitmap.getHeight());
        Point2 p3 = new Point2( 0, mBitmap.getHeight()+0);

        pointArr = new Point2[4];
        pointArr[0] = p0;
        pointArr[1] = p1;
        pointArr[2] = p2;
        pointArr[3] = p3;

        pointPaintArr = new Point2[4];
        pointPaintArr[0] = p0;
        pointPaintArr[1] = p1;
        pointPaintArr[2] = p2;
        pointPaintArr[3] = p3;


        paintEdge=new Paint();
        paintEdge.setColor(Color.YELLOW);
        paintEdge.setAntiAlias(true);
        // 绘制边框
        paintEdge.setStyle(Paint.Style.STROKE);
        paintEdge.setStrokeJoin(Paint.Join.ROUND);
        paintEdge.setStrokeWidth(5f);



        buildPath();
        mPolyMatrix = new Matrix();//
        mCollectMatrix=new Matrix();
        mTransMatrix=new Matrix();
//        mTransMatrix.setTranslate(100,100);

//        mPolyMatrix.setPolyToPoly(src, 0, src, 0, 4);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.translate(100,100);

/*        // 绘制坐标系
        CanvasAidUtils.setCoordinateLen(900, 0, 1200, 0);
        CanvasAidUtils.drawCoordinateSpace(canvas);*/

       float x= (pointArr[0].x+pointArr[1].x)/2;
       float y=(pointArr[0].y+pointArr[3].y)/2;

        System.out.println(" mPolyMatrix.toString()"+ mPolyMatrix.toString());
//        mCollectMatrix.setConcat(mPolyMatrix,mTransMatrix);

        canvas.save();      //保存状态
         //具体操作
        canvas.setMatrix(mTransMatrix);
        canvas.drawBitmap(mBitmap, mPolyMatrix, null);
        canvas.restore();   //回滚到之前的状态

        // 根据Matrix绘制一个变换后的图片
        canvas.drawPath(path,paintEdge);
        // 画顶点缩放图片
        rect.left = (int) (x - srcIcon.getWidth() / 2);
        rect.right = (int) (x + srcIcon.getWidth() / 2);
        rect.top = (int) (y - srcIcon.getHeight() / 2);
        rect.bottom = (int) (y + srcIcon.getHeight() / 2);
        canvas.drawBitmap(srcIcon, null, rect, null);


      /*  showPoints(canvas);
        float[] dst = new float[8];*/
//        mPolyMatrix.mapPoints(dst,src);
        //获得 matrix之后的点的坐标


    }

    /**
     * 判断手指触摸的区域是否在顶点的操作按钮内
     *
     * @param event
     * @return
     */
    public boolean isInActionCheck(MotionEvent event) {
        int left = rect.left;
        int right = rect.right;
        int top = rect.top;
        int bottom = rect.bottom;
        return event.getX(0) >= left && event.getX(0) <= right && event.getY(0) >= top && event.getY(0) <= bottom;
    }
    // 手指按下屏幕的X坐标
    private float downX;
    // 手指按下屏幕的Y坐标
    private float downY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float pointX = event.getX();
        float pointY = event.getY();

        switch (event.getAction()){

            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();

                if(isInActionCheck(event)){
                    mode=ActionMode.TRANS;
                    downMatrix.set(mTransMatrix);
//                    System.out.println("点击按钮ACTION_DOWN+TRANS");
                }else{
                    mode=ActionMode.DRAG;
//                    System.out.println("点击按钮ACTION_DOWN+DRAG");
                }

                downPoint = new Point2(pointX, pointY);
                besidePoint = null;//该点与点击点最近的点
                if (pointArr.length != 0) {
                    double minDis = 0;
                    double tmpDis = 0;
                    for (Point2 p : pointArr) { //第一个点
                        if (besidePoint == null) {
                            besidePoint = p;
                            minDis = distanceBetween(p, downPoint);
                        } else {
                            tmpDis = distanceBetween(p, downPoint);
                            if (tmpDis < minDis) {
                                besidePoint = p;
                                minDis = tmpDis;
                            }
                        }
                    }
                    if (besidePoint != null) {
                        orgBesidePoint = new Point2(besidePoint.x, besidePoint.y);
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if(mode==ActionMode.DRAG){
//                    System.out.println("点击按钮ACTION_MOVE+DRAG");
                    for (int i = 0; i < pointPaintArr.length; i++) {
                        if (orgBesidePoint.x == pointPaintArr[i].x && orgBesidePoint.y == pointPaintArr[i].y) {
                            position = i;
                        }
                    }
                    if (position != -1) {
                        besidePoint.x = orgBesidePoint.x + (event.getX() - downX);
                        besidePoint.y = orgBesidePoint.y + (event.getY() - downY);
                        pointArr[position] = new Point2(besidePoint.x, besidePoint.y);
                        pointPaintArr[position] = new Point2(besidePoint.x, besidePoint.y);
                    }

                    setDst();

                    // 核心要点
                    mPolyMatrix.reset();
                    mPolyMatrix.setPolyToPoly(src, 0, dst, 0, 4);
                    buildPath();
                    invalidate();
                }else if(mode==ActionMode.TRANS){



//                    System.out.println("点击按钮ACTION_MOVE+TRANS");
                    pianyi = new Point2(pointX - downPoint.x, pointY - downPoint.y);//平移的值跟其他平移值要分开，不然会错乱
                    for (int j = 0; j < 4; j++) {
                        pointPaintArr[j] = new Point2(pointPaintArr[j].x + pianyi.x, pointPaintArr[j].y + pianyi.y);
                    }
                    moveMatrix.set(downMatrix);
                    moveMatrix.postTranslate(event.getX() - downX, event.getY() - downY);
                    mTransMatrix.set(moveMatrix);
                    downPoint = new Point2(pointX, pointY);
                    buildPath();
                    invalidate();
                }



                break;
            case MotionEvent.ACTION_UP:
                position = -1;
                mode = ActionMode.NONE;
                break;
        }

        return true;
    }

    private void setDst() {

        float[] temp = {pointArr[0].x,pointArr[0].y,                                    // 左上
                pointArr[1].x, pointArr[1].y,                          // 右上
                pointArr[2].x, pointArr[2].y,        // 右下
                pointArr[3].x, pointArr[3].y};                        // 左下
        dst = temp.clone();
        System.out.println("打印dst"+ Arrays.toString(dst));
        System.out.println("打印src"+ Arrays.toString(src));
    }
    private void showPoints(Canvas canvas) {
        paintEdge=new Paint();
        paintEdge.setColor(Color.YELLOW);
        paintEdge.setAntiAlias(true);
        // 绘制边框
        paintEdge.setStyle(Paint.Style.STROKE);
        paintEdge.setStrokeJoin(Paint.Join.ROUND);
        paintEdge.setStrokeWidth(5f);
        canvas.drawPath(path, paintEdge);
        // 绘制四个点
//        makPoints(canvas);
    }
    protected void buildPath() {
        if(path!=null){
            path = new Path();
        }
        int i = 0;
        Point2 p0 = null;
        for (Point2 p : pointPaintArr) {
            if (i == 0) {
                p0 = p;
                path.moveTo(p.x, p.y);
            } else {
                path.lineTo(p.x, p.y);
            }

            i++;
        }
        path.close();
    }


    /* 计算两点间的距离
     *
     * @param source
     * @param width
     * @param height
     * @return
     */
    public static double distanceBetween(Point2 p1, Point2 p2) {
        double dis = Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
        return dis;
    }
}