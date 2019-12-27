rm *.class
javac -Xlint:unchecked -Xlint:deprecation FileScanner.java
java -Xmx8192M -Xss256M FileScanner