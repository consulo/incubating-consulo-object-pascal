package com.siberika.idea.pascal.jps.util;

import consulo.process.event.ProcessAdapter;
import consulo.process.event.ProcessEvent;
import consulo.util.dataholder.Key;
import org.jetbrains.annotations.NotNull;

public abstract class PascalConsoleProcessAdapter extends ProcessAdapter {
    private StringBuffer sb = new StringBuffer();

    abstract public boolean onLine(String text);

    @Override
    public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
        sb.append(event.getText());
        int end;
        while ((end = sb.indexOf("\n")) >= 0) {
            doProcessLine(sb.substring(0, end));
            sb.delete(0, end + 1);
        }
    }

    @Override
    public void processTerminated(@NotNull ProcessEvent event) {
        super.processTerminated(event);
        if (sb.length() > 0) {
            doProcessLine(sb.toString());
            sb = new StringBuffer();
        }
    }

    private void doProcessLine(String line) {
        onLine(line.endsWith("\r") ? line.substring(0, line.length() - 1) : line);
    }

}
