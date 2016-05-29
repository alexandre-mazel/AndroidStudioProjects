package com.electronoos.blangle;

/**
 * Created by a on 29/05/16.
 */
public class Printeuse {

    private static final int NBR_MAX_CINEMATIC_ELEMENT = 16;

    private String strName_;
    private float refA_; // mm
    private float refB_; // mm
    private float refC_; // mm
    private CinematicElement[] aEl_;

    public static final int EDIT_STEP_INIT = 0;
    public static final int EDIT_STEP_NAME = 1;
    public static final int EDIT_STEP_REFERENCE = 2;
    public static final int EDIT_STEP_DEV_CYL = 3;
    public static final int EDIT_STEP_LONG_RACLE = 4;
    public static final int EDIT_STEP_CINEMATIC_CHOICE = 5;
    public static final int EDIT_STEP_CINEMATIC_UPPER = 6;

    private int nEditStep_; //

    public Printeuse()
    {
        aEl_ = new CinematicElement[NBR_MAX_CINEMATIC_ELEMENT];
        nEditStep_ = EDIT_STEP_INIT;
    }

    public void updateEditStep()
    {
        nEditStep_ += 1;
    }
    public int getEditStep() //const
    {
        return nEditStep_;
    }

}
