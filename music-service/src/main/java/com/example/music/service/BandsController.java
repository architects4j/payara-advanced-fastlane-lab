package com.example.music.service;

import com.example.music.service.faker.FakerService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 *
 */
@Path("/bands")
@ApplicationScoped
public class BandsController {

    @Inject
    private FakerService fakerService;

    //TODO: Should return a list of bands
    @GET
    public String listBands() {
        return "This should return a list of bands...";
    }


    //TODO: Should return a list of albums of a specific e.g. /bands/morrissey/albums
    public String listBandAlbums() {
        //TODO: Should throw a BandNotFoundException in case the band does not exist in the list
        //TODO: The BandNotFoundException should be returned as an HTTP 404
        return "This should return a list of albums of a specific band...";
    }


}
