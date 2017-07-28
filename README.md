# FinTx[1]

## What's is FinTx?

FinTx is an open source group focus on financial technologies.

## What's is fintx-identifier

fintx-identifier is for generating unique id in high performance and distribution environment. It extends the mongodb's ObjectId that using full MAC address to prevent the duplicated Id. It does not depend on the seeds like snowflake id generator. It can generate both 20 charachers base64 URL safe id(recommend) and 30 characters hex character id. Both id characters are in sequence that not random.

##Limitations
1. ProcessId on os could not bigger then 65535(the default max value in most linux OS).    
2. Only in one bundle of same JVM when using OSGI.    
3. Id requirement could not more then about 16777215 per second per JVM.    
4. Maybe it will generate duplicated id every 69 years.

##Example
1. Get a 20 characters length unique id.    
    String id = UniqueId.get().toBase64String();
2. Parse id to get timestamp, machine identifier ( physical MAC address ), process identifier, counter number.    
    UniqueId uniqueId = UniqueId.UniqueId.fromBase64String(id);    
    long timestamp = uniqueId.getTimestamp();    
    long machineId = uniqueId.getMachineIdentifier();    
    int processId = uniqueId.getProcessIdentifier();    
    long counter = uniqueId.getCounter();    

[1] FinTx https://www.fintx.org/    
[2] Maven https://maven.apache.org/    
