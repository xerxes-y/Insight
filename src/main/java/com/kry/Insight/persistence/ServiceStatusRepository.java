package com.kry.Insight.persistence;


import com.kry.Insight.model.Service;
import com.kry.Insight.model.ServiceStatus;
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
public class ServiceStatusRepository {

  private static Function<Row, ServiceStatus> MAPPER = (row) ->
    ServiceStatus.of(
      row.getLong("id"),
      row.getString("url"),
      row.getLong("serviceId"),
      row.getString("name"),
      row.getString("user"),
      row.getString("status"),
      row.getLocalDateTime("created_at")
    );


  private final MySQLPool client;

  private ServiceStatusRepository(MySQLPool _client) {
    this.client = _client;
  }

  //factory method
  public static ServiceStatusRepository create(MySQLPool client) {
    return new ServiceStatusRepository(client);
  }


  public Flowable<ServiceStatus> findServicesByUserId(String  user) {
    Objects.requireNonNull(user, "user can not be null");
    return client.preparedQuery(  "WITH ranked_service AS (\n" +
      "    SELECT m.*, ROW_NUMBER() OVER (PARTITION BY serviceId ORDER BY id DESC) AS rn\n" +
      "    FROM service_status AS m\n" +
      ")\n" +
      "SELECT * FROM ranked_service WHERE rn = 1 and user=?").rxExecute(Tuple.of(user))
      .flattenAsFlowable(
        rows -> StreamSupport.stream(rows.spliterator(), false)
          .map(MAPPER)
          .collect(Collectors.toList()));
  }

  public @NonNull Single<Long> save(ServiceStatus data) {
    Date date = new Date();
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
    String currentDateTime = format.format(date);
    return client.preparedQuery("INSERT INTO service_status (url,serviceId,name, status,user,created_at) VALUES (?,?, ?,?,?,?)")
      .rxExecute(Tuple.of(data.getUrl(),data.getServiceId(),data.getName(), data.getStatus(),data.getUser(),currentDateTime))
      .map(res -> Long.parseLong(res.value().property(MySQLClient.LAST_INSERTED_ID).toString()));
  }

  public Single<Integer> saveAll(List<ServiceStatus> data) {
    Date date = new Date();
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
    String currentDateTime = format.format(date);
    var tuples = data.stream()
      .map(d -> Tuple.of(d.getUrl(),d.getServiceId(),d.getName()
        , d.getStatus(), d.getUser(),currentDateTime))
      .collect(Collectors.toList());
    return client.preparedQuery("INSERT INTO service_status (url,serviceId,name, user,status,created_at) VALUES (?, ?,?,?,?)")
      .rxExecuteBatch(tuples)
      .map(SqlResult::rowCount);
  }
  public Single<Integer> deleteByServiceId(Long id) {
    Objects.requireNonNull(id, "id can not be null");
    return client.preparedQuery("DELETE FROM service_status WHERE serviceId=?").rxExecute(Tuple.of(id))
      .map(SqlResult::rowCount);
  }

}
