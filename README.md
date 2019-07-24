# spring-vault-transit-demo

```console
$ git clone https://github.com/tkaburagi/spring-vault-transit-demo
$ cd spring-vault-transit-demo
$ sed "s|VAULT_TOKEN=|VAULT_TOKEN=<YOUR_TOKEN>|g" set-env-local.sh > my-set-env-local.sh
$ cat my-set-env-local.sh
$ source my-set-env-local.sh
$ mvn clean package -DskipTests
$ java -jar target/demo-0.0.1-SNAPSHOT.jar
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.1.4.RELEASE)

2019-07-15 21:50:19.739  INFO 7226 --- [           main] com.example.demo.VaultDemoApplication    : Starting VaultDemoApplication v0.0.1-SNAPSHOT on Takayukis-MacBook-Pro.local with PID 7226 (/Users/kabu/hashicorp/intellij/springboot-vault-transit/target/demo-0.0.1-SNAPSHOT.jar started by kabu in /Users/kabu/hashicorp/intellij/springboot-vault-transit)
2019-07-15 21:50:19.741  INFO 7226 --- [           main] com.example.demo.VaultDemoApplication    : No active profile set, falling back to default profiles: default
```