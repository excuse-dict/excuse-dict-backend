package net.whgkswo.stonesmith;

import net.whgkswo.stonesmith.responses.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HelloWorldController {

    @GetMapping("")
    public ResponseEntity<?> handleHelloWorldRequest(){
        return ResponseEntity.ok(
                Response.simpleString("돌장장이 프로젝트 시작합니다~")
        );
    }
}
