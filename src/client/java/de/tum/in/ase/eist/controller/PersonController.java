package de.tum.in.ase.eist.controller;

import de.tum.in.ase.eist.model.Note;
import de.tum.in.ase.eist.model.Person;
import de.tum.in.ase.eist.util.PersonSortingOptions;
import io.netty.handler.codec.http.HttpResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PersonController {
    private final WebClient webClient;
    private final List<Person> persons;

    public PersonController() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8080/")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.persons = new ArrayList<>();
    }

    @RequestMapping(value = { "/create", "/" }, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void addPerson(Person person, Consumer<List<Person>> personsConsumer) {
        // Part 2: Make an http post request to the server
        webClient.post()
                .uri("persons")
                .bodyValue(person)
                .retrieve()
                .bodyToMono(Person.class)
                .onErrorStop()
                .subscribe(newPerson -> {
                    persons.add(newPerson);
                    personsConsumer.accept(persons);
                });
    }

    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void updatePerson(Person person, Consumer<List<Person>> personsConsumer) {
        // Part 2: Make an http put request to the server
        webClient.put()
                .uri("persons/" + person.getId())
                .bodyValue(person)
                .retrieve()
                .bodyToMono(Person.class)
                .onErrorStop()
                .subscribe(newPerson -> {
                    persons.replaceAll(oldPerson -> oldPerson.getId().equals(newPerson.getId()) ? newPerson : oldPerson);
                    personsConsumer.accept(persons);
                });
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deletePerson(Person person, Consumer<List<Person>> personsConsumer) {
        // Part 2: Make an http delete request to the server
        webClient.delete()
                .uri("persons/" + person.getId())
                .retrieve()
                .toBodilessEntity()
                .onErrorStop()
                .subscribe(v -> {
                    persons.remove(person);
                    personsConsumer.accept(persons);
                });
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public void getAllPersons(PersonSortingOptions sortingOptions, Consumer<List<Person>> personsConsumer) {
        // Part 2: Make an https get request to the server
        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("persons")
                        .queryParam("sortField", "ID")
                        .queryParam("sortingOrder", "ASCENDING")
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Person>>() {})
                .onErrorStop()
                .subscribe(newPersons -> {
                    persons.clear();
                    persons.addAll(newPersons);
                    personsConsumer.accept(persons);
                });
    }
}
