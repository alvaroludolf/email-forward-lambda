package br.com.loom.destination;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class MessageEvent {

    String message;
    long number;
    Date data;
    boolean flag;
}
