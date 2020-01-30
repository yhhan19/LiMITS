rm *.class
javac -Xlint:unchecked -Xlint:deprecation LIMITS.java
java -Xmx11G -Xss256M LIMITS
rm *.class