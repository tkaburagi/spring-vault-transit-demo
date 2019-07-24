package com.example.demo;

import com.example.demo.Repository.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.Ciphertext;
import org.springframework.vault.support.Plaintext;

public class VaultTransitConverter {

    VaultOperations vaultOps = BeanUtil.getBean(VaultOperations.class);

    @Autowired
    UserJpaRepository userJpaRepository;

    public String encryptData(String ptext) {
        Plaintext plaintext = Plaintext.of(ptext);
        String cipherText = vaultOps.opsForTransit().encrypt("springdemo", plaintext).getCiphertext();
        return cipherText;
    }

    public String decryptData(String ctext) {
        Ciphertext ciphertext = Ciphertext.of(ctext);
        String plaintext = vaultOps.opsForTransit().decrypt("springdemo", ciphertext).asString();
        return plaintext;
    }

    public String rewrapData(String ctext) {
        Ciphertext cipherext = Ciphertext.of(ctext);
        String rewrappedtext = vaultOps.opsForTransit().rewrap("springdemo", cipherext.getCiphertext());
        return rewrappedtext;
    }

}
