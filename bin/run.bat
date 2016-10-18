call set_classpath.bat

set VM_OPTIONS=-Xms250m -Xmx500m -server -XX:CompileThreshold=1 -XX:+UnlockDiagnosticVMOptions -XX:+HeapDumpOnOutOfMemoryError -XX:PrintAssemblyOptions=hsdis-print-bytes -XX:+DebugNonSafepoints -XX:MaxDirectMemorySize=2G -Dlog4j.configuration="file:C:\projects\java\personal\MemoryManager\log4j.properties" -Dlog4j.configurationFile="C:\projects\java\personal\MemoryManager\log4j2.xml" -Dlog4j.skipJansi=true 

C:\apps\java\jdk1.8.0_45\bin\java %VM_OPTIONS% com.ocean927.memory.test.TestMemoryMgr