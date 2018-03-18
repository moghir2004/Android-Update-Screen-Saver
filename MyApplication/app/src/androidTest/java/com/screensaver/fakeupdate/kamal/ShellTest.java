package com.screensaver.fakeupdate.kamal;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ShellTest {
    private Shell shell;

    @Before
    public void initialize() throws IOException {
        shell = new Shell(true);
    }

    @Test
    public void exec() throws Exception {
        String output = shell.exec("echo hello");
        assertThat(output, is("hello"));
    }

    @Test
    public void execTimeout() throws Exception {
        try {
            shell.exec("sleep 1", 500);
            fail("Did not throw TimeoutException");
        } catch (TimeoutException e) {
            // Success
        }
    }

}