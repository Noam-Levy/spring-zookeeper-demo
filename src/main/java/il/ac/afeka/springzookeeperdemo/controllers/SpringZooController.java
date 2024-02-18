package il.ac.afeka.springzookeeperdemo.controllers;

import il.ac.afeka.springzookeeperdemo.boundaries.ServiceBoundary;
import il.ac.afeka.springzookeeperdemo.services.ZKService;
import org.apache.zookeeper.KeeperException;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class SpringZooController {
    private final ZKService zk;

    public SpringZooController(ZKService zk) {
        this.zk = zk;
    }

    @GetMapping("/discover")
    public Flux<ServiceBoundary> discoverServices(@RequestParam(defaultValue = "false") boolean includeSelf) {
        try {
            return Flux.fromStream(
                    this.zk.getAllNodes(includeSelf)
                            .stream()
                            .map(ServiceBoundary::new)
            );
        } catch (KeeperException | InterruptedException e) {
            return Flux.error(new RuntimeException(e.getMessage()));
        }
    }

    @GetMapping("/discover/master")
    public Mono<ServiceBoundary> getMasterService() {
        try {
            return Mono.just(this.zk.getLeaderNodeData())
                    .map(ServiceBoundary::new);
        } catch (KeeperException e) {
            if (e.code().equals(KeeperException.Code.NONODE))
                return Mono.empty();
            return Mono.error(e);
        } catch (InterruptedException e) {
            return Mono.error(e);
        }
    }

    @DeleteMapping("/{serviceId}")
    public Mono<Void> deleteService(@PathVariable String serviceId) {
        try {
            this.zk.deleteNodeFromCluster(serviceId);
            return Mono.empty();
        } catch (InterruptedException | KeeperException e) {
            return Mono.error(new Exception(e.getMessage()));
        }
    }

}
