package cloud.actorsmicroservice.services;

import cloud.actorsmicroservice.boundaries.ActorBoundary;
import cloud.actorsmicroservice.boundaries.ActorMoviesBoundary;
import reactor.core.publisher.Flux;

public interface ActorMoviesService extends ActorService{

    public Flux<ActorMoviesBoundary> getAllActorsWithMovies();
    public Flux<ActorMoviesBoundary> getActorsWithMoviesByCriteria(String criteria, String value);
}
