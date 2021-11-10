package com.kry.Insight.persistence;


import com.kry.Insight.model.Service;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.*;
import lombok.extern.slf4j.Slf4j;
import io.vertx.rxjava3.mysqlclient.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class ServiceRepository {

  private static Function<Row, Service> MAPPER = (row) ->
    Service.of(
      row.getLong("id"),
      row.getString("name"),
      row.getString("url"),
      row.getString("user"),
      row.getString("status"),
      row.getLocalDateTime("created_at")
    );


  private final io.vertx.rxjava3.mysqlclient.MySQLPool client;

  private ServiceRepository(MySQLPool _client) {
    this.client = _client;
  }

  //factory method
  public static ServiceRepository create(MySQLPool client) {
    return new ServiceRepository(client);
  }

  public Flowable<Service> findAll() {
    return this.client
      .preparedQuery("SELECT * FROM services")
      .rxExecute()
      .flattenAsFlowable(
        rows -> StreamSupport.stream(rows.spliterator(), false)
          .map(MAPPER)
          .collect(Collectors.toList())
      );
  }

  public Single<Service> findById(Long id) {
    Objects.requireNonNull(id, "id can not be null");
    return client.preparedQuery("SELECT * FROM services WHERE id=?").rxExecute(Tuple.of(id))
      .map(RowSet::iterator)
      .flatMap(iterator -> iterator.hasNext() ? Single.just(MAPPER.apply(iterator.next())) : Single.error(new ServiceNotFoundException(id)));
  }

  public @NonNull Single<Integer> save(Service data) {
    Date date = new Date();
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
    String currentDateTime = format.format(date);
      return client.preparedQuery("INSERT INTO services(name, url,user,status,created_at) VALUES (?, ?,?,?,?)")
        .rxExecute(Tuple.of(data.getName(), data.getUrl(), data.getUser(),data.getStatus(),currentDateTime)).map(SqlResult::rowCount);

  }

  public Single<Integer> update(Service data) {
    Date date = new Date();
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
    String currentDateTime = format.format(date);
    return client.preparedQuery("UPDATE services SET name=?, url=? ,user=? ,created_at=?,status=? WHERE id=?")
      .rxExecute(Tuple.of(data.getName(), data.getUrl(), data.getUser(),currentDateTime,data.getStatus(), data.getId()))
      .map(SqlResult::rowCount);
  }

  public Single<Integer> deleteById(Long id) {
    Objects.requireNonNull(id, "id can not be null");
    return client.preparedQuery("DELETE FROM services WHERE id=?").rxExecute(Tuple.of(id))
      .map(SqlResult::rowCount);
  }

}
