package com.kreolite.cvrccar.ColorBlobDetection;

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
    private static double      mMinContourAreaRatio = 0.2;
    // Color radius for range checking in HSV color space
    private Scalar             mColorRadius = new Scalar(12,50,50,0);
    private Mat                mSpectrum = new Mat();
    private List<MatOfPoint>   mContours = new ArrayList<>();

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
        // Ensure HSV colors are within range
        hsvColor.val[0] = (hsvColor.val[0] <= 0) ? 0 : (hsvColor.val[0] >= 179) ? 179 : hsvColor.val[0];
        hsvColor.val[1] = (hsvColor.val[1] <= 0) ? 0 : (hsvColor.val[1] >= 255) ? 255 : hsvColor.val[1];
        hsvColor.val[2] = (hsvColor.val[2] <= 0) ? 0 : (hsvColor.val[2] >= 255) ? 255 : hsvColor.val[2];

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
        mMinContourAreaRatio = area;
    }

    public void findContours(Mat rgbaImage) {
        Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        Imgproc.GaussianBlur(mPyrDownMat, mPyrDownMat, new Size(5,5), 2, 2);
        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);
        Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
        Imgproc.dilate(mMask, mDilatedMask, new Mat());
        Imgproc.pyrUp(mDilatedMask, mDilatedMask);

        List<MatOfPoint> contours = new ArrayList<>();
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

        // Filter contours by area
        mContours.clear();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourAreaRatio * maxArea) {
                mContours.add(contour);
            }
        }
    }
    public List<MatOfPoint> getContours() {
        return mContours;
    }
}