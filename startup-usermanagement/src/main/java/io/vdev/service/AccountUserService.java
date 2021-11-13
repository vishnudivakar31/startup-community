package io.vdev.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.mutiny.Uni;
import io.vdev.model.AccountUser;
import io.vdev.util.JWTTokenUtil;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;

@ApplicationScoped
@Slf4j
public class AccountUserService {

    @Inject
    JWTTokenUtil jwtTokenUtil;

    public Uni<AccountUser> createAccountUser(AccountUser accountUser) {
        accountUser.setCreatedAt(new Date());
        accountUser.setUpdatedAt(new Date());
        accountUser.setPassword(BcryptUtil.bcryptHash(accountUser.getPassword()));
        return  accountUser.persistAndFlush();
    }

    public Uni<AccountUser> getAccountUser(Long id, String username) {
        log.info("account user with ID: {}", id);
        return AccountUser.<AccountUser>findById(id)
                .onItem()
                .transform(item -> (item != null ? (item.getUsername().equals(username) ? item : null) : null));
    }

    public Uni<String> loginWithUsernameAndPassword(String username, String password) {
        return AccountUser.<AccountUser>find("username", username).firstResult()
                .onItem().transform(accountUser -> {
                    if (accountUser != null && BcryptUtil.matches(password, accountUser.getPassword())) {
                        try {
                            return jwtTokenUtil.generateToken(accountUser.getUsername(), accountUser.getEmail(), accountUser.getAccountUserType().toString());
                        } catch (IOException e) {
                            log.error(e.getMessage());
                            return null;
                        } catch (GeneralSecurityException e) {
                            log.error(e.getMessage());
                            return null;
                        }
                    }
                    return null;
                });
    }

    public Uni<AccountUser> updateAccountUser(AccountUser accountUser) {
        accountUser.setUpdatedAt(new Date());
        return accountUser.persistAndFlush();
    }

    public Uni<Boolean> deleteUserAccount(Long id) {
        return AccountUser.<AccountUser>findById(id)
                .onItem().transform(accountUser -> {
                    if (accountUser != null) {
                        accountUser.delete();
                        accountUser.flush();
                        return true;
                    }
                    return false;
                });
    }
}
