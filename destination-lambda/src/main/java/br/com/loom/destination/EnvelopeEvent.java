package br.com.loom.destination;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class EnvelopeEvent {

    String version;
    String timestamp;
    MessageEvent responsePayload;

}
