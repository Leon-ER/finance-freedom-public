package finance.freedom.finance_freedom_backend.config;

import finance.freedom.finance_freedom_backend.interfaces.aws.IAwsSecretsService;
import finance.freedom.finance_freedom_backend.model.aws.JavaMailSenderDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class EmailConfiguration {
    private final IAwsSecretsService awsSecretsService;

    @Bean
    public JavaMailSender javaMailSender(){
        JavaMailSenderDetails javaMailSenderInfo = awsSecretsService.getJavaMailSenderInfo();


        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(javaMailSenderInfo.getSmtpHost());
        mailSender.setPort(javaMailSenderInfo.getSmtpPort());
        mailSender.setUsername(javaMailSenderInfo.getSmtpUsername());
        mailSender.setPassword(javaMailSenderInfo.getSmtpPassword());


        Properties props = mailSender.getJavaMailProperties();

        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
}
