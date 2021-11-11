package io.vdev.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.mutiny.Uni;
import io.vdev.model.AccountUser;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import java.util.Date;

@ApplicationScoped
@Slf4j
public class AccountUserService {


    public Uni<AccountUser> createAccountUser(AccountUser accountUser) {
        accountUser.setCreatedAt(new Date());
        accountUser.setUpdatedAt(new Date());
        accountUser.setPassword(BcryptUtil.bcryptHash(accountUser.getPassword()));
        return  accountUser.persistAndFlush();
    }

    public Uni<AccountUser> getAccountUser(Long id) {
        log.info("account user with ID: {}", id);
        return AccountUser.findById(id);
    }
}
