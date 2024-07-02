package cloud.actorsmicroservice.services;

import cloud.actorsmicroservice.boundaries.ActorBoundary;
import cloud.actorsmicroservice.entities.ActorEntity;
import cloud.actorsmicroservice.exception.BadRequestException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ActorServiceImplantation implements ActorService {

    private ActorsCrud actors;

    public ActorServiceImplantation(ActorsCrud actors) {this.actors = actors;}

    @Override
    public Mono<ActorBoundary> createActor(ActorBoundary actor) {
        Mono<ActorEntity> entity = this.actors.findById(actor.getId());
        return entity.hasElement()
                .flatMap(
                        exists -> {
                            if (exists)
                                return Mono.error(new BadRequestException("Movie with id: " + actor.getId() + " already exists"));
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
                case ("name") -> this.actors.findAllByNameIgnoreCase(value);
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
                        actorEntity.setBirthdate(actor.getBirthdate());
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
        rv.setBirthdate(actorEntity.getBirthdate());
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
        rv.setBirthdate(actorBoundary.getBirthdate());
        rv.setName(actorBoundary.getName());
        rv.setMovies(actorBoundary.getMovies());
        return rv;
    }
}
