package uk.gov.justice.digital.pdf.service;

import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.NotImplementedException;
import uk.gov.justice.digital.pdf.interfaces.TemplateRepository;

public class MongoDbRepository implements TemplateRepository {

    @Inject
    public MongoDbRepository(@Named("mongoUri") String mongoUri, @Named("dbName") String dbName) {

        //@TODO: Implement MongoDbRepository that uses the injected Mongo connection URI and dbName above to
        // retrieve templates from a MongoDB database below in get() using the mongodb-driver-rx non blocking reactive Java MongoDB driver
        // Can then switch out ResourceRepository for MongoDbRepository in Configuration, and use ResourceRepository in tests
    }

    @Override
    public String get(String name) {

        throw new NotImplementedException("TODO: Implement MongoDB Template Repository");
    }
}
