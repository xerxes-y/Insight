package com.kry.Insight.persistence;


import com.kry.Insight.model.Service;
import com.kry.Insight.model.User;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.mysqlclient.MySQLClient;
import io.vertx.rxjava3.mysqlclient.MySQLPool;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.RowSet;
import io.vertx.rxjava3.sqlclient.SqlResult;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class UserRepository {

  private static Function<Row, User> MAPPER = (row) ->
    User.of(
      row.getLong("id"),
      row.getString("email"),
      row.getString("password"),
      row.getLocalDateTime("created_at")
    );


  private final MySQLPool client;

  private UserRepository(MySQLPool _client) {
    this.client = _client;
  }

  //factory method
  public static UserRepository create(MySQLPool client) {
    return new UserRepository(client);
  }


  public Single<Integer> userCount() {
    return client.preparedQuery("SELECT COUNT(*) FROM user").rxExecute()
      .map(RowSet::iterator)
      .flatMap(iterator -> iterator.hasNext() ? Single.just(iterator.next().getInteger(0)) : Single.error(new UserNotFoundException("email")));
  }

  public Single<User> findByEmail(String email) {
    Objects.requireNonNull(email, "email can not be null");
    return client.preparedQuery("SELECT * FROM user WHERE email=?").rxExecute(Tuple.of(email))
      .map(RowSet::iterator)
      .flatMap(iterator -> iterator.hasNext() ? Single.just(MAPPER.apply(iterator.next())) : Single.error(new UserNotFoundException(email)));
  }
  public Single<User> findByEmailAndPassword(String email,String password) {
    Objects.requireNonNull(email, "email can not be null");
    return client.preparedQuery("SELECT * FROM user WHERE email=? and password=?").rxExecute(Tuple.of(email,password))
      .map(RowSet::iterator)
      .flatMap(iterator -> iterator.hasNext() ? Single.just(MAPPER.apply(iterator.next())) : Single.error(new UserNotFoundException(email)));
  }
  public @NonNull Single<Long> save(User data) {
    Date date = new Date();
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
    String currentDateTime = format.format(date);
    return client.preparedQuery("INSERT INTO user(email, password,created_at) VALUES (?, ?,?)")
      .rxExecute(Tuple.of(data.getEmail(), data.getPassword(), currentDateTime))
      .map(res -> Long.parseLong(res.value().property(MySQLClient.LAST_INSERTED_ID).toString()));
  }


}
