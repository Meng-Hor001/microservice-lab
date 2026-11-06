package kh.edu.istad.platform.iam.application.exception;

public class DuplicationPermissionException extends RuntimeException{
    public DuplicationPermissionException(String name){
        super("Permission already exists" + name);
    }
}
