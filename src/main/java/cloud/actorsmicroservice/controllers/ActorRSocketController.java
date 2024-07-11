package cloud.actorsmicroservice.controllers;

import cloud.actorsmicroservice.boundaries.ActorMoviesBoundary;
import cloud.actorsmicroservice.boundaries.ActorSearchBoundray;
import cloud.actorsmicroservice.services.ActorMoviesService;
import cloud.actorsmicroservice.services.ActorService;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.MessageMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class ActorRSocketController {
    private ActorMoviesService actorMoviesService;

    public ActorRSocketController(ActorMoviesService actorMoviesService) {this.actorMoviesService = actorMoviesService;}

    //--fnf --route=Delete-All-Actors--debug tcp://localhost:7002
@MessageMapping("Delete-All-Actors")
    public Mono<Void> deleteAllActors(){
        return this.actorMoviesService
                .deleteAllActors()
                .log();
    }

   // --channel --route=get-actors-by-id-channel --data=- --debug tcp://localhost:7002
    @MessageMapping("get-actors-by-id-channel")
    public Flux<ActorMoviesBoundary> getById (
            @Payload Flux<ActorSearchBoundray> filters){
        //this.log.trace("initialized channel with consumer...");

        return filters
                .flatMap(filter->
                this.actorMoviesService
                .getActorsWithMoviesByCriteria(
                        filter.getCriteria(),filter.getValue()))
                .log();
    }
}
