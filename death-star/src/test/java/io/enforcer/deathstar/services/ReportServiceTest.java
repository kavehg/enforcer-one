package io.enforcer.deathstar.services;

import io.enforcer.deathstar.pojos.Report;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by kavehg on 7/30/2015.
 */
public class ReportServiceTest {

    private ReportService reportService;
    private Report report_1;

    @Before
    public void setUp() throws Exception {
        reportService = new ReportService();
        report_1 = new Report(1, "main", "ADDED", "host", "2015-07-22T21:00:00Z");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testAddReport() throws Exception {
        reportService.addReport(report_1);
        assertEquals((Integer)1, reportService.numberOfReceivedReports());
    }
}