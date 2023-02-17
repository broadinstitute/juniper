package bio.terra.pearl.core;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.BaseEntity;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.Mappers;
import org.jdbi.v3.core.mapper.NoSuchMapperException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.statement.StatementContext;

/**
 *  IN-PROGRESS PROTOTYPE!!!
 *  Design for a JdbiDao for single-table inheritance structures.  enables getting a list of entities where each
 *  row will be mapped to the appropriate subclass.
 *
 *  adapted from https://stackoverflow.com/questions/67289610/jdbi-and-inheritance-conditional-mapping
 *
 *  This has not been used or tested!  This was written to see if it would help with NotificationConfig subclasses,
 *  but then I decided to just keep it simple and have a single class.  This is committed to source so we don't lose
 *  it -- I suspect it will come in handy later.
 */
public abstract class PolymorphicJdbiDao<T extends BaseEntity> extends BaseMutableJdbiDao<T> {
    private static final String DISCRIMINATOR_COLUMN = "dtype";
    protected abstract List<Class<? extends T>> getSubclasses();
    protected BidiMap<String, Class<? extends T>> discriminatorClassMap = new DualHashBidiMap<>();

    @Override
    protected List<String> generateGetFields(Class<T> clazz) {
        Set<String> parentClassFields = new HashSet<>();
        parentClassFields.addAll(super.generateGetFields(clazz));
        for (Class subclass : getSubclasses()) {
            List<String> subclassFields = super.generateGetFields(subclass);
            parentClassFields.addAll(subclassFields);
        }
        return parentClassFields.stream().toList();
    }

    public class PolymorphicRowMapper implements RowMapper<T> {
        @Override
        public T map(ResultSet rs, StatementContext ctx) throws SQLException {
            String type = rs.getString(DISCRIMINATOR_COLUMN);
            return mapTo(rs, ctx, discriminatorClassMap.get(type));
        }
        private <S extends T> S mapTo(
                ResultSet rs,
                StatementContext ctx,
                Class<S> targetClass
        ) throws SQLException {
            return ctx.getConfig().get(Mappers.class)
                    .findFor(targetClass)
                    .orElseThrow(() ->
                            new NoSuchMapperException(String.format("No mapper registered for %s class", targetClass))
                    )
                    .map(rs, ctx);
        }
    }

    public PolymorphicJdbiDao(Jdbi jdbi) {
        super(jdbi);
        for (Class subclass : getSubclasses()) {
            discriminatorClassMap.put(subclass.getName(), subclass);
        }
    }

    protected void initializeRowMapper(Jdbi jdbi) {
        // register a basic bean mapper for each subclass
        getSubclasses().stream().forEach(clazz ->
                jdbi.registerRowMapper(clazz, BeanMapper.of(clazz)));
        // then register the polymorphic mapper for the base class
        jdbi.registerRowMapper(getClazz(), new PolymorphicRowMapper());
    }
}
