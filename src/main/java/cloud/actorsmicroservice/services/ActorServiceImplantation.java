package cloud.actorsmicroservice.services;

import cloud.actorsmicroservice.boundaries.ActorBoundary;
import cloud.actorsmicroservice.boundaries.ActorMoviesBoundary;
import cloud.actorsmicroservice.entities.ActorEntity;
import cloud.actorsmicroservice.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ActorServiceImplantation implements ActorMoviesService {

    private ActorsCrud actors;
    private WebClient moviesWebClient;
    private WebClient usersWebClient;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Value("${remote.movies.service.url: http://localhost:9090/movies}")
    public void setMoviesWebClientWebClient(String remoteServiceUrl){
        //System.err.println("%%%" + remoteServiceUrl);
        this.moviesWebClient = WebClient.create(remoteServiceUrl);
    }

    @Value("${remote.users.service.url: http://localhost:8080/users}")
    public void setUsersWebClientWebClient(String remoteServiceUrl){
        //System.err.println("%%%" + remoteServiceUrl);
        this.usersWebClient = WebClient.create(remoteServiceUrl);
    }



    public ActorServiceImplantation(ActorsCrud actors) {this.actors = actors;}

    @Override
    public Mono<ActorBoundary> createActor(ActorBoundary actor) {
        Mono<ActorEntity> entity = this.actors.findById(actor.getId());
        return entity.hasElement()
                .flatMap(
                        exists -> {
                            if (exists)
                                return Mono.error(new BadRequestException("Actor with id: " + actor.getId() + " already exists"));
                            return Mono.just(actor);
                        })
                .map(this::actorToEntity)
                .flatMap(this.actors::save)
                .map(this::actorToBoundary);
    }


    @Deprecated
    @Override
    public Flux<ActorBoundary> getAllActors() {
        return this.actors
                .findAll()
                .map(this::actorToBoundary);
    }

    @Override
    public Flux<ActorMoviesBoundary> getAllActorsWithMovies() {
        return actors.findAll().flatMap(this::getActorMoviesBoundary);
        /*return this.actors.findAll()
                .flatMap(actor -> {
                    Flux<Set<Map<String, Object>>> moviesFlux = Flux.fromIterable(actor.getMovies())
                            .flatMap(movieId -> this.moviesWebClient.get()
                                    .uri(uriBuilder -> uriBuilder
                                            .queryParam("criteria", "id")
                                            .queryParam("value", movieId)
                                            .build())
                                    .retrieve()
                                    .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                                    .collectList()
                                    .map(HashSet::new));
                    return moviesFlux.collectList()
                            .flatMap(movieSets -> {

                                ActorMoviesBoundary actorMoviesBoundary = actorToActorMoviesBoundary(actor);
                                actorMoviesBoundary.setMovies(new HashSet<>());
                                movieSets.forEach(actorMoviesBoundary.getMovies()::addAll);
                                return Mono.just(actorMoviesBoundary);
                            });
                });*/
    }



    @Deprecated
    @Override
    public Flux<ActorBoundary> getActorByCriteria(String criteria, String value) {
        Flux<ActorEntity> actors;
        try {
            actors = switch (criteria) {
                case ("id") -> this.actors.findById(value).flux();
                case ("name") -> this.actors.findAllByNameContainsIgnoreCase(value);
                default -> throw new BadRequestException("Invalid criteria: " + criteria);
            };
        }
        catch (NumberFormatException e) {
            throw new BadRequestException("Invalid value for criteria: " + criteria + ". Value must be an integer.");
        }
        return actors.map(this::actorToBoundary);
    }

    @Override
    public Flux<ActorMoviesBoundary> getActorsWithMoviesByCriteria(String criteria, String value) {
        Flux<ActorEntity> actors;
        try {
            actors = switch (criteria) {
                case ("id") -> this.actors.findById(value).flux();
                case ("name") -> this.actors.findAllByNameContainsIgnoreCase(value);
                default -> throw new BadRequestException("Invalid criteria: " + criteria);
            };
        }
        catch (NumberFormatException e) {
            throw new BadRequestException("Invalid value for criteria: " + criteria + ". Value must be an integer.");
        }
        return actors.flatMap(this::getActorMoviesBoundary);
    }

    @Override
    public Mono<Void> updateActor(String id, String email, String password, ActorBoundary actor) {
        return isUserExists(email, password)
                .then(this.actors.findById(id))
                .switchIfEmpty(Mono.error(new BadRequestException("Actor with id: " + id + " does not exist.")))
                .flatMap(actorEntity -> {
                    if (actor.getName() != null)
                        actorEntity.setName(actor.getName());
                    if (actor.getBirthdate() != null)
                        actorEntity.setBirthdate(convertToDate(actor.getBirthdate()));
                    if (actor.getMovies() != null)
                        actorEntity.setMovies(actor.getMovies());
                    return this.actors.save(actorEntity);
                })
                .then();
    }

    @Override
    public Mono<Void> deleteActor(String id, String email, String password) {
        return isUserExists(email, password)
                .then(this.actors.findById(id))
                .switchIfEmpty(Mono.error(new BadRequestException("Actor with id: " + id + " does not exist.")))
                .flatMap(actors::delete)
                .then();
    }

    @Override
    public Mono<Void> deleteAllActors() {
        return this.actors.deleteAll();
    }

    private ActorBoundary actorToBoundary(ActorEntity actorEntity) {
        ActorBoundary rv = new ActorBoundary();
        rv.setId(actorEntity.getId());
        rv.setBirthdate(actorEntity.getBirthdate().format(formatter));
        rv.setName(actorEntity.getName());
        rv.setMovies(actorEntity.getMovies());
    return rv;
    }

    private ActorMoviesBoundary actorToActorMoviesBoundary(ActorEntity actorEntity) {
        ActorMoviesBoundary rv = new ActorMoviesBoundary();
        rv.setId(actorEntity.getId());
        rv.setBirthdate(actorEntity.getBirthdate().format(formatter));
        rv.setName(actorEntity.getName());
        return rv;
    }

    private Mono<ActorMoviesBoundary> getActorMoviesBoundary(ActorEntity actor) {
        Flux<Set<Map<String, Object>>> moviesFlux = Flux.fromIterable(actor.getMovies())
                .flatMap(movieId -> this.moviesWebClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .queryParam("criteria", "id")
                                .queryParam("value", movieId)
                                .build())
                        .retrieve()
                        .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .collectList()
                        .map(HashSet::new));

        return moviesFlux.collectList()
                .flatMap(movieSets -> {
                    ActorMoviesBoundary actorMoviesBoundary = actorToActorMoviesBoundary(actor);
                    actorMoviesBoundary.setMovies(new HashSet<>());
                    movieSets.forEach(actorMoviesBoundary.getMovies()::addAll);
                    return Mono.just(actorMoviesBoundary);
                });
    }

    private ActorEntity actorToEntity(ActorBoundary actorBoundary) {
        ActorEntity rv = new ActorEntity();
        if(actorBoundary.getId() == null) {
            throw new BadRequestException("Actor id cannot be null.");
        }
        rv.setId(actorBoundary.getId());
        if (actorBoundary.getBirthdate() == null)
            throw new BadRequestException("Birthdate must be provided.");
        try {
            rv.setBirthdate(convertToDate(actorBoundary.getBirthdate()));
        } catch (BadRequestException e) {
            throw new BadRequestException("Invalid date format. Please use dd-mm-yyyy format.");
        }
        rv.setName(actorBoundary.getName());
        rv.setMovies(actorBoundary.getMovies());
        return rv;
    }

    private LocalDate convertToDate(String dateString) throws BadRequestException {
        try {
            return LocalDate.parse(dateString, formatter);
        } catch (Exception e) {
            throw new BadRequestException("Invalid date format. please use dd-mm-yyyy format.");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}(\\.[A-Za-z]{2,})?$";
        Pattern pattern = Pattern.compile(emailRegex);
        return email != null && pattern.matcher(email).matches();
    }

    private Mono<Boolean> isUserExists(String email, String password) {
        if (!isValidEmail(email))
            return Mono.error(new BadRequestException("Invalid email format."));
        return this.usersWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{email}")
                        .queryParam("password", password)
                        .build(email))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new BadRequestException("Can not find or authenticate user: " + email) ))
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful());
    }


}
