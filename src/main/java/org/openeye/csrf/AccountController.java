package org.openeye.csrf;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

@Controller
public class AccountController {

    private String email = "yassineachkhity56@gmail.com";

    @GetMapping("/")
    public String home(Model model, Principal principal)
    {
        model.addAttribute("username", principal.getName());
        model.addAttribute("email", email);

        return "home";

    }

    @PostMapping("/account/change-email")
    @ResponseBody
    public String changeEmail(
            @RequestParam String email,
            Principal principal
    ) {

        this.email = email;

        return """
                Email changed successfully!

                User: %s
                New email: %s
                """.formatted(
                principal.getName(),
                email
        );
    }
}

