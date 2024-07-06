package cloud.actorsmicroservice.controllers;

import cloud.actorsmicroservice.boundaries.ActorBoundary;
import cloud.actorsmicroservice.boundaries.ActorMoviesBoundary;
import cloud.actorsmicroservice.exception.BadRequestException;
import cloud.actorsmicroservice.services.ActorMoviesService;
import cloud.actorsmicroservice.services.ActorService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/actors")
public class ActorsController {

    private ActorMoviesService actorsService;


    public ActorsController(ActorMoviesService actorsService) {this.actorsService = actorsService;}

    @GetMapping(
            produces = {MediaType.TEXT_EVENT_STREAM_VALUE}
    )
    public Flux<ActorMoviesBoundary> getActors(@RequestParam(value = "criteria", required = false) String criteria,
                                               @RequestParam(value = "value", required = false) String value) {
        if (criteria == null && value == null)
            return this.actorsService.getAllActorsWithMovies();
        if (criteria != null && value != null)
            return this.actorsService.getActorsWithMoviesByCriteria(criteria, value);
        throw new BadRequestException("Provide both criteria and value or none.");

    }

    @PostMapping(
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    public Mono<ActorBoundary> createActor(@RequestBody ActorBoundary actor) {
        return actorsService.createActor(actor);
    }


    @PutMapping(
            path = "/{actorId}",
            consumes = {MediaType.APPLICATION_JSON_VALUE}
    )
    public Mono<Void> updateActor(@PathVariable(name = "actorId") String actorId, @RequestParam(name = "email") String email, @RequestBody ActorBoundary actor) {
        return actorsService.updateActor(actorId, email, actor);
    }

    @DeleteMapping(
            path = "/{actorId}"
    )
    public Mono<Void> deleteActor(@PathVariable(name = "actorId") String actorId, @RequestParam(name = "email") String email) {
        return actorsService.deleteActor(actorId, email);
    }

    @DeleteMapping
    public Mono<Void> deleteAllActors() {
        return actorsService.deleteAllActors();
    }

}
