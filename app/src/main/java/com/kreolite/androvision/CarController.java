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

package com.kreolite.androvision;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Point;

public class CarController {
	private static final String _TAG = "CarController";

    /* Pan values */
    private static final int RIGHT_FULL_TURN_PAN_PWM = 2400;
    private static final int LEFT_FULL_TURN_PAN_PWM = 900;
    private static final int CENTER_PAN_PWM = 1680;
    private static final int RANGE_PAN_PWM = RIGHT_FULL_TURN_PAN_PWM - CENTER_PAN_PWM;
    private double mPwmPan;
    private double mLastPwmPan;

    /* Steering values */
    private static final int RIGHT_FULL_TURN_WHEELS_PWM = 1910;
    private static final int LEFT_FULL_TURN_WHEELS_PWM = 1360;
    private static final int CENTER_FRONT_WHEELS_PWM = 1640;
    private static final int RANGE_WHEELS_PWM = RIGHT_FULL_TURN_WHEELS_PWM - CENTER_FRONT_WHEELS_PWM;
    private double mPwmFrontWheels;

	/* Throttle values */
    private static final int MOTOR_FORWARD_PWM = 1560;
    private static final int MOTOR_REVERSE_PWM = 1370;
    private static final int MOTOR_NEUTRAL_PWM = 1490;
    private double mPwmMotor;

    private Point mIncrement = new Point(0, 0);
    private Point mLastCenterPoint = new Point(0, 0);


	// IRSensors _irSensors;

	public CarController() {
		// set the pulse width to be exactly the middle
        mLastPwmPan = mPwmPan = CENTER_PAN_PWM;
        mPwmMotor = MOTOR_NEUTRAL_PWM;
		mPwmFrontWheels = CENTER_FRONT_WHEELS_PWM;

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
            jsonObj.put("steering", (int) mPwmFrontWheels);
            jsonObj.put("throttle", (int) mPwmMotor);
			return jsonObj.toString();
		} catch (JSONException e) {
			Log.e(_TAG, e.getMessage());
		}
		return null;
	}

    public String getPWMNeutralValuesToJson() {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("pan", (int) mPwmPan);
            jsonObj.put("steering", (int) mPwmFrontWheels);
            jsonObj.put("throttle", MOTOR_NEUTRAL_PWM);
            return jsonObj.toString();
        } catch (JSONException e) {
            Log.e(_TAG, e.getMessage());
        }
        return null;
    }

	public void updateTargetPWM(Point screenCenter,
                                Point targetCenter,
                                double forwardBoundaryPercent,
                                double reverseBoundaryPercent) throws InterruptedException {

        // if (!_irSensors.foundObstacle())

        // Compute pan
        updatePanPwm(screenCenter, targetCenter);

        // Compute steering
        if (!isReversing())
            mPwmFrontWheels = ((RIGHT_FULL_TURN_WHEELS_PWM - LEFT_FULL_TURN_WHEELS_PWM) / (screenCenter.x * 2)) * targetCenter.x + LEFT_FULL_TURN_WHEELS_PWM;
        else
            mPwmFrontWheels = ((LEFT_FULL_TURN_WHEELS_PWM - RIGHT_FULL_TURN_WHEELS_PWM) / (screenCenter.x * 2)) * targetCenter.x + RIGHT_FULL_TURN_WHEELS_PWM;

        // Compute throttle
        if (targetCenter.y < (screenCenter.y - forwardBoundaryPercent*screenCenter.y * 2))
            // mPwmMotor = MOTOR_FORWARD_PWM;
            mPwmMotor = MOTOR_NEUTRAL_PWM;
        else if (targetCenter.y > (screenCenter.y + reverseBoundaryPercent * screenCenter.y * 2))
            // mPwmMotor = MOTOR_REVERSE_PWM;
            mPwmMotor = MOTOR_NEUTRAL_PWM;
        else mPwmMotor = MOTOR_NEUTRAL_PWM;
	}

    public void reset() {
        mPwmPan = CENTER_PAN_PWM;
        mPwmFrontWheels = CENTER_FRONT_WHEELS_PWM;
        mPwmMotor = MOTOR_NEUTRAL_PWM;
	}

    private void updateWheelsPWM() {
        mPwmFrontWheels = constrain(1.3 * ((CENTER_PAN_PWM - mPwmPan) / RANGE_PAN_PWM) * RANGE_WHEELS_PWM + CENTER_FRONT_WHEELS_PWM, RIGHT_FULL_TURN_WHEELS_PWM, LEFT_FULL_TURN_WHEELS_PWM);
    }

    private double constrain(double input, double min, double max) {
        return (input < min) ? min : (input > max) ? max : input;
    }

    private void updatePanPwm(Point screenCenterPoint, Point currentCenterPoint) {
        // --- Set up objects to calculate the error and derivative error
        Point error = new Point(0, 0); // The position error
        Point setpoint = new Point(0, 0);
        Point derivativeTerm = new Point(0, 0);

        final double kD_X = 0.8; // Derivative gain (Kd)
        final int MID_SCREEN_BOUNDARY = (int) (screenCenterPoint.x * 2 * 15) / 352; // 15 when screen size = 352, 288

        setpoint.x = (screenCenterPoint.x - currentCenterPoint.x) * 1.35;
        if ((setpoint.x < -MID_SCREEN_BOUNDARY || setpoint.x > MID_SCREEN_BOUNDARY) && currentCenterPoint.x > 0) {
            if (mLastCenterPoint.x != currentCenterPoint.x) {
                mIncrement.x = setpoint.x * 0.18;
                mLastPwmPan = mPwmPan;
            }
            error.x = (mPwmPan - mIncrement.x);

            derivativeTerm.x = (mPwmPan - mLastPwmPan);

            mLastPwmPan = mPwmPan;

            mPwmPan = error.x - constrain(kD_X * derivativeTerm.x, -9, 9);
            mPwmPan = constrain(mPwmPan, LEFT_FULL_TURN_PAN_PWM, RIGHT_FULL_TURN_PAN_PWM);

            mLastCenterPoint.x = currentCenterPoint.x;
        }
    }

	/*class IRSensors {
		double _sideLeftIR, _sideRightIR, _frontRightIR, _frontLeftIR;

		public boolean foundObstacle() {
			boolean foundObstacle = false;

			if (_frontRightIR > 0.9) {
				mPwmFrontWheels = LEFT_FULL_TURN_WHEELS_PWM;
				// Log.e(_TAG, Double.toString(_frontRightIR));
				foundObstacle = true;
			} else if (_frontLeftIR > 0.9) {
				mPwmFrontWheels = LEFT_FULL_TURN_WHEELS_PWM;
				foundObstacle = true;
			} else if (_sideLeftIR > 1.1) {
				mPwmFrontWheels = RIGHT_FULL_TURN_WHEELS_PWM - 100;
				foundObstacle = true;
			} else if (_sideRightIR > 1.1) {
				mPwmFrontWheels = LEFT_FULL_TURN_WHEELS_PWM - 100;
				foundObstacle = true;
			}
			return foundObstacle;
		}

		public void updateIRSensorsVoltage(float sideLeftIR, float sideRightIR, float frontRightIR, float frontLeftIR) {
			_sideLeftIR = sideLeftIR;
			_sideRightIR = sideRightIR;
			_frontRightIR = frontRightIR;
			_frontLeftIR = frontLeftIR;
		}
	}*/
}