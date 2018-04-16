package com.screensaver.fakeupdate.kamal;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.typeCompatibleWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class InputInterceptorTest {
    InputInterceptor interceptor;

    @Before
    public void initialize() {
        interceptor = new InputInterceptor();
    }

    @Test
    public void listProcesses() throws Exception {
        LinkedList<Integer> pids = interceptor.listProcesses();
        assertThat(pids, not(null));

        // There's at application and processes started by it,
        // but if method were successful it should return all processes.
        // 10 is a gross under estimation.
        assertThat(pids.size(), greaterThan(10));

        Iterator<Integer> iterator = pids.descendingIterator();
        while (iterator.hasNext()) {
            Integer pid = iterator.next();
            assertThat(pid, greaterThan(0));
        }
    }

    @Test
    public void readProcessName() throws Exception {
        String processName = interceptor.readProcessName(1);
        assertThat(processName, not(null));
        assertThat(processName, is("/init"));
    }

    @Test
    public void findProcessByName() throws Exception {
        Integer pid = interceptor.findProcessByName("system_server");
        assertThat(pid, not(null));

        // Assuming that pids lower than 300 is reserved for kernel
        assertThat(pid, greaterThan(300));
    }

}