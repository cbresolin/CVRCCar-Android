package com.kreolite.cvrccar.ColorBlobDetection;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.SurfaceView;
import android.widget.Toast;

import com.kreolite.cvrccar.BluetoothService.BluetoothService;
import com.kreolite.cvrccar.BluetoothService.Constants;
import com.kreolite.cvrccar.R;

import java.lang.ref.WeakReference;
import java.util.List;

public class ColorBlobDetectionActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {
    private static final String                TAG = "ColorBlobDetectActivity";
    private static final int                   ZOOM = 5;
    private static Scalar                      COLOR_RADIUS = new Scalar(5,50,200,0);
    private static final int                   REQUEST_ENABLE_BT = 0;

    private Size                               SCREEN_SIZE;
    private Size                               SPECTRUM_SIZE;
    private Scalar                             CONTOUR_COLOR;
    private Scalar                             BOUNDARIES_COLOR;
    volatile Point                             mTargetCenter = new Point(-1, -1);
    private Point                              mScreenCenter = new Point(-1, -1);
    private int                                mTargetNum = 0;
    private boolean                            mIsColorSelected = false;
    private Mat                                mRgba;
    private Scalar                             mBlobColorRgba;
    private Scalar                             mBlobColorHsv;
    private ColorBlobDetector                  mDetector;
    private Mat                                mSpectrum;
    private CameraBridgeViewBase               mOpenCvCameraView;
    private CarController                      mCarController;
    private MyHandler                          mHandler;
    private SharedPreferences                  mSharedPref;
    private double                             mForwardBoundaryPercent = -0.15;
    private double                             mReverseBoundaryPercent = 0.3;
    private int                                mMinRadius = 15;
    private String                             mLastPwmJsonValues = "";
    private boolean                            mIsReversingHandled = false;
    private int                                mCountOutOfFrame = 0;
    private BluetoothAdapter                   mBluetoothAdapter = null;
    private BluetoothService                   mBluetoothService = null;
    private String                             mBluetoothDeviceName = null;
    private BluetoothDevice                    mBtDevice = null;
    private boolean                            mIsObstacle = false;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ColorBlobDetectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_blob_detection_surface_view);

        mSharedPref = getApplicationContext().getSharedPreferences(getString(R.string.settings_file),
                Context.MODE_PRIVATE);
        boolean isReso1 = mSharedPref.getBoolean(getString(R.string.is_reso1), false);
        boolean isReso2 = mSharedPref.getBoolean(getString(R.string.is_reso2), true);
        boolean isReso3 = mSharedPref.getBoolean(getString(R.string.is_reso3), false);

        // 1920x1080, 1280x960, 800x480 else 352x288
        if (isReso1) {
            SCREEN_SIZE = new Size(1920, 1080);
        } else if (isReso2) {
            SCREEN_SIZE = new Size(1280, 960);
        } else if (isReso3) {
            SCREEN_SIZE = new Size(800, 480);
        }  else {
            SCREEN_SIZE = new Size(352, 288);
        }

        mForwardBoundaryPercent = Double.parseDouble(mSharedPref.getString(getString(R.string.forward_boundary_percent), "-10")) / 100;
        mReverseBoundaryPercent = Double.parseDouble(mSharedPref.getString(getString(R.string.reverse_boundary_percent), "25")) / 100;
        mMinRadius = Integer.parseInt(mSharedPref.getString(getString(R.string.minimum_radius_value), "15"));

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setMaxFrameSize((int) SCREEN_SIZE.width, (int) SCREEN_SIZE.height);

        mCarController = new CarController();
        mCountOutOfFrame = 0;

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            CharSequence text = "Device does not support BT!";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();
            finish();
        }
        else
            // Get BT device name to connect to from settings
            mBluetoothDeviceName = mSharedPref.getString(getString(R.string.bt_device_name), "HC-05");

        mHandler = new MyHandler(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mBluetoothService == null) {
            setupBtService();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        hideNavigationBar();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothService != null) {

            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth services
                mBluetoothService.start();
            }

            if (mBluetoothService.getState() == BluetoothService.STATE_PAIRED) {
                // Get paired device for connection
                mBtDevice = mBluetoothService.getBtDevice();
                mBluetoothService.connect(mBtDevice);
            }
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null) mOpenCvCameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) mOpenCvCameraView.disableView();
        if (mBluetoothService != null) mBluetoothService.stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                setupBtService();
            }
            else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                CharSequence text = "BT not enabled!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                toast.show();
                finish();
            }
        }
    }

    private void setupBtService() {
        // Initialize the BluetoothService to perform bluetooth connections
        mBluetoothService = new BluetoothService(this, mHandler);
        mBluetoothService.setBtDeviceName(mBluetoothDeviceName);
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mDetector.setColorRadius(COLOR_RADIUS);
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,255,10,255);
        BOUNDARIES_COLOR = new Scalar(255,0,0,255);
        mScreenCenter.x = width / 2;
        mScreenCenter.y = height / 2;
        mIsColorSelected = false;
    }

    public void onCameraViewStopped() {
        mTargetNum = 0;
        mTargetCenter.x = -1;
        mTargetCenter.y = -1;
        mCarController.reset();
        updateCarPwms();
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>ZOOM) ? x-ZOOM : 0;
        touchedRect.y = (y>ZOOM) ? y-ZOOM : 0;

        touchedRect.width = (x+ZOOM < cols) ? x + ZOOM - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+ZOOM < rows) ? y + ZOOM - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }

    private void displayContours(Mat matRgba){
        Point centers = new Point(-1, -1);

        mDetector.findContours(matRgba);
        List<MatOfPoint> contours = mDetector.getContours();
        mTargetNum = contours.size();
        Log.e(TAG, "Target count: " + mTargetNum);

        MatOfPoint2f points = new MatOfPoint2f();
        float[] targetRadius = new float[mTargetNum];
        for (int i = 0, n = mTargetNum; i < n; i++) {
            // contours.get(x) is a single MatOfPoint, but to use minEnclosingCircle
            // we need to pass a MatOfPoint2f so we need to do a conversion
            contours.get(i).convertTo(points, CvType.CV_32FC2);
            Imgproc.minEnclosingCircle(points, centers, targetRadius);

            if (targetRadius[i] > mMinRadius) {
                mTargetCenter = centers;
                Imgproc.circle(matRgba, mTargetCenter, 3, CONTOUR_COLOR, Core.FILLED);
                Imgproc.circle(matRgba, mTargetCenter, (int) targetRadius[i], CONTOUR_COLOR, 2, 0, 0);
                Log.i(TAG, "Target Center [" + i + "]= " + mTargetCenter);
                Log.i(TAG, "Target Radius [" + i + "]= " + targetRadius[i]);
            }
        }
    }

    private void displayBoundaries(Mat matRgba){

        Point topLeft = new Point(0, mScreenCenter.y - mForwardBoundaryPercent*mScreenCenter.y*2);
        Point bottomRight = new Point(mScreenCenter.x*2, mScreenCenter.y + mReverseBoundaryPercent*mScreenCenter.y*2);
        Imgproc.rectangle(matRgba, topLeft, bottomRight, BOUNDARIES_COLOR, 2);
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        if (mIsColorSelected) {
            displayContours(mRgba);
            displayBoundaries(mRgba);

            Mat colorLabel = mRgba.submat(mRgba.rows()-68, mRgba.rows()-4, 4, 68);
            colorLabel.setTo(mBlobColorRgba);

            Mat spectrumLabel = mRgba.submat(mRgba.rows()-(mSpectrum.rows()+4), mRgba.rows()-4, 70, 70 + mSpectrum.cols());
            mSpectrum.copyTo(spectrumLabel);

            updateCarPwms();
        }
        return mRgba;
    }

    private void hideNavigationBar() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }

    private static class MyHandler extends Handler {
        private final WeakReference<ColorBlobDetectionActivity> mActivity;

        MyHandler(ColorBlobDetectionActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_PAIRED:
                            Toast.makeText(mActivity.get(), "Device paired!",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Toast.makeText(mActivity.get(), "Connecting device...",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(mActivity.get(), "Device connected!",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_NONE:
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    /*byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);*/
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.i(TAG, "Obstacle found = " + readMessage);
                    if (readMessage.contentEquals("1"))
                        mActivity.get().mIsObstacle = true;
                    else
                        mActivity.get().mIsObstacle = false;
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(mActivity.get(), msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private void updateCarPwms(){
        String pwmJsonValues, pwmJsonNeutralValues;

        try {
            if (mTargetNum > 0) {
                mCarController.updateTargetPWM(mScreenCenter,
                        mTargetCenter,
                        mForwardBoundaryPercent,
                        mReverseBoundaryPercent,
                        mIsObstacle);
                mCountOutOfFrame = 0;
            } else {
                mCountOutOfFrame++;
                if (mCountOutOfFrame > 2) {
                    mTargetCenter.x = -1;
                    mTargetCenter.y = -1;
                    mCountOutOfFrame = 0;
                    mCarController.searchTarget();
                }
            }

            pwmJsonValues = mCarController.getPWMValuesToJson();
            if ((pwmJsonValues != null) && !pwmJsonValues.contentEquals(mLastPwmJsonValues)) {
                Log.i(TAG, "Update Actuator ...");

                if (mBluetoothService != null && mBluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
                    if (!mCarController.isReversing()) {
                        Log.i(TAG, "Sending PWM values: " + pwmJsonValues);
                        mBluetoothService.write(pwmJsonValues.getBytes());
                        mIsReversingHandled = false;
                    }
                    else {
                        Log.i(TAG, "Sending PWM values: " + pwmJsonValues);
                        mBluetoothService.write(pwmJsonValues.getBytes());

                        // When reversing, need to send neutral first
                        if (!mIsReversingHandled) {
                            pwmJsonNeutralValues = mCarController.getPWMNeutralValuesToJson();
                            Log.i(TAG, "Sending PWM values: " + pwmJsonNeutralValues);
                            mBluetoothService.write(pwmJsonNeutralValues.getBytes());

                            Log.i(TAG, "Sending PWM values: " + pwmJsonValues);
                            mBluetoothService.write(pwmJsonValues.getBytes());
                            mIsReversingHandled = true;
                        }
                    }
                }
                mLastPwmJsonValues = pwmJsonValues;
            }
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
    }
}