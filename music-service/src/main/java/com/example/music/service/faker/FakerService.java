package com.example.music.service.faker;


import com.example.music.service.model.Band;
import com.github.javafaker.Faker;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

//TODO: Should be a singleton and be eagerly initialized during start up
@ApplicationScoped
public class FakerService {

    private static final Logger LOGGER = Logger.getLogger(FakerService.class.getName());

    @Inject
    private Faker faker;

    public List<Band> produceOneHundredBands() {
        List<Band> musicList = new ArrayList<>();
        Set<String> albums;

        for (int i = 0; i < 100; i++) {
            albums = new HashSet<>();
            albums.add(faker.resolve("music.albums"));
            albums.add(faker.resolve("music.albums"));

            musicList.add(Band.of(faker.resolve("music.bands"), faker.music().genre(), albums));
        }

        return musicList;
    }

}
