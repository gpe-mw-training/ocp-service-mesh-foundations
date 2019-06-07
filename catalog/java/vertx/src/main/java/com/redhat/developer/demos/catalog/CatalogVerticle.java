package com.redhat.developer.demos.catalog;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class CatalogVerticle extends AbstractVerticle {

    private static final String RESPONSE_STRING_FORMAT = "catalog v1 from '%s': %d\n";

    private String HOSTNAME;
    private int LISTEN_ON;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Counter to help us see the lifecycle
     */
    private int count = 0;

    @Override
    public void start() throws Exception {
    	
    	try {
			logger.info("start(): BEGIN");
			
			Router router = Router.router(vertx);
			router.get("/").handler(this::logging);
			router.get("/").handler(this::getCatalogData);

			HealthCheckHandler hc = HealthCheckHandler.create(vertx);
			hc.register("dummy-health-check", future -> future.complete(Status.OK()));
			router.get("/health").handler(hc);

			HOSTNAME = "demo";
			LISTEN_ON = 8080;
			
			logger.info("HOSTNAME = " +         HOSTNAME);
			logger.info("LISTEN_ON = " + LISTEN_ON);

			logger.info("start(): starting server");
			vertx.createHttpServer().requestHandler(router::accept).listen(LISTEN_ON);
		} catch (Exception exc) {
			logger.info("start() EXCEPTION:" + exc);
			exc.printStackTrace();
		}
    }

    private void logging(RoutingContext ctx) {
        logger.info(String.format("catalog request from %s: %d", HOSTNAME, count));
        ctx.next();
    }

    private void getCatalogData(RoutingContext ctx) {
            count++;
            ctx.response().end(String.format(RESPONSE_STRING_FORMAT, HOSTNAME, count));
    }

    protected static String parseContainerIdFromHostname(String hostname) {
        return hostname.replaceAll("catalog-v\\d+-", "");
    }
    
    public static void main(String[] args) {
    	
        Vertx.vertx().deployVerticle(new CatalogVerticle());
    }

}
