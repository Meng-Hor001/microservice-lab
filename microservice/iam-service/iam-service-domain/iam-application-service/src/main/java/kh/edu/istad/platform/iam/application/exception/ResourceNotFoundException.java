package kh.edu.istad.platform.iam.application.exception;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String resource,Object identifier){
        super(resource + "not found" + identifier);
    }
}
