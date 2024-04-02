package org.example.kaibutsu.container.exception;

public class MagatamaPipelineNotFoundException extends ContainerException {
    String pipelineName;

    public MagatamaPipelineNotFoundException(String pipelineName, String message) {
        super(message);
        this.pipelineName = pipelineName;
    }
}
