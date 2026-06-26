package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Component
public class AvroJsonDeserializer {

    public <T extends SpecificRecordBase> T deserialize(
            String json,
            Class<T> eventClass,
            org.apache.avro.Schema schema) {
        try {
            ByteArrayInputStream inputStream =
                    new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

            Decoder decoder =
                    DecoderFactory.get().jsonDecoder(schema, inputStream);

            SpecificDatumReader<T> reader =
                    new SpecificDatumReader<>(schema);

            return reader.read(null, decoder);

        } catch (Exception e) {
            throw new IllegalArgumentException("Error deserializing Avro event", e);
        }
    }
}