# Microservizio ETL_L1

Il modulo ETL_L1, realizzato in linguaggio Java, si occupa di effettuare le seguenti operazioni:

- Caricare i dati da topic Kafka
- Effettuare una trasformazione in formato JSON
- Effettuare un'elaborazione sul documento
- Depositare i documenti su altri topic di Kafka. 

Per tale scopo, il microservizio utilizza la stream processing API di Kafka

I dati in input CSV, reperiti dai topic Kafka _VIN000000000000XX_ sono nel seguente formato:
`VinVehicle,Timestamp,Driver,Odometer,LifeConsumption,Position.lon,Position.lat,Position.altitude,Position.heading,Position.speed,Position.satellites`

Es:

`VIN00000000000010,1590971400,Driver10,1000016000,1333001000,12.366820845888245,45.23895419650073,-13.0,20.01,81,4`

Una volta prelevati da Kafka, vengono convertiti secondo i seguenti formati json:

- Topic _batch_:
```
{
	"Timestamp" : 1592526600,
	"VinVehicle" : "VIN00000000000008",
	"Driver" : "Driver08",
	"LifeConsumption" : 1357095000,
	"Odometer" : 1007094000,
	"Position" : {
		"lon" : 12.01734812052307,
		"lat" : 46.40505540215499,
		"altitude" : 0,
		"heading" : 20.01,
		"speed" : 62,
		"satellites" : 4
	}
}
```

- Topic _realtime_:
```
{
	"Timestamp" : 1592526600,
	"VinVehicle" : "VIN00000000000008",
	"Position" : {
		"lon" : 12.01734812052307,
		"lat" : 46.40505540215499,
		"altitude" : 0,
		"heading" : 20.01,
		"speed" : 62,
		"satellites" : 4
	}
}
```


## Configurazione
E' possibile configurare l'ETL_L1 mediante un file di properties, passato contestualmente al lancio del servizio, 
mediante l'opzione java `-Dproperties.file="ETL_L1.properties"`, contenente i seguenti campi:

- **Kafka.servers**: elenco di coppie `host:port` separate da virgola ',', usato nel caso in cui `OuputAdapter` sia `KafkaOutputChannelAdapter`
- **Input.topic.pattern.batch**: espressione regolare contenente il pattern per i topic a cui sottoscrivere il servizio per l'ETL batch.  
- **Input.topic.pattern.realtime**: espressione regolare contenente il pattern per i topic a cui sottoscrivere il servizio per l'ETL realtime.  
- **Output.topic.batch**: nome del topic su cui scrivere i documenti json per il ramo batch  
- **Output.topic.realtime**: nome del topic su cui scrivere i documenti json per il ramo realtime
- **Error.topic.batch**: nome del topic su cui scrivere i documenti json che non hanno superato la validazione per il ramo batch  
- **Error.topic.realtime**: nome del topic su cui scrivere i documenti json che non hanno superato la validazione  per il ramo realtime

## Funzionamento
La classe `ETL_L1` viene istanziata senza richiedere alcun parametro al costruttore

Tramite il metodo `execute`, due stream processors (Interfaccia ITransformerAdapter, vedi sotto) vengono istanziati (rispettivamente uno per il ramo batch ed uno per il ramo realtime), i quali si sottoscrivono ad un gruppo di topic corrispondente all'espressione regolare definita in configurazione, e per ogni messaggio, effettuano la conversione appropriata e la pubblicazione sul topic definito in configurazione. 

```
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
```

## ITransformerAdapter
Interfaccia per realizzare la trasformazione dati, a partire da un insieme di topic di input, verso un topic di output:
```
public interface ITransformerAdapter {

    public String getApplicationId();

    public void transform(Pattern inputTopicPattern, String outputTopic, String errorTopic);
}
```

- `getApplicationId()` - restituisce l'ApplicationID usato da Kafka
- `transform()` - implementa la logica ETL.



## Transformer
Classe astratta per la conversione dei messaggi, implementa l'interfaccia `ITransformerAdapter`.

Implementa il metodo `transform()` che opera le seguenti operazioni:

- Sottoscrizione al/i topic di input
- Acquisizione messaggi da stream
- Parsing del messaggio `abstract`
- Validazione del messaggio
- Correzione di parametri anomali del messaggio `abstract`
- Conversione del messaggio `abstract`
- Pubblicazione del messaggio

Inoltre, i messaggi che vengono ritenuti non validi per errori sul formato vengono scritti sul topic di errore specifico
 `errorTopic`.

## BatchTransformer
Estende la class `Transformer` definendo i metodi:

- `parse()` lettura dati da CSV
- `fix()` invalidazione dei dati di posizione non attendibili (`Position.satellites` < 3) e sogliatura dell'altitudine 
se negativa
- `convert()` conversione del messaggio nel formato JSON per il ramo batch

## RealtimeTransformer
Estende e riutilizza le funzionalità di `BatchTransformer`, ridefinendo il solo metodo:

- `convert()` conversione del messaggio nel formato JSON per il ramo realtime 