package org.ops4j.pax.web.service.webapp.proxy;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A proxy servlet to relay to an OSGi bridge servlet
 */
public class ProxyServlet extends HttpServlet {

    private ServletDispatcherTracker servletDispatcherTracker = null;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            this.servletDispatcherTracker = new ServletDispatcherTracker(getBundleContext());
            this.servletDispatcherTracker.open();
        } catch (InvalidSyntaxException e) {
            servletDispatcherTracker = null;
            throw new ServletException("Error during proxy servlet init", e);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Servlet dispatcherServlet = this.servletDispatcherTracker.getServlet();
        if (dispatcherServlet != null) {
            dispatcherServlet.service(req, resp);
        }
    }

    @Override
    public void destroy() {
        this.servletDispatcherTracker.close();
        super.destroy();
    }

    private BundleContext getBundleContext()
            throws ServletException {
        Object context = getServletContext().getAttribute(BundleContext.class.getName());
        if (context == null) {
            throw new ServletException("Bundle context attribute [" + BundleContext.class.getName() +
                    "] not set in servlet context");
        }
        if (context instanceof BundleContext) {
            return (BundleContext) context;
        }
        throw new ServletException("Bundle context attribute [" + BundleContext.class.getName() +
                "] is not of type BundleContext but of type " + context.getClass());
    }

}
