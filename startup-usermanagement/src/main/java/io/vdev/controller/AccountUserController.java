package io.vdev.controller;

import io.smallrye.mutiny.Uni;
import io.vdev.model.AccountUser;
import io.vdev.model.LoginRequest;
import io.vdev.service.AccountUserService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.jboss.resteasy.reactive.RestPath;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@Path("/user")
@RequestScoped
public class AccountUserController {
    @Inject
    private AccountUserService accountUserService;

    @Claim(standard = Claims.preferred_username)
    private String jwtUsername;


    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @PermitAll
    @Path("/sign-up")
    public Uni<Response> createAccountUser(AccountUser accountUser) {
        log.info("creating account user for {}", accountUser);
        return accountUserService.createAccountUser(accountUser)
                .map(item -> Response.created(URI.create("/user/" + item.getId())).entity(item).build())
                .onFailure().retry().atMost(2)
                .onFailure().invoke(failure -> {
                    log.error(failure.getLocalizedMessage());
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(failure.getLocalizedMessage()).build();
                });
    }

    @GET
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @RolesAllowed({"COMPANY_ADMIN","FREELANCER"})
    @Path("/{id}")
    public Uni<Response> getAccountUser(@RestPath Long id) {
        log.info("getting user for {}", id);
        log.info("username: " + jwtUsername);
        return accountUserService.getAccountUser(id, jwtUsername)
                .onItem()
                .transform(item -> item == null ?
                        Response.status(Response.Status.NOT_FOUND).entity(Map.of("message", "user account not found")).build() :
                        Response.ok().entity(item).build());
    }

    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @PermitAll
    @Path("/login")
    public Uni<Response> login(LoginRequest loginRequest) {
        log.info("login for username: " + loginRequest.getUsername());
        return accountUserService.loginWithUsernameAndPassword(loginRequest.getUsername(), loginRequest.getPassword())
                .onItem()
                .transform(item -> {
                    if (item != null)
                        return Response.ok().header("Authorization", "Bearer " + item).build();
                    else
                        return Response.status(Response.Status.UNAUTHORIZED).build();
                });
    }

    @PUT
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @RolesAllowed({"COMPANY_ADMIN","FREELANCER"})
    @Path("/update")
    public Uni<Response> updateAccountUser(AccountUser accountUser) {
        log.info("update user account");
        return accountUserService.updateAccountUser(accountUser)
                .map(item -> Response.created(URI.create("/user/" + item.getId())).entity(item).build())
                .onFailure().retry().atMost(2)
                .onFailure().invoke(failure -> {
                    log.error(failure.getLocalizedMessage());
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(failure.getLocalizedMessage()).build();
                });
    }

    @DELETE
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @RolesAllowed({"COMPANY_ADMIN","FREELANCER"})
    @Path("/{id}")
    public Uni<Response> deleteAccountUser(@RestPath Long id) {
        log.info("delete user account with ID: {}", id);
        return accountUserService.deleteUserAccount(id)
                .onItem().transform(status -> status ? Response.ok(Map.of("message", "deleted")).build() :
                        Response.ok(Map.of("message", "not deleted. try again later.")).build());
    }


}
