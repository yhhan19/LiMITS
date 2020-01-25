rm *.class
javac -Xlint:unchecked -Xlint:deprecation LIMITS.java
java -Xmx10G -Xss256M LIMITS
rm *.class