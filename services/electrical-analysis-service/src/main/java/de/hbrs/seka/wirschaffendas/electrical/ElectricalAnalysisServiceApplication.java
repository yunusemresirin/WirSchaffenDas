package de.hbrs.seka.wirschaffendas.electrical;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ElectricalAnalysisServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElectricalAnalysisServiceApplication.class, args);
    }
}
