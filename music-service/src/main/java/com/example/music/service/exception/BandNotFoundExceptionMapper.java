package com.example.music.service.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

//TODO: Should be annotated with @Provider
//TODO: Should implement the interface ExceptionMapper<BandNotFoundException>
public class BandNotFoundExceptionMapper {

   // @Override
    public Response toResponse(BandNotFoundException exception) {
        //TODO: Return HTTP 404 for BandNotFoundException
        return Response
                .status(Response.Status.OK)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new Message())
                .build();
    }

    public static class Message {
        private final Long id;
        private final String name;
        private final String description;

        public Message() {
            this.id = 1L;
            //TODO: Initialize the message with a name
            this.name = "";
            //TODO: Initialize the message with a description
            this.description = "";
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
}
