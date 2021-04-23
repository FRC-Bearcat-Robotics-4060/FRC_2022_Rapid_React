/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.kauailabs.navx.frc.AHRS;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ColorMatch;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorSensorV3;
import com.revrobotics.EncoderType;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;

//import frc.robot.imports.*;
public class Robot extends TimedRobot {

  AHRS navx;

  Joystick _joystick1 = new Joystick(0);
  //Change to DPad like control schemea

  Timer shooterTimer = new Timer();
  Boolean wasShooting = false;

  // Joystick Buttons -

  CANSparkMax _leftBackCanSparkMax = new CANSparkMax((1), MotorType.kBrushless);
  CANSparkMax _leftFrontCanSparkMax = new CANSparkMax((4), MotorType.kBrushless);
  CANSparkMax _rightBackCanSparkMax = new CANSparkMax((2), MotorType.kBrushless);
  CANSparkMax _rightFrontCanSparkMax = new CANSparkMax((3), MotorType.kBrushless);
  private SpeedControllerGroup m_LeftMotors = new SpeedControllerGroup(_leftBackCanSparkMax, _leftFrontCanSparkMax);
  private SpeedControllerGroup m_RightMotors = new SpeedControllerGroup(_rightBackCanSparkMax, _rightFrontCanSparkMax);
  private DifferentialDrive m_Drive = new DifferentialDrive(m_LeftMotors, m_RightMotors);

//Screwed Up Git Again, need to redo the variable speed for the shooter.

  CANSparkMax _collectVert = new CANSparkMax((13), MotorType.kBrushless);

  CANSparkMax _shooterMotorLeft = new CANSparkMax((14), MotorType.kBrushless);
  CANSparkMax _shooterMotorRight = new CANSparkMax((15), MotorType.kBrushless);

  // CANSparkMax _miscSpark = new CANSparkMax((null), MotorType.kBrushless);

  TalonSRX _colorWheelTalon = new TalonSRX(10);

  TalonSRX _ColectorMotor = new TalonSRX(11);

  TalonSRX _liftmotor = new TalonSRX(12);

  DigitalInput bottomSensor = new DigitalInput(0);

  DigitalInput topSensor = new DigitalInput(1);

  Spark _magMotor1 = new Spark(0);
  Spark _magMotor2 = new Spark(1);

  Boolean button12Toggle = false;
  Boolean button11Toggle = false;
  Boolean speedToggle = false;
  Boolean lihtAct = false;
  double _tavar = 0.0;
  Boolean lightspeed = false;
  double shooterSpeed = 0.1;
  int isChangingSpeed = 0;
  int reachedPoint = 0;
  int reachedPoint1 = 1;

  private final I2C.Port i2cPort = I2C.Port.kOnboard;
  private final ColorSensorV3 m_colorSensor = new ColorSensorV3(i2cPort);
  private final ColorMatch m_colorMatcher = new ColorMatch();

  private final Color kBlueTarget = ColorMatch.makeColor(0.143, 0.427, 0.429);
  private final Color kGreenTarget = ColorMatch.makeColor(0.197, 0.561, 0.240);
  private final Color kRedTarget = ColorMatch.makeColor(0.561, 0.232, 0.114);
  private final Color kYellowTarget = ColorMatch.makeColor(0.361, 0.524, 0.113);

  double encoderInches = 0;

  NetworkTableEntry ledEntry;
  NetworkTableEntry camMode;

  private boolean m_LimelightHasValidTarget = false;
  private double m_LimelightDriveCommand = 0.0;
  private double m_LimelightSteerCommand = 0.0;
  private boolean isGettingBall = false;

   CANEncoder leftBack_encoder = _leftBackCanSparkMax.getEncoder();
    CANEncoder rightBack_encoder = _rightBackCanSparkMax.getEncoder();

  int numbOfBalls;

  int bottomSensorLock;
  int topSensorLock;

  @Override
  public void robotInit() {


//Init USB Camera Server For Streaming
    CameraServer.getInstance().startAutomaticCapture();



//Set amount of time allowed for RAMP
    final double rampSeconds = 0.15;

    //Set The Max Time allowed for mode OpenLoopRampRate
    _leftBackCanSparkMax.setOpenLoopRampRate(rampSeconds);
    _leftFrontCanSparkMax.setOpenLoopRampRate(rampSeconds);
    _rightBackCanSparkMax.setOpenLoopRampRate(rampSeconds);
    _rightFrontCanSparkMax.setOpenLoopRampRate(rampSeconds);
    _shooterMotorLeft.setOpenLoopRampRate(rampSeconds);
    _shooterMotorRight.setOpenLoopRampRate(rampSeconds);
    _collectVert.setOpenLoopRampRate(rampSeconds);


//Set IDLE Modes for SparkMax's
    _leftBackCanSparkMax.setIdleMode(IdleMode.kBrake);
    _leftFrontCanSparkMax.setIdleMode(IdleMode.kBrake);
    _rightBackCanSparkMax.setIdleMode(IdleMode.kBrake);
    _rightFrontCanSparkMax.setIdleMode(IdleMode.kBrake);


//Add Colors For Color Matcher to Find
    m_colorMatcher.addColorMatch(kBlueTarget);
    m_colorMatcher.addColorMatch(kGreenTarget);
    m_colorMatcher.addColorMatch(kRedTarget);
    m_colorMatcher.addColorMatch(kYellowTarget);

    try {
      /* Communicate w/navX-MXP via the MXP SPI Bus. */
      /* Alternatively: I2C.Port.kMXP, SerialPort.Port.kMXP or SerialPort.Port.kUSB */
      /*
       * See http://navx-mxp.kauailabs.com/guidance/selecting-an-interface/ for
       * details.
       */
      navx = new AHRS(SPI.Port.kMXP);
      navx.enableLogging(true);
    } catch (RuntimeException ex) {
      DriverStation.reportError("Error instantiating navX-MXP:  " + ex.getMessage(), true);

    }
  }

  @Override
  public void robotPeriodic() {

    // navxReadout();
    encoderProcessing();

   

    SmartDashboard.putBoolean("SpeedToggle", speedToggle);
    SmartDashboard.putBoolean("LightSpeed", lightspeed);
    

    SmartDashboard.putBoolean("BottomSensor", bottomSensor.get());

    SmartDashboard.putBoolean("TopSensor", topSensor.get());


    Color detectedColor = m_colorSensor.getColor();
    String colorString;
    String fieldColor;
    ColorMatchResult match = m_colorMatcher.matchClosestColor(detectedColor);

    if (match.color == kBlueTarget) {
      colorString = "Blue";
      fieldColor = "Red";
    } else if (match.color == kRedTarget) {
      colorString = "Red";
      fieldColor = "Blue";
    } else if (match.color == kGreenTarget) {
      colorString = "Green";
      fieldColor = "Yellow";
    } else if (match.color == kYellowTarget) {
      colorString = "Yellow";
      fieldColor = "Green";
    } else {
      colorString = "Unknown";
      fieldColor = "IDunno";
    }

    /**
     * Open Smart Dashboard or Shuffleboard to see the color detected by the sensor.
     */
    SmartDashboard.putNumber("Red", detectedColor.red);
    SmartDashboard.putNumber("Green", detectedColor.green);
    SmartDashboard.putNumber("Blue", detectedColor.blue);
    SmartDashboard.putNumber("Confidence", match.confidence);
    SmartDashboard.putString("Detected Color", colorString);
    SmartDashboard.putString("Field Color", fieldColor);

    SmartDashboard.putNumber("Joystick Forward", -_joystick1.getY());
    SmartDashboard.putNumber("Robot Forward", _leftFrontCanSparkMax.getAppliedOutput());
  }

  Timer myTimer = new Timer();

  @Override
  public void autonomousInit() {
    teleopInit();

    // Reset timer to 0sec
    // myTimer.reset();

    // Start timer
    // myTimer.start();
  }

  @Override
  public void autonomousPeriodic() {

// if (encoderInches <= -17) {
//   reachedPoint = 1;
// }
// if (encoderInches >= 0) {
//   reachedPoint1 = 1;
// }

// if (reachedPoint == 1 && encoderInches <= -17) {
//             reachedPoint1 = 0;
// }
// if (reachedPoint1 == 1 && encoderInches >= 0) {
//   reachedPoint = 0;
// }
   

// if (reachedPoint == 0) {

//             m_Drive.arcadeDrive(0.1, 0, true);
//             //println('Less Then 17');
//       }

//       else if (reachedPoint1 == 0) {
//           m_Drive.arcadeDrive(-0.1, 0, true);
//           // println('At Then 17');
//           // m_Drive.arcadeDrive(0, 0, true);
        

//       }
//       else {
//             m_Drive.arcadeDrive(0.0, 0.0, true);
//       }

    
//                   // m_Drive.arcadeDrive(0.1, 0, false);
//                   //Forward value, rotate value, square inputs

//       SmartDashboard.putNumber("reachedPoint", reachedPoint);    
//       SmartDashboard.putNumber("reachedPoint1", reachedPoint1);    

//Do PID Loop to get smooth motion so tracking is more accurate
//'Error' is the distance to the goal
//Proportional (x error) is the max speed
//Integral (x ) area under a curve or line, is sum of all past error
//Derivative calculates the change in error, can predict future system behavior.

int targetInches = 17;

double P = 1.0; //Oos time 1.75 s
double I = 0;
double D = 0; //Causes bad ossilations if not 0

double speedLimit = 0.3;

double error = targetInches - encoderInches; //Error = Target - Actual
double gral =+ (error * 0.02); // Integral is increased by the error*time (which is .02 seconds using normal IterativeRobot)
double derivative = (error - 0) / 0.02;
double output = P*error + I*gral + D*derivative;

double outputParsed;
 
if (output > 1) {
  outputParsed = speedLimit;
}
else if (output < -1) {
  outputParsed = -speedLimit;
}
else {
  outputParsed = output;
}

// outputParsed = output;

// if (encoderInches >= targetInches - 1.1) {
//   outputParsed = 0;
// }

m_Drive.arcadeDrive(outputParsed, 0, true);

SmartDashboard.putNumber("PID Out", outputParsed);    

  }

  @Override
  public void teleopInit() {
    speedToggle = false;
    lightspeed = false;
    Update_Limelight_Tracking();
    limelightTracking(false);
    numbOfBalls = 0;
    bottomSensorLock = 1;
    topSensorLock = 1;
    rightBack_encoder.setPosition(0);
    leftBack_encoder.setPosition(0);
  }

  @Override
  public void teleopPeriodic() {
    buttonToggles();

    
    double deadzone = 0.3;
    double tx = NetworkTableInstance.getDefault().getTable("limelight").getEntry("tx").getDouble(0);
  
//Get left and right Joysticks and invert it 

  double _leftjoyforwardRaw = -_joystick1.getRawAxis(1);
  double _rightsidejoysideRaw = _joystick1.getRawAxis(4);
      SmartDashboard.putNumber("Raw1", _leftjoyforwardRaw);    
   SmartDashboard.putNumber("Raw4", _rightsidejoysideRaw);    

    

    double forward = (_leftjoyforwardRaw * 0.5);
    double rotate = (_rightsidejoysideRaw * 0.5);
    

    if (_joystick1.getRawButton(3)) {
      limelightAutonomous();
    }
    else {
      ledEntry.setDouble(1);
      camMode.setDouble(1);
    }


  if (!_joystick1.getRawButton(7)) {
    m_Drive.arcadeDrive(forward, ((forward < -0.1 || forward > 0.1) ? rotate * 1.2: rotate), true);
  }
  else if (_joystick1.getRawButton(7)) {
    m_Drive.arcadeDrive(forward * 1.5, ((forward < -0.1 || forward > 0.1) ? rotate*1.5 : rotate), true);

  }
    // Lifting and collecting
    //Set Buttons

    boolean collectorButton = _joystick1.getRawButton(5); //Left Shoulder
      boolean shooterButton = _joystick1.getRawButton(6); //Right Trigger (Is float, not bool)

  int dpadDir = _joystick1.getPOV(0);

    Boolean liftUp = (dpadDir == 0) ? true : false; //ADD DPAD UP
    Boolean liftDown = (dpadDir == 180) ? true : false; //ADD DPAD DOWN
    SmartDashboard.putNumber("DPadDir", dpadDir);    
  //Tell motor what to do

    // _liftmotor.set(ControlMode.PercentOutput, (liftDown ^ liftUp) ? (0.4) : 0.0);
  if (liftUp) {
    _liftmotor.set(ControlMode.PercentOutput, 0.4);
  }
  else if (liftDown) {
   _liftmotor.set(ControlMode.PercentOutput, -0.4);
   } 
   else {
     _liftmotor.set(ControlMode.PercentOutput, 0.0);
  }
  


      // For gathering
      // Read current from powerboard for shooter
      // Right Trigger Shooting
      // Collector Left Shoulder

        //Top and bottom parts of collector, plus if balls should be collected 
        //WILL be handled by retroreflective sensors! 
      
        //Add collector Logic

        if (collectorButton) {

      if (bottomSensorLock == 0 && !bottomSensor.get()) {
        numbOfBalls ++;
        bottomSensorLock = 1;
        isGettingBall = true;
      }
      else if (bottomSensorLock == 1 && bottomSensor.get()) {
        bottomSensorLock = 0;
        isGettingBall = false;
      }
    

      if (topSensorLock == 0 && topSensor.get()) {
        numbOfBalls --;
        topSensorLock = 1;
      }
      else if (topSensorLock == 1 && !topSensor.get()) {
        topSensorLock = 0;
      }

  //Now do the ball collecting logic for running through the carage feed

        if (numbOfBalls < 0) {
                  System.out.println("Collector Has Negative Balls");
        }
        else if (numbOfBalls > 5) {
                  System.out.println("More Then five Balls");
        }

        //When collecting and balls are zero, run bottom collector only, if one then run both.

      if (numbOfBalls == 0) {
        _magMotor1.set(-0.4);
      }
      else if (numbOfBalls >= 1 && topSensor.get()) {
        _magMotor1.set(-0.4);
        _magMotor2.set(0.4);
      }
      else if (numbOfBalls >= 1 && !topSensor.get()){
                _magMotor1.set(-0.4);
                _magMotor2.set(0);
      }
        else {
        _magMotor1.set(0);
        _magMotor2.set(0);
        }
  }

else {
        _magMotor1.set(0);
        _magMotor2.set(0);
        }

        //Add Shooter speed changer
        if (dpadDir == 90 && isChangingSpeed == 0) {
          shooterSpeed = shooterSpeed + 0.1;
          isChangingSpeed = 1;
        }
        else if (dpadDir == 270 && isChangingSpeed == 0) {
          shooterSpeed = shooterSpeed - 0.1;
          isChangingSpeed = 1;
        }
        else if (isChangingSpeed == 1) {
          isChangingSpeed = 0;
        }

        if (shooterButton && !collectorButton) {
        _shooterMotorLeft.set(-shooterSpeed);
        _shooterMotorRight.set(shooterSpeed);
                _magMotor2.set(0.4);
        }
      else if (!shooterButton && !collectorButton) {
        _shooterMotorLeft.set(0);
        _shooterMotorRight.set(0);
        _magMotor2.set(0);
      }


        

        SmartDashboard.putNumber("Amount Of Balls In", numbOfBalls);
        SmartDashboard.putNumber("bottomSensorLock", bottomSensorLock);        
        SmartDashboard.putBoolean("isGettingBall", isGettingBall);  
        SmartDashboard.putNumber("Shooter Speed", shooterSpeed * 100);        

    
        
      if (collectorButton) {
        _ColectorMotor.set(ControlMode.PercentOutput, -0.5);
        _collectVert.set(-0.5);

      

      }
      else {
        _ColectorMotor.set(ControlMode.PercentOutput, 0.0);
       _collectVert.set(0.0);
      }

    if (shooterButton) {
      startShooterTimer(); //<---- Investigate WHAT IS ShooterTimer?
    }
    
    
    
    

  } 

  //   if (_joystick1.getRawButton(5)) {
  //     _colorWheelTalon.set(ControlMode.PercentOutput, 1);
  //   }

  //   else if (_joystick1.getRawButton(3)) {
  //     _colorWheelTalon.set(ControlMode.PercentOutput, -1);     <--- No Clue What ANY of this did.
  //   }

  //   else {
  //     _colorWheelTalon.set(ControlMode.PercentOutput, 0);
  //   }
  // }

  public void Update_Limelight_Tracking() {
    // These numbers must be tuned for your Robot! Be careful!
    final double STEER_K = 0.02; // how hard to turn toward the target
    final double DRIVE_K = 0.16; // how hard to drive fwd toward the target
    final double DESIRED_TARGET_AREA = 13.0; // Area of the target when the robot reaches the wall
    final double MAX_DRIVE = 0.7; // Simple speed limit so we don't drive too fast

    double tv = NetworkTableInstance.getDefault().getTable("limelight").getEntry("tv").getDouble(0);
    double tx = NetworkTableInstance.getDefault().getTable("limelight").getEntry("tx").getDouble(0);
    double ty = NetworkTableInstance.getDefault().getTable("limelight").getEntry("ty").getDouble(0);
    double ta = NetworkTableInstance.getDefault().getTable("limelight").getEntry("ta").getDouble(0);
    camMode = NetworkTableInstance.getDefault().getTable("limelight").getEntry("camMode");
    ledEntry = NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode");
    // ty = ty - 8.00;
    ta = ta - 13.00;
    tx = tx + 4.1;
    // System.out.println(tv);
    if (tv != 1.0) {
      m_LimelightHasValidTarget = false;
      m_LimelightDriveCommand = 0.0;
      m_LimelightSteerCommand = 0.0;
      return;
    } else if (tv == 1.0) {
      m_LimelightHasValidTarget = true;
    }

    // Start with proportional steering
    double steer_cmd = tx * STEER_K;
    m_LimelightSteerCommand = steer_cmd;

    // try to drive forward until the target area reaches our desired area
    // double drive_cmd = (DESIRED_TARGET_AREA - ta) * DRIVE_K;
    double drive_cmd = ta * DRIVE_K;

    // don't let the robot drive too fast into the goal
    // if (drive_cmd > MAX_DRIVE)
    // {
    // drive_cmd = MAX_DRIVE;
    // }
    m_LimelightDriveCommand = drive_cmd / 5;

    _tavar = ta;
  }

  public void limelightAutonomous() {
    Update_Limelight_Tracking();
    if (_joystick1.getRawButton(3)) {
      ledEntry.setDouble(3);
      camMode.setDouble(0);
    } else {
      ledEntry.setDouble(1);
      camMode.setDouble(1);
    }
    double steer = _joystick1.getZ();
    double drive = _joystick1.getY();
    boolean auto = false;// _joystick1.getRawButton(8);

    steer *= 1;
    drive *= -1;

    if (!auto) {
      if (m_LimelightHasValidTarget) {
        m_Drive.arcadeDrive(-m_LimelightDriveCommand, m_LimelightSteerCommand);
        System.out.println("Drive" + -m_LimelightDriveCommand);
        System.out.println("Steer" + -m_LimelightSteerCommand);
      }
      else if (_tavar > 3.0) {
        m_Drive.arcadeDrive(-0.3, 0);
      } else {
        m_Drive.arcadeDrive(0.0, 0.0);
      }
    } else {
      m_Drive.arcadeDrive(drive / 2, steer / 2);
    }
  }

  public void limelightShooter() {
    Update_Limelight_Tracking();
    ledEntry.setDouble(3);
    camMode.setDouble(0);

    boolean auto = false;// _joystick1.getRawButton(8);

    if (!auto) {
      if (m_LimelightHasValidTarget) {
        // _miscSpark.set()
        System.out.println("Drive" + -m_LimelightDriveCommand);

        System.out.println("Steer" + -m_LimelightSteerCommand);
      }
    }
  }

  public void buttonToggles() {
    // int speedbutton = 11;
    // if (_joystick1.getRawButtonPressed(speedbutton)) {
    //   if (!speedToggle) {
    //     speedToggle = true;
    //   } else if (speedToggle) {
    //     speedToggle = false;
    //   }
    // }



  }

  public void navxReadout() {
    SmartDashboard.putBoolean("IMU_Connected", navx.isConnected());
    SmartDashboard.putBoolean("IMU_IsCalibrating", navx.isCalibrating());
    SmartDashboard.putNumber("IMU_Yaw", navx.getYaw());
    SmartDashboard.putNumber("IMU_Pitch", navx.getPitch());
    SmartDashboard.putNumber("IMU_Roll", navx.getRoll());

    /* Display tilt-corrected, Magnetometer-based heading (requires */
    /* magnetometer calibration to be useful) */

    SmartDashboard.putNumber("IMU_CompassHeading", navx.getCompassHeading());

    /* Display 9-axis Heading (requires magnetometer calibration to be useful) */
    SmartDashboard.putNumber("IMU_FusedHeading", navx.getFusedHeading());

    /* These functions are compatible w/the WPI Gyro Class, providing a simple */
    /* path for upgrading from the Kit-of-Parts gyro to the navx-MXP */

    SmartDashboard.putNumber("IMU_TotalYaw", navx.getAngle());
    SmartDashboard.putNumber("IMU_YawRateDPS", navx.getRate());

    /* Display Processed Acceleration Data (Linear Acceleration, Motion Detect) */

    SmartDashboard.putNumber("IMU_Accel_X", navx.getWorldLinearAccelX());
    SmartDashboard.putNumber("IMU_Accel_Y", navx.getWorldLinearAccelY());
    SmartDashboard.putBoolean("IMU_IsMoving", navx.isMoving());
    SmartDashboard.putBoolean("IMU_IsRotating", navx.isRotating());

    /* Display estimates of velocity/displacement. Note that these values are */
    /* not expected to be accurate enough for estimating robot position on a */
    /* FIRST FRC Robotics Field, due to accelerometer noise and the compounding */
    /* of these errors due to single (velocity) integration and especially */
    /* double (displacement) integration. */

    SmartDashboard.putNumber("Velocity_X", navx.getVelocityX());
    SmartDashboard.putNumber("Velocity_Y", navx.getVelocityY());
    SmartDashboard.putNumber("Displacement_X", navx.getDisplacementX());
    SmartDashboard.putNumber("Displacement_Y", navx.getDisplacementY());

    /* Display Raw Gyro/Accelerometer/Magnetometer Values */
    /* NOTE: These values are not normally necessary, but are made available */
    /* for advanced users. Before using this data, please consider whether */
    /* the processed data (see above) will suit your needs. */

    SmartDashboard.putNumber("RawGyro_X", navx.getRawGyroX());
    SmartDashboard.putNumber("RawGyro_Y", navx.getRawGyroY());
    SmartDashboard.putNumber("RawGyro_Z", navx.getRawGyroZ());
    SmartDashboard.putNumber("RawAccel_X", navx.getRawAccelX());
    SmartDashboard.putNumber("RawAccel_Y", navx.getRawAccelY());
    SmartDashboard.putNumber("RawAccel_Z", navx.getRawAccelZ());
    SmartDashboard.putNumber("RawMag_X", navx.getRawMagX());
    SmartDashboard.putNumber("RawMag_Y", navx.getRawMagY());
    SmartDashboard.putNumber("RawMag_Z", navx.getRawMagZ());
    SmartDashboard.putNumber("IMU_Temp_C", navx.getTempC());

    /* Omnimount Yaw Axis Information */
    /* For more info, see http://navx-mxp.kauailabs.com/installation/omnimount */
    // navx.BoardYawAxis yaw_axis = navx.getBoardYawAxis();
    // SmartDashboard.putString( "YawAxisDirection", yaw_axis.up ? "Up" : "Down" );
    // SmartDashboard.putNumber( "YawAxis", yaw_axis.board_axis.getValue() );

    /* Sensor Board Information */
    SmartDashboard.putString("FirmwareVersion", navx.getFirmwareVersion());

    /* Quaternion Data */
    /* Quaternions are fascinating, and are the most compact representation of */
    /* orientation data. All of the Yaw, Pitch and Roll Values can be derived */
    /* from the Quaternions. If interested in motion processing, knowledge of */
    /* Quaternions is highly recommended. */
    SmartDashboard.putNumber("QuaternionW", navx.getQuaternionW());
    SmartDashboard.putNumber("QuaternionX", navx.getQuaternionX());
    SmartDashboard.putNumber("QuaternionY", navx.getQuaternionY());
    SmartDashboard.putNumber("QuaternionZ", navx.getQuaternionZ());

    /* Connectivity Debugging Support */
    SmartDashboard.putNumber("IMU_Byte_Count", navx.getByteCount());
    SmartDashboard.putNumber("IMU_Update_Count", navx.getUpdateCount());
  }

  // Basic Motor Functions, temp until subsystems and commands

  public void shooter(double Power_Positive) {
    _shooterMotorLeft.set(Power_Positive);
    _shooterMotorRight.set(-Power_Positive);
  }

  public void collectorOn(boolean Active, boolean Direction) {
    // false is normal, true is reversed.
    if (Active) {
      if (!Direction) {
        _ColectorMotor.set(ControlMode.PercentOutput, -0.75);
        _collectVert.set(-0.95);
      } else if (Direction) {
        _ColectorMotor.set(ControlMode.PercentOutput, 0.75);
        _collectVert.set(0.95);
      } else {
        _ColectorMotor.set(ControlMode.PercentOutput, 0);
        _collectVert.set(0);
      }
    } else {
      _ColectorMotor.set(ControlMode.PercentOutput, 0);
      _collectVert.set(0);
    }
  }

  public void limelightTracking(boolean isTracking) {
    if (!isTracking) {
      ledEntry.setDouble(1);
      camMode.setDouble(1);
    } else {
      ledEntry.setDouble(3);
      camMode.setDouble(0);
    }
  }

  // Put on smartdashboard
  public void smartdashboardUpdate() {

  }

  // Encoder Math
  // 42 Ticks Per Rev.

  public void startShooterTimer() {
    shooterTimer.reset();
    shooterTimer.start();
  }

  public void encoderProcessing() {
    SmartDashboard.putNumber("Left Encoder RAW", leftBack_encoder.getPosition());
    SmartDashboard.putNumber("Right Encoder RAW", rightBack_encoder.getPosition());

    SmartDashboard.putNumber("Left Encoder_Graph", leftBack_encoder.getPosition());
    SmartDashboard.putNumber("Right Encoder_Graph", rightBack_encoder.getPosition());

    //Add encoder data processing, so Encoder Ticks -> Shaft Rotations -> Gearbox rotations -> Inches Traveled

    //Aprox 9.5 'ticks' is one revolution on the Wheel

    //5 Inch Wheel at one revolution is 5*pi is inches? So in one rotation it would be 15.7 inches 
    //Circumfrence (Distance Traveled By Rotation) is C=d*Pi

    encoderInches = ((rightBack_encoder.getPosition() * Math.PI) / 1.5) * -1;
    SmartDashboard.putNumber("Right Encoder Inches Traveled", encoderInches);


  }

}

