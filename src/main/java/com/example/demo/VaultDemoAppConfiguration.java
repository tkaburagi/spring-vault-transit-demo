package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;
import org.springframework.vault.support.VaultToken;

@Configuration
class VaultDemoAppConfiguration extends AbstractVaultConfiguration {

    @Value("${VAULT_HOST}")
    private String vault_host;

    @Value("${VAULT_TOKEN}")
    private String vault_token;

    @Value("${APPROLE}")
    private String approle;

    @Value("${VAULT_PORT}")
    private int vault_port;

    @Override
    public VaultEndpoint vaultEndpoint() {
        VaultEndpoint endpoint = VaultEndpoint.create(vault_host, vault_port);
        endpoint.setScheme("http");
        return  endpoint;
    }

    @Override
    public ClientAuthentication clientAuthentication() {

        VaultToken initialToken = VaultToken.of(vault_token);
        AppRoleAuthenticationOptions options = AppRoleAuthenticationOptions.builder()
                .appRole(approle)
                .roleId(AppRoleAuthenticationOptions.RoleId.pull(initialToken))
                .secretId(AppRoleAuthenticationOptions.SecretId.pull(initialToken))
                .build();


        return new AppRoleAuthentication(options, restOperations());
    }


}