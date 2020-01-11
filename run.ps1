rm *.class
javac -Xlint:unchecked -Xlint:deprecation LIMITS.java
java -Xmx8192M -Xss256M LIMITS
