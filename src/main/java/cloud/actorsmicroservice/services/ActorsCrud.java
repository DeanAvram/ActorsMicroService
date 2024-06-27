package cloud.actorsmicroservice.services;

import cloud.actorsmicroservice.entities.ActorEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ActorsCrud extends ReactiveMongoRepository<ActorEntity, String>{

}
