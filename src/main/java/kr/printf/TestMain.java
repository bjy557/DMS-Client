package kr.printf;

/**
 * Created by nexusz99 on 9/19/16.
 */
public class TestMain{


    public static void main(String[] args) {

        MqttTester tester = new MqttTester();
        String runMode = System.getenv("RUN_MODE");
        int testCase = Integer.parseInt(System.getenv("TEST_CASE"));
        int qos = Integer.parseInt(System.getenv("QOS"));

        int thread = Integer.parseInt(System.getenv("THREAD"));

        tester.runTester(runMode, testCase, qos, thread);
    }

}
