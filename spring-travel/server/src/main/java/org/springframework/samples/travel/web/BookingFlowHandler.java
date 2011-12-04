package org.springframework.samples.travel.web;

import org.springframework.webflow.core.FlowException;
import org.springframework.webflow.execution.FlowExecutionOutcome;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.mvc.servlet.AbstractFlowHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * the {@link org.springframework.webflow.mvc.servlet.FlowHandler} is a generic interface that you can implement to
 * tap into certain lifecycle events in Spring Webflow. This is useful, for example, in handling exceptions in flows in a generic, high level
 * context.
 */
public class BookingFlowHandler extends AbstractFlowHandler {

    private static final String REDIRECT_URL = "contextRelative:/hotels/search";


    @Override
    public String handleExecutionOutcome(FlowExecutionOutcome outcome, HttpServletRequest request, HttpServletResponse response) {
        return REDIRECT_URL;
    }

    /**
     * when there's an exception in a web flow of the type {@link NoSuchFlowExecutionException which is caused when a session's expired and the Webflow's no longer being maintained},
     * redirect to {@code  /hotels/search } (the start of the flow).
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return the logical path to redirect to based on the inputs.
     */
    @Override
    public String handleException(FlowException e, HttpServletRequest request, HttpServletResponse response) {
        if (e instanceof NoSuchFlowExecutionException) {
            return REDIRECT_URL;
        } else {
            throw e;
        }
    }

}
