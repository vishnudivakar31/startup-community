package io.vdev.controller;

import io.smallrye.mutiny.Uni;
import io.vdev.model.AccountUser;
import io.vdev.service.AccountUserService;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.RestPath;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@Path("/user")
public class AccountUserController {
    @Inject
    private AccountUserService accountUserService;

    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Path("/sign-up")
    public Uni<Response> createAccountUser(AccountUser accountUser) {
        log.info("creating account user for {}", accountUser);
        return accountUserService.createAccountUser(accountUser)
                .map(item -> Response.created(URI.create("/user/" + item.getId())).entity(Map.of("message", "welcome to startup-community")).build())
                .onFailure().retry().atMost(2)
                .onFailure().invoke(failure -> {
                    log.error(failure.getLocalizedMessage());
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(failure.getLocalizedMessage()).build();
                });
    }

    @GET
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Path("/{id}")
    public Uni<Response> getAccountUser(@RestPath Long id) {
        log.info("getting user for {}", id);
        return accountUserService.getAccountUser(id)
                .onItem()
                .transform(item -> item == null ?
                        Response.status(Response.Status.NOT_FOUND).entity(Map.of("message", "user account not found")).build() :
                        Response.ok().entity(item).build());
    }
}
