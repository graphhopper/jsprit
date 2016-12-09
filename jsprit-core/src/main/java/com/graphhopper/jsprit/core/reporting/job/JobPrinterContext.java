package com.graphhopper.jsprit.core.reporting.job;

import com.graphhopper.jsprit.core.problem.job.AbstractJob;
import com.graphhopper.jsprit.core.reporting.PrinterContext;

/**
 * The context of the detailed route printer columns.
 *
 * <p>
 * This is a semi-mutable class: only the activity could be altered. Therefore
 * for each route a new instance should be created.
 * </p>
 *
 * @author balage
 *
 */
public interface JobPrinterContext extends PrinterContext {

    public AbstractJob getJob();

}
