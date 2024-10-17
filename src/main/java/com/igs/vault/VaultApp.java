package com.igs.vault;

import com.igs.vault.secret.CertificatesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.vault.authentication.SessionManager;
import org.springframework.vault.core.VaultSysOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.core.VaultTransitOperations;
import org.springframework.vault.support.VaultMount;

import java.util.Arrays;

/**
 * @projectName: spring-vault-demo
 * @package: com.igs.vault
 * @className: App
 * @author: aliven
 * @description: TODO
 * @date: 2023/5/10 17:01
 * @version: 1.0
 */
@SpringBootApplication
@EnableConfigurationProperties(CertificatesConfiguration.class)
public class VaultApp implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(VaultApp.class);
    @Autowired
    private VaultTemplate vaultTemplate;
    @Autowired
    private SessionManager sessionManager;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPass;

    @Autowired
    private CertificatesConfiguration certificatesConfiguration;

    public static void main(String[] args) {
        String param = "DB";
        SpringApplication.run(VaultApp.class, param);
    }


    public void transit() {
        // Let's encrypt some data using the Transit backend.
        VaultTransitOperations transitOperations = vaultTemplate.opsForTransit();

        String keyName = "foo-key";
        // We need to setup transit first (assuming you didn't set up it yet).
        VaultSysOperations sysOperations = vaultTemplate.opsForSys();

        if (!sysOperations.getMounts().containsKey("transit/")) {

            sysOperations.mount("transit", VaultMount.create("transit"));

            transitOperations.createKey(keyName);
        }

        // Encrypt a plain-text value
        String ciphertext = transitOperations.encrypt(keyName, "Secure message");

        System.out.println("Encrypted value");
        System.out.println("-------------------------------");
        System.out.println(ciphertext);
        System.out.println("-------------------------------");
        System.out.println();



        //rotate new key
        transitOperations.rotate(keyName);
        System.out.println("rotate  transit key"+ keyName);
        System.out.println("-------------------------------");
        System.out.println();

        //rotate new key
        System.out.println("before transit rewrap ciphertext："+ ciphertext);
        System.out.println("-------------------------------");
        String newCiphertext = transitOperations.rewrap(keyName, ciphertext);
        System.out.println("after transit rewrap ciphertext："+ newCiphertext);
        System.out.println("-------------------------------");
        System.out.println();

        // Decrypt
        String plaintext = transitOperations.decrypt(keyName, ciphertext);
        System.out.println("Decrypted old ciphertext value:"+ciphertext);
        System.out.println("-------------------------------");
        System.out.println(plaintext);
        System.out.println("-------------------------------");
        System.out.println();

        // Decrypt new
        String newPlaintext = transitOperations.decrypt(keyName, newCiphertext);
        System.out.println("Decrypted new ciphertext value:"+ newCiphertext);
        System.out.println("-------------------------------");
        System.out.println(newPlaintext);
        System.out.println("-------------------------------");
        System.out.println();
    }

    public void DBVault() throws Exception {
        logger.info("Got Vault Token: " + sessionManager.getSessionToken().getToken());
        logger.info("Got DB User: " + dbUser);
        logger.info("Got DB Pass: " + dbPass);
    }

    private void kv() {

//        logger.info("----------------------------------------");
//        logger.info("Configuration properties");
//        logger.info("   example.username is {}", kvConfiguration.getUsername());
//        logger.info("   example.password is {}", kvConfiguration.getPassword());
//        logger.info("----------------------------------------");
    }

    private void cert() {

        logger.info("----------------------------------------");
        logger.info("Certification Configuration properties");
        logger.info("   cert file is {}", certificatesConfiguration.getCert());
        logger.info("   key file is {}", certificatesConfiguration.getKey());
        logger.info("----------------------------------------");
    }

    @Override
    public void run(String... strings) throws Exception {

        System.out.println("commandLine Args: "+ Arrays.toString(strings));
        String cmd = "";
        if (strings.length > 0) {
            cmd = strings[0];
        }
        if ("DB".equals(cmd)) {
            DBVault();
        }
        if ("KV".equals(cmd)) {
//            kv();
        }
        if ("TR".equals(cmd)) {
            transit();
        }
        if ("CERT".equals(cmd)) {
            cert();
        }

    }
}
