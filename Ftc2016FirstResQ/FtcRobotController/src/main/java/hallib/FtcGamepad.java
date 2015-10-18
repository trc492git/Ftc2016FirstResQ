package hallib;

import com.qualcomm.robotcore.hardware.Gamepad;

import trclib.TrcDbgTrace;
import trclib.TrcRobot;
import trclib.TrcTaskMgr;

public class FtcGamepad implements TrcTaskMgr.Task
{
    public static final int GAMEPAD_A           = ((int)1 << 0);
    public static final int GAMEPAD_B           = ((int)1 << 1);
    public static final int GAMEPAD_X           = ((int)1 << 2);
    public static final int GAMEPAD_Y           = ((int)1 << 3);
    public static final int GAMEPAD_BACK        = ((int)1 << 4);
    public static final int GAMEPAD_START       = ((int)1 << 5);
    public static final int GAMEPAD_LBUMPER     = ((int)1 << 6);
    public static final int GAMEPAD_RBUMPER     = ((int)1 << 7);
    public static final int GAMEPAD_LSTICK_BTN  = ((int)1 << 8);
    public static final int GAMEPAD_RSTICK_BTN  = ((int)1 << 9);
    public static final int GAMEPAD_DPAD_LEFT   = ((int)1 << 10);
    public static final int GAMEPAD_DPAD_RIGHT  = ((int)1 << 11);
    public static final int GAMEPAD_DPAD_UP     = ((int)1 << 12);
    public static final int GAMEPAD_DPAD_DOWN   = ((int)1 << 13);

    public interface ButtonHandler
    {
        public void gamepadButtonEvent(
                FtcGamepad gamepad,
                final int btnMask,
                final boolean pressed);
    }   //interface ButonHandler

    private static final String moduleName = "FtcGamepad";
    private static final boolean debugEnabled = false;
    private TrcDbgTrace dbgTrace = null;

    private Gamepad gamepad;
    private ButtonHandler buttonHandler;
    private int prevButtons;
    private int ySign;

    public FtcGamepad(
            final String instanceName,
            Gamepad gamepad,
            ButtonHandler buttonHandler)
    {
        if (debugEnabled)
        {
            dbgTrace = new TrcDbgTrace(
                    moduleName + "." + instanceName,
                    false,
                    TrcDbgTrace.TraceLevel.API,
                    TrcDbgTrace.MsgLevel.INFO);
        }

        if (gamepad == null)
        {
            throw new NullPointerException("Gamepad must not be null");
        }

        this.gamepad = gamepad;
        this.buttonHandler = buttonHandler;
        prevButtons = getButtons();
        ySign = 1;

        TrcTaskMgr.registerTask(
                instanceName,
                this,
                TrcTaskMgr.TaskType.PREPERIODIC_TASK);
    }   //FtcGamepad

    public FtcGamepad(
            final String instanceName,
            Gamepad gamepad,
            ButtonHandler buttonHandler,
            final double deadbandThreshold)
    {
        this(instanceName, gamepad, buttonHandler);
        gamepad.setJoystickDeadzone((float)deadbandThreshold);
    }   //FtcGamepad

    public int getButtons()
    {
        int buttons = 0;
        buttons |= gamepad.a? GAMEPAD_A: 0;
        buttons |= gamepad.b? GAMEPAD_B: 0;
        buttons |= gamepad.x? GAMEPAD_X: 0;
        buttons |= gamepad.y? GAMEPAD_Y: 0;
        buttons |= gamepad.back? GAMEPAD_BACK: 0;
        buttons |= gamepad.start? GAMEPAD_START: 0;
        buttons |= gamepad.left_bumper? GAMEPAD_LBUMPER: 0;
        buttons |= gamepad.right_bumper? GAMEPAD_RBUMPER: 0;
        buttons |= gamepad.left_stick_button? GAMEPAD_LSTICK_BTN: 0;
        buttons |= gamepad.right_stick_button? GAMEPAD_RSTICK_BTN: 0;
        buttons |= gamepad.dpad_left? GAMEPAD_DPAD_LEFT: 0;
        buttons |= gamepad.dpad_right? GAMEPAD_DPAD_RIGHT: 0;
        buttons |= gamepad.dpad_up? GAMEPAD_DPAD_UP: 0;
        buttons |= gamepad.dpad_down? GAMEPAD_DPAD_DOWN: 0;
        return buttons;
    }   //getButtons

    public void setYInverted(boolean inverted)
    {
        final String funcName = "setYInverted";

        if (debugEnabled)
        {
            dbgTrace.traceEnter(
                    funcName, TrcDbgTrace.TraceLevel.API,
                    "inverted=%s",
                    Boolean.toString(inverted));
        }

        ySign = inverted? -1: 1;

        if (debugEnabled)
        {
            dbgTrace.traceExit(funcName, TrcDbgTrace.TraceLevel.API);
        }
    }   //set YInverted

    public double getLeftStickX(boolean squared)
    {
        return squareValue((double)gamepad.left_stick_x, squared);
    }   //getLeftStickX

    public double getLeftStickX()
    {
        return getLeftStickX(false);
    }   //getLeftStickX

    public double getLeftStickY(boolean squared)
    {
        return squareValue((double)gamepad.left_stick_y, squared);
    }   //getLeftStickY

    public double getLeftStickY()
    {
        return getLeftStickY(false);
    }   //getLeftStickY

    public double getRightStickX(boolean squared)
    {
        return squareValue((double)gamepad.right_stick_x, squared);
    }   //getRightStickX

    public double getRightStickX()
    {
        return getRightStickX(false);
    }   //getRightStickX

    public double getRightStickY(boolean squared)
    {
        return squareValue((double)gamepad.right_stick_y, squared);
    }   //getRightStickY

    public double getRightStickY()
    {
        return getRightStickY(false);
    }   //getRightStickY

    public double getLeftTrigger(boolean squared)
    {
        return squareValue((double)gamepad.left_trigger, squared);
    }   //getLeftTrigger

    public double getLeftTrigger()
    {
        return getLeftTrigger(false);
    }   //getLeftTrigger

    public double getRightTrigger(boolean squared)
    {
        return squareValue((double)gamepad.right_trigger, squared);
    }   //getRightTrigger

    public double getRightTrigger()
    {
        return getRightTrigger(false);
    }   //getRightTrigger

    public double getLeftStickMagnitude(boolean squared)
    {
        return getMagnitude(getLeftStickX(squared), getLeftStickY(squared));
    }   //getLeftStickMagnitude

    public double getLeftStickMagnitude()
    {
        return getLeftStickMagnitude(false);
    }   //getLeftStickMagnitude

    public double getRightStickMagnitude(boolean squared)
    {
        return getMagnitude(getRightStickX(squared), getRightStickY(squared));
    }   //getRightStickMagnitude

    public double getRightStickMagnitude()
    {
        return getRightStickMagnitude(false);
    }   //getRightStickMagnitude

    public double getLeftStickDirectionRadians(boolean squared)
    {
        return Math.atan2(getLeftStickY(squared), getLeftStickX(squared));
    }   //getLeftStickDirectionRadians

    public double getLeftStickDirectionRadians()
    {
        return getLeftStickDirectionRadians(false);
    }   //getLeftStickDirectionRadians

    public double getRightStickDirectionRadians(boolean squared)
    {
        return Math.atan2(getRightStickY(squared), getRightStickX(squared));
    }   //getRightStickDirectionRadian

    public double getRightStickDirectionRadians()
    {
        return getRightStickDirectionRadians(false);
    }   //getRightStickDirectionRadians

    public double getLeftStickDirectionDegrees(boolean squared)
    {
        return Math.toDegrees(getLeftStickDirectionRadians(squared));
    }   //getLeftStickDirectionDegrees

    public double getLeftStickDirectionDegrees()
    {
        return getLeftStickDirectionDegrees(false);
    }   //getLeftStickDirectionDegrees

    public double getRightStickDirectionDegrees(boolean squared)
    {
        return Math.toDegrees(getRightStickDirectionRadians(squared));
    }   //getRightStickDirectionDegrees

    public double getRightStickDirectionDegrees()
    {
        return getRightStickDirectionDegrees(false);
    }   //getRightStickDirectionDegrees

    //
    // Implements TrcTaskMgr.Task
    //
    public void startTask(TrcRobot.RunMode runMode)
    {
    }   //startTask

    public void stopTask(TrcRobot.RunMode runMode)
    {
    }   //stopTask

    public void prePeriodicTask(TrcRobot.RunMode runMode)
    {
        final String funcName = "prePeriodic";

        if (debugEnabled)
        {
            dbgTrace.traceEnter(
                    funcName, TrcDbgTrace.TraceLevel.TASK,
                    "mode=%s", runMode.toString());
        }

        int currButtons = getButtons();
        if (buttonHandler != null)
        {
            int changedButtons = prevButtons^currButtons;
            int buttonMask;
            while (changedButtons != 0)
            {
                //
                // buttonMask contains the least significant set bit.
                //
                buttonMask = changedButtons & ~(changedButtons^-changedButtons);
                if ((currButtons & buttonMask) != 0)
                {
                    //
                    // Button is pressed.
                    //
                    if (debugEnabled)
                    {
                        dbgTrace.traceInfo(
                                funcName,
                                "Button %x pressed",
                                buttonMask);
                    }
                    buttonHandler.gamepadButtonEvent(
                            this, buttonMask, true);
                }
                else
                {
                    //
                    // Button is released.
                    //
                    if (debugEnabled)
                    {
                        dbgTrace.traceInfo(
                                funcName,
                                "Button %x released",
                                buttonMask);
                    }
                    buttonHandler.gamepadButtonEvent(
                            this, buttonMask, false);
                }
                //
                // Clear the least significant set bit.
                //
                changedButtons &= ~buttonMask;
            }
        }
        prevButtons = currButtons;

        if (debugEnabled)
        {
            dbgTrace.traceExit(funcName, TrcDbgTrace.TraceLevel.TASK);
        }
    }   //prePeriodicTask

    public void postPeriodicTask(TrcRobot.RunMode runMode)
    {
    }   //postPeriodicTask

    public void preContinuousTask(TrcRobot.RunMode runMode)
    {
    }   //preContinuousTask

    public void postContinuousTask(TrcRobot.RunMode runMode)
    {
    }   //postContinuousTask

    private double squareValue(double value, boolean squared)
    {
        if (squared)
        {
            int dir = (value >= 0.0)? 1: -1;
            value = dir*value*value;
        }
        return value;
    }   //squareValue

    private double getMagnitude(double x, double y)
    {
        return Math.sqrt(x*x + y*y);
    }   //getMagnitude

}   //class FtcGamepad
