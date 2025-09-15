package net.whgkswo.excuse_bundle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class ExcuseDictApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExcuseDictApplication.class, args);
	}

}
