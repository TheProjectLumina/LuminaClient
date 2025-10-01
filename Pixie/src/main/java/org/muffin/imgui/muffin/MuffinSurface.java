package org.muffin.imgui.muffin;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MuffinSurface extends GLSurfaceView implements Renderer {

    //Native
    public static native void Init(Surface surface);

    public static native boolean Initialized();

    public static native void SurfaceChanged(GL10 gl, int width, int height);

    public static native void Tick(MuffinSurface imGuiSurface);

    public static native void Shutdown();

    
    public native void AddLog(String logMessage);

    
    public void logMessage(String message) {
        if (Initialized()) { 
            AddLog(message); 
        } else {
            
            android.util.Log.e("MuffinSurface", "Native side not initialized, cannot add log: " + message);
        }
    }

    public MuffinSurface(Context context) {
        super(context);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setEGLContextClientVersion(3);
        setRenderer(this);
    }

    public static void tryShutdown() {
        if (Initialized())
            Shutdown();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Init(getHolder().getSurface());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        SurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Tick(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        tryShutdown();
    }

}
