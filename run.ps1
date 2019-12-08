rm *.class
javac -Xlint:unchecked Simplifier.java
java -Xmx4096M -Xss128M Simplifier