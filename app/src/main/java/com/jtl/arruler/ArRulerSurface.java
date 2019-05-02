package com.jtl.arruler;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.jtl.arruler.callback.ArRulerCallBack;
import com.jtl.arruler.helper.DisplayRotationHelper;
import com.jtl.arruler.helper.SessionHelper;
import com.jtl.arruler.helper.TapHelper;
import com.jtl.arruler.old.Config;
import com.jtl.arruler.old.LineRender;
import com.jtl.arruler.old.MathUtil;
import com.jtl.arruler.old.PictureRender;
import com.jtl.arruler.old.PointRender;
import com.jtl.arruler.render.BackgroundRenderer;
import com.jtl.arruler.render.PointPictureRender;
import com.socks.library.KLog;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.jtl.arruler.old.Config.DrawState.END;

/**
 * 作者:jtl
 * 日期:Created in 2019/5/2 16:50
 * 描述:
 * 更改:
 */
public class ArRulerSurface extends GLSurfaceView implements GLSurfaceView.Renderer {
    private static final String TAG = ArRulerSurface.class.getSimpleName();
    private Session mSession;
    private DisplayRotationHelper displayRotationHelper;
    private BackgroundRenderer mBackgroundRenderer;
    private PointPictureRender mPointPictureRender;
    private PointRender mPointRender;
    private LineRender mLineRender;
    private PictureRender mPictureRender;
    private List<Anchor> mAnchorList;
    private Point mPoint;
    private MotionEvent mMotionEvent;
    private Anchor mCurrentAnchor;
    private TapHelper mTapHelper;
    private ArRulerCallBack mArRulerCallBack;
    private volatile boolean isHitTest =false;
    private volatile TrackingState mTrackingState;

    public ArRulerSurface(Context context) {
        this(context, null);
    }

    public ArRulerSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.setEGLContextClientVersion(2);
        this.setPreserveEGLContextOnPause(true);
        this.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        this.setRenderer(this);
        this.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        displayRotationHelper = new DisplayRotationHelper(getContext());

        mBackgroundRenderer = new BackgroundRenderer();
        mPointPictureRender = new PointPictureRender();
        mPointRender = new PointRender();
        mPictureRender = new PictureRender();
        mLineRender = new LineRender();

        mBackgroundRenderer.createOnGlThread(getContext());
        mPointPictureRender.createOnGlThread(getContext());
        mPointRender.createOnGlThread(getContext());
        mPictureRender.createOnGlThread(getContext());
        mLineRender.createOnGlThread(getContext());

        mSession = SessionHelper.getInstance().getSession(getContext());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        if (mSession == null) {
            return;
        }
        displayRotationHelper.updateSessionIfNeeded(mSession);
        try {
            mSession.setCameraTextureName(mBackgroundRenderer.getTextureId());
            Frame frame = mSession.update();
            mBackgroundRenderer.draw(frame);

            Camera camera = frame.getCamera();
            KLog.e(TAG, camera.getTrackingState());

            float[] viewMatrix = new float[16];
            float[] projectMatrix = new float[16];
            camera.getViewMatrix(viewMatrix, 0);
            camera.getProjectionMatrix(projectMatrix, 0, 0.1f, 10f);
            mTrackingState=camera.getTrackingState();
            if (mTrackingState != TrackingState.TRACKING) {
                mArRulerCallBack.showPrompt(true,"状态丢失");
                return;
            }

            drawPointPicture(frame, viewMatrix, projectMatrix);
            //用mCurrentAnchor==null 来控制是否绘制
            if (!isHitTest){
                return;
            }
            //多线程，加锁。防止主线程remove，这里绘制时数组越界。
            synchronized (mAnchorList) {
                if (mTapHelper.poll() != null) {
                    //最多绘制10个点
                    if (mAnchorList.size() >= 10) {
                        mAnchorList.remove(0);
                        mAnchorList.remove(0);
                    }
                    if (mCurrentAnchor != null) {
                        mAnchorList.add(mCurrentAnchor);
                    }
                }

                int count = mAnchorList.size();
                int pointCount = count % 2 == 0 ? count : count - 1;//只渲染锚定的点。
                boolean isSingle = count % 2 != 0;//只渲染锚定的点。
                //渲染点
                for (int i = 0; i < pointCount; i++) {
                    float[] model = mAnchorList.get(i).getPose().getTranslation();

                    mPointRender.upData(model, viewMatrix, projectMatrix);
                    mPointRender.onDraw(END);
                }

                drawLineAll(pointCount, viewMatrix, projectMatrix, camera.getPose());

                if (isSingle) {
                    Anchor anchor = mAnchorList.get(count - 1);
                    mPointRender.upData(anchor.getPose().getTranslation(), viewMatrix, projectMatrix);
                    mPointRender.onDraw(Config.DrawState.DOING);


                    drawLine(Config.DrawState.DOING, anchor.getPose().getTranslation(), mCurrentAnchor.getPose().getTranslation(), viewMatrix, projectMatrix, true, camera.getPose());
                }
            }
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (displayRotationHelper != null)
            displayRotationHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (displayRotationHelper != null)
            displayRotationHelper.onPause();
    }

    public void setAnchorList(List<Anchor> anchorList) {
        mAnchorList = anchorList;
    }

    public void drawPointPicture(Frame frame, float[] viewMatrix, float[] projectionMatrix) {
        Anchor anchor = null;
        KLog.e(TAG, mPoint.toString());
        for (HitResult hitResult : frame.hitTest(mMotionEvent)) {
            Trackable trackable = hitResult.getTrackable();
            if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {
                anchor = hitResult.createAnchor();
                mCurrentAnchor=anchor;
            }
        }

        if (anchor != null) {
            float[] model = new float[16];
            anchor.getPose().toMatrix(model, 0);
            mPointPictureRender.upData(model, viewMatrix, projectionMatrix);
            mPointPictureRender.onDraw();
            KLog.e(TAG, Arrays.toString(anchor.getPose().getTranslation()));
        }
        isHitTest =anchor != null;
        mArRulerCallBack.showPrompt(anchor == null,"检测平面，锚点失败");
    }

    /**
     * 画全部线
     *
     * @param count
     * @param viewMatrix
     * @param projectionMatrix
     */
    private void drawLineAll(int count, float[] viewMatrix, float[] projectionMatrix, Pose pose) {
        //已经测量出距离的直线绘制。
        //1,3,5,7  0,2,4,6
        for (int i = 1; i < count; i = i + 2) {
            Anchor rulerAnchor1 = mAnchorList.get(i - 1);
            Pose pose1 = rulerAnchor1.getPose();
            Anchor rulerAnchor2 = mAnchorList.get(i);
            Pose pose2 = rulerAnchor2.getPose();
            float[] point1 = pose1.getTranslation();
            float[] point2 = pose2.getTranslation();
            drawLine(END, point1, point2, viewMatrix, projectionMatrix, true, pose);
        }
    }

    //画一条线
    private void drawLine(@Config.DrawState int type, float[] point1, float[] point2, float[] viewMatrix, float[] projectionMatrix, boolean drawLength, Pose cameraPose) {
        mLineRender.upData(viewMatrix, projectionMatrix, point1, point2);
        mLineRender.onDraw(type);
        if (drawLength) {
            //计算距离
            double y = point1[1] - point2[1];
            double x = point1[0] - point2[0];
            double z = point1[2] - point2[2];
            double length = Math.sqrt(x * x + y * y + z * z);
            DecimalFormat df = new DecimalFormat("0.00");
            String lineLength = df.format(length) + "m";
            //绘制测量结果的贴图
            if (JudgePointPosition(point1, point2, cameraPose)) {
                mPictureRender.upData(point1, point2, viewMatrix, projectionMatrix, lineLength, END);
            }
            mPictureRender.onDraw();
        }
    }

    private boolean JudgePointPosition(float[] point1, float[] point2, Pose cameraPose) {
        //12,13,14
        float[] center = new float[3];
        center[0] = (point2[0] + point1[0]) / 2;
        center[1] = (point2[1] + point1[1]) / 2;
        center[2] = (point2[2] + point1[2]) / 2;

        float[] cameraNormal = {0, 0, -1, 1};
        float[] cameraMatrix = new float[16];
        cameraPose.toMatrix(cameraMatrix, 0);
        Matrix.multiplyMV(cameraNormal, 0, cameraMatrix, 0, cameraNormal, 0);
        return MathUtil.pointMultiplication(center, cameraNormal) > 0;
    }


    public void setPoint(Point point) {
        mPoint = point;
    }

    public void setMotionEvent(MotionEvent motionEvent) {
        mMotionEvent = motionEvent;
    }

    public void setTapHelper(TapHelper tapHelper) {
        mTapHelper = tapHelper;
    }

    public void setArRulerCallBack(ArRulerCallBack arRulerCallBack) {
        mArRulerCallBack = arRulerCallBack;
    }

    public TrackingState getTrackingState(){
        return mTrackingState;
    }

    public boolean isHitTest() {
        return isHitTest;
    }
}
