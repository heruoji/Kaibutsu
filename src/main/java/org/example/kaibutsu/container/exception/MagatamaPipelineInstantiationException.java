package org.example.kaibutsu.container.exception;

public class MagatamaPipelineInstantiationException extends ContainerException{

    String magatamaPipelineName;

    public MagatamaPipelineInstantiationException(String magatamaPipelineName, String message, Throwable throwable) {
        super(message, throwable);
        this.magatamaPipelineName = magatamaPipelineName;
    }
}
