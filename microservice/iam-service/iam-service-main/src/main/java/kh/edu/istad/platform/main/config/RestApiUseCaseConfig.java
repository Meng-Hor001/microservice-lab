package kh.edu.istad.platform.main.config;

import kh.edu.istad.platform.iam.application.port.out.*;
import kh.edu.istad.platform.iam.application.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;


@Configuration(proxyBeanMethods = false)
public class RestApiUseCaseConfig {
    @Bean
    public CreateUserUseCase createUserUseCase(
            UserRepository users,
            PasswordHasher hasher,
            AssignRolesToUserUseCase assignRolesToUserUseCase,
            TransactionRunner transactionRunner,
            CurrentActorProvider actor,
            Clock clock
    ) {
        return new CreateUserUseCase(users, hasher, assignRolesToUserUseCase, transactionRunner, actor, clock);
    }

    @Bean
    public GetUserUseCase getUserUseCase(UserRepository users) {
        return new GetUserUseCase(users);
    }

    @Bean
    public EnableUserUseCase enableUserUseCase(
            UserRepository users,
            CurrentActorProvider actor,
            Clock clock
    ) {
        return new EnableUserUseCase(users, actor, clock);
    }

    @Bean
    public DisableUserUseCase disableUserUseCase(
            UserRepository users,
            CurrentActorProvider actor,
            Clock clock
    ) {
        return new DisableUserUseCase(users, actor, clock);
    }

    @Bean
    public LockUserUseCase lockUserUseCase(
            UserRepository users,
            CurrentActorProvider actor,
            Clock clock
    ) {
        return new LockUserUseCase(users, actor, clock);
    }

    @Bean
    public UnlockUserUseCase unlockUserUseCase(
            UserRepository users,
            CurrentActorProvider actor,
            Clock clock
    ) {
        return new UnlockUserUseCase(users, actor, clock);
    }

    @Bean
    public DeleteUseUseCase deleteUserUseCase(
            UserRepository users,
            CurrentActorProvider actor,
            Clock clock
    ) {
        return new DeleteUseUseCase(users, actor, clock);
    }

    @Bean
    public ChangePasswordUseCase changePasswordUseCase(
            UserRepository users,
            PasswordHasher hasher,
            CurrentActorProvider actor,
            Clock clock
    ) {
        return new ChangePasswordUseCase(users, hasher, actor, clock);
    }

    @Bean
    public ResetUserPasswordUseCase resetUserPasswordUseCase(
            UserRepository users,
            PasswordHasher hasher,
            CurrentActorProvider actor,
            Clock clock
    ) {
        return new ResetUserPasswordUseCase(users, hasher, actor, clock);
    }

    @Bean
    public AssignRolesToUserUseCase assignRolesToUserUseCase(
            UserRepository users,
            RoleRepository roles,
            UserRoleAssignmentRepository assignments,
            CurrentActorProvider actor,
            Clock clock
    ) {
        return new AssignRolesToUserUseCase(
                users, roles, assignments, actor, clock
        );
    }

    @Bean
    public RevokedRoleFromUserUseCase revokeRoleFromUserUseCase(
            UserRoleAssignmentRepository assignments,
            CurrentActorProvider actor,
            Clock clock
    ) {
        return new RevokedRoleFromUserUseCase(assignments, actor, clock);
    }

    @Bean
    public RevokePermissionFromUserUseCase revokePermissionFromUserUseCase(
            UserPermissionAssignmentRepository assignments,
            CurrentActorProvider actor,
            Clock clock
    ) {
        return new RevokePermissionFromUserUseCase(
                assignments, actor, clock
        );
    }

    @Bean
    public GetRoleUseCase getRoleUseCase(RoleRepository roles) {
        return new GetRoleUseCase(roles);
    }

    @Bean
    public ActiveRoleUseCase activeRoleUseCase(
            RoleRepository roles,
            CurrentActorProvider actor,
            Clock clock
    ) {
        return new ActiveRoleUseCase(roles, actor, clock);
    }

    @Bean
    public DeactivateRoleUseCase deactivateRoleUseCase(
            RoleRepository roles,
            CurrentActorProvider actor,
            Clock clock
    ) {
        return new DeactivateRoleUseCase(roles, actor, clock);
    }

    @Bean
    public DeleteRoleUseCase deleteRoleUseCase(
            RoleRepository roles,
            CurrentActorProvider actor,
            Clock clock
    ) {
        return new DeleteRoleUseCase(roles, actor, clock);
    }
}
