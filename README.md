# FinTx[1]

## What's is FinTx?

FinTx is an open source group focus on financial technologies.

## What's is fintx-identifer

fintx-identifer is for generating unique id in high performance and distribution environment. It extends the mongodb's ObjectId that using full MAC address to prevent the duplicated Id. It does not depend on the seeds like snowflake id generator. It can generate both 20 charachers base64 URL safe id(recommend) and 30 characters hex character id. Both id characters are in sequence that not random.

##Limitations
>ProcessId on os could not bigger then 65535. Only in one bundle of same JVM when using OSGI. Id requirement could not more then about 800 million per second per JVM. Maybe generate duplicated id every 69 years.


[1] FinTx https://www.fintx.org/
[2] Maven https://maven.apache.org/
