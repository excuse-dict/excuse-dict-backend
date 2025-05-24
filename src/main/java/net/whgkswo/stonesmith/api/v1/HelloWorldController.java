package net.whgkswo.stonesmith.api.v1;

import net.whgkswo.stonesmith.responses.Response;
import net.whgkswo.stonesmith.responses.SimpleTextDto;
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
                Response.simpleText("돌장장이 프로젝트 시작합니다~")
        );
    }
}
