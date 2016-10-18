cls
set classpath=

set JAVA_LIBS=C:\apps\Sun\SDK\jdk\jre\lib\ext

set LIBS=C:\projects\java\personal\MemoryManager\target\classes
set TIB_LIB=C:\apps\tibco\tibrv\8.3\lib\tibrvj.jar
set SQL_LIB=%JAVA_LIBS%\JSQLConnect.jar
set XML_LIB=%JAVA_LIBS%\xerces.jar;%JAVA_LIBS%\xmlparserv2.jar
set MAIL=%JAVA_LIBS%\activation.jar;%JAVA_LIBS%\mail.jar;%JAVA_LIBS%\mailapi.jar
set SSH_FACTORY=%JAVA_LIBS%\sshfactory.jar;%JAVA_LIBS%\sftp.jar
set APACHE_MATH=C:\projects\java\personal\libs\apache\math\3.3.2\commons-math3-3.2\commons-math3-3.2.jar
set JHICCUP=C:\projects\java\personal\libs\jHiccup.1.3.5\jHiccup\target\jHiccup.jar;C:\projects\java\personal\libs\jHiccup.1.3.5\jHiccup\target\lib\HdrHistogram-1.0.6.jar
set EA_LIB=C:\projects\java\personal\ocean927\target\classes
set JSCIENCE=C:\projects\java\personal\libs\jscience-4.3.1-bin\jscience-4.3\jscience.jar
set LOG4J_LIB=C:\Users\eugene\.m2\repository\log4j\log4j\1.2.17\log4j-1.2.17.jar;C:\Users\eugene\.m2\repository\org\apache\logging\log4j\log4j-api\2.5\log4j-api-2.5.jar;C:\Users\eugene\.m2\repository\org\apache\logging\log4j\log4j-core\2.5\log4j-core-2.5.jar
set LIB_JUNIT=C:\Users\eugene\.m2\repository\org\junit\4.11\junit-4.11.jar
set LIB_AVRO=C:\Users\eugene\.m2\repository\org\apache\avro\avro\1.8.1\avro-1.8.1.jar
set LIB_APACHE=C:\Users\eugene\.m2\repository\commons-codec\commons-codec\1.9\commons-codec-1.9.jar;C:\Users\eugene\.m2\repository\commons-lang\commons-lang\2.6\commons-lang-2.6.jar
set LIB_GUICE=C:\Users\eugene\.m2\repository\com\google\inject\guice\4.0\guice-4.0.jar

set classpath=%LIBS%;%TIB_LIB%;%XML_LIB%;%MAIL%;%SSH_FACTORY%;%SQL_LIB%;%EA_LIB%;%JHICCUP%;%APACHE_MATH%;%JSCIENCE%;%LOG4J_LIB%;%LIB_JUNIT%;%LIB_AVRO%;%LIB_APACHE%;%LIB_GUICE%

cls