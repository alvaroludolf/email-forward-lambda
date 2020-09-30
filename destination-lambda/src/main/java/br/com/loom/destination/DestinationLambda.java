package br.com.loom.destination;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DestinationLambda implements RequestHandler<EnvelopeEvent, String> {

    private static final Logger logger = LoggerFactory.getLogger(DestinationLambda.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public String handleRequest(EnvelopeEvent event, Context context) {
        try {
            logger.info("ENVIRONMENT VARIABLES: {}", gson.toJson(System.getenv()));
            logger.info("CONTEXT: {}", gson.toJson(context));
            logger.info("EVENT: {}", event);

            return "ok";
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

}
