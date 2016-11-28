package com.kreolite.androvision;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ColorBlobDetector {
    // Lower and Upper bounds for range checking in HSV color space
    private Scalar             mLowerBound = new Scalar(0);
    private Scalar             mUpperBound = new Scalar(0);
    // Minimum contour area in percent for contours filtering
    private static double      mMinContourArea = 0.1;
    // Color radius for range checking in HSV color space
    private Scalar             mColorRadius = new Scalar(25,50,50,0);
    private Mat                mSpectrum = new Mat();
    private List<MatOfPoint>   mContours = new ArrayList<MatOfPoint>();
    private Mat                mCircles = new Mat();
    private int                mMinRadius = 0;
    private int                mMaxRadius = 0;

    // Cache
    Mat mPyrDownMat = new Mat();
    Mat mHsvMat = new Mat();
    Mat mMask = new Mat();
    Mat mDilatedMask = new Mat();
    Mat mHierarchy = new Mat();

    public void setColorRadius(Scalar radius) {
        mColorRadius = radius;
    }

    public void setHsvColor(Scalar hsvColor) {
        double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0] - mColorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0] + mColorRadius.val[0] <= 179) ? hsvColor.val[0] + mColorRadius.val[0] : 179;
        mLowerBound.val[0] = minH;
        mUpperBound.val[0] = maxH;

        double minS = (hsvColor.val[1] >= mColorRadius.val[1]) ? hsvColor.val[1] - mColorRadius.val[1] : 0;
        double maxS = (hsvColor.val[1] + mColorRadius.val[1] <= 255) ? hsvColor.val[1] + mColorRadius.val[1] : 255;
        mLowerBound.val[1] = minS;
        mUpperBound.val[1] = maxS;

        double minV = (hsvColor.val[2] >= mColorRadius.val[2]) ? hsvColor.val[2] - mColorRadius.val[2] : 0;
        double maxV = (hsvColor.val[2] + mColorRadius.val[2] <= 255) ? hsvColor.val[2] + mColorRadius.val[2] : 255;
        mLowerBound.val[2] = minV;
        mUpperBound.val[2] = maxV;

        mLowerBound.val[3] = 0;
        mUpperBound.val[3] = 255;

        Mat spectrumHsv = new Mat(1, (int)(maxH-minH), CvType.CV_8UC3);

        for (int j = 0; j < maxH-minH; j++) {
            byte[] tmp = {(byte)(minH+j), (byte)255, (byte)255};
            spectrumHsv.put(0, j, tmp);
        }

        Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
    }

    public Mat getSpectrum() {
        return mSpectrum;
    }

    public void setMinContourArea(double area) {
        mMinContourArea = area;
    }

    public void findContours(Mat rgbaImage) {
        // Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        // Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);
        // Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

        Imgproc.cvtColor(rgbaImage, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);
        Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
        Imgproc.dilate(mMask, mDilatedMask, new Mat());
        Imgproc.erode(mDilatedMask, mDilatedMask, new Mat());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea)
                maxArea = area;
        }

        // Filter contours by area and resize to fit the original image size
        mContours.clear();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea*maxArea) {
                // Core.multiply(contour, new Scalar(4,4,0,0), contour);
                mContours.add(contour);
            }
        }
    }
    public List<MatOfPoint> getContours() {
        return mContours;
    }

    public void findCircles(Mat rgbaImage) {
        Imgproc.medianBlur(rgbaImage, rgbaImage, 3);
        Imgproc.cvtColor(rgbaImage, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);
        Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
        Imgproc.dilate(mMask, mDilatedMask, new Mat());
        Imgproc.GaussianBlur(mDilatedMask, mDilatedMask, new Size(9,9), 2, 2);
        Imgproc.HoughCircles(mDilatedMask, mCircles, Imgproc.CV_HOUGH_GRADIENT, 1, mDilatedMask.rows()/8, 100, 25, mMinRadius, mMaxRadius);
    }
    public Mat getCircles() {
        return mCircles;
    }

    public void setMinRadius(int minRadius) { mMinRadius = minRadius; }

    public void setMaxRadius(int maxRadius) { mMaxRadius = maxRadius; }
}
