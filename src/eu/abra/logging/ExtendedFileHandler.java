package eu.abra.logging;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.XMLFormatter;

public class ExtendedFileHandler extends Handler {

	private Queue<LogRecord> toLogQueue = new ConcurrentLinkedQueue<LogRecord>();
	private FileHandler commonFilehandler;
	private Map<String, FileHandler> fileHandlers = new ConcurrentHashMap<String, FileHandler>();
	private Writter writter = null;
	private boolean separateThreads = false;
	private String pattern;
	private long limit = 0;
	private int count = 1;
	private boolean append = false;;
	private boolean sysout = false;
	private static final  Object lock = new Object();


	private class Writter extends Thread {

		public Writter(String name) {
			super(name);
		}

		@Override
		public void run() {
			sysOut("ExtendedFileHandler RUN");
			boolean terminate = false;
			if (!separateThreads) {
				try {
					sysOut("ExtendedFileHandler create filehandler pattern"+pattern+" limit "+limit+" count "+count);
					commonFilehandler = new FileHandler(pattern, limit, count, append);
					commonFilehandler.setFilter(getFilter());
					commonFilehandler.setFormatter(getFormatter());
					sysOut("ExtendedFileHandler create filehandler2");
				} catch (Exception e) {
					sysOut("ExtendedFileHandler create failed");
					e.printStackTrace();
					commonFilehandler = null;
				}
			}
			sysOut("ExtendedFileHandler RUN2");
			while (true) {
				sysOut("ExtendedFileHandler RUNNING");
				while (!toLogQueue.isEmpty()) {
					sysOut("ExtendedFileHandler QUEUE not empty");
					LogRecord toLogRecord = toLogQueue.poll();
					if (toLogRecord != null) {
						FileHandler actualHandler = null;;
						if (!separateThreads) {
							actualHandler = commonFilehandler;
						} else {

						}
						if (actualHandler != null) {
							sysOut("ExtendedFileHandler publish");
							actualHandler.publish(toLogRecord);
						}
					}
				}
				if (terminate || isInterrupted()) {
					sysOut("ExtendedFileHandler finalizing");
					if (!separateThreads) {
						commonFilehandler.flush();
						commonFilehandler.close();
					} else {
						for (FileHandler fileHandler: fileHandlers.values()) {
							fileHandler.flush();
							fileHandler.close();
						}
					}
					sysOut("ExtendedFileHandler break");
					break;
				}
				synchronized (lock) {
					//Slep to save CPU resources
					try {
						sysOut("ExtendedFileHandler wait");
						lock.wait(60000);
					} catch (InterruptedException e) {
						//Iterrupted flag is cleared after InterruptedException raised
						//so we need to write all to files and quit this thread.
						//one more iterate and than break;
						//The queue should not be long, because every time it is added to the queue, the thread is woken up and immediately processes the font
						terminate = true;
						sysOut("ExtendedFileHandler terminated");
					}
				}
			}
			sysOut("ExtendedFileHandler STOP");
		}
	}


    private void configure() {
        LogManager manager = LogManager.getLogManager();

        String cname = getClass().getName();

        String val = manager.getProperty(cname + ".sysout");
        if (val == null) {
        	sysout = false;
        } else {
        	val = val.toLowerCase();
        	if (val.equals("true") || val.equals("1")) {
        		sysout = true;
        	} else if (val.equals("false") || val.equals("0")) {
        		sysout = false;
        	}
        }
        sysOut("ExtendedFileHandler append "+append);


        pattern = manager.getProperty(cname + ".pattern");
        if (pattern == null) {
        	pattern = "%h/java%u.log";
        }
        sysOut("ExtendedFileHandler pattern "+pattern);

        val = manager.getProperty(cname + ".limit");
        if (val == null) {
        	limit = 0;
        } else {
        	try {
        		limit = Long.parseLong(val.trim());
        	} catch (Exception ex) {
        		limit = 0;
        	}
        }
        if (limit < 0) {
            limit = 0;
        }
        sysOut("ExtendedFileHandler limit "+limit);

        val = manager.getProperty(cname + ".count");
        if (val == null) {
        	count = 1;
        } else {
        	try {
        		count = Integer.parseInt(val.trim());
        	} catch (Exception ex) {
        		count = 1;
        	}
        }

        if (count <= 0) {
            count = 1;
        }
        sysOut("ExtendedFileHandler count "+count);

        val = manager.getProperty(cname + ".append");
        if (val == null) {
            append = false;
        } else {
        	val = val.toLowerCase();
        	if (val.equals("true") || val.equals("1")) {
        		append = true;
        	} else if (val.equals("false") || val.equals("0")) {
        		append = false;
        	}
        }
        sysOut("ExtendedFileHandler append "+append);


        val = manager.getProperty(cname + ".level").toUpperCase();
        if ("OFF".equalsIgnoreCase(val)) {
        	setLevel(Level.OFF);
        } else if ("SEVERE".equalsIgnoreCase(val)) {
        	setLevel(Level.SEVERE);
        } else if ("WARNING".equalsIgnoreCase(val)) {
        	setLevel(Level.WARNING);
        } else if ("INFO".equalsIgnoreCase(val)) {
        	setLevel(Level.INFO);
        } else if ("CONFIG".equalsIgnoreCase(val)) {
        	setLevel(Level.CONFIG);
        } else if ("FINE".equalsIgnoreCase(val)) {
        	setLevel(Level.FINE);
        } else if ("FINER".equalsIgnoreCase(val)) {
        	setLevel(Level.FINER);
        } else if ("FINEST".equalsIgnoreCase(val)) {
        	setLevel(Level.FINEST);
        } else if ("ALL".equalsIgnoreCase(val)) {
        	setLevel(Level.ALL);
        }
        sysOut("ExtendedFileHandler level "+getLevel());

        setFilter(null);
        val = manager.getProperty(cname + ".filter");
        try {
            if (val != null) {
                @SuppressWarnings("deprecation")
                Object o = ClassLoader.getSystemClassLoader().loadClass(val).newInstance();
                setFilter((Filter) o);
            }
        } catch (Exception ex) {
        }

        setFormatter(new XMLFormatter());
        val = manager.getProperty(cname + ".formatter");
        try {
            if (val != null) {
                @SuppressWarnings("deprecation")
                Object o = ClassLoader.getSystemClassLoader().loadClass(val).newInstance();
                setFormatter((Formatter) o);
            }
        } catch (Exception ex) {
        }
    }

	public ExtendedFileHandler() throws IOException, SecurityException {
		sysOut("ExtendedFileHandler C0");
		configure();
		startWritter();
	}

	private void startWritter() {
		writter = new Writter("ExtendedFileHandler - async log writter");
		writter.setDaemon(true);
		writter.start();
		sysOut("ExtendedFileHandler Writter started");
	}

	public ExtendedFileHandler(String pattern) throws IOException, SecurityException {
		sysOut("ExtendedFileHandler C1");
		configure();
		this.pattern = pattern;
		startWritter();
	}

	public ExtendedFileHandler(String pattern, boolean append) throws IOException, SecurityException {
		sysOut("ExtendedFileHandler C2");
		configure();
		this.pattern = pattern;
		this.append = append;
		startWritter();
	}

	public ExtendedFileHandler(String pattern, int limit, int count) {
		sysOut("ExtendedFileHandler C3");
		configure();
		this.pattern = pattern;
		this.limit = limit;
		this.count = count;
		startWritter();
	}

	public ExtendedFileHandler(String pattern, int limit, int count, boolean append) {
		sysOut("ExtendedFileHandler C4");
		configure();
		this.pattern = pattern;
		this.limit = limit;
		this.count = count;
		this.append = append;
		startWritter();
	}

	public ExtendedFileHandler(String pattern, long limit, int count, boolean append) {
		sysOut("ExtendedFileHandler C5");
		configure();
		this.pattern = pattern;
		this.limit = limit;
		this.count = count;
		this.append = append;
		startWritter();
	}

//	public ExtendedFileHandler(String pattern, long limit, int count, boolean append, boolean separateEachThread) {
//		sysOut("ExtendedFileHandler C6");
//		configure();
//		this.pattern = pattern;
//		this.limit = limit;
//		this.count = count;
//		this.append = append;
//		this.separateThreads = separateEachThread;
//		startWritter();
//	}

	@Override
	public synchronized void publish(LogRecord record) {
		sysOut("ExtendedFileHandler Log writter required");
		if (writter.isAlive() && !writter.isInterrupted()) {
			if (toLogQueue.offer(record)) {
				sysOut("ExtendedFileHandler Log writter added");
				flush();
			} else {
				sysOut("ExtendedFileHandler Log writter NOTadded");
				//handle not added record
			}
		}
	}

	private void sysOut(String text) {
		if (sysout) {
			System.out.println(new Date() + " " + text);
		}
	}

	@Override
	public void flush() {
		synchronized (lock ) {
			lock.notifyAll();
		}
	}

	@Override
	public void close() throws SecurityException {
		sysOut("ExtendedFileHandler CLOSE");
		writter.interrupt();
	}

}
