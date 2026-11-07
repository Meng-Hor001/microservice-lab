package kh.edu.istad.platform.iam.application.exception;

public class VersionConflictException extends RuntimeException{
    public VersionConflictException(String resource){
        super(resource + "was changed by another operation");
    }
}

