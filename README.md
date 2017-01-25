# CVRCCar-Android
Android app using OpenCV to control RC car over BT serial link

# Presentation
This Android app makes a RC car following a tracked object based on its color.
The actual car control is implemented by https://github.com/cbresolin/CVRCCar-Arduino
project based on Arduino Uno.

# Features

## Object Tracking using opencv
This app tracks an object thanks to smartphone camera using opencv library.
It uses contour detection based on a color pre-filtered image. Once contour is
detected, it constructs a circle with an area equal to detected contour's.
From this circle, the app determines its center and radius. Smallest radius are
ignored to avoid noise. Color is chosen by user by tapping on smartphone screen.
Tracking is only based on color, not shape.

## Navigation

### Pan
Car pan is used to makes sure smartphone camera is able to follow the object with
almost any angle. When tracked object center is below of above certain values,
the pan servo is updated such as to keep tracked object at center of camera.

If for some reason the tracked object is no more in camera view, a scanning procedure
is launched. Camera is turning back and forth to search for the object. As soon
as the object is found again, tracking resumes and car is driving towards the object.

### steering
Car steering is set according to pan values. Steering is set proportionally to pan.
Steering maximum/minimum is reached before pan maximum/minimum does.
This is to ensure car reacts quickly enough to always keep object in camera view.

### Throttle
Camera screen is split in 3 horizontal sections (from bottom to top):
- reverse section; car throttle shall reverse
- neutral section; car throttle shall go to neutral
- forward section; car throttle shall go forward

Those 3 sections boundaries are set by user thanks to this app settings.
They are expressed in % of total screen height (whatever screen resolution).
For example:
- 10% for forward boundary means 10% of screen height starting from screen mid height
upwards
- 25% for reverse boundary means 25% of screen height starting from screen mid height downwards

## Communication
Communication between Android app and Arduino is serial using Bluetooth.
BT communication enables free movements of Android smartphone used to track the
object.

## Settings
- BT device name to connect to (Arduino module HC-05)
- Screen resolutions (4 choices)
- Forward & Reverse boundaries
- Minimum radius avoiding noise

# Reference
- https://www.androidexperiments.com/experiment/autonomous-android-vehicle
- http://docs.opencv.org/2.4/doc/tutorials/imgproc/table_of_content_imgproc/table_of_content_imgproc.html#table-of-content-imgproc
- http://docs.opencv.org/2.4/modules/refman.html
- https://sourceforge.net/projects/opencvlibrary/files/opencv-android/3.2.0/opencv-3.2.0-android-sdk.zip/download
- http://www.allaboutcircuits.com/projects/communicate-with-your-arduino-through-android/
- https://developer.android.com/guide/topics/connectivity/bluetooth.html
- https://www.intorobotics.com/how-to-develop-simple-bluetooth-android-application-to-control-a-robot-remote/
- https://wingoodharry.wordpress.com/2014/04/15/android-sendreceive-data-with-arduino-using-bluetooth-part-2/
- https://felhr85.net/2014/11/11/usbserial-a-serial-port-driver-library-for-android-v2-0/
