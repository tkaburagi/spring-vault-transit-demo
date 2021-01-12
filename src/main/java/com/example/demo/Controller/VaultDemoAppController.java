package com.example.demo.Controller;

import com.example.demo.Entity.User;
import com.example.demo.Repository.UserJpaRepository;
import com.example.demo.Entity.Secrets;
import com.example.demo.VaultTransitUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class VaultDemoAppController {

    @Autowired
    VaultTemplate vaultTemplate;

    private final ObjectMapper mapper = new ObjectMapper();
    private User u = new User();
    private final UserJpaRepository userJpaRepository;

    public VaultDemoAppController(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @RequestMapping(value = "/api/v1/kv/put", method = RequestMethod.POST)
    String putGenericData(@RequestParam String username, String password, String path){
        Secrets secrets = new Secrets();
        secrets.setUsername(username);
        secrets.setPassword(password);
        vaultTemplate.write(path, secrets);
        return "put to kv";
    }

    @RequestMapping(value = "/api/v1/kv/get", method = RequestMethod.GET)
    String getGenericData(@RequestParam String path) throws Exception {
        VaultResponseSupport<Secrets> vaultResponse = vaultTemplate.read(path, Secrets.class);
        String json = mapper.writeValueAsString(vaultResponse.getData());
        return json;
    }

    @RequestMapping(value = "/api/v1/transit/encrypt", method = RequestMethod.GET)
    String transitEncrypt(@RequestParam String ptext){
        return new VaultTransitUtil().encryptData(ptext);
    }

    @RequestMapping(value = "/api/v1/transit/decrypt", method = RequestMethod.GET)
    String transitDecrypt(@RequestParam String ctext){
        return new VaultTransitUtil().decryptData(ctext);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/v1/get-all-users")
    public Object getAllUsers() {
        return userJpaRepository.findAll();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/api/v1/plain/add-user")
    public Object addOneUser(@RequestParam String username, String password, String email, String address, String creditcard)  {
        User u = new User();

        u.setId(UUID.randomUUID().toString());
        u.setUsername(username);
        u.setPassword(password);
        u.setEmail(email);
        u.setAddress(address);
        u.setCreditcard(creditcard);
        return userJpaRepository.save(u);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/api/v1/encrypt/add-user")
    public Object addOneEncryptedUser(@RequestParam String username, String password, String email, String address, String creditcard)  {
        VaultTransitUtil vaultTransitUtil = new VaultTransitUtil();

        u.setId(UUID.randomUUID().toString());
        u.setUsername(username);
        u.setPassword(vaultTransitUtil.encryptData(password));
        u.setEmail(email);
        u.setAddress(address);
        u.setCreditcard(vaultTransitUtil.encryptData(creditcard));

        System.out.println(u.getPassword());

        return userJpaRepository.save(u);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/v1/non-decrypt/get-user")
    public Object getOneUser (@RequestParam String uuid) throws Exception {
        User u = userJpaRepository.getOne(uuid);
        String json = mapper.writeValueAsString(u);
        return json;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/v1/decrypt/get-user")
    public Object getOneDecryptedUser (@RequestParam String uuid) {
        VaultTransitUtil vaultTransitUtil = new VaultTransitUtil();
        u = userJpaRepository.getOne(uuid);
        u.setPassword(vaultTransitUtil.decryptData(u.getPassword()));
        u.setCreditcard(vaultTransitUtil.decryptData(u.getCreditcard()));
        return u;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/v1/rewrap")
    public Object rewrapOneDecryptedUser (@RequestParam String uuid) {
        VaultTransitUtil vaultTransitUtil = new VaultTransitUtil();
        u = userJpaRepository.getOne(uuid);
        u.setId(u.getId());
        u.setPassword(vaultTransitUtil.rewrapData(u.getPassword()));
        u.setCreditcard(vaultTransitUtil.rewrapData(u.getCreditcard()));
        userJpaRepository.save(u);
        return u;
    }

    @RequestMapping(method = RequestMethod.GET, value = "api/v1/get-keys")
    public Object getKeys() {
        VaultTransitUtil vaultTransitUtil = new VaultTransitUtil();
        return vaultTransitUtil.getKeyStatus();
    }


}
