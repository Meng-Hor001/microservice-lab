package kh.edu.istad.platform.iam.domain.exception;

public class DuplicateRoleException extends RuntimeException{
    public DuplicateRoleException(String name){
        super("Role Already Exists :" + name);
    }
}
