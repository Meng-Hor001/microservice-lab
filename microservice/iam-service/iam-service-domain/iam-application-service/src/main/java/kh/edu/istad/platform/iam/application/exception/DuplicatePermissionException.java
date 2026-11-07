package kh.edu.istad.platform.iam.application.exception;

public class DuplicatePermissionException extends RuntimeException{
    public DuplicatePermissionException(String name){
        super("Permission already exists" + name);
    }
}
