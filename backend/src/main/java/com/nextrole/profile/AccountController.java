package com.nextrole.profile;
import com.nextrole.auth.CurrentUser;
import com.nextrole.common.ApiProblem;
import com.nextrole.data.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/account")
public class AccountController {
    private final UserAccountRepository users;
    public AccountController(UserAccountRepository users){this.users=users;}
    @DeleteMapping @ResponseStatus(HttpStatus.NO_CONTENT) @Transactional
    public void delete(){
        var u=users.findById(CurrentUser.id()).orElseThrow(()->ApiProblem.notFound("Usuario"));
        users.delete(u);
    }
}
