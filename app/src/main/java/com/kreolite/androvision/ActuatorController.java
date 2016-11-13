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

import java.util.Random;

public class ActuatorController {
	private static final String _TAG = "ActuatorController";

    /* Steering values */
	public static final int RIGHT_FULL_TURN_WHEELS_PWM = 1910;
	public static final int LEFT_FULL_TURN_WHEELS_PWM = 1360;
	public static final int CENTER_FRONT_WHEELS_PWM = 1640;
    public double _pwmFrontWheels;

	/* Throttle values */
    public static final int MOTOR_FORWARD_PWM = 1560;
	public static final int MOTOR_REVERSE_PWM = 1370;
	public static final int MOTOR_NEUTRAL_PWM = 1490;
    public double _pwmMotor;

	// IRSensors _irSensors;

	public ActuatorController() {
		// set the pulse width to be exactly the middle
		_pwmMotor = MOTOR_NEUTRAL_PWM;
		_pwmFrontWheels = CENTER_FRONT_WHEELS_PWM;

		// _irSensors = new IRSensors();
	}

    public synchronized boolean isReversing() {
        if (_pwmMotor == MOTOR_REVERSE_PWM)
            return true;
        else
            return false;
    }

	public synchronized String getPWMValuesToJson() {
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("throttle", (int)_pwmMotor);
			jsonObj.put("steering", (int)_pwmFrontWheels);
			return jsonObj.toString();
		} catch (JSONException e) {
			Log.e(_TAG, e.getMessage());
		}
		return null;
	}

    public String getPWMNeutralValuesToJson() {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("throttle", MOTOR_NEUTRAL_PWM);
            jsonObj.put("steering", (int)_pwmFrontWheels);
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

		// Compute throttle
        if (targetCenter.y < (screenCenter.y - forwardBoundaryPercent*screenCenter.y * 2))
            _pwmMotor = MOTOR_FORWARD_PWM;
        else if (targetCenter.y > (screenCenter.y + reverseBoundaryPercent * screenCenter.y * 2))
            _pwmMotor = MOTOR_REVERSE_PWM;
        else _pwmMotor = MOTOR_NEUTRAL_PWM;

        // Compute steering
        if (!isReversing())
            _pwmFrontWheels = ((RIGHT_FULL_TURN_WHEELS_PWM - LEFT_FULL_TURN_WHEELS_PWM) / (screenCenter.x * 2)) * targetCenter.x + LEFT_FULL_TURN_WHEELS_PWM;
        else
            _pwmFrontWheels = ((LEFT_FULL_TURN_WHEELS_PWM - RIGHT_FULL_TURN_WHEELS_PWM) / (screenCenter.x * 2)) * targetCenter.x + RIGHT_FULL_TURN_WHEELS_PWM;
	}

    public void reset() {
		_pwmMotor = MOTOR_NEUTRAL_PWM;
		_pwmFrontWheels = CENTER_FRONT_WHEELS_PWM;
	}

	/*class IRSensors {
		double _sideLeftIR, _sideRightIR, _frontRightIR, _frontLeftIR;

		public boolean foundObstacle() {
			boolean foundObstacle = false;

			if (_frontRightIR > 0.9) {
				_pwmFrontWheels = LEFT_FULL_TURN_WHEELS_PWM;
				// Log.e(_TAG, Double.toString(_frontRightIR));
				foundObstacle = true;
			} else if (_frontLeftIR > 0.9) {
				_pwmFrontWheels = LEFT_FULL_TURN_WHEELS_PWM;
				foundObstacle = true;
			} else if (_sideLeftIR > 1.1) {
				_pwmFrontWheels = RIGHT_FULL_TURN_WHEELS_PWM - 100;
				foundObstacle = true;
			} else if (_sideRightIR > 1.1) {
				_pwmFrontWheels = LEFT_FULL_TURN_WHEELS_PWM - 100;
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