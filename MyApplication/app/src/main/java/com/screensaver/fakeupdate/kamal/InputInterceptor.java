package com.screensaver.fakeupdate.kamal;

import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 1) List processes by reading "/proc/$PID/status"
 * 2) Find "InputReader" process
 * 3) Pause it by calling "kill -SIGSTOP $PID"
 * 4) Listen input events directly by calling "getevent"
 * 5) Resume InputReader by calling "kill -SIGCONT $PID"
 *
 * The method is blocked in Oreo (maybe Nougat) forward by Android because it is using "illegal system calls".
 * For them to be allowed we must have root access or SELinux must be set "permissive".
 */
public class InputInterceptor {
    private static String TAG = "InputInterceptor";

    public InputInterceptor() {

    }

    /**
     * Get process ids by listing them from filesystem
     * @return LinkedList of process ids or null
     * @throws IOException Executes commands
     * @throws InterruptedException Is waiting for exit
     * @throws TimeoutException Execution can timeout
     */
    @Nullable
    public LinkedList<Integer> listProcesses() throws IOException, InterruptedException, TimeoutException {
        // Debug
        String tag = TAG + ": listProcesses()";
        Log.d(tag, "Called");

        // Start process with root
        Process proc = Runtime.getRuntime().exec("su");

        // Input commands:
        // ls /proc
        // exit
        OutputStream stdin = proc.getOutputStream();
        stdin.write("ls /proc\n".getBytes());
        stdin.write("exit\n".getBytes());
        stdin.flush();
        stdin.close();

        // Gather process ids from the stdout
        LinkedList<Integer> pids = new LinkedList<>();
        BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String filename;
        while ((filename = stdout.readLine()) != null) {
            Boolean matches = filename.matches("^\\d+$");
            Log.d(tag, filename + ": " + matches.toString()); // Debug
            if (!matches) continue;
            pids.add(Integer.parseInt(filename));
        }
        stdout.close();

        // Handle timing out execution
        if (Build.VERSION.SDK_INT >= 26) {
            boolean exited = proc.waitFor(1L, TimeUnit.SECONDS);
            if (!exited) {
                proc.destroy();
                throw new TimeoutException();
            }
        } else {
            proc.waitFor();
        }
        Log.d(tag, "Exited with: " + Integer.toString(proc.exitValue())); // Debug

        return pids;
    }

    /**
     * Read process name from filesystem
     * @param pid Id of the process to get name for
     * @return Process name or null
     * @throws IOException Executes commands
     * @throws InterruptedException Is waiting for exit
     * @throws TimeoutException Execution can timeout
     */
    @Nullable
    public String readProcessName(int pid) throws IOException, InterruptedException, TimeoutException {
        // Debug
        String tag = TAG + ": readProcessName()";
        Log.d(tag, "Called");

        // Start process with root
        Process proc = Runtime.getRuntime().exec("su");

        // Input commands:
        // cat "/proc/$PID/status
        // exit
        OutputStream stdin = proc.getOutputStream();
        stdin.write(String.format("cat /proc/%s/status\n", Integer.toString(pid)).getBytes());
        stdin.write("exit\n".getBytes());
        stdin.flush();
        stdin.close();

        // Create regular expression patter for looking for process name in it's status
        Pattern namePattern = Pattern.compile("^Name:\\s+(.+)$");

        // Gather and parse status from the stdout
        BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line;
        while ((line = stdout.readLine()) != null) {
            Matcher result = namePattern.matcher(line);
            if (line == null) continue;

            Boolean matches = result.matches();
            Log.d(tag, line + ": " + matches.toString());  // Debug
            if (!result.matches()) continue;

            String processName = result.group();
            Log.d(tag, "Return: " + processName); // Debug
            return result.group();

        }
        stdout.close();

        // Handle timing out execution
        if (Build.VERSION.SDK_INT >= 26) {
            boolean exited = proc.waitFor(1L, TimeUnit.SECONDS);
            if (!exited) {
                proc.destroy();
                throw new TimeoutException();
            }
        } else {
            proc.waitFor();
        }
        Log.d(tag, "Exited with: " + Integer.toString(proc.exitValue())); // Debug

        return null;
    }

    /**
     * Find process id by name
     * @param targetName Process name to search for
     * @return Process id or null
     * @throws IOException Executes commands
     * @throws InterruptedException Is waiting for exit
     * @throws TimeoutException Execution can timeout
     */
    @Nullable
    public Integer findProcessByName(String targetName) throws IOException, InterruptedException, TimeoutException {
        // Debug
        String tag = TAG + ": readProcessName()";
        Log.d(tag, "Called");

        // List all processes
        LinkedList<Integer> pids = listProcesses();
        if (pids == null || pids.size() == 0) {
            // Something went wrong...
            // TODO: Handle it like a pro.
            Log.e(tag, "Process count is zero"); // Debug
            return null;
        }

        // Go through listed processes and try matching with given target
        Iterator<Integer> iterator = pids.descendingIterator();
        while (iterator.hasNext()) {
            Integer pid = iterator.next();
            String processName = readProcessName(pid);
            Log.d(tag, processName + " (" + pid.toString() + ")"); // Debug
            if (processName != null && processName.equals(targetName)) {
                Log.d(tag, "Return: " + pid.toString()); // Debug
                return pid;
            }
        }
        return null;
    }
}