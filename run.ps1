rm *.class
javac -Xlint:unchecked Evaluator.java
java -Xmx8192M -Xss256M Evaluator