package com.kry.Insight.persistence;

import com.kry.Insight.model.User;
import io.vertx.rxjava3.mysqlclient.MySQLPool;
import io.vertx.rxjava3.pgclient.PgPool;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class StructureInitializer {

    private MySQLPool client;

    public StructureInitializer(MySQLPool client) {
        this.client = client;
    }

    public static StructureInitializer create(MySQLPool client) {
        return new StructureInitializer(client);
    }

    public void run() {
      var userRepository = UserRepository.create(client);
        log.info("Structure initialization is starting...");
      client
        .preparedQuery(
          "create table if not exists service_status\n" +
            "(\n" +
            "\tid int auto_increment,\n" +
            "\turl text null,\n" +
            "\tname text null,\n" +
            "\tserviceId int null,\n" +
            "\tuser text null,\n" +
            "\tstatus text null,\n" +
            "\tcreated_at datetime not null,\n" +
            "\tconstraint service_status_pk\n" +
            "\t\tprimary key (id)\n" +
            ");\n"
        ).rxExecute().subscribe();


      client
            .preparedQuery(
              "create table if not exists services\n" +
                "(\n" +
                "\tid int auto_increment,\n" +
                "\tuser text null,\n" +
                "\tname text null,\n" +
                "\turl text null,\n" +
                "\tstatus text null,\n" +
                "\tcreated_at datetime not null,\n" +
                "\t\tconstraint UNIQUE (user(20), url(20)),\n"+
                "\tconstraint services_pk\n" +
                "\t\tprimary key (id)\n" +
                ");\n"
            ).rxExecute().subscribe();
      client
        .preparedQuery(
          "create table if not exists user\n" +
            "(\n" +
            "\tid int auto_increment,\n" +
            "\temail text null,\n" +
            "\tpassword text null,\n" +
            "\tcreated_at datetime not null,\n" +
            "\tconstraint user_pk\n" +
            "\t\tprimary key (id)\n" +
            ");\n"
        ).rxExecute().subscribe(rows -> {
        userRepository.userCount().subscribe(count -> {

          if(count==0){
            userRepository.save(User.builder().email("khashayar@gmail.com").password("1234").build()).subscribe();
            userRepository.save(User.builder().email("admin@kry.com").password("1234").build()).subscribe();
            userRepository.save(User.builder().email("khashayar@kry.com").password("1234").build()).subscribe();
          }
        });
      });
    }
}
