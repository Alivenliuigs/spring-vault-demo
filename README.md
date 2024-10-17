
###启动 vault
```angular2html
 docker run -p 8200:8200 -e 'VAULT_DEV_ROOT_TOKEN_ID=dev-only-token' vault
```
##生成证书
```angular2html
[root@es-sky-131 tls]# cat test.cnf
[ req ]
req_extensions = v3_req
distinguished_name = req_distinguished_name

[ req_distinguished_name ]

[ v3_req ]
basicConstraints = CA:FALSE
keyUsage = nonRepudiation, digitalSignature, keyEncipherment
subjectAltName = @alt_names

[ alt_names ]
DNS.1= igsl.vault
IP.2 = 192.168.0.42

openssl genrsa -out ca.key 2048
openssl req -x509 -new -nodes -key ca.key -subj "/O=HashiCorp /CN=Vault" -days 34000 -out ca.crt
openssl genrsa -out server.key 2048
openssl req -new -key server.key -config test.cnf -subj "/CN=Vault" -out server.csr
openssl x509 -req -in server.csr -CA /opt/vault/tls/ca.crt -CAkey /opt/vault/tls/ca.key -CAcreateserial -days 36500 -extensions v3_req -extfile test.cnf -out server.crt
```
### 生成jks证书
```angular2html
$ keytool -importcert -keystore keystore.jks -file server.crt -noprompt -storepass changeit -alias <domain>
```
## Secert KV Demo
###在docker进程中执行
```angular2html
http命令行设置
$ export VAULT_TOKEN="00000000-0000-0000-0000-000000000000"
$ export VAULT_ADDR="http://127.0.0.1:8200"

https 命令行设置
cp /opt/vault/tls/ca.* /etc/ssl/certs/
$ export VAULT_ADDR="https://127.0.0.1:8200"
$ VAULT_SSL=/opt/vault/tls

$ vault secrets enable -path=kv kv
$ vault kv put secret/gs-vault-config example.username=demouser example.password=demopassword

    $ vault kv put kv/certfication cert=@server.crt key=@server.key
```
执行代码SecertApplication

## 动态数据库凭据 Database Demo
```angular2html
--
-- Sample schema for testing vault database secrets
--
create schema fakebank;
use fakebank;
create table account(
id decimal(16,0),
name varchar(30),
branch_id decimal(16,0),
customer_id decimal(16,0),
primary key (id));

--
-- MySQL user that will be used by Vault to create other users on demand
--
create user 'fakebank-admin'@'%' identified by 'Sup&rSecre7!';
grant all privileges on fakebank.* to 'fakebank-admin'@'%' with grant option;
grant create user on *.* to 'fakebank-admin' with grant option;

flush privileges;

$ vault secrets enable database
$ vault write database/config/fakebank plugin_name=mysql-legacy-database-plugin connection_url="{{username}}:{{password}}@tcp(192.168.0.40:43306)/fakebank" allowed_roles="*" username="fakebank-admin" password="Sup&rSecre7!"
```
Vault数据库角色设置，授予对fakebank模式的所有表的只读访问权限：
```
$ vault write database/roles/fakebank-accounts-ro db_name=fakebank creation_statements="CREATE USER '{{name}}'@'%' IDENTIFIED BY '{{password}}';GRANT SELECT ON fakebank.* TO '{{name}}'@'%';"
```
创建动态凭据
```angular2html
$ vault read database/creds/fakebank-accounts-ro
```
### Transit 加解密数据
```angular2html
$ vault kv put kv/github github.oauth2.key=foobar
```
