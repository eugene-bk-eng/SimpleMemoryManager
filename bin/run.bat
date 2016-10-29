set VM_OPTIONS=-Dlog4j.configurationFile=file:"../log4j.xml" -Dlog4j.configurationFile=file:"../log4j.xml" -XX:MaxDirectMemorySize=2G -XX:+UnlockDiagnosticVMOptions -XX:+HeapDumpOnOutOfMemoryError

C:\apps\java\jdk1.8.0_45\bin\java %VM_OPTIONS% -cp "../target/MemoryManager-1.0.1-SNAPSHOT-jar-with-dependencies.jar" com.ocean927.memory.examples.SimpleExample