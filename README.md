# ExtendedFileHandler
Asynchronous variant of JAVA logging FileHandler

Eclipse JAVA project

## Configuration
Configuration is same as FileHandler configuration
In Tomcal conf/logging.properties  add handler eu.abra.logging.ExtendedFileHandler to end of section handlers=

Then configure handler

**max_queue_size** 
is a parameter that specifies the maximum length of the queue waiting to be processed by an asynchronous thread. If the maximum length is reached, then the thread requesting logging waits blocked until space is freed in the queue. If it is not set or is 0 then the length is unlimited. Be careful, here there is a risk that queue will consume all the available memory of the application and it will stop.

<code>
eu.abra.logging.ExtendedFileHandler.level = FINEST
eu.abra.logging.ExtendedFileHandler.encoding = UTF-8
eu.abra.logging.ExtendedFileHandler.pattern = ${catalina.base}/logs/mylog
eu.abra.logging.ExtendedFileHandler.limit = 1000000000
eu.abra.logging.ExtendedFileHandler.count = 2
eu.abra.logging.ExtendedFileHandler.max_queue_size=500000
eu.abra.logging.ExtendedFileHandler.formatter = java.util.logging.SimpleFormatter
</code>
