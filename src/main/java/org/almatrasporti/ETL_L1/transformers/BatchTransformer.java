package org.almatrasporti.ETL_L1.transformers;

import org.almatrasporti.common.models.CANBusMessage;
import org.almatrasporti.common.services.CANBusMessageFactory;
import org.almatrasporti.common.services.CANBusMessageFormatter;

public class BatchTransformer extends Transformer {

    @Override
    public String getApplicationId() {
        return "ETL_L1_BATCH";
    }

    protected CANBusMessage fix(CANBusMessage message) {
        if (message.getSatellites() < 3) {
            message.setLat(-1.0);
            message.setLon(-1.0);
        }

        if (message.getAltitude() < 0) {
            message.setAltitude(0.0f);
        }

        return message;
    }

    protected CANBusMessage parse(String value) {
        return CANBusMessageFactory.fromCSV(value);
    }


    protected String convert(CANBusMessage message) {

        return CANBusMessageFormatter.toBatchJSON(message);
    }
}
