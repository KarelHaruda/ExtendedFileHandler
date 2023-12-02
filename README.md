# ExtendedFileHandler
Asynchronous variant of JAVA logging FileHandler.
Queues logging requests and does not slow down threads that need to write something to the log. Another thread in parallel then removes from the queue and writes to disk. In this way, it can cover the sudden large number of logging requests and spread it over time when the system is less busy.

Eclipse JAVA project

## TOMCAT Configuration
Configuration is same as FileHandler configuration
In Tomcat conf/logging.properties add handler eu.abra.logging.ExtendedFileHandler to end of section handlers=

Be sure that jar is on the Tomcat`s JAVA classpath 

![image](https://github.com/KarelHaruda/ExtendedFileHandler/assets/19901055/06929b21-955e-40bb-ba57-223b443d7d3d)

<code>
eu.abra.logging.ExtendedFileHandler.level = FINEST
eu.abra.logging.ExtendedFileHandler.encoding = UTF-8
eu.abra.logging.ExtendedFileHandler.pattern = ${catalina.base}/logs/mylog
eu.abra.logging.ExtendedFileHandler.limit = 1000000000
eu.abra.logging.ExtendedFileHandler.count = 2
eu.abra.logging.ExtendedFileHandler.max_queue_size=500000
eu.abra.logging.ExtendedFileHandler.formatter = java.util.logging.SimpleFormatter
</code>


**max_queue_size** 
is a parameter that specifies the maximum length of the queue waiting to be processed by an asynchronous thread. If the maximum length is reached, then the thread requesting logging waits blocked until space is freed in the queue. If it is not set or is 0 then the length is unlimited. Be careful, here there is a risk that queue will consume all the available memory of the application and it will stop.

