package cloud.actorsmicroservice.services;

import cloud.actorsmicroservice.boundaries.ActorBoundary;
import cloud.actorsmicroservice.entities.ActorEntity;
import cloud.actorsmicroservice.exception.BadRequestException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Service
public class ActorServiceImplantation implements ActorService {

    private ActorsCrud actors;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

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



    @Override
    public Flux<ActorBoundary> getAllActors() {
        return this.actors
                .findAll()
                .map(this::actorToBoundary);
    }



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
    public Mono<Void> updateActor(String id, String email, ActorBoundary actor) {
            return this.actors.findById(id)
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
    public Mono<Void> deleteActor(String id, String email) {
        return this.actors.findById(id)
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
}
