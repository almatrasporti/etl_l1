package org.almatrasporti.ETL_L1.transformers;

import java.util.regex.Pattern;

public interface ITransformerAdapter {

    public String getApplicationId();

    public void transform(Pattern inputTopicPattern, String outputTopic, String errorTopic);
}
