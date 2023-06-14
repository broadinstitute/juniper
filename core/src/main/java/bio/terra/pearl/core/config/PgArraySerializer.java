package bio.terra.pearl.core.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.sql.SQLException;
import org.postgresql.jdbc.PgArray;


public class PgArraySerializer extends StdSerializer<PgArray> {
  public PgArraySerializer() {
    this(null);
  }

  public PgArraySerializer(Class<PgArray> t) {
    super(t);
  }
  @Override
  public void serialize(PgArray value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    try {
      Object arrayVal = value.getArray();
      provider.findValueSerializer(arrayVal.getClass()).serialize(arrayVal, gen, provider);
    } catch (SQLException e) {
      throw new RuntimeException("Could not serialize array <" + value + ">", e);
    }
  }

  public static SimpleModule module() {
    SimpleModule module = new SimpleModule();
    module.addSerializer(PgArray.class, new PgArraySerializer());
    return module;
  }
}
