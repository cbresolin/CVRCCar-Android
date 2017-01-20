/*
 * This file is part of the Autonomous Android Vehicle (AAV) application.
 *
 * AAV is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AAV is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AAV.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kreolite.cvrccar.ColorBlobDetection;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Point;

public class CarController {
	private static final String _TAG = "CarController";

    /* Pan values */
    private static final int MAX_RIGHT_PAN_PWM = 2250;
    private static final int MAX_LEFT_PAN_PWM = 565;
    private static final int CENTER_PAN_PWM = 1385;
    private static final int REDUCED_PAN_FACTOR = 400;
    private static final int PAN_INCREMENT = 20;
    private static final double PAN_RANGE = (MAX_RIGHT_PAN_PWM - REDUCED_PAN_FACTOR) - (MAX_LEFT_PAN_PWM + REDUCED_PAN_FACTOR);
    private double mPwmPan;
    private boolean mIsSearchingRight=false;
    private boolean mIsSearchingLeft=false;

    /* Steering values */
    private static final int MAX_RIGHT_STEERING_PWM = 1920;
    private static final int MAX_LEFT_STEERING_PWM = 1340;
    private static final int CENTER_STEERING_PWM = 1640;
    private static final double STEERING_RANGE = MAX_RIGHT_STEERING_PWM - MAX_LEFT_STEERING_PWM;
    private double mPwmSteering;

	/* Throttle values */
    private static final int MOTOR_FORWARD_PWM = 1560;
    private static final int MOTOR_REVERSE_PWM = 1370;
    private static final int MOTOR_NEUTRAL_PWM = 1490;
    private double mPwmMotor;

	// IRSensors _irSensors;

	public CarController() {
		// set the pulse width to be exactly the middle
        mPwmPan = CENTER_PAN_PWM;
        mPwmMotor = MOTOR_NEUTRAL_PWM;
		mPwmSteering = CENTER_STEERING_PWM;

		// _irSensors = new IRSensors();
	}

    public synchronized boolean isReversing() {
        if (mPwmMotor == MOTOR_REVERSE_PWM)
            return true;
        else
            return false;
    }

	public synchronized String getPWMValuesToJson() {
		try {
			JSONObject jsonObj = new JSONObject();
            jsonObj.put("pan", (int) mPwmPan);
            jsonObj.put("steering", (int) mPwmSteering);
            jsonObj.put("throttle", (int) mPwmMotor);
            return (jsonObj.toString() + ";");
		} catch (JSONException e) {
			Log.e(_TAG, e.getMessage());
		}
		return null;
	}

    public String getPWMNeutralValuesToJson() {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("pan", (int) mPwmPan);
            jsonObj.put("steering", (int) mPwmSteering);
            jsonObj.put("throttle", MOTOR_NEUTRAL_PWM);
            return (jsonObj.toString() + ";");
        } catch (JSONException e) {
            Log.e(_TAG, e.getMessage());
        }
        return null;
    }

	public void updateTargetPWM(Point screenCenter,
                                Point targetCenter,
                                double forwardBoundaryPercent,
                                double reverseBoundaryPercent,
                                boolean isObstacle) throws InterruptedException {

        double panSteeringConvert;

        // Compute pan
        updatePanPwm(screenCenter, targetCenter);

        // Compute throttle
        if (targetCenter.y < (screenCenter.y - forwardBoundaryPercent*screenCenter.y * 2) && !isObstacle)
            mPwmMotor = MOTOR_FORWARD_PWM;
        else if (targetCenter.y > (screenCenter.y + reverseBoundaryPercent * screenCenter.y * 2))
            mPwmMotor = MOTOR_REVERSE_PWM;
        else mPwmMotor = MOTOR_NEUTRAL_PWM;

        // Compute steering
        if (!isReversing()) {
            panSteeringConvert = ((STEERING_RANGE / PAN_RANGE) * mPwmPan + (MAX_RIGHT_STEERING_PWM - (MAX_RIGHT_PAN_PWM - REDUCED_PAN_FACTOR) * (STEERING_RANGE / PAN_RANGE)));
            mPwmSteering = constrain(panSteeringConvert, MAX_LEFT_STEERING_PWM, MAX_RIGHT_STEERING_PWM);
        }
        else {
            panSteeringConvert = ((-STEERING_RANGE / PAN_RANGE) * mPwmPan + (MAX_RIGHT_STEERING_PWM - (MAX_LEFT_PAN_PWM + REDUCED_PAN_FACTOR) * (-STEERING_RANGE / PAN_RANGE)));
            mPwmSteering = constrain(panSteeringConvert, MAX_LEFT_STEERING_PWM, MAX_RIGHT_STEERING_PWM);
        }
	}

    public void reset() {
        mPwmPan = CENTER_PAN_PWM;
        mPwmSteering = CENTER_STEERING_PWM;
        mPwmMotor = MOTOR_NEUTRAL_PWM;
	}

    private double constrain(double input, double min, double max) {
        return (input <= min) ? min : (input >= max) ? max : input;
    }

    private void updatePanPwm(Point screenCenterPoint, Point targetCenterPoint) {
        // --- Set up objects to calculate the error
        Point setpoint = new Point(0, 0);
        Point mIncrement = new Point(0, 0);

        final int MID_SCREEN_BOUNDARY = (int) (screenCenterPoint.x * 2 * 30) / 352; // 30 when screen size = 352, 288

        setpoint.x = screenCenterPoint.x - targetCenterPoint.x;
        if (setpoint.x < -MID_SCREEN_BOUNDARY || setpoint.x > MID_SCREEN_BOUNDARY) {
            mIncrement.x = setpoint.x * 0.20;
            mPwmPan -= mIncrement.x;
            mPwmPan = constrain(mPwmPan, MAX_LEFT_PAN_PWM, MAX_RIGHT_PAN_PWM);
        }
    }

    public void searchTarget() {
        mPwmMotor = MOTOR_NEUTRAL_PWM;

        if (mPwmPan >= MAX_RIGHT_PAN_PWM) {
            mPwmPan -= PAN_INCREMENT;
            mIsSearchingLeft = true;
            mIsSearchingRight = false;
        }
        else if (mPwmPan <= MAX_LEFT_PAN_PWM) {
            mPwmPan += PAN_INCREMENT;
            mIsSearchingLeft = false;
            mIsSearchingRight = true;
        }
        else {
            if (!mIsSearchingLeft && !mIsSearchingRight)
            {
                mPwmPan -= PAN_INCREMENT;
                mIsSearchingLeft = true;
            }
            else if (mIsSearchingLeft) mPwmPan -= PAN_INCREMENT;
            else mPwmPan += PAN_INCREMENT;
        }
        mPwmPan = constrain(mPwmPan, MAX_LEFT_PAN_PWM, MAX_RIGHT_PAN_PWM);
    }
}