# spring-vault-transit-demo

## Pre-requisite

* Installing Vault
* MySQL 5.7
* Setup

## How to set up

### Running MySQL

```shell
mysql.server start
mysql -uroot
```

Then, update password for root user.

```shell
mysql> ALTER USER 'root'@'localhost' IDENTIFIED BY 'rooooot';
```

Lastly, create a database and a table.

```shell
mysql> create database handson;
mysql> use handson;
mysql> create table users (id varchar(50), username varchar(50), password varchar(200), email varchar(50), address varchar(50), creditcard varchar(200));
```

### Running Vault

```shell
vault server -config=local-config-oss.hcl start
```

If you are not familiar with how to write config, please refer [here](https://github.com/tkaburagi/vault-configs/blob/master/local-config-oss.hcl) as example.

Next, you need to enable `approle` , `transit` and `database`.

```shell
vault login <TOKEN>
vault secrets enable database
vault secrets enable transit
vault auth enable approle
```

Write a config for MySQL to Vault.

```shell
vault write database/config/mysql-handson-db \
  plugin_name=mysql-legacy-database-plugin \
  connection_url="{{username}}:{{password}}@tcp(127.0.0.1:3306)/" \
  allowed_roles="role-demoapp" \
  username="root" \
  password="rooooot"
```

Write a role.

```shell
vault write database/roles/role-demoapp \
  db_name=mysql-handson-db \
  creation_statements="CREATE USER '{{name}}'@'%' IDENTIFIED BY '{{password}}';GRANT SELECT,INSERT,UPDATE ON handson.users TO '{{name}}'@'%';" \
  default_ttl="5h" \
  max_ttl="5h"
```

Test the config and role. 

```shell
vault read database/creds/role-demoapp

Key                Value
---                -----
lease_id           database/creds/role-demoapp/GwOQKPDCIJS1K1Z626RdrQlW
lease_duration     5h
lease_renewable    true
password           A1a-4VU2FVBp5HdIJGvz
username           v-role-FWRN0zpOp
```

This creds is not used by app. This app `pull` the AppRole `Secret ID` and `Role ID` from Vault.

This is a last. Let's create a policy and write a approle. The token generated as a result of AppRole authentication will have this authorizations.

```shell
vault policy write vault-policy policy-vault.hcl
vault write auth/approle/role/vault-approle policies=vault-policy period=1h
```

Please see [here](https://github.com/tkaburagi/vault-configs/blob/master/policies/policy-cf-vault.hcl) as a sample of policy.

## Running App

Finally you are able to run app.

```shell
git clone https://github.com/tkaburagi/spring-vault-transit-demo
cd spring-vault-transit-demo
sed "s|VAULT_TOKEN=|VAULT_TOKEN=<YOUR_TOKEN>|g" set-env-local.sh > my-set-env-local.sh
cat my-set-env-local.sh
source my-set-env-local.sh
mvn clean package -DskipTests
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### Playing Transit

This app has three api.

1. Put the data encrypted by Vault.
2. Get the data decrypted by Vault.
3. Rewrap the data.

Firstly, hit the api to write data to databse. This is like a registration form.

```shell
curl http://localhost:8080/api/v1/encrypt/add-user -d username="Takayuki Kaburagi" -d password="PqssWOrd" -d address="Yokohama" --data-urlencode creditcard="9999-8888-6666-6666" --data-urlencode email="vault@kabuctl.run"
```

Output will be like this and you can ensure password and creditcard was encrypted.

```json
{
	"id": "db0bbb62-fdfd-4e2e-a4db-1e5e32e36761",
	"username": "Takayuki Kaburagi",
	"password": "vault:v1:aRtAJK+ED8ap2vM5f9ba8eL0VvnjD+Akw8ag2eHLYNucXfRx",
	"email": "vault@kabuctl.run",
	"address": "Yokohama",
	"creditcard": "vault:v1:LYpkecFI4bY6c7I8a3fB47d0oHNf6bPL/6VTc14g+zgEVg47EoRjKWTJeYeaisw="
}
```

Let's confirm inside database too.

```shell
mysql> select * from users;
```

You can get encrypted data.

```
+--------------------------------------+-----------------+-----------------------------------------------------------+-------------------+----------+---------------------------------------------------------------------------+
| id                                   | username        | password                                                  | email             | address  | creditcard                                                                |
+--------------------------------------+-----------------+-----------------------------------------------------------+-------------------+----------+---------------------------------------------------------------------------+
| db0bbb62-fdfd-4e2e-a4db-1e5e32e36761 | Takayuki Kaburagi | vault:v1:aRtAJK+ED8ap2vM5f9ba8eL0VvnjD+Akw8ag2eHLYNucXfRx | vault@kabuctl.run | Yokohama | vault:v1:LYpkecFI4bY6c7I8a3fB47d0oHNf6bPL/6VTc14g+zgEVg47EoRjKWTJeYeaisw= |
+--------------------------------------+-----------------+-----------------------------------------------------------+-------------------+----------+---------------------------------------------------------------------------+
```

Next, Let's get the data from MySQL. At first try to get data without using Vault. `non-decrypte` endpoint. This is just select the data from Database.

```shell
curl -G "http://localhost:8080/api/v1/non-decrypt/get-user" -d uuid=db0bbb62-fdfd-4e2e-a4db-1e5e32e36761 | jq
```

Ofcourse, data is not decrypted and this data is not valuable for application.

```json
{
  "id": "db0bbb62-fdfd-4e2e-a4db-1e5e32e36761",
  "username": "Hiroki Kaburagi",
  "password": "vault:v1:aRtAJK+ED8ap2vM5f9ba8eL0VvnjD+Akw8ag2eHLYNucXfRx",
  "email": "h.kaburagi@me.com",
  "address": "Yokohama",
  "creditcard": "vault:v1:LYpkecFI4bY6c7I8a3fB47d0oHNf6bPL/6VTc14g+zgEVg47EoRjKWTJeYeaisw="
}
```

Get the decrypted data.

```shell
curl -G "http://localhost:8080/api/v1/decrypt/get-user" -d uuid=db0bbb62-fdfd-4e2e-a4db-1e5e32e36761 | jq
```

You can see decrypted data by Vault.

```json
{
  "id": "db0bbb62-fdfd-4e2e-a4db-1e5e32e36761",
  "username": "Takayuki Kaburagi",
  "password": "PqssWOrd",
  "email": "t.kaburagi@me.com",
  "address": "Yokohama",
  "creditcard": "9999-8888-6666-6666"
}
```

Lastly, let's rotate the key.

