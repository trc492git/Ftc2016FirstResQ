package hallib;

public class HalRobotDrive
{
    public static class MotorType
    {
        public final int value;

        private static final int kFrontLeft_val = 0;
        private static final int kFrontRight_val = 1;
        private static final int kRearLeft_val = 2;
        private static final int kRearRight_val = 3;

        public static final MotorType kFrontLeft = new MotorType(kFrontLeft_val);
        public static final MotorType kFrontRight = new MotorType(kFrontRight_val);
        public static final MotorType kRearLeft = new MotorType(kRearLeft_val);
        public static final MotorType kRearRight = new MotorType(kRearRight_val);

        private MotorType(int value)
        {
            this.value = value;
        }   //MotorType
    }   //class MotorType

    public static double kDefaultSensitivity = 0.5;
    public static double kDefaultMaxOutput = 1.0;

    private static double MOTOR_MAX_VALUE = 1.0;
    private static double MOTOR_MIN_VALUE = -1.0;
    private static int MAX_NUM_MOTORS = 4;

    private double sensitivity;
    private double maxOutput;
    private int numMotors;
    private HalSpeedController frontLeftMotor;
    private HalSpeedController frontRightMotor;
    private HalSpeedController rearLeftMotor;
    private HalSpeedController rearRightMotor;

    private void robotDriveInit(
            HalSpeedController frontLeftMotor,
            HalSpeedController rearLeftMotor,
            HalSpeedController frontRightMotor,
            HalSpeedController rearRightMotor)
    {
        sensitivity = kDefaultSensitivity;
        maxOutput = kDefaultMaxOutput;
        numMotors = 0;

        this.frontLeftMotor = frontLeftMotor;
        if (frontLeftMotor != null) numMotors++;
        this.rearLeftMotor = rearLeftMotor;
        if (rearLeftMotor != null) numMotors++;
        this.frontRightMotor = frontRightMotor;
        if (frontRightMotor != null) numMotors++;
        this.rearRightMotor = rearRightMotor;
        if (rearRightMotor != null) numMotors++;

        stopMotor();
    }   //robotDriveInit

    public HalRobotDrive(
            HalSpeedController frontLeftMotor,
            HalSpeedController rearLeftMotor,
            HalSpeedController frontRightMotor,
            HalSpeedController rearRightMotor)
    {
        if (frontLeftMotor == null || rearLeftMotor == null ||
            frontRightMotor == null || rearRightMotor == null)
        {
            this.frontLeftMotor = this.rearLeftMotor
                                = this.frontRightMotor
                                = this.rearRightMotor = null;
            throw new NullPointerException("Null motor provided");
        }
        robotDriveInit(frontLeftMotor, rearLeftMotor, frontRightMotor, rearRightMotor);
    }   //HalRobotDrive

    public HalRobotDrive(HalSpeedController leftMotor, HalSpeedController rightMotor)
    {
        if (leftMotor == null || rightMotor == null)
        {
            this.rearLeftMotor = this.rearRightMotor = null;
            throw new NullPointerException("Null motor provided");
        }
        robotDriveInit(null, leftMotor, null, rightMotor);
    }   //HalRobotDrive

    public void drive(double magnitude, double curve)
    {
        double leftOutput;
        double rightOutput;

        if (curve < 0.0)
        {
            double value = Math.log(-curve);
            double ratio = (value - sensitivity)/(value + sensitivity);
            if (ratio == 0.0)
            {
                ratio = 0.0000000001;
            }
            leftOutput = magnitude/ratio;
            rightOutput = magnitude;
        }
        else if (curve > 0.0)
        {
            double value = Math.log(curve);
            double ratio = (value - sensitivity)/(value + sensitivity);
            if (ratio == 0.0)
            {
                ratio = 0.0000000001;
            }
            leftOutput = magnitude;
            rightOutput = magnitude/ratio;
        }
        else
        {
            leftOutput = magnitude;
            rightOutput = magnitude;
        }
        tankDrive(leftOutput, rightOutput);
    }   //drive

    public void stopMotor()
    {
        if (frontLeftMotor != null) frontLeftMotor.setPower(0.0);
        if (frontRightMotor != null) frontRightMotor.setPower(0.0);
        if (rearLeftMotor != null) rearLeftMotor.setPower(0.0);
        if (rearRightMotor != null) rearRightMotor.setPower(0.0);
    }   //stopMotor

    public void setSensitivity(double sensitivity)
    {
        this.sensitivity = sensitivity;
    }   //setSensitivity

    public void setMaxOutput(double maxOutput)
    {
        this.maxOutput = maxOutput;
    }   //setMaxOutput

    public int getNumMotors()
    {
        return numMotors;
    }   //getNumMotors

    public void setInvertedMotor(MotorType motorType, boolean isInverted)
    {
        switch (motorType.value)
        {
            case MotorType.kFrontLeft_val:
                if (frontLeftMotor != null)
                {
                    frontLeftMotor.setInverted(isInverted);
                }
                break;

            case MotorType.kFrontRight_val:
                if (frontRightMotor != null)
                {
                    frontRightMotor.setInverted(isInverted);
                }
                break;

            case MotorType.kRearLeft_val:
                if (rearLeftMotor != null)
                {
                    rearLeftMotor.setInverted(isInverted);
                }
                break;

            case MotorType.kRearRight_val:
                if (rearRightMotor != null)
                {
                    rearRightMotor.setInverted(isInverted);
                }
                break;
        }
    }   //setInvertedMotor

    public void tankDrive(double leftPower, double rightPower)
    {
        leftPower = limit(leftPower);
        rightPower = limit(rightPower);

        if (frontLeftMotor != null)
        {
            frontLeftMotor.setPower(leftPower);
        }

        if (frontRightMotor != null)
        {
            frontRightMotor.setPower(rightPower);
        }

        if (rearLeftMotor != null)
        {
            rearLeftMotor.setPower(leftPower);
        }

        if (rearRightMotor != null)
        {
            rearRightMotor.setPower(rightPower);
        }
    }   //tankDrive

    public void arcadeDrive(double drivePower, double turnPower)
    {
        double leftPower;
        double rightPower;

        drivePower = limit(drivePower);
        turnPower = limit(turnPower);

        if (drivePower + turnPower > MOTOR_MAX_VALUE)
        {
            //
            // Forward right:
            //  left = drive + turn - (drive + turn - MOTOR_MAX_VALUE)
            //  right = drive - turn - (drive + turn - MOTOR_MAX_VALUE)
            //
            leftPower = MOTOR_MAX_VALUE;
            rightPower = -2*turnPower + MOTOR_MAX_VALUE;
        }
        else if (drivePower - turnPower > MOTOR_MAX_VALUE)
        {
            //
            // Forward left:
            //  left = drive + turn - (drive - turn - MOTOR_MAX_VALUE)
            //  right = drive - turn - (drive - turn - MOTOR_MAX_VALUE)
            //
            leftPower = 2*turnPower + MOTOR_MAX_VALUE;
            rightPower = MOTOR_MAX_VALUE;
        }
        else if (drivePower + turnPower < MOTOR_MIN_VALUE)
        {
            //
            // Backward left:
            //  left = drive + turn - (drive + turn - MOTOR_MIN_VALUE)
            //  right = drive - turn - (drive + turn - MOTOR_MIN_VALUE)
            //
            leftPower = MOTOR_MIN_VALUE;
            rightPower = -2*turnPower + MOTOR_MIN_VALUE;
        }
        else if (drivePower - turnPower < MOTOR_MIN_VALUE)
        {
            //
            // Backward right:
            //  left = drive + turn - (drive - turn - MOTOR_MIN_VALUE)
            //  right = drive - turn - (drive - turn - MOTOR_MIN_VALUE)
            //
            leftPower = 2*turnPower + MOTOR_MIN_VALUE;
            rightPower = MOTOR_MIN_VALUE;
        }
        else
        {
            leftPower = drivePower + turnPower;
            rightPower = drivePower - turnPower;
        }
        tankDrive(leftPower, rightPower);
    }   //arcadeDrive

    public void mecanumDrive_Cartesian(double x, double y, double rotation, double gyroAngle)
    {
        if (numMotors != MAX_NUM_MOTORS)
        {
            throw new IllegalArgumentException("Mecanum drive requires 4 motors");
        }

        x = limit(x);
        y = limit(y);
        rotation = limit(rotation);

        double cosA = Math.cos(Math.toRadians(gyroAngle));
        double sinA = Math.sin(Math.toRadians(gyroAngle));
        x = x*cosA - y*sinA;
        y = x*sinA + y*cosA;

        double wheelSpeeds[] = new double[MAX_NUM_MOTORS];
        wheelSpeeds[MotorType.kFrontLeft_val] = x + y + rotation;
        wheelSpeeds[MotorType.kFrontRight_val] = -x + y - rotation;
        wheelSpeeds[MotorType.kRearLeft_val] = -x + y + rotation;
        wheelSpeeds[MotorType.kRearRight_val] = x + y - rotation;
        normalize(wheelSpeeds);

        if (frontLeftMotor != null)
        {
            frontLeftMotor.setPower(wheelSpeeds[MotorType.kFrontLeft_val]);
        }

        if (frontRightMotor != null)
        {
            frontRightMotor.setPower(wheelSpeeds[MotorType.kFrontRight_val]);
        }

        if (rearLeftMotor != null)
        {
            rearLeftMotor.setPower(wheelSpeeds[MotorType.kRearLeft_val]);
        }

        if (rearRightMotor != null)
        {
            rearRightMotor.setPower(wheelSpeeds[MotorType.kRearRight_val]);
        }
    }   //mecanumDrive_Cartesian

    public void mecanumDrive_Polar(double magnitude, double direction, double rotation)
    {
        if (numMotors != MAX_NUM_MOTORS)
        {
            throw new IllegalArgumentException("Mecanum drive requires 4 motors");
        }

        magnitude = limit(magnitude)*Math.sqrt(2.0);
        double dirInRad = Math.toRadians(direction + 45.0);
        double cosD = Math.cos(dirInRad);
        double sinD = Math.sin(dirInRad);

        double wheelSpeeds[] = new double[MAX_NUM_MOTORS];
        wheelSpeeds[MotorType.kFrontLeft_val] = (sinD*magnitude + rotation);
        wheelSpeeds[MotorType.kFrontRight_val] = (cosD*magnitude - rotation);
        wheelSpeeds[MotorType.kRearLeft_val] = (cosD*magnitude + rotation);
        wheelSpeeds[MotorType.kRearRight_val] = (sinD*magnitude - rotation);
        normalize(wheelSpeeds);

        if (frontLeftMotor != null)
        {
            frontLeftMotor.setPower(wheelSpeeds[MotorType.kFrontLeft_val]);
        }

        if (frontRightMotor != null)
        {
            frontRightMotor.setPower(wheelSpeeds[MotorType.kFrontRight_val]);
        }

        if (rearLeftMotor != null)
        {
            rearLeftMotor.setPower(wheelSpeeds[MotorType.kRearLeft_val]);
        }

        if (rearRightMotor != null)
        {
            rearRightMotor.setPower(wheelSpeeds[MotorType.kRearRight_val]);
        }
    }   //mecanumDrive_Polar

    private double limit(double value)
    {
        return value < -1.0? -1.0: value > 1.0? 1.0: value;
    }   //limit

    private void normalize(double[] wheelSpeeds)
    {
        double maxMagnitude = Math.abs(wheelSpeeds[0]);
        for (int i = 1; i < wheelSpeeds.length; i++)
        {
            double magnitude = Math.abs(wheelSpeeds[i]);
            if (magnitude > maxMagnitude)
            {
                maxMagnitude = magnitude;
            }
        }

        if (maxMagnitude > 1.0)
        {
            for (int i = 0; i < wheelSpeeds.length; i++)
            {
                wheelSpeeds[i] /= maxMagnitude;
            }
        }
    }   //normalize

}   //HalRobotDrive
