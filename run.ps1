rm *.class
javac -Xlint:unchecked Simplifier.java
java -Xmx8192M -Xss256M Simplifier