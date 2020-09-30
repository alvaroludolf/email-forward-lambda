package br.com.loom.source;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class SourceLambda implements RequestHandler<ScheduledEvent, MessageEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SourceLambda.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public MessageEvent handleRequest(ScheduledEvent event, Context context) {
        try {
            logger.info("ENVIRONMENT VARIABLES: {}", gson.toJson(System.getenv()));
            logger.info("CONTEXT: {}", gson.toJson(context));
            logger.info("EVENT: {}", gson.toJson(event));

            return new MessageEvent()
                    .setMessage("Message")
                    .setData(new Date())
                    .setFlag(true)
                    .setNumber(1023L);

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

}
