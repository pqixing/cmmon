package com.pqixing.shell;

public class AppCommand extends com.stericson.RootShell.execution.Command {
    private static int id = 0;
    private Result result;

    public AppCommand(String[] cmds, Result result) {
        super(id++, cmds);
        this.result = result;
    }

    @Override
    public void commandOutput(int id, String line) {
        super.commandOutput(id, line);
        if (result != null) result.onCommand(false, line);
    }

    @Override
    public void commandCompleted(int id, int exitcode) {
        super.commandCompleted(id, exitcode);
        if (result != null) result.onCommand(true, exitcode + "");
    }

    public static interface Result {
        public void onCommand(boolean complete, String result);

    }
}
