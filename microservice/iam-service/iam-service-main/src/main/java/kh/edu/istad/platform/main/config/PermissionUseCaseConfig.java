package kh.edu.istad.platform.main.config;


import kh.edu.istad.platform.iam.application.port.out.*;
import kh.edu.istad.platform.iam.application.usecase.*;
import kh.edu.istad.platform.iam.application.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.domain.service.AuthorityCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class PermissionUseCaseConfig {
    @Bean
    public Clock clock(){
        return Clock.systemUTC();
    }

    @Bean
    public CreatePermissionUseCase createPermissionUseCase(
            PermissionRepository repository,
            CurrentActorProvider actorProvider,
            Clock clock
    ){
        return new CreatePermissionUseCase(
                repository,actorProvider,clock
        );
    }

    @Bean
    public ActivePermissionUseCase activePermissionUseCase(
            PermissionRepository repository,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        return new ActivePermissionUseCase(
                repository, actorProvider, clock
        );
    }

    @Bean
    public DeactivatePermissionUseCase deactivatePermissionUseCase(
            PermissionRepository repository,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        return new DeactivatePermissionUseCase(
                repository, actorProvider, clock
        );
    }

    @Bean
    public DeletePermissionUseCase deletePermissionUseCase(
            PermissionRepository repository,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        return new DeletePermissionUseCase(
                repository, actorProvider, clock
        );
    }

    @Bean
    public GetPermissionUseCase getPermissionUseCase(
            PermissionRepository repository
    ) {
        return new GetPermissionUseCase(repository);
    }

    @Bean
    public ListPermissionUseCase listPermissionUseCase(
            PermissionRepository repository
    ) {
        return new ListPermissionUseCase(repository);
    }

    @Bean
    public UpdatePermissionUseCase updatePermissionUseCase(
            PermissionRepository repository,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        return new UpdatePermissionUseCase(repository, actorProvider, clock);
    }

    @Bean
    public UpdateUserProfileUseCase updateUserProfileUseCase(
            UserRepository users, CurrentActorProvider actorProvider, Clock clock
    ) {
        return new UpdateUserProfileUseCase(users, actorProvider, clock);
    }

    @Bean
    public CreateRoleUseCase createRoleUseCase(
            AssignmentPermissionsToRoleUseCase permissionsAssignments,
            RoleRepository roleRepository,
            TransactionRunner transaction,
            CurrentActorProvider actorProvider,
            Clock clock
    ){
        return new CreateRoleUseCase(permissionsAssignments,roleRepository, transaction, actorProvider, clock);
    }

    @Bean
    public ListRolesUseCase listRolesUseCase(RoleRepository roleRepository){
        return new ListRolesUseCase(roleRepository);
    }

    @Bean
     public UpdateRoleUseCase updateRoleUseCase(
             RoleRepository roleRepository,
             CurrentActorProvider actorProvider,
             Clock clock
    ){
        return new UpdateRoleUseCase(roleRepository, actorProvider, clock);
    }

    @Bean
    public RevokePermissionFromRoleUseCase revokePermissionFromRoleUseCase(
            RoleRepository roles,
            PermissionRepository permissions,
            RolePermissionAssignmentRepository assignments,
            CurrentActorProvider actorProvider,
            Clock clock
    )
    {
        return new RevokePermissionFromRoleUseCase(
                roles,
                permissions,
                assignments,
                actorProvider,
                clock
        );
    }

    @Bean
    public AssignmentPermissionsToRoleUseCase assignPermissionsToRoleUseCase(
            RoleRepository roles,
            PermissionRepository permissions,
            RolePermissionAssignmentRepository assignments,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        return new AssignmentPermissionsToRoleUseCase(
                roles, permissions, assignments, actorProvider, clock
        );
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(
            UserRepository users,
            PasswordHasher passwordHasher,
            CurrentActorProvider actorProvider,
            Clock clock
    ){
        return new RegisterUserUseCase(
                users,
                passwordHasher,
                actorProvider,
                clock
        ) ;
    }

    @Bean
    public ListUserUseCase listUserUseCase(UserRepository users){
        return new ListUserUseCase( users);
    }

    @Bean
    public GrantPermissionToUserUseCase grantPermissionToUserUseCase(
            UserRepository users,
            PermissionRepository permissions,
            UserPermissionAssignmentRepository assignments,
            CurrentActorProvider actorProvider,
            Clock clock
    ){
        return new GrantPermissionToUserUseCase(users, permissions, assignments, actorProvider, clock);
    }

    @Bean
    public AuthorityCalculator authorityCalculator(){
        return new AuthorityCalculator();
    }

    @Bean
    public GetUserAuthoritiesUseCase getUserAuthoritiesUseCase(
            UserRepository users,
            RoleRepository roles,
            PermissionRepository permissions,
            UserRoleAssignmentRepository userRoles,
            RolePermissionAssignmentRepository rolePermissions,
            UserPermissionAssignmentRepository userPermissions,
            AuthorityCalculator calculator,
            Clock clock
    ) {
        return new GetUserAuthoritiesUseCase(
                users,
                roles,
                permissions,
                userRoles,
                rolePermissions,
                userPermissions,
                calculator,
                clock
        );
    }

    @Bean
    public LoadUserForAuthenticationUseCase loadUserForAuthenticationUseCase(
            UserRepository users
    ) {
        return new LoadUserForAuthenticationUseCase(users);
    }

    @Bean
    public RecordLoginAttemptUseCase recordLoginAttemptUseCase(
            UserRepository users,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        return new RecordLoginAttemptUseCase(users, actorProvider, clock);
    }

    @Bean
    public LinkIdentityUseCase linkIdentityUseCase(
            UserRepository users,
            IdentityRepository identities,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        return new LinkIdentityUseCase(
                users, identities, actorProvider, clock
        );
    }

    @Bean
    public FindOrProvisionFederatedUserUseCase findOrProvisionFederatedUserUseCase(
            UserRepository users,
            TransactionRunner transaction,
            IdentityRepository identities,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        return new FindOrProvisionFederatedUserUseCase(
                users, identities,transaction , actorProvider, clock
        );
    }

    @Bean
    public UnlinkIdentityUseCase unlinkIdentityUseCase(
            UserRepository users,
            IdentityRepository identities,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        return new UnlinkIdentityUseCase(
                users, identities, actorProvider, clock
        );
    }


    @Bean
    public ListUserIdentitiesUseCase listUserIdentitiesUseCase(
            IdentityRepository identities
    ) {
        return new ListUserIdentitiesUseCase(identities);
    }
}
