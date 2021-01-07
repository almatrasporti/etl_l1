package org.almatrasporti.ETL_L1.transformers;

import org.almatrasporti.common.models.CANBusMessage;
import org.almatrasporti.common.services.CANBusMessageFormatter;

public class RealtimeTransformer extends BatchTransformer {

    @Override
    protected String convert(CANBusMessage message) {
        return CANBusMessageFormatter.toRealtimeJSON(message);
    }

    @Override
    public String getApplicationId() {
        return "ETL_L1_REALTIME";
    }
}
