package fmi.spo.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

public class EulerCallable implements Callable<BigDecimal> {
    private static final String THREAD_STARTED_MSG_TEMPLATE = "Thread-%s started.";
    private static final String THREAD_STOPPED_MSG_TEMPLATE = "Thread-%s stopped.";
    private static final String THREAD_EXEC_TIME = "Thread-%s execution time was (millis): %d";
    private int start;
    private int end;
    private int precision;
    private static final List<BigInteger> factorialsList = new Vector<>(Collections.singletonList(BigInteger.ONE));

    public EulerCallable(int start, int end, int precision) {
        this.start = start;
        this.end = end;
        this.precision = precision;
    }

    public BigDecimal call() {
        String threadName = Thread.currentThread().getName();
        long startMillis = System.currentTimeMillis();
        System.out.println(String.format(THREAD_STARTED_MSG_TEMPLATE, threadName));
        BigDecimal numerator = getNumerator(start);
        BigDecimal denominator = getDenominator(start);
        BigDecimal partialSum = numerator.divide(denominator, precision, RoundingMode.HALF_UP);
        for (int i = start + 1; i < end; i++) {
            numerator = getNumerator(i);
            denominator = denominator.multiply(BigDecimal.valueOf(2 * i * (2 * i + 1)));
            partialSum = partialSum.add(numerator.divide(denominator, precision, RoundingMode.HALF_UP));
        }
        long endMillis = System.currentTimeMillis();
        System.out.println(String.format(THREAD_EXEC_TIME, threadName,endMillis-startMillis));
        System.out.println(String.format(THREAD_STOPPED_MSG_TEMPLATE, threadName));
        return partialSum;
    }

    private static BigDecimal getNumerator(int k) {
        return BigDecimal.valueOf(3 - 4 * k * k);
    }

    private static BigDecimal getDenominator(int k) {
        return new BigDecimal(factorial(2 * k + 1));
    }

    private static BigInteger factorial(int cur) {
        synchronized (factorialsList) {
            if (factorialsList.size() <= cur) {
                for (int i = factorialsList.size(); i <= cur; i++) {
                    BigInteger lastFactorial = factorialsList.get(factorialsList.size() - 1);
                    factorialsList.add(i, lastFactorial.multiply(BigInteger.valueOf(i)));
                }
            }
            return factorialsList.get(cur);
        }
    }
}