package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.opengl.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.android.ar_ruler_kt.IViewInterface
import com.android.ar_ruler_kt.helper.DisplayRotationHelper
import com.google.ar.core.*
import java.util.concurrent.ConcurrentLinkedQueue
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author：TianLong
 * @date：2022/6/27 23:32
 * @detail：背景渲染类
 */
class BackgroundSurface: GLSurface ,SessionImpl {
    lateinit var backgroundRenderer:BackgroundRenderer
    lateinit var bitmapRenderer: BitmapRenderer
    lateinit var pointRenderer: PointRenderer
    lateinit var lineRenderer: LineRenderer
    lateinit var pictureRenderer: PictureRenderer
    val motionEvent:MotionEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0f, 0f, 0) // 没有具体意义
    var viewMatrix = FloatArray(16)
    var projectMatrix = FloatArray(16)
    var iViewInterface:IViewInterface? = null
    private val anchorQueue:ConcurrentLinkedQueue<MotionEvent> by lazy {ConcurrentLinkedQueue<MotionEvent>() }
    private val limitsSize = 10 // 点的上限个数
    private val anchorList = ArrayList<Anchor>(limitsSize)
    private val displayRotationHelper by lazy { DisplayRotationHelper(context) }
    override var session : Session? = null
    var detectPointOrPlane = false

    constructor(context: Context) : super(context,null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        initialize(context)
    }

    private fun initialize(context: Context){
        backgroundRenderer = BackgroundRenderer(context)
        bitmapRenderer= BitmapRenderer(context)
        pointRenderer = PointRenderer(context)
        lineRenderer = LineRenderer(context)
        pictureRenderer = PictureRenderer(context)
        // 设置为单位矩阵
        Matrix.setIdentityM(viewMatrix,0)
        Matrix.setIdentityM(projectMatrix,0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
        backgroundRenderer.onSurfaceCreated()
        bitmapRenderer.onSurfaceCreated()
        pointRenderer.onSurfaceCreated()
        lineRenderer.onSurfaceCreated()
        pictureRenderer.onSurfaceCreated()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl,width,height)
        displayRotationHelper.onSurfaceChanged(width, height)
        backgroundRenderer.onSurfaceChanged(width,height)
        bitmapRenderer.onSurfaceChanged(width,height)
        pointRenderer.onSurfaceChanged(width,height)
        pictureRenderer.onSurfaceChanged(width,height)
    }

    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)
        session?.run {
            displayRotationHelper.updateSessionIfNeeded(session)
            this.setCameraTextureName(backgroundRenderer.textureIds[0])
            // ARCore 更新frame 数据
            val frame = this.update()
            // 从ARCore获取顶点数据和纹理数据
            if (frame.hasDisplayGeometryChanged()) {
                frame.transformCoordinates2d(
                    Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
                    backgroundRenderer.vertexBuffer,
                    Coordinates2d.TEXTURE_NORMALIZED,
                    backgroundRenderer.textureBuffer)
            }

            if (frame.timestamp == 0L) {
                detectFailed("Time is 0")
                return
            }
            backgroundRenderer.onDrawFrame()

            val tt = System.currentTimeMillis()
            val camera = frame.camera

            if (camera.trackingState!=TrackingState.TRACKING){
               detectFailed(camera.trackingState.name)
                Log.e(TAG,"It's error  because camera trackingState is ${camera.trackingState.name}")
                return
            }

            camera.getViewMatrix(viewMatrix,0)
            camera.getProjectionMatrix(projectMatrix,0,0.01f,10f)
            // 暂时在此处渲染
            drawPoint()
            drawLine(null,anchorList,viewMatrix,projectMatrix)

            val pointX = width/2F
            val pointY = height/2F
            val hitResults =frame.hitTest(pointX,pointY) // 检测点为屏幕正中央

            // 锚点不知道是否准确
            if (hitResults.isNotEmpty() and (hitResults.size>0)){
                val hitResult = hitResults.last()
                val type = trackable(hitResult.trackable) // 这一步仅仅是打印hitResult的trackable，没有任何实际意义

                val trackable = hitResult.trackable

//                if ((trackable is Plane ) && trackable.isPoseInPolygon(hitResult.hitPose)){ // 追踪类型为平面，且锚点在平面上，检测太为苛刻
                if((trackable is Plane ) or (trackable is Point) or (trackable is DepthPoint )){// 追踪类型为平面，点，深度点即可
                    val anchor : Anchor
                    try {
                        anchor = hitResult.createAnchor()

                        if (anchor.trackingState == TrackingState.TRACKING){
                            detectSuccess("Anchor: $type ${anchor.trackingState.name} ")
                            // 获取点的位置
                            val pose = FloatArray(16)
                            anchor.pose.toMatrix(pose ,0)

                            // 渲染Bitmap(圆圈bitmap)
                            bitmapRenderer.upDateMatrix(pose,viewMatrix,projectMatrix)
                            bitmapRenderer.onDrawFrame()

                            // 填加锚点
                            addAnchorPoint(anchor)
                            Log.e(TAG,"bitmapRenderer.onDrawFrame():${hitResult.distance}")
                        }else{
                           detectFailed(anchor.trackingState.name)
                        }

                        // 暂时在此处渲染
                        drawPoint(true)
                        drawLine(anchor,anchorList,viewMatrix,projectMatrix)
                    }catch (e:Exception){
                        Log.e(TAG,"Exception:$e")
                    }
                }
                else{
                    detectFailed(trackable(hitResult.trackable))
                }
            }else{
                detectFailed("请移动手机，获取特征值")
                return
            }

            Log.w(TAG,"耗时：${System.currentTimeMillis()-tt}")
        }
    }

    private fun trackable(trackable: Trackable):String{
        val msg = when (trackable) {
            is Point -> "Point"
            is Plane -> "Plane"
            is InstantPlacementPoint -> "InstantPlacementPoint"
            is Earth -> "Earth"
            is DepthPoint -> "DepthPoint"
            is AugmentedFace -> "AugmentedFace"
            is AugmentedImage -> "AugmentedImage"
            else -> "Other"
        }
        Log.e(TAG,"trackable is $msg")

        return msg
    }

    fun add(){
        if (detectPointOrPlane){
            anchorQueue.clear()
            anchorQueue.add(motionEvent)
            Log.w(TAG,"add: ${anchorQueue.size}")
        }
    }

    @Synchronized
    private fun addAnchorPoint(anchor: Anchor){
        anchorQueue.poll()?.run {
            anchorList.takeIf {anchorList.size==limitsSize }?.apply {
                anchorList.first().detach()
                anchorList.removeFirst()
                anchorList.first().detach()
                anchorList.removeFirst()
            }
            anchorList.add(anchor)
        }
    }
    @Synchronized
    fun delete(){
        if (detectPointOrPlane && anchorList.isNotEmpty()){
            if (anchorList.size%2==0){
                Log.w(TAG,"delete:1 ${anchorList.size}")
                anchorList.last().detach()
                Log.w(TAG,"delete:2 ${anchorList.size}")
                anchorList.removeLast()
                Log.w(TAG,"delete:3 ${anchorList.size}")
                anchorList.last().detach()
                Log.w(TAG,"delete:4 ${anchorList.size}")
                anchorList.removeLast()
                Log.w(TAG,"delete:5 ${anchorList.size}")
            }else{
                Log.w(TAG,"delete:6 ${anchorList.size}")
                anchorList.last().detach()
                Log.w(TAG,"delete:7 ${anchorList.size}")
                anchorList.removeLast()
                Log.w(TAG,"delete:8 ${anchorList.size}")
            }

            Log.w(TAG,"delete: ${anchorList.size}")
        }
    }


    /**
     * Draw line
     * 画线
     * @param currentAnchor 当前锚点
     * @param list 锚点list
     * @param view 视矩阵
     * @param project 投影矩阵
     */
    @Synchronized
    private fun drawLine(currentAnchor: Anchor?, list:ArrayList<Anchor>, view:FloatArray, project:FloatArray){
            val size = list.size / 2
            for (index in 0 until size){
                // 两个点效果一样。所以注掉
                val pose1 = list[index*2].pose.translation
                val pose2 = list[index*2+1].pose.translation
                val point1 = floatArrayOf(
                    pose1[0],pose1[1],pose1[2],
                    pose2[0],pose2[1],pose2[2],
                )
                lineRenderer.vertexBuffer.put(point1).position(0)
                lineRenderer.upDateMatrix(view,project)
                lineRenderer.onDrawFrame()

                drawPicture(list[index*2].pose, list[index*2+1].pose,view,project)
            }

        // 当前锚点不为空时，进行渲染
        currentAnchor?.run {
            val isAnchor = list.isNotEmpty() && list.size % 2 != 0
            if (isAnchor){
                val anchor = list.last()
                val pose1 = anchor.pose
                val pose2 = this.pose
                var data = floatArrayOf(
                    pose1.tx(),pose1.ty(),pose1.tz(),
                    pose2.tx(),pose2.ty(),pose2.tz()
                )

                lineRenderer.vertexBuffer.put(data).position(0)
                lineRenderer.upDateMatrix(view,project)
                lineRenderer.onDrawFrame()

                drawPicture(pose1,pose2,view,project)
            }
        }
    }

    /**
     * Draw point
     * 画点
     * @param draw 当点个数为单数时，是否画单独的那个点
     */
    @Synchronized
    private fun drawPoint(draw:Boolean = false){
        if (anchorList.isNullOrEmpty()){
            return
        }
        val remainder = anchorList.size % 2
        var size = anchorList.size - 1
        if (!draw && remainder!=0){
            size -= 1
        }

        for (i in 0..size){
            val pose = FloatArray(16)
            val anchor = anchorList[i]
            anchor.pose.toMatrix(pose ,0)
            pointRenderer.upDateMatrix(pose,viewMatrix,projectMatrix)
            pointRenderer.onDrawFrame()
        }
    }

    @Synchronized
    fun drawPicture(pose1:Pose,pose2:Pose,view: FloatArray,project: FloatArray){
        // 计算两个点之间的距离
        val length = pictureRenderer.length(pose1,pose2)
        val res = String.format("%.2f", length)
        // 获取将要绘制的bitmap

        pictureRenderer.setLength2Bitmap("${res}m")
        // 更新顶点坐标
        pictureRenderer.upDataVertex(pose1,pose2,view)
        // 更新MVP矩阵，进行绘制
        pictureRenderer.upDatePMatrix(project)
        pictureRenderer.onDrawFrame()
    }


    private fun detectSuccess(msg:String = "检测成功"){
        detectPointOrPlane=true
        iViewInterface?.detectSuccess(msg)
    }

    private fun detectFailed(msg:String = "检测失败"){
        detectPointOrPlane=false
        iViewInterface?.detectFailed(msg)
    }
}