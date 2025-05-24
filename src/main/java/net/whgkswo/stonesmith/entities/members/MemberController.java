package net.whgkswo.stonesmith.entities.members;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(MemberController.BASE_PATH)
public class MemberController {
    private MemberService memberService;

    public static final String BASE_PATH = "/api/v1/users";


}
