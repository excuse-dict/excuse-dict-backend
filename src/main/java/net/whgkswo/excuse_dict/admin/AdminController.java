package net.whgkswo.excuse_dict.admin;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_dict.entities.excuses.Excuse;
import net.whgkswo.excuse_dict.entities.excuses.service.ExcuseService;
import net.whgkswo.excuse_dict.general.responses.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AdminController.BASE_URL)
@RequiredArgsConstructor
public class AdminController {

    private final ExcuseService excuseService;

    public static final String BASE_URL = "/admin";

    @PostMapping("/migrate-morphemes")
    public ResponseEntity<Response<?>> migrateMorphemes(HttpServletRequest request){

        String ip = request.getRemoteAddr();
        if (!"127.0.0.1".equals(ip) && !"0:0:0:0:0:0:0:1".equals(ip) && !"::1".equals(ip)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Response.simpleString("Localhost only"));
        }

        excuseService.migrateMorphemes();

        return ResponseEntity.ok().build();
    }
}
