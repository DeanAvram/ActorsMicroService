package cloud.actorsmicroservice.services;

import cloud.actorsmicroservice.boundaries.ActorBoundary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ActorService {
    public Mono<ActorBoundary> createActor(ActorBoundary actor);
    public Flux<ActorBoundary> getAllActors();
    public Flux<ActorBoundary> getActorByCriteria(String criteria, String value);
    public Mono<Void> updateActor(String id, String email, ActorBoundary actor);
    public Mono<Void> deleteActor(String id, String email);
    public Mono<Void> deleteAllActors();
}
