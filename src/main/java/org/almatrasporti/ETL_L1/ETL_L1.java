package org.almatrasporti.ETL_L1;

import org.almatrasporti.ETL_L1.transformers.ITransformerAdapter;
import org.almatrasporti.ETL_L1.transformers.BatchTransformer;
import org.almatrasporti.ETL_L1.transformers.RealtimeTransformer;
import org.almatrasporti.common.utils.Config;


import java.util.regex.Pattern;

public class ETL_L1 {

    public void execute() {
        ITransformerAdapter batchTransformer = new BatchTransformer();
        batchTransformer.transform(Pattern.compile(Config.getInstance().get("Input.topic.pattern.batch")),
                Config.getInstance().get("Output.topic.batch"),
                Config.getInstance().get("Error.topic.batch"));

        ITransformerAdapter realtimeTransformer = new RealtimeTransformer();
        realtimeTransformer.transform(Pattern.compile(Config.getInstance().get("Input.topic.pattern.realtime")),
                Config.getInstance().get("Output.topic.realtime"),
                Config.getInstance().get("Error.topic.realtime"));
    }



    public static void main(String args[]) {
        ETL_L1 worker = new ETL_L1();

        worker.execute();
    }
}
