package sk.mvp.user_service.infra.asynch;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {
//    @Bean(name = "kafkaExecutor")
    public Executor kafkaExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("KafkaWorker-");
        executor.initialize();
        return executor;
    }
    @Bean(name = "outboxExecutor")
    public Executor outboxExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Koľko vlákien beží minimálne (stále)
        executor.setCorePoolSize(3);
        // Maximálny počet vlákien pri veľkej záťaži
        executor.setMaxPoolSize(6);
        // Kapacita fronty predtým, než sa začnú vytvárať ďalšie vlákna (nad Core size)
        executor.setQueueCapacity(50);
        // Prefix pre logy - skvelé pre debugging (uvidíš napr. [outbox-1])
        executor.setThreadNamePrefix("outbox-");

        // Senior Tip: Čo sa má stať, ak je fronta aj pool plný?
        // CallerRunsPolicy spôsobí, že task spracuje vlákno, ktoré ho vyvolalo (spomalí to hlavný proces, ale nestratíš dáta)
        //executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }

}
