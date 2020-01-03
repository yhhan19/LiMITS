rm *.class
javac -Xlint:unchecked -Xlint:deprecation MITS.java
java -Xmx8192M -Xss256M MITS