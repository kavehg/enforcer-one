package io.enforcer.deathstar.services;

import io.enforcer.deathstar.pojos.Action;
import io.enforcer.deathstar.pojos.Report;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.junit.Assert.*;

/**
 * Created by kavehg on 7/30/2015.
 */
public class ReportServiceTest {

    private ReportService reportService;
    private Report report_1;
    private Report report_2;

    @Before
    public void setUp() throws Exception {
        reportService = new ReportService();
        report_1 = new Report(1, "main", "ADDED", "hostABC", "2015-07-22T21:00:00Z");
        report_2 = new Report(2, "main", "ADDED", "hostDEF", "2015-08-22T21:00:00Z");
    }

    @After
    public void tearDown() throws Exception {

    }

    /**
     * When started the report service is empty
     */
    @Test
    public void reportServiceEmptyAtStart() {
        assertEquals((Integer)0, reportService.numberOfReceivedReports());
    }

    /**
     * When a report is added the number of received reports goes up
     *
     * @throws Exception
     */
    @Test
    public void testAddReport() throws Exception {
        reportService.addReport(report_1);
        assertEquals((Integer)1, reportService.numberOfReceivedReports());
    }

    /**
     * Can get existing actions if report gets added twice
     */
    @Test
    public void addingExistingReportReturnsAssociatedActions() {
        // first time a report is added, null should be returned
        ConcurrentLinkedDeque<Action> noExistingActions = reportService.addReport(report_1);
        assertNull(noExistingActions);

        // second time a report is added, its associated actions should be returned
        ConcurrentLinkedDeque<Action> existingActionsEmpty = reportService.addReport(report_1);
        assertEquals(0, existingActionsEmpty.size());
    }

    /**
     * Test that getting all reports returns the complete set
     */
    @Test
    public void testGetAllReports() {
        HashSet<Report> reports = new HashSet<>();
        reports.add(report_1);
        reports.add(report_2);

        reportService.addReport(report_1);
        reportService.addReport(report_2);

        assertEquals(reports, reportService.getAllReports());
    }
}