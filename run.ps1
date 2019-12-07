rm *.class
javac -Xlint:unchecked Simplifier.java
java -Xmx1024M -Xss64M Simplifier