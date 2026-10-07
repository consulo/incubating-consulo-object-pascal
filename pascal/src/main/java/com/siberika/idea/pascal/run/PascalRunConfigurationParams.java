package com.siberika.idea.pascal.run;

import consulo.execution.CommonProgramRunConfigurationParameters;

/**
 * Author: George Bakhtadze
 * Date: 07/01/2013
 */
public interface PascalRunConfigurationParams extends CommonProgramRunConfigurationParameters {
    String getProgramParameters();

    String getWorkingDirectory();

    boolean getFixIOBuffering();

    boolean getDebugMode();

    void setProgramParameters(String parameters);

    void setWorkingDirectory(String workingDirectory);

    void setFixIOBuffering(boolean value);

    void setDebugMode(boolean value);

    String getModuleName();

    void setModuleName(String name);
}
