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

import io.github.cooperlyt.cloud.uid.utils.DockerUtils;
import io.github.cooperlyt.cloud.uid.utils.NetUtils;
import io.github.cooperlyt.cloud.uid.utils.ValuedEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.RandomUtils;
import reactor.core.publisher.Mono;

/**
 * Represents an implementation of {@link WorkerIdAssigner},
 * the worker id will be discarded after assigned to the UidGenerator
 *
 * @author yutianbao
 */

@Slf4j
public abstract class HostWorkerIdAssigner implements WorkerIdAssigner {

    public enum WorkerNodeType implements ValuedEnum<Integer> {

        /**
         * 容器
         */
        CONTAINER(1),
        /**
         * 物理机
         */
        ACTUAL(2);

        /**
         * Lock type
         */
        private final Integer type;

        /**
         * Constructor with field of type
         */
        WorkerNodeType(Integer type) {
            this.type = type;
        }

        @Override
        public Integer value() {
            return type;
        }

    }

    protected abstract Mono<Long> assignWorkerId(WorkerNodeType type,String host, String port);
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
        if (DockerUtils.isDocker()){
            return assignWorkerId(WorkerNodeType.CONTAINER,DockerUtils.getDockerHost(),DockerUtils.getDockerPort());
        }
        return assignWorkerId(WorkerNodeType.ACTUAL,NetUtils.getLocalAddress(),fakePort());
    }


    @Override
    public void releaseWorkerId(long id , long lastTime){
        //do nothing
        log.info("release worker node: {}", id);
    }

    private String fakePort() {
        return System.currentTimeMillis() + "-" + RandomUtils.nextInt(100000);
    }

}
