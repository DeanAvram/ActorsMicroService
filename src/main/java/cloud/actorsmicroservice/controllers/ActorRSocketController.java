package cloud.actorsmicroservice.controllers;

import cloud.actorsmicroservice.boundaries.ActorMoviesBoundary;
import cloud.actorsmicroservice.boundaries.ActorSearchBoundray;
import cloud.actorsmicroservice.services.ActorMoviesService;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.MessageMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class ActorRSocketController {
    private ActorMoviesService actorMoviesService;

    public ActorRSocketController(ActorMoviesService actorMoviesService) {this.actorMoviesService = actorMoviesService;}

    //--fnf --route=delete-all-actors--debug tcp://localhost:7002
@MessageMapping("delete-all-actors")
    public Mono<Void> deleteAllActors(){
        return this.actorMoviesService
                .deleteAllActors()
                .log();
    }

   // --channel --route=get-actors-by-criteria-channel --data=- --debug tcp://localhost:7002
   @MessageMapping("get-actors-by-criteria-channel")
   public Flux<ActorMoviesBoundary> getByCriteria(
           @Payload Flux<ActorSearchBoundray> filters) {
       return filters.flatMap(filter -> {
           if (filter.getCriteria() == null && filter.getValue() == null) {
               return this.actorMoviesService.getAllActorsWithMovies();
           } else {
               return this.actorMoviesService
                       .getActorsWithMoviesByCriteria(filter.getCriteria(), filter.getValue())
                       .log();
           }
       });
   }

}