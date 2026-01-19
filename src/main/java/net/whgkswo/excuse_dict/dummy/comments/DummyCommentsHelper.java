package net.whgkswo.excuse_dict.dummy.comments;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DummyCommentsHelper {

    @Getter
    private List<String> comments = new ArrayList<>();
    private final Random random = new Random();

    @PostConstruct
    public void loadComments() throws IOException {
        ClassPathResource resource = new ClassPathResource("dummy/comments.txt");
        comments = Files.readAllLines(Paths.get(resource.getURI())).stream()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .filter(line -> !line.startsWith("//") && !line.startsWith("--"))
                .toList()
        ;
    }

    public String getRandomComment() {
        return comments.get(random.nextInt(comments.size()));
    }
}
