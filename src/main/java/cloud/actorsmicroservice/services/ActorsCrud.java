package cloud.actorsmicroservice.services;

import cloud.actorsmicroservice.entities.ActorEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;

public interface ActorsCrud extends ReactiveMongoRepository<ActorEntity, String>{

    public Flux<ActorEntity> findAllByNameContainsIgnoreCase(@Param("value") String name);
}
