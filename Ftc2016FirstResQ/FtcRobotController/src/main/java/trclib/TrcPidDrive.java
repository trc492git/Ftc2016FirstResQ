package trclib;

import hallib.HalTimer;

public class TrcPidDrive implements TrcTaskMgr.Task
{
    private static final String moduleName = "TrcPidDrive";
    private static final boolean debugEnabled = false;
    private TrcDbgTrace dbgTrace = null;

    private static final int PIDDRIVEF_ENABLED          = (1 << 0);
    private static final int PIDDRIVEF_HOLD_TARGET      = (1 << 1);
    private static final int PIDDRIVEF_TURN_ONLY        = (1 << 2);
    private static final int PIDDRIVEF_SET_HEADING      = (1 << 3);
    private static final int PIDDRIVEF_CANCELED         = (1 << 4);

    private String instanceName;
    private TrcDriveBase driveBase;
    private TrcPidController xPidCtrl;
    private TrcPidController yPidCtrl;
    private TrcPidController turnPidCtrl;
    private TrcEvent notifyEvent;
    private double expiredTime;
    private int flags;
    private double manualX;
    private double manualY;

    public TrcPidDrive(
        final String instanceName,
        TrcDriveBase driveBase,
        TrcPidController xPidCtrl,
        TrcPidController yPidCtrl,
        TrcPidController turnPidCtrl)
    {
        if (debugEnabled)
        {
            dbgTrace = new TrcDbgTrace(
                    moduleName + "." + instanceName,
                    false,
                    TrcDbgTrace.TraceLevel.API,
                    TrcDbgTrace.MsgLevel.INFO);
        }

        this.instanceName = instanceName;
        this.driveBase = driveBase;
        this.xPidCtrl = xPidCtrl;
        this.yPidCtrl = yPidCtrl;
        this.turnPidCtrl = turnPidCtrl;
        this.notifyEvent = null;
        this.expiredTime = 0.0;
        this.flags = 0;
        this.manualX = 0.0;
        this.manualY = 0.0;
    }   //TrcPidDrive

    public void setTarget(
            double xTarget,
            double yTarget,
            double turnTarget,
            boolean holdTarget,
            TrcEvent event,
            double timeout)
    {
        final String funcName = "setTarget";

        if (debugEnabled)
        {
            dbgTrace.traceEnter(
                    funcName, TrcDbgTrace.TraceLevel.API,
                    "x=%f,y=%f,turn=%f,hold=%s,event=%s,timeout=%f",
                    xTarget, yTarget, turnTarget, Boolean.toString(holdTarget),
                    event.getName(), timeout);
        }

        if (xPidCtrl != null)
        {
            xPidCtrl.setTarget(xTarget);
        }

        if (yPidCtrl != null)
        {
            yPidCtrl.setTarget(yTarget);
        }

        if (turnPidCtrl != null)
        {
            turnPidCtrl.setTarget(turnTarget);
        }

        if (event != null)
        {
            event.clear();
        }
        this.notifyEvent = event;
        this.expiredTime = timeout;
        if (timeout != 0)
        {
            this.expiredTime += HalTimer.getCurrentTime();
        }

        flags = 0;
        if (holdTarget)
        {
            flags |= PIDDRIVEF_HOLD_TARGET;
        }

        if (xTarget == 0.0 && yTarget == 0.0 && turnTarget != 0.0)
        {
            flags |= PIDDRIVEF_TURN_ONLY;
        }

        setEnabled(true);

        if (debugEnabled)
        {
            dbgTrace.traceExit(funcName, TrcDbgTrace.TraceLevel.API);
        }
    }   //setTarget

    public void setTarget(
            double yTarget,
            double turnTarget,
            boolean holdTarget,
            TrcEvent event,
            double timeout)
    {
        setTarget(
                0.0,
                yTarget,
                turnTarget,
                holdTarget,
                event,
                timeout);
    }   //setTarget

    public void setHeadingTarget(
            double xPower,
            double yPower,
            double headingTarget)
    {
        final String funcName = "setHeadingTarget";

        if (debugEnabled)
        {
            dbgTrace.traceEnter(
                    funcName, TrcDbgTrace.TraceLevel.API,
                    "xPower=%f,yPower=%f,heading=%f",
                    xPower, yPower, headingTarget);
        }

        if (xPidCtrl != null)
        {
            manualX = xPower;
            manualY = yPower;
            if (turnPidCtrl != null)
            {
                turnPidCtrl.setTarget(headingTarget);
            }
            flags = PIDDRIVEF_SET_HEADING;
            setEnabled(true);
        }

        if (debugEnabled)
        {
            dbgTrace.traceExit(funcName, TrcDbgTrace.TraceLevel.API);
        }
    }   //setHeadingTarget

    public boolean isEnabled()
    {
        final String funcName = "isEnabled";

        if (debugEnabled)
        {
            dbgTrace.traceEnter(funcName, TrcDbgTrace.TraceLevel.API);
            dbgTrace.traceExit(
                    funcName, TrcDbgTrace.TraceLevel.API,
                    "=%s", Boolean.toString((flags & PIDDRIVEF_ENABLED) != 0));
        }

        return (flags & PIDDRIVEF_ENABLED) != 0;
    }   //isEnabled

    public void cancel()
    {
        final String funcName = "cancel";

        if (debugEnabled)
        {
            dbgTrace.traceEnter(funcName, TrcDbgTrace.TraceLevel.API);
        }

        if ((flags & PIDDRIVEF_ENABLED) != 0)
        {
            stop();
            flags |= PIDDRIVEF_CANCELED;
            if (notifyEvent != null)
            {
                notifyEvent.cancel();
                notifyEvent = null;
            }
        }

        if (debugEnabled)
        {
            dbgTrace.traceExit(funcName, TrcDbgTrace.TraceLevel.API);
        }
    }   //cancel

    public boolean isCanceled()
    {
        final String funcName = "isCanceled";

        if (debugEnabled)
        {
            dbgTrace.traceEnter(funcName, TrcDbgTrace.TraceLevel.API);
            dbgTrace.traceExit(
                    funcName, TrcDbgTrace.TraceLevel.API,
                    "=%s", Boolean.toString((flags & PIDDRIVEF_CANCELED) != 0));
        }

        return (flags & PIDDRIVEF_CANCELED) != 0;
    }   //isCanceled

    private void stop()
    {
        final String funcName = "stop";

        if (debugEnabled)
        {
            dbgTrace.traceEnter(funcName, TrcDbgTrace.TraceLevel.FUNC);
        }

        setEnabled(false);
        driveBase.stop();

        if (xPidCtrl != null)
        {
            xPidCtrl.reset();
        }

        if (yPidCtrl != null)
        {
            yPidCtrl.reset();
        }

        if (turnPidCtrl != null)
        {
            turnPidCtrl.reset();
        }

        flags = 0;

        if (debugEnabled)
        {
            dbgTrace.traceExit(funcName, TrcDbgTrace.TraceLevel.FUNC);
        }
    }   //stop

    private void setEnabled(boolean enabled)
    {
        final String funcName = "setEnabled";

        if (debugEnabled)
        {
            dbgTrace.traceEnter(
                    funcName, TrcDbgTrace.TraceLevel.FUNC,
                    "enabled=%s", Boolean.toString(enabled));
        }

        if (enabled)
        {
            TrcTaskMgr.registerTask(
                    instanceName,
                    this,
                    TrcTaskMgr.TaskType.STOP_TASK);
            TrcTaskMgr.registerTask(
                    instanceName,
                    this,
                    TrcTaskMgr.TaskType.POSTPERIODIC_TASK);
            flags |= PIDDRIVEF_ENABLED;
        }
        else
        {
            TrcTaskMgr.unregisterTask(
                    this,
                    TrcTaskMgr.TaskType.STOP_TASK);
            TrcTaskMgr.unregisterTask(
                    this,
                    TrcTaskMgr.TaskType.POSTPERIODIC_TASK);
            flags &= ~PIDDRIVEF_ENABLED;
        }

        if (debugEnabled)
        {
            dbgTrace.traceExit(funcName, TrcDbgTrace.TraceLevel.FUNC);
        }
    }   //setEnabled

    //
    // Implements TrcTaskMgr.Task
    //
    public void startTask(TrcRobot.RunMode runMode)
    {
    }   //startTask

    public void stopTask(TrcRobot.RunMode runMode)
    {
        final String funcName = "stopTask";

        if (debugEnabled)
        {
            dbgTrace.traceEnter(
                    funcName, TrcDbgTrace.TraceLevel.TASK,
                    "mode=%s", runMode.toString());
        }

        stop();

        if (debugEnabled)
        {
            dbgTrace.traceExit(funcName, TrcDbgTrace.TraceLevel.TASK);
        }
    }   //stopTask

    public void prePeriodicTask(TrcRobot.RunMode runMode)
    {
    }   //prePeriodicTask

    public void postPeriodicTask(TrcRobot.RunMode runMode)
    {
        final String funcName = "postPeriodic";

        if (debugEnabled)
        {
            dbgTrace.traceEnter(
                    funcName, TrcDbgTrace.TraceLevel.TASK,
                    "mode=%s", runMode.toString());
        }

        double xPower =
                (((flags & PIDDRIVEF_TURN_ONLY) != 0) || (xPidCtrl == null))?
                        0.0: xPidCtrl.getOutput();
        double yPower =
                (((flags & PIDDRIVEF_TURN_ONLY) != 0) || (yPidCtrl == null))?
                        0.0: yPidCtrl.getOutput();
        double turnPower = (turnPidCtrl == null)? 0.0: turnPidCtrl.getOutput();

        boolean expired =
                expiredTime != 0.0 && HalTimer.getCurrentTime() >= expiredTime;
        boolean xOnTarget = xPidCtrl == null || xPidCtrl.isOnTarget();
        boolean yOnTarget = yPidCtrl == null || yPidCtrl.isOnTarget();
        boolean turnOnTarget = turnPidCtrl == null || turnPidCtrl.isOnTarget();

        if ((flags & PIDDRIVEF_SET_HEADING) != 0)
        {
            driveBase.mecanumDrive_Cartesian(manualX, manualY, turnPower, 0.0);
        }
        else if (expired ||
                 turnOnTarget &&
                 ((flags & PIDDRIVEF_TURN_ONLY) != 0 ||
                  xOnTarget && yOnTarget))
        {
            if ((flags & PIDDRIVEF_HOLD_TARGET) == 0)
            {
                stop();
                if (notifyEvent != null)
                {
                    notifyEvent.set(true);
                    notifyEvent = null;
                }
            }
            else if (xPidCtrl != null)
            {
                driveBase.mecanumDrive_Cartesian(0.0, 0.0, 0.0, 0.0);
            }
            else
            {
                driveBase.drive(0.0, 0.0);
            }
        }
        else if (xPidCtrl != null)
        {
            driveBase.mecanumDrive_Cartesian(xPower, yPower, turnPower, 0.0);
        }
        else
        {
            driveBase.arcadeDrive(yPower, turnPower);
        }

        if (debugEnabled)
        {
            dbgTrace.traceExit(funcName, TrcDbgTrace.TraceLevel.TASK);
        }
    }   //postPeriodicTask

    public void preContinuousTask(TrcRobot.RunMode runMode)
    {
    }   //preContinuousTask

    public void postContinuousTask(TrcRobot.RunMode runMode)
    {
    }   //postContinuousTask

}   //class TrcPidDrive
