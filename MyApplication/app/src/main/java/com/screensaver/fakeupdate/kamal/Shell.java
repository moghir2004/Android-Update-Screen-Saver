package com.screensaver.fakeupdate.kamal;


import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Shell {
    private static String TAG = "Shell";
    public Process process;

    public Shell(boolean root) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        process = runtime.exec(root ? "su" : "sh");
    }

    public String exec(String command) throws IOException, InterruptedException, TimeoutException {
        return exec(command, -1);
    }
    public String exec(String command, int timeout) throws IOException, InterruptedException, TimeoutException {
        // Debug
        String tag = TAG + ": exec()";

        // Input the command
        OutputStream stdin = process.getOutputStream();
        stdin.write((command + "\n").getBytes());
        stdin.flush();
        stdin.close();

        // Read output
        BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = stdout.readLine()) != null) {
            Log.d(tag, line);
            builder.append(line + "\n");
        }
        stdout.close();

        // Handle timing out execution
        if (Build.VERSION.SDK_INT >= 26 && timeout > -1) {
            boolean exited = process.waitFor(Integer.toUnsignedLong(timeout), TimeUnit.MILLISECONDS);
            if (!exited) {
                process.destroy();
                throw new TimeoutException();
            }
        } else {
            process.waitFor();
        }
        Log.d(tag, "Process exited with: " + Integer.toString(process.exitValue())); // Debug

        // Finally return the output
        return builder.toString();
    }

    public void close() throws IOException, InterruptedException, TimeoutException {
        exec("exit", 100);
    }

}
