package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.mapper;

import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Component("AvroJsonSerializerOut")
public class AvroJsonSerializer {

    public String serialize(SpecificRecordBase event) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            DatumWriter<SpecificRecordBase> writer =
                    new SpecificDatumWriter<>(event.getSchema());

            JsonEncoder encoder =
                    EncoderFactory.get().jsonEncoder(event.getSchema(), outputStream);

            writer.write(event, encoder);
            encoder.flush();

            return outputStream.toString(StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new IllegalStateException("Error serializing Avro event", e);
        }
    }
}