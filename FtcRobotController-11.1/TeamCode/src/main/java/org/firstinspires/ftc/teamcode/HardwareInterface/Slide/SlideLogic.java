package org.firstinspires.ftc.teamcode.HardwareInterface.Slide;

public class SlideLogic {


    private final SlideControl slideControl;
    private int slideExtensionTarget = 0;
    private final SlideProperties slideProperties;
    private int slideMaxExtension;
    private int slideMinExtension;
    private int slideExtensionStep;
    private int slideFirstExtentionStep;

    public SlideLogic(SlideControl slideControl, SlideProperties slideProperties) {
        this.slideControl = slideControl;
        this.slideProperties = slideProperties;
        setSlideProperties();
        resetEncoders();
    }

    public void setSlideProperties()
    {
        this.slideMaxExtension = slideProperties.getSlideMaxExtension();
        this.slideMinExtension = slideProperties.getSlideMinExtension();
        this.slideExtensionStep = slideProperties.getSlideExtensionStep();
        this.slideFirstExtentionStep = slideProperties.getSlideFirstExtensionStep();
    }

    public boolean slidesBottomReached() {
        return slideControl.isLimitSwitchPressed();
    }

    public int getSlideExtensionTarget() {
        return slideExtensionTarget;
    }

    public void setSlideExtensionTarget(int slideExtensionTarget) {
        if (isExtensionTargetNotInBounds(slideExtensionTarget)) return;
        this.slideExtensionTarget = slideExtensionTarget;
        slideControl.setSlidePosition(slideExtensionTarget);
    }

    public void addSlideExtension(int addSlideExtension) {
        if (isExtensionTargetNotInBounds(slideExtensionTarget + addSlideExtension)) return;
        this.slideExtensionTarget += addSlideExtension;
        slideControl.setSlidePosition(slideExtensionTarget);
    }

    public void stepUp()
    {
        addSlideExtension(slideExtensionStep);
    }

    public void stepDown()
    {
        addSlideExtension(-slideExtensionStep);
    }

    public boolean isExtensionTargetNotInBounds(int slideExtensionTarget) {
        return slideExtensionTarget < slideMinExtension || slideExtensionTarget > slideMaxExtension;
    }

    public void resetEncoders() {
        slideControl.resetEncoders();
    }

    public void setMaxSpeed(double maxSpeed){
        slideProperties.setSlideMaxSpeed(maxSpeed);
    }
    public int getSlidePosition() {
        //System.out.println(slideControl.getSlidePosition());
        return slideControl.getSlidePosition();
    }

    public void limitSpeed(double speed) {
        slideControl.limitSpeed(speed);
    }
}