/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserve.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.cooperlyt.cloud.uid.worker;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Represents an implementation of {@link WorkerIdAssigner},
 * the worker id will be discarded after assigned to the UidGenerator
 *
 * @author yutianbao
 */

@Slf4j
public abstract class HostWorkerIdAssigner implements WorkerIdAssigner {

    private final WorkerNodeIdent workerNodeIdent;

    protected HostWorkerIdAssigner(WorkerNodeIdent workerNodeIdent) {
        this.workerNodeIdent = workerNodeIdent;
    }


    protected abstract Mono<Long> assignWorkerId(WorkerNodeIdent workerNodeIdent);
    /**
     * Assign worker id base on database.<p>
     * If there is host name & port in the environment, we considered that the node runs in Docker container<br>
     * Otherwise, the node runs on an actual machine.
     *
     * @return assigned worker id
     */

    @Override
    public Mono<Long> assignWorkerId() {
        log.info("request assign uid worker node id ");
        // build worker node entity
        return assignWorkerId(workerNodeIdent);
    }


    @Override
    public void releaseWorkerId(long id , long lastTime){
        //do nothing
        log.info("release worker node: {}", id);
    }

}
